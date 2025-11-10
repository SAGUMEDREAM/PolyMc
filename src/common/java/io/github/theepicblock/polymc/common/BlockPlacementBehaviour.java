package io.github.theepicblock.polymc.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.html.BlockView;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

public enum BlockPlacementBehaviour {
    /**
     * Block always places a full-block
     */
    FULL_BLOCK(i -> normalBlockItem(i) && allShapes(i, Block::isShapeFullBlock) && getPlaceAtClass(i) == BlockBehaviour.class && hasNormalCanReplace(i)),
    /**
     * Block always places an empty block
     */
    EMPTY_BLOCK(i -> (normalBlockItem(i) || i.getClass() == DoubleHighBlockItem.class) && allShapes(i, VoxelShape::isEmpty) && getPlaceAtClass(i) == BlockBehaviour.class && hasNormalCanReplace(i)),
    /**
     * Placeable on farmland
     */
    CROP(i -> (normalBlockItem(i) || i.getClass() == DoubleHighBlockItem.class) && allShapes(i, VoxelShape::isEmpty) && getPlaceAtClass(i) == CropBlock.class && getPlantOnTopClass(i) == CropBlock.class && hasNormalCanReplace(i)),
    /**
     * Placeable on farmland and dirt
     */
    PLANT(i -> normalBlockItem(i) && allShapes(i, VoxelShape::isEmpty) && getPlaceAtClass(i) == VegetationBlock.class && getPlantOnTopClass(i) == VegetationBlock.class && hasNormalCanReplace(i)),
    DOOR(i -> i.getClass() == DoubleHighBlockItem.class && getBehaviourClass(i) == DoorBlock.class && getPlaceAtClass(i) == DoorBlock.class && getCollisionClass(i) == DoorBlock.class && hasNormalCanReplace(i)),
    TRAP_DOOR(i -> normalBlockItem(i) && getBehaviourClass(i) == TrapDoorBlock.class && getPlaceAtClass(i) == BlockBehaviour.class && getCollisionClass(i) == TrapDoorBlock.class && hasNormalCanReplace(i)),
    SLAB(i -> normalBlockItem(i) && getBehaviourClass(i) == SlabBlock.class && getPlaceAtClass(i) == BlockBehaviour.class && getCollisionClass(i) == SlabBlock.class && getCanReplaceClass(i) == SlabBlock.class),
    STAIR(i -> normalBlockItem(i) && getBehaviourClass(i) == StairBlock.class && getPlaceAtClass(i) == BlockBehaviour.class && getCollisionClass(i) == StairBlock.class && hasNormalCanReplace(i));

    final Predicate<BlockItem> match;

    BlockPlacementBehaviour(Predicate<BlockItem> match) {
        this.match = match;
    }

    @Nullable
    public static BlockPlacementBehaviour get(BlockItem item) {
        for (var behaviour : BlockPlacementBehaviour.values()) {
            if (behaviour.match.test(item)) {
                return behaviour;
            }
        }
        return null;
    }

    private static boolean allShapes(BlockItem i, Predicate<VoxelShape> predicate) {
        // We assume that the block only places states of itself
        var blocks = new HashMap<Block, Item>();
        i.registerBlocks(blocks, i);
        Stream<BlockState> states = blocks.keySet().stream().flatMap(
                block -> block.getStateDefinition().getPossibleStates().stream()
        );

        return states.allMatch(state -> {
            try {
                return predicate.test(state.getCollisionShape(null, null));
            } catch (Exception e) {
                return false;
            }
        });
    }

    private static boolean normalBlockItem(BlockItem item) {
        var itemClass = item.getClass();
        return itemClass == BlockItem.class || itemClass == StandingAndWallBlockItem.class;
    }

    private static boolean hasNormalCanReplace(BlockItem item) {
        return getCanReplaceClass(item) == BlockBehaviour.class || getCanReplaceClass(item) == GrowingPlantBodyBlock.class;
    }

    private static Class<?> getCanReplaceClass(BlockItem item) {
        return getDefiner(item, "canReplace", BlockState.class, BlockPlaceContext.class);
    }

    private static Class<?> getPlantOnTopClass(BlockItem item) {
        return getDefiner(item, "canPlantOnTop", BlockState.class, BlockView.class, BlockPos.class);
    }

    private static Class<?> getPlaceAtClass(BlockItem item) {
        return getDefiner(item, "canPlaceAt", BlockState.class, LevelReader.class, BlockPos.class);
    }

    private static Class<?> getBehaviourClass(BlockItem item) {
        return getDefiner(item, "getPlacementState", BlockPlaceContext.class);
    }

    private static Class<?> getDefiner(BlockItem item, String methodName, Class<?>... parameters) {
        var block = item.getBlock();
        var blockClass = block.getClass();
        try {
            var method = blockClass.getMethod(methodName, parameters);
            return method.getDeclaringClass();
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Class<?> getCollisionClass(BlockItem item) {
        var block = item.getBlock();
        var blockClass = block.getClass();
        try {
            var method = blockClass.getMethod("getCollisionShape", BlockState.class, BlockView.class, BlockPos.class, CollisionContext.class);
            if (method.getDeclaringClass() == BlockBehaviour.class) {
                return blockClass.getMethod("getOutlineShape", BlockState.class, BlockView.class, BlockPos.class, CollisionContext.class).getDeclaringClass();
            } else {
                return method.getDeclaringClass();
            }
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
