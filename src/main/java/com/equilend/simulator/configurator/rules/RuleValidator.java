package com.equilend.simulator.configurator.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.api.DatalendAPIConnector;
import com.equilend.simulator.auth.DatalendToken;
import com.os.client.model.Instrument;

public class RuleValidator {

    private static final Logger logger = LogManager.getLogger(RuleValidator.class.getName());


    public static List<String> parseRule(String ruleStr) {
        List<Integer> commaIdxs = new ArrayList<>();
        boolean withinRange = false;

        for (int i = 1; i < ruleStr.length() - 1; i++) {
            if (!withinRange && ruleStr.charAt(i) == ',') {
                commaIdxs.add(i);
            } else if (ruleStr.charAt(i) == '(' || ruleStr.charAt(i) == '[') {
                withinRange = true;
            } else if (ruleStr.charAt(i) == ')' || ruleStr.charAt(i) == ']') {
                withinRange = false;
            }
        }
        commaIdxs.add(ruleStr.length() - 1);

        List<String> args = new ArrayList<>();
        int start = 1;
        for (int idx : commaIdxs) {
            int colon = ruleStr.indexOf(":", start);
            if (colon >= 0 && start < idx) {
                start = colon + 1;
            }
            String arg = ruleStr.substring(start, idx).trim();
            args.add(arg);
            start = idx + 1;
        }

        return args;
    }

    public static boolean validCounterparty(Set<String> counterparties, String counterparty) {
        return counterparties.contains("*") || counterparties.contains(counterparty);
    }

    public static boolean validSecurity(Set<String> securities, Instrument security) {
        if (securities.contains("*")) {
            return true;
        }

        for (String id : securities) {
            int bang = id.indexOf("!");
            String idType = bang == -1 ? "T" : id.substring(0, bang).trim();
            String idValue = bang == -1 ? id : id.substring(bang + 1).trim();
            idValue = idValue.trim().toUpperCase();
            switch (idType.toUpperCase().charAt(0)) {
                case 'F':
                    if (idValue.equals(security.getFigi())) {
                        return true;
                    }
                    break;
                case 'I':
                    if (idValue.equals(security.getIsin())) {
                        return true;
                    }
                    break;
                case 'S':
                    if (idValue.equals(security.getSedol())) {
                        return true;
                    }
                    break;
                case 'C':
                    if (idValue.equals(security.getCusip())) {
                        return true;
                    }
                    break;
                default:
                    if (idValue.equals(security.getTicker())) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    private static boolean validQuantityHelper(String quantityRange, long quantity) {
        if (quantity <= 0) {
            return false;
        }

        if (quantityRange.equals("*")) {
            return true;
        }

        int delim = quantityRange.indexOf(",");
        if (delim == -1) {
            return false;
        }

        String lowerStr = quantityRange.substring(1, delim).trim();
        String upperStr = quantityRange.substring(delim + 1, quantityRange.length() - 1).trim().toUpperCase();

        boolean lowerInclusive = quantityRange.charAt(0) == '[';
        boolean upperInclusive = quantityRange.charAt(quantityRange.length() - 1) == ']';

        long lower = Long.parseLong(lowerStr);
        long upper = (upperStr.equals("INF")) ? Long.MAX_VALUE : Long.parseLong(upperStr);

        return (lowerInclusive && quantity >= lower || !lowerInclusive && quantity > lower)
            && (upperInclusive && quantity <= upper || !upperInclusive && quantity < upper);
    }

    public static boolean validQuantity(Set<String> quantities, long quantity) {
        for (String quantityRange : quantities) {
            if (validQuantityHelper(quantityRange, quantity)) {
                return true;
            }
        }
        return false;
    }

    private static boolean validRateHelper(String rateRange, double rate, String sedolValue, boolean rebate) {
        if (rateRange.equals("*")) {
            return true;
        }

        int delim = rateRange.indexOf(",");
        if (delim == -1) {
            return false;
        }

        String lowerStr = rateRange.substring(1, delim).trim().toUpperCase();
        String upperStr = rateRange.substring(delim + 1, rateRange.length() - 1).trim().toUpperCase();

        boolean lowerInclusive = rateRange.charAt(0) == '[';
        boolean upperInclusive = rateRange.charAt(rateRange.length() - 1) == ']';

        double lower = Double.MIN_VALUE;
        if (lowerStr.equals("AVG")) {
            if (rebate) {
                try {
                    lower =
                        DatalendAPIConnector.getSecurityRebate(DatalendToken.getToken(), "sedol", sedolValue) / 100.0;
                } catch (APIException e) {
                    logger.debug("Unable to get avg rebate of sedol {}, defaulting to 5.00 effective rate", sedolValue);
                }
            } else {
                try {
                    lower = DatalendAPIConnector.getSecurityFee(DatalendToken.getToken(), "sedol", sedolValue) / 100.0;
                } catch (APIException e) {
                    logger.debug("Unable to get avg fee of sedol {}, defaulting to 5.00 effective rate", sedolValue);
                }
            }
        } else if (!lowerStr.equals("-INF")) {
            lower = Double.parseDouble(lowerStr);
        }

        double upper = Double.MAX_VALUE;
        if (upperStr.equals("AVG")) {
            if (rebate) {
                try {
                    upper =
                        DatalendAPIConnector.getSecurityRebate(DatalendToken.getToken(), "sedol", sedolValue) / 100.0;
                    logger.debug("Avg rebate of sedol {} is {} bps or {}%", sedolValue, upper * 100, upper);
                } catch (APIException e) {
                    logger.debug("Unable to get avg rebate of sedol {}, defaulting to 5.00 effective rate", sedolValue);
                }
            } else {
                try {
                    upper = DatalendAPIConnector.getSecurityFee(DatalendToken.getToken(), "sedol", sedolValue) / 100.0;
                    logger.debug("Avg fee of sedol {} is {} bps or {}%", sedolValue, upper * 100, upper);
                } catch (APIException e) {
                    logger.debug("Unable to get avg fee of sedol {}, defaulting to 5.00 effective rate", sedolValue);
                }
            }
        } else if (!upperStr.equals("INF")) {
            upper = Double.parseDouble(upperStr);
        }

        return (lowerInclusive && rate >= lower || !lowerInclusive && rate > lower)
            && (upperInclusive && rate <= upper || !upperInclusive && rate < upper);

    }

    public static boolean validRate(Set<String> rates, double rate, String sedolValue, boolean rebate) {
        for (String rateRange : rates) {
            if (validRateHelper(rateRange, rate, sedolValue, rebate)) {
                return true;
            }
        }
        return false;
    }

    public static boolean validDouble(String ruleRange, Double value) {
        if (ruleRange.equals("*")) {
            return true;
        }
        int delim = ruleRange.indexOf(",");
        if (delim >= 0) {
            String lowerStr = ruleRange.substring(1, delim).trim();
            String upperStr = ruleRange.substring(delim + 1, ruleRange.length() - 1).trim().toUpperCase();

            boolean lowerInclusive = ruleRange.charAt(0) == '[';
            boolean upperInclusive = ruleRange.charAt(ruleRange.length() - 1) == ']';

            Double lower = (upperStr.equals("-INF")) ? Double.MIN_VALUE : Double.parseDouble(upperStr);
            Double upper = (upperStr.equals("INF")) ? Double.MAX_VALUE : Double.parseDouble(upperStr);

            return (lowerInclusive && value >= lower || !lowerInclusive && value > lower)
                && (upperInclusive && value <= upper || !upperInclusive && value < upper);
        } else {
            Double rulePrice = Double.parseDouble(ruleRange);
            return rulePrice.equals(value);
        }
    }

    public static boolean validDouble(Set<String> rates, Double value) {
        for (String rateRange : rates) {
            if (validDouble(rateRange, value)) {
                return true;
            }
        }
        return false;
    }

}
