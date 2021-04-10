package Evil_Code_FrameProtector;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class FrameProtector extends JavaPlugin implements Listener{	
	
	@Override public void onEnable(){getServer().getPluginManager().registerEvents(this, this);}
	
	@EventHandler
	public void onPlayerPunchFrame(EntityDamageByEntityEvent evt){
		if(evt.getEntityType() == EntityType.ITEM_FRAME && !evt.isCancelled()){
			if(evt.getDamager() instanceof Player){
				if(canEdit(((Player)evt.getDamager()), evt.getEntity().getLocation()) == false){
					evt.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractItemFrame(PlayerInteractEntityEvent evt){
		if(evt.getRightClicked().getType() == EntityType.ITEM_FRAME && !evt.isCancelled()){
			if(canEdit(evt.getPlayer(), evt.getRightClicked().getLocation()) == false){
				evt.setCancelled(true);
			}
		}
	}
	
	private boolean canEdit(Player player, Location location){
		BlockBreakEvent event = new BlockBreakEvent(location.getWorld().getBlockAt(location), player);
		getServer().getPluginManager().callEvent(event);
		
		if(event.isCancelled()) return false;
		else{
			event.setCancelled(true);
			return true;
		}
	}
}