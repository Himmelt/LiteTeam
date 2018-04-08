package org.soraworld.guild.config;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.soraworld.guild.constant.Constant;
import org.soraworld.guild.core.TeamManager;
import org.soraworld.guild.economy.Economy;
import org.soraworld.guild.economy.IEconomy;
import org.soraworld.guild.flans.Flans;
import org.soraworld.violet.config.IIConfig;

import javax.annotation.Nonnull;
import java.io.File;

public class Config extends IIConfig {

    private String ecoType = "Vault";
    private boolean enableEco = true;
    private boolean teamPvP = false;

    private Flans flans;
    private IEconomy iEconomy;
    private TeamManager teamManager;

    public Config(File path, Plugin plugin) {
        super(path, plugin);
    }

    protected void loadOptions() {
        ecoType = config_yaml.getString("ecoType", "Vault");
        if (ecoType == null || ecoType.isEmpty()) ecoType = "Vault";
        enableEco = config_yaml.getBoolean("enableEco", true);
        teamPvP = config_yaml.getBoolean("teamPvP", false);
        getTeamManager().readLevels(config_yaml.getList("levels"));
        getTeamManager().loadGuild();
    }

    protected void saveOptions() {
        config_yaml.set("ecoType", ecoType);
        config_yaml.set("enableEco", enableEco);
        config_yaml.set("teamPvP", teamPvP);
        config_yaml.set("levels", getTeamManager().writeLevels());
        getTeamManager().saveGuild();
    }

    public void afterLoad() {
        if (flans == null) flans = new Flans(this);
        iEconomy = new Economy(this);
    }

    @Nonnull
    protected ChatColor defaultChatColor() {
        return ChatColor.BLUE;
    }

    @Nonnull
    protected String defaultChatHead() {
        return "[" + Constant.PLUGIN_NAME + "] ";
    }

    public String defaultAdminPerm() {
        return Constant.PERM_ADMIN;
    }

    public Flans getFlans() {
        if (flans == null) flans = new Flans(this);
        return flans;
    }

    public IEconomy getEconomy() {
        if (iEconomy == null) iEconomy = new Economy(this);
        return iEconomy;
    }

    public boolean checkEcoType(String type) {
        return ecoType.equals(type);
    }

    public boolean isTeamPvP() {
        return teamPvP;
    }

    public TeamManager getTeamManager() {
        if (teamManager == null) teamManager = new TeamManager(this, config_file.getParentFile());
        return teamManager;
    }

}
