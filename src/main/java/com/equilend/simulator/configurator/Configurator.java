package com.equilend.simulator.configurator;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.DatalendAPIConnector;
import com.equilend.simulator.auth.DatalendToken;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.configurator.rules.Rules;
import com.equilend.simulator.configurator.rules.agreement_rules.AgreementRules;
import com.equilend.simulator.configurator.rules.buyin_rules.BuyinRules;
import com.equilend.simulator.configurator.rules.contract_rules.ContractRules;
import com.equilend.simulator.configurator.rules.event_rules.EventRules;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateRules;
import com.equilend.simulator.configurator.rules.return_rules.ReturnRules;
import com.equilend.simulator.model.instrument.Instrument;
import com.equilend.simulator.model.party.Party;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Configurator {

    private static final Logger logger = LogManager.getLogger(Configurator.class.getName());
    private final Map<String, Party> parties = new HashMap<>();
    private final Map<String, Instrument> instruments = new HashMap<>();
    private EventRules eventRules;
    private AgreementRules agreementRules;
    private ContractRules contractRules;
    private RerateRules rerateRules;
    private ReturnRules returnRules;
    private BuyinRules buyinRules;
    private Properties properties;

    public Configurator(Properties props) {
        properties = props;
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

        loadRules(Parser.readRulesFile(properties));

        OneSourceToken.configureToken(get1SourceKeycloakLoginInfo(), properties.getProperty("1source.keycloak.url"));
        APIConnector.setRestAPIURL(properties.getProperty("1source.api_url"));

        DatalendToken.configureToken(getDatalendKeycloakLoginInfo(), properties.getProperty("datalend.keycloak.url"));
        DatalendAPIConnector.setRestAPIURL(properties.getProperty("datalend.api_url"));
    }

    private Map<String, String> get1SourceKeycloakLoginInfo() {
        Map<String, String> keycloakLoginInfo = new HashMap<>();
        keycloakLoginInfo.put("client_id", properties.getProperty("1source.keycloak.client_id"));
        keycloakLoginInfo.put("client_secret", properties.getProperty("1source.keycloak.client_secret"));
        keycloakLoginInfo.put("grant_type", properties.getProperty("1source.keycloak.grant_type"));
        keycloakLoginInfo.put("username", properties.getProperty("1source.keycloak.username"));
        keycloakLoginInfo.put("password", properties.getProperty("1source.keycloak.password"));
        return keycloakLoginInfo;
    }

    private Map<String, String> getDatalendKeycloakLoginInfo() {
        Map<String, String> keycloakLoginInfo = new HashMap<>();
        keycloakLoginInfo.put("client_id", properties.getProperty("datalend.keycloak.client_id"));
        keycloakLoginInfo.put("client_secret", properties.getProperty("datalend.keycloak.client_secret"));
        keycloakLoginInfo.put("grant_type", properties.getProperty("datalend.keycloak.grant_type"));
        keycloakLoginInfo.put("username", properties.getProperty("datalend.keycloak.username"));
        keycloakLoginInfo.put("password", properties.getProperty("datalend.keycloak.password"));
        return keycloakLoginInfo;
    }

    private List<Party> loadPartiesTomlFile() {
        String filename = properties.getProperty("parties_toml_file", "config/parties.toml");
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
        String filename = properties.getProperty("instruments_toml_file", "config/instruments.toml");
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
                case "RETURNS":
                    returnRules = (ReturnRules) rules.get(section);
                    break;
                case "BUYINS":
                    buyinRules = (BuyinRules) rules.get(section);
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

    public ReturnRules getReturnRules() {
        return returnRules;
    }

    public BuyinRules getBuyinRules() {
        return buyinRules;
    }

    public String getBotPartyId() {
        return properties.getProperty("bot_party_id");
    }

    public long getEventFetchIntervalMillis() {
        return Long.parseLong(properties.getProperty("event_fetch_interval_secs"))*1000;
    }
}