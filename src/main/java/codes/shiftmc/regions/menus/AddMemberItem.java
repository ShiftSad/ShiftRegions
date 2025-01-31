package codes.shiftmc.regions.menus;

import codes.shiftmc.regions.model.Flag;
import codes.shiftmc.regions.model.Region;
import codes.shiftmc.regions.repository.RegionRepository;
import codes.shiftmc.regions.service.RegionService;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.internal.AnvilInventory;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.Click;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.window.AnvilWindow;
import xyz.xenondevs.invui.window.Window;

import java.util.HashMap;
import java.util.UUID;

public class AddMemberItem extends AbstractItem {

    private static final HashMap<UUID, String> data = new HashMap<>();

    private final RegionService regionService;
    private final Region region;

    public AddMemberItem(RegionService regionService, Region region) {
        this.regionService = regionService;
        this.region = region;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        var item = ItemStack.of(Material.GREEN_CONCRETE);
        var meta = item.getItemMeta();
        meta.displayName(Component.text("Add Member"));
        item.setItemMeta(meta);
        return new ItemWrapper(item);
    }

    @Override
    public void handleClick(ClickType clickType, Player player, Click click) {
        data.remove(player.getUniqueId());
        var item = ItemStack.of(Material.PAPER);
        var meta = item.getItemMeta();
        meta.displayName(Component.text("Player Name"));
        item.setItemMeta(meta);

        var playerItem = ItemStack.of(Material.PLAYER_HEAD);
        var playerMeta = (SkullMeta) playerItem.getItemMeta();
        playerMeta.displayName(Component.text("Player"));
        playerMeta.setOwningPlayer(player);

        var gui = Gui.normal().setStructure("# . P").addIngredient('#', new ItemWrapper(item)).addIngredient('P', new ItemWrapper(playerItem)).build();

        var window = AnvilWindow.single()
                .setViewer(player)
                .setTitle("Add Member")
                .setGui(gui)
                .addRenameHandler(s -> {
                    if (s.isEmpty()) return;
                    var target = Bukkit.getOfflinePlayerIfCached(s);
                    data.put(player.getUniqueId(), s);
                    if (target == null) return;
                    playerMeta.setOwningPlayer(target);
                    gui.notifyWindows();
                })
                .addCloseHandler(() -> {
                    var name = data.get(player.getUniqueId());
                    if (name == null) {
                        player.sendMessage("No name entered");
                        return;
                    }
                    var target = Bukkit.getOfflinePlayerIfCached(name);
                    if (target == null) {
                        player.sendMessage("Player not found");
                        return;
                    }

                    var members = region.members();
                    members.add(Pair.of(target.getUniqueId(), Flag.NONE.getBit()));
                    region.setMembers(members);

                    regionService.updateRegion(region).subscribe();
                    player.sendMessage("Member " + target.getName() + " added");
                })
                .build();
        window.open();
    }
}
