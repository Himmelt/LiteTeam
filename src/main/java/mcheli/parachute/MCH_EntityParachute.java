package mcheli.parachute;

import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.World;

public class MCH_EntityParachute extends Entity {

    private double speedMultiplier;
    private int paraPosRotInc;
    private double paraX;
    private double paraY;
    private double paraZ;
    private double paraYaw;
    private double paraPitch;
    public Entity user;
    public int onGroundCount;

    public MCH_EntityParachute(World world) {
        super(world);
    }

    public MCH_EntityParachute(World world, double x, double y, double z) {
        super(world);
    }

    protected void c() {
    }

    protected void a(NBTTagCompound nbtTagCompound) {
    }

    protected void b(NBTTagCompound nbtTagCompound) {
    }

    public void setType(int n) {
    }
}
