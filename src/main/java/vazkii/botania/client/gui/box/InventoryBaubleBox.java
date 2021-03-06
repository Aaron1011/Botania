/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [25/11/2015, 19:59:16 (GMT)]
 */
package vazkii.botania.client.gui.box;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class InventoryBaubleBox implements IItemHandlerModifiable {

	private final IItemHandlerModifiable boxInv;
	final ItemStack box;

	public InventoryBaubleBox(ItemStack box) {
		this.box = box;
		this.boxInv = (IItemHandlerModifiable) box.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		boxInv.setStackInSlot(slot, stack);
	}

	@Override
	public int getSlots() {
		return boxInv.getSlots();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return boxInv.getStackInSlot(slot);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		return boxInv.insertItem(slot, stack, simulate);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return boxInv.extractItem(slot, amount, simulate);
	}

}
