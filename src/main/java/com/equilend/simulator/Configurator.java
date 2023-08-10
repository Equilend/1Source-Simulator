package com.equilend.simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.Trade.TransactingParty.PartyRole;

public class Configurator {
    private static final Logger logger = LogManager.getLogger();
    
    //Configurator should probably look for config file itself..
    private static String lenderFilename = "src/main/java/com/equilend/simulator/config/lender_config.txt";
    private static String borrowerFilename = "src/main/java/com/equilend/simulator/config/borrower_config.txt";
    //max attempts should be read from config file
    private static int maxAttempts = 3;
    private static long waitIntervalInMs = 5000;

    // TODO: Validate the login info file instead of blindly creating hash map
    private static Map<String, String> readLoginConfig (String filename) 
    {
        Map<String, String> loginInfo = new HashMap<>();
        try (Scanner scanner = new Scanner(new File(filename))){
            while (scanner.hasNextLine()){
                String line = scanner.nextLine();
                String[] keyValuePair = line.split("=");
                loginInfo.put(keyValuePair[0], keyValuePair[1]);
            }
        } catch (FileNotFoundException e){
            String message = "File not found: " + filename;
            logger.error(message, e);
        }
        
        return loginInfo;
    }   

    public static User createLender(){
        User lender = new User(readLoginConfig(lenderFilename), PartyRole.LENDER);
        return lender;
    }

    public static User createBorrower(){
        User borrower = new User(readLoginConfig(borrowerFilename), PartyRole.BORROWER);
        return borrower;
    }

    public static int getMaxAttempts(){
        return maxAttempts;
    }

    public static long getWaitInterval(){
        return waitIntervalInMs;
    }
}


