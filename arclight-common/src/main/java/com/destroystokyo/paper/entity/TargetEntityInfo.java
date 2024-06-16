package com.destroystokyo.paper.entity;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class TargetEntityInfo {
    private final Entity entity;
    private final Vector hitVec;

    public TargetEntityInfo(@NotNull Entity entity, @NotNull Vector hitVec) {
        this.entity = entity;
        this.hitVec = hitVec;
    }

    @NotNull
    public Entity getEntity() {
        return entity;
    }

    @NotNull
    public Vector getHitVec() {
        return hitVec;
    }
}
