package com.huawei.security.hwassetmanager;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Base64;
import android.util.Log;
import com.huawei.hwpartsecurity.BuildConfig;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.conscrypt.HwTrustManager;
import com.huawei.security.hwassetmanager.IHwAssetObserver;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterBlob;
import com.huawei.security.keymaster.HwKeymasterCertificateChain;
import com.huawei.security.keymaster.HwKeymasterDefs;
import com.huawei.security.keystore.HwKeyProperties;
import com.huawei.security.keystore.HwUniversalKeyStoreProvider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class HwAssetManager {
    public static final int ALLOW_ACCESS_DEFAULT = 0;
    public static final int ALLOW_ACCESS_DEFINE_CREATOR = 2;
    public static final int ALLOW_ACCESS_DEFINE_OWNER = 1;
    public static final int ALL_DATA_TYPE = 7;
    public static final int ALL_EVENTS = 31;
    private static final int ASSETMANAGER_TAG_INVALID = -1;
    public static final int ASSET_BATCH_SELECT_BEGIN = 0;
    public static final int ASSET_BATCH_SELECT_CONTINUE = 1;
    public static final int ASSET_PURPOSE_DECRYPT = 2;
    public static final int ASSET_PURPOSE_DERIVE_KEY = 16;
    public static final int ASSET_PURPOSE_ENCRYPT = 1;
    public static final int ASSET_PURPOSE_SIGN = 4;
    public static final int ASSET_PURPOSE_UNWRAP = 64;
    public static final int ASSET_PURPOSE_VERIFY = 8;
    public static final int ASSET_PURPOSE_WRAP = 32;
    public static final int ASSET_SELECT_GET_PUBKEY = 4;
    public static final int ASSET_SELECT_IS_PRECISE = 1;
    public static final int ASSET_SELECT_WITH_CONTENT = 2;
    public static final int ASSET_TYPE_CERTIFICATE = 7;
    public static final int ASSET_TYPE_CREDIT_CARD = 1;
    public static final int ASSET_TYPE_KEY_PAIR = 6;
    public static final int ASSET_TYPE_PRIVATE_KEY = 5;
    public static final int ASSET_TYPE_PUBLIC_KEY = 4;
    public static final int ASSET_TYPE_SECRET_KEY = 3;
    public static final int ASSET_TYPE_TOKEN = 2;
    public static final int ASSET_TYPE_USERNAME_PASSWORD = 0;
    public static final String BUNDLE_ACCESSLIMITATION = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_ACCESSLIMITATION);
    public static final String BUNDLE_ACCOUNT_LIMITATION = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_ACCOUNTLIMIT);
    public static final String BUNDLE_AEADASSET = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_AEADASSET);
    public static final String BUNDLE_ALIAS = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_KEYALIAS);
    public static final String BUNDLE_APPTAG = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_APPTAG);
    public static final String BUNDLE_APP_PACKAGE_NAME = String.valueOf((int) HwKeymasterDefs.KM_TAG_APP_PACKAGE_NAME);
    public static final String BUNDLE_APP_PACKAGE_PUBKEY = String.valueOf((int) HwKeymasterDefs.KM_TAG_APP_PACKAGE_PUBKEY);
    public static final String BUNDLE_ASSETHANDLE = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETHANDLE);
    public static final String BUNDLE_ASSETTYPE = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETTYPE);
    public static final String BUNDLE_ASSOCIATED_DATA = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSOCIATED_DATA);
    public static final String BUNDLE_ATTESTATION_CHALLENGE = String.valueOf((int) HwKeymasterDefs.KM_TAG_ATTESTATION_CHALLENGE);
    public static final String BUNDLE_AUTHENTICATELIMITATION = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_AUTHENTICATELIMITATION);
    public static final String BUNDLE_BASE_PWD = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_BASE_PWD);
    public static final String BUNDLE_BATCHASSET = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_BATCHASSET);
    public static final String BUNDLE_BATCH_SELEC = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_BATCH_SELECT);
    public static final String BUNDLE_CERT_CHAIN = String.valueOf((int) HwKeymasterDefs.KM_TAG_ATTESTATION_CERT_CHAIN);
    public static final String BUNDLE_CERT_PUBKEY = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_CERT_PK);
    public static final String BUNDLE_CLOUD_SYNC_ACCOUNT_UID = String.valueOf((int) HwKeymasterDefs.KM_TAG_CLOUD_SYNC_ACCOUNT_UID);
    public static final String BUNDLE_CLOUD_SYNC_LIMITATION = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_CLOUDSYNCLIMIT);
    public static final String BUNDLE_CONTEXT_PUBKEY = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_CONTEXT_PUBKEY);
    public static final String BUNDLE_DATA_CREATOR = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_DATA_CREATOR);
    public static final String BUNDLE_DATA_OWNER = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_DATA_OWNER);
    public static final String BUNDLE_DELETE = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_ISDELETE);
    public static final String BUNDLE_DERIVE_KEY_ALG = String.valueOf((int) HwKeymasterDefs.KM_TAG_DERIVE_KEY_ALG);
    public static final String BUNDLE_DIRTY = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_ISDIRTY);
    public static final String BUNDLE_EXTINFO = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_EXTINFO);
    public static final String BUNDLE_GUID = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_GUID);
    public static final String BUNDLE_IS_SYNC_APP_DATA = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_SYN_APP_DATA);
    public static final String BUNDLE_KEY_AGREEMENT_ALG = String.valueOf((int) HwKeymasterDefs.KM_TAG_KEY_AGREEMENT_ALG);
    public static final String BUNDLE_KEY_ALGO_EXTINFO = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_TARGET_ALGEXTINFO);
    public static final String BUNDLE_KEY_ALGO_PARAM = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_TARGET_ALGPAR);
    public static final String BUNDLE_KEY_ALGO_TYPE = String.valueOf((int) HwKeymasterDefs.KM_TAG_ALGORITHM);
    public static final String BUNDLE_KEY_ALLOW_WRAP = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_TARGET_ALLOWWRAP);
    public static final String BUNDLE_KEY_BLOCKMODE = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_TARGET_BLOCKMODE);
    public static final String BUNDLE_KEY_DERIVE_LABEL = String.valueOf((int) HwKeymasterDefs.KM_TAG_KEY_DERIVE_LABEL);
    public static final String BUNDLE_KEY_DERIVE_SALT = String.valueOf((int) HwKeymasterDefs.KM_TAG_KEY_DERIVE_SALT);
    public static final String BUNDLE_KEY_DIGEST_ALGO = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_TARGET_DIGESTALG);
    public static final String BUNDLE_KEY_GEN_ALIAS = String.valueOf((int) HwKeymasterDefs.KM_TAG_DERIVE_MASTER_KEY);
    public static final String BUNDLE_KEY_GEN_DERIVE_FACTOR = String.valueOf((int) HwKeymasterDefs.KM_TAG_DERIVE_FACTOR);
    public static final String BUNDLE_KEY_GEN_INTER_PROCESS = String.valueOf((int) HwKeymasterDefs.KM_TAG_ALLOW_ACCESS_BY_OTHER_PROC);
    public static final String BUNDLE_KEY_GEN_TYPE = String.valueOf((int) HwKeymasterDefs.KM_TAG_KEY_GENERATE_TYPE);
    public static final String BUNDLE_KEY_LENGTH = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_TARGET_KEYLENGTH);
    public static final String BUNDLE_KEY_OUTGOING_TYPE = String.valueOf((int) HwKeymasterDefs.KM_TAG_TEE_OUTGOING_TYPE);
    public static final String BUNDLE_KEY_PADDING = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_TARGET_PADDING);
    public static final String BUNDLE_KEY_PURPOSE = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_TARGET_PURPOSE);
    public static final String BUNDLE_KEY_WRAP_TYPE = String.valueOf((int) HwKeymasterDefs.KM_TAG_WRAP_KEY_TYPE);
    public static final String BUNDLE_LOCAL_LIMITATION = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_LOCALLIMIT);
    public static final String BUNDLE_OPCODE = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE);
    public static final String BUNDLE_OPERATION_HANDLE = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_OPERATION_HANDLE);
    public static final String BUNDLE_OWNER_ID = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_OWNER_ID);
    public static final String BUNDLE_PROCESS_TYPE = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_PROCESS_TYPE);
    public static final String BUNDLE_RESETFLAG = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_RESETFLAG);
    public static final String BUNDLE_SELECTFLAG = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_SELECT_FLAG);
    public static final String BUNDLE_SERVICE_ID = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_SERVICE_ID);
    public static final String BUNDLE_SIGNATURE_DATA = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_SIGNATURE_DATA);
    public static final String BUNDLE_SRC_DATA = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_SRCDATA);
    public static final String BUNDLE_SYNCLIMITATION = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_SYNCLIMITATION);
    public static final String BUNDLE_SYNCPOLICY = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_SYNCPOLICY);
    public static final String BUNDLE_TARGET_ALG = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_TARGET_ALGTYPE);
    public static final String BUNDLE_TARGET_ALIAS = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_TARGET_KEYALIAS);
    public static final String BUNDLE_TEE_LIMITATION = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_TEELIMIT);
    public static final String BUNDLE_TEE_STORAGE = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_TEESTORAGE);
    public static final String BUNDLE_TRANSFER_DATA = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_TRANSFER_DATA);
    public static final String BUNDLE_TRANSFER_ECDH_ALIAS = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_TRANSFER_ECDH_ALIAS);
    public static final String BUNDLE_TRANSFER_ECDH_PUBKEY = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_TRANSFER_ECDH_PUBKEY);
    public static final String BUNDLE_TRANSFER_TYPE = "transferType";
    public static final String BUNDLE_UNSTRUCT_UUID = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_UNSTRUCTUUID);
    public static final String BUNDLE_UPDATE_FLAG = String.valueOf((int) HwKeymasterDefs.KM_TAG_ASSETSTORE_UPDATE_FLAG);
    public static final int DATA_TYPE_CERT = 4;
    public static final int DATA_TYPE_KEY_PAIR = 2;
    public static final int DATA_TYPE_PASSWORD = 1;
    public static final int ERROR_CODE_BASE64_DECODE_FAIL = -214;
    public static final int ERROR_CODE_CAN_NOT_FIND_TARGET_ALIAS = -160;
    public static final int ERROR_CODE_CHALLENGE_CHECK_FAIL = -123;
    public static final int ERROR_CODE_CHECKIDENTITY_FAIL = -157;
    public static final int ERROR_CODE_CHECK_PERMMISSION_FAIL = -213;
    public static final int ERROR_CODE_CONSTRUCT_CALLBACK_FAIL = -210;
    public static final int ERROR_CODE_CRYPTO_MAC_INVALID = -309;
    public static final int ERROR_CODE_DATABASE_ERROR = -4;
    public static final int ERROR_CODE_DATA_NOT_EXIST = -150;
    public static final int ERROR_CODE_DEL_DB_FAIL = -251;
    public static final int ERROR_CODE_FAKE_DELETE_DB_FAIL = -252;
    public static final int ERROR_CODE_GENERATE_CLOUD_SYNCING_STATE = -119;
    public static final int ERROR_CODE_GET_BLOB_FROM_STRING = -202;
    public static final int ERROR_CODE_GET_NEWBLOB_FAIL = -215;
    public static final int ERROR_CODE_GET_OBSERVER_FAIL = -211;
    public static final int ERROR_CODE_HUKS_AES_ENC_DEC = -300;
    public static final int ERROR_CODE_HUKS_BEGIN_FAIL = -301;
    public static final int ERROR_CODE_HUKS_DEL_FAIL = -307;
    public static final int ERROR_CODE_HUKS_EXPORTKEY = -306;
    public static final int ERROR_CODE_HUKS_FINISH_FAIL = -303;
    public static final int ERROR_CODE_HUKS_GEN_FAIL = -308;
    public static final int ERROR_CODE_HUKS_UNWRAP_FAIL = -305;
    public static final int ERROR_CODE_HUKS_UPDATE_FAIL = -302;
    public static final int ERROR_CODE_HUKS_WRAP_FAIL = -304;
    public static final int ERROR_CODE_ILLEGAL_OPCODE_FOR_NOT_SYNCAPP = -118;
    public static final int ERROR_CODE_IMPORTKEY_FAIL = -208;
    public static final int ERROR_CODE_INIT_DBHANDLER_FAIL = -115;
    public static final int ERROR_CODE_INSERT_DB_FAIL = -250;
    public static final int ERROR_CODE_INVALID_ARGUMENT = -1;
    public static final int ERROR_CODE_JSON_PARSE_FAIL = -201;
    public static final int ERROR_CODE_NOT_SYNCAPP = -108;
    public static final int ERROR_CODE_NO_ASSETHANDLE_GEN = -212;
    public static final int ERROR_CODE_NO_ASSETHANDLE_TAG = -209;
    public static final int ERROR_CODE_NO_CERT_CHAIN = -121;
    public static final int ERROR_CODE_NO_CHALLENGE = -120;
    public static final int ERROR_CODE_NO_CLOUD_SYNC_ACCOUNT_UID_TAG = -103;
    public static final int ERROR_CODE_NO_DATA_CREATOR = -152;
    public static final int ERROR_CODE_NO_DATA_OWNER = -153;
    public static final int ERROR_CODE_NO_DIFF_OPCODE = -117;
    public static final int ERROR_CODE_NO_KEYALIAS_TAG = -101;
    public static final int ERROR_CODE_NO_NO_SRCDATA_TAG = -104;
    public static final int ERROR_CODE_NO_OPFUNC = -205;
    public static final int ERROR_CODE_NO_OP_TAG = -107;
    public static final int ERROR_CODE_NO_SYNCPOLICY_TAG = -105;
    public static final int ERROR_CODE_NO_TARGET_KEYALIAS_TAG = -102;
    public static final int ERROR_CODE_NO_TARGET_PURPOSE_TAG = -106;
    public static final int ERROR_CODE_NO_WRAP_KEY_TYPE_TAG = -100;
    public static final int ERROR_CODE_OPCODE_INVALID = -200;
    public static final int ERROR_CODE_OUTPUTRES_LEN_CHECK_FAIL = -113;
    public static final int ERROR_CODE_OUTPUTRES_MALLOC_FAIL = -114;
    public static final int ERROR_CODE_OUTPUTRES_PARAM_INVALID = -112;
    public static final int ERROR_CODE_OUTRES_NULL = -207;
    public static final int ERROR_CODE_OWNERUNLOCK_FAIL = -206;
    public static final int ERROR_CODE_PARSE_CHALLENGE_FAIL = -122;
    public static final int ERROR_CODE_PERMISSION_DENIED = -2;
    public static final int ERROR_CODE_PREHANDLE_BLOB_FAIL = -159;
    public static final int ERROR_CODE_PREHANDLE_STRING_FAIL = -158;
    public static final int ERROR_CODE_REBUILD_DATACREATOR_FAIL = -111;
    public static final int ERROR_CODE_REBUILD_DATAOWNER_FAIL = -110;
    public static final int ERROR_CODE_REBUILD_FAIL = -116;
    public static final int ERROR_CODE_SELECT_DB_ALREADY_EXIST = -256;
    public static final int ERROR_CODE_SELECT_DB_FAIL = -254;
    public static final int ERROR_CODE_SELECT_DB_NONE = -255;
    public static final int ERROR_CODE_SELECT_DB_NO_TABLE = -257;
    public static final int ERROR_CODE_SYNCAPP_OWN_DATA = -109;
    public static final int ERROR_CODE_SYNCIN_ADD_FAIL = -163;
    public static final int ERROR_CODE_SYNCIN_ADD_TAG_NOT_SUPPORT = -164;
    public static final int ERROR_CODE_SYNCIN_NOT_SUPPORT_SCENCE = -161;
    public static final int ERROR_CODE_SYNCIN_NO_GUID = -167;
    public static final int ERROR_CODE_SYNCIN_NO_LUID = -168;
    public static final int ERROR_CODE_SYNCIN_POLICY_FIELD_ERROR = -165;
    public static final int ERROR_CODE_SYNCIN_POLICY_SCENCE_ERROR = -166;
    public static final int ERROR_CODE_SYNCIN_UPDATE_FAIL = -162;
    public static final int ERROR_CODE_SYNC_APP_NO_OWNER = -151;
    public static final int ERROR_CODE_SYNC_NOTFIND_DATA_OWNER = -155;
    public static final int ERROR_CODE_SYNC_NOTFIND_TARGET_ALIAS = -156;
    public static final int ERROR_CODE_SYSTEM_ERROR = -10;
    public static final int ERROR_CODE_TARGET_PURPOSE_INVALID = -154;
    public static final int ERROR_CODE_UNINITIALIZED = -3;
    public static final int ERROR_CODE_UNSECURED_ENVIRONMENT = -5;
    public static final int ERROR_CODE_UPDATE_DB_FAIL = -253;
    public static final int ERROR_CODE_WRITE_BLOB = -204;
    public static final int ERROR_CODE_WRITE_JSON = -203;
    public static final int EVENT_DELETE = 4;
    public static final int EVENT_GENERATE = 1;
    public static final int EVENT_INSERT = 2;
    public static final int EVENT_SELECT = 16;
    public static final int EVENT_UPDATE = 8;
    public static final int GEN_INNER_PROCESS_KEY = 0;
    public static final int GEN_INTER_PROCESS_KEY = 1;
    public static final int HW_DERIVE_KEY_ALG_HKDF = 1;
    public static final int HW_DERIVE_KEY_ALG_HMAC = 0;
    private static final String KEYCHAIN_CLONE_ALIAS = "hwkeychainclone";
    public static final int KEY_GENERATE_TYPE_DEFAULT = 0;
    public static final int KEY_GENERATE_TYPE_DERIVE = 1;
    public static final int KEY_GENERATE_TYPE_IMPORT = 2;
    public static final int KEY_STORAGE_ASSET_DB = 2;
    public static final int KEY_STORAGE_DEFAULT = 0;
    public static final int KEY_STORAGE_TEE = 1;
    public static final int KM_ACCESS_TYPE_CE = 3;
    public static final int KM_ACCESS_TYPE_DE = 2;
    public static final int KM_ACCESS_TYPE_ECE = 4;
    public static final int KM_ACCESS_TYPE_SECE = 5;
    public static final int KM_ALGORITHM_AES = 32;
    public static final int KM_ALGORITHM_EC = 3;
    public static final int KM_ALGORITHM_ED25519 = 4;
    public static final int KM_ALGORITHM_HMAC = 128;
    public static final int KM_ALGORITHM_PBKDF2 = 1000;
    public static final int KM_ALGORITHM_RSA = 1;
    public static final int KM_ALGORITHM_X25519 = 5;
    public static final int KM_DIGEST_MD5 = 1;
    public static final int KM_DIGEST_NONE = 0;
    public static final int KM_DIGEST_SHA1 = 2;
    public static final int KM_DIGEST_SHA_2_224 = 3;
    public static final int KM_DIGEST_SHA_2_256 = 4;
    public static final int KM_DIGEST_SHA_2_384 = 5;
    public static final int KM_DIGEST_SHA_2_512 = 6;
    public static final int KM_MODE_CBC = 2;
    public static final int KM_MODE_CCM = 31;
    public static final int KM_MODE_CTR = 3;
    public static final int KM_MODE_ECB = 1;
    public static final int KM_MODE_GCM = 32;
    public static final int KM_PAD_NONE = 1;
    public static final int KM_PAD_PKCS7 = 64;
    public static final int KM_PAD_RSA_OAEP = 2;
    public static final int KM_PAD_RSA_PKCS1_1_5_ENCRYPT = 4;
    public static final int KM_PAD_RSA_PKCS1_1_5_SIGN = 5;
    public static final int KM_PAD_RSA_PSS = 3;
    public static final String KM_TAG_ATTESTATION_SESSION = String.valueOf((int) HwKeymasterDefs.KM_TAG_ATTESTATION_SESSION);
    private static final int MAX_CERTIFICATE_NUM_IN_CHAIN = 4;
    private static final int OPCODE_ACQUIRE_LOCK = 17;
    private static final int OPCODE_BACKUP_CONTINUE = 10;
    private static final int OPCODE_BACKUP_START = 9;
    private static final int OPCODE_CLONEIN_CONTINUE = 5;
    private static final int OPCODE_CLONEIN_START = 8;
    private static final int OPCODE_CLONEOUT_CONTINUE = 4;
    private static final int OPCODE_CLONEOUT_START = 7;
    private static final int OPCODE_CLOUD_SYNC = 19;
    private static final int OPCODE_DELETE = 1;
    private static final int OPCODE_ECDH = 22;
    private static final int OPCODE_GENERATE = 13;
    private static final int OPCODE_GETAPPS = 3;
    private static final int OPCODE_GET_CERTIFICATE_CHAIN = 20;
    private static final int OPCODE_INSERT = 0;
    private static final int OPCODE_LOAD_ENTRY = 21;
    private static final int OPCODE_LOAD_KEY = 23;
    private static final int OPCODE_OPERATE = 14;
    private static final int OPCODE_RELEASE_LOCK = 18;
    private static final int OPCODE_RESTORE_CONTINUE = 12;
    private static final int OPCODE_RESTORE_START = 11;
    private static final int OPCODE_SELECT = 2;
    private static final int OPCODE_UNWRAP = 16;
    private static final int OPCODE_UPDATE = 6;
    private static final int OPCODE_WRAP = 15;
    public static final int PROCESS_CLOUD_DATA = 1;
    public static final int PROCESS_LOCAL_DATA = 0;
    public static final int RESET_SELECT_CONTINUE = 2;
    public static final int RESET_SELECT_FROM_BEGIN = 1;
    public static final int RESET_SELECT_ONCE = 0;
    public static final int SUCCESS = 0;
    private static final String TAG = "HwAssetManager";
    public static final int TEE_OUTGOING_TYPE_CLOUD_SYNC = 2;
    public static final int TEE_OUTGOING_TYPE_DEFAULT = 0;
    public static final int TEE_OUTGOING_TYPE_WRAP = 1;
    public static final int TRANSFER_TYPE_BACKUP_RESTORE = 2;
    public static final int TRANSFER_TYPE_CLONE = 1;
    public static final int UPDATE_WITHOUT_ASSET_HANDLE = 0;
    public static final int UPDATE_WITH_ASSET_HANDLE = 1;
    public static final int WRAP_KEY_TYPE_ASSET_DATA = 1;
    public static final int WRAP_KEY_TYPE_DEFAULT = 0;
    public static final int WRAP_KEY_TYPE_ECDH_ENC_DATA = 4;
    public static final int WRAP_KEY_TYPE_PWD_ENC_DATA = 5;
    public static final int WRAP_KEY_TYPE_SYNC_DATA = 3;
    public static final int WRAP_KEY_TYPE_SYNC_KEY = 2;
    public static final int WRAP_KEY_TYPE_TRUST_CIRCLE_ENC_DATA = 6;
    private static volatile HwAssetManager sInstance = null;
    private HashSet<String> mBundleTags = null;

    public static class AssetResult {
        public final int resultCode;
        public final List<String> resultInfo;
        public final int resultNumber;

        public AssetResult(int resultCode2) {
            this(resultCode2, null, 0);
        }

        public AssetResult(int resultCode2, List<String> resultInfo2) {
            this(resultCode2, resultInfo2, 0);
        }

        public AssetResult(int resultCode2, List<String> resultInfo2, int resultNumber2) {
            this.resultCode = resultCode2;
            this.resultInfo = resultInfo2 == null ? null : Collections.unmodifiableList(resultInfo2);
            this.resultNumber = resultNumber2;
        }
    }

    private HwAssetManager() {
        HwUniversalKeyStoreProvider.install();
        this.mBundleTags = getBundleTags();
    }

    private HashSet<String> getBundleTags() {
        HashSet<String> hashSet = this.mBundleTags;
        if (hashSet != null) {
            return hashSet;
        }
        HashSet<String> tags = new HashSet<>();
        Field[] fields = HwAssetManager.class.getFields();
        for (Field field : fields) {
            if (field.getName().contains("BUNDLE_")) {
                try {
                    tags.add((String) field.get(null));
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "get bundle tags error:" + e.getMessage());
                }
            }
        }
        return tags;
    }

    private byte[] getBytesHelper(String rawStr) {
        if (rawStr != null) {
            return rawStr.getBytes(StandardCharsets.UTF_8);
        }
        return BuildConfig.FLAVOR.getBytes(StandardCharsets.UTF_8);
    }

    private AssetResult assetHandle(Context context, HwKeymasterArguments assetArgs) {
        if (assetArgs == null) {
            Log.e(TAG, "assetHandle assetArgs is null");
            return new AssetResult(-1);
        }
        int resCode = HwKeystoreManager.getInstance().assetHandleReq(assetArgs, new HwKeymasterCertificateChain());
        if (resCode == 0) {
            return new AssetResult(0);
        }
        Log.e(TAG, String.format("assetHandle opCode: %d, assetHandleReq failed!", Integer.valueOf(assetArgs.getEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, -1))));
        return new AssetResult(resCode);
    }

    private AssetResult assetHandleOutInfo(Context context, HwKeymasterArguments assetArgs, boolean shouldCheckEmpty, Charset charSet) {
        if (assetArgs == null) {
            Log.e(TAG, "assetHandleOutInfo assetArgs is null");
            return new AssetResult(-1);
        }
        HwKeymasterCertificateChain rawChain = new HwKeymasterCertificateChain();
        int resCode = HwKeystoreManager.getInstance().assetHandleReq(assetArgs, rawChain);
        if (resCode != 0) {
            Log.e(TAG, String.format("assetHandleOutInfo opCode: %d, assetHandleReq failed!", Integer.valueOf(assetArgs.getEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, -1))));
            return new AssetResult(resCode);
        }
        List<byte[]> outChain = rawChain.getCertificates();
        List<String> resultInfo = new ArrayList<>();
        boolean isOutChainEmpty = outChain == null || outChain.isEmpty();
        if (!shouldCheckEmpty || !isOutChainEmpty) {
            if (!isOutChainEmpty) {
                for (byte[] info : outChain) {
                    resultInfo.add(new String(info, charSet));
                }
            }
            return new AssetResult(0, resultInfo);
        }
        Log.e(TAG, String.format("assetHandleOutInfo opCode: %d, outChain is unexpected NULL!", Integer.valueOf(assetArgs.getEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, -1))));
        return new AssetResult(-10);
    }

    private AssetResult transferHandle(HwKeymasterArguments assetArgs) {
        if (assetArgs == null) {
            Log.e(TAG, "transferHandle assetArgs is null");
            return new AssetResult(-1);
        }
        int resCode = HwKeystoreManager.getInstance().assetHandleReq(assetArgs, new HwKeymasterCertificateChain());
        if (resCode >= 0) {
            return new AssetResult(0, null, resCode);
        }
        Log.e(TAG, String.format("transferHandle opCode: %d, assetHandleReq failed!", Integer.valueOf(assetArgs.getEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, -1))));
        return new AssetResult(resCode);
    }

    private AssetResult transferHandleOutInfo(HwKeymasterArguments assetArgs) {
        if (assetArgs == null) {
            Log.e(TAG, "transferHandleOutInfo assetArgs is null");
            return new AssetResult(-1);
        }
        HwKeymasterCertificateChain rawChain = new HwKeymasterCertificateChain();
        int resCode = HwKeystoreManager.getInstance().assetHandleReq(assetArgs, rawChain);
        if (resCode < 0) {
            Log.e(TAG, String.format("transferHandleOutInfo opCode: %d, assetHandleReq failed!", Integer.valueOf(assetArgs.getEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, -1))));
            return new AssetResult(resCode);
        }
        List<byte[]> outChain = rawChain.getCertificates();
        if (outChain == null || outChain.isEmpty()) {
            Log.e(TAG, String.format("transferHandleOutInfo opCode: %d, outChain is NULL!", Integer.valueOf(assetArgs.getEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, -1))));
            return new AssetResult(-10);
        }
        List<String> resultInfo = new ArrayList<>();
        resultInfo.add(new String(outChain.get(0), StandardCharsets.UTF_8));
        return new AssetResult(0, resultInfo, resCode);
    }

    private HwKeymasterArguments parseBundle(Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "parseBundle: bundle is null");
            return null;
        }
        HwKeymasterArguments assetArgs = new HwKeymasterArguments();
        for (String rawKey : bundle.keySet()) {
            if (!this.mBundleTags.contains(rawKey)) {
                Log.w(TAG, "parseBundle: bundle tag not exists, tag: " + rawKey);
            }
            try {
                addAssetArgument(assetArgs, Integer.parseInt(rawKey), bundle.get(rawKey));
            } catch (NumberFormatException e) {
                Log.w(TAG, "parseBundle: bundle key is not integer format!");
            }
        }
        return assetArgs;
    }

    private void addAssetArgument(HwKeymasterArguments assetArgs, int key, Object value) {
        if (value == null) {
            if (HwKeymasterDefs.getTagType(key) == -1879048192) {
                assetArgs.addBytes(key, getBytesHelper(null));
            }
            Log.i(TAG, "addAssetArgument: value is set to be null");
        } else if (value instanceof Integer) {
            assetArgs.addEnum(key, ((Integer) value).intValue());
        } else if (value instanceof Boolean) {
            assetArgs.addEnum(key, ((Boolean) value).booleanValue() ? 1 : 0);
        } else if (value instanceof int[]) {
            assetArgs.addEnums(key, (int[]) value);
        } else if (value instanceof String) {
            assetArgs.addBytes(key, getBytesHelper((String) value));
        } else if (value instanceof byte[]) {
            assetArgs.addBytes(key, (byte[]) value);
        } else {
            Log.w(TAG, "addAssetArgument: value type is not supported");
        }
    }

    public static HwAssetManager getInstance() {
        if (sInstance == null) {
            synchronized (HwAssetManager.class) {
                if (sInstance == null) {
                    sInstance = new HwAssetManager();
                }
            }
        }
        return sInstance;
    }

    public AssetResult assetInsert(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "assetInsert: bundle is null");
            return new AssetResult(-1);
        }
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 0);
        int assetType = bundle.getInt(BUNDLE_ASSETTYPE);
        if (assetType == 0 || assetType == 1 || assetType == 2) {
            return assetHandleOutInfo(context, parseBundle(bundle), true, StandardCharsets.UTF_8);
        }
        return assetHandleOutInfo(context, parseBundle(bundle), false, StandardCharsets.ISO_8859_1);
    }

    public AssetResult assetDelete(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "assetDelete: bundle is null");
            return new AssetResult(-1);
        }
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 1);
        return assetHandle(context, parseBundle(bundle));
    }

    public AssetResult assetUpdate(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "assetUpdate: bundle is null");
            return new AssetResult(-1);
        }
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 6);
        int assetType = bundle.getInt(BUNDLE_ASSETTYPE);
        if (assetType == 0 || assetType == 1 || assetType == 2) {
            return assetHandleOutInfo(context, parseBundle(bundle), true, StandardCharsets.UTF_8);
        }
        return assetHandleOutInfo(context, parseBundle(bundle), false, StandardCharsets.ISO_8859_1);
    }

    public AssetResult assetSelect(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "assetSelect: bundle is null");
            return new AssetResult(-1);
        }
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 2);
        int assetType = bundle.getInt(BUNDLE_ASSETTYPE);
        int resetFlag = bundle.getInt(BUNDLE_RESETFLAG);
        if (!(assetType == 0 || assetType == 1 || assetType == 2)) {
            return assetHandleOutInfo(context, parseBundle(bundle), false, StandardCharsets.ISO_8859_1);
        }
        if (resetFlag == 0) {
            return assetHandleOutInfo(context, parseBundle(bundle), true, StandardCharsets.UTF_8);
        }
        return assetHandleOutInfo(context, parseBundle(bundle), false, StandardCharsets.UTF_8);
    }

    public AssetResult assetGetAllAppTags(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "assetGetAllAppTags: bundle is null");
            return new AssetResult(-1);
        }
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 3);
        int assetType = bundle.getInt(BUNDLE_ASSETTYPE);
        boolean isPasswordVault = true;
        if (!(assetType == 0 || assetType == 1 || assetType == 2)) {
            isPasswordVault = false;
        }
        if (isPasswordVault) {
            return assetHandleOutInfo(context, parseBundle(bundle), false, StandardCharsets.UTF_8);
        }
        return assetHandleOutInfo(context, parseBundle(bundle), false, StandardCharsets.ISO_8859_1);
    }

    public AssetResult assetTransferOutInit(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "assetTransferOutInit: bundle is null");
            return new AssetResult(-1);
        }
        int transferType = bundle.getInt(BUNDLE_TRANSFER_TYPE);
        if (transferType == 1) {
            return cloneOutInit(context, bundle);
        }
        if (transferType == 2) {
            return backupInit(context, bundle);
        }
        Log.e(TAG, "assetTransferOutInit: transferType is invalid, transferType = " + transferType);
        return new AssetResult(-1);
    }

    public AssetResult assetTransferOut(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "assetTransferOut: bundle is null");
            return new AssetResult(-1);
        }
        int transferType = bundle.getInt(BUNDLE_TRANSFER_TYPE);
        if (transferType == 1) {
            return cloneOut(context, bundle);
        }
        if (transferType == 2) {
            return backup(context, bundle);
        }
        Log.e(TAG, "assetTransferOut: transferType is invalid, transferType = " + transferType);
        return new AssetResult(-1);
    }

    public AssetResult assetTransferInInit(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "assetTransferInInit: bundle is null");
            return new AssetResult(-1);
        }
        int transferType = bundle.getInt(BUNDLE_TRANSFER_TYPE);
        if (transferType == 1) {
            return cloneInInit(context, bundle);
        }
        if (transferType == 2) {
            return restoreInit(context, bundle);
        }
        Log.e(TAG, "assetTransferInInit: transferType is invalid, transferType = " + transferType);
        return new AssetResult(-1);
    }

    public AssetResult assetTransferIn(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "assetTransferIn: bundle is null");
            return new AssetResult(-1);
        }
        int transferType = bundle.getInt(BUNDLE_TRANSFER_TYPE);
        if (transferType == 1) {
            return cloneIn(context, bundle);
        }
        if (transferType == 2) {
            return restore(context, bundle);
        }
        Log.e(TAG, "assetTransferIn: transferType is invalid, transferType = " + transferType);
        return new AssetResult(-1);
    }

    public AssetResult assetGetCertificateChain(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "assetGetCertificateChain: bundle is null");
            return new AssetResult(-1);
        }
        int assetType = bundle.getInt(BUNDLE_ASSETTYPE);
        boolean isPasswordVault = true;
        if (!(assetType == 0 || assetType == 1 || assetType == 2)) {
            isPasswordVault = false;
        }
        if (isPasswordVault) {
            List<String> resultInfo = new ArrayList<>();
            resultInfo.add(getNewCerts());
            return new AssetResult(0, resultInfo);
        }
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 20);
        return assetHandleOutInfo(context, parseBundle(bundle), false, StandardCharsets.ISO_8859_1);
    }

    public AssetResult keyAgreement(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "keyAgreement: bundle is null");
            return new AssetResult(-1);
        }
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 22);
        return assetHandleOutInfo(context, parseBundle(bundle), false, StandardCharsets.ISO_8859_1);
    }

    public AssetResult assetLoadKey(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "assetLoadKey: bundle is null");
            return new AssetResult(-1);
        }
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 23);
        return assetHandleOutInfo(context, parseBundle(bundle), false, StandardCharsets.ISO_8859_1);
    }

    public AssetResult assetGenerate(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "assetGenerate: bundle is null");
            return new AssetResult(-1);
        }
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 13);
        return assetHandleOutInfo(context, parseBundle(bundle), false, StandardCharsets.ISO_8859_1);
    }

    public AssetResult assetOperate(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "assetOperate: bundle is null");
            return new AssetResult(-1);
        }
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 14);
        return assetHandleOutInfo(context, parseBundle(bundle), false, StandardCharsets.ISO_8859_1);
    }

    public AssetResult assetLoadEntry(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "loadAssetEntry: bundle is null");
            return new AssetResult(-1);
        }
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 21);
        return assetHandleOutInfo(context, parseBundle(bundle), false, StandardCharsets.ISO_8859_1);
    }

    public AssetResult assetUnwrap(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "assetUnwrap: bundle is null");
            return new AssetResult(-1);
        }
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 16);
        return assetHandleOutInfo(context, parseBundle(bundle), false, StandardCharsets.ISO_8859_1);
    }

    public AssetResult assetWrap(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "assetWrap: bundle is null");
            return new AssetResult(-1);
        }
        Log.d(TAG, "assetWrap Enter");
        if (bundle.getInt(BUNDLE_KEY_WRAP_TYPE) == 4) {
            try {
                byte[] byteCertChain = bundle.getByteArray(BUNDLE_CERT_CHAIN);
                if (byteCertChain == null) {
                    return new AssetResult(-1);
                }
                JSONObject jsonObject = new JSONObject(new String(byteCertChain, StandardCharsets.ISO_8859_1));
                int certsNum = ((Integer) jsonObject.get("certs")).intValue();
                X509Certificate[] certs = new X509Certificate[certsNum];
                for (int i = 0; i < certsNum; i++) {
                    Object certObject = jsonObject.get("cert" + (i + 1));
                    if (certObject instanceof String) {
                        certs[i] = getCertificate(((String) certObject).getBytes(Charset.defaultCharset()));
                    }
                }
                if (!new HwTrustManager().verifyCertificateChain(certs)) {
                    Log.e(TAG, "assetWrap verifyCertificateChain failed!");
                    return new AssetResult(-10);
                }
                bundle.putByteArray(BUNDLE_TRANSFER_ECDH_PUBKEY, certs[0].getPublicKey().getEncoded());
            } catch (IOException | ArrayIndexOutOfBoundsException | CertificateException | JSONException e) {
                Log.e(TAG, "assetWrap " + e.getMessage());
                return new AssetResult(-10);
            }
        }
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 15);
        return assetHandleOutInfo(context, parseBundle(bundle), false, StandardCharsets.ISO_8859_1);
    }

    public AssetResult assetCloudSync(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "assetCloudSync: bundle is null");
            return new AssetResult(-1);
        }
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 19);
        return assetHandleOutInfo(context, parseBundle(bundle), false, StandardCharsets.ISO_8859_1);
    }

    public AssetResult acquireLock(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "acquireLock: bundle is null");
            return new AssetResult(-1);
        }
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 17);
        return assetHandle(context, parseBundle(bundle));
    }

    public AssetResult releaseLock(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "releaseLock: bundle is null");
            return new AssetResult(-1);
        }
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 18);
        return assetHandle(context, parseBundle(bundle));
    }

    private AssetResult cloneOutInit(Context context, Bundle bundle) {
        String certChain = bundle.getString(BUNDLE_TRANSFER_DATA);
        if (certChain == null) {
            Log.e(TAG, "cloneOutInit certChain is null!");
            return new AssetResult(-1);
        }
        try {
            JSONObject jsonObject = new JSONObject(certChain);
            int certsNum = ((Integer) jsonObject.get("certs")).intValue();
            X509Certificate[] certs = new X509Certificate[certsNum];
            for (int i = 0; i < certsNum; i++) {
                certs[i] = getCertificate(Base64.decode((String) jsonObject.get(BuildConfig.FLAVOR + i), 0));
            }
            if (!new HwTrustManager().verifyCertificateChain(certs)) {
                Log.e(TAG, "cloneOutInit verifyCertificateChain failed!");
                return new AssetResult(-10);
            }
            X509Certificate cert = certs[0];
            HwKeymasterArguments assetArgs = new HwKeymasterArguments();
            assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETTYPE, bundle.getInt(BUNDLE_ASSETTYPE));
            assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 7);
            assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_CONTEXT_PUBKEY, getContextPubKey(context));
            assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_TRANSFER_ECDH_PUBKEY, cert.getPublicKey().getEncoded());
            assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_TRANSFER_ECDH_ALIAS, KEYCHAIN_CLONE_ALIAS.getBytes(StandardCharsets.UTF_8));
            Certificate[] localCerts = getCertificateChain();
            if (localCerts == null) {
                Log.e(TAG, "cloneOutInit localCerts failed!");
                return new AssetResult(-10);
            }
            AssetResult transResult = transferHandleOutInfo(assetArgs);
            if (transResult.resultCode != 0) {
                return transResult;
            }
            String itemString = transResult.resultInfo.get(0);
            JSONObject result = new JSONObject();
            try {
                String aliasTran = (String) jsonObject.get("alias");
                if (aliasTran == null) {
                    Log.e(TAG, "cloneOutInit can not get the alias!");
                    return new AssetResult(-10);
                }
                result.put("assettype", bundle.getInt(BUNDLE_ASSETTYPE));
                result.put("alias", aliasTran);
                result.put("transkeys", itemString);
                result.put("pubkey", Base64.encodeToString(localCerts[0].getPublicKey().getEncoded(), 0));
                List<String> resultInfo = new ArrayList<>();
                resultInfo.add(result.toString());
                return new AssetResult(0, resultInfo, transResult.resultNumber);
            } catch (JSONException e) {
                Log.e(TAG, "cloneOutInit getNewCerts json failed JSONException!");
            }
        } catch (IOException | CertificateException | JSONException e2) {
            Log.e(TAG, "cloneOutInit " + e2.getMessage());
            return new AssetResult(-10);
        }
    }

    private AssetResult cloneOut(Context context, Bundle bundle) {
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 4);
        return transferHandleOutInfo(parseBundle(bundle));
    }

    private AssetResult cloneInInit(Context context, Bundle bundle) {
        String transData = bundle.getString(BUNDLE_TRANSFER_DATA);
        if (transData == null) {
            Log.e(TAG, "cloneInInit transData is NULL!");
            return new AssetResult(-1);
        }
        String alias = BuildConfig.FLAVOR;
        String transKeys = BuildConfig.FLAVOR;
        String parternerPubKey = BuildConfig.FLAVOR;
        int assetType = -1;
        try {
            JSONObject jsonObject = new JSONObject(transData);
            assetType = ((Integer) jsonObject.get("assettype")).intValue();
            alias = (String) jsonObject.get("alias");
            transKeys = (String) jsonObject.get("transkeys");
            parternerPubKey = (String) jsonObject.get("pubkey");
            if (!(alias == null || transKeys == null)) {
                if (parternerPubKey != null) {
                    HwKeymasterArguments assetArgs = new HwKeymasterArguments();
                    assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_TRANSFER_DATA, getBytesHelper(transKeys));
                    assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 8);
                    assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETTYPE, assetType);
                    assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_CONTEXT_PUBKEY, getContextPubKey(context));
                    assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_TRANSFER_ECDH_PUBKEY, Base64.decode(parternerPubKey, 0));
                    assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_TRANSFER_ECDH_ALIAS, getBytesHelper(alias));
                    return transferHandle(assetArgs);
                }
            }
            Log.e(TAG, "cloneInInit get data failed!");
            return new AssetResult(-1);
        } catch (JSONException e) {
            Log.e(TAG, "cloneInInit json failed JSONException!");
        }
    }

    private AssetResult cloneIn(Context context, Bundle bundle) {
        String transData = bundle.getString(BUNDLE_TRANSFER_DATA);
        if (transData == null) {
            Log.e(TAG, "cloneIn transData is NULL!");
            return new AssetResult(-1);
        }
        HwKeymasterArguments assetArgs = new HwKeymasterArguments();
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_TRANSFER_DATA, getBytesHelper(transData));
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 5);
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_CONTEXT_PUBKEY, getContextPubKey(context));
        return transferHandle(assetArgs);
    }

    private AssetResult backupInit(Context context, Bundle bundle) {
        HwKeymasterArguments assetArgs = new HwKeymasterArguments();
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETTYPE, bundle.getInt(BUNDLE_ASSETTYPE));
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 9);
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_CONTEXT_PUBKEY, getContextPubKey(context));
        AssetResult transResult = transferHandle(assetArgs);
        if (transResult.resultCode != 0) {
            return transResult;
        }
        JSONObject result = new JSONObject();
        try {
            result.put("assettype", bundle.getInt(BUNDLE_ASSETTYPE));
            List<String> resultInfo = new ArrayList<>();
            resultInfo.add(result.toString());
            return new AssetResult(0, resultInfo, transResult.resultNumber);
        } catch (JSONException e) {
            Log.e(TAG, "backupInit json failed JSONException!");
            return new AssetResult(-10);
        }
    }

    private AssetResult backup(Context context, Bundle bundle) {
        bundle.putByteArray(BUNDLE_CONTEXT_PUBKEY, getContextPubKey(context));
        bundle.putInt(BUNDLE_OPCODE, 10);
        return transferHandleOutInfo(parseBundle(bundle));
    }

    private AssetResult restoreInit(Context context, Bundle bundle) {
        String transData = bundle.getString(BUNDLE_TRANSFER_DATA);
        if (transData == null) {
            Log.e(TAG, "restoreInit transData is NULL!");
            return new AssetResult(-1);
        }
        int assetType = -1;
        try {
            assetType = ((Integer) new JSONObject(transData).get("assettype")).intValue();
        } catch (JSONException e) {
            Log.e(TAG, "restoreInit json failed JSONException!");
        }
        HwKeymasterArguments assetArgs = new HwKeymasterArguments();
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 11);
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETTYPE, assetType);
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_CONTEXT_PUBKEY, getContextPubKey(context));
        return transferHandle(assetArgs);
    }

    private AssetResult restore(Context context, Bundle bundle) {
        String transData = bundle.getString(BUNDLE_TRANSFER_DATA);
        if (transData == null) {
            Log.e(TAG, "restore transData is NULL!");
            return new AssetResult(-1);
        }
        HwKeymasterArguments assetArgs = new HwKeymasterArguments();
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_TRANSFER_DATA, getBytesHelper(transData));
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 12);
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_CONTEXT_PUBKEY, getContextPubKey(context));
        return transferHandle(assetArgs);
    }

    private X509Certificate getCertificate(byte[] bData) throws CertificateException, IOException {
        InputStream in = new ByteArrayInputStream(bData);
        X509Certificate x509Cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
        in.close();
        return x509Cert;
    }

    private KeyPair generateKeyPair(String alias) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(HwKeyProperties.KEY_ALGORITHM_EC, new HwUniversalKeyStoreProvider());
            Calendar start = new GregorianCalendar();
            Calendar end = new GregorianCalendar();
            end.add(1, 10);
            keyPairGenerator.initialize(new KeyGenParameterSpec.Builder(alias, 3).setDigests(HwKeyProperties.DIGEST_SHA256).setEncryptionPaddings(HwKeyProperties.ENCRYPTION_PADDING_NONE).setCertificateSerialNumber(BigInteger.valueOf(1337)).setCertificateNotBefore(start.getTime()).setCertificateNotAfter(end.getTime()).setAttestationChallenge("hello world".getBytes(StandardCharsets.UTF_8)).setUserAuthenticationRequired(false).build());
            return keyPairGenerator.generateKeyPair();
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
            Log.e(TAG, "generateKeyPair " + e.getMessage());
            return null;
        }
    }

    private Certificate[] getCertificateChain() {
        Certificate[] certificates = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("HwKeyStore");
            keyStore.load(null);
            KeyStore.Entry entry = keyStore.getEntry(KEYCHAIN_CLONE_ALIAS, null);
            if (entry == null) {
                Log.e(TAG, "getCertificateChain generateKeyPair!");
                if (generateKeyPair(KEYCHAIN_CLONE_ALIAS) == null) {
                    Log.w(TAG, "can not get Entry");
                    return null;
                }
                entry = keyStore.getEntry(KEYCHAIN_CLONE_ALIAS, null);
                if (entry == null) {
                    Log.w(TAG, "Entry not exists");
                    return null;
                }
            }
            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                Log.w(TAG, "Not an instance of a PrivateKeyEntry");
                return null;
            }
            certificates = ((KeyStore.PrivateKeyEntry) entry).getCertificateChain();
            Log.i(TAG, "getCertificateChain succ!");
            return certificates;
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateException e) {
            Log.e(TAG, "getCertificateChain " + e.getMessage());
        }
    }

    private byte[] getContextPubKey(Context mContext) {
        String pubKey = BuildConfig.FLAVOR;
        try {
            pubKey = ((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 64).signatures[0].toByteArray()))).getPublicKey().toString();
        } catch (PackageManager.NameNotFoundException | CertificateException e) {
            Log.e(TAG, "get Context public key:" + e.getMessage());
        }
        return getBytesHelper(pubKey);
    }

    private String getNewCerts() {
        JSONObject result = new JSONObject();
        Certificate[] certs = getCertificateChain();
        if (certs != null && certs.length > 0) {
            try {
                result.put("alias", KEYCHAIN_CLONE_ALIAS);
                result.put("certs", certs.length);
            } catch (JSONException e) {
                Log.e(TAG, "getNewCerts json failed JSONException!");
            }
            for (int i = 0; i < certs.length; i++) {
                try {
                    result.put(BuildConfig.FLAVOR + i, Base64.encodeToString(certs[i].getEncoded(), 0));
                } catch (CertificateEncodingException | JSONException e2) {
                    Log.e(TAG, "get certs: " + e2.getMessage());
                }
            }
        }
        return result.toString();
    }

    private X509Certificate getValidCertificate(byte[] byteCertChain) {
        try {
            JSONObject jsonObject = new JSONObject(new String(byteCertChain, StandardCharsets.ISO_8859_1));
            int certsNum = ((Integer) jsonObject.get("certs")).intValue();
            if (certsNum > 4) {
                Log.e(TAG, "Invalid Certificate Num!");
                return null;
            }
            X509Certificate[] certs = new X509Certificate[certsNum];
            for (int i = 0; i < certsNum; i++) {
                Object certObject = jsonObject.get("cert" + (i + 1));
                if (certObject instanceof String) {
                    certs[i] = getCertificate(((String) certObject).getBytes(Charset.defaultCharset()));
                }
            }
            if (new HwTrustManager().verifyCertificateChain(certs)) {
                return certs[0];
            }
            Log.e(TAG, "verifyCertificateChain failed!");
            return null;
        } catch (IOException | ArrayIndexOutOfBoundsException | CertificateException | JSONException e) {
            Log.e(TAG, "getValidCertificate " + e.getMessage());
            return null;
        }
    }

    public boolean registerObserver(HwAssetObserver observer, String dataOwner, int event, int dataType) {
        Log.i(TAG, "registerObserver start");
        boolean result = HwKeystoreManager.getInstance().registerObserver(new ObserverTransport(observer, null), dataOwner, event, dataType);
        Log.i(TAG, "registerObserver result: " + result);
        return result;
    }

    public boolean registerObserver(HwAssetObserver observer, String dataOwner, int event, int dataType, Looper looper) {
        Log.i(TAG, "registerObserverEx start");
        boolean result = HwKeystoreManager.getInstance().registerObserver(new ObserverTransport(observer, looper), dataOwner, event, dataType);
        Log.i(TAG, "registerObserverEx result: " + result);
        return result;
    }

    public boolean unRegisterObserver(String dataOwner, int event, int dataType) {
        Log.i(TAG, "unRegisterObserver start");
        boolean result = HwKeystoreManager.getInstance().unRegisterObserver(dataOwner, event, dataType);
        Log.i(TAG, "unRegisterObserver result:" + result);
        return result;
    }

    public AssetResult getSecurityCapabilities(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "getSecurityCapabilities: bundle is null");
            return new AssetResult(-1);
        }
        HwKeymasterArguments params = parseBundle(bundle);
        HwKeymasterCertificateChain rawChain = new HwKeymasterCertificateChain();
        int resCode = HwKeystoreManager.getInstance().getSecurityCapabilities(params, rawChain);
        if (resCode < 0) {
            Log.e(TAG, "getSecurityCapabilities failed");
            return new AssetResult(resCode);
        }
        List<byte[]> outChain = rawChain.getCertificates();
        if (outChain == null || outChain.isEmpty()) {
            Log.e(TAG, "getSecurityCapabilities outChain is null");
            return new AssetResult(-10);
        }
        List<String> resultInfo = new ArrayList<>();
        resultInfo.add(new String(outChain.get(0), StandardCharsets.ISO_8859_1));
        return new AssetResult(0, resultInfo);
    }

    public AssetResult getSecurityChallenge(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "getSecurityChallenge: bundle is null");
            return new AssetResult(-1);
        }
        HwKeymasterArguments params = parseBundle(bundle);
        HwKeymasterCertificateChain rawChain = new HwKeymasterCertificateChain();
        int resCode = HwKeystoreManager.getInstance().getSecurityChallenge(params, rawChain);
        if (resCode < 0) {
            Log.e(TAG, "getSecurityChallenge failed");
            return new AssetResult(resCode);
        }
        List<byte[]> outChain = rawChain.getCertificates();
        if (outChain == null || outChain.isEmpty()) {
            Log.e(TAG, "getSecurityChallenge outChain is null");
            return new AssetResult(-10);
        }
        List<String> resultInfo = new ArrayList<>();
        resultInfo.add(new String(outChain.get(0), StandardCharsets.ISO_8859_1));
        return new AssetResult(0, resultInfo, resCode);
    }

    public AssetResult verifyCertificateChain(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "getSecurityChallenge: bundle is null");
            return new AssetResult(-1);
        }
        try {
            byte[] byteCertChain = bundle.getByteArray(BUNDLE_CERT_CHAIN);
            if (byteCertChain == null) {
                return new AssetResult(-1);
            }
            X509Certificate cert = getValidCertificate(byteCertChain);
            if (cert == null) {
                Log.e(TAG, "verifyCertificateChain failed");
                return new AssetResult(-10);
            }
            int resCode = HwKeystoreManager.getInstance().verifySecurityChallenge(parseBundle(bundle));
            if (resCode != 0) {
                Log.e(TAG, "verifySecurityChallenge failed");
                return new AssetResult(-10);
            }
            List<String> resultInfo = new ArrayList<>();
            resultInfo.add(new String(cert.getPublicKey().getEncoded(), StandardCharsets.ISO_8859_1));
            return new AssetResult(0, resultInfo, resCode);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "verifyCertificateChain " + e.getMessage());
            return new AssetResult(-1);
        }
    }

    private class ObserverTransport extends IHwAssetObserver.Stub {
        private HwAssetObserver mObserver;
        private final Handler mObserverHandler;

        ObserverTransport(HwAssetObserver observer, Looper looper) {
            this.mObserver = observer;
            if (looper == null) {
                this.mObserverHandler = new Handler(HwAssetManager.this) {
                    /* class com.huawei.security.hwassetmanager.HwAssetManager.ObserverTransport.AnonymousClass1 */

                    @Override // android.os.Handler
                    public void handleMessage(Message msg) {
                        ObserverTransport.this.observerHandleMessage(msg);
                    }
                };
            } else {
                this.mObserverHandler = new Handler(looper, HwAssetManager.this) {
                    /* class com.huawei.security.hwassetmanager.HwAssetManager.ObserverTransport.AnonymousClass2 */

                    @Override // android.os.Handler
                    public void handleMessage(Message msg) {
                        ObserverTransport.this.observerHandleMessage(msg);
                    }
                };
            }
        }

        public void onEvent(int event, int dataType, HwKeymasterBlob extra) {
            Message msg = Message.obtain();
            msg.arg1 = event;
            msg.arg2 = dataType;
            msg.obj = blobToBundle(extra);
            this.mObserverHandler.sendMessage(msg);
        }

        private Bundle blobToBundle(HwKeymasterBlob extra) {
            Bundle bundle = new Bundle();
            JSONObject obj = extra.getBlob();
            if (obj == null) {
                Log.e(HwAssetManager.TAG, "ObserverTransport: extra is null");
                return bundle;
            }
            Iterator<String> keys = obj.keys();
            while (keys.hasNext()) {
                String rawKey = keys.next();
                try {
                    String value = obj.getString(rawKey);
                    if (value == null) {
                        Log.e(HwAssetManager.TAG, "Value from json is null!");
                    } else {
                        int tagType = HwKeymasterDefs.getTagType(Integer.parseInt(rawKey));
                        if (tagType == -1879048192) {
                            bundle.putByteArray(rawKey, Base64.decode(value, 0));
                        } else if (tagType == 268435456) {
                            bundle.putInt(rawKey, Integer.parseInt(value));
                        } else {
                            Log.e(HwAssetManager.TAG, "ObserverTransport: invalid onEvent tag type: " + Integer.toHexString(tagType));
                        }
                    }
                } catch (NumberFormatException | JSONException e) {
                    Log.w(HwAssetManager.TAG, "blobToBundle exception thrown: " + e.getMessage());
                }
            }
            Log.i(HwAssetManager.TAG, "blobToBundle succeed");
            return bundle;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void observerHandleMessage(Message msg) {
            this.mObserver.onEvent(msg.arg1, msg.arg2, (Bundle) msg.obj);
        }
    }
}
