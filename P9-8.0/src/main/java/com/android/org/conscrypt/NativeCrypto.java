package com.android.org.conscrypt;

import com.android.org.conscrypt.EvpMdRef.MD5;
import com.android.org.conscrypt.NativeRef.EC_GROUP;
import com.android.org.conscrypt.NativeRef.EC_POINT;
import com.android.org.conscrypt.NativeRef.EVP_CIPHER_CTX;
import com.android.org.conscrypt.NativeRef.EVP_MD_CTX;
import com.android.org.conscrypt.NativeRef.EVP_PKEY;
import com.android.org.conscrypt.NativeRef.EVP_PKEY_CTX;
import com.android.org.conscrypt.NativeRef.HMAC_CTX;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.nio.Buffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.net.ssl.SSLException;
import javax.security.auth.x500.X500Principal;

public final class NativeCrypto {
    public static final String[] DEFAULT_PROTOCOLS = TLSV12_PROTOCOLS;
    static final String[] DEFAULT_PSK_CIPHER_SUITES = new String[]{"TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256", "TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA", "TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA", "TLS_PSK_WITH_AES_128_CBC_SHA", "TLS_PSK_WITH_AES_256_CBC_SHA"};
    static final String[] DEFAULT_X509_CIPHER_SUITES = (EVP_has_aes_hardware() == 1 ? new String[]{"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_RSA_WITH_AES_128_GCM_SHA256", "TLS_RSA_WITH_AES_256_GCM_SHA384", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA"} : new String[]{"TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_RSA_WITH_AES_128_GCM_SHA256", "TLS_RSA_WITH_AES_256_GCM_SHA384", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA"});
    public static final int EXTENSION_TYPE_CRITICAL = 1;
    public static final int EXTENSION_TYPE_NON_CRITICAL = 0;
    public static final int GN_STACK_ISSUER_ALT_NAME = 2;
    public static final int GN_STACK_SUBJECT_ALT_NAME = 1;
    static final String OBSOLETE_PROTOCOL_SSLV3 = "SSLv3";
    public static final Map<String, String> OPENSSL_TO_STANDARD_CIPHER_SUITES = new HashMap();
    public static final int PKCS7_CERTS = 1;
    public static final int PKCS7_CRLS = 2;
    public static final int SSL_VERIFY_FAIL_IF_NO_PEER_CERT = 2;
    public static final int SSL_VERIFY_NONE = 0;
    public static final int SSL_VERIFY_PEER = 1;
    public static final Map<String, String> STANDARD_TO_OPENSSL_CIPHER_SUITES = new LinkedHashMap();
    private static final String[] SUPPORTED_CIPHER_SUITES;
    public static final Set<String> SUPPORTED_CIPHER_SUITES_SET = new HashSet();
    private static final String SUPPORTED_PROTOCOL_TLSV1 = "TLSv1";
    private static final String SUPPORTED_PROTOCOL_TLSV1_1 = "TLSv1.1";
    private static final String SUPPORTED_PROTOCOL_TLSV1_2 = "TLSv1.2";
    public static final String[] TLSV11_PROTOCOLS = new String[]{SUPPORTED_PROTOCOL_TLSV1, SUPPORTED_PROTOCOL_TLSV1_1, SUPPORTED_PROTOCOL_TLSV1_2};
    public static final String[] TLSV12_PROTOCOLS = new String[]{SUPPORTED_PROTOCOL_TLSV1, SUPPORTED_PROTOCOL_TLSV1_1, SUPPORTED_PROTOCOL_TLSV1_2};
    public static final String[] TLSV1_PROTOCOLS = new String[]{SUPPORTED_PROTOCOL_TLSV1, SUPPORTED_PROTOCOL_TLSV1_1, SUPPORTED_PROTOCOL_TLSV1_2};
    public static final String TLS_EMPTY_RENEGOTIATION_INFO_SCSV = "TLS_EMPTY_RENEGOTIATION_INFO_SCSV";
    public static final String TLS_FALLBACK_SCSV = "TLS_FALLBACK_SCSV";

    public interface SSLHandshakeCallbacks {
        void clientCertificateRequested(byte[] bArr, byte[][] bArr2) throws CertificateEncodingException, SSLException;

        int clientPSKKeyRequested(String str, byte[] bArr, byte[] bArr2);

        void onSSLStateChange(int i, int i2);

        int serverPSKKeyRequested(String str, String str2, byte[] bArr);

        void verifyCertificateChain(long[] jArr, String str) throws CertificateException;
    }

    public static native void ASN1_TIME_to_Calendar(long j, Calendar calendar);

    public static native byte[] ASN1_seq_pack_X509(long[] jArr);

    public static native long[] ASN1_seq_unpack_X509_bio(long j);

    public static native void BIO_free_all(long j);

    public static native int BIO_read(long j, byte[] bArr);

    public static native void BIO_write(long j, byte[] bArr, int i, int i2) throws IOException;

    public static native int ECDH_compute_key(byte[] bArr, int i, EVP_PKEY evp_pkey, EVP_PKEY evp_pkey2) throws InvalidKeyException;

    public static native void EC_GROUP_clear_free(long j);

    public static native byte[] EC_GROUP_get_cofactor(EC_GROUP ec_group);

    public static native byte[][] EC_GROUP_get_curve(EC_GROUP ec_group);

    public static native String EC_GROUP_get_curve_name(EC_GROUP ec_group);

    public static native int EC_GROUP_get_degree(EC_GROUP ec_group);

    public static native long EC_GROUP_get_generator(EC_GROUP ec_group);

    public static native byte[] EC_GROUP_get_order(EC_GROUP ec_group);

    public static native long EC_GROUP_new_arbitrary(byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5, byte[] bArr6, int i);

    public static native long EC_GROUP_new_by_curve_name(String str);

    public static native long EC_KEY_generate_key(EC_GROUP ec_group);

    public static native long EC_KEY_get1_group(EVP_PKEY evp_pkey);

    public static native byte[] EC_KEY_get_private_key(EVP_PKEY evp_pkey);

    public static native long EC_KEY_get_public_key(EVP_PKEY evp_pkey);

    public static native void EC_POINT_clear_free(long j);

    public static native byte[][] EC_POINT_get_affine_coordinates(EC_GROUP ec_group, EC_POINT ec_point);

    public static native long EC_POINT_new(EC_GROUP ec_group);

    public static native void EC_POINT_set_affine_coordinates(EC_GROUP ec_group, EC_POINT ec_point, byte[] bArr, byte[] bArr2);

    public static native int ENGINE_SSL_do_handshake(long j, SSLHandshakeCallbacks sSLHandshakeCallbacks);

    public static native int ENGINE_SSL_read_BIO_direct(long j, long j2, long j3, int i, SSLHandshakeCallbacks sSLHandshakeCallbacks) throws IOException;

    public static native int ENGINE_SSL_read_BIO_heap(long j, long j2, byte[] bArr, int i, int i2, SSLHandshakeCallbacks sSLHandshakeCallbacks) throws IOException;

    public static native int ENGINE_SSL_read_direct(long j, long j2, int i, SSLHandshakeCallbacks sSLHandshakeCallbacks) throws IOException;

    public static native int ENGINE_SSL_read_heap(long j, byte[] bArr, int i, int i2, SSLHandshakeCallbacks sSLHandshakeCallbacks) throws IOException;

    public static native void ENGINE_SSL_shutdown(long j, SSLHandshakeCallbacks sSLHandshakeCallbacks) throws IOException;

    public static native int ENGINE_SSL_write_BIO_direct(long j, long j2, long j3, int i, SSLHandshakeCallbacks sSLHandshakeCallbacks) throws IOException;

    public static native int ENGINE_SSL_write_BIO_heap(long j, long j2, byte[] bArr, int i, int i2, SSLHandshakeCallbacks sSLHandshakeCallbacks) throws IOException;

    public static native int ENGINE_SSL_write_direct(long j, long j2, int i, SSLHandshakeCallbacks sSLHandshakeCallbacks) throws IOException;

    public static native int ENGINE_SSL_write_heap(long j, byte[] bArr, int i, int i2, SSLHandshakeCallbacks sSLHandshakeCallbacks) throws IOException;

    public static native long ERR_peek_last_error();

    public static native int EVP_AEAD_CTX_open(long j, byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, byte[] bArr4, int i3, int i4, byte[] bArr5) throws BadPaddingException;

    public static native int EVP_AEAD_CTX_seal(long j, byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, byte[] bArr4, int i3, int i4, byte[] bArr5) throws BadPaddingException;

    public static native int EVP_AEAD_max_overhead(long j);

    public static native int EVP_AEAD_max_tag_len(long j);

    public static native int EVP_AEAD_nonce_length(long j);

    public static native int EVP_CIPHER_CTX_block_size(EVP_CIPHER_CTX evp_cipher_ctx);

    public static native void EVP_CIPHER_CTX_free(long j);

    public static native long EVP_CIPHER_CTX_new();

    public static native void EVP_CIPHER_CTX_set_key_length(EVP_CIPHER_CTX evp_cipher_ctx, int i);

    public static native void EVP_CIPHER_CTX_set_padding(EVP_CIPHER_CTX evp_cipher_ctx, boolean z);

    public static native int EVP_CIPHER_iv_length(long j);

    public static native int EVP_CipherFinal_ex(EVP_CIPHER_CTX evp_cipher_ctx, byte[] bArr, int i) throws BadPaddingException, IllegalBlockSizeException;

    public static native void EVP_CipherInit_ex(EVP_CIPHER_CTX evp_cipher_ctx, long j, byte[] bArr, byte[] bArr2, boolean z);

    public static native int EVP_CipherUpdate(EVP_CIPHER_CTX evp_cipher_ctx, byte[] bArr, int i, byte[] bArr2, int i2, int i3);

    public static native int EVP_DigestFinal_ex(EVP_MD_CTX evp_md_ctx, byte[] bArr, int i);

    public static native int EVP_DigestInit_ex(EVP_MD_CTX evp_md_ctx, long j);

    public static native byte[] EVP_DigestSignFinal(EVP_MD_CTX evp_md_ctx);

    public static native long EVP_DigestSignInit(EVP_MD_CTX evp_md_ctx, long j, EVP_PKEY evp_pkey);

    public static native void EVP_DigestSignUpdate(EVP_MD_CTX evp_md_ctx, byte[] bArr, int i, int i2);

    public static native void EVP_DigestSignUpdateDirect(EVP_MD_CTX evp_md_ctx, long j, int i);

    public static native void EVP_DigestUpdate(EVP_MD_CTX evp_md_ctx, byte[] bArr, int i, int i2);

    public static native void EVP_DigestUpdateDirect(EVP_MD_CTX evp_md_ctx, long j, int i);

    public static native boolean EVP_DigestVerifyFinal(EVP_MD_CTX evp_md_ctx, byte[] bArr, int i, int i2);

    public static native long EVP_DigestVerifyInit(EVP_MD_CTX evp_md_ctx, long j, EVP_PKEY evp_pkey);

    public static native void EVP_DigestVerifyUpdate(EVP_MD_CTX evp_md_ctx, byte[] bArr, int i, int i2);

    public static native void EVP_DigestVerifyUpdateDirect(EVP_MD_CTX evp_md_ctx, long j, int i);

    public static native void EVP_MD_CTX_cleanup(EVP_MD_CTX evp_md_ctx);

    public static native int EVP_MD_CTX_copy_ex(EVP_MD_CTX evp_md_ctx, EVP_MD_CTX evp_md_ctx2);

    public static native long EVP_MD_CTX_create();

    public static native void EVP_MD_CTX_destroy(long j);

    public static native int EVP_MD_block_size(long j);

    public static native int EVP_MD_size(long j);

    public static native void EVP_PKEY_CTX_free(long j);

    public static native void EVP_PKEY_CTX_set_rsa_mgf1_md(long j, long j2) throws InvalidAlgorithmParameterException;

    public static native void EVP_PKEY_CTX_set_rsa_oaep_label(long j, byte[] bArr) throws InvalidAlgorithmParameterException;

    public static native void EVP_PKEY_CTX_set_rsa_oaep_md(long j, long j2) throws InvalidAlgorithmParameterException;

    public static native void EVP_PKEY_CTX_set_rsa_padding(long j, int i) throws InvalidAlgorithmParameterException;

    public static native void EVP_PKEY_CTX_set_rsa_pss_saltlen(long j, int i) throws InvalidAlgorithmParameterException;

    public static native int EVP_PKEY_cmp(EVP_PKEY evp_pkey, EVP_PKEY evp_pkey2);

    public static native int EVP_PKEY_decrypt(EVP_PKEY_CTX evp_pkey_ctx, byte[] bArr, int i, byte[] bArr2, int i2, int i3);

    public static native long EVP_PKEY_decrypt_init(EVP_PKEY evp_pkey);

    public static native int EVP_PKEY_encrypt(EVP_PKEY_CTX evp_pkey_ctx, byte[] bArr, int i, byte[] bArr2, int i2, int i3);

    public static native long EVP_PKEY_encrypt_init(EVP_PKEY evp_pkey);

    public static native void EVP_PKEY_free(long j);

    public static native long EVP_PKEY_new_EC_KEY(EC_GROUP ec_group, EC_POINT ec_point, byte[] bArr);

    public static native long EVP_PKEY_new_RSA(byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5, byte[] bArr6, byte[] bArr7, byte[] bArr8);

    public static native String EVP_PKEY_print_params(EVP_PKEY evp_pkey);

    public static native String EVP_PKEY_print_public(EVP_PKEY evp_pkey);

    public static native int EVP_PKEY_size(EVP_PKEY evp_pkey);

    public static native int EVP_PKEY_type(EVP_PKEY evp_pkey);

    public static native long EVP_aead_aes_128_gcm();

    public static native long EVP_aead_aes_256_gcm();

    public static native long EVP_get_cipherbyname(String str);

    public static native long EVP_get_digestbyname(String str);

    public static native int EVP_has_aes_hardware();

    public static native void HMAC_CTX_free(long j);

    public static native long HMAC_CTX_new();

    public static native byte[] HMAC_Final(HMAC_CTX hmac_ctx);

    public static native void HMAC_Init_ex(HMAC_CTX hmac_ctx, byte[] bArr, long j);

    public static native void HMAC_Update(HMAC_CTX hmac_ctx, byte[] bArr, int i, int i2);

    public static native void HMAC_UpdateDirect(HMAC_CTX hmac_ctx, long j, int i);

    public static native int OBJ_txt2nid(String str);

    public static native String OBJ_txt2nid_longName(String str);

    public static native String OBJ_txt2nid_oid(String str);

    public static native long[] PEM_read_bio_PKCS7(long j, int i);

    public static native long PEM_read_bio_PUBKEY(long j);

    public static native long PEM_read_bio_PrivateKey(long j);

    public static native long PEM_read_bio_X509(long j);

    public static native long PEM_read_bio_X509_CRL(long j);

    public static native void RAND_bytes(byte[] bArr);

    public static native long RSA_generate_key_ex(int i, byte[] bArr);

    public static native int RSA_private_decrypt(int i, byte[] bArr, byte[] bArr2, EVP_PKEY evp_pkey, int i2) throws BadPaddingException, SignatureException;

    public static native int RSA_private_encrypt(int i, byte[] bArr, byte[] bArr2, EVP_PKEY evp_pkey, int i2);

    public static native int RSA_public_decrypt(int i, byte[] bArr, byte[] bArr2, EVP_PKEY evp_pkey, int i2) throws BadPaddingException, SignatureException;

    public static native int RSA_public_encrypt(int i, byte[] bArr, byte[] bArr2, EVP_PKEY evp_pkey, int i2);

    public static native int RSA_size(EVP_PKEY evp_pkey);

    public static native long SSL_BIO_new(long j) throws SSLException;

    public static native String SSL_CIPHER_get_kx_name(long j);

    public static native void SSL_CTX_free(long j);

    public static native long SSL_CTX_new();

    public static native void SSL_CTX_set_session_id_context(long j, byte[] bArr);

    public static native String SSL_SESSION_cipher(long j);

    public static native void SSL_SESSION_free(long j);

    public static native long SSL_SESSION_get_time(long j);

    public static native String SSL_SESSION_get_version(long j);

    public static native byte[] SSL_SESSION_session_id(long j);

    public static native void SSL_accept_renegotiations(long j) throws SSLException;

    public static native void SSL_check_private_key(long j) throws SSLException;

    public static native void SSL_clear_error();

    public static native long SSL_clear_mode(long j, long j2);

    public static native long SSL_clear_options(long j, long j2);

    public static native void SSL_configure_alpn(long j, boolean z, byte[] bArr) throws IOException;

    public static native void SSL_do_handshake(long j, FileDescriptor fileDescriptor, SSLHandshakeCallbacks sSLHandshakeCallbacks, int i) throws SSLException, SocketTimeoutException, CertificateException;

    public static native void SSL_enable_ocsp_stapling(long j);

    public static native void SSL_enable_signed_cert_timestamps(long j);

    public static native void SSL_enable_tls_channel_id(long j) throws SSLException;

    public static native void SSL_free(long j);

    public static native byte[] SSL_get0_alpn_selected(long j);

    public static native long SSL_get0_session(long j);

    public static native long SSL_get1_session(long j);

    public static native long[] SSL_get_certificate(long j);

    public static native long[] SSL_get_ciphers(long j);

    public static native int SSL_get_error(long j, int i);

    public static native String SSL_get_error_string(long j);

    public static native int SSL_get_last_error_number();

    public static native long SSL_get_mode(long j);

    public static native byte[] SSL_get_ocsp_response(long j);

    public static native long SSL_get_options(long j);

    public static native long[] SSL_get_peer_cert_chain(long j);

    public static native String SSL_get_servername(long j);

    public static native int SSL_get_shutdown(long j);

    public static native byte[] SSL_get_signed_cert_timestamp_list(long j);

    public static native byte[] SSL_get_tls_channel_id(long j) throws SSLException;

    public static native void SSL_interrupt(long j);

    public static native int SSL_max_seal_overhead(long j);

    public static native long SSL_new(long j) throws SSLException;

    public static native int SSL_pending_readable_bytes(long j);

    public static native int SSL_pending_written_bytes_in_BIO(long j);

    public static native int SSL_read(long j, FileDescriptor fileDescriptor, SSLHandshakeCallbacks sSLHandshakeCallbacks, byte[] bArr, int i, int i2, int i3) throws IOException;

    public static native void SSL_renegotiate(long j) throws SSLException;

    public static native boolean SSL_session_reused(long j);

    public static native void SSL_set1_tls_channel_id(long j, EVP_PKEY evp_pkey);

    public static native void SSL_set_accept_state(long j);

    public static native void SSL_set_cipher_lists(long j, String[] strArr);

    public static native void SSL_set_client_CA_list(long j, byte[][] bArr);

    public static native void SSL_set_connect_state(long j);

    public static native long SSL_set_mode(long j, long j2);

    public static native void SSL_set_ocsp_response(long j, byte[] bArr);

    public static native long SSL_set_options(long j, long j2);

    public static native void SSL_set_session(long j, long j2) throws SSLException;

    public static native void SSL_set_session_creation_enabled(long j, boolean z) throws SSLException;

    public static native void SSL_set_signed_cert_timestamp_list(long j, byte[] bArr);

    public static native void SSL_set_tlsext_host_name(long j, String str) throws SSLException;

    public static native void SSL_set_verify(long j, int i);

    public static native void SSL_shutdown(long j, FileDescriptor fileDescriptor, SSLHandshakeCallbacks sSLHandshakeCallbacks) throws IOException;

    public static native void SSL_shutdown_BIO(long j, long j2, long j3, SSLHandshakeCallbacks sSLHandshakeCallbacks) throws IOException;

    public static native void SSL_use_PrivateKey(long j, EVP_PKEY evp_pkey);

    public static native void SSL_use_certificate(long j, long[] jArr);

    public static native void SSL_use_psk_identity_hint(long j, String str) throws SSLException;

    public static native void SSL_write(long j, FileDescriptor fileDescriptor, SSLHandshakeCallbacks sSLHandshakeCallbacks, byte[] bArr, int i, int i2, int i3) throws IOException;

    public static native void X509_CRL_free(long j);

    public static native long X509_CRL_get0_by_cert(long j, long j2);

    public static native long X509_CRL_get0_by_serial(long j, byte[] bArr);

    public static native long[] X509_CRL_get_REVOKED(long j);

    public static native long X509_CRL_get_ext(long j, String str);

    public static native byte[] X509_CRL_get_ext_oid(long j, String str);

    public static native byte[] X509_CRL_get_issuer_name(long j);

    public static native long X509_CRL_get_lastUpdate(long j);

    public static native long X509_CRL_get_nextUpdate(long j);

    public static native long X509_CRL_get_version(long j);

    public static native void X509_CRL_print(long j, long j2);

    public static native void X509_CRL_verify(long j, EVP_PKEY evp_pkey);

    public static native String X509_NAME_print_ex(long j, long j2);

    public static native long X509_REVOKED_dup(long j);

    public static native long X509_REVOKED_get_ext(long j, String str);

    public static native byte[] X509_REVOKED_get_ext_oid(long j, String str);

    public static native byte[] X509_REVOKED_get_serialNumber(long j);

    public static native void X509_REVOKED_print(long j, long j2);

    public static native int X509_check_issued(long j, long j2);

    public static native int X509_cmp(long j, long j2);

    public static native void X509_delete_ext(long j, String str);

    public static native long X509_dup(long j);

    public static native void X509_free(long j);

    public static native byte[] X509_get_ext_oid(long j, String str);

    public static native byte[] X509_get_issuer_name(long j);

    public static native long X509_get_notAfter(long j);

    public static native long X509_get_notBefore(long j);

    public static native long X509_get_pubkey(long j) throws NoSuchAlgorithmException, InvalidKeyException;

    public static native byte[] X509_get_serialNumber(long j);

    public static native byte[] X509_get_subject_name(long j);

    public static native long X509_get_version(long j);

    public static native void X509_print_ex(long j, long j2, long j3, long j4);

    public static native int X509_supported_extension(long j);

    public static native void X509_verify(long j, EVP_PKEY evp_pkey) throws BadPaddingException;

    private static native void clinit();

    public static native long create_BIO_InputStream(OpenSSLBIOInputStream openSSLBIOInputStream, boolean z);

    public static native long create_BIO_OutputStream(OutputStream outputStream);

    public static native long[] d2i_PKCS7_bio(long j, int i);

    public static native long d2i_PKCS8_PRIV_KEY_INFO(byte[] bArr);

    public static native long d2i_PUBKEY(byte[] bArr);

    public static native long d2i_SSL_SESSION(byte[] bArr) throws IOException;

    public static native long d2i_X509(byte[] bArr) throws ParsingException;

    public static native long d2i_X509_CRL_bio(long j);

    public static native long d2i_X509_bio(long j);

    public static native long getDirectBufferAddress(Buffer buffer);

    public static native long getECPrivateKeyWrapper(PrivateKey privateKey, EC_GROUP ec_group);

    public static native long getRSAPrivateKeyWrapper(PrivateKey privateKey, byte[] bArr);

    public static native int get_EVP_CIPHER_CTX_buf_len(EVP_CIPHER_CTX evp_cipher_ctx);

    public static native boolean get_EVP_CIPHER_CTX_final_used(EVP_CIPHER_CTX evp_cipher_ctx);

    public static native byte[][] get_RSA_private_params(EVP_PKEY evp_pkey);

    public static native byte[][] get_RSA_public_params(EVP_PKEY evp_pkey);

    public static native String get_SSL_SESSION_tlsext_hostname(long j);

    public static native byte[] get_X509_CRL_crl_enc(long j);

    public static native String[] get_X509_CRL_ext_oids(long j, int i);

    public static native String get_X509_CRL_sig_alg_oid(long j);

    public static native byte[] get_X509_CRL_sig_alg_parameter(long j);

    public static native byte[] get_X509_CRL_signature(long j);

    public static native Object[][] get_X509_GENERAL_NAME_stack(long j, int i) throws CertificateParsingException;

    public static native String[] get_X509_REVOKED_ext_oids(long j, int i);

    public static native long get_X509_REVOKED_revocationDate(long j);

    public static native byte[] get_X509_cert_info_enc(long j);

    public static native int get_X509_ex_flags(long j);

    public static native boolean[] get_X509_ex_kusage(long j);

    public static native int get_X509_ex_pathlen(long j);

    public static native String[] get_X509_ex_xkusage(long j);

    public static native String[] get_X509_ext_oids(long j, int i);

    public static native boolean[] get_X509_issuerUID(long j);

    public static native String get_X509_pubkey_oid(long j);

    public static native String get_X509_sig_alg_oid(long j);

    public static native byte[] get_X509_sig_alg_parameter(long j);

    public static native byte[] get_X509_signature(long j);

    public static native boolean[] get_X509_subjectUID(long j);

    public static native String[] get_cipher_names(String str);

    public static native byte[] get_ocsp_single_extension(byte[] bArr, String str, long j, long j2);

    public static native byte[] i2d_PKCS7(long[] jArr);

    public static native byte[] i2d_PKCS8_PRIV_KEY_INFO(EVP_PKEY evp_pkey);

    public static native byte[] i2d_PUBKEY(EVP_PKEY evp_pkey);

    public static native byte[] i2d_RSAPrivateKey(EVP_PKEY evp_pkey);

    public static native byte[] i2d_RSAPublicKey(EVP_PKEY evp_pkey);

    public static native byte[] i2d_SSL_SESSION(long j);

    public static native byte[] i2d_X509(long j);

    public static native byte[] i2d_X509_CRL(long j);

    public static native byte[] i2d_X509_PUBKEY(long j);

    public static native byte[] i2d_X509_REVOKED(long j);

    public static native void set_SSL_psk_client_callback_enabled(long j, boolean z);

    public static native void set_SSL_psk_server_callback_enabled(long j, boolean z);

    static {
        NativeCryptoJni.init();
        clinit();
        add("ADH-AES128-GCM-SHA256", "TLS_DH_anon_WITH_AES_128_GCM_SHA256");
        add("ADH-AES128-SHA256", "TLS_DH_anon_WITH_AES_128_CBC_SHA256");
        add("ADH-AES128-SHA", "TLS_DH_anon_WITH_AES_128_CBC_SHA");
        add("ADH-AES256-GCM-SHA384", "TLS_DH_anon_WITH_AES_256_GCM_SHA384");
        add("ADH-AES256-SHA256", "TLS_DH_anon_WITH_AES_256_CBC_SHA256");
        add("ADH-AES256-SHA", "TLS_DH_anon_WITH_AES_256_CBC_SHA");
        add("ADH-DES-CBC3-SHA", "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA");
        add("ADH-DES-CBC-SHA", "SSL_DH_anon_WITH_DES_CBC_SHA");
        add("AECDH-AES128-SHA", "TLS_ECDH_anon_WITH_AES_128_CBC_SHA");
        add("AECDH-AES256-SHA", "TLS_ECDH_anon_WITH_AES_256_CBC_SHA");
        add("AECDH-DES-CBC3-SHA", "TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA");
        add("AECDH-NULL-SHA", "TLS_ECDH_anon_WITH_NULL_SHA");
        add("AES128-GCM-SHA256", "TLS_RSA_WITH_AES_128_GCM_SHA256");
        add("AES128-SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA256");
        add("AES128-SHA", "TLS_RSA_WITH_AES_128_CBC_SHA");
        add("AES256-GCM-SHA384", "TLS_RSA_WITH_AES_256_GCM_SHA384");
        add("AES256-SHA256", "TLS_RSA_WITH_AES_256_CBC_SHA256");
        add("AES256-SHA", "TLS_RSA_WITH_AES_256_CBC_SHA");
        add("DES-CBC3-SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA");
        add("DES-CBC-SHA", "SSL_RSA_WITH_DES_CBC_SHA");
        add("ECDH-ECDSA-AES128-GCM-SHA256", "TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256");
        add("ECDH-ECDSA-AES128-SHA256", "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256");
        add("ECDH-ECDSA-AES128-SHA", "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA");
        add("ECDH-ECDSA-AES256-GCM-SHA384", "TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384");
        add("ECDH-ECDSA-AES256-SHA384", "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384");
        add("ECDH-ECDSA-AES256-SHA", "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA");
        add("ECDH-ECDSA-DES-CBC3-SHA", "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA");
        add("ECDH-ECDSA-NULL-SHA", "TLS_ECDH_ECDSA_WITH_NULL_SHA");
        add("ECDHE-ECDSA-AES128-GCM-SHA256", "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256");
        add("ECDHE-ECDSA-AES128-SHA256", "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256");
        add("ECDHE-ECDSA-AES128-SHA", "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA");
        add("ECDHE-ECDSA-AES256-GCM-SHA384", "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384");
        add("ECDHE-ECDSA-AES256-SHA384", "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384");
        add("ECDHE-ECDSA-AES256-SHA", "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA");
        add("ECDHE-ECDSA-CHACHA20-POLY1305", "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305");
        add("ECDHE-ECDSA-CHACHA20-POLY1305", "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256");
        add("ECDHE-ECDSA-DES-CBC3-SHA", "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA");
        add("ECDHE-ECDSA-NULL-SHA", "TLS_ECDHE_ECDSA_WITH_NULL_SHA");
        add("ECDHE-PSK-AES128-CBC-SHA", "TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA");
        add("ECDHE-PSK-AES128-GCM-SHA256", "TLS_ECDHE_PSK_WITH_AES_128_GCM_SHA256");
        add("ECDHE-PSK-AES256-CBC-SHA", "TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA");
        add("ECDHE-PSK-AES256-GCM-SHA384", "TLS_ECDHE_PSK_WITH_AES_256_GCM_SHA384");
        add("ECDHE-PSK-CHACHA20-POLY1305", "TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256");
        add("ECDHE-RSA-AES128-GCM-SHA256", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        add("ECDHE-RSA-AES128-SHA256", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256");
        add("ECDHE-RSA-AES128-SHA", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA");
        add("ECDHE-RSA-AES256-GCM-SHA384", "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");
        add("ECDHE-RSA-AES256-SHA384", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384");
        add("ECDHE-RSA-AES256-SHA", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA");
        add("ECDHE-RSA-CHACHA20-POLY1305", "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305");
        add("ECDHE-RSA-CHACHA20-POLY1305", "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256");
        add("ECDHE-RSA-DES-CBC3-SHA", "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA");
        add("ECDHE-RSA-NULL-SHA", "TLS_ECDHE_RSA_WITH_NULL_SHA");
        add("ECDH-RSA-AES128-GCM-SHA256", "TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256");
        add("ECDH-RSA-AES128-SHA256", "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256");
        add("ECDH-RSA-AES128-SHA", "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA");
        add("ECDH-RSA-AES256-GCM-SHA384", "TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384");
        add("ECDH-RSA-AES256-SHA384", "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384");
        add("ECDH-RSA-AES256-SHA", "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA");
        add("ECDH-RSA-DES-CBC3-SHA", "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA");
        add("ECDH-RSA-NULL-SHA", "TLS_ECDH_RSA_WITH_NULL_SHA");
        add("EXP-ADH-DES-CBC-SHA", "SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA");
        add("EXP-DES-CBC-SHA", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA");
        add("NULL-MD5", "SSL_RSA_WITH_NULL_MD5");
        add("NULL-SHA256", "TLS_RSA_WITH_NULL_SHA256");
        add("NULL-SHA", "SSL_RSA_WITH_NULL_SHA");
        add("PSK-3DES-EDE-CBC-SHA", "TLS_PSK_WITH_3DES_EDE_CBC_SHA");
        add("PSK-AES128-CBC-SHA", "TLS_PSK_WITH_AES_128_CBC_SHA");
        add("PSK-AES256-CBC-SHA", "TLS_PSK_WITH_AES_256_CBC_SHA");
        String[] allOpenSSLCipherSuites = get_cipher_names("ALL:!DHE");
        int size = allOpenSSLCipherSuites.length;
        SUPPORTED_CIPHER_SUITES = new String[(size + 2)];
        for (int i = 0; i < size; i++) {
            String standardName = (String) OPENSSL_TO_STANDARD_CIPHER_SUITES.get(allOpenSSLCipherSuites[i]);
            if (standardName == null) {
                throw new IllegalArgumentException("Unknown cipher suite supported by native code: " + allOpenSSLCipherSuites[i]);
            }
            SUPPORTED_CIPHER_SUITES[i] = standardName;
            SUPPORTED_CIPHER_SUITES_SET.add(standardName);
        }
        SUPPORTED_CIPHER_SUITES[size] = TLS_EMPTY_RENEGOTIATION_INFO_SCSV;
        SUPPORTED_CIPHER_SUITES[size + 1] = TLS_FALLBACK_SCSV;
    }

    public static int X509_NAME_hash(X500Principal principal) {
        return X509_NAME_hash(principal, "SHA1");
    }

    public static int X509_NAME_hash_old(X500Principal principal) {
        return X509_NAME_hash(principal, MD5.JCA_NAME);
    }

    private static int X509_NAME_hash(X500Principal principal, String algorithm) {
        try {
            byte[] digest = MessageDigest.getInstance(algorithm).digest(principal.getEncoded());
            int offset = 1 + 1;
            return ((((digest[0] & 255) << 0) | ((digest[1] & 255) << 8)) | ((digest[offset] & 255) << 16)) | ((digest[offset + 1] & 255) << 24);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private static void add(String openssl, String standard) {
        OPENSSL_TO_STANDARD_CIPHER_SUITES.put(openssl, standard);
        STANDARD_TO_OPENSSL_CIPHER_SUITES.put(standard, openssl);
    }

    public static String[] getSupportedCipherSuites() {
        return (String[]) SUPPORTED_CIPHER_SUITES.clone();
    }

    public static String[] getSupportedProtocols() {
        return new String[]{SUPPORTED_PROTOCOL_TLSV1, SUPPORTED_PROTOCOL_TLSV1_1, SUPPORTED_PROTOCOL_TLSV1_2};
    }

    public static void setEnabledProtocols(long ssl, String[] protocols) {
        checkEnabledProtocols(protocols);
        long optionsToSet = 503316480;
        long optionsToClear = 0;
        for (String protocol : protocols) {
            if (protocol.equals(SUPPORTED_PROTOCOL_TLSV1)) {
                optionsToSet &= -67108865;
                optionsToClear |= 67108864;
            } else if (protocol.equals(SUPPORTED_PROTOCOL_TLSV1_1)) {
                optionsToSet &= -268435457;
                optionsToClear |= 268435456;
            } else if (protocol.equals(SUPPORTED_PROTOCOL_TLSV1_2)) {
                optionsToSet &= -134217729;
                optionsToClear |= 134217728;
            } else if (!protocol.equals(OBSOLETE_PROTOCOL_SSLV3)) {
                throw new IllegalStateException();
            }
        }
        SSL_set_options(ssl, optionsToSet);
        SSL_clear_options(ssl, optionsToClear);
    }

    public static String[] checkEnabledProtocols(String[] protocols) {
        if (protocols == null) {
            throw new IllegalArgumentException("protocols == null");
        }
        int i = 0;
        int length = protocols.length;
        while (i < length) {
            String protocol = protocols[i];
            if (protocol == null) {
                throw new IllegalArgumentException("protocols contains null");
            } else if (protocol.equals(SUPPORTED_PROTOCOL_TLSV1) || (protocol.equals(SUPPORTED_PROTOCOL_TLSV1_1) ^ 1) == 0 || (protocol.equals(SUPPORTED_PROTOCOL_TLSV1_2) ^ 1) == 0 || (protocol.equals(OBSOLETE_PROTOCOL_SSLV3) ^ 1) == 0) {
                i++;
            } else {
                throw new IllegalArgumentException("protocol " + protocol + " is not supported");
            }
        }
        return protocols;
    }

    public static void setEnabledCipherSuites(long ssl, String[] cipherSuites) {
        checkEnabledCipherSuites(cipherSuites);
        List<String> opensslSuites = new ArrayList();
        for (String cipherSuite : cipherSuites) {
            if (!cipherSuite.equals(TLS_EMPTY_RENEGOTIATION_INFO_SCSV)) {
                if (cipherSuite.equals(TLS_FALLBACK_SCSV)) {
                    SSL_set_mode(ssl, 1024);
                } else {
                    String openssl = (String) STANDARD_TO_OPENSSL_CIPHER_SUITES.get(cipherSuite);
                    opensslSuites.add(openssl == null ? cipherSuite : openssl);
                }
            }
        }
        SSL_set_cipher_lists(ssl, (String[]) opensslSuites.toArray(new String[opensslSuites.size()]));
    }

    public static String[] checkEnabledCipherSuites(String[] cipherSuites) {
        if (cipherSuites == null) {
            throw new IllegalArgumentException("cipherSuites == null");
        }
        for (int i = 0; i < cipherSuites.length; i++) {
            String cipherSuite = cipherSuites[i];
            if (cipherSuite == null) {
                throw new IllegalArgumentException("cipherSuites[" + i + "] == null");
            }
            if (!(cipherSuite.equals(TLS_EMPTY_RENEGOTIATION_INFO_SCSV) || cipherSuite.equals(TLS_FALLBACK_SCSV) || SUPPORTED_CIPHER_SUITES_SET.contains(cipherSuite))) {
                String standardName = (String) OPENSSL_TO_STANDARD_CIPHER_SUITES.get(cipherSuite);
                if (standardName == null || !SUPPORTED_CIPHER_SUITES_SET.contains(standardName)) {
                    throw new IllegalArgumentException("cipherSuite " + cipherSuite + " is not supported.");
                }
            }
        }
        return cipherSuites;
    }
}
