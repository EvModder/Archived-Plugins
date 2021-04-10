package Evil_Code_Gold4Cash;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class Gold4Cash extends JavaPlugin implements Listener{
    private boolean enabled = true;
	
	@Override public void onEnable(){getServer().getPluginManager().registerEvents(this, this);}

    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]){
    	if(cmd.getName().equalsIgnoreCase("Gold4Cash")){
    		if(args != null){
	            if(args.length > 0){
	            	if(args[0].equalsIgnoreCase("on"))enabled = true;
	            	else enabled = false;
	            }
    		}
        	if(enabled == false)enabled = true;
        	else enabled = false;
        	
        	return true;
        }
    	else return false;
    }
    
    @SuppressWarnings("deprecation")
	@EventHandler
    public void onSignClickEvent(PlayerInteractEvent event){
        try{Material mat = event.getClickedBlock().getType();
        if((mat != Material.SIGN  && mat != Material.WALL_SIGN && mat != Material.SIGN_POST) || event.isCancelled())return;}
        catch(NullPointerException e1){return;}
        
        Sign shop = (Sign) event.getClickedBlock().getState();
        if(shop.getLine(0).contains("-GoldShop-") == false)return;
        //------------------------------------------------------------ Finished checking to make sure sign & -GoldShop-
        
        String[] info = shop.getLine(2).replace(" ", "").replace("|", ":").replace("-", ":").replace("*", ":").replace("#", ":").split(":");
        if(info.length != 2){event.getPlayer().sendMessage("§4ERROR: §cUnable to parse line 3"); return;}
        
        Material itemSold = null;
        String itemName = info[0];
        
        //Get item Sold ------------------------------------------------------------------------------------------------------------
        if(Material.getMaterial(itemName) != null)itemSold = Material.getMaterial(itemName);
    	else{
	    	try{itemSold = Material.getMaterial(Integer.parseInt(itemName.split(":")[0]));}
	    	catch(NumberFormatException e){itemSold = null;}
        }
        
    	if(itemSold == null){event.getPlayer().sendMessage("§4ERROR: §cNo such item found!"); return;}
        //--------------------------------------------------------------------------------------------------------------------------
    	
		Player buyer = event.getPlayer();
		
        //Get the amount and the price ---------------------------------------------------------------------------------------------
    	int price = 0;
    	int amount= 0;
    	try{
    		amount= Integer.parseInt(info[1]);
			price = Integer.parseInt(shop.getLine(3).replace(" ", "").replace("$", "").replace("§2", "").replace("§a", "").replace("§", ""));
		}
    	catch(NumberFormatException e){e.printStackTrace(); shop.setLine(3, "§4Invalid Price");}
    	
		if(amount <= 0 || price <= 0){buyer.sendMessage("§cInvalid price/amount"); return;}
        //--------------------------------------------------------------------------------------------------------------------------
		
	    //Search for the chest -----------------------------------------------------------------------------------------------------
		Chest chest = null;
		
		if(shop.getWorld().getBlockAt(shop.getX(), shop.getY()-1, shop.getZ()).getType() == Material.CHEST)
			chest = (Chest) shop.getWorld().getBlockAt(shop.getX(), shop.getY()-1, shop.getZ()).getState();
			
		else if(shop.getWorld().getBlockAt(shop.getX()+1, shop.getY(), shop.getZ()).getType() == Material.CHEST)
			chest = (Chest) shop.getWorld().getBlockAt(shop.getX()+1, shop.getY(), shop.getZ()).getState();
					
		else if(shop.getWorld().getBlockAt(shop.getX()-1, shop.getY(), shop.getZ()).getType() == Material.CHEST)
			chest = (Chest) shop.getWorld().getBlockAt(shop.getX()-1, shop.getY(), shop.getZ()).getState();
					
		else if(shop.getWorld().getBlockAt(shop.getX(), shop.getY(), shop.getZ()+1).getType() == Material.CHEST)
			chest = (Chest) shop.getWorld().getBlockAt(shop.getX(), shop.getY(), shop.getZ()+1).getState();
					
		else if(shop.getWorld().getBlockAt(shop.getX(), shop.getY(), shop.getZ()-1).getType() == Material.CHEST)
			chest = (Chest) shop.getWorld().getBlockAt(shop.getX()+1, shop.getY(), shop.getZ()-1).getState();
		
		if(chest == null){buyer.sendMessage("§4Could not find the chest!"); return;}
		//--------------------------------------------------------------------------------------------------------------------------
		
		ItemStack item = new ItemStack(itemSold);
		item.setAmount(amount);
		ItemStack GOLD = new ItemStack(Material.GOLD_INGOT);
		GOLD.setAmount(price);
		
		boolean buying = shop.getLine(1).toLowerCase().contains("buy");
		// if the player is [Buy]ing from the chest
		if(buying && chest.getInventory().containsAtLeast(item, amount) &&//if the chest has the item
						 buyer.getInventory().containsAtLeast(GOLD, price) &&
						 chest.getInventory().first(item.getType()) != -1 &&
						 buyer.getInventory().first(GOLD.getType()) != -1){//if the player has the gold
			
			Inventory chestInv = chest.getInventory();
			/*-----------------------------------------------------------------------------------------*/
			ItemStack[] stuff = chestInv.getContents();
			int originalGold = 0;
			for(ItemStack is : stuff)try{
					if(is.getType() ==  GOLD.getType())originalGold += is.getAmount();
				}catch(NullPointerException ex){}
			if(chestInv.contains(Material.GOLD_INGOT))
				chestInv.getItem(chestInv.first(Material.GOLD_INGOT))
					.setAmount(chestInv.getItem(chestInv.first(Material.GOLD_INGOT)).getAmount() + price);
			else chestInv.addItem(GOLD);
			if(chestInv.containsAtLeast(GOLD, originalGold + price) == false)return;
			/*-----------------------------------------------------------------------------------------*/
			
			giveItem(buyer.getInventory(), item);
				
			int countAmount = amount;
			while(countAmount > 0){
				ItemStack itemcheck = chest.getInventory().getItem(chest.getInventory().first(item));
				countAmount -= itemcheck.getAmount();
				itemcheck.setAmount(itemcheck.getAmount()-amount);
				amount = countAmount;
				
				if(itemcheck.getAmount() <= 0)
					chest.getInventory().getItem(chest.getInventory().first(item)).setType(Material.AIR);
				else{
					chest.getInventory().getItem(chest.getInventory().first(item)).setAmount(itemcheck.getAmount());
					break;
				}
			}
			
			int countPrice = price;
			while(countPrice > 0){
				ItemStack itemcheck = buyer.getInventory().getItem(buyer.getInventory().first(GOLD));
				countPrice -= itemcheck.getAmount();
				itemcheck.setAmount(itemcheck.getAmount()-price);
				price = countPrice;
				
				if(itemcheck.getAmount() <= 0)
					buyer.getInventory().getItem(buyer.getInventory().first(GOLD)).setType(Material.AIR);
				else{
					buyer.getInventory().getItem(buyer.getInventory().first(GOLD)).setAmount(itemcheck.getAmount());
					break;
				}
			}
			
			event.getPlayer().sendMessage("§2Exchange Successful");
			return;
		}
		else if(buying == false && chest.getInventory().containsAtLeast(GOLD, price) &&//if the chest has enough gold
							 buyer.getInventory().containsAtLeast(item, amount) &&
							 buyer.getInventory().first(GOLD.getType()) != -1 &&
							 chest.getInventory().first(item.getType()) != -1){//if the player has the items to sell
		
			/*-----------------------------------------------------------------------------------------*/
			int original = 0;
			ItemStack[] stuff = chest.getInventory().getContents();
			for(ItemStack is : stuff)try{
					if(is.getType() == item.getType())original += is.getAmount();
				}catch(NullPointerException ex){}
			/*-----------------------------------------------------------------------------------------*/
			chest.getInventory().addItem(item);//give the chest it's item(s)
		
			if(chest.getInventory().containsAtLeast(item, (amount + original))){//<--------- If the chest could fit the item(s) sold
				
				int countPrice = price;
				while(countPrice > 0){
					ItemStack itemcheck = chest.getInventory().getItem(chest.getInventory().first(Material.GOLD_INGOT));
					countPrice -= itemcheck.getAmount();
					itemcheck.setAmount(itemcheck.getAmount()-price);
					price = countPrice;
					
					if(itemcheck.getAmount() <= 0)
						chest.getInventory().getItem(chest.getInventory().first(GOLD)).setType(Material.AIR);
					else{
						chest.getInventory().getItem(chest.getInventory().first(GOLD)).setAmount(itemcheck.getAmount());
						break;
					}
				}
				
				int countAmount = amount;
				while(countAmount > 0){
					ItemStack itemcheck = buyer.getInventory().getItem(buyer.getInventory().first(item.getType()));
					countAmount -= itemcheck.getAmount();
					itemcheck.setAmount(itemcheck.getAmount()-amount);
					amount = countAmount;
					
					if(itemcheck.getAmount() <= 0)
						buyer.getInventory().getItem(buyer.getInventory().first(item)).setType(Material.AIR);
					else{
						buyer.getInventory().getItem(buyer.getInventory().first(item)).setAmount(itemcheck.getAmount());
						break;
					}
				}
				//and give the pay to the seller!
				giveItem(buyer.getInventory(), GOLD);
				event.getPlayer().sendMessage("§2Exchange Successful");
				return;
			}//if the chest has room
		}//if the player is [Sell]ing to the chest
		buyer.sendMessage("§cUnable to perform the trade!\n§c§oEither you or the chest cannot afford this or are/is out of stock.");
	}
	
    @SuppressWarnings("deprecation")
	@EventHandler
    public void shopInstalled(SignChangeEvent event){
       	if(!(event.getLine(0).toLowerCase().replace("-", "").contains("goldshop")))return;
       	event.setLine(0, "§c-GoldShop-");
       	
        Player owner = event.getPlayer();
   		if(event.getLine(1).equals("") || owner.hasPermission("Gold4Cash.others") == false)event.setLine(1, owner.getName());
		
		String[] inf = event.getLine(2).replace(" ", "").replace("|", ":").replace("-", ":").replace("*", ":").replace("#", ":").split(":");
		if(inf.length != 2){event.setLine(2, "§4To Many Values"); return;}
		
      	ItemStack itemSold = null;
      	String itemName = inf[0];
      				
   		if(Material.getMaterial(itemName) != null)itemSold = new ItemStack(Material.getMaterial(itemName));
   		else{
   			try{itemSold = new ItemStack(Integer.parseInt(itemName));}
   			catch(NumberFormatException e){itemSold = null;}
    	}
   		
   		if(itemSold == null)event.setLine(2, "§4Unknown");
   		else{
   			int price = 0;
   			int amount= 0;
   			try{
   				amount = Integer.parseInt(inf[1]);
				price = Integer.parseInt(event.getLine(3).replace(" ", "").replace("$", "").replace("§", ""));
				
				if(amount >= 0 && price >= 0){
					event.setLine(3, ("§a"+price+"§2$"));
					if(event.getLine(1).toLowerCase().contains("b"))event.setLine(1, "[Buy]");
					else event.setLine(1, "[Sell]");
				}
				else event.setLine(3, ("§4"+price));
				
				//if it made it here it's all good and well
				event.setLine(0, "§6§l-GoldShop-");
			}
   			catch(NumberFormatException e){e.printStackTrace(); event.setLine(3, "§4Invalid Price");}
  	 	}
   	}
    
    private void giveItem(Inventory inv, ItemStack i){
		//and give the pay to the seller!
		if(inv.contains(i.getType()))
			inv.getItem(inv.first(i.getType())).setAmount(
				inv.getItem(inv.first(i.getType())).getAmount() + i.getAmount());
		
		else if(inv.contains(Material.AIR)){
			inv.getItem(inv.firstEmpty()).setType(i.getType());
			inv.getItem(inv.first(i.getType())).setAmount(i.getAmount());
		}
		else if(inv.getHolder() instanceof Player){
			((Player)(inv.getHolder())).getWorld().dropItemNaturally(((Player)inv.getHolder()).getLocation(), i);
		}
    	
    }
}