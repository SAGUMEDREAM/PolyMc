package io.github.theepicblock.polymc.impl.misc;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.impl.mixin.BlockStateDuck;
import io.github.theepicblock.polymc.mixins.block.IdListAccessor;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockIdRemapper {
    public static void remapFromInternalList() {
        try {
            var list = readInternalList();
            list.forEach(BlockStateDuck::markVanilla);
            remapBlocks(list);
            PolyMc.LOGGER.info("Successfully remapped "+list.size()+" vanilla blocks");
        } catch (Exception e) {
            PolyMc.LOGGER.error("Couldn't remap block ids");
            e.printStackTrace();
        }
    }

    private static List<BlockState> readInternalList() throws IOException {
        var blob = PolyMc.class.getResourceAsStream("/block-ids").readAllBytes();

        var buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(blob));

        var propertyLookupTable = new PropertyLookupTable(buf);

        var vanillaBlocks = new BlockState[buf.readVarInt()];
        int totalBlocks = buf.readVarInt();
        for (int i = 0; i < totalBlocks; i++) {
            readBlock(buf, propertyLookupTable, vanillaBlocks);
        }

        return List.of(vanillaBlocks);
    }

    private static void readBlock(FriendlyByteBuf buf, PropertyLookupTable table, BlockState[] outputList) {
        var path = buf.readUtf();
        var id = ResourceLocation.parse(path);
        Block block = BuiltInRegistries.BLOCK.getValue(id);

        var baseState = block.defaultBlockState();

        var properties = buf.readCollection(ArrayList::new,
                (buf0) -> table.getProperty(buf.readVarInt(), block));

        var firstStateId = buf.readVarInt();

        var amountOfStates = buf.readVarInt();
        for (int i = 0; i < amountOfStates; i++) {
            var state = baseState;
            for (var property : properties) {
                var valueId = buf.readVarInt();
                state = blockStateWith(state, property, table.getValue(property, valueId));
            }
            var stateId = firstStateId+i;
            if (outputList[stateId] != null) {
                throw new IllegalStateException("Duplicate blockstate for id "+stateId+" : "+path);
            }
            outputList[stateId] = state;
        }
    }

    private static <T extends Comparable<T>, V extends T> BlockState blockStateWith(BlockState state, Property<T> property, Object value) {
        return state.setValue(property, (V)value);
    }

    private static void remapBlocks(List<BlockState> vanillaBlocks) {
        var accessor = (IdListAccessor<BlockState>)Block.BLOCK_STATE_REGISTRY;

        var blockList = accessor.getIdToT();
        var idMap = accessor.getTToId();

        var blockListCopy = List.copyOf(blockList);
        blockList.clear();
        blockList.addAll(vanillaBlocks);

        for (BlockState state : blockListCopy) {
            if (!vanillaBlocks.contains(state)) {
                blockList.add(state);
            }
        }

        // Update idMap to match new ids
        idMap.clear();
        for (int i = 0; i < blockList.size(); i++) {
            var state = blockList.get(i);
            idMap.put(state, i);
        }
    }
}
