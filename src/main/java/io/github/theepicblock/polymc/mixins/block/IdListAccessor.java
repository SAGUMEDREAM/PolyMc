package io.github.theepicblock.polymc.mixins.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import net.minecraft.core.IdMapper;

/**
 * Used to remap block ids
 * @see io.github.theepicblock.polymc.impl.misc.BlockIdRemapper
 */
@Mixin(IdMapper.class)
public interface IdListAccessor<T> {
    @Accessor
    List<T> getIdToT();

    @Accessor
    Reference2IntMap<T> getTToId();
}
