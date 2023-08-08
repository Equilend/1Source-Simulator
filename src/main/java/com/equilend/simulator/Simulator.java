package com.equilend.simulator;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.Thread;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;

import com.equilend.simulator.Trade.TransactingParty.PartyRole;

public class Simulator 
{
    private static String allParties = "*";

    public static void main(String[] args) 
    {
        OffsetDateTime since = APIConnector.getCurrentTime();

        String lenderFilename = "src/main/java/com/equilend/simulator/lender_config.txt";
        User lender;
        try {
            lender = new User(lenderFilename, PartyRole.LENDER);
        } catch (FileNotFoundException | TokenException e) {
            e.printStackTrace();
            return;
        }

        String borrowerFilename = "src/main/java/com/equilend/simulator/borrower_config.txt";
        User borrower;
        try {
            borrower = new User(borrowerFilename, PartyRole.BORROWER);
        } catch (FileNotFoundException | TokenException e) {
            e.printStackTrace();
            return;
        }

        while (true){
            waitMillisecs(5000L);
            OffsetDateTime before = APIConnector.getCurrentTime();
            try {
                lender.proposeContractsFromAgreements(since, before, allParties);
            } catch (URISyntaxException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
            try {
                borrower.acceptContractProposals(since, before);
            } catch (URISyntaxException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
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
