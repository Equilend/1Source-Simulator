package com.equilend.simulator.utils;

public class RuleProcessorUtil {

    public static void waitForDelay(Long startTime, Double delay) {
        long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }
    }

}
