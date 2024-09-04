package com.equilend.simulator.configurator;

import com.equilend.simulator.configurator.rules.Rules;
import com.equilend.simulator.configurator.rules.agreement_rules.AgreementRules;
import com.equilend.simulator.configurator.rules.buyin_rules.BuyinRules;
import com.equilend.simulator.configurator.rules.loan_rules.LoanRules;
import com.equilend.simulator.configurator.rules.event_rules.EventRules;
import com.equilend.simulator.configurator.rules.recall_rules.RecallRules;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateRules;
import com.equilend.simulator.configurator.rules.return_rules.ReturnRules;
import com.equilend.simulator.configurator.rules.split_rules.SplitRules;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Parser {

    private static final Logger logger = LogManager.getLogger(Parser.class.getName());

    private static List<String> splitIntoSections(StringBuilder str) {
        List<String> sections = new ArrayList<>();
        int end = 0;
        int start = -1;
        while ((end = str.indexOf("[[", end)) != -1) {
            if (start >= 0) {
                String section = str.substring(start, end - 1);
                sections.add(section);
            }
            start = end++;
        }
        String lastSection = str.substring(start, str.length() - 1);
        sections.add(lastSection);

        return sections;
    }

    private static String getSectionHeader(String section) {
        int start = section.indexOf("[[") + 2;
        int end = section.indexOf("]]");

        return section.substring(start, end);
    }

    private static List<String> splitIntoSubsections(String section) {
        List<String> subsections = new ArrayList<>();

        if (!section.contains("<")) {
            //no lender/borrower 
            int start = section.indexOf("]]") + 3;
            String general = section.substring(start).trim();
            if (!general.isEmpty()) {
                subsections.add(general);
            }
        } else {
            //yes lender/borrower 
            if ((section.indexOf("]]") + 3) != section.indexOf("<")) {
                //yes general section
                int start = section.indexOf("]]") + 2;
                int end = section.indexOf("<");
                String general = section.substring(start, end).trim();
                subsections.add(general);
            }

            int end = 0;
            int start = -1;
            while ((end = section.indexOf("<", end)) != -1) {
                if (start >= 0) {
                    String nonGeneral = section.substring(start, end).trim();
                    subsections.add(nonGeneral);
                }
                start = end++;
            }
            String nonGeneral = section.substring(start).trim();
            subsections.add(nonGeneral);
        }

        return subsections;
    }

    private static String getSubsectionSubheader(String subsection) {
        int end;
        String subheader;

        if ((end = subsection.indexOf(">")) != -1) {
            int start = subsection.indexOf("<") + 1;
            subheader = subsection.substring(start, end);
        } else {
            subheader = "general";
        }

        return subheader;
    }

    private static Map<String, String> getSubsectionRules(String subsection) {
        int idx;
        if ((idx = subsection.indexOf(">")) != -1) {
            subsection = subsection.substring(idx + 1).trim();
        }

        Map<String, String> rules = new HashMap<>();
        String[] lines = subsection.split("\n");
        for (String str : lines) {
            int split = str.indexOf("=");
            String key = str.substring(0, split).trim();
            String value = str.substring(split + 1).trim();
            rules.put(key, value);
        }
        return rules;
    }

    private static Map<String, Map<String, String>> loadSectionRules(String section) {
        Map<String, Map<String, String>> map = new HashMap<>();

        List<String> subsections = splitIntoSubsections(section);
        for (String subsection : subsections) {
            String subheader = getSubsectionSubheader(subsection);
            Map<String, String> rules = getSubsectionRules(subsection);
            map.put(subheader, rules);
        }

        return map;
    }

    public static Map<String, Rules> readRulesFile(Properties properties) {
        String rulesFilename = properties.getProperty("rules_file", "config/rules.txt");
        StringBuilder str = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(rulesFilename))) {
            boolean inList = false;
            String line;
            while ((line = reader.readLine()) != null) {
                str.append(line.trim());
                if (line.contains("{")) {
                    inList = true;
                }
                if (line.contains("}")) {
                    if (inList) {
                        int endParenthesis = str.lastIndexOf(")");
                        str.insert(endParenthesis + 1, ";");
                    }
                    inList = false;
                }
                if (inList) {
                    str.append(";");
                } else {
                    str.append("\n");
                }
            }
        } catch (IOException e) {
            logger.error("Error opening and/or reading rules file", e);
        }

        List<String> sections = splitIntoSections(str);
        Map<String, Rules> rules = new HashMap<>();

        for (String section : sections) {
            String header = getSectionHeader(section);
            Map<String, Map<String, String>> sectionRulesMap = loadSectionRules(section);

            switch (header.toUpperCase()) {
                case "EVENTS":
                    rules.put(header, new EventRules(sectionRulesMap));
                    break;
                case "AGREEMENTS":
                    rules.put(header, new AgreementRules(sectionRulesMap));
                    break;
                case "LOANS":
                    rules.put(header, new LoanRules(sectionRulesMap));
                    break;
                case "RERATES":
                    rules.put(header, new RerateRules(sectionRulesMap));
                    break;
                case "RETURNS":
                    rules.put(header, new ReturnRules(sectionRulesMap));
                    break;
                case "RECALLS":
                    rules.put(header, new RecallRules(sectionRulesMap));
                    break;
                case "BUYINS":
                    rules.put(header, new BuyinRules(sectionRulesMap));
                    break;
                case "SPLITS":
                    rules.put(header, new SplitRules(sectionRulesMap));
                    break;
                default:
                    logger.error("Error reading rules file, unrecognized section header");
            }
        }

        return rules;
    }

}