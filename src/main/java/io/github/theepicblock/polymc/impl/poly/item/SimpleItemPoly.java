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
package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.api.resource.json.JModelOverride;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.resource.ResourceConstants;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseCooldown;

/**
 * The most standard ItemPoly implementation
 */
public class SimpleItemPoly implements ItemPoly {
    protected final Item clientItem;

    /**
     * Makes a poly that generates the specified item with a custom model data value
     * If the item used doesn't matter it is recommended to use the more generic method instead
     * @param target         the serverside items that can be chosen from
     */
    public SimpleItemPoly(Item target) {
        this.clientItem = target;
    }

    /**
     * Adds PolyMc specific tags to the item to display correctly on the client.
     * These shouldn't change depending on the stack as this method will be cached.
     * For un-cached tags, use {@link #getClientItem(ItemStack, ServerPlayer, ItemLocation)}
     */
    protected void addCustomTagsToItem(ItemStack stack) {}

    @SuppressWarnings("ConstantConditions")
    @Override
    public ItemStack getClientItem(ItemStack input, @Nullable ServerPlayer player, @Nullable ItemLocation location) {
        var output = Util.copyWithItem(input, clientItem, player);

        {
            var current = input.get(DataComponents.USE_COOLDOWN);
            if (current == null) {
                output.set(DataComponents.USE_COOLDOWN, new UseCooldown(0.00001f, Optional.of(BuiltInRegistries.ITEM.getKey(input.getItem()))));
            } else if (current.cooldownGroup().isEmpty()) {
                output.set(DataComponents.USE_COOLDOWN, new UseCooldown(current.seconds(), Optional.of(BuiltInRegistries.ITEM.getKey(input.getItem()))));
            }
        }

        this.addCustomTagsToItem(output);
        output.set(DataComponents.ITEM_NAME, input.getItem().getName(input));

        return output;
    }

    @Override
    public String getDebugInfo(Item item) {
        return "item:" + clientItem.getDescriptionId();
    }
}
