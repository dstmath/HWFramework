package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.Phonemetadata.PhoneMetadata;
import com.android.i18n.phonenumbers.Phonemetadata.PhoneNumberDesc;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.internal.MatcherApi;
import com.android.i18n.phonenumbers.internal.RegexBasedMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShortNumberInfo {
    private static final /* synthetic */ int[] -com-android-i18n-phonenumbers-ShortNumberInfo$ShortNumberCostSwitchesValues = null;
    private static final ShortNumberInfo INSTANCE = new ShortNumberInfo(RegexBasedMatcher.create());
    private static final Set<String> REGIONS_WHERE_EMERGENCY_NUMBERS_MUST_BE_EXACT = new HashSet();
    private static final Logger logger = Logger.getLogger(ShortNumberInfo.class.getName());
    private final Map<Integer, List<String>> countryCallingCodeToRegionCodeMap = CountryCodeToRegionCodeMap.getCountryCodeToRegionCodeMap();
    private final MatcherApi matcherApi;

    public enum ShortNumberCost {
        TOLL_FREE,
        STANDARD_RATE,
        PREMIUM_RATE,
        UNKNOWN_COST
    }

    private static /* synthetic */ int[] -getcom-android-i18n-phonenumbers-ShortNumberInfo$ShortNumberCostSwitchesValues() {
        if (-com-android-i18n-phonenumbers-ShortNumberInfo$ShortNumberCostSwitchesValues != null) {
            return -com-android-i18n-phonenumbers-ShortNumberInfo$ShortNumberCostSwitchesValues;
        }
        int[] iArr = new int[ShortNumberCost.values().length];
        try {
            iArr[ShortNumberCost.PREMIUM_RATE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ShortNumberCost.STANDARD_RATE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ShortNumberCost.TOLL_FREE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ShortNumberCost.UNKNOWN_COST.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -com-android-i18n-phonenumbers-ShortNumberInfo$ShortNumberCostSwitchesValues = iArr;
        return iArr;
    }

    static {
        REGIONS_WHERE_EMERGENCY_NUMBERS_MUST_BE_EXACT.add("BR");
        REGIONS_WHERE_EMERGENCY_NUMBERS_MUST_BE_EXACT.add("CL");
        REGIONS_WHERE_EMERGENCY_NUMBERS_MUST_BE_EXACT.add("NI");
    }

    public static ShortNumberInfo getInstance() {
        return INSTANCE;
    }

    ShortNumberInfo(MatcherApi matcherApi) {
        this.matcherApi = matcherApi;
    }

    private List<String> getRegionCodesForCountryCode(int countryCallingCode) {
        List list = (List) this.countryCallingCodeToRegionCodeMap.get(Integer.valueOf(countryCallingCode));
        if (list == null) {
            list = new ArrayList(0);
        }
        return Collections.unmodifiableList(list);
    }

    private boolean regionDialingFromMatchesNumber(PhoneNumber number, String regionDialingFrom) {
        return getRegionCodesForCountryCode(number.getCountryCode()).contains(regionDialingFrom);
    }

    public boolean isPossibleShortNumberForRegion(PhoneNumber number, String regionDialingFrom) {
        if (!regionDialingFromMatchesNumber(number, regionDialingFrom)) {
            return false;
        }
        PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(regionDialingFrom);
        if (phoneMetadata == null) {
            return false;
        }
        return phoneMetadata.getGeneralDesc().getPossibleLengthList().contains(Integer.valueOf(getNationalSignificantNumber(number).length()));
    }

    public boolean isPossibleShortNumber(PhoneNumber number) {
        List<String> regionCodes = getRegionCodesForCountryCode(number.getCountryCode());
        int shortNumberLength = getNationalSignificantNumber(number).length();
        for (String region : regionCodes) {
            PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(region);
            if (phoneMetadata != null && phoneMetadata.getGeneralDesc().getPossibleLengthList().contains(Integer.valueOf(shortNumberLength))) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidShortNumberForRegion(PhoneNumber number, String regionDialingFrom) {
        if (!regionDialingFromMatchesNumber(number, regionDialingFrom)) {
            return false;
        }
        PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(regionDialingFrom);
        if (phoneMetadata == null) {
            return false;
        }
        String shortNumber = getNationalSignificantNumber(number);
        if (matchesPossibleNumberAndNationalNumber(shortNumber, phoneMetadata.getGeneralDesc())) {
            return matchesPossibleNumberAndNationalNumber(shortNumber, phoneMetadata.getShortCode());
        }
        return false;
    }

    public boolean isValidShortNumber(PhoneNumber number) {
        List<String> regionCodes = getRegionCodesForCountryCode(number.getCountryCode());
        String regionCode = getRegionCodeForShortNumberFromRegionList(number, regionCodes);
        if (regionCodes.size() <= 1 || regionCode == null) {
            return isValidShortNumberForRegion(number, regionCode);
        }
        return true;
    }

    public ShortNumberCost getExpectedCostForRegion(PhoneNumber number, String regionDialingFrom) {
        if (!regionDialingFromMatchesNumber(number, regionDialingFrom)) {
            return ShortNumberCost.UNKNOWN_COST;
        }
        PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(regionDialingFrom);
        if (phoneMetadata == null) {
            return ShortNumberCost.UNKNOWN_COST;
        }
        String shortNumber = getNationalSignificantNumber(number);
        if (!phoneMetadata.getGeneralDesc().getPossibleLengthList().contains(Integer.valueOf(shortNumber.length()))) {
            return ShortNumberCost.UNKNOWN_COST;
        }
        if (matchesPossibleNumberAndNationalNumber(shortNumber, phoneMetadata.getPremiumRate())) {
            return ShortNumberCost.PREMIUM_RATE;
        }
        if (matchesPossibleNumberAndNationalNumber(shortNumber, phoneMetadata.getStandardRate())) {
            return ShortNumberCost.STANDARD_RATE;
        }
        if (matchesPossibleNumberAndNationalNumber(shortNumber, phoneMetadata.getTollFree())) {
            return ShortNumberCost.TOLL_FREE;
        }
        if (isEmergencyNumber(shortNumber, regionDialingFrom)) {
            return ShortNumberCost.TOLL_FREE;
        }
        return ShortNumberCost.UNKNOWN_COST;
    }

    public ShortNumberCost getExpectedCost(PhoneNumber number) {
        List<String> regionCodes = getRegionCodesForCountryCode(number.getCountryCode());
        if (regionCodes.size() == 0) {
            return ShortNumberCost.UNKNOWN_COST;
        }
        if (regionCodes.size() == 1) {
            return getExpectedCostForRegion(number, (String) regionCodes.get(0));
        }
        ShortNumberCost cost = ShortNumberCost.TOLL_FREE;
        for (String regionCode : regionCodes) {
            ShortNumberCost costForRegion = getExpectedCostForRegion(number, regionCode);
            switch (-getcom-android-i18n-phonenumbers-ShortNumberInfo$ShortNumberCostSwitchesValues()[costForRegion.ordinal()]) {
                case 1:
                    return ShortNumberCost.PREMIUM_RATE;
                case 2:
                    if (cost == ShortNumberCost.UNKNOWN_COST) {
                        break;
                    }
                    cost = ShortNumberCost.STANDARD_RATE;
                    break;
                case 3:
                    break;
                case 4:
                    cost = ShortNumberCost.UNKNOWN_COST;
                    break;
                default:
                    logger.log(Level.SEVERE, "Unrecognised cost for region: " + costForRegion);
                    break;
            }
        }
        return cost;
    }

    private String getRegionCodeForShortNumberFromRegionList(PhoneNumber number, List<String> regionCodes) {
        if (regionCodes.size() == 0) {
            return null;
        }
        if (regionCodes.size() == 1) {
            return (String) regionCodes.get(0);
        }
        String nationalNumber = getNationalSignificantNumber(number);
        for (String regionCode : regionCodes) {
            PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(regionCode);
            if (phoneMetadata != null && matchesPossibleNumberAndNationalNumber(nationalNumber, phoneMetadata.getShortCode())) {
                return regionCode;
            }
        }
        return null;
    }

    Set<String> getSupportedRegions() {
        return MetadataManager.getSupportedShortNumberRegions();
    }

    String getExampleShortNumber(String regionCode) {
        PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(regionCode);
        if (phoneMetadata == null) {
            return "";
        }
        PhoneNumberDesc desc = phoneMetadata.getShortCode();
        if (desc.hasExampleNumber()) {
            return desc.getExampleNumber();
        }
        return "";
    }

    String getExampleShortNumberForCost(String regionCode, ShortNumberCost cost) {
        PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(regionCode);
        if (phoneMetadata == null) {
            return "";
        }
        PhoneNumberDesc desc = null;
        switch (-getcom-android-i18n-phonenumbers-ShortNumberInfo$ShortNumberCostSwitchesValues()[cost.ordinal()]) {
            case 1:
                desc = phoneMetadata.getPremiumRate();
                break;
            case 2:
                desc = phoneMetadata.getStandardRate();
                break;
            case 3:
                desc = phoneMetadata.getTollFree();
                break;
        }
        if (desc == null || !desc.hasExampleNumber()) {
            return "";
        }
        return desc.getExampleNumber();
    }

    public boolean connectsToEmergencyNumber(String number, String regionCode) {
        return matchesEmergencyNumberHelper(number, regionCode, true);
    }

    public boolean isEmergencyNumber(String number, String regionCode) {
        return matchesEmergencyNumberHelper(number, regionCode, false);
    }

    private boolean matchesEmergencyNumberHelper(String number, String regionCode, boolean allowPrefixMatch) {
        number = PhoneNumberUtil.extractPossibleNumber(number);
        if (PhoneNumberUtil.PLUS_CHARS_PATTERN.matcher(number).lookingAt()) {
            return false;
        }
        PhoneMetadata metadata = MetadataManager.getShortNumberMetadataForRegion(regionCode);
        if (metadata == null || (metadata.hasEmergency() ^ 1) != 0) {
            return false;
        }
        return this.matcherApi.matchesNationalNumber(PhoneNumberUtil.normalizeDigitsOnly(number), metadata.getEmergency(), allowPrefixMatch ? REGIONS_WHERE_EMERGENCY_NUMBERS_MUST_BE_EXACT.contains(regionCode) ^ 1 : false);
    }

    public boolean isCarrierSpecific(PhoneNumber number) {
        String regionCode = getRegionCodeForShortNumberFromRegionList(number, getRegionCodesForCountryCode(number.getCountryCode()));
        String nationalNumber = getNationalSignificantNumber(number);
        PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(regionCode);
        if (phoneMetadata != null) {
            return matchesPossibleNumberAndNationalNumber(nationalNumber, phoneMetadata.getCarrierSpecific());
        }
        return false;
    }

    public boolean isCarrierSpecificForRegion(PhoneNumber number, String regionDialingFrom) {
        boolean z = false;
        if (!regionDialingFromMatchesNumber(number, regionDialingFrom)) {
            return false;
        }
        String nationalNumber = getNationalSignificantNumber(number);
        PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(regionDialingFrom);
        if (phoneMetadata != null) {
            z = matchesPossibleNumberAndNationalNumber(nationalNumber, phoneMetadata.getCarrierSpecific());
        }
        return z;
    }

    private static String getNationalSignificantNumber(PhoneNumber number) {
        StringBuilder nationalNumber = new StringBuilder();
        if (number.isItalianLeadingZero()) {
            char[] zeros = new char[number.getNumberOfLeadingZeros()];
            Arrays.fill(zeros, '0');
            nationalNumber.append(new String(zeros));
        }
        nationalNumber.append(number.getNationalNumber());
        return nationalNumber.toString();
    }

    private boolean matchesPossibleNumberAndNationalNumber(String number, PhoneNumberDesc numberDesc) {
        if (numberDesc.getPossibleLengthCount() <= 0 || (numberDesc.getPossibleLengthList().contains(Integer.valueOf(number.length())) ^ 1) == 0) {
            return this.matcherApi.matchesNationalNumber(number, numberDesc, false);
        }
        return false;
    }
}
