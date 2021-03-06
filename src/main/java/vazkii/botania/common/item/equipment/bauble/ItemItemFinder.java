/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jul 31, 2014, 12:59:16 AM (GMT)]
 */
package vazkii.botania.common.item.equipment.bauble;

import baubles.api.BaubleType;
import baubles.common.lib.PlayerHandler;
import baubles.common.network.PacketHandler;
import baubles.common.network.PacketSyncBauble;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.MerchantRecipe;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import vazkii.botania.api.item.IBaubleRender;
import vazkii.botania.client.core.handler.MiscellaneousIcons;
import vazkii.botania.client.core.helper.IconHelper;
import vazkii.botania.common.Botania;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.lib.LibItemNames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemItemFinder extends ItemBauble implements IBaubleRender {

	private static final String TAG_POSITIONS_OLD = "highlightPositions";
	private static final String TAG_ENTITY_POSITIONS = "highlightPositionsEnt";
	private static final String TAG_BLOCK_POSITIONS = "highlightPositionsBlock";

	public ItemItemFinder() {
		super(LibItemNames.ITEM_FINDER);
	}

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase player) {
		super.onWornTick(stack, player);

		if(!(player instanceof EntityPlayer))
			return;

		if(player.worldObj.isRemote)
			tickClient(stack, (EntityPlayer) player);
		else tickServer(stack, (EntityPlayer) player);
	}

	protected void tickClient(ItemStack stack, EntityPlayer player) {
		if(!Botania.proxy.isTheClientPlayer(player))
			return;

		long[] blocks = getBlockPositions(stack);
		Botania.proxy.setWispFXDepthTest(false);
		for(long l : blocks) {
			BlockPos pos = BlockPos.fromLong(l);
			float m = 0.02F;
			Botania.proxy.wispFX(player.worldObj, pos.getX() + (float) Math.random(), pos.getY() + (float) Math.random(), pos.getZ() + (float) Math.random(), (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.15F + 0.05F * (float) Math.random(), m * (float) (Math.random() - 0.5), m * (float) (Math.random() - 0.5), m * (float) (Math.random() - 0.5));
		}

		int[] entities = ItemNBTHelper.getIntArray(stack, TAG_ENTITY_POSITIONS);
		for(int i : entities) {
			Entity e =  player.worldObj.getEntityByID(i);
			if(e != null && Math.random() < 0.6) {
				Botania.proxy.setWispFXDepthTest(Math.random() < 0.6);
				Botania.proxy.wispFX(player.worldObj, e.posX + (float) (Math.random() * 0.5 - 0.25) * 0.45F, e.posY + e.height, e.posZ + (float) (Math.random() * 0.5 - 0.25) * 0.45F, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.15F + 0.05F * (float) Math.random(), -0.05F - 0.03F * (float) Math.random());
			}
		}

		Botania.proxy.setWispFXDepthTest(true);
	}

	protected void tickServer(ItemStack stack, EntityPlayer player) {
		ItemNBTHelper.removeEntry(stack, TAG_POSITIONS_OLD);

		TIntArrayList entPosBuilder = new TIntArrayList();
		TLongArrayList blockPosBuilder = new TLongArrayList();

		scanForStack(player.getHeldItemMainhand(), player, entPosBuilder, blockPosBuilder);
		scanForStack(player.getHeldItemOffhand(), player, entPosBuilder, blockPosBuilder);

		int[] currentEnts = entPosBuilder.toArray();
		long[] currentBlocks = blockPosBuilder.toArray();

		boolean entsEqual = Arrays.equals(currentEnts, ItemNBTHelper.getIntArray(stack, TAG_ENTITY_POSITIONS));
		boolean blocksEqual = Arrays.equals(currentBlocks, getBlockPositions(stack));

		if(!entsEqual)
			ItemNBTHelper.setIntArray(stack, TAG_ENTITY_POSITIONS, currentEnts);
		if(!blocksEqual)
			setBlockPositions(stack, currentBlocks);
		if(!entsEqual || !blocksEqual)
			PacketHandler.INSTANCE.sendToAll(new PacketSyncBauble(player, 0));
	}

	private void scanForStack(ItemStack pstack, EntityPlayer player, TIntArrayList entIdBuilder, TLongArrayList blockPosBuilder) {
		if(pstack != null || player.isSneaking()) {
			int range = 24;

			List<Entity> entities = player.worldObj.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(player.posX - range, player.posY - range, player.posZ - range, player.posX + range, player.posY + range, player.posZ + range));
			for(Entity e : entities) {
				if(e == player)
					continue;
				if(e.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) && !(e instanceof EntityPlayer)) {
					if(scanInventory(e.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), pstack))
						entIdBuilder.add(e.getEntityId());

				} else if(e instanceof EntityItem) {
					EntityItem item = (EntityItem) e;
					ItemStack istack = item.getEntityItem();
					if(player.isSneaking() || istack.isItemEqual(pstack) && ItemStack.areItemStackTagsEqual(istack, pstack))
						entIdBuilder.add(item.getEntityId());

				} else if(e instanceof IInventory) {
					IInventory inv = (IInventory) e;
					if(scanInventory(new InvWrapper(inv), pstack))
						entIdBuilder.add(e.getEntityId());

				} else if(e instanceof EntityPlayer) {
					EntityPlayer player_ = (EntityPlayer) e;
					IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
					IItemHandler binv = new InvWrapper(PlayerHandler.getPlayerBaubles(player_));
					if(scanInventory(binv, pstack) || scanInventory(playerInv, pstack))
						entIdBuilder.add(player_.getEntityId());

				} else if(e instanceof EntityVillager) {
					EntityVillager villager = (EntityVillager) e;
					ArrayList<MerchantRecipe> recipes = villager.getRecipes(player);
					if(pstack != null && recipes != null)
						for(MerchantRecipe recipe : recipes)
							if(recipe != null && !recipe.isRecipeDisabled() && (equalStacks(pstack, recipe.getItemToBuy()) || equalStacks(pstack, recipe.getItemToSell()))) {
								entIdBuilder.add(villager.getEntityId());
								break;
							}

				}
			}

			if(pstack != null) {
				range = 12;
				BlockPos pos = new BlockPos(player);
				for(BlockPos pos_ : BlockPos.getAllInBoxMutable(pos.add(-range, -range, -range), pos.add(range + 1, range + 1, range + 1))) {
					TileEntity tile = player.worldObj.getTileEntity(pos_);
					if(tile != null) {
						boolean foundCap = false;
						for(EnumFacing e : EnumFacing.VALUES) {
							if(tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, e)
									&& scanInventory(tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, e), pstack)) {
								blockPosBuilder.add(pos_.toLong());
								foundCap = true;
								break;
							}
						}
						if(!foundCap && tile instanceof IInventory) {
							IInventory inv = (IInventory) tile;
							if(scanInventory(new InvWrapper(inv), pstack))
								blockPosBuilder.add(pos_.toLong());
						}
					}
				}
			}
		}
	}

	private boolean equalStacks(ItemStack stack1, ItemStack stack2) {
		return stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
	}

	private boolean scanInventory(IItemHandler inv, ItemStack pstack) {
		if(pstack == null)
			return false;

		for(int l = 0; l < inv.getSlots(); l++) {
			ItemStack istack = inv.getStackInSlot(l);
			if(istack != null && equalStacks(istack, pstack))
				return true;
		}

		return false;
	}

	private long[] getBlockPositions(ItemStack stack) {
		NBTTagList list = ItemNBTHelper.getList(stack, TAG_BLOCK_POSITIONS, Constants.NBT.TAG_LONG, false);
		long[] ret = new long[list.tagCount()];
		for (int i = 0; i < list.tagCount(); i++) {
			ret[i] = ((NBTTagLong) list.get(i)).getLong();
		}
		return ret;
	}

	private void setBlockPositions(ItemStack stack, long[] vals) {
		NBTTagList list = new NBTTagList();
		for(long l : vals)
			list.appendTag(new NBTTagLong(l));
		ItemNBTHelper.setList(stack, TAG_BLOCK_POSITIONS, list);
	}

	@Override
	public BaubleType getBaubleType(ItemStack arg0) {
		return BaubleType.AMULET;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onPlayerBaubleRender(ItemStack stack, EntityPlayer player, RenderType type, float partialTicks) {
		TextureAtlasSprite gemIcon = MiscellaneousIcons.INSTANCE.itemFinderGem;
		if(type == RenderType.HEAD) {
			float f = gemIcon.getMinU();
			float f1 = gemIcon.getMaxU();
			float f2 = gemIcon.getMinV();
			float f3 = gemIcon.getMaxV();
			boolean armor = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null;
			Helper.translateToHeadLevel(player);
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.rotate(90F, 0F, 1F, 0F);
			GlStateManager.rotate(180F, 1F, 0F, 0F);
			GlStateManager.translate(-0.4F, -1.4F, armor ? -0.3F : -0.25F);
			GlStateManager.scale(0.75F, 0.75F, 0.75F);
			IconHelper.renderIconIn3D(Tessellator.getInstance(), f1, f2, f, f3, gemIcon.getIconWidth(), gemIcon.getIconHeight(), 1F / 16F);
		}
	}

}
