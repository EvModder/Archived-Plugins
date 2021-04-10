package Evil_Code_BigPet;

import org.bukkit.entity.LivingEntity;

public class EntityPet {
	String owner;
	LivingEntity pet;
	
	public EntityPet(String owner, LivingEntity pet){
		this.owner = owner;
		this.pet = pet;
	}
}
