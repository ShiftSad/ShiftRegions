package codes.shiftmc.regions.commands;

import codes.shiftmc.regions.listener.PlayerListener;
import codes.shiftmc.regions.math.Cuboid;
import codes.shiftmc.regions.math.MathUtils;
import codes.shiftmc.regions.model.Flag;
import codes.shiftmc.regions.model.Region;
import codes.shiftmc.regions.service.RegionService;
import codes.shiftmc.regions.service.UserService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.joml.Vector3d;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

// I also mostly know what I'm doing
@SuppressWarnings("UnstableApiUsage")
public class RegionCommand {

    private final RegionService regionService;
    private final UserService userService;

    /**
     * <pre>
     * {@code
     * /region
     *     - create
     *     - delete
     *     - flags
     *     - relations
     * }
     **/
    public RegionCommand(RegionService regionService, UserService userService) {
        this.regionService = regionService;
        this.userService = userService;

        var root = Commands.literal("region")
                .then(Commands.literal("create").executes(this::create))
                .then(Commands.literal("delete").executes(this::delete))
                .then(Commands.literal("flags").executes(this::flags))
                .then(Commands.literal("relations").executes(this::relations));
    }

    private int create(CommandContext<CommandSourceStack> ctx) {
        var player = getPlayer(ctx);
        if (player == null) return Command.SINGLE_SUCCESS;

        var pdc = player.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey("regions", "pos1"), PersistentDataType.INTEGER_ARRAY) ||
                !pdc.has(new NamespacedKey("regions", "pos2"), PersistentDataType.INTEGER_ARRAY)) {
            player.sendMessage("You must select both positions before creating a region");
            return Command.SINGLE_SUCCESS;
        }

        var pos1 = pdc.get(new NamespacedKey("regions", "pos1"), PersistentDataType.INTEGER_ARRAY);
        var pos2 = pdc.get(new NamespacedKey("regions", "pos2"), PersistentDataType.INTEGER_ARRAY);

        userService.findByUUID(player.getUniqueId())
                .flatMap(user -> {
                    if (user == null) {
                        player.sendMessage("Failed loading user data, try logging.");
                        return Mono.empty();
                    }

                    assert pos1 != null;
                    assert pos2 != null;
                    var cuboid = new Cuboid(
                            Pair.of(new Vector3d(pos1[0], pos1[1], pos1[2]),
                                    new Vector3d(pos2[0], pos2[1], pos2[2]))
                    );
                    if (cuboid.getVolume() > user.blockClaims()) {
                        player.sendMessage("You have reached the maximum amount of blocks you can claim by "
                                + (cuboid.getVolume() - user.blockClaims()));
                        return Mono.empty();
                    }

                    var region = new Region(
                            UUID.randomUUID(),
                            MathUtils.toBlockVector(cuboid.getFirst()),
                            MathUtils.toBlockVector(cuboid.getSecond()),
                            player.getUniqueId(),
                            List.of(),
                            Flag.NONE.getBit()
                    );

                    return regionService.save(region);
                })
                .doOnSuccess(region -> {
                    if (region != null) {
                        player.sendMessage("Region created successfully");

                        pdc.remove(new NamespacedKey("regions", "pos1"));
                        pdc.remove(new NamespacedKey("regions", "pos2"));
                        PlayerListener.clearSelection(player.getUniqueId());
                    }
                })
                .subscribe(
                        unused -> {},
                        throwable -> player.sendMessage("An error occurred while creating the region: "
                                + throwable.getMessage())
                );

        return Command.SINGLE_SUCCESS;
    }

    private int delete(CommandContext<CommandSourceStack> ctx) {
        var player = getPlayer(ctx);
        if (player == null) return Command.SINGLE_SUCCESS;
        var location = player.getLocation();

        var region = regionService.findByLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ()).block();
        if (region == null) {
            player.sendMessage("No region found at your location");
            return Command.SINGLE_SUCCESS;
        }

        if (!region.owner().equals(player.getUniqueId())) {
            player.sendMessage("You do not own this region");
            return Command.SINGLE_SUCCESS;
        }

        regionService.deleteRegion(region.id());
        player.sendMessage("Region deleted successfully");

        return Command.SINGLE_SUCCESS;
    }

    private int flags(CommandContext<CommandSourceStack> ctx) {

    }

    private int relations(CommandContext<CommandSourceStack> ctx) {

    }

    private Player getPlayer(CommandContext<CommandSourceStack> ctx) {
        var sender = ctx.getSource().getSender();
        if (sender instanceof Player player) {
            return player;
        }
        sender.sendMessage("You must be a player to execute this command");
        return null;
    }
}
