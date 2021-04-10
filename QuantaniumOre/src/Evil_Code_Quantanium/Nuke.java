package Evil_Code_Quantanium;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;

public class Nuke{
	private Block center;
	private Block furnace;
	boolean isValid = false;
	private int power;
	
	@SuppressWarnings("deprecation")
	public Nuke(Block redstoneBlock){
		center = redstoneBlock;
		
		if(redstoneBlock.getType() == Material.REDSTONE_BLOCK){
			Block up1 = redstoneBlock.getRelative(BlockFace.UP);
			//
			if(up1.getType() == Material.FURNACE){
				Furnace furnace = (Furnace) up1.getState();
				up1.setType(Material.BURNING_FURNACE);
				//
				if(furnace.getInventory().getFuel().getType() == Material.SULPHUR && 
						furnace.getInventory().getSmelting().getType() == Material.COMMAND_MINECART){
					//
					Block down1 = redstoneBlock.getRelative(BlockFace.DOWN);
					if(down1.getType() == Material.QUARTZ_BLOCK && down1.getData() != 0){
						if((redstoneBlock.getRelative(BlockFace.EAST).getType() == Material.EMERALD_BLOCK &&
							redstoneBlock.getRelative(BlockFace.WEST).getType() == Material.EMERALD_BLOCK) ||
							(redstoneBlock.getRelative(BlockFace.NORTH).getType() == Material.EMERALD_BLOCK &&
							redstoneBlock.getRelative(BlockFace.SOUTH).getType() == Material.EMERALD_BLOCK)){
							//
							isValid = true;
							loadPower(redstoneBlock);
						}
					}
				}
			}
		}
	}
	
	private void loadPower(Block center){
		for(Block block : getAllTouching(center)){
			if(block.getType() == Material.EMERALD_BLOCK) power++;
		}
	}
	
	private ArrayList<Block> getAllTouching(Block start){
		ArrayList<Block> list = new ArrayList<Block>();
		
		for(Block b : getNeighbors(start)){
			ArrayList<Block> neighbors = getAllTouching(b);
			neighbors.remove(start);
			list.addAll(neighbors);
		}
		return list;
	}
	
	private ArrayList<Block> getNeighbors(Block block){
		ArrayList<Block> list = new ArrayList<Block>();
		//
		if(isRocketPart(block.getRelative(BlockFace.UP).getType())) list.add(block.getRelative(BlockFace.UP));
		if(isRocketPart(block.getRelative(BlockFace.UP).getType())) list.add(block.getRelative(BlockFace.DOWN));
		if(isRocketPart(block.getRelative(BlockFace.UP).getType())) list.add(block.getRelative(BlockFace.NORTH));
		if(isRocketPart(block.getRelative(BlockFace.UP).getType())) list.add(block.getRelative(BlockFace.SOUTH));
		if(isRocketPart(block.getRelative(BlockFace.UP).getType())) list.add(block.getRelative(BlockFace.EAST));
		if(isRocketPart(block.getRelative(BlockFace.UP).getType())) list.add(block.getRelative(BlockFace.WEST));
		//
		return list;
	}
	
	private boolean isRocketPart(Material mat){
		return (mat != Material.AIR && mat != Material.WATER && mat != Material.LAVA && mat != Material.DIRT &&
				mat != Material.GRASS && mat != Material.NETHER_FENCE && mat != Material.IRON_FENCE);
	}
	
	public Block getCenter(){return center;}
	public void setCenter(Block b){center = b;}
	public int getPower(){return power;}
	
	public boolean isActive(){
		Furnace f = (Furnace) furnace.getState();
		if((f.getBurnTime() == 0 && f.getCookTime() == 0) ||
				f.getInventory().getFuel() == null || f.getInventory().getSmelting() == null) return false;
		else return true;
	}
}
