package io.github.theepicblock.polymc.mixins.wizards;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemFrame.class)
public interface ItemFrameEntityAccessor {
    @Accessor(value = "DATA_ITEM")
    static EntityDataAccessor<ItemStack> getItemStackTracker() {
        throw new IllegalStateException();
    }

    @Accessor(value = "DATA_ROTATION")
    static EntityDataAccessor<Integer> getRotationTracker() {
        throw new IllegalStateException();
    }
}
