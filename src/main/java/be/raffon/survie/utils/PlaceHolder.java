package be.raffon.survie.utils;

import be.raffon.survie.Bank;
import com.sun.istack.internal.NotNull;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceHolder extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "Survie";
    }

    @Override
    public @NotNull
    String getAuthor() {
        return "Raffon";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier){

        Bank bank = null;
        switch (identifier) {
            case "survietop1":
                if (!Bank.getBanks().isEmpty()) {
                    Bank.sort();
                    bank = Bank.getBanks().get(0);
                }
                else
                    return "";
                break;
            case "survietop2":
                if (Bank.getBanks().size() > 1) {
                    Bank.sort();
                    bank = Bank.getBanks().get(1);
                }
                else
                    return "";
                break;
            case "survietop3":
                if (Bank.getBanks().size() > 2) {
                    Bank.sort();
                    bank = Bank.getBanks().get(2);
                }
                else
                    return "";
                break;
            case "survietop4":
                if (Bank.getBanks().size() > 3) {
                    Bank.sort();
                    bank = Bank.getBanks().get(3);
                }
                else
                    return "";
                break;
            case "survietop5":
                if (Bank.getBanks().size() > 4) {
                    Bank.sort();
                    bank = Bank.getBanks().get(4);
                }
                else
                    return "";
                break;
        }
        if (bank != null)
            return Bukkit.getOfflinePlayer(bank.getUuid()).getName() + " > " + bank.getEmeralds();
        return null;
    }
}
