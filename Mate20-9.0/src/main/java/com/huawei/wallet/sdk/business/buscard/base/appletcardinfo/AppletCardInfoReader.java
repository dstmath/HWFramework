package com.huawei.wallet.sdk.business.buscard.base.appletcardinfo;

import android.content.Context;
import android.nfc.tech.IsoDep;
import android.os.PowerManager;
import com.huawei.wallet.sdk.business.buscard.base.model.CardInfo;
import com.huawei.wallet.sdk.business.buscard.base.model.TransactionRecord;
import com.huawei.wallet.sdk.business.buscard.base.traffic.TrafficCardInfoReader;
import com.huawei.wallet.sdk.business.buscard.base.traffic.datacheck.DataCheckerManager;
import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.constant.AutoReportErrorCode;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.contactless.ContactlessApduManager;
import com.huawei.wallet.sdk.common.apdu.oma.OmaApduManager;
import java.util.List;

public class AppletCardInfoReader implements AppletCardInfoReadApi {
    private static final String TAG = "AppletCardInfo";
    private static final Object WAKE_LOCK_SYNC = new Object();
    private static final int WAKE_LOCK_TIMEOUT = 180000;
    private static PowerManager.WakeLock wakeLock;
    private ConfigData configData = ConfigDataManager.getInstance(this.mContext);
    private IAPDUService contactlessApduService = ContactlessApduManager.getInstance();
    private AppletVersionReader mAppletVersionReader = new AppletVersionReader(this.omaService, this.configData);
    private Context mContext;
    private IAPDUService omaService = OmaApduManager.getInstance(this.mContext);
    private TrafficCardInfoReader tCardInfoContactlessReader = new TrafficCardInfoReader(this.mContext, this.contactlessApduService, this.configData);
    private TrafficCardInfoReader tCardInfoReader = new TrafficCardInfoReader(this.mContext, this.omaService, this.configData);

    public AppletCardInfoReader(Context context) {
        this.mContext = context;
    }

    public AppletCardResult<CardInfo> readTrafficCardInfo(String aid, String productId, int dataType) {
        LogX.i("AppletCardInfo readTrafficCardInfo begin");
        acquireWakeLock(this.mContext);
        AppletCardResult<CardInfo> result = new AppletCardResult<>();
        try {
            this.tCardInfoReader.setDataChecker(DataCheckerManager.getInstance().getDataCheckerForCard(aid));
            synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
                result.setData(this.tCardInfoReader.readCardInfo(aid, productId, dataType));
            }
        } catch (AppletCardException e) {
            AppletCardException e2 = e;
            this.tCardInfoReader.closeChannel();
            result.setResultCode(e2.getErrCode());
            result.setMsg(e2.getMessage());
            reportErrorInfo(aid, productId, "readTrafficCardInfo", AutoReportErrorCode.ERROR_EVENT_ID_NFC_ESE_GET_TRAFFIC_CARD_INFO_FAIL, result);
        }
        LogX.i("AppletCardInfo readTrafficCardInfo end. result : " + result.getResultCode());
        releaseWakeLock();
        return result;
    }

    private static void acquireWakeLock(Context mContext2) {
        synchronized (WAKE_LOCK_SYNC) {
            if (wakeLock == null) {
                LogX.i("AppletCardInfoReader acquireWakeLock, wakeLock is null ,wake lock now.");
                wakeLock = ((PowerManager) mContext2.getSystemService("power")).newWakeLock(1, "beginWakeLock");
                wakeLock.setReferenceCounted(true);
            } else {
                LogX.i("AppletCardInfoReader acquireWakeLock, wakeLock not null.");
            }
            wakeLock.acquire(180000);
            LogX.i("AppletCardInfoReader acquireWakeLock, lock has been wake. WAKE_LOCK_TIMEOUT= 180000");
        }
    }

    private static void releaseWakeLock() {
        synchronized (WAKE_LOCK_SYNC) {
            if (wakeLock != null) {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                    LogX.i("AppletCardInfoReader releaseWakeLock, wakeLock release. WAKE_LOCK_TIMEOUT= 180000");
                } else {
                    LogX.i("AppletCardInfoReader releaseWakeLock, wakeLock not held. ");
                }
                wakeLock = null;
            } else {
                LogX.i("AppletCardInfoReader releaseWakeLock, wakeLock is null. ");
            }
        }
    }

    public AppletCardResult<CardInfo> readTrafficCardInfoContactless(String productId, int dataType, IsoDep isoDep) {
        LogX.i("AppletCardInfo readTrafficCardInfoContactless begin");
        acquireWakeLock(this.mContext);
        AppletCardResult<CardInfo> result = new AppletCardResult<>();
        synchronized (IAPDUService.CONTACTLESSC_LOCK) {
            try {
                result.setData(this.tCardInfoContactlessReader.readCardInfo(productId, dataType, isoDep));
            } catch (AppletCardException e) {
                AppletCardException e2 = e;
                result.setResultCode(e2.getErrCode());
                result.setMsg(e2.getMessage());
                reportErrorInfo("", productId, "readTrafficCardInfo", AutoReportErrorCode.ERROR_EVENT_ID_NFC_ESE_GET_TRAFFIC_CARD_INFO_FAIL, result);
            }
        }
        LogX.i("AppletCardInfo readTrafficCardInfoContactless end. result : " + result.getResultCode());
        releaseWakeLock();
        return result;
    }

    public AppletCardResult<List<TransactionRecord>> readTrafficCardTransactionRecord(String aid, String productId) {
        LogX.i("AppletCardInfo readTrafficCardTransactionRecord begin");
        acquireWakeLock(this.mContext);
        AppletCardResult<List<TransactionRecord>> result = new AppletCardResult<>();
        try {
            synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
                result.setData(this.tCardInfoReader.readTransactionRecords(aid, productId));
            }
        } catch (AppletCardException e) {
            AppletCardException e2 = e;
            this.tCardInfoReader.closeChannel();
            result.setResultCode(e2.getErrCode());
            result.setMsg(e2.getMessage());
            reportErrorInfo(aid, productId, "readTrafficCardTransactionRecord", AutoReportErrorCode.ERROR_EVENT_ID_NFC_ESE_GET_TRAFFIC_CARD_TRADE_RECORD_FAIL, result);
        }
        LogX.i("AppletCardInfo readTrafficCardTransactionRecord end. result : " + result.getResultCode());
        releaseWakeLock();
        return result;
    }

    public AppletCardResult<String> readBankCardNum(String aid) {
        LogX.i("AppletCardInfo readBankCardNum begin");
        return new AppletCardResult<>();
    }

    public AppletCardResult<String> readAppletVersion(String aid) {
        LogX.i("AppletCardInfo readAppletVersion begin");
        AppletCardResult<String> result = new AppletCardResult<>();
        try {
            synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
                result.setData(this.mAppletVersionReader.readAppletVersion(aid));
                result.setResultCode(0);
            }
        } catch (AppletCardException e) {
            AppletCardException e2 = e;
            result.setResultCode(e2.getErrCode());
            result.setMsg(e2.getMessage());
            reportErrorInfo(aid, "", "readAppletVersion", AutoReportErrorCode.ERROR_EVENT_ID_NFC_ESE_GET_APPLET_VERSION_FAIL, result);
        }
        LogX.i("AppletCardInfo readAppletVersion end. result : " + result.getResultCode());
        return result;
    }

    public AppletCardResult<String> readLastTransactionInfo(String aid) {
        LogX.i("AppletCardInfo readLastTransactionInfo begin");
        AppletCardResult<String> result = new AppletCardResult<>();
        LogX.i("AppletCardInfo readLastTransactionInfo end. result : " + result.getResultCode());
        return result;
    }

    public AppletCardResult<String> readFMCardInfo(String aid, String productId) {
        LogX.i("AppletCardInfo readFMCardInfo begin");
        AppletCardResult<String> result = new AppletCardResult<>();
        try {
            synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
                result.setData(this.tCardInfoReader.readFMCardInfo(aid, productId));
            }
        } catch (AppletCardException e) {
            AppletCardException e2 = e;
            this.tCardInfoReader.closeChannel();
            result.setResultCode(e2.getErrCode());
            result.setMsg(e2.getMessage());
            reportErrorInfo(aid, productId, "readFMCardInfo", AutoReportErrorCode.ERROR_EVENT_ID_NFC_ESE_GET_TRAFFIC_CARD_INFO_FAIL, result);
        }
        LogX.i("AppletCardInfo readFMCardInfo end. result : " + result.getResultCode());
        return result;
    }

    private void reportErrorInfo(String aid, String productId, String method, int code, AppletCardResult result) {
    }
}
