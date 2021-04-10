package Evil_Code_EvKits;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class Section {
	final int maxX, minX;
	final int maxY, minY;
	final int maxZ, minZ;
	final String world;
	
	public Section(String world, int maxX, int minX, int maxY, int minY, int maxZ, int minZ){
		this.world = world;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
	}
		
	public boolean contains(Location l){
		if(((maxX >= l.getBlockX() || maxX == 0) && (minX <= l.getBlockX() || minX == 0)) &&
		   ((maxY >= l.getBlockY() || maxY == 0) && (minY <= l.getBlockY() || minY == 0)) &&
		   ((maxZ >= l.getBlockZ() || maxZ == 0) && (minZ <= l.getBlockZ() || minZ == 0)) &&
		   	l.getWorld().getName().equals(world))return true;	
		else return false;
	}
	public boolean contains(Block b){
		if(((maxX >= b.getX() || maxX == 0) && (minX <= b.getX() || minX == 0)) &&
		   ((maxY >= b.getY() || maxY == 0) && (minY <= b.getY() || minY == 0)) &&
		   ((maxZ >= b.getZ() || maxZ == 0) && (minZ <= b.getZ() || minZ == 0)) &&
		   	(world == null || b.getWorld().getName().equals(world)))return true;	
		else return false;
	}
}
