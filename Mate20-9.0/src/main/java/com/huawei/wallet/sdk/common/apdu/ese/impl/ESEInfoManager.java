package com.huawei.wallet.sdk.common.apdu.ese.impl;

import android.content.Context;
import android.os.PowerManager;
import android.text.TextUtils;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.TaskResult;
import com.huawei.wallet.sdk.common.apdu.ese.api.ESEInfoManagerApi;
import com.huawei.wallet.sdk.common.apdu.manager.NFCAesManager;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import com.huawei.wallet.sdk.common.apdu.oma.OmaApduManager;
import com.huawei.wallet.sdk.common.apdu.util.HexByteHelper;
import com.huawei.wallet.sdk.common.apdu.util.OmaUtil;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.storage.NFCPreferences;
import com.huawei.wallet.sdk.common.utils.EMUIBuildUtil;
import com.huawei.wallet.sdk.common.utils.NfcUtil;
import com.huawei.wallet.sdk.common.utils.PhoneFeatureAdaptUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ESEInfoManager implements ESEInfoManagerApi {
    private static final String AMSD_AID = "A000000151000000";
    private static final String APDU_DEACTIVATE_PPSE = "80F00100104F0E325041592E5359532E4444463031";
    private static final String APDU_QUERY_CPLC = "80CA9F7F00";
    private static final String APDU_TYPE_ACTIVIE_CARD_BY_OMA = "active_card_by_oma";
    private static final String APDU_TYPE_DEACTIVE_CARD_BY_OMA = "deactive_card_by_oma";
    private static final String APDU_TYPE_DEACTIVIE_PPSE = "deactive_ppse";
    private static final String APDU_TYPE_QUERY_CARD_NUM = "query_bankcard_num";
    private static final String APDU_TYPE_QUERY_CPLC = "query_cplc";
    private static final String COMMON_CHECKER = "9000";
    private static final String CRS_AID = "A00000015143525300";
    private static final String SP_CPLC_FLAG = "sp_cplc_flag";
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String TAG = "ESEInfoManager|";
    private static final int WAKE_LOCK_TIMEOUT = 60000;
    private static volatile ESEInfoManager instance;
    private static PowerManager.WakeLock wakeLock;
    private static final Object wakeLockSync = new Object();
    private Map<String, List<ApduCommand>> apduRepo = new HashMap();
    private String cplc;
    private HashMap<String, String> errorMsg = new HashMap<>();
    private String inSEcplc;
    private Context mContext;
    private IAPDUService omaService;
    private ChannelID powerOnChannelId;
    private String processPrefix = "";
    private String subProcessPrefix = "";

    public static ESEInfoManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new ESEInfoManager(context);
                }
            }
        }
        return instance;
    }

    private ESEInfoManager(Context context) {
        this.mContext = context.getApplicationContext();
        this.omaService = OmaApduManager.getInstance(this.mContext);
    }

    /* access modifiers changed from: package-private */
    public List<ApduCommand> getApduCommand(String type, String instanceID) {
        String k = type + "_" + instanceID;
        List<ApduCommand> apdus = this.apduRepo.get(k);
        if (apdus != null) {
            return apdus;
        }
        if (APDU_TYPE_QUERY_CPLC.equals(type)) {
            List<ApduCommand> apdus2 = new ArrayList<>();
            apdus2.add(new ApduCommand(1, OmaUtil.getSelectApdu(instanceID), COMMON_CHECKER));
            apdus2.add(new ApduCommand(2, APDU_QUERY_CPLC, COMMON_CHECKER));
            this.apduRepo.put(k, apdus2);
            return apdus2;
        } else if (!APDU_TYPE_DEACTIVIE_PPSE.equals(type)) {
            return null;
        } else {
            List<ApduCommand> apdus3 = new ArrayList<>();
            apdus3.add(new ApduCommand(1, OmaUtil.getSelectApdu(instanceID), COMMON_CHECKER));
            apdus3.add(new ApduCommand(2, APDU_DEACTIVATE_PPSE, COMMON_CHECKER));
            this.apduRepo.put(k, apdus3);
            return apdus3;
        }
    }

    public String queryCplc() {
        if ((EMUIBuildUtil.VERSION.EMUI_SDK_INT < 14 || PhoneFeatureAdaptUtil.isUseI2C()) && !NfcUtil.isEnabledNFC(this.mContext)) {
            LogC.i(this.subProcessPrefix + "Query cplc failed. NFC is not enabled.", false);
        }
        if (!StringUtil.isEmpty(this.cplc, true)) {
            LogC.i(this.subProcessPrefix + "Cplc is not null, return cplc.", false);
            return this.cplc;
        }
        String querycplc = null;
        LogC.i(this.subProcessPrefix + "Query cplc, before OMA_ACCESS_SYNC_LOCK.", false);
        synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
            LogC.i(this.subProcessPrefix + "Query cplc, in OMA_ACCESS_SYNC_LOCK.", false);
            if (!StringUtil.isEmpty(this.cplc, true)) {
                LogC.i(this.subProcessPrefix + "Cplc is not null, after OMA_ACCESS_SYNC_LOCK.", false);
                String str = this.cplc;
                return str;
            }
            LogC.i(this.subProcessPrefix + "Cplc is null, get cplc firstly.", false);
            TaskResult<ChannelID> result = getCplcFromESE(0, 0);
            if (result.getResultCode() == 0) {
                querycplc = parseCplc(result.getLastExcutedCommand().getRapdu());
            } else {
                this.errorMsg.clear();
            }
            this.omaService.setProcessPrefix(this.processPrefix, null);
            this.omaService.closeChannel(result.getData());
            this.omaService.resetProcessPrefix();
            this.cplc = querycplc;
            LogC.i(this.subProcessPrefix + "Query cplc, after OMA_ACCESS_SYNC_LOCK.", false);
            return this.cplc;
        }
    }

    public String queryCplcFromSp() {
        if (TextUtils.isEmpty("") || TextUtils.isEmpty(NFCPreferences.getInstance(this.mContext).getString("chaos_uuid_1", ""))) {
            LogC.i("sp do not have cplc or key", false);
            if (PhoneFeatureAdaptUtil.isMultiEseDevice()) {
                return queryinSECplc();
            }
            return queryCplc();
        }
        String tempCplc = NFCAesManager.getInstance().descryptPersistent("");
        LogC.i("sp have cplc decrypt it", false);
        return tempCplc;
    }

    public String queryCplcListString() {
        String tempCplc;
        LogC.d("ESEInfoManager|queryCplcListString start", false);
        if (PhoneFeatureAdaptUtil.isMultiEseDevice()) {
            String tempCplc2 = "" + queryCplc();
            tempCplc = tempCplc2 + "|" + queryinSECplc();
        } else {
            tempCplc = queryCplc();
        }
        LogC.d("ESEInfoManager|queryCplcListString end|tempCplc:" + tempCplc, false);
        return tempCplc;
    }

    public String queryCplcByMediaType(int mediaType) {
        String tempCplc;
        LogC.d("ESEInfoManager|queryCplcByMediaType start|mediaType:" + mediaType, false);
        if (mediaType == 3) {
            tempCplc = queryinSECplc();
        } else {
            tempCplc = queryCplc();
        }
        LogC.d("ESEInfoManager|queryCplcByMediaType end|tempCplc:" + tempCplc, false);
        return tempCplc;
    }

    private String parseCplc(String cplcRapdu) {
        String cplc2 = null;
        if (StringUtil.isEmpty(cplcRapdu, true)) {
            return null;
        }
        if (cplcRapdu.startsWith("9F7F")) {
            cplc2 = cplcRapdu.substring(6, (HexByteHelper.hexStringToDecimalInteger(cplcRapdu.substring(4, 6)) * 2) + 6);
        }
        return cplc2;
    }

    public String queryinSECplc() {
        if (EMUIBuildUtil.VERSION.EMUI_SDK_INT < 19) {
            LogC.i(this.subProcessPrefix + "Query inSE cplc, emui less than 19.", false);
            if (!NfcUtil.isEnabledNFC(this.mContext)) {
                LogC.i(this.subProcessPrefix + "Query inSE cplc failed. NFC is not enabled.", false);
                this.inSEcplc = null;
                return this.inSEcplc;
            }
        }
        if (!StringUtil.isEmpty(this.inSEcplc, true)) {
            LogC.i(this.subProcessPrefix + "InSE cplc is not null, return inSE cplc.", false);
            return this.inSEcplc;
        }
        String querycplc = null;
        LogC.i(this.subProcessPrefix + "Query inSE cplc, before OMA_ACCESS_SYNC_LOCK.", false);
        synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
            LogC.i(this.subProcessPrefix + "Query inSE cplc, in OMA_ACCESS_SYNC_LOCK.", false);
            if (!StringUtil.isEmpty(this.inSEcplc, true)) {
                LogC.i(this.subProcessPrefix + "InSE cplc is not null, after OMA_ACCESS_SYNC_LOCK.", false);
                String str = this.inSEcplc;
                return str;
            }
            LogC.i(this.subProcessPrefix + "InSE cplc is null, get inSE cplc firstly.", false);
            TaskResult<ChannelID> result = getCplcFromESE(0, 3);
            if (result.getResultCode() == 0) {
                querycplc = parseCplc(result.getLastExcutedCommand().getRapdu());
            } else {
                this.errorMsg.clear();
                String msg = "query inSEcplc failed. " + result.getMsg();
                this.errorMsg.put("resultCode", "" + result.getResultCode());
                this.errorMsg.put("fail_reason", msg);
                LogC.e(msg, false);
            }
            this.omaService.setProcessPrefix(this.processPrefix, null);
            this.omaService.closeChannel(result.getData());
            this.omaService.resetProcessPrefix();
            this.inSEcplc = querycplc;
            LogC.i(this.subProcessPrefix + "Query inSE cplc, after OMA_ACCESS_SYNC_LOCK.", false);
            return this.inSEcplc;
        }
    }

    private TaskResult<ChannelID> getCplcFromESE(int channelType, int mediaType) {
        TaskResult<ChannelID> result;
        synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
            LogC.i(this.subProcessPrefix + "Start to get cplc from eSE, channelType: " + channelType + ", mediaType: " + mediaType, false);
            acquireWakeLock(this.mContext);
            List<ApduCommand> apdu = getApduCommand(APDU_TYPE_QUERY_CPLC, AMSD_AID);
            ChannelID channelID = new ChannelID();
            channelID.setChannelType(channelType);
            channelID.setMediaType(mediaType);
            this.omaService.setProcessPrefix(this.processPrefix, null);
            result = this.omaService.excuteApduList(apdu, channelID);
            this.omaService.resetProcessPrefix();
            LogC.i(this.subProcessPrefix + "Get cplc from eSE end, " + result.getPrintMsg(), false);
            releaseLostTaskWakeLock();
        }
        return result;
    }

    private static void acquireWakeLock(Context mContext2) {
        synchronized (wakeLockSync) {
            if (wakeLock == null) {
                LogC.i("ESEInfoManager acquireWakeLock, wakeLock is null ,wake lock now.", false);
                wakeLock = ((PowerManager) mContext2.getSystemService("power")).newWakeLock(1, "beginWakeLock");
                wakeLock.setReferenceCounted(true);
            } else {
                LogC.i("ESEInfoManager acquireWakeLock, wakeLock not null.", false);
            }
            wakeLock.acquire(60000);
            LogC.i("ESEInfoManager acquireWakeLock, lock has been wake. WAKE_LOCK_TIMEOUT= 60000", false);
        }
    }

    private static void releaseLostTaskWakeLock() {
        synchronized (wakeLockSync) {
            if (wakeLock != null) {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                    LogC.i("ESEInfoManager releaseLostTaskWakeLock, wakeLock release. WAKE_LOCK_TIMEOUT= 60000", false);
                } else {
                    LogC.i("ESEInfoManager releaseLostTaskWakeLock, wakeLock not held. ", false);
                }
                wakeLock = null;
            } else {
                LogC.i("ESEInfoManager releaseLostTaskWakeLock, wakeLock is null. ", false);
            }
        }
    }

    public void closeSession() {
        synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
            this.omaService.setProcessPrefix(this.processPrefix, null);
            TaskResult<Integer> result = this.omaService.closeSEService();
            this.omaService.resetProcessPrefix();
            LogC.i(this.subProcessPrefix + "ESEInfoManager close session end," + result.getPrintMsg(), false);
        }
    }

    public boolean esePowerOn(int mediaType) {
        boolean isSuccess;
        synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
            isSuccess = false;
            LogC.i("esePowerOn begin.", false);
            TaskResult<ChannelID> result = getCplcFromESE(0, mediaType);
            if (result.getResultCode() == 0) {
                this.powerOnChannelId = result.getData();
                isSuccess = true;
            } else {
                this.errorMsg.clear();
                LogC.e("esePowerOn failed. " + result.getPrintMsg(), false);
            }
            LogC.i("esePowerOn end. result : " + isSuccess, false);
        }
        return isSuccess;
    }

    public boolean esePowerOff() {
        synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
            LogC.i("esePowerOff begin", false);
            if (this.powerOnChannelId != null) {
                if (this.omaService.closeChannel(this.powerOnChannelId).getResultCode() != 0) {
                    String msg = "esePowerOff failed, " + result.getPrintMsg();
                    this.errorMsg.clear();
                    this.errorMsg.put("fail_code", "" + result.getResultCode());
                    this.errorMsg.put("fail_reason", msg);
                    LogC.e(msg, false);
                }
                this.powerOnChannelId = null;
            }
            LogC.i("esePowerOff off", false);
        }
        return true;
    }

    public byte[] querySeid() {
        byte[] defaultByteArray = new byte[0];
        String cplc2 = queryCplc();
        if (StringUtil.isEmpty(cplc2, true)) {
            LogC.e("querySeid, illegal cplc", false);
            return defaultByteArray;
        }
        StringBuilder seidStr = new StringBuilder(20);
        seidStr.append(cplc2.substring(0, 4));
        seidStr.append(cplc2.substring(20, 36));
        return HexByteHelper.hexStringToByteArray(seidStr.toString());
    }

    public int queryOpenMobileChannel() {
        int idx;
        synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
            LogC.i("queryOpenMobileChannel begin", false);
            idx = -1;
            TaskResult<Integer> result = this.omaService.getReaderId(0);
            if (result.getResultCode() == 0) {
                idx = result.getData().intValue();
            }
            LogC.i("queryOpenMobileChannel end. idx : " + idx, false);
        }
        return idx;
    }

    public boolean isEseLocked() {
        boolean isLocked;
        synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
            LogC.i(this.subProcessPrefix + "Start to check isEseLocked.", false);
            isLocked = false;
            TaskResult<ChannelID> result = getCplcFromESE(0, 0);
            if (result.getResultCode() == 2007) {
                isLocked = true;
            } else if (result.getResultCode() != 0) {
                String msg = this.subProcessPrefix + "isEseLocked check failed, " + result.getPrintMsg();
                this.errorMsg.clear();
                this.errorMsg.put("fail_code", "" + result.getResultCode());
                this.errorMsg.put("fail_reason", msg);
                LogC.e(msg, false);
            }
            this.omaService.setProcessPrefix(this.processPrefix, null);
            this.omaService.closeChannel(result.getData());
            this.omaService.resetProcessPrefix();
            LogC.i(this.subProcessPrefix + "Check isEseLocked end, isLocked: " + isLocked, false);
        }
        return isLocked;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0094, code lost:
        if (com.huawei.wallet.sdk.common.utils.StringUtil.isEmpty(r0, true) != false) goto L_0x0098;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0096, code lost:
        r10.cplc = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0098, code lost:
        return r0;
     */
    public String getCplcByBasicChannel() {
        if (!StringUtil.isEmpty(this.cplc, true)) {
            LogC.i("CplcByBasicChannel  cplc is not  null  return cplc ", false);
            return this.cplc;
        }
        String cplc2 = null;
        synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
            if (!StringUtil.isEmpty(this.cplc, true)) {
                LogC.i("CplcByBasicChannel cplc is not  null,in synchronized return cplc ", false);
                String str = this.cplc;
                return str;
            }
            LogC.i(" CplcByBasicChannel cplc is null  is first  getCplc", false);
            TaskResult<ChannelID> result = getCplcFromESE(1, 0);
            if (result.getResultCode() == 0) {
                cplc2 = parseCplc(result.getLastExcutedCommand().getRapdu());
            } else {
                String msg = "getCplcByBasicChannel failed, " + result.getPrintMsg();
                this.errorMsg.clear();
                this.errorMsg.put("fail_code", "" + result.getResultCode());
                this.errorMsg.put("fail_reason", msg);
                LogC.e(msg, false);
            }
            this.omaService.closeChannel(result.getData());
        }
    }

    public void deactivatePPSE() {
        synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
            LogC.i("deactivatePPSE begin.", false);
            List<ApduCommand> apdu = getApduCommand(APDU_TYPE_DEACTIVIE_PPSE, CRS_AID);
            ChannelID channelId = new ChannelID();
            channelId.setChannelType(1);
            TaskResult<ChannelID> result = this.omaService.excuteApduList(apdu, channelId);
            if (result.getResultCode() != 0) {
                String msg = "deactivatePPSE failed. " + result.getPrintMsg();
                this.errorMsg.clear();
                this.errorMsg.put("fail_code", "" + result.getResultCode());
                this.errorMsg.put("fail_reason", msg);
                LogC.e(msg, false);
            }
            this.omaService.closeChannel(result.getData());
            LogC.i("deactivatePPSE end.", false);
        }
    }

    public boolean deactivateCard(String aid) {
        boolean deactivateCardResult;
        synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
            deactivateCardResult = true;
            LogC.i("deactivateCard begin.", false);
            List<ApduCommand> apdu = getActiveCardApduCommand(APDU_TYPE_DEACTIVE_CARD_BY_OMA, aid);
            ChannelID channelId = new ChannelID();
            channelId.setChannelType(1);
            TaskResult<ChannelID> result = this.omaService.excuteApduList(apdu, channelId);
            if (result.getResultCode() != 0) {
                deactivateCardResult = false;
                String msg = "deactivateCard failed. " + result.getPrintMsg();
                this.errorMsg.clear();
                this.errorMsg.put("fail_code", "" + result.getResultCode());
                this.errorMsg.put("fail_reason", msg);
                LogC.e(msg, false);
            }
            this.omaService.closeChannel(result.getData());
            LogC.i("deactivateCard by oma end.and result :" + deactivateCardResult, false);
        }
        return deactivateCardResult;
    }

    public boolean activateCard(String aid) {
        boolean activeResult;
        synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
            activeResult = true;
            LogC.i("activateCard begin.", false);
            List<ApduCommand> apdu = getActiveCardApduCommand(APDU_TYPE_ACTIVIE_CARD_BY_OMA, aid);
            ChannelID channelId = new ChannelID();
            channelId.setChannelType(1);
            TaskResult<ChannelID> result = this.omaService.excuteApduList(apdu, channelId);
            if (result.getResultCode() != 0) {
                String msg = "activateCard failed. " + result.getPrintMsg();
                this.errorMsg.clear();
                this.errorMsg.put("fail_code", "" + result.getResultCode());
                this.errorMsg.put("fail_reason", msg);
                LogC.e(msg, false);
                activeResult = false;
            }
            this.omaService.closeChannel(result.getData());
            LogC.i("activateCard by oma end.and result :" + activeResult, false);
        }
        return activeResult;
    }

    private List<ApduCommand> getActiveCardApduCommand(String type, String instanceID) {
        String str;
        String str2;
        String str3;
        String str4;
        String k = type + "_" + instanceID;
        List<ApduCommand> apdus = this.apduRepo.get(k);
        if (apdus != null) {
            LogC.d("getActiveCardApduCommand type is :" + type + ",apdu is : " + apdus.get(1).getApdu(), false);
            return apdus;
        }
        int aidlength = instanceID.length() / 2;
        String apdu = "";
        String aidleth = Integer.toHexString(aidlength + 2).toUpperCase(Locale.getDefault());
        String aidleth2 = Integer.toHexString(aidlength).toUpperCase(Locale.getDefault());
        if (APDU_TYPE_ACTIVIE_CARD_BY_OMA.equals(type)) {
            StringBuilder sb = new StringBuilder();
            sb.append("80F00101");
            if (aidleth.length() > 1) {
                str3 = aidleth;
            } else {
                str3 = "0" + aidleth;
            }
            sb.append(str3);
            sb.append("4F");
            if (aidleth2.length() > 1) {
                str4 = aidleth2;
            } else {
                str4 = "0" + aidleth2;
            }
            sb.append(str4);
            sb.append(instanceID);
            apdu = sb.toString();
            LogC.d("active apdu :" + apdu, false);
        }
        if (APDU_TYPE_DEACTIVE_CARD_BY_OMA.equals(type)) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("80F00100");
            if (aidleth.length() > 1) {
                str = aidleth;
            } else {
                str = "0" + aidleth;
            }
            sb2.append(str);
            sb2.append("4F");
            if (aidleth2.length() > 1) {
                str2 = aidleth2;
            } else {
                str2 = "0" + aidleth2;
            }
            sb2.append(str2);
            sb2.append(instanceID);
            apdu = sb2.toString();
            LogC.d("deactive apdu :" + apdu, false);
        }
        List<ApduCommand> apdus2 = new ArrayList<>();
        apdus2.add(new ApduCommand(1, OmaUtil.getSelectApdu(CRS_AID), COMMON_CHECKER));
        apdus2.add(new ApduCommand(2, apdu, COMMON_CHECKER));
        this.apduRepo.put(k, apdus2);
        return apdus2;
    }

    public void setProcessPrefix(String processPrefix2, String tag) {
        this.processPrefix = processPrefix2;
        this.subProcessPrefix = this.processPrefix + TAG;
    }

    public void resetProcessPrefix() {
        this.processPrefix = "";
        this.subProcessPrefix = "";
    }
}
