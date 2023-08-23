package com.equilend.simulator.Configurator;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.Contract;
import com.equilend.simulator.Trade.Trade;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;

public class Configurator {
    private static final Logger logger = LogManager.getLogger();
    
    //Configurator should probably look for config file itself..
    private String configFilename = "src/main/java/com/equilend/simulator/config/config.toml";
    
    private Map<String, String> loginMap;

    private Mode mode;

    //max attempts should be read from config file
    private int maxAttempts = 3;
    private long waitIntervalInMs = 10 * 1000;
    
    public Configurator(){
        readLoginConfig();
    }

    public void readLoginConfig() 
    {
        TomlMapper tomlMapper = new TomlMapper();
        Map<String, Map<String, String>> data = null;
        try {
            data = tomlMapper.readValue(new File(this.configFilename), new TypeReference<Map<String, Map<String, String>>>() {});
        } catch (IOException e) {
            logger.error("Error reading config TOML file", e);
            return;
        }

        if (data == null){
            return;
        } 

        this.mode = Mode.valueOf(data.get("bot").get("mode"));
        this.loginMap = data.get("login");
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Map<String, String> getLoginMap(){
        return loginMap;
    }

    public int getMaxAttempts(){
        return maxAttempts;
    }

    public long getWaitInterval(){
        return waitIntervalInMs;
    }

    public boolean actOnTrade(Trade trade){
        //Determine whether you should propose a contract from the trade or not
        return true;
    }

    public boolean ignoreProposal(Contract contract){
        //Either ignore contract proposal
        //Or Accept/Decline it
        return false;
    }

    public boolean shouldAcceptProposal(Contract contract){
        //Determine whether you should accept or decline contract proposal
        return true;
    }

}


