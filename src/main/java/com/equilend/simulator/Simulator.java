package com.equilend.simulator;

import java.lang.Thread;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Simulator 
{   
    private static final Logger logger = LogManager.getLogger();
    
    public static void main(String[] args)  
    {
        Configurator configurator = new Configurator();
        User lender = configurator.createLender();
        if (!lender.isValid()){
            logger.fatal("womp womp. Couldn't validate lender user");
            return;
        } 
        User borrower = configurator.createBorrower();
        if (!borrower.isValid()){
            logger.fatal("womp womp. Couldn't validate borrower user");
        }
        logger.info("Lender and borrower both valid!");

        logger.info("DONE :)");

        /*
        int attempts = 0;
        final int MAX_ATTEMPTS = Configurator.getMaxAttempts();
        long intervalInMs = Configurator.getWaitInterval();
        OffsetDateTime since = APIConnector.getCurrentTime();
        OffsetDateTime before;
        while (true){
            waitMillisecs(intervalInMs);
            before = APIConnector.getCurrentTime();
            while (attempts < MAX_ATTEMPTS && (!lender.proposeContractsFromAgreements(since, before, allParties) || !borrower.acceptContractProposals(since, before))){
                //what if some contracts were proposed and some contract proposals were accepted?
                //well, aCP() checks for contracts in the proposed state before sending approve request.
                //what about pCFA() tho? 
                //WELL... trade agreements are supposed to eventually be consumed once a contract is proposed from it. this doesn't happen w my generated shtts tho..
                logger.info("Something didn't work, attempting to propose/accept again");
                attempts++;
                lender.refreshToken();
                borrower.refreshToken();
            }
            if (attempts == MAX_ATTEMPTS){
                logger.fatal("We've tried %d times mate its JUST NOT WORKING", MAX_ATTEMPTS);
                return;
            }
            attempts = 0;
            since = before;
        }
        */
    }
    
    public static void waitMillisecs(Long interval){
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e ){
            e.printStackTrace();
        }
    }

}
