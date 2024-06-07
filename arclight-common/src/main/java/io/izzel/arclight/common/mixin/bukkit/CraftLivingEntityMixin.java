package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftLivingEntity.class, remap = false)
public class CraftLivingEntityMixin {
    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    public void fluorite$checkHealth(double health, CallbackInfo ci) {
        if (!Double.isFinite(health)) {
            new IllegalArgumentException("A plugin is trying to set invalid health(" + health + ")").printStackTrace();
            ci.cancel();
        }
    }
}
