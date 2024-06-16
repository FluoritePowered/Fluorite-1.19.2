package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SimpleCommandMap.class, remap = false)
public abstract class SimpleCommandMapMixin {
    // @formatter:off
    @Shadow public abstract boolean register(@NotNull String fallbackPrefix, @NotNull Command command);
    // @formatter:on

    @Inject(method = "setDefaultCommands", at = @At("TAIL"))
    private void fluorite$registerCommand(CallbackInfo ci) {
        register("fluorite", new moe.kotori.fluorite.commands.ChunkStatsCommand("chunkstats"));
    }
}
