package nl.theepicblock.polymc.testmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;

import static nl.theepicblock.polymc.testmod.YellowStatusEffect.YELLOW;

public class Testmod implements ModInitializer {
    private static final String MODID = "polymc-testmod";

    public static final BlockSetType TEST_IRON_BLOCKSET = new BlockSetTypeBuilder()
            .soundGroup(SoundType.BONE_BLOCK)
            .openableByHand(false)
            .register(id("test_iron"));
    public static final BlockSetType TEST_WOOD_BLOCKSET = new BlockSetTypeBuilder()
            .soundGroup(SoundType.WOOL)
            .openableByHand(true)
            .register(id("test_wood"));

    public static final Item TEST_ITEM = registerItem(id("test_item"), settings -> new TestItem(settings.stacksTo(6).rarity(Rarity.EPIC)));
    public static final Item TEST_FOOD = registerItem(id("test_food"), settings -> new Item(settings.food(Foods.COOKED_CHICKEN)));
    public static final ArmorMaterial TEST_MATERIAL = new ArmorMaterial(5, Util.make(new EnumMap<>(ArmorType.class), (map) -> {
        map.put(ArmorType.BOOTS, 1);
        map.put(ArmorType.LEGGINGS, 2);
        map.put(ArmorType.CHESTPLATE, 3);
        map.put(ArmorType.HELMET, 1);
        map.put(ArmorType.BODY, 3);
    }), 5, SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS, 3, 4, TagKey.create(Registries.ITEM, id("empty")), ResourceKey.create(EquipmentAssets.ROOT_ID, id("armor_material")));
    public static final Item TELMET = registerItem(id("test_helmet"), settings -> new Item(settings.humanoidArmor(TEST_MATERIAL, ArmorType.HELMET)));
    public static final Item TESTPLATE =  registerItem(id("test_chestplate"), settings -> new Item(settings.humanoidArmor(TEST_MATERIAL, ArmorType.CHESTPLATE)));
    public static final Item TEGGINGS =  registerItem(id("test_leggings"), settings -> new Item(settings.humanoidArmor(TEST_MATERIAL, ArmorType.LEGGINGS)));
    public static final Item TOOTS =  registerItem(id("test_boots"), settings -> new Item(settings.humanoidArmor(TEST_MATERIAL, ArmorType.BOOTS)));

    public static final Block TEST_BLOCK = registerBlock(id("test_block"), BlockBehaviour.Properties.of(), TestBlock::new);
    public static final Block TEST_STAIRS = registerBlock(id("test_stairs"), BlockBehaviour.Properties.of(), settings -> new TestStairsBlock(TEST_BLOCK.defaultBlockState(), settings));
    public static final Block TEST_SLAB = registerBlock(id("test_slab"), BlockBehaviour.Properties.of(), TestSlabBlock::new);
    public static final Block TEST_DOOR = registerBlock(id("test_door"), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR), settings -> new DoorBlock(TEST_WOOD_BLOCKSET, settings));
    public static final Block TEST_IRON_DOOR = registerBlock(id("test_iron_door"), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR), settings -> new DoorBlock(TEST_IRON_BLOCKSET, settings));
    public static final Block TEST_TRAP_DOOR = registerBlock(id("test_trapdoor"), Block.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR), settings -> new TrapDoorBlock(TEST_WOOD_BLOCKSET, settings));
    public static final Block TEST_IRON_TRAP_DOOR = registerBlock(id("test_iron_trapdoor"), Block.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR), settings -> new TrapDoorBlock(TEST_IRON_BLOCKSET, settings));
    public static final Block TEST_BLOCK_GLOWING = registerBlock(id("test_block_glowing"), Block.Properties.of().lightLevel(x -> 9), Block::new);
    public static final Block TEST_BLOCK_WIZARD = registerBlock(id("test_block_wizard"), Block.Properties.of(), settings -> new ColoredFallingBlock(new ColorRGBA(0), settings));

    public static final EntityType<? extends LivingEntity> TEST_ENTITY_DIRECT = registerEntity(id("test_entity_direct"), FabricEntityTypeBuilder.create().entityFactory(Creeper::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)));
    public static final EntityType<? extends LivingEntity> TEST_ENTITY_EXTEND_DIRECT = registerEntity(id("test_entity_extend_direct"), FabricEntityTypeBuilder.create().entityFactory(TestExtendDirectEntity::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)));
    public static final EntityType<? extends LivingEntity> TEST_ENTITY_EXTEND_MOB = registerEntity(id("test_entity_extend_mob"), FabricEntityTypeBuilder.create().entityFactory(TestExtendMobEntity::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)));
    public static final EntityType<? extends LivingEntity> TEST_ENTITY_EXTEND_GOLEM = registerEntity(id("test_entity_extend_golem"),FabricEntityTypeBuilder.create().entityFactory(TestExtendGolemEntity::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)));
    public static final EntityType<? extends LivingEntity> TEST_ENTITY_LIVING = registerEntity(id("test_entity_extend_living"),FabricEntityTypeBuilder.create().entityFactory(TestLivingEntity::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)));
    public static final EntityType<?> TEST_ENTITY_OTHER = registerEntity(id("test_entity_other"),FabricEntityTypeBuilder.create().entityFactory(TestOtherEntity::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)));
    public static final EntityType<?> TEST_FLYING_WAXED_WEATHERED_CUT_COPPER_STAIRS_ENTITY = registerEntity(id("test_flying_waxed_weathered_cut_copper_stairs"), FabricEntityTypeBuilder.create().entityFactory(TestFlyingWaxedWeatheredCutCopperStairs::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)));

    public static final Holder.Reference<MobEffect> TEST_EFFECT = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, id("yellow_effect"), new YellowStatusEffect(MobEffectCategory.HARMFUL.HARMFUL, YELLOW));
    public static final Potion TEST_POTION_TYPE = Registry.register(BuiltInRegistries.POTION, id("yellow_potion"), new Potion("yellow_potion", new MobEffectInstance(TEST_EFFECT, 9600)));

    @Override
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(TEST_ENTITY_DIRECT, Creeper.createAttributes());
        FabricDefaultAttributeRegistry.register(TEST_ENTITY_EXTEND_DIRECT, Creeper.createAttributes());

        FabricDefaultAttributeRegistry.register(TEST_ENTITY_EXTEND_MOB, Mob.createMobAttributes());

        FabricDefaultAttributeRegistry.register(TEST_ENTITY_EXTEND_GOLEM, Mob.createMobAttributes());

        FabricDefaultAttributeRegistry.register(TEST_ENTITY_LIVING, LivingEntity.createLivingAttributes());

        CommandRegistrationCallback.EVENT.register(TestCommands::register);

        var e = BuiltInRegistries.POTION.getId(TEST_POTION_TYPE);
        System.out.println("qwertgyuwgdyuyqw "+e);
        e = BuiltInRegistries.MOB_EFFECT.getId(TEST_EFFECT.value());
        System.out.println("eeeeeeeeeeeeeeee "+e);
    }

    public static void debugSend(@Nullable Player playerEntity, String text) {
        if (playerEntity != null) playerEntity.displayClientMessage(Component.literal(text), false);
    }

    private static <T extends Entity> EntityType<T> registerEntity(ResourceLocation id, FabricEntityTypeBuilder<T> builder) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, id, builder.build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    }


    private static <T extends Item> T registerItem(ResourceLocation id, Function<Item.Properties, T> block) {
        var entry = block.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
        Registry.register(BuiltInRegistries.ITEM, id, entry);
        return entry;
    }

    private static <T extends Block> T registerBlock(ResourceLocation id, BlockBehaviour.Properties settings, Function<BlockBehaviour.Properties, T> block) {
        var entry = block.apply(settings.setId(ResourceKey.create(Registries.BLOCK, id)));
        Registry.register(BuiltInRegistries.BLOCK, id, entry);
        Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(entry, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
        return entry;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
