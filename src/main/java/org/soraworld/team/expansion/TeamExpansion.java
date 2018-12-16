package org.soraworld.team.expansion;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.soraworld.team.core.Team;
import org.soraworld.team.manager.TeamManager;

public class TeamExpansion extends PlaceholderExpansion {

    private final TeamManager manager;

    public TeamExpansion(TeamManager manager) {
        this.manager = manager;
    }

    public String getIdentifier() {
        return manager.getPlugin().getId();
    }

    public String getAuthor() {
        return "Himmelt";
    }

    public String getVersion() {
        return manager.getPlugin().getVersion();
    }

    public String onPlaceholderRequest(Player player, String params) {
        Team guild = manager.fetchTeam(player);
        if (guild != null) return guild.getVariable(params);
        else return "not join guild";
    }
}
