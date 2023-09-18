package com.equilend.simulator.configurator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.contract.Contract;
import com.equilend.simulator.rules.AgreementRules;
import com.equilend.simulator.rules.AuthorizationRules;
import com.equilend.simulator.rules.EventRules;
import com.equilend.simulator.rules.GeneralRules;
import com.equilend.simulator.rules.Parser;
import com.equilend.simulator.rules.Rules;
import com.equilend.simulator.trade.Trade;
import com.equilend.simulator.trade.instrument.Instrument;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;

public class Configurator {

    private List<Instrument> instruments = null;
    private GeneralRules generalRules;
    private AuthorizationRules authorizationRules;
    private EventRules eventRules;
    private AgreementRules agreementRules;
    private static final Logger logger = LogManager.getLogger();
    
    public Configurator() {
        readInstrumentsFile();
        loadRules(Parser.readRulesFile());
    }

    private void loadRules(Map<String, Rules> rules) {
        for (String section : rules.keySet()){
            switch (section.toUpperCase()){
                case "GENERAL":
                    generalRules = (GeneralRules) rules.get(section);
                    break;
                case "AUTH":
                    authorizationRules = (AuthorizationRules) rules.get(section);
                    break;
                case "EVENTS":
                    eventRules = (EventRules) rules.get(section);
                    break;
                case "AGREEMENTS":
                    agreementRules = (AgreementRules) rules.get(section);
                    break;                    
                default:
                    logger.error("Unrecognized rules section header");
            }
        }
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

    public List<Instrument> getInstruments() {
        return instruments;
    }

    public GeneralRules getGeneralRules() {
        return generalRules;
    }

    public AuthorizationRules getAuthorizationRules() {
        return authorizationRules;
    }

    public EventRules getEventRules(){
        return eventRules;
    }

    public boolean shouldIgnoreEvent(String eventType){
        //Determine whether you should process or 
        return true;
    }

    public boolean shouldIgnoreTrade(Trade trade) {
        //Determine whether you should propose a contract from the trade or not
        return true;
    }

    public boolean shouldIgnoreContract(Contract contract) {
        //Either ignore contract proposal

        //Or Accept/Decline it
        return false;
    }

    public boolean shouldAcceptProposal(Contract contract) {
        //Determine whether you should accept or decline contract proposal
        return true;
    }

}