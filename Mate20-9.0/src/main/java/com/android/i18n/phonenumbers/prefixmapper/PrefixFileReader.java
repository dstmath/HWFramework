package com.android.i18n.phonenumbers.prefixmapper;

import com.android.i18n.phonenumbers.Phonenumber;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrefixFileReader {
    private static final Logger logger = Logger.getLogger(PrefixFileReader.class.getName());
    private Map<String, PhonePrefixMap> availablePhonePrefixMaps = new HashMap();
    private MappingFileProvider mappingFileProvider = new MappingFileProvider();
    private final String phonePrefixDataDirectory;

    public PrefixFileReader(String phonePrefixDataDirectory2) {
        this.phonePrefixDataDirectory = phonePrefixDataDirectory2;
        loadMappingFileProvider();
    }

    private void loadMappingFileProvider() {
        InputStream source = PrefixFileReader.class.getResourceAsStream(this.phonePrefixDataDirectory + "config");
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(source);
            this.mappingFileProvider.readExternal(in);
        } catch (IOException e) {
            logger.log(Level.WARNING, e.toString());
        } catch (Throwable th) {
            close(in);
            throw th;
        }
        close(in);
    }

    private PhonePrefixMap getPhonePrefixDescriptions(int prefixMapKey, String language, String script, String region) {
        String fileName = this.mappingFileProvider.getFileName(prefixMapKey, language, script, region);
        if (fileName.length() == 0) {
            return null;
        }
        if (!this.availablePhonePrefixMaps.containsKey(fileName)) {
            loadPhonePrefixMapFromFile(fileName);
        }
        return this.availablePhonePrefixMaps.get(fileName);
    }

    private void loadPhonePrefixMapFromFile(String fileName) {
        InputStream source = PrefixFileReader.class.getResourceAsStream(this.phonePrefixDataDirectory + fileName);
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(source);
            PhonePrefixMap map = new PhonePrefixMap();
            map.readExternal(in);
            this.availablePhonePrefixMaps.put(fileName, map);
        } catch (IOException t) {
            logger.log(Level.WARNING, t.toString());
        } catch (NullPointerException e) {
            logger.log(Level.WARNING, e.toString());
        } catch (Throwable th) {
            close(in);
            throw th;
        }
        close(in);
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

    public String getDescriptionForNumber(Phonenumber.PhoneNumber number, String language, String script, String region) {
        int countryCallingCode = number.getCountryCode();
        int phonePrefix = countryCallingCode != 1 ? countryCallingCode : 1000 + ((int) (number.getNationalNumber() / 10000000));
        PhonePrefixMap phonePrefixDescriptions = getPhonePrefixDescriptions(phonePrefix, language, script, region);
        String description = phonePrefixDescriptions != null ? phonePrefixDescriptions.lookup(number) : null;
        if ((description == null || description.length() == 0) && mayFallBackToEnglish(language)) {
            PhonePrefixMap defaultMap = getPhonePrefixDescriptions(phonePrefix, "en", "", "");
            if (defaultMap == null) {
                return "";
            }
            description = defaultMap.lookup(number);
        }
        return description != null ? description : "";
    }

    private boolean mayFallBackToEnglish(String lang) {
        return !lang.equals("zh") && !lang.equals("ja") && !lang.equals("ko");
    }
}
