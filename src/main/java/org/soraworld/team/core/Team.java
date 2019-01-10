package org.soraworld.team.core;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeMap;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.node.Setting;
import org.soraworld.team.event.JoinApplicationEvent;
import org.soraworld.team.manager.TeamManager;
import org.soraworld.team.teleport.TeleportTask;
import org.soraworld.team.teleport.TeleportUtil;

import java.lang.reflect.Field;
import java.util.*;

import static org.soraworld.team.LiteTeam.residenceApi;
import static org.soraworld.violet.util.ChatColor.RESET;

public class Team {

    @Setting
    private int size = 5;
    @Setting
    private double balance = 0;
    @Setting
    private boolean pvp = false;
    private String display;
    @Setting
    private HashSet<String> members = new HashSet<>();
    @Setting
    private HashSet<String> blackList = new HashSet<>();
    @Setting
    private LinkedHashSet<String> applications = new LinkedHashSet<>();

    private OfflinePlayer leader;
    private TeleportTask task;
    private final TeamManager manager;
    private final HashSet<UUID> invites = new HashSet<>();

    public Team(OfflinePlayer leader, TeamManager manager) {
        this.leader = leader;
        this.manager = manager;
    }

    public boolean isLeader(Player player) {
        return player.getUniqueId().equals(leader.getUniqueId());
    }

    public boolean isLeader(String player) {
        return player.equals(leader.getName());
    }

    public String getName() {
        return leader.getName();
    }

    public Player getLeader() {
        return leader.getPlayer();
    }

    public String getDisplay() {
        return leader.getName();
        //return display == null ? "" : ChatColor.RED + display + RESET;//.replace('&', ChatColor.COLOR_CHAR);
    }

    public void setDisplay(String display) {
        //if (!display.endsWith("&r")) display += "&r";
        this.display = display;
    }

    public boolean addMember(String player) {
        if (blackList.contains(player)) return false;
        if (members.size() < size) {
            members.add(player);
            return true;
        }
        return false;
    }

    public boolean isBlack(String name) {
        return blackList.contains(name);
    }

    public void addBlack(String player) {
        if (!members.contains(player) && !leader.getName().equals(player)) {
            blackList.add(player);
        }
    }

    public void removeBlack(String player) {
        blackList.remove(player);
    }

    public boolean hasMember(String name) {
        return isLeader(name) || members.contains(name);
    }

    public boolean hasMember(Player player) {
        return isLeader(player) || members.contains(player.getName());
    }

    public void delMember(String player) {
        members.remove(player);
    }

    public int getAmount() {
        return members.size();
    }

    public void addJoinApplication(String username) {
        applications.add(username);
        Bukkit.getPluginManager().callEvent(new JoinApplicationEvent(leader.getName(), username));
    }

    public void notifyApplication(Player player) {
        for (String applicant : applications) {
            handleApplication(player, applicant);
        }
    }

    public void handleApplication(Player handler, String applicant) {
        if (handler != null) {
            manager.sendHandleMessage(handler, applicant);
            return;
        }
        handler = leader.getPlayer();
        if (handler != null) {
            manager.sendHandleMessage(handler, applicant);
        }
    }

    public boolean hasApplication(String applicant) {
        return applications.contains(applicant);
    }

    public void closeApplication(String applicant) {
        applications.remove(applicant);
    }

    public void notifyLeave(String username) {
        Player handler = leader.getPlayer();
        if (handler != null) {
            manager.sendKey(handler, "guild.notifyLeave", username);
        }
    }

    public void showMemberList(CommandSender sender) {
        manager.sendKey(sender, "list.head", getDisplay());
        manager.sendKey(sender, "list.leader", leader.getName());
        for (String member : members) {
            manager.sendKey(sender, "list.member", member);
        }
        manager.sendKey(sender, "list.foot");
    }

    public boolean equals(Object obj) {
        return this == obj || obj instanceof Team && this.leader.equals(((Team) obj).leader);
    }

    public static Team deserialize(Node node, String leader, TeamManager manager) {
        if (node instanceof NodeMap) {
            Team guild = new Team(Bukkit.getOfflinePlayer(leader), manager);
            ((NodeMap) node).modify(guild);
            if (!guild.display.endsWith("&r") && !guild.display.endsWith(RESET.toString())) guild.display += "&r";
            return guild;
        }
        return null;
    }

    public static NodeMap serialize(Team guild, Options options) {
        NodeMap node = new NodeMap(options);
        if (guild != null) node.extract(guild);
        return node;
    }

    private void setLeader(Player player) {
        this.leader = player;
        members.remove(player.getName());
    }

    public void sendInvite(Player man, Player target) {
        invites.add(target.getUniqueId());
        manager.sendInviteMessage(man, target, this);
    }

    public boolean acceptInvite(Player player) {
        if (invites.contains(player.getUniqueId())) {
            invites.remove(player.getUniqueId());
            return addMember(player.getName());
        } else return false;
    }

    public boolean isInvited(Player player) {
        return invites.contains(player.getUniqueId());
    }

    public void unInvite(UUID uuid) {
        invites.remove(uuid);
    }

    public void unInviteAll() {
        invites.clear();
    }

    public String getHover() {
        return manager.trans("info.display", getDisplay()) + '\n' +
                manager.trans("info.leader", leader.getName());
    }

    public String getHomeName() {
        return "TeamHome_" + leader.getName();
    }

    public String getVariable(String params) {
        switch (params) {
            case "leader":
                return leader.getName();
            case "display":
                return getDisplay();
            case "balance":
                return String.valueOf(balance);
            case "mem_amount":
                return String.valueOf(getAmount());
            case "mem_max":
                return String.valueOf(size);
            default:
                return "no variable";
        }
    }

    private void renameHome(String oldName, String newName, String oldLeader, String newLeader) {
        if (residenceApi) {
            Residence plugin = Residence.getInstance();
            ResidenceManager apiR = plugin.getResidenceManager();
            ClaimedResidence home = apiR.getByName(oldName);
            if (home != null) {
                //ResidenceRenameEvent event = new ResidenceRenameEvent(home, newName, oldName);
                //Bukkit.getPluginManager().callEvent(event);
                apiR.removeChunkList(oldName);
                home.setName(newName);
                try {
                    Field residences = ResidenceManager.class.getDeclaredField("residences");
                    residences.setAccessible(true);
                    Map map = (Map) residences.get(apiR);
                    map.put(newName.toLowerCase(), home);
                    map.remove(oldName.toLowerCase());
                    apiR.calculateChunks(newName);
                    plugin.getSignUtil().updateSignResName(home);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                ResidencePermissions perm = home.getPermissions();
                perm.removeAllPlayerFlags(oldLeader);
                perm.removeAllPlayerFlags(newLeader);
                perm.setPlayerFlag(newLeader, "admin", FlagPermissions.FlagState.TRUE);
            }
        }
    }

    public double getEco() {
        return balance;
    }

    public void setEco(double amount) {
        balance = amount;
    }

    public boolean hasEco(double amount) {
        return manager.ignoreNoEco || balance >= amount;
    }

    public void giveEco(double amount) {
        balance += amount;
    }

    public boolean takeEco(double amount) {
        return balance >= amount ? (balance -= amount) >= 0 : manager.ignoreNoEco;
    }

    public void convoke(String message) {
        for (String mem : members) {
            Player player = Bukkit.getPlayer(mem);
            if (player != null) manager.sendConvoke(player, message);
        }
    }

    public UUID getUUID() {
        return leader.getUniqueId();
    }

    public List<String> getMembers() {
        ArrayList<String> list = new ArrayList<>();
        list.add(leader.getName());
        list.addAll(members);
        return list;
    }

    public void teamChat(Player source, String message) {
        if (source != null) message = "[" + source.getName() + "] " + message;
        else message = manager.trans("teamPrefix") + message;
        Player player = leader.getPlayer();
        if (player != null) player.sendMessage(message);
        for (String mem : members) {
            player = Bukkit.getPlayer(mem);
            if (player != null) player.sendMessage(message);
        }
    }

    public boolean startTpTask() {
        if (task != null) {
            if (task.isExecuted()) {
                task.cancel();
            } else return false;
        }
        task = new TeleportTask(manager, leader.getName(), manager.tprDelay);
        task.taskId = Bukkit.getScheduler().runTaskTimer(manager.getPlugin(), task, 20, 20).getTaskId();
        sendTprInvite();
        return true;
    }

    public void sendTprInvite() {
        for (String member : members) {
            Player player = Bukkit.getPlayer(member);
            if (player != null) manager.sendTprMessage(player);
        }
    }

    public void acceptTpr(UUID uuid) {
        if (task != null && !uuid.equals(leader.getUniqueId())) task.accepts.add(uuid);
    }

    public void teamTpr() {
        Player leader = this.leader.getPlayer();
        if (leader != null) {
            Location location = TeleportUtil.randomLocation(leader.getLocation(), manager.minRadius, manager.maxRadius, manager.fixHeight, true);
            if (manager.isDebug()) manager.console("RandomLocation:" + location);
            boolean flag = TeleportUtil.teleportWithParachute(leader, location);
            if (manager.isDebug()) manager.console("teleportWithParachute leader: " + leader.getName() + " : " + flag);
            for (UUID uuid : task.accepts) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && (!manager.tprSameWorld || leader.getWorld().equals(player.getWorld()))) {
                    flag = TeleportUtil.teleportWithParachute(player, location);
                    if (manager.isDebug()) manager.console("teleportWithParachute for " + player.getName() + " : " + flag);
                }
            }
            task.accepts.clear();
        }
    }

    public boolean canAccept() {
        return task != null && !task.isExecuted();
    }

    public void cancelTpr() {
        task.cancel();
        task = null;
    }

    public boolean isPvP() {
        return pvp;
    }

    public void setPvP(boolean pvp) {
        this.pvp = pvp;
    }
}
