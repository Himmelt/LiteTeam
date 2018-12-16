package org.soraworld.team.command;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.selection.SelectionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.soraworld.team.core.Team;
import org.soraworld.team.economy.Economy;
import org.soraworld.team.manager.TeamManager;
import org.soraworld.violet.command.Args;
import org.soraworld.violet.command.SpigotCommand;
import org.soraworld.violet.command.Sub;

import java.util.UUID;

import static org.soraworld.team.LiteTeam.residenceApi;

public final class CommandTeam {

    @Sub(onlyPlayer = true)
    public static void pvp(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        Team team = manager.fetchTeam(player);
        if (team != null) {
            if (team.isLeader(player)) {
                team.setPvP(!team.isPvP());
                manager.sendKey(player, team.isPvP() ? "pvpOn" : "pvpOff");
            } else manager.sendKey(player, "leader.notLeader");
        } else manager.sendKey(player, "player.notInAny");
    }

    @Sub
    public static void info(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        if (args.empty()) {
            if (sender instanceof Player) {
                Team guild = manager.fetchTeam((Player) sender);
                if (guild != null) sender.sendMessage(guild.getHover());
                else manager.sendKey(sender, "player.notInAny");
            } else manager.sendKey(sender, "invalidArgs");
        } else {
            Team guild = manager.fetchTeam(args.first());
            if (guild != null) sender.sendMessage(guild.getHover());
            else manager.sendKey(sender, "guild.notExist");
        }
    }

    @Sub
    public static void disband(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        if (args.empty()) {
            if (sender instanceof Player) {
                Team guild = manager.getTeam(sender.getName());
                if (guild != null) {
                    manager.disband(guild);
                    if (!guild.hasMember((Player) sender)) {
                        manager.sendKey(sender, "guild.disband", guild.getDisplay());
                    }
                } else manager.sendKey(sender, "player.ownNone");
            } else manager.sendKey(sender, "emptyArgs");
        } else {
            Team guild = manager.getTeam(args.first());
            if (guild != null) {
                if (sender.hasPermission(manager.defAdminPerm()) || guild.isLeader(sender.getName())) {
                    manager.disband(guild);
                    if (sender instanceof Player) {
                        if (!guild.hasMember((Player) sender)) {
                            manager.sendKey(sender, "guild.disband", guild.getDisplay());
                        }
                    } else manager.sendKey(sender, "guild.disband", guild.getDisplay());
                } else manager.sendKey(sender, "player.notLeaderAdmin");
            } else manager.sendKey(sender, "guild.notExist");
        }
    }

    @Sub(path = "eco.give", perm = "admin")
    public static void eco_give(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        if (args.notEmpty()) {
            if (args.size() == 2) {
                Team guild = manager.getTeam(args.first());
                if (guild != null) {
                    try {
                        float amount = Float.valueOf(args.get(1));
                        manager.updateGuild(guild, g -> g.giveEco(amount));
                        manager.sendKey(sender, "guild.eco.give", guild.getDisplay(), amount);
                    } catch (Throwable ignored) {
                        manager.sendKey(sender, "invalidFloat");
                    }
                } else manager.sendKey(sender, "guild.notExist");
            } else manager.sendKey(sender, "invalidArgs");
        } else manager.sendKey(sender, "emptyArgs");
    }

    @Sub(path = "eco.take", perm = "admin")
    public static void eco_take(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        if (args.notEmpty()) {
            if (args.size() == 2) {
                Team guild = manager.getTeam(args.first());
                if (guild != null) {
                    try {
                        float amount = Float.valueOf(args.get(1));
                        if (guild.hasEco(amount)) {
                            manager.updateGuild(guild, g -> g.takeEco(-1 * amount));
                            manager.sendKey(sender, "guild.eco.take", guild.getDisplay(), amount);
                        } else manager.sendKey(sender, "guild.eco.none", guild.getDisplay(), guild.getEco());
                    } catch (Throwable ignored) {
                        manager.sendKey(sender, "invalidFloat");
                    }
                } else manager.sendKey(sender, "guild.notExist");
            } else manager.sendKey(sender, "invalidArgs");
        } else manager.sendKey(sender, "emptyArgs");
    }

    @Sub(onlyPlayer = true)
    public static void create(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        manager.createGuild(player, player.getName());
    }

    @Sub(onlyPlayer = true)
    public static void donate(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        if (args.notEmpty()) {
            Team guild = manager.fetchTeam(player);
            if (guild != null) {
                try {
                    int amount = Integer.valueOf(args.first());
                    if (Economy.hasEnough(player, amount)) {
                        Economy.takeEco(player, amount);
                        guild.giveEco(amount);
                        manager.sendKey(player, "guild.donate", amount);
                    } else manager.sendKey(player, "player.noEco");
                } catch (Throwable e) {
                    manager.sendKey(player, "invalidInt");
                }
            } else manager.sendKey(player, "player.notInAny");
        } else manager.sendKey(player, "emptyArgs");
    }

    @Sub(onlyPlayer = true)
    public static void chat(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        if (args.notEmpty()) {
            Team team = manager.fetchTeam(player);
            if (team != null) {
                team.teamChat(player, args.getContent());
            } else manager.sendKey(player, "player.notInAny");
        } else manager.sendKey(player, "emptyArgs");
    }

    @Sub(onlyPlayer = true)
    public static void home(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        if (residenceApi) {
            Team guild = manager.fetchTeam(player);
            if (guild != null) {
                ClaimedResidence res = Residence.getInstance().getResidenceManager().getByName(guild.getHomeName());
                if (res != null) {
                    Location loc = res.getTeleportLocation();
                    if (loc != null && player.teleport(loc)) manager.sendKey(player, "home.tpSuccess");
                    else manager.sendKey(player, "home.tpFailed");
                } else manager.sendKey(player, "home.notExist");
            } else manager.sendKey(player, "player.notInAny");
        } else manager.sendKey(player, "residence.notHook");
    }

    @Sub(onlyPlayer = true)
    public static void sethome(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        if (residenceApi) {
            Team guild = manager.getTeam(player.getName());
            if (guild != null) {
                ResidenceManager apiR = Residence.getInstance().getResidenceManager();
                SelectionManager apiS = Residence.getInstance().getSelectionManager();
                ClaimedResidence res = apiR.getByName(guild.getHomeName());
                if (res == null) {
                    if (apiS.hasPlacedBoth(player)) {
                        double amount = apiS.getSelectionCuboid(player).getSize() * manager.residencePrice;
                        if (guild.hasEco(amount)) {
                            String serverOwner = Residence.getInstance().getServerLandname();
                            String serverUUID = Residence.getInstance().getServerLandUUID();
                            if (apiR.addResidence(player, serverOwner, guild.getHomeName(), apiS.getPlayerLoc1(player), apiS.getPlayerLoc2(player), true)) {
                                res = apiR.getByName(guild.getHomeName());
                                res.getPermissions().setOwnerUUID(UUID.fromString(serverUUID));
                                //res.getPermissions().setOwner(serverOwner, false);
                                res.getPermissions().setPlayerFlag(player.getName(), "admin", FlagPermissions.FlagState.TRUE);
                                guild.takeEco(amount);
                                manager.sendKey(player, "home.created");
                            } else manager.sendKey(player, "home.createFailed");
                        } else manager.sendKey(player, "home.noEco");
                    } else manager.sendKey(player, "home.selectFirst");
                } else manager.sendKey(player, "home.alreadyExist");
            } else manager.sendKey(player, "player.ownNone");
        } else manager.sendKey(player, "residence.notHook");
    }

    @Sub(onlyPlayer = true)
    public static void delhome(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        if (residenceApi) {
            Team guild = manager.fetchTeam(player);
            if (guild != null) {
                ResidenceManager apiR = Residence.getInstance().getResidenceManager();
                ClaimedResidence res = apiR.getByName(guild.getHomeName());
                if (res != null) {
                    apiR.removeResidence(res);
                    manager.sendKey(player, "home.remove");
                } else manager.sendKey(player, "home.notExist");
            } else manager.sendKey(player, "player.notInAny");
        } else manager.sendKey(player, "residence.notHook");
    }

    @Sub(onlyPlayer = true)
    public static void join(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        if (args.empty()) manager.sendKey(player, "emptyArgs");
        else manager.joinGuild(player, args.first());
    }

    @Sub(path = "accept.join", onlyPlayer = true)
    public static void accept_join(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        if (args.empty()) {
            manager.sendKey(player, "emptyArgs");
            return;
        }
        Team guild = manager.fetchTeam(player);
        if (guild == null) {
            manager.sendKey(player, "player.notInAny");
            return;
        }
        if (guild.isLeader(player)) {
            String applicant = args.first();
            Team team = manager.fetchTeam(applicant);
            if (team != null) {
                manager.sendKey(player, "application.alreadyIn", applicant, team.getDisplay());
                guild.closeApplication(applicant);
                manager.saveGuild();
                return;
            }
            if (guild.hasApplication(applicant)) {
                if (guild.addMember(applicant)) {
                    manager.sendKey(player, "application.accept", applicant);
                    Player app = Bukkit.getPlayer(applicant);
                    if (app != null) {
                        manager.sendKey(app, "application.accepted", player.getName(), guild.getDisplay());
                    }
                } else {
                    manager.sendKey(player, "application.acceptMax");
                    Player app = Bukkit.getPlayer(applicant);
                    if (app != null) {
                        manager.sendKey(app, "application.acceptedMax", player.getName(), guild.getDisplay());
                    }
                }
                guild.closeApplication(applicant);
                manager.saveGuild();
            } else manager.sendKey(player, "application.none", applicant);
        } else manager.sendKey(player, "leader.notLeader");
    }

    @Sub(path = "reject.join", onlyPlayer = true)
    public static void reject_join(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        if (args.empty()) {
            manager.sendKey(player, "emptyArgs");
            return;
        }
        Team guild = manager.fetchTeam(player);
        if (guild == null) {
            manager.sendKey(player, "player.notInAny");
            return;
        }
        if (guild.isLeader(player)) {
            String applicant = args.first();
            if (guild.hasApplication(applicant)) {
                manager.sendKey(player, "application.reject", applicant);
                guild.closeApplication(applicant);
                manager.saveGuild();
                Player app = Bukkit.getPlayer(applicant);
                if (app != null) manager.sendKey(app, "application.rejected");
            } else manager.sendKey(player, "application.none", applicant);
        } else manager.sendKey(player, "leader.notLeader");
    }

    @Sub(onlyPlayer = true)
    public static void invite(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        if (args.notEmpty()) {
            Team guild = manager.fetchTeam(player);
            if (guild != null) {
                if (guild.isLeader(player)) {
                    Player target = Bukkit.getPlayer(args.first());
                    if (target != null) {
                        if (!guild.isBlack(target.getName())) {
                            guild.sendInvite(player, target);
                            manager.sendKey(player, "invite.send", target.getName());
                        } else manager.send(player, "Player " + target.getName() + " is in your Team's Blacklist.");
                    } else manager.sendKey(player, "player.offline", args.first());
                } else manager.sendKey(player, "leader.notLeader");
            } else manager.sendKey(player, "guild.notExist");
        } else manager.sendKey(player, "emptyArgs");
    }

    @Sub(onlyPlayer = true, path = "black.add")
    public static void black_add(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        if (args.notEmpty()) {
            Team guild = manager.fetchTeam(player);
            if (guild != null) {
                if (guild.isLeader(player)) {
                    guild.addBlack(args.first());
                    manager.send(sender, "Add " + args.first() + " to Team Blacklist.");
                } else manager.sendKey(player, "leader.notLeader");
            } else manager.sendKey(player, "guild.notExist");
        } else manager.sendKey(player, "emptyArgs");
    }

    @Sub(onlyPlayer = true, path = "black.remove")
    public static void black_remove(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        if (args.notEmpty()) {
            Team guild = manager.fetchTeam(player);
            if (guild != null) {
                if (guild.isLeader(player)) {
                    guild.removeBlack(args.first());
                    manager.send(sender, "Remove " + args.first() + " from Team Blacklist.");
                } else manager.sendKey(player, "leader.notLeader");
            } else manager.sendKey(player, "guild.notExist");
        } else manager.sendKey(player, "emptyArgs");
    }

    @Sub(onlyPlayer = true)
    public static void uninvite(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        if (args.notEmpty()) {
            Team guild = manager.fetchTeam(player);
            if (guild != null) {
                if (guild.isLeader(player)) {
                    if (args.first().equals("@ALL") && guild.isLeader(player)) {
                        guild.unInviteAll();
                        manager.sendKey(player, "invite.cancelAll");
                        return;
                    }
                    UUID target = Bukkit.getOfflinePlayer(args.first()).getUniqueId();
                    guild.unInvite(target);
                    manager.sendKey(player, "invite.cancel", args.first());
                } else manager.sendKey(player, "leader.notLeader");
            } else manager.sendKey(player, "guild.notExist");
        } else manager.sendKey(player, "emptyArgs");
    }

    @Sub(path = "accept.invite", onlyPlayer = true)
    public static void accept_invite(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        if (args.notEmpty()) {
            Team team = manager.fetchTeam(player);
            Team guild = manager.getTeam(args.first());
            if (team == null) {
                if (guild != null) {
                    if (guild.isInvited(player)) {
                        if (guild.acceptInvite(player)) {
                            manager.sendKey(player, "invite.accept", guild.getDisplay());
                        } else manager.sendKey(player, "invite.max");
                    } else manager.sendKey(player, "invite.notInvited");
                } else manager.sendKey(player, "guild.notExist");
            } else if (team.equals(guild)) {
                guild.acceptInvite(player);
                manager.sendKey(player, "player.alreadyJoined");
            } else manager.sendKey(player, "player.inAnother");
        } else manager.sendKey(player, "emptyArgs");
    }

    @Sub(path = "reject.invite", onlyPlayer = true)
    public static void reject_invite(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        if (args.notEmpty()) {
            Team guild = manager.getTeam(args.first());
            if (guild != null) {
                guild.unInvite(player.getUniqueId());
                manager.sendKey(player, "invite.reject");
            } else manager.sendKey(player, "guild.notExist");
        } else manager.sendKey(player, "emptyArgs");
    }

    @Sub(onlyPlayer = true)
    public static void convoke(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        Team guild = manager.getTeam(player.getName());
        if (guild != null) {
            guild.convoke(args.first());
        } else manager.sendKey(player, "player.ownNone");
    }

    @Sub(onlyPlayer = true)
    public static void leave(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        String username = player.getName();
        Team guild = manager.fetchTeam(player);
        if (guild != null) {
            if (guild.isLeader(username)) {
                manager.sendKey(player, "leader.cantLeave");
            } else {
                manager.leaveGuild(player, guild);
                manager.sendKey(player, "member.leave", guild.getDisplay());
                guild.notifyLeave(username);
            }
        } else manager.sendKey(player, "player.notInAny");
    }

    @Sub(onlyPlayer = true)
    public static void kick(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        if (args.notEmpty()) {
            Team guild = manager.fetchTeam(player);
            if (guild != null) {
                if (guild.isLeader(player)) {
                    String beKick = args.first();
                    if (!guild.isLeader(beKick)) {
                        if (guild.hasMember(beKick)) {
                            manager.leaveGuild(beKick, guild);
                            manager.sendKey(player, "manager.kickSuccess", beKick);
                            Player mmp = Bukkit.getPlayer(beKick);
                            if (mmp != null) manager.sendKey(mmp, "member.beKicked", guild.getDisplay());
                        } else manager.sendKey(player, "manager.noMember", beKick);
                    } else manager.sendKey(player, "manager.cantKickLeader");
                } else manager.sendKey(player, "leader.notLeader");
            } else manager.sendKey(player, "player.notInAny");
        } else manager.sendKey(player, "emptyArgs");
    }

    //@Sub(onlyPlayer = true)
    public static void display(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        Team guild = manager.getTeam(player.getName());
        if (guild == null) {
            manager.sendKey(player, "player.ownNone");
            return;
        }
        if (args.empty()) {
            manager.sendKey(player, "get.display", guild.getDisplay());
        } else {
            String text = args.first();
            try {
                if (text.getBytes("GB2312").length <= manager.maxDisplay) {
                    guild.setDisplay(text);
                    manager.sendKey(player, "set.display", text);
                    manager.saveGuild();
                } else {
                    manager.sendKey(player, "textTooLong", manager.maxDisplay);
                }
            } catch (Throwable e) {
                if (manager.isDebug()) e.printStackTrace();
                manager.sendKey(player, "EncodingException");
            }
        }
    }

    @Sub(onlyPlayer = true)
    public static void list(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        Team team = manager.fetchTeam(player);
        if (team != null) {
            team.showMemberList(player);
        } else manager.sendKey(player, "player.notInAny");
    }

    @Sub(onlyPlayer = true)
    public static void tpr(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        Team team = manager.fetchTeam(player);
        if (team != null) {
            if (team.isLeader(player)) {
                if (team.startTpTask()) {
                    manager.sendKey(player, "tpr.startTp", manager.tprDelay / 20);
                } else manager.sendKey(player, "tpr.inTprTask");
            } else manager.sendKey(player, "leader.notLeader");
        } else manager.sendKey(player, "player.notInAny");
    }

    @Sub(onlyPlayer = true)
    public static void tprcancel(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        Team team = manager.fetchTeam(player);
        if (team != null) {
            if (team.isLeader(player)) {
                team.cancelTpr();
                manager.sendKey(player, "tpr.tprCanceled");
            } else manager.sendKey(player, "leader.notLeader");
        } else manager.sendKey(player, "player.notInAny");
    }

    @Sub(onlyPlayer = true)
    public static void tpraccept(SpigotCommand self, CommandSender sender, Args args) {
        TeamManager manager = (TeamManager) self.manager;
        Player player = (Player) sender;
        Team team = manager.fetchTeam(player);
        if (team != null) {
            if (team.canAccept()) {
                team.acceptTpr(player.getUniqueId());
                manager.sendKey(player, "tpr.accept");
                Player leader = team.getLeader();
                if (manager.tprSameWorld && leader != null && !leader.getWorld().equals(player.getWorld())) {
                    manager.sendKey(player, "tpr.notInSameWorld");
                }
            } else manager.sendKey(player, "tpr.noTprTask");
        } else manager.sendKey(player, "player.notInAny");
    }
}
