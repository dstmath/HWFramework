package com.huawei.wallet.tz;

import android.util.Log;

public class WalletCa {
    private static final String TAG = "WalletCa";
    private static final Object taLock = new Object();

    public static class OutputParam {
        public boolean boolValue;
        public byte[] bytes;
        public int[] intArray;
        public int intValue;
    }

    private static native long addCard(int i, byte[] bArr, byte[] bArr2);

    private static native long applyEnableDisableCard(int i, byte[] bArr);

    public static native long checkUserAccount(byte[] bArr);

    private static native long disablePosTrade(byte[] bArr);

    private static native long enablePosTrade(int i, byte[] bArr);

    private static native long getCardByIndex(int i, OutputParam outputParam, OutputParam outputParam2, OutputParam outputParam3, OutputParam outputParam4, OutputParam outputParam5);

    private static native long getCardNum(OutputParam outputParam);

    private static native long getDefaultCard(int i, OutputParam outputParam);

    private static native long getFingerId(OutputParam outputParam);

    private static native long getSignature(byte[] bArr, OutputParam outputParam);

    private static native long getTrustedStorageInfo(OutputParam outputParam);

    private static native long initCertification(byte[] bArr);

    private static native long queryCertification(OutputParam outputParam, OutputParam outputParam2, OutputParam outputParam3);

    private static native long removeCard(int i, byte[] bArr);

    private static native long removeDefaultCard(int i, byte[] bArr);

    private static native long removeFingerId();

    private static native long removeUserAccount();

    private static native long setCardActiveStatus(int i, byte[] bArr);

    private static native long setDefaultCard(int i, byte[] bArr);

    private static native long setFingerId(int i);

    private static native long setTrustedStorageInfo(byte[] bArr);

    private static native long setUserAccount(byte[] bArr);

    private static native long updateCard(int i, byte[] bArr, byte[] bArr2);

    public static native long walletTaInitImpl(byte[] bArr);

    public static native long walletTaUnInitImpl();

    public static boolean loadLibrary(String name) {
        try {
            System.loadLibrary(name);
            Log.i(TAG, "load huawei_wallet_ca success");
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "load huawei_wallet_ca error", e);
            return false;
        }
    }

    public static long walletTaInit(byte[] taPath) {
        long walletTaInitImpl;
        synchronized (taLock) {
            walletTaInitImpl = walletTaInitImpl(taPath);
        }
        return walletTaInitImpl;
    }

    public static long walletTaUnInit() {
        long walletTaUnInitImpl;
        synchronized (taLock) {
            walletTaUnInitImpl = walletTaUnInitImpl();
        }
        return walletTaUnInitImpl;
    }

    public static long setFingerIdImpl(int fingerId) {
        long fingerId2;
        synchronized (taLock) {
            fingerId2 = setFingerId(fingerId);
        }
        return fingerId2;
    }

    public static long getFingerIdImpl(OutputParam fingerId) {
        long fingerId2;
        synchronized (taLock) {
            fingerId2 = getFingerId(fingerId);
        }
        return fingerId2;
    }

    public static long removeFingerIdImpl() {
        long removeFingerId;
        synchronized (taLock) {
            removeFingerId = removeFingerId();
        }
        return removeFingerId;
    }

    public static long setDefaultCardImpl(int cardType, byte[] aid) {
        long defaultCard;
        synchronized (taLock) {
            defaultCard = setDefaultCard(cardType, aid);
        }
        return defaultCard;
    }

    public static long getDefaultCardImpl(int cardType, OutputParam aidBytes) {
        long defaultCard;
        synchronized (taLock) {
            defaultCard = getDefaultCard(cardType, aidBytes);
        }
        return defaultCard;
    }

    public static long removeDefaultCardImpl(int cardType, byte[] aid) {
        long removeDefaultCard;
        synchronized (taLock) {
            removeDefaultCard = removeDefaultCard(cardType, aid);
        }
        return removeDefaultCard;
    }

    public static long enablePosTradeImpl(int fingerId, byte[] aid) {
        long enablePosTrade;
        synchronized (taLock) {
            enablePosTrade = enablePosTrade(fingerId, aid);
        }
        return enablePosTrade;
    }

    public static long applyEnableDisableCardImpl(int type, byte[] aid) {
        long applyEnableDisableCard;
        synchronized (taLock) {
            applyEnableDisableCard = applyEnableDisableCard(type, aid);
        }
        return applyEnableDisableCard;
    }

    public static long setCardActiveStatusImpl(int flag, byte[] aid) {
        long cardActiveStatus;
        synchronized (taLock) {
            cardActiveStatus = setCardActiveStatus(flag, aid);
        }
        return cardActiveStatus;
    }

    public static long disablePosTradeImpl(byte[] aid) {
        long disablePosTrade;
        synchronized (taLock) {
            disablePosTrade = disablePosTrade(aid);
        }
        return disablePosTrade;
    }

    public static long setUserAccountImpl(byte[] account) {
        long userAccount;
        synchronized (taLock) {
            userAccount = setUserAccount(account);
        }
        return userAccount;
    }

    public static long checkUserAccountImpl(byte[] account) {
        long result;
        synchronized (taLock) {
            result = checkUserAccount(account);
            Log.i(TAG, "checkUserAccountImpl result = " + result);
        }
        return result;
    }

    public static long removeUserAccountImpl() {
        long removeUserAccount;
        synchronized (taLock) {
            removeUserAccount = removeUserAccount();
        }
        return removeUserAccount;
    }

    public static long addCardImpl(int cardType, byte[] aid, byte[] cardInfo) {
        long addCard;
        synchronized (taLock) {
            addCard = addCard(cardType, aid, cardInfo);
        }
        return addCard;
    }

    public static long updateCardImpl(int cardType, byte[] aid, byte[] cardInfo) {
        long updateCard;
        synchronized (taLock) {
            updateCard = updateCard(cardType, aid, cardInfo);
        }
        return updateCard;
    }

    public static long removeCardImpl(int cardType, byte[] aid) {
        long removeCard;
        synchronized (taLock) {
            removeCard = removeCard(cardType, aid);
        }
        return removeCard;
    }

    public static long getCardNumImpl(OutputParam cardNum) {
        long cardNum2;
        synchronized (taLock) {
            cardNum2 = getCardNum(cardNum);
        }
        return cardNum2;
    }

    public static long getCardByIndexImpl(int index, OutputParam cardType, OutputParam isDefCard, OutputParam isActivated, OutputParam aidBytes, OutputParam cardInfoBytes) {
        long cardByIndex;
        synchronized (taLock) {
            cardByIndex = getCardByIndex(index, cardType, isDefCard, isActivated, aidBytes, cardInfoBytes);
        }
        return cardByIndex;
    }

    public static long getTrustedStorageInfoImpl(OutputParam tsInfoBytes) {
        long trustedStorageInfo;
        synchronized (taLock) {
            trustedStorageInfo = getTrustedStorageInfo(tsInfoBytes);
        }
        return trustedStorageInfo;
    }

    public static long setTrustedStorageInfoImpl(byte[] tsInfo) {
        long trustedStorageInfo;
        synchronized (taLock) {
            trustedStorageInfo = setTrustedStorageInfo(tsInfo);
        }
        return trustedStorageInfo;
    }

    public static long initCertificationImpl(byte[] authSignRequest) {
        long initCertification;
        synchronized (taLock) {
            initCertification = initCertification(authSignRequest);
        }
        return initCertification;
    }

    public static long queryCertificationImpl(OutputParam deviceCert, OutputParam businessCert, OutputParam authSignResult) {
        long queryCertification;
        synchronized (taLock) {
            queryCertification = queryCertification(deviceCert, businessCert, authSignResult);
        }
        return queryCertification;
    }

    public static long getSignatureImpl(byte[] signatureReq, OutputParam signatureRes) {
        long signature;
        synchronized (taLock) {
            signature = getSignature(signatureReq, signatureRes);
        }
        return signature;
    }

    public static OutputParam newOutputParam() {
        return new OutputParam();
    }
}
