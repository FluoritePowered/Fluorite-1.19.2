package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.server.ArclightServer;
import org.spigotmc.RestartCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RestartCommand.class, remap = false)
public class RestartCommandMixin {
    @Inject(method = "restart(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private static void fluorite$disableRestartCommand(String restartScript, CallbackInfo ci) {
        ArclightServer.get().getLogger().warning("Spigot's restartCommand is disabled in Fluorite!");
        ci.cancel();
    }
}
