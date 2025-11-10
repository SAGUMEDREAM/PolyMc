package io.github.theepicblock.polymc.mixins.component;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapLike;
import io.github.theepicblock.polymc.api.item.CustomDataAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CustomData.class)
public class CustomDataMixin implements CustomDataAccessor {
    @Shadow
    @Final
    private CompoundTag tag;

    @Override
    @Unique
    public <T> DataResult<T> polyMc$read(MapDecoder<T> mapDecoder) {
        return this.polyMc$read(NbtOps.INSTANCE, mapDecoder);
    }

    @Override
    @Unique
    public <T> DataResult<T> polyMc$read(DynamicOps<Tag> dynamicOps, MapDecoder<T> mapDecoder) {
        MapLike<Tag> mapLike = (MapLike) dynamicOps.getMap(this.tag).getOrThrow();
        return mapDecoder.decode(dynamicOps, mapLike);
    }

    @Override
    @Unique
    public CompoundTag polyMc$getTag() {
        return this.tag;
    }
}
