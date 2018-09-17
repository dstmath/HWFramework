package com.android.internal.logging;

import android.util.LogException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AndroidConfig {
    public AndroidConfig() {
        try {
            Logger rootLogger = Logger.getLogger(LogException.NO_VALUE);
            rootLogger.addHandler(new AndroidHandler());
            rootLogger.setLevel(Level.INFO);
            Logger.getLogger("org.apache").setLevel(Level.WARNING);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
