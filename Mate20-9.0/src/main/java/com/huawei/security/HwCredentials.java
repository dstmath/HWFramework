package com.huawei.security;

public class HwCredentials {
    public static final String CA_CERTIFICATE = "CACERT_";
    public static final String CERTIFICATE_CHAIN = "CERTCHAIN_";
    public static final String EXTENSION_CER = ".cer";
    public static final String EXTENSION_CRT = ".crt";
    public static final String EXTENSION_P12 = ".p12";
    public static final String EXTENSION_PFX = ".pfx";
    public static final String EXTRA_CA_CERTIFICATES_DATA = "ca_certificates_data";
    public static final String EXTRA_CA_CERTIFICATES_NAME = "ca_certificates_name";
    public static final String EXTRA_INSTALL_AS_UID = "install_as_uid";
    public static final String EXTRA_PRIVATE_KEY = "PKEY";
    public static final String EXTRA_PUBLIC_KEY = "KEY";
    public static final String EXTRA_USER_CERTIFICATE_DATA = "user_certificate_data";
    public static final String EXTRA_USER_CERTIFICATE_NAME = "user_certificate_name";
    public static final String EXTRA_USER_PRIVATE_KEY_DATA = "user_private_key_data";
    public static final String EXTRA_USER_PRIVATE_KEY_NAME = "user_private_key_name";
    public static final String EXTRA_WAPI_AS_CERTIFICATES_DATA = "wapi_ca_certificates_data";
    public static final String EXTRA_WAPI_AS_CERTIFICATES_NAME = "wapi_ca_certificates_name";
    public static final String EXTRA_WAPI_USER_CERTIFICATES_DATA = "wapi_user_certificate_data";
    public static final String EXTRA_WAPI_USER_CERTIFICATES_NAME = "wapi_user_certificate_name";
    private static final String TAG = "HwCredentials";
    public static final String USER_CERTIFICATE = "USRCERT_";
    public static final String USER_PRIVATE_KEY = "USRPKEY_";
    public static final String USER_SECRET_KEY = "USRSKEY_";
    public static final String WAPI_AS_CERTIFICATE = "WAPIAS_";
    public static final String WAPI_USER_CERTIFICATE = "WAPIUSR_";

    public static boolean deleteAllTypesForAlias(HwKeystoreManager keystore, String alias) {
        return deleteAllTypesForAlias(keystore, alias, -1);
    }

    public static boolean deleteAllTypesForAlias(HwKeystoreManager keystore, String alias, int uid) {
        return deletePrivateKeyTypeForAlias(keystore, alias, uid) & deleteCertificateTypesForAlias(keystore, alias, uid);
    }

    public static boolean deleteCertificateTypesForAlias(HwKeystoreManager keystore, String alias) {
        return deleteCertificateTypesForAlias(keystore, alias, -1);
    }

    public static boolean deleteCertificateTypesForAlias(HwKeystoreManager keystore, String alias, int uid) {
        return keystore.delete(CERTIFICATE_CHAIN + alias, uid);
    }

    static boolean deletePrivateKeyTypeForAlias(HwKeystoreManager keystore, String alias) {
        return deletePrivateKeyTypeForAlias(keystore, alias, -1);
    }

    static boolean deletePrivateKeyTypeForAlias(HwKeystoreManager keystore, String alias, int uid) {
        return keystore.delete(USER_PRIVATE_KEY + alias, uid);
    }
}
