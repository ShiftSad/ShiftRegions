package codes.shiftmc.regions.menus;

import codes.shiftmc.regions.menus.members.AddMemberItem;
import codes.shiftmc.regions.menus.members.ManageMembers;
import codes.shiftmc.regions.model.Flag;
import codes.shiftmc.regions.model.Region;
import codes.shiftmc.regions.service.RegionService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ManageFlags {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    public ManageFlags(Region region, RegionService regionService, Player player) {
        var border = Item.simple(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build());

        var flags = Arrays.stream(Flag.values())
                .map(flag -> {
                    String color = region.hasFlag(flag) ? "<green>" : "<red>";
                    return new FlagItem(flag, region, regionService, List.of(
                            mm.deserialize("<gray>Flag: {color}{flag}".replace("{color}", color).replace("{flag}", flag.name())),
                            Component.empty(),
                            mm.deserialize("\uD835\uDDEB <aqua>LEFT CLICK"),
                            mm.deserialize(" Add flag to region"),
                            Component.empty(),
                            mm.deserialize("\uD835\uDDEB <aqua>RIGHT CLICK"),
                            mm.deserialize(" Remove flag from region")
                    ));
                }).toList();

        var gui = PagedGui.items()
                .setStructure(
                        "# # # # # # # # #",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "# # # # # # # # #")
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('#', border)
                .setContent((List<Item>) (List<?>) flags)
                .build();

        var window = Window.single()
                .setViewer(player)
                .setTitle("Manage Flags")
                .setGui(gui)
                .build();

        window.open();
    }
}
