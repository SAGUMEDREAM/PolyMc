package io.github.theepicblock.polymc.datagen;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;

public class Main implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("datagen");

    @Override
    public void onInitialize() {
        try {
            LOGGER.info("Retrieving vanilla ids");
            String output = System.getenv("output-dir");
            File outputDir = new File(output);
            outputDir.mkdirs();

            FriendlyByteBuf outBuf = new FriendlyByteBuf(Unpooled.buffer());
            File outputFile = new File(outputDir, "block-ids");
            LOGGER.info("Vanilla ids: "+outputFile.toPath().toAbsolutePath());

            var properties = new HashSet<Property<?>>();
            for (var block : BuiltInRegistries.BLOCK) {
                properties.addAll(block.defaultBlockState().getProperties());
            }

            var propertyTable = new PropertyLookupTable(properties);

            propertyTable.write(outBuf);

            outBuf.writeVarInt(Block.BLOCK_STATE_REGISTRY.size());
            outBuf.writeVarInt(BuiltInRegistries.BLOCK.size());
            for (var block : BuiltInRegistries.BLOCK) {
                writeBlock(block, propertyTable, outBuf);
            }

            Files.write(outputFile.toPath(), outBuf.array(), StandardOpenOption.CREATE);

            PopulateBlockItemInfo.doStuff(outputDir);

            System.exit(0); // Shut down the server
        } catch (Exception e) {
            e.printStackTrace();
            // Shutdown the server and tell Gradle something went wrong
            System.exit(1);
        }
    }

    private static void writeBlock(Block block, PropertyLookupTable table, FriendlyByteBuf buf) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (!id.getNamespace().equals("minecraft")) {
            // This is supposed to be a list with vanilla ids, no modded allowed
            throw new AssertionError("Non-mc block detected: "+id);
        }

        // Write block id
        buf.writeUtf(id.getPath());

        // Write property types
        var properties = block.defaultBlockState().getProperties();
        buf.writeCollection(properties, (byteBuf, property) -> {
            byteBuf.writeVarInt(table.getPropertyId(property));
        });


        var states = block.getStateDefinition().getPossibleStates();
        // Write first id
        buf.writeVarInt(Block.getId(states.get(0)));
        var lastId = Block.getId(states.get(0))-1;

        buf.writeVarInt(states.size());
        for (var state : states) {
            // Write its property values
            for (var property : properties) {
                buf.writeVarInt(table.getValueId(property, state.getValue(property)));
            }
            // And finally, write the correct id for it
            if (lastId+1 != Block.getId(state)) {
                throw new AssertionError("Unordered id's!");
            }
            lastId = Block.getId(state);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String nameValue(Property<T> property, Comparable<?> value) {
        return property.getName((T)value);
    }
}
