package com.android.org.conscrypt;

final class NativeConstants {
    static final int EVP_PKEY_EC = 408;
    static final int EVP_PKEY_RSA = 6;
    static final int EXFLAG_CA = 16;
    static final int EXFLAG_CRITICAL = 512;
    static final int RSA_NO_PADDING = 3;
    static final int RSA_PKCS1_OAEP_PADDING = 4;
    static final int RSA_PKCS1_PADDING = 1;
    static final int RSA_PKCS1_PSS_PADDING = 6;
    static final int SSL3_RT_ALERT = 21;
    static final int SSL3_RT_APPLICATION_DATA = 23;
    static final int SSL3_RT_CHANGE_CIPHER_SPEC = 20;
    static final int SSL3_RT_HANDSHAKE = 22;
    static final int SSL3_RT_HEADER_LENGTH = 5;
    static final int SSL3_RT_MAX_PACKET_SIZE = 16709;
    static final int SSL3_RT_MAX_PLAIN_LENGTH = 16384;
    static final int SSL_CB_HANDSHAKE_DONE = 32;
    static final int SSL_CB_HANDSHAKE_START = 16;
    static final int SSL_ERROR_NONE = 0;
    static final int SSL_ERROR_WANT_READ = 2;
    static final int SSL_ERROR_WANT_WRITE = 3;
    static final int SSL_ERROR_ZERO_RETURN = 6;
    static final int SSL_MODE_CBC_RECORD_SPLITTING = 256;
    static final int SSL_MODE_ENABLE_FALSE_START = 128;
    static final int SSL_MODE_SEND_FALLBACK_SCSV = 1024;
    static final int SSL_OP_CIPHER_SERVER_PREFERENCE = 4194304;
    static final int SSL_OP_NO_SSLv3 = 33554432;
    static final int SSL_OP_NO_TICKET = 16384;
    static final int SSL_OP_NO_TLSv1 = 67108864;
    static final int SSL_OP_NO_TLSv1_1 = 268435456;
    static final int SSL_OP_NO_TLSv1_2 = 134217728;
    static final int SSL_RECEIVED_SHUTDOWN = 2;
    static final int SSL_SENT_SHUTDOWN = 1;
    static final int SSL_VERIFY_FAIL_IF_NO_PEER_CERT = 2;
    static final int SSL_VERIFY_NONE = 0;
    static final int SSL_VERIFY_PEER = 1;
    static final int TLS_CT_ECDSA_SIGN = 64;
    static final int TLS_CT_RSA_SIGN = 1;

    NativeConstants() {
    }
}
