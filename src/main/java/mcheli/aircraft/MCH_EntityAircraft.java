package mcheli.aircraft;

import mcheli.MCH_Camera;
import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.World;

public class MCH_EntityAircraft extends Entity {
    public MCH_Camera camera;

    public MCH_EntityAircraft(World world) {
        super(world);
    }

    public void setTypeName(String name) {
    }

    public MCH_EntityAircraft setTextureName(String name) {
        return this;
    }

    protected void c() {
    }

    protected void a(NBTTagCompound nbtTagCompound) {
    }

    protected void b(NBTTagCompound nbtTagCompound) {
    }
}
