package io.izzel.arclight.common.mixin.bukkit.paperapi;

import io.izzel.arclight.common.bridge.bukkit.paperapi.LivingEntityBridge_PaperAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public interface LivingEntityMixin_PaperAPI extends LivingEntityBridge_PaperAPI {
    @Nullable
    public default Entity getTargetEntity(int maxDistance) {
        return getTargetEntity(maxDistance, false);
    }

    @Nullable
    default Entity getTargetEntity(int maxDistance, boolean ignoreBlocks) {
        return this.bridge$getTargetEntity(maxDistance, ignoreBlocks);
    }

    @Nullable
    public default com.destroystokyo.paper.entity.TargetEntityInfo getTargetEntityInfo(int maxDistance) {
        return getTargetEntityInfo(maxDistance, false);
    }

    @Nullable
    default com.destroystokyo.paper.entity.TargetEntityInfo getTargetEntityInfo(int maxDistance, boolean ignoreBlocks) {
        return this.bridge$getTargetEntityInfo(maxDistance, ignoreBlocks);
    }
}
