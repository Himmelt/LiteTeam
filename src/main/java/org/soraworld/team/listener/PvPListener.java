package org.soraworld.team.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.soraworld.team.core.Team;
import org.soraworld.team.flans.Flans;
import org.soraworld.team.manager.TeamManager;

public class PvPListener implements Listener {

    private final TeamManager manager;

    public PvPListener(TeamManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();
        if (damagee instanceof Player) {
            Team guild = manager.fetchTeam((Player) damagee);
            if (guild == null || guild.isPvP()) return;
            if (damager instanceof Player && guild.hasMember((Player) damager)) {
                event.setCancelled(true);
                return;
            }
            if (damager instanceof Projectile) {
                ProjectileSource source = ((Projectile) damager).getShooter();
                if (source instanceof Player && guild.hasMember((Player) source)) {
                    event.setCancelled(true);
                }
                return;
            }
            // Flans support
            Player shooter = Flans.getShooter(damager);
            if (shooter != null && guild.hasMember(shooter)) {
                event.setCancelled(true);
            }
        }
    }
}
