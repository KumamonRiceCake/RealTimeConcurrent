import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Jeong Won Kim (101094705)
 *
 * This represent an agent thread
 */
public class Agent extends Thread {
	private Table table;	//Shared buffer
	
	public Agent(Table t) {
		table = t;
	}

	/**
	 * run function of a chef thread
	 */
	public void run() {
		//One agent thread can supply 2 ingredients for 20 sandwiches
		for (int i = 0; i < 20; i++) {
			ArrayList<Ingredients> agentSupply = selectIngredients();
			System.out.println("=============== Sandwich #" + (i+1) + " ===============");
			System.out.println(Thread.currentThread().getName()	+ " placed " + agentSupply);
			table.placeTwoIngredients(agentSupply);
			
			try {
				Thread.sleep(1000);		//Sleep for 1 second
			} catch (InterruptedException e) {}
		}
	}
	
	/**
	 * This function simulates the agent supplies 2 randomly chosen ingredients
	 * @return ArrayList with two random ingredients
	 */
	public ArrayList<Ingredients> selectIngredients() {
		Random rand = new Random();
		ArrayList<Ingredients> agentSelection = new ArrayList<Ingredients>(Arrays.asList(Ingredients.BREAD, Ingredients.PEANUT_BUTTER, Ingredients.JAM));
		agentSelection.remove(rand.nextInt(3));
		return agentSelection;
	}
}
