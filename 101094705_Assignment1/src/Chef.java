import java.util.ArrayList;

/**
 * @author Jeong Won Kim (101094705)
 *
 * This represent a chef thread
 */
public class Chef extends Thread{
	private Table table;			//Shared buffer
	private Ingredients supply;		//Infinite supply of a chosen ingredient 
	
	public Chef(Table t, Ingredients i)	{
		table = t;
		supply = i;
	}

	/**
	 * run function of a chef thread
	 */
	public void run() {
		//One chef thread can make up to 20 sandwiches
		for(int i = 0; i < 20; i++) {
			ArrayList<Ingredients> agentSupply = table.makeSandwich(supply);
			System.out.println(String.format("%s with %s made a sandwich using %s and ate it!\n", Thread.currentThread().getName(), supply, agentSupply));
			
			try {
				Thread.sleep(1000);		//Sleep for 1 second
			} catch (InterruptedException e) {}
		}
	}
}
