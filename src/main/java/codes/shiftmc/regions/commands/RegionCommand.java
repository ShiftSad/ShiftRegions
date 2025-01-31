package codes.shiftmc.regions.commands;

import codes.shiftmc.regions.listener.PlayerListener;
import codes.shiftmc.regions.math.Cuboid;
import codes.shiftmc.regions.math.MathUtils;
import codes.shiftmc.regions.menus.ManageMembers;
import codes.shiftmc.regions.model.Flag;
import codes.shiftmc.regions.model.Region;
import codes.shiftmc.regions.service.RegionService;
import codes.shiftmc.regions.service.UserService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Vector3d;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;

// I also mostly know what I'm doing
@SuppressWarnings("UnstableApiUsage")
public class RegionCommand {

    private static final MiniMessage mm = MiniMessage.builder().build();

    private final RegionService regionService;
    private final UserService userService;
    private final JavaPlugin plugin;

    /**
     * <pre>
     * {@code
     * /region
     *     - create
     *     - delete
     *     - flags
     *     - relations
     *     - wand
     * }
     **/
    public RegionCommand(RegionService regionService, UserService userService, JavaPlugin plugin) {
        this.regionService = regionService;
        this.userService = userService;
        this.plugin = plugin;
    }

    public final LiteralCommandNode<CommandSourceStack> root = Commands.literal("region")
            .then(Commands.literal("create").executes(this::create))
            .then(Commands.literal("delete").executes(this::delete))
            .then(Commands.literal("flags").executes(this::flags))
            .then(Commands.literal("members").executes(this::members))
            .then(Commands.literal("wand").executes(this::wand))
            .build();

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

        assert pos1 != null;
        assert pos2 != null;
        var cuboid = new Cuboid(
                Pair.of(new Vector3d(pos1[0], pos1[1], pos1[2]),
                        new Vector3d(pos2[0], pos2[1], pos2[2]))
        );

        userService.findByUUID(player.getUniqueId())
                .switchIfEmpty(Mono.defer(() -> {
                    player.sendMessage("Failed loading user data, try logging in again");
                    return Mono.empty();
                }))
                .flatMap(user -> {
                    if (cuboid.getVolume() > user.blockClaims()) {
                        player.sendMessage("You have reached the maximum amount of blocks you can claim by "
                                + (cuboid.getVolume() - user.blockClaims()));
                        return Mono.empty();
                    }

                    // Update user's block claims and save the region
                    var updatedUser = user.setBlockClaims(user.blockClaims() - cuboid.getVolume());
                    return userService.updateUser(updatedUser)
                            .then(Mono.defer(() -> {
                                var region = new Region(
                                        UUID.randomUUID(),
                                        MathUtils.toBlockVector(cuboid.getFirst()),
                                        MathUtils.toBlockVector(cuboid.getSecond()),
                                        player.getUniqueId(),
                                        List.of(),
                                        Flag.NONE.getBit()
                                );
                                return regionService.save(region, false);
                            }));
                })
                .doOnSuccess(region -> {
                    if (region != null) {
                        player.sendMessage("Region created successfully");

                        // Clear selection and remove persistent data
                        pdc.remove(new NamespacedKey("regions", "pos1"));
                        pdc.remove(new NamespacedKey("regions", "pos2"));

                        // Can only be done async
                        Bukkit.getScheduler().runTask(plugin, () -> PlayerListener.clearSelection(player.getUniqueId()));
                    }
                })
                .doOnError(throwable -> player.sendMessage("An error occurred while creating the region: "
                        + throwable.getMessage()))
                .subscribeOn(Schedulers.boundedElastic()) // Offload to a non-blocking thread
                .subscribe();


        return Command.SINGLE_SUCCESS;
    }

    private int delete(CommandContext<CommandSourceStack> ctx) {
        Player player = getPlayer(ctx);
        if (player == null) return Command.SINGLE_SUCCESS;

        var location = player.getLocation();
        var regionOpt = regionService.findByLocation(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );

        if (regionOpt.isEmpty()) {
            player.sendMessage("No region found at your location");
            return Command.SINGLE_SUCCESS;
        }

        var region = regionOpt.get();
        if (!region.owner().equals(player.getUniqueId())) {
            player.sendMessage("You do not own this region");
            return Command.SINGLE_SUCCESS;
        }

        regionService.deleteRegion(region.id());
        player.sendMessage("Region deleted successfully");

        return Command.SINGLE_SUCCESS;
    }

    private int flags(CommandContext<CommandSourceStack> ctx) {
        return Command.SINGLE_SUCCESS;
    }

    private int members(CommandContext<CommandSourceStack> ctx) {
        Player player = getPlayer(ctx);
        if (player == null) return Command.SINGLE_SUCCESS;

        var location = player.getLocation();
        var regionOpt = regionService.findByLocation(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );

        if (regionOpt.isEmpty()) {
            player.sendMessage("No region found at your location");
            return Command.SINGLE_SUCCESS;
        }

        var region = regionOpt.get();
        new ManageMembers(region, player);
        return Command.SINGLE_SUCCESS;
    }

    private int wand(CommandContext<CommandSourceStack> ctx) {
        var player = getPlayer(ctx);
        if (player == null) return Command.SINGLE_SUCCESS;

        var item = ItemStack.of(Material.ECHO_SHARD);
        item.editMeta(meta -> {
           meta.customName(mm.deserialize("<green>Terrain selection"));
           meta.lore(List.of(
                   mm.deserialize("\uD835\uDDEB <aqua>LEFT CLICK for first position"),
                   mm.deserialize("\uD835\uDDEB <aqua>RIGHT CLICK for second position")
           ));

           var pdc = meta.getPersistentDataContainer();
           pdc.set(new NamespacedKey("regions", "region_creator"), PersistentDataType.BOOLEAN, true);
        });

        var inventory = player.getInventory();
        inventory.addItem(item);

        return Command.SINGLE_SUCCESS;
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
