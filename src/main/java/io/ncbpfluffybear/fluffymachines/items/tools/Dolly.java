package io.ncbpfluffybear.fluffymachines.items.tools;

import io.github.thebusybiscuit.slimefun4.api.player.PlayerBackpack;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunPlugin;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.ncbpfluffybear.fluffymachines.utils.Utils;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import me.mrCookieSlime.Slimefun.cscorelib2.chat.ChatColors;
import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectableAction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dolly extends SimpleSlimefunItem<ItemUseHandler> {

    private static final ItemStack lockItem = Utils.buildNonInteractable(Material.DIRT, "&4&lDolly empty", "&cHow did you get in here?");

    public Dolly(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    public ItemUseHandler getItemHandler() {
        return e -> {
            e.cancel();

            Player p = e.getPlayer();
            ItemStack dolly = e.getItem();

            if (!e.getClickedBlock().isPresent()) {
                return;
            }

            Block b = e.getClickedBlock().get();

            if (!Utils.hasPermission(e.getPlayer(), b.getLocation(), ProtectableAction.BREAK_BLOCK)) {
                return;
            }

            Block relative = b.getRelative(e.getClickedFace());

            if (b.getType() == Material.CHEST && !BlockStorage.hasBlockInfo(b)) {

                ItemMeta dollyMeta = dolly.getItemMeta();
                for (String line : dollyMeta.getLore()) {
                    if (line.contains("ID: <ID>")) {
                        PlayerProfile.get(p, profile -> {
                            int backpackId = profile.createBackpack(54).getId();
                            SlimefunPlugin.getBackpackListener().setBackpackId(p, dolly, 3, backpackId);
                            PlayerProfile.getBackpack(dolly, backpack -> backpack.getInventory().setItem(0, lockItem));
                        });
                    }
                }

                Inventory chest = ((InventoryHolder) b.getState()).getInventory();

                /*
                if (chest.getSize() > 27) {
                    Utils.send(p, "&cYou can only pick up single chests!");
                    return;
                }
                 */

                ItemStack[] contents = chest.getContents();

                AtomicBoolean exists = new AtomicBoolean(false);
                PlayerProfile.getBackpack(dolly, backpack -> {
                    if (backpack != null && backpack.getInventory().getItem(0) != null
                            && Utils.checkNonInteractable(backpack.getInventory().getItem(0))
                    ) {
                        // Dolly size update message
                        if (backpack.getSize() == 27) {
                            backpack.setSize(54);
                            Utils.send(p, "&aDollies can now pick up double chests! Dolly size upgraded to 54.");
                        }

                        backpack.getInventory().setStorageContents(contents);
                        chest.clear();
                        PlayerProfile.getBackpack(dolly, PlayerBackpack::markDirty);
                        exists.set(true);
                        dolly.setType(Material.CHEST_MINECART);

                        dollyMeta.setDisplayName(ChatColors.color("&bDolly &7(&e" + contents.length + " slots used&7)"));
                        dolly.setItemMeta(dollyMeta);
                    } else {
                        Utils.send(p, "&cThis dolly is already carrying a chest!");
                    }
                });

                // Deals with async problems
                if (exists.get()) {
                    b.setType(Material.AIR);
                    Utils.send(p, "&aYou have picked up this chest");
                }


            } else if (relative.getType() == Material.AIR) {

                PlayerProfile.getBackpack(dolly, backpack -> {
                    if (backpack != null && (backpack.getInventory().getItem(0) == null || !Utils.checkNonInteractable(backpack.getInventory().getItem(0)))) {
                        ItemStack[] bpContents = backpack.getInventory().getContents();

                        // Check if chest placed needs to be double
                        if (bpContents.length > 27) {
                            Block rightDouble = relative.getRelative(getClockwiseFace(p.getFacing()));
                            if (rightDouble.getType() == Material.AIR
                                    && Utils.hasPermission(p, rightDouble.getLocation(), ProtectableAction.PLACE_BLOCK)
                            ) {
                                rightDouble.setType(Material.CHEST);
                            } else {
                                Utils.send(p, "&cYou can not place a double chest here!");
                                return;
                            }
                        }

                        //backpack.getInventory().clear();

                        // Lock backpack
                        //backpack.getInventory().setItem(0, lockItem);
                        relative.setType(Material.CHEST);
                        //((InventoryHolder) relative.getState()).getInventory().setStorageContents(bpContents);
                        //dolly.setType(Material.MINECART);
                        ItemMeta dollyMeta = dolly.getItemMeta();
                        dollyMeta.setDisplayName(ChatColors.color("&bDolly &7(&eEmpty&7)"));

                        Utils.send(p, "&aChest has been placed");
                    } else {
                        Utils.send(p, "&cYou must pick up a chest first!");
                    }
                });
            }
        };
    }

    private static BlockFace getClockwiseFace(BlockFace face) {
        switch (face) {
            case NORTH:
                return BlockFace.EAST;
            case EAST:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.NORTH;
            default:
                return null;
        }
    }


}
