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
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.block.BlockStateManager;
import io.github.theepicblock.polymc.api.block.BlockStateProfile;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.BooleanContainer;
import io.github.theepicblock.polymc.impl.poly.block.FunctionBlockStatePoly;
import io.github.theepicblock.polymc.impl.poly.block.SimpleReplacementPoly;
import io.github.theepicblock.polymc.impl.resource.ModdedResourceContainerImpl;
import io.github.theepicblock.polymc.mixins.block.SlabBlockAccessor;
import io.github.theepicblock.polymc.mixins.block.TrapdoorBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Class to automatically generate {@link BlockPoly}s for {@link Block}s
 */
public class BlockPolyGenerator {
    private static final ModdedResources RESOURCES = new ModdedResourceContainerImpl();

    /**
     * Generates the most suitable {@link BlockPoly} for a given {@link Block}
     */
    public static BlockPoly generatePoly(Block block, PolyRegistry registry) {
        return new FunctionBlockStatePoly(block, (state, isUniqueCallback) -> registerClientState(state, isUniqueCallback, registry.getSharedValues(BlockStateManager.KEY)));
    }

    /**
     * @param isUniqueCallback will be set to true if the return value is a unique block that'll only be used for the inputted moddedState
     * @return a client state which best matches the moddedState
     */
    public static BlockState registerClientState(BlockState moddedState, BooleanContainer isUniqueCallback, BlockStateManager manager) {
        var moddedBlock = moddedState.getBlock();
        var fakeWorld = new FakedWorld(moddedState);

        var blockId = BuiltInRegistries.BLOCK.getKey(moddedBlock);
        var blockStateDef = RESOURCES.getBlockState(blockId.getNamespace(), blockId.getPath());

        // This following line works because it gets the best matching variant from the definition itself
        // The effect is that all states which match up to the same entry get deduplicated
        var modelId = blockStateDef != null ? blockId + "[" + blockStateDef.getVariantId(moddedState) + "]" : null;

        //Get the state's collision shape.
        VoxelShape collisionShape;
        try {
            collisionShape = moddedState.getCollisionShape(fakeWorld, BlockPos.ZERO);
        } catch (Exception e) {
            PolyMc.LOGGER.warn("Failed to get collision shape for " + moddedState.toString());
            e.printStackTrace();
            collisionShape = Shapes.INFINITY;
        }

        //=== INVISIBLE BLOCKS ===
        if (moddedState.getRenderShape() == RenderShape.INVISIBLE) {
            //This block is supposed to be invisible anyway

            if (Block.isShapeFullBlock(collisionShape)) {
                isUniqueCallback.set(false);
                return Blocks.BARRIER.defaultBlockState();
            }

            if (collisionShape.isEmpty()) {
                //Try to get its selection shape so we can decide between a structure void (which has a selection box) and air (which doesn't)
                try {
                    VoxelShape outlineShape = moddedState.getShape(fakeWorld, BlockPos.ZERO);

                    if (outlineShape.isEmpty()) {
                        isUniqueCallback.set(false);
                        return Blocks.VOID_AIR.defaultBlockState();
                    } else {
                        isUniqueCallback.set(false);
                        return Blocks.STRUCTURE_VOID.defaultBlockState();
                    }
                } catch (Exception e) {
                    PolyMc.LOGGER.warn("Failed to get outline shape for " + moddedState);
                    e.printStackTrace();
                }
            }

            //This is neither full not empty, yet it's invisible. So the other strategies won't work.
            //Default to stone
            isUniqueCallback.set(false);
            return Blocks.STONE.defaultBlockState();
        }

        //=== FLUIDS ===
        if (moddedBlock instanceof LiquidBlock) {
            isUniqueCallback.set(false);
            return copyAllProperties(moddedState, Blocks.WATER);
        }

        //=== LEAVES ===
        if (moddedBlock instanceof LeavesBlock || moddedState.is(BlockTags.LEAVES)) { //TODO I don't like that leaves can be set tags in datapacks, it might cause issues. However, as not every leaf block extends LeavesBlock I can't see much of a better option. Except to maybe check the id if it ends on "_leaves"
            try {
                isUniqueCallback.set(true);

                var state = manager.requestBlockState(BlockStateProfile.LEAVES_PROFILE, modelId);
                return moddedState.hasProperty(BlockStateProperties.WATERLOGGED) ? state.setValue(BlockStateProperties.WATERLOGGED, moddedState.getValue(BlockStateProperties.WATERLOGGED)) : state;
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== FENCE GATES ===
        if (moddedBlock instanceof FenceGateBlock) {
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState((moddedState.getValue(FenceGateBlock.OPEN) ? BlockStateProfile.OPEN_FENCE_GATE_PROFILE : BlockStateProfile.FENCE_GATE_PROFILE)
                        .and(state -> propertyMatches(state, moddedState, FenceGateBlock.IN_WALL, HorizontalDirectionalBlock.FACING)), modelId);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== (TRAP)DOORS ===
        if (moddedBlock instanceof DoorBlock doorBlock) {
            boolean isIronLike = !doorBlock.type().canOpenByHand();
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState((isIronLike ? BlockStateProfile.METAL_DOOR_PROFILE : BlockStateProfile.DOOR_PROFILE)
                        .and((state) -> propertyMatches(state, moddedState, DoorBlock.OPEN, DoorBlock.FACING, DoorBlock.HINGE, DoorBlock.HALF)), modelId);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }
        if (moddedBlock instanceof TrapDoorBlock trapdoorBlock) {
            boolean isIronLike = !((TrapdoorBlockAccessor)trapdoorBlock).getType().canOpenByHand();
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState((isIronLike ? BlockStateProfile.METAL_TRAPDOOR_PROFILE : BlockStateProfile.TRAPDOOR_PROFILE)
                        .and((state) -> propertyMatches(state, moddedState, TrapDoorBlock.OPEN, TrapDoorBlock.FACING, TrapDoorBlock.HALF, TrapDoorBlock.WATERLOGGED)), modelId);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== SLABS ===
        if (moddedBlock instanceof SlabBlock) {
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(BlockStateProfile.SLAB_PROFILE.and(
                        state -> propertyMatches(state, moddedState, SlabBlock.WATERLOGGED, SlabBlock.TYPE)
                ), modelId);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        if (Util.areEqual(collisionShape, SlabBlockAccessor.getSHAPE_BOTTOM())) {
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(BlockStateProfile.SCULK_SENSOR_PROFILE.and(
                        state -> moddedState.getFluidState().equals(state.getFluidState())
                ), modelId);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== STAIRS ===
        if (moddedBlock instanceof StairBlock) {
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(BlockStateProfile.WAXED_COPPER_STAIR_PROFILE.and(
                        state -> propertyMatches(state, moddedState, StairBlock.FACING, StairBlock.HALF, StairBlock.WATERLOGGED, StairBlock.SHAPE)
                ), modelId);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== FULL BLOCKS ===
        // Blocks that have a full top face and at least something on the bottom are considered full blocks. This works better for some blocks
        if (Block.isFaceFull(collisionShape, Direction.UP) && collisionShape.min(Direction.Axis.Y) <= 0) {

            if (!moddedState.canOcclude()) {
                // Chorus flowers are full cubes & are not opaque.
                // There are only 4 available states to reuse though
                try {
                    isUniqueCallback.set(true);
                    return manager.requestBlockState(BlockStateProfile.CHORUS_FLOWER_BLOCK_PROFILE, modelId);
                } catch (BlockStateManager.StateLimitReachedException ignored) {}

                // Each chorus plant state has a slightly different collision box.
                // But it's roughly a full cube (it's the corners that miss a few pixels of collision)
                // Patbox: This caused way too annoying collision desyncs, so I feel it's best to disable it.
                /*try {
                    isUniqueCallback.set(true);
                    return manager.requestBlockState(BlockStateProfile.CHORUS_PLANT_BLOCK_PROFILE, modelId);
                } catch (BlockStateManager.StateLimitReachedException ignored) {}*/
            }

            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(BlockStateProfile.FULL_BLOCK_PROFILE, modelId);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== NO COLLISION BLOCKS ===
        if (collisionShape.isEmpty() && !(moddedState.getBlock() instanceof WallBlock)) {

            try {
                if (moddedState.is(BlockTags.CLIMBABLE)) {
                    isUniqueCallback.set(true);
                    return manager.requestBlockState(BlockStateProfile.CLIMBABLE_PROFILE, modelId);
                }
            } catch (BlockStateManager.StateLimitReachedException ignored) {}

            var outlineShape = moddedState.getShape(fakeWorld, BlockPos.ZERO);

            if (outlineShape.isEmpty()) {
                try {
                    isUniqueCallback.set(true);
                    return manager.requestBlockState(BlockStateProfile.NO_COLLISION_WALL_PROFILE.and(
                            state -> moddedState.getFluidState().equals(state.getFluidState())
                    ), modelId);
                } catch (BlockStateManager.StateLimitReachedException ignored) {}
            }

            if (outlineShape.max(Direction.Axis.Y) <= (1.0f / 16.0f)) {
                try {
                    isUniqueCallback.set(true);
                    return manager.requestBlockState(BlockStateProfile.PRESSURE_PLATE_PROFILE.and(
                            state -> moddedState.getFluidState().equals(state.getFluidState())
                    ), modelId);
                } catch (BlockStateManager.StateLimitReachedException ignored) {}
            }

            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(BlockStateProfile.NO_COLLISION_PROFILE.and(
                        state -> moddedState.getFluidState().equals(state.getFluidState())
                ), modelId);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== FARMLAND-LIKE BLOCKS ===
        if (Util.areEqual(collisionShape, Blocks.FARMLAND.defaultBlockState().getCollisionShape(fakeWorld, BlockPos.ZERO, CollisionContext.empty()))) {
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(BlockStateProfile.FARMLAND_PROFILE, modelId);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== CACTUS-LIKE BLOCKS ===
        if (Util.areEqual(collisionShape, Blocks.CACTUS.defaultBlockState().getCollisionShape(fakeWorld, BlockPos.ZERO, CollisionContext.empty()))) {
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(BlockStateProfile.CACTUS_PROFILE, modelId);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== DEFAULT ===
        //PolyMc can't handle this block. TODO implement more general polys to more of these cases
        isUniqueCallback.set(false);
        return Blocks.STONE.defaultBlockState();
    }

    public static boolean propertyMatches(BlockState a, BlockState b, Property<?>... properties) {
        for (var property : properties) {
            if (!propertyMatches(a, b, property)) return false;
        }
        return true;
    }

    public static <T extends Comparable<T>> boolean propertyMatches(BlockState a, BlockState b, Property<T> property) {
        return a.getValue(property) == b.getValue(property);
    }

    public static BlockState copyAllProperties(BlockState input, Block output) {
        BlockState out = output.defaultBlockState();
        for (Property<?> p : input.getProperties()) {
            out = copyProperty(out, input, p);
        }
        return out;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState a, BlockState b, Property<T> p) {
        return a.setValue(p, b.getValue(p));
    }

    /**
     * Generates the most suitable {@link BlockPoly} and directly adds it to the {@link PolyRegistry}
     * @see #generatePoly(Block, PolyRegistry)
     */
    public static void addBlockToBuilder(Block block, PolyRegistry builder) {
        try {
            builder.registerBlockPoly(block, generatePoly(block, builder));
        } catch (Exception e) {
            PolyMc.LOGGER.error("Failed to generate a poly for block " + block.getDescriptionId());
            e.printStackTrace();
            PolyMc.LOGGER.error("Attempting to recover by using a default poly. Please report this");
            builder.registerBlockPoly(block, new SimpleReplacementPoly(Blocks.RED_STAINED_GLASS));
        }
    }

    /**
     * A world filled with air except for a single block at 0,0,0.
     */
    public static class FakedWorld implements BlockGetter {
        public final BlockState blockState;
        public @Nullable BlockEntity blockEntity;

        /**
         * Initializes a new fake world. This world is filled with air except for 0,0,0
         * @param block The block that will be used at 0,0,0
         */
        public FakedWorld(BlockState block) {
            blockState = block;
        }

        @Override
        @Nullable
        public BlockEntity getBlockEntity(BlockPos pos) {
            if (this.blockEntity == null && blockState.getBlock() instanceof EntityBlock beProvider) {
                this.blockEntity = beProvider.newBlockEntity(BlockPos.ZERO, blockState);
            }
            return blockEntity;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            if (pos.equals(BlockPos.ZERO)) {
                return blockState;
            }
            return Blocks.AIR.defaultBlockState();
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return this.getBlockState(pos).getFluidState();
        }

        @Override
        public int getHeight() {
            return 255;
        }

        @Override
        public int getMinY() {
            return 0;
        }
    }
}
