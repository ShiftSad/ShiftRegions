package codes.shiftmc.regions.repository.impl;

import codes.shiftmc.regions.model.UserData;
import codes.shiftmc.regions.repository.UserRepository;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class MySQLUserRepository implements UserRepository {

    private final MySQLPool client;

    public MySQLUserRepository(MySQLPool client) {
        this.client = client;
    }

    @Override
    public Mono<Void> createTable() {
        String query = """
            CREATE TABLE IF NOT EXISTS users (
                id CHAR(36) PRIMARY KEY,
                block_claims INT NOT NULL DEFAULT 0,
                max_block_claims INT NOT NULL DEFAULT 0
            );
        """;

        return Mono.create(sink ->
                client.query(query).execute(ar -> {
                    if (ar.succeeded()) sink.success();
                    else sink.error(ar.cause());

                })
        );
    }

    @Override
    public Mono<UserData> findByUUID(UUID uuid) {
        String query = "SELECT * FROM users WHERE id = ?";

        return Mono.create(sink ->
                client.preparedQuery(query).execute(Tuple.of(uuid.toString()), ar -> {
                    if (ar.failed()) {
                        sink.error(ar.cause());
                        return;
                    }

                    RowSet<Row> rows = ar.result();
                    if (rows.size() == 0) {
                        // User not found -> emit null (or you could emit Mono.empty() depending on your needs)
                        sink.success(null);
                        return;
                    }

                    Row row = rows.iterator().next();
                    UserData user = mapRowToUser(row);
                    sink.success(user);
                })
        );
    }

    @Override
    public Mono<UserData> save(UserData userData) {
        String query = """
            INSERT INTO users (id, block_claims, max_block_claims)
            VALUES (?, ?, ?)
        """;

        // Insert the new user
        return Mono.create(sink ->
                client.preparedQuery(query)
                        .execute(
                                Tuple.of(
                                        userData.id().toString(),
                                        userData.blockClaims(),
                                        userData.maxBlockClaims()
                                ),
                                ar -> {
                                    if (ar.succeeded()) {
                                        sink.success(userData);
                                    } else {
                                        sink.error(ar.cause());
                                    }
                                }
                        )
        );
    }

    @Override
    public Mono<UserData> updateUser(UserData userData) {
        String query = """
            UPDATE users
            SET block_claims = ?, max_block_claims = ?
            WHERE id = ?
        """;

        // Update the user’s record
        return Mono.create(sink ->
                client.preparedQuery(query)
                        .execute(
                                Tuple.of(
                                        userData.blockClaims(),
                                        userData.maxBlockClaims(),
                                        userData.id().toString()
                                ),
                                ar -> {
                                    if (ar.succeeded()) {
                                        sink.success(userData);
                                    } else {
                                        sink.error(ar.cause());
                                    }
                                }
                        )
        );
    }

    /**
     * Utility method to map a Row to a UserData object.
     */
    private UserData mapRowToUser(Row row) {
        UUID id = UUID.fromString(row.getString("id"));
        int blockClaims = row.getInteger("block_claims");
        int maxBlockClaims = row.getInteger("max_block_claims");

        return new UserData(id, blockClaims, maxBlockClaims);
    }
}
