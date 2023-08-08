package com.equilend.simulator;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.Thread;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.Map;

import com.equilend.simulator.Trade.TransactingParty.PartyRole;

public class Simulator 
{
    private static String allParties = "*";

    public static void main(String[] args) throws FileNotFoundException, TokenException 
    {
        OffsetDateTime since = APIConnector.getCurrentTime();

        String lenderFilename = "src/main/java/com/equilend/simulator/lender_config.txt";
        Map<String, String> lenderLoginInfo = Configurator.readLoginConfig(lenderFilename);
        User lender = new User(lenderLoginInfo, PartyRole.LENDER);

        String borrowerFilename = "src/main/java/com/equilend/simulator/borrower_config.txt";
        Map<String, String> borrowerLoginInfo = Configurator.readLoginConfig(borrowerFilename);
        User borrower = new User(borrowerLoginInfo, PartyRole.BORROWER);

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
