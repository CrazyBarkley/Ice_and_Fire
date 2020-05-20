package com.github.alexthe666.iceandfire.world;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.MyrmexHive;
import com.google.common.collect.Lists;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;

public class DragonPosWorldData extends WorldSavedData {

    private static final String IDENTIFIER = "iceandfire_dragonPositions";
    protected final Map<UUID, BlockPos> lastDragonPositions = new HashMap<>();
    private World world;
    private int tickCounter;

    public DragonPosWorldData(String name) {
        super(name);
    }

    public DragonPosWorldData(World world) {
        super(IDENTIFIER);
        this.world = world;
        this.markDirty();
    }

    public static DragonPosWorldData get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        DragonPosWorldData instance = (DragonPosWorldData) storage.getOrLoadData(DragonPosWorldData.class, IDENTIFIER);

        if (instance == null) {
            instance = new DragonPosWorldData(world);
            storage.setData(IDENTIFIER, instance);
        }
        instance.markDirty();
        return instance;
    }

    public void addDragon(UUID uuid, BlockPos pos) {
        lastDragonPositions.put(uuid, pos);
        this.markDirty();
    }

    public void removeDragon(UUID uuid) {
        lastDragonPositions.remove(uuid);
        this.markDirty();
    }

    public BlockPos getDragonPos(UUID uuid) {
        return lastDragonPositions.get(uuid);
    }

    public void debug() {
        IceAndFire.logger.warn(lastDragonPositions.toString());
    }


    public void tick() {
        ++this.tickCounter;
    }

    public void readFromNBT(CompoundNBT nbt) {
        this.tickCounter = nbt.getInteger("Tick");
        ListNBT nbttaglist = nbt.getList("DragonMap", 10);
        this.lastDragonPositions.clear();
        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            CompoundNBT CompoundNBT = nbttaglist.getCompoundTagAt(i);
            UUID uuid = CompoundNBT.getUniqueId("DragonUUID");
            BlockPos pos = new BlockPos(CompoundNBT.getInteger("DragonPosX"), CompoundNBT.getInteger("DragonPosY"), CompoundNBT.getInteger("DragonPosZ"));
            this.lastDragonPositions.put(uuid, pos);
        }
    }

    public CompoundNBT writeToNBT(CompoundNBT compound) {
        compound.setInteger("Tick", this.tickCounter);
        ListNBT nbttaglist = new ListNBT();
        for (Map.Entry<UUID, BlockPos> pair : lastDragonPositions.entrySet()) {
            CompoundNBT CompoundNBT = new CompoundNBT();
            CompoundNBT.setUniqueId("DragonUUID", pair.getKey());
            CompoundNBT.setInteger("DragonPosX", pair.get().getX());
            CompoundNBT.setInteger("DragonPosY", pair.get().getY());
            CompoundNBT.setInteger("DragonPosZ", pair.get().getZ());
            nbttaglist.appendTag(CompoundNBT);
        }
        compound.setTag("DragonMap", nbttaglist);
        return compound;
    }
}
