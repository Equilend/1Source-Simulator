package com.equilend.simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.equilend.simulator.Trade.TransactingParty.PartyRole;

public class Configurator {
    //Configurator should probably look for config file itself..
    private static String lenderFilename = "src/main/java/com/equilend/simulator/config/lender_config.txt";
    private static String borrowerFilename = "src/main/java/com/equilend/simulator/config/borrower_config.txt";
    //max attempts should be read from config file
    private static int maxAttempts = 3;

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
            System.out.println("File not found: " + filename);
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
}
