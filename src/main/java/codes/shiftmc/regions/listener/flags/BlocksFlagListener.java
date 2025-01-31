package codes.shiftmc.regions.listener.flags;

import codes.shiftmc.regions.ShiftRegions;
import codes.shiftmc.regions.listener.ShiftListener;
import codes.shiftmc.regions.model.Flag;
import codes.shiftmc.regions.service.RegionService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlocksFlagListener extends ShiftListener {

    private final RegionService regionService;

    public BlocksFlagListener(ShiftRegions plugin, RegionService regionService) {
        super(plugin);
        this.regionService = regionService;
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        var player = event.getPlayer();
        var region = regionService.findByLocation(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());
        if (region.isPresent()) {
            var blockPlaceFlag = (region.get().hasFlag(Flag.BLOCK_PLACE));
            if (!blockPlaceFlag) {
                region.get().members().stream().filter(pair -> pair.first().equals(player.getUniqueId())).findFirst().ifPresent(pair -> {
                    if (!Flag.hasFlag(pair.second(), Flag.BLOCK_PLACE)) {
                        event.setCancelled(true);
                    }
                });
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        var player = event.getPlayer();
        var region = regionService.findByLocation(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());
        if (region.isPresent()) {
            var blockBreakFlag = (region.get().hasFlag(Flag.BLOCK_BREAK));
            if (!blockBreakFlag) {
                region.get().members().stream().filter(pair -> pair.first().equals(player.getUniqueId())).findFirst().ifPresent(pair -> {
                    if (!Flag.hasFlag(pair.second(), Flag.BLOCK_BREAK)) {
                        event.setCancelled(true);
                    }
                });
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent event) {
        var player = event.getPlayer();
        if (event.getClickedBlock() == null) return;
        var region = regionService.findByLocation(event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ());
        if (region.isPresent()) {
            var interactFlag = (region.get().hasFlag(Flag.INTERACT));
            if (!interactFlag) {
                region.get().members().stream().filter(pair -> pair.first().equals(player.getUniqueId())).findFirst().ifPresent(pair -> {
                    if (!Flag.hasFlag(pair.second(), Flag.INTERACT)) {
                        event.setCancelled(true);
                    }
                });
                event.setCancelled(true);
            }
        }
    }
}
