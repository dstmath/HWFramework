package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberMatcher;
import com.android.i18n.phonenumbers.Phonemetadata;
import com.android.i18n.phonenumbers.Phonenumber;
import com.android.i18n.phonenumbers.internal.MatcherApi;
import com.android.i18n.phonenumbers.internal.RegexBasedMatcher;
import com.android.i18n.phonenumbers.internal.RegexCache;
import gov.nist.core.Separators;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sip.header.WarningHeader;

public class PhoneNumberUtil {
    private static final Map<Character, Character> ALL_PLUS_NUMBER_GROUPING_SYMBOLS;
    private static final Map<Character, Character> ALPHA_MAPPINGS;
    private static final Map<Character, Character> ALPHA_PHONE_MAPPINGS;
    private static final Pattern CAPTURING_DIGIT_PATTERN = Pattern.compile("(\\p{Nd})");
    private static final String CAPTURING_EXTN_DIGITS = "(\\p{Nd}{1,7})";
    private static final String CC_STRING = "$CC";
    private static final String COLOMBIA_MOBILE_TO_FIXED_LINE_PREFIX = "3";
    private static final String DEFAULT_EXTN_PREFIX = " ext. ";
    private static final Map<Character, Character> DIALLABLE_CHAR_MAPPINGS;
    private static final String DIGITS = "\\p{Nd}";
    private static final Pattern EXTN_PATTERN = Pattern.compile("(?:" + EXTN_PATTERNS_FOR_PARSING + ")$", REGEX_FLAGS);
    static final String EXTN_PATTERNS_FOR_MATCHING = createExtnPattern("xｘ#＃~～");
    private static final String EXTN_PATTERNS_FOR_PARSING;
    private static final String FG_STRING = "$FG";
    private static final Pattern FIRST_GROUP_ONLY_PREFIX_PATTERN = Pattern.compile("\\(?\\$1\\)?");
    private static final Pattern FIRST_GROUP_PATTERN = Pattern.compile("(\\$\\d)");
    private static final Set<Integer> GEO_MOBILE_COUNTRIES;
    private static final Set<Integer> GEO_MOBILE_COUNTRIES_WITHOUT_MOBILE_AREA_CODES;
    private static final int MAX_INPUT_STRING_LENGTH = 250;
    static final int MAX_LENGTH_COUNTRY_CODE = 3;
    static final int MAX_LENGTH_FOR_NSN = 17;
    private static final int MIN_LENGTH_FOR_NSN = 2;
    private static final Map<Integer, String> MOBILE_TOKEN_MAPPINGS;
    private static final int NANPA_COUNTRY_CODE = 1;
    static final Pattern NON_DIGITS_PATTERN = Pattern.compile("(\\D+)");
    private static final String NP_STRING = "$NP";
    static final String PLUS_CHARS = "+＋";
    static final Pattern PLUS_CHARS_PATTERN = Pattern.compile("[+＋]+");
    static final char PLUS_SIGN = '+';
    static final int REGEX_FLAGS = 66;
    public static final String REGION_CODE_FOR_NON_GEO_ENTITY = "001";
    private static final String RFC3966_EXTN_PREFIX = ";ext=";
    private static final String RFC3966_ISDN_SUBADDRESS = ";isub=";
    private static final String RFC3966_PHONE_CONTEXT = ";phone-context=";
    private static final String RFC3966_PREFIX = "tel:";
    private static final String SECOND_NUMBER_START = "[\\\\/] *x";
    static final Pattern SECOND_NUMBER_START_PATTERN = Pattern.compile(SECOND_NUMBER_START);
    private static final Pattern SEPARATOR_PATTERN = Pattern.compile("[-x‐-―−ー－-／  ­​⁠　()（）［］.\\[\\]/~⁓∼～]+");
    private static final Pattern SINGLE_INTERNATIONAL_PREFIX = Pattern.compile("[\\d]+(?:[~⁓∼～][\\d]+)?");
    private static final char STAR_SIGN = '*';
    private static final String UNKNOWN_REGION = "ZZ";
    private static final String UNWANTED_END_CHARS = "[[\\P{N}&&\\P{L}]&&[^#]]+$";
    static final Pattern UNWANTED_END_CHAR_PATTERN = Pattern.compile(UNWANTED_END_CHARS);
    private static final String VALID_ALPHA = (Arrays.toString(ALPHA_MAPPINGS.keySet().toArray()).replaceAll("[, \\[\\]]", "") + Arrays.toString(ALPHA_MAPPINGS.keySet().toArray()).toLowerCase().replaceAll("[, \\[\\]]", ""));
    private static final Pattern VALID_ALPHA_PHONE_PATTERN = Pattern.compile("(?:.*?[A-Za-z]){3}.*");
    private static final String VALID_PHONE_NUMBER = ("\\p{Nd}{2}|[+＋]*+(?:[-x‐-―−ー－-／  ­​⁠　()（）［］.\\[\\]/~⁓∼～*]*\\p{Nd}){3,}[-x‐-―−ー－-／  ­​⁠　()（）［］.\\[\\]/~⁓∼～*" + VALID_ALPHA + DIGITS + "]*");
    private static final Pattern VALID_PHONE_NUMBER_PATTERN = Pattern.compile(VALID_PHONE_NUMBER + "(?:" + EXTN_PATTERNS_FOR_PARSING + ")?", REGEX_FLAGS);
    static final String VALID_PUNCTUATION = "-x‐-―−ー－-／  ­​⁠　()（）［］.\\[\\]/~⁓∼～";
    private static final String VALID_START_CHAR = "[+＋\\p{Nd}]";
    private static final Pattern VALID_START_CHAR_PATTERN = Pattern.compile(VALID_START_CHAR);
    private static PhoneNumberUtil instance = null;
    private static final Logger logger = Logger.getLogger(PhoneNumberUtil.class.getName());
    private final Map<Integer, List<String>> countryCallingCodeToRegionCodeMap;
    private final Set<Integer> countryCodesForNonGeographicalRegion = new HashSet();
    private final MatcherApi matcherApi = RegexBasedMatcher.create();
    private final MetadataSource metadataSource;
    private final Set<String> nanpaRegions = new HashSet(35);
    private final RegexCache regexCache = new RegexCache(100);
    private final Set<String> supportedRegions = new HashSet(320);

    /* renamed from: com.android.i18n.phonenumbers.PhoneNumberUtil$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType = new int[PhoneNumberType.values().length];

        static {
            try {
                $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType[PhoneNumberType.PREMIUM_RATE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType[PhoneNumberType.TOLL_FREE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType[PhoneNumberType.MOBILE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType[PhoneNumberType.FIXED_LINE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType[PhoneNumberType.FIXED_LINE_OR_MOBILE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType[PhoneNumberType.SHARED_COST.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType[PhoneNumberType.VOIP.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType[PhoneNumberType.PERSONAL_NUMBER.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType[PhoneNumberType.PAGER.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType[PhoneNumberType.UAN.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType[PhoneNumberType.VOICEMAIL.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberFormat = new int[PhoneNumberFormat.values().length];
            try {
                $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberFormat[PhoneNumberFormat.E164.ordinal()] = 1;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberFormat[PhoneNumberFormat.INTERNATIONAL.ordinal()] = 2;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberFormat[PhoneNumberFormat.RFC3966.ordinal()] = 3;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberFormat[PhoneNumberFormat.NATIONAL.ordinal()] = 4;
            } catch (NoSuchFieldError e15) {
            }
            $SwitchMap$com$google$i18n$phonenumbers$Phonenumber$PhoneNumber$CountryCodeSource = new int[Phonenumber.PhoneNumber.CountryCodeSource.values().length];
            try {
                $SwitchMap$com$google$i18n$phonenumbers$Phonenumber$PhoneNumber$CountryCodeSource[Phonenumber.PhoneNumber.CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN.ordinal()] = 1;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$google$i18n$phonenumbers$Phonenumber$PhoneNumber$CountryCodeSource[Phonenumber.PhoneNumber.CountryCodeSource.FROM_NUMBER_WITH_IDD.ordinal()] = 2;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$google$i18n$phonenumbers$Phonenumber$PhoneNumber$CountryCodeSource[Phonenumber.PhoneNumber.CountryCodeSource.FROM_NUMBER_WITHOUT_PLUS_SIGN.ordinal()] = 3;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$google$i18n$phonenumbers$Phonenumber$PhoneNumber$CountryCodeSource[Phonenumber.PhoneNumber.CountryCodeSource.FROM_DEFAULT_COUNTRY.ordinal()] = 4;
            } catch (NoSuchFieldError e19) {
            }
        }
    }

    public enum Leniency {
        POSSIBLE {
            /* access modifiers changed from: package-private */
            public boolean verify(Phonenumber.PhoneNumber number, CharSequence candidate, PhoneNumberUtil util) {
                return util.isPossibleNumber(number);
            }
        },
        VALID {
            /* access modifiers changed from: package-private */
            public boolean verify(Phonenumber.PhoneNumber number, CharSequence candidate, PhoneNumberUtil util) {
                if (!util.isValidNumber(number) || !PhoneNumberMatcher.containsOnlyValidXChars(number, candidate.toString(), util)) {
                    return false;
                }
                return PhoneNumberMatcher.isNationalPrefixPresentIfRequired(number, util);
            }
        },
        STRICT_GROUPING {
            /* access modifiers changed from: package-private */
            public boolean verify(Phonenumber.PhoneNumber number, CharSequence candidate, PhoneNumberUtil util) {
                String candidateString = candidate.toString();
                if (!util.isValidNumber(number) || !PhoneNumberMatcher.containsOnlyValidXChars(number, candidateString, util) || PhoneNumberMatcher.containsMoreThanOneSlashInNationalNumber(number, candidateString) || !PhoneNumberMatcher.isNationalPrefixPresentIfRequired(number, util)) {
                    return false;
                }
                return PhoneNumberMatcher.checkNumberGroupingIsValid(number, candidate, util, new PhoneNumberMatcher.NumberGroupingChecker() {
                    public boolean checkGroups(PhoneNumberUtil util, Phonenumber.PhoneNumber number, StringBuilder normalizedCandidate, String[] expectedNumberGroups) {
                        return PhoneNumberMatcher.allNumberGroupsRemainGrouped(util, number, normalizedCandidate, expectedNumberGroups);
                    }
                });
            }
        },
        EXACT_GROUPING {
            /* access modifiers changed from: package-private */
            public boolean verify(Phonenumber.PhoneNumber number, CharSequence candidate, PhoneNumberUtil util) {
                String candidateString = candidate.toString();
                if (!util.isValidNumber(number) || !PhoneNumberMatcher.containsOnlyValidXChars(number, candidateString, util) || PhoneNumberMatcher.containsMoreThanOneSlashInNationalNumber(number, candidateString) || !PhoneNumberMatcher.isNationalPrefixPresentIfRequired(number, util)) {
                    return false;
                }
                return PhoneNumberMatcher.checkNumberGroupingIsValid(number, candidate, util, new PhoneNumberMatcher.NumberGroupingChecker() {
                    public boolean checkGroups(PhoneNumberUtil util, Phonenumber.PhoneNumber number, StringBuilder normalizedCandidate, String[] expectedNumberGroups) {
                        return PhoneNumberMatcher.allNumberGroupsAreExactlyPresent(util, number, normalizedCandidate, expectedNumberGroups);
                    }
                });
            }
        };

        /* access modifiers changed from: package-private */
        public abstract boolean verify(Phonenumber.PhoneNumber phoneNumber, CharSequence charSequence, PhoneNumberUtil phoneNumberUtil);
    }

    public enum MatchType {
        NOT_A_NUMBER,
        NO_MATCH,
        SHORT_NSN_MATCH,
        NSN_MATCH,
        EXACT_MATCH
    }

    public enum PhoneNumberFormat {
        E164,
        INTERNATIONAL,
        NATIONAL,
        RFC3966
    }

    public enum PhoneNumberType {
        FIXED_LINE,
        MOBILE,
        FIXED_LINE_OR_MOBILE,
        TOLL_FREE,
        PREMIUM_RATE,
        SHARED_COST,
        VOIP,
        PERSONAL_NUMBER,
        PAGER,
        UAN,
        VOICEMAIL,
        UNKNOWN
    }

    public enum ValidationResult {
        IS_POSSIBLE,
        IS_POSSIBLE_LOCAL_ONLY,
        INVALID_COUNTRY_CODE,
        TOO_SHORT,
        INVALID_LENGTH,
        TOO_LONG
    }

    static {
        HashMap<Integer, String> mobileTokenMap = new HashMap<>();
        mobileTokenMap.put(52, "1");
        mobileTokenMap.put(54, "9");
        MOBILE_TOKEN_MAPPINGS = Collections.unmodifiableMap(mobileTokenMap);
        HashSet<Integer> geoMobileCountriesWithoutMobileAreaCodes = new HashSet<>();
        geoMobileCountriesWithoutMobileAreaCodes.add(86);
        GEO_MOBILE_COUNTRIES_WITHOUT_MOBILE_AREA_CODES = Collections.unmodifiableSet(geoMobileCountriesWithoutMobileAreaCodes);
        HashSet<Integer> geoMobileCountries = new HashSet<>();
        geoMobileCountries.add(52);
        geoMobileCountries.add(54);
        geoMobileCountries.add(55);
        geoMobileCountries.add(62);
        geoMobileCountries.addAll(geoMobileCountriesWithoutMobileAreaCodes);
        GEO_MOBILE_COUNTRIES = Collections.unmodifiableSet(geoMobileCountries);
        HashMap<Character, Character> asciiDigitMappings = new HashMap<>();
        asciiDigitMappings.put('0', '0');
        asciiDigitMappings.put('1', '1');
        asciiDigitMappings.put('2', '2');
        asciiDigitMappings.put('3', '3');
        asciiDigitMappings.put('4', '4');
        asciiDigitMappings.put('5', '5');
        asciiDigitMappings.put('6', '6');
        asciiDigitMappings.put('7', '7');
        asciiDigitMappings.put('8', '8');
        asciiDigitMappings.put('9', '9');
        HashMap<Character, Character> alphaMap = new HashMap<>(40);
        alphaMap.put('A', '2');
        alphaMap.put('B', '2');
        alphaMap.put('C', '2');
        alphaMap.put('D', '3');
        alphaMap.put('E', '3');
        alphaMap.put('F', '3');
        alphaMap.put('G', '4');
        alphaMap.put('H', '4');
        alphaMap.put('I', '4');
        alphaMap.put('J', '5');
        alphaMap.put('K', '5');
        alphaMap.put('L', '5');
        alphaMap.put('M', '6');
        alphaMap.put('N', '6');
        alphaMap.put('O', '6');
        alphaMap.put('P', '7');
        alphaMap.put('Q', '7');
        alphaMap.put('R', '7');
        alphaMap.put('S', '7');
        alphaMap.put('T', '8');
        alphaMap.put('U', '8');
        alphaMap.put('V', '8');
        alphaMap.put('W', '9');
        alphaMap.put('X', '9');
        alphaMap.put('Y', '9');
        alphaMap.put('Z', '9');
        ALPHA_MAPPINGS = Collections.unmodifiableMap(alphaMap);
        HashMap<Character, Character> combinedMap = new HashMap<>(100);
        combinedMap.putAll(ALPHA_MAPPINGS);
        combinedMap.putAll(asciiDigitMappings);
        ALPHA_PHONE_MAPPINGS = Collections.unmodifiableMap(combinedMap);
        HashMap<Character, Character> diallableCharMap = new HashMap<>();
        diallableCharMap.putAll(asciiDigitMappings);
        diallableCharMap.put(Character.valueOf(PLUS_SIGN), Character.valueOf(PLUS_SIGN));
        diallableCharMap.put(Character.valueOf(STAR_SIGN), Character.valueOf(STAR_SIGN));
        diallableCharMap.put('#', '#');
        DIALLABLE_CHAR_MAPPINGS = Collections.unmodifiableMap(diallableCharMap);
        HashMap<Character, Character> allPlusNumberGroupings = new HashMap<>();
        for (Character charValue : ALPHA_MAPPINGS.keySet()) {
            char c = charValue.charValue();
            allPlusNumberGroupings.put(Character.valueOf(Character.toLowerCase(c)), Character.valueOf(c));
            allPlusNumberGroupings.put(Character.valueOf(c), Character.valueOf(c));
        }
        allPlusNumberGroupings.putAll(asciiDigitMappings);
        allPlusNumberGroupings.put('-', '-');
        allPlusNumberGroupings.put(65293, '-');
        allPlusNumberGroupings.put(8208, '-');
        allPlusNumberGroupings.put(8209, '-');
        allPlusNumberGroupings.put(8210, '-');
        allPlusNumberGroupings.put(8211, '-');
        allPlusNumberGroupings.put(8212, '-');
        allPlusNumberGroupings.put(8213, '-');
        allPlusNumberGroupings.put(8722, '-');
        allPlusNumberGroupings.put('/', '/');
        allPlusNumberGroupings.put(65295, '/');
        allPlusNumberGroupings.put(' ', ' ');
        allPlusNumberGroupings.put(12288, ' ');
        allPlusNumberGroupings.put(8288, ' ');
        allPlusNumberGroupings.put('.', '.');
        allPlusNumberGroupings.put(65294, '.');
        ALL_PLUS_NUMBER_GROUPING_SYMBOLS = Collections.unmodifiableMap(allPlusNumberGroupings);
        StringBuilder sb = new StringBuilder();
        sb.append(",;");
        sb.append("xｘ#＃~～");
        EXTN_PATTERNS_FOR_PARSING = createExtnPattern(sb.toString());
    }

    private static String createExtnPattern(String singleExtnSymbols) {
        return ";ext=(\\p{Nd}{1,7})|[  \\t,]*(?:e?xt(?:ensi(?:ó?|ó))?n?|ｅ?ｘｔｎ?|[" + singleExtnSymbols + "]|int|anexo|ｉｎｔ)[:\\.．]?[  \\t,-]*" + CAPTURING_EXTN_DIGITS + "#?|[- ]+(" + DIGITS + "{1,5})#";
    }

    PhoneNumberUtil(MetadataSource metadataSource2, Map<Integer, List<String>> countryCallingCodeToRegionCodeMap2) {
        this.metadataSource = metadataSource2;
        this.countryCallingCodeToRegionCodeMap = countryCallingCodeToRegionCodeMap2;
        for (Map.Entry<Integer, List<String>> entry : countryCallingCodeToRegionCodeMap2.entrySet()) {
            List<String> regionCodes = entry.getValue();
            if (regionCodes.size() != 1 || !REGION_CODE_FOR_NON_GEO_ENTITY.equals(regionCodes.get(0))) {
                this.supportedRegions.addAll(regionCodes);
            } else {
                this.countryCodesForNonGeographicalRegion.add(entry.getKey());
            }
        }
        if (this.supportedRegions.remove(REGION_CODE_FOR_NON_GEO_ENTITY)) {
            logger.log(Level.WARNING, "invalid metadata (country calling code was mapped to the non-geo entity as well as specific region(s))");
        }
        this.nanpaRegions.addAll(countryCallingCodeToRegionCodeMap2.get(1));
    }

    static CharSequence extractPossibleNumber(CharSequence number) {
        Matcher m = VALID_START_CHAR_PATTERN.matcher(number);
        if (!m.find()) {
            return "";
        }
        CharSequence number2 = number.subSequence(m.start(), number.length());
        Matcher trailingCharsMatcher = UNWANTED_END_CHAR_PATTERN.matcher(number2);
        if (trailingCharsMatcher.find()) {
            number2 = number2.subSequence(0, trailingCharsMatcher.start());
        }
        Matcher secondNumber = SECOND_NUMBER_START_PATTERN.matcher(number2);
        if (secondNumber.find()) {
            number2 = number2.subSequence(0, secondNumber.start());
        }
        return number2;
    }

    static boolean isViablePhoneNumber(CharSequence number) {
        if (number.length() < 2) {
            return false;
        }
        return VALID_PHONE_NUMBER_PATTERN.matcher(number).matches();
    }

    static StringBuilder normalize(StringBuilder number) {
        if (VALID_ALPHA_PHONE_PATTERN.matcher(number).matches()) {
            number.replace(0, number.length(), normalizeHelper(number, ALPHA_PHONE_MAPPINGS, true));
        } else {
            number.replace(0, number.length(), normalizeDigitsOnly(number));
        }
        return number;
    }

    public static String normalizeDigitsOnly(CharSequence number) {
        return normalizeDigits(number, false).toString();
    }

    static StringBuilder normalizeDigits(CharSequence number, boolean keepNonDigits) {
        StringBuilder normalizedDigits = new StringBuilder(number.length());
        for (int i = 0; i < number.length(); i++) {
            char c = number.charAt(i);
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                normalizedDigits.append(digit);
            } else if (keepNonDigits) {
                normalizedDigits.append(c);
            }
        }
        return normalizedDigits;
    }

    public static String normalizeDiallableCharsOnly(CharSequence number) {
        return normalizeHelper(number, DIALLABLE_CHAR_MAPPINGS, true);
    }

    public static String convertAlphaCharactersInNumber(CharSequence number) {
        return normalizeHelper(number, ALPHA_PHONE_MAPPINGS, false);
    }

    public int getLengthOfGeographicalAreaCode(Phonenumber.PhoneNumber number) {
        Phonemetadata.PhoneMetadata metadata = getMetadataForRegion(getRegionCodeForNumber(number));
        if (metadata == null) {
            return 0;
        }
        if (!metadata.hasNationalPrefix() && !number.isItalianLeadingZero()) {
            return 0;
        }
        PhoneNumberType type = getNumberType(number);
        int countryCallingCode = number.getCountryCode();
        if ((type != PhoneNumberType.MOBILE || !GEO_MOBILE_COUNTRIES_WITHOUT_MOBILE_AREA_CODES.contains(Integer.valueOf(countryCallingCode))) && isNumberGeographical(type, countryCallingCode)) {
            return getLengthOfNationalDestinationCode(number);
        }
        return 0;
    }

    public int getLengthOfNationalDestinationCode(Phonenumber.PhoneNumber number) {
        Phonenumber.PhoneNumber copiedProto;
        if (number.hasExtension()) {
            copiedProto = new Phonenumber.PhoneNumber();
            copiedProto.mergeFrom(number);
            copiedProto.clearExtension();
        } else {
            copiedProto = number;
        }
        String[] numberGroups = NON_DIGITS_PATTERN.split(format(copiedProto, PhoneNumberFormat.INTERNATIONAL));
        if (numberGroups.length <= 3) {
            return 0;
        }
        if (getNumberType(number) != PhoneNumberType.MOBILE || getCountryMobileToken(number.getCountryCode()).equals("")) {
            return numberGroups[2].length();
        }
        return numberGroups[2].length() + numberGroups[3].length();
    }

    public static String getCountryMobileToken(int countryCallingCode) {
        if (MOBILE_TOKEN_MAPPINGS.containsKey(Integer.valueOf(countryCallingCode))) {
            return MOBILE_TOKEN_MAPPINGS.get(Integer.valueOf(countryCallingCode));
        }
        return "";
    }

    private static String normalizeHelper(CharSequence number, Map<Character, Character> normalizationReplacements, boolean removeNonMatches) {
        StringBuilder normalizedNumber = new StringBuilder(number.length());
        for (int i = 0; i < number.length(); i++) {
            char character = number.charAt(i);
            Character newDigit = normalizationReplacements.get(Character.valueOf(Character.toUpperCase(character)));
            if (newDigit != null) {
                normalizedNumber.append(newDigit);
            } else if (!removeNonMatches) {
                normalizedNumber.append(character);
            }
        }
        return normalizedNumber.toString();
    }

    static synchronized void setInstance(PhoneNumberUtil util) {
        synchronized (PhoneNumberUtil.class) {
            instance = util;
        }
    }

    public Set<String> getSupportedRegions() {
        return Collections.unmodifiableSet(this.supportedRegions);
    }

    public Set<Integer> getSupportedGlobalNetworkCallingCodes() {
        return Collections.unmodifiableSet(this.countryCodesForNonGeographicalRegion);
    }

    public Set<Integer> getSupportedCallingCodes() {
        return Collections.unmodifiableSet(this.countryCallingCodeToRegionCodeMap.keySet());
    }

    private static boolean descHasPossibleNumberData(Phonemetadata.PhoneNumberDesc desc) {
        return (desc.getPossibleLengthCount() == 1 && desc.getPossibleLength(0) == -1) ? false : true;
    }

    private static boolean descHasData(Phonemetadata.PhoneNumberDesc desc) {
        return desc.hasExampleNumber() || descHasPossibleNumberData(desc) || desc.hasNationalNumberPattern();
    }

    private Set<PhoneNumberType> getSupportedTypesForMetadata(Phonemetadata.PhoneMetadata metadata) {
        Set<PhoneNumberType> types = new TreeSet<>();
        for (PhoneNumberType type : PhoneNumberType.values()) {
            if (!(type == PhoneNumberType.FIXED_LINE_OR_MOBILE || type == PhoneNumberType.UNKNOWN || !descHasData(getNumberDescByType(metadata, type)))) {
                types.add(type);
            }
        }
        return Collections.unmodifiableSet(types);
    }

    public Set<PhoneNumberType> getSupportedTypesForRegion(String regionCode) {
        if (isValidRegionCode(regionCode)) {
            return getSupportedTypesForMetadata(getMetadataForRegion(regionCode));
        }
        Logger logger2 = logger;
        Level level = Level.WARNING;
        logger2.log(level, "Invalid or unknown region code provided: " + regionCode);
        return Collections.unmodifiableSet(new TreeSet());
    }

    public Set<PhoneNumberType> getSupportedTypesForNonGeoEntity(int countryCallingCode) {
        Phonemetadata.PhoneMetadata metadata = getMetadataForNonGeographicalRegion(countryCallingCode);
        if (metadata != null) {
            return getSupportedTypesForMetadata(metadata);
        }
        Logger logger2 = logger;
        Level level = Level.WARNING;
        logger2.log(level, "Unknown country calling code for a non-geographical entity provided: " + countryCallingCode);
        return Collections.unmodifiableSet(new TreeSet());
    }

    public static synchronized PhoneNumberUtil getInstance() {
        PhoneNumberUtil phoneNumberUtil;
        synchronized (PhoneNumberUtil.class) {
            if (instance == null) {
                setInstance(createInstance(MetadataManager.DEFAULT_METADATA_LOADER));
            }
            phoneNumberUtil = instance;
        }
        return phoneNumberUtil;
    }

    public static PhoneNumberUtil createInstance(MetadataLoader metadataLoader) {
        if (metadataLoader != null) {
            return createInstance((MetadataSource) new MultiFileMetadataSourceImpl(metadataLoader));
        }
        throw new IllegalArgumentException("metadataLoader could not be null.");
    }

    private static PhoneNumberUtil createInstance(MetadataSource metadataSource2) {
        if (metadataSource2 != null) {
            return new PhoneNumberUtil(metadataSource2, CountryCodeToRegionCodeMap.getCountryCodeToRegionCodeMap());
        }
        throw new IllegalArgumentException("metadataSource could not be null.");
    }

    static boolean formattingRuleHasFirstGroupOnly(String nationalPrefixFormattingRule) {
        return nationalPrefixFormattingRule.length() == 0 || FIRST_GROUP_ONLY_PREFIX_PATTERN.matcher(nationalPrefixFormattingRule).matches();
    }

    public boolean isNumberGeographical(Phonenumber.PhoneNumber phoneNumber) {
        return isNumberGeographical(getNumberType(phoneNumber), phoneNumber.getCountryCode());
    }

    public boolean isNumberGeographical(PhoneNumberType phoneNumberType, int countryCallingCode) {
        return phoneNumberType == PhoneNumberType.FIXED_LINE || phoneNumberType == PhoneNumberType.FIXED_LINE_OR_MOBILE || (GEO_MOBILE_COUNTRIES.contains(Integer.valueOf(countryCallingCode)) && phoneNumberType == PhoneNumberType.MOBILE);
    }

    private boolean isValidRegionCode(String regionCode) {
        return regionCode != null && this.supportedRegions.contains(regionCode);
    }

    private boolean hasValidCountryCallingCode(int countryCallingCode) {
        return this.countryCallingCodeToRegionCodeMap.containsKey(Integer.valueOf(countryCallingCode));
    }

    public String format(Phonenumber.PhoneNumber number, PhoneNumberFormat numberFormat) {
        if (number.getNationalNumber() == 0 && number.hasRawInput()) {
            String rawInput = number.getRawInput();
            if (rawInput.length() > 0) {
                return rawInput;
            }
        }
        StringBuilder formattedNumber = new StringBuilder(20);
        format(number, numberFormat, formattedNumber);
        return formattedNumber.toString();
    }

    public void format(Phonenumber.PhoneNumber number, PhoneNumberFormat numberFormat, StringBuilder formattedNumber) {
        formattedNumber.setLength(0);
        int countryCallingCode = number.getCountryCode();
        String nationalSignificantNumber = getNationalSignificantNumber(number);
        if (numberFormat == PhoneNumberFormat.E164) {
            formattedNumber.append(nationalSignificantNumber);
            prefixNumberWithCountryCallingCode(countryCallingCode, PhoneNumberFormat.E164, formattedNumber);
        } else if (!hasValidCountryCallingCode(countryCallingCode)) {
            formattedNumber.append(nationalSignificantNumber);
        } else {
            Phonemetadata.PhoneMetadata metadata = getMetadataForRegionOrCallingCode(countryCallingCode, getRegionCodeForCountryCode(countryCallingCode));
            formattedNumber.append(formatNsn(nationalSignificantNumber, metadata, numberFormat));
            maybeAppendFormattedExtension(number, metadata, numberFormat, formattedNumber);
            prefixNumberWithCountryCallingCode(countryCallingCode, numberFormat, formattedNumber);
        }
    }

    public String formatByPattern(Phonenumber.PhoneNumber number, PhoneNumberFormat numberFormat, List<Phonemetadata.NumberFormat> userDefinedFormats) {
        int countryCallingCode = number.getCountryCode();
        String nationalSignificantNumber = getNationalSignificantNumber(number);
        if (!hasValidCountryCallingCode(countryCallingCode)) {
            return nationalSignificantNumber;
        }
        Phonemetadata.PhoneMetadata metadata = getMetadataForRegionOrCallingCode(countryCallingCode, getRegionCodeForCountryCode(countryCallingCode));
        StringBuilder formattedNumber = new StringBuilder(20);
        Phonemetadata.NumberFormat formattingPattern = chooseFormattingPatternForNumber(userDefinedFormats, nationalSignificantNumber);
        if (formattingPattern == null) {
            formattedNumber.append(nationalSignificantNumber);
        } else {
            Phonemetadata.NumberFormat.Builder numFormatCopy = Phonemetadata.NumberFormat.newBuilder();
            numFormatCopy.mergeFrom(formattingPattern);
            String nationalPrefixFormattingRule = formattingPattern.getNationalPrefixFormattingRule();
            if (nationalPrefixFormattingRule.length() > 0) {
                String nationalPrefix = metadata.getNationalPrefix();
                if (nationalPrefix.length() > 0) {
                    numFormatCopy.setNationalPrefixFormattingRule(nationalPrefixFormattingRule.replace(NP_STRING, nationalPrefix).replace(FG_STRING, "$1"));
                } else {
                    numFormatCopy.clearNationalPrefixFormattingRule();
                }
            }
            formattedNumber.append(formatNsnUsingPattern(nationalSignificantNumber, numFormatCopy, numberFormat));
        }
        maybeAppendFormattedExtension(number, metadata, numberFormat, formattedNumber);
        prefixNumberWithCountryCallingCode(countryCallingCode, numberFormat, formattedNumber);
        return formattedNumber.toString();
    }

    public String formatNationalNumberWithCarrierCode(Phonenumber.PhoneNumber number, CharSequence carrierCode) {
        int countryCallingCode = number.getCountryCode();
        String nationalSignificantNumber = getNationalSignificantNumber(number);
        if (!hasValidCountryCallingCode(countryCallingCode)) {
            return nationalSignificantNumber;
        }
        Phonemetadata.PhoneMetadata metadata = getMetadataForRegionOrCallingCode(countryCallingCode, getRegionCodeForCountryCode(countryCallingCode));
        StringBuilder formattedNumber = new StringBuilder(20);
        formattedNumber.append(formatNsn(nationalSignificantNumber, metadata, PhoneNumberFormat.NATIONAL, carrierCode));
        maybeAppendFormattedExtension(number, metadata, PhoneNumberFormat.NATIONAL, formattedNumber);
        prefixNumberWithCountryCallingCode(countryCallingCode, PhoneNumberFormat.NATIONAL, formattedNumber);
        return formattedNumber.toString();
    }

    private Phonemetadata.PhoneMetadata getMetadataForRegionOrCallingCode(int countryCallingCode, String regionCode) {
        if (REGION_CODE_FOR_NON_GEO_ENTITY.equals(regionCode)) {
            return getMetadataForNonGeographicalRegion(countryCallingCode);
        }
        return getMetadataForRegion(regionCode);
    }

    public String formatNationalNumberWithPreferredCarrierCode(Phonenumber.PhoneNumber number, CharSequence fallbackCarrierCode) {
        CharSequence charSequence;
        if (number.getPreferredDomesticCarrierCode().length() > 0) {
            charSequence = number.getPreferredDomesticCarrierCode();
        } else {
            charSequence = fallbackCarrierCode;
        }
        return formatNationalNumberWithCarrierCode(number, charSequence);
    }

    public String formatNumberForMobileDialing(Phonenumber.PhoneNumber number, String regionCallingFrom, boolean withFormatting) {
        String str;
        String str2;
        String str3;
        int countryCallingCode = number.getCountryCode();
        if (!hasValidCountryCallingCode(countryCallingCode)) {
            return number.hasRawInput() ? number.getRawInput() : "";
        }
        String formattedNumber = "";
        Phonenumber.PhoneNumber numberNoExt = new Phonenumber.PhoneNumber().mergeFrom(number).clearExtension();
        String regionCode = getRegionCodeForCountryCode(countryCallingCode);
        PhoneNumberType numberType = getNumberType(numberNoExt);
        boolean isFixedLineOrMobile = false;
        boolean isValidNumber = numberType != PhoneNumberType.UNKNOWN;
        if (regionCallingFrom.equals(regionCode)) {
            if (numberType == PhoneNumberType.FIXED_LINE || numberType == PhoneNumberType.MOBILE || numberType == PhoneNumberType.FIXED_LINE_OR_MOBILE) {
                isFixedLineOrMobile = true;
            }
            if (regionCode.equals("CO") && numberType == PhoneNumberType.FIXED_LINE) {
                formattedNumber = formatNationalNumberWithCarrierCode(numberNoExt, COLOMBIA_MOBILE_TO_FIXED_LINE_PREFIX);
            } else if (regionCode.equals("BR") && isFixedLineOrMobile) {
                if (numberNoExt.getPreferredDomesticCarrierCode().length() > 0) {
                    str3 = formatNationalNumberWithPreferredCarrierCode(numberNoExt, "");
                    String formattedNumber2 = str3;
                } else {
                    str3 = "";
                }
                formattedNumber = str3;
            } else if (isValidNumber && regionCode.equals("HU")) {
                formattedNumber = getNddPrefixForRegion(regionCode, true) + Separators.SP + format(numberNoExt, PhoneNumberFormat.NATIONAL);
            } else if (countryCallingCode == 1) {
                formattedNumber = (!canBeInternationallyDialled(numberNoExt) || testNumberLength(getNationalSignificantNumber(numberNoExt), getMetadataForRegion(regionCallingFrom)) == ValidationResult.TOO_SHORT) ? format(numberNoExt, PhoneNumberFormat.NATIONAL) : format(numberNoExt, PhoneNumberFormat.INTERNATIONAL);
            } else {
                formattedNumber = ((regionCode.equals(REGION_CODE_FOR_NON_GEO_ENTITY) || ((regionCode.equals("MX") || regionCode.equals("CL")) && isFixedLineOrMobile)) && canBeInternationallyDialled(numberNoExt)) ? format(numberNoExt, PhoneNumberFormat.INTERNATIONAL) : format(numberNoExt, PhoneNumberFormat.NATIONAL);
            }
        } else if (isValidNumber && canBeInternationallyDialled(numberNoExt)) {
            if (withFormatting) {
                str2 = format(numberNoExt, PhoneNumberFormat.INTERNATIONAL);
            } else {
                str2 = format(numberNoExt, PhoneNumberFormat.E164);
            }
            return str2;
        }
        if (withFormatting) {
            str = formattedNumber;
        } else {
            str = normalizeDiallableCharsOnly(formattedNumber);
        }
        return str;
    }

    public String formatOutOfCountryCallingNumber(Phonenumber.PhoneNumber number, String regionCallingFrom) {
        if (!isValidRegionCode(regionCallingFrom)) {
            Logger logger2 = logger;
            Level level = Level.WARNING;
            logger2.log(level, "Trying to format number from invalid region " + regionCallingFrom + ". International formatting applied.");
            return format(number, PhoneNumberFormat.INTERNATIONAL);
        }
        int countryCallingCode = number.getCountryCode();
        String nationalSignificantNumber = getNationalSignificantNumber(number);
        if (!hasValidCountryCallingCode(countryCallingCode)) {
            return nationalSignificantNumber;
        }
        if (countryCallingCode == 1) {
            if (isNANPACountry(regionCallingFrom)) {
                return countryCallingCode + Separators.SP + format(number, PhoneNumberFormat.NATIONAL);
            }
        } else if (countryCallingCode == getCountryCodeForValidRegion(regionCallingFrom)) {
            return format(number, PhoneNumberFormat.NATIONAL);
        }
        Phonemetadata.PhoneMetadata metadataForRegionCallingFrom = getMetadataForRegion(regionCallingFrom);
        String internationalPrefix = metadataForRegionCallingFrom.getInternationalPrefix();
        String internationalPrefixForFormatting = "";
        if (SINGLE_INTERNATIONAL_PREFIX.matcher(internationalPrefix).matches()) {
            internationalPrefixForFormatting = internationalPrefix;
        } else if (metadataForRegionCallingFrom.hasPreferredInternationalPrefix()) {
            internationalPrefixForFormatting = metadataForRegionCallingFrom.getPreferredInternationalPrefix();
        }
        Phonemetadata.PhoneMetadata metadataForRegion = getMetadataForRegionOrCallingCode(countryCallingCode, getRegionCodeForCountryCode(countryCallingCode));
        StringBuilder formattedNumber = new StringBuilder(formatNsn(nationalSignificantNumber, metadataForRegion, PhoneNumberFormat.INTERNATIONAL));
        maybeAppendFormattedExtension(number, metadataForRegion, PhoneNumberFormat.INTERNATIONAL, formattedNumber);
        if (internationalPrefixForFormatting.length() > 0) {
            formattedNumber.insert(0, Separators.SP).insert(0, countryCallingCode).insert(0, Separators.SP).insert(0, internationalPrefixForFormatting);
        } else {
            prefixNumberWithCountryCallingCode(countryCallingCode, PhoneNumberFormat.INTERNATIONAL, formattedNumber);
        }
        return formattedNumber.toString();
    }

    public String formatInOriginalFormat(Phonenumber.PhoneNumber number, String regionCallingFrom) {
        String formattedNumber;
        String regionCode;
        if (number.hasRawInput() && !hasFormattingPatternForNumber(number)) {
            return number.getRawInput();
        }
        if (!number.hasCountryCodeSource()) {
            return format(number, PhoneNumberFormat.NATIONAL);
        }
        switch (number.getCountryCodeSource()) {
            case FROM_NUMBER_WITH_PLUS_SIGN:
                regionCode = format(number, PhoneNumberFormat.INTERNATIONAL);
                break;
            case FROM_NUMBER_WITH_IDD:
                regionCode = formatOutOfCountryCallingNumber(number, regionCallingFrom);
                break;
            case FROM_NUMBER_WITHOUT_PLUS_SIGN:
                regionCode = format(number, PhoneNumberFormat.INTERNATIONAL).substring(1);
                break;
            default:
                String regionCode2 = getRegionCodeForCountryCode(number.getCountryCode());
                String nationalPrefix = getNddPrefixForRegion(regionCode2, true);
                String nationalFormat = format(number, PhoneNumberFormat.NATIONAL);
                if (nationalPrefix != null && nationalPrefix.length() != 0) {
                    if (!rawInputContainsNationalPrefix(number.getRawInput(), nationalPrefix, regionCode2)) {
                        Phonemetadata.PhoneMetadata metadata = getMetadataForRegion(regionCode2);
                        Phonemetadata.NumberFormat formatRule = chooseFormattingPatternForNumber(metadata.numberFormats(), getNationalSignificantNumber(number));
                        if (formatRule != null) {
                            String candidateNationalPrefixRule = formatRule.getNationalPrefixFormattingRule();
                            int indexOfFirstGroup = candidateNationalPrefixRule.indexOf("$1");
                            if (indexOfFirstGroup > 0) {
                                if (normalizeDigitsOnly(candidateNationalPrefixRule.substring(0, indexOfFirstGroup)).length() != 0) {
                                    Phonemetadata.NumberFormat.Builder numFormatCopy = Phonemetadata.NumberFormat.newBuilder();
                                    numFormatCopy.mergeFrom(formatRule);
                                    numFormatCopy.clearNationalPrefixFormattingRule();
                                    List<Phonemetadata.NumberFormat> numberFormats = new ArrayList<>(1);
                                    numberFormats.add(numFormatCopy);
                                    regionCode = formatByPattern(number, PhoneNumberFormat.NATIONAL, numberFormats);
                                    break;
                                } else {
                                    formattedNumber = nationalFormat;
                                    break;
                                }
                            } else {
                                formattedNumber = nationalFormat;
                                break;
                            }
                        } else {
                            formattedNumber = nationalFormat;
                            break;
                        }
                    } else {
                        formattedNumber = nationalFormat;
                        break;
                    }
                } else {
                    formattedNumber = nationalFormat;
                    break;
                }
                break;
        }
        formattedNumber = regionCode;
        String formattedNumber2 = formattedNumber;
        String rawInput = number.getRawInput();
        if (formattedNumber2 != null && rawInput.length() > 0 && !normalizeDiallableCharsOnly(formattedNumber2).equals(normalizeDiallableCharsOnly(rawInput))) {
            formattedNumber2 = rawInput;
        }
        return formattedNumber2;
    }

    private boolean rawInputContainsNationalPrefix(String rawInput, String nationalPrefix, String regionCode) {
        String normalizedNationalNumber = normalizeDigitsOnly(rawInput);
        if (!normalizedNationalNumber.startsWith(nationalPrefix)) {
            return false;
        }
        try {
            return isValidNumber(parse(normalizedNationalNumber.substring(nationalPrefix.length()), regionCode));
        } catch (NumberParseException e) {
            return false;
        }
    }

    private boolean hasFormattingPatternForNumber(Phonenumber.PhoneNumber number) {
        int countryCallingCode = number.getCountryCode();
        Phonemetadata.PhoneMetadata metadata = getMetadataForRegionOrCallingCode(countryCallingCode, getRegionCodeForCountryCode(countryCallingCode));
        boolean z = false;
        if (metadata == null) {
            return false;
        }
        if (chooseFormattingPatternForNumber(metadata.numberFormats(), getNationalSignificantNumber(number)) != null) {
            z = true;
        }
        return z;
    }

    public String formatOutOfCountryKeepingAlphaChars(Phonenumber.PhoneNumber number, String regionCallingFrom) {
        String rawInput = number.getRawInput();
        if (rawInput.length() == 0) {
            return formatOutOfCountryCallingNumber(number, regionCallingFrom);
        }
        int countryCode = number.getCountryCode();
        if (!hasValidCountryCallingCode(countryCode)) {
            return rawInput;
        }
        String rawInput2 = normalizeHelper(rawInput, ALL_PLUS_NUMBER_GROUPING_SYMBOLS, true);
        String nationalNumber = getNationalSignificantNumber(number);
        if (nationalNumber.length() > 3) {
            int firstNationalNumberDigit = rawInput2.indexOf(nationalNumber.substring(0, 3));
            if (firstNationalNumberDigit != -1) {
                rawInput2 = rawInput2.substring(firstNationalNumberDigit);
            }
        }
        Phonemetadata.PhoneMetadata metadataForRegionCallingFrom = getMetadataForRegion(regionCallingFrom);
        if (countryCode == 1) {
            if (isNANPACountry(regionCallingFrom)) {
                return countryCode + Separators.SP + rawInput2;
            }
        } else if (metadataForRegionCallingFrom != null && countryCode == getCountryCodeForValidRegion(regionCallingFrom)) {
            Phonemetadata.NumberFormat formattingPattern = chooseFormattingPatternForNumber(metadataForRegionCallingFrom.numberFormats(), nationalNumber);
            if (formattingPattern == null) {
                return rawInput2;
            }
            Phonemetadata.NumberFormat.Builder newFormat = Phonemetadata.NumberFormat.newBuilder();
            newFormat.mergeFrom(formattingPattern);
            newFormat.setPattern("(\\d+)(.*)");
            newFormat.setFormat("$1$2");
            return formatNsnUsingPattern(rawInput2, newFormat, PhoneNumberFormat.NATIONAL);
        }
        String internationalPrefixForFormatting = "";
        if (metadataForRegionCallingFrom != null) {
            String internationalPrefix = metadataForRegionCallingFrom.getInternationalPrefix();
            internationalPrefixForFormatting = SINGLE_INTERNATIONAL_PREFIX.matcher(internationalPrefix).matches() ? internationalPrefix : metadataForRegionCallingFrom.getPreferredInternationalPrefix();
        }
        StringBuilder formattedNumber = new StringBuilder(rawInput2);
        maybeAppendFormattedExtension(number, getMetadataForRegionOrCallingCode(countryCode, getRegionCodeForCountryCode(countryCode)), PhoneNumberFormat.INTERNATIONAL, formattedNumber);
        if (internationalPrefixForFormatting.length() > 0) {
            formattedNumber.insert(0, Separators.SP).insert(0, countryCode).insert(0, Separators.SP).insert(0, internationalPrefixForFormatting);
        } else {
            if (!isValidRegionCode(regionCallingFrom)) {
                Logger logger2 = logger;
                Level level = Level.WARNING;
                logger2.log(level, "Trying to format number from invalid region " + regionCallingFrom + ". International formatting applied.");
            }
            prefixNumberWithCountryCallingCode(countryCode, PhoneNumberFormat.INTERNATIONAL, formattedNumber);
        }
        return formattedNumber.toString();
    }

    public String getNationalSignificantNumber(Phonenumber.PhoneNumber number) {
        StringBuilder nationalNumber = new StringBuilder();
        if (number.isItalianLeadingZero() && number.getNumberOfLeadingZeros() > 0) {
            char[] zeros = new char[number.getNumberOfLeadingZeros()];
            Arrays.fill(zeros, '0');
            nationalNumber.append(new String(zeros));
        }
        nationalNumber.append(number.getNationalNumber());
        return nationalNumber.toString();
    }

    private void prefixNumberWithCountryCallingCode(int countryCallingCode, PhoneNumberFormat numberFormat, StringBuilder formattedNumber) {
        switch (numberFormat) {
            case E164:
                formattedNumber.insert(0, countryCallingCode).insert(0, PLUS_SIGN);
                return;
            case INTERNATIONAL:
                formattedNumber.insert(0, Separators.SP).insert(0, countryCallingCode).insert(0, PLUS_SIGN);
                return;
            case RFC3966:
                formattedNumber.insert(0, "-").insert(0, countryCallingCode).insert(0, PLUS_SIGN).insert(0, RFC3966_PREFIX);
                return;
            default:
                return;
        }
    }

    private String formatNsn(String number, Phonemetadata.PhoneMetadata metadata, PhoneNumberFormat numberFormat) {
        return formatNsn(number, metadata, numberFormat, null);
    }

    private String formatNsn(String number, Phonemetadata.PhoneMetadata metadata, PhoneNumberFormat numberFormat, CharSequence carrierCode) {
        List<Phonemetadata.NumberFormat> availableFormats;
        if (metadata.intlNumberFormats().size() == 0 || numberFormat == PhoneNumberFormat.NATIONAL) {
            availableFormats = metadata.numberFormats();
        } else {
            availableFormats = metadata.intlNumberFormats();
        }
        Phonemetadata.NumberFormat formattingPattern = chooseFormattingPatternForNumber(availableFormats, number);
        if (formattingPattern == null) {
            return number;
        }
        return formatNsnUsingPattern(number, formattingPattern, numberFormat, carrierCode);
    }

    /* access modifiers changed from: package-private */
    public Phonemetadata.NumberFormat chooseFormattingPatternForNumber(List<Phonemetadata.NumberFormat> availableFormats, String nationalNumber) {
        for (Phonemetadata.NumberFormat numFormat : availableFormats) {
            int size = numFormat.leadingDigitsPatternSize();
            if ((size == 0 || this.regexCache.getPatternForRegex(numFormat.getLeadingDigitsPattern(size - 1)).matcher(nationalNumber).lookingAt()) && this.regexCache.getPatternForRegex(numFormat.getPattern()).matcher(nationalNumber).matches()) {
                return numFormat;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public String formatNsnUsingPattern(String nationalNumber, Phonemetadata.NumberFormat formattingPattern, PhoneNumberFormat numberFormat) {
        return formatNsnUsingPattern(nationalNumber, formattingPattern, numberFormat, null);
    }

    private String formatNsnUsingPattern(String nationalNumber, Phonemetadata.NumberFormat formattingPattern, PhoneNumberFormat numberFormat, CharSequence carrierCode) {
        String formattedNationalNumber;
        String numberFormatRule = formattingPattern.getFormat();
        Matcher m = this.regexCache.getPatternForRegex(formattingPattern.getPattern()).matcher(nationalNumber);
        if (numberFormat != PhoneNumberFormat.NATIONAL || carrierCode == null || carrierCode.length() <= 0 || formattingPattern.getDomesticCarrierCodeFormattingRule().length() <= 0) {
            String nationalPrefixFormattingRule = formattingPattern.getNationalPrefixFormattingRule();
            if (numberFormat != PhoneNumberFormat.NATIONAL || nationalPrefixFormattingRule == null || nationalPrefixFormattingRule.length() <= 0) {
                formattedNationalNumber = m.replaceAll(numberFormatRule);
            } else {
                formattedNationalNumber = m.replaceAll(FIRST_GROUP_PATTERN.matcher(numberFormatRule).replaceFirst(nationalPrefixFormattingRule));
            }
        } else {
            formattedNationalNumber = m.replaceAll(FIRST_GROUP_PATTERN.matcher(numberFormatRule).replaceFirst(formattingPattern.getDomesticCarrierCodeFormattingRule().replace(CC_STRING, carrierCode)));
        }
        if (numberFormat != PhoneNumberFormat.RFC3966) {
            return formattedNationalNumber;
        }
        Matcher matcher = SEPARATOR_PATTERN.matcher(formattedNationalNumber);
        if (matcher.lookingAt()) {
            formattedNationalNumber = matcher.replaceFirst("");
        }
        return matcher.reset(formattedNationalNumber).replaceAll("-");
    }

    public Phonenumber.PhoneNumber getExampleNumber(String regionCode) {
        return getExampleNumberForType(regionCode, PhoneNumberType.FIXED_LINE);
    }

    public Phonenumber.PhoneNumber getInvalidExampleNumber(String regionCode) {
        if (!isValidRegionCode(regionCode)) {
            logger.log(Level.WARNING, "Invalid or unknown region code provided: " + regionCode);
            return null;
        }
        Phonemetadata.PhoneNumberDesc desc = getNumberDescByType(getMetadataForRegion(regionCode), PhoneNumberType.FIXED_LINE);
        if (!desc.hasExampleNumber()) {
            return null;
        }
        String exampleNumber = desc.getExampleNumber();
        int phoneNumberLength = exampleNumber.length();
        while (true) {
            phoneNumberLength--;
            if (phoneNumberLength < 2) {
                return null;
            }
            try {
                Phonenumber.PhoneNumber possiblyValidNumber = parse(exampleNumber.substring(0, phoneNumberLength), regionCode);
                if (!isValidNumber(possiblyValidNumber)) {
                    return possiblyValidNumber;
                }
            } catch (NumberParseException e) {
            }
        }
    }

    public Phonenumber.PhoneNumber getExampleNumberForType(String regionCode, PhoneNumberType type) {
        if (!isValidRegionCode(regionCode)) {
            Logger logger2 = logger;
            Level level = Level.WARNING;
            logger2.log(level, "Invalid or unknown region code provided: " + regionCode);
            return null;
        }
        Phonemetadata.PhoneNumberDesc desc = getNumberDescByType(getMetadataForRegion(regionCode), type);
        try {
            if (desc.hasExampleNumber()) {
                return parse(desc.getExampleNumber(), regionCode);
            }
        } catch (NumberParseException e) {
            logger.log(Level.SEVERE, e.toString());
        }
        return null;
    }

    public Phonenumber.PhoneNumber getExampleNumberForType(PhoneNumberType type) {
        for (String regionCode : getSupportedRegions()) {
            Phonenumber.PhoneNumber exampleNumber = getExampleNumberForType(regionCode, type);
            if (exampleNumber != null) {
                return exampleNumber;
            }
        }
        for (Integer intValue : getSupportedGlobalNetworkCallingCodes()) {
            int countryCallingCode = intValue.intValue();
            Phonemetadata.PhoneNumberDesc desc = getNumberDescByType(getMetadataForNonGeographicalRegion(countryCallingCode), type);
            try {
                if (desc.hasExampleNumber()) {
                    return parse("+" + countryCallingCode + desc.getExampleNumber(), UNKNOWN_REGION);
                }
            } catch (NumberParseException e) {
                logger.log(Level.SEVERE, e.toString());
            }
        }
        return null;
    }

    public Phonenumber.PhoneNumber getExampleNumberForNonGeoEntity(int countryCallingCode) {
        Phonemetadata.PhoneMetadata metadata = getMetadataForNonGeographicalRegion(countryCallingCode);
        if (metadata != null) {
            for (Phonemetadata.PhoneNumberDesc desc : Arrays.asList(new Phonemetadata.PhoneNumberDesc[]{metadata.getMobile(), metadata.getTollFree(), metadata.getSharedCost(), metadata.getVoip(), metadata.getVoicemail(), metadata.getUan(), metadata.getPremiumRate()})) {
                if (desc != null) {
                    try {
                        if (desc.hasExampleNumber()) {
                            return parse("+" + countryCallingCode + desc.getExampleNumber(), UNKNOWN_REGION);
                        }
                    } catch (NumberParseException e) {
                        logger.log(Level.SEVERE, e.toString());
                    }
                }
            }
        } else {
            Logger logger2 = logger;
            Level level = Level.WARNING;
            logger2.log(level, "Invalid or unknown country calling code provided: " + countryCallingCode);
        }
        return null;
    }

    private void maybeAppendFormattedExtension(Phonenumber.PhoneNumber number, Phonemetadata.PhoneMetadata metadata, PhoneNumberFormat numberFormat, StringBuilder formattedNumber) {
        if (number.hasExtension() && number.getExtension().length() > 0) {
            if (numberFormat == PhoneNumberFormat.RFC3966) {
                formattedNumber.append(RFC3966_EXTN_PREFIX);
                formattedNumber.append(number.getExtension());
            } else if (metadata.hasPreferredExtnPrefix()) {
                formattedNumber.append(metadata.getPreferredExtnPrefix());
                formattedNumber.append(number.getExtension());
            } else {
                formattedNumber.append(DEFAULT_EXTN_PREFIX);
                formattedNumber.append(number.getExtension());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Phonemetadata.PhoneNumberDesc getNumberDescByType(Phonemetadata.PhoneMetadata metadata, PhoneNumberType type) {
        switch (AnonymousClass2.$SwitchMap$com$google$i18n$phonenumbers$PhoneNumberUtil$PhoneNumberType[type.ordinal()]) {
            case 1:
                return metadata.getPremiumRate();
            case 2:
                return metadata.getTollFree();
            case 3:
                return metadata.getMobile();
            case 4:
            case 5:
                return metadata.getFixedLine();
            case 6:
                return metadata.getSharedCost();
            case 7:
                return metadata.getVoip();
            case 8:
                return metadata.getPersonalNumber();
            case 9:
                return metadata.getPager();
            case WarningHeader.ATTRIBUTE_NOT_UNDERSTOOD /*10*/:
                return metadata.getUan();
            case 11:
                return metadata.getVoicemail();
            default:
                return metadata.getGeneralDesc();
        }
    }

    public PhoneNumberType getNumberType(Phonenumber.PhoneNumber number) {
        String regionCode = getRegionCodeForNumber(number);
        Phonemetadata.PhoneMetadata metadata = getMetadataForRegionOrCallingCode(number.getCountryCode(), regionCode);
        if (metadata == null) {
            return PhoneNumberType.UNKNOWN;
        }
        PhoneNumberType pNT = getNumberTypeHelper(getNationalSignificantNumber(number), metadata);
        if (pNT != PhoneNumberType.UNKNOWN || regionCode == null || !regionCode.equals("ME")) {
            return pNT;
        }
        logger.log(Level.WARNING, "pNT == PhoneNumberType.UNKNOWN and regionCode.equals(ME)");
        return PhoneNumberType.FIXED_LINE;
    }

    private PhoneNumberType getNumberTypeHelper(String nationalNumber, Phonemetadata.PhoneMetadata metadata) {
        if (!isNumberMatchingDesc(nationalNumber, metadata.getGeneralDesc())) {
            return PhoneNumberType.UNKNOWN;
        }
        if (isNumberMatchingDesc(nationalNumber, metadata.getPremiumRate())) {
            return PhoneNumberType.PREMIUM_RATE;
        }
        if (isNumberMatchingDesc(nationalNumber, metadata.getTollFree())) {
            return PhoneNumberType.TOLL_FREE;
        }
        if (isNumberMatchingDesc(nationalNumber, metadata.getSharedCost())) {
            return PhoneNumberType.SHARED_COST;
        }
        if (isNumberMatchingDesc(nationalNumber, metadata.getVoip())) {
            return PhoneNumberType.VOIP;
        }
        if (isNumberMatchingDesc(nationalNumber, metadata.getPersonalNumber())) {
            return PhoneNumberType.PERSONAL_NUMBER;
        }
        if (isNumberMatchingDesc(nationalNumber, metadata.getPager())) {
            return PhoneNumberType.PAGER;
        }
        if (isNumberMatchingDesc(nationalNumber, metadata.getUan())) {
            return PhoneNumberType.UAN;
        }
        if (isNumberMatchingDesc(nationalNumber, metadata.getVoicemail())) {
            return PhoneNumberType.VOICEMAIL;
        }
        if (isNumberMatchingDesc(nationalNumber, metadata.getFixedLine())) {
            if (metadata.getSameMobileAndFixedLinePattern()) {
                return PhoneNumberType.FIXED_LINE_OR_MOBILE;
            }
            if (isNumberMatchingDesc(nationalNumber, metadata.getMobile())) {
                return PhoneNumberType.FIXED_LINE_OR_MOBILE;
            }
            return PhoneNumberType.FIXED_LINE;
        } else if (metadata.getSameMobileAndFixedLinePattern() || !isNumberMatchingDesc(nationalNumber, metadata.getMobile())) {
            return PhoneNumberType.UNKNOWN;
        } else {
            return PhoneNumberType.MOBILE;
        }
    }

    /* access modifiers changed from: package-private */
    public Phonemetadata.PhoneMetadata getMetadataForRegion(String regionCode) {
        if (!isValidRegionCode(regionCode)) {
            return null;
        }
        return this.metadataSource.getMetadataForRegion(regionCode);
    }

    /* access modifiers changed from: package-private */
    public Phonemetadata.PhoneMetadata getMetadataForNonGeographicalRegion(int countryCallingCode) {
        if (!this.countryCallingCodeToRegionCodeMap.containsKey(Integer.valueOf(countryCallingCode))) {
            return null;
        }
        return this.metadataSource.getMetadataForNonGeographicalRegion(countryCallingCode);
    }

    /* access modifiers changed from: package-private */
    public boolean isNumberMatchingDesc(String nationalNumber, Phonemetadata.PhoneNumberDesc numberDesc) {
        int actualLength = nationalNumber.length();
        List<Integer> possibleLengths = numberDesc.getPossibleLengthList();
        if (possibleLengths.size() <= 0 || possibleLengths.contains(Integer.valueOf(actualLength))) {
            return this.matcherApi.matchNationalNumber(nationalNumber, numberDesc, false);
        }
        return false;
    }

    public boolean isValidNumber(Phonenumber.PhoneNumber number) {
        return isValidNumberForRegion(number, getRegionCodeForNumber(number));
    }

    public boolean isValidNumberForRegion(Phonenumber.PhoneNumber number, String regionCode) {
        int countryCode = number.getCountryCode();
        Phonemetadata.PhoneMetadata metadata = getMetadataForRegionOrCallingCode(countryCode, regionCode);
        boolean z = false;
        if (metadata == null || (!REGION_CODE_FOR_NON_GEO_ENTITY.equals(regionCode) && countryCode != getCountryCodeForValidRegion(regionCode))) {
            return false;
        }
        if (getNumberTypeHelper(getNationalSignificantNumber(number), metadata) != PhoneNumberType.UNKNOWN) {
            z = true;
        }
        return z;
    }

    public String getRegionCodeForNumber(Phonenumber.PhoneNumber number) {
        int countryCode = number.getCountryCode();
        List<String> regions = this.countryCallingCodeToRegionCodeMap.get(Integer.valueOf(countryCode));
        if (regions == null) {
            Logger logger2 = logger;
            Level level = Level.INFO;
            logger2.log(level, "Missing/invalid country_code (" + countryCode + Separators.RPAREN);
            return null;
        } else if (regions.size() == 1) {
            return regions.get(0);
        } else {
            return getRegionCodeForNumberFromRegionList(number, regions);
        }
    }

    private String getRegionCodeForNumberFromRegionList(Phonenumber.PhoneNumber number, List<String> regionCodes) {
        String nationalNumber = getNationalSignificantNumber(number);
        for (String regionCode : regionCodes) {
            Phonemetadata.PhoneMetadata metadata = getMetadataForRegion(regionCode);
            if (metadata.hasLeadingDigits()) {
                if (this.regexCache.getPatternForRegex(metadata.getLeadingDigits()).matcher(nationalNumber).lookingAt()) {
                    return regionCode;
                }
            } else if (getNumberTypeHelper(nationalNumber, metadata) != PhoneNumberType.UNKNOWN) {
                return regionCode;
            }
        }
        return null;
    }

    public String getRegionCodeForCountryCode(int countryCallingCode) {
        List<String> regionCodes = this.countryCallingCodeToRegionCodeMap.get(Integer.valueOf(countryCallingCode));
        return regionCodes == null ? UNKNOWN_REGION : regionCodes.get(0);
    }

    public List<String> getRegionCodesForCountryCode(int countryCallingCode) {
        List<String> regionCodes = this.countryCallingCodeToRegionCodeMap.get(Integer.valueOf(countryCallingCode));
        return Collections.unmodifiableList(regionCodes == null ? new ArrayList<>(0) : regionCodes);
    }

    public int getCountryCodeForRegion(String regionCode) {
        if (isValidRegionCode(regionCode)) {
            return getCountryCodeForValidRegion(regionCode);
        }
        Logger logger2 = logger;
        Level level = Level.WARNING;
        StringBuilder sb = new StringBuilder();
        sb.append("Invalid or missing region code (");
        sb.append(regionCode == null ? "null" : regionCode);
        sb.append(") provided.");
        logger2.log(level, sb.toString());
        return 0;
    }

    private int getCountryCodeForValidRegion(String regionCode) {
        Phonemetadata.PhoneMetadata metadata = getMetadataForRegion(regionCode);
        if (metadata != null) {
            return metadata.getCountryCode();
        }
        throw new IllegalArgumentException("Invalid region code: " + regionCode);
    }

    public String getNddPrefixForRegion(String regionCode, boolean stripNonDigits) {
        Phonemetadata.PhoneMetadata metadata = getMetadataForRegion(regionCode);
        if (metadata == null) {
            Logger logger2 = logger;
            Level level = Level.WARNING;
            StringBuilder sb = new StringBuilder();
            sb.append("Invalid or missing region code (");
            sb.append(regionCode == null ? "null" : regionCode);
            sb.append(") provided.");
            logger2.log(level, sb.toString());
            return null;
        }
        String nationalPrefix = metadata.getNationalPrefix();
        if (nationalPrefix.length() == 0) {
            return null;
        }
        if (stripNonDigits) {
            nationalPrefix = nationalPrefix.replace("~", "");
        }
        return nationalPrefix;
    }

    public boolean isNANPACountry(String regionCode) {
        return this.nanpaRegions.contains(regionCode);
    }

    public boolean isAlphaNumber(CharSequence number) {
        if (!isViablePhoneNumber(number)) {
            return false;
        }
        StringBuilder strippedNumber = new StringBuilder(number);
        maybeStripExtension(strippedNumber);
        return VALID_ALPHA_PHONE_PATTERN.matcher(strippedNumber).matches();
    }

    public boolean isPossibleNumber(Phonenumber.PhoneNumber number) {
        ValidationResult result = isPossibleNumberWithReason(number);
        return result == ValidationResult.IS_POSSIBLE || result == ValidationResult.IS_POSSIBLE_LOCAL_ONLY;
    }

    public boolean isPossibleNumberForType(Phonenumber.PhoneNumber number, PhoneNumberType type) {
        ValidationResult result = isPossibleNumberForTypeWithReason(number, type);
        return result == ValidationResult.IS_POSSIBLE || result == ValidationResult.IS_POSSIBLE_LOCAL_ONLY;
    }

    private ValidationResult testNumberLength(CharSequence number, Phonemetadata.PhoneMetadata metadata) {
        return testNumberLength(number, metadata, PhoneNumberType.UNKNOWN);
    }

    private ValidationResult testNumberLength(CharSequence number, Phonemetadata.PhoneMetadata metadata, PhoneNumberType type) {
        List<Integer> list;
        Phonemetadata.PhoneNumberDesc descForType = getNumberDescByType(metadata, type);
        List<Integer> possibleLengths = descForType.getPossibleLengthList().isEmpty() ? metadata.getGeneralDesc().getPossibleLengthList() : descForType.getPossibleLengthList();
        List<Integer> localLengths = descForType.getPossibleLengthLocalOnlyList();
        if (type == PhoneNumberType.FIXED_LINE_OR_MOBILE) {
            if (!descHasPossibleNumberData(getNumberDescByType(metadata, PhoneNumberType.FIXED_LINE))) {
                return testNumberLength(number, metadata, PhoneNumberType.MOBILE);
            }
            Phonemetadata.PhoneNumberDesc mobileDesc = getNumberDescByType(metadata, PhoneNumberType.MOBILE);
            if (descHasPossibleNumberData(mobileDesc)) {
                possibleLengths = new ArrayList<>(possibleLengths);
                if (mobileDesc.getPossibleLengthList().size() == 0) {
                    list = metadata.getGeneralDesc().getPossibleLengthList();
                } else {
                    list = mobileDesc.getPossibleLengthList();
                }
                possibleLengths.addAll(list);
                Collections.sort(possibleLengths);
                if (localLengths.isEmpty()) {
                    localLengths = mobileDesc.getPossibleLengthLocalOnlyList();
                } else {
                    localLengths = new ArrayList<>(localLengths);
                    localLengths.addAll(mobileDesc.getPossibleLengthLocalOnlyList());
                    Collections.sort(localLengths);
                }
            }
        }
        if (possibleLengths.get(0).intValue() == -1) {
            return ValidationResult.INVALID_LENGTH;
        }
        int actualLength = number.length();
        if (localLengths.contains(Integer.valueOf(actualLength))) {
            return ValidationResult.IS_POSSIBLE_LOCAL_ONLY;
        }
        int minimumLength = possibleLengths.get(0).intValue();
        if (minimumLength == actualLength) {
            return ValidationResult.IS_POSSIBLE;
        }
        if (minimumLength > actualLength) {
            return ValidationResult.TOO_SHORT;
        }
        if (possibleLengths.get(possibleLengths.size() - 1).intValue() < actualLength) {
            return ValidationResult.TOO_LONG;
        }
        return possibleLengths.subList(1, possibleLengths.size()).contains(Integer.valueOf(actualLength)) ? ValidationResult.IS_POSSIBLE : ValidationResult.INVALID_LENGTH;
    }

    public ValidationResult isPossibleNumberWithReason(Phonenumber.PhoneNumber number) {
        return isPossibleNumberForTypeWithReason(number, PhoneNumberType.UNKNOWN);
    }

    public ValidationResult isPossibleNumberForTypeWithReason(Phonenumber.PhoneNumber number, PhoneNumberType type) {
        String nationalNumber = getNationalSignificantNumber(number);
        int countryCode = number.getCountryCode();
        if (!hasValidCountryCallingCode(countryCode)) {
            return ValidationResult.INVALID_COUNTRY_CODE;
        }
        return testNumberLength(nationalNumber, getMetadataForRegionOrCallingCode(countryCode, getRegionCodeForCountryCode(countryCode)), type);
    }

    public boolean isPossibleNumber(CharSequence number, String regionDialingFrom) {
        try {
            return isPossibleNumber(parse(number, regionDialingFrom));
        } catch (NumberParseException e) {
            return false;
        }
    }

    public boolean truncateTooLongNumber(Phonenumber.PhoneNumber number) {
        if (isValidNumber(number)) {
            return true;
        }
        Phonenumber.PhoneNumber numberCopy = new Phonenumber.PhoneNumber();
        numberCopy.mergeFrom(number);
        long nationalNumber = number.getNationalNumber();
        do {
            nationalNumber /= 10;
            numberCopy.setNationalNumber(nationalNumber);
            if (isPossibleNumberWithReason(numberCopy) == ValidationResult.TOO_SHORT || nationalNumber == 0) {
                return false;
            }
        } while (!isValidNumber(numberCopy));
        number.setNationalNumber(nationalNumber);
        return true;
    }

    public AsYouTypeFormatter getAsYouTypeFormatter(String regionCode) {
        return new AsYouTypeFormatter(regionCode);
    }

    /* access modifiers changed from: package-private */
    public int extractCountryCode(StringBuilder fullNumber, StringBuilder nationalNumber) {
        if (fullNumber.length() == 0 || fullNumber.charAt(0) == '0') {
            return 0;
        }
        int numberLength = fullNumber.length();
        int i = 1;
        while (i <= 3 && i <= numberLength) {
            int potentialCountryCode = Integer.parseInt(fullNumber.substring(0, i));
            if (this.countryCallingCodeToRegionCodeMap.containsKey(Integer.valueOf(potentialCountryCode))) {
                nationalNumber.append(fullNumber.substring(i));
                return potentialCountryCode;
            }
            i++;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int maybeExtractCountryCode(CharSequence number, Phonemetadata.PhoneMetadata defaultRegionMetadata, StringBuilder nationalNumber, boolean keepRawInput, Phonenumber.PhoneNumber phoneNumber) throws NumberParseException {
        Phonemetadata.PhoneMetadata phoneMetadata = defaultRegionMetadata;
        StringBuilder sb = nationalNumber;
        Phonenumber.PhoneNumber phoneNumber2 = phoneNumber;
        if (number.length() == 0) {
            return 0;
        }
        StringBuilder fullNumber = new StringBuilder(number);
        String possibleCountryIddPrefix = "NonMatch";
        if (phoneMetadata != null) {
            possibleCountryIddPrefix = defaultRegionMetadata.getInternationalPrefix();
        }
        Phonenumber.PhoneNumber.CountryCodeSource countryCodeSource = maybeStripInternationalPrefixAndNormalize(fullNumber, possibleCountryIddPrefix);
        if (keepRawInput) {
            phoneNumber2.setCountryCodeSource(countryCodeSource);
        }
        if (countryCodeSource == Phonenumber.PhoneNumber.CountryCodeSource.FROM_DEFAULT_COUNTRY) {
            if (phoneMetadata != null) {
                int defaultCountryCode = defaultRegionMetadata.getCountryCode();
                String defaultCountryCodeString = String.valueOf(defaultCountryCode);
                String normalizedNumber = fullNumber.toString();
                if (normalizedNumber.startsWith(defaultCountryCodeString)) {
                    StringBuilder potentialNationalNumber = new StringBuilder(normalizedNumber.substring(defaultCountryCodeString.length()));
                    Phonemetadata.PhoneNumberDesc generalDesc = defaultRegionMetadata.getGeneralDesc();
                    maybeStripNationalPrefixAndCarrierCode(potentialNationalNumber, phoneMetadata, null);
                    if ((!this.matcherApi.matchNationalNumber(fullNumber, generalDesc, false) && this.matcherApi.matchNationalNumber(potentialNationalNumber, generalDesc, false)) || testNumberLength(fullNumber, phoneMetadata) == ValidationResult.TOO_LONG) {
                        sb.append(potentialNationalNumber);
                        if (keepRawInput) {
                            phoneNumber2.setCountryCodeSource(Phonenumber.PhoneNumber.CountryCodeSource.FROM_NUMBER_WITHOUT_PLUS_SIGN);
                        }
                        phoneNumber2.setCountryCode(defaultCountryCode);
                        return defaultCountryCode;
                    }
                }
            }
            phoneNumber2.setCountryCode(0);
            return 0;
        } else if (fullNumber.length() > 2) {
            int potentialCountryCode = extractCountryCode(fullNumber, sb);
            if (potentialCountryCode != 0) {
                phoneNumber2.setCountryCode(potentialCountryCode);
                return potentialCountryCode;
            }
            throw new NumberParseException(NumberParseException.ErrorType.INVALID_COUNTRY_CODE, "Country calling code supplied was not recognised.");
        } else {
            throw new NumberParseException(NumberParseException.ErrorType.TOO_SHORT_AFTER_IDD, "Phone number had an IDD, but after this was not long enough to be a viable phone number.");
        }
    }

    private boolean parsePrefixAsIdd(Pattern iddPattern, StringBuilder number) {
        Matcher m = iddPattern.matcher(number);
        if (!m.lookingAt()) {
            return false;
        }
        int matchEnd = m.end();
        Matcher digitMatcher = CAPTURING_DIGIT_PATTERN.matcher(number.substring(matchEnd));
        if (digitMatcher.find() && normalizeDigitsOnly(digitMatcher.group(1)).equals("0")) {
            return false;
        }
        number.delete(0, matchEnd);
        return true;
    }

    /* access modifiers changed from: package-private */
    public Phonenumber.PhoneNumber.CountryCodeSource maybeStripInternationalPrefixAndNormalize(StringBuilder number, String possibleIddPrefix) {
        Phonenumber.PhoneNumber.CountryCodeSource countryCodeSource;
        if (number.length() == 0) {
            return Phonenumber.PhoneNumber.CountryCodeSource.FROM_DEFAULT_COUNTRY;
        }
        Matcher m = PLUS_CHARS_PATTERN.matcher(number);
        if (m.lookingAt()) {
            number.delete(0, m.end());
            normalize(number);
            return Phonenumber.PhoneNumber.CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN;
        }
        Pattern iddPattern = this.regexCache.getPatternForRegex(possibleIddPrefix);
        normalize(number);
        if (parsePrefixAsIdd(iddPattern, number)) {
            countryCodeSource = Phonenumber.PhoneNumber.CountryCodeSource.FROM_NUMBER_WITH_IDD;
        } else {
            countryCodeSource = Phonenumber.PhoneNumber.CountryCodeSource.FROM_DEFAULT_COUNTRY;
        }
        return countryCodeSource;
    }

    /* access modifiers changed from: package-private */
    public boolean maybeStripNationalPrefixAndCarrierCode(StringBuilder number, Phonemetadata.PhoneMetadata metadata, StringBuilder carrierCode) {
        int numberLength = number.length();
        String possibleNationalPrefix = metadata.getNationalPrefixForParsing();
        if (numberLength == 0 || possibleNationalPrefix.length() == 0) {
            return false;
        }
        Matcher prefixMatcher = this.regexCache.getPatternForRegex(possibleNationalPrefix).matcher(number);
        if (!prefixMatcher.lookingAt()) {
            return false;
        }
        Phonemetadata.PhoneNumberDesc generalDesc = metadata.getGeneralDesc();
        boolean isViableOriginalNumber = this.matcherApi.matchNationalNumber(number, generalDesc, false);
        int numOfGroups = prefixMatcher.groupCount();
        String transformRule = metadata.getNationalPrefixTransformRule();
        if (transformRule != null && transformRule.length() != 0 && prefixMatcher.group(numOfGroups) != null) {
            StringBuilder transformedNumber = new StringBuilder(number);
            transformedNumber.replace(0, numberLength, prefixMatcher.replaceFirst(transformRule));
            if (isViableOriginalNumber && !this.matcherApi.matchNationalNumber(transformedNumber.toString(), generalDesc, false)) {
                return false;
            }
            if (carrierCode != null && numOfGroups > 1) {
                carrierCode.append(prefixMatcher.group(1));
            }
            number.replace(0, number.length(), transformedNumber.toString());
            return true;
        } else if (isViableOriginalNumber && !this.matcherApi.matchNationalNumber(number.substring(prefixMatcher.end()), generalDesc, false)) {
            return false;
        } else {
            if (!(carrierCode == null || numOfGroups <= 0 || prefixMatcher.group(numOfGroups) == null)) {
                carrierCode.append(prefixMatcher.group(1));
            }
            number.delete(0, prefixMatcher.end());
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public String maybeStripExtension(StringBuilder number) {
        Matcher m = EXTN_PATTERN.matcher(number);
        if (m.find() && isViablePhoneNumber(number.substring(0, m.start()))) {
            int length = m.groupCount();
            for (int i = 1; i <= length; i++) {
                if (m.group(i) != null) {
                    String extension = m.group(i);
                    number.delete(m.start(), number.length());
                    return extension;
                }
            }
        }
        return "";
    }

    private boolean checkRegionForParsing(CharSequence numberToParse, String defaultRegion) {
        if (isValidRegionCode(defaultRegion) || (numberToParse != null && numberToParse.length() != 0 && PLUS_CHARS_PATTERN.matcher(numberToParse).lookingAt())) {
            return true;
        }
        return false;
    }

    public Phonenumber.PhoneNumber parse(CharSequence numberToParse, String defaultRegion) throws NumberParseException {
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        parse(numberToParse, defaultRegion, phoneNumber);
        return phoneNumber;
    }

    public void parse(CharSequence numberToParse, String defaultRegion, Phonenumber.PhoneNumber phoneNumber) throws NumberParseException {
        parseHelper(numberToParse, defaultRegion, true, true, phoneNumber);
    }

    public Phonenumber.PhoneNumber parseAndKeepRawInput(CharSequence numberToParse, String defaultRegion) throws NumberParseException {
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        parseAndKeepRawInput(numberToParse, defaultRegion, phoneNumber);
        return phoneNumber;
    }

    public void parseAndKeepRawInput(CharSequence numberToParse, String defaultRegion, Phonenumber.PhoneNumber phoneNumber) throws NumberParseException {
        parseHelper(numberToParse, defaultRegion, true, true, phoneNumber);
    }

    public Iterable<PhoneNumberMatch> findNumbers(CharSequence text, String defaultRegion) {
        return findNumbers(text, defaultRegion, Leniency.VALID, Long.MAX_VALUE);
    }

    public Iterable<PhoneNumberMatch> findNumbers(CharSequence text, String defaultRegion, Leniency leniency, long maxTries) {
        final CharSequence charSequence = text;
        final String str = defaultRegion;
        final Leniency leniency2 = leniency;
        final long j = maxTries;
        AnonymousClass1 r0 = new Iterable<PhoneNumberMatch>() {
            public Iterator<PhoneNumberMatch> iterator() {
                PhoneNumberMatcher phoneNumberMatcher = new PhoneNumberMatcher(PhoneNumberUtil.this, charSequence, str, leniency2, j);
                return phoneNumberMatcher;
            }
        };
        return r0;
    }

    static void setItalianLeadingZerosForPhoneNumber(CharSequence nationalNumber, Phonenumber.PhoneNumber phoneNumber) {
        if (nationalNumber.length() > 1 && nationalNumber.charAt(0) == '0') {
            phoneNumber.setItalianLeadingZero(true);
            int numberOfLeadingZeros = 1;
            while (numberOfLeadingZeros < nationalNumber.length() - 1 && nationalNumber.charAt(numberOfLeadingZeros) == '0') {
                numberOfLeadingZeros++;
            }
            if (numberOfLeadingZeros != 1) {
                phoneNumber.setNumberOfLeadingZeros(numberOfLeadingZeros);
            }
        }
    }

    private void parseHelper(CharSequence numberToParse, String defaultRegion, boolean keepRawInput, boolean checkRegion, Phonenumber.PhoneNumber phoneNumber) throws NumberParseException {
        int countryCode;
        Phonemetadata.PhoneMetadata regionMetadata;
        StringBuilder normalizedNationalNumber;
        Phonemetadata.PhoneMetadata regionMetadata2;
        String str = defaultRegion;
        Phonenumber.PhoneNumber phoneNumber2 = phoneNumber;
        if (numberToParse == null) {
            throw new NumberParseException(NumberParseException.ErrorType.NOT_A_NUMBER, "The phone number supplied was null.");
        } else if (numberToParse.length() <= MAX_INPUT_STRING_LENGTH) {
            StringBuilder nationalNumber = new StringBuilder();
            String numberBeingParsed = numberToParse.toString();
            buildNationalNumberForParsing(numberBeingParsed, nationalNumber);
            if (!isViablePhoneNumber(nationalNumber)) {
                throw new NumberParseException(NumberParseException.ErrorType.NOT_A_NUMBER, "The string supplied did not seem to be a phone number.");
            } else if (!checkRegion || checkRegionForParsing(nationalNumber, str)) {
                if (keepRawInput) {
                    phoneNumber2.setRawInput(numberBeingParsed);
                }
                String extension = maybeStripExtension(nationalNumber);
                if (extension.length() > 0) {
                    phoneNumber2.setExtension(extension);
                }
                Phonemetadata.PhoneMetadata regionMetadata3 = getMetadataForRegion(str);
                StringBuilder normalizedNationalNumber2 = new StringBuilder();
                StringBuilder normalizedNationalNumber3 = normalizedNationalNumber2;
                Phonemetadata.PhoneMetadata regionMetadata4 = regionMetadata3;
                try {
                    countryCode = maybeExtractCountryCode(nationalNumber, regionMetadata3, normalizedNationalNumber2, keepRawInput, phoneNumber2);
                } catch (NumberParseException e) {
                    NumberParseException numberParseException = e;
                    Matcher matcher = PLUS_CHARS_PATTERN.matcher(nationalNumber);
                    if (e.getErrorType() != NumberParseException.ErrorType.INVALID_COUNTRY_CODE || !matcher.lookingAt()) {
                        StringBuilder sb = normalizedNationalNumber3;
                        Phonemetadata.PhoneMetadata phoneMetadata = regionMetadata4;
                        throw new NumberParseException(e.getErrorType(), e.getMessage());
                    }
                    Matcher matcher2 = matcher;
                    int countryCode2 = maybeExtractCountryCode(nationalNumber.substring(matcher.end()), regionMetadata4, normalizedNationalNumber3, keepRawInput, phoneNumber2);
                    if (countryCode2 != 0) {
                        countryCode = countryCode2;
                    } else {
                        Phonemetadata.PhoneMetadata phoneMetadata2 = regionMetadata4;
                        throw new NumberParseException(NumberParseException.ErrorType.INVALID_COUNTRY_CODE, "Could not interpret numbers after plus-sign.");
                    }
                }
                if (countryCode != 0) {
                    String phoneNumberRegion = getRegionCodeForCountryCode(countryCode);
                    if (!phoneNumberRegion.equals(str)) {
                        regionMetadata2 = getMetadataForRegionOrCallingCode(countryCode, phoneNumberRegion);
                    } else {
                        regionMetadata2 = regionMetadata4;
                    }
                    regionMetadata = regionMetadata2;
                    normalizedNationalNumber = normalizedNationalNumber3;
                } else {
                    normalizedNationalNumber = normalizedNationalNumber3;
                    normalizedNationalNumber.append(normalize(nationalNumber));
                    if (str != null) {
                        regionMetadata = regionMetadata4;
                        phoneNumber2.setCountryCode(regionMetadata.getCountryCode());
                    } else {
                        regionMetadata = regionMetadata4;
                        if (keepRawInput) {
                            phoneNumber.clearCountryCodeSource();
                        }
                    }
                }
                if (normalizedNationalNumber.length() >= 2) {
                    if (regionMetadata != null) {
                        StringBuilder carrierCode = new StringBuilder();
                        StringBuilder potentialNationalNumber = new StringBuilder(normalizedNationalNumber);
                        maybeStripNationalPrefixAndCarrierCode(potentialNationalNumber, regionMetadata, carrierCode);
                        ValidationResult validationResult = testNumberLength(potentialNationalNumber, regionMetadata);
                        if (!(validationResult == ValidationResult.TOO_SHORT || validationResult == ValidationResult.IS_POSSIBLE_LOCAL_ONLY || validationResult == ValidationResult.INVALID_LENGTH)) {
                            normalizedNationalNumber = potentialNationalNumber;
                            if (keepRawInput && carrierCode.length() > 0) {
                                phoneNumber2.setPreferredDomesticCarrierCode(carrierCode.toString());
                            }
                        }
                    }
                    int lengthOfNationalNumber = normalizedNationalNumber.length();
                    if (lengthOfNationalNumber < 2) {
                        throw new NumberParseException(NumberParseException.ErrorType.TOO_SHORT_NSN, "The string supplied is too short to be a phone number.");
                    } else if (lengthOfNationalNumber <= MAX_LENGTH_FOR_NSN) {
                        setItalianLeadingZerosForPhoneNumber(normalizedNationalNumber, phoneNumber2);
                        phoneNumber2.setNationalNumber(Long.parseLong(normalizedNationalNumber.toString()));
                    } else {
                        throw new NumberParseException(NumberParseException.ErrorType.TOO_LONG, "The string supplied is too long to be a phone number.");
                    }
                } else {
                    throw new NumberParseException(NumberParseException.ErrorType.TOO_SHORT_NSN, "The string supplied is too short to be a phone number.");
                }
            } else {
                throw new NumberParseException(NumberParseException.ErrorType.INVALID_COUNTRY_CODE, "Missing or invalid default region.");
            }
        } else {
            throw new NumberParseException(NumberParseException.ErrorType.TOO_LONG, "The string supplied was too long to parse.");
        }
    }

    private void buildNationalNumberForParsing(String numberToParse, StringBuilder nationalNumber) {
        int indexOfPhoneContext = numberToParse.indexOf(RFC3966_PHONE_CONTEXT);
        if (indexOfPhoneContext >= 0) {
            int phoneContextStart = RFC3966_PHONE_CONTEXT.length() + indexOfPhoneContext;
            if (phoneContextStart < numberToParse.length() - 1 && numberToParse.charAt(phoneContextStart) == '+') {
                int phoneContextEnd = numberToParse.indexOf(59, phoneContextStart);
                if (phoneContextEnd > 0) {
                    nationalNumber.append(numberToParse.substring(phoneContextStart, phoneContextEnd));
                } else {
                    nationalNumber.append(numberToParse.substring(phoneContextStart));
                }
            }
            int indexOfRfc3966Prefix = numberToParse.indexOf(RFC3966_PREFIX);
            nationalNumber.append(numberToParse.substring(indexOfRfc3966Prefix >= 0 ? RFC3966_PREFIX.length() + indexOfRfc3966Prefix : 0, indexOfPhoneContext));
        } else {
            nationalNumber.append(extractPossibleNumber(numberToParse));
        }
        int indexOfIsdn = nationalNumber.indexOf(RFC3966_ISDN_SUBADDRESS);
        if (indexOfIsdn > 0) {
            nationalNumber.delete(indexOfIsdn, nationalNumber.length());
        }
    }

    private static Phonenumber.PhoneNumber copyCoreFieldsOnly(Phonenumber.PhoneNumber phoneNumberIn) {
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setCountryCode(phoneNumberIn.getCountryCode());
        phoneNumber.setNationalNumber(phoneNumberIn.getNationalNumber());
        if (phoneNumberIn.getExtension().length() > 0) {
            phoneNumber.setExtension(phoneNumberIn.getExtension());
        }
        if (phoneNumberIn.isItalianLeadingZero()) {
            phoneNumber.setItalianLeadingZero(true);
            phoneNumber.setNumberOfLeadingZeros(phoneNumberIn.getNumberOfLeadingZeros());
        }
        return phoneNumber;
    }

    public MatchType isNumberMatch(Phonenumber.PhoneNumber firstNumberIn, Phonenumber.PhoneNumber secondNumberIn) {
        Phonenumber.PhoneNumber firstNumber = copyCoreFieldsOnly(firstNumberIn);
        Phonenumber.PhoneNumber secondNumber = copyCoreFieldsOnly(secondNumberIn);
        if (firstNumber.hasExtension() && secondNumber.hasExtension() && !firstNumber.getExtension().equals(secondNumber.getExtension())) {
            return MatchType.NO_MATCH;
        }
        int firstNumberCountryCode = firstNumber.getCountryCode();
        int secondNumberCountryCode = secondNumber.getCountryCode();
        if (firstNumberCountryCode == 0 || secondNumberCountryCode == 0) {
            firstNumber.setCountryCode(secondNumberCountryCode);
            if (firstNumber.exactlySameAs(secondNumber)) {
                return MatchType.NSN_MATCH;
            }
            if (isNationalNumberSuffixOfTheOther(firstNumber, secondNumber)) {
                return MatchType.SHORT_NSN_MATCH;
            }
            return MatchType.NO_MATCH;
        } else if (firstNumber.exactlySameAs(secondNumber)) {
            return MatchType.EXACT_MATCH;
        } else {
            if (firstNumberCountryCode != secondNumberCountryCode || !isNationalNumberSuffixOfTheOther(firstNumber, secondNumber)) {
                return MatchType.NO_MATCH;
            }
            return MatchType.SHORT_NSN_MATCH;
        }
    }

    private boolean isNationalNumberSuffixOfTheOther(Phonenumber.PhoneNumber firstNumber, Phonenumber.PhoneNumber secondNumber) {
        String firstNumberNationalNumber = String.valueOf(firstNumber.getNationalNumber());
        String secondNumberNationalNumber = String.valueOf(secondNumber.getNationalNumber());
        return firstNumberNationalNumber.endsWith(secondNumberNationalNumber) || secondNumberNationalNumber.endsWith(firstNumberNationalNumber);
    }

    public MatchType isNumberMatch(CharSequence firstNumber, CharSequence secondNumber) {
        try {
            return isNumberMatch(parse(firstNumber, UNKNOWN_REGION), secondNumber);
        } catch (NumberParseException e) {
            if (e.getErrorType() == NumberParseException.ErrorType.INVALID_COUNTRY_CODE) {
                try {
                    return isNumberMatch(parse(secondNumber, UNKNOWN_REGION), firstNumber);
                } catch (NumberParseException e2) {
                    if (e2.getErrorType() == NumberParseException.ErrorType.INVALID_COUNTRY_CODE) {
                        try {
                            Phonenumber.PhoneNumber firstNumberProto = new Phonenumber.PhoneNumber();
                            Phonenumber.PhoneNumber secondNumberProto = new Phonenumber.PhoneNumber();
                            parseHelper(firstNumber, null, false, false, firstNumberProto);
                            parseHelper(secondNumber, null, false, false, secondNumberProto);
                            return isNumberMatch(firstNumberProto, secondNumberProto);
                        } catch (NumberParseException e3) {
                            return MatchType.NOT_A_NUMBER;
                        }
                    }
                    return MatchType.NOT_A_NUMBER;
                }
            }
            return MatchType.NOT_A_NUMBER;
        }
    }

    public MatchType isNumberMatch(Phonenumber.PhoneNumber firstNumber, CharSequence secondNumber) {
        try {
            return isNumberMatch(firstNumber, parse(secondNumber, UNKNOWN_REGION));
        } catch (NumberParseException e) {
            if (e.getErrorType() == NumberParseException.ErrorType.INVALID_COUNTRY_CODE) {
                String firstNumberRegion = getRegionCodeForCountryCode(firstNumber.getCountryCode());
                try {
                    if (!firstNumberRegion.equals(UNKNOWN_REGION)) {
                        MatchType match = isNumberMatch(firstNumber, parse(secondNumber, firstNumberRegion));
                        if (match == MatchType.EXACT_MATCH) {
                            return MatchType.NSN_MATCH;
                        }
                        return match;
                    }
                    Phonenumber.PhoneNumber secondNumberProto = new Phonenumber.PhoneNumber();
                    parseHelper(secondNumber, null, false, false, secondNumberProto);
                    return isNumberMatch(firstNumber, secondNumberProto);
                } catch (NumberParseException e2) {
                    return MatchType.NOT_A_NUMBER;
                }
            }
            return MatchType.NOT_A_NUMBER;
        }
    }

    public boolean canBeInternationallyDialled(Phonenumber.PhoneNumber number) {
        Phonemetadata.PhoneMetadata metadata = getMetadataForRegion(getRegionCodeForNumber(number));
        if (metadata == null) {
            return true;
        }
        return true ^ isNumberMatchingDesc(getNationalSignificantNumber(number), metadata.getNoInternationalDialling());
    }

    public boolean isMobileNumberPortableRegion(String regionCode) {
        Phonemetadata.PhoneMetadata metadata = getMetadataForRegion(regionCode);
        if (metadata != null) {
            return metadata.isMobileNumberPortableRegion();
        }
        Logger logger2 = logger;
        Level level = Level.WARNING;
        logger2.log(level, "Invalid or unknown region code provided: " + regionCode);
        return false;
    }
}
