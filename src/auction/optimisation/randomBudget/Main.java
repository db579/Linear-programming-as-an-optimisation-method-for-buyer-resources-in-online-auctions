package	 auction.optimisation.randomBudget;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

import org.javasim.RestartException;
import org.javasim.Scheduler;
import org.javasim.SimulationException;
import org.javasim.SimulationProcess;

public class Main extends SimulationProcess{
	
	private int simDuration;
	private ArrayList<OptimisedBidder> OptimisedBidders = new ArrayList<OptimisedBidder>();
	private int numberOfOptimisedBidders = 1;
	
	public static void main(String[] args) {
		
		Main main = new Main();													
		main.await();																		//Pause main thread and start simulator/scheduler thread
	}
	
	public void await ()
    {
		this.resumeProcess();
		SimulationProcess.mainSuspend();
    }
	
	public void printResults(ArrayList<Bidder> Bidders, ArrayList<Auction> Auctions, ArrayList<OptimisedBidder>OptimisedBidders) throws IOException{
		
		  BufferedWriter out;
		  out = new BufferedWriter(new FileWriter("/home/dan/Documents/Project/Results.txt", true));
		
		DecimalFormat decFor = new DecimalFormat("00.00");
		
		for (int i=0; i<OptimisedBidders.size(); i++){
		OptimisedBidder OptimisedBidder = OptimisedBidders.get(i);
		double optimisedBidderutility = 0;
		for (int j=0; j<Auctions.size(); j++){
			Auction auction = Auctions.get(j);
			if(OptimisedBidder == auction.getHighBidder()){
				optimisedBidderutility = optimisedBidderutility + auction.getUtility();
			}
		}
		System.out.println(OptimisedBidder.getUserName()+" has made a utility of $"+decFor.format(optimisedBidderutility));
		out.write(decFor.format(optimisedBidderutility)+" ");
		}
		
		//Print the utility that all generated bidders have made from the auction
		for(int i=0; i<Bidders.size(); i++){
			Bidder bidder = Bidders.get(i);
			double utility = 0;
			for (int j=0; j<Auctions.size(); j++){
				Auction auction = Auctions.get(j);
				if(bidder == auction.getHighBidder()){
					utility = utility + auction.getUtility();
				}
			}
			System.out.println(bidder.getUserName()+" has made a utility of $"+decFor.format(utility));
			out.write(decFor.format(utility)+" ");
		}
		out.write("\n");
		out.write("\n");
		out.close();
	}
	
	public void run(){
		
		double budget = 5 + (Math.random()*1000);
		
		for(int i=0; i<numberOfOptimisedBidders; i++){										 		
		OptimisedBidder OptimisedBidder = new OptimisedBidder("OptimisedBidder"+i, budget);   //Instantiate OptimisedBidder class
		OptimisedBidders.add(OptimisedBidder);
		}		
		
		ScenarioGenerator scenGen = new ScenarioGenerator(20);		   						//Instantiate ScenarioGenerator class
		scenGen.createItemAndAuction();													    //Create items and auctions
		scenGen.createBidders(20, 20, 20);													//Create bidders
		scenGen.passAuctionsToBidders();												    //Pass list of generated auctions to all simulated bidders
		
		for(int i=0; i<OptimisedBidders.size(); i++){
		Optimiser optimiser = new Optimiser();
		OptimisedBidder OptimisedBidder = OptimisedBidders.get(i);
		OptimisedBidder.setAuctions(scenGen.getAuctions());								    //Pass list of generated auctions to OptimisedBidder
		OptimisedBidder.setOptimiser(optimiser);										    //Pass optimiser to OptimisedBidder
		OptimisedBidder.setScenarioGenerator(scenGen);									  	//Pass scenGen to OptimisedBidder
		}
		
		for (int i=0; i<scenGen.getAuctions().size(); i++){
			Auction auction = scenGen.getAuctions().get(i);
			if(auction.getDuration() > simDuration){
				simDuration = auction.getDuration();
			}
		}
		
		
		scenGen.printItemsUtility(); 													   	//Prints a list of all items created by the scenario generator, the current high bid on that item and it's resultant utility
		
		System.out.println("Simulation started at: "+time());
		
		ArrayList<Bidder> tempBidders = new ArrayList<Bidder>();
		for(int i=0; i<OptimisedBidders.size(); i++){
			OptimisedBidder optimisedBidder = OptimisedBidders.get(i);
			tempBidders.add(optimisedBidder);
		}
		
		for(int i=0; i<scenGen.getBidders().size(); i++){
			Bidder bidder = scenGen.getBidders().get(i);
			tempBidders.add(bidder);
		}
		
		
		Collections.shuffle(tempBidders);
		
		try {
		for(int i=0; i<tempBidders.size(); i++){
			Bidder bidder = tempBidders.get(i);
			bidder.activate();
		}

				Scheduler.startSimulation();												//Start simulation	
				hold(simDuration);
				SimulationProcess.mainResume();												//Resume main thread
		}	 
			catch (SimulationException e) {
				e.printStackTrace();
			} catch (RestartException e) {
				e.printStackTrace();
			}		

		System.out.println("Simulation ended at: "+time());
		try {
			printResults(scenGen.getBidders(), scenGen.getAuctions(), OptimisedBidders);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}