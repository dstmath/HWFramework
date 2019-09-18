package com.android.i18n.phonenumbers.geocoding;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber;
import com.android.i18n.phonenumbers.prefixmapper.PrefixFileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PhoneNumberOfflineGeocoder {
    private static final String MAPPING_DATA_DIRECTORY = "/com/android/i18n/phonenumbers/geocoding/data/";
    private static Map<String, List<String>> SAME_COUNTRY_MAPPINGS;
    private static PhoneNumberOfflineGeocoder instance = null;
    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    private PrefixFileReader prefixFileReader = null;

    static {
        HashMap<String, List<String>> sameCountrymap = new HashMap<>();
        List<String> countryList = new ArrayList<>(2);
        countryList.add("CC");
        countryList.add("CX");
        sameCountrymap.put("AU", countryList);
        List<String> countryList2 = new ArrayList<>(1);
        countryList2.add("ZG");
        sameCountrymap.put("MM", countryList2);
        List<String> countryList3 = new ArrayList<>(1);
        countryList3.add("AX");
        sameCountrymap.put("FI", countryList3);
        SAME_COUNTRY_MAPPINGS = Collections.unmodifiableMap(sameCountrymap);
    }

    PhoneNumberOfflineGeocoder(String phonePrefixDataDirectory) {
        this.prefixFileReader = new PrefixFileReader(phonePrefixDataDirectory);
    }

    public static synchronized PhoneNumberOfflineGeocoder getInstance() {
        PhoneNumberOfflineGeocoder phoneNumberOfflineGeocoder;
        synchronized (PhoneNumberOfflineGeocoder.class) {
            if (instance == null) {
                instance = new PhoneNumberOfflineGeocoder(MAPPING_DATA_DIRECTORY);
            }
            phoneNumberOfflineGeocoder = instance;
        }
        return phoneNumberOfflineGeocoder;
    }

    private String getCountryNameForNumber(Phonenumber.PhoneNumber number, Locale language) {
        List<String> regionCodes = this.phoneUtil.getRegionCodesForCountryCode(number.getCountryCode());
        if (!"".equals(getPreferredRegionCode(regionCodes)) && number.getCountryCodeSource() != Phonenumber.PhoneNumber.CountryCodeSource.FROM_DEFAULT_COUNTRY) {
            return getRegionDisplayName(getPreferredRegionCode(regionCodes), language);
        }
        if (regionCodes.size() == 1) {
            return getRegionDisplayName(regionCodes.get(0), language);
        }
        String regionWhereNumberIsValid = "ZZ";
        for (String regionCode : regionCodes) {
            if (this.phoneUtil.isValidNumberForRegion(number, regionCode)) {
                if (regionWhereNumberIsValid.equals("ZZ")) {
                    regionWhereNumberIsValid = regionCode;
                } else if (!SAME_COUNTRY_MAPPINGS.containsKey(regionWhereNumberIsValid) || !SAME_COUNTRY_MAPPINGS.get(regionWhereNumberIsValid).contains(regionCode)) {
                    return "";
                }
            }
        }
        return getRegionDisplayName(regionWhereNumberIsValid, language);
    }

    private String getPreferredRegionCode(List<String> regionCodes) {
        if (regionCodes.size() < 2) {
            return "";
        }
        String preferred = regionCodes.get(0);
        if (!SAME_COUNTRY_MAPPINGS.containsKey(preferred)) {
            return "";
        }
        List<String> regionList = SAME_COUNTRY_MAPPINGS.get(preferred);
        int i = 1;
        if (regionCodes.size() - 1 > regionList.size()) {
            return "";
        }
        while (true) {
            int i2 = i;
            if (i2 >= regionCodes.size()) {
                return preferred;
            }
            if (!regionList.contains(regionCodes.get(i2))) {
                return "";
            }
            i = i2 + 1;
        }
    }

    private String getRegionDisplayName(String regionCode, Locale language) {
        return (regionCode == null || regionCode.equals("ZZ") || regionCode.equals(PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY)) ? "" : new Locale("", regionCode).getDisplayCountry(language);
    }

    public String getDescriptionForValidNumber(Phonenumber.PhoneNumber number, Locale languageCode) {
        String region;
        Phonenumber.PhoneNumber copiedNumber;
        String langStr = languageCode.getLanguage();
        String regionStr = languageCode.getCountry();
        String mobileToken = PhoneNumberUtil.getCountryMobileToken(number.getCountryCode());
        String nationalNumber = this.phoneUtil.getNationalSignificantNumber(number);
        if (mobileToken.equals("") || !nationalNumber.startsWith(mobileToken)) {
            region = this.prefixFileReader.getDescriptionForNumber(number, langStr, "", regionStr);
        } else {
            try {
                copiedNumber = this.phoneUtil.parse(nationalNumber.substring(mobileToken.length()), this.phoneUtil.getRegionCodeForCountryCode(number.getCountryCode()));
            } catch (NumberParseException e) {
                copiedNumber = number;
            }
            region = this.prefixFileReader.getDescriptionForNumber(copiedNumber, langStr, "", regionStr);
        }
        if (region.length() > 0) {
            return region;
        }
        return getCountryNameForNumber(number, languageCode);
    }

    public String getDescriptionForValidNumber(Phonenumber.PhoneNumber number, Locale languageCode, String userRegion) {
        String regionCode = this.phoneUtil.getRegionCodeForNumber(number);
        if (userRegion.equals(regionCode)) {
            return getDescriptionForValidNumber(number, languageCode);
        }
        return getRegionDisplayName(regionCode, languageCode);
    }

    public String getDescriptionForNumber(Phonenumber.PhoneNumber number, Locale languageCode) {
        PhoneNumberUtil.PhoneNumberType numberType = this.phoneUtil.getNumberType(number);
        if (numberType == PhoneNumberUtil.PhoneNumberType.UNKNOWN) {
            return "";
        }
        if (!this.phoneUtil.isNumberGeographical(numberType, number.getCountryCode())) {
            return getCountryNameForNumber(number, languageCode);
        }
        return getDescriptionForValidNumber(number, languageCode);
    }

    public String getDescriptionForNumber(Phonenumber.PhoneNumber number, Locale languageCode, String userRegion) {
        PhoneNumberUtil.PhoneNumberType numberType = this.phoneUtil.getNumberType(number);
        if (numberType == PhoneNumberUtil.PhoneNumberType.UNKNOWN) {
            return "";
        }
        if (!this.phoneUtil.isNumberGeographical(numberType, number.getCountryCode())) {
            return getCountryNameForNumber(number, languageCode);
        }
        return getDescriptionForValidNumber(number, languageCode, userRegion);
    }
}
