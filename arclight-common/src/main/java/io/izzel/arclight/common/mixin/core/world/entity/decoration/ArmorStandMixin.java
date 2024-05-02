package io.izzel.arclight.common.mixin.core.world.entity.decoration;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.entity.decoration.ArmorStandBridge;
import io.izzel.arclight.common.bridge.core.entity.player.ServerPlayerEntityBridge;
import io.izzel.arclight.common.mixin.core.world.entity.LivingEntityMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(net.minecraft.world.entity.decoration.ArmorStand.class)
public abstract class ArmorStandMixin extends LivingEntityMixin implements ArmorStandBridge {

    // @formatter:off
    @Shadow private boolean invisible;
    @Shadow public abstract ItemStack getItemBySlot(net.minecraft.world.entity.EquipmentSlot slotIn);
    @Shadow @Final private NonNullList<ItemStack> handItems;
    @Shadow @Final private NonNullList<ItemStack> armorItems;
    // @formatter:on

    private ArrayList<org.bukkit.inventory.ItemStack> fluorite$BukkitDrops = new ArrayList<>();

    @Override
    public List<org.bukkit.inventory.ItemStack> bridge$getDrops() {
        return this.fluorite$BukkitDrops;
    }

    @Override
    public void bridge$addDrop(org.bukkit.inventory.ItemStack itemStack) {
        this.fluorite$BukkitDrops.add(itemStack);
    }

    @Override
    public float getBukkitYaw() {
        return this.getYRot();
    }

    @Override
    protected boolean shouldDropExperience() {
        return true;
    }

    @Override
    public void setItemSlot(net.minecraft.world.entity.EquipmentSlot slotIn, ItemStack stack, boolean silent) {
        switch (slotIn.getType()) {
            case HAND -> this.bridge$playEquipSound(slotIn, this.handItems.set(slotIn.getIndex(), stack), stack, silent);
            case ARMOR -> this.bridge$playEquipSound(slotIn, this.armorItems.set(slotIn.getIndex(), stack), stack, silent);
        }
    }

    @Inject(method = "swapItem", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAbilities()Lnet/minecraft/world/entity/player/Abilities;"))
    public void arclight$manipulateEvent(net.minecraft.world.entity.player.Player playerEntity, net.minecraft.world.entity.EquipmentSlot slotType, ItemStack itemStack, InteractionHand hand, CallbackInfoReturnable<Boolean> cir) {
        ItemStack itemStack1 = this.getItemBySlot(slotType);

        org.bukkit.inventory.ItemStack armorStandItem = CraftItemStack.asCraftMirror(itemStack1);
        org.bukkit.inventory.ItemStack playerHeldItem = CraftItemStack.asCraftMirror(itemStack);

        Player player = ((ServerPlayerEntityBridge) playerEntity).bridge$getBukkitEntity();
        ArmorStand self = (ArmorStand) ((EntityBridge) this).bridge$getBukkitEntity();

        EquipmentSlot slot = CraftEquipmentSlot.getSlot(slotType);
        EquipmentSlot bukkitHand = CraftEquipmentSlot.getHand(hand);
        PlayerArmorStandManipulateEvent event = new PlayerArmorStandManipulateEvent(player, self, playerHeldItem, armorStandItem, slot, bukkitHand);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;kill()V", ordinal = 0, shift = At.Shift.BEFORE), cancellable = true)
    private void fluorite$handleNonLivingEntityDamageEvent(DamageSource p_31579_, float p_31580_, CallbackInfoReturnable<Boolean> cir) {
        if (org.bukkit.craftbukkit.v.event.CraftEventFactory.handleNonLivingEntityDamageEvent((net.minecraft.world.entity.decoration.ArmorStand) (Object) this, p_31579_, p_31580_)) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;invisible:Z"))
    private boolean fluorite$alwaysTrue(net.minecraft.world.entity.decoration.ArmorStand instance) {
        return false;
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;isExplosion()Z", shift = At.Shift.BEFORE), cancellable = true)
    private void fluorite$handleNonLivingEntityDamageEvent_2(DamageSource p_31579_, float p_31580_, CallbackInfoReturnable<Boolean> cir) {
        if (org.bukkit.craftbukkit.v.event.CraftEventFactory.handleNonLivingEntityDamageEvent((net.minecraft.world.entity.decoration.ArmorStand) (Object) this, p_31579_, p_31580_, true, this.invisible)) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;kill()V", ordinal = 3))
    private void fluorite$redirectKill(net.minecraft.world.entity.decoration.ArmorStand instance) {
        this.discard();
    }

    @Redirect(method = "brokenByPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"))
    private void fluorite$redirectPopResource(Level p_49841_, BlockPos p_49842_, ItemStack p_49843_) {
        this.bridge$addDrop(org.bukkit.craftbukkit.v.inventory.CraftItemStack.asBukkitCopy(p_49843_));
    }

    @Redirect(method = "brokenByAnything", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;dropAllDeathLoot(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void fluorite$redrictDropAllDeathLoot(net.minecraft.world.entity.decoration.ArmorStand instance, DamageSource damageSource) {
        // Empty, move down
    }

    @Redirect(method = "brokenByAnything", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", ordinal = 0))
    private void fluorite$redirectPopResource_2(Level p_49841_, BlockPos p_49842_, ItemStack p_49843_) {
        this.bridge$addDrop(org.bukkit.craftbukkit.v.inventory.CraftItemStack.asBukkitCopy(p_49843_));
    }

    @Redirect(method = "brokenByAnything", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", ordinal = 1))
    private void fluorite$redirectPopResource_3(Level p_49841_, BlockPos p_49842_, ItemStack p_49843_) {
        this.bridge$addDrop(org.bukkit.craftbukkit.v.inventory.CraftItemStack.asBukkitCopy(p_49843_));
    }

    @Inject(method = "brokenByAnything", at = @At(value = "RETURN"))
    private void fluorite$dropAllDeathLoot(DamageSource p_31654_, CallbackInfo ci) {
        this.dropAllDeathLoot(p_31654_);
    }

    @Inject(method = "kill", at = @At("HEAD"))
    private void fluorite$callEntityDeathEvent(CallbackInfo ci) {
        org.bukkit.craftbukkit.v.event.CraftEventFactory.callEntityDeathEvent((net.minecraft.world.entity.decoration.ArmorStand) (Object) this, this.bridge$getDrops());
    }
}
