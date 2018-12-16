package org.soraworld.team.manager;

import net.minecraft.server.v1_7_R4.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.soraworld.hocon.node.FileNode;
import org.soraworld.hocon.node.Setting;
import org.soraworld.team.core.Team;
import org.soraworld.team.economy.Economy;
import org.soraworld.team.flans.Flans;
import org.soraworld.violet.manager.SpigotManager;
import org.soraworld.violet.plugin.SpigotPlugin;
import org.soraworld.violet.util.ChatColor;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.server.v1_7_R4.EnumClickAction.RUN_COMMAND;
import static net.minecraft.server.v1_7_R4.EnumHoverAction.SHOW_TEXT;
import static org.soraworld.team.core.Team.deserialize;
import static org.soraworld.team.core.Team.serialize;

public class TeamManager extends SpigotManager {

    @Setting(comment = "comment.ecoType")
    private String ecoType = "Vault";
    @Setting(comment = "comment.ignoreNoEco")
    public boolean ignoreNoEco = false;
    @Setting(comment = "comment.teamCost")
    public int teamCost = 0;
    //@Setting(comment = "comment.maxDisplay")
    public int maxDisplay = 10;
    @Setting(comment = "comment.residencePrice")
    public float residencePrice = 1.0F;
    @Setting(comment = "comment.textCommand")
    private String textCommand = "/team";
    @Setting(comment = "comment.tprDelay")
    public int tprDelay = 20;
    @Setting(comment = "comment.tprSameWorld")
    public boolean tprSameWorld = true;
    @Setting(comment = "comment.minRadius")
    public int minRadius = 10;
    @Setting(comment = "comment.maxRadius")
    public int maxRadius = 40;
    @Setting(comment = "comment.fixHeight")
    public int fixHeight = 230;

    private final Path guildFile;
    private final HashMap<UUID, Team> teams = new HashMap<>();
    private final HashMap<String, Team> guilds = new HashMap<>();

    private static final Pattern FORMAT = Pattern.compile("((?<![&|\\u00A7])[&|\\u00A7][0-9a-fk-or])+");

    public TeamManager(SpigotPlugin plugin, Path path) {
        super(plugin, path);
        guildFile = path.resolve("guilds.conf");
    }

    public boolean save() {
        saveGuild();
        return super.save();
    }

    @Nonnull
    public ChatColor defChatColor() {
        return ChatColor.AQUA;
    }

    public void afterLoad() {
        loadGuild();
        Economy.checkEconomy(this, ecoType, ignoreNoEco);
        Flans.checkFlans(this);
    }

    public void saveGuild() {
        FileNode node = new FileNode(guildFile.toFile(), options);
        for (Map.Entry<String, Team> entry : guilds.entrySet()) {
            node.set(entry.getKey(), serialize(entry.getValue(), options));
        }
        try {
            node.save();
        } catch (Exception e) {
            if (isDebug()) e.printStackTrace();
            console(ChatColor.RED + "&cGuild file save exception !!!");
        }
    }

    public void loadGuild() {
        if (Files.notExists(guildFile)) {
            saveGuild();
            return;
        }
        FileNode node = new FileNode(guildFile.toFile(), options);
        try {
            node.load(false);
            teams.clear();
            guilds.clear();
            for (String leader : node.keys()) {
                Team guild = deserialize(node.get(leader), leader, this);
                if (guild != null) {
                    guilds.put(leader, guild);
                }
            }
        } catch (Exception e) {
            if (isDebug()) e.printStackTrace();
            console(ChatColor.RED + "Guild file load exception !!!");
        }
    }

    public void clearPlayer(Player player) {
        teams.remove(player.getUniqueId());
    }

    public void createGuild(Player player, String display) {
        Team guild = teams.get(player.getUniqueId());
        if (guild == null) {
            guild = new Team(player, this);
            guild.setDisplay(display);
            if (Economy.takeEco(player, teamCost)) {
                teams.put(player.getUniqueId(), guild);
                guilds.put(player.getName(), guild);
                sendKey(player, "create.success", teamCost);
                saveGuild();
            } else sendKey(player, "create.noEco", teamCost);
        } else sendKey(player, "create.inTeam");
    }

    public void joinGuild(Player player, String leader) {
        Team guild = teams.get(player.getUniqueId());
        if (guild == null) {
            guild = guilds.get(leader);
            if (guild != null) {
                if (!guild.hasMember(player)) {
                    if (!guild.isBlack(player.getName())) {
                        guild.addJoinApplication(player.getName());
                        sendKey(player, "application.send", guild.getDisplay());
                        saveGuild();
                    } else send(player, "You are in the Team's Blacklist.");
                } else sendKey(player, "player.alreadyJoined");
            } else sendKey(player, "guild.notExist");
        } else if (guild.equals(guilds.get(leader))) {
            sendKey(player, "player.alreadyJoined");
        } else sendKey(player, "player.inAnother");
    }

    public void leaveGuild(Player player, Team guild) {
        guild.delMember(player.getName());
        teams.remove(player.getUniqueId());
        saveGuild();
    }

    public void leaveGuild(String player, Team guild) {
        guild.delMember(player);
        teams.remove(Bukkit.getOfflinePlayer(player).getUniqueId());
        saveGuild();
    }

    public Team getTeam(String leader) {
        return guilds.get(leader);
    }

    public Team fetchTeam(Player player) {
        Team team = teams.get(player.getUniqueId());
        if (team == null) {
            for (Team guild : guilds.values()) {
                if (guild.hasMember(player)) {
                    team = guild;
                    teams.put(player.getUniqueId(), team);
                    break;
                }
            }
        }
        return team;
    }

    public Team fetchTeam(String player) {
        Team team = teams.get(Bukkit.getOfflinePlayer(player).getUniqueId());
        if (team == null) {
            for (Team guild : guilds.values()) {
                if (guild.hasMember(player)) {
                    team = guild;
                    teams.put(Bukkit.getOfflinePlayer(player).getUniqueId(), team);
                    break;
                }
            }
        }
        return team;
    }

    public void disband(final Team team) {
        guilds.remove(team.getName());
        final String display = team.getDisplay();
        teams.entrySet().removeIf(entry -> {
            if (team.equals(entry.getValue())) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) sendKey(player, "guild.disband", display);
                return true;
            } else return false;
        });
        saveGuild();
    }

    private static ChatModifier parseStyle(String text) {
        ChatModifier style = new ChatModifier();
        int length = text.length();
        for (int i = 1; i < length; i += 2) {
            switch (text.charAt(i)) {
                case '0':
                    style.setColor(EnumChatFormat.BLACK);
                    break;
                case '1':
                    style.setColor(EnumChatFormat.DARK_BLUE);
                    break;
                case '2':
                    style.setColor(EnumChatFormat.DARK_GREEN);
                    break;
                case '3':
                    style.setColor(EnumChatFormat.DARK_AQUA);
                    break;
                case '4':
                    style.setColor(EnumChatFormat.DARK_RED);
                    break;
                case '5':
                    style.setColor(EnumChatFormat.DARK_PURPLE);
                    break;
                case '6':
                    style.setColor(EnumChatFormat.GOLD);
                    break;
                case '7':
                    style.setColor(EnumChatFormat.GRAY);
                    break;
                case '8':
                    style.setColor(EnumChatFormat.DARK_GRAY);
                    break;
                case '9':
                    style.setColor(EnumChatFormat.BLUE);
                    break;
                case 'a':
                    style.setColor(EnumChatFormat.GREEN);
                    break;
                case 'b':
                    style.setColor(EnumChatFormat.AQUA);
                    break;
                case 'c':
                    style.setColor(EnumChatFormat.RED);
                    break;
                case 'd':
                    style.setColor(EnumChatFormat.LIGHT_PURPLE);
                    break;
                case 'e':
                    style.setColor(EnumChatFormat.YELLOW);
                    break;
                case 'f':
                    style.setColor(EnumChatFormat.WHITE);
                    break;
                case 'k':
                    style.setRandom(true);
                    break;
                case 'l':
                    style.setBold(true);
                    break;
                case 'm':
                    style.setStrikethrough(true);
                    break;
                case 'n':
                    style.setUnderline(true);
                    break;
                case 'o':
                    style.setItalic(true);
                    break;
                default:
                    style = new ChatModifier();
            }
        }
        return style;
    }

    public static IChatBaseComponent format(String text) {
        return format(text, null, null, null, null);
    }

    public static IChatBaseComponent format(String text, EnumClickAction ca, String cv, EnumHoverAction ha, String hv) {
        Matcher matcher = FORMAT.matcher(text);
        IChatBaseComponent component = new ChatComponentText("");
        int head = 0;
        ChatModifier style = new ChatModifier();
        while (matcher.find()) {
            component.addSibling(new ChatComponentText(text.substring(head, matcher.start()).replaceAll("&&", "&")).setChatModifier(style));
            style = parseStyle(matcher.group());
            head = matcher.end();
        }
        component.addSibling(new ChatComponentText(text.substring(head).replaceAll("&&", "&")).setChatModifier(style));
        if (ca != null && cv != null) {
            component.getChatModifier().setChatClickable(new ChatClickable(ca, cv));
        }
        if (ha != null && hv != null) {
            component.getChatModifier().a(new ChatHoverable(ha, format(hv)));
        }
        return component;
    }

    public void sendMessage(Player player, IChatBaseComponent... siblings) {
        EntityPlayer handle = ((CraftPlayer) player).getHandle();
        IChatBaseComponent text = new ChatComponentText(colorHead);
        for (IChatBaseComponent component : siblings) text.addSibling(component);
        handle.b(text);
    }

    public void sendHandleMessage(Player handler, String applicant) {
        sendMessage(handler,
                format(trans("application.receive", applicant)),
                format(trans("acceptText"),
                        RUN_COMMAND, textCommand + " accept join " + applicant,
                        SHOW_TEXT, trans("acceptHover")),
                format(trans("rejectText"),
                        RUN_COMMAND, textCommand + " reject join " + applicant,
                        SHOW_TEXT, trans("rejectHover"))
        );
    }

    public void updateGuild(Team guild, Consumer<Team> consumer) {
        consumer.accept(guild);
    }

    public void sendInviteMessage(Player man, Player target, Team guild) {
        sendMessage(target,
                format(trans("invite.receive", man.getName(), guild.getDisplay())),
                format(trans("acceptText"),
                        RUN_COMMAND, textCommand + " accept invite " + guild.getName(),
                        SHOW_TEXT, trans("acceptHover")),
                format(trans("rejectText"),
                        RUN_COMMAND, textCommand + " reject invite " + guild.getName(),
                        SHOW_TEXT, trans("rejectHover"))
        );
    }

    public void sendConvoke(Player player, String message) {
        sendMessage(player,
                format(trans("convokeMessage", message)),
                format(trans("gotoHome"),
                        RUN_COMMAND, textCommand + " home",
                        SHOW_TEXT, trans("gotoHome"))
        );
    }

    public void sendTprMessage(Player player) {
        sendMessage(player,
                format(trans("tpr.receiveTpr")),
                format(trans("acceptText"),
                        RUN_COMMAND, textCommand + " tpraccept",
                        SHOW_TEXT, trans("acceptHover")
                )
        );
    }
}
