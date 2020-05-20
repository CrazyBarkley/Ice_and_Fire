package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntityMP;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CyclopsAITargetSheepPlayers<T extends LivingEntity> extends TargetGoal {
    protected final Class<T> targetClass;
    protected final CyclopsAITargetSheepPlayers.Sorter sorter;
    protected final Predicate<? super T> targetEntitySelector;
    private final int targetChance;
    protected T targetEntity;

    public CyclopsAITargetSheepPlayers(MobEntity creature, Class<T> classTarget, boolean checkSight) {
        this(creature, classTarget, checkSight, true);
    }

    public CyclopsAITargetSheepPlayers(MobEntity creature, Class<T> classTarget, boolean checkSight, boolean onlyNearby) {
        this(creature, classTarget, 10, checkSight, onlyNearby, null);
    }

    public CyclopsAITargetSheepPlayers(MobEntity creature, Class<T> classTarget, int chance, boolean checkSight, boolean onlyNearby, @Nullable final Predicate<? super T> targetSelector) {
        super(creature, checkSight, onlyNearby);
        this.targetClass = classTarget;
        this.targetChance = chance;
        this.sorter = new CyclopsAITargetSheepPlayers.Sorter(creature);
        this.setMutexBits(1);
        this.targetEntitySelector = new Predicate<T>() {
            public boolean apply(@Nullable T p_apply_1_) {
                if (p_apply_1_ == null) {
                    return false;
                } else if (targetSelector != null && !targetSelector.apply(p_apply_1_)) {
                    return false;
                } else {
                    return EntitySelectors.NOT_SPECTATING.apply(p_apply_1_) && CyclopsAITargetSheepPlayers.this.isSuitableTarget(p_apply_1_, false);
                }
            }
        };
    }

    /**
     * Returns whether the Goal should begin execution.
     */
    public boolean shouldExecute() {
        if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0) {
            return false;
        } else if (this.targetClass != PlayerEntity.class && this.targetClass != PlayerEntityMP.class) {
            List<T> list = this.taskOwner.world.getEntitiesWithinAABB(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);

            if (list.isEmpty()) {
                return false;
            } else {
                Collections.sort(list, this.sorter);
                this.targetEntity = list.get(0);
                return true;
            }
        } else {
            this.targetEntity = (T) this.taskOwner.world.getNearestAttackablePlayer(this.taskOwner.getPosX(), this.taskOwner.getPosY() + (double) this.taskOwner.getEyeHeight(), this.taskOwner.getPosZ(), this.getTargetDistance(), this.getTargetDistance(), new Function<PlayerEntity, Double>() {
                @Nullable
                public Double apply(@Nullable PlayerEntity player) {
                    ItemStack helmet = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
                    ItemStack chestplate = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
                    ItemStack leggings = player.getItemStackFromSlot(EquipmentSlotType.LEGS);
                    ItemStack boots = player.getItemStackFromSlot(EquipmentSlotType.FEET);
                    double subHelm = helmet != null && helmet.getItem() != null && helmet.getItem() == IafItemRegistry.SHEEP_HELMET ? 0.2D : 0;
                    double subChest = chestplate != null && chestplate.getItem() != null && chestplate.getItem() == IafItemRegistry.SHEEP_CHESTPLATE ? 0.2D : 0;
                    double subLegs = leggings != null && leggings.getItem() != null && leggings.getItem() == IafItemRegistry.SHEEP_LEGGINGS ? 0.2D : 0;
                    double subBoots = boots != null && boots.getItem() != null && boots.getItem() == IafItemRegistry.SHEEP_BOOTS ? 0.2D : 0;
                    double subSneaking = player.isShiftKeyDown() ? 0.2D : 0;
                    return 1.0D - subHelm - subChest - subLegs - subBoots - subSneaking;
                }
            }, (Predicate<PlayerEntity>) this.targetEntitySelector);
            return this.targetEntity != null;
        }
    }

    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        return this.taskOwner.getBoundingBox().grow(targetDistance, targetDistance, targetDistance);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.taskOwner.setAttackTarget(this.targetEntity);
        super.startExecuting();
    }

    public static class Sorter implements Comparator<Entity> {
        private final Entity entity;

        public Sorter(Entity entityIn) {
            this.entity = entityIn;
        }

        public int compare(Entity p_compare_1_, Entity p_compare_2_) {
            double d0 = this.entity.getDistanceSq(p_compare_1_);
            double d1 = this.entity.getDistanceSq(p_compare_2_);

            if (d0 < d1) {
                return -1;
            } else {
                return d0 > d1 ? 1 : 0;
            }
        }
    }
}