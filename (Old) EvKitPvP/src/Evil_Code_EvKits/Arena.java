package Evil_Code_EvKits;

public class Arena{
	final String name;
	final BlockData[] blocks;
	final Section bounds;
	private boolean isRunning;

	public Arena(String name, Section bounds, BlockData[] blocks){
		this.name = name;
		this.blocks = blocks;
		this.bounds = bounds;
	}

	public void setRunning(boolean newState){
		isRunning = newState;
	}
	public boolean isRunning(){
		return isRunning;
	}
}
