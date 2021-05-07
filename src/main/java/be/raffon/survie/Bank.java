package be.raffon.survie;

import be.raffon.survie.utils.BankSorter;
import be.raffon.survie.utils.SQLManager;
import fr.ChadOW.cinventory.CContent.CInventory;
import fr.ChadOW.cinventory.CContent.CItem;
import fr.ChadOW.cinventory.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Bank {

    private static CInventory depositMenu;
    private static List<Bank> banks = new ArrayList<>();

    public static void init() {
        SQLManager.getInstance().query("SELECT * FROM survie_banque;", rs -> {
            try {
                while(rs.next()) {
                    banks.add(new Bank(UUID.fromString(rs.getString("username")), rs.getInt("emeralds")));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        initMenu();
    }

    private static void initMenu() {
        depositMenu = new CInventory(27, "§aEMERALD BANK");
        for (int i = 0; i < 27; i++) {
            CItem font = new CItem(new ItemCreator(Material.BLUE_STAINED_GLASS_PANE, 0).setName("§r")).setSlot(i);
            depositMenu.addElement(font);
        }

        CItem item = new CItem(new ItemCreator(Material.EMERALD, 0).setAmount(5).setName("§aDeposit x5")).setSlot(11)
                .addEvent((inventoryRepresentation, itemRepresentation, player, clickContext) -> {
                    tryRemoveEmeralds(player, 5);
                });
        depositMenu.addElement(item);
        item = new CItem(new ItemCreator(Material.EMERALD, 0).setAmount(32).setName("§aDeposit x32")).setSlot(13)
                .addEvent((inventoryRepresentation, itemRepresentation, player, clickContext) -> {
                    tryRemoveEmeralds(player, 32);
                });
        depositMenu.addElement(item);
        item = new CItem(new ItemCreator(Material.EMERALD, 0).setAmount(64).setName("§aDeposit x64")).setSlot(15)
                .addEvent((inventoryRepresentation, itemRepresentation, player, clickContext) -> {
                    tryRemoveEmeralds(player, 64);
                });
        depositMenu.addElement(item);
    }

    private static void tryRemoveEmeralds(Player player, int quantity) {
        int counter = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType().equals(Material.EMERALD))
                counter += item.getAmount();
        }
        if (counter >= quantity) {
            player.sendMessage(Survie.prefix + "You put " + quantity + " emeralds in your emerald bank");
            get(player.getUniqueId()).setEmeralds(get(player.getUniqueId()).getEmeralds() + quantity);
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType().equals(Material.EMERALD)) {
                    if (item.getAmount() > quantity) {
                        item.setAmount(item.getAmount() - quantity);
                        break;
                    } else {
                        quantity -= item.getAmount();
                        item.setAmount(0);
                    }
                }
            }
        } else {
            player.sendMessage(Survie.prefix + "You don't have enough emeralds !");
        }
    }

    public static List<Bank> getBanks() {
        return banks;
    }

    public static void sort() {
        banks.sort(new BankSorter());
    }

    public static Bank get(UUID uuid) {
        for (Bank bank : banks) {
            if (bank.getUuid().equals(uuid))
                return bank;
        }
        return new Bank(uuid, 0);
    }

    private UUID uuid;

    private int emeralds;

    public Bank(UUID uuid, int emeralds) {
        this.uuid = uuid;
        this.emeralds = emeralds;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getEmeralds() {
        return emeralds;
    }

    public void setEmeralds(int emeralds) {
        this.emeralds = emeralds;
    }

    public static CInventory getDepositMenu() {
        return depositMenu;
    }

    public void sendToDB() {
        SQLManager.getInstance().update("INSERT INTO survie_banque (username, emeralds) VALUES ('" + getUuid() + "', " + getEmeralds() + ") ON DUPLICATE KEY UPDATE emeralds="+ getEmeralds()+";");
    }
}
