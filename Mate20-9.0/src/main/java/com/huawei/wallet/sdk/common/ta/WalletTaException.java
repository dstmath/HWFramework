package com.huawei.wallet.sdk.common.ta;

public class WalletTaException extends Exception {
    protected static final long WALLET_TA_ERR_ACCESS_DENIED = 4294901761L;
    protected static final long WALLET_TA_ERR_ACCOUNT_MISMATCH = 4294770693L;
    protected static final long WALLET_TA_ERR_ACCOUNT_NOT_EXIST = 4294770704L;
    protected static final long WALLET_TA_ERR_APDU_APP_UNEXIST_OR_NON_ACTIVATABLE = 4294770695L;
    protected static final long WALLET_TA_ERR_APDU_CONFLICT_APPLICATION = 4294770696L;
    protected static final long WALLET_TA_ERR_APDU_CONFLICT_PROTOCOL_PARAM = 4294770697L;
    protected static final long WALLET_TA_ERR_APDU_FAIL_GENERIC = 4294770703L;
    protected static final long WALLET_TA_ERR_APDU_WRONG_LENGTH_IN_LC = 4294770702L;
    protected static final long WALLET_TA_ERR_BAD_PARAMETERS = 4294901766L;
    protected static final long WALLET_TA_ERR_CARD_ALREADY_EXIST = 4294770699L;
    protected static final long WALLET_TA_ERR_CARD_NOT_EXIST = 4294770698L;
    protected static final long WALLET_TA_ERR_CARD_NUM_REACH_MAX = 4294770700L;
    protected static final long WALLET_TA_ERR_CA_LIB_LOAD_ERROR = 4294770704L;
    protected static final long WALLET_TA_ERR_DEFAULT_CARD_NOT_EXIST = 4294770707L;
    protected static final long WALLET_TA_ERR_FID_MISMATCHING = 4294770692L;
    protected static final long WALLET_TA_ERR_FID_NOT_EXIST = 4294770690L;
    protected static final long WALLET_TA_ERR_GENERIC = 4294901760L;
    protected static final long WALLET_TA_ERR_INVALID_CMD = 1;
    protected static final long WALLET_TA_ERR_ITEM_NOT_FOUND = 4294901768L;
    protected static final long WALLET_TA_ERR_LEN_INCORRECT = 4294770694L;
    protected static final long WALLET_TA_ERR_OP_DEFAULT_CARD_REPEATED = 4294770701L;
    protected static final long WALLET_TA_ERR_REQUEST_TIMEOUT = 4294914049L;
    protected static final long WALLET_TA_ERR_SHORT_BUFFER = 4294901776L;
    protected static final long WALLET_TA_ERR_STORAGE_NO_SPACE = 4294914113L;
    protected static final long WALLET_TA_ERR_SYSTEM_BUSY = 4294901773L;
    protected static final long WALLET_TA_ERR_TARGET_DEAD = 4294914084L;
    protected static final long WALLET_TA_ERR_TEE_ERROR_OUT_OF_MEMORY = 4294901772L;
    protected static final long WALLET_TA_ERR_TEE_UNINITED = 4294770689L;
    protected static final long WALLET_TA_ERR_TRUSTED_APP_LOAD_ERROR = 13;
    protected static final long WALLET_TA_ERR_UNSUPPORTED_ENCODING = 4294770706L;
    protected static final long WALLET_TA_SUCCESS = 0;
    private long code;

    public static class WalletTaAccountNotExistException extends WalletTaException {
    }

    public static class WalletTaBadParammeterException extends WalletTaException {
    }

    public static class WalletTaCardAlreadyExistException extends WalletTaException {
    }

    public static class WalletTaCardNotExistException extends WalletTaException {
    }

    public static class WalletTaCardNumReachMaxException extends WalletTaException {
    }

    public static class WalletTaDefaultCardNotExistException extends WalletTaException {
    }

    public static class WalletTaFingerIdMismatchException extends WalletTaException {
    }

    public static class WalletTaFingerIdNotExistException extends WalletTaException {
    }

    public static class WalletTaItemNotExistException extends WalletTaException {
    }

    public static class WalletTaSystemErrorException extends WalletTaException {
        public WalletTaSystemErrorException(long code) {
            super(code);
        }
    }

    public WalletTaException() {
    }

    public WalletTaException(long code2) {
        this.code = code2;
    }

    public long getCode() {
        return this.code;
    }

    public WalletTaSystemErrorException newWalletTaSystemErrorException(long code2) {
        return new WalletTaSystemErrorException(code2);
    }

    public WalletTaFingerIdNotExistException newWalletTaFingerIdNotExistException() {
        return new WalletTaFingerIdNotExistException();
    }

    public WalletTaAccountNotExistException newWalletTaAccountNotExistException() {
        return new WalletTaAccountNotExistException();
    }

    public WalletTaBadParammeterException newWalletTaBadParammeterException() {
        return new WalletTaBadParammeterException();
    }

    public WalletTaDefaultCardNotExistException newWalletTaDefaultCardNotExistException() {
        return new WalletTaDefaultCardNotExistException();
    }

    public WalletTaCardNotExistException newWalletTaCardNotExistException() {
        return new WalletTaCardNotExistException();
    }

    public WalletTaCardAlreadyExistException newWalletTaCardAlreadyExistException() {
        return new WalletTaCardAlreadyExistException();
    }

    public WalletTaCardNumReachMaxException newWalletTaCardNumReachMaxException() {
        return new WalletTaCardNumReachMaxException();
    }

    public WalletTaFingerIdMismatchException newWalletTaFingerIdMismatchException() {
        return new WalletTaFingerIdMismatchException();
    }

    public WalletTaItemNotExistException newWalletTaItemNotExistException() {
        return new WalletTaItemNotExistException();
    }
}
