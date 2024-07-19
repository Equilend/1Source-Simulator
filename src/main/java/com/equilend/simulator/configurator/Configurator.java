package com.equilend.simulator.configurator;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.DatalendAPIConnector;
import com.equilend.simulator.auth.DatalendToken;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.configurator.rules.AuthorizationRules;
import com.equilend.simulator.configurator.rules.GeneralRules;
import com.equilend.simulator.configurator.rules.Rules;
import com.equilend.simulator.configurator.rules.agreement_rules.AgreementRules;
import com.equilend.simulator.configurator.rules.contract_rules.ContractRules;
import com.equilend.simulator.configurator.rules.event_rules.EventRules;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateRules;
import com.equilend.simulator.model.party.Party;
import com.equilend.simulator.model.instrument.Instrument;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Configurator {

    private final Map<String, Party> parties = new HashMap<>();
    private final Map<String, Instrument> instruments = new HashMap<>();
    private GeneralRules generalRules;
    private AuthorizationRules authorizationRules;
    private EventRules eventRules;
    private AgreementRules agreementRules;
    private ContractRules contractRules;
    private RerateRules rerateRules;
    private static final Logger logger = LogManager.getLogger();

    public Configurator() {
        List<Party> partiesList = loadPartiesTomlFile();

        try {
            assert partiesList != null;
            partiesList.forEach(p -> parties.put(p.getPartyId(), p));
        } catch (NullPointerException npe) {
            logger.error("Null Pointer Exception reading parties from the Parties TOML file: " + npe.getMessage());
        }

        List<Instrument> instrumentsList = loadInstrumentsTomlFile();

        try {
            assert instrumentsList != null;
            instrumentsList.forEach(i -> instruments.put(i.getTicker(), i));
        } catch (NullPointerException npe) {
            logger.error(
                "Null Pointer Exception reading instruments from the Instrument TOML file: " + npe.getMessage());
        }

        loadRules(Parser.readRulesFile());

        OneSourceToken.configureToken(authorizationRules.getOneSource(), generalRules.getOneSourceKeycloakURL());
        APIConnector.setRestAPIURL(generalRules.getOneSourceAPIURL());

        DatalendToken.configureToken(authorizationRules.getDatalend(), generalRules.getDatalendKeycloakURL());
        DatalendAPIConnector.setRestAPIURL(generalRules.getDatalendAPIURL());
    }

    private List<Party> loadPartiesTomlFile() {
        String filename = "config/parties.toml";
        TomlMapper tomlMapper = new TomlMapper();
        Map<String, List<Party>> map;

        try {
            map = tomlMapper.readValue(new File(filename), new TypeReference<>() {
            });
        } catch (IOException e) {
            logger.error("Error reading parties file", e);
            return null;
        }
        if (map == null) {
            logger.error("Parties unable to be successfully loaded");
        }

        assert map != null;
        return map.get("parties");
    }

    private List<Instrument> loadInstrumentsTomlFile() {
        String filename = "config/instruments.toml";
        TomlMapper tomlMapper = new TomlMapper();
        Map<String, List<Instrument>> map;
        try {
            map = tomlMapper.readValue(new File(filename), new TypeReference<>() {
            });
        } catch (IOException e) {
            logger.error("Error reading instruments file", e);
            return null;
        }
        if (map == null) {
            logger.error("Instruments unable to be successfully loaded");
        }

        assert map != null;
        return map.get("instruments");
    }

    private void loadRules(Map<String, Rules> rules) {
        for (String section : rules.keySet()) {
            switch (section.toUpperCase()) {
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
                case "RERATES":
                    rerateRules = (RerateRules) rules.get(section);
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

    public EventRules getEventRules() {
        return eventRules;
    }

    public AgreementRules getAgreementRules() {
        return agreementRules;
    }

    public ContractRules getContractRules() {
        return contractRules;
    }

    public RerateRules getRerateRules() {
        return rerateRules;
    }

}