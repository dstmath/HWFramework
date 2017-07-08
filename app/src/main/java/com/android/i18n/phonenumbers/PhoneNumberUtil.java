package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.NumberParseException.ErrorType;
import com.android.i18n.phonenumbers.Phonemetadata.NumberFormat;
import com.android.i18n.phonenumbers.Phonemetadata.PhoneMetadata;
import com.android.i18n.phonenumbers.Phonemetadata.PhoneNumberDesc;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber.CountryCodeSource;
import com.google.i18n.phonenumbers.Phonemetadata;
import gov.nist.core.Separators;
import gov.nist.javax.sip.parser.TokenTypes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sip.header.WarningHeader;
import org.ccil.cowan.tagsoup.HTMLModels;
import org.ccil.cowan.tagsoup.Schema;

public class PhoneNumberUtil {
    private static final /* synthetic */ int[] -com-android-i18n-phonenumbers-PhoneNumberUtil$PhoneNumberFormatSwitchesValues = null;
    private static final /* synthetic */ int[] -com-android-i18n-phonenumbers-PhoneNumberUtil$PhoneNumberTypeSwitchesValues = null;
    private static final /* synthetic */ int[] -com-android-i18n-phonenumbers-Phonenumber$PhoneNumber$CountryCodeSourceSwitchesValues = null;
    private static final Map<Character, Character> ALL_PLUS_NUMBER_GROUPING_SYMBOLS = null;
    private static final Map<Character, Character> ALPHA_MAPPINGS = null;
    private static final Map<Character, Character> ALPHA_PHONE_MAPPINGS = null;
    private static final Pattern CAPTURING_DIGIT_PATTERN = null;
    private static final String CAPTURING_EXTN_DIGITS = "(\\p{Nd}{1,7})";
    private static final Pattern CC_PATTERN = null;
    private static final String COLOMBIA_MOBILE_TO_FIXED_LINE_PREFIX = "3";
    private static final String DEFAULT_EXTN_PREFIX = " ext. ";
    static final MetadataLoader DEFAULT_METADATA_LOADER = null;
    private static final Map<Character, Character> DIALLABLE_CHAR_MAPPINGS = null;
    private static final String DIGITS = "\\p{Nd}";
    private static final Pattern EXTN_PATTERN = null;
    static final String EXTN_PATTERNS_FOR_MATCHING = null;
    private static final String EXTN_PATTERNS_FOR_PARSING = null;
    private static final Pattern FG_PATTERN = null;
    private static final Pattern FIRST_GROUP_ONLY_PREFIX_PATTERN = null;
    private static final Pattern FIRST_GROUP_PATTERN = null;
    private static final int MAX_INPUT_STRING_LENGTH = 250;
    static final int MAX_LENGTH_COUNTRY_CODE = 3;
    static final int MAX_LENGTH_FOR_NSN = 17;
    private static final int MIN_LENGTH_FOR_NSN = 2;
    private static final Map<Integer, String> MOBILE_TOKEN_MAPPINGS = null;
    private static final int NANPA_COUNTRY_CODE = 1;
    static final Pattern NON_DIGITS_PATTERN = null;
    private static final Pattern NP_PATTERN = null;
    static final String PLUS_CHARS = "+\uff0b";
    static final Pattern PLUS_CHARS_PATTERN = null;
    static final char PLUS_SIGN = '+';
    static final int REGEX_FLAGS = 66;
    public static final String REGION_CODE_FOR_NON_GEO_ENTITY = "001";
    private static final String RFC3966_EXTN_PREFIX = ";ext=";
    private static final String RFC3966_ISDN_SUBADDRESS = ";isub=";
    private static final String RFC3966_PHONE_CONTEXT = ";phone-context=";
    private static final String RFC3966_PREFIX = "tel:";
    private static final String SECOND_NUMBER_START = "[\\\\/] *x";
    static final Pattern SECOND_NUMBER_START_PATTERN = null;
    private static final Pattern SEPARATOR_PATTERN = null;
    private static final char STAR_SIGN = '*';
    private static final Pattern UNIQUE_INTERNATIONAL_PREFIX = null;
    private static final String UNKNOWN_REGION = "ZZ";
    private static final String UNWANTED_END_CHARS = "[[\\P{N}&&\\P{L}]&&[^#]]+$";
    static final Pattern UNWANTED_END_CHAR_PATTERN = null;
    private static final String VALID_ALPHA = null;
    private static final Pattern VALID_ALPHA_PHONE_PATTERN = null;
    private static final String VALID_PHONE_NUMBER = null;
    private static final Pattern VALID_PHONE_NUMBER_PATTERN = null;
    static final String VALID_PUNCTUATION = "-x\u2010-\u2015\u2212\u30fc\uff0d-\uff0f \u00a0\u00ad\u200b\u2060\u3000()\uff08\uff09\uff3b\uff3d.\\[\\]/~\u2053\u223c\uff5e";
    private static final String VALID_START_CHAR = "[+\uff0b\\p{Nd}]";
    private static final Pattern VALID_START_CHAR_PATTERN = null;
    private static PhoneNumberUtil instance;
    private static final Logger logger = null;
    private final Map<Integer, List<String>> countryCallingCodeToRegionCodeMap;
    private final Set<Integer> countryCodesForNonGeographicalRegion;
    private final MetadataSource metadataSource;
    private final Set<String> nanpaRegions;
    private final RegexCache regexCache;
    private final Set<String> supportedRegions;

    /* renamed from: com.android.i18n.phonenumbers.PhoneNumberUtil.2 */
    class AnonymousClass2 implements Iterable<PhoneNumberMatch> {
        final /* synthetic */ String val$defaultRegion;
        final /* synthetic */ Leniency val$leniency;
        final /* synthetic */ long val$maxTries;
        final /* synthetic */ CharSequence val$text;

        AnonymousClass2(CharSequence val$text, String val$defaultRegion, Leniency val$leniency, long val$maxTries) {
            this.val$text = val$text;
            this.val$defaultRegion = val$defaultRegion;
            this.val$leniency = val$leniency;
            this.val$maxTries = val$maxTries;
        }

        public Iterator<PhoneNumberMatch> iterator() {
            return new PhoneNumberMatcher(PhoneNumberUtil.this, this.val$text, this.val$defaultRegion, this.val$leniency, this.val$maxTries);
        }
    }

    public enum Leniency {
        ;

        /* renamed from: com.android.i18n.phonenumbers.PhoneNumberUtil.Leniency.1 */
        enum AnonymousClass1 extends Leniency {
            AnonymousClass1(String str, int i) {
                super(i, null);
            }

            boolean verify(PhoneNumber number, String candidate, PhoneNumberUtil util) {
                return util.isPossibleNumber(number);
            }
        }

        /* renamed from: com.android.i18n.phonenumbers.PhoneNumberUtil.Leniency.2 */
        enum AnonymousClass2 extends Leniency {
            AnonymousClass2(String str, int i) {
                super(i, null);
            }

            boolean verify(PhoneNumber number, String candidate, PhoneNumberUtil util) {
                if (util.isValidNumber(number) && PhoneNumberMatcher.containsOnlyValidXChars(number, candidate, util)) {
                    return PhoneNumberMatcher.isNationalPrefixPresentIfRequired(number, util);
                }
                return false;
            }
        }

        /* renamed from: com.android.i18n.phonenumbers.PhoneNumberUtil.Leniency.3 */
        enum AnonymousClass3 extends Leniency {
            AnonymousClass3(String str, int i) {
                super(i, null);
            }

            boolean verify(PhoneNumber number, String candidate, PhoneNumberUtil util) {
                if (util.isValidNumber(number) && PhoneNumberMatcher.containsOnlyValidXChars(number, candidate, util) && !PhoneNumberMatcher.containsMoreThanOneSlashInNationalNumber(number, candidate) && PhoneNumberMatcher.isNationalPrefixPresentIfRequired(number, util)) {
                    return PhoneNumberMatcher.checkNumberGroupingIsValid(number, candidate, util, new NumberGroupingChecker() {
                        public boolean checkGroups(PhoneNumberUtil util, PhoneNumber number, StringBuilder normalizedCandidate, String[] expectedNumberGroups) {
                            return PhoneNumberMatcher.allNumberGroupsRemainGrouped(util, number, normalizedCandidate, expectedNumberGroups);
                        }
                    });
                }
                return false;
            }
        }

        /* renamed from: com.android.i18n.phonenumbers.PhoneNumberUtil.Leniency.4 */
        enum AnonymousClass4 extends Leniency {
            AnonymousClass4(String str, int i) {
                super(i, null);
            }

            boolean verify(PhoneNumber number, String candidate, PhoneNumberUtil util) {
                if (util.isValidNumber(number) && PhoneNumberMatcher.containsOnlyValidXChars(number, candidate, util) && !PhoneNumberMatcher.containsMoreThanOneSlashInNationalNumber(number, candidate) && PhoneNumberMatcher.isNationalPrefixPresentIfRequired(number, util)) {
                    return PhoneNumberMatcher.checkNumberGroupingIsValid(number, candidate, util, new NumberGroupingChecker() {
                        public boolean checkGroups(PhoneNumberUtil util, PhoneNumber number, StringBuilder normalizedCandidate, String[] expectedNumberGroups) {
                            return PhoneNumberMatcher.allNumberGroupsAreExactlyPresent(util, number, normalizedCandidate, expectedNumberGroups);
                        }
                    });
                }
                return false;
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.i18n.phonenumbers.PhoneNumberUtil.Leniency.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.i18n.phonenumbers.PhoneNumberUtil.Leniency.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberUtil.Leniency.<clinit>():void");
        }

        abstract boolean verify(PhoneNumber phoneNumber, String str, PhoneNumberUtil phoneNumberUtil);
    }

    public enum MatchType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.i18n.phonenumbers.PhoneNumberUtil.MatchType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.i18n.phonenumbers.PhoneNumberUtil.MatchType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberUtil.MatchType.<clinit>():void");
        }
    }

    public enum PhoneNumberFormat {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.<clinit>():void");
        }
    }

    public enum PhoneNumberType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType.<clinit>():void");
        }
    }

    public enum ValidationResult {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.i18n.phonenumbers.PhoneNumberUtil.ValidationResult.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.i18n.phonenumbers.PhoneNumberUtil.ValidationResult.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberUtil.ValidationResult.<clinit>():void");
        }
    }

    private static /* synthetic */ int[] -getcom-android-i18n-phonenumbers-PhoneNumberUtil$PhoneNumberFormatSwitchesValues() {
        if (-com-android-i18n-phonenumbers-PhoneNumberUtil$PhoneNumberFormatSwitchesValues != null) {
            return -com-android-i18n-phonenumbers-PhoneNumberUtil$PhoneNumberFormatSwitchesValues;
        }
        int[] iArr = new int[PhoneNumberFormat.values().length];
        try {
            iArr[PhoneNumberFormat.E164.ordinal()] = NANPA_COUNTRY_CODE;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PhoneNumberFormat.INTERNATIONAL.ordinal()] = MIN_LENGTH_FOR_NSN;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[PhoneNumberFormat.NATIONAL.ordinal()] = MAX_LENGTH_COUNTRY_CODE;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[PhoneNumberFormat.RFC3966.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -com-android-i18n-phonenumbers-PhoneNumberUtil$PhoneNumberFormatSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-android-i18n-phonenumbers-PhoneNumberUtil$PhoneNumberTypeSwitchesValues() {
        if (-com-android-i18n-phonenumbers-PhoneNumberUtil$PhoneNumberTypeSwitchesValues != null) {
            return -com-android-i18n-phonenumbers-PhoneNumberUtil$PhoneNumberTypeSwitchesValues;
        }
        int[] iArr = new int[PhoneNumberType.values().length];
        try {
            iArr[PhoneNumberType.FIXED_LINE.ordinal()] = NANPA_COUNTRY_CODE;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PhoneNumberType.FIXED_LINE_OR_MOBILE.ordinal()] = MIN_LENGTH_FOR_NSN;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[PhoneNumberType.MOBILE.ordinal()] = MAX_LENGTH_COUNTRY_CODE;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[PhoneNumberType.PAGER.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[PhoneNumberType.PERSONAL_NUMBER.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[PhoneNumberType.PREMIUM_RATE.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[PhoneNumberType.SHARED_COST.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[PhoneNumberType.TOLL_FREE.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[PhoneNumberType.UAN.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[PhoneNumberType.UNKNOWN.ordinal()] = 20;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[PhoneNumberType.VOICEMAIL.ordinal()] = 10;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[PhoneNumberType.VOIP.ordinal()] = 11;
        } catch (NoSuchFieldError e12) {
        }
        -com-android-i18n-phonenumbers-PhoneNumberUtil$PhoneNumberTypeSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-android-i18n-phonenumbers-Phonenumber$PhoneNumber$CountryCodeSourceSwitchesValues() {
        if (-com-android-i18n-phonenumbers-Phonenumber$PhoneNumber$CountryCodeSourceSwitchesValues != null) {
            return -com-android-i18n-phonenumbers-Phonenumber$PhoneNumber$CountryCodeSourceSwitchesValues;
        }
        int[] iArr = new int[CountryCodeSource.values().length];
        try {
            iArr[CountryCodeSource.FROM_DEFAULT_COUNTRY.ordinal()] = NANPA_COUNTRY_CODE;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CountryCodeSource.FROM_NUMBER_WITHOUT_PLUS_SIGN.ordinal()] = MIN_LENGTH_FOR_NSN;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CountryCodeSource.FROM_NUMBER_WITH_IDD.ordinal()] = MAX_LENGTH_COUNTRY_CODE;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -com-android-i18n-phonenumbers-Phonenumber$PhoneNumber$CountryCodeSourceSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.i18n.phonenumbers.PhoneNumberUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.i18n.phonenumbers.PhoneNumberUtil.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberUtil.<clinit>():void");
    }

    private static String createExtnPattern(String singleExtnSymbols) {
        return ";ext=(\\p{Nd}{1,7})|[ \u00a0\\t,]*(?:e?xt(?:ensi(?:o\u0301?|\u00f3))?n?|\uff45?\uff58\uff54\uff4e?|[" + singleExtnSymbols + "]|int|anexo|\uff49\uff4e\uff54)" + "[:\\.\uff0e]?[ \u00a0\\t,-]*" + CAPTURING_EXTN_DIGITS + "#?|" + "[- ]+(" + DIGITS + "{1,5})#";
    }

    PhoneNumberUtil(MetadataSource metadataSource, Map<Integer, List<String>> countryCallingCodeToRegionCodeMap) {
        this.nanpaRegions = new HashSet(35);
        this.regexCache = new RegexCache(100);
        this.supportedRegions = new HashSet(320);
        this.countryCodesForNonGeographicalRegion = new HashSet();
        this.metadataSource = metadataSource;
        this.countryCallingCodeToRegionCodeMap = countryCallingCodeToRegionCodeMap;
        for (Entry<Integer, List<String>> entry : countryCallingCodeToRegionCodeMap.entrySet()) {
            List<String> regionCodes = (List) entry.getValue();
            if (regionCodes.size() == NANPA_COUNTRY_CODE && REGION_CODE_FOR_NON_GEO_ENTITY.equals(regionCodes.get(0))) {
                this.countryCodesForNonGeographicalRegion.add((Integer) entry.getKey());
            } else {
                this.supportedRegions.addAll(regionCodes);
            }
        }
        if (this.supportedRegions.remove(REGION_CODE_FOR_NON_GEO_ENTITY)) {
            logger.log(Level.WARNING, "invalid metadata (country calling code was mapped to the non-geo entity as well as specific region(s))");
        }
        this.nanpaRegions.addAll((Collection) countryCallingCodeToRegionCodeMap.get(Integer.valueOf(NANPA_COUNTRY_CODE)));
    }

    static String extractPossibleNumber(String number) {
        Matcher m = VALID_START_CHAR_PATTERN.matcher(number);
        if (!m.find()) {
            return "";
        }
        number = number.substring(m.start());
        Matcher trailingCharsMatcher = UNWANTED_END_CHAR_PATTERN.matcher(number);
        if (trailingCharsMatcher.find()) {
            number = number.substring(0, trailingCharsMatcher.start());
            logger.log(Level.FINER, "Stripped trailing characters: " + number);
        }
        Matcher secondNumber = SECOND_NUMBER_START_PATTERN.matcher(number);
        if (secondNumber.find()) {
            number = number.substring(0, secondNumber.start());
        }
        return number;
    }

    static boolean isViablePhoneNumber(String number) {
        if (number.length() < MIN_LENGTH_FOR_NSN) {
            return false;
        }
        return VALID_PHONE_NUMBER_PATTERN.matcher(number).matches();
    }

    static String normalize(String number) {
        if (VALID_ALPHA_PHONE_PATTERN.matcher(number).matches()) {
            return normalizeHelper(number, ALPHA_PHONE_MAPPINGS, true);
        }
        return normalizeDigitsOnly(number);
    }

    static void normalize(StringBuilder number) {
        number.replace(0, number.length(), normalize(number.toString()));
    }

    public static String normalizeDigitsOnly(String number) {
        return normalizeDigits(number, false).toString();
    }

    static StringBuilder normalizeDigits(String number, boolean keepNonDigits) {
        StringBuilder normalizedDigits = new StringBuilder(number.length());
        char[] toCharArray = number.toCharArray();
        int length = toCharArray.length;
        for (int i = 0; i < length; i += NANPA_COUNTRY_CODE) {
            char c = toCharArray[i];
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                normalizedDigits.append(digit);
            } else if (keepNonDigits) {
                normalizedDigits.append(c);
            }
        }
        return normalizedDigits;
    }

    static String normalizeDiallableCharsOnly(String number) {
        return normalizeHelper(number, DIALLABLE_CHAR_MAPPINGS, true);
    }

    public static String convertAlphaCharactersInNumber(String number) {
        return normalizeHelper(number, ALPHA_PHONE_MAPPINGS, false);
    }

    public int getLengthOfGeographicalAreaCode(PhoneNumber number) {
        PhoneMetadata metadata = getMetadataForRegion(getRegionCodeForNumber(number));
        if (metadata == null) {
            return 0;
        }
        if ((metadata.hasNationalPrefix() || number.isItalianLeadingZero()) && isNumberGeographical(number)) {
            return getLengthOfNationalDestinationCode(number);
        }
        return 0;
    }

    public int getLengthOfNationalDestinationCode(PhoneNumber number) {
        PhoneNumber copiedProto;
        if (number.hasExtension()) {
            copiedProto = new PhoneNumber();
            copiedProto.mergeFrom(number);
            copiedProto.clearExtension();
        } else {
            copiedProto = number;
        }
        String[] numberGroups = NON_DIGITS_PATTERN.split(format(copiedProto, PhoneNumberFormat.INTERNATIONAL));
        if (numberGroups.length <= MAX_LENGTH_COUNTRY_CODE) {
            return 0;
        }
        if (getNumberType(number) != PhoneNumberType.MOBILE || getCountryMobileToken(number.getCountryCode()).equals("")) {
            return numberGroups[MIN_LENGTH_FOR_NSN].length();
        }
        return numberGroups[MIN_LENGTH_FOR_NSN].length() + numberGroups[MAX_LENGTH_COUNTRY_CODE].length();
    }

    public static String getCountryMobileToken(int countryCallingCode) {
        if (MOBILE_TOKEN_MAPPINGS.containsKey(Integer.valueOf(countryCallingCode))) {
            return (String) MOBILE_TOKEN_MAPPINGS.get(Integer.valueOf(countryCallingCode));
        }
        return "";
    }

    private static String normalizeHelper(String number, Map<Character, Character> normalizationReplacements, boolean removeNonMatches) {
        StringBuilder normalizedNumber = new StringBuilder(number.length());
        for (int i = 0; i < number.length(); i += NANPA_COUNTRY_CODE) {
            char character = number.charAt(i);
            Character newDigit = (Character) normalizationReplacements.get(Character.valueOf(Character.toUpperCase(character)));
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

    public static synchronized PhoneNumberUtil getInstance() {
        PhoneNumberUtil phoneNumberUtil;
        synchronized (PhoneNumberUtil.class) {
            if (instance == null) {
                setInstance(createInstance(DEFAULT_METADATA_LOADER));
            }
            phoneNumberUtil = instance;
        }
        return phoneNumberUtil;
    }

    public static PhoneNumberUtil createInstance(MetadataSource metadataSource) {
        if (metadataSource != null) {
            return new PhoneNumberUtil(metadataSource, CountryCodeToRegionCodeMap.getCountryCodeToRegionCodeMap());
        }
        throw new IllegalArgumentException("metadataSource could not be null.");
    }

    public static PhoneNumberUtil createInstance(MetadataLoader metadataLoader) {
        if (metadataLoader != null) {
            return createInstance(new MultiFileMetadataSourceImpl(metadataLoader));
        }
        throw new IllegalArgumentException("metadataLoader could not be null.");
    }

    static boolean formattingRuleHasFirstGroupOnly(String nationalPrefixFormattingRule) {
        if (nationalPrefixFormattingRule.length() != 0) {
            return FIRST_GROUP_ONLY_PREFIX_PATTERN.matcher(nationalPrefixFormattingRule).matches();
        }
        return true;
    }

    boolean isNumberGeographical(PhoneNumber phoneNumber) {
        PhoneNumberType numberType = getNumberType(phoneNumber);
        if (numberType == PhoneNumberType.FIXED_LINE || numberType == PhoneNumberType.FIXED_LINE_OR_MOBILE) {
            return true;
        }
        return false;
    }

    private boolean isValidRegionCode(String regionCode) {
        return regionCode != null ? this.supportedRegions.contains(regionCode) : false;
    }

    private boolean hasValidCountryCallingCode(int countryCallingCode) {
        return this.countryCallingCodeToRegionCodeMap.containsKey(Integer.valueOf(countryCallingCode));
    }

    public String format(PhoneNumber number, PhoneNumberFormat numberFormat) {
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

    public void format(PhoneNumber number, PhoneNumberFormat numberFormat, StringBuilder formattedNumber) {
        formattedNumber.setLength(0);
        int countryCallingCode = number.getCountryCode();
        String nationalSignificantNumber = getNationalSignificantNumber(number);
        if (numberFormat == PhoneNumberFormat.E164) {
            formattedNumber.append(nationalSignificantNumber);
            prefixNumberWithCountryCallingCode(countryCallingCode, PhoneNumberFormat.E164, formattedNumber);
        } else if (hasValidCountryCallingCode(countryCallingCode)) {
            PhoneMetadata metadata = getMetadataForRegionOrCallingCode(countryCallingCode, getRegionCodeForCountryCode(countryCallingCode));
            formattedNumber.append(formatNsn(nationalSignificantNumber, metadata, numberFormat));
            maybeAppendFormattedExtension(number, metadata, numberFormat, formattedNumber);
            prefixNumberWithCountryCallingCode(countryCallingCode, numberFormat, formattedNumber);
        } else {
            formattedNumber.append(nationalSignificantNumber);
        }
    }

    public String formatByPattern(PhoneNumber number, PhoneNumberFormat numberFormat, List<NumberFormat> userDefinedFormats) {
        int countryCallingCode = number.getCountryCode();
        String nationalSignificantNumber = getNationalSignificantNumber(number);
        if (!hasValidCountryCallingCode(countryCallingCode)) {
            return nationalSignificantNumber;
        }
        PhoneMetadata metadata = getMetadataForRegionOrCallingCode(countryCallingCode, getRegionCodeForCountryCode(countryCallingCode));
        StringBuilder formattedNumber = new StringBuilder(20);
        NumberFormat formattingPattern = chooseFormattingPatternForNumber(userDefinedFormats, nationalSignificantNumber);
        if (formattingPattern == null) {
            formattedNumber.append(nationalSignificantNumber);
        } else {
            NumberFormat numFormatCopy = new NumberFormat();
            numFormatCopy.mergeFrom(formattingPattern);
            String nationalPrefixFormattingRule = formattingPattern.getNationalPrefixFormattingRule();
            if (nationalPrefixFormattingRule.length() > 0) {
                String nationalPrefix = metadata.getNationalPrefix();
                if (nationalPrefix.length() > 0) {
                    numFormatCopy.setNationalPrefixFormattingRule(FG_PATTERN.matcher(NP_PATTERN.matcher(nationalPrefixFormattingRule).replaceFirst(nationalPrefix)).replaceFirst("\\$1"));
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

    public String formatNationalNumberWithCarrierCode(PhoneNumber number, String carrierCode) {
        int countryCallingCode = number.getCountryCode();
        String nationalSignificantNumber = getNationalSignificantNumber(number);
        if (!hasValidCountryCallingCode(countryCallingCode)) {
            return nationalSignificantNumber;
        }
        PhoneMetadata metadata = getMetadataForRegionOrCallingCode(countryCallingCode, getRegionCodeForCountryCode(countryCallingCode));
        StringBuilder formattedNumber = new StringBuilder(20);
        formattedNumber.append(formatNsn(nationalSignificantNumber, metadata, PhoneNumberFormat.NATIONAL, carrierCode));
        maybeAppendFormattedExtension(number, metadata, PhoneNumberFormat.NATIONAL, formattedNumber);
        prefixNumberWithCountryCallingCode(countryCallingCode, PhoneNumberFormat.NATIONAL, formattedNumber);
        return formattedNumber.toString();
    }

    private PhoneMetadata getMetadataForRegionOrCallingCode(int countryCallingCode, String regionCode) {
        if (REGION_CODE_FOR_NON_GEO_ENTITY.equals(regionCode)) {
            return getMetadataForNonGeographicalRegion(countryCallingCode);
        }
        return getMetadataForRegion(regionCode);
    }

    public String formatNationalNumberWithPreferredCarrierCode(PhoneNumber number, String fallbackCarrierCode) {
        if (number.hasPreferredDomesticCarrierCode()) {
            fallbackCarrierCode = number.getPreferredDomesticCarrierCode();
        }
        return formatNationalNumberWithCarrierCode(number, fallbackCarrierCode);
    }

    public String formatNumberForMobileDialing(PhoneNumber number, String regionCallingFrom, boolean withFormatting) {
        int countryCallingCode = number.getCountryCode();
        if (hasValidCountryCallingCode(countryCallingCode)) {
            String formattedNumber = "";
            PhoneNumber numberNoExt = new PhoneNumber().mergeFrom(number).clearExtension();
            String regionCode = getRegionCodeForCountryCode(countryCallingCode);
            PhoneNumberType numberType = getNumberType(numberNoExt);
            boolean isValidNumber = numberType != PhoneNumberType.UNKNOWN;
            if (regionCallingFrom.equals(regionCode)) {
                boolean isFixedLineOrMobile = (numberType == PhoneNumberType.FIXED_LINE || numberType == PhoneNumberType.MOBILE) ? true : numberType == PhoneNumberType.FIXED_LINE_OR_MOBILE;
                if (regionCode.equals("CO") && numberType == PhoneNumberType.FIXED_LINE) {
                    formattedNumber = formatNationalNumberWithCarrierCode(numberNoExt, COLOMBIA_MOBILE_TO_FIXED_LINE_PREFIX);
                } else if (regionCode.equals("BR") && isFixedLineOrMobile) {
                    formattedNumber = numberNoExt.hasPreferredDomesticCarrierCode() ? formatNationalNumberWithPreferredCarrierCode(numberNoExt, "") : "";
                } else if (isValidNumber && regionCode.equals("HU")) {
                    formattedNumber = getNddPrefixForRegion(regionCode, true) + Separators.SP + format(numberNoExt, PhoneNumberFormat.NATIONAL);
                } else if (countryCallingCode == NANPA_COUNTRY_CODE) {
                    formattedNumber = (!canBeInternationallyDialled(numberNoExt) || isShorterThanPossibleNormalNumber(getMetadataForRegion(regionCallingFrom), getNationalSignificantNumber(numberNoExt))) ? format(numberNoExt, PhoneNumberFormat.NATIONAL) : format(numberNoExt, PhoneNumberFormat.INTERNATIONAL);
                } else {
                    formattedNumber = ((regionCode.equals(REGION_CODE_FOR_NON_GEO_ENTITY) || ((regionCode.equals("MX") || regionCode.equals("CL")) && isFixedLineOrMobile)) && canBeInternationallyDialled(numberNoExt)) ? format(numberNoExt, PhoneNumberFormat.INTERNATIONAL) : format(numberNoExt, PhoneNumberFormat.NATIONAL);
                }
            } else if (isValidNumber && canBeInternationallyDialled(numberNoExt)) {
                String format;
                if (withFormatting) {
                    format = format(numberNoExt, PhoneNumberFormat.INTERNATIONAL);
                } else {
                    format = format(numberNoExt, PhoneNumberFormat.E164);
                }
                return format;
            }
            if (!withFormatting) {
                formattedNumber = normalizeDiallableCharsOnly(formattedNumber);
            }
            return formattedNumber;
        }
        return number.hasRawInput() ? number.getRawInput() : "";
    }

    public String formatOutOfCountryCallingNumber(PhoneNumber number, String regionCallingFrom) {
        if (isValidRegionCode(regionCallingFrom)) {
            int countryCallingCode = number.getCountryCode();
            String nationalSignificantNumber = getNationalSignificantNumber(number);
            if (!hasValidCountryCallingCode(countryCallingCode)) {
                return nationalSignificantNumber;
            }
            if (countryCallingCode == NANPA_COUNTRY_CODE) {
                if (isNANPACountry(regionCallingFrom)) {
                    return countryCallingCode + Separators.SP + format(number, PhoneNumberFormat.NATIONAL);
                }
            } else if (countryCallingCode == getCountryCodeForValidRegion(regionCallingFrom)) {
                return format(number, PhoneNumberFormat.NATIONAL);
            }
            PhoneMetadata metadataForRegionCallingFrom = getMetadataForRegion(regionCallingFrom);
            String internationalPrefix = metadataForRegionCallingFrom.getInternationalPrefix();
            String internationalPrefixForFormatting = "";
            if (UNIQUE_INTERNATIONAL_PREFIX.matcher(internationalPrefix).matches()) {
                internationalPrefixForFormatting = internationalPrefix;
            } else if (metadataForRegionCallingFrom.hasPreferredInternationalPrefix()) {
                internationalPrefixForFormatting = metadataForRegionCallingFrom.getPreferredInternationalPrefix();
            }
            PhoneMetadata metadataForRegion = getMetadataForRegionOrCallingCode(countryCallingCode, getRegionCodeForCountryCode(countryCallingCode));
            StringBuilder formattedNumber = new StringBuilder(formatNsn(nationalSignificantNumber, metadataForRegion, PhoneNumberFormat.INTERNATIONAL));
            maybeAppendFormattedExtension(number, metadataForRegion, PhoneNumberFormat.INTERNATIONAL, formattedNumber);
            if (internationalPrefixForFormatting.length() > 0) {
                formattedNumber.insert(0, Separators.SP).insert(0, countryCallingCode).insert(0, Separators.SP).insert(0, internationalPrefixForFormatting);
            } else {
                prefixNumberWithCountryCallingCode(countryCallingCode, PhoneNumberFormat.INTERNATIONAL, formattedNumber);
            }
            return formattedNumber.toString();
        }
        logger.log(Level.WARNING, "Trying to format number from invalid region " + regionCallingFrom + ". International formatting applied.");
        return format(number, PhoneNumberFormat.INTERNATIONAL);
    }

    public String formatInOriginalFormat(PhoneNumber number, String regionCallingFrom) {
        if (number.hasRawInput() && (hasUnexpectedItalianLeadingZero(number) || !hasFormattingPatternForNumber(number))) {
            return number.getRawInput();
        }
        if (number.hasCountryCodeSource()) {
            String formattedNumber;
            switch (-getcom-android-i18n-phonenumbers-Phonenumber$PhoneNumber$CountryCodeSourceSwitchesValues()[number.getCountryCodeSource().ordinal()]) {
                case MIN_LENGTH_FOR_NSN /*2*/:
                    formattedNumber = format(number, PhoneNumberFormat.INTERNATIONAL).substring(NANPA_COUNTRY_CODE);
                    break;
                case MAX_LENGTH_COUNTRY_CODE /*3*/:
                    formattedNumber = formatOutOfCountryCallingNumber(number, regionCallingFrom);
                    break;
                case Schema.F_NOFORCE /*4*/:
                    formattedNumber = format(number, PhoneNumberFormat.INTERNATIONAL);
                    break;
                default:
                    String regionCode = getRegionCodeForCountryCode(number.getCountryCode());
                    String nationalPrefix = getNddPrefixForRegion(regionCode, true);
                    String nationalFormat = format(number, PhoneNumberFormat.NATIONAL);
                    if (nationalPrefix != null && nationalPrefix.length() != 0) {
                        if (!rawInputContainsNationalPrefix(number.getRawInput(), nationalPrefix, regionCode)) {
                            PhoneMetadata metadata = getMetadataForRegion(regionCode);
                            String nationalNumber = getNationalSignificantNumber(number);
                            NumberFormat formatRule = chooseFormattingPatternForNumber(metadata.numberFormats(), nationalNumber);
                            if (formatRule != null) {
                                String candidateNationalPrefixRule = formatRule.getNationalPrefixFormattingRule();
                                int indexOfFirstGroup = candidateNationalPrefixRule.indexOf("$1");
                                if (indexOfFirstGroup > 0) {
                                    if (normalizeDigitsOnly(candidateNationalPrefixRule.substring(0, indexOfFirstGroup)).length() != 0) {
                                        NumberFormat numFormatCopy = new NumberFormat();
                                        numFormatCopy.mergeFrom(formatRule);
                                        numFormatCopy.clearNationalPrefixFormattingRule();
                                        List<Phonemetadata.NumberFormat> numberFormats = new ArrayList(NANPA_COUNTRY_CODE);
                                        numberFormats.add(numFormatCopy);
                                        formattedNumber = formatByPattern(number, PhoneNumberFormat.NATIONAL, numberFormats);
                                        break;
                                    }
                                    formattedNumber = nationalFormat;
                                    break;
                                }
                                formattedNumber = nationalFormat;
                                break;
                            }
                            formattedNumber = nationalFormat;
                            break;
                        }
                        formattedNumber = nationalFormat;
                        break;
                    }
                    formattedNumber = nationalFormat;
                    break;
            }
            String rawInput = number.getRawInput();
            if (!(formattedNumber == null || rawInput.length() <= 0 || normalizeDiallableCharsOnly(formattedNumber).equals(normalizeDiallableCharsOnly(rawInput)))) {
                formattedNumber = rawInput;
            }
            return formattedNumber;
        }
        return format(number, PhoneNumberFormat.NATIONAL);
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

    private boolean hasUnexpectedItalianLeadingZero(PhoneNumber number) {
        return number.isItalianLeadingZero() && !isLeadingZeroPossible(number.getCountryCode());
    }

    private boolean hasFormattingPatternForNumber(PhoneNumber number) {
        boolean z = false;
        int countryCallingCode = number.getCountryCode();
        PhoneMetadata metadata = getMetadataForRegionOrCallingCode(countryCallingCode, getRegionCodeForCountryCode(countryCallingCode));
        if (metadata == null) {
            return false;
        }
        if (chooseFormattingPatternForNumber(metadata.numberFormats(), getNationalSignificantNumber(number)) != null) {
            z = true;
        }
        return z;
    }

    public String formatOutOfCountryKeepingAlphaChars(PhoneNumber number, String regionCallingFrom) {
        String rawInput = number.getRawInput();
        if (rawInput.length() == 0) {
            return formatOutOfCountryCallingNumber(number, regionCallingFrom);
        }
        int countryCode = number.getCountryCode();
        if (!hasValidCountryCallingCode(countryCode)) {
            return rawInput;
        }
        rawInput = normalizeHelper(rawInput, ALL_PLUS_NUMBER_GROUPING_SYMBOLS, true);
        String nationalNumber = getNationalSignificantNumber(number);
        if (nationalNumber.length() > MAX_LENGTH_COUNTRY_CODE) {
            int firstNationalNumberDigit = rawInput.indexOf(nationalNumber.substring(0, MAX_LENGTH_COUNTRY_CODE));
            if (firstNationalNumberDigit != -1) {
                rawInput = rawInput.substring(firstNationalNumberDigit);
            }
        }
        PhoneMetadata metadataForRegionCallingFrom = getMetadataForRegion(regionCallingFrom);
        if (countryCode == NANPA_COUNTRY_CODE) {
            if (isNANPACountry(regionCallingFrom)) {
                return countryCode + Separators.SP + rawInput;
            }
        } else if (metadataForRegionCallingFrom != null && countryCode == getCountryCodeForValidRegion(regionCallingFrom)) {
            NumberFormat formattingPattern = chooseFormattingPatternForNumber(metadataForRegionCallingFrom.numberFormats(), nationalNumber);
            if (formattingPattern == null) {
                return rawInput;
            }
            NumberFormat newFormat = new NumberFormat();
            newFormat.mergeFrom(formattingPattern);
            newFormat.setPattern("(\\d+)(.*)");
            newFormat.setFormat("$1$2");
            return formatNsnUsingPattern(rawInput, newFormat, PhoneNumberFormat.NATIONAL);
        }
        String internationalPrefixForFormatting = "";
        if (metadataForRegionCallingFrom != null) {
            String internationalPrefix = metadataForRegionCallingFrom.getInternationalPrefix();
            if (UNIQUE_INTERNATIONAL_PREFIX.matcher(internationalPrefix).matches()) {
                internationalPrefixForFormatting = internationalPrefix;
            } else {
                internationalPrefixForFormatting = metadataForRegionCallingFrom.getPreferredInternationalPrefix();
            }
        }
        StringBuilder formattedNumber = new StringBuilder(rawInput);
        PhoneNumber phoneNumber = number;
        maybeAppendFormattedExtension(phoneNumber, getMetadataForRegionOrCallingCode(countryCode, getRegionCodeForCountryCode(countryCode)), PhoneNumberFormat.INTERNATIONAL, formattedNumber);
        if (internationalPrefixForFormatting.length() > 0) {
            formattedNumber.insert(0, Separators.SP).insert(0, countryCode).insert(0, Separators.SP).insert(0, internationalPrefixForFormatting);
        } else {
            logger.log(Level.WARNING, "Trying to format number from invalid region " + regionCallingFrom + ". International formatting applied.");
            prefixNumberWithCountryCallingCode(countryCode, PhoneNumberFormat.INTERNATIONAL, formattedNumber);
        }
        return formattedNumber.toString();
    }

    public String getNationalSignificantNumber(PhoneNumber number) {
        StringBuilder nationalNumber = new StringBuilder();
        if (number.isItalianLeadingZero()) {
            char[] zeros = new char[number.getNumberOfLeadingZeros()];
            Arrays.fill(zeros, '0');
            nationalNumber.append(new String(zeros));
        }
        nationalNumber.append(number.getNationalNumber());
        return nationalNumber.toString();
    }

    private void prefixNumberWithCountryCallingCode(int countryCallingCode, PhoneNumberFormat numberFormat, StringBuilder formattedNumber) {
        switch (-getcom-android-i18n-phonenumbers-PhoneNumberUtil$PhoneNumberFormatSwitchesValues()[numberFormat.ordinal()]) {
            case NANPA_COUNTRY_CODE /*1*/:
                formattedNumber.insert(0, countryCallingCode).insert(0, PLUS_SIGN);
            case MIN_LENGTH_FOR_NSN /*2*/:
                formattedNumber.insert(0, Separators.SP).insert(0, countryCallingCode).insert(0, PLUS_SIGN);
            case Schema.F_NOFORCE /*4*/:
                formattedNumber.insert(0, "-").insert(0, countryCallingCode).insert(0, PLUS_SIGN).insert(0, RFC3966_PREFIX);
            default:
        }
    }

    private String formatNsn(String number, PhoneMetadata metadata, PhoneNumberFormat numberFormat) {
        return formatNsn(number, metadata, numberFormat, null);
    }

    private String formatNsn(String number, PhoneMetadata metadata, PhoneNumberFormat numberFormat, String carrierCode) {
        List<Phonemetadata.NumberFormat> availableFormats;
        if (metadata.intlNumberFormats().size() == 0 || numberFormat == PhoneNumberFormat.NATIONAL) {
            availableFormats = metadata.numberFormats();
        } else {
            availableFormats = metadata.intlNumberFormats();
        }
        NumberFormat formattingPattern = chooseFormattingPatternForNumber(availableFormats, number);
        if (formattingPattern == null) {
            return number;
        }
        return formatNsnUsingPattern(number, formattingPattern, numberFormat, carrierCode);
    }

    NumberFormat chooseFormattingPatternForNumber(List<NumberFormat> availableFormats, String nationalNumber) {
        for (NumberFormat numFormat : availableFormats) {
            int size = numFormat.leadingDigitsPatternSize();
            if ((size == 0 || this.regexCache.getPatternForRegex(numFormat.getLeadingDigitsPattern(size - 1)).matcher(nationalNumber).lookingAt()) && this.regexCache.getPatternForRegex(numFormat.getPattern()).matcher(nationalNumber).matches()) {
                return numFormat;
            }
        }
        return null;
    }

    String formatNsnUsingPattern(String nationalNumber, NumberFormat formattingPattern, PhoneNumberFormat numberFormat) {
        return formatNsnUsingPattern(nationalNumber, formattingPattern, numberFormat, null);
    }

    private String formatNsnUsingPattern(String nationalNumber, NumberFormat formattingPattern, PhoneNumberFormat numberFormat, String carrierCode) {
        String numberFormatRule = formattingPattern.getFormat();
        Matcher m = this.regexCache.getPatternForRegex(formattingPattern.getPattern()).matcher(nationalNumber);
        String formattedNationalNumber = "";
        if (numberFormat != PhoneNumberFormat.NATIONAL || carrierCode == null || carrierCode.length() <= 0 || formattingPattern.getDomesticCarrierCodeFormattingRule().length() <= 0) {
            String nationalPrefixFormattingRule = formattingPattern.getNationalPrefixFormattingRule();
            if (numberFormat != PhoneNumberFormat.NATIONAL || nationalPrefixFormattingRule == null || nationalPrefixFormattingRule.length() <= 0) {
                formattedNationalNumber = m.replaceAll(numberFormatRule);
            } else {
                formattedNationalNumber = m.replaceAll(FIRST_GROUP_PATTERN.matcher(numberFormatRule).replaceFirst(nationalPrefixFormattingRule));
            }
        } else {
            formattedNationalNumber = m.replaceAll(FIRST_GROUP_PATTERN.matcher(numberFormatRule).replaceFirst(CC_PATTERN.matcher(formattingPattern.getDomesticCarrierCodeFormattingRule()).replaceFirst(carrierCode)));
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

    public PhoneNumber getExampleNumber(String regionCode) {
        return getExampleNumberForType(regionCode, PhoneNumberType.FIXED_LINE);
    }

    public PhoneNumber getExampleNumberForType(String regionCode, PhoneNumberType type) {
        if (isValidRegionCode(regionCode)) {
            PhoneNumberDesc desc = getNumberDescByType(getMetadataForRegion(regionCode), type);
            try {
                if (desc.hasExampleNumber()) {
                    return parse(desc.getExampleNumber(), regionCode);
                }
            } catch (NumberParseException e) {
                logger.log(Level.SEVERE, e.toString());
            }
            return null;
        }
        logger.log(Level.WARNING, "Invalid or unknown region code provided: " + regionCode);
        return null;
    }

    public PhoneNumber getExampleNumberForNonGeoEntity(int countryCallingCode) {
        PhoneMetadata metadata = getMetadataForNonGeographicalRegion(countryCallingCode);
        if (metadata != null) {
            PhoneNumberDesc desc = metadata.getGeneralDesc();
            try {
                if (desc.hasExampleNumber()) {
                    return parse("+" + countryCallingCode + desc.getExampleNumber(), UNKNOWN_REGION);
                }
            } catch (NumberParseException e) {
                logger.log(Level.SEVERE, e.toString());
            }
        } else {
            logger.log(Level.WARNING, "Invalid or unknown country calling code provided: " + countryCallingCode);
        }
        return null;
    }

    private void maybeAppendFormattedExtension(PhoneNumber number, PhoneMetadata metadata, PhoneNumberFormat numberFormat, StringBuilder formattedNumber) {
        if (number.hasExtension() && number.getExtension().length() > 0) {
            if (numberFormat == PhoneNumberFormat.RFC3966) {
                formattedNumber.append(RFC3966_EXTN_PREFIX).append(number.getExtension());
            } else if (metadata.hasPreferredExtnPrefix()) {
                formattedNumber.append(metadata.getPreferredExtnPrefix()).append(number.getExtension());
            } else {
                formattedNumber.append(DEFAULT_EXTN_PREFIX).append(number.getExtension());
            }
        }
    }

    PhoneNumberDesc getNumberDescByType(PhoneMetadata metadata, PhoneNumberType type) {
        switch (-getcom-android-i18n-phonenumbers-PhoneNumberUtil$PhoneNumberTypeSwitchesValues()[type.ordinal()]) {
            case NANPA_COUNTRY_CODE /*1*/:
            case MIN_LENGTH_FOR_NSN /*2*/:
                return metadata.getFixedLine();
            case MAX_LENGTH_COUNTRY_CODE /*3*/:
                return metadata.getMobile();
            case Schema.F_NOFORCE /*4*/:
                return metadata.getPager();
            case 5:
                return metadata.getPersonalNumber();
            case 6:
                return metadata.getPremiumRate();
            case 7:
                return metadata.getSharedCost();
            case HTMLModels.M_BLOCKINLINE /*8*/:
                return metadata.getTollFree();
            case TokenTypes.HT /*9*/:
                return metadata.getUan();
            case WarningHeader.ATTRIBUTE_NOT_UNDERSTOOD /*10*/:
                return metadata.getVoicemail();
            case 11:
                return metadata.getVoip();
            default:
                return metadata.getGeneralDesc();
        }
    }

    public PhoneNumberType getNumberType(PhoneNumber number) {
        String regionCode = getRegionCodeForNumber(number);
        PhoneMetadata metadata = getMetadataForRegionOrCallingCode(number.getCountryCode(), regionCode);
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

    private PhoneNumberType getNumberTypeHelper(String nationalNumber, PhoneMetadata metadata) {
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
            if (metadata.isSameMobileAndFixedLinePattern()) {
                return PhoneNumberType.FIXED_LINE_OR_MOBILE;
            }
            if (isNumberMatchingDesc(nationalNumber, metadata.getMobile())) {
                return PhoneNumberType.FIXED_LINE_OR_MOBILE;
            }
            return PhoneNumberType.FIXED_LINE;
        } else if (metadata.isSameMobileAndFixedLinePattern() || !isNumberMatchingDesc(nationalNumber, metadata.getMobile())) {
            return PhoneNumberType.UNKNOWN;
        } else {
            return PhoneNumberType.MOBILE;
        }
    }

    PhoneMetadata getMetadataForRegion(String regionCode) {
        if (isValidRegionCode(regionCode)) {
            return this.metadataSource.getMetadataForRegion(regionCode);
        }
        return null;
    }

    PhoneMetadata getMetadataForNonGeographicalRegion(int countryCallingCode) {
        if (this.countryCallingCodeToRegionCodeMap.containsKey(Integer.valueOf(countryCallingCode))) {
            return this.metadataSource.getMetadataForNonGeographicalRegion(countryCallingCode);
        }
        return null;
    }

    boolean isNumberPossibleForDesc(String nationalNumber, PhoneNumberDesc numberDesc) {
        return this.regexCache.getPatternForRegex(numberDesc.getPossibleNumberPattern()).matcher(nationalNumber).matches();
    }

    boolean isNumberMatchingDesc(String nationalNumber, PhoneNumberDesc numberDesc) {
        Matcher nationalNumberPatternMatcher = this.regexCache.getPatternForRegex(numberDesc.getNationalNumberPattern()).matcher(nationalNumber);
        if (isNumberPossibleForDesc(nationalNumber, numberDesc)) {
            return nationalNumberPatternMatcher.matches();
        }
        return false;
    }

    public boolean isValidNumber(PhoneNumber number) {
        return isValidNumberForRegion(number, getRegionCodeForNumber(number));
    }

    public boolean isValidNumberForRegion(PhoneNumber number, String regionCode) {
        boolean z = false;
        int countryCode = number.getCountryCode();
        PhoneMetadata metadata = getMetadataForRegionOrCallingCode(countryCode, regionCode);
        if (metadata == null || (!REGION_CODE_FOR_NON_GEO_ENTITY.equals(regionCode) && countryCode != getCountryCodeForValidRegion(regionCode))) {
            return false;
        }
        if (getNumberTypeHelper(getNationalSignificantNumber(number), metadata) != PhoneNumberType.UNKNOWN) {
            z = true;
        }
        return z;
    }

    public String getRegionCodeForNumber(PhoneNumber number) {
        int countryCode = number.getCountryCode();
        List<String> regions = (List) this.countryCallingCodeToRegionCodeMap.get(Integer.valueOf(countryCode));
        if (regions == null) {
            logger.log(Level.INFO, "Missing/invalid country_code (" + countryCode + ") for number " + getNationalSignificantNumber(number));
            return null;
        } else if (regions.size() == NANPA_COUNTRY_CODE) {
            return (String) regions.get(0);
        } else {
            return getRegionCodeForNumberFromRegionList(number, regions);
        }
    }

    private String getRegionCodeForNumberFromRegionList(PhoneNumber number, List<String> regionCodes) {
        String nationalNumber = getNationalSignificantNumber(number);
        for (String regionCode : regionCodes) {
            PhoneMetadata metadata = getMetadataForRegion(regionCode);
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
        List<String> regionCodes = (List) this.countryCallingCodeToRegionCodeMap.get(Integer.valueOf(countryCallingCode));
        return regionCodes == null ? UNKNOWN_REGION : (String) regionCodes.get(0);
    }

    public List<String> getRegionCodesForCountryCode(int countryCallingCode) {
        List list = (List) this.countryCallingCodeToRegionCodeMap.get(Integer.valueOf(countryCallingCode));
        if (list == null) {
            list = new ArrayList(0);
        }
        return Collections.unmodifiableList(list);
    }

    public int getCountryCodeForRegion(String regionCode) {
        if (isValidRegionCode(regionCode)) {
            return getCountryCodeForValidRegion(regionCode);
        }
        Logger logger = logger;
        Level level = Level.WARNING;
        StringBuilder append = new StringBuilder().append("Invalid or missing region code (");
        if (regionCode == null) {
            regionCode = "null";
        }
        logger.log(level, append.append(regionCode).append(") provided.").toString());
        return 0;
    }

    private int getCountryCodeForValidRegion(String regionCode) {
        PhoneMetadata metadata = getMetadataForRegion(regionCode);
        if (metadata != null) {
            return metadata.getCountryCode();
        }
        throw new IllegalArgumentException("Invalid region code: " + regionCode);
    }

    public String getNddPrefixForRegion(String regionCode, boolean stripNonDigits) {
        PhoneMetadata metadata = getMetadataForRegion(regionCode);
        if (metadata == null) {
            Logger logger = logger;
            Level level = Level.WARNING;
            StringBuilder append = new StringBuilder().append("Invalid or missing region code (");
            if (regionCode == null) {
                regionCode = "null";
            }
            logger.log(level, append.append(regionCode).append(") provided.").toString());
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

    boolean isLeadingZeroPossible(int countryCallingCode) {
        PhoneMetadata mainMetadataForCallingCode = getMetadataForRegionOrCallingCode(countryCallingCode, getRegionCodeForCountryCode(countryCallingCode));
        if (mainMetadataForCallingCode == null) {
            return false;
        }
        return mainMetadataForCallingCode.isLeadingZeroPossible();
    }

    public boolean isAlphaNumber(String number) {
        if (!isViablePhoneNumber(number)) {
            return false;
        }
        StringBuilder strippedNumber = new StringBuilder(number);
        maybeStripExtension(strippedNumber);
        return VALID_ALPHA_PHONE_PATTERN.matcher(strippedNumber).matches();
    }

    public boolean isPossibleNumber(PhoneNumber number) {
        return isPossibleNumberWithReason(number) == ValidationResult.IS_POSSIBLE;
    }

    private ValidationResult testNumberLengthAgainstPattern(Pattern numberPattern, String number) {
        Matcher numberMatcher = numberPattern.matcher(number);
        if (numberMatcher.matches()) {
            return ValidationResult.IS_POSSIBLE;
        }
        if (numberMatcher.lookingAt()) {
            return ValidationResult.TOO_LONG;
        }
        return ValidationResult.TOO_SHORT;
    }

    private boolean isShorterThanPossibleNormalNumber(PhoneMetadata regionMetadata, String number) {
        return testNumberLengthAgainstPattern(this.regexCache.getPatternForRegex(regionMetadata.getGeneralDesc().getPossibleNumberPattern()), number) == ValidationResult.TOO_SHORT;
    }

    public ValidationResult isPossibleNumberWithReason(PhoneNumber number) {
        String nationalNumber = getNationalSignificantNumber(number);
        int countryCode = number.getCountryCode();
        if (!hasValidCountryCallingCode(countryCode)) {
            return ValidationResult.INVALID_COUNTRY_CODE;
        }
        return testNumberLengthAgainstPattern(this.regexCache.getPatternForRegex(getMetadataForRegionOrCallingCode(countryCode, getRegionCodeForCountryCode(countryCode)).getGeneralDesc().getPossibleNumberPattern()), nationalNumber);
    }

    public boolean isPossibleNumber(String number, String regionDialingFrom) {
        try {
            return isPossibleNumber(parse(number, regionDialingFrom));
        } catch (NumberParseException e) {
            return false;
        }
    }

    public boolean truncateTooLongNumber(PhoneNumber number) {
        if (isValidNumber(number)) {
            return true;
        }
        PhoneNumber numberCopy = new PhoneNumber();
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

    int extractCountryCode(StringBuilder fullNumber, StringBuilder nationalNumber) {
        if (fullNumber.length() == 0 || fullNumber.charAt(0) == '0') {
            return 0;
        }
        int numberLength = fullNumber.length();
        int i = NANPA_COUNTRY_CODE;
        while (i <= MAX_LENGTH_COUNTRY_CODE && i <= numberLength) {
            int potentialCountryCode = Integer.parseInt(fullNumber.substring(0, i));
            if (this.countryCallingCodeToRegionCodeMap.containsKey(Integer.valueOf(potentialCountryCode))) {
                nationalNumber.append(fullNumber.substring(i));
                return potentialCountryCode;
            }
            i += NANPA_COUNTRY_CODE;
        }
        return 0;
    }

    int maybeExtractCountryCode(String number, PhoneMetadata defaultRegionMetadata, StringBuilder nationalNumber, boolean keepRawInput, PhoneNumber phoneNumber) throws NumberParseException {
        if (number.length() == 0) {
            return 0;
        }
        StringBuilder fullNumber = new StringBuilder(number);
        String possibleCountryIddPrefix = "NonMatch";
        if (defaultRegionMetadata != null) {
            possibleCountryIddPrefix = defaultRegionMetadata.getInternationalPrefix();
        }
        CountryCodeSource countryCodeSource = maybeStripInternationalPrefixAndNormalize(fullNumber, possibleCountryIddPrefix);
        if (keepRawInput) {
            phoneNumber.setCountryCodeSource(countryCodeSource);
        }
        if (countryCodeSource == CountryCodeSource.FROM_DEFAULT_COUNTRY) {
            if (defaultRegionMetadata != null) {
                int defaultCountryCode = defaultRegionMetadata.getCountryCode();
                String defaultCountryCodeString = String.valueOf(defaultCountryCode);
                String normalizedNumber = fullNumber.toString();
                if (normalizedNumber.startsWith(defaultCountryCodeString)) {
                    StringBuilder potentialNationalNumber = new StringBuilder(normalizedNumber.substring(defaultCountryCodeString.length()));
                    PhoneNumberDesc generalDesc = defaultRegionMetadata.getGeneralDesc();
                    Pattern validNumberPattern = this.regexCache.getPatternForRegex(generalDesc.getNationalNumberPattern());
                    maybeStripNationalPrefixAndCarrierCode(potentialNationalNumber, defaultRegionMetadata, null);
                    Pattern possibleNumberPattern = this.regexCache.getPatternForRegex(generalDesc.getPossibleNumberPattern());
                    if ((!validNumberPattern.matcher(fullNumber).matches() && validNumberPattern.matcher(potentialNationalNumber).matches()) || testNumberLengthAgainstPattern(possibleNumberPattern, fullNumber.toString()) == ValidationResult.TOO_LONG) {
                        nationalNumber.append(potentialNationalNumber);
                        if (keepRawInput) {
                            phoneNumber.setCountryCodeSource(CountryCodeSource.FROM_NUMBER_WITHOUT_PLUS_SIGN);
                        }
                        phoneNumber.setCountryCode(defaultCountryCode);
                        return defaultCountryCode;
                    }
                }
            }
            phoneNumber.setCountryCode(0);
            return 0;
        } else if (fullNumber.length() <= MIN_LENGTH_FOR_NSN) {
            throw new NumberParseException(ErrorType.TOO_SHORT_AFTER_IDD, "Phone number had an IDD, but after this was not long enough to be a viable phone number.");
        } else {
            int potentialCountryCode = extractCountryCode(fullNumber, nationalNumber);
            if (potentialCountryCode != 0) {
                phoneNumber.setCountryCode(potentialCountryCode);
                return potentialCountryCode;
            }
            throw new NumberParseException(ErrorType.INVALID_COUNTRY_CODE, "Country calling code supplied was not recognised.");
        }
    }

    private boolean parsePrefixAsIdd(Pattern iddPattern, StringBuilder number) {
        Matcher m = iddPattern.matcher(number);
        if (!m.lookingAt()) {
            return false;
        }
        int matchEnd = m.end();
        Matcher digitMatcher = CAPTURING_DIGIT_PATTERN.matcher(number.substring(matchEnd));
        if (digitMatcher.find() && normalizeDigitsOnly(digitMatcher.group(NANPA_COUNTRY_CODE)).equals("0")) {
            return false;
        }
        number.delete(0, matchEnd);
        return true;
    }

    CountryCodeSource maybeStripInternationalPrefixAndNormalize(StringBuilder number, String possibleIddPrefix) {
        if (number.length() == 0) {
            return CountryCodeSource.FROM_DEFAULT_COUNTRY;
        }
        Matcher m = PLUS_CHARS_PATTERN.matcher(number);
        if (m.lookingAt()) {
            number.delete(0, m.end());
            normalize(number);
            return CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN;
        }
        CountryCodeSource countryCodeSource;
        Pattern iddPattern = this.regexCache.getPatternForRegex(possibleIddPrefix);
        normalize(number);
        if (parsePrefixAsIdd(iddPattern, number)) {
            countryCodeSource = CountryCodeSource.FROM_NUMBER_WITH_IDD;
        } else {
            countryCodeSource = CountryCodeSource.FROM_DEFAULT_COUNTRY;
        }
        return countryCodeSource;
    }

    boolean maybeStripNationalPrefixAndCarrierCode(StringBuilder number, PhoneMetadata metadata, StringBuilder carrierCode) {
        int numberLength = number.length();
        String possibleNationalPrefix = metadata.getNationalPrefixForParsing();
        if (numberLength == 0 || possibleNationalPrefix.length() == 0) {
            return false;
        }
        Matcher prefixMatcher = this.regexCache.getPatternForRegex(possibleNationalPrefix).matcher(number);
        if (!prefixMatcher.lookingAt()) {
            return false;
        }
        Pattern nationalNumberRule = this.regexCache.getPatternForRegex(metadata.getGeneralDesc().getNationalNumberPattern());
        boolean isViableOriginalNumber = nationalNumberRule.matcher(number).matches();
        int numOfGroups = prefixMatcher.groupCount();
        String transformRule = metadata.getNationalPrefixTransformRule();
        if (transformRule != null && transformRule.length() != 0 && prefixMatcher.group(numOfGroups) != null) {
            StringBuilder transformedNumber = new StringBuilder(number);
            transformedNumber.replace(0, numberLength, prefixMatcher.replaceFirst(transformRule));
            if (isViableOriginalNumber && !nationalNumberRule.matcher(transformedNumber.toString()).matches()) {
                return false;
            }
            if (carrierCode != null && numOfGroups > NANPA_COUNTRY_CODE) {
                carrierCode.append(prefixMatcher.group(NANPA_COUNTRY_CODE));
            }
            number.replace(0, number.length(), transformedNumber.toString());
            return true;
        } else if (isViableOriginalNumber && !nationalNumberRule.matcher(number.substring(prefixMatcher.end())).matches()) {
            return false;
        } else {
            if (!(carrierCode == null || numOfGroups <= 0 || prefixMatcher.group(numOfGroups) == null)) {
                carrierCode.append(prefixMatcher.group(NANPA_COUNTRY_CODE));
            }
            number.delete(0, prefixMatcher.end());
            return true;
        }
    }

    String maybeStripExtension(StringBuilder number) {
        Matcher m = EXTN_PATTERN.matcher(number);
        if (m.find() && isViablePhoneNumber(number.substring(0, m.start()))) {
            int length = m.groupCount();
            for (int i = NANPA_COUNTRY_CODE; i <= length; i += NANPA_COUNTRY_CODE) {
                if (m.group(i) != null) {
                    String extension = m.group(i);
                    number.delete(m.start(), number.length());
                    return extension;
                }
            }
        }
        return "";
    }

    private boolean checkRegionForParsing(String numberToParse, String defaultRegion) {
        if (isValidRegionCode(defaultRegion) || (numberToParse != null && numberToParse.length() != 0 && PLUS_CHARS_PATTERN.matcher(numberToParse).lookingAt())) {
            return true;
        }
        return false;
    }

    public PhoneNumber parse(String numberToParse, String defaultRegion) throws NumberParseException {
        PhoneNumber phoneNumber = new PhoneNumber();
        parse(numberToParse, defaultRegion, phoneNumber);
        return phoneNumber;
    }

    public void parse(String numberToParse, String defaultRegion, PhoneNumber phoneNumber) throws NumberParseException {
        parseHelper(numberToParse, defaultRegion, true, true, phoneNumber);
    }

    public PhoneNumber parseAndKeepRawInput(String numberToParse, String defaultRegion) throws NumberParseException {
        PhoneNumber phoneNumber = new PhoneNumber();
        parseAndKeepRawInput(numberToParse, defaultRegion, phoneNumber);
        return phoneNumber;
    }

    public void parseAndKeepRawInput(String numberToParse, String defaultRegion, PhoneNumber phoneNumber) throws NumberParseException {
        parseHelper(numberToParse, defaultRegion, true, true, phoneNumber);
    }

    public Iterable<PhoneNumberMatch> findNumbers(CharSequence text, String defaultRegion) {
        return findNumbers(text, defaultRegion, Leniency.VALID, Long.MAX_VALUE);
    }

    public Iterable<PhoneNumberMatch> findNumbers(CharSequence text, String defaultRegion, Leniency leniency, long maxTries) {
        return new AnonymousClass2(text, defaultRegion, leniency, maxTries);
    }

    static void setItalianLeadingZerosForPhoneNumber(String nationalNumber, PhoneNumber phoneNumber) {
        if (nationalNumber.length() > NANPA_COUNTRY_CODE && nationalNumber.charAt(0) == '0') {
            phoneNumber.setItalianLeadingZero(true);
            int numberOfLeadingZeros = NANPA_COUNTRY_CODE;
            while (numberOfLeadingZeros < nationalNumber.length() - 1 && nationalNumber.charAt(numberOfLeadingZeros) == '0') {
                numberOfLeadingZeros += NANPA_COUNTRY_CODE;
            }
            if (numberOfLeadingZeros != NANPA_COUNTRY_CODE) {
                phoneNumber.setNumberOfLeadingZeros(numberOfLeadingZeros);
            }
        }
    }

    private void parseHelper(String numberToParse, String defaultRegion, boolean keepRawInput, boolean checkRegion, PhoneNumber phoneNumber) throws NumberParseException {
        int countryCode;
        if (numberToParse == null) {
            throw new NumberParseException(ErrorType.NOT_A_NUMBER, "The phone number supplied was null.");
        } else if (numberToParse.length() > MAX_INPUT_STRING_LENGTH) {
            throw new NumberParseException(ErrorType.TOO_LONG, "The string supplied was too long to parse.");
        } else {
            StringBuilder nationalNumber = new StringBuilder();
            buildNationalNumberForParsing(numberToParse, nationalNumber);
            if (!isViablePhoneNumber(nationalNumber.toString())) {
                throw new NumberParseException(ErrorType.NOT_A_NUMBER, "The string supplied did not seem to be a phone number.");
            } else if (!checkRegion || checkRegionForParsing(nationalNumber.toString(), defaultRegion)) {
                if (keepRawInput) {
                    phoneNumber.setRawInput(numberToParse);
                }
                String extension = maybeStripExtension(nationalNumber);
                if (extension.length() > 0) {
                    phoneNumber.setExtension(extension);
                }
                PhoneMetadata regionMetadata = getMetadataForRegion(defaultRegion);
                StringBuilder normalizedNationalNumber = new StringBuilder();
                try {
                    countryCode = maybeExtractCountryCode(nationalNumber.toString(), regionMetadata, normalizedNationalNumber, keepRawInput, phoneNumber);
                } catch (NumberParseException e) {
                    Matcher matcher = PLUS_CHARS_PATTERN.matcher(nationalNumber.toString());
                    if (e.getErrorType() == ErrorType.INVALID_COUNTRY_CODE && matcher.lookingAt()) {
                        countryCode = maybeExtractCountryCode(nationalNumber.substring(matcher.end()), regionMetadata, normalizedNationalNumber, keepRawInput, phoneNumber);
                        if (countryCode == 0) {
                            throw new NumberParseException(ErrorType.INVALID_COUNTRY_CODE, "Could not interpret numbers after plus-sign.");
                        }
                    }
                    throw new NumberParseException(e.getErrorType(), e.getMessage());
                }
                if (countryCode != 0) {
                    String phoneNumberRegion = getRegionCodeForCountryCode(countryCode);
                    if (!phoneNumberRegion.equals(defaultRegion)) {
                        regionMetadata = getMetadataForRegionOrCallingCode(countryCode, phoneNumberRegion);
                    }
                } else {
                    normalize(nationalNumber);
                    normalizedNationalNumber.append(nationalNumber);
                    if (defaultRegion != null) {
                        phoneNumber.setCountryCode(regionMetadata.getCountryCode());
                    } else if (keepRawInput) {
                        phoneNumber.clearCountryCodeSource();
                    }
                }
                if (normalizedNationalNumber.length() < MIN_LENGTH_FOR_NSN) {
                    throw new NumberParseException(ErrorType.TOO_SHORT_NSN, "The string supplied is too short to be a phone number.");
                }
                if (regionMetadata != null) {
                    StringBuilder carrierCode = new StringBuilder();
                    StringBuilder stringBuilder = new StringBuilder(normalizedNationalNumber);
                    maybeStripNationalPrefixAndCarrierCode(stringBuilder, regionMetadata, carrierCode);
                    if (!isShorterThanPossibleNormalNumber(regionMetadata, stringBuilder.toString())) {
                        normalizedNationalNumber = stringBuilder;
                        if (keepRawInput) {
                            phoneNumber.setPreferredDomesticCarrierCode(carrierCode.toString());
                        }
                    }
                }
                int lengthOfNationalNumber = normalizedNationalNumber.length();
                if (lengthOfNationalNumber < MIN_LENGTH_FOR_NSN) {
                    throw new NumberParseException(ErrorType.TOO_SHORT_NSN, "The string supplied is too short to be a phone number.");
                } else if (lengthOfNationalNumber > MAX_LENGTH_FOR_NSN) {
                    throw new NumberParseException(ErrorType.TOO_LONG, "The string supplied is too long to be a phone number.");
                } else {
                    setItalianLeadingZerosForPhoneNumber(normalizedNationalNumber.toString(), phoneNumber);
                    phoneNumber.setNationalNumber(Long.parseLong(normalizedNationalNumber.toString()));
                }
            } else {
                throw new NumberParseException(ErrorType.INVALID_COUNTRY_CODE, "Missing or invalid default region.");
            }
        }
    }

    private void buildNationalNumberForParsing(String numberToParse, StringBuilder nationalNumber) {
        int indexOfNationalNumber = 0;
        int indexOfPhoneContext = numberToParse.indexOf(RFC3966_PHONE_CONTEXT);
        if (indexOfPhoneContext > 0) {
            int phoneContextStart = indexOfPhoneContext + RFC3966_PHONE_CONTEXT.length();
            if (numberToParse.charAt(phoneContextStart) == PLUS_SIGN) {
                int phoneContextEnd = numberToParse.indexOf(59, phoneContextStart);
                if (phoneContextEnd > 0) {
                    nationalNumber.append(numberToParse.substring(phoneContextStart, phoneContextEnd));
                } else {
                    nationalNumber.append(numberToParse.substring(phoneContextStart));
                }
            }
            int indexOfRfc3966Prefix = numberToParse.indexOf(RFC3966_PREFIX);
            if (indexOfRfc3966Prefix >= 0) {
                indexOfNationalNumber = indexOfRfc3966Prefix + RFC3966_PREFIX.length();
            }
            nationalNumber.append(numberToParse.substring(indexOfNationalNumber, indexOfPhoneContext));
        } else {
            nationalNumber.append(extractPossibleNumber(numberToParse));
        }
        int indexOfIsdn = nationalNumber.indexOf(RFC3966_ISDN_SUBADDRESS);
        if (indexOfIsdn > 0) {
            nationalNumber.delete(indexOfIsdn, nationalNumber.length());
        }
    }

    public MatchType isNumberMatch(PhoneNumber firstNumberIn, PhoneNumber secondNumberIn) {
        PhoneNumber firstNumber = new PhoneNumber();
        firstNumber.mergeFrom(firstNumberIn);
        PhoneNumber secondNumber = new PhoneNumber();
        secondNumber.mergeFrom(secondNumberIn);
        firstNumber.clearRawInput();
        firstNumber.clearCountryCodeSource();
        firstNumber.clearPreferredDomesticCarrierCode();
        secondNumber.clearRawInput();
        secondNumber.clearCountryCodeSource();
        secondNumber.clearPreferredDomesticCarrierCode();
        if (firstNumber.hasExtension() && firstNumber.getExtension().length() == 0) {
            firstNumber.clearExtension();
        }
        if (secondNumber.hasExtension() && secondNumber.getExtension().length() == 0) {
            secondNumber.clearExtension();
        }
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
            if (firstNumberCountryCode == secondNumberCountryCode && isNationalNumberSuffixOfTheOther(firstNumber, secondNumber)) {
                return MatchType.SHORT_NSN_MATCH;
            }
            return MatchType.NO_MATCH;
        }
    }

    private boolean isNationalNumberSuffixOfTheOther(PhoneNumber firstNumber, PhoneNumber secondNumber) {
        String firstNumberNationalNumber = String.valueOf(firstNumber.getNationalNumber());
        String secondNumberNationalNumber = String.valueOf(secondNumber.getNationalNumber());
        if (firstNumberNationalNumber.endsWith(secondNumberNationalNumber)) {
            return true;
        }
        return secondNumberNationalNumber.endsWith(firstNumberNationalNumber);
    }

    public MatchType isNumberMatch(String firstNumber, String secondNumber) {
        try {
            return isNumberMatch(parse(firstNumber, UNKNOWN_REGION), secondNumber);
        } catch (NumberParseException e) {
            if (e.getErrorType() == ErrorType.INVALID_COUNTRY_CODE) {
                try {
                    return isNumberMatch(parse(secondNumber, UNKNOWN_REGION), firstNumber);
                } catch (NumberParseException e2) {
                    if (e2.getErrorType() == ErrorType.INVALID_COUNTRY_CODE) {
                        try {
                            PhoneNumber firstNumberProto = new PhoneNumber();
                            PhoneNumber secondNumberProto = new PhoneNumber();
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

    public MatchType isNumberMatch(PhoneNumber firstNumber, String secondNumber) {
        try {
            return isNumberMatch(firstNumber, parse(secondNumber, UNKNOWN_REGION));
        } catch (NumberParseException e) {
            if (e.getErrorType() == ErrorType.INVALID_COUNTRY_CODE) {
                String firstNumberRegion = getRegionCodeForCountryCode(firstNumber.getCountryCode());
                try {
                    if (firstNumberRegion.equals(UNKNOWN_REGION)) {
                        PhoneNumber secondNumberProto = new PhoneNumber();
                        parseHelper(secondNumber, null, false, false, secondNumberProto);
                        return isNumberMatch(firstNumber, secondNumberProto);
                    }
                    MatchType match = isNumberMatch(firstNumber, parse(secondNumber, firstNumberRegion));
                    if (match == MatchType.EXACT_MATCH) {
                        return MatchType.NSN_MATCH;
                    }
                    return match;
                } catch (NumberParseException e2) {
                    return MatchType.NOT_A_NUMBER;
                }
            }
            return MatchType.NOT_A_NUMBER;
        }
    }

    boolean canBeInternationallyDialled(PhoneNumber number) {
        boolean z = true;
        PhoneMetadata metadata = getMetadataForRegion(getRegionCodeForNumber(number));
        if (metadata == null) {
            return true;
        }
        if (isNumberMatchingDesc(getNationalSignificantNumber(number), metadata.getNoInternationalDialling())) {
            z = false;
        }
        return z;
    }

    public boolean isMobileNumberPortableRegion(String regionCode) {
        PhoneMetadata metadata = getMetadataForRegion(regionCode);
        if (metadata != null) {
            return metadata.isMobileNumberPortableRegion();
        }
        logger.log(Level.WARNING, "Invalid or unknown region code provided: " + regionCode);
        return false;
    }
}
