/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Mar 29, 2015, 10:16:29 PM (GMT)]
 */
package vazkii.botania.common.item.relic;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import vazkii.botania.api.item.IExtendedWireframeCoordinateListProvider;
import vazkii.botania.api.item.IWireframeCoordinateListProvider;
import vazkii.botania.common.achievement.ICraftAchievement;
import vazkii.botania.common.achievement.ModAchievements;
import vazkii.botania.common.core.handler.MethodHandles;
import vazkii.botania.common.crafting.recipe.AesirRingRecipe;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.lib.LibItemNames;
import baubles.api.BaubleType;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemAesirRing extends ItemRelicBauble implements IExtendedWireframeCoordinateListProvider, ICraftAchievement {

	Multimap<String, AttributeModifier> attributes = HashMultimap.create();

	public ItemAesirRing() {
		super(LibItemNames.AESIR_RING);
		GameRegistry.addRecipe(new AesirRingRecipe());
		RecipeSorter.register("botania:aesirRing", AesirRingRecipe.class, Category.SHAPELESS, "");
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onDropped(ItemTossEvent event) {
		if(event.entityItem != null && event.entityItem.getEntityItem() != null && !event.entityItem.worldObj.isRemote) {
			ItemStack stack = event.entityItem.getEntityItem();
			if(stack.getItem() != null && stack.getItem() == this) {
				event.entityItem.setDead();

				UUID user = getSoulbindUUID(stack);
				for(Item item : new Item[] { ModItems.thorRing, ModItems.lokiRing, ModItems.odinRing }) {
					ItemStack stack1 = new ItemStack(item);
					bindToUUID(user, stack1);
					EntityItem entity = new EntityItem(event.entityItem.worldObj, event.entityItem.posX, event.entityItem.posY, event.entityItem.posZ, stack1);
					entity.motionX = event.entityItem.motionX;
					entity.motionY = event.entityItem.motionY;
					entity.motionZ = event.entityItem.motionZ;

					try {
						MethodHandles.itemAge_setter.invokeExact(entity, MethodHandles.itemAge_getter.invokeExact(entity));
					} catch (Throwable ignored) {}

					int pickupDelay = 0;
					try {
						pickupDelay = (int) MethodHandles.pickupDelay_getter.invokeExact(event.entityItem);
					} catch (Throwable ignored) {}
					entity.setPickupDelay(pickupDelay);

					entity.worldObj.spawnEntityInWorld(entity);
				}
			}
		}
	}

	@Override
	public void onValidPlayerWornTick(ItemStack stack, EntityPlayer player) {
		((ItemOdinRing) ModItems.odinRing).onValidPlayerWornTick(stack, player);
	}

	@Override
	public BaubleType getBaubleType(ItemStack arg0) {
		return BaubleType.RING;
	}

	@Override
	public List<BlockPos> getWireframesToDraw(EntityPlayer player, ItemStack stack) {
		return ((IWireframeCoordinateListProvider) ModItems.lokiRing).getWireframesToDraw(player, stack);
	}

	@Override
	public BlockPos getSourceWireframe(EntityPlayer player, ItemStack stack) {
		return ((IExtendedWireframeCoordinateListProvider) ModItems.lokiRing).getSourceWireframe(player, stack);
	}

	@Override
	public void onEquippedOrLoadedIntoWorld(ItemStack stack, EntityLivingBase player) {
		attributes.clear();
		fillModifiers(attributes, stack);
		player.getAttributeMap().applyAttributeModifiers(attributes);
	}

	@Override
	public void onUnequipped(ItemStack stack, EntityLivingBase player) {
		attributes.clear();
		fillModifiers(attributes, stack);
		player.getAttributeMap().removeAttributeModifiers(attributes);
	}


	void fillModifiers(Multimap<String, AttributeModifier> attributes, ItemStack stack) {
		attributes.put(SharedMonsterAttributes.maxHealth.getAttributeUnlocalizedName(), new AttributeModifier(getBaubleUUID(stack), "Bauble modifier", 20, 0));
	}

	@Override
	public Achievement getAchievementOnCraft(ItemStack stack, EntityPlayer player, IInventory matrix) {
		return ModAchievements.relicAesirRing;
	}

}
