package com.progun.dunta_sdk.proxy.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalChannelCounter {
    private static final HashMap<Integer, List<String[]>> channelStrings = new HashMap<>();

    public static void addToChannelString(int id, String str1, String str2) {
        if ("STOP".equals(str1) || "STOP".equals(str2)) {
            System.out.println("Received ERROR for channel " + id);
        }
        String[] values = {str1, str2};
        List<String[]> list = channelStrings.computeIfAbsent(id, k -> new ArrayList<>());
        list.add(values);
    }

    public static Map<Integer, List<String[]>> getAllChannelStrings() {
        return channelStrings;
    }

    public static void printAllChannelStrings() {
        for (Map.Entry<Integer, List<String[]>> entry : channelStrings.entrySet()) {
            int id = entry.getKey();
            List<String[]> valueLists = entry.getValue();

            for (String[] values : valueLists) {
                System.out.println("ID: " + id + ", Values: " + values[0] + ", " + values[1]);
            }
        }
    }
}