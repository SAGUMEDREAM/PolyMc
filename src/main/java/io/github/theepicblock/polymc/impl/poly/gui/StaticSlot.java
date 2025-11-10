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
package io.github.theepicblock.polymc.impl.poly.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class StaticSlot extends Slot {
    public final ItemStack stack;

    public StaticSlot(ItemStack stack) {
        super(EmptyInventory.INSTANCE, 0, 0, 0);
        this.stack = stack;
    }

    public void onQuickCraft(ItemStack originalItem, ItemStack itemStack) {
        throw new AssertionError("PolyMc: the contents of a static, unchangeable slot were changed. Containing: " + stack.toString());
    }

    public void onTake(Player player, ItemStack stack) {
        throw new AssertionError("PolyMc: tried to take item out of an static, unchangeable slot. Containing: " + stack.toString());
    }

    public ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    public boolean mayPickup(Player playerEntity) {
        GuiUtils.resyncPlayerInventory(playerEntity);
        return false;
    }

    public ItemStack getItem() {
        return this.stack == null ? ItemStack.EMPTY : this.stack;
    }

    @Override
    public void setByPlayer(ItemStack stack) {
    }

    public void setChanged() {
    }

    public int getMaxStackSize() {
        return this.getItem().getCount();
    }

    public int getMaxStackSize(ItemStack stack) {
        return this.getMaxStackSize();
    }
}
