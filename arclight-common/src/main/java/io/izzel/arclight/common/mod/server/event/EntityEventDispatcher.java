package io.izzel.arclight.common.mod.server.event;

import io.izzel.arclight.common.bridge.core.entity.LivingEntityBridge;
import io.izzel.arclight.common.bridge.core.entity.decoration.ArmorStandBridge;
import io.izzel.arclight.common.mod.util.ArclightCaptures;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;

import java.util.ArrayList;
import java.util.List;

public class EntityEventDispatcher {

    @SubscribeEvent(receiveCanceled = true)
    public void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) {
            LivingEntity livingEntity = event.getEntity();
            ((LivingEntityBridge) livingEntity).bridge$setExpToDrop(((LivingEntityBridge) livingEntity).bridge$getExpReward());

            List<org.bukkit.inventory.ItemStack> bukkitDrops = new ArrayList<>();
            for (ItemEntity forgeDrop : event.getDrops()) {
                bukkitDrops.add(CraftItemStack.asCraftMirror(forgeDrop.getItem()));
            }

            // Fluorite start - handle ArmorStand from CatServer
            if (livingEntity instanceof net.minecraft.world.entity.decoration.ArmorStand armorStand) {
                bukkitDrops.addAll(((ArmorStandBridge) armorStand).bridge$getDrops());
                ((ArmorStandBridge) armorStand).bridge$getDrops().clear();
            }

            CraftEventFactory.callEntityDeathEvent(livingEntity, bukkitDrops);
            ((LivingEntityBridge) livingEntity).bridge$dropExperience();
            // Fluorite end
        }
    }

    @SubscribeEvent
    public void onEntityTame(AnimalTameEvent event) {
        event.setCanceled(CraftEventFactory.callEntityTameEvent(event.getAnimal(), event.getTamer()).isCancelled());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLightningBolt(EntityStruckByLightningEvent event) {
        ArclightCaptures.captureDamageEventEntity(event.getLightning());
    }
}
