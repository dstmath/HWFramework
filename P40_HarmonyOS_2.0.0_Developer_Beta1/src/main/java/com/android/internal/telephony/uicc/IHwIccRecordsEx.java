package com.android.internal.telephony.uicc;

import android.content.ContentResolver;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.huawei.internal.telephony.OperatorInfoEx;
import com.huawei.internal.telephony.uicc.UsimServiceTableEx;
import java.util.ArrayList;

public interface IHwIccRecordsEx {
    public static final int EVENT_EONS = 100;
    public static final int EVENT_GET_CDMA_GSM_IMSI_DONE = 101;
    public static final int HW_CARRIER_FILE_GID1 = 5;
    public static final int HW_CARRIER_FILE_GID2 = 6;
    public static final int HW_CARRIER_FILE_SPN = 7;
    public static final int HW_CUST_EVENT_BASE = 100;
    public static final String[] MCCMNC_CODES_HAVING_2DIGITS_MNC = {"40400", "40401", "40402", "40403", "40404", "40405", "40407", "40409", "40410", "40411", "40412", "40413", "40414", "40415", "40416", "40417", "40418", "40419", "40420", "40421", "40422", "40424", "40425", "40427", "40428", "40429", "40430", "40431", "40433", "40434", "40435", "40436", "40437", "40438", "40440", "40441", "40442", "40443", "40444", "40445", "40446", "40449", "40450", "40451", "40452", "40453", "40454", "40455", "40456", "40457", "40458", "40459", "40460", "40462", "40464", "40466", "40467", "40468", "40469", "40470", "40471", "40472", "40473", "40474", "40475", "40476", "40477", "40478", "40479", "40480", "40481", "40482", "40483", "40484", "40485", "40486", "40487", "40488", "40489", "40490", "40491", "40492", "40493", "40494", "40495", "40496", "40497", "40498", "40501", "40505", "40506", "40507", "40508", "40509", "40510", "40511", "40512", "40513", "40514", "40515", "40517", "40518", "40519", "40520", "40521", "40522", "40523", "40524", "40548", "40551", "40552", "40553", "40554", "40555", "40556", "40566", "40567", "40570", "23210"};
    public static final String[] MCCMNC_CODES_HAVING_2DIGITS_MNC_ZERO_PREFIX_RELIANCE = {"40503", "40504"};

    default void registerForCsgRecordsLoaded(Handler h, int what, Object obj) {
    }

    default void unregisterForCsgRecordsLoaded(Handler h) {
    }

    default void notifyRegisterForCsgRecordsLoaded() {
    }

    default void registerForIccRefresh(Handler h, int what, Object obj) {
    }

    default void notifyRegisterForIccRefresh() {
    }

    default void unRegisterForIccRefresh(Handler h) {
    }

    default boolean getIccidSwitch() {
        return false;
    }

    default void sendIccidDoneBroadcast(String iccId) {
    }

    default boolean beforeHandleSimRefresh(int refreshResult, int efId) {
        return false;
    }

    default boolean afterHandleSimRefresh(int refreshResult) {
        return false;
    }

    default boolean beforeHandleRuimRefresh(int refreshResult) {
        return false;
    }

    default boolean afterHandleRuimRefresh(int refreshResult) {
        return false;
    }

    default void onIccIdLoadedHw() {
    }

    default void onImsiLoadedHw() {
    }

    default String getOperatorNumericEx(ContentResolver cr, String name) {
        return null;
    }

    default boolean has3Gphonebook() {
        return false;
    }

    default boolean isGetPBRDone() {
        return false;
    }

    default void getPbrRecordSize() {
    }

    default void loadEons() {
    }

    default String getEons() {
        return null;
    }

    default boolean isEonsDisabled() {
        return true;
    }

    default boolean updateEons(String regOperator, int lac) {
        return true;
    }

    default ArrayList<OperatorInfoEx> getEonsForAvailableNetworks(ArrayList<OperatorInfoEx> arrayList) {
        return null;
    }

    default void registerForLoadIccID(Handler h, int what, Object obj) {
    }

    default void unRegisterForLoadIccID(Handler h) {
    }

    default void notifyRegisterLoadIccID(Object userObj, Object result, Throwable exception) {
    }

    default String[] getEhplmnOfSim() {
        return new String[0];
    }

    default String getActingHplmn() {
        return null;
    }

    default void setMdnNumber(String alphaTag, String number, Message onComplete) {
    }

    default boolean getImsiReady() {
        return false;
    }

    default void setImsiReady(boolean isReady) {
    }

    default byte[] getOcsgl() {
        return new byte[0];
    }

    default boolean getCsglexist() {
        return false;
    }

    default void setCsglexist(boolean isCglExist) {
    }

    default boolean isHwCustDataRoamingOpenArea() {
        return false;
    }

    default void updateCarrierFile(int slotId, int fileType, String fileValue) {
    }

    default byte[] getGID1() {
        return new byte[0];
    }

    default void onOperatorNumericLoadedHw() {
    }

    default void onAllRecordsLoadedHw() {
    }

    default void loadGID1() {
    }

    default void custMncLength(String mcc) {
    }

    default void updateSarMnc(String imsi) {
    }

    default boolean checkFileInServiceTable(int efid, UsimServiceTableEx usimServiceTable, byte[] data) {
        return false;
    }

    default void initFdnPsStatus(int slotId) {
    }

    default void loadCardSpecialFile(int fileid) {
    }

    default void refreshCardType() {
    }

    default void loadSimMatchedFileFromRilCache() {
    }

    default void loadSimMatchedFileFromRilCacheByEfid(int efId) {
    }

    default void onGetSimMatchedFileDone(Message msg) {
    }

    default String getVmSimImsi() {
        return null;
    }

    default void setVmSimImsi(String imsi) {
    }

    default void sendCspChangedBroadcast(boolean isOldCspPlmnEnabled, boolean isCspPlmnEnabled) {
    }

    default void adapterForDoubleRilChannelAfterImsiReady() {
    }

    default void updateMccMncConfigWithGplmn(String operatorNumeric) {
    }

    default String decodeCdmaImsi(byte[] data) {
        return null;
    }

    default void updateCsimImsi(byte[] data) {
    }

    default void onReady() {
    }

    default void dispose() {
    }

    default void resetRecords() {
    }

    default void beforeGetVoiceMailNumber() {
    }

    default void updateMccMncConfigWithCplmn(String operatorNumeric) {
    }

    default void getCdmaGsmImsiFromHwRil() {
    }

    default String getOperatorNumericHw() {
        return PhoneConfigurationManager.SSSS;
    }
}
