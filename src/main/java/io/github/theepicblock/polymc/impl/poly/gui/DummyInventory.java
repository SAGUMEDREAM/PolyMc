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

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DummyInventory implements Container {
    private final int size;

    public DummyInventory(int size) {
        this.size = size;
    }

    @Override
    public int getContainerSize() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getItem(int slot) {
        return new ItemStack(Items.DIRT);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return new ItemStack(Items.DIRT);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return new ItemStack(Items.DIRT);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {

    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {

    }
}
