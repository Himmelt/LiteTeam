package org.soraworld.guild.core;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class TeamGuild {

    private int size = 5;
    private String leader;
    private String display;
    private String description;
    private final String name;
    private final HashSet<String> members = new HashSet<>();

    public TeamGuild(@Nonnull String name, @Nonnull String leader) {
        this.name = name;
        this.leader = leader;
    }

    public String getName() {
        return name;
    }

    public boolean isLeader(String player) {
        return leader.equals(player);
    }

    @Nonnull
    public String getLeader() {
        return leader;
    }

    public void setLeader(@Nonnull String leader) {
        this.leader = leader;
    }

    @Nonnull
    public String getDisplay() {
        return display == null ? "" : display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public void addMember(String player) {
        members.add(player);
    }

    public boolean hasMember(String player) {
        return members.contains(player);
    }

    public void delMember(String player) {
        members.remove(player);
    }

    public int getSize() {
        return size < 1 ? 1 : size;
    }

    public void setSize(int size) {
        this.size = size < 1 ? 1 : size;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}