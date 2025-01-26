package codes.shiftmc.regions;

import codes.shiftmc.regions.configuration.DataSource;
import codes.shiftmc.regions.repository.RegionRepository;
import codes.shiftmc.regions.repository.UserRepository;
import codes.shiftmc.regions.repository.impl.MySQLRegionRepository;
import codes.shiftmc.regions.repository.impl.MySQLUserRepository;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShiftRegions extends JavaPlugin {

    private RegionRepository regionRepository;
    private UserRepository userRepository;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfiguration();

        regionRepository.createTable().subscribe();
        userRepository.createTable().subscribe();
    }

    private void loadConfiguration() {
        var config = getConfig();
        DataSource dataSource;

        if (!config.contains("settings.datasource")) {
            dataSource = new DataSource("localhost", 3306, "test", "root", "password", true, 10, 100, 5, 1000, 10);
            config.createSection("settings.datasource", dataSource.serialize());
            saveConfig();
        } else dataSource = DataSource.deserialize(config.getConfigurationSection("settings.datasource").getValues(false));

        regionRepository = new MySQLRegionRepository(DataSource.getClient(dataSource));
        userRepository = new MySQLUserRepository(DataSource.getClient(dataSource));
    }
}
