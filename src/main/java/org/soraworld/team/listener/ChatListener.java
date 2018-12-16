package org.soraworld.team.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.soraworld.team.core.Team;
import org.soraworld.team.manager.TeamManager;

public class ChatListener implements Listener {

    private final TeamManager manager;

    public ChatListener(final TeamManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (!event.isCancelled()) {
            if (event.getFormat().contains("%1$s")) {
                Team guild = manager.fetchTeam(event.getPlayer());
                if (guild != null) {
                    StringBuilder build = new StringBuilder(event.getFormat());
                    build.insert(build.indexOf("%1$s") + 4, '[' + guild.getDisplay() + ']');
                    event.setFormat(build.toString());
                }
            }
        }
    }
}
