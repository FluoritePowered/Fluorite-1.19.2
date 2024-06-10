package io.izzel.arclight.common.mixin.core.network.protocol.game;

import io.izzel.arclight.common.bridge.core.world.border.WorldBorderBridge;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundSetBorderCenterPacket.class)
public class ClientboundSetBorderCenterPacketMixin {
    // @formatter:off
    @Mutable
    @Shadow @Final private double newCenterX;

    @Mutable
    @Shadow @Final private double newCenterZ;
    // @formatter:on

    @Inject(method = "<init>(Lnet/minecraft/world/level/border/WorldBorder;)V", at = @At("RETURN"))
    public void fluorite$netherBorder(WorldBorder p_179214_, CallbackInfo ci) {
        this.newCenterX = p_179214_.getCenterX() * (((WorldBorderBridge) p_179214_).bridge$getWorld() != null ? ((WorldBorderBridge) p_179214_).bridge$getWorld().dimensionType().coordinateScale() : 1.0);
        this.newCenterZ = p_179214_.getCenterZ() * (((WorldBorderBridge) p_179214_).bridge$getWorld() != null ? ((WorldBorderBridge) p_179214_).bridge$getWorld().dimensionType().coordinateScale() : 1.0);
    }
}
