package Evil_Code_BigPet;

import java.util.ArrayList;
import java.util.Random;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class BigPet extends JavaPlugin implements Listener{
	ArrayList<EntityPet> petlist = new ArrayList<EntityPet>();
	Random rand = new Random();
	//flashninja1519
	
	@Override public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@SuppressWarnings("deprecation") @EventHandler
	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
		if(cmd.getName().equalsIgnoreCase("bigpet") && sender instanceof Player) {
			if(args.length < 1) sender.sendMessage("�cPlease supply a player name");
			else if(args.length < 2) sender.sendMessage("�cPlease supply an entity type");
			
			else if(getServer().getPlayerExact(args[0]) == null) sender.sendMessage("�cPlayer not found.");
			//
			Player owner = getServer().getPlayerExact(args[0]);
			//
			if(args[1].equalsIgnoreCase("bigpet"))giantPet(owner);
			else if(args[1].equalsIgnoreCase("witherpet"))witherPet(owner);
			else if(args[1].equalsIgnoreCase("batpet"))batPet(owner);
			else{
				//---------------------------- Spawn a default, boring pet ----------------------------
				EntityType type;
				try{type = EntityType.valueOf(args[1].toUpperCase());}
				catch(NullPointerException ex){sender.sendMessage("�cInvalid EntityType!"); return true;}
				
				Player p = (Player) sender;
				Entity entity = p.getWorld().spawnEntity(p.getLocation(), type);
				if(entity != null && entity instanceof LivingEntity == false){
					entity.remove();
					p.sendMessage("�eOnly living entities can be used as pets!");
					p.sendMessage("�ePlease reference the list below:");
					String typelist = "";
					for(EntityType ent : EntityType.values())typelist += ent.getClass().getName() +", ";
					typelist = (typelist+"*").replace(", *", "");
					
					p.sendMessage("�7"+typelist);
				}
				else{
					petlist.add(new EntityPet(args[0], (LivingEntity)entity));
					//
					MetadataValue md = new FixedMetadataValue(this, args[0]);
					entity.setMetadata("Pet", md);
				}
			}
			return true;
		}
		return false;
	}
		
	@EventHandler
	public void onBigPetTarget(EntityTargetEvent evt){
		if(evt.getTarget() instanceof Player){
		//	Player p = (Player) evt.getTarget();
			for(EntityPet pet : petlist){
				if(//pet.owner.equalsIgnoreCase(p.getName()) &&
						pet.pet.getUniqueId().toString().equals(evt.getEntity().getUniqueId().toString()))
					evt.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityClick(PlayerInteractEntityEvent evt){
		if(evt.getRightClicked() instanceof Bat){
			for(EntityPet pet : petlist){
				if(pet.owner.equalsIgnoreCase(evt.getPlayer().getName())){
					if(pet.pet.getUniqueId().toString().equals(evt.getRightClicked().getUniqueId().toString()))
						evt.getRightClicked().setPassenger(evt.getPlayer());
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityByEntityDamage(EntityDamageByEntityEvent evt){
		if(evt.getEntity() instanceof LivingEntity){
			LivingEntity le = (LivingEntity) evt.getEntity();
			
			if(le instanceof Giant){
				if(rand.nextInt(3) == 0){
					le.teleport(evt.getDamager());
				}
			}
			for(EntityPet pet : petlist){
				if(pet instanceof Monster){
					if(le instanceof Player && pet.owner.equalsIgnoreCase(le.getCustomName())
							&& evt.getDamager() instanceof LivingEntity){
						((Monster)pet).setTarget((LivingEntity)evt.getDamager());
					}
					if(evt.getDamager() instanceof Player &&
							pet.owner.equalsIgnoreCase(((LivingEntity)evt.getDamager()).getCustomName())){
						((Monster)pet).setTarget(le);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent evt){
		for(EntityPet pet : petlist){
			if(pet.owner.equalsIgnoreCase(evt.getPlayer().getName())){
				if(isWithinXBlocks(pet.pet.getLocation(), evt.getPlayer().getLocation(), 8))
					pet.pet.teleport(evt.getTo());
			}
		}
	}
	
	@EventHandler
	public void onChunkLoad(final ChunkLoadEvent evt){
		for(Entity ent : evt.getChunk().getEntities()){
			if(ent instanceof LivingEntity){
				if(ent.getMetadata("Pet").isEmpty() == false){
					for(MetadataValue v : ent.getMetadata("Pet")){
						if(v.getOwningPlugin().getName().equalsIgnoreCase(this.getName())){
							petlist.add(new EntityPet(v.asString(), (LivingEntity)ent));
						}
					}
				}
				else
				for(EntityPet pet : petlist){
					if(pet.pet.getUniqueId().toString().equals(ent.getUniqueId().toString())){
						ent.remove();
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onChunkUnload(final ChunkUnloadEvent evt){
		for(Entity ent : evt.getChunk().getEntities()){
			for(EntityPet pet : petlist){
				if(pet.pet.getUniqueId().toString().equals(ent.getUniqueId().toString())){
					evt.setCancelled(true);
					getServer().getScheduler().scheduleSyncDelayedTask(this, new Thread(){public void run(){
						evt.getChunk().load();
					}}, 1);
				}
			}
		}
	}
	
	//
	public boolean isWithinXBlocks(Location loc1, Location loc2, double range){
		return !(loc1.getX()-loc2.getBlockX() > range ||
				 loc1.getY()-loc2.getBlockY() > range ||
				 loc1.getZ()-loc2.getBlockZ() > range ||
				 loc1.getWorld().getName().equals(loc2.getWorld().getName()) == false);
	}
	
	private ItemStack addEnchants(ItemStack item){
		ItemMeta meta = item.getItemMeta();
			meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 10, true);
			meta.addEnchant(Enchantment.PROTECTION_PROJECTILE, 10, true);
			meta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 10, true);
			meta.addEnchant(Enchantment.PROTECTION_FIRE, 10, true);
			meta.addEnchant(Enchantment.PROTECTION_FALL, 10, true);
			meta.addEnchant(Enchantment.WATER_WORKER, 10, true);
			meta.addEnchant(Enchantment.DURABILITY, 100, true);
			meta.addEnchant(Enchantment.OXYGEN, 10, true);
			meta.addEnchant(Enchantment.THORNS, 5, true);
		item.setItemMeta(meta);
		return item;
	}
	
	//Pet List --------------------------------------------------------
	public void giantPet(Player owner){
		final Giant bigpet = (Giant) owner.getWorld().spawnEntity(owner.getLocation(), EntityType.GIANT);
		bigpet.setCustomName("�c�lBlargh");
		bigpet.setCustomNameVisible(true);
		bigpet.setMaxHealth(1000);
		bigpet.setHealth(950);
		
		bigpet.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 3));//10m
		bigpet.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100000, 0));//10m
		bigpet.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100000, 3));//10m
		bigpet.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100000, 3));//10m
		
		//giveOpGear(bigpet);
		bigpet.getEquipment().setHelmetDropChance(0);
		bigpet.getEquipment().setChestplateDropChance(0);
		bigpet.getEquipment().setLeggingsDropChance(0);
		bigpet.getEquipment().setBootsDropChance(0);
		petlist.add(new EntityPet(owner.getName(), bigpet));
		//
		MetadataValue md = new FixedMetadataValue(this, owner.getName());
		bigpet.setMetadata("Pet", md);
		
		//If the bigpet gets bored, it will attack stuff around it
	    getServer().getScheduler().scheduleSyncRepeatingTask(this, new Thread(){public void run(){
	    	if(bigpet != null && bigpet.isLeashed() == false){
	    		for(Entity ent : bigpet.getLocation().getChunk().getEntities()){
	    			if(ent instanceof LivingEntity) bigpet.setTarget((LivingEntity) ent);
	    		}
	    	}
	    }}, 1200, 6000);//waits for 1 minute before starting, then runs every 5 mins (1200 ticks = 60 seconds)
	}
	
	public void witherPet(Player owner){
		final Skeleton with = (Skeleton) owner.getWorld().spawnEntity(owner.getLocation(), EntityType.SKELETON);
		with.setCustomName("Sebastian");
		with.setCustomNameVisible(true);
		with.setMaxHealth(1000);
		with.setHealth(950);//FlashNinja519
		
		with.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 3));//inf
		with.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100000, 3));//inf
		with.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1200, 0));//36000, 0));//30min
		with.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100000, 0));//inf
		with.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100000, 3));//inf
		with.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100000, 0));//inf
		//helm
//		ItemStack helm = new ItemStack(Material.LEATHER_HELMET);
//		LeatherArmorMeta meta = (LeatherArmorMeta)helm.getItemMeta();
//		meta.setColor(Color.BLACK); helm.setItemMeta(meta);
//		addEnchants(helm); with.getEquipment().setHelmet(helm);
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) org.bukkit.SkullType.PLAYER.ordinal());//
		SkullMeta skullmeta = (SkullMeta) skull.getItemMeta();//
		skullmeta.setOwner("FlashNinja519");//
		skull.setItemMeta(skullmeta);//
		with.getEquipment().setHelmet(skull);//
		//chestplate
		ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta meta = (LeatherArmorMeta)chest.getItemMeta();
		meta.setColor(Color.BLACK); chest.setItemMeta(meta);
		addEnchants(chest); with.getEquipment().setHelmet(chest);
		//leggings
		ItemStack leggs = new ItemStack(Material.LEATHER_LEGGINGS);
		meta = (LeatherArmorMeta)leggs.getItemMeta();
		meta.setColor(Color.BLACK); leggs.setItemMeta(meta);
		addEnchants(leggs); with.getEquipment().setHelmet(leggs);
		//boots
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
		meta = (LeatherArmorMeta)boots.getItemMeta();
		meta.setColor(Color.BLACK); boots.setItemMeta(meta);
		addEnchants(boots); with.getEquipment().setHelmet(boots);
		//boots
		ItemStack sword = new ItemStack(Material.GOLD_SWORD);
		ItemMeta swordMeta = sword.getItemMeta();
		meta.addEnchant(Enchantment.DAMAGE_ALL, 10, true);
		meta.addEnchant(Enchantment.DAMAGE_UNDEAD, 5, true);
		meta.addEnchant(Enchantment.DAMAGE_ARTHROPODS, 5, true);
		meta.addEnchant(Enchantment.LOOT_BONUS_MOBS, 5, true);
		meta.addEnchant(Enchantment.FIRE_ASPECT, 3, true);
		meta.addEnchant(Enchantment.DURABILITY, 100, true);
		//
		swordMeta.setDisplayName("�8�oDark whispers");
		with.getEquipment().setItemInHand(sword);
		//
		with.getEquipment().setHelmetDropChance(0);
		with.getEquipment().setChestplateDropChance(0);
		with.getEquipment().setLeggingsDropChance(0);
		with.getEquipment().setBootsDropChance(0);
		with.getEquipment().setItemInHandDropChance(0);
		//
		petlist.add(new EntityPet(owner.getName(), with));
		MetadataValue md = new FixedMetadataValue(this, owner.getName());
		with.setMetadata("Pet", md);
	}
	
	public void batPet(Player owner){
		final Bat bat = (Bat) owner.getWorld().spawnEntity(owner.getLocation(), EntityType.BAT);
		bat.setCustomName("�bChirp");
		bat.setCustomNameVisible(true);
		bat.setMaxHealth(100);
		bat.setHealth(80);
		
		bat.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 3));//inf
		bat.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100000, 0));//inf
		bat.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100000, 0));//inf
		
		//
		petlist.add(new EntityPet(owner.getName(), bat));
		MetadataValue md = new FixedMetadataValue(this, owner.getName());
		bat.setMetadata("Pet", md);
	}
}
