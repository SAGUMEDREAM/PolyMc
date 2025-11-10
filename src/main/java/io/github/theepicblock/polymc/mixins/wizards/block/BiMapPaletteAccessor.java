package io.github.theepicblock.polymc.mixins.wizards.block;

import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.level.chunk.HashMapPalette;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HashMapPalette.class)
public interface BiMapPaletteAccessor<T> {
    @Accessor
    CrudeIncrementalIntIdentityHashBiMap<T> getValues();
}
