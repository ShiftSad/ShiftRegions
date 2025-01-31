package codes.shiftmc.regions.menus;

import codes.shiftmc.regions.model.Region;
import codes.shiftmc.regions.service.RegionService;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.Click;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.List;
import java.util.UUID;

public class PlayerItem extends AbstractItem {

    private static final Logger log = LoggerFactory.getLogger(PlayerItem.class);

    private final UUID uuid;
    private final List<Component> lore;
    private final Region region;
    private final RegionService regionService;

    public PlayerItem(UUID uuid, List<Component> lore, Region region, RegionService regionService) {
        this.uuid = uuid;
        this.lore = lore;
        this.region = region;
        this.regionService = regionService;
    }

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player viewer) {
        var item = ItemStack.of(Material.PLAYER_HEAD);
        var meta = (SkullMeta) item.getItemMeta();
        var name = viewer.getServer().getOfflinePlayer(uuid).getName();
        if (name != null) meta.displayName(Component.text(name));
        meta.setOwningPlayer(viewer.getServer().getOfflinePlayer(uuid));
        meta.lore(lore);
        item.setItemMeta(meta);
        return new ItemBuilder(item);
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        if (!List.of(ClickType.DROP, ClickType.CONTROL_DROP).contains(clickType)) return;

        var members = region.members();
        members.removeIf(pair -> pair.first().equals(uuid));
        region.setMembers(members);
        regionService.updateRegion(region).subscribe();

        player.sendMessage(Component.text("Player removed from region"));
    }
}
