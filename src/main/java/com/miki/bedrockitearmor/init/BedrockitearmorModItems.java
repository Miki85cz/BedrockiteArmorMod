package com.miki.bedrockitearmor.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

import net.minecraft.world.item.Item;

import com.miki.bedrockitearmor.item.BedrockiteItem;
import com.miki.bedrockitearmor.BedrockiteArmorMod;

public class BedrockiteArmorModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(BedrockiteArmorMod.MODID);
	public static final DeferredItem<Item> BEDROCKITE_HELMET;
	public static final DeferredItem<Item> BEDROCKITE_CHESTPLATE;
	public static final DeferredItem<Item> BEDROCKITE_LEGGINGS;
	public static final DeferredItem<Item> BEDROCKITE_BOOTS;
	static {
		BEDROCKITE_HELMET = REGISTRY.register("bedrockite_helmet", BedrockiteItem.Helmet::new);
		BEDROCKITE_CHESTPLATE = REGISTRY.register("bedrockite_chestplate", BedrockiteItem.Chestplate::new);
		BEDROCKITE_LEGGINGS = REGISTRY.register("bedrockite_leggings", BedrockiteItem.Leggings::new);
		BEDROCKITE_BOOTS = REGISTRY.register("bedrockite_boots", BedrockiteItem.Boots::new);
	}
}