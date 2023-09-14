package com.equilend.simulator.rules;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Parser {

    private static final Logger logger = LogManager.getLogger();

    private static List<String> splitIntoSections(StringBuilder str){
        List<String> sections = new ArrayList<>();
        int end = 0;
        int start = -1;
        while ((end = str.indexOf("[", end)) != -1){
            if (start >= 0){
                String section = str.substring(start, end-1);
                sections.add(section);
            }
            start = end++;
        }
        String lastSection = str.substring(start, str.length()-1);
        sections.add(lastSection);

        return sections;
    }

    private static String getSectionHeader(String section){
        int start = section.indexOf('[') + 1;
        int end = section.indexOf(']');

        return section.substring(start, end);
    }

    private static List<String> splitIntoSubsections(String section){
        List<String> subsections = new ArrayList<>();

        if (section.indexOf('<', 0) == -1){
            //no lender/borrower 
            int start = section.indexOf(']', 0) + 2;
            String general = section.substring(start, section.length()).trim();
            if (general.length() > 0) subsections.add(general);
        }
        else {
            //yes lender/borrower 
            if ((section.indexOf(']') + 2) != section.indexOf('<', 0)){
                //yes general section
                int start = section.indexOf(']') + 2;
                int end = section.indexOf('<', 0);
                String general = section.substring(start, end).trim();
                subsections.add(general);
            }

            int end = 0;
            int start = -1;
            while ((end = section.indexOf("<", end)) != -1){
                if (start >= 0){
                    String nonGeneral = section.substring(start, end).trim();
                    subsections.add(nonGeneral);
                }
                start = end++;
            }
            String nonGeneral = section.substring(start, section.length()).trim();
            subsections.add(nonGeneral);
        }

        return subsections;
    }

    private static String getSubsectionSubheader(String subsection){
        int end = 0;
        String subheader;
        
        if ( (end = subsection.indexOf('>')) != -1){
            int start = subsection.indexOf('<') + 1;
            subheader = subsection.substring(start, end);
        }else{
            subheader = "general";
        }
        
        return subheader;
    }

    private static Map<String, String> getSubsectionRules(String subsection){
        int idx = 0;
        if ( (idx = subsection.indexOf('>')) != -1){
            subsection = subsection.substring(idx+1).trim();
        }

        Map<String, String> rules = new HashMap<>();
        String[] lines = subsection.split("\n");
        for (String str : lines){
            String[] parts = str.split("=");
            rules.put(parts[0].trim(), parts[1].trim());
        }
        return rules;
    }

    private static Map<String, Map<String, String>> loadSectionRules(String section){
        Map<String, Map<String, String>> map = new HashMap<>();

        List<String> subsections = splitIntoSubsections(section);
        for (String subsection : subsections){
            String subheader = getSubsectionSubheader(subsection);
            Map<String, String> rules = getSubsectionRules(subsection);
            map.put(subheader, rules);
        }

        return map;
    }

    public static Map<String, Rules> readRulesFile(){
        String rulesFilename = "config/rules.txt";
        StringBuilder str = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(rulesFilename))) {
            String line;
            while ((line = reader.readLine()) != null){
                str.append(line.trim());
                if (line.length() > 0) str.append("\n");
            }
        } catch (IOException e) {
            logger.error("Error reading rules file", e);
        }

        List<String> sections = splitIntoSections(str);
        Map<String, Rules> rules = new HashMap<>();
        for (int i = 0; i < sections.size(); i++){
            String section = sections.get(i);
            String header = getSectionHeader(section);
            Map<String, Map<String, String>> sectionRulesMap = loadSectionRules(section);
            
            switch (header){
                case "General" :
                    rules.put(header, new GeneralRules(sectionRulesMap));
                case "Auth" :
                    rules.put(header, new AuthorizationRules(sectionRulesMap));
            }
        }

        return rules;
    }

}