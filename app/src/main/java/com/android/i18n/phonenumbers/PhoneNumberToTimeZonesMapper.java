package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.prefixmapper.PrefixTimeZonesMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PhoneNumberToTimeZonesMapper {
    private static final Logger LOGGER = null;
    private static final String MAPPING_DATA_DIRECTORY = "/com/android/i18n/phonenumbers/timezones/data/";
    private static final String MAPPING_DATA_FILE_NAME = "map_data";
    private static final String UNKNOWN_TIMEZONE = "Etc/Unknown";
    static final List<String> UNKNOWN_TIME_ZONE_LIST = null;
    private PrefixTimeZonesMap prefixTimeZonesMap;

    private static class LazyHolder {
        private static final PhoneNumberToTimeZonesMapper INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.LazyHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.LazyHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.LazyHolder.<clinit>():void");
        }

        private LazyHolder() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.<clinit>():void");
    }

    /* synthetic */ PhoneNumberToTimeZonesMapper(PrefixTimeZonesMap prefixTimeZonesMap, PhoneNumberToTimeZonesMapper phoneNumberToTimeZonesMapper) {
        this(prefixTimeZonesMap);
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
        InputStream inputStream = null;
        PrefixTimeZonesMap map = new PrefixTimeZonesMap();
        try {
            InputStream in = new ObjectInputStream(source);
            try {
                map.readExternal(in);
                close(in);
                inputStream = in;
            } catch (IOException e2) {
                e = e2;
                inputStream = in;
                try {
                    LOGGER.log(Level.WARNING, e.toString());
                    close(inputStream);
                    return map;
                } catch (Throwable th2) {
                    th = th2;
                    close(inputStream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = in;
                close(inputStream);
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            LOGGER.log(Level.WARNING, e.toString());
            close(inputStream);
            return map;
        }
        return map;
    }

    private static void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.toString());
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
        if (canBeGeocoded(numberType)) {
            return getTimeZonesForGeographicalNumber(number);
        }
        return getCountryLevelTimeZonesforNumber(number);
    }

    private boolean canBeGeocoded(PhoneNumberType numberType) {
        if (numberType == PhoneNumberType.FIXED_LINE || numberType == PhoneNumberType.MOBILE || numberType == PhoneNumberType.FIXED_LINE_OR_MOBILE) {
            return true;
        }
        return false;
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
