package org.soraworld.team.teleport;

import mcheli.parachute.MCH_EntityParachute;
import mcheli.tank.MCH_EntityTank;
import mcheli.tank.MCH_TankInfo;
import mcheli.tank.MCH_TankInfoManager;
import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.MathHelper;
import net.minecraft.server.v1_7_R4.WorldServer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class TeleportUtil {

    private static Field yaw, motX, motY, motZ, fallDistance, speedMultiplier;

    static {
        try {
            yaw = Entity.class.getDeclaredField("yaw");
        } catch (Throwable ignored) {
            try {
                yaw = Entity.class.getDeclaredField("field_70177_z");
                yaw.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        try {
            motX = Entity.class.getDeclaredField("motX");
        } catch (Throwable ignored) {
            try {
                motX = Entity.class.getDeclaredField("field_70159_w");
                motX.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        try {
            motY = Entity.class.getDeclaredField("motY");
        } catch (Throwable ignored) {
            try {
                motY = Entity.class.getDeclaredField("field_70181_x");
                motY.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        try {
            motZ = Entity.class.getDeclaredField("motZ");
        } catch (Throwable ignored) {
            try {
                motZ = Entity.class.getDeclaredField("field_73135_l");
                motZ.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        try {
            fallDistance = Entity.class.getDeclaredField("fallDistance");
        } catch (Throwable ignored) {
            try {
                fallDistance = Entity.class.getDeclaredField("field_70143_R");
                fallDistance.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        try {
            speedMultiplier = MCH_EntityParachute.class.getDeclaredField("speedMultiplier");
            speedMultiplier.setAccessible(true);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static boolean teleportWithParachute(Player player, Location location) {
        if (player.teleport(location)) {
            World world = player.getWorld();
            if (world instanceof CraftWorld && player instanceof CraftPlayer) {
                WorldServer worldServer = ((CraftWorld) world).getHandle();
                EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                spawnParachute(worldServer, entityPlayer);
            }
            return true;
        }
        return false;
    }

    public static void spawnParachute(WorldServer world, EntityPlayer player) {
        if (player.vehicle == null) {
            int i = MathHelper.floor(player.locX / 16.0D);
            int j = MathHelper.floor(player.locZ / 16.0D);
            world.chunkProvider.getChunkAt(i, j);
            double x = player.locX + 0.5D;
            double y = player.locY + 3.5D;
            double z = player.locZ + 0.5D;
            MCH_EntityParachute entity = new MCH_EntityParachute(world, x, y, z);
            try {
                yaw.set(entity, yaw.get(player));
                motX.set(entity, motX.get(player));
                motY.set(entity, motY.get(player));
                motZ.set(entity, motZ.get(player));
                fallDistance.set(entity, fallDistance.get(player));
                fallDistance.set(player, 0.0F);
                speedMultiplier.set(entity, 0.1F);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            entity.user = player;
            entity.setType(1);
            world.addEntity(entity);
        }
    }

    public static Location randomLocation(Location center, int minR, int maxR, int locY, boolean useY) {
        double radius = getRand(minR, maxR);
        double theta = Math.random() * 6.28318531D;
        int x = (int) (center.getBlockX() + Math.sin(theta) * radius);
        int z = (int) (center.getBlockZ() + Math.cos(theta) * radius);
        int y = useY ? locY : getY(center.getWorld(), x, center.getBlockY(), z, radius);
        y = y < 1 ? 1 : y > 255 ? 255 : y;
        return new Location(center.getWorld(), x, y, z);
    }

    private static double getRand(int min, int max) {
        return min + Math.abs((max - min) * Math.random());
    }

    private static int getY(World world, int x, int y, int z, double radius) {
        Block block;
        for (int i = -1; i <= radius && y + i < 255 && y - i > 1; i++) {
            block = world.getBlockAt(x, y + i, z);
            if (block.getType().isSolid() || block.isLiquid()) {
                block = world.getBlockAt(x, y + i + 1, z);
                if (!block.getType().isOccluding()) {
                    block = world.getBlockAt(x, y + i + 2, z);
                    if (!block.getType().isOccluding()) return y + i + 1;
                }
            }
            block = world.getBlockAt(x, y - i, z);
            if (!block.getType().isOccluding()) {
                block = world.getBlockAt(x, y - i - 1, z);
                if (!block.getType().isOccluding()) {
                    block = world.getBlockAt(x, y - i - 2, z);
                    if (block.getType().isSolid() || block.isLiquid())
                        return y - i - 1;
                }
            }
        }
        return -1;
    }

    public MCH_EntityTank createTank(World bukkitWorld, double x, double y, double z, String name) {
        net.minecraft.server.v1_7_R4.World world = ((CraftWorld) bukkitWorld).getHandle();
        MCH_TankInfo info = MCH_TankInfoManager.get(name);
        if (info == null) {
            return null;
        } else {
            MCH_EntityTank tank = new MCH_EntityTank(world);
            tank.setPosition(x, y + (double) tank.height, z);
            tank.lastX = x;
            tank.lastY = y;
            tank.lastZ = z;
            tank.camera.setPosition(x, y, z);
            tank.setTypeName(info.name);
            tank.setTextureName(info.getTextureName());
            return tank;
        }
    }
}
