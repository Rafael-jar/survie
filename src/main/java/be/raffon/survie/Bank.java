package be.raffon.survie;

import be.raffon.survie.utils.BankSorter;
import be.raffon.survie.utils.SQLManager;
import javafx.collections.transformation.SortedList;

import java.sql.SQLException;
import java.util.*;

public class Bank {

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
    }

    public static void save() {
        banks.forEach(bank -> SQLManager.getInstance().update("INSERT INTO survie_banque (username, emeralds) VALUES ('" + bank.getUuid() + "', " + bank.getEmeralds() + ") ON DUPLICATE KEY UPDATE emeralds="+bank.getEmeralds()+";"));
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
}
