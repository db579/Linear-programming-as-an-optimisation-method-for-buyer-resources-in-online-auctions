package auction.optimisation.stage3;
import java.text.DecimalFormat;
import java.util.ArrayList;
import lpsolve.*;

public class Optimiser {

	private double[] coefficients;
	private DecimalFormat decFor = new DecimalFormat("00.00");
	
	public double[] optimiseAllocation(ArrayList<Auction> Auctions, double budget, Bidder bidder){
		
		String constraint = "";
		String objectiveFunction = "";
		double utility = 0;
		
	    try {
	        // Create a problem with as many variables as there are auctions and 0 constraints
	        LpSolve solver = LpSolve.makeLp(0, Auctions.size());
	        
			for (int i=0; i<Auctions.size(); i++){		//Iterate through the ArrayList of auctions generated by the scenario generator
				Auction auction = Auctions.get(i);
				
				//Set all variables to binary
				solver.setBinary(i+1, true);
				
				//Create string variable to use in constraint of the form: x1 auction.sureBid + x2 auction.sureBid <= budget
				if(auction.getHighBidder() == bidder){
					constraint = constraint+auction.getCurrentPrice()+" ";
					utility = auction.getUtility();
					//Set items currently being won to 1
					solver.setLowbo(i+1,1);
				}
				else{
					if(bidder.checkIfAuctionEnded(auction)){
						solver.setUpbo(i+1, 0);
					}
						constraint = constraint+(auction.getCurrentPrice()+auction.getMinimumBiddingIncrement())+ " "; 
						utility = auction.getUtilityIfBidOn();		
				}
				
				//Create string to use in objective function of the form: Max: x1 auction.utility + x2 auction.utility
				objectiveFunction = objectiveFunction + utility+ " ";
			}
			
	        // Set objective function
	        solver.strSetObjFn(objectiveFunction);
	        
	        //Set the solver to maximise the objective function
	        solver.setMaxim();
	        
	        //Add constraints
	        solver.strAddConstraint(constraint, LpSolve.LE, budget);
	       
	        
	        //Set amount of information provided by solver to only warnings and errors
	        solver.setVerbose(3);
	        
	        //Solve the problem
	        solver.solve();

		      //Print total utility made (i.e. maximised objective function)
	          double buyerUtility = solver.getObjective();
	          buyerUtility = (int)(buyerUtility*100);	
			  buyerUtility = buyerUtility/100;
		      System.out.println("Value of objective function (i.e. the amount of utility the bidder would gain): £" + decFor.format(buyerUtility));
		      //Print whether or not to bid on each item
		      coefficients = solver.getPtrVariables();
		      for (int i = 0; i < coefficients.length; i++) {
		        System.out.println("x"+(i+1)+" = " + coefficients[i]);
		      }  
		      
	        //Delete the problem and free memory
	        solver.deleteLp();
	      }
	      catch (LpSolveException e) {
	         e.printStackTrace();
	      }
	    return coefficients;
	}
}
