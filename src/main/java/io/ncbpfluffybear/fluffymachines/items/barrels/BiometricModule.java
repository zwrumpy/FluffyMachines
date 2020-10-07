package io.ncbpfluffybear.fluffymachines.items.barrels;

import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunPlugin;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.ncbpfluffybear.fluffymachines.utils.Utils;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectableAction;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BiometricModule extends SimpleSlimefunItem<ItemUseHandler> {

    public BiometricModule(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    public ItemUseHandler getItemHandler() {
        return e -> {

            Player p = e.getPlayer();

            ItemStack item = e.getItem();
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            List<String> newLore = new ArrayList<>();
            boolean bound = true;
            UUID uuid = null;

            for (String line : lore) {
                if (line.contains("UUID: ")) {
                    if (line.contains("None")) {
                        newLore.add("&7UUID: " + p.getUniqueId());
                        Utils.send(p, "&eBiometric Module has been registered to " + p.getUniqueId()
                            + " (" + p.getDisplayName() + ")");
                        bound = false;
                    } else {
                        uuid = UUID.fromString(line.replace("UUID: ", ""));
                        Utils.send(p, "&aBiometric Module belongs to " + uuid
                            + " (" + Bukkit.getOfflinePlayer(uuid).getName() + ")");
                        break; // No need to go further, lore does not need to be changed
                    }
                } else {
                    newLore.add(line);
                }
            }

            // This only runs if it has just been bound
            if (!bound) {
                meta.setLore(newLore);
                item.setItemMeta(meta);
                // Prevents binding and applying all in one go
                return;
            }

            Optional<Block> opt = e.getClickedBlock();
            if (opt.isPresent()) {
                Block b = opt.get();

                if (!SlimefunPlugin.getProtectionManager().hasPermission(e.getPlayer(), b.getLocation(),
                    ProtectableAction.BREAK_BLOCK)) {
                    return;
                }

                if (BlockStorage.hasBlockInfo(b)
                    && BlockStorage.check(b).getID().endsWith("_FLUFFY_BARREL")) {

                    BlockStorage.addBlockInfo(b.getLocation(), "owner", uuid.toString());

                    Utils.send(p, "&aYour barrel has been locked");
                }
            }
        };
    }
}
