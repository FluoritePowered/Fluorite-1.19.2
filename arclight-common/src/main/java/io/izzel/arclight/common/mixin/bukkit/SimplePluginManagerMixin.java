package io.izzel.arclight.common.mixin.bukkit;

import moe.kotori.fluorite.Fluorite;
import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = SimplePluginManager.class, remap = false)
public abstract class SimplePluginManagerMixin {
    // @formatter:off
    @Shadow @Final private Server server;
    @Shadow protected abstract void fireEvent(@NotNull Event event);
    // @formatter:on

    /**
     * @author Kotori0629
     * @reason Thread safety check
     */
    @Overwrite
    public void callEvent(@NotNull Event event) {
        if (event.isAsynchronous() || !server.isPrimaryThread()) {
            if (!event.isAsynchronous() && Thread.currentThread() instanceof java.util.concurrent.ForkJoinWorkerThread) {
                Fluorite.LOGGER.debug("Call event at worker thread!", new RuntimeException());
            }
            if (Thread.holdsLock(this)) {
                throw new IllegalStateException(event.getEventName() + " cannot be triggered asynchronously from inside synchronized code.");
            }
            if (server.isPrimaryThread()) {
                throw new IllegalStateException(event.getEventName() + " cannot be triggered asynchronously from primary server thread.");
            }
            fireEvent(event);
        } else {
            synchronized (this) {
                fireEvent(event);
            }
        }
    }
}
