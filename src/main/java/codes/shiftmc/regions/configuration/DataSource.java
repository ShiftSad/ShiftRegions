package codes.shiftmc.regions.configuration;

import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("DataSource")
public record DataSource(
        String hostname,
        int port,
        String database,
        String username,
        String password,
        boolean cachePreparedStatement,
        int preparedStatementCacheMaxSize,
        int preparedStatementCacheSqlLimit,
        int reconnectAttempts,
        int reconnectInterval,
        int maxPoolSize
) implements ConfigurationSerializable {

    private static MySQLPool client;

    @Override
    public @NotNull Map<String, Object> serialize() {
        final Map<String, Object> map = new HashMap<>();
        map.put("hostname", hostname);
        map.put("port", port);
        map.put("database", database);
        map.put("username", username);
        map.put("password", password);
        map.put("cachePreparedStatement", cachePreparedStatement);
        map.put("preparedStatementCacheMaxSize", preparedStatementCacheMaxSize);
        map.put("preparedStatementCacheSqlLimit", preparedStatementCacheSqlLimit);
        map.put("reconnectAttempts", reconnectAttempts);
        map.put("reconnectInterval", reconnectInterval);
        map.put("maxPoolSize", maxPoolSize);
        return map;
    }

    public static DataSource deserialize(final Map<String, Object> map) {
        return new DataSource(
                (String) map.get("hostname"),
                (int) map.get("port"),
                (String) map.get("database"),
                (String) map.get("username"),
                (String) map.get("password"),
                (boolean) map.get("cachePreparedStatement"),
                (int) map.get("preparedStatementCacheMaxSize"),
                (int) map.get("preparedStatementCacheSqlLimit"),
                (int) map.get("reconnectAttempts"),
                (int) map.get("reconnectInterval"),
                (int) map.get("maxPoolSize")
        );
    }

    public static MySQLPool getClient() {
        return client;
    }

    public static MySQLPool getClient(DataSource source) {
        setClient(source);
        return client;
    }

    public static void setClient(DataSource source) {
        if (client != null) client.close();
        var connectOptions = new MySQLConnectOptions()
                .setPort(source.port())
                .setHost(source.hostname())
                .setDatabase(source.database())
                .setUser(source.username())
                .setPassword(source.password())
                .setCachePreparedStatements(source.cachePreparedStatement())
                .setPreparedStatementCacheMaxSize(source.preparedStatementCacheMaxSize())
                .setPreparedStatementCacheSqlLimit(source.preparedStatementCacheSqlLimit())
                .setReconnectAttempts(source.reconnectAttempts())
                .setReconnectInterval(source.reconnectInterval());

        var poolOptions = new PoolOptions()
                .setMaxSize(source.maxPoolSize);

        client = MySQLPool.pool(connectOptions, poolOptions);
    }
}