package codes.shiftmc.regions;

import codes.shiftmc.regions.math.Cuboid;
import codes.shiftmc.regions.math.Line;
import codes.shiftmc.regions.math.MathUtils;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShiftRegions extends JavaPlugin implements Listener {

    Location point1;
    Location point2;
    Cuboid cuboid;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onMessage(PlayerChatEvent event) {
        if (event.getMessage().equals("point1")) {
            if (cuboid != null) {
                cuboid.setMax(MathUtils.toVector(event.getPlayer().getLocation()));
                return;
            }
            point1 = event.getPlayer().getLocation();
            event.getPlayer().sendMessage("Point 1 set to " + point1);
        }

        else if (event.getMessage().equals("point2")) {
            if (cuboid != null) {
                cuboid.setMin(MathUtils.toVector(event.getPlayer().getLocation()));
                return;
            }
            point2 = event.getPlayer().getLocation();
            event.getPlayer().sendMessage("Point 2 set to " + point2);
        }

        else if (event.getMessage().equals("line")) {
            cuboid = new Cuboid(Pair.of(MathUtils.toVector(point1), MathUtils.toVector(point2)));
            cuboid.render(point1.getWorld());
        }
    }
}
