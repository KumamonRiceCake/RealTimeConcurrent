/**
 * @author Jeong Won Kim (101094705)
 *
 * Main class that is responsible for program initialization
 */
public class Sandwich {
	public static void main(String[] args) {
		Thread agentThread, chefThread1, chefThread2, chefThread3;
		Table table = new Table();
	
		// 1 agent thread and 3 chefs threads with a different ingredient supply
		agentThread = new Thread(new Agent(table),"Agent");
		chefThread1 = new Thread(new Chef(table, Ingredients.BREAD), "Chef1");
		chefThread2 = new Thread(new Chef(table, Ingredients.PEANUT_BUTTER), "Chef2");
		chefThread3 = new Thread(new Chef(table, Ingredients.JAM), "Chef3");
	
		agentThread.start();
		chefThread1.start();
		chefThread2.start();
		chefThread3.start();
	}
}