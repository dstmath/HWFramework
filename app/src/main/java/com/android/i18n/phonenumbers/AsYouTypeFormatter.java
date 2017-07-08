package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.Phonemetadata.NumberFormat;
import com.android.i18n.phonenumbers.Phonemetadata.PhoneMetadata;
import com.google.i18n.phonenumbers.Phonemetadata;
import gov.nist.javax.sip.parser.TokenTypes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ccil.cowan.tagsoup.Schema;

public class AsYouTypeFormatter {
    private static final Pattern CHARACTER_CLASS_PATTERN = null;
    private static final Pattern DIGIT_PATTERN = null;
    private static final String DIGIT_PLACEHOLDER = "\u2008";
    private static final Pattern ELIGIBLE_FORMAT_PATTERN = null;
    private static final PhoneMetadata EMPTY_METADATA = null;
    private static final int MIN_LEADING_DIGITS_LENGTH = 3;
    private static final Pattern NATIONAL_PREFIX_SEPARATORS_PATTERN = null;
    private static final char SEPARATOR_BEFORE_NATIONAL_NUMBER = ' ';
    private static final Pattern STANDALONE_DIGIT_PATTERN = null;
    private boolean ableToFormat;
    private StringBuilder accruedInput;
    private StringBuilder accruedInputWithoutFormatting;
    private String currentFormattingPattern;
    private PhoneMetadata currentMetadata;
    private String currentOutput;
    private String defaultCountry;
    private PhoneMetadata defaultMetadata;
    private String extractedNationalPrefix;
    private StringBuilder formattingTemplate;
    private boolean inputHasFormatting;
    private boolean isCompleteNumber;
    private boolean isExpectingCountryCallingCode;
    private int lastMatchPosition;
    private StringBuilder nationalNumber;
    private int originalPosition;
    private final PhoneNumberUtil phoneUtil;
    private int positionToRemember;
    private List<NumberFormat> possibleFormats;
    private StringBuilder prefixBeforeNationalNumber;
    private RegexCache regexCache;
    private boolean shouldAddSpaceAfterNationalPrefix;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.i18n.phonenumbers.AsYouTypeFormatter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.i18n.phonenumbers.AsYouTypeFormatter.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.AsYouTypeFormatter.<clinit>():void");
    }

    AsYouTypeFormatter(String regionCode) {
        this.currentOutput = "";
        this.formattingTemplate = new StringBuilder();
        this.currentFormattingPattern = "";
        this.accruedInput = new StringBuilder();
        this.accruedInputWithoutFormatting = new StringBuilder();
        this.ableToFormat = true;
        this.inputHasFormatting = false;
        this.isCompleteNumber = false;
        this.isExpectingCountryCallingCode = false;
        this.phoneUtil = PhoneNumberUtil.getInstance();
        this.lastMatchPosition = 0;
        this.originalPosition = 0;
        this.positionToRemember = 0;
        this.prefixBeforeNationalNumber = new StringBuilder();
        this.shouldAddSpaceAfterNationalPrefix = false;
        this.extractedNationalPrefix = "";
        this.nationalNumber = new StringBuilder();
        this.possibleFormats = new ArrayList();
        this.regexCache = new RegexCache(64);
        this.defaultCountry = regionCode;
        this.currentMetadata = getMetadataForRegion(this.defaultCountry);
        this.defaultMetadata = this.currentMetadata;
    }

    private PhoneMetadata getMetadataForRegion(String regionCode) {
        PhoneMetadata metadata = this.phoneUtil.getMetadataForRegion(this.phoneUtil.getRegionCodeForCountryCode(this.phoneUtil.getCountryCodeForRegion(regionCode)));
        if (metadata != null) {
            return metadata;
        }
        return EMPTY_METADATA;
    }

    private boolean maybeCreateNewTemplate() {
        Iterator<Phonemetadata.NumberFormat> it = this.possibleFormats.iterator();
        while (it.hasNext()) {
            NumberFormat numberFormat = (NumberFormat) it.next();
            String pattern = numberFormat.getPattern();
            if (this.currentFormattingPattern.equals(pattern)) {
                return false;
            }
            if (createFormattingTemplate(numberFormat)) {
                this.currentFormattingPattern = pattern;
                this.shouldAddSpaceAfterNationalPrefix = NATIONAL_PREFIX_SEPARATORS_PATTERN.matcher(numberFormat.getNationalPrefixFormattingRule()).find();
                this.lastMatchPosition = 0;
                return true;
            }
            it.remove();
        }
        this.ableToFormat = false;
        return false;
    }

    private void getAvailableFormats(String leadingDigits) {
        List<Phonemetadata.NumberFormat> formatList;
        if (!this.isCompleteNumber || this.currentMetadata.intlNumberFormatSize() <= 0) {
            formatList = this.currentMetadata.numberFormats();
        } else {
            formatList = this.currentMetadata.intlNumberFormats();
        }
        boolean nationalPrefixIsUsedByCountry = this.currentMetadata.hasNationalPrefix();
        Iterator format$iterator = formatList.iterator();
        while (format$iterator.hasNext()) {
            NumberFormat format = (NumberFormat) format$iterator.next();
            if ((!nationalPrefixIsUsedByCountry || this.isCompleteNumber || format.isNationalPrefixOptionalWhenFormatting() || PhoneNumberUtil.formattingRuleHasFirstGroupOnly(format.getNationalPrefixFormattingRule())) && isFormatEligible(format.getFormat())) {
                this.possibleFormats.add(format);
            }
        }
        narrowDownPossibleFormats(leadingDigits);
    }

    private boolean isFormatEligible(String format) {
        return ELIGIBLE_FORMAT_PATTERN.matcher(format).matches();
    }

    private void narrowDownPossibleFormats(String leadingDigits) {
        int indexOfLeadingDigitsPattern = leadingDigits.length() - 3;
        Iterator<Phonemetadata.NumberFormat> it = this.possibleFormats.iterator();
        while (it.hasNext()) {
            NumberFormat format = (NumberFormat) it.next();
            if (format.leadingDigitsPatternSize() != 0) {
                if (!this.regexCache.getPatternForRegex(format.getLeadingDigitsPattern(Math.min(indexOfLeadingDigitsPattern, format.leadingDigitsPatternSize() - 1))).matcher(leadingDigits).lookingAt()) {
                    it.remove();
                }
            }
        }
    }

    private boolean createFormattingTemplate(NumberFormat format) {
        String numberPattern = format.getPattern();
        if (numberPattern.indexOf(TokenTypes.BAR) != -1) {
            return false;
        }
        numberPattern = STANDALONE_DIGIT_PATTERN.matcher(CHARACTER_CLASS_PATTERN.matcher(numberPattern).replaceAll("\\\\d")).replaceAll("\\\\d");
        this.formattingTemplate.setLength(0);
        String tempTemplate = getFormattingTemplate(numberPattern, format.getFormat());
        if (tempTemplate.length() <= 0) {
            return false;
        }
        this.formattingTemplate.append(tempTemplate);
        return true;
    }

    private String getFormattingTemplate(String numberPattern, String numberFormat) {
        Matcher m = this.regexCache.getPatternForRegex(numberPattern).matcher("999999999999999");
        m.find();
        String aPhoneNumber = m.group();
        if (aPhoneNumber.length() < this.nationalNumber.length()) {
            return "";
        }
        return aPhoneNumber.replaceAll(numberPattern, numberFormat).replaceAll("9", DIGIT_PLACEHOLDER);
    }

    public void clear() {
        this.currentOutput = "";
        this.accruedInput.setLength(0);
        this.accruedInputWithoutFormatting.setLength(0);
        this.formattingTemplate.setLength(0);
        this.lastMatchPosition = 0;
        this.currentFormattingPattern = "";
        this.prefixBeforeNationalNumber.setLength(0);
        this.extractedNationalPrefix = "";
        this.nationalNumber.setLength(0);
        this.ableToFormat = true;
        this.inputHasFormatting = false;
        this.positionToRemember = 0;
        this.originalPosition = 0;
        this.isCompleteNumber = false;
        this.isExpectingCountryCallingCode = false;
        this.possibleFormats.clear();
        this.shouldAddSpaceAfterNationalPrefix = false;
        if (!this.currentMetadata.equals(this.defaultMetadata)) {
            this.currentMetadata = getMetadataForRegion(this.defaultCountry);
        }
    }

    public String inputDigit(char nextChar) {
        this.currentOutput = inputDigitWithOptionToRememberPosition(nextChar, false);
        return this.currentOutput;
    }

    public String inputDigitAndRememberPosition(char nextChar) {
        this.currentOutput = inputDigitWithOptionToRememberPosition(nextChar, true);
        return this.currentOutput;
    }

    private String inputDigitWithOptionToRememberPosition(char nextChar, boolean rememberPosition) {
        this.accruedInput.append(nextChar);
        if (rememberPosition) {
            this.originalPosition = this.accruedInput.length();
        }
        if (isDigitOrLeadingPlusSign(nextChar)) {
            nextChar = normalizeAndAccrueDigitsAndPlusSign(nextChar, rememberPosition);
        } else {
            this.ableToFormat = false;
            this.inputHasFormatting = true;
        }
        if (this.ableToFormat) {
            switch (this.accruedInputWithoutFormatting.length()) {
                case Schema.M_EMPTY /*0*/:
                case Schema.F_RESTART /*1*/:
                case Schema.F_CDATA /*2*/:
                    return this.accruedInput.toString();
                case MIN_LEADING_DIGITS_LENGTH /*3*/:
                    if (attemptToExtractIdd()) {
                        this.isExpectingCountryCallingCode = true;
                        break;
                    }
                    this.extractedNationalPrefix = removeNationalPrefixFromNationalNumber();
                    return attemptToChooseFormattingPattern();
            }
            if (this.isExpectingCountryCallingCode) {
                if (attemptToExtractCountryCallingCode()) {
                    this.isExpectingCountryCallingCode = false;
                }
                return this.prefixBeforeNationalNumber + this.nationalNumber.toString();
            } else if (this.possibleFormats.size() <= 0) {
                return attemptToChooseFormattingPattern();
            } else {
                String tempNationalNumber = inputDigitHelper(nextChar);
                String formattedNumber = attemptToFormatAccruedDigits();
                if (formattedNumber.length() > 0) {
                    return formattedNumber;
                }
                narrowDownPossibleFormats(this.nationalNumber.toString());
                if (maybeCreateNewTemplate()) {
                    return inputAccruedNationalNumber();
                }
                String appendNationalNumber;
                if (this.ableToFormat) {
                    appendNationalNumber = appendNationalNumber(tempNationalNumber);
                } else {
                    appendNationalNumber = this.accruedInput.toString();
                }
                return appendNationalNumber;
            }
        } else if (this.inputHasFormatting) {
            return this.accruedInput.toString();
        } else {
            if (attemptToExtractIdd()) {
                if (attemptToExtractCountryCallingCode()) {
                    return attemptToChoosePatternWithPrefixExtracted();
                }
            } else if (ableToExtractLongerNdd()) {
                this.prefixBeforeNationalNumber.append(SEPARATOR_BEFORE_NATIONAL_NUMBER);
                return attemptToChoosePatternWithPrefixExtracted();
            }
            return this.accruedInput.toString();
        }
    }

    private String attemptToChoosePatternWithPrefixExtracted() {
        this.ableToFormat = true;
        this.isExpectingCountryCallingCode = false;
        this.possibleFormats.clear();
        this.lastMatchPosition = 0;
        this.formattingTemplate.setLength(0);
        this.currentFormattingPattern = "";
        return attemptToChooseFormattingPattern();
    }

    String getExtractedNationalPrefix() {
        return this.extractedNationalPrefix;
    }

    private boolean ableToExtractLongerNdd() {
        if (this.extractedNationalPrefix.length() > 0) {
            this.nationalNumber.insert(0, this.extractedNationalPrefix);
            this.prefixBeforeNationalNumber.setLength(this.prefixBeforeNationalNumber.lastIndexOf(this.extractedNationalPrefix));
        }
        if (this.extractedNationalPrefix.equals(removeNationalPrefixFromNationalNumber())) {
            return false;
        }
        return true;
    }

    private boolean isDigitOrLeadingPlusSign(char nextChar) {
        if (Character.isDigit(nextChar)) {
            return true;
        }
        if (this.accruedInput.length() == 1) {
            return PhoneNumberUtil.PLUS_CHARS_PATTERN.matcher(Character.toString(nextChar)).matches();
        }
        return false;
    }

    String attemptToFormatAccruedDigits() {
        for (NumberFormat numberFormat : this.possibleFormats) {
            Matcher m = this.regexCache.getPatternForRegex(numberFormat.getPattern()).matcher(this.nationalNumber);
            if (m.matches()) {
                this.shouldAddSpaceAfterNationalPrefix = NATIONAL_PREFIX_SEPARATORS_PATTERN.matcher(numberFormat.getNationalPrefixFormattingRule()).find();
                return appendNationalNumber(m.replaceAll(numberFormat.getFormat()));
            }
        }
        return "";
    }

    public int getRememberedPosition() {
        if (!this.ableToFormat) {
            return this.originalPosition;
        }
        int accruedInputIndex = 0;
        int currentOutputIndex = 0;
        while (accruedInputIndex < this.positionToRemember && currentOutputIndex < this.currentOutput.length()) {
            if (this.accruedInputWithoutFormatting.charAt(accruedInputIndex) == this.currentOutput.charAt(currentOutputIndex)) {
                accruedInputIndex++;
            }
            currentOutputIndex++;
        }
        return currentOutputIndex;
    }

    private String appendNationalNumber(String nationalNumber) {
        int prefixBeforeNationalNumberLength = this.prefixBeforeNationalNumber.length();
        if (!this.shouldAddSpaceAfterNationalPrefix || prefixBeforeNationalNumberLength <= 0 || this.prefixBeforeNationalNumber.charAt(prefixBeforeNationalNumberLength - 1) == SEPARATOR_BEFORE_NATIONAL_NUMBER) {
            return this.prefixBeforeNationalNumber + nationalNumber;
        }
        return new String(this.prefixBeforeNationalNumber) + SEPARATOR_BEFORE_NATIONAL_NUMBER + nationalNumber;
    }

    private String attemptToChooseFormattingPattern() {
        if (this.nationalNumber.length() < MIN_LEADING_DIGITS_LENGTH) {
            return appendNationalNumber(this.nationalNumber.toString());
        }
        getAvailableFormats(this.nationalNumber.toString());
        String formattedNumber = attemptToFormatAccruedDigits();
        if (formattedNumber.length() > 0) {
            return formattedNumber;
        }
        return maybeCreateNewTemplate() ? inputAccruedNationalNumber() : this.accruedInput.toString();
    }

    private String inputAccruedNationalNumber() {
        int lengthOfNationalNumber = this.nationalNumber.length();
        if (lengthOfNationalNumber <= 0) {
            return this.prefixBeforeNationalNumber.toString();
        }
        String tempNationalNumber = "";
        for (int i = 0; i < lengthOfNationalNumber; i++) {
            tempNationalNumber = inputDigitHelper(this.nationalNumber.charAt(i));
        }
        return this.ableToFormat ? appendNationalNumber(tempNationalNumber) : this.accruedInput.toString();
    }

    private boolean isNanpaNumberWithNationalPrefix() {
        if (this.currentMetadata.getCountryCode() == 1 && this.nationalNumber.charAt(0) == '1' && this.nationalNumber.charAt(1) != '0') {
            return this.nationalNumber.charAt(1) != '1';
        } else {
            return false;
        }
    }

    private String removeNationalPrefixFromNationalNumber() {
        int startOfNationalNumber = 0;
        if (isNanpaNumberWithNationalPrefix()) {
            startOfNationalNumber = 1;
            this.prefixBeforeNationalNumber.append('1').append(SEPARATOR_BEFORE_NATIONAL_NUMBER);
            this.isCompleteNumber = true;
        } else if (this.currentMetadata.hasNationalPrefixForParsing()) {
            Matcher m = this.regexCache.getPatternForRegex(this.currentMetadata.getNationalPrefixForParsing()).matcher(this.nationalNumber);
            if (m.lookingAt() && m.end() > 0) {
                this.isCompleteNumber = true;
                startOfNationalNumber = m.end();
                this.prefixBeforeNationalNumber.append(this.nationalNumber.substring(0, startOfNationalNumber));
            }
        }
        String nationalPrefix = this.nationalNumber.substring(0, startOfNationalNumber);
        this.nationalNumber.delete(0, startOfNationalNumber);
        return nationalPrefix;
    }

    private boolean attemptToExtractIdd() {
        Matcher iddMatcher = this.regexCache.getPatternForRegex("\\+|" + this.currentMetadata.getInternationalPrefix()).matcher(this.accruedInputWithoutFormatting);
        if (!iddMatcher.lookingAt()) {
            return false;
        }
        this.isCompleteNumber = true;
        int startOfCountryCallingCode = iddMatcher.end();
        this.nationalNumber.setLength(0);
        this.nationalNumber.append(this.accruedInputWithoutFormatting.substring(startOfCountryCallingCode));
        this.prefixBeforeNationalNumber.setLength(0);
        this.prefixBeforeNationalNumber.append(this.accruedInputWithoutFormatting.substring(0, startOfCountryCallingCode));
        if (this.accruedInputWithoutFormatting.charAt(0) != '+') {
            this.prefixBeforeNationalNumber.append(SEPARATOR_BEFORE_NATIONAL_NUMBER);
        }
        return true;
    }

    private boolean attemptToExtractCountryCallingCode() {
        if (this.nationalNumber.length() == 0) {
            return false;
        }
        StringBuilder numberWithoutCountryCallingCode = new StringBuilder();
        int countryCode = this.phoneUtil.extractCountryCode(this.nationalNumber, numberWithoutCountryCallingCode);
        if (countryCode == 0) {
            return false;
        }
        this.nationalNumber.setLength(0);
        this.nationalNumber.append(numberWithoutCountryCallingCode);
        String newRegionCode = this.phoneUtil.getRegionCodeForCountryCode(countryCode);
        if (PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY.equals(newRegionCode)) {
            this.currentMetadata = this.phoneUtil.getMetadataForNonGeographicalRegion(countryCode);
        } else if (!newRegionCode.equals(this.defaultCountry)) {
            this.currentMetadata = getMetadataForRegion(newRegionCode);
        }
        this.prefixBeforeNationalNumber.append(Integer.toString(countryCode)).append(SEPARATOR_BEFORE_NATIONAL_NUMBER);
        this.extractedNationalPrefix = "";
        return true;
    }

    private char normalizeAndAccrueDigitsAndPlusSign(char nextChar, boolean rememberPosition) {
        char normalizedChar;
        if (nextChar == '+') {
            normalizedChar = nextChar;
            this.accruedInputWithoutFormatting.append(nextChar);
        } else {
            normalizedChar = Character.forDigit(Character.digit(nextChar, 10), 10);
            this.accruedInputWithoutFormatting.append(normalizedChar);
            this.nationalNumber.append(normalizedChar);
        }
        if (rememberPosition) {
            this.positionToRemember = this.accruedInputWithoutFormatting.length();
        }
        return normalizedChar;
    }

    private String inputDigitHelper(char nextChar) {
        Matcher digitMatcher = DIGIT_PATTERN.matcher(this.formattingTemplate);
        if (digitMatcher.find(this.lastMatchPosition)) {
            String tempTemplate = digitMatcher.replaceFirst(Character.toString(nextChar));
            this.formattingTemplate.replace(0, tempTemplate.length(), tempTemplate);
            this.lastMatchPosition = digitMatcher.start();
            return this.formattingTemplate.substring(0, this.lastMatchPosition + 1);
        }
        if (this.possibleFormats.size() == 1) {
            this.ableToFormat = false;
        }
        this.currentFormattingPattern = "";
        return this.accruedInput.toString();
    }
}
