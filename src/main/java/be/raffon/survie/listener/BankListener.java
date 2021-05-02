package be.raffon.survie.listener;

import be.raffon.survie.Bank;
import be.raffon.survie.Survie;
import be.raffon.survie.scoreboards.FastBoard;
import be.raffon.survie.utils.SQLManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

public class BankListener implements Listener {


    @EventHandler
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Console can not use this plugin!");
            return true;
        }
        if (alias.equalsIgnoreCase("clearbank")) {
            if (args.length < 1) return true;

            OfflinePlayer pl = Bukkit.getOfflinePlayer(args[0]);

            Bank.get(pl.getUniqueId()).setEmeralds(0);
            sender.sendMessage(Survie.prefix + " You have successfuly cleared his bank.");

            return true;
        }
        Player player = (Player) sender;
        if (args[0].equals("pnj")) {
            if (!player.hasPermission("minecraft.command.summon")) {
                player.sendMessage(Survie.prefix + " You do not have the permission to use this command !");
                return true;
            }
            FileConfiguration config = Survie.INSTANCE.getConfig();
            Integer x = config.getInt("bank.x");
            Integer y = config.getInt("bank.y");
            Integer z = config.getInt("bank.z");
            player.chat("/minecraft:kill @e[type=villager,name=Bank]");

            config.set("bank.x", player.getLocation().getBlockX());
            config.set("bank.y", player.getLocation().getBlockY());
            config.set("bank.z", player.getLocation().getBlockZ());
            config.set("bank.world", player.getLocation().getWorld().getName());
            Survie.INSTANCE.saveConfig();

            player.sendMessage(Survie.prefix + " The pnj was successfully set to your location !");
            player.chat("/summon villager ~ ~ ~ {VillagerData:{profession:farmer,level:2,type:plains},NoAI:1,CustomName:\"\\\"Bank\\\"\",Invulnerable:1}");

        }
        return true;

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) throws SQLException {
        Block block = event.getBlock();
        if (!getStaff(event.getPlayer()) && event.getPlayer().getWorld().getName().equals("survie") && event.getPlayer().getGameMode() == GameMode.SURVIVAL) {

            if (block.getType() == Material.DIAMOND_ORE) {
                SQLManager.getInstance().update("INSERT INTO staff_baltop (username, diamonds) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE diamonds = diamonds + 1;");
            }
            if (block.getType() == Material.GOLD_ORE) {
                SQLManager.getInstance().update("INSERT INTO staff_baltop (username, golds) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE golds = golds + 1;");
            }
            if (block.getType() == Material.IRON_ORE) {
                SQLManager.getInstance().update("INSERT INTO staff_baltop (username, iron) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE iron = iron + 1;");
            }
            if (block.getType() == Material.COAL_ORE) {
                SQLManager.getInstance().update("INSERT INTO staff_baltop (username, coal) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE coal = coal + 1;");
            }
            if (block.getType() == Material.LAPIS_ORE) {
                SQLManager.getInstance().update("INSERT INTO staff_baltop (username, lapis) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE lapis = lapis + 1;");
            }
            if (block.getType() == Material.REDSTONE_ORE) {
                SQLManager.getInstance().update("INSERT INTO staff_baltop (username, redstone) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE redstone = redstone + 1;");
            }
            if (block.getType() == Material.ANCIENT_DEBRIS) {
                SQLManager.getInstance().update("INSERT INTO staff_baltop (username, netherite) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE netherite = netherite + 1;");
            }
            if (block.getType() == Material.STONE || block.getType() == Material.ANDESITE || block.getType() == Material.GRANITE || block.getType() == Material.DIORITE || block.getType() == Material.GRAVEL) {
                SQLManager.getInstance().update("INSERT INTO staff_baltop (username, normal_blocks) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE normal_blocks = normal_blocks + 1;");
            }
            if (block.getType() == Material.BASALT || block.getType() == Material.NETHERRACK || block.getType() == Material.BLACKSTONE) {
                SQLManager.getInstance().update("INSERT INTO staff_baltop (username, normal_nether) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE normal_nether = normal_nether + 1;");
            }
        }
    }

    @EventHandler
    public void onClickOnEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager)) return;

        Entity entity = event.getRightClicked();
        FileConfiguration config = Survie.INSTANCE.getConfig();
        if (entity.getType().equals(EntityType.VILLAGER) && entity.getName().equalsIgnoreCase("Bank")) {
            if (getStaff(event.getPlayer())) {
                event.getPlayer().sendMessage(Survie.prefix + " You can't use this pnj in staff mode !");
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);

            Bank.getDepositMenu().open(event.getPlayer());

            /*CInventory cinv = new CInventory();

            Page page = new Page(2, ChatColor.BLUE + "BANK", 1, true);

            ItemStack pane = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1);
            ItemMeta panem = pane.getItemMeta();
            panem.setDisplayName(" ");
            pane.setItemMeta(panem);
            CItem paneitem = new CItem(pane, "{nothing}");

            ItemStack emerald5 = new ItemStack(Material.EMERALD, 1);
            emerald5.setAmount(5);
            ItemMeta emerald5m = emerald5.getItemMeta();
            emerald5m.setDisplayName(" ");
            emerald5.setItemMeta(emerald5m);
            CItem emerald5i = new CItem(emerald5, e -> {
                e.setCancelled(true);
                Player player = (Player) e.getWhoClicked();
                Integer emeralds = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null) {
                        if (item.getType().equals(Material.EMERALD)) {
                            emeralds = emeralds + item.getAmount();
                        }
                    }
                }
                if (emeralds >= 5) {
                    Integer minus = 5;
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null) {
                            if (item.getType().equals(Material.EMERALD)) {
                                Integer amount = item.getAmount();
                                if (amount > minus) {
                                    item.setAmount(amount - minus);
                                    minus = 0;
                                } else if (amount == minus) {
                                    minus = 0;
                                    item.setAmount(0);
                                } else {
                                    minus = minus - amount;
                                    item.setAmount(0);
                                }
                            }
                        }
                    }

                    Bank bank = Bank.get(event.getPlayer().getUniqueId());
                    bank.setEmeralds(bank.getEmeralds() + 5);
                    // 10 - (prevem % 10) > diff
                    Integer diff = 5;
                    if (10 - (bank.getEmeralds() % 10) <= diff) {
                        if (diff > 10) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmysterybox give " + event.getPlayer().getName() + " " + (int) Math.floor(diff / 10.0) + " 4");
                            event.getPlayer().sendMessage(Survie.prefix + " Well done you recieved " + (int) Math.floor(diff / 10.0) + " mystery boxs level 4 because of your participation :).");
                            return;
                        }
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmysterybox give " + event.getPlayer().getName() + " 1 4");
                        event.getPlayer().sendMessage(Survie.prefix + " Well done you received a mystery box level 4 because of your participation :).");
                    }
                } else {
                    player.sendMessage("You do not have enough emeralds !");
                }


				if(em % 20 == 0 && em != 0) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmysterybox give "+ event.getPlayer().getName() + " 1 5");
					event.getPlayer().sendMessage(this.Survie.prefix + " Well done you recieved a mystery box level 4 because of your participation :).");
				}

            });

            ItemStack emerald32 = new ItemStack(Material.EMERALD, 1);
            ItemMeta emerald32m = emerald32.getItemMeta();
            emerald32.setAmount(32);
            emerald32m.setDisplayName(" ");
            emerald32.setItemMeta(emerald32m);

            CItem emerald32i = new CItem(emerald32, e -> {
                e.setCancelled(true);
                Player player = (Player) e.getWhoClicked();
                Integer emeralds = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null) {
                        if (item.getType().equals(Material.EMERALD)) {
                            emeralds = emeralds + item.getAmount();
                        }
                    }
                }
                if (emeralds >= 32) {
                    Integer minus = 32;
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null) {
                            if (item.getType().equals(Material.EMERALD)) {
                                Integer amount = item.getAmount();
                                if (amount > minus) {
                                    item.setAmount(amount - minus);
                                    minus = 0;
                                } else if (amount == minus) {
                                    minus = 0;
                                    item.setAmount(0);
                                } else {
                                    minus = minus - amount;
                                    item.setAmount(0);
                                }
                            }
                        }
                    }

                    Bank bank = Bank.get(event.getPlayer().getUniqueId());
                    bank.setEmeralds(bank.getEmeralds() + 32);
                    // 10 - (prevem % 10) > diff
                    Integer diff = 32;
                    if (10 - (bank.getEmeralds() % 10) <= diff) {
                        if (bank.getEmeralds() % 10 >= 8) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmysterybox give " + event.getPlayer().getName() + " " + ((int) Math.floor(diff / 10.0) + 1) + " 4");
                            event.getPlayer().sendMessage(Survie.prefix + " Well done you recieved " + ((int) Math.floor(diff / 10.0) + 1) + " mystery boxs level 4 because of your participation :).");
                            return;
                        } else {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmysterybox give " + event.getPlayer().getName() + " " + ((int) Math.floor(diff / 10.0)) + " 4");
                            event.getPlayer().sendMessage(Survie.prefix + " Well done you received " + ((int) Math.floor(diff / 10.0)) + " mystery boxs level 4 because of your participation :).");
                            return;
                        }
                    }
                } else {
                    player.sendMessage("You do not have enough emeralds !");
                }

            });

            ItemStack emerald64 = new ItemStack(Material.EMERALD, 1);
            emerald64.setAmount(64);
            ItemMeta emerald64m = emerald64.getItemMeta();
            emerald64m.setDisplayName(" ");
            emerald64.setItemMeta(emerald64m);
            CItem emerald64i = new CItem(emerald64, e -> {
                e.setCancelled(true);
                Player player = (Player) e.getWhoClicked();
                Integer emeralds = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null) {
                        if (item.getType().equals(Material.EMERALD)) {
                            emeralds = emeralds + item.getAmount();
                        }
                    }
                }
                if (emeralds >= 64) {
                    Integer minus = 64;
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null) {
                            if (item.getType().equals(Material.EMERALD)) {
                                Integer amount = item.getAmount();
                                if (amount > minus) {
                                    item.setAmount(amount - minus);
                                    minus = 0;
                                } else if (amount == minus) {
                                    minus = 0;
                                    item.setAmount(0);
                                } else {
                                    minus = minus - amount;
                                    item.setAmount(0);
                                }
                            }
                        }
                    }

                    Bank bank = Bank.get(event.getPlayer().getUniqueId());
                    bank.setEmeralds(bank.getEmeralds() + 64);
                    // 10 - (prevem % 10) > diff
                    Integer diff = 64;
                    if (10 - (bank.getEmeralds() % 10) <= diff) {
                        if (bank.getEmeralds() % 10 >= 6) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmysterybox give " + event.getPlayer().getName() + " " + ((int) Math.floor(diff / 10.0) + 1) + " 4");
                            event.getPlayer().sendMessage(Survie.prefix + " Well done you recieved " + ((int) Math.floor(diff / 10.0) + 1) + " mystery boxs level 4 because of your participation :).");
                            return;
                        } else {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmysterybox give " + event.getPlayer().getName() + " " + ((int) Math.floor(diff / 10.0)) + " 4");
                            event.getPlayer().sendMessage(Survie.prefix + " Well done you received " + ((int) Math.floor(diff / 10.0)) + " mystery boxs level 4 because of your participation :).");
                            return;
                        }
                    }
                } else {
                    player.sendMessage("You do not have enough emeralds !");
                }

            });

            page.addItem(paneitem, 0);
            page.addItem(paneitem, 1);
            page.addItem(paneitem, 2);
            page.addItem(paneitem, 3);
            page.addItem(paneitem, 4);
            page.addItem(paneitem, 5);
            page.addItem(paneitem, 6);
            page.addItem(paneitem, 7);
            page.addItem(paneitem, 8);
            page.addItem(paneitem, 9);
            page.addItem(paneitem, 10);
            page.addItem(paneitem, 12);
            page.addItem(paneitem, 14);
            page.addItem(paneitem, 16);
            page.addItem(paneitem, 17);
            page.addItem(paneitem, 18);


            page.addItem(emerald5i, 11);
            page.addItem(emerald32i, 13);
            page.addItem(emerald64i, 15);

            cinv.addPage(page);
            cinv.display(event.getPlayer());
            Survie.invs.registerInventory(event.getPlayer().getUniqueId(), cinv);*/

        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        FastBoard board = Survie.boards.remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        Player player = evt.getPlayer();

        FastBoard board = new FastBoard(player);

        board.updateTitle(ChatColor.GREEN + "◊ Emeralds ◊");

        Survie.boards.put(player.getUniqueId(), board);

        try {
            updateBoard(evt.getPlayer());
        } catch (SQLException e1) {
            e1.printStackTrace();
        }

		/*ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM survie_histoire WHERE username = '" + player.getUniqueId() + "'");

		if(!rs.next()) {
			CInventory cinv = new CInventory();
			Page page = new Page(2, "Choose language", 1, true, false);

		   	ItemStack pane = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1);
			ItemMeta panem = pane.getItemMeta();
			panem.setDisplayName(" ");
			pane.setItemMeta(panem);
			CItem paneitem = new CItem(pane, "{nothing}");

			CItem french = new CItem(applySkullSurvie.prefixure("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTEyNjlhMDY3ZWUzN2U2MzYzNWNhMWU3MjNiNjc2ZjEzOWRjMmRiZGRmZjk2YmJmZWY5OWQ4YjM1Yzk5NmJjIn19fQ==", "Français"), e -> {
		    	e.setCancelled(true);
				try {
					connection.createStatement().execute("INSERT INTO survie_histoire (username, chapter, language) VALUES ('" + e.getWhoClicked().getUniqueId() + "', '1.0', 0);");
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		    	e.getWhoClicked().sendMessage("");
			});

			CItem english = new CItem(applySkullSurvie.prefixure("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTllZGNkZDdiMDYxNzNkN2QyMjFjNzI3NGM4NmNiYTM1NzMwMTcwNzg4YmI2YTFkYjA5Y2M2ODEwNDM1YjkyYyJ9fX0=", "English"), e -> {
				e.setCancelled(true);
				try {
					connection.createStatement().execute("INSERT INTO survie_histoire (username, chapter, language) VALUES ('" + e.getWhoClicked().getUniqueId() + "', '1.0', 1);");
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		    	e.getWhoClicked().sendMessage("");
		    	survie.unregister(e.getWhoClicked().getUniqueId());;
			});

			page.addItem(paneitem, 0);
			page.addItem(paneitem,  1);
			page.addItem(paneitem,  2);
			page.addItem(paneitem,  3);
			page.addItem(paneitem,  4);
			page.addItem(paneitem,  5);
			page.addItem(paneitem,  6);
			page.addItem(paneitem,  7);
			page.addItem(paneitem,  8);
			page.addItem(paneitem,  9);
			page.addItem(paneitem,  10);
			page.addItem(paneitem,  12);
			page.addItem(paneitem,  14);
			page.addItem(paneitem,  13);
			page.addItem(paneitem,  16);
			page.addItem(paneitem,  17);
			page.addItem(paneitem,  18);

			page.addItem(french,  11);
			page.addItem(english,  15);

			cinv.addPage(page);
			cinv.display(evt.getPlayer());
			invs.registerInventory(evt.getPlayer().getUniqueId(), cinv);

		}*/
    }


    public Boolean getStaff(Player pl) {
        AtomicReference<Boolean> bool = new AtomicReference<Boolean>();
        if (!pl.hasPermission("staff.staff")) return false;
        SQLManager.getInstance().query("SELECT * FROM staff_players WHERE username = '" + pl.getUniqueId() + "';", rs -> {
            try {
                if (rs.next()) {
                    Boolean staff = rs.getBoolean("staff");
                    String world = rs.getString("world");
                    if (staff && world.endsWith(pl.getLocation().getWorld().getName()) && world.startsWith("survie")) {
                        bool.set(true);
                        return;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            bool.set(false);
            return;
        });
        return bool.get();

    }


    public void updateBoard(Player player) throws SQLException {
        FastBoard fb = Survie.boards.get(player.getUniqueId());
        if (fb == null) {
            return;
        }
        Integer bank_em = 0;
        Integer emeralds = 0;
        Bank.get(player.getUniqueId()).setEmeralds(0);
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                if (item.getType().equals(Material.EMERALD)) {
                    emeralds = emeralds + item.getAmount();
                }
            }
        }
        fb.updateLines(
                "",
                ChatColor.YELLOW + "Bank: " + ChatColor.WHITE + bank_em,
                ChatColor.YELLOW + "Inventory: " + ChatColor.WHITE + emeralds
        );

    }

}
