package Evil_Code_InvisiBlock;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class InvisiBlock extends JavaPlugin implements Listener {
	private Set<Location> invisibleBlocks = new HashSet<Location>();

	@Override
	public void onEnable(){	
		checkForAndInstallUpdate();
		loadInvisibleBlocks();
	}
	
	@Override
	public void onDisable(){
		saveInvisibleBlocks();
	}
	
	private void loadInvisibleBlocks(){
		File file = new File("./plugins/EvFolder/invisible blocks.txt");
		BufferedReader reader = null;
		try{reader = new BufferedReader(new FileReader(file));}
		catch(FileNotFoundException e){reader = null;}
		
		if(reader != null){
			String line = null;
			int x,y,z;
			
			try{while((line = reader.readLine()) != null){
				//-----------------------------------------------------
				String[] data = line.split(",");
				
				World world = getServer().getWorld(data[0]);
				if(world != null) try{
					x = Integer.parseInt(data[1]); y = Integer.parseInt(data[2]); z = Integer.parseInt(data[3]);
					invisibleBlocks.add(new Location(world, x, y, z));
				}
				catch(NumberFormatException ex){}
				catch(ArrayIndexOutOfBoundsException ex){}
				//-----------------------------------------------------
			}}catch(IOException e){}
		}
	}
	
	private void saveInvisibleBlocks(){
		//-------------------
		StringBuilder builder = new StringBuilder();
		for(Location loc : invisibleBlocks){
			builder.append(loc.getWorld().getName()); builder.append(",");
			builder.append(loc.getBlockX()); builder.append(",");
			builder.append(loc.getBlockY()); builder.append(",");
			builder.append(loc.getBlockZ()); builder.append("\n");
		}
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter("./plugins/EvFolder/invisible blocks.txt"));
			writer.write(builder.toString()); writer.flush();writer.close();
		}
		catch(IOException e){getLogger().info(e.getMessage());}
		//-------------------
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]){
		if(cmd.getName().equalsIgnoreCase("magicbone")){
			if(!(sender instanceof Player)) sender.sendMessage("§4This command can only be used by ingame players.");
			else{
				ItemStack magicbone = new ItemStack(Material.BONE);
				ItemMeta meta = magicbone.getItemMeta();
				meta.setDisplayName("§8-§bM§7a§bg§7i§bc§7B§bo§7n§be§8-");
				meta.setLore(Arrays.asList("§aA magical item§m §a-use this", "§ato make blocks dissapear!"));
				magicbone.setItemMeta(meta);
				((Player)sender).getInventory().addItem(magicbone);
			}
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onBlockPunch(BlockDamageEvent evt){
		if(isVanishedBlock(evt.getBlock().getLocation())){
			vanishBlockForPlayer(evt.getPlayer().getUniqueId(), evt.getBlock().getLocation());
		}
	}
	
	@EventHandler
	public void onBlockClick(PlayerInteractEvent evt){
		if(evt.getClickedBlock() != null){
			if(isVanishedBlock(evt.getClickedBlock().getLocation())){
				vanishBlockForPlayer(evt.getPlayer().getUniqueId(), evt.getClickedBlock().getLocation());
			}
			else if(evt.getItem() != null && evt.getPlayer().isSneaking() && evt.getItem().getType() == Material.BONE &&
					evt.getItem().hasItemMeta() && evt.getItem().getItemMeta().hasLore() &&
					evt.getPlayer().hasPermission("evp.invisiblock.vanishblock"))
			{
				vanishBlock(evt.getClickedBlock().getLocation());
			}
		}
	}
	
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent evt){
		vanishAllBlocksForPlayer(evt.getPlayer().getUniqueId());
	}
	
	//
	// Non @EventHandler Methods
	@SuppressWarnings("deprecation")
	private void vanishAllBlocksForPlayer(final UUID uuid){
		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){@Override public void run(){
			Player p = getServer().getPlayer(uuid);
			if(p != null)
			for(Location bLoc : invisibleBlocks){
				p.sendBlockChange(bLoc, Material.AIR, (byte) 0);
			}
		}}, 1);
	}
	
	private void vanishBlockForPlayer(final UUID uuid, final Location loc){
		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){@SuppressWarnings("deprecation")
		@Override public void run(){
			Player p = getServer().getPlayer(uuid);
			if(p != null) p.sendBlockChange(loc, Material.AIR, (byte) 0);
		}}, 1);
	}
	
	@SuppressWarnings("deprecation")
	public void vanishBlock(final Location loc){
		if(invisibleBlocks.contains(loc) == false) invisibleBlocks.add(loc);
		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){@Override public void run(){
			for(Player p : getServer().getOnlinePlayers()) p.sendBlockChange(loc, Material.AIR, (byte) 0);
		}}, 1);
	}
	
	public boolean isVanishedBlock(Location loc){
		return invisibleBlocks.contains(loc);
	}
	
	private void checkForAndInstallUpdate(){
		final String url = "https://dl.dropboxusercontent.com/u/105298795/Plugins/InvisiBlock.jar";
		new Thread(){public void run(){
			BufferedInputStream in = null;
			FileOutputStream out = null;
			
			try{
				in = new BufferedInputStream(new URL(url).openStream());
				out = new FileOutputStream("./plugins/InvisiBlock.jar");
				
				byte data[] = new byte[1024];
				int count;
				while((count = in.read(data, 0, 1024)) != -1)out.write(data, 0, count);
				out.flush();
			}
			catch(MalformedURLException e){}
			catch(IOException e){}
			finally{
				try{
					if(in != null)in.close();
					if(out != null)out.close();
				}catch(IOException e){}
			}
		}}.start();
		
		getServer().getPluginManager().registerEvents(this, this);
	}
}