package io.izzel.arclight.common.mixin.core.world.level.chunk;

import io.izzel.arclight.common.mod.util.ArclightCaptures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/world/level/chunk/LevelChunk$BoundTickingBlockEntity")
public class LevelChunk_BoundTickingBlockEntityMixin<T extends BlockEntity> {

    @Shadow @Final private T blockEntity;

    @Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/util/function/Supplier;)V"))
    private void fluorite$chunkStatTickStart(CallbackInfo ci) {
        moe.kotori.fluorite.commands.ChunkStatsCommand.onTickStart();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V"))
    private void fluorite$chunkStatTickEnd(CallbackInfo ci) {
        moe.kotori.fluorite.commands.ChunkStatsCommand.onTickEnd((ServerLevel) this.blockEntity.level, this.blockEntity.getBlockPos());
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntityTicker;tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
    private void arclight$captureBlockEntity(CallbackInfo ci) {
        ArclightCaptures.captureTickingBlockEntity(this.blockEntity);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/level/block/entity/BlockEntityTicker;tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
    private void arclight$resetBlockEntity(CallbackInfo ci) {
        ArclightCaptures.resetTickingBlockEntity();
    }
}
