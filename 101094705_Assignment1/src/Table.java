import java.util.ArrayList;

/**
 * @author Jeong Won Kim (101094705)
 *
 * This class controls buffer to arbitrate chef and agent threads
 */
public class Table {
	private ArrayList<Ingredients> suppliedIngredients;		//Two ingredients supplied by agent
	private boolean empty;	//Indicate if buffer is empty
	
	public Table() {
		empty = true;
	}
	
	/**
	 * Put an ArrayList of two ingredients into buffer
	 * @param agentSupplies - two ingredients supplied by agent
	 */
	public synchronized void placeTwoIngredients(ArrayList<Ingredients> agentSupplies) {
		while (!empty) {
			try {
				wait();
			} catch (InterruptedException e) {System.err.println(e);}
		}
		suppliedIngredients = agentSupplies;
		empty = false;
		notifyAll();
	}

	/**
	 * Consume an ArrayList of two ingredients if having the remaining ingredient
	 * @param chefSupply - a supplied ingredient of a chef
	 * @return an ArrayList of two ingredients supplied by agent
	 */
	public synchronized ArrayList<Ingredients> makeSandwich(Ingredients chefSupply) {
		ArrayList<Ingredients> agentSupplies;
		//Wait while empty or not all 3 ingredients are ready
		while (empty || !checkIngredient(chefSupply)) {
			try {
				wait();
			} catch (InterruptedException e){System.err.println(e);}
		}
		agentSupplies = suppliedIngredients;
		empty = true;
		notifyAll();
		return agentSupplies;
	}
	
	/**
	 * Check if all 3 ingredients are ready
	 * @param chefSupply - a supplied ingredient of a chef
	 * @return true if ready for making a sandwich
	 */
	public boolean checkIngredient(Ingredients chefSupply) {
		//Check for agent supplies
		if (suppliedIngredients == null)
			return false;
		//Check for remaining supply from chef
		if (suppliedIngredients.contains(chefSupply)) {
			return false;
		}
		return true;
	}
}
