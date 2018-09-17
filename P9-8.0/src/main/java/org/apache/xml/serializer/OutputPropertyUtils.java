package org.apache.xml.serializer;

import java.util.Properties;

public final class OutputPropertyUtils {
    public static boolean getBooleanProperty(String key, Properties props) {
        String s = props.getProperty(key);
        if (s == null || (s.equals("yes") ^ 1) != 0) {
            return false;
        }
        return true;
    }

    public static int getIntProperty(String key, Properties props) {
        String s = props.getProperty(key);
        if (s == null) {
            return 0;
        }
        return Integer.parseInt(s);
    }
}
