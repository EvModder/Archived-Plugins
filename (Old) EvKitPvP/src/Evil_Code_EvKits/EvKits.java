package Evil_Code_EvKits;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("deprecation")
public final class EvKits extends JavaPlugin implements Listener{	
	String pvpTagged = ",";
	private ArrayList<Arena> arenas;
	private final String prefix = "§6§l[§2Lousy§aPvP 3Arena§6§l]§6 ";
	private Location pos1 = null, pos2 = null;
	private int endPlayerNum = 1;
	final int COUNT_DOWN = 3;
 	final String[] kits = new String[]{
		//Format: "<Name>=<id,data,enchantment,amount>,<next item>,<next item>,<etc>",
		"Archer=261,302,303,304,305,262x64",
		"Tank=258,310,311,312,313",
		"Warrior=276e16-1,307,308,364x3",
		"Alchemist=268,310,307,316,301,373:16396x4,373:16388,373:16394,373:16393",
		"Enderman=276e16-2,368x16,373:8290,300,301",
		"Viking=279,298,299,300,301,322",
		"Survivor=272:25,261:154,259:33,307:105,308:94,309:59,349:1x2,363x1,367x4,306:36,262x8",
		"MasterBowman=261e49-1,299,300,262x64,262x64",
		"Army Chef=310e20-1,298,303,304,322x2,335,400,297x3",
		"Fist-Fighter=310e0-3,311e0-4,312e0-4,313e0-3",
		"Pyromaniac=286e20-1,261e50-1,259,262x10,298,315,300,317,373:16451,383:61",
		"Witchdoctor=267,261,420,262x32,304,299,397:1e0-1,373:16428,349:1x2",
		"Ghost=352e19-1e16-1,30e16-2,397e0-5,291e20-1e16-1,373:8238x10",
		"Swag=283e19-3,314,315,316,317e0-1",
	};	
	
	@Override public void onEnable(){
		loadArenas();
		getServer().getPluginManager().registerEvents(this, this);
	}
	@Override public void onDisable(){/* */}
	
	private void loadArenas(){
		arenas = new ArrayList<Arena>();
		BufferedReader reader = null;
		//Load the conf -------------------------------------------------------------------------------------------------------------
		try{reader = new BufferedReader(new FileReader("./plugins/EvFolder/pvp-arenas.txt"));}
		catch(FileNotFoundException e){
			
			//Create Directory
			File dir = new File("./plugins/EvFolder");
			if(!dir.exists()){dir.mkdir(); getLogger().info("Directory Created!");}
			
			//Create the file
			File conf = new File("./plugins/EvFolder/pvp-arenas.txt");
			try{conf.createNewFile();}
			catch(IOException e1){getLogger().info(e1.getStackTrace().toString());}
			
			//Attempt again to load the file
			try{reader = new BufferedReader(new FileReader("./plugins/EvFolder/pvp-arenas.txt"));}
			catch(FileNotFoundException e2){getLogger().info(e2.getStackTrace().toString());}
		}
		if(reader != null){
			
			String info = "";
			String line = null;
			try{while((line = reader.readLine()) != null){info+=line+"\n";}
				reader.close();
			}catch(IOException e){getLogger().info(e.getMessage());}
			
			String[] arenaList = info.replace(" ", "").split("<x>");
			for(String arena : arenaList){
				if(arena.replace(" ", "").length() < 15)continue;
				
				String[] arenaData = arena.replace(";", "\n").split("\n");
				
				String name = "Default";
				String world = "world";
				int maxX=0, minX=0, maxY=0, minY=0, maxZ=0, minZ=0;
				ArrayList<BlockData> blockData = new ArrayList<BlockData>();
				
				for(String data : arenaData){
					data = data.replace(" ", "");
					
					if(data.startsWith("Name:"))name = data.split(":")[1];
					else if(data.startsWith("World:"))world = data.split(":")[1];
					else if(data.startsWith("Bounds:")){
						String[] coords = data.split(":")[1].split(",");
						maxX = Integer.parseInt(coords[0].replace("x=", ""));
						minX = Integer.parseInt(coords[1].replace("x=", ""));
						maxY = Integer.parseInt(coords[2].replace("y=", ""));
						minY = Integer.parseInt(coords[3].replace("y=", ""));
						maxZ = Integer.parseInt(coords[4].replace("z=", ""));
						minZ = Integer.parseInt(coords[5].replace("z=", ""));
					}
					else if(data.startsWith("BlockData:")){
						String[] blocks = data.replace(data.split(":")[0], "").split(",");
						String[] blockinfo;
						for(String blockdatas : blocks) {
							
							if(blockdatas.contains("*") == false)continue;
							blockinfo = blockdatas.split("*");
							if(blockinfo.length == 4){
								int x,y,z;
								try{
									x = Integer.parseInt(blockinfo[1]);
									y = Integer.parseInt(blockinfo[2]);
									z = Integer.parseInt(blockinfo[3]);
									
									blockData.add(new BlockData(blockinfo[0], x, y, z));
								}catch(NumberFormatException e){}
							}
						}//for loop to read the the blocks from this blockdata
					}//if the data value is 'BlockData:'
				}//for loop to read in this arena's data
				arenas.add(new Arena(name, new Section(world, maxX, minX, maxY, minY, maxZ, minZ),
																		blockData.toArray(new BlockData[]{})));
			}//for loop to read in all the arenas
		}//if the read != null
		//--------------------------------------------------------------------------------------------------------------------------
	}//end method
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]){
		if((sender instanceof Player) == false){
			sender.sendMessage("§cThis command can only be run by in-game players!");
			return false;
		}//setarena
		Player p = (Player) sender;
		
		if(cmd.getName().equalsIgnoreCase("evkit")){
			if(args.length < 1){p.sendMessage("Too few arguments!"); return false;}
			if(p.hasPermission("evp.evkit.kitcommand.anywhere") || isInArena(p.getLocation())){
				
				args[0] = args[0]/*.replaceAll("[^\\p{Alpha}]", "")*/.toLowerCase(); 
				giveKit(p, args[0]);
			}
			else p.sendMessage("This kit can only be used inside the Arena.");
			//
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("setarena")){
			if(args.length < 1){p.sendMessage("Too few arguments!"); return false;}
			
			if(pos1 != null && pos2 != null){
				if(pos1.getWorld().getName().equals(pos2.getWorld().getName()) == false){
					p.sendMessage("§cThe two positions need to be in the same world!");
					return true;
				}
				String name = "";
				for(String arg : args) name += arg + " ";
				name = (name+"***").replace(" ***", "");
				//
				createArena(name);
				p.sendMessage("§aArena Saved!");
			}
			else p.sendMessage("§cYou need to set a //pos1 and //pos2 first.");
			//
			return true;
		}
		else return false;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteractBlock(PlayerInteractEvent evt){
		//this little block of code is to allow clicking in arenas, regardless of claimed land.
		if(evt.getAction() == Action.RIGHT_CLICK_BLOCK){				
			if(evt.getClickedBlock() != null && isInArena(evt.getClickedBlock().getLocation())){
				evt.setCancelled(false);
			}
		}//------------------------------------------------------------------------------------
		Player p = evt.getPlayer();
		//
		if(evt.getItem() != null && evt.getItem().getType() == Material.MUSHROOM_SOUP && 
					p.getItemInHand().getItemMeta().getLore().get(0).toLowerCase().contains("instant health")){
			
			getLogger().info("Healing " + p.getName());
			
			if(p.getHealth()+6 <= p.getMaxHealth()){
				p.setHealth(p.getHealth()+6);
				return;
			}
			else p.setHealth(p.getMaxHealth());
			
			if(p.getFoodLevel()+6 <= 20)p.setFoodLevel(p.getFoodLevel()+6);
			else p.setFoodLevel(20);
			
			if(evt.getItem().getAmount()-1 == 0)p.getItemInHand().setType(Material.AIR);
			else p.getItemInHand().setAmount(evt.getItem().getAmount()-1);
		}
		
		if(evt.getClickedBlock() != null)
		if(evt.getClickedBlock().getType() == Material.WALL_SIGN || evt.getClickedBlock().getType() == Material.SIGN_POST){
			//All EvManager Signs (PvP Kits, Power Soup)============================================================================
			Sign sign = (Sign) evt.getClickedBlock().getState();
			String line1 = sign.getLine(0).toLowerCase();
			
			if(line1.contains("power soup") && evt.getItem().getType() == Material.MUSHROOM_SOUP
					&& p.getInventory().contains(Material.GOLD_INGOT)){
				
				getLogger().info(p.getName()+" bought 1 Powered Soup");
				
				if(p.getInventory().contains(Material.GOLD_INGOT)){
					int index = p.getInventory().first(Material.GOLD_INGOT);
					p.getInventory().getItem(index).setAmount(p.getInventory().getItem(index).getAmount()-1);
					
					ItemStack soupbowl = p.getItemInHand();
					ItemMeta meta = soupbowl.getItemMeta();
					meta.setDisplayName("§fHealing Soup");
					ArrayList<String> lore = new ArrayList<String>();lore.add("§a§oInstant Health");
					meta.setLore(lore);
					soupbowl.setItemMeta(meta);
					p.setItemInHand(soupbowl);
					p.updateInventory();
				}
			}
			else if(line1.contains("[")&&line1.contains("k")&&line1.contains("i")&&line1.contains("t")&&line1.contains("]")){
				removeKitItems(p);
				if(evt.getItem() != null) if(evt.getItem().getType() != Material.AIR){
					p.sendMessage("§cYou must be empty-handed to use this sign");
					return;
				}
				
				//- Items Check ----------------------------------------------------------------------------------------------------
				ItemStack[] stuff = p.getInventory().getContents();
				int items = 0;
				for(ItemStack item : stuff)if(item != null) if(item.getType() != Material.AIR && 
					item.getType() != Material.ARROW && item.getType() != Material.GLASS_BOTTLE)items++;
				//+++++++++
				stuff = p.getInventory().getArmorContents();
				for(ItemStack item : stuff)if(item != null && item.getType() != Material.AIR)items++;
				//+++++++++
				
				if(items > 0){
					p.sendMessage("§cYou must have a completely empty inventory to use this sign!");
					return;
				}//-----------------------------------------------------------------------------------------------------------------
			
				for(int i = 0; i < kits.length; i++){
					if(sign.getLine(1).toLowerCase().contains(kits[i].split("=")[0].toLowerCase())){
						if(giveKit(p, kits[i]))
							p.sendMessage("§aGranted Kit: [§6"+kits[i].split("=")[0]+"§a]");
					}
				}//for(kits)
			}//if the sign contains the characters '[' 'k' 'i' 't' ']'
		}//if the block clicked is a WALL_SIGN or a SIGN_POST
	}
	
	@EventHandler
	public void onPlayerInteractItemFrame(PlayerInteractEntityEvent evt){
		if(evt.getRightClicked().getType() == EntityType.ITEM_FRAME){
			ItemMeta meta = ((ItemFrame)evt.getRightClicked()).getItem().getItemMeta();
			if(meta.hasDisplayName() || meta.hasLore()){
				
				Location loc = evt.getRightClicked().getLocation();
				for(Arena arena : arenas){
					if(arena.bounds.contains(loc)){
						evt.setCancelled(true);
						
						getServer().broadcastMessage(
								prefix + "§6" + evt.getPlayer().getDisplayName() + " has started a PvP match in the " +
										"Arena: §c" + arena.name);
						resetArena(arena);
						startArena(arena);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerDropPvpItem(PlayerDropItemEvent evt){
		if(evt.getItemDrop().getItemStack().getItemMeta().hasDisplayName()){
			if(evt.getItemDrop().getItemStack().getItemMeta().getDisplayName().contains("¤"))evt.setCancelled(true);
		}
	}
	
	@EventHandler
	public void kitSignPlaced(SignChangeEvent evt){
		String line = evt.getLine(0);
		if(line.contains("[") && line.contains("k") && line.contains("i") && line.contains("t") && line.contains("]")
			&& evt.getPlayer().isOp() == false)evt.setCancelled(true);
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent evt){
		if(evt.getInventory().getType().equals(InventoryType.PLAYER) == false)removeKitItems(evt.getPlayer());
	}	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent evt){
		if(evt.isCancelled() == false && isInArena(evt.getTo()) == false || isInArena(evt.getFrom()) == false){
			
			removeKitItems(evt.getPlayer());
			
			//if teleporting FROM an arena
			if(isInArena(evt.getFrom())){
				evt.getPlayer().getInventory().remove(Material.ARROW);
				evt.getPlayer().getInventory().remove(Material.GLASS_BOTTLE);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent evt){
		removeKitItems(evt.getEntity());
		evt.getDrops().clear();
		for(ItemStack i : evt.getEntity().getInventory().getContents())evt.getDrops().add(i);
		for(ItemStack i : evt.getEntity().getInventory().getArmorContents())evt.getDrops().add(i);
		
		String[] taggedppl = pvpTagged.split(",");
		
		for(String str : taggedppl){
			//if the dead person was tagged, remove them (now that they have died)
			if(str.split(":")[0].equalsIgnoreCase(evt.getEntity().getName())){
				pvpTagged = pvpTagged.replace(","+str+",", ",");
			}
		}	
		//fix death messages
		if(evt.getDeathMessage().contains("[")){//if the message is equal to "string [ string ] string"
			String fixedMessage = evt.getDeathMessage().replaceFirst("\\[", "§f");
			if(evt.getDeathMessage().split("\\[")[1].contains("]")){
				
				
				String[] parts = fixedMessage.split("\\]"); fixedMessage = "";
				for(int i = 0; i < parts.length; i++){
					if(i < parts.length-2)fixedMessage += parts[i] + "]";
					else fixedMessage += parts[i];
				}
				evt.setDeathMessage("§f"+fixedMessage);
			}
		}
		for(Arena arena : arenas){
			if(arena.isRunning() && arena.bounds.contains(evt.getEntity().getLocation())){
				int players = 0;
				for(Player p : getServer().getOnlinePlayers()){
					if(p.getName().equals(evt.getEntity().getName()) == false) players++;
				}
				if(players <= endPlayerNum){
					resetArena(arena);
					break;
				}
			}
		}
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent evt){
		String[] taggedppl = pvpTagged.split(",");
		
		for(String str : taggedppl){
			if(str.split(":")[0].equalsIgnoreCase(evt.getPlayer().getName())){//if the disconnecting person is tagged
				
				String attacker = str.split(":")[1];
				Entity[] ents = evt.getPlayer().getNearbyEntities(20, 50, 20).toArray(new Entity[]{});//nearby entities on logout
				
				for(Entity ent : ents){
					if(ent instanceof Player)if(((Player)ent).getName().equalsIgnoreCase(attacker)){//if the attacker is still nearby
						
						evt.getPlayer().setHealth(0); // code to kill PvP-Logger
						getServer().broadcastMessage("§4[PVP] §c"+evt.getPlayer().getName()+" logged out while Combat-Tagged.");
						return;
					}
				}
				pvpTagged = pvpTagged.replace(","+str+",", ",");
			}//if the player is tagged
		}//for loop
	}//method for PlayerQuit
	
	@EventHandler //Cancel damage in an arena before it starts
	public void onDamageEvent(EntityDamageEvent evt){
		if(evt.isCancelled() == false && evt.getEntity() instanceof Player){
			for(Arena arena : arenas){
				if(arena.bounds.contains(evt.getEntity().getLocation())){
					if(arena.isRunning() == false) evt.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onDamageLogEvent(EntityDamageEvent evt){
		if(evt.getEntity() instanceof Player && evt.isCancelled() == false){
			Player damagedPlayer = (Player)evt.getEntity();
			if(evt.getCause().equals(DamageCause.ENTITY_ATTACK)){
				EntityDamageByEntityEvent dE = (EntityDamageByEntityEvent)evt;
				
				if(dE.getDamager() instanceof Player){//if the attacker is a player..
					
					//they have to have below 7 hearts to be tagged
					if(damagedPlayer.getMaxHealth() - damagedPlayer.getHealth() < 5)return;
					
					if(pvpTagged.contains(","+damagedPlayer.getName()+":") == false)damagedPlayer.sendMessage(
						"§4[PVP] §6You have been Combat-Tagged by §e"+ 
						 ((Player)dE.getDamager()).getName() +"§6, you will die if you log out.");
					
					//======================================== Tag them ========================================
					final String tag;
					try{tag = damagedPlayer.getName()+":"+((Player)dE.getDamager()).getName();}
					catch(NoClassDefFoundError ex){return;}
					
					pvpTagged += tag + ",";
					
					getServer().getScheduler().scheduleSyncDelayedTask(this, new Thread(){
						public void run(){
							if(pvpTagged.contains("," + tag + ",")){
								pvpTagged = pvpTagged.replaceFirst(","+tag+",", ",");
								
								if(pvpTagged.contains("," + tag + ",") == false){
									try{getServer().getPlayerExact(tag.split(":")[0]).sendMessage("§4[PVP] §6Your Combat-Tag faded.");}
									catch(NullPointerException ex){}
								}
							}
						}
					}, 600);// 30 seconds
					//==========================================================================================
				}//if the attacker is a player
			}//if the DamageType was an attack by another entity
			
			if(damagedPlayer.getHealth() <= 0 && damagedPlayer.hasPermission("evp.evkit.nodeathdrops")){
				
				BufferedReader reader = null;
				try{reader = new BufferedReader(new FileReader("./plugins/Essentials/spawn.YML"));}
				catch(FileNotFoundException e){getLogger().info(e.getStackTrace().toString());}
				
				String file = "";
				if(reader != null){
					String line = null;
					try{while((line = reader.readLine()) != null){file += line.replace(" ", "").toLowerCase()+",";}}
					catch(IOException e){return;}
				}
				String[] lines = file.split(",");
				
				//teleport player to essentials-spawn
				String worldname="";
				double x=8.234, y=8.234, z=8.234;
				for(int a = 0; a < lines.length; a++){
					if(lines[a].startsWith("world:"))worldname = lines[a].split("world:")[1];
					else if(lines[a].startsWith("x:"))x = Double.parseDouble(lines[a].split("x:")[1]);
					else if(lines[a].startsWith("y:"))y = Double.parseDouble(lines[a].split("y:")[1]);
					else if(lines[a].startsWith("z:"))z = Double.parseDouble(lines[a].split("z:")[1]);
					if(worldname.equals("") == false && x != 8.234 && y != 8.234&& z != 8.234){
						damagedPlayer.teleport(new Location(getServer().getWorld(worldname), x, y, z));
						evt.setCancelled(true);
						damagedPlayer.setHealth(damagedPlayer.getMaxHealth());
					}
				}
			}//'else if' for if the player reaches 0 health in the Events world.
		}//if the damaged entity is a player
	}
	
	//
	//Non-event-triggered methods	
	private void removeKitItems(HumanEntity p){
		ItemStack[] stuff = p.getInventory().getContents();
		for(int i = 0; i < stuff.length; i++){
			if(stuff[i] != null){
				ItemMeta meta = stuff[i].getItemMeta();
				try{
					if(meta.hasDisplayName())if(meta.getDisplayName().contains("¤")) p.getInventory().remove(stuff[i]);
					else if(meta.getLore().get(0).contains("PvP Gear")) p.getInventory().remove(stuff[i]);
				}catch(NullPointerException ex){}
			}
		}
		try{
			ItemMeta meta = p.getItemInHand().getItemMeta();
			if((meta.hasDisplayName() && meta.getDisplayName().contains("¤")) ||
						(meta.hasLore() && meta.getLore().get(0).contains("PvP Gear"))){
				p.setItemInHand(null);
			}
		}catch(NullPointerException e){}
		
		try{
			ItemMeta meta = p.getInventory().getBoots().getItemMeta();
			if((meta.hasDisplayName() && meta.getDisplayName().contains("¤")) ||
						(meta.hasLore() && meta.getLore().get(0).contains("PvP Gear"))){
				p.getInventory().setBoots(new ItemStack(Material.AIR));
				p.getInventory().setBoots(null);
				getLogger().info("boots detected!");
			}
		}catch(NullPointerException e){}
		
		try{
			ItemMeta meta = p.getInventory().getLeggings().getItemMeta();
			if(meta.hasDisplayName())if(meta.getDisplayName().contains("¤") || meta.getLore().get(0).contains("PvP Gear")){
			p.getInventory().setLeggings(new ItemStack(Material.AIR));
			p.getInventory().setLeggings(null);
		}}catch(NullPointerException e){}
		
		try{
			ItemMeta meta = p.getInventory().getChestplate().getItemMeta();
			if(meta.hasDisplayName())if(meta.getDisplayName().contains("¤") || meta.getLore().get(0).contains("PvP Gear")){
			p.getInventory().setChestplate(new ItemStack(Material.AIR));
			p.getInventory().setChestplate(null);
		}}catch(NullPointerException e){}
		
		try{
			ItemMeta meta = p.getInventory().getHelmet().getItemMeta();
			if(meta.hasDisplayName())if(meta.getDisplayName().contains("¤") || meta.getLore().get(0).contains("PvP Gear")){
			p.getInventory().setHelmet(new ItemStack(Material.AIR));
			p.getInventory().setHelmet(null);
		}}catch(NullPointerException e){}
		
		ItemMeta meta = p.getItemInHand().getItemMeta();
		try{
			if((meta.hasDisplayName() && meta.getDisplayName().contains("¤")) ||
			   (meta.hasLore() && meta.getLore().get(0).contains("PvP Gear"))){
				p.setItemInHand(new ItemStack(Material.AIR));
			}
		}catch(NullPointerException ex){}
	}
	
	private boolean giveKit(Player p, String kitname){
		removeKitItems(p);
		String kit = null;
		
		if(kitname.contains("="))kit = kitname;
		else for(String str : kits)if(kitname.equalsIgnoreCase(str.split("=")[0]))kit = str;
		if(kit == null){p.sendMessage("§cThat kit is not defined."); return false;}
			
		//----------------------- add new stuff
		String[] info = (kit.split("=")[1]).split(",");
		//
		ItemStack[] newStuff = new ItemStack[info.length];
		for(int a = 0; a < info.length; a++){
			int amount = 1, classif = 0, ench = -1, level = 1;
			
			if(info[a].contains("x")){
				amount = Integer.parseInt(info[a].split("x")[1]);
				info[a] = info[a].split("x")[0];
			}
			if(info[a].contains("e")){
				for(String enchantment : info[a].split("e")){
					if(enchantment.contains("-")){
						ench = Integer.parseInt(enchantment.split("-")[0]);
						level = Integer.parseInt(enchantment.split("-")[1]);
					}
				}
				info[a] = info[a].split("e")[0];
			}
			if(info[a].contains(":")){
				classif = Integer.parseInt(info[a].split(":")[1]);
				info[a] = info[a].split(":")[0];
			}
			
			newStuff[a] = new ItemStack(Material.getMaterial(Integer.parseInt(info[a])), amount, (short)classif);
			
			ItemMeta meta = newStuff[a].getItemMeta();
			if(ench != -1) meta.addEnchant(Enchantment.getById(ench), level, true);
			meta.setDisplayName("§7¤§2§l>§6Property of PvP Arena§2§l<§7¤");
			ArrayList<String> lore = new ArrayList<String>();lore.add("§9Temp PvP Gear");
			meta.setLore(lore);
			newStuff[a].setItemMeta(meta);
		}
		
		p.getInventory().setContents(newStuff);
		p.updateInventory();
		return true;	
	}
	
	public void startArena(final Arena arena){
		for(int i = COUNT_DOWN; i > 0; i--){
			final int seconds = i;
			
			getServer().getScheduler().scheduleSyncDelayedTask(this, new Thread(){public void run(){
				for(Player p : getServer().getOnlinePlayers()){
					//
					if(arena.bounds.contains(p.getLocation())){
						p.sendMessage(prefix + "§aStarting in §5§l§m[§e" + seconds + "§5§l§m]");
					}
				}
			}}, i * 20);//1 second * time left
		}
		arena.setRunning(true);
	}
	
	public void resetArena(Arena arena){
		arena.setRunning(false);
		org.bukkit.World world = getServer().getWorld(arena.bounds.world);
		if(world == null){
			getLogger().severe("Invalid world name!!");
			return;
		}
		
		for(BlockData bd : arena.blocks){
			world.getBlockAt(bd.x, bd.y, bd.z).setType(bd.mat);
			if(bd.data != 0) world.getBlockAt(bd.x, bd.y, bd.z).setData(bd.data);
		}
	}
	
	public boolean isInArena(Location loc){
		for(Arena arena : arenas){
			if(arena.bounds.contains(loc))return true;
		}
		return false;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPosCommand(PlayerCommandPreprocessEvent event){
		if(event.isCancelled())return;
		
		String cmd = event.getMessage().split(" ")[0];//command
		
		String[] args = new String[event.getMessage().split(" ").length-1];//args
		for(int i = 1; i < event.getMessage().split(" ").length; i++)args[i-1] = event.getMessage().split(" ")[i];
		
		if(event.getPlayer().hasPermission("evp.stayinside.claim")){//if they do not have perms and performed W.E. command...
			Player p = event.getPlayer();
			
			//----------------------------------------------------------------------------------
			if(cmd.equalsIgnoreCase("//pos1")){
				if(args.length == 1){
					String[] coords = args[0].split(",");
					try{
						pos1 = new Location(p.getWorld(), Integer.parseInt(coords[0]), Integer.parseInt(coords[1]),
								Integer.parseInt(coords[2]));
					}catch(NumberFormatException e){}
				}
				else pos1 = p.getLocation();
			}
			else if(cmd.equalsIgnoreCase("//hpos1"))pos1 = p.getTargetBlock(null, 20).getLocation();
			//----------------------------------------------------------------------------------
			else if(cmd.equalsIgnoreCase("//pos2")){
				if(args.length == 1){
					String[] coords = args[0].split(",");
					try{
						pos2 = new Location(p.getWorld(), Integer.parseInt(coords[0]), Integer.parseInt(coords[1]),
								Integer.parseInt(coords[2]));
					}catch(NumberFormatException e){}
				}
				else pos2 = p.getLocation();
			}
			else if(cmd.equalsIgnoreCase("//hpos2"))pos2 = p.getTargetBlock(null, 20).getLocation();
			//----------------------------------------------------------------------------------
			else return;
		}
	}
	
	private void createArena(String name){
		if(pos1 == null || pos2 == null)return;
		int maxX = 0, maxY = 0, maxZ = 0, minX = 0, minY = 0, minZ = 0;
		
		if(pos1.getBlockX() > pos2.getBlockX()){maxX = pos1.getBlockX(); minX = pos2.getBlockX();}
		else{maxX = pos2.getBlockX(); minX = pos1.getBlockX();}
		
		if(pos1.getBlockY() > pos2.getBlockY()){maxY = pos1.getBlockY(); minY = pos2.getBlockY();}
		else{maxY = pos2.getBlockY(); minY = pos1.getBlockY();}
		
		if(pos1.getBlockZ() > pos2.getBlockZ()){maxZ = pos1.getBlockZ(); minZ = pos2.getBlockZ();}
		else{maxZ = pos2.getBlockZ(); minZ = pos1.getBlockZ();}
		
		//---------------------------------------------------------------------------------------------------------------
		String line, file = "";
		
		int area = (maxX-minX) * (maxZ-minZ) * (maxY-minY);//length * width * height
		BlockData[] blockdata = new BlockData[area];
		int i = 0;
		for(int x = minX; x < maxX; x++)
			for(int y = minY; y < maxY; y++)
				for(int z = minZ; z < maxZ; z++){
					Block b = pos1.getWorld().getBlockAt(x, y, z);
					if(b.getData() == 0) blockdata[i] = new BlockData(b.getType(), x, y, z);
					//
					else blockdata[i] = new BlockData(b.getTypeId()+":"+b.getData(), x, y, z);
					i++;
				}
		
		BufferedReader reader;
		try{
			reader = new BufferedReader(new FileReader("./plugins/EvFolder/pvp-arenas.txt"));
			while((line = reader.readLine()) != null){file += line+"\n";}
			reader.close();

			BufferedWriter writer = new BufferedWriter(new FileWriter("./plugins/EvFolder/pvp-arenas.txt"));
			if(!file.equals(""))writer.write(file + "<x>\n");
			writer.write("Name: " + name + "\n" +
						 "World: " + pos1.getWorld().getName() + "\n" +
						 "Bounds: x=" + maxX + ",x=" + minX + ",y=" + maxY + ",y=" + minY + ",z=" + maxZ + ",z=" + minZ + "\n");
			
			writer.write("BlockData:");
			String datas = "";
			for(BlockData bd : blockdata){
				if(bd.data == 0) datas += bd.mat.getId();
				else datas += bd.mat.getId()+":"+bd.data;
				//
				datas += "*" + bd.x + "*" + bd.y + "*" + bd.z + ",";
			}
			
			datas = (datas+"$%&").replace(",$%&", "").replace("$%&", "");
			writer.write(datas);
			writer.flush();
			writer.close();
		}
		catch(IOException e){getLogger().info(e.getMessage());}
		
		arenas.add(new Arena(name, new Section(pos1.getWorld().getName(), maxX, minX, maxY, minY, maxZ, minZ), blockdata));
    }
}