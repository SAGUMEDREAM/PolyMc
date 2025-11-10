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
package io.github.theepicblock.polymc.impl.generator;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.common.BlockItemType;
import io.github.theepicblock.polymc.impl.ConfigManager;
import io.github.theepicblock.polymc.impl.poly.item.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Class to automatically generate {@link ItemPoly}s for {@link Item}s
 */
public class ItemPolyGenerator {
    /**
     * Generates the most suitable {@link ItemPoly} for a given {@link Item}
     */
    public static ItemPoly generatePoly(Item item, PolyRegistry builder) {
        var cmdManager = builder.getSharedValues(CustomModelDataManager.KEY);

        if (item instanceof AxeItem) {
            return new SimpleItemPoly(Items.IRON_AXE);
        }
        if (item instanceof HoeItem) {
            return new SimpleItemPoly(Items.IRON_HOE);
        }
        if (item instanceof ShovelItem) {
            return new SimpleItemPoly(Items.IRON_SHOVEL);
        }
        if (item instanceof ShieldItem) {
            return new SimpleItemPoly(Items.SHIELD);
        }
        if (item instanceof FishingRodItem) {
            return new SimpleItemPoly(Items.FISHING_ROD);
        }
        if (item instanceof CompassItem) {
            return new SimpleItemPoly(Items.COMPASS);
        }
        if (item instanceof BrushItem) {
            return new SimpleItemPoly(Items.BRUSH);
        }
        if (item instanceof CrossbowItem) {
            return new SimpleItemPoly(Items.CROSSBOW);
        }
        if (item instanceof ProjectileWeaponItem && item.getUseDuration(new ItemStack(item), null) != 0) {
            return new SimpleItemPoly(Items.BOW);
        }
        if (item instanceof BlockItem blockItem) {

            // Todo: fix fuels
            //if (AbstractFurnaceBlockEntity.canUseAsFuel(new ItemStack(item))) {
            //    return new PlaceableItemPoly(CustomModelDataManager.FUEL_ITEMS);
            //}

            if (ConfigManager.getConfig().blockItemMatching) {
                var blockItemInfo = builder.getSharedValues(BlockItemInfo.KEY);
                var vanillaEquivalents = blockItemInfo.getExamples(BlockItemType.of(blockItem));
                if (vanillaEquivalents != null) {
                    // This one doesn't need custom breaking sounds because it's built-in
                    return new SimpleItemPoly(vanillaEquivalents[0]);
                }
            }

            return new PlaceableItemPoly(CustomModelDataManager.BLOCK_ITEMS[0]);
        }
        // Todo: fix fuels
        //if (AbstractFurnaceBlockEntity.canUseAsFuel(new ItemStack(item))) {
        //    return new SimpleItemPoly(CustomModelDataManager.FUEL_ITEMS);
        //}

        return new SimpleItemPoly(Items.TRIAL_KEY);
    }

    /**
     * Attempts to create the best possible {@link ItemPoly} for a {@link BlockItem}.
     * Amongst other factors, this decision might be influenced by the placement logic and the placement sound of the source {@link BlockItem} and the {@link net.minecraft.world.level.block.Block} that's attached to it.
     */
    private static Item[] getBestVanillaItemsForBlockItem(BlockItem item) {
        var block = item.getBlock();
        var fakeWorld = new BlockPolyGenerator.FakedWorld(block.defaultBlockState());

        //Get the state's collision shape.
        VoxelShape collisionShape;
        try {
            collisionShape = block.defaultBlockState().getCollisionShape(fakeWorld, BlockPos.ZERO);
        } catch (Exception e) {
            PolyMc.LOGGER.warn("Failed to get collision shape for " + block.defaultBlockState().toString());
            e.printStackTrace();
            collisionShape = Shapes.INFINITY;
        }
        if (Block.isShapeFullBlock(collisionShape)) {
            return CustomModelDataManager.FULL_BLOCK_ITEMS;
        } else {
            return CustomModelDataManager.BLOCK_ITEMS;
        }

    }

    /**
     * Generates the most suitable {@link ItemPoly} and directly adds it to the {@link PolyRegistry}
     * @see #generatePoly(Item, PolyRegistry)
     */
    public static void addItemToBuilder(Item item, PolyRegistry builder) {
        try {
            builder.registerItemPoly(item, generatePoly(item, builder));
        } catch (Exception e) {
            PolyMc.LOGGER.error("Failed to generate a poly for item " + item.getDescriptionId());
            e.printStackTrace();
            PolyMc.LOGGER.error("Attempting to recover by using a default poly. Please report this");
            builder.registerItemPoly(item, (input, player, location) -> new ItemStack(Items.BARRIER));
        }
    }
}
