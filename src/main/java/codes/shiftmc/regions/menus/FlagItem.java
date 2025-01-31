package codes.shiftmc.regions.menus;

import codes.shiftmc.regions.model.Flag;
import codes.shiftmc.regions.model.Region;
import codes.shiftmc.regions.service.RegionService;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.Click;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.ItemWrapper;

import java.util.List;

public class FlagItem extends AbstractItem {

    private final Flag flag;
    private final Region region;
    private final RegionService regionService;
    private final List<Component> lore;

    public FlagItem(Flag flag, Region region, RegionService regionService, List<Component> lore) {
        this.flag = flag;
        this.region = region;
        this.regionService = regionService;
        this.lore = lore;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        var item = ItemStack.of(Material.PAPER);
        var meta = item.getItemMeta();
        meta.displayName(Component.text(flag.name()));
        meta.lore(lore);
        item.setItemMeta(meta);
        return new ItemWrapper(item);
    }

    @Override
    public void handleClick(ClickType clickType, Player player, Click click) {
        switch (clickType) {
            case LEFT:
                region.addFlag(flag);
                player.sendMessage(Component.text("Flag added to region"));
                regionService.updateRegion(region).subscribe();
                break;
            case RIGHT:
                region.removeFlag(flag);
                player.sendMessage(Component.text("Flag removed from region"));
                regionService.updateRegion(region).subscribe();
                break;
            default:
                break;
        }
    }
}
