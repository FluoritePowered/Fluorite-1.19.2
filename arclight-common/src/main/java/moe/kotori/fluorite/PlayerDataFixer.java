package moe.kotori.fluorite;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.bukkit.util.NumberConversions;

// https://github.com/Luohuayu/CatServer/blob/1.16.5/src/main/java/catserver/server/PlayerDataFixer.java
public class PlayerDataFixer {
    public static void checkVector(Entity entity) {
        Vec3 vector = entity.getDeltaMovement();
        if (!NumberConversions.isFinite(vector.x) || !NumberConversions.isFinite(vector.y) || !NumberConversions.isFinite(vector.z)) {
            entity.setDeltaMovement(Vec3.ZERO);
        }
    }

    public static void checkLocation(Player entity) {
        Vec3 position = entity.position();
        if (!NumberConversions.isFinite(position.x) || !NumberConversions.isFinite(position.y) || !NumberConversions.isFinite(position.z)) {
            BlockPos pos = entity.level.getSharedSpawnPos();
            entity.setPosRaw(pos.getX(), pos.getY(), pos.getZ());
        }
    }

    public static void checkHealth(Player entity) {
        if (!NumberConversions.isFinite(entity.getHealth())) {
            entity.setHealth(0.0F);
        }
    }
}
