package io.github.theepicblock.polymc.impl;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.gui.GuiPoly;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class NOPPolyMap implements PolyMap {
    public static final NOPPolyMap INSTANCE = new NOPPolyMap();
    @Override
    public ItemStack getClientItem(ItemStack serverItem, @Nullable ServerPlayer player, @Nullable ItemLocation location) {
        return serverItem;
    }

    @Override
    public BlockState getClientState(BlockState serverBlock, @Nullable ServerPlayer player) {
        return serverBlock;
    }

    @Override
    public ItemPoly getItemPoly(Item item) {
        return null;
    }

    @Override
    public GuiPoly getGuiPoly(MenuType<?> serverGuiType) {
        return null;
    }

    @Override
    public BlockPoly getBlockPoly(Block block) {
        return null;
    }

    @Override
    public <T extends Entity> EntityPoly<T> getEntityPoly(EntityType<T> entity) {
        return null;
    }

    @Override
    public ItemStack reverseClientItem(ItemStack clientItem, @Nullable ServerPlayer player) {
        return clientItem;
    }

    @Override
    public boolean isVanillaLikeMap() {
        return false; //This disables patches meant for vanilla clients
    }

    @Override
    public boolean hasBlockWizards() {
        return false;
    }

    @Override
    public boolean shouldForceBlockStateSync(BlockState sourceState, BlockState clientState, Direction direction) {
        return false;
    }

    @Override
    public @Nullable PolyMcResourcePack generateResourcePack(SimpleLogger logger) {
        return null;
    }

    @Override
    public String dumpDebugInfo() {
        return "";
    }
}
