package io.github.theepicblock.polymc.mixins.wizards;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ThrowableItemProjectile.class)
public interface ThrownItemEntityAccessor {
    @Accessor("DATA_ITEM_STACK")
    static EntityDataAccessor<ItemStack> polymc$getTrackedItem() {
        throw new IllegalStateException();
    }
}
