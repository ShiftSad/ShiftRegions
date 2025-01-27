package codes.shiftmc.regions.menus;

import codes.shiftmc.regions.model.Region;
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

    public ManageMembers(Region region, Player player) {

        var border = Item.simple(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build());

        var heads = region.members().stream()
                .map(Pair::first)
                .map(uuid -> {
                    String membersColor = "gray";
                    String role = "Member";
                    if (region.owner() == uuid) {
                        membersColor = "<yellow>";
                        role = "Owner";
                    }
                    return new PlayerItem(uuid, List.of(
                            mm.deserialize("<gray>Role: {color}{role}".replace("{color}", membersColor).replace("{role}", role)),
                            Component.empty(),
                            mm.deserialize("\uD835\uDDEB <cyan>KEY Q"),
                            mm.deserialize("   Remove from terrain")
                    ));
                }).toList();

        var gui = PagedGui.items()
                .setStructure(
                        "# # # # # # # # #",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "# # # S # A # # #")
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('#', border)
                .setContent((List<Item>) (List<?>) heads)
                .build();

        var window = Window.single()
                .setViewer(player)
                .setTitle("Manage Members")
                .setGui(gui)
                .build();

        window.open();
    }


}
