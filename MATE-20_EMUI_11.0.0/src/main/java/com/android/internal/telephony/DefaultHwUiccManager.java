package com.android.internal.telephony;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.uicc.IccUtils;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class DefaultHwUiccManager implements HwUiccManager {
    private static HwUiccManager msInstance = new DefaultHwUiccManager();

    public static HwUiccManager getDefault() {
        return msInstance;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public boolean isHwSimPhonebookEnabled() {
        return false;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public Cursor simContactsQuery(Context context, Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public String simContactsGetType(Context context, Uri url) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public Uri simContactsInsert(Context context, Uri url, ContentValues initialValues) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public int simContactsDelete(Context context, Uri url, String where, String[] whereArgs) {
        return 0;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public int simContactsUpdate(Context context, Uri url, ContentValues values, String where, String[] whereArgs) {
        return 0;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public byte[] buildAdnStringHw(int recordSize, String mAlphaTag, String mNumber) {
        return new byte[0];
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public String prependPlusInLongAdnNumber(String mNumber) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public boolean arrayCompareNullEqualsEmpty(String[] s1, String[] s2) {
        return false;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public String[] updateAnrEmailArrayHelper(String[] dest, String[] src, int fileCount) {
        return new String[0];
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public int getAlphaTagEncodingLength(String alphaTag) {
        return 0;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public String bcdIccidToString(byte[] data, int offset, int length) {
        return IccUtils.bcdToString(data, 0, data.length);
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public String adnStringFieldToStringForSTK(byte[] data, int offset, int length) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public boolean isContainZeros(byte[] data, int length, int totalLength, int curIndex) {
        return false;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void initHwSubscriptionManager(Context c, CommandsInterfaceEx[] ci) {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void updateUserPreferences(boolean setDds) {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void updateDataSlot() {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public AlertDialog createSimAddDialog(Context mContext, boolean isAdded, int mSlotId) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public FileReader getVoiceMailFileReader() {
        try {
            return new FileReader(new File(Environment.getRootDirectory(), "etc/voicemail-conf.xml"));
        } catch (FileNotFoundException e) {
            Rlog.w("DefaultHwUiccManager", "Can't open file.");
            return null;
        }
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public boolean isHotswapSupported() {
        return false;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void registerForIccChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void unregisterForIccChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public boolean getSwitchingSlot() {
        return false;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public int getUserSwitchSlots() {
        return 0;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public boolean isFullNetworkSupported() {
        return false;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void setPreferredNetworkType(int networkType, int phoneId, Message response) {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public String cdmaDTMFToString(byte[] data, int offset, int length) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public boolean isCDMASimCard(int slotId) {
        return false;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void initHwCarrierConfigCardManager(Context context) {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public boolean get4GSlotInSwitchProgress() {
        return false;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public String getAtrHw(int phoneId, String atr) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public String cdmaBcdToStringHw(byte[] data, int offset, int length) {
        return null;
    }
}
