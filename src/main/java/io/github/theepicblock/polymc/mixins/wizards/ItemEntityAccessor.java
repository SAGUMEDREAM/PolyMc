package io.github.theepicblock.polymc.mixins.wizards;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemEntity.class)
public interface ItemEntityAccessor {
    @Accessor("DATA_ITEM")
    static EntityDataAccessor<ItemStack> getStackTracker() {
        throw new IllegalStateException();
    }
}
