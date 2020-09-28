package com.mediatek.gba;

import java.util.Hashtable;

class GbaCipherSuite {
    static final byte[] CODE_SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA = {0, 17};
    static final byte[] CODE_SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA = {0, 19};
    static final byte[] CODE_SSL_DHE_DSS_WITH_DES_CBC_SHA = {0, 18};
    static final byte[] CODE_SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA = {0, 20};
    static final byte[] CODE_SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA = {0, 22};
    static final byte[] CODE_SSL_DHE_RSA_WITH_DES_CBC_SHA = {0, 21};
    static final byte[] CODE_SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA = {0, 25};
    static final byte[] CODE_SSL_DH_anon_EXPORT_WITH_RC4_40_MD5 = {0, 23};
    static final byte[] CODE_SSL_DH_anon_WITH_3DES_EDE_CBC_SHA = {0, 27};
    static final byte[] CODE_SSL_DH_anon_WITH_DES_CBC_SHA = {0, 26};
    static final byte[] CODE_SSL_DH_anon_WITH_RC4_128_MD5 = {0, 24};
    static final byte[] CODE_SSL_NULL_WITH_NULL_NULL = {0, 0};
    static final byte[] CODE_SSL_RSA_EXPORT_WITH_DES40_CBC_SHA = {0, 8};
    static final byte[] CODE_SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5 = {0, 6};
    static final byte[] CODE_SSL_RSA_EXPORT_WITH_RC4_40_MD5 = {0, 3};
    static final byte[] CODE_SSL_RSA_WITH_3DES_EDE_CBC_SHA = {0, 10};
    static final byte[] CODE_SSL_RSA_WITH_DES_CBC_SHA = {0, 9};
    static final byte[] CODE_SSL_RSA_WITH_NULL_MD5 = {0, 1};
    static final byte[] CODE_SSL_RSA_WITH_NULL_SHA = {0, 2};
    static final byte[] CODE_SSL_RSA_WITH_RC4_128_MD5 = {0, 4};
    static final byte[] CODE_SSL_RSA_WITH_RC4_128_SHA = {0, 5};
    static final byte[] CODE_TLS_DHE_DSS_WITH_AES_128_CBC_SHA = {0, 50};
    static final byte[] CODE_TLS_DHE_DSS_WITH_AES_256_CBC_SHA = {0, 56};
    static final byte[] CODE_TLS_DHE_RSA_WITH_AES_128_CBC_SHA = {0, 51};
    static final byte[] CODE_TLS_DHE_RSA_WITH_AES_256_CBC_SHA = {0, 57};
    static final byte[] CODE_TLS_DH_anon_WITH_AES_128_CBC_SHA = {0, 52};
    static final byte[] CODE_TLS_DH_anon_WITH_AES_256_CBC_SHA = {0, 58};
    static final byte[] CODE_TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA = {-64, 8};
    static final byte[] CODE_TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA = {-64, 9};
    static final byte[] CODE_TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA = {-64, 10};
    static final byte[] CODE_TLS_ECDHE_ECDSA_WITH_NULL_SHA = {-64, 6};
    static final byte[] CODE_TLS_ECDHE_ECDSA_WITH_RC4_128_SHA = {-64, 7};
    static final byte[] CODE_TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA = {-64, 18};
    static final byte[] CODE_TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA = {-64, 19};
    static final byte[] CODE_TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA = {-64, 20};
    static final byte[] CODE_TLS_ECDHE_RSA_WITH_NULL_SHA = {-64, 16};
    static final byte[] CODE_TLS_ECDHE_RSA_WITH_RC4_128_SHA = {-64, 17};
    static final byte[] CODE_TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA = {-64, 3};
    static final byte[] CODE_TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA = {-64, 4};
    static final byte[] CODE_TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA = {-64, 5};
    static final byte[] CODE_TLS_ECDH_ECDSA_WITH_NULL_SHA = {-64, 1};
    static final byte[] CODE_TLS_ECDH_ECDSA_WITH_RC4_128_SHA = {-64, 2};
    static final byte[] CODE_TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA = {-64, 13};
    static final byte[] CODE_TLS_ECDH_RSA_WITH_AES_128_CBC_SHA = {-64, 14};
    static final byte[] CODE_TLS_ECDH_RSA_WITH_AES_256_CBC_SHA = {-64, 15};
    static final byte[] CODE_TLS_ECDH_RSA_WITH_NULL_SHA = {-64, 11};
    static final byte[] CODE_TLS_ECDH_RSA_WITH_RC4_128_SHA = {-64, 12};
    static final byte[] CODE_TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA = {-64, 23};
    static final byte[] CODE_TLS_ECDH_anon_WITH_AES_128_CBC_SHA = {-64, 24};
    static final byte[] CODE_TLS_ECDH_anon_WITH_AES_256_CBC_SHA = {-64, 25};
    static final byte[] CODE_TLS_ECDH_anon_WITH_NULL_SHA = {-64, 21};
    static final byte[] CODE_TLS_ECDH_anon_WITH_RC4_128_SHA = {-64, 22};
    static final byte[] CODE_TLS_RSA_WITH_AES_128_CBC_SHA = {0, 47};
    static final byte[] CODE_TLS_RSA_WITH_AES_256_CBC_SHA = {0, 53};
    static final GbaCipherSuite SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA = new GbaCipherSuite("SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", CODE_SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA);
    static final GbaCipherSuite SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA = new GbaCipherSuite("SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", CODE_SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA);
    static final GbaCipherSuite SSL_DHE_DSS_WITH_DES_CBC_SHA = new GbaCipherSuite("SSL_DHE_DSS_WITH_DES_CBC_SHA", CODE_SSL_DHE_DSS_WITH_DES_CBC_SHA);
    static final GbaCipherSuite SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA = new GbaCipherSuite("SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", CODE_SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA);
    static final GbaCipherSuite SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA = new GbaCipherSuite("SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", CODE_SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA);
    static final GbaCipherSuite SSL_DHE_RSA_WITH_DES_CBC_SHA = new GbaCipherSuite("SSL_DHE_RSA_WITH_DES_CBC_SHA", CODE_SSL_DHE_RSA_WITH_DES_CBC_SHA);
    static final GbaCipherSuite SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA = new GbaCipherSuite("SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA", CODE_SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA);
    static final GbaCipherSuite SSL_DH_anon_EXPORT_WITH_RC4_40_MD5 = new GbaCipherSuite("SSL_DH_anon_EXPORT_WITH_RC4_40_MD5", CODE_SSL_DH_anon_EXPORT_WITH_RC4_40_MD5);
    static final GbaCipherSuite SSL_DH_anon_WITH_3DES_EDE_CBC_SHA = new GbaCipherSuite("SSL_DH_anon_WITH_3DES_EDE_CBC_SHA", CODE_SSL_DH_anon_WITH_3DES_EDE_CBC_SHA);
    static final GbaCipherSuite SSL_DH_anon_WITH_DES_CBC_SHA = new GbaCipherSuite("SSL_DH_anon_WITH_DES_CBC_SHA", CODE_SSL_DH_anon_WITH_DES_CBC_SHA);
    static final GbaCipherSuite SSL_DH_anon_WITH_RC4_128_MD5 = new GbaCipherSuite("SSL_DH_anon_WITH_RC4_128_MD5", CODE_SSL_DH_anon_WITH_RC4_128_MD5);
    static final GbaCipherSuite SSL_NULL_WITH_NULL_NULL = new GbaCipherSuite("SSL_NULL_WITH_NULL_NULL", CODE_SSL_NULL_WITH_NULL_NULL);
    static final GbaCipherSuite SSL_RSA_EXPORT_WITH_DES40_CBC_SHA = new GbaCipherSuite("SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", CODE_SSL_RSA_EXPORT_WITH_DES40_CBC_SHA);
    static final GbaCipherSuite SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5 = new GbaCipherSuite("SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5", CODE_SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5);
    static final GbaCipherSuite SSL_RSA_EXPORT_WITH_RC4_40_MD5 = new GbaCipherSuite("SSL_RSA_EXPORT_WITH_RC4_40_MD5", CODE_SSL_RSA_EXPORT_WITH_RC4_40_MD5);
    static final GbaCipherSuite SSL_RSA_WITH_3DES_EDE_CBC_SHA = new GbaCipherSuite("SSL_RSA_WITH_3DES_EDE_CBC_SHA", CODE_SSL_RSA_WITH_3DES_EDE_CBC_SHA);
    static final GbaCipherSuite SSL_RSA_WITH_DES_CBC_SHA = new GbaCipherSuite("SSL_RSA_WITH_DES_CBC_SHA", CODE_SSL_RSA_WITH_DES_CBC_SHA);
    static final GbaCipherSuite SSL_RSA_WITH_NULL_MD5 = new GbaCipherSuite("SSL_RSA_WITH_NULL_MD5", CODE_SSL_RSA_WITH_NULL_MD5);
    static final GbaCipherSuite SSL_RSA_WITH_NULL_SHA = new GbaCipherSuite("SSL_RSA_WITH_NULL_SHA", CODE_SSL_RSA_WITH_NULL_SHA);
    static final GbaCipherSuite SSL_RSA_WITH_RC4_128_MD5 = new GbaCipherSuite("SSL_RSA_WITH_RC4_128_MD5", CODE_SSL_RSA_WITH_RC4_128_MD5);
    static final GbaCipherSuite SSL_RSA_WITH_RC4_128_SHA = new GbaCipherSuite("SSL_RSA_WITH_RC4_128_SHA", CODE_SSL_RSA_WITH_RC4_128_SHA);
    private static final GbaCipherSuite[] SUITES_BY_CODE_0x00 = {SSL_NULL_WITH_NULL_NULL, SSL_RSA_WITH_NULL_MD5, SSL_RSA_WITH_NULL_SHA, SSL_RSA_EXPORT_WITH_RC4_40_MD5, SSL_RSA_WITH_RC4_128_MD5, SSL_RSA_WITH_RC4_128_SHA, null, null, SSL_RSA_EXPORT_WITH_DES40_CBC_SHA, SSL_RSA_WITH_DES_CBC_SHA, SSL_RSA_WITH_3DES_EDE_CBC_SHA, null, null, null, null, null, null, SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA, SSL_DHE_DSS_WITH_DES_CBC_SHA, SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA, SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA, SSL_DHE_RSA_WITH_DES_CBC_SHA, SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DH_anon_EXPORT_WITH_RC4_40_MD5, SSL_DH_anon_WITH_RC4_128_MD5, SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA, SSL_DH_anon_WITH_DES_CBC_SHA, SSL_DH_anon_WITH_3DES_EDE_CBC_SHA, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, TLS_RSA_WITH_AES_128_CBC_SHA, null, null, TLS_DHE_DSS_WITH_AES_128_CBC_SHA, TLS_DHE_RSA_WITH_AES_128_CBC_SHA, TLS_DH_anon_WITH_AES_128_CBC_SHA, TLS_RSA_WITH_AES_256_CBC_SHA, null, null, TLS_DHE_DSS_WITH_AES_256_CBC_SHA, TLS_DHE_RSA_WITH_AES_256_CBC_SHA, TLS_DH_anon_WITH_AES_256_CBC_SHA};
    private static final GbaCipherSuite[] SUITES_BY_CODE_0xc0 = {null, TLS_ECDH_ECDSA_WITH_NULL_SHA, TLS_ECDH_ECDSA_WITH_RC4_128_SHA, TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA, TLS_ECDHE_ECDSA_WITH_NULL_SHA, TLS_ECDHE_ECDSA_WITH_RC4_128_SHA, TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA, TLS_ECDH_RSA_WITH_NULL_SHA, TLS_ECDH_RSA_WITH_RC4_128_SHA, TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDH_RSA_WITH_AES_128_CBC_SHA, TLS_ECDH_RSA_WITH_AES_256_CBC_SHA, TLS_ECDHE_RSA_WITH_NULL_SHA, TLS_ECDHE_RSA_WITH_RC4_128_SHA, TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA, TLS_ECDH_anon_WITH_NULL_SHA, TLS_ECDH_anon_WITH_RC4_128_SHA, TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA, TLS_ECDH_anon_WITH_AES_128_CBC_SHA, TLS_ECDH_anon_WITH_AES_256_CBC_SHA};
    private static final HttpCipherSuite[] SUITES_BY_OKHTTP = {HttpCipherSuite.TLS_RSA_WITH_NULL_MD5, HttpCipherSuite.TLS_RSA_WITH_NULL_SHA, HttpCipherSuite.TLS_RSA_EXPORT_WITH_RC4_40_MD5, HttpCipherSuite.TLS_RSA_WITH_RC4_128_MD5, HttpCipherSuite.TLS_RSA_WITH_RC4_128_SHA, HttpCipherSuite.TLS_RSA_EXPORT_WITH_DES40_CBC_SHA, HttpCipherSuite.TLS_RSA_WITH_DES_CBC_SHA, HttpCipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA, HttpCipherSuite.TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA, HttpCipherSuite.TLS_DHE_DSS_WITH_DES_CBC_SHA, HttpCipherSuite.TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA, HttpCipherSuite.TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA, HttpCipherSuite.TLS_DHE_RSA_WITH_DES_CBC_SHA, HttpCipherSuite.TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA, HttpCipherSuite.TLS_DH_anon_EXPORT_WITH_RC4_40_MD5, HttpCipherSuite.TLS_DH_anon_WITH_RC4_128_MD5, HttpCipherSuite.TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA, HttpCipherSuite.TLS_DH_anon_WITH_DES_CBC_SHA, HttpCipherSuite.TLS_DH_anon_WITH_3DES_EDE_CBC_SHA, HttpCipherSuite.TLS_KRB5_WITH_DES_CBC_SHA, HttpCipherSuite.TLS_KRB5_WITH_3DES_EDE_CBC_SHA, HttpCipherSuite.TLS_KRB5_WITH_RC4_128_SHA, HttpCipherSuite.TLS_KRB5_WITH_DES_CBC_MD5, HttpCipherSuite.TLS_KRB5_WITH_3DES_EDE_CBC_MD5, HttpCipherSuite.TLS_KRB5_WITH_RC4_128_MD5, HttpCipherSuite.TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA, HttpCipherSuite.TLS_KRB5_EXPORT_WITH_RC4_40_SHA, HttpCipherSuite.TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5, HttpCipherSuite.TLS_KRB5_EXPORT_WITH_RC4_40_MD5, HttpCipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA, HttpCipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA, HttpCipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA, HttpCipherSuite.TLS_DH_anon_WITH_AES_128_CBC_SHA, HttpCipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA, HttpCipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA, HttpCipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA, HttpCipherSuite.TLS_DH_anon_WITH_AES_256_CBC_SHA, HttpCipherSuite.TLS_RSA_WITH_NULL_SHA256, HttpCipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA256, HttpCipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA256, HttpCipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA256, HttpCipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA256, HttpCipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA256, HttpCipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256, HttpCipherSuite.TLS_DH_anon_WITH_AES_128_CBC_SHA256, HttpCipherSuite.TLS_DH_anon_WITH_AES_256_CBC_SHA256, HttpCipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256, HttpCipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384, HttpCipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256, HttpCipherSuite.TLS_DHE_DSS_WITH_AES_128_GCM_SHA256, HttpCipherSuite.TLS_DHE_DSS_WITH_AES_256_GCM_SHA384, HttpCipherSuite.TLS_DH_anon_WITH_AES_128_GCM_SHA256, HttpCipherSuite.TLS_DH_anon_WITH_AES_256_GCM_SHA384, HttpCipherSuite.TLS_EMPTY_RENEGOTIATION_INFO_SCSV, HttpCipherSuite.TLS_ECDH_ECDSA_WITH_NULL_SHA, HttpCipherSuite.TLS_ECDH_ECDSA_WITH_RC4_128_SHA, HttpCipherSuite.TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA, HttpCipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA, HttpCipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA, HttpCipherSuite.TLS_ECDHE_ECDSA_WITH_NULL_SHA, HttpCipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA, HttpCipherSuite.TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA, HttpCipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, HttpCipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA, HttpCipherSuite.TLS_ECDH_RSA_WITH_NULL_SHA, HttpCipherSuite.TLS_ECDH_RSA_WITH_RC4_128_SHA, HttpCipherSuite.TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA, HttpCipherSuite.TLS_ECDH_RSA_WITH_AES_128_CBC_SHA, HttpCipherSuite.TLS_ECDH_RSA_WITH_AES_256_CBC_SHA, HttpCipherSuite.TLS_ECDHE_RSA_WITH_NULL_SHA, HttpCipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA, HttpCipherSuite.TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA, HttpCipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, HttpCipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA, HttpCipherSuite.TLS_ECDH_anon_WITH_NULL_SHA, HttpCipherSuite.TLS_ECDH_anon_WITH_RC4_128_SHA, HttpCipherSuite.TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA, HttpCipherSuite.TLS_ECDH_anon_WITH_AES_128_CBC_SHA, HttpCipherSuite.TLS_ECDH_anon_WITH_AES_256_CBC_SHA, HttpCipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256, HttpCipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384, HttpCipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256, HttpCipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384, HttpCipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256, HttpCipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384, HttpCipherSuite.TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256, HttpCipherSuite.TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384, HttpCipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, HttpCipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384, HttpCipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256, HttpCipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384, HttpCipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, HttpCipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, HttpCipherSuite.TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256, HttpCipherSuite.TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384};
    static final GbaCipherSuite TLS_DHE_DSS_WITH_AES_128_CBC_SHA = new GbaCipherSuite("TLS_DHE_DSS_WITH_AES_128_CBC_SHA", CODE_TLS_DHE_DSS_WITH_AES_128_CBC_SHA);
    static final GbaCipherSuite TLS_DHE_DSS_WITH_AES_256_CBC_SHA = new GbaCipherSuite("TLS_DHE_DSS_WITH_AES_256_CBC_SHA", CODE_TLS_DHE_DSS_WITH_AES_256_CBC_SHA);
    static final GbaCipherSuite TLS_DHE_RSA_WITH_AES_128_CBC_SHA = new GbaCipherSuite("TLS_DHE_RSA_WITH_AES_128_CBC_SHA", CODE_TLS_DHE_RSA_WITH_AES_128_CBC_SHA);
    static final GbaCipherSuite TLS_DHE_RSA_WITH_AES_256_CBC_SHA = new GbaCipherSuite("TLS_DHE_RSA_WITH_AES_256_CBC_SHA", CODE_TLS_DHE_RSA_WITH_AES_256_CBC_SHA);
    static final GbaCipherSuite TLS_DH_anon_WITH_AES_128_CBC_SHA = new GbaCipherSuite("TLS_DH_anon_WITH_AES_128_CBC_SHA", CODE_TLS_DH_anon_WITH_AES_128_CBC_SHA);
    static final GbaCipherSuite TLS_DH_anon_WITH_AES_256_CBC_SHA = new GbaCipherSuite("TLS_DH_anon_WITH_AES_256_CBC_SHA", CODE_TLS_DH_anon_WITH_AES_256_CBC_SHA);
    static final GbaCipherSuite TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA = new GbaCipherSuite("TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA", CODE_TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA);
    static final GbaCipherSuite TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA = new GbaCipherSuite("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", CODE_TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA);
    static final GbaCipherSuite TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA = new GbaCipherSuite("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA", CODE_TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA);
    static final GbaCipherSuite TLS_ECDHE_ECDSA_WITH_NULL_SHA = new GbaCipherSuite("TLS_ECDHE_ECDSA_WITH_NULL_SHA", CODE_TLS_ECDHE_ECDSA_WITH_NULL_SHA);
    static final GbaCipherSuite TLS_ECDHE_ECDSA_WITH_RC4_128_SHA = new GbaCipherSuite("TLS_ECDHE_ECDSA_WITH_RC4_128_SHA", CODE_TLS_ECDHE_ECDSA_WITH_RC4_128_SHA);
    static final GbaCipherSuite TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA = new GbaCipherSuite("TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA", CODE_TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA);
    static final GbaCipherSuite TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA = new GbaCipherSuite("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", CODE_TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA);
    static final GbaCipherSuite TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA = new GbaCipherSuite("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", CODE_TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA);
    static final GbaCipherSuite TLS_ECDHE_RSA_WITH_NULL_SHA = new GbaCipherSuite("TLS_ECDHE_RSA_WITH_NULL_SHA", CODE_TLS_ECDHE_RSA_WITH_NULL_SHA);
    static final GbaCipherSuite TLS_ECDHE_RSA_WITH_RC4_128_SHA = new GbaCipherSuite("TLS_ECDHE_RSA_WITH_RC4_128_SHA", CODE_TLS_ECDHE_RSA_WITH_RC4_128_SHA);
    static final GbaCipherSuite TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA = new GbaCipherSuite("TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA", CODE_TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA);
    static final GbaCipherSuite TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA = new GbaCipherSuite("TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA", CODE_TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA);
    static final GbaCipherSuite TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA = new GbaCipherSuite("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA", CODE_TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA);
    static final GbaCipherSuite TLS_ECDH_ECDSA_WITH_NULL_SHA = new GbaCipherSuite("TLS_ECDH_ECDSA_WITH_NULL_SHA", CODE_TLS_ECDH_ECDSA_WITH_NULL_SHA);
    static final GbaCipherSuite TLS_ECDH_ECDSA_WITH_RC4_128_SHA = new GbaCipherSuite("TLS_ECDH_ECDSA_WITH_RC4_128_SHA", CODE_TLS_ECDH_ECDSA_WITH_RC4_128_SHA);
    static final GbaCipherSuite TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA = new GbaCipherSuite("TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA", CODE_TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA);
    static final GbaCipherSuite TLS_ECDH_RSA_WITH_AES_128_CBC_SHA = new GbaCipherSuite("TLS_ECDH_RSA_WITH_AES_128_CBC_SHA", CODE_TLS_ECDH_RSA_WITH_AES_128_CBC_SHA);
    static final GbaCipherSuite TLS_ECDH_RSA_WITH_AES_256_CBC_SHA = new GbaCipherSuite("TLS_ECDH_RSA_WITH_AES_256_CBC_SHA", CODE_TLS_ECDH_RSA_WITH_AES_256_CBC_SHA);
    static final GbaCipherSuite TLS_ECDH_RSA_WITH_NULL_SHA = new GbaCipherSuite("TLS_ECDH_RSA_WITH_NULL_SHA", CODE_TLS_ECDH_RSA_WITH_NULL_SHA);
    static final GbaCipherSuite TLS_ECDH_RSA_WITH_RC4_128_SHA = new GbaCipherSuite("TLS_ECDH_RSA_WITH_RC4_128_SHA", CODE_TLS_ECDH_RSA_WITH_RC4_128_SHA);
    static final GbaCipherSuite TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA = new GbaCipherSuite("TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA", CODE_TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA);
    static final GbaCipherSuite TLS_ECDH_anon_WITH_AES_128_CBC_SHA = new GbaCipherSuite("TLS_ECDH_anon_WITH_AES_128_CBC_SHA", CODE_TLS_ECDH_anon_WITH_AES_128_CBC_SHA);
    static final GbaCipherSuite TLS_ECDH_anon_WITH_AES_256_CBC_SHA = new GbaCipherSuite("TLS_ECDH_anon_WITH_AES_256_CBC_SHA", CODE_TLS_ECDH_anon_WITH_AES_256_CBC_SHA);
    static final GbaCipherSuite TLS_ECDH_anon_WITH_NULL_SHA = new GbaCipherSuite("TLS_ECDH_anon_WITH_NULL_SHA", CODE_TLS_ECDH_anon_WITH_NULL_SHA);
    static final GbaCipherSuite TLS_ECDH_anon_WITH_RC4_128_SHA = new GbaCipherSuite("TLS_ECDH_anon_WITH_RC4_128_SHA", CODE_TLS_ECDH_anon_WITH_RC4_128_SHA);
    static final GbaCipherSuite TLS_RSA_WITH_AES_128_CBC_SHA = new GbaCipherSuite("TLS_RSA_WITH_AES_128_CBC_SHA", CODE_TLS_RSA_WITH_AES_128_CBC_SHA);
    static final GbaCipherSuite TLS_RSA_WITH_AES_256_CBC_SHA = new GbaCipherSuite("TLS_RSA_WITH_AES_256_CBC_SHA", CODE_TLS_RSA_WITH_AES_256_CBC_SHA);
    private static final Hashtable<String, GbaCipherSuite> mSuiteByName = new Hashtable<>();
    private final byte[] mCipherSuiteCode;
    private final String mCipherSuiteName;

    static {
        registerCipherSuitesByCode(SUITES_BY_CODE_0x00);
        registerCipherSuitesByCode(SUITES_BY_CODE_0xc0);
    }

    private GbaCipherSuite(String name, byte[] code) {
        this.mCipherSuiteName = name;
        this.mCipherSuiteCode = code;
    }

    private static int registerCipherSuitesByCode(GbaCipherSuite[] cipherSuites) {
        for (int i = 0; i < cipherSuites.length; i++) {
            if (!(cipherSuites[i] == SSL_NULL_WITH_NULL_NULL || cipherSuites[i] == null)) {
                mSuiteByName.put(cipherSuites[i].getName(), cipherSuites[i]);
            }
        }
        return 0;
    }

    public static GbaCipherSuite getByName(String name) {
        GbaCipherSuite suite = mSuiteByName.get(name);
        if (suite == null) {
            HttpCipherSuite[] httpCipherSuiteArr = SUITES_BY_OKHTTP;
            for (HttpCipherSuite http : httpCipherSuiteArr) {
                if (http.javaName.equals(name)) {
                    return new GbaCipherSuite(http.javaName, http.mCipherSuiteCode);
                }
            }
        }
        return suite;
    }

    public String getName() {
        return this.mCipherSuiteName;
    }

    public byte[] getCode() {
        return this.mCipherSuiteCode;
    }
}
