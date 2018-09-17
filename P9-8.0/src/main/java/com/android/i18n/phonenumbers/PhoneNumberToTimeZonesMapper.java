package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.prefixmapper.PrefixTimeZonesMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PhoneNumberToTimeZonesMapper {
    private static final String MAPPING_DATA_DIRECTORY = "/com/android/i18n/phonenumbers/timezones/data/";
    private static final String MAPPING_DATA_FILE_NAME = "map_data";
    private static final String UNKNOWN_TIMEZONE = "Etc/Unknown";
    static final List<String> UNKNOWN_TIME_ZONE_LIST = new ArrayList(1);
    private static final Logger logger = Logger.getLogger(PhoneNumberToTimeZonesMapper.class.getName());
    private PrefixTimeZonesMap prefixTimeZonesMap;

    private static class LazyHolder {
        private static final PhoneNumberToTimeZonesMapper INSTANCE = new PhoneNumberToTimeZonesMapper(PhoneNumberToTimeZonesMapper.loadPrefixTimeZonesMapFromFile("/com/android/i18n/phonenumbers/timezones/data/map_data"), null);

        private LazyHolder() {
        }
    }

    /* synthetic */ PhoneNumberToTimeZonesMapper(PrefixTimeZonesMap prefixTimeZonesMap, PhoneNumberToTimeZonesMapper -this1) {
        this(prefixTimeZonesMap);
    }

    static {
        UNKNOWN_TIME_ZONE_LIST.add(UNKNOWN_TIMEZONE);
    }

    PhoneNumberToTimeZonesMapper(String prefixTimeZonesMapDataDirectory) {
        this.prefixTimeZonesMap = null;
        this.prefixTimeZonesMap = loadPrefixTimeZonesMapFromFile(prefixTimeZonesMapDataDirectory + MAPPING_DATA_FILE_NAME);
    }

    private PhoneNumberToTimeZonesMapper(PrefixTimeZonesMap prefixTimeZonesMap) {
        this.prefixTimeZonesMap = null;
        this.prefixTimeZonesMap = prefixTimeZonesMap;
    }

    private static PrefixTimeZonesMap loadPrefixTimeZonesMapFromFile(String path) {
        IOException e;
        Throwable th;
        InputStream source = PhoneNumberToTimeZonesMapper.class.getResourceAsStream(path);
        InputStream in = null;
        PrefixTimeZonesMap map = new PrefixTimeZonesMap();
        try {
            InputStream in2 = new ObjectInputStream(source);
            try {
                map.readExternal(in2);
                close(in2);
                in = in2;
            } catch (IOException e2) {
                e = e2;
                in = in2;
                try {
                    logger.log(Level.WARNING, e.toString());
                    close(in);
                    return map;
                } catch (Throwable th2) {
                    th = th2;
                    close(in);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                in = in2;
                close(in);
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            logger.log(Level.WARNING, e.toString());
            close(in);
            return map;
        }
        return map;
    }

    private static void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, e.toString());
            }
        }
    }

    public static synchronized PhoneNumberToTimeZonesMapper getInstance() {
        PhoneNumberToTimeZonesMapper -get0;
        synchronized (PhoneNumberToTimeZonesMapper.class) {
            -get0 = LazyHolder.INSTANCE;
        }
        return -get0;
    }

    public List<String> getTimeZonesForGeographicalNumber(PhoneNumber number) {
        return getTimeZonesForGeocodableNumber(number);
    }

    public List<String> getTimeZonesForNumber(PhoneNumber number) {
        PhoneNumberType numberType = PhoneNumberUtil.getInstance().getNumberType(number);
        if (numberType == PhoneNumberType.UNKNOWN) {
            return UNKNOWN_TIME_ZONE_LIST;
        }
        if (PhoneNumberUtil.getInstance().isNumberGeographical(numberType, number.getCountryCode())) {
            return getTimeZonesForGeographicalNumber(number);
        }
        return getCountryLevelTimeZonesforNumber(number);
    }

    public static String getUnknownTimeZone() {
        return UNKNOWN_TIMEZONE;
    }

    private List<String> getTimeZonesForGeocodableNumber(PhoneNumber number) {
        List<String> timezones = this.prefixTimeZonesMap.lookupTimeZonesForNumber(number);
        if (timezones.isEmpty()) {
            timezones = UNKNOWN_TIME_ZONE_LIST;
        }
        return Collections.unmodifiableList(timezones);
    }

    private List<String> getCountryLevelTimeZonesforNumber(PhoneNumber number) {
        List<String> timezones = this.prefixTimeZonesMap.lookupCountryLevelTimeZonesForNumber(number);
        if (timezones.isEmpty()) {
            timezones = UNKNOWN_TIME_ZONE_LIST;
        }
        return Collections.unmodifiableList(timezones);
    }
}
