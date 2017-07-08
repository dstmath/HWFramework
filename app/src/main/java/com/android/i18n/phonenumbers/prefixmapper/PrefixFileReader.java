package com.android.i18n.phonenumbers.prefixmapper;

import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrefixFileReader {
    private static final Logger LOGGER = null;
    private Map<String, PhonePrefixMap> availablePhonePrefixMaps;
    private MappingFileProvider mappingFileProvider;
    private final String phonePrefixDataDirectory;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.i18n.phonenumbers.prefixmapper.PrefixFileReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.i18n.phonenumbers.prefixmapper.PrefixFileReader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.prefixmapper.PrefixFileReader.<clinit>():void");
    }

    public PrefixFileReader(String phonePrefixDataDirectory) {
        this.mappingFileProvider = new MappingFileProvider();
        this.availablePhonePrefixMaps = new HashMap();
        this.phonePrefixDataDirectory = phonePrefixDataDirectory;
        loadMappingFileProvider();
    }

    private void loadMappingFileProvider() {
        IOException e;
        Throwable th;
        InputStream inputStream = null;
        try {
            InputStream in = new ObjectInputStream(PrefixFileReader.class.getResourceAsStream(this.phonePrefixDataDirectory + "config"));
            try {
                this.mappingFileProvider.readExternal(in);
                close(in);
                inputStream = in;
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

    private PhonePrefixMap getPhonePrefixDescriptions(int prefixMapKey, String language, String script, String region) {
        String fileName = this.mappingFileProvider.getFileName(prefixMapKey, language, script, region);
        if (fileName.length() == 0) {
            return null;
        }
        if (!this.availablePhonePrefixMaps.containsKey(fileName)) {
            loadPhonePrefixMapFromFile(fileName);
        }
        return (PhonePrefixMap) this.availablePhonePrefixMaps.get(fileName);
    }

    private void loadPhonePrefixMapFromFile(String fileName) {
        IOException e;
        Throwable th;
        InputStream inputStream = null;
        try {
            InputStream in = new ObjectInputStream(PrefixFileReader.class.getResourceAsStream(this.phonePrefixDataDirectory + fileName));
            try {
                PhonePrefixMap map = new PhonePrefixMap();
                map.readExternal(in);
                this.availablePhonePrefixMaps.put(fileName, map);
                close(in);
                inputStream = in;
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

    private static void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.toString());
            }
        }
    }

    public String getDescriptionForNumber(PhoneNumber number, String lang, String script, String region) {
        int phonePrefix;
        String str = null;
        int countryCallingCode = number.getCountryCode();
        if (countryCallingCode != 1) {
            phonePrefix = countryCallingCode;
        } else {
            phonePrefix = ((int) (number.getNationalNumber() / 10000000)) + 1000;
        }
        PhonePrefixMap phonePrefixDescriptions = getPhonePrefixDescriptions(phonePrefix, lang, script, region);
        if (phonePrefixDescriptions != null) {
            str = phonePrefixDescriptions.lookup(number);
        }
        if ((str == null || str.length() == 0) && mayFallBackToEnglish(lang)) {
            PhonePrefixMap defaultMap = getPhonePrefixDescriptions(phonePrefix, "en", "", "");
            if (defaultMap == null) {
                return "";
            }
            str = defaultMap.lookup(number);
        }
        if (str == null) {
            str = "";
        }
        return str;
    }

    private boolean mayFallBackToEnglish(String lang) {
        return (lang.equals("zh") || lang.equals("ja") || lang.equals("ko")) ? false : true;
    }
}
