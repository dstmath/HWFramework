package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.Phonemetadata.PhoneMetadata;
import com.android.i18n.phonenumbers.Phonemetadata.PhoneMetadataCollection;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

class MetadataManager {
    private static final String ALTERNATE_FORMATS_FILE_PREFIX = "/com/android/i18n/phonenumbers/data/PhoneNumberAlternateFormatsProto";
    private static final Logger LOGGER = null;
    private static final String SHORT_NUMBER_METADATA_FILE_PREFIX = "/com/android/i18n/phonenumbers/data/ShortNumberMetadataProto";
    private static final Map<Integer, PhoneMetadata> callingCodeToAlternateFormatsMap = null;
    private static final Set<Integer> countryCodeSet = null;
    private static final Set<String> regionCodeSet = null;
    private static final Map<String, PhoneMetadata> regionCodeToShortNumberMetadataMap = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.i18n.phonenumbers.MetadataManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.i18n.phonenumbers.MetadataManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.MetadataManager.<clinit>():void");
    }

    private MetadataManager() {
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

    private static void loadAlternateFormatsMetadataFromFile(int countryCallingCode) {
        IOException e;
        Throwable th;
        InputStream inputStream = null;
        try {
            InputStream in = new ObjectInputStream(PhoneNumberMatcher.class.getResourceAsStream("/com/android/i18n/phonenumbers/data/PhoneNumberAlternateFormatsProto_" + countryCallingCode));
            try {
                PhoneMetadataCollection alternateFormats = new PhoneMetadataCollection();
                alternateFormats.readExternal(in);
                for (PhoneMetadata metadata : alternateFormats.getMetadataList()) {
                    callingCodeToAlternateFormatsMap.put(Integer.valueOf(metadata.getCountryCode()), metadata);
                }
                close(in);
            } catch (IOException e2) {
                e = e2;
                inputStream = in;
                try {
                    LOGGER.log(Level.WARNING, e.toString());
                    close(inputStream);
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
        }
    }

    static PhoneMetadata getAlternateFormatsForCountry(int countryCallingCode) {
        if (!countryCodeSet.contains(Integer.valueOf(countryCallingCode))) {
            return null;
        }
        synchronized (callingCodeToAlternateFormatsMap) {
            if (!callingCodeToAlternateFormatsMap.containsKey(Integer.valueOf(countryCallingCode))) {
                loadAlternateFormatsMetadataFromFile(countryCallingCode);
            }
        }
        return (PhoneMetadata) callingCodeToAlternateFormatsMap.get(Integer.valueOf(countryCallingCode));
    }

    private static void loadShortNumberMetadataFromFile(String regionCode) {
        IOException e;
        Throwable th;
        InputStream inputStream = null;
        try {
            InputStream in = new ObjectInputStream(PhoneNumberMatcher.class.getResourceAsStream("/com/android/i18n/phonenumbers/data/ShortNumberMetadataProto_" + regionCode));
            try {
                PhoneMetadataCollection shortNumberMetadata = new PhoneMetadataCollection();
                shortNumberMetadata.readExternal(in);
                for (PhoneMetadata metadata : shortNumberMetadata.getMetadataList()) {
                    regionCodeToShortNumberMetadataMap.put(regionCode, metadata);
                }
                close(in);
            } catch (IOException e2) {
                e = e2;
                inputStream = in;
                try {
                    LOGGER.log(Level.WARNING, e.toString());
                    close(inputStream);
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
        }
    }

    static Set<String> getShortNumberMetadataSupportedRegions() {
        return regionCodeSet;
    }

    static PhoneMetadata getShortNumberMetadataForRegion(String regionCode) {
        if (!regionCodeSet.contains(regionCode)) {
            return null;
        }
        synchronized (regionCodeToShortNumberMetadataMap) {
            if (!regionCodeToShortNumberMetadataMap.containsKey(regionCode)) {
                loadShortNumberMetadataFromFile(regionCode);
            }
        }
        return (PhoneMetadata) regionCodeToShortNumberMetadataMap.get(regionCode);
    }
}
