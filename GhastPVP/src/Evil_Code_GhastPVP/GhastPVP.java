package Evil_Code_GhastPVP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class GhastPVP extends JavaPlugin implements Listener{
	private String fireballed = ",";
	private ArrayList<GhastHolder> ghasts;
	private boolean gameInProgress;

	@Override public void onEnable(){
		registerGhasts();
		getServer().getPluginManager().registerEvents(this, this);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Thread(){
			public void run(){/*-*/checkGhasts();/*-*/}
		}, 1200, 40);//waits for 1 minute before starting, (40 ticks = 2 seconds)
	}

	private void registerGhasts(){
		ghasts = new ArrayList<GhastHolder>();
		BufferedReader reader = null;
		//Load the conf -------------------------------------------------------------------------------------------------------------
		try{reader = new BufferedReader(new FileReader("./plugins/EvFolder/ghastpvp.txt"));}
		catch(FileNotFoundException e){

			//Create Directory
			File dir = new File("./plugins/EvFolder");
			if(!dir.exists()){dir.mkdir(); getLogger().info("Directory Created!");}

			//Create the file
			File conf = new File("./plugins/EvFolder/ghastpvp.txt");
			try{conf.createNewFile();}
			catch(IOException e1){getLogger().info(e1.getStackTrace().toString());}

			//Attempt again to load the file
			try{reader = new BufferedReader(new FileReader("./plugins/EvFolder/ghastpvp.txt"));}
			catch(FileNotFoundException e2){getLogger().info(e2.getStackTrace().toString());}
		}
		if(reader != null){

			String info = "";
			String line = null;
			try{while((line = reader.readLine()) != null){info+=line+"\n";}
			reader.close();
			}catch(IOException e){getLogger().info(e.getMessage());}

			String[] settings = info.split("\n");
			for(int i = 0; i < settings.length; i++){
				if(settings[i].toLowerCase().startsWith("ghast") && settings[i].split(":").length > 1){
					String ghast = settings[i].toLowerCase().replace(" ", "").replace("ghastlocation", "").split(":")[1];
					getLogger().info(ghast);
					try{
						Location loc = new Location(getServer().getWorld(ghast.split(",")[0]),
								Integer.parseInt(ghast.split(",")[1]),
								Integer.parseInt(ghast.split(",")[2]),
								Integer.parseInt(ghast.split(",")[3]));

						Ghast g = (Ghast) loc.getWorld().spawnEntity(loc, EntityType.GHAST);
						g.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100); g.setHealth(100);
						g.setNoDamageTicks(180*20);//180 seconds (3 minutes)
						g.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100000, 3));
						g.setCustomName("�6Pitcher");
						ghasts.add(new GhastHolder(g, loc));
						getLogger().info("Spawned a Ghast at x="+loc.getBlockX()+" y="+loc.getBlockY()+" z="+loc.getBlockZ());

					}catch(NumberFormatException ex){
						getLogger().info("Line "+(i+1)+" of the config is invalid! Skipping to next line..");
					}
				}
			}

		}//--------------------------------------------------------------------------------------------------------------------------
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]){

		if(cmd.getName().equalsIgnoreCase("ghastpvp-restart")){
			for(GhastHolder gh : ghasts)gh.ghast.remove();
			registerGhasts();
			return true;
		}
		return false;
	}

	@EventHandler
	public void projectileStrike(ProjectileHitEvent event){
		if(event.getEntity().getType() == EntityType.FIREBALL){
			final Fireball shot = (Fireball) event.getEntity();

			//Find who it struck---------------------------
			Player target = null;
			Location loc = event.getEntity().getLocation();

			for(Player p : getServer().getOnlinePlayers()){
				if(isCloseTo(p.getLocation(), loc))target = p;
			}//--------------------------------------------

			if(target != null){
				final Player p = target;
				final double healthBefore = p.getHealth();

				if(shot.getShooter() != null)
					getServer().getScheduler().scheduleSyncDelayedTask(this, new Thread(){public void run(){
						if(shot.getShooter() instanceof Player && p.hasPermission("GhastPVP.instantkill") &&
								p.getHealth() < healthBefore) {
							p.setHealth(0);

							Player shooter = (Player)shot.getShooter();
							fireballed += p.getName() + ",";

							if(shooter.getInventory().getItemInMainHand().getType() == Material.STICK){
								Random rand = new Random();
								int bonus = rand.nextInt(6);

								if(bonus == 0){
									shooter.getInventory().getItemInMainHand().addEnchantment(Enchantment.FIRE_ASPECT, 1);
								}
								else if(bonus == 1){
									shooter.getInventory().getItemInMainHand().addEnchantment(Enchantment.KNOCKBACK, rand.nextInt(2));
								}
								else if(bonus == 2){
									ItemStack item = new ItemStack(Material.BLAZE_ROD);
									ItemMeta named = item.getItemMeta();
									if(rand.nextInt(2) == 0)named.setDisplayName("�7Stick");
									item.setItemMeta(named);
									item.addEnchantment(Enchantment.KNOCKBACK, rand.nextInt(2)+1);
									shooter.getInventory().setItemInMainHand(item);
								}
								else if(bonus == 3){
									ItemStack item = shooter.getInventory().getItemInMainHand();
									ItemMeta named = item.getItemMeta();
									named.setDisplayName("�7Stick");
									item.setItemMeta(named);
									item.addEnchantment(Enchantment.DAMAGE_ALL, rand.nextInt(2)+2);
									shooter.getInventory().setItemInMainHand(item);
								}
							}
						}
						else if(shot.getShooter() instanceof Ghast){//negate ghast damage in the arena
							Ghast ghast = (Ghast)shot.getShooter();
							try{ghast.getCustomName().startsWith("a");}catch(NullPointerException ex){return;}
							if(ghast.getCustomName().toLowerCase().contains("pitcher")){
								p.setHealth(healthBefore - 2);
							}
						}
					}}, 2);//if player != null  // 1/10th of a second
			}
		}//if entity type == fireball
	}

	@EventHandler
	public void ghastDamaged(EntityDamageEvent evt){
		if(evt.getCause() == DamageCause.PROJECTILE){
			if(evt.getEntityType() == EntityType.GHAST){
				Ghast g = (Ghast) evt.getEntity();
				if(g.getCustomName() != null) evt.setDamage(5);
			}
			else if(evt.getEntityType() == EntityType.PLAYER){
				Player p = (Player) evt.getEntity();
				if(isInArena(p.getLocation())){
					if(p.getHealth() - 2 >= 0){
						evt.setCancelled(true);
						p.setHealth(p.getHealth()-2);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onDeath(PlayerDeathEvent evt){
		if(fireballed.contains("," + evt.getEntity().getName() + ",")){
			evt.setDeathMessage(evt.getDeathMessage()+" in GhAsTpVp");
			fireballed = fireballed.replace("," + evt.getEntity().getName() + ",", ",");

			for(GhastHolder gh : ghasts)gh.ghast.remove();
			return;
		}
		else if(evt.getEntity().getLastDamageCause() != null){
			if(evt.getEntity().getLastDamageCause().getCause() == DamageCause.PROJECTILE){
				if(isInArena(evt.getEntity().getLocation())){
					for(GhastHolder gh : ghasts)gh.ghast.remove();
				}
			}
		}
	}

	@EventHandler
	public void onPlayerPressButton(PlayerInteractEvent evt){
		if(evt.isCancelled() == false && gameInProgress == false &&
				(evt.getClickedBlock().getType() == Material.WOOD_BUTTON || evt.getClickedBlock().getType() == Material.STONE_BUTTON)){

			Location loc = evt.getClickedBlock().getLocation();
			for(GhastHolder ghast : ghasts){
				if(isWithinXBlocks(ghast.loc, loc, 20)){

					boolean startGame = false;
					Entity[] ents = evt.getPlayer().getNearbyEntities(10, 6, 10).toArray(new Entity[]{});
					for(Entity ent : ents)if(ent.getType() == EntityType.PLAYER){
						startGame = true;
						((Player)ent).sendMessage("�6Match starts in 15 seconds!");
					}

					if(startGame){
						for(GhastHolder gh : ghasts)gh.ghast.remove();
						getServer().getScheduler().scheduleSyncDelayedTask(this, new Thread(){public void run(){
							registerGhasts();
						}}, 300);//15 seconds
						return;
					}
				}//if within X blocks of a ghast location
			}//for loop checking all ghast locations
		}//if the object pressed is a button, and there isn't already a game in progress
	}//end method

	public boolean isCloseTo(Location l1, Location l2){
		if(Math.abs(l1.getBlockX()- l2.getBlockX()) < 2 &&
				Math.abs(l1.getBlockY()- l2.getBlockY()) < 2 &&
				Math.abs(l1.getBlockZ()- l2.getBlockZ()) < 2 &&
				l1.getWorld().getName().equals(l2.getWorld().getName()))return true;

		else return false;
	}

	public boolean isInArena(Location loc){
		for(GhastHolder ghast : ghasts){
			if(isWithinXBlocks(ghast.loc, loc, 20)){
				return true;
			}
		}
		return false;
	}

	public boolean isWithinXBlocks(Location l1, Location l2, int x){
		if(Math.abs(l1.getBlockX()- l2.getBlockX()) < x &&
				Math.abs(l1.getBlockY()- l2.getBlockY()) < x &&
				Math.abs(l1.getBlockZ()- l2.getBlockZ()) < x &&
				l1.getWorld().getName().equals(l2.getWorld().getName()))return true;

		else return false;
	}

	private void checkGhasts(){
		for(int i = 0; i < ghasts.size(); i++){
			//if the ghast is dead or if it is nearly dead
			if(ghasts.get(i).ghast != null && ghasts.get(i).ghast.isDead() == false){

				if(isCloseTo(ghasts.get(i).ghast.getLocation(), ghasts.get(i).loc) == false){

					Ghast g = (Ghast) ghasts.get(i).loc.getWorld().spawnEntity(ghasts.get(i).loc, EntityType.GHAST);
					g.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100);
					g.setHealth(ghasts.get(i).ghast.getHealth());
					g.setNoDamageTicks(ghasts.get(i).ghast.getNoDamageTicks());//180 seconds (3 minutes)
					g.setCustomName("�6Pitcher");
					g.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100000, 3));

					ghasts.get(i).ghast.remove();
					ghasts.set(i, new GhastHolder(g, ghasts.get(i).loc));
				}
			}
		}
	}
}
