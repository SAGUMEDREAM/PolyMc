package io.github.theepicblock.polymc.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record BlockItemType(@NotNull BlockPlacementBehaviour placementBehaviour, SoundEvent placeSound) {
    public BlockItemType(FriendlyByteBuf buf) {
        this(buf.readEnum(BlockPlacementBehaviour.class), SoundEvent.DIRECT_STREAM_CODEC.decode(buf));
    }

    @Nullable
    public static BlockItemType of(BlockItem blockItem) {
        var block = blockItem.getBlock();
        var behavior = BlockPlacementBehaviour.get(blockItem);
        if (behavior == null) return null;
        var sound = block.defaultBlockState().getSoundType().getPlaceSound();
        return new BlockItemType(behavior, sound);
    }

    public static void write(FriendlyByteBuf buf, BlockItemType self) {
        buf.writeEnum(self.placementBehaviour);
        SoundEvent.DIRECT_STREAM_CODEC.encode(buf, self.placeSound);
    }

    // We need custom equals and hashCode because SoundEvent doesn't have a proper one

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockItemType that = (BlockItemType)o;
        return placementBehaviour == that.placementBehaviour &&
                Objects.equals(placeSound.location(), that.placeSound.location()) &&
                Objects.equals(placeSound.getRange(1), placeSound.getRange(1)) &&
                Objects.equals(placeSound.getRange(0.5f), placeSound.getRange(0.5f));
    }

    @Override
    public int hashCode() {
        return Objects.hash(placementBehaviour, placeSound.location(), placeSound.getRange(1), placeSound.getRange(0.5f));
    }
}
