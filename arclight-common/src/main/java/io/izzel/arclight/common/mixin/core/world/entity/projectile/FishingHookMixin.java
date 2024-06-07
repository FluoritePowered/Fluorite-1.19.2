package io.izzel.arclight.common.mixin.core.world.entity.projectile;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.entity.player.ServerPlayerEntityBridge;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.FishHook;
import org.bukkit.event.player.PlayerFishEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends ProjectileMixin {

    // @formatter:off
    @Shadow @Nullable public abstract Player getPlayerOwner();
    @Shadow private int nibble;
    @Shadow private int timeUntilHooked;
    @Shadow private int timeUntilLured;
    @Shadow @Final private int lureSpeed;
    @Shadow @Nullable public Entity hookedIn;
    @Shadow protected abstract boolean shouldStopFishing(Player p_37137_);
    @Shadow public abstract void pullEntity(Entity p_150156_);
    @Shadow@Final private int luck;
    
    public int minWaitTime = 100;
    public int maxWaitTime = 600;
    public boolean applyLure = true;
    // @formatter:on

    @Redirect(method = "checkCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
    private void arclight$collide(FishingHook fishingHook, HitResult hitResult) {
        this.preOnHit(hitResult);
    }

    @Inject(method = "catchingFish", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/syncher/SynchedEntityData;set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;)V", ordinal = 0))
    private void fluorite$callPlayerFishEvent(BlockPos p_37146_, CallbackInfo ci) {
        PlayerFishEvent event = new PlayerFishEvent(((ServerPlayerEntityBridge) this.getPlayerOwner()).bridge$getBukkitEntity(), null, (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.FAILED_ATTEMPT);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Inject(method = "catchingFish", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/world/entity/projectile/FishingHook;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"), cancellable = true)
    private void fluorite$callPlayerFishEvent_1(BlockPos p_37146_, CallbackInfo ci) {
        PlayerFishEvent event = new PlayerFishEvent(((ServerPlayerEntityBridge) this.getPlayerOwner()).bridge$getBukkitEntity(), null, (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.BITE);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "catchingFish", at = @At("RETURN"))
    private void arclight$modifyWaitingTime(BlockPos p_37146_, CallbackInfo ci) {
        if (this.nibble <= 0 && this.timeUntilHooked <= 0 && this.timeUntilLured <= 0) {
            this.timeUntilLured = Mth.nextInt(this.random, this.minWaitTime, this.maxWaitTime);
            this.timeUntilLured -= (this.applyLure) ? this.lureSpeed * 20 * 5 : 0;
        }
    }

    /**
     * @author IzzelAliz
     * @author Kotori0629
     * @reason
     */
    @Overwrite
    public int retrieve(ItemStack stack) {
        Player player = this.getPlayerOwner();
        if (!this.level.isClientSide && player != null && !this.shouldStopFishing(player)) {
            int i = 0;
            net.minecraftforge.event.entity.player.ItemFishedEvent event = null;
            if (this.hookedIn != null) {
                // CraftBukkit start
                PlayerFishEvent playerFishEvent = new PlayerFishEvent(((ServerPlayerEntityBridge) player).bridge$getBukkitEntity(), ((EntityBridge) this.hookedIn).bridge$getBukkitEntity(), (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.CAUGHT_ENTITY);
                Bukkit.getPluginManager().callEvent(playerFishEvent);
                if (playerFishEvent.isCancelled()) {
                    return 0;
                }
                // CraftBukkit end
                this.pullEntity(this.hookedIn);
                CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer) player, stack, (FishingHook) (Object) this, Collections.emptyList());
                this.level.broadcastEntityEvent((FishingHook) (Object) this, (byte) 31);
                i = this.hookedIn instanceof ItemEntity ? 3 : 5;
            } else if (this.nibble > 0) {
                LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerLevel) this.level)).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.TOOL, stack).withParameter(LootContextParams.THIS_ENTITY, (FishingHook) (Object) this).withRandom(this.random).withLuck((float) this.luck + player.getLuck());
                lootcontext$builder.withParameter(LootContextParams.KILLER_ENTITY, this.getOwner()).withParameter(LootContextParams.THIS_ENTITY, (FishingHook) (Object) this);
                LootTable loottable = this.level.getServer().getLootTables().get(BuiltInLootTables.FISHING);
                List<ItemStack> list = loottable.getRandomItems(lootcontext$builder.create(LootContextParamSets.FISHING));
                event = new net.minecraftforge.event.entity.player.ItemFishedEvent(list, this.onGround ? 2 : 1, (FishingHook) (Object) this);
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
                if (event.isCanceled()) {
                    this.discard();
                    return event.getRodDamage();
                }
                CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer) player, stack, (FishingHook) (Object) this, list);

                for (ItemStack itemstack : list) {
                    ItemEntity itementity = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), itemstack);
                    // CraftBukkit start
                    PlayerFishEvent playerFishEvent = new PlayerFishEvent(((ServerPlayerEntityBridge) player).bridge$getBukkitEntity(), ((EntityBridge) itementity).bridge$getBukkitEntity(), (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.CAUGHT_FISH);
                    playerFishEvent.setExpToDrop(this.random.nextInt(6) + 1);
                    Bukkit.getPluginManager().callEvent(playerFishEvent);

                    if (playerFishEvent.isCancelled()) {
                        return 0;
                    }
                    // CraftBukkit end
                    double d0 = player.getX() - this.getX();
                    double d1 = player.getY() - this.getY();
                    double d2 = player.getZ() - this.getZ();
                    double d3 = 0.1D;
                    itementity.setDeltaMovement(d0 * 0.1D, d1 * 0.1D + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * 0.1D);
                    this.level.addFreshEntity(itementity);
                    // CraftBukkit start - this.random.nextInt(6) + 1 -> playerFishEvent.getExpToDrop()
                    if (playerFishEvent.getExpToDrop() > 0) {
                        player.level.addFreshEntity(new ExperienceOrb(player.level, player.getX(), player.getY() + 0.5D, player.getZ() + 0.5D, playerFishEvent.getExpToDrop()));
                    }
                    // CraftBukkit end
                    if (itemstack.is(ItemTags.FISHES)) {
                        player.awardStat(Stats.FISH_CAUGHT, 1);
                    }
                }

                i = 1;
            }

            if (this.onGround) {
                // CraftBukkit start
                PlayerFishEvent playerFishEvent = new PlayerFishEvent(((ServerPlayerEntityBridge) player).bridge$getBukkitEntity(), null, (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.IN_GROUND);
                Bukkit.getPluginManager().callEvent(playerFishEvent);

                if (playerFishEvent.isCancelled()) {
                    return 0;
                }
                // CraftBukkit end
                i = 2;
            }

            // CraftBukkit start
            if (i == 0) {
                PlayerFishEvent playerFishEvent = new PlayerFishEvent(((ServerPlayerEntityBridge) player).bridge$getBukkitEntity(), null, (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.REEL_IN);
                Bukkit.getPluginManager().callEvent(playerFishEvent);
                if (playerFishEvent.isCancelled()) {
                    return 0;
                }
            }
            // CraftBukkit end

            this.discard();
            return event == null ? i : event.getRodDamage();
        } else {
            return 0;
        }
    }
}
