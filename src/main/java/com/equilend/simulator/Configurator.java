package com.equilend.simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Configurator {
    public static Map<String, String> readLoginConfig (String filename) throws FileNotFoundException
    {
        Map<String, String> loginInfo = new HashMap<>();
        try (Scanner scanner = new Scanner(new File(filename))){
            while (scanner.hasNextLine()){
                String line = scanner.nextLine();
                String[] keyValuePair = line.split("=");
                loginInfo.put(keyValuePair[0], keyValuePair[1]);
            }
        } catch (FileNotFoundException e){
            throw new FileNotFoundException(filename + " not found");
        }
        
        return loginInfo;
    }   
}
