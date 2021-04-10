package Evil_Code_DropIngots;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class DropIngots extends JavaPlugin implements Listener {

	@Override
	public void onEnable(){	
		//Automatic update (or below): https://www.dropbox.com/s/7hee49mupuhp2ij/DropIngots.jar?dl=1
		final String url = "https://dl.dropboxusercontent.com/s/7hee49mupuhp2ij/DropIngots.jar";
		new Thread(){public void run(){
			BufferedInputStream in = null;
			FileOutputStream out = null;
			
			try{
				in = new BufferedInputStream(new URL(url).openStream());
				out = new FileOutputStream("./plugins/DropIngots.jar");
				
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
		getServer().getPluginManager().registerEvents(new Extraneous(this), this);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockMine(BlockBreakEvent evt){
		if(evt.isCancelled() || evt.getPlayer().getGameMode() == GameMode.CREATIVE || evt.getPlayer().getInventory().getItemInMainHand() == null
					|| evt.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
		
		Material toolUsed = evt.getPlayer().getInventory().getItemInMainHand().getType();
		if(toolUsed == Material.DIAMOND_PICKAXE || toolUsed == Material.IRON_PICKAXE 
				|| toolUsed == Material.GOLD_PICKAXE || toolUsed == Material.STONE_PICKAXE){
			
			if(evt.getBlock().getType() == Material.IRON_ORE){
				evt.setCancelled(true);
				evt.getBlock().setType(Material.AIR);
				
				ItemStack drops = new ItemStack(Material.IRON_INGOT);
				drops.setAmount(oreDropWithFortune(
						evt.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)));
				
				evt.getBlock().getWorld().dropItemNaturally(evt.getBlock().getLocation(), drops);
				evt.getPlayer().giveExp(new Random().nextInt(4)+1);
			}
			else if(toolUsed != Material.STONE_PICKAXE && evt.getBlock().getType() == Material.GOLD_ORE){
				evt.setCancelled(true);
				evt.getBlock().setType(Material.AIR);
				
				ItemStack drops = new ItemStack(Material.GOLD_INGOT);
				drops.setAmount(oreDropWithFortune(
						evt.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)));
				
				evt.getBlock().getWorld().dropItemNaturally(evt.getBlock().getLocation(), drops);
				evt.getPlayer().giveExp(new Random().nextInt(5)+2);
			}
		}
	}
	
	public int oreDropWithFortune(int level){
		if(level <= 0)return 1;
		int drops = 2;
		
		int bonusChance = 35 - level*5;
		if(level > 100)drops += Math.round(level-100 / 2.5);
		if(level > 50)bonusChance = 1;
		else if(level > 25)bonusChance = 2;
		else if(level > 15)bonusChance = 3;
		else if(level > 8)bonusChance = 4;
		
		if(bonusChance < 5)level = 100;
		
		int chanceIndex = bonusChance;
		int randomChance = new Random().nextInt(100)+1;
		//
		while(chanceIndex <= bonusChance * (level+1)){
			if(randomChance <= chanceIndex) return drops;
			// else
			chanceIndex += bonusChance;
			drops++;
		}
		
		return 1;
	}
}