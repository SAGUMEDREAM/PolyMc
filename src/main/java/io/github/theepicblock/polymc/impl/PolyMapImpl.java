/*
 * PolyMc
 * Copyright (C) 2020-2020 TheEpicBlock_TEB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package io.github.theepicblock.polymc.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.DebugInfoProvider;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.PolyMcEntrypoint;
import io.github.theepicblock.polymc.api.SharedValuesKey;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.gui.GuiPoly;
import io.github.theepicblock.polymc.api.item.CustomDataAccessor;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.api.item.ItemTransformer;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.api.resource.SimpleAsset;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.resource.ModdedResourceContainerImpl;
import io.github.theepicblock.polymc.impl.resource.ResourcePackImplementation;
import io.github.theepicblock.polymc.impl.resource.json.JModelImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * This is the standard implementation of the PolyMap that PolyMc uses by default.
 * You can use a {@link io.github.theepicblock.polymc.api.PolyRegistry} to build one of these more easily.
 */
public class PolyMapImpl implements PolyMap {
    /**
     * The nbt tag name that stores the original item nbt so it can be restored
     * @see PolyMap#getClientItem(ItemStack, ServerPlayer, ItemLocation)
     * @see #recoverOriginalItem(ItemStack, ServerPlayer)
     */
    private static final String ORIGINAL_ITEM_NBT = "PolyMcOriginal";
    private static final boolean ALWAYS_ADD_CREATIVE_NBT = ConfigManager.getConfig().alwaysSendFullNbt;
    private static final List<ResourceLocation> ADVANCEMENT_BACKGROUNDS = new ArrayList<>();
    /**
     * Encodes all data that's meant to be server controlled. In practice this is simply all the ItemStack data minus
     * the count
     */
    private static final Codec<ItemStack> ITEM_DATA_CODEC = ItemStack.SINGLE_ITEM_CODEC;
    public static final MapCodec<Optional<ItemStack>> ORIGINAL_ITEM_CODEC = ITEM_DATA_CODEC.optionalFieldOf(ORIGINAL_ITEM_NBT);

    private final ImmutableMap<Item,ItemPoly> itemPolys;
    private final ItemTransformer[] globalItemPolys;
    private final ImmutableMap<Block,BlockPoly> blockPolys;
    private final ImmutableMap<MenuType<?>,GuiPoly> guiPolys;
    private final ImmutableMap<EntityType<?>,EntityPoly<?>> entityPolys;
    private final ImmutableList<SharedValuesKey.ResourceContainer> sharedValueResources;

    private final boolean hasBlockWizards;

    public PolyMapImpl(ImmutableMap<Item,ItemPoly> itemPolys,
                       ItemTransformer[] globalItemPolys,
                       ImmutableMap<Block,BlockPoly> blockPolys,
                       ImmutableMap<MenuType<?>,GuiPoly> guiPolys,
                       ImmutableMap<EntityType<?>,EntityPoly<?>> entityPolys,
                       ImmutableList<SharedValuesKey.ResourceContainer> sharedValueResources) {
        this.itemPolys = itemPolys;
        this.globalItemPolys = globalItemPolys;
        this.blockPolys = blockPolys;
        this.guiPolys = guiPolys;
        this.entityPolys = entityPolys;
        this.sharedValueResources = sharedValueResources;

        this.hasBlockWizards = blockPolys.values().stream().anyMatch(BlockPoly::hasWizard);
    }

    public static void updateAdvancementBackgrounds(ServerAdvancementManager advancementLoader) {
        ADVANCEMENT_BACKGROUNDS.clear();
        for (var advancement : advancementLoader.getAllAdvancements()) {
            var optional = advancement.value().display().map(DisplayInfo::getBackground).flatMap(x -> x);
            if (optional.isPresent()) {
                var texture = optional.get();
                ADVANCEMENT_BACKGROUNDS.add(texture.texturePath());
            }
        }
    }

    @Override
    public ItemStack getClientItem(ItemStack serverItem, @Nullable ServerPlayer player, @Nullable ItemLocation location) {
        if (serverItem.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack ret = serverItem;

        ItemPoly poly = itemPolys.get(serverItem.getItem());
        if (poly != null) ret = poly.getClientItem(serverItem, player, location);

        for (ItemTransformer globalPoly : globalItemPolys) {
            ret = globalPoly.transform(serverItem, ret, this, player, location);
        }

        // If max count varies between the client and server item, set the max count.
        if (ret.getMaxStackSize() != serverItem.getMaxStackSize()) ret.set(DataComponents.MAX_STACK_SIZE, serverItem.getMaxStackSize());

        if ((player == null || player.isCreative() || location == ItemLocation.CREATIVE || ALWAYS_ADD_CREATIVE_NBT) && !ItemStack.isSameItemSameComponents(serverItem, ret) && !serverItem.isEmpty()) {

            RegistryOps<Tag> registryOps = Util.getRegistryManager(player).createSerializationContext(NbtOps.INSTANCE);

            // Preserves the nbt of the original item, so it can be reverted
            var finalRet = ret;
            PolymerCommonUtils.executeWithoutNetworkingLogic(() -> {
                CustomDataAccessor accessor = (CustomDataAccessor)(Object)CustomData.EMPTY;
                ORIGINAL_ITEM_CODEC.encode(Optional.of(serverItem), registryOps, registryOps.mapBuilder()).build(accessor.polyMc$getTag()).map((tag->{
                    return CustomData.of((CompoundTag)tag);
                })).result().ifPresent((nbt) -> {
                    finalRet.set(DataComponents.CUSTOM_DATA, nbt);
                });
            });
        }

        return ret;
    }

    @Override
    public ItemPoly getItemPoly(Item item) {
        return itemPolys.get(item);
    }

    @Override
    public BlockPoly getBlockPoly(Block block) {
        return blockPolys.get(block);
    }

    @Override
    public GuiPoly getGuiPoly(MenuType<?> serverGuiType) {
        return guiPolys.get(serverGuiType);
    }

    @Override
    public <T extends Entity> EntityPoly<T> getEntityPoly(EntityType<T> entity) {
        return (EntityPoly<T>)entityPolys.get(entity);
    }

    @Override
    public ItemStack reverseClientItem(ItemStack clientItem, @Nullable ServerPlayer player) {
        return recoverOriginalItem(clientItem, player);
    }

    public static ItemStack recoverOriginalItem(ItemStack input, @Nullable ServerPlayer player) {
        var data = input.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return input;
        }
        var dataAccessor = ((CustomDataAccessor)(Object)data);
        var registryOps = Util.getRegistryManager(player).createSerializationContext(NbtOps.INSTANCE);
        var result = dataAccessor.polyMc$read(registryOps, ORIGINAL_ITEM_CODEC);
        if (result.error().isPresent()) {
            var stack = new ItemStack(Items.CLAY_BALL);
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("Invalid Item").withStyle(ChatFormatting.ITALIC));
            return stack;
        } else {
            // Return the original only if it's present
            var polymcOriginal = result.result().orElseThrow();
            ItemStack recovered_stack = polymcOriginal.orElse(input);
            recovered_stack.setCount(input.getCount());
            return recovered_stack;
        }
    }

    @Override
    public boolean isVanillaLikeMap() {
        return true;
    }

    @Override
    public boolean hasBlockWizards() {
        return hasBlockWizards;
    }

    @Override
    public boolean shouldForceBlockStateSync(BlockState sourceState, BlockState clientState, Direction direction) {
        Block block = clientState.getBlock();
        if (block == Blocks.NOTE_BLOCK) {
            return direction == Direction.UP || direction == Direction.DOWN;
        } else if (block == Blocks.MYCELIUM || block == Blocks.PODZOL) {
            return direction == Direction.DOWN;
        } else if (block == Blocks.TRIPWIRE) {
            if (sourceState == null) return direction.getAxis().isHorizontal();

            //Checks if the connected property for the block isn't what it should be
            //If the source block in that direction is string, it should be true. Otherwise false
            return direction.getAxis().isHorizontal() &&
                    clientState.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction.getOpposite())) != (sourceState.getBlock() instanceof TripWireBlock);
        }
        return false;
    }

    @Override
    public @Nullable PolyMcResourcePack generateResourcePack(SimpleLogger logger) {
        var moddedResources = new ModdedResourceContainerImpl();
        var pack = new ResourcePackImplementation();

        logger.info("Using: " + moddedResources);

        //Let mods register resources via the api
        List<PolyMcEntrypoint> entrypoints = FabricLoader.getInstance().getEntrypoints("polymc", PolyMcEntrypoint.class);
        for (PolyMcEntrypoint entrypointEntry : entrypoints) {
            entrypointEntry.registerModSpecificResources(moddedResources, pack, logger);
        }

        for (var prefix : new String[]{"items/", "equipment/", "textures/"}) {
            for (var itemFile : moddedResources.locateFiles(prefix)) {
                pack.setAsset(itemFile.getA().getNamespace(), itemFile.getA().getPath(), new SimpleAsset(itemFile.getB()));
            }
        }

        for (var itemFile : moddedResources.locateFiles("models/")) {
            if (itemFile.getA().getPath().endsWith(".json")) {
                try {
                    pack.setAsset(itemFile.getA().getNamespace(), itemFile.getA().getPath(), JModelImpl.of(itemFile.getB().get(), itemFile.getA().toString()));
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }

        // Hooks for all itempolys
        this.itemPolys.forEach((item, itemPoly) -> {
            try {
                itemPoly.addToResourcePack(item, moddedResources, pack, logger);
            } catch (Throwable e) {
                logger.warn("Exception whilst generating resources for " + item.getDescriptionId());
                e.printStackTrace();
            }
        });

        // Hooks for all blockpolys
        this.blockPolys.forEach((block, blockPoly) -> {
            try {
                blockPoly.addToResourcePack(block, moddedResources, pack, logger);
            } catch (Throwable e) {
                logger.warn("Exception whilst generating resources for " + block.getDescriptionId());
                e.printStackTrace();
            }
        });

        // Write the resources generated from shared values
        sharedValueResources.forEach((sharedValueResourceContainer) -> {
            try {
                sharedValueResourceContainer.addToResourcePack(moddedResources, pack, logger);
            } catch (Throwable e) {
                logger.warn("Exception whilst generating resources for shared values: " + sharedValueResourceContainer);
                e.printStackTrace();
            }
        });

        // Import the language files for all mods
        var languageKeys = new TreeMap<String, Map<String, String>>(); // The first hashmap is per-language. Then it's translationkey->translation
        for (var lang : moddedResources.locateLanguageFiles()) {
            // Ignore fapi
            if (lang.getA().getNamespace().equals("fabric")) continue;
            try (var streamReader = new InputStreamReader(lang.getB().get(), StandardCharsets.UTF_8)){
                // Copy all the language keys into the main map
                var languageObject = pack.getGson().fromJson(streamReader, JsonObject.class);
                var mainLangMap = languageKeys.computeIfAbsent(lang.getA().getPath(), (key) -> new TreeMap<>());
                languageObject.entrySet().forEach(entry -> addTranslation(mainLangMap, entry.getKey(), entry.getValue()));
            } catch (JsonSyntaxException e) {
                logger.warn(lang.getA() + " is not a valid json file! " + e.getMessage());
            } catch (Throwable e) {
                logger.error("Couldn't parse lang file " + lang.getA());
                e.printStackTrace();
            }
        }
        // It doesn't actually matter which namespace the language files are under. We're just going to put them all under 'polymc-lang'
        languageKeys.forEach((path, translations) -> {
            pack.setAsset("polymc-lang", path, (stream, gson) -> {
                Util.writeJsonToStream(stream, gson, translations);
            });
        });

        // Import sounds
        for (var namespace : moddedResources.getAllNamespaces()) {
            try {
                var soundsRegistry = moddedResources.getSoundRegistry(namespace, "sounds.json");
                if (soundsRegistry == null) continue;
                pack.setSoundRegistry(namespace, "sounds.json", soundsRegistry);
                pack.importRequirements(moddedResources, soundsRegistry, logger);
            } catch (Throwable e) {
                logger.error("Couldn't parse sounds file " + namespace);
                e.printStackTrace();
            }
        }


        for (var texture : ADVANCEMENT_BACKGROUNDS) {
            var asset = moddedResources.getTextureRaw(texture.getNamespace(), texture.getPath());
            if (asset != null) {
                pack.setAsset(texture.getNamespace(), texture.getPath(), asset);
                pack.importRequirements(moddedResources, asset, logger);
            }
        }


        try {
            moddedResources.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed to close modded resources");
        }
        return pack;
    }

    private void addTranslation(Map<String, String> mainLangMap, String key, JsonElement value) {
        if (value instanceof JsonArray array) { // Assume owo lib text
            var x = ComponentSerialization.CODEC.decode(PolyMc.FALLBACK_REGISTRY_MANAGER.createSerializationContext(JsonOps.INSTANCE), array).result().map(Pair::getFirst).orElse(null);
            mainLangMap.put(key, x != null ? x.getString() : "<INVALID TRANSLATION: " + key + ">");
        } else if (value instanceof JsonObject object) { // Assume that one library which allows objects for text
            for (var e : object.entrySet()) {
                addTranslation(mainLangMap, key + "." + e.getKey(), e.getValue());
            }
        } else { // Vanilla Translation
            mainLangMap.put(key, GsonHelper.convertToString(value, key));
        }
    }

    @Override
    public String dumpDebugInfo() {
        StringBuilder builder = new StringBuilder();

        writeHeader(builder, "ITEMS");
        this.itemPolys
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(item -> item.getKey().getDescriptionId()))
                .forEach(entry -> {
                    var item = entry.getKey();
                    var poly = entry.getValue();
                    addDebugProviderToDump(builder, item, item.getDescriptionId(), poly);
        });

        writeHeader(builder, "BLOCKS");
        this.blockPolys
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(block -> block.getKey().getDescriptionId()))
                .forEach(entry -> {
                    var block = entry.getKey();
                    var poly = entry.getValue();
                    addDebugProviderToDump(builder, block, block.getDescriptionId(), poly);
        });

        writeHeader(builder, "ENTITIES");
        this.entityPolys
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(block -> block.getKey().getDescriptionId()))
                .forEach(entry -> {
                    var entity = entry.getKey();
                    var poly = entry.getValue();
                    addDebugProviderToDump(builder, entity, entity.getDescriptionId(), poly);
                });

        this.sharedValueResources.stream()
                .flatMap(sharedValue -> sharedValue.addDebugSections().stream())
                .forEach(debugSection -> {
                    writeHeader(builder, debugSection.name());
                    debugSection.writer().accept(builder);
                });

        return builder.toString();
    }

    private static void writeHeader(StringBuilder builder, String n) {
        var middleLine = "## " + n + " ##";

        // First line
        middleLine.chars().forEach(i -> builder.append("#"));
        builder.append("\n");
        // Middle line
        builder.append(middleLine);
        builder.append("\n");
        // Last line
        middleLine.chars().forEach(i -> builder.append("#"));
        builder.append("\n");
    }

    private static <T> void addDebugProviderToDump(StringBuilder b, T object, String key, DebugInfoProvider<T> poly) {
        b.append(Util.expandTo(key, 45));
        b.append(" --> ");
        b.append(Util.expandTo(poly.getClass().getName(), 60));
        try {
            String info = poly.getDebugInfo(object);
            if (info != null) {
                b.append("|");
                b.append(info);
            }
        } catch (Exception e) {
            PolyMc.LOGGER.info(String.format("Error whilst getting debug info from '%s' which is registered to '%s'", poly.getClass().getName(), key));
            e.printStackTrace();
        }
        b.append("\n");
    }
}
