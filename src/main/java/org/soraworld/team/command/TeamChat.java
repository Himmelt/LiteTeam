package org.soraworld.team.command;

import org.bukkit.entity.Player;
import org.soraworld.team.core.Team;
import org.soraworld.team.manager.TeamManager;
import org.soraworld.violet.command.Args;
import org.soraworld.violet.command.SpigotCommand;
import org.soraworld.violet.manager.SpigotManager;

public class TeamChat extends SpigotCommand {
    public TeamChat(String name, String perm, boolean onlyPlayer, SpigotManager manager, String... aliases) {
        super(name, perm, onlyPlayer, manager, aliases);
    }

    public void execute(Player player, Args args) {
        if (args.notEmpty()) {
            Team team = ((TeamManager) manager).fetchTeam(player);
            if (team != null) {
                team.teamChat(player, args.getContent());
            } else manager.sendKey(player, "player.notInAny");
        } else manager.sendKey(player, "emptyArgs");
    }

    public String getUsage() {
        return "/teamchat|tchat|tmsg|tm <message>";
    }
}
