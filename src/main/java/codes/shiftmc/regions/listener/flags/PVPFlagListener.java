package codes.shiftmc.regions.listener.flags;

import codes.shiftmc.regions.ShiftRegions;
import codes.shiftmc.regions.listener.ShiftListener;
import codes.shiftmc.regions.model.Flag;
import codes.shiftmc.regions.service.RegionService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PVPFlagListener extends ShiftListener {

    private final RegionService regionService;

    public PVPFlagListener(ShiftRegions plugin, RegionService regionService) {
        super(plugin);
        this.regionService = regionService;
    }

    @EventHandler
    public void onPlayerVersusPlayer(EntityDamageByEntityEvent event) {
        var attacker = event.getDamager();
        var defender = event.getEntity();

        if (attacker instanceof Player && defender instanceof Player) {
            var region = regionService.findByLocation(defender.getLocation().getBlockX(), defender.getLocation().getBlockY(), defender.getLocation().getBlockZ());
            if (region.isPresent()) {
                var pvpFlag = (region.get().hasFlag(Flag.PVP));
                if (!pvpFlag) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
