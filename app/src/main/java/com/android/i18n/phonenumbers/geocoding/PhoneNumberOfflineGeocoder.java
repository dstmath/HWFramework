package com.android.i18n.phonenumbers.geocoding;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber.CountryCodeSource;
import com.android.i18n.phonenumbers.prefixmapper.PrefixFileReader;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PhoneNumberOfflineGeocoder {
    private static final String MAPPING_DATA_DIRECTORY = "/com/android/i18n/phonenumbers/geocoding/data/";
    private static Map<String, List<String>> SAME_COUNTRY_MAPPINGS;
    private static PhoneNumberOfflineGeocoder instance;
    private final PhoneNumberUtil phoneUtil;
    private PrefixFileReader prefixFileReader;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder.<clinit>():void");
    }

    PhoneNumberOfflineGeocoder(String phonePrefixDataDirectory) {
        this.prefixFileReader = null;
        this.phoneUtil = PhoneNumberUtil.getInstance();
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

    private String getCountryNameForNumber(PhoneNumber number, Locale language) {
        List<String> regionCodes = this.phoneUtil.getRegionCodesForCountryCode(number.getCountryCode());
        if (!"".equals(getPreferredRegionCode(regionCodes)) && number.getCountryCodeSource() != CountryCodeSource.FROM_DEFAULT_COUNTRY) {
            return getRegionDisplayName(getPreferredRegionCode(regionCodes), language);
        }
        if (regionCodes.size() == 1) {
            return getRegionDisplayName((String) regionCodes.get(0), language);
        }
        String regionWhereNumberIsValid = "ZZ";
        for (String regionCode : regionCodes) {
            if (this.phoneUtil.isValidNumberForRegion(number, regionCode)) {
                if (regionWhereNumberIsValid.equals("ZZ")) {
                    regionWhereNumberIsValid = regionCode;
                } else if (!SAME_COUNTRY_MAPPINGS.containsKey(regionWhereNumberIsValid) || !((List) SAME_COUNTRY_MAPPINGS.get(regionWhereNumberIsValid)).contains(regionCode)) {
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
        String preferred = (String) regionCodes.get(0);
        if (!SAME_COUNTRY_MAPPINGS.containsKey(preferred)) {
            return "";
        }
        List<String> regionList = (List) SAME_COUNTRY_MAPPINGS.get(preferred);
        if (regionCodes.size() - 1 > regionList.size()) {
            return "";
        }
        for (int i = 1; i < regionCodes.size(); i++) {
            if (!regionList.contains(regionCodes.get(i))) {
                return "";
            }
        }
        return preferred;
    }

    private String getRegionDisplayName(String regionCode, Locale language) {
        return (regionCode == null || regionCode.equals("ZZ") || regionCode.equals(PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY)) ? "" : new Locale("", regionCode).getDisplayCountry(language);
    }

    public String getDescriptionForValidNumber(PhoneNumber number, Locale languageCode) {
        String areaDescription;
        String langStr = languageCode.getLanguage();
        String scriptStr = "";
        String regionStr = languageCode.getCountry();
        String mobileToken = PhoneNumberUtil.getCountryMobileToken(number.getCountryCode());
        String nationalNumber = this.phoneUtil.getNationalSignificantNumber(number);
        if (mobileToken.equals("") || !nationalNumber.startsWith(mobileToken)) {
            areaDescription = this.prefixFileReader.getDescriptionForNumber(number, langStr, scriptStr, regionStr);
        } else {
            PhoneNumber copiedNumber;
            try {
                copiedNumber = this.phoneUtil.parse(nationalNumber.substring(mobileToken.length()), this.phoneUtil.getRegionCodeForCountryCode(number.getCountryCode()));
            } catch (NumberParseException e) {
                copiedNumber = number;
            }
            areaDescription = this.prefixFileReader.getDescriptionForNumber(copiedNumber, langStr, scriptStr, regionStr);
        }
        if (areaDescription.length() > 0) {
            return areaDescription;
        }
        return getCountryNameForNumber(number, languageCode);
    }

    public String getDescriptionForValidNumber(PhoneNumber number, Locale languageCode, String userRegion) {
        String regionCode = this.phoneUtil.getRegionCodeForNumber(number);
        if (userRegion.equals(regionCode)) {
            return getDescriptionForValidNumber(number, languageCode);
        }
        return getRegionDisplayName(regionCode, languageCode);
    }

    public String getDescriptionForNumber(PhoneNumber number, Locale languageCode) {
        PhoneNumberType numberType = this.phoneUtil.getNumberType(number);
        if (numberType == PhoneNumberType.UNKNOWN) {
            return "";
        }
        if (canBeGeocoded(numberType)) {
            return getDescriptionForValidNumber(number, languageCode);
        }
        return getCountryNameForNumber(number, languageCode);
    }

    public String getDescriptionForNumber(PhoneNumber number, Locale languageCode, String userRegion) {
        PhoneNumberType numberType = this.phoneUtil.getNumberType(number);
        if (numberType == PhoneNumberType.UNKNOWN) {
            return "";
        }
        if (canBeGeocoded(numberType)) {
            return getDescriptionForValidNumber(number, languageCode, userRegion);
        }
        return getCountryNameForNumber(number, languageCode);
    }

    private boolean canBeGeocoded(PhoneNumberType numberType) {
        if (numberType == PhoneNumberType.FIXED_LINE || numberType == PhoneNumberType.MOBILE || numberType == PhoneNumberType.FIXED_LINE_OR_MOBILE) {
            return true;
        }
        return false;
    }
}
