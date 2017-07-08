package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeRef.EC_GROUP;
import com.android.org.conscrypt.NativeRef.EC_POINT;
import com.android.org.conscrypt.NativeRef.EVP_AEAD_CTX;
import com.android.org.conscrypt.NativeRef.EVP_CIPHER_CTX;
import com.android.org.conscrypt.NativeRef.EVP_MD_CTX;
import com.android.org.conscrypt.NativeRef.EVP_PKEY;
import com.android.org.conscrypt.NativeRef.HMAC_CTX;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.nio.Buffer;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.net.ssl.SSLException;
import javax.security.auth.x500.X500Principal;

public final class NativeCrypto {
    public static final String[] DEFAULT_PROTOCOLS = null;
    static final String[] DEFAULT_PSK_CIPHER_SUITES = null;
    static final String[] DEFAULT_X509_CIPHER_SUITES = null;
    public static final int EC_CURVE_GF2M = 2;
    public static final int EC_CURVE_GFP = 1;
    public static final int EXTENSION_TYPE_CRITICAL = 1;
    public static final int EXTENSION_TYPE_NON_CRITICAL = 0;
    public static final int GN_STACK_ISSUER_ALT_NAME = 2;
    public static final int GN_STACK_SUBJECT_ALT_NAME = 1;
    public static final Map<String, String> OPENSSL_TO_STANDARD_CIPHER_SUITES = null;
    public static final int PKCS7_CERTS = 1;
    public static final int PKCS7_CRLS = 2;
    public static final int RAND_SEED_LENGTH_IN_BYTES = 1024;
    public static final String[] SSLV3_PROTOCOLS = null;
    public static final int SSL_VERIFY_FAIL_IF_NO_PEER_CERT = 2;
    public static final int SSL_VERIFY_NONE = 0;
    public static final int SSL_VERIFY_PEER = 1;
    public static final Map<String, String> STANDARD_TO_OPENSSL_CIPHER_SUITES = null;
    private static final String[] SUPPORTED_CIPHER_SUITES = null;
    public static final Set<String> SUPPORTED_CIPHER_SUITES_SET = null;
    private static final String SUPPORTED_PROTOCOL_SSLV3 = "SSLv3";
    private static final String SUPPORTED_PROTOCOL_TLSV1 = "TLSv1";
    private static final String SUPPORTED_PROTOCOL_TLSV1_1 = "TLSv1.1";
    private static final String SUPPORTED_PROTOCOL_TLSV1_2 = "TLSv1.2";
    public static final String[] TLSV11_PROTOCOLS = null;
    public static final String[] TLSV12_PROTOCOLS = null;
    public static final String[] TLSV1_PROTOCOLS = null;
    public static final String TLS_EMPTY_RENEGOTIATION_INFO_SCSV = "TLS_EMPTY_RENEGOTIATION_INFO_SCSV";
    public static final String TLS_FALLBACK_SCSV = "TLS_FALLBACK_SCSV";
    public static final boolean isBoringSSL = false;

    public interface SSLHandshakeCallbacks {
        void clientCertificateRequested(byte[] bArr, byte[][] bArr2) throws CertificateEncodingException, SSLException;

        int clientPSKKeyRequested(String str, byte[] bArr, byte[] bArr2);

        void onSSLStateChange(long j, int i, int i2);

        int serverPSKKeyRequested(String str, String str2, byte[] bArr);

        void verifyCertificateChain(long j, long[] jArr, String str) throws CertificateException;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.NativeCrypto.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.NativeCrypto.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.NativeCrypto.<clinit>():void");
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

    public static native void EC_GROUP_set_asn1_flag(EC_GROUP ec_group, int i);

    public static native void EC_GROUP_set_point_conversion_form(EC_GROUP ec_group, int i);

    public static native long EC_KEY_generate_key(EC_GROUP ec_group);

    public static native long EC_KEY_get1_group(EVP_PKEY evp_pkey);

    public static native byte[] EC_KEY_get_private_key(EVP_PKEY evp_pkey);

    public static native long EC_KEY_get_public_key(EVP_PKEY evp_pkey);

    public static native void EC_KEY_set_nonce_from_hash(EVP_PKEY evp_pkey, boolean z);

    public static native void EC_POINT_clear_free(long j);

    public static native byte[][] EC_POINT_get_affine_coordinates(EC_GROUP ec_group, EC_POINT ec_point);

    public static native long EC_POINT_new(EC_GROUP ec_group);

    public static native void EC_POINT_set_affine_coordinates(EC_GROUP ec_group, EC_POINT ec_point, byte[] bArr, byte[] bArr2);

    public static native int ENGINE_add(long j);

    public static native long ENGINE_by_id(String str);

    public static native int ENGINE_ctrl_cmd_string(long j, String str, String str2, int i);

    public static native int ENGINE_finish(long j);

    public static native int ENGINE_free(long j);

    public static native String ENGINE_get_id(long j);

    public static native int ENGINE_init(long j);

    public static native void ENGINE_load_dynamic();

    public static native long ENGINE_load_private_key(long j, String str) throws InvalidKeyException;

    public static native long ERR_peek_last_error();

    public static native void EVP_AEAD_CTX_cleanup(long j);

    public static native long EVP_AEAD_CTX_init(long j, byte[] bArr, int i);

    public static native int EVP_AEAD_CTX_open(EVP_AEAD_CTX evp_aead_ctx, byte[] bArr, int i, byte[] bArr2, byte[] bArr3, int i2, int i3, byte[] bArr4) throws BadPaddingException;

    public static native int EVP_AEAD_CTX_seal(EVP_AEAD_CTX evp_aead_ctx, byte[] bArr, int i, byte[] bArr2, byte[] bArr3, int i2, int i3, byte[] bArr4) throws BadPaddingException;

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

    public static native void EVP_PKEY_CTX_set_rsa_mgf1_md(long j, long j2);

    public static native void EVP_PKEY_CTX_set_rsa_padding(long j, int i);

    public static native void EVP_PKEY_CTX_set_rsa_pss_saltlen(long j, int i);

    public static native int EVP_PKEY_cmp(EVP_PKEY evp_pkey, EVP_PKEY evp_pkey2);

    public static native void EVP_PKEY_free(long j);

    public static native long EVP_PKEY_new_DSA(byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5);

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

    public static native int RAND_load_file(String str, long j);

    public static native void RAND_seed(byte[] bArr);

    public static native long RSA_generate_key_ex(int i, byte[] bArr);

    public static native int RSA_private_decrypt(int i, byte[] bArr, byte[] bArr2, EVP_PKEY evp_pkey, int i2) throws BadPaddingException, SignatureException;

    public static native int RSA_private_encrypt(int i, byte[] bArr, byte[] bArr2, EVP_PKEY evp_pkey, int i2);

    public static native int RSA_public_decrypt(int i, byte[] bArr, byte[] bArr2, EVP_PKEY evp_pkey, int i2) throws BadPaddingException, SignatureException;

    public static native int RSA_public_encrypt(int i, byte[] bArr, byte[] bArr2, EVP_PKEY evp_pkey, int i2);

    public static native int RSA_size(EVP_PKEY evp_pkey);

    public static native String SSL_CIPHER_get_kx_name(long j);

    public static native void SSL_CTX_disable_npn(long j);

    public static native void SSL_CTX_enable_npn(long j);

    public static native void SSL_CTX_free(long j);

    public static native long SSL_CTX_new();

    public static native void SSL_CTX_set_ocsp_response(long j, byte[] bArr);

    public static native void SSL_CTX_set_session_id_context(long j, byte[] bArr);

    public static native void SSL_CTX_set_signed_cert_timestamp_list(long j, byte[] bArr);

    public static native String SSL_SESSION_cipher(long j);

    public static native void SSL_SESSION_free(long j);

    public static native long SSL_SESSION_get_time(long j);

    public static native String SSL_SESSION_get_version(long j);

    public static native byte[] SSL_SESSION_session_id(long j);

    public static native void SSL_check_private_key(long j) throws SSLException;

    public static native long SSL_clear_mode(long j, long j2);

    public static native long SSL_clear_options(long j, long j2);

    public static native long SSL_do_handshake(long j, FileDescriptor fileDescriptor, SSLHandshakeCallbacks sSLHandshakeCallbacks, int i, boolean z, byte[] bArr, byte[] bArr2) throws SSLException, SocketTimeoutException, CertificateException;

    public static native long SSL_do_handshake_bio(long j, long j2, long j3, SSLHandshakeCallbacks sSLHandshakeCallbacks, boolean z, byte[] bArr, byte[] bArr2) throws SSLException, SocketTimeoutException, CertificateException;

    public static native void SSL_enable_ocsp_stapling(long j);

    public static native void SSL_enable_signed_cert_timestamps(long j);

    public static native void SSL_enable_tls_channel_id(long j) throws SSLException;

    public static native void SSL_free(long j);

    public static native byte[] SSL_get0_alpn_selected(long j);

    public static native long[] SSL_get_certificate(long j);

    public static native long[] SSL_get_ciphers(long j);

    public static native long SSL_get_mode(long j);

    public static native byte[] SSL_get_npn_negotiated_protocol(long j);

    public static native byte[] SSL_get_ocsp_response(long j);

    public static native long SSL_get_options(long j);

    public static native long[] SSL_get_peer_cert_chain(long j);

    public static native String SSL_get_servername(long j);

    public static native int SSL_get_shutdown(long j);

    public static native byte[] SSL_get_signed_cert_timestamp_list(long j);

    public static native byte[] SSL_get_tls_channel_id(long j) throws SSLException;

    public static native void SSL_interrupt(long j);

    public static native long SSL_new(long j) throws SSLException;

    public static native int SSL_read(long j, FileDescriptor fileDescriptor, SSLHandshakeCallbacks sSLHandshakeCallbacks, byte[] bArr, int i, int i2, int i3) throws IOException;

    public static native int SSL_read_BIO(long j, byte[] bArr, int i, int i2, long j2, long j3, SSLHandshakeCallbacks sSLHandshakeCallbacks) throws IOException;

    public static native void SSL_renegotiate(long j) throws SSLException;

    public static native boolean SSL_session_reused(long j);

    public static native void SSL_set1_tls_channel_id(long j, EVP_PKEY evp_pkey);

    public static native void SSL_set_accept_state(long j);

    public static native int SSL_set_alpn_protos(long j, byte[] bArr);

    public static native void SSL_set_cipher_lists(long j, String[] strArr);

    public static native void SSL_set_client_CA_list(long j, byte[][] bArr);

    public static native void SSL_set_connect_state(long j);

    public static native long SSL_set_mode(long j, long j2);

    public static native long SSL_set_options(long j, long j2);

    public static native void SSL_set_reject_peer_renegotiations(long j, boolean z) throws SSLException;

    public static native void SSL_set_session(long j, long j2) throws SSLException;

    public static native void SSL_set_session_creation_enabled(long j, boolean z) throws SSLException;

    public static native void SSL_set_tlsext_host_name(long j, String str) throws SSLException;

    public static native void SSL_set_verify(long j, int i);

    public static native void SSL_shutdown(long j, FileDescriptor fileDescriptor, SSLHandshakeCallbacks sSLHandshakeCallbacks) throws IOException;

    public static native void SSL_shutdown_BIO(long j, long j2, long j3, SSLHandshakeCallbacks sSLHandshakeCallbacks) throws IOException;

    public static native void SSL_use_PrivateKey(long j, EVP_PKEY evp_pkey);

    public static native void SSL_use_certificate(long j, long[] jArr);

    public static native void SSL_use_psk_identity_hint(long j, String str) throws SSLException;

    public static native void SSL_write(long j, FileDescriptor fileDescriptor, SSLHandshakeCallbacks sSLHandshakeCallbacks, byte[] bArr, int i, int i2, int i3) throws IOException;

    public static native int SSL_write_BIO(long j, byte[] bArr, int i, long j2, SSLHandshakeCallbacks sSLHandshakeCallbacks) throws IOException;

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

    public static native long X509_get_pubkey(long j) throws NoSuchAlgorithmException;

    public static native byte[] X509_get_serialNumber(long j);

    public static native byte[] X509_get_subject_name(long j);

    public static native long X509_get_version(long j);

    public static native void X509_print_ex(long j, long j2, long j3, long j4);

    public static native int X509_supported_extension(long j);

    public static native void X509_verify(long j, EVP_PKEY evp_pkey) throws BadPaddingException;

    private static native boolean clinit();

    public static native long create_BIO_InputStream(OpenSSLBIOInputStream openSSLBIOInputStream, boolean z);

    public static native long create_BIO_OutputStream(OutputStream outputStream);

    public static native long[] d2i_PKCS7_bio(long j, int i);

    public static native long d2i_PKCS8_PRIV_KEY_INFO(byte[] bArr);

    public static native long d2i_PUBKEY(byte[] bArr);

    public static native long d2i_SSL_SESSION(byte[] bArr) throws IOException;

    public static native long d2i_X509(byte[] bArr);

    public static native long d2i_X509_CRL_bio(long j);

    public static native long d2i_X509_bio(long j);

    public static native long getDirectBufferAddress(Buffer buffer);

    public static native long getECPrivateKeyWrapper(PrivateKey privateKey, EC_GROUP ec_group);

    public static native long getRSAPrivateKeyWrapper(PrivateKey privateKey, byte[] bArr);

    public static native int get_EC_GROUP_type(EC_GROUP ec_group);

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

    public static int X509_NAME_hash(X500Principal principal) {
        return X509_NAME_hash(principal, "SHA1");
    }

    public static int X509_NAME_hash_old(X500Principal principal) {
        return X509_NAME_hash(principal, "MD5");
    }

    private static int X509_NAME_hash(X500Principal principal, String algorithm) {
        try {
            byte[] digest = MessageDigest.getInstance(algorithm).digest(principal.getEncoded());
            int offset = SSL_VERIFY_PEER + SSL_VERIFY_PEER;
            return ((((digest[SSL_VERIFY_NONE] & 255) << SSL_VERIFY_NONE) | ((digest[SSL_VERIFY_PEER] & 255) << 8)) | ((digest[offset] & 255) << 16)) | ((digest[offset + SSL_VERIFY_PEER] & 255) << 24);
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
        return new String[]{SUPPORTED_PROTOCOL_SSLV3, SUPPORTED_PROTOCOL_TLSV1, SUPPORTED_PROTOCOL_TLSV1_1, SUPPORTED_PROTOCOL_TLSV1_2};
    }

    public static void setEnabledProtocols(long ssl, String[] protocols) {
        checkEnabledProtocols(protocols);
        long optionsToSet = 503316480;
        long optionsToClear = 0;
        for (int i = SSL_VERIFY_NONE; i < protocols.length; i += SSL_VERIFY_PEER) {
            String protocol = protocols[i];
            if (protocol.equals(SUPPORTED_PROTOCOL_SSLV3)) {
                optionsToSet &= -33554433;
                optionsToClear |= 33554432;
            } else if (protocol.equals(SUPPORTED_PROTOCOL_TLSV1)) {
                optionsToSet &= -67108865;
                optionsToClear |= 67108864;
            } else if (protocol.equals(SUPPORTED_PROTOCOL_TLSV1_1)) {
                optionsToSet &= -268435457;
                optionsToClear |= 268435456;
            } else if (protocol.equals(SUPPORTED_PROTOCOL_TLSV1_2)) {
                optionsToSet &= -134217729;
                optionsToClear |= 134217728;
            } else {
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
        int i = SSL_VERIFY_NONE;
        while (i < protocols.length) {
            String protocol = protocols[i];
            if (protocol == null) {
                throw new IllegalArgumentException("protocols[" + i + "] == null");
            } else if (protocol.equals(SUPPORTED_PROTOCOL_SSLV3) || protocol.equals(SUPPORTED_PROTOCOL_TLSV1) || protocol.equals(SUPPORTED_PROTOCOL_TLSV1_1) || protocol.equals(SUPPORTED_PROTOCOL_TLSV1_2)) {
                i += SSL_VERIFY_PEER;
            } else {
                throw new IllegalArgumentException("protocol " + protocol + " is not supported");
            }
        }
        return protocols;
    }

    public static void setEnabledCipherSuites(long ssl, String[] cipherSuites) {
        checkEnabledCipherSuites(cipherSuites);
        List<String> opensslSuites = new ArrayList();
        for (int i = SSL_VERIFY_NONE; i < cipherSuites.length; i += SSL_VERIFY_PEER) {
            String cipherSuite = cipherSuites[i];
            if (!cipherSuite.equals(TLS_EMPTY_RENEGOTIATION_INFO_SCSV)) {
                if (cipherSuite.equals(TLS_FALLBACK_SCSV)) {
                    SSL_set_mode(ssl, 1024);
                } else {
                    String cs;
                    String openssl = (String) STANDARD_TO_OPENSSL_CIPHER_SUITES.get(cipherSuite);
                    if (openssl == null) {
                        cs = cipherSuite;
                    } else {
                        cs = openssl;
                    }
                    opensslSuites.add(cs);
                }
            }
        }
        SSL_set_cipher_lists(ssl, (String[]) opensslSuites.toArray(new String[opensslSuites.size()]));
    }

    public static String[] checkEnabledCipherSuites(String[] cipherSuites) {
        if (cipherSuites == null) {
            throw new IllegalArgumentException("cipherSuites == null");
        }
        for (int i = SSL_VERIFY_NONE; i < cipherSuites.length; i += SSL_VERIFY_PEER) {
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
