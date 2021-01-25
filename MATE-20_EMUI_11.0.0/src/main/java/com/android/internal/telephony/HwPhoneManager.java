package com.android.internal.telephony;

import android.content.Context;
import android.os.Message;
import com.huawei.internal.telephony.MmiCodeExt;
import com.huawei.internal.telephony.PhoneConstantsExt;
import com.huawei.internal.telephony.PhoneExt;
import java.util.Locale;
import java.util.regex.Pattern;

public interface HwPhoneManager {
    public static final String C2 = "F79345FB999D4D9B8A1E1FDA9BFBAAF8";
    public static final int INVALID_COUNTRY_CODE = -1;
    public static final int MATCH_GROUP_DIALING_NUMBER = 16;
    public static final int MATCH_GROUP_PWD_CONFIRM = 15;
    public static final int MATCH_GROUP_SIA = 6;
    public static final int MATCH_GROUP_SIB = 9;
    public static final int MATCH_GROUP_SIC = 12;
    public static final Pattern sPatternSuppService = Pattern.compile("((\\*|#|\\*#|\\*\\*|##)(\\d{2,3})((\\*#|\\*|#)([^*#]*)((\\*|#)([^*#]*)((\\*|#)([^*#]*)((\\*|#)([^*#]*))?)?)?)?#)(.*)");

    public interface PhoneServiceInterface {
        void setPhone(PhoneExt[] phoneExtArr, Context context);
    }

    default boolean changeMMItoUSSD(PhoneExt phone, String poundString) {
        return false;
    }

    default void loadHuaweiPhoneService(PhoneExt[] phones, Context context) {
    }

    default String custTimeZoneForMcc(int mcc) {
        return null;
    }

    default String custCountryCodeForMcc(int mcc) {
        return null;
    }

    default String custLanguageForMcc(int mcc) {
        return null;
    }

    default int custSmallestDigitsMccForMnc(int mcc) {
        return -1;
    }

    default Locale getSpecialLoacleConfig(Context context, int mcc) {
        return null;
    }

    default Locale getBetterMatchLocale(Context context, String language, String script, String country, Locale bestMatch) {
        return null;
    }

    default String custScriptForMcc(int mcc) {
        return null;
    }

    default void setMccTableImsi(String imsi) {
    }

    default void setMccTableIccId(String iccid) {
    }

    default void setMccTableMnc(int mnc) {
    }

    default void setGsmCdmaPhone(PhoneExt phoneExt, Context context) {
    }

    default void setRoamingBrokerIccId(String iccId) {
    }

    default void setRoamingBrokerOperator(String plmn) {
    }

    default boolean isRoamingBrokerActivated() {
        return false;
    }

    default String getRoamingBrokerVoicemail() {
        return null;
    }

    default String getRoamingBrokerOperatorNumeric() {
        return null;
    }

    default boolean isDefaultTimezone() {
        return false;
    }

    default void setDefaultTimezone(Context context) {
    }

    default void changedDefaultTimezone() {
    }

    default boolean isShortCodeCustomization() {
        return false;
    }

    default String convertUssdMessage(String ussdMessage) {
        return ussdMessage;
    }

    default int siToServiceClass(String si) {
        return 0;
    }

    default boolean isStringHuaweiCustCode(String dialString) {
        return false;
    }

    default boolean isStringHuaweiIgnoreCode(PhoneExt phone, String dialString) {
        return false;
    }

    default boolean shouldSkipUpdateMccMnc(String mccmnc) {
        return false;
    }

    default void setRoamingBrokerIccId(String iccId, int simId) {
    }

    default void setRoamingBrokerImsi(String imsi, Integer simId) {
    }

    default String getRoamingBrokerImsi() {
        return null;
    }

    default void setRoamingBrokerImsi(String imsi) {
    }

    default String getRoamingBrokerImsi(Integer simId) {
        return null;
    }

    default boolean isRoamingBrokerActivated(Integer simId) {
        return false;
    }

    default String getRoamingBrokerOperatorNumeric(Integer simId) {
        return null;
    }

    default String updateSelectionForRoamingBroker(String selection, int slotId) {
        return null;
    }

    default String updateSelectionForRoamingBroker(String selection) {
        return null;
    }

    default void setRoamingBrokerOperator(String plmn, int simId) {
    }

    default String getRoamingBrokerVoicemail(int simId) {
        return null;
    }

    default CharSequence processgoodPinString(Context context, String sc) {
        return sc;
    }

    default CharSequence processBadPinString(Context context, String sc) {
        return sc;
    }

    default int handlePasswordError(String sc) {
        return 17040608;
    }

    default int showMmiError(int sc, int slotid) {
        return sc;
    }

    default boolean processImsPhoneMmiCode(MmiCodeExt mmiCodeExt, PhoneExt imsPhone) {
        return false;
    }

    default void handleMessageGsmMmiCode(MmiCodeExt mmiCodeExt, Message msg) {
    }

    default boolean isSupportOrangeApn(PhoneExt phone) {
        return false;
    }

    default void addSpecialAPN(PhoneExt phone) {
    }

    default CharSequence getCallForwardingString(Context context, String sc) {
        return PhoneConfigurationManager.SSSS;
    }

    default boolean processSendUssdInImsCall(MmiCodeExt mmiCodeExt, PhoneExt imsPhone) {
        return false;
    }

    default boolean shouldRunUtIgnoreCSService(PhoneExt phone, boolean isUt) {
        return false;
    }

    default boolean needUnEscapeHtmlforUssdMsg(PhoneExt phone) {
        return false;
    }

    default String unEscapeHtml4(String str) {
        return str;
    }

    default void encryptInfo(Context context, String sensitiveInfo, String encryptTag) {
    }

    default String decryptInfo(Context context, String encryptTag) {
        return null;
    }

    default void checkMMICode(String mmiCode, int phoneId) {
    }

    default boolean checkApnShouldNotify(int subId, String apnType, PhoneConstantsExt.DataStateEx state) {
        return true;
    }
}
