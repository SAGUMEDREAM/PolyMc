package io.github.theepicblock.polymc.mixins.block.implementations;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.BitStorage;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.GlobalPalette;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(PalettedContainer.Data.class)
public class IdListImplementation {
    @Redirect(method = "write", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/world/level/chunk/PalettedContainer$Data;storage:Lnet/minecraft/util/BitStorage;"))
    private BitStorage getData(PalettedContainer.Data<?> container, FriendlyByteBuf buf)  {
        var originalStorage = container.storage();

        if (!(container.palette() instanceof GlobalPalette<?>)) {
            return originalStorage;
        }

        var player = PacketContext.get();
        var polyMap = Util.tryGetPolyMap(player);

        if (!polyMap.isVanillaLikeMap()) {
            return originalStorage;
        }

        // Check if we're actually doing things with blocks
        if (!(container.palette().valueFor(0) instanceof BlockState)) {
            return originalStorage;
        }

        var oldArray = originalStorage.getRaw();
        var newArray = new long[oldArray.length]; // SAFETY the size of the array mustn't change, otherwise we'd have to inject into getPacketSize as well

        var elementBits = originalStorage.getBits(); // The amount of bits per element
        var size = originalStorage.getSize();
        var elementsPerLong = (char)(64 / elementBits);
        var maxValue = (1L << elementBits) - 1L;

        int i = 0; // Counts the elements
        a: for (int j = 0; j < oldArray.length; j++) {
            long oldLong = oldArray[j];
            long newLong = 0;
            for (int k = 0; k < elementsPerLong; k++) {
                var oldElementValue = oldLong & maxValue;
                var newElementValue = transform(oldElementValue, polyMap, player.getPlayer());

                newLong |= newElementValue << (elementBits * k); // Insert the next element
                oldLong >>= elementBits; // Shift oldLong to read the next element

                i++; // Check if we've reached the end already
                if (i >= size) break a;
            }
            newArray[j] = newLong;
        }

        return new SimpleBitStorage(elementBits, size, newArray);
    }

    @Unique
    private long transform(long in, PolyMap map, ServerPlayer playerEntity) {
        var state = Block.stateById((int)in);
        return map.getClientStateRawId(state, playerEntity);
    }
}
