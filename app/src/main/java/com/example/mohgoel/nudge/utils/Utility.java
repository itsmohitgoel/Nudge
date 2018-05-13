package com.example.mohgoel.nudge.utils;

import java.util.Date;

/**
 * Created by MOHGOEL on 13-May-18.
 */

public class Utility {
    public static String getFriedlyDate(String epochTime) {
        long epoch = Long.valueOf(epochTime);
        String dateString = new java.text.SimpleDateFormat("hh:mm a")
                .format(new Date(epoch));
        return dateString;
    }
}
