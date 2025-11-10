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
package io.github.theepicblock.polymc.mixins.gui;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.theepicblock.polymc.api.gui.GuiPoly;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.ScreenHandlerFactoryWrapperSoFabricApiDoesntDetectIt;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Mixin(value = ServerPlayer.class, priority = 600)
public class GuiPolyImplementation {

    @Shadow private int containerCounter;

    /*@Inject(method = "openHandledScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", shift = At.Shift.BEFORE))
    private void replaceHandler(NamedScreenHandlerFactory factory, CallbackInfoReturnable<OptionalInt> cir, @Local LocalRef<ScreenHandler> handler) {
        var base = handler.get();
        GuiPoly poly = PolyMapProvider.getPolyMap((ServerPlayerEntity) (Object) this).getGuiPoly(base.getType());
        if (poly != null) {
            handler.set(poly.replaceScreenHandler(base, (ServerPlayerEntity) (Object) this, this.screenHandlerSyncId));
        }
    }

    @ModifyVariable(method = "openHandledScreen(Lnet/minecraft/screen/NamedScreenHandlerFactory;)Ljava/util/OptionalInt;", at = @At("HEAD"), argsOnly = true)
    private NamedScreenHandlerFactory hackForFabricApi(NamedScreenHandlerFactory factory) {
        if (Util.isPolyMapVanillaLike((ServerPlayerEntity)(Object)this) && factory instanceof ExtendedScreenHandlerFactory) {
            return new ScreenHandlerFactoryWrapperSoFabricApiDoesntDetectIt(factory);
        }
        return factory;
    }*/
}
