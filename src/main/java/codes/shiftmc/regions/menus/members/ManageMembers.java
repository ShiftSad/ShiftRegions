package codes.shiftmc.regions.menus.members;

import codes.shiftmc.regions.model.Region;
import codes.shiftmc.regions.service.RegionService;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

import java.util.List;

public class ManageMembers {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    public ManageMembers(Region region, Player player, RegionService regionService) {
        var border = Item.simple(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build());

        var gui = PagedGui.items()
                .setStructure(
                        "# # # # # # # # #",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "# # # # M # # # #")
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('#', border)
                .addIngredient('M', new AddMemberItem(regionService, region));

        var heads = region.members().stream()
                .map(Pair::first)
                .map(uuid -> {
                    return new PlayerItem(uuid, List.of(
                            mm.deserialize("<gray>Role: {color}{role}".replace("{color}", "<gray>").replace("{role}", "Member")),
                            Component.empty(),
                            mm.deserialize("\uD835\uDDEB <aqua>KEY Q"),
                            mm.deserialize(" Remove from region")
                    ), region, regionService);
                }).toList();

        gui.setContent((List<Item>) (List<?>) heads);
        gui.build();

        var window = Window.single()
                .setViewer(player)
                .setTitle("Manage Members")
                .setGui(gui)
                .build();

        window.open();
    }


}
