package be.raffon.survie;

import be.raffon.survie.inventories.InventoryManager;
import be.raffon.survie.listener.BankListener;
import be.raffon.survie.scoreboards.FastBoard;
import be.raffon.survie.utils.PlaceHolder;
import be.raffon.survie.utils.SQLManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;


@SuppressWarnings("deprecation")
public class Survie extends JavaPlugin {
	public static Survie INSTANCE;
	public static InventoryManager invs;
	public final static Map<UUID, FastBoard> boards = new HashMap<>();

	public static String prefix = "["+ChatColor.RED+"Survie"+ChatColor.WHITE+"]";
	private SQLManager sqlManager;

	@Override
	public void onEnable() {
		INSTANCE = this;
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

		sqlManager = new SQLManager("localhost", 3306, "sf2021", "sf2021", "Lq%n9aajZS7CtU");

		String sql = "CREATE TABLE IF NOT EXISTS survie_banque (\n"
					+ "	username VARCHAR(100) PRIMARY KEY,\n"
					+ "	emeralds INTEGER\n"
					+ ");";
		SQLManager.getInstance().update(sql);


		sql = "CREATE TABLE IF NOT EXISTS survie_histoire (\n"
				+ "	username VARCHAR(100) PRIMARY KEY,\n"
				+ "	chapter VARCHAR(100),\n"
				+ "	language INTEGER\n"
				+ ");";
		SQLManager.getInstance().update(sql);
		Bank.init();

		invs = new InventoryManager();
		
		System.setProperty("file.encoding", "UTF-8");
		getServer().getPluginManager().registerEvents(new BankListener(), this);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
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

				Bank bank = Bank.get(player.getUniqueId());
				bank.setEmeralds(0);

				int emeralds = 0;
			    for (ItemStack item : player.getInventory().getContents()) {
			    	if(item != null) {
				    	if(item.getType().equals(Material.EMERALD)) {
				    		emeralds = emeralds + item.getAmount();
				    	}
			    	}
			    }
		        fb.updateLines(
		                "",
		                ChatColor.YELLOW + "Bank: " + ChatColor.WHITE + bank.getEmeralds(),
		                ChatColor.YELLOW + "Inventory: " + ChatColor.WHITE + emeralds
		        );
			}

			
		}, 0L, 20L);
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
			new PlaceHolder().register();
		}
	}

	@Override
	public void onDisable() {
		Bank.save();
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
