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

import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class GuiUtils {
    public static List<Slot> removePlayerSlots(List<Slot> base) {
        return base.stream().filter(
                (slot) -> !(slot.container instanceof Inventory)
        ).collect(Collectors.toList());
    }

    public static void resyncPlayerInventory(Player player) {
        if (player instanceof ServerPlayer) {
            resyncPlayerInventory((ServerPlayer)player);
        }
    }

    public static void resyncPlayerInventory(ServerPlayer player) {
        player.containerMenu.sendAllDataToRemote();
    }
}
