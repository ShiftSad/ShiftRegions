package codes.shiftmc.regions.listener;

import codes.shiftmc.regions.ShiftRegions;
import codes.shiftmc.regions.math.BlockVector;
import codes.shiftmc.regions.math.Cuboid;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.joml.Vector3d;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerListener extends ShiftListener {

    private static final Cache<UUID, Cuboid> selectionCache = Caffeine.newBuilder()
            .maximumSize(10)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .removalListener((uuid, cuboid, cause) -> {
                if (cuboid instanceof Cuboid c) c.dispose();
            })
            .build();

    public static void clearSelection(UUID uuid) {
        var cuboid = selectionCache.getIfPresent(uuid);
        if (cuboid != null) {
            cuboid.dispose();
            selectionCache.invalidate(uuid);
        }
    }

    public PlayerListener(ShiftRegions plugin) {
        super(plugin);
    }

    /**
     * Handles the player interaction event
     * allowing the player to select the region
     */
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var action = event.getAction();
        var lookingAt = player.getTargetBlock(null, 5).getLocation();
        var item = player.getInventory().getItemInMainHand();

        var ipdc = item.getItemMeta().getPersistentDataContainer();
        var ppdc = player.getPersistentDataContainer();
        if (!ipdc.has(new NamespacedKey("regions", "region_creator"), PersistentDataType.BOOLEAN)) return;

        String key = "";
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> key = "pos1";
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> key = "pos2";
        }
        if (key.isEmpty()) return;
        ppdc.set(
                new NamespacedKey("regions", key),
                PersistentDataType.INTEGER_ARRAY,
                new int[]{lookingAt.getBlockX(), lookingAt.getBlockY(), lookingAt.getBlockZ()}
        );

        var cuboid = selectionCache.getIfPresent(player.getUniqueId());
        // If null, check if we have both positions
        if (cuboid == null) {
            var pos1 = ppdc.get(new NamespacedKey("regions", "pos1"), PersistentDataType.INTEGER_ARRAY);
            var pos2 = ppdc.get(new NamespacedKey("regions", "pos2"), PersistentDataType.INTEGER_ARRAY);
            if (pos1 != null && pos2 != null) {
                cuboid = new Cuboid(Pair.of(new Vector3d(pos1[0], pos1[1], pos1[2]),
                                            new Vector3d(pos2[0], pos2[1], pos2[2]))
                );
                cuboid.render(player.getWorld());
                selectionCache.put(player.getUniqueId(), cuboid);
            }
            return;
        }

        switch (key) {
            case "pos1" -> cuboid.setFirst(new Vector3d(lookingAt.getBlockX(), lookingAt.getBlockY(), lookingAt.getBlockZ()));
            case "pos2" -> cuboid.setSecond(new Vector3d(lookingAt.getBlockX(), lookingAt.getBlockY(), lookingAt.getBlockZ()));
        }
    }
}
