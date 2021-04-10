package Evil_Code_Quantanium;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class Quantainium extends JavaPlugin implements Listener {
	final String nameOre = "Quantanium Ore";
	final String nameItem = "Quantanium";

	@Override
	public void onEnable(){
	    
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void onBlockMine(BlockBreakEvent evt){
		if(evt.isCancelled() == false && evt.getBlock().getType() == Material.SPONGE){
			if(evt.getPlayer().getGameMode() == GameMode.CREATIVE)return;
			evt.setCancelled(true);
			
			if(evt.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)){
				ItemStack drop = new ItemStack(Material.SPONGE);
				ItemMeta meta = drop.getItemMeta();meta.setDisplayName("§fQuantanium Ore");drop.setItemMeta(meta);
				evt.getBlock().getWorld().dropItemNaturally(evt.getBlock().getLocation(), drop);
			}
			else{
				ItemStack drop = new ItemStack(Material.COMMAND_MINECART);
				ItemMeta meta = drop.getItemMeta();meta.setDisplayName("§fQuantanium");drop.setItemMeta(meta);
				evt.getBlock().getWorld().dropItemNaturally(evt.getBlock().getLocation(), drop);
			}
			evt.getBlock().setType(Material.AIR);
		}
	}
	
	@EventHandler
	public void onMinecartPlace(PlayerInteractEvent evt){
		if(evt.getClickedBlock() != null){
			if(evt.getPlayer().getInventory().getItemInMainHand().getType() == Material.COMMAND_MINECART
					&& evt.getPlayer().isOp() == false) evt.setCancelled(true);
			
			else if(evt.getClickedBlock().getType() == Material.LEVER){
				Block ignition = evt.getClickedBlock().getRelative(evt.getBlockFace().getOppositeFace());
				Nuke nuke = new Nuke(ignition);
				if(nuke.isValid);
			}
		}
	}
	
	@EventHandler
	public void onItemSmelt(FurnaceSmeltEvent evt){
		if(evt.getSource().getType() == Material.COMMAND_MINECART)
			evt.setResult(new ItemStack(Material.GHAST_TEAR));
	}
}