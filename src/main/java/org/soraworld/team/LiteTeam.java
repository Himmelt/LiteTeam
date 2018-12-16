package org.soraworld.team;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidenceApi;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.soraworld.team.command.CommandTeam;
import org.soraworld.team.command.TeamChat;
import org.soraworld.team.core.Team;
import org.soraworld.team.expansion.TeamExpansion;
import org.soraworld.team.listener.EventListener;
import org.soraworld.team.listener.PvPListener;
import org.soraworld.team.manager.TeamManager;
import org.soraworld.violet.command.SpigotBaseSubs;
import org.soraworld.violet.command.SpigotCommand;
import org.soraworld.violet.manager.SpigotManager;
import org.soraworld.violet.plugin.SpigotPlugin;
import org.soraworld.violet.util.ChatColor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LiteTeam extends SpigotPlugin {

    private static final boolean placeholderApi;
    public static final boolean residenceApi;
    private static TeamManager API;

    static {
        boolean residence = false, placeholder = false;
        try {
            PlaceholderAPI.class.getName();
            PlaceholderExpansion.class.getName();
            placeholder = true;
        } catch (Throwable ignored) {
        }
        try {
            Residence.class.getName();
            ResidenceApi.class.getName();
            residence = true;
        } catch (Throwable ignored) {
        }
        placeholderApi = placeholder;
        residenceApi = residence;
    }

    public void afterEnable() {
        if (placeholderApi) {
            try {
                PlaceholderExpansion expansion = TeamExpansion.class.getConstructor(TeamManager.class).newInstance(manager);
                if (PlaceholderAPI.registerExpansion(expansion)) {
                    manager.consoleKey("placeholder.expansionSuccess");
                } else manager.consoleKey("placeholder.expansionFailed");
            } catch (Throwable ignored) {
                manager.console(ChatColor.RED + "GuildExpansion Construct Instance failed !!!");
            }
        } else manager.consoleKey("placeholder.notHook");
    }

    protected SpigotManager registerManager(Path path) {
        API = new TeamManager(this, path);
        return API;
    }

    protected List<Listener> registerListeners() {
        ArrayList<Listener> listeners = new ArrayList<>();
        if (manager instanceof TeamManager) {
            TeamManager manager = (TeamManager) this.manager;
            listeners.add(new EventListener(manager));
            listeners.add(new PvPListener(manager));
            //listeners.add(new ChatListener(manager));
        }
        return listeners;
    }

    protected void registerCommands() {
        SpigotCommand command = new SpigotCommand(getId(), null, false, manager, "team");
        command.extractSub(SpigotBaseSubs.class);
        command.extractSub(CommandTeam.class);
        command.setUsage("/team ....");
        register(this, command);
        register(this, new TeamChat("teamchat", null, true, manager, "tchat", "tmsg", "tm"));
    }

    public static Team getTeam(Player player) {
        return API.fetchTeam(player);
    }

    public static Team getTeam(String player) {
        return API.fetchTeam(player);
    }
}
