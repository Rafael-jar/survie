package be.raffon.survie;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

public class PlaceHolder extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return null;
    }

    @Override
    public @NotNull String getAuthor() {
        return "Raffon";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {

        if (identifier.equals("survietop1")) {
            UUID uuid = new ArrayList<>(survie.bank.keySet()).get(0);
            return Bukkit.getOfflinePlayer(uuid).getName();
        } else if(identifier.equals("survietop2")) {
            UUID uuid = new ArrayList<>(survie.bank.keySet()).get(1);
            return Bukkit.getOfflinePlayer(uuid).getName();
        }else if(identifier.equals("survietop3")) {
            UUID uuid = new ArrayList<>(survie.bank.keySet()).get(2);
            return Bukkit.getOfflinePlayer(uuid).getName();
        }else if(identifier.equals("survietop4")) {
            UUID uuid = new ArrayList<>(survie.bank.keySet()).get(3);
            return Bukkit.getOfflinePlayer(uuid).getName();
        }
        else if(identifier.equals("survietop5")) {
            UUID uuid = new ArrayList<>(survie.bank.keySet()).get(4);
            return Bukkit.getOfflinePlayer(uuid).getName();
        } else if(identifier.equals("survieem1")) {
            UUID uuid = new ArrayList<>(survie.bank.keySet()).get(0);
            return String.valueOf(survie.bank.get(uuid));
        } else if(identifier.equals("survieem2")) {
            UUID uuid = new ArrayList<>(survie.bank.keySet()).get(1);
            return String.valueOf(survie.bank.get(uuid));
        } else if(identifier.equals("survieem3")) {
            UUID uuid = new ArrayList<>(survie.bank.keySet()).get(2);
            return String.valueOf(survie.bank.get(uuid));
        } else if(identifier.equals("survieem4")) {
            UUID uuid = new ArrayList<>(survie.bank.keySet()).get(3);
            return String.valueOf(survie.bank.get(uuid));
        }else if(identifier.equals("survieem5")) {
            UUID uuid = new ArrayList<>(survie.bank.keySet()).get(4);
            return String.valueOf(survie.bank.get(uuid));
        }

        return null;
    }
}
