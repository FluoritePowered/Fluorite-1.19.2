package io.izzel.arclight.common.mixin.core.world.storage;

import io.izzel.arclight.common.bridge.core.world.storage.DerivedWorldInfoBridge;
import io.izzel.arclight.i18n.ArclightConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DerivedLevelData.class)
public class DerivedWorldInfoMixin implements DerivedWorldInfoBridge {

    @Shadow @Final private ServerLevelData wrapped;

    private ResourceKey<LevelStem> typeKey;

    private String properName;

    /**
     * @author Kotori0629
     * @reason compat bukkitWorld
     */
    @Overwrite
    public String getLevelName() {
        return this.properName != null ? this.properName : this.wrapped.getLevelName();
    }

    @Override
    public void bridge$setWorldName(String name) {
        this.properName = name;
    }

    @Override
    public ServerLevelData bridge$getDelegate() {
        return wrapped;
    }

    @Override
    public void bridge$setDimType(ResourceKey<LevelStem> typeKey) {
        this.typeKey = typeKey;
    }
}
