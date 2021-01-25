package com.huawei.attestation;

public class HwAttestationStatus {
    public static final int AUTH_OK = 0;
    public static final int CERT_INVAILD_LENGTH = 65535;
    public static final int CERT_MAX_LENGTH = 2048;
    public static final int DEVICE_CERT_STATUS_FAILED = -1;
    public static final int DEVICE_CERT_STATUS_OK = 0;
    public static final int DEVICE_ID_TYPE_EMMC = 1;
    public static final int KEY_FORMAT_ECC_CERT = 2;
    public static final int KEY_FORMAT_RSA_CERT = 1;
    public static final int KEY_INDEX_GENERAL = 2;
    public static final int KEY_INDEX_HWCLOUD = 1;
    public static final int KEY_INDEX_MAX = 3;
    public static final int STATE_ERR_DEVICE_KEY = -2;
    public static final int STATE_ERR_GET_CERT = -8;
    public static final int STATE_ERR_GET_CERT_TYPE = -7;
    public static final int STATE_ERR_GET_PUBKEY = -6;
    public static final int STATE_ERR_INPUT_PARAMETER = -4;
    public static final int STATE_ERR_NO_ATTESTATION_SERVICE = -1;
    public static final int STATE_ERR_PERMISSION_DENIED = -5;
    public static final int STATE_ERR_READ_EMMCID = -3;
    public static final int STATE_ERR_UNKNOW = -10;
    public static final int STATE_OK = 0;
}
