package io.izzel.arclight.common.bridge.core.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Optional;

public interface ServerPlayerEntityBridge extends PlayerEntityBridge {
    void bridge$setNewExp(int i);

    int bridge$getNewExp();

    void bridge$setNewTotalExp(int i);

    int bridge$getNewTotalExp();

    void bridge$setNewLevel(int i);

    int bridge$getNewLevel();

    void bridge$setKeepLevel(boolean b);

    boolean bridge$getKeepLevel();

    @Override
    CraftPlayer bridge$getBukkitEntity();

    void bridge$pushChangeDimensionCause(PlayerTeleportEvent.TeleportCause cause);

    Optional<PlayerTeleportEvent.TeleportCause> bridge$getTeleportCause();

    BlockPos bridge$getSpawnPoint(ServerLevel world);

    boolean bridge$isMovementBlocked();

    void bridge$setCompassTarget(Location location);

    boolean bridge$isJoining();

    void bridge$reset();

    Entity bridge$changeDimension(ServerLevel world, PlayerTeleportEvent.TeleportCause cause);

    boolean bridge$initialized();

    boolean bridge$isTrackerDirty();

    void bridge$setTrackerDirty(boolean flag);
}
