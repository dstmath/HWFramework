package com.android.i18n.phonenumbers;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

public final class Phonemetadata {

    public static class NumberFormat implements Externalizable {
        private static final long serialVersionUID = 1;
        private String domesticCarrierCodeFormattingRule_;
        private String format_;
        private boolean hasDomesticCarrierCodeFormattingRule;
        private boolean hasFormat;
        private boolean hasNationalPrefixFormattingRule;
        private boolean hasNationalPrefixOptionalWhenFormatting;
        private boolean hasPattern;
        private List<String> leadingDigitsPattern_;
        private String nationalPrefixFormattingRule_;
        private boolean nationalPrefixOptionalWhenFormatting_;
        private String pattern_;

        public static final class Builder extends NumberFormat {
            public NumberFormat build() {
                return this;
            }
        }

        public NumberFormat() {
            this.pattern_ = "";
            this.format_ = "";
            this.leadingDigitsPattern_ = new ArrayList();
            this.nationalPrefixFormattingRule_ = "";
            this.nationalPrefixOptionalWhenFormatting_ = false;
            this.domesticCarrierCodeFormattingRule_ = "";
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public boolean hasPattern() {
            return this.hasPattern;
        }

        public String getPattern() {
            return this.pattern_;
        }

        public NumberFormat setPattern(String value) {
            this.hasPattern = true;
            this.pattern_ = value;
            return this;
        }

        public boolean hasFormat() {
            return this.hasFormat;
        }

        public String getFormat() {
            return this.format_;
        }

        public NumberFormat setFormat(String value) {
            this.hasFormat = true;
            this.format_ = value;
            return this;
        }

        public List<String> leadingDigitPatterns() {
            return this.leadingDigitsPattern_;
        }

        public int leadingDigitsPatternSize() {
            return this.leadingDigitsPattern_.size();
        }

        public String getLeadingDigitsPattern(int index) {
            return (String) this.leadingDigitsPattern_.get(index);
        }

        public NumberFormat addLeadingDigitsPattern(String value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.leadingDigitsPattern_.add(value);
            return this;
        }

        public boolean hasNationalPrefixFormattingRule() {
            return this.hasNationalPrefixFormattingRule;
        }

        public String getNationalPrefixFormattingRule() {
            return this.nationalPrefixFormattingRule_;
        }

        public NumberFormat setNationalPrefixFormattingRule(String value) {
            this.hasNationalPrefixFormattingRule = true;
            this.nationalPrefixFormattingRule_ = value;
            return this;
        }

        public NumberFormat clearNationalPrefixFormattingRule() {
            this.hasNationalPrefixFormattingRule = false;
            this.nationalPrefixFormattingRule_ = "";
            return this;
        }

        public boolean hasNationalPrefixOptionalWhenFormatting() {
            return this.hasNationalPrefixOptionalWhenFormatting;
        }

        public boolean isNationalPrefixOptionalWhenFormatting() {
            return this.nationalPrefixOptionalWhenFormatting_;
        }

        public NumberFormat setNationalPrefixOptionalWhenFormatting(boolean value) {
            this.hasNationalPrefixOptionalWhenFormatting = true;
            this.nationalPrefixOptionalWhenFormatting_ = value;
            return this;
        }

        public boolean hasDomesticCarrierCodeFormattingRule() {
            return this.hasDomesticCarrierCodeFormattingRule;
        }

        public String getDomesticCarrierCodeFormattingRule() {
            return this.domesticCarrierCodeFormattingRule_;
        }

        public NumberFormat setDomesticCarrierCodeFormattingRule(String value) {
            this.hasDomesticCarrierCodeFormattingRule = true;
            this.domesticCarrierCodeFormattingRule_ = value;
            return this;
        }

        public NumberFormat mergeFrom(NumberFormat other) {
            if (other.hasPattern()) {
                setPattern(other.getPattern());
            }
            if (other.hasFormat()) {
                setFormat(other.getFormat());
            }
            int leadingDigitsPatternSize = other.leadingDigitsPatternSize();
            for (int i = 0; i < leadingDigitsPatternSize; i++) {
                addLeadingDigitsPattern(other.getLeadingDigitsPattern(i));
            }
            if (other.hasNationalPrefixFormattingRule()) {
                setNationalPrefixFormattingRule(other.getNationalPrefixFormattingRule());
            }
            if (other.hasDomesticCarrierCodeFormattingRule()) {
                setDomesticCarrierCodeFormattingRule(other.getDomesticCarrierCodeFormattingRule());
            }
            setNationalPrefixOptionalWhenFormatting(other.isNationalPrefixOptionalWhenFormatting());
            return this;
        }

        public void writeExternal(ObjectOutput objectOutput) throws IOException {
            objectOutput.writeUTF(this.pattern_);
            objectOutput.writeUTF(this.format_);
            int leadingDigitsPatternSize = leadingDigitsPatternSize();
            objectOutput.writeInt(leadingDigitsPatternSize);
            for (int i = 0; i < leadingDigitsPatternSize; i++) {
                objectOutput.writeUTF((String) this.leadingDigitsPattern_.get(i));
            }
            objectOutput.writeBoolean(this.hasNationalPrefixFormattingRule);
            if (this.hasNationalPrefixFormattingRule) {
                objectOutput.writeUTF(this.nationalPrefixFormattingRule_);
            }
            objectOutput.writeBoolean(this.hasDomesticCarrierCodeFormattingRule);
            if (this.hasDomesticCarrierCodeFormattingRule) {
                objectOutput.writeUTF(this.domesticCarrierCodeFormattingRule_);
            }
            objectOutput.writeBoolean(this.nationalPrefixOptionalWhenFormatting_);
        }

        public void readExternal(ObjectInput objectInput) throws IOException {
            setPattern(objectInput.readUTF());
            setFormat(objectInput.readUTF());
            int leadingDigitsPatternSize = objectInput.readInt();
            for (int i = 0; i < leadingDigitsPatternSize; i++) {
                this.leadingDigitsPattern_.add(objectInput.readUTF());
            }
            if (objectInput.readBoolean()) {
                setNationalPrefixFormattingRule(objectInput.readUTF());
            }
            if (objectInput.readBoolean()) {
                setDomesticCarrierCodeFormattingRule(objectInput.readUTF());
            }
            setNationalPrefixOptionalWhenFormatting(objectInput.readBoolean());
        }
    }

    public static class PhoneMetadata implements Externalizable {
        private static final long serialVersionUID = 1;
        private PhoneNumberDesc carrierSpecific_;
        private int countryCode_;
        private PhoneNumberDesc emergency_;
        private PhoneNumberDesc fixedLine_;
        private PhoneNumberDesc generalDesc_;
        private boolean hasCarrierSpecific;
        private boolean hasCountryCode;
        private boolean hasEmergency;
        private boolean hasFixedLine;
        private boolean hasGeneralDesc;
        private boolean hasId;
        private boolean hasInternationalPrefix;
        private boolean hasLeadingDigits;
        private boolean hasLeadingZeroPossible;
        private boolean hasMainCountryForCode;
        private boolean hasMobile;
        private boolean hasMobileNumberPortableRegion;
        private boolean hasNationalPrefix;
        private boolean hasNationalPrefixForParsing;
        private boolean hasNationalPrefixTransformRule;
        private boolean hasNoInternationalDialling;
        private boolean hasPager;
        private boolean hasPersonalNumber;
        private boolean hasPreferredExtnPrefix;
        private boolean hasPreferredInternationalPrefix;
        private boolean hasPremiumRate;
        private boolean hasSameMobileAndFixedLinePattern;
        private boolean hasSharedCost;
        private boolean hasShortCode;
        private boolean hasStandardRate;
        private boolean hasTollFree;
        private boolean hasUan;
        private boolean hasVoicemail;
        private boolean hasVoip;
        private String id_;
        private String internationalPrefix_;
        private List<NumberFormat> intlNumberFormat_;
        private String leadingDigits_;
        private boolean leadingZeroPossible_;
        private boolean mainCountryForCode_;
        private boolean mobileNumberPortableRegion_;
        private PhoneNumberDesc mobile_;
        private String nationalPrefixForParsing_;
        private String nationalPrefixTransformRule_;
        private String nationalPrefix_;
        private PhoneNumberDesc noInternationalDialling_;
        private List<NumberFormat> numberFormat_;
        private PhoneNumberDesc pager_;
        private PhoneNumberDesc personalNumber_;
        private String preferredExtnPrefix_;
        private String preferredInternationalPrefix_;
        private PhoneNumberDesc premiumRate_;
        private boolean sameMobileAndFixedLinePattern_;
        private PhoneNumberDesc sharedCost_;
        private PhoneNumberDesc shortCode_;
        private PhoneNumberDesc standardRate_;
        private PhoneNumberDesc tollFree_;
        private PhoneNumberDesc uan_;
        private PhoneNumberDesc voicemail_;
        private PhoneNumberDesc voip_;

        public static final class Builder extends PhoneMetadata {
            public PhoneMetadata build() {
                return this;
            }
        }

        public PhoneMetadata() {
            this.generalDesc_ = null;
            this.fixedLine_ = null;
            this.mobile_ = null;
            this.tollFree_ = null;
            this.premiumRate_ = null;
            this.sharedCost_ = null;
            this.personalNumber_ = null;
            this.voip_ = null;
            this.pager_ = null;
            this.uan_ = null;
            this.emergency_ = null;
            this.voicemail_ = null;
            this.shortCode_ = null;
            this.standardRate_ = null;
            this.carrierSpecific_ = null;
            this.noInternationalDialling_ = null;
            this.id_ = "";
            this.countryCode_ = 0;
            this.internationalPrefix_ = "";
            this.preferredInternationalPrefix_ = "";
            this.nationalPrefix_ = "";
            this.preferredExtnPrefix_ = "";
            this.nationalPrefixForParsing_ = "";
            this.nationalPrefixTransformRule_ = "";
            this.sameMobileAndFixedLinePattern_ = false;
            this.numberFormat_ = new ArrayList();
            this.intlNumberFormat_ = new ArrayList();
            this.mainCountryForCode_ = false;
            this.leadingDigits_ = "";
            this.leadingZeroPossible_ = false;
            this.mobileNumberPortableRegion_ = false;
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public boolean hasGeneralDesc() {
            return this.hasGeneralDesc;
        }

        public PhoneNumberDesc getGeneralDesc() {
            return this.generalDesc_;
        }

        public PhoneMetadata setGeneralDesc(PhoneNumberDesc value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.hasGeneralDesc = true;
            this.generalDesc_ = value;
            return this;
        }

        public boolean hasFixedLine() {
            return this.hasFixedLine;
        }

        public PhoneNumberDesc getFixedLine() {
            return this.fixedLine_;
        }

        public PhoneMetadata setFixedLine(PhoneNumberDesc value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.hasFixedLine = true;
            this.fixedLine_ = value;
            return this;
        }

        public boolean hasMobile() {
            return this.hasMobile;
        }

        public PhoneNumberDesc getMobile() {
            return this.mobile_;
        }

        public PhoneMetadata setMobile(PhoneNumberDesc value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.hasMobile = true;
            this.mobile_ = value;
            return this;
        }

        public boolean hasTollFree() {
            return this.hasTollFree;
        }

        public PhoneNumberDesc getTollFree() {
            return this.tollFree_;
        }

        public PhoneMetadata setTollFree(PhoneNumberDesc value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.hasTollFree = true;
            this.tollFree_ = value;
            return this;
        }

        public boolean hasPremiumRate() {
            return this.hasPremiumRate;
        }

        public PhoneNumberDesc getPremiumRate() {
            return this.premiumRate_;
        }

        public PhoneMetadata setPremiumRate(PhoneNumberDesc value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.hasPremiumRate = true;
            this.premiumRate_ = value;
            return this;
        }

        public boolean hasSharedCost() {
            return this.hasSharedCost;
        }

        public PhoneNumberDesc getSharedCost() {
            return this.sharedCost_;
        }

        public PhoneMetadata setSharedCost(PhoneNumberDesc value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.hasSharedCost = true;
            this.sharedCost_ = value;
            return this;
        }

        public boolean hasPersonalNumber() {
            return this.hasPersonalNumber;
        }

        public PhoneNumberDesc getPersonalNumber() {
            return this.personalNumber_;
        }

        public PhoneMetadata setPersonalNumber(PhoneNumberDesc value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.hasPersonalNumber = true;
            this.personalNumber_ = value;
            return this;
        }

        public boolean hasVoip() {
            return this.hasVoip;
        }

        public PhoneNumberDesc getVoip() {
            return this.voip_;
        }

        public PhoneMetadata setVoip(PhoneNumberDesc value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.hasVoip = true;
            this.voip_ = value;
            return this;
        }

        public boolean hasPager() {
            return this.hasPager;
        }

        public PhoneNumberDesc getPager() {
            return this.pager_;
        }

        public PhoneMetadata setPager(PhoneNumberDesc value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.hasPager = true;
            this.pager_ = value;
            return this;
        }

        public boolean hasUan() {
            return this.hasUan;
        }

        public PhoneNumberDesc getUan() {
            return this.uan_;
        }

        public PhoneMetadata setUan(PhoneNumberDesc value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.hasUan = true;
            this.uan_ = value;
            return this;
        }

        public boolean hasEmergency() {
            return this.hasEmergency;
        }

        public PhoneNumberDesc getEmergency() {
            return this.emergency_;
        }

        public PhoneMetadata setEmergency(PhoneNumberDesc value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.hasEmergency = true;
            this.emergency_ = value;
            return this;
        }

        public boolean hasVoicemail() {
            return this.hasVoicemail;
        }

        public PhoneNumberDesc getVoicemail() {
            return this.voicemail_;
        }

        public PhoneMetadata setVoicemail(PhoneNumberDesc value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.hasVoicemail = true;
            this.voicemail_ = value;
            return this;
        }

        public boolean hasShortCode() {
            return this.hasShortCode;
        }

        public PhoneNumberDesc getShortCode() {
            return this.shortCode_;
        }

        public PhoneMetadata setShortCode(PhoneNumberDesc value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.hasShortCode = true;
            this.shortCode_ = value;
            return this;
        }

        public boolean hasStandardRate() {
            return this.hasStandardRate;
        }

        public PhoneNumberDesc getStandardRate() {
            return this.standardRate_;
        }

        public PhoneMetadata setStandardRate(PhoneNumberDesc value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.hasStandardRate = true;
            this.standardRate_ = value;
            return this;
        }

        public boolean hasCarrierSpecific() {
            return this.hasCarrierSpecific;
        }

        public PhoneNumberDesc getCarrierSpecific() {
            return this.carrierSpecific_;
        }

        public PhoneMetadata setCarrierSpecific(PhoneNumberDesc value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.hasCarrierSpecific = true;
            this.carrierSpecific_ = value;
            return this;
        }

        public boolean hasNoInternationalDialling() {
            return this.hasNoInternationalDialling;
        }

        public PhoneNumberDesc getNoInternationalDialling() {
            return this.noInternationalDialling_;
        }

        public PhoneMetadata setNoInternationalDialling(PhoneNumberDesc value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.hasNoInternationalDialling = true;
            this.noInternationalDialling_ = value;
            return this;
        }

        public boolean hasId() {
            return this.hasId;
        }

        public String getId() {
            return this.id_;
        }

        public PhoneMetadata setId(String value) {
            this.hasId = true;
            this.id_ = value;
            return this;
        }

        public boolean hasCountryCode() {
            return this.hasCountryCode;
        }

        public int getCountryCode() {
            return this.countryCode_;
        }

        public PhoneMetadata setCountryCode(int value) {
            this.hasCountryCode = true;
            this.countryCode_ = value;
            return this;
        }

        public boolean hasInternationalPrefix() {
            return this.hasInternationalPrefix;
        }

        public String getInternationalPrefix() {
            return this.internationalPrefix_;
        }

        public PhoneMetadata setInternationalPrefix(String value) {
            this.hasInternationalPrefix = true;
            this.internationalPrefix_ = value;
            return this;
        }

        public boolean hasPreferredInternationalPrefix() {
            return this.hasPreferredInternationalPrefix;
        }

        public String getPreferredInternationalPrefix() {
            return this.preferredInternationalPrefix_;
        }

        public PhoneMetadata setPreferredInternationalPrefix(String value) {
            this.hasPreferredInternationalPrefix = true;
            this.preferredInternationalPrefix_ = value;
            return this;
        }

        public boolean hasNationalPrefix() {
            return this.hasNationalPrefix;
        }

        public String getNationalPrefix() {
            return this.nationalPrefix_;
        }

        public PhoneMetadata setNationalPrefix(String value) {
            this.hasNationalPrefix = true;
            this.nationalPrefix_ = value;
            return this;
        }

        public boolean hasPreferredExtnPrefix() {
            return this.hasPreferredExtnPrefix;
        }

        public String getPreferredExtnPrefix() {
            return this.preferredExtnPrefix_;
        }

        public PhoneMetadata setPreferredExtnPrefix(String value) {
            this.hasPreferredExtnPrefix = true;
            this.preferredExtnPrefix_ = value;
            return this;
        }

        public boolean hasNationalPrefixForParsing() {
            return this.hasNationalPrefixForParsing;
        }

        public String getNationalPrefixForParsing() {
            return this.nationalPrefixForParsing_;
        }

        public PhoneMetadata setNationalPrefixForParsing(String value) {
            this.hasNationalPrefixForParsing = true;
            this.nationalPrefixForParsing_ = value;
            return this;
        }

        public boolean hasNationalPrefixTransformRule() {
            return this.hasNationalPrefixTransformRule;
        }

        public String getNationalPrefixTransformRule() {
            return this.nationalPrefixTransformRule_;
        }

        public PhoneMetadata setNationalPrefixTransformRule(String value) {
            this.hasNationalPrefixTransformRule = true;
            this.nationalPrefixTransformRule_ = value;
            return this;
        }

        public boolean hasSameMobileAndFixedLinePattern() {
            return this.hasSameMobileAndFixedLinePattern;
        }

        public boolean isSameMobileAndFixedLinePattern() {
            return this.sameMobileAndFixedLinePattern_;
        }

        public PhoneMetadata setSameMobileAndFixedLinePattern(boolean value) {
            this.hasSameMobileAndFixedLinePattern = true;
            this.sameMobileAndFixedLinePattern_ = value;
            return this;
        }

        public List<NumberFormat> numberFormats() {
            return this.numberFormat_;
        }

        public int numberFormatSize() {
            return this.numberFormat_.size();
        }

        public NumberFormat getNumberFormat(int index) {
            return (NumberFormat) this.numberFormat_.get(index);
        }

        public PhoneMetadata addNumberFormat(NumberFormat value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.numberFormat_.add(value);
            return this;
        }

        public List<NumberFormat> intlNumberFormats() {
            return this.intlNumberFormat_;
        }

        public int intlNumberFormatSize() {
            return this.intlNumberFormat_.size();
        }

        public NumberFormat getIntlNumberFormat(int index) {
            return (NumberFormat) this.intlNumberFormat_.get(index);
        }

        public PhoneMetadata addIntlNumberFormat(NumberFormat value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.intlNumberFormat_.add(value);
            return this;
        }

        public PhoneMetadata clearIntlNumberFormat() {
            this.intlNumberFormat_.clear();
            return this;
        }

        public boolean hasMainCountryForCode() {
            return this.hasMainCountryForCode;
        }

        public boolean isMainCountryForCode() {
            return this.mainCountryForCode_;
        }

        public boolean getMainCountryForCode() {
            return this.mainCountryForCode_;
        }

        public PhoneMetadata setMainCountryForCode(boolean value) {
            this.hasMainCountryForCode = true;
            this.mainCountryForCode_ = value;
            return this;
        }

        public boolean hasLeadingDigits() {
            return this.hasLeadingDigits;
        }

        public String getLeadingDigits() {
            return this.leadingDigits_;
        }

        public PhoneMetadata setLeadingDigits(String value) {
            this.hasLeadingDigits = true;
            this.leadingDigits_ = value;
            return this;
        }

        public boolean hasLeadingZeroPossible() {
            return this.hasLeadingZeroPossible;
        }

        public boolean isLeadingZeroPossible() {
            return this.leadingZeroPossible_;
        }

        public PhoneMetadata setLeadingZeroPossible(boolean value) {
            this.hasLeadingZeroPossible = true;
            this.leadingZeroPossible_ = value;
            return this;
        }

        public boolean hasMobileNumberPortableRegion() {
            return this.hasMobileNumberPortableRegion;
        }

        public boolean isMobileNumberPortableRegion() {
            return this.mobileNumberPortableRegion_;
        }

        public PhoneMetadata setMobileNumberPortableRegion(boolean value) {
            this.hasMobileNumberPortableRegion = true;
            this.mobileNumberPortableRegion_ = value;
            return this;
        }

        public void writeExternal(ObjectOutput objectOutput) throws IOException {
            int i;
            objectOutput.writeBoolean(this.hasGeneralDesc);
            if (this.hasGeneralDesc) {
                this.generalDesc_.writeExternal(objectOutput);
            }
            objectOutput.writeBoolean(this.hasFixedLine);
            if (this.hasFixedLine) {
                this.fixedLine_.writeExternal(objectOutput);
            }
            objectOutput.writeBoolean(this.hasMobile);
            if (this.hasMobile) {
                this.mobile_.writeExternal(objectOutput);
            }
            objectOutput.writeBoolean(this.hasTollFree);
            if (this.hasTollFree) {
                this.tollFree_.writeExternal(objectOutput);
            }
            objectOutput.writeBoolean(this.hasPremiumRate);
            if (this.hasPremiumRate) {
                this.premiumRate_.writeExternal(objectOutput);
            }
            objectOutput.writeBoolean(this.hasSharedCost);
            if (this.hasSharedCost) {
                this.sharedCost_.writeExternal(objectOutput);
            }
            objectOutput.writeBoolean(this.hasPersonalNumber);
            if (this.hasPersonalNumber) {
                this.personalNumber_.writeExternal(objectOutput);
            }
            objectOutput.writeBoolean(this.hasVoip);
            if (this.hasVoip) {
                this.voip_.writeExternal(objectOutput);
            }
            objectOutput.writeBoolean(this.hasPager);
            if (this.hasPager) {
                this.pager_.writeExternal(objectOutput);
            }
            objectOutput.writeBoolean(this.hasUan);
            if (this.hasUan) {
                this.uan_.writeExternal(objectOutput);
            }
            objectOutput.writeBoolean(this.hasEmergency);
            if (this.hasEmergency) {
                this.emergency_.writeExternal(objectOutput);
            }
            objectOutput.writeBoolean(this.hasVoicemail);
            if (this.hasVoicemail) {
                this.voicemail_.writeExternal(objectOutput);
            }
            objectOutput.writeBoolean(this.hasShortCode);
            if (this.hasShortCode) {
                this.shortCode_.writeExternal(objectOutput);
            }
            objectOutput.writeBoolean(this.hasStandardRate);
            if (this.hasStandardRate) {
                this.standardRate_.writeExternal(objectOutput);
            }
            objectOutput.writeBoolean(this.hasCarrierSpecific);
            if (this.hasCarrierSpecific) {
                this.carrierSpecific_.writeExternal(objectOutput);
            }
            objectOutput.writeBoolean(this.hasNoInternationalDialling);
            if (this.hasNoInternationalDialling) {
                this.noInternationalDialling_.writeExternal(objectOutput);
            }
            objectOutput.writeUTF(this.id_);
            objectOutput.writeInt(this.countryCode_);
            objectOutput.writeUTF(this.internationalPrefix_);
            objectOutput.writeBoolean(this.hasPreferredInternationalPrefix);
            if (this.hasPreferredInternationalPrefix) {
                objectOutput.writeUTF(this.preferredInternationalPrefix_);
            }
            objectOutput.writeBoolean(this.hasNationalPrefix);
            if (this.hasNationalPrefix) {
                objectOutput.writeUTF(this.nationalPrefix_);
            }
            objectOutput.writeBoolean(this.hasPreferredExtnPrefix);
            if (this.hasPreferredExtnPrefix) {
                objectOutput.writeUTF(this.preferredExtnPrefix_);
            }
            objectOutput.writeBoolean(this.hasNationalPrefixForParsing);
            if (this.hasNationalPrefixForParsing) {
                objectOutput.writeUTF(this.nationalPrefixForParsing_);
            }
            objectOutput.writeBoolean(this.hasNationalPrefixTransformRule);
            if (this.hasNationalPrefixTransformRule) {
                objectOutput.writeUTF(this.nationalPrefixTransformRule_);
            }
            objectOutput.writeBoolean(this.sameMobileAndFixedLinePattern_);
            int numberFormatSize = numberFormatSize();
            objectOutput.writeInt(numberFormatSize);
            for (i = 0; i < numberFormatSize; i++) {
                ((NumberFormat) this.numberFormat_.get(i)).writeExternal(objectOutput);
            }
            int intlNumberFormatSize = intlNumberFormatSize();
            objectOutput.writeInt(intlNumberFormatSize);
            for (i = 0; i < intlNumberFormatSize; i++) {
                ((NumberFormat) this.intlNumberFormat_.get(i)).writeExternal(objectOutput);
            }
            objectOutput.writeBoolean(this.mainCountryForCode_);
            objectOutput.writeBoolean(this.hasLeadingDigits);
            if (this.hasLeadingDigits) {
                objectOutput.writeUTF(this.leadingDigits_);
            }
            objectOutput.writeBoolean(this.leadingZeroPossible_);
            objectOutput.writeBoolean(this.mobileNumberPortableRegion_);
        }

        public void readExternal(ObjectInput objectInput) throws IOException {
            int i;
            if (objectInput.readBoolean()) {
                PhoneNumberDesc desc = new PhoneNumberDesc();
                desc.readExternal(objectInput);
                setGeneralDesc(desc);
            }
            if (objectInput.readBoolean()) {
                desc = new PhoneNumberDesc();
                desc.readExternal(objectInput);
                setFixedLine(desc);
            }
            if (objectInput.readBoolean()) {
                desc = new PhoneNumberDesc();
                desc.readExternal(objectInput);
                setMobile(desc);
            }
            if (objectInput.readBoolean()) {
                desc = new PhoneNumberDesc();
                desc.readExternal(objectInput);
                setTollFree(desc);
            }
            if (objectInput.readBoolean()) {
                desc = new PhoneNumberDesc();
                desc.readExternal(objectInput);
                setPremiumRate(desc);
            }
            if (objectInput.readBoolean()) {
                desc = new PhoneNumberDesc();
                desc.readExternal(objectInput);
                setSharedCost(desc);
            }
            if (objectInput.readBoolean()) {
                desc = new PhoneNumberDesc();
                desc.readExternal(objectInput);
                setPersonalNumber(desc);
            }
            if (objectInput.readBoolean()) {
                desc = new PhoneNumberDesc();
                desc.readExternal(objectInput);
                setVoip(desc);
            }
            if (objectInput.readBoolean()) {
                desc = new PhoneNumberDesc();
                desc.readExternal(objectInput);
                setPager(desc);
            }
            if (objectInput.readBoolean()) {
                desc = new PhoneNumberDesc();
                desc.readExternal(objectInput);
                setUan(desc);
            }
            if (objectInput.readBoolean()) {
                desc = new PhoneNumberDesc();
                desc.readExternal(objectInput);
                setEmergency(desc);
            }
            if (objectInput.readBoolean()) {
                desc = new PhoneNumberDesc();
                desc.readExternal(objectInput);
                setVoicemail(desc);
            }
            if (objectInput.readBoolean()) {
                desc = new PhoneNumberDesc();
                desc.readExternal(objectInput);
                setShortCode(desc);
            }
            if (objectInput.readBoolean()) {
                desc = new PhoneNumberDesc();
                desc.readExternal(objectInput);
                setStandardRate(desc);
            }
            if (objectInput.readBoolean()) {
                desc = new PhoneNumberDesc();
                desc.readExternal(objectInput);
                setCarrierSpecific(desc);
            }
            if (objectInput.readBoolean()) {
                desc = new PhoneNumberDesc();
                desc.readExternal(objectInput);
                setNoInternationalDialling(desc);
            }
            setId(objectInput.readUTF());
            setCountryCode(objectInput.readInt());
            setInternationalPrefix(objectInput.readUTF());
            if (objectInput.readBoolean()) {
                setPreferredInternationalPrefix(objectInput.readUTF());
            }
            if (objectInput.readBoolean()) {
                setNationalPrefix(objectInput.readUTF());
            }
            if (objectInput.readBoolean()) {
                setPreferredExtnPrefix(objectInput.readUTF());
            }
            if (objectInput.readBoolean()) {
                setNationalPrefixForParsing(objectInput.readUTF());
            }
            if (objectInput.readBoolean()) {
                setNationalPrefixTransformRule(objectInput.readUTF());
            }
            setSameMobileAndFixedLinePattern(objectInput.readBoolean());
            int nationalFormatSize = objectInput.readInt();
            for (i = 0; i < nationalFormatSize; i++) {
                NumberFormat numFormat = new NumberFormat();
                numFormat.readExternal(objectInput);
                this.numberFormat_.add(numFormat);
            }
            int intlNumberFormatSize = objectInput.readInt();
            for (i = 0; i < intlNumberFormatSize; i++) {
                numFormat = new NumberFormat();
                numFormat.readExternal(objectInput);
                this.intlNumberFormat_.add(numFormat);
            }
            setMainCountryForCode(objectInput.readBoolean());
            if (objectInput.readBoolean()) {
                setLeadingDigits(objectInput.readUTF());
            }
            setLeadingZeroPossible(objectInput.readBoolean());
            setMobileNumberPortableRegion(objectInput.readBoolean());
        }
    }

    public static class PhoneMetadataCollection implements Externalizable {
        private static final long serialVersionUID = 1;
        private List<PhoneMetadata> metadata_;

        public static final class Builder extends PhoneMetadataCollection {
            public PhoneMetadataCollection build() {
                return this;
            }
        }

        public PhoneMetadataCollection() {
            this.metadata_ = new ArrayList();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public List<PhoneMetadata> getMetadataList() {
            return this.metadata_;
        }

        public int getMetadataCount() {
            return this.metadata_.size();
        }

        public PhoneMetadataCollection addMetadata(PhoneMetadata value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.metadata_.add(value);
            return this;
        }

        public void writeExternal(ObjectOutput objectOutput) throws IOException {
            int size = getMetadataCount();
            objectOutput.writeInt(size);
            for (int i = 0; i < size; i++) {
                ((PhoneMetadata) this.metadata_.get(i)).writeExternal(objectOutput);
            }
        }

        public void readExternal(ObjectInput objectInput) throws IOException {
            int size = objectInput.readInt();
            for (int i = 0; i < size; i++) {
                PhoneMetadata metadata = new PhoneMetadata();
                metadata.readExternal(objectInput);
                this.metadata_.add(metadata);
            }
        }

        public PhoneMetadataCollection clear() {
            this.metadata_.clear();
            return this;
        }
    }

    public static class PhoneNumberDesc implements Externalizable {
        private static final long serialVersionUID = 1;
        private String exampleNumber_;
        private boolean hasExampleNumber;
        private boolean hasNationalNumberPattern;
        private boolean hasPossibleNumberPattern;
        private String nationalNumberPattern_;
        private String possibleNumberPattern_;

        public static final class Builder extends PhoneNumberDesc {
            public PhoneNumberDesc build() {
                return this;
            }
        }

        public PhoneNumberDesc() {
            this.nationalNumberPattern_ = "";
            this.possibleNumberPattern_ = "";
            this.exampleNumber_ = "";
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public boolean hasNationalNumberPattern() {
            return this.hasNationalNumberPattern;
        }

        public String getNationalNumberPattern() {
            return this.nationalNumberPattern_;
        }

        public PhoneNumberDesc setNationalNumberPattern(String value) {
            this.hasNationalNumberPattern = true;
            this.nationalNumberPattern_ = value;
            return this;
        }

        public boolean hasPossibleNumberPattern() {
            return this.hasPossibleNumberPattern;
        }

        public String getPossibleNumberPattern() {
            return this.possibleNumberPattern_;
        }

        public PhoneNumberDesc setPossibleNumberPattern(String value) {
            this.hasPossibleNumberPattern = true;
            this.possibleNumberPattern_ = value;
            return this;
        }

        public boolean hasExampleNumber() {
            return this.hasExampleNumber;
        }

        public String getExampleNumber() {
            return this.exampleNumber_;
        }

        public PhoneNumberDesc setExampleNumber(String value) {
            this.hasExampleNumber = true;
            this.exampleNumber_ = value;
            return this;
        }

        public PhoneNumberDesc mergeFrom(PhoneNumberDesc other) {
            if (other.hasNationalNumberPattern()) {
                setNationalNumberPattern(other.getNationalNumberPattern());
            }
            if (other.hasPossibleNumberPattern()) {
                setPossibleNumberPattern(other.getPossibleNumberPattern());
            }
            if (other.hasExampleNumber()) {
                setExampleNumber(other.getExampleNumber());
            }
            return this;
        }

        public boolean exactlySameAs(PhoneNumberDesc other) {
            if (this.nationalNumberPattern_.equals(other.nationalNumberPattern_) && this.possibleNumberPattern_.equals(other.possibleNumberPattern_)) {
                return this.exampleNumber_.equals(other.exampleNumber_);
            }
            return false;
        }

        public void writeExternal(ObjectOutput objectOutput) throws IOException {
            objectOutput.writeBoolean(this.hasNationalNumberPattern);
            if (this.hasNationalNumberPattern) {
                objectOutput.writeUTF(this.nationalNumberPattern_);
            }
            objectOutput.writeBoolean(this.hasPossibleNumberPattern);
            if (this.hasPossibleNumberPattern) {
                objectOutput.writeUTF(this.possibleNumberPattern_);
            }
            objectOutput.writeBoolean(this.hasExampleNumber);
            if (this.hasExampleNumber) {
                objectOutput.writeUTF(this.exampleNumber_);
            }
        }

        public void readExternal(ObjectInput objectInput) throws IOException {
            if (objectInput.readBoolean()) {
                setNationalNumberPattern(objectInput.readUTF());
            }
            if (objectInput.readBoolean()) {
                setPossibleNumberPattern(objectInput.readUTF());
            }
            if (objectInput.readBoolean()) {
                setExampleNumber(objectInput.readUTF());
            }
        }
    }

    private Phonemetadata() {
    }
}
