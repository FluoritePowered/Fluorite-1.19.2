package io.izzel.arclight.common.bridge.core.entity.decoration;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface ArmorStandBridge {
    List<ItemStack> bridge$getDrops();

    void bridge$addDrop(org.bukkit.inventory.ItemStack itemStack);
}
