package io.izzel.arclight.common.mixin.bukkit;

import com.destroystokyo.paper.entity.TargetEntityInfo;
import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.entity.LivingEntityBridge;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftLivingEntity.class, remap = false)
public abstract class CraftLivingEntityMixin {
    // @formatter:off
    @Shadow public abstract LivingEntity getHandle();
    // @formatter:on

    public Entity getTargetEntity(int maxDistance, boolean ignoreBlocks) {
        net.minecraft.world.phys.EntityHitResult rayTrace = rayTraceEntity(maxDistance, ignoreBlocks);
        return rayTrace == null ? null : ((EntityBridge) rayTrace.getEntity()).bridge$getBukkitEntity();
    }

    public TargetEntityInfo getTargetEntityInfo(int maxDistance, boolean ignoreBlocks) {
        net.minecraft.world.phys.EntityHitResult rayTrace = rayTraceEntity(maxDistance, ignoreBlocks);
        return rayTrace == null ? null : new TargetEntityInfo(((EntityBridge) rayTrace.getEntity()).bridge$getBukkitEntity(), new org.bukkit.util.Vector(rayTrace.getLocation().x, rayTrace.getLocation().y, rayTrace.getLocation().z));
    }

    public net.minecraft.world.phys.EntityHitResult rayTraceEntity(int maxDistance, boolean ignoreBlocks) {
        net.minecraft.world.phys.EntityHitResult rayTrace = ((LivingEntityBridge) getHandle()).bridge$getTargetEntity(maxDistance);
        if (rayTrace == null) {
            return null;
        }
        if (!ignoreBlocks) {
            net.minecraft.world.phys.HitResult rayTraceBlocks = ((LivingEntityBridge) getHandle()).bridge$getRayTrace(maxDistance, net.minecraft.world.level.ClipContext.Fluid.NONE);
            if (rayTraceBlocks != null) {
                net.minecraft.world.phys.Vec3 eye = getHandle().getEyePosition(1.0F);
                if (eye.distanceToSqr(rayTraceBlocks.getLocation()) <= eye.distanceToSqr(rayTrace.getLocation())) {
                    return null;
                }
            }
        }
        return rayTrace;
    }

    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    public void fluorite$checkHealth(double health, CallbackInfo ci) {
        if (!Double.isFinite(health)) {
            new IllegalArgumentException("A plugin is trying to set invalid health(" + health + ")").printStackTrace();
            ci.cancel();
        }
    }
}
