package be.raffon.survie;

import be.raffon.survie.listener.BankListener;
import be.raffon.survie.scoreboards.FastBoard;
import be.raffon.survie.utils.PlaceHolder;
import be.raffon.survie.utils.SQLManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class Survie extends JavaPlugin {
	public static Survie INSTANCE;
	public final static Map<UUID, FastBoard> boards = new HashMap<>();

	public static String prefix = "["+ChatColor.RED+"Survie"+ChatColor.WHITE+"]";

	@Override
	public void onEnable() {
		INSTANCE = this;

		new SQLManager("localhost", 3306, "sf2021", "sf2021", "Lq%n9aajZS7CtU");
		Bank.init();
		getServer().getPluginManager().registerEvents(new BankListener(), this);

		System.setProperty("file.encoding", "UTF-8");
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

}
