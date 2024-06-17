package io.izzel.arclight.common.mixin.core.world.level;

import io.izzel.arclight.common.bridge.core.server.management.PlayerListBridge;
import io.izzel.arclight.common.bridge.core.world.WorldBridge;
import io.izzel.arclight.common.bridge.core.world.border.WorldBorderBridge;
import io.izzel.arclight.common.bridge.core.world.level.levelgen.ChunkGeneratorBridge;
import io.izzel.arclight.common.bridge.core.world.server.ServerChunkProviderBridge;
import io.izzel.arclight.common.bridge.core.world.server.ServerWorldBridge;
import io.izzel.arclight.common.mod.server.ArclightServer;
import io.izzel.arclight.common.mod.server.world.WrappedWorlds;
import io.izzel.arclight.common.mod.util.ArclightCaptures;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.craftbukkit.v.generator.CraftWorldInfo;
import org.bukkit.craftbukkit.v.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v.generator.CustomWorldChunkManager;
import org.bukkit.craftbukkit.v.util.CraftSpawnCategory;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.generator.ChunkGenerator;
import org.spigotmc.SpigotWorldConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(Level.class)
public abstract class LevelMixin implements WorldBridge, LevelWriter {

    // @formatter:off
    @Shadow @Nullable public BlockEntity getBlockEntity(BlockPos pos) { return null; }
    @Shadow public abstract BlockState getBlockState(BlockPos pos);
    @Shadow public abstract WorldBorder getWorldBorder();
    @Mutable @Shadow @Final private WorldBorder worldBorder;
    @Shadow public abstract long getDayTime();
    @Shadow public abstract MinecraftServer shadow$getServer();
    @Shadow public abstract LevelData getLevelData();
    @Shadow public abstract ResourceKey<Level> dimension();
    @Shadow(remap = false) public abstract void markAndNotifyBlock(BlockPos p_46605_,@org.jetbrains.annotations.Nullable LevelChunk levelchunk, BlockState blockstate, BlockState p_46606_, int p_46607_, int p_46608_);
    @Shadow public abstract DimensionType dimensionType();
    @Shadow public abstract Explosion explode(@org.jetbrains.annotations.Nullable Entity p_46526_,@org.jetbrains.annotations.Nullable DamageSource p_46527_,@org.jetbrains.annotations.Nullable ExplosionDamageCalculator p_46528_, double p_46529_, double p_46530_, double p_46531_, float p_46532_, boolean p_46533_, Explosion.BlockInteraction p_46534_);
    @Accessor("thread") public abstract Thread arclight$getMainThread();
    // @formatter:on

    protected CraftWorld world;
    public boolean pvpMode;
    public boolean keepSpawnInMemory = true;
    public final Object2LongOpenHashMap<SpawnCategory> ticksPerSpawnCategory = new Object2LongOpenHashMap<>();
    public boolean populating;
    public org.bukkit.generator.ChunkGenerator generator;
    protected org.bukkit.World.Environment environment;
    protected org.bukkit.generator.BiomeProvider biomeProvider;
    public org.spigotmc.SpigotWorldConfig spigotConfig;
    @SuppressWarnings("unused") // Access transformed to public by ArclightMixinPlugin
    private static BlockPos lastPhysicsProblem; // Spigot
    public boolean preventPoiUpdated = false;

    public void arclight$constructor(WritableLevelData worldInfo, ResourceKey<Level> dimension, final Holder<DimensionType> dimensionType, Supplier<ProfilerFiller> profiler, boolean isRemote, boolean isDebug, long seed, int maxNeighborUpdate) {
        throw new RuntimeException();
    }

    public void arclight$constructor(WritableLevelData worldInfo, ResourceKey<Level> dimension, final Holder<DimensionType> dimensionType, Supplier<ProfilerFiller> profiler, boolean isRemote, boolean isDebug, long seed, int maxNeighborUpdate, org.bukkit.generator.ChunkGenerator gen, org.bukkit.generator.BiomeProvider biomeProvider, org.bukkit.World.Environment env) {
        arclight$constructor(worldInfo, dimension, dimensionType, profiler, isRemote, isDebug, seed, maxNeighborUpdate);
        this.generator = gen;
        this.environment = env;
        this.biomeProvider = biomeProvider;
        bridge$getWorld();
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", ordinal = 0, target = "Lnet/minecraft/world/level/Level;worldBorder:Lnet/minecraft/world/level/border/WorldBorder;"))
    private void fluorite$getCenter(Level instance, WorldBorder value) {
        this.worldBorder = new WorldBorder() {
            public double getCenterX() {
                return super.getCenterX(); // CraftBukkit
            }

            public double getCenterZ() {
                return super.getCenterZ(); // CraftBukkit
            }
        };
    }

    private transient boolean fluorite$isFire;

    @Override
    public void bridge$setFire(boolean fire) {
        this.fluorite$isFire = fire;
    }

    @Redirect(
            method = "explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Explosion$BlockInteraction;)Lnet/minecraft/world/level/Explosion;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Explosion$BlockInteraction;)Lnet/minecraft/world/level/Explosion;")
    )
    private Explosion fluorite$fire(Level instance, Entity p_46526_, DamageSource p_46527_, ExplosionDamageCalculator p_46528_, double p_46529_, double p_46530_, double p_46531_, float p_46532_, boolean p_46533_, Explosion.BlockInteraction p_46534_) {
        return this.explode(p_46526_, null, null, p_46529_, p_46530_, p_46531_, p_46532_, this.fluorite$isFire, p_46534_);
    }

    @Inject(method = "guardEntityTick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraftforge/server/timings/TimeTracker;trackStart(Ljava/lang/Object;)V"))
    private <T extends Entity> void fluorite$guardStatStart(Consumer<T> p_46654_, T p_46655_, CallbackInfo ci) {
        moe.kotori.fluorite.commands.ChunkStatsCommand.onTickStart();
    }

    @Inject(method = "guardEntityTick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private <T extends Entity> void fluorite$guardStatEnd(Consumer<T> p_46654_, T p_46655_, CallbackInfo ci) {
        moe.kotori.fluorite.commands.ChunkStatsCommand.onTickEnd((Level) (Object) this, p_46655_.blockPosition());
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void arclight$init(WritableLevelData info, ResourceKey<Level> dimension, Holder<DimensionType> dimType, Supplier<ProfilerFiller> profiler, boolean isRemote, boolean isDebug, long seed, int maxNeighborUpdates, CallbackInfo ci) {
        ((WorldBorderBridge) this.worldBorder).bridge$setWorld((Level) (Object) this);
        getWorldBorder().addListener(new BorderChangeListener() {
            @Override
            public void onBorderSizeSet(WorldBorder worldborder, double d0) {
                ((PlayerListBridge) getCraftServer().getHandle()).bridge$broadcastAll(new ClientboundSetBorderSizePacket(worldborder), ((WorldBorderBridge) worldborder).bridge$getWorld());
            }

            @Override
            public void onBorderSizeLerping(WorldBorder worldborder, double d0, double d1, long i) {
                ((PlayerListBridge) getCraftServer().getHandle()).bridge$broadcastAll(new ClientboundSetBorderLerpSizePacket(worldborder), ((WorldBorderBridge) worldborder).bridge$getWorld());
            }

            @Override
            public void onBorderCenterSet(WorldBorder worldborder, double d0, double d1) {
                ((PlayerListBridge) getCraftServer().getHandle()).bridge$broadcastAll(new ClientboundSetBorderCenterPacket(worldborder), ((WorldBorderBridge) worldborder).bridge$getWorld());
            }

            @Override
            public void onBorderSetWarningTime(WorldBorder worldborder, int i) {
                ((PlayerListBridge) getCraftServer().getHandle()).bridge$broadcastAll(new ClientboundSetBorderWarningDelayPacket(worldborder), ((WorldBorderBridge) worldborder).bridge$getWorld());
            }

            @Override
            public void onBorderSetWarningBlocks(WorldBorder worldborder, int i) {
                ((PlayerListBridge) getCraftServer().getHandle()).bridge$broadcastAll(new ClientboundSetBorderWarningDistancePacket(worldborder), ((WorldBorderBridge) worldborder).bridge$getWorld());
            }

            @Override
            public void onBorderSetDamagePerBlock(WorldBorder worldborder, double d0) {
            }

            @Override
            public void onBorderSetDamageSafeZOne(WorldBorder worldborder, double d0) {
            }
        });

        for (SpawnCategory spawnCategory : SpawnCategory.values()) {
            if (CraftSpawnCategory.isValidForLimits(spawnCategory)) {
                this.ticksPerSpawnCategory.put(spawnCategory, this.getCraftServer().getTicksPerSpawns(spawnCategory));
            }
        }
    }

    @Override
    public void bridge$setLastPhysicsProblem(BlockPos pos) {
        lastPhysicsProblem = pos;
    }

    @Override
    public Object2LongOpenHashMap<SpawnCategory> bridge$ticksPerSpawnCategory() {
        return this.ticksPerSpawnCategory;
    }

    public abstract ResourceKey<LevelStem> getTypeKey();

    @Override
    public ResourceKey<LevelStem> bridge$getTypeKey() {
        return getTypeKey();
    }

    @Override
    public SpigotWorldConfig bridge$spigotConfig() {
        return this.spigotConfig;
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
        at = @At("HEAD"), cancellable = true)
    private void arclight$hooks(BlockPos pos, BlockState newState, int flags, CallbackInfoReturnable<Boolean> cir) {
        if (!processCaptures(pos, newState, flags)) {
            cir.setReturnValue(false);
        }
    }

    private boolean processCaptures(BlockPos pos, BlockState newState, int flags) {
        Entity entityChangeBlock = ArclightCaptures.getEntityChangeBlock();
        if (entityChangeBlock != null) {
            if (CraftEventFactory.callEntityChangeBlockEvent(entityChangeBlock, pos, newState).isCancelled()) {
                return false;
            }
        }
        return true;
    }

    @Inject(method = "markAndNotifyBlock", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;updateNeighbourShapes(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;II)V"))
    private void arclight$callBlockPhysics(BlockPos pos, LevelChunk chunk, BlockState blockstate, BlockState state, int flags, int recursionLeft, CallbackInfo ci) {
        try {
            if (this.world != null) {
                BlockPhysicsEvent event = new BlockPhysicsEvent(CraftBlock.at((LevelAccessor) this, pos), CraftBlockData.fromData(state));
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    ci.cancel();
                }
            }
        } catch (StackOverflowError e) {
            lastPhysicsProblem = pos;
        }
    }

    @Inject(method = "markAndNotifyBlock", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;onBlockStateChange(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private void arclight$preventPoiUpdate(BlockPos p_46605_, LevelChunk levelchunk, BlockState blockstate, BlockState p_46606_, int p_46607_, int p_46608_, CallbackInfo ci) {
        if (this.preventPoiUpdated) {
            ci.cancel();
        }
    }

    public void notifyAndUpdatePhysics(BlockPos blockposition, LevelChunk chunk, BlockState oldBlock, BlockState newBlock, BlockState actualBlock, int i, int j) {
        this.markAndNotifyBlock(blockposition, chunk, oldBlock, newBlock, i, j);
    }

    public CraftServer getCraftServer() {
        return (CraftServer) Bukkit.getServer();
    }

    public CraftWorld getWorld() {
        if (this.world == null) {
            Optional<Field> delegate = WrappedWorlds.getDelegate(this.getClass());
            if (delegate.isPresent()) {
                try {
                    return ((WorldBridge) delegate.get().get(this)).bridge$getWorld();
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            if (environment == null) {
                environment = ArclightServer.getEnvironment(this.getTypeKey());
            }
            if (generator == null) {
                generator = getCraftServer().getGenerator(((ServerLevelData) this.getLevelData()).getLevelName());
                if (generator != null && (Object) this instanceof ServerLevel serverWorld) {
                    org.bukkit.generator.WorldInfo worldInfo = new CraftWorldInfo((ServerLevelData) getLevelData(),
                        ((ServerWorldBridge) this).bridge$getConvertable(), environment, this.dimensionType());
                    if (biomeProvider == null && generator != null) {
                        biomeProvider = generator.getDefaultBiomeProvider(worldInfo);
                    }
                    var generator = serverWorld.getChunkSource().getGenerator();
                    if (biomeProvider != null) {
                        BiomeSource biomeSource = new CustomWorldChunkManager(worldInfo, biomeProvider, serverWorld.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY));
                        ((ChunkGeneratorBridge) generator).bridge$setBiomeSource(biomeSource);
                    }
                    CustomChunkGenerator gen = new CustomChunkGenerator(serverWorld, generator, this.generator);
                    ((ServerChunkProviderBridge) serverWorld.getChunkSource()).bridge$setChunkGenerator(gen);
                }
            }
            this.world = new CraftWorld((ServerLevel) (Object) this, generator, biomeProvider, environment);
            getCraftServer().addWorld(this.world);
        }
        return this.world;
    }

    public BlockEntity getBlockEntity(BlockPos pos, boolean validate) {
        return getBlockEntity(pos);
    }

    @Override
    public BlockEntity bridge$getTileEntity(BlockPos pos, boolean validate) {
        return getBlockEntity(pos, validate);
    }

    @Override
    public CraftServer bridge$getServer() {
        return (CraftServer) Bukkit.getServer();
    }

    @Override
    public CraftWorld bridge$getWorld() {
        return this.getWorld();
    }

    @Override
    public boolean bridge$isPvpMode() {
        return this.pvpMode;
    }

    @Override
    public boolean bridge$isKeepSpawnInMemory() {
        return this.keepSpawnInMemory;
    }

    @Override
    public boolean bridge$isPopulating() {
        return this.populating;
    }

    @Override
    public void bridge$setPopulating(boolean populating) {
        this.populating = populating;
    }

    @Override
    public ChunkGenerator bridge$getGenerator() {
        return generator;
    }

    @Override
    public ServerLevel bridge$getMinecraftWorld() {
        return getWorld().getHandle();
    }

    @Override
    public boolean bridge$addEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        if (getWorld().getHandle() != (Object) this) {
            return ((WorldBridge) getWorld().getHandle()).bridge$addEntity(entity, reason);
        } else {
            this.bridge$pushAddEntityReason(reason);
            return this.addFreshEntity(entity);
        }
    }

    @Override
    public void bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason reason) {
        if (getWorld().getHandle() != (Object) this) {
            ((WorldBridge) getWorld().getHandle()).bridge$pushAddEntityReason(reason);
        }
    }

    @Override
    public CreatureSpawnEvent.SpawnReason bridge$getAddEntityReason() {
        if (getWorld().getHandle() != (Object) this) {
            return ((WorldBridge) getWorld().getHandle()).bridge$getAddEntityReason();
        }
        return null;
    }
}
