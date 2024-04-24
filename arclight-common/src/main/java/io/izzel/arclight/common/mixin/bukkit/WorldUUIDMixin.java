package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.craftbukkit.v.util.WorldUUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.util.UUID;

@Mixin(value = WorldUUID.class, remap = false)
public class WorldUUIDMixin {
    @Inject(method = "getUUID", at = @At(value = "INVOKE", target = "Ljava/util/UUID;randomUUID()Ljava/util/UUID;", shift = At.Shift.BEFORE))
    private static void fluorite$firstLoad(File baseDir, CallbackInfoReturnable<UUID> cir) {
        if (!baseDir.exists()) {
            baseDir.mkdirs(); // Fluorite - prevent FileNotFoundException from being thrown on first load
        }
    }
}
