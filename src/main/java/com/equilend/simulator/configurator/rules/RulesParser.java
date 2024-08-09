package com.equilend.simulator.configurator.rules;

import java.util.Set;

public class RulesParser {

    public static Set<String> parseLogicalOr(String exp) {
        String[] arr = exp.split("\\|");
        return Set.of(arr);
    }

}
