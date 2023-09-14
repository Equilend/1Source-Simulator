package com.equilend.simulator.configurator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.contract.Contract;
import com.equilend.simulator.rules_parser.Parser;
import com.equilend.simulator.trade.Trade;
import com.equilend.simulator.trade.instrument.Instrument;
import com.equilend.simulator.trade.transacting_party.PartyRole;
import com.equilend.simulator.trade.transacting_party.TransactingParty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;

public class Configurator {

    private String configFilename = "config/config.toml";
    private PartyRole mode;
    private Map<String, String> loginMap;
    private String clientPartyId;
    private long waitIntervalMillis = 10 * 1000;
    private int maxAttempts = 3;
    private List<Instrument> instruments = null;
    private static final Logger logger = LogManager.getLogger();
    
    public Configurator() {
        readTOMLFile();
        readInstrumentsFile();
        Parser.readRulesFile();
    }

    private void readTOMLFile() {
        TomlMapper tomlMapper = new TomlMapper();
        Map<String, Map<String, String>> settings = null;
        try {
            settings = tomlMapper.readValue(new File(this.configFilename), new TypeReference<Map<String, Map<String, String>>>() {});
        } catch (IOException e) {
            logger.error("Error reading config TOML file", e);
            return;
        }
        if (settings == null){
            logger.error("TOML file mapper unable to be created");
        }

        this.mode = PartyRole.valueOf(settings.get("bot").get("mode"));
        this.loginMap = (this.mode == PartyRole.LENDER) ? settings.get("lender_bot_login") : settings.get("borrower_bot_login");
        Map<String, String> general = settings.get("general");
        this.clientPartyId = general.get("your_party_id");
        this.waitIntervalMillis = Long.parseLong(general.get("wait_interval_ms"));
        this.maxAttempts = Integer.parseInt(general.get("max_refresh_attempts"));
    }

    private void readInstrumentsFile() {
        String instrumentsFilename = "config/instruments.toml";
        TomlMapper tomlMapper = new TomlMapper();
        Map<String, List<Instrument>> map = null;
        try {
            map = tomlMapper.readValue(new File(instrumentsFilename), new TypeReference<Map<String, List<Instrument>>>() {});
        } catch(IOException e){
            logger.error("Error reading instruments file", e);
            return;
        }
        if (map == null){
            logger.error("Instruments unable to be successfully loaded");
        }

        this.instruments = map.get("instruments");
    }

    public PartyRole getMode() {
        return mode;
    }

    public Map<String, String> getLoginMap() { 
        return loginMap;
    }

    public String getClientPartyId() {
        return clientPartyId;
    }
    
    public Long getWaitIntervalMillis() {
        return waitIntervalMillis;
    }
        
    public int getMaxAttempts() {
        return maxAttempts;
    }

    public List<Instrument> getInstruments() {
        return instruments;
    }

    public void setInstruments(List<Instrument> instruments) {
        this.instruments = instruments;
    }    

    public boolean correctPartner(Trade trade) {
        List<TransactingParty> parties = trade.getTransactingParties();
        for (TransactingParty tp : parties){
            if (tp.getPartyRole() != mode && !tp.getParty().getPartyId().equals(clientPartyId)){
                return false;
            }
        }
        return true;
    }

    public boolean actOnTrade(Trade trade) {
        //if trade not between bot and clientPartyId... CHOPT
        if (!correctPartner(trade)) return false;
        
        //Determine whether you should propose a contract from the trade or not
        return true;
    }

    public boolean ignoreProposal(Contract contract) {
        //If proposal not from clientPartyId... CHOPT
        if (!correctPartner(contract.getTrade())) return false;

        //Either ignore contract proposal
        //Or Accept/Decline it
        return false;
    }

    public boolean shouldAcceptProposal(Contract contract) {
        //Determine whether you should accept or decline contract proposal
        return true;
    }

}