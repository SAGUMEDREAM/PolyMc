package io.github.theepicblock.polymc.mixins.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SynchedEntityData.class)
public interface DataTrackerAccessor {
    @Accessor
    SynchedEntityData.DataItem<?>[] getItemsById();
}
