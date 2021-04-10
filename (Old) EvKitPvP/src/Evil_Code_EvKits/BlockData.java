package Evil_Code_EvKits;

import org.bukkit.Material;

@SuppressWarnings("deprecation")
public class BlockData {
	final int x, y, z;
	final Material mat;
	final byte data;
	
	public BlockData(String id, int x, int y, int z){
		this.x = x; this.y = y; this.z = z;
		
		byte d = 0;
		Material m;
		try{
			if(id.contains(":")){
				m = Material.getMaterial(Integer.parseInt(id.split(":")[0]));
				d = Byte.parseByte(id.split(":")[1]);
			}
			else m = Material.getMaterial(Integer.parseInt(id));
		}catch(NumberFormatException ex){
			m = Material.getMaterial(id);
		}
		mat = m;
		data = d;
	}
	public BlockData(Material m, int x, int y, int z){
		this.x = x; this.y = y; this.z = z;
		mat = m;
		data = 0;
	}
}
