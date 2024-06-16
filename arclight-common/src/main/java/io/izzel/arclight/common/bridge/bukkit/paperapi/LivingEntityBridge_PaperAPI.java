package io.izzel.arclight.common.bridge.bukkit.paperapi;

import com.destroystokyo.paper.entity.TargetEntityInfo;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;

public interface LivingEntityBridge_PaperAPI {
    @Nullable Entity bridge$getTargetEntity(int maxDistance, boolean ignoreBlocks);
    @Nullable TargetEntityInfo bridge$getTargetEntityInfo(int maxDistance, boolean ignoreBlocks);
}
