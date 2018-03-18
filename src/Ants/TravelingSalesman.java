package Ants;

import java.util.ArrayList;

import Display.*;
import Graph.*;

/**
 * Solves the Traveling Salesman Problem using vanilla Ant Colony Optimization.
 */
public class TravelingSalesman {

	private Graph graph;
	private int numOfAnts, generations;
	private Ant[] currentAnts;

	/**
	 * Construct TravelingSalesman.
	 * 
	 * @param ants
	 *            the number of ants to run per generation
	 * @param generations
	 *            the number of generations to run
	 * @param evaporation
	 *            the rate of evaporation
	 * @param alpha
	 *            the impact of pheromones on decision making
	 * @param beta
	 *            the impact of distance in decision making
	 */
	public TravelingSalesman(int ants, int generations, double evaporation, int alpha, int beta) {
		this.numOfAnts = ants;
		this.generations = generations;
		graph = IO.Import.getGraph(evaporation, alpha, beta);
	}

	/**
	 * Run the algorithm.
	 */
	public void start() {
		WindowTSP windowTSP = new WindowTSP(graph.getVertices());

		Ant bestAnt = null;
		int bestEval = 0;

		delay(1000); // Allow WindowTSP to load.

		for (int i = 0; i < generations; i++) {
			Ant[] ants = createAnts(numOfAnts);
			this.currentAnts = ants;

			Ant ant = travel(ants);
			updatePheromones(ants);

			if (bestAnt == null) {
				bestAnt = ant;
				bestEval = ant.eval();
				System.out.println(bestAnt);
				System.out.println(bestEval);
			} else if (ant.eval() < bestEval) {
				bestAnt = ant;
				bestEval = ant.eval();
				System.out.println(bestAnt);
				System.out.println(bestEval);
			}
			
			System.out.println("calculated");
			windowTSP.draw(bestAnt.getTour());
		}

		System.out.print("Best Tour: ");
		System.out.println(bestAnt);
		System.out.println("Evaluation: " + bestEval);
	}

	/**
	 * Create ants and put them on random starting positions on the graph.
	 * 
	 * @param quantity
	 *            the quantity of ants to create
	 * @return an array of the ants created
	 */
	private Ant[] createAnts(int quantity) {
		Ant[] ants = new Ant[quantity];
		for (int i = 0; i < quantity; i++) {
			ants[i] = new Ant(graph);
		}
		return ants;
	}

	/**
	 * Let each ant in the input array travel until an entire tour is completed.
	 * 
	 * @param ants
	 *            the ants to allow to travel
	 * @return the ant with the best evaluation
	 */
	private Ant travel(Ant[] ants) {

		Ant bestAnt = null;
		int bestEval = 0;

		// Ant bestAnt = Ant.getGreedy(graph);
		// int bestEval = bestAnt.eval();

		// for (Ant ant : ants) {
		// while (ant.notFinished()) {
		// ant.travel();
		// }
		// if (bestAnt == null) {
		// bestAnt = ant;
		// bestEval = ant.eval();
		// } else if (ant.eval() < bestEval) {
		// bestAnt = ant;
		// bestEval = ant.eval();
		// }
		// }

		ArrayList<Thread> threads = new ArrayList<>();
		ArrayList<ThreadTravel> threadTravels = new ArrayList<>();
		int start = 0;
		int numOfThread = 20;
		for (int i = 0; i < numOfThread; i++) {
			start = (ants.length / numOfThread) * i;
			ThreadTravel tt = new ThreadTravel(start, ants.length / numOfThread);
			Thread t = new Thread(tt);
			t.start();
			threads.add(t);
			threadTravels.add(tt);
		}
		for (int i = 0; i < threads.size(); i++) {
			try {
				threads.get(i).join();
				Ant ant = threadTravels.get(i).getBestAnt();

				if (bestAnt == null) {
					bestAnt = ant;
					bestEval = ant.eval();
				} else if (ant.eval() < bestEval) {
					bestAnt = ant;
					bestEval = ant.eval();
				}
			} catch (Exception e) {

			}
		}

		return bestAnt;
	}

	/**
	 * Update the pheromones in the graph based on an array of ants with completed
	 * tours.
	 * 
	 * @param ants
	 *            the ants that will be used to update the pheromones
	 */
	private void updatePheromones(Ant[] ants) {
		for (Ant ant : ants) {
			graph.updatePheromone(ant);
		}
	}

	/**
	 * Sleep the thread for a specified amount of time.
	 * 
	 * @param ms
	 *            milliseconds to sleep for
	 */
	private static void delay(int ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	class ThreadTravel implements Runnable {
		int fromIndex = 0;
		int offset = 0;
		Ant bestAnt = null;

		public ThreadTravel(int from, int offset) {
			this.fromIndex = from;
			this.offset = offset;
		}

		@Override
		public void run() {
			int bestEval = 0;

			for (int i = fromIndex; i < fromIndex + offset; i++) {
				Ant currentAnt = currentAnts[i];
				while (currentAnt.notFinished()) {
					currentAnt.travel();
				}

				if (bestAnt == null) {
					bestAnt = currentAnt;
					bestEval = currentAnt.eval();
				} else if (currentAnt.eval() < bestEval) {
					bestAnt = currentAnt;
					bestEval = currentAnt.eval();
				}
			}
		}

		public Ant getBestAnt() {
			return bestAnt;
		}
	}

}
