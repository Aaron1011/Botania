/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jun 10, 2014, 11:48:12 PM (GMT)]
 */
package vazkii.botania.common.item.equipment.tool;

import com.google.common.collect.Multimap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.item.IColorable;
import vazkii.botania.common.item.equipment.tool.manasteel.ItemManasteelSword;
import vazkii.botania.common.lib.LibItemNames;

import javax.annotation.Nonnull;
import java.awt.*;

public class ItemEnderDagger extends ItemManasteelSword implements IColorable {

	public ItemEnderDagger() {
		super(BotaniaAPI.manasteelToolMaterial, LibItemNames.ENDER_DAGGER);
		setMaxDamage(69); // What you looking at?
		setNoRepair();
	}

	@Override
	public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
		if(par2 == 0)
			return 0xFFFFFF;

		return Color.HSBtoRGB(0.75F, 1F, 1.5F - (float) Math.min(1F, Math.sin(System.currentTimeMillis() / 100D) * 0.5 + 1.2F));
	}

	@Nonnull
	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack) {
		return EnumAction.NONE;
	}

	@Override
	public boolean hitEntity(ItemStack par1ItemStack, EntityLivingBase par2EntityLivingBase, @Nonnull EntityLivingBase par3EntityLivingBase) {
		if(par2EntityLivingBase instanceof EntityEnderman && par3EntityLivingBase instanceof EntityPlayer)
			par2EntityLivingBase.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) par3EntityLivingBase), 20);
		par1ItemStack.damageItem(1, par3EntityLivingBase);
		return true;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity player, int par4, boolean par5) {
		// NO-OP
	}

	@Override
	public boolean usesMana(ItemStack stack) {
		return false;
	}

	@Nonnull
	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);

		if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -1.25, 0));
		}

		return multimap;
	}

}
