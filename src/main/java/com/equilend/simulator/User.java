package com.equilend.simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.equilend.simulator.Trade.TransactingParty.PartyRole;

public class User {
    protected Map<String, String> loginInfo;
    protected String configFileName;
    PartyRole role;
    PartyRole counterRole;
    Token token;

    public User(String fn, PartyRole r) throws URISyntaxException, IOException, InterruptedException{
        this.configFileName = fn;
        this.loginInfo = readFormData(configFileName);
        this.role = r;
        this.counterRole = (r == PartyRole.LENDER) ? PartyRole.BORROWER : PartyRole.LENDER;
        this.token = APIConnector.getBearerToken(loginInfo);
    }
    
    void refreshToken() throws URISyntaxException, IOException, InterruptedException{
        this.token = APIConnector.getBearerToken(loginInfo);
    }

    public Map<String, String> readFormData (String filename){
        loginInfo = new HashMap<>();
        try{
                Scanner scanner = new Scanner(new File(filename));
                while (scanner.hasNextLine()){
                    String line = scanner.nextLine();
                    String[] keyValuePair = line.split("=");
                    loginInfo.put(keyValuePair[0], keyValuePair[1]);
                }
                scanner.close();
        } catch (FileNotFoundException e){
            System.out.println(filename + " not found");
        }
        return loginInfo;
    }

    // TODO: Propose Contract From Trade
    
    // TODO: Get All Agreements Since X
    
    // TODO: Propose Contracts From All Agreements Since X
    
    // TODO: Propose Contracts From All Agreements Since X From

    // TODO: Get All Agreements Today

    // TODO: Propose Contracts From All Agreements Today

    // TODO: Propose Contracts From All Agreements Today From

    // TODO: Cancel Contract

    // TODO: Accept Contract

    // TODO: Decline Contract

}
