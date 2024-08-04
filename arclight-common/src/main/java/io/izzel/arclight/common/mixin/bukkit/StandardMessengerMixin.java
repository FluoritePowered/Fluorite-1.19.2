package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = StandardMessenger.class, remap = false)
public class StandardMessengerMixin {
    @ModifyConstant(method = "validateAndCorrectChannel", constant = @Constant(intValue = Messenger.MAX_CHANNEL_SIZE))
    private static int fluorite$modifyMaxChannelSize(int value) {
        return 256;
    }
}
