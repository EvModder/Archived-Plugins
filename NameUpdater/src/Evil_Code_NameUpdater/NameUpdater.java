package Evil_Code_NameUpdater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class NameUpdater extends JavaPlugin implements Listener{
	private boolean chestshopInstalled, locketteInstalled;

	@Override public void onEnable(){
		if(getServer().getPluginManager().getPlugin("Lockette") != null) locketteInstalled = true;
		// if(getServer().getPluginManager().getPlugin("ChestShop") != null)
		// chestshopInstalled = true;
		if(chestshopInstalled || locketteInstalled) getServer().getPluginManager().registerEvents(this, this);

		getLogger().info("NameUpdater Enabled!");
	}

	@EventHandler public void onPlayerLogin(PlayerLoginEvent evt){
		String oldName = getServer().getOfflinePlayer(evt.getPlayer().getUniqueId()).getName();
		String newName = evt.getPlayer().getName();

		if(!oldName.equals(newName)) {
			getLogger().info("Updating name-based data for: " + evt.getPlayer().getUniqueId());

			// -------------- Update Factions -------------------------------------------------------------------------------
			Plugin pl = getServer().getPluginManager().getPlugin("Factions");
			if(pl != null) {
				getServer().getPluginManager().disablePlugin(pl);

				String data = loadFile("./plugins/Factions/player.json");
				data = data.replace(oldName, newName);
				saveFile("./plugins/Factions/player.json", data);

				data = loadFile("./plugins/Factions/factions.json");
				data = data.replace(oldName, newName);
				saveFile("./plugins/Factions/factions.json", data);

				getServer().getPluginManager().enablePlugin(pl);
			}
			// -------------- Update uSkyblock -------------------------------------------------------------------------------
			pl = getServer().getPluginManager().getPlugin("uSkyBlock");
			if(pl != null) {
				getServer().getPluginManager().disablePlugin(pl);

				File file = new File("./plugins/uSkyBlock/players/" + oldName + ".yml");

				@SuppressWarnings("unused")
				boolean uuidMatch = false;
				String data = null;
				String islandFilename = ",";
				try{
					BufferedReader br = null;
					try{
						br = new BufferedReader(new FileReader(file));
						StringBuilder sb = new StringBuilder();
						String line = br.readLine();

						while(line != null){
							if(line.contains("islandX:")) {
								islandFilename = line.replace(" ", "").replace("islandX:", "") + islandFilename;
							}
							else if(line.contains("islandZ:")) {
								islandFilename = islandFilename + line.replace(" ", "").replace("islandZ:", "");
							}
							else if(line.contains("uuid:")) {
								if(evt.getPlayer().getUniqueId().equals(UUID.fromString(line.replace(" ", "").replace("uuid", "")))) {
									uuidMatch = true;
								}
							}
							else if(line.contains("displayName:")) {
								line = line.replace(oldName, newName);
							}
							sb.append(line);
							sb.append(System.lineSeparator());
							line = br.readLine();
						}
						br.close();
						data = sb.toString();
					}
					catch(FileNotFoundException e){/* Plugin is not installed */}
					if(br != null) br.close();
				}
				catch(IOException e){
					e.printStackTrace();
				}

				if(data != null) saveFile("./plugins/uSkyBlock/players/" + oldName + ".yml", data);
				file.renameTo(new File("./plugins/uSkyBlock/players/" + newName + ".yml"));

				data = loadFile("./plugins/uSkyBlock/islands/" + islandFilename + ".yml");
				data = data.replace(oldName, newName);
				saveFile("./plugins/uSkyBlock/islands/" + islandFilename + ".yml", data);

				getServer().getPluginManager().enablePlugin(pl);
			}
			// -------------- Update ChestShop ------------------------------------------------------------------------------
		}
	}

	public String loadFile(String filename){
		try{
			BufferedReader br = null;
			try{
				br = new BufferedReader(new FileReader("./plugins/Factions/player.json"));
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();

				while(line != null){
					sb.append(line);
					sb.append(System.lineSeparator());
					line = br.readLine();
				}
				br.close();
				return sb.toString();
			}
			catch(FileNotFoundException e){/* Plugin is not installed */}
			if(br != null) br.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}

		return null;
	}

	public boolean saveFile(String path, String content){
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(path));
			writer.write(content);
			writer.close();
			return true;
		}
		catch(IOException e){
			return false;
		}
	}

	@EventHandler(priority = EventPriority.MONITOR) public void onSignPlace(SignChangeEvent evt){
		if(evt.isCancelled()) return;

		final Location loc = evt.getBlock().getLocation();
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			@Override public void run(){
				if(loc.getBlock().getState() instanceof Sign) {
					updateSign((Sign)loc.getBlock().getState());
				}
			}
		}, 1);
	}

	@EventHandler public void onSignClick(PlayerInteractEvent evt){
		if(evt.isCancelled()) return;

		if(evt.getClickedBlock().getState() instanceof Sign) {
			updateSign((Sign)evt.getClickedBlock().getState());
		}
	}

	@SuppressWarnings("deprecation") private void updateSign(Sign sign){
		if(isChestShopSign(sign)) {
			if(getServer().getOfflinePlayer(sign.getLine(0)).hasPlayedBefore() == false) sign.getBlock().breakNaturally();
		}
		// if(isLocketteSign(sign)){}
	}

	@SuppressWarnings("deprecation") public boolean isChestShopSign(Sign sign){
		if(chestshopInstalled) {
			if(getServer().getOfflinePlayer(sign.getLine(0)).hasPlayedBefore()) {
				if(sign.getLine(1).matches("[-+]?\\d*\\.?\\d+")) {
					if(sign.getLine(2).contains("B") || sign.getLine(2).contains("S")) {
						if(Material.getMaterial(sign.getLine(3).split(":")[0]) != null) return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isLocketteSign(Sign sign){
		if(locketteInstalled) {
			String line0 = sign.getLine(0).toLowerCase();
			if(line0.contains("[") && line0.contains("]") && (line0.contains("private") || line0.contains("more users"))) {
				if(!sign.getLine(1).isEmpty() || !sign.getLine(2).isEmpty() || !sign.getLine(3).isEmpty()) return true;
			}
		}
		return false;
	}
}
