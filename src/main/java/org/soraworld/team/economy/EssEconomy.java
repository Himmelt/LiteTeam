package org.soraworld.team.economy;

import com.earth2me.essentials.Essentials;
import net.ess3.api.Economy;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class EssEconomy implements IEconomy {
    EssEconomy() {
        Essentials.class.getName();
        Economy.class.getName();
    }

    public boolean setEco(OfflinePlayer player, double amount) {
        try {
            Economy.setMoney(player.getName(), BigDecimal.valueOf(amount));
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public boolean addEco(OfflinePlayer player, double amount) {
        try {
            Economy.add(player.getName(), BigDecimal.valueOf(amount));
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public double getEco(OfflinePlayer player) {
        try {
            return Economy.getMoneyExact(player.getName()).doubleValue();
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public boolean hasEnough(OfflinePlayer player, double amount) {
        try {
            return Economy.hasEnough(player.getName(), BigDecimal.valueOf(amount));
        } catch (Throwable ignored) {
            return false;
        }
    }

    public boolean takeEco(OfflinePlayer player, double amount) {
        try {
            Economy.substract(player.getName(), BigDecimal.valueOf(amount));
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
