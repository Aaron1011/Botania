/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 25, 2014, 9:38:23 PM (GMT)]
 */
package vazkii.botania.common.block.mana;

import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import vazkii.botania.api.lexicon.ILexiconable;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.mana.ILens;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.state.enums.SpreaderVariant;
import vazkii.botania.api.wand.IWandHUD;
import vazkii.botania.api.wand.IWandable;
import vazkii.botania.api.wand.IWireframeAABBProvider;
import vazkii.botania.common.block.BlockMod;
import vazkii.botania.common.block.tile.mana.TileSpreader;
import vazkii.botania.common.core.helper.InventoryHelper;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.block.ItemBlockWithMetaNameAndColor;
import vazkii.botania.common.lexicon.LexiconData;
import vazkii.botania.common.lib.LibBlockNames;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class BlockSpreader extends BlockMod implements IWandable, IWandHUD, ILexiconable, IWireframeAABBProvider {

	public BlockSpreader() {
		super(Material.WOOD, LibBlockNames.SPREADER);
		setHardness(2.0F);
		setSoundType(SoundType.WOOD);
		setDefaultState(blockState.getBaseState().withProperty(BotaniaStateProps.SPREADER_VARIANT, SpreaderVariant.MANA));
	}

	@Nonnull
	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BotaniaStateProps.SPREADER_VARIANT);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BotaniaStateProps.SPREADER_VARIANT).ordinal();
	}

	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta > SpreaderVariant.values().length) {
			meta = 0;
		}
		return getDefaultState().withProperty(BotaniaStateProps.SPREADER_VARIANT, SpreaderVariant.values()[meta]);
	}

	@Override
	public void registerItemForm() {
		GameRegistry.register(new ItemBlockWithMetaNameAndColor(this), getRegistryName());
	}

	@Override
	public void getSubBlocks(@Nonnull Item item, CreativeTabs par2, List<ItemStack> par3) {
		for(int i = 0; i < 4; i++)
			par3.add(new ItemStack(item, 1, i));
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
		EnumFacing orientation = BlockPistonBase.getFacingFromEntity(pos, par5EntityLivingBase);
		TileSpreader spreader = (TileSpreader) world.getTileEntity(pos);
		world.setBlockState(pos, getStateFromMeta(par6ItemStack.getItemDamage()), 1 | 2);

		switch(orientation) {
		case DOWN:
			spreader.rotationY = -90F;
			break;
		case UP:
			spreader.rotationY = 90F;
			break;
		case NORTH:
			spreader.rotationX = 270F;
			break;
		case SOUTH:
			spreader.rotationX = 90F;
			break;
		case WEST:
			break;
		default:
			spreader.rotationX = 180F;
			break;
		}
	}

	@Override
	public int damageDropped(IBlockState par1) {
		return getMetaFromState(par1);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing par6, float par7, float par8, float par9) {
		TileEntity tile = world.getTileEntity(pos);
		if(!(tile instanceof TileSpreader))
			return false;

		TileSpreader spreader = (TileSpreader) tile;
		ItemStack lens = spreader.getItemHandler().getStackInSlot(0);
		boolean isHeldItemLens = heldItem != null && heldItem.getItem() instanceof ILens;
		boolean wool = heldItem != null && heldItem.getItem() == Item.getItemFromBlock(Blocks.WOOL);

		if(heldItem != null)
			if(heldItem.getItem() == ModItems.twigWand)
				return false;

		if(lens == null && isHeldItemLens) {
			if (!player.capabilities.isCreativeMode)
				player.inventory.setInventorySlotContents(player.inventory.currentItem, null);

			spreader.getItemHandler().setStackInSlot(0, heldItem.copy());
			spreader.markDirty();
		} else if(lens != null && !wool) {
			ItemStack add = lens.copy();
			if(!player.inventory.addItemStackToInventory(add))
				player.dropItem(add, false);
			spreader.getItemHandler().setStackInSlot(0, null);
			spreader.markDirty();
		}

		if(wool && spreader.paddingColor == -1) {
			spreader.paddingColor = heldItem.getItemDamage();
			heldItem.stackSize--;
			if(heldItem.stackSize == 0)
				player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
		} else if(heldItem == null && spreader.paddingColor != -1 && lens == null) {
			ItemStack pad = new ItemStack(Blocks.WOOL, 1, spreader.paddingColor);
			if(!player.inventory.addItemStackToInventory(pad))
				player.dropItem(pad, false);
			spreader.paddingColor = -1;
			spreader.markDirty();
		}

		return true;
	}

	@Override
	public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		TileEntity tile = world.getTileEntity(pos);
		if(!(tile instanceof TileSpreader))
			return;

		TileSpreader inv = (TileSpreader) tile;

		if(inv.paddingColor != -1) {
			net.minecraft.inventory.InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Blocks.WOOL, 1, inv.paddingColor));
		}

		InventoryHelper.dropInventory(inv, world, state, pos);

		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean onUsedByWand(EntityPlayer player, ItemStack stack, World world, BlockPos pos, EnumFacing side) {
		((TileSpreader) world.getTileEntity(pos)).onWanded(player, stack);
		return true;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileSpreader();
	}

	@Override
	public void renderHUD(Minecraft mc, ScaledResolution res, World world, BlockPos pos) {
		((TileSpreader) world.getTileEntity(pos)).renderHUD(mc, res);
	}

	@Override
	public LexiconEntry getEntry(World world, BlockPos pos, EntityPlayer player, ItemStack lexicon) {
		SpreaderVariant variant = world.getBlockState(pos).getValue(BotaniaStateProps.SPREADER_VARIANT);
		return variant == SpreaderVariant.MANA ? LexiconData.spreader : variant == SpreaderVariant.REDSTONE ? LexiconData.redstoneSpreader : LexiconData.dreamwoodSpreader;
	}

	@Override
	public AxisAlignedBB getWireframeAABB(World world, BlockPos pos) {
		float f = 1F / 16F;
		return new AxisAlignedBB(pos.getX() + f, pos.getY() + f, pos.getZ() + f,
				pos.getX() + 1 - f, pos.getY() + 1 - f, pos.getZ() + 1 - f);
	}

}
