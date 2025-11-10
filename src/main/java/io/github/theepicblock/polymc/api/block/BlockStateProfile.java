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
package io.github.theepicblock.polymc.api.block;

import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.impl.poly.block.ConditionalSimpleBlockPoly;
import io.github.theepicblock.polymc.impl.poly.block.ListOfSlabs;
import io.github.theepicblock.polymc.impl.poly.block.PropertyRetainingReplacementPoly;
import io.github.theepicblock.polymc.impl.poly.block.SimpleReplacementPoly;
import net.minecraft.core.Direction;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SmallDripleafBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.WallSide;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * A BlockStateProfile defines a group of states which may be used on the client for other purposes.
 * The {@link #onFirstRegister} field should register a poly which removes these states from regular use.
 *
 * PolyMc has a whole host of built-in profiles which make use of various properties that don't affect the client.
 */
@SuppressWarnings("PointlessBooleanExpression")
public class BlockStateProfile {
    public final Block[] blocks;
    public final Predicate<BlockState> filter;
    public final BiConsumer<Block,PolyRegistry> onFirstRegister;
    public final String name;

    /**
     * @deprecated Use {@link #newProfile(String, Block[], Predicate, BiConsumer)} instead
     */
    @ApiStatus.Internal
    @Deprecated
    public BlockStateProfile(String name, Block[] blocks, Predicate<BlockState> filter, BiConsumer<Block,PolyRegistry> onFirstRegister) {
        this.blocks = blocks;
        this.filter = filter;
        this.onFirstRegister = onFirstRegister;
        this.name = name;
    }

    /**
     * @deprecated Use {@link #newProfile(String, Block, Predicate, BiConsumer)} instead
     */
    @Deprecated
    public BlockStateProfile(String name, Block block, Predicate<BlockState> filter, BiConsumer<Block,PolyRegistry> onFirstRegister) {
        this.blocks = new Block[]{block};
        this.filter = filter;
        this.onFirstRegister = onFirstRegister;
        this.name = name;
    }

    //////////////////
    // ALL PROFILES //
    //////////////////
    public static final List<BlockStateProfile> ALL_PROFILES = new ArrayList<>();

    ///////////////////////
    //  LISTS OF BLOCKS  //
    ///////////////////////
    private static final Block[] NO_COLOUR_LEAVES_BLOCKS = {Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES, Blocks.CHERRY_LEAVES};
    private static final Block[] CONSTANT_COLOUR_LEAVES_BLOCKS = {Blocks.BIRCH_LEAVES, Blocks.SPRUCE_LEAVES};
    private static final Block[] BIOME_COLOUR_LEAVES_BLOCKS = {Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.MANGROVE_LEAVES};
    private static final Block[] SAPLING_BLOCKS = {Blocks.ACACIA_SAPLING, Blocks.BIRCH_SAPLING, Blocks.DARK_OAK_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.CHERRY_SAPLING};
    private static final Block[] DOOR_BLOCKS = {Blocks.ACACIA_DOOR, Blocks.BIRCH_DOOR, Blocks.DARK_OAK_DOOR, Blocks.JUNGLE_DOOR, Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.CRIMSON_DOOR, Blocks.WARPED_DOOR, Blocks.MANGROVE_DOOR, Blocks.CHERRY_DOOR};
    private static final Block[] TRAPDOOR_BLOCKS = {Blocks.ACACIA_TRAPDOOR, Blocks.BIRCH_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.JUNGLE_TRAPDOOR, Blocks.OAK_TRAPDOOR, Blocks.SPRUCE_TRAPDOOR, Blocks.CRIMSON_TRAPDOOR, Blocks.WARPED_TRAPDOOR, Blocks.MANGROVE_TRAPDOOR, Blocks.CHERRY_TRAPDOOR};
    private static final Block[] FENCE_GATE_BLOCKS = {Blocks.ACACIA_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.CRIMSON_FENCE_GATE, Blocks.WARPED_FENCE_GATE, Blocks.MANGROVE_FENCE_GATE, Blocks.CHERRY_FENCE_GATE};
    private static final Block[] WAXED_COPPER_STAIR_BLOCKS = {Blocks.WAXED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS};
    private static final Block[] WAXED_COPPER_SLAB_BLOCKS = {Blocks.WAXED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB};
    private static final Block[] WAXED_COPPER_FULL_BLOCKS = {Blocks.WAXED_COPPER_BLOCK, Blocks.WAXED_EXPOSED_COPPER, Blocks.WAXED_WEATHERED_COPPER, Blocks.WAXED_OXIDIZED_COPPER, Blocks.WAXED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER, Blocks.WAXED_OXIDIZED_COPPER};
    private static final Block[] INFESTED_BLOCKS = {Blocks.INFESTED_COBBLESTONE, Blocks.INFESTED_STONE, Blocks.INFESTED_CHISELED_STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.INFESTED_DEEPSLATE, Blocks.INFESTED_MOSSY_STONE_BRICKS};
    private static final Block[] DISPENSER_BLOCKS = {Blocks.DISPENSER, Blocks.DROPPER};
    private static final Block[] BEEHIVE_BLOCKS = {Blocks.BEEHIVE, Blocks.BEE_NEST};
    private static final Block[] SNOWY_GRASS_BLOCKS = {Blocks.MYCELIUM, Blocks.PODZOL}; // Snowy mycelium and podzol look the same as snowy grass
    private static final Block[] WATERLOGGED_SLABS = {Blocks.SMOOTH_STONE_SLAB}; // Smooth stone double slabs do not look like regular smooth stone blocks. Therefore, only the waterlogged double slab is available to us.
    private static final Block[] WALL_BLOCKS = {Blocks.COBBLESTONE_WALL, Blocks.MOSSY_COBBLESTONE_WALL, Blocks.BRICK_WALL, Blocks.PRISMARINE_WALL, Blocks.RED_SANDSTONE_WALL, Blocks.MOSSY_STONE_BRICK_WALL, Blocks.GRANITE_WALL, Blocks.STONE_BRICK_WALL, Blocks.NETHER_BRICK_WALL, Blocks.ANDESITE_WALL, Blocks.RED_NETHER_BRICK_WALL, Blocks.SANDSTONE_WALL, Blocks.END_STONE_BRICK_WALL, Blocks.DIORITE_WALL, Blocks.BLACKSTONE_WALL, Blocks.POLISHED_BLACKSTONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE_WALL, Blocks.COBBLED_DEEPSLATE_WALL, Blocks.POLISHED_DEEPSLATE_WALL, Blocks.DEEPSLATE_TILE_WALL, Blocks.DEEPSLATE_BRICK_WALL, Blocks.MUD_BRICK_WALL};
    private static final Block[] PRESSURE_PLATE_BLOCKS = {Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE};
    private static final Block[] NETHER_VINE_BLOCKS = {Blocks.WEEPING_VINES, Blocks.TWISTING_VINES};

    /////////////////
    //   FILTERS   //
    /////////////////
    private static final Predicate<BlockState> DEFAULT_FILTER = (blockState) -> blockState != blockState.getBlock().defaultBlockState();
    private static final Predicate<BlockState> ALWAYS_TRUE_FILTER = (blockState) -> true;
    private static final Predicate<BlockState> LEAVES_FILTER = (blockState) ->
            // We choose the persistent states as the ones we don't mess with because that's the default placement state
            blockState != blockState.getBlock().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true) &&
            !blockState.getValue(LeavesBlock.WATERLOGGED);
    private static final Predicate<BlockState> WALL_FILTER = (blockState) ->
            blockState.getValue(WallBlock.NORTH) == WallSide.NONE &&
            blockState.getValue(WallBlock.WEST) == WallSide.NONE &&
            blockState.getValue(WallBlock.EAST) == WallSide.NONE &&
            blockState.getValue(WallBlock.SOUTH) == WallSide.NONE &&
            blockState.getValue(WallBlock.UP) == false;
    private static final Predicate<BlockState> TRIPWIRE_FILTER = BlockStateProfile::isStringUseable;
    private static final Predicate<BlockState> PRESSURE_PLATE_FILTER = state -> state.getValue(BlockStateProperties.POWER) > 1;
    private static final Predicate<BlockState> SMALL_DRIPLEAF_FILTER = state -> state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER && state.getValue(SmallDripleafBlock.FACING) != Direction.NORTH;
    private static final Predicate<BlockState> CAVE_VINES_FILTER = state -> state.getValue(GrowingPlantHeadBlock.AGE) != 0 && state.getValue(CaveVines.BERRIES) != true; // Don't use the berry states, they cause desyncs on right-click
    private static final Predicate<BlockState> FARMLAND_FILTER = (blockState) -> {
        int moisture = blockState.getValue(FarmBlock.MOISTURE);
        return moisture != 0 && moisture != 7;
    };
    private static final Predicate<BlockState> BEEHIVE_FILTER = (blockState) -> {
        int level = blockState.getValue(BeehiveBlock.HONEY_LEVEL);
        return level != 0 && level != BeehiveBlock.MAX_HONEY_LEVELS;
    };
    private static final Predicate<BlockState> DOOR_FILTER = (blockState) -> getDoorCannonState(blockState) != blockState;
    private static final Predicate<BlockState> POWERED_FILTER = (blockState) -> blockState.getValue(BlockStateProperties.POWERED) == true;
    private static final Predicate<BlockState> TRIGGERED_FILTER = (blockState) -> blockState.getValue(BlockStateProperties.TRIGGERED) == true;
    private static final Predicate<BlockState> OPEN_FENCE_GATE_FILTER = POWERED_FILTER.and(state -> state.getValue(FenceGateBlock.OPEN) == true);
    private static final Predicate<BlockState> FENCE_GATE_FILTER = POWERED_FILTER.and(state -> state.getValue(FenceGateBlock.OPEN) == false);
    private static final Predicate<BlockState> SCULK_FILTER = (blockState) -> blockState.getValue(SculkSensorBlock.POWER) != 0 && blockState.getValue(SculkSensorBlock.PHASE) != SculkSensorPhase.ACTIVE; // Active sculk sensors have particles and emissive lighting
    private static final Predicate<BlockState> SNOWY_GRASS_FILTER = (blockState) -> blockState.getValue(GrassBlock.SNOWY);
    private static final Predicate<BlockState> DOUBLE_SLAB_FILTER = (blockState) -> blockState.getValue(SlabBlock.TYPE) == SlabType.DOUBLE;
    private static final Predicate<BlockState> WATERLOGGED_SLAB_FILTER = (blockState) -> blockState.getValue(SlabBlock.TYPE) == SlabType.DOUBLE && blockState.getValue(SlabBlock.WATERLOGGED);
    private static final Predicate<BlockState> SLAB_FILTER = (blockState) -> blockState.getValue(SlabBlock.TYPE) != SlabType.DOUBLE;

    //////////////////////////
    //  ON FIRST REGISTERS  //
    //////////////////////////
    private static final Predicate<BlockState> CHORUS_FLOWER_FILTER = (blockState) -> {
        int age = blockState.getValue(ChorusFlowerBlock.AGE);
        return age > 0 && age < 5;
    };
    private static final Predicate<BlockState> CHORUS_PLANT_FILTER = (blockState) -> {
        boolean down = hasBooleanDirection(blockState, Direction.DOWN);
        boolean up = hasBooleanDirection(blockState, Direction.UP);
        boolean east = hasBooleanDirection(blockState, Direction.EAST);
        boolean north = hasBooleanDirection(blockState, Direction.NORTH);
        boolean south = hasBooleanDirection(blockState, Direction.SOUTH);
        boolean west = hasBooleanDirection(blockState, Direction.WEST);

        int sides = (east ? 1 : 0) + (north ? 1 : 0) + (south ? 1 : 0) + (west ? 1 : 0);

        if (!up && !down) {
            if (sides == 0) {
                return true; // This plant has no sides
            }
        }

        // A middle-piece can not have any sides
        return up && down && sides > 0;
    };

    //ON FIRST REGISTERS
    private static final BiConsumer<Block,PolyRegistry> DEFAULT_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new SimpleReplacementPoly(block.defaultBlockState()));
    private static final BiConsumer<Block,PolyRegistry> LEAVES_ON_FIRST_REGISTER = (block, polyRegistry) -> {
        var defaultState = block.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        polyRegistry.registerBlockPoly(block, input -> defaultState.setValue(BlockStateProperties.WATERLOGGED, input.getValue(BlockStateProperties.WATERLOGGED)));
    };
    private static final BiConsumer<Block,PolyRegistry> WALL_ON_FIRST_REGISTER = (block, polyRegistry) -> {
        polyRegistry.registerBlockPoly(block, input -> {
            if (WALL_FILTER.test(input)) {
                return input.getBlock().defaultBlockState();
            }
            return input;
        });
    };
    private static final BiConsumer<Block,PolyRegistry> TRIPWIRE_ON_FIRST_REGISTER = (block, polyRegistry) -> {
        polyRegistry.registerBlockPoly(block, (input) ->
                input.setValue(BlockStateProperties.POWERED, false).setValue(BlockStateProperties.DISARMED,false)
        );
    };
    private static final BiConsumer<Block,PolyRegistry> SMALL_DRIPLEAF_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, input -> {
        if (input.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
            return input.setValue(SmallDripleafBlock.FACING, Direction.NORTH);
        }
        return input;
    });
    private static final BiConsumer<Block,PolyRegistry> CAVE_VINES_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, input -> input.setValue(GrowingPlantHeadBlock.AGE, 0));
    private static final BiConsumer<Block,PolyRegistry> SCULK_SENSOR_ON_FIRST_REGISTER = (block, polyRegistry) -> {
        polyRegistry.registerBlockPoly(block, (input) ->
                input.setValue(SculkSensorBlock.POWER, 0)
        );
    };
    private static final BiConsumer<Block,PolyRegistry> FARMLAND_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new ConditionalSimpleBlockPoly(Blocks.FARMLAND.defaultBlockState(), FARMLAND_FILTER));
    private static final BiConsumer<Block,PolyRegistry> BEEHIVE_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, (input) -> {
        if (BEEHIVE_FILTER.test(input)) {
            return input.setValue(BeehiveBlock.HONEY_LEVEL, 0);
        }
        return input;
    });
    private static final BiConsumer<Block,PolyRegistry> PRESSURE_PLATE_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, (input) -> {
        if (input.getValue(BlockStateProperties.POWER) > 1) {
            return input.setValue(BlockStateProperties.POWER, 1);
        }
        return input;
    });
    private static final BiConsumer<Block,PolyRegistry> DOOR_BLOCK_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, BlockStateProfile::getDoorCannonState);
    private static final BiConsumer<Block,PolyRegistry> POWERED_BLOCK_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, (input) -> input.setValue(BlockStateProperties.POWERED, false));
    private static final BiConsumer<Block,PolyRegistry> TRIGGERED_BLOCK_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, (input) -> input.setValue(BlockStateProperties.TRIGGERED, false));
    private static final BiConsumer<Block,PolyRegistry> SNOWY_GRASS_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, input -> {
        if (input.getValue(GrassBlock.SNOWY)) {
            return Blocks.GRASS_BLOCK.defaultBlockState().setValue(GrassBlock.SNOWY, true);
        }
        return input;
    });
    private static final BiConsumer<Block,PolyRegistry> WAXED_COPPER_ON_FIRST_REGISTER = (block, polyRegistry) -> {
        var unwaxedBlock = HoneycombItem.WAX_OFF_BY_BLOCK.get().get(block);
        polyRegistry.registerBlockPoly(block, new PropertyRetainingReplacementPoly(unwaxedBlock));
    };
    private static final BiConsumer<Block,PolyRegistry> INFESTED_BLOCK_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, input -> ((InfestedBlock)block).hostStateByInfested(input));
    private static final BiConsumer<Block,PolyRegistry> DOUBLESLAB_ON_FIRST_REGISTER = (block, polyRegistry) -> {
        if (!ListOfSlabs.SLAB2FULL.containsKey(block)) throw new IllegalArgumentException(block.getDescriptionId() + " isn't a registered slab");
        // We can replace (for example) oak double slabs with regular oak planks. Because double slabs can technically be waterlogged this frees up two states per slab
        var regularFullBlock = ListOfSlabs.SLAB2FULL.get(block).defaultBlockState();
        if (ArrayUtils.contains(WAXED_COPPER_SLAB_BLOCKS, block) || block == Blocks.PETRIFIED_OAK_SLAB) {
            var slabReplacement = block == Blocks.PETRIFIED_OAK_SLAB ?
                    Blocks.PETRIFIED_OAK_SLAB.defaultBlockState() :
                    HoneycombItem.WAX_OFF_BY_BLOCK.get().get(block).defaultBlockState();
            polyRegistry.registerBlockPoly(block, (input) -> {
                if (input.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
                    return regularFullBlock;
                } else {
                    return slabReplacement
                            .setValue(SlabBlock.TYPE, input.getValue(SlabBlock.TYPE))
                            .setValue(SlabBlock.WATERLOGGED, input.getValue(SlabBlock.WATERLOGGED));
                }
            });
        } else {
            polyRegistry.registerBlockPoly(block, (input) -> {
                if (input.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
                    return regularFullBlock;
                }
                return input;
            });
        }
    };
    private static final BiConsumer<Block,PolyRegistry> WATERLOGGED_SLAB_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, (input) -> {
        if (WATERLOGGED_SLAB_FILTER.test(input)) {
            return input.setValue(SlabBlock.WATERLOGGED, false);
        }
        return input;
    });
    private static final BiConsumer<Block,PolyRegistry> CHORUS_PLANT_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new ConditionalSimpleBlockPoly(Blocks.CHORUS_PLANT.defaultBlockState(), CHORUS_PLANT_FILTER));
    private static final BiConsumer<Block,PolyRegistry> CHORUS_FLOWER_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new ConditionalSimpleBlockPoly(Blocks.CHORUS_FLOWER.defaultBlockState(), CHORUS_FLOWER_FILTER));

    ////////////////////
    //  SUB PROFILES  //
    ////////////////////
    public static final BlockStateProfile SAPLING_SUB_PROFILE = getProfileWithDefaultFilter("sapling", SAPLING_BLOCKS);
    public static final BlockStateProfile SUGARCANE_SUB_PROFILE = getProfileWithDefaultFilter("sugarcane", Blocks.SUGAR_CANE);
    public static final BlockStateProfile TRIPWIRE_SUB_PROFILE = newProfile("tripwire", Blocks.TRIPWIRE, TRIPWIRE_FILTER, TRIPWIRE_ON_FIRST_REGISTER);
    public static final BlockStateProfile SMALL_DRIPLEAF_SUB_PROFILE = newProfile("drip leaf", Blocks.SMALL_DRIPLEAF, SMALL_DRIPLEAF_FILTER, SMALL_DRIPLEAF_ON_FIRST_REGISTER);
    public static final BlockStateProfile CAVE_VINES_SUB_PROFILE = newProfile("cave vines", Blocks.CAVE_VINES, CAVE_VINES_FILTER, CAVE_VINES_ON_FIRST_REGISTER);
    public static final BlockStateProfile NETHER_VINES_SUB_PROFILE = getProfileWithDefaultFilter("nether vines", NETHER_VINE_BLOCKS);
    public static final BlockStateProfile KELP_SUB_PROFILE = getProfileWithDefaultFilter("kelp", Blocks.KELP);
    public static final BlockStateProfile NOTE_BLOCK_SUB_PROFILE = getProfileWithDefaultFilter("note block", Blocks.NOTE_BLOCK);
    public static final BlockStateProfile TARGET_BLOCK_SUB_PROFILE = getProfileWithDefaultFilter("target block", Blocks.TARGET);
    public static final BlockStateProfile DISPENSER_SUB_PROFILE = newProfile("dispenser and dropper", DISPENSER_BLOCKS, TRIGGERED_FILTER, TRIGGERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile TNT_SUB_PROFILE = getProfileWithDefaultFilter("tnt", Blocks.TNT);
    public static final BlockStateProfile JUKEBOX_SUB_PROFILE = getProfileWithDefaultFilter("jukebox", Blocks.JUKEBOX);
    public static final BlockStateProfile BEEHIVE_SUB_PROFILE = newProfile("beehive", BEEHIVE_BLOCKS, BEEHIVE_FILTER, BEEHIVE_ON_FIRST_REGISTER);
    public static final BlockStateProfile SNOWY_GRASS_SUB_PROFILE = newProfile("snowy grass", SNOWY_GRASS_BLOCKS, SNOWY_GRASS_FILTER, SNOWY_GRASS_ON_FIRST_REGISTER);
    public static final BlockStateProfile DOUBLE_SLAB_SUB_PROFILE = newProfile("slabs", ListOfSlabs.SLAB2FULL.keySet().toArray(Block[]::new), DOUBLE_SLAB_FILTER, DOUBLESLAB_ON_FIRST_REGISTER);
    public static final BlockStateProfile WATERLOGGED_SLAB_SUB_PROFILE = newProfile("waterlogged only slabs", WATERLOGGED_SLABS, WATERLOGGED_SLAB_FILTER, WATERLOGGED_SLAB_ON_FIRST_REGISTER);
    public static final BlockStateProfile WAXED_COPPER_FULLBLOCK_SUB_PROFILE = newProfile("waxed copper fullblocks", WAXED_COPPER_FULL_BLOCKS, ALWAYS_TRUE_FILTER, WAXED_COPPER_ON_FIRST_REGISTER);
    public static final BlockStateProfile INFESTED_STONE_SUB_PROFILE = newProfile("infested stone", INFESTED_BLOCKS, ALWAYS_TRUE_FILTER, INFESTED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile PETRIFIED_OAK_SLAB_SUB_PROFILE = newProfile("petrified oak slab", Blocks.PETRIFIED_OAK_SLAB, SLAB_FILTER, DOUBLESLAB_ON_FIRST_REGISTER); // This profile only handles top/bottom slabs. The double slabs are exposed via `DOUBLE_SLAB_SUB_PROFILE`
    public static final BlockStateProfile WAXED_COPPER_SLAB_SUB_PROFILE = newProfile("waxed copper slab", WAXED_COPPER_SLAB_BLOCKS, SLAB_FILTER, DOUBLESLAB_ON_FIRST_REGISTER); // This profile only handles top/bottom slabs. The double slabs are exposed via `DOUBLE_SLAB_SUB_PROFILE`
    /**
     * Leaves that don't have any block colouring applied
     */
    public static final BlockStateProfile NO_COLOUR_LEAVES_SUB_PROFILE = newProfile("no colour leaves", NO_COLOUR_LEAVES_BLOCKS, LEAVES_FILTER, LEAVES_ON_FIRST_REGISTER);
    public static final BlockStateProfile CONSTANT_COLOUR_LEAVES_SUB_PROFILE = newProfile("constant colour leaves", CONSTANT_COLOUR_LEAVES_BLOCKS, LEAVES_FILTER, LEAVES_ON_FIRST_REGISTER);
    public static final BlockStateProfile BIOME_COLOUR_LEAVES_SUB_PROFILE = newProfile("biome colour leaves", BIOME_COLOUR_LEAVES_BLOCKS, LEAVES_FILTER, LEAVES_ON_FIRST_REGISTER);
    public static final BlockStateProfile OPEN_FENCE_GATE_PROFILE = newProfile("open fence gate", FENCE_GATE_BLOCKS, OPEN_FENCE_GATE_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile FENCE_GATE_PROFILE = newProfile("fence gate", FENCE_GATE_BLOCKS, FENCE_GATE_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile PRESSURE_PLATE_PROFILE = newProfile("pressure plate", PRESSURE_PLATE_BLOCKS, PRESSURE_PLATE_FILTER, PRESSURE_PLATE_ON_FIRST_REGISTER);

    ////////////////
    //  PROFILES  //
    ////////////////
    public static final BlockStateProfile FULL_BLOCK_PROFILE = combine("full blocks", INFESTED_STONE_SUB_PROFILE, /*TNT_SUB_PROFILE,*/ SNOWY_GRASS_SUB_PROFILE, NOTE_BLOCK_SUB_PROFILE, DISPENSER_SUB_PROFILE, BEEHIVE_SUB_PROFILE, WAXED_COPPER_FULLBLOCK_SUB_PROFILE, JUKEBOX_SUB_PROFILE, DOUBLE_SLAB_SUB_PROFILE, TARGET_BLOCK_SUB_PROFILE, WATERLOGGED_SLAB_SUB_PROFILE);
    public static final BlockStateProfile CLIMBABLE_PROFILE = combine("climbable blocks", CAVE_VINES_SUB_PROFILE, NETHER_VINES_SUB_PROFILE);
    public static final BlockStateProfile LEAVES_PROFILE = combine("leaves", BIOME_COLOUR_LEAVES_SUB_PROFILE, CONSTANT_COLOUR_LEAVES_SUB_PROFILE, NO_COLOUR_LEAVES_SUB_PROFILE);
    public static final BlockStateProfile NO_COLLISION_WALL_PROFILE = newProfile("empty walls", WALL_BLOCKS, WALL_FILTER, WALL_ON_FIRST_REGISTER);
    public static final BlockStateProfile NO_COLLISION_PROFILE = combine("blocks without collisions", KELP_SUB_PROFILE, SAPLING_SUB_PROFILE, SUGARCANE_SUB_PROFILE, TRIPWIRE_SUB_PROFILE, SMALL_DRIPLEAF_SUB_PROFILE, OPEN_FENCE_GATE_PROFILE, PRESSURE_PLATE_PROFILE);
    public static final BlockStateProfile FARMLAND_PROFILE = newProfile("farmland", Blocks.FARMLAND, FARMLAND_FILTER, FARMLAND_ON_FIRST_REGISTER);
    public static final BlockStateProfile CACTUS_PROFILE = getProfileWithDefaultFilter("cactus", Blocks.CACTUS);
    public static final BlockStateProfile DOOR_PROFILE = newProfile("door", DOOR_BLOCKS, DOOR_FILTER, DOOR_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile TRAPDOOR_PROFILE = newProfile("trapdoor", TRAPDOOR_BLOCKS, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile METAL_DOOR_PROFILE = newProfile("metal door", Blocks.IRON_DOOR, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile METAL_TRAPDOOR_PROFILE = newProfile("metal trapdoor", Blocks.IRON_TRAPDOOR, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile WAXED_COPPER_STAIR_PROFILE = newProfile("waxed copper stair", WAXED_COPPER_STAIR_BLOCKS, ALWAYS_TRUE_FILTER, WAXED_COPPER_ON_FIRST_REGISTER);
    public static final BlockStateProfile SLAB_PROFILE = combine("slab", PETRIFIED_OAK_SLAB_SUB_PROFILE, WAXED_COPPER_SLAB_SUB_PROFILE);
    public static final BlockStateProfile SCULK_SENSOR_PROFILE = newProfile("sculk sensor", Blocks.SCULK_SENSOR, SCULK_FILTER, SCULK_SENSOR_ON_FIRST_REGISTER);
    public static final BlockStateProfile CHORUS_PLANT_BLOCK_PROFILE = newProfile("chorus plant block", Blocks.CHORUS_PLANT, CHORUS_PLANT_FILTER, CHORUS_PLANT_ON_FIRST_REGISTER);
    public static final BlockStateProfile CHORUS_FLOWER_BLOCK_PROFILE = newProfile("chorus flower block", Blocks.CHORUS_FLOWER, CHORUS_FLOWER_FILTER, CHORUS_FLOWER_ON_FIRST_REGISTER);

    //////////////////
    //  OTHER CODE  //
    //////////////////
    public static BlockStateProfile getProfileWithDefaultFilter(String name, Block[] blocks) {
        return newProfile(name, blocks, DEFAULT_FILTER, DEFAULT_ON_FIRST_REGISTER);
    }

    public static BlockStateProfile getProfileWithDefaultFilter(String name, Block block) {
        return newProfile(name, block, DEFAULT_FILTER, DEFAULT_ON_FIRST_REGISTER);
    }

    public static BlockStateProfile combine(String name, BlockStateProfile... parents) {
        return newProfile(
                name,
                // Combine blocks into one array
                Arrays.stream(parents).flatMap((v) -> Arrays.stream(v.blocks)).toArray(Block[]::new),
                // Use the correct filter for the correct block (it is assumed that no two profiles declare the same block)
                (state) -> {
                    for (var parent : parents) {
                        if (ArrayUtils.contains(parent.blocks, state.getBlock())) {
                            return parent.filter.test(state);
                        }
                    }
                    return true;
                },
                (block, registry) -> {
                    for (var parent : parents) {
                        if (ArrayUtils.contains(parent.blocks, block)) {
                            parent.onFirstRegister.accept(block, registry);
                            break;
                        }
                    }
                }
        );
    }

    public static BlockStateProfile newProfile(String name, Block[] blocks, Predicate<BlockState> filter, BiConsumer<Block,PolyRegistry> onFirstRegister) {
        var profile = new BlockStateProfile(name, blocks, filter, onFirstRegister);
        ALL_PROFILES.add(profile);
        return profile;
    }

    public static BlockStateProfile newProfile(String name, Block block, Predicate<BlockState> filter, BiConsumer<Block,PolyRegistry> onFirstRegister) {
        return newProfile(name, new Block[]{block}, filter, onFirstRegister);
    }

    private static boolean isStringUseable(BlockState state) {
        return  state.getValue(BlockStateProperties.POWERED) == true ||
                state.getValue(TripWireBlock.DISARMED) == true;
    }

    private static BlockState getDoorCannonState(BlockState input) {
        input = input.setValue(BlockStateProperties.POWERED, false);
        var facing = input.getValue(DoorBlock.FACING);
        if (facing == Direction.WEST || facing == Direction.EAST) {
            // West and east are reserved for modded. Let's transform them into a south/north equivalent
            var hinge = input.getValue(DoorBlock.HINGE);
            input = input.setValue(DoorBlock.FACING, (hinge == DoorHingeSide.RIGHT ^ facing == Direction.WEST) ? Direction.NORTH : Direction.SOUTH);
            input = input.cycle(DoorBlock.HINGE);
            input = input.cycle(DoorBlock.OPEN);
        }
        return input;
    }

    public BlockStateProfile and(Predicate<BlockState> filter) {
        return new BlockStateProfile(this.name, this.blocks, this.filter.and(filter), this.onFirstRegister);
    }

    /**
     * Check if the BlockState has the given Direction property enabled.
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     */
    public static boolean hasBooleanDirection(BlockState state, Direction direction) {
        BooleanProperty booleanProperty = PipeBlock.PROPERTY_BY_DIRECTION.get(direction);
        return state.hasProperty(booleanProperty) && state.getValue(booleanProperty);
    }
}
