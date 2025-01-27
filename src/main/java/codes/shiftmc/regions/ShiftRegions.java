package codes.shiftmc.regions;

import codes.shiftmc.regions.commands.RegionCommand;
import codes.shiftmc.regions.configuration.DataSource;
import codes.shiftmc.regions.listener.PlayerListener;
import codes.shiftmc.regions.repository.RegionRepository;
import codes.shiftmc.regions.repository.UserRepository;
import codes.shiftmc.regions.repository.impl.MySQLRegionRepository;
import codes.shiftmc.regions.repository.impl.MySQLUserRepository;
import codes.shiftmc.regions.service.RegionService;
import codes.shiftmc.regions.service.UserService;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

// By now, I should probably know what I'm doing
@SuppressWarnings("UnstableApiUsage")
public final class ShiftRegions extends JavaPlugin {

    private RegionRepository regionRepository;
    private UserRepository userRepository;

    private RegionService regionService;
    private UserService userService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfiguration();

        regionService = new RegionService(regionRepository);
        userService = new UserService(userRepository);

        // I know the correct way would be the bootloader, but I can't be bothered
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            var region = new RegionCommand(regionService, userService, this);
            commands.registrar().register(region.root);
        });

        // Register Player Listener
        new PlayerListener(this, userService);
    }

    private void loadConfiguration() {
        var config = getConfig();
        DataSource dataSource;

        if (!config.contains("settings.datasource")) {
            dataSource = new DataSource("localhost", 3306, "test", "root", "password", true, 10, 100, 5, 1000, 10);
            config.createSection("settings.datasource", dataSource.serialize());
            saveConfig();
        } else dataSource = DataSource.deserialize(config.getConfigurationSection("settings.datasource").getValues(false));

        DataSource.getClient(dataSource);
        regionRepository = new MySQLRegionRepository(DataSource.getClient());
        userRepository = new MySQLUserRepository(DataSource.getClient());
    }
}
