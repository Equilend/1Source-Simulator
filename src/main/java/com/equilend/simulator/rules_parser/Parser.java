package com.equilend.simulator.rules_parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Parser {

    private static final Logger logger = LogManager.getLogger();

    private static List<String> splitIntoSubsections(String section){
        List<String> subsections = new ArrayList<>();

        if (section.indexOf('<', 0) == -1){
            //no lender/borrower 
            int start = section.indexOf(']', 0) + 2;
            String general = section.substring(start, section.length()).trim();
            subsections.add(general);
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

    public static void readRulesFile(){
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
        System.out.println("======================");
        for (int i = 0; i < sections.size(); i++){
            // System.out.println(sections.get(i));
            List<String> subsections = splitIntoSubsections(sections.get(i));
            System.out.println("- - - - - - - - - - -");
            for (int j = 0; j < subsections.size(); j++){
                System.out.println(subsections.get(j));
                System.out.println("- - - - - - - - - - -");
            }
            System.out.println("======================");
        }
    }

}