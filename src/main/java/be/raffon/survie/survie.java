package be.raffon.survie;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import be.raffon.survie.inventories.Page;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import be.raffon.survie.inventories.CItem;
import be.raffon.survie.inventories.CInventory;
import be.raffon.survie.inventories.InventoryManager;
import be.raffon.survie.scoreboards.FastBoard;


@SuppressWarnings("deprecation")
public class survie extends JavaPlugin implements Listener{
	
	String host;
	int port;
	String database;
	String username;
	String password;
	private static InventoryManager invs;
	public YamlConfiguration config;
	public File fileconfig;
	public String text = "["+ChatColor.RED+"Survie"+ChatColor.WHITE+"]";
	private final Map<UUID, FastBoard> boards = new HashMap<>();
	public static Map<UUID, Integer> bank;
	SQLManager sqlManager;

	@Override
	public void onEnable() {
		/*File dir = this.getDataFolder(); // Get the parent directory
		dir.mkdirs();
		js = new File(this.getDataFolder() + "//" + "config.JSON");
		if(!js.exists()) {
			new File(this.getDataFolder() + "//").mkdirs();
			
			JSONObject o = setJSON();
			try {
				FileWriter file = new FileWriter(this.getDataFolder() + "//" + "config.JSON");
				file.write(o.toJSONString());
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		js = new File(this.getDataFolder(), "//" + "config.JSON");*/

		host = "localhost";
		port = 3306;
		database = "sf2021";
		username = "sf2021";
		password = "Lq%n9aajZS7CtU";
		sqlManager = new SQLManager(host, port, database, username, password);

		String sql = "CREATE TABLE IF NOT EXISTS survie_banque (\n"
					+ "	username VARCHAR(100) PRIMARY KEY,\n"
					+ "	emeralds INTEGER\n"
					+ ");";
		sqlManager.getInstance().update(sql);


		sql = "CREATE TABLE IF NOT EXISTS survie_histoire (\n"
				+ "	username VARCHAR(100) PRIMARY KEY,\n"
				+ "	chapter VARCHAR(100),\n"
				+ "	language INTEGER\n"
				+ ");";
		sqlManager.getInstance().update(sql);

		AtomicReference<HashMap<UUID, Integer>> hashmap = new AtomicReference<HashMap<UUID, Integer>>();
		SQLManager.getInstance().query("SELECT * FROM survie_banque;", rs -> {
			try {
				HashMap<UUID, Integer> hash = new HashMap<UUID, Integer>();
				while(rs.next()) {
					String username = rs.getString("username");
					Integer emeralds = rs.getInt("emeralds");
					hash.put(UUID.fromString(username), emeralds);
				}
				hashmap.set(hash);
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
		});

		invs = new InventoryManager();

		bank = hashmap.get();
		
		System.out.println("Survie succeffuly loaded !");
		
		System.setProperty("file.encoding", "UTF-8");
		getServer().getPluginManager().registerEvents(this, this);
		
		this.fileconfig = new File(this.getDataFolder().getPath(), "config.yml");
		YamlConfiguration config = YamlConfiguration.loadConfiguration(fileconfig);
		this.config = config;
		
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {

			@Override
			public void run() {
				for(Player player: Bukkit.getOnlinePlayers()) {
					this.updateBoard(player);
				}
			}
			private void updateBoard(Player player) {
		    	
		    	FastBoard fb = boards.get(player.getUniqueId());
		    	if(fb == null) {
					fb = new FastBoard(player);

					fb.updateTitle(ChatColor.GREEN + "◊ Emeralds ◊");

					boards.put(player.getUniqueId(), fb);
		    	}

				Integer bank_em = 0;
				Integer emeralds = 0;
				if (bank.get(player.getUniqueId()) == null) {bank.put(player.getUniqueId(), 0);}
				bank_em = bank.get(player.getUniqueId());


			    for (ItemStack item : player.getInventory().getContents()) {
			    	if(item != null) {
				    	if(item.getType().equals(Material.EMERALD)) {
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

			
		}, 0L, 20L);
	}

	@Override
	public void onDisable() {
		for(Map.Entry<UUID, Integer> entry : bank.entrySet()) {
			UUID key = entry.getKey();
			Integer value = entry.getValue();

			sqlManager.getInstance().update("INSERT INTO survie_banque (username, emeralds) VALUES ('" + key + "', " + value + ") ON DUPLICATE KEY UPDATE emeralds="+value+";");
		}
	}

    
	@EventHandler
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Console can not use this plugin!");
            return true;
        }
		if(alias.equalsIgnoreCase("clearbank")) {
			if(args.length < 1) return true;

			OfflinePlayer pl = Bukkit.getOfflinePlayer(args[0]);

			bank.remove(pl.getUniqueId());
			bank.put(pl.getUniqueId(), 0);
			sender.sendMessage(text + " You have successfuly cleared his bank.");

			return true;
		}
		Player player = (Player) sender;
		if(args[0].equals("pnj")) {
			if(!player.hasPermission("minecraft.command.summon")) {
				player.sendMessage(text + " You do not have the permission to use this command !");
				return true;
			}
			Integer x = config.getInt("bank.x");
			Integer y = config.getInt("bank.y");
			Integer z = config.getInt("bank.z");
			player.chat("/minecraft:kill @e[type=villager,name=Bank]");
			
			config.set("bank.x", player.getLocation().getBlockX());
			config.set("bank.y", player.getLocation().getBlockY());
			config.set("bank.z", player.getLocation().getBlockZ());
			config.set("bank.world", player.getLocation().getWorld().getName());
			try {
				config.save(fileconfig);
			} catch (IOException e) {
				e.printStackTrace();
			}
			config = YamlConfiguration.loadConfiguration(fileconfig);
			
			player.sendMessage(text + " The pnj was successfully set to your location !");
			player.chat("/summon villager ~ ~ ~ {VillagerData:{profession:farmer,level:2,type:plains},NoAI:1,CustomName:\"\\\"Bank\\\"\",Invulnerable:1}");
			
		}
		return true;
		
	}
	
	public Boolean getStaff(Player pl) {
		AtomicReference<Boolean> bool = new AtomicReference<Boolean>();
		if(!pl.hasPermission("staff.staff")) return false;
		sqlManager.getInstance().query("SELECT * FROM staff_players WHERE username = '" + pl.getUniqueId() + "';", rs -> {
			try {
				if(rs.next()) {
					Boolean staff = rs.getBoolean("staff");
					String world = rs.getString("world");
					if(staff && world.endsWith(pl.getLocation().getWorld().getName()) && world.startsWith("survie")) {
						bool.set(true);
						return;
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bool.set(false);
			return;
		});
		return bool.get();
		
	}
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) throws SQLException{
		Block block = event.getBlock();
		if(!getStaff(event.getPlayer()) && event.getPlayer().getWorld().getName().equals("survie")&&event.getPlayer().getGameMode() == GameMode.SURVIVAL) {

			if(block.getType() == Material.DIAMOND_ORE) {
				sqlManager.getInstance().update("INSERT INTO staff_baltop (username, diamonds) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE diamonds = diamonds + 1;");
			}
			if(block.getType() == Material.GOLD_ORE) {
				sqlManager.getInstance().update("INSERT INTO staff_baltop (username, golds) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE golds = golds + 1;");
			}
			if(block.getType() == Material.IRON_ORE) {
				sqlManager.getInstance().update("INSERT INTO staff_baltop (username, iron) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE iron = iron + 1;");
			}
			if(block.getType() == Material.COAL_ORE) {
				sqlManager.getInstance().update("INSERT INTO staff_baltop (username, coal) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE coal = coal + 1;");
			}
			if(block.getType() == Material.LAPIS_ORE) {
				sqlManager.getInstance().update("INSERT INTO staff_baltop (username, lapis) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE lapis = lapis + 1;");
			}
			if(block.getType() == Material.REDSTONE_ORE) {
				sqlManager.getInstance().update("INSERT INTO staff_baltop (username, redstone) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE redstone = redstone + 1;");
			}
			if(block.getType() == Material.ANCIENT_DEBRIS) {
				sqlManager.getInstance().update("INSERT INTO staff_baltop (username, netherite) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE netherite = netherite + 1;");
			}
			if(block.getType() == Material.STONE || block.getType() == Material.ANDESITE || block.getType() == Material.GRANITE|| block.getType() == Material.DIORITE || block.getType() == Material.GRAVEL) {
				sqlManager.getInstance().update("INSERT INTO staff_baltop (username, normal_blocks) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE normal_blocks = normal_blocks + 1;");
			}
			if(block.getType() == Material.BASALT || block.getType() == Material.NETHERRACK || block.getType() == Material.BLACKSTONE) {
				sqlManager.getInstance().update("INSERT INTO staff_baltop (username, normal_nether) VALUES ('" + event.getPlayer().getUniqueId() + "', 1) ON DUPLICATE KEY UPDATE normal_nether = normal_nether + 1;");
			}
		}
	}

	@EventHandler
	public void onClickOnEntity(PlayerInteractEntityEvent event) {
	if(!(event.getRightClicked() instanceof Villager)) return;
	
	Entity entity = event.getRightClicked();
	Location loc = entity.getLocation();
	if(loc.getBlockX() == config.getInt("bank.x") && loc.getBlockY() == config.getInt("bank.y") && loc.getBlockZ() == config.getInt("bank.z") && loc.getWorld().getName().equals(config.getString("bank.world"))) {
			if(getStaff(event.getPlayer())) { event.getPlayer().sendMessage(text + " You can't use this pnj in staff mode !"); event.setCancelled(true);return;}
		 	event.setCancelled(true);
		   
		   	CInventory cinv = new CInventory();
		   
		   	Page page = new Page(2, ChatColor.BLUE+"BANK", 1, true);
		   	
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
			    	if(item != null) {
				    	if(item.getType().equals(Material.EMERALD)) {
				    		emeralds = emeralds + item.getAmount();
				    	}
			    	}
			    }
			    if(emeralds >= 5) {
			    	Integer minus = 5;
				    for (ItemStack item : player.getInventory().getContents()) {
				    	if(item != null) {
					    	if(item.getType().equals(Material.EMERALD)) {
					    		Integer amount = item.getAmount();
					    		if(amount > minus) {
					    			item.setAmount(amount - minus);
					    			minus = 0;
					    		} else if(amount == minus){
					    			minus = 0;
					    			item.setAmount(0);
					    		} else {
					    			minus = minus - amount;
					    			item.setAmount(0);
					    		}
					    	}
				    	}
				    }
			    } else {
			    	player.sendMessage("You do not have enough emeralds !");
			    }
			    Integer em = this.bank.get(event.getPlayer().getUniqueId());
				Integer prevem = em;

				this.bank.remove(event.getPlayer().getUniqueId());
			    bank.put(event.getPlayer().getUniqueId(), em+5);
				// 10 - (prevem % 10) > diff
				Integer diff = 5;
			    if(10 - (prevem % 10) <= diff) {
			    	if(diff > 10) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmysterybox give "+ event.getPlayer().getName() + " " + (int) Math.floor(diff/10.0) + " 4");
						event.getPlayer().sendMessage(this.text + " Well done you recieved " + (int) Math.floor(diff/10.0) + " mystery boxs level 4 because of your participation :).");
						return;
					}
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmysterybox give "+ event.getPlayer().getName() + " 1 4");
					event.getPlayer().sendMessage(this.text + " Well done you received a mystery box level 4 because of your participation :).");
				}

				/*if(em % 20 == 0 && em != 0) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmysterybox give "+ event.getPlayer().getName() + " 1 5");
					event.getPlayer().sendMessage(this.text + " Well done you recieved a mystery box level 4 because of your participation :).");
				}*/

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
			    	if(item != null) {
				    	if(item.getType().equals(Material.EMERALD)) {
				    		emeralds = emeralds + item.getAmount();
				    	}
			    	}
			    }
			    if(emeralds >= 32) {
			    	Integer minus = 32;
				    for (ItemStack item : player.getInventory().getContents()) {
				    	if(item != null) {
					    	if(item.getType().equals(Material.EMERALD)) {
					    		Integer amount = item.getAmount();
					    		if(amount > minus) {
					    			item.setAmount(amount - minus);
					    			minus = 0;
					    		} else if(amount == minus){
					    			minus = 0;
					    			item.setAmount(0);
					    		} else {
					    			minus = minus - amount;
					    			item.setAmount(0);
					    		}
					    	}
				    	}
				    }
			    } else {
			    	player.sendMessage("You do not have enough emeralds !");
			    }
				Integer em = this.bank.get(event.getPlayer().getUniqueId());
				Integer prevem = em;

				this.bank.remove(event.getPlayer().getUniqueId());
				bank.put(event.getPlayer().getUniqueId(), em+32);
				// 10 - (prevem % 10) > diff
				Integer diff = 32;
				if(10 - (prevem % 10) <= diff) {
					if(prevem % 10 >= 8) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmysterybox give "+ event.getPlayer().getName() + " " + ((int)Math.floor(diff/10.0)+1) + " 4");
						event.getPlayer().sendMessage(this.text + " Well done you recieved " + ((int)Math.floor(diff/10.0)+1) + " mystery boxs level 4 because of your participation :).");
						return;
					} else {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmysterybox give "+ event.getPlayer().getName() + " " + ((int)Math.floor(diff/10.0)) + " 4");
						event.getPlayer().sendMessage(this.text + " Well done you received " + ((int)Math.floor(diff/10.0)) + " mystery boxs level 4 because of your participation :).");
						return;
					}
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
			    	if(item != null) {
				    	if(item.getType().equals(Material.EMERALD)) {
				    		emeralds = emeralds + item.getAmount();
				    	}
			    	}
			    }
			    if(emeralds >= 64) {
			    	Integer minus = 64;
				    for (ItemStack item : player.getInventory().getContents()) {
				    	if(item != null) {
					    	if(item.getType().equals(Material.EMERALD)) {
					    		Integer amount = item.getAmount();
					    		if(amount > minus) {
					    			item.setAmount(amount - minus);
					    			minus = 0;
					    		} else if(amount == minus){
					    			minus = 0;
					    			item.setAmount(0);
					    		} else {
					    			minus = minus - amount;
					    			item.setAmount(0);
					    		}
					    	}
				    	}
				    }
			    } else {
			    	player.sendMessage("You do not have enough emeralds !");
			    }
				Integer em = this.bank.get(event.getPlayer().getUniqueId());
				Integer prevem = em;

				this.bank.remove(event.getPlayer().getUniqueId());
				bank.put(event.getPlayer().getUniqueId(), em+64);
				// 10 - (prevem % 10) > diff
				Integer diff = 64;
				if(10 - (prevem % 10) <= diff) {
					if(prevem % 10 >= 6) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmysterybox give "+ event.getPlayer().getName() + " " + ((int)Math.floor(diff/10.0)+1) + " 4");
						event.getPlayer().sendMessage(this.text + " Well done you recieved " + ((int)Math.floor(diff/10.0)+1) + " mystery boxs level 4 because of your participation :).");
						return;
					} else {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmysterybox give "+ event.getPlayer().getName() + " " + ((int)Math.floor(diff/10.0)) + " 4");
						event.getPlayer().sendMessage(this.text + " Well done you received " + ((int)Math.floor(diff/10.0)) + " mystery boxs level 4 because of your participation :).");
						return;
					}
				}
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
			page.addItem(paneitem,  16);
			page.addItem(paneitem,  17);
			page.addItem(paneitem,  18);
			
			
			page.addItem(emerald5i,  11);
			page.addItem(emerald32i,  13);
			page.addItem(emerald64i,  15);
			
			cinv.addPage(page);
			cinv.display(event.getPlayer());
			invs.registerInventory(event.getPlayer().getUniqueId(), cinv);
			
	   }
	}

	
	@EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        FastBoard board = boards.remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        Player player = evt.getPlayer();

        FastBoard board = new FastBoard(player);

        board.updateTitle(ChatColor.GREEN + "◊ Emeralds ◊");

        boards.put(player.getUniqueId(), board);

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
			
			CItem french = new CItem(applySkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTEyNjlhMDY3ZWUzN2U2MzYzNWNhMWU3MjNiNjc2ZjEzOWRjMmRiZGRmZjk2YmJmZWY5OWQ4YjM1Yzk5NmJjIn19fQ==", "Français"), e -> {
		    	e.setCancelled(true);
				try {
					connection.createStatement().execute("INSERT INTO survie_histoire (username, chapter, language) VALUES ('" + e.getWhoClicked().getUniqueId() + "', '1.0', 0);");
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		    	e.getWhoClicked().sendMessage("");
			});
			
			CItem english = new CItem(applySkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTllZGNkZDdiMDYxNzNkN2QyMjFjNzI3NGM4NmNiYTM1NzMwMTcwNzg4YmI2YTFkYjA5Y2M2ODEwNDM1YjkyYyJ9fX0=", "English"), e -> {
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
   
    
    public void updateBoard(Player player) throws SQLException {
		FastBoard fb = boards.get(player.getUniqueId());
		if (fb == null) {
			return;
		}
		Integer bank_em = 0;
		Integer emeralds = 0;
		if (bank.get(player.getUniqueId()) == null) {bank.put(player.getUniqueId(), 0);}
		bank_em = bank.get(player.getUniqueId());
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
    
	@EventHandler
    public void onInventoryClick(InventoryClickEvent event)  {
		Player player = (Player) event.getWhoClicked();
    	Inventory inv = event.getClickedInventory();
    	ItemStack clicked = event.getCurrentItem();
    	if(clicked == null) {
    		return;
    	}
    	CInventory cinv = invs.getInventory(player.getUniqueId());
    	if(cinv == null) {
    		return;
    	}
    	if(event.getClick() == ClickType.LEFT){
    		cinv.clickItem(event.getCurrentItem(), event.getSlot(), inv, player, event, invs, false);
    	} else {
    		cinv.clickItem(event.getCurrentItem(), event.getSlot(), inv, player, event, invs, true);
    	}
		
    }
	
	/*@EventHandler
    public void InvClose(InventoryCloseEvent event){
        Inventory inv = event.getInventory();
        if(invs.getInventory(event.getPlayer().getUniqueId()) != null) {
        	CInventory cinv = invs.getInventory(event.getPlayer().getUniqueId());
        	
        	
        	invs.unregisterInventory(event.getPlayer().getUniqueId());
        	ArrayList<Page> pages = cinv.pages;
    		for(int i=0; i<pages.size(); i++) {
    			Page page = pages.get(i);
    			
    			Inventory ine = page.returnInv();
    			
    			if(Arrays.equals(inv.getContents(), ine.getContents())) {
    				if(!page.closable) {
    					
    					cinv.display((Player) event.getPlayer());
    					
    					invs.registerInventory(event.getPlayer().getUniqueId(), cinv);
    					
    					return;
    				}
    			}
    		}
    		
    		
        	
        }
    }*/
	
	public static void unregister(UUID uuid) {
		invs.unregisterInventory(uuid);
	}

}
