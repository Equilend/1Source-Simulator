package com.equilend.simulator;

import java.lang.Thread;
import java.time.OffsetDateTime;


public class Simulator 
{   
    private static String allParties = "*";
    private static final int MAX_ATTEMPTS = 3;
    public static void main(String[] args)  
    {
        
        User lender = Configurator.createLender();
        assert(lender.isValid()); 
        User borrower = Configurator.createBorrower();
        assert(borrower.isValid());
        
        OffsetDateTime since = APIConnector.getCurrentTime();
        OffsetDateTime before;
        int attempts = 0;
        while (true){
            waitMillisecs(5000L);
            before = APIConnector.getCurrentTime();

            while (attempts < MAX_ATTEMPTS && (!lender.proposeContractsFromAgreements(since, before, allParties) || !borrower.acceptContractProposals(since, before))){
                //what if some contracts were proposed and some contract proposals were accepted?
                //well, aCP() checks for contracts in the proposed state before sending approve request.
                //what about pCFA() tho? 
                //WELL... trade agreements are supposed to eventually be consumed once a contract is proposed from it. this doesn't happen w my generated shtts tho..

                attempts++;
                lender.refreshToken();
                borrower.refreshToken();
            }
            if (attempts == MAX_ATTEMPTS){
                System.out.println("Not working mate. Something must be up");
                return;
            }
            attempts = 0;
            since = before;

        }
    }
    
    public static void waitMillisecs(Long interval){
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e ){
            e.printStackTrace();
        }
    }

}
