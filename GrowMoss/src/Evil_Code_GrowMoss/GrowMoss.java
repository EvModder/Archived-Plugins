package Evil_Code_GrowMoss;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class GrowMoss extends JavaPlugin implements Listener{
	boolean mossGrowth;
	
	@Override public void onEnable(){getServer().getPluginManager().registerEvents(this, this); getLogger().info("CannonWars Loaded");}
	@Override public void onDisable(){getLogger().info("CannonWars Closed");}

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]){
    	if(cmd.getName().equalsIgnoreCase("15-second_mark") && !(sender instanceof Player)){
    		int w = Bukkit.getWorlds().size()-1;
    		for(int i = 0; i < w; i++){
    			World world = Bukkit.getWorlds().get(i);
    			
    			Chunk[] chunks = world.getLoadedChunks();
    			for(int a = 0; a < chunks.length; a++){
    				//code to use new Random().nextInt(100); on every cobble for a 1% chance of turning it to mossy cobble.
    			}
    		}
    	}
    	else if(cmd.getName().equalsIgnoreCase("moss") && sender.hasPermission("GrowMoss.onoff")){
			
			if(args.length == 0){
				if(mossGrowth){mossGrowth = false; sender.sendMessage("Moss-Growth Disabled");}
				else{mossGrowth = true; sender.sendMessage("Moss-Growth Enabled");}
				return true;
			}
			else{
				if(args[0].equalsIgnoreCase("on") && mossGrowth == false){mossGrowth = true; sender.sendMessage("Moss-Growth Enabled");}
				else if(args[0].equalsIgnoreCase("off") && mossGrowth == true){mossGrowth = false; sender.sendMessage("Moss-Growth Disabled");}
				return true;
			}
		}
    	return false;
    }
}