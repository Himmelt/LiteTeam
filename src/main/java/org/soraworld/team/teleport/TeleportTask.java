package org.soraworld.team.teleport;

import org.bukkit.Bukkit;
import org.soraworld.team.core.Team;
import org.soraworld.team.manager.TeamManager;

import java.util.HashSet;
import java.util.UUID;

public class TeleportTask implements Runnable {

    public int taskId;
    public final HashSet<UUID> accepts = new HashSet<>();

    private int delay;
    private boolean executed = false;
    private final String leader;
    private final TeamManager manager;

    public TeleportTask(TeamManager manager, String leader, int delay) {
        this.leader = leader;
        this.manager = manager;
        this.delay = delay;
    }

    public void run() {
        Team team = manager.getTeam(leader);
        if (team != null) {
            delay--;
            if (delay <= 5 || delay == 10 || delay == 15 || delay == 20) {
                team.teamChat(null, manager.trans("tpr.timeDelay", delay));
            }
            if (delay <= 0) {
                team.teamTpr();
                executed = true;
                cancel();
            }
        } else cancel();
    }

    public void cancel() {
        accepts.clear();
        Bukkit.getScheduler().cancelTask(taskId);
    }

    public boolean isExecuted() {
        return executed;
    }
}
