package codes.shiftmc.regions.repository.impl;

import codes.shiftmc.regions.math.BlockVector;
import codes.shiftmc.regions.model.Region;
import codes.shiftmc.regions.repository.RegionRepository;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import it.unimi.dsi.fastutil.Pair;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                flags INT NOT NULL DEFAULT 0
            );
        """;

        // Example table for region members
        String createMembersTable = """
            CREATE TABLE IF NOT EXISTS region_members (
                region_id CHAR(36) NOT NULL,
                member_id CHAR(36) NOT NULL,
                flags INT NOT NULL,
                PRIMARY KEY (region_id),
                FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
            );
        """;

        return Mono.create(sink ->
                client.query(createRegionsTable).execute(ar -> {
                    if (ar.failed()) {
                        sink.error(ar.cause());
                        return;
                    }
                    client.query(createMembersTable).execute(ar2 -> {
                        if (ar2.failed()) sink.error(ar2.cause());
                        else sink.success();
                    });
                })
        );
    }

    @Override
    public Flux<Region> findAll() {
        String selectAllRegions = "SELECT * FROM regions";
        return Mono.<RowSet<Row>>create(sink ->
                        client.query(selectAllRegions).execute(ar -> {
                            if (ar.failed()) {
                                sink.error(ar.cause());
                            } else {
                                sink.success(ar.result());
                            }
                        })
                )
                .flatMapMany(Flux::fromIterable)
                .flatMap(this::fetchRegion);
    }

    @Override
    public Mono<Void> save(Region region) {
        String insertRegion = """
            INSERT INTO regions (id, min_x, min_y, min_z, max_x, max_y, max_z, owner, flags)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        Mono<Void> insertRegionMono = Mono.create(sink -> {
            client.preparedQuery(insertRegion)
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

        return insertRegionMono
                .then(saveRegionMembers(region.id(), region.members()))
                .then();
    }

    @Override
    public Mono<Void> delete(UUID uuid) {
        String deleteRegionQuery = "DELETE FROM regions WHERE id = ?";
        String deleteMembersQuery = "DELETE FROM region_members WHERE region_id = ?";

        return Mono.create(sink -> {
            client.preparedQuery(deleteRegionQuery)
                    .execute(Tuple.of(uuid.toString()), ar -> {
                        if (ar.succeeded()) {
                            sink.success();
                        } else {
                            sink.error(ar.cause());
                        }
                    });
        }).then(Mono.create(sink -> {
            client.preparedQuery(deleteMembersQuery)
                    .execute(Tuple.of(uuid.toString()), ar -> {
                        if (ar.succeeded()) {
                            sink.success();
                        } else {
                            sink.error(ar.cause());
                        }
                    });
        }));
    }

    @Override
    public Mono<Void> saveAll(List<Region> regions) {
        if (regions.isEmpty()) {
            return Mono.empty(); // no regions => no inserts
        }

        StringBuilder queryRegion = new StringBuilder("INSERT INTO regions (id, min_x, min_y, min_z, max_x, max_y, max_z, owner, flags) VALUES ");
        List<Object> allParameters = new ArrayList<>();
        for (int i = 0; i < regions.size(); i++) {
            queryRegion.append("(?, ?, ?, ?, ?, ?, ?, ?, ?)");
            if (i < regions.size() - 1) {
                queryRegion.append(", ");
            }
        }

        for (Region region : regions) {
            allParameters.add(region.id().toString());
            allParameters.add(region.min().x());
            allParameters.add(region.min().y());
            allParameters.add(region.min().z());
            allParameters.add(region.max().x());
            allParameters.add(region.max().y());
            allParameters.add(region.max().z());
            allParameters.add(region.owner().toString());
            allParameters.add(region.flags());
        }

        // Insert all regions
        Mono<Void> insertRegionsMono = Mono.create(sink -> {
            client.preparedQuery(queryRegion.toString())
                    .execute(Tuple.wrap(allParameters.toArray()), ar -> {
                        if (ar.succeeded()) {
                            sink.success();
                        } else {
                            sink.error(ar.cause());
                        }
                    });
        });

        // Insert all region members
        List<Tuple> memberTuples = new ArrayList<>();
        for (Region region : regions) {
            if (region.members().isEmpty()) continue;

            for (Pair<UUID, Integer> member : region.members()) {
                memberTuples.add(Tuple.of(region.id().toString(), member.first().toString(), member.second()));
            }
        }

        if (memberTuples.isEmpty()) return insertRegionsMono;

        StringBuilder queryMember = new StringBuilder(
                "INSERT INTO region_members (region_id, member_id, flags) VALUES "
        );
        for (int i = 0; i < memberTuples.size(); i++) {
            queryMember.append("(?, ?, ?)");
            if (i < memberTuples.size() - 1) {
                queryMember.append(", ");
            }
        }

        Mono<Void> insertMembersMono = Mono.create(sink -> {
            client.preparedQuery(queryMember.toString())
                    .execute(Tuple.wrap(memberTuples.toArray()), ar -> {
                        if (ar.succeeded()) {
                            sink.success();
                        } else {
                            sink.error(ar.cause());
                        }
                    });
        });

        return insertRegionsMono
                .then(insertMembersMono);
    }

    /**
     * Given a Row from 'regions', fetch members and return a Mono<Region>.
     */
    private Mono<Region> fetchRegion(Row row) {
        return Mono.create(sink -> fetchMembersAndCompleteRegion(row, sink));
    }

    /**
     * Insert (region_id, member_id, flags) into region_members for each member.
     */
    private Mono<Void> saveRegionMembers(UUID regionId, List<Pair<UUID, Integer>> members) {
        if (members.isEmpty()) {
            return Mono.empty(); // no members => no inserts
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
                .then(); // completes when all inserts finish
    }

    /**
     * Fetch all members for a given region row, build a Region object, and complete the Mono sink.
     */
    private void fetchMembersAndCompleteRegion(Row regionRow, MonoSink<Region> sink) {
        UUID regionId = UUID.fromString(regionRow.getString("id"));
        String memberQuery = "SELECT member_id, flags FROM region_members WHERE region_id = ?";

        client.preparedQuery(memberQuery)
                .execute(Tuple.of(regionId.toString()), memberAr -> {
                    if (memberAr.failed()) {
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
    }

    /**
     * Convert a Row into a Region object, given its row data and a member list.
     */
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