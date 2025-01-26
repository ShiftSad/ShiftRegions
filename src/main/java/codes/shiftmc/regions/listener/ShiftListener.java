package codes.shiftmc.regions.listener;

import codes.shiftmc.regions.ShiftRegions;
import org.bukkit.event.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ShiftListener implements Listener {

    protected static final Logger log = LoggerFactory.getLogger(ShiftListener.class);

    public ShiftListener(ShiftRegions plugin) {
        log.info("Registering listener: {}", getClass().getSimpleName());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}
