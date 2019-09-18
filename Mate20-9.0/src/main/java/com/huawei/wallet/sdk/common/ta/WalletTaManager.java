package com.huawei.wallet.sdk.common.ta;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import com.huawei.wallet.sdk.business.bankcard.modle.CertificationInfo;
import com.huawei.wallet.sdk.business.bankcard.modle.TATrustedStorageInfo;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.ta.WalletTaException;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.huawei.wallet.sdk.common.utils.crypto.AES;
import com.huawei.wallet.tz.WalletCa;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WalletTaManager {
    private static final String HIANALYTICSKEY = "WalletTaManager";
    public static final String HUAWEI_WALLET_CA_DEFAULT = "huawei_wallet_ca";
    public static final String HUAWEI_WALLET_CA_MATES = "huawei_wallet_ca_mates";
    public static final String HUAWEI_WALLET_CA_MATES_SO = "libhuawei_wallet_ca_mates.so";
    private static final int NOT_UPDATE_CARD_STATUS = -10000;
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String TA_FILE_NAME = "4ae7ba51-2810-4cee-abbe-a42307b4ace3.sec";
    private static ArrayList<TACardInfo> cardInfoListCache = null;
    private static final Object cardInfoListLock = new Object();
    private static volatile WalletTaManager instance;
    private static final List<String> matesModel = Arrays.asList(new String[]{"HUAWEI CRR-CL00", "HUAWEI CRR-CL20", "HUAWEI CRR-TL00", "HUAWEI CRR-UL00", "HUAWEI CRR-UL20"});
    private Context mContext;

    public static WalletTaManager getInstance(Context context) {
        LogC.d("WalletTaManager.getInstance executed", false);
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new WalletTaManager(context);
                }
            }
        }
        return instance;
    }

    private static Context getContext(Context context) {
        if (context instanceof Activity) {
            return context.getApplicationContext();
        }
        return context;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005f, code lost:
        return r1;
     */
    public ArrayList<TACardInfo> getCardList() {
        synchronized (cardInfoListLock) {
            ArrayList<TACardInfo> cardInfoList = new ArrayList<>();
            if (cardInfoListCache == null) {
                try {
                    cardInfoListCache = getCardListFromTa();
                    LogC.i("cardInfoListCache is null, refresh", false);
                } catch (WalletTaException.WalletTaSystemErrorException e) {
                    LogC.e("WalletTaManager get tacardList erro,errorCode=" + e.getCode(), false);
                    return null;
                }
            }
            if (cardInfoListCache != null) {
                if (cardInfoListCache.size() != 0) {
                    Iterator<TACardInfo> it = cardInfoListCache.iterator();
                    while (it.hasNext()) {
                        cardInfoList.add(it.next().clone());
                    }
                    return cardInfoList;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0020, code lost:
        return null;
     */
    public TACardInfo getCard(String referenceId) {
        synchronized (cardInfoListLock) {
            if (cardInfoListCache != null) {
                if (!cardInfoListCache.isEmpty()) {
                    TACardInfo cardInfo = getCardFromCardListCache(referenceId);
                    if (cardInfo == null) {
                        return null;
                    }
                    TACardInfo clone = cardInfo.clone();
                    return clone;
                }
            }
        }
    }

    private TACardInfo getCardFromCardListCache(String referenceId) {
        if (cardInfoListCache != null) {
            Iterator<TACardInfo> it = cardInfoListCache.iterator();
            while (it.hasNext()) {
                TACardInfo cardInfo = it.next();
                if (cardInfo.getDpanDigest().equals(referenceId)) {
                    return cardInfo;
                }
            }
        }
        return null;
    }

    public void updateCardStatus(String referenceId, int cardStatus) throws WalletTaException.WalletTaCardNotExistException, WalletTaException.WalletTaSystemErrorException {
        synchronized (cardInfoListLock) {
            if (cardInfoListCache == null) {
                cardInfoListCache = getCardListFromTa();
            }
            TACardInfo cardInfoCache = getCardFromCardListCache(referenceId);
            if (cardInfoCache != null) {
                TACardInfo cardInfo = cardInfoCache.clone();
                if (cardStatus != NOT_UPDATE_CARD_STATUS) {
                    cardInfo.setCardStatus(cardStatus);
                }
                cardInfo.setStatusUpdateTime(System.currentTimeMillis());
                updateCard(cardInfo);
                if (cardStatus != NOT_UPDATE_CARD_STATUS) {
                    cardInfoCache.setCardStatus(cardStatus);
                }
            } else {
                LogC.i("updateCard failed, card is not exsit", false);
                throw new WalletTaException().newWalletTaCardNotExistException();
            }
        }
    }

    private void updateCard(TACardInfo cardInfo) throws WalletTaException.WalletTaCardNotExistException, WalletTaException.WalletTaSystemErrorException {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("cardGroupType is :");
            sb.append(cardInfo.getCardGroupType());
            sb.append(",");
            sb.append("getAid is :");
            sb.append(cardInfo.getAid());
            sb.append(",");
            sb.append("TaCardInfoJsonStr is :");
            sb.append(cardInfo.getTaCardInfoJsonStr());
            LogC.i("updateCard " + sb.toString(), true);
            long result = WalletCa.updateCardImpl(cardInfo.getCardGroupType(), cardInfo.getAid().getBytes(AES.CHAR_ENCODING), cardInfo.getTaCardInfoJsonStr().getBytes(AES.CHAR_ENCODING));
            if (0 == result) {
                LogC.i("WalletTaManager updateCard success.", false);
                LogC.i("wallet TA update card success", false);
            } else if (4294770698L == result) {
                LogC.i("updateCard failed, card is not exsit", false);
                throw new WalletTaException().newWalletTaCardNotExistException();
            } else {
                LogC.e("updateCard failed, result = " + Long.toHexString(result), false);
                throw new WalletTaException().newWalletTaSystemErrorException(result);
            }
        } catch (UnsatisfiedLinkError e) {
            LogC.i("WalletTaManager updateCard fail, UnsatisfiedLinkError. Ca lib load error, errorCode= 4294770704", false);
            throw new WalletTaException().newWalletTaSystemErrorException(4294770704L);
        } catch (UnsupportedEncodingException e2) {
            LogC.i("WalletTaManager updateCard fail, UnsupportedEncodingException. Unsupported encoding, errorCode= 4294770706", false);
            LogC.e("getBytes failed, UnsupportedEncodingException", false);
            throw new WalletTaException().newWalletTaSystemErrorException(4294770706L);
        }
    }

    public TACardInfo getCardInfoByAid(String aid) {
        TACardInfo clone;
        synchronized (cardInfoListLock) {
            TACardInfo ta = getCardInfoFromCacheByAid(aid);
            clone = ta == null ? null : ta.clone();
        }
        return clone;
    }

    private TACardInfo getCardInfoFromCacheByAid(String aid) {
        if (cardInfoListCache == null || cardInfoListCache.isEmpty()) {
            return null;
        }
        Iterator<TACardInfo> it = cardInfoListCache.iterator();
        while (it.hasNext()) {
            TACardInfo cardInfo = it.next();
            if (cardInfo != null) {
                if (TextUtils.equals(cardInfo.getAid(), aid)) {
                    return cardInfo;
                }
                if (cardInfo.getAid2() != null && TextUtils.equals(cardInfo.getAid2(), aid)) {
                    return cardInfo;
                }
            }
        }
        return null;
    }

    public void removeCard(String referenceId) throws WalletTaException.WalletTaCardNotExistException, WalletTaException.WalletTaSystemErrorException {
        synchronized (cardInfoListLock) {
            if (cardInfoListCache == null) {
                cardInfoListCache = getCardListFromTa();
            }
            TACardInfo cardInfo = getCardFromCardListCache(referenceId);
            long result = getRemoveCardResult(cardInfo);
            if (0 == result) {
                LogC.i("WalletTaManager removeCard success.", false);
                Iterator<TACardInfo> it = cardInfoListCache.iterator();
                while (it.hasNext()) {
                    TACardInfo taCardInfo = it.next();
                    if (taCardInfo.getCardGroupType() == cardInfo.getCardGroupType() && taCardInfo.getDpanDigest().equals(cardInfo.getDpanDigest())) {
                        cardInfoListCache.remove(taCardInfo);
                        return;
                    }
                }
            } else if (4294770698L == result) {
                LogC.i("removeCard failed, card is not exsit", false);
                throw new WalletTaException().newWalletTaCardNotExistException();
            } else {
                LogC.e("removeCard failed, result = " + Long.toHexString(result), false);
                throw new WalletTaException().newWalletTaSystemErrorException(result);
            }
        }
    }

    private static long getRemoveCardResult(TACardInfo cardInfo) throws WalletTaException.WalletTaCardNotExistException, WalletTaException.WalletTaSystemErrorException {
        if (cardInfo != null) {
            try {
                long result = WalletCa.removeCardImpl(cardInfo.getCardGroupType(), cardInfo.getAid().getBytes(AES.CHAR_ENCODING));
                LogC.i("WalletTaManager getRemoveCardResult, result: " + Long.toHexString(result), false);
                return result;
            } catch (UnsatisfiedLinkError e) {
                LogC.i("WalletTaManager getRemoveCardResult fail, UnsatisfiedLinkError. Ca lib load error, errorCode= 4294770704", false);
                throw new WalletTaException().newWalletTaSystemErrorException(4294770704L);
            } catch (UnsupportedEncodingException e2) {
                LogC.e("aid getBytes failed, UnsupportedEncodingException", false);
                LogC.i("WalletTaManager getRemoveCardResult fail, UnsupportedEncodingException. Unsuooorted encoding, errorCode= 4294770706", false);
                throw new WalletTaException().newWalletTaSystemErrorException(4294770706L);
            }
        } else {
            LogC.i("removeCard failed, card is not exsit", false);
            throw new WalletTaException().newWalletTaCardNotExistException();
        }
    }

    public void initCertification(String authSignRequest) throws WalletTaException.WalletTaSystemErrorException {
        if (authSignRequest != null) {
            try {
                long result = WalletCa.initCertificationImpl(authSignRequest.getBytes(AES.CHAR_ENCODING));
                if (0 == result) {
                    LogC.i(" WalletTaManager:initCertification" + " init certification successfully.", false);
                    return;
                }
                LogC.e(" WalletTaManager:initCertification" + " init certification failed, result = " + Long.toHexString(result), false);
                throw new WalletTaException().newWalletTaSystemErrorException(result);
            } catch (UnsatisfiedLinkError e) {
                String message = " WalletTaManager:initCertification" + " init certification meet UnsatisfiedLinkError with ErrCode= 4294901760";
                LogC.i(message + ", desc=" + e.getMessage(), false);
                throw new WalletTaException().newWalletTaSystemErrorException(4294901760L);
            } catch (UnsupportedEncodingException e2) {
                String message2 = " WalletTaManager:initCertification" + " init certification meet UnsupportedEncodingException with ErrCode=4294901760";
                LogC.i(message2 + ", desc: " + e2.getMessage(), false);
                throw new WalletTaException().newWalletTaSystemErrorException(4294901760L);
            }
        }
    }

    public CertificationInfo queryCertification() throws WalletTaException.WalletTaSystemErrorException {
        WalletCa.OutputParam deviceCert = WalletCa.newOutputParam();
        WalletCa.OutputParam businessCert = WalletCa.newOutputParam();
        WalletCa.OutputParam authSignRes = WalletCa.newOutputParam();
        try {
            long result = WalletCa.queryCertificationImpl(deviceCert, businessCert, authSignRes);
            if (0 == result) {
                LogC.i(" WalletTaManager::queryCertification" + " query certifcation successfully.", false);
                try {
                    String deviceCertStr = Base64.encodeToString(deviceCert.bytes, 2);
                    String businessCertStr = Base64.encodeToString(businessCert.bytes, 2);
                    String authSignResultStr = Base64.encodeToString(authSignRes.bytes, 2);
                    CertificationInfo certificationInfo = new CertificationInfo();
                    certificationInfo.setDeviceCert(deviceCertStr);
                    certificationInfo.setServiceCert(businessCertStr);
                    certificationInfo.setAuthSignResult(authSignResultStr);
                    return certificationInfo;
                } catch (Exception e) {
                    String message = message + " query certifcation meet UnsupportedEncodingException with ErrCode=4294770706";
                    LogC.e(message + ", desc=" + e.getMessage(), false);
                    throw new WalletTaException().newWalletTaSystemErrorException(4294770706L);
                }
            } else {
                LogC.i(" WalletTaManager::queryCertification" + " query certifcation failed, result = " + Long.toHexString(result), false);
                throw new WalletTaException().newWalletTaSystemErrorException(result);
            }
        } catch (UnsatisfiedLinkError e2) {
            String message2 = " WalletTaManager::queryCertification" + " query certifcation meet UnsatisfiedLinkError with ErrCode= 4294901760";
            LogC.i(message2 + ", desc=" + e2.getMessage(), false);
            throw new WalletTaException().newWalletTaSystemErrorException(4294901760L);
        }
    }

    public String getSignature(String signatureReq) throws WalletTaException.WalletTaSystemErrorException {
        if (signatureReq == null) {
            return null;
        }
        WalletCa.OutputParam signatureRes = WalletCa.newOutputParam();
        try {
            long result = WalletCa.getSignatureImpl(signatureReq.getBytes(AES.CHAR_ENCODING), signatureRes);
            if (0 == result) {
                LogC.i(" WalletTaManager::getSignature" + " get signature successfully.", false);
                try {
                    return Base64.encodeToString(signatureRes.bytes, 2);
                } catch (Exception e) {
                    LogC.e(message + ", desc=" + e.getMessage(), false);
                    LogC.i(message + " get signature meet UnsupportedEncodingException with ErrCode=4294770706", false);
                    throw new WalletTaException().newWalletTaSystemErrorException(4294770706L);
                }
            } else {
                LogC.i(" WalletTaManager::getSignature" + " get signature failed, result = " + Long.toHexString(result), false);
                throw new WalletTaException().newWalletTaSystemErrorException(result);
            }
        } catch (UnsatisfiedLinkError e2) {
            String message = " WalletTaManager::getSignature" + " get signature meet UnsatisfiedLinkError with ErrCode= 4294901760";
            LogC.i(message + ", desc=" + e2.getMessage(), false);
            throw new WalletTaException().newWalletTaSystemErrorException(4294901760L);
        } catch (UnsupportedEncodingException e3) {
            String message2 = " WalletTaManager::getSignature" + " get signature meet UnsupportedEncodingException. Unsupported encoding with errorCode=4294901760";
            LogC.e(message2 + ", desc: " + e3.getMessage(), false);
            throw new WalletTaException().newWalletTaSystemErrorException(4294901760L);
        }
    }

    public int getCertUploadFlag() throws WalletTaException.WalletTaSystemErrorException {
        return getTATrustedStorageInfo("getCertUploadFlag").getCertUploadFlag();
    }

    public void setCertUploadFlag(int certUploadFlag) throws WalletTaException.WalletTaSystemErrorException {
        TATrustedStorageInfo tsInfo = getTATrustedStorageInfoForSet("setCertUploadFlag");
        tsInfo.setCertUploadFlag(certUploadFlag);
        setTATrustedStorageInfo(tsInfo, "setCertUploadFlag");
    }

    public int getResetFactoryFlag() throws WalletTaException.WalletTaSystemErrorException {
        return getTATrustedStorageInfo("getResetFactoryFlag").getResetFactoryFlag();
    }

    public void setResetFactoryFlag(int resetFactoryFlag) throws WalletTaException.WalletTaSystemErrorException {
        TATrustedStorageInfo tsInfo = getTATrustedStorageInfoForSet("setResetFactoryFlag");
        tsInfo.setResetFactoryFlag(resetFactoryFlag);
        setTATrustedStorageInfo(tsInfo, "setResetFactoryFlag");
    }

    public void setRouterInfo(String routerInfo) throws WalletTaException.WalletTaSystemErrorException {
        TATrustedStorageInfo tsInfo = getTATrustedStorageInfoForSet("setRouterInfo");
        tsInfo.setRouterInfo(routerInfo);
        setTATrustedStorageInfo(tsInfo, "setRouterInfo");
    }

    public String getRouterInfo() throws WalletTaException.WalletTaSystemErrorException {
        return getTATrustedStorageInfo("getRouterInfo").getRouterInfo();
    }

    public void setCountryCode(String countryCode) throws WalletTaException.WalletTaSystemErrorException {
        TATrustedStorageInfo tsInfo = getTATrustedStorageInfoForSet("setCountryCode");
        tsInfo.setCountryCode(countryCode);
        setTATrustedStorageInfo(tsInfo, "setCountryCode");
    }

    public String getCountryCode() throws WalletTaException.WalletTaSystemErrorException {
        return getTATrustedStorageInfo("getCountryCode").getCountryCode();
    }

    private TATrustedStorageInfo getTATrustedStorageInfo(String functionName) throws WalletTaException.WalletTaSystemErrorException {
        String tsInfoStr;
        WalletCa.OutputParam tsInfoBytes = WalletCa.newOutputParam();
        String message = " WalletTaManager:: " + functionName;
        try {
            long result = WalletCa.getTrustedStorageInfoImpl(tsInfoBytes);
            if (0 == result) {
                LogC.i(message + " get storage info successfully.", false);
                try {
                    LogC.i("trustedStorageInfoStr = " + tsInfoStr, false);
                    return new TATrustedStorageInfo(tsInfoStr);
                } catch (UnsupportedEncodingException e) {
                    LogC.e(message + ", desc=" + e.getMessage(), false);
                    LogC.i(message + " transfer to bytes meet UnsupportedEncodingException with ErrCode=4294770706", false);
                    throw new WalletTaException().newWalletTaSystemErrorException(4294770706L);
                }
            } else {
                LogC.e(message + " get storage info failed, result = " + Long.toHexString(result), false);
                throw new WalletTaException().newWalletTaSystemErrorException(result);
            }
        } catch (UnsatisfiedLinkError e2) {
            String message2 = message + " get trusted storage info meet UnsatisfiedLinkError with ErrCode= 4294770704";
            LogC.i(message2 + ", desc=" + e2.getMessage(), false);
            throw new WalletTaException().newWalletTaSystemErrorException(4294770704L);
        }
    }

    private TATrustedStorageInfo getTATrustedStorageInfoForSet(String functionName) throws WalletTaException.WalletTaSystemErrorException {
        String tsInfoStr;
        WalletCa.OutputParam tsInfoBytes = WalletCa.newOutputParam();
        String message = " WalletTaManager:" + functionName;
        try {
            long result = WalletCa.getTrustedStorageInfoImpl(tsInfoBytes);
            if (4294901768L != result) {
                if (0 == result) {
                    message = message + " get trusted storage info success.";
                    LogC.i(message, false);
                } else {
                    LogC.e(message + " get trusted storage info failed, result = " + Long.toHexString(result), false);
                    throw new WalletTaException().newWalletTaSystemErrorException(result);
                }
            }
            if (4294901768L == result) {
                LogC.i(message + " trusted storage info not found before update", false);
                return new TATrustedStorageInfo();
            }
            try {
                LogC.e("trustedStorageInfoStr = " + tsInfoStr, false);
                return new TATrustedStorageInfo(tsInfoStr);
            } catch (UnsupportedEncodingException e) {
                LogC.e(message + ", desc=" + e.getMessage(), false);
                LogC.i(message + " transfer to bytes failed meet UnsupportedEncodingException with ErrCode=4294770706", false);
                throw new WalletTaException().newWalletTaSystemErrorException(4294770706L);
            }
        } catch (UnsatisfiedLinkError e2) {
            String message2 = message + " get trusted storage info meet UnsatisfiedLinkError with errorCode= 4294770704";
            LogC.i(message2 + ",desc: " + e2.getMessage(), false);
            throw new WalletTaException().newWalletTaSystemErrorException(4294770704L);
        }
    }

    private void setTATrustedStorageInfo(TATrustedStorageInfo tsInfo, String functionName) throws WalletTaException.WalletTaSystemErrorException {
        String message = " WalletTaManager:" + functionName;
        try {
            long result = WalletCa.setTrustedStorageInfoImpl(tsInfo.getTaTsInfoJsonStr().getBytes(AES.CHAR_ENCODING));
            if (0 == result) {
                LogC.i(message + " set trusted storage info success.", false);
                return;
            }
            LogC.e(message + " set trusted storage info failed, result = " + Long.toHexString(result), false);
            throw new WalletTaException().newWalletTaSystemErrorException(result);
        } catch (UnsatisfiedLinkError e) {
            String message2 = message + " when set trusted storage info meet UnsatisfiedLinkError with ErrCode=4294770704";
            LogC.i(message2 + ", desc: " + e.getMessage(), false);
            throw new WalletTaException().newWalletTaSystemErrorException(4294770704L);
        } catch (UnsupportedEncodingException e2) {
            String message3 = message + " when set trusted storage info meet UnsupportedEncodingException. Unsupported encoding with errorCode=4294770706";
            LogC.e(message3 + ", desc: " + e2.getMessage(), false);
            throw new WalletTaException().newWalletTaSystemErrorException(4294770706L);
        }
    }

    private WalletTaManager(Context context) {
        this.mContext = getContext(context);
        init(context);
        synchronized (cardInfoListLock) {
            try {
                cardInfoListCache = getCardListFromTa();
            } catch (WalletTaException e) {
                LogX.e("cardInfoListCache init, getCardListFromTa failed, errorCode = " + e.getCode());
            }
        }
    }

    public static void unInitTA() {
        LogC.i("wallet factory unInitTA ", false);
        synchronized (SYNC_LOCK) {
            try {
                LogC.i("wallet factory unInitTA1 ", false);
                long result = WalletCa.walletTaUnInit();
                LogC.i("wallet factory unInitTA2 ", false);
                if (0 != result) {
                    LogC.i("wallet TA unInit failed, result = " + Long.toHexString(result), false);
                }
                instance = null;
                LogC.i("wallet factory unInitTA end ", false);
            } catch (UnsatisfiedLinkError e) {
                LogX.i("unInitTA fail, UnsatisfiedLinkError. Ca lib load error, errorCode= 4294770704");
            } catch (Exception e2) {
                LogX.i("unInitTA fail cause of hit exception , errorInfo is:" + e2.getMessage());
            }
        }
    }

    private void init(Context context) {
        String caLibName;
        LogC.i("init begin to init ta", false);
        LogC.i("WalletTa load library: " + caLibName, false);
        WalletCa.loadLibrary(caLibName);
        try {
            if (0 != WalletCa.walletTaInit("vendor/bin/".getBytes(AES.CHAR_ENCODING))) {
                String message = "wallet TA init failed, result = " + Long.toHexString(result);
                LogC.e(message + ", taPath:" + "vendor/bin/" + ", fail_code" + String.valueOf(result), false);
                return;
            }
            LogC.i("init TA success.", false);
        } catch (UnsatisfiedLinkError e) {
            LogC.e("walletTaInit Native method not found", false);
        } catch (UnsupportedEncodingException e2) {
            LogC.e("taPath getBytes failed, UnsupportedEncodingException", false);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v0, resolved type: java.io.FileOutputStream} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v1, resolved type: byte[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: byte[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v3, resolved type: byte[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v4, resolved type: byte[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: byte[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v6, resolved type: byte[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v7, resolved type: byte[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void copyAssetFileToFiles(Context context, String filePath, String filename) {
        byte[] buffer;
        InputStream is = null;
        FileOutputStream os = null;
        FileOutputStream fileOutputStream = os;
        try {
            AssetManager assets = context.getAssets();
            InputStream is2 = assets.open(filePath + filename);
            buffer = new byte[is2.available()];
            if (-1 == is2.read(buffer)) {
                LogC.e("copyAssetFileToFiles, read file error.", false);
            }
            if (is2 != null) {
                try {
                    is2.close();
                } catch (IOException e) {
                    LogC.e("InputStream close failed", false);
                    buffer = buffer;
                }
            }
        } catch (IOException e2) {
            LogC.e("InputStream open failed", false);
            buffer = fileOutputStream;
            if (is != null) {
                buffer = fileOutputStream;
                is.close();
                buffer = fileOutputStream;
                buffer = fileOutputStream;
            }
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e3) {
                    LogC.e("InputStream close failed", false);
                }
            }
            throw th;
        }
        try {
            File of = new File(context.getFilesDir() + "/" + filename);
            if (!of.createNewFile()) {
                LogC.i("createNewFile,already exsits.", false);
            }
            FileOutputStream os2 = new FileOutputStream(of);
            if (buffer != null) {
                os2.write(buffer);
            }
            try {
                os2.close();
            } catch (IOException e4) {
                LogC.e("FileOutputStream close failed", false);
            }
        } catch (FileNotFoundException e5) {
            LogC.e("FileOutputStream get failed", false);
            if (os != null) {
                os.close();
            }
        } catch (IOException e6) {
            LogC.e("FileOutputStream write failed", false);
            if (os != null) {
                os.close();
            }
        } catch (Throwable th2) {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e7) {
                    LogC.e("FileOutputStream close failed", false);
                }
            }
            throw th2;
        }
    }

    private String getCaLibName(Context context) {
        try {
            String libPath = context.getApplicationInfo().nativeLibraryDir;
            if (TextUtils.isEmpty(libPath)) {
                return HUAWEI_WALLET_CA_DEFAULT;
            }
            File file = new File(libPath + "/" + HUAWEI_WALLET_CA_MATES_SO);
            if (!file.exists() || !file.isFile()) {
                return HUAWEI_WALLET_CA_DEFAULT;
            }
            String model = Build.MODEL;
            return (TextUtils.isEmpty(model) || !matesModel.contains(model.toUpperCase(new Locale(AES.CHAR_ENCODING)))) ? HUAWEI_WALLET_CA_DEFAULT : HUAWEI_WALLET_CA_MATES;
        } catch (SecurityException e) {
            LogC.i("Check ca lib path failed, will use default ca lib.", false);
            return HUAWEI_WALLET_CA_DEFAULT;
        }
    }

    private ArrayList<TACardInfo> getCardListFromTa() throws WalletTaException.WalletTaSystemErrorException {
        String cardInfoStr;
        ArrayList<TACardInfo> cardList = new ArrayList<>();
        WalletCa.OutputParam num = WalletCa.newOutputParam();
        long result = 4294901768L;
        try {
            result = WalletCa.getCardNumImpl(num);
        } catch (UnsatisfiedLinkError e) {
            UnsatisfiedLinkError unsatisfiedLinkError = e;
            if (cardList.size() <= 0) {
                LogC.i("WalletTaManager get getCardListFromTa failed," + e.getMessage(), false);
                throw new WalletTaException().newWalletTaSystemErrorException(4294770704L);
            }
        }
        long j = 0;
        if (0 == result) {
            LogC.i("WalletTaManager getCardListFromTa, get card number success.", false);
        } else if (4294901768L == result) {
            LogC.e("WalletTaManager|getCardListFromTa|wallet TA get card number failed: item not found, result = " + Long.toHexString(result), false);
            return cardList;
        } else {
            String message = "WalletTaManager|getCardListFromTa|wallet TA get card number failed, result = " + Long.toHexString(result);
            LogC.e(message + ", fail_code=" + String.valueOf(result), false);
            if (cardList.size() <= 0) {
                throw new WalletTaException().newWalletTaSystemErrorException(result);
            }
        }
        if (num.intValue == 0) {
            LogC.i("WalletTaManager|getCardListFromTa|getCardNum success, num is 0.", false);
            return cardList;
        }
        WalletCa.OutputParam aidBytes = WalletCa.newOutputParam();
        WalletCa.OutputParam cardInfoBytes = WalletCa.newOutputParam();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < num.intValue) {
                WalletCa.OutputParam type = WalletCa.newOutputParam();
                WalletCa.OutputParam isDefCard = WalletCa.newOutputParam();
                int i3 = i2 + 1;
                WalletCa.OutputParam isActivated = WalletCa.newOutputParam();
                WalletCa.OutputParam isDefCard2 = isDefCard;
                int i4 = i2;
                try {
                    long result2 = WalletCa.getCardByIndexImpl(i3, type, isDefCard, isActivated, aidBytes, cardInfoBytes);
                    if (j == result2) {
                        LogC.i("WalletTaManager getCardListFromTa, get card by index success.", false);
                        try {
                            LogC.d("WalletTaManager|getCardListFromTa|cardInfoStr = " + cardInfoStr, true);
                            StringBuilder sb = new StringBuilder();
                            sb.append("WalletTaManager|getCardListFromTa|getCardListFromTa the card isActivated=");
                            WalletCa.OutputParam isActivated2 = isActivated;
                            sb.append(isActivated2.boolValue);
                            LogC.i(sb.toString(), false);
                            TACardInfo info = new TACardInfo(cardInfoStr);
                            handleTACardInfoUpdate(info);
                            info.setDefaultCard(isDefCard2.boolValue);
                            info.setActivatedStatus(isActivated2.boolValue);
                            cardList.add(info);
                            info.getTaCardInfoJsonStr();
                            i = i4 + 1;
                            j = 0;
                        } catch (UnsupportedEncodingException e2) {
                            WalletCa.OutputParam outputParam = isActivated;
                            WalletCa.OutputParam outputParam2 = isDefCard2;
                            LogC.e("WalletTaManager|getCardListFromTa|cardInfoStr new String failed, UnsupportedEncodingException, " + e2.getMessage(), false);
                            throw new WalletTaException().newWalletTaSystemErrorException(4294770706L);
                        }
                    } else {
                        WalletCa.OutputParam outputParam3 = isActivated;
                        WalletCa.OutputParam outputParam4 = isDefCard2;
                        String message2 = "WalletTaManager|getCardListFromTa|wallet TA get card by index failed, result = " + Long.toHexString(result2);
                        LogC.e(message2 + "fail_code=" + String.valueOf(result2), false);
                        throw new WalletTaException().newWalletTaSystemErrorException(result2);
                    }
                } catch (UnsatisfiedLinkError e3) {
                    WalletCa.OutputParam outputParam5 = isActivated;
                    WalletCa.OutputParam outputParam6 = isDefCard2;
                    UnsatisfiedLinkError unsatisfiedLinkError2 = e3;
                    LogC.i("WalletTaManager getCardByIndexImpl failed," + e3.getMessage(), false);
                    throw new WalletTaException().newWalletTaSystemErrorException(4294770704L);
                }
            } else {
                return cardList;
            }
        }
    }

    private void handleTACardInfoUpdate(TACardInfo taInfo) throws WalletTaException.WalletTaSystemErrorException {
        if (taInfo == null) {
            LogC.d("handleTACardInfoUpdate, taInfo is null!", false);
        } else if (!"A0000003330101020063020000000301".equals(taInfo.getAid())) {
            LogC.d("handleTACardInfoUpdate, not citic card, no need to upgrade.", false);
        } else if (!StringUtil.isEmpty(taInfo.getProductId(), true)) {
            LogC.d("handleTACardInfoUpdate, productId existed, no need to upgrade.", false);
        } else {
            LogC.i("upgrade citic ta info now.", false);
            taInfo.setCardType(3);
            taInfo.setProductId("*_63020000_01");
            taInfo.setIssuerId("63020000");
            try {
                StringBuffer sb = new StringBuffer();
                sb.append("cardGroupType is :");
                sb.append(taInfo.getCardGroupType());
                sb.append(",");
                sb.append("getAid is :");
                sb.append(taInfo.getAid());
                sb.append(",");
                sb.append("TaCardInfoJsonStr is :");
                sb.append(taInfo.getTaCardInfoJsonStr());
                LogC.i("updateCard " + sb.toString(), false);
                long result = WalletCa.updateCardImpl(taInfo.getCardGroupType(), taInfo.getAid().getBytes(AES.CHAR_ENCODING), taInfo.getTaCardInfoJsonStr().getBytes(AES.CHAR_ENCODING));
                if (0 != result) {
                    String message = "wallet TA update card product ID and issued ID failed, result = " + Long.toHexString(result);
                    LogC.e(message + ", cardGroupType=" + String.valueOf(taInfo.getCardGroupType()) + ", cardAid" + taInfo.getAid() + ", fail_code=" + String.valueOf(result), false);
                    if (4294770698L == result) {
                        LogC.i("updateCardProidAndIssuerId failed, card is not exsit", false);
                    } else {
                        LogC.e("updateCardProidAndIssuerId failed, result = " + Long.toHexString(result), false);
                    }
                    throw new WalletTaException().newWalletTaSystemErrorException(result);
                }
                LogC.i("handleTACardInfoUpdate success.", false);
            } catch (UnsatisfiedLinkError e) {
                LogC.i("handleTACardInfoUpdate fail, UnsatisfiedLinkError. Ca lib load error, errorCode= 4294770704", false);
                throw new WalletTaException().newWalletTaSystemErrorException(4294770704L);
            } catch (UnsupportedEncodingException e2) {
                LogC.e("updateCardProidAndIssuerId getBytes failed, UnsupportedEncodingException", false);
                LogC.i("handleTACardInfoUpdate fail, UnsupportedEncodingException. Unsupported encoding, errorCode= 4294770706", false);
                throw new WalletTaException().newWalletTaSystemErrorException(4294770706L);
            }
        }
    }

    public ArrayList<TACardInfo> getResetAccessCardList() {
        synchronized (cardInfoListLock) {
            ArrayList<TACardInfo> cardInfoList = new ArrayList<>();
            if (cardInfoListCache == null) {
                return null;
            }
            if (cardInfoListCache.size() == 0) {
                return cardInfoList;
            }
            Iterator<TACardInfo> it = cardInfoListCache.iterator();
            while (it.hasNext()) {
                TACardInfo cardInfo = it.next();
                if (cardInfo.getCardType() == 13) {
                    cardInfoList.add(cardInfo.clone());
                }
            }
            return cardInfoList;
        }
    }

    public void removeCardByAid(String aid) throws WalletTaException.WalletTaCardNotExistException, WalletTaException.WalletTaSystemErrorException {
        synchronized (cardInfoListLock) {
            if (cardInfoListCache == null) {
                cardInfoListCache = getCardListFromTa();
            }
            TACardInfo cardInfo = null;
            Iterator<TACardInfo> it = cardInfoListCache.iterator();
            while (it.hasNext()) {
                TACardInfo info = it.next();
                if (info.getAid().equals(aid)) {
                    cardInfo = info;
                }
            }
            long result = getRemoveCardResult(cardInfo);
            if (0 != result) {
                if (cardInfo != null) {
                    LogX.e("removeCardByAid: wallet TA remove card failed, result=" + result + ", cardAid=" + cardInfo.getAid() + ", cardGroupType=" + cardInfo.getCardGroupType() + ", fail_reason=" + result, false);
                }
                if (4294770698L == result) {
                    LogX.i("removeCard failed , card is not exist");
                    throw new WalletTaException().newWalletTaCardNotExistException();
                }
                LogX.e("removeCard failed, result = " + Long.toHexString(result));
                throw new WalletTaException().newWalletTaSystemErrorException(result);
            }
            LogX.i("removeCardByAid success.");
            LogC.i("TA remove card success:", false);
            Iterator<TACardInfo> it2 = cardInfoListCache.iterator();
            while (it2.hasNext()) {
                TACardInfo taCardInfo = it2.next();
                if (cardInfo != null && taCardInfo.getCardGroupType() == cardInfo.getCardGroupType() && taCardInfo.getAid().equals(cardInfo.getAid())) {
                    cardInfoListCache.remove(taCardInfo);
                    return;
                }
            }
        }
    }

    public void updateCardStatusByAid(String aid, int cardStatus) throws WalletTaException.WalletTaCardNotExistException, WalletTaException.WalletTaSystemErrorException {
        synchronized (cardInfoListLock) {
            if (cardInfoListCache == null) {
                cardInfoListCache = getCardListFromTa();
            }
            TACardInfo cardInfoCache = getCardInfoFromCacheByAid(aid);
            if (cardInfoCache != null) {
                TACardInfo cardInfo = cardInfoCache.clone();
                if (cardStatus == NOT_UPDATE_CARD_STATUS) {
                    LogX.i("updateCardStatusByAid skip for status=-10000");
                    return;
                }
                cardInfo.setCardStatus(cardStatus);
                cardInfo.setStatusUpdateTime(System.currentTimeMillis());
                updateCard(cardInfo);
                cardInfoCache.setCardStatus(cardStatus);
                return;
            }
            LogX.i("updateCardStatusByAid failed, aid(" + aid + ") is not exist");
            throw new WalletTaException().newWalletTaCardNotExistException();
        }
    }

    public void addCard(TACardInfo taCardInfo) throws WalletTaException.WalletTaCardAlreadyExistException, WalletTaException.WalletTaCardNumReachMaxException, WalletTaException.WalletTaBadParammeterException, WalletTaException.WalletTaSystemErrorException {
        if (taCardInfo == null || taCardInfo.getAid() == null || taCardInfo.getTaCardInfoJsonStr() == null) {
            LogX.i("addCard failed, bad parameter");
            throw new WalletTaException().newWalletTaBadParammeterException();
        }
        taCardInfo.setStatusUpdateTime(System.currentTimeMillis());
        try {
            long result = WalletCa.addCardImpl(taCardInfo.getCardGroupType(), taCardInfo.getAid().getBytes(AES.CHAR_ENCODING), taCardInfo.getTaCardInfoJsonStr().getBytes(AES.CHAR_ENCODING));
            if (0 != result) {
                Map<String, String> params = new HashMap<>();
                params.put("fail_reason", "wallet TA add card failed, result = " + Long.toHexString(result));
                params.put("cardGroupType", String.valueOf(taCardInfo.getCardGroupType()));
                params.put("cardAid", taCardInfo.getAid());
                params.put("fail_code", String.valueOf(result));
                if (4294770699L == result) {
                    LogX.i("addCard failed, card already exist");
                    throw new WalletTaException().newWalletTaCardAlreadyExistException();
                } else if (4294770700L == result) {
                    LogX.i("addCard failed, card reach max");
                    throw new WalletTaException().newWalletTaCardNumReachMaxException();
                } else {
                    LogX.e("addCard failed, result = " + Long.toHexString(result));
                    throw new WalletTaException().newWalletTaSystemErrorException(result);
                }
            } else {
                LogX.i("WalletTaManager addCard success.");
                LogX.i("AddCard success :" + taCardInfo.getAid());
                synchronized (cardInfoListLock) {
                    if (cardInfoListCache == null) {
                        cardInfoListCache = getCardListFromTa();
                    } else {
                        cardInfoListCache.add(taCardInfo);
                    }
                }
            }
        } catch (UnsatisfiedLinkError e) {
            LogX.i("WalletTaManager addCard fail, UnsatisfiedLinkError. Ca lib load error, errorCode= 4294770704, desc: " + e.getMessage());
            throw new WalletTaException().newWalletTaSystemErrorException(4294770704L);
        } catch (UnsupportedEncodingException e2) {
            LogX.i("WalletTaManager addCard fail, UnsupportedEncodingException. Unsupported encoding, errorCode= 4294770706, desc: " + e2.getMessage());
            throw new WalletTaException().newWalletTaSystemErrorException(4294770706L);
        }
    }

    public ArrayList<TACardInfo> getBlankCardList() {
        synchronized (cardInfoListLock) {
            ArrayList<TACardInfo> cardInfoList = new ArrayList<>();
            if (cardInfoListCache == null) {
                return null;
            }
            if (cardInfoListCache.size() == 0) {
                return cardInfoList;
            }
            Iterator<TACardInfo> it = cardInfoListCache.iterator();
            while (it.hasNext()) {
                TACardInfo cardInfo = it.next();
                if (cardInfo.getCardType() == 14) {
                    cardInfoList.add(cardInfo.clone());
                }
            }
            return cardInfoList;
        }
    }
}
