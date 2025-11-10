package io.github.theepicblock.polymc.api.item;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapDecoder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public interface CustomDataAccessor {
    <T> DataResult<T> polyMc$read(MapDecoder<T> mapDecoder);
    <T> DataResult<T> polyMc$read(DynamicOps<Tag> dynamicOps, MapDecoder<T> mapDecoder);
    CompoundTag polyMc$getTag();
}
