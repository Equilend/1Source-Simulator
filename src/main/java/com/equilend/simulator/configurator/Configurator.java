package com.equilend.simulator.configurator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.rules.AgreementRules;
import com.equilend.simulator.rules.AuthorizationRules;
import com.equilend.simulator.rules.ContractRules;
import com.equilend.simulator.rules.EventRules;
import com.equilend.simulator.rules.GeneralRules;
import com.equilend.simulator.rules.Parser;
import com.equilend.simulator.rules.Rules;
import com.equilend.simulator.trade.instrument.Instrument;
import com.equilend.simulator.trade.transacting_party.Party;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;

public class Configurator {

    private List<Party> partiesList = null;
    private Map<String, Party> parties = new HashMap<>();
    private List<Instrument> instrumentsList = null;
    private Map<String, Instrument> instruments = new HashMap<>();
    private GeneralRules generalRules;
    private AuthorizationRules authorizationRules;
    private EventRules eventRules;
    private AgreementRules agreementRules;
    private ContractRules contractRules;
    private static final Logger logger = LogManager.getLogger();
    
    public Configurator() {
        this.partiesList = loadPartiesTomlFile();
        partiesList.forEach(p -> parties.put(p.getPartyId(), p));
        
        this.instrumentsList = loadInstrumentsTomlFile();
        instrumentsList.forEach(i -> instruments.put(i.getTicker(), i));

        loadRules(Parser.readRulesFile());
    }

    private List<Instrument> loadInstrumentsTomlFile(){
        String filename = "config/instruments.toml";
        TomlMapper tomlMapper = new TomlMapper();
        Map<String, List<Instrument>> map = null;
        try {
            map = tomlMapper.readValue(new File(filename), new TypeReference<Map<String, List<Instrument>>>() {});
        } catch (IOException e){
            logger.error("Error reading instruments file", e);
            return null;
        }
        if (map == null){
            logger.error("Instruments unable to be successfully loaded");
        }

        return map.get("instruments");
    }

    private List<Party> loadPartiesTomlFile(){
        String filename = "config/parties.toml";
        TomlMapper tomlMapper = new TomlMapper();
        Map<String, List<Party>> map = null;
        try {
            map = tomlMapper.readValue(new File(filename), new TypeReference<Map<String, List<Party>>>() {});
        } catch (IOException e){
            logger.error("Error reading parties file", e);
            return null;
        }
        if (map == null){
            logger.error("Parties unable to be successfully loaded");
        }

        return map.get("parties");
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
                case "CONTRACTS":
                    contractRules = (ContractRules) rules.get(section);
                    break;
                default:
                    logger.error("Unrecognized rules section header");
            }
        }
    }

    public Map<String, Party> getParties() {
        return parties;
    }

    public Map<String, Instrument> getInstruments() {
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

    public AgreementRules getAgreementRules(){
        return agreementRules;
    }

    public ContractRules getContractRules(){
        return contractRules;
    }

}