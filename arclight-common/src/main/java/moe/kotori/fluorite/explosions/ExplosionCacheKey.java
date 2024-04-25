package moe.kotori.fluorite.explosions;

import io.izzel.arclight.common.bridge.core.world.ExplosionBridge;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

// https://github.com/PaperMC/Paper/blob/ver/1.19.4/patches/server/0040-Optimize-explosions.patch
public class ExplosionCacheKey {
    private final Level world;
    private final double posX, posY, posZ;
    private final double minX, minY, minZ;
    private final double maxX, maxY, maxZ;

    public ExplosionCacheKey(Explosion explosion, AABB aabb) {
        this.world = ((ExplosionBridge) explosion).bridge$getExploder().getLevel();
        this.posX = ((ExplosionBridge) explosion).bridge$getExploder().getX();
        this.posY = ((ExplosionBridge) explosion).bridge$getExploder().getY();
        this.posZ = ((ExplosionBridge) explosion).bridge$getExploder().getZ();
        this.minX = aabb.minX;
        this.minY = aabb.minY;
        this.minZ = aabb.minZ;
        this.maxX = aabb.maxX;
        this.maxY = aabb.maxY;
        this.maxZ = aabb.maxZ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExplosionCacheKey cacheKey = (ExplosionCacheKey) o;
        if (Double.compare(cacheKey.posX, posX) != 0) return false;
        if (Double.compare(cacheKey.posY, posY) != 0) return false;
        if (Double.compare(cacheKey.posZ, posZ) != 0) return false;
        if (Double.compare(cacheKey.minX, minX) != 0) return false;
        if (Double.compare(cacheKey.minY, minY) != 0) return false;
        if (Double.compare(cacheKey.minZ, minZ) != 0) return false;
        if (Double.compare(cacheKey.maxX, maxX) != 0) return false;
        if (Double.compare(cacheKey.maxY, maxY) != 0) return false;
        if (Double.compare(cacheKey.maxZ, maxZ) != 0) return false;
        return world.equals(cacheKey.world);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = world.hashCode();
        temp = Double.doubleToLongBits(posX);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(posY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(posZ);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minX);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minZ);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxX);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxZ);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
