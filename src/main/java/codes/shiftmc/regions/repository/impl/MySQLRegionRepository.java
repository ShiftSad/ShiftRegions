package codes.shiftmc.regions.repository.impl;

import codes.shiftmc.regions.math.BlockVector;
import codes.shiftmc.regions.model.Region;
import codes.shiftmc.regions.repository.RegionRepository;
import it.unimi.dsi.fastutil.Pair;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

public class MySQLRegionRepository implements RegionRepository {

    private final MySQLPool client;

    public MySQLRegionRepository(MySQLPool client) {
        this.client = client;
    }

    @Override
    public Mono<Void> createTable() {
        String createRegionsTable = """
        CREATE TABLE IF NOT EXISTS regions (
            id CHAR(36) PRIMARY KEY,
            min_x INT NOT NULL,
            min_y INT NOT NULL,
            min_z INT NOT NULL,
            max_x INT NOT NULL,
            max_y INT NOT NULL,
            max_z INT NOT NULL,
            owner CHAR(36) NOT NULL,
            flags INT NOT NULL DEFAULT 0,
            UNIQUE (owner)
        );
    """;

        String createMembersTable = """
        CREATE TABLE IF NOT EXISTS members (
            region_id CHAR(36) NOT NULL,
            member_id CHAR(36) NOT NULL,
            value INT NOT NULL,
            PRIMARY KEY (region_id, member_id),
            FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
        );
    """;

        return Mono.create(sink ->
                client.query(createRegionsTable).execute(ar -> {
                    if (!ar.succeeded()) {
                        sink.error(ar.cause());
                        return;
                    }
                    client.query(createMembersTable).execute(ar2 -> {
                        if (ar2.succeeded()) sink.success();
                        else sink.error(ar2.cause());
                    });
                })
        );
    }

    @Override
    public Mono<Region> findByUUID(UUID uuid) {
        return findRegionByField("id", uuid.toString());
    }

    @Override
    public Mono<Region> findByOwner(UUID uuid) {
        return findRegionByField("owner", uuid.toString());
    }

    @Override
    public Mono<Region> save(Region region) {
        String regionQuery = """
        INSERT INTO regions (id, min_x, min_y, min_z, max_x, max_y, max_z, owner, flags)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        Mono<Void> insertRegionMono = Mono.create(sink -> {
            client.preparedQuery(regionQuery)
                    .execute(
                            Tuple.of(
                                    region.id().toString(),
                                    region.min().x(),
                                    region.min().y(),
                                    region.min().z(),
                                    region.max().x(),
                                    region.max().y(),
                                    region.max().z(),
                                    region.owner().toString(),
                                    region.flags()
                            ),
                            ar -> {
                                if (ar.succeeded()) {
                                    sink.success();
                                } else {
                                    sink.error(ar.cause());
                                }
                            }
                    );
        });

        // First insert the region, then insert members, then return region
        return insertRegionMono
                .then(saveRegionMembers(region.id(), region.members()))
                .thenReturn(region);
    }

    @Override
    public Mono<Region> updateRegion(Region region) {
        String regionQuery = """
        UPDATE regions
        SET min_x = ?, min_y = ?, min_z = ?, max_x = ?, max_y = ?, max_z = ?, owner = ?, flags = ?
        WHERE id = ?
    """;

        Mono<Void> updateRegionMono = Mono.create(sink -> {
            client.preparedQuery(regionQuery)
                    .execute(
                            Tuple.of(
                                    region.min().x(),
                                    region.min().y(),
                                    region.min().z(),
                                    region.max().x(),
                                    region.max().y(),
                                    region.max().z(),
                                    region.owner().toString(),
                                    region.flags(),
                                    region.id().toString()
                            ),
                            ar -> {
                                if (ar.succeeded()) {
                                    sink.success();
                                } else {
                                    sink.error(ar.cause());
                                }
                            }
                    );
        });

        // First update the region, then delete members, then re-insert new members, then return region
        return updateRegionMono
                .then(deleteRegionMembers(region.id()))
                .then(saveRegionMembers(region.id(), region.members()))
                .thenReturn(region);
    }

    private Mono<Void> saveRegionMembers(UUID regionId, List<Pair<UUID, Integer>> members) {
        if (members.isEmpty()) {
            // If no members, return an already completed Mono
            return Mono.empty();
        }

        String memberQuery = """
        INSERT INTO region_members (region_id, member_id, flags)
        VALUES (?, ?, ?)
    """;

        return Flux.fromIterable(members)
                .flatMap(member -> Mono.create(sink -> {
                    client.preparedQuery(memberQuery)
                            .execute(
                                    Tuple.of(
                                            regionId.toString(),
                                            member.first().toString(),
                                            member.second()
                                    ),
                                    ar -> {
                                        if (ar.succeeded()) sink.success();
                                        else sink.error(ar.cause());
                                    }
                            );
                }))
                .then();
    }

    private Mono<Void> deleteRegionMembers(UUID regionId) {
        String deleteMembersQuery = "DELETE FROM region_members WHERE region_id = ?";

        return Mono.create(sink -> {
            client.preparedQuery(deleteMembersQuery)
                    .execute(Tuple.of(regionId.toString()), ar -> {
                        if (ar.succeeded()) sink.success();
                        else sink.error(ar.cause());
                    });
        });
    }

    private Mono<Region> findRegionByField(String fieldName, String value) {
        String regionQuery = "SELECT * FROM regions WHERE " + fieldName + " = ?";
        String memberQuery = "SELECT member_id, flags FROM region_members WHERE region_id = ?";

        return Mono.create(sink ->
                client.preparedQuery(regionQuery).execute(Tuple.of(value), ar -> {
                    if (!ar.succeeded()) {
                        sink.error(ar.cause());
                        return;
                    }

                    RowSet<Row> rows = ar.result();
                    if (rows.size() == 0) {
                        sink.success(null);
                        return;
                    }

                    Row regionRow = rows.iterator().next();
                    UUID regionId = UUID.fromString(regionRow.getString("id"));

                    // Now fetch all members for this region
                    client.preparedQuery(memberQuery)
                            .execute(Tuple.of(regionId.toString()), memberAr -> {
                                if (!memberAr.succeeded()) {
                                    sink.error(memberAr.cause());
                                    return;
                                }

                                List<Pair<UUID, Integer>> members = new ArrayList<>();
                                for (Row memberRow : memberAr.result()) {
                                    UUID memberId = UUID.fromString(memberRow.getString("member_id"));
                                    int flags = memberRow.getInteger("flags");
                                    members.add(Pair.of(memberId, flags));
                                }

                                Region region = mapRowToRegion(regionRow, members);
                                sink.success(region);
                            });
                })
        );
    }

    private Region mapRowToRegion(Row row, List<Pair<UUID, Integer>> members) {
        UUID id = UUID.fromString(row.getString("id"));
        BlockVector min = new BlockVector(
                row.getInteger("min_x"),
                row.getInteger("min_y"),
                row.getInteger("min_z")
        );
        BlockVector max = new BlockVector(
                row.getInteger("max_x"),
                row.getInteger("max_y"),
                row.getInteger("max_z")
        );
        UUID owner = UUID.fromString(row.getString("owner"));
        int flags = row.getInteger("flags");
        return new Region(id, min, max, owner, members, flags);
    }
}
