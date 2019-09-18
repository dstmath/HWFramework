package com.huawei.security.keystore;

import android.content.Context;
import android.content.pm.PackageManager;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Base64;
import android.util.Log;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.conscrypt.HwTrustManager;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterCertificateChain;
import com.huawei.security.keymaster.HwKeymasterDefs;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

public class HwAssetStoreUtils {
    private static final int ASSET_OPCODE_BACKUPCARRYON = 10;
    private static final int ASSET_OPCODE_BACKUPSTART = 9;
    private static final int ASSET_OPCODE_CLONEINCARRYON = 5;
    private static final int ASSET_OPCODE_CLONEINSTART = 8;
    private static final int ASSET_OPCODE_CLONEOUTCARRYON = 4;
    private static final int ASSET_OPCODE_CLONEOUTSTART = 7;
    private static final int ASSET_OPCODE_DELETE = 1;
    private static final int ASSET_OPCODE_GETAPPS = 3;
    private static final int ASSET_OPCODE_INSERT = 0;
    private static final int ASSET_OPCODE_RESTORECARRYON = 12;
    private static final int ASSET_OPCODE_RESTORESTART = 11;
    private static final int ASSET_OPCODE_SELECT = 2;
    private static final int ASSET_OPCODE_UPDATE = 6;
    private static final int ASSET_RETRIVE_CONTINUE = 2;
    private static final int ASSET_RETRIVE_FROMBEGIN = 1;
    private static final int ASSET_RETRIVE_ONCE = 0;
    public static final int ASSET_TYPE_CREADIT_CARD = 1;
    public static final int ASSET_TYPE_TOKEN = 2;
    public static final int ASSET_TYPE_USERNAME_PASSWORD = 0;
    private static final String TAG = "HwAssetStoreUtils";
    private String mAlias = "hwkeychainclone";

    private HwAssetStoreUtils() {
        HwUniversalKeyStoreProvider.install();
    }

    public static HwAssetStoreUtils getInstance() {
        return new HwAssetStoreUtils();
    }

    public String insert(Context mContext, String apptag, String batchAsset, String aeadAsset, int assetType, String extInfo, int authenticateLimitation, int syncLimitation, int accessLimitation) {
        String str = apptag;
        String str2 = batchAsset;
        String str3 = aeadAsset;
        String str4 = extInfo;
        HwKeymasterArguments assetArgs = new HwKeymasterArguments();
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_PUBLICKEY, getPubKey(mContext).getBytes(StandardCharsets.UTF_8));
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_APPTAG, str != null ? str.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8));
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_BATCHASSET, str2 != null ? str2.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8));
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_AEADASSET, str3 != null ? str3.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8));
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETTYPE, assetType);
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_EXTINFO, str4 != null ? str4.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8));
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_AUTHENTICATELIMITATION, authenticateLimitation);
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_SYNCLIMITATION, syncLimitation);
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_ACCESSLIMITATION, accessLimitation);
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 0);
        HwKeymasterCertificateChain outChain = new HwKeymasterCertificateChain();
        if (HwKeystoreManager.getInstance().assetHandleReq(assetArgs, outChain) == -1) {
            Log.e(TAG, "insert failed!!");
            return null;
        }
        Iterator it = outChain.getCertificates().iterator();
        if (it.hasNext()) {
            return new String(it.next(), StandardCharsets.UTF_8);
        }
        Log.e(TAG, "insert result is null!!");
        return null;
    }

    public int delete(Context mContext, String assetHandle, String apptag, int assetType) {
        HwKeymasterArguments assetArgs = new HwKeymasterArguments();
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETHANDLE, assetHandle != null ? assetHandle.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8));
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_PUBLICKEY, getPubKey(mContext).getBytes(StandardCharsets.UTF_8));
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_APPTAG, apptag != null ? apptag.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8));
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETTYPE, assetType);
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 1);
        return HwKeystoreManager.getInstance().assetHandleReq(assetArgs, new HwKeymasterCertificateChain());
    }

    public String update(Context mContext, String assetHandle, String apptag, String batchAsset, String aeadAsset, int assetType, String extInfo, int authenticateLimitation, int syncLimitation, int accessLimitation) {
        String str = assetHandle;
        String str2 = apptag;
        String str3 = batchAsset;
        String str4 = aeadAsset;
        String str5 = extInfo;
        HwKeymasterArguments assetArgs = new HwKeymasterArguments();
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETHANDLE, str != null ? str.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8));
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_PUBLICKEY, getPubKey(mContext).getBytes(StandardCharsets.UTF_8));
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_APPTAG, str2 != null ? str2.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8));
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_BATCHASSET, str3 != null ? str3.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8));
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_AEADASSET, str4 != null ? str4.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8));
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETTYPE, assetType);
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_EXTINFO, str5 != null ? str5.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8));
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_AUTHENTICATELIMITATION, authenticateLimitation);
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_SYNCLIMITATION, syncLimitation);
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_ACCESSLIMITATION, accessLimitation);
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 6);
        HwKeymasterCertificateChain outChain = new HwKeymasterCertificateChain();
        if (HwKeystoreManager.getInstance().assetHandleReq(assetArgs, outChain) == -1) {
            Log.e(TAG, "update failed!!");
            return null;
        }
        Iterator it = outChain.getCertificates().iterator();
        if (it.hasNext()) {
            return new String(it.next(), StandardCharsets.UTF_8);
        }
        Log.e(TAG, "update result is null!!");
        return null;
    }

    public Collection<byte[]> select(Context mContext, String apptag, int assetType, boolean resetFlag) {
        HwKeymasterCertificateChain outChain = new HwKeymasterCertificateChain();
        HwKeymasterArguments assetArgs = new HwKeymasterArguments();
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_PUBLICKEY, getPubKey(mContext).getBytes(StandardCharsets.UTF_8));
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_APPTAG, apptag != null ? apptag.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8));
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETTYPE, assetType);
        if (resetFlag) {
            assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_RESETFLAG, 1);
        } else {
            assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_RESETFLAG, 2);
        }
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 2);
        if (HwKeystoreManager.getInstance().assetHandleReq(assetArgs, outChain) != -1) {
            return outChain.getCertificates();
        }
        Log.e(TAG, "select failed!!");
        return null;
    }

    public String select(Context mContext, String apptag, String assetHandle, int assetType) {
        HwKeymasterCertificateChain outChain = new HwKeymasterCertificateChain();
        HwKeymasterArguments assetArgs = new HwKeymasterArguments();
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETHANDLE, assetHandle != null ? assetHandle.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8));
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_PUBLICKEY, getPubKey(mContext).getBytes(StandardCharsets.UTF_8));
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_APPTAG, apptag != null ? apptag.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8));
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETTYPE, assetType);
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 2);
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_RESETFLAG, 0);
        if (HwKeystoreManager.getInstance().assetHandleReq(assetArgs, outChain) == -1) {
            Log.e(TAG, "select failed!!");
            return null;
        }
        Iterator it = outChain.getCertificates().iterator();
        if (it.hasNext()) {
            return new String(it.next(), StandardCharsets.UTF_8);
        }
        Log.e(TAG, "select result is null!!");
        return null;
    }

    public Collection<byte[]> getAllApptags(Context mContext, int assetType, boolean resetFlag) {
        HwKeymasterCertificateChain outChain = new HwKeymasterCertificateChain();
        HwKeymasterArguments assetArgs = new HwKeymasterArguments();
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETTYPE, assetType);
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 3);
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_PUBLICKEY, getPubKey(mContext).getBytes(StandardCharsets.UTF_8));
        if (resetFlag) {
            assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_RESETFLAG, 1);
        } else {
            assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_RESETFLAG, 2);
        }
        if (HwKeystoreManager.getInstance().assetHandleReq(assetArgs, outChain) != -1) {
            return outChain.getCertificates();
        }
        Log.e(TAG, "getAllApptags failed!!");
        return null;
    }

    public int cloneOutStart(Context mContext, int assetType, String certchain, TransKeys transKeys) {
        JSONObject result;
        int i = assetType;
        String str = certchain;
        if (str == null) {
            Log.e(TAG, "cloneOutStart chain is null!!");
            return -1;
        }
        JSONObject jsonObject = null;
        X509Certificate cert = null;
        try {
            jsonObject = new JSONObject(str);
            int certsnum = ((Integer) jsonObject.get("certs")).intValue();
            X509Certificate[] certs = new X509Certificate[certsnum];
            for (int i2 = 0; i2 < certsnum; i2++) {
                certs[i2] = getCertificate(Base64.decode((String) jsonObject.get("" + i2), 0));
            }
            if (!new HwTrustManager().verifyCertificateChain(certs)) {
                Log.e(TAG, "cloneOutStart verifyCertificateChain failed!!");
                return -1;
            }
            cert = certs[0];
            if (cert == null) {
                return -1;
            }
            HwKeymasterArguments assetArgs = new HwKeymasterArguments();
            assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETTYPE, i);
            assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 7);
            assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_PUBLICKEY, getPubKey(mContext).getBytes(StandardCharsets.UTF_8));
            assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_PUBLICKEY_FORECDH, cert.getPublicKey().getEncoded());
            Certificate[] localcerts = getCertificateChain();
            if (localcerts == null) {
                Log.e(TAG, "cloneOutStart localcerts failed!!");
                return -1;
            }
            assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_ALIAS_FORECDH, this.mAlias.getBytes(StandardCharsets.UTF_8));
            HwKeymasterCertificateChain outChain = new HwKeymasterCertificateChain();
            int totalNum = HwKeystoreManager.getInstance().assetHandleReq(assetArgs, outChain);
            if (totalNum == -1) {
                Log.e(TAG, "cloneOutStart failed!!");
                return -1;
            }
            Iterator it = outChain.getCertificates().iterator();
            if (it.hasNext()) {
                String itemstring = new String(it.next(), StandardCharsets.UTF_8);
                JSONObject result2 = new JSONObject();
                try {
                    String aliastran = (String) jsonObject.get("alias");
                    if (aliastran == null) {
                        try {
                            Log.e(TAG, "cloneOutStart can not get the alias!!");
                            return -1;
                        } catch (JSONException e) {
                            result = result2;
                            Log.e(TAG, "getNewCerts json failed JSONException!!");
                            transKeys.mtranskeys = result.toString();
                            return totalNum;
                        }
                    } else {
                        result = result2;
                        try {
                            result.put("assettype", i);
                            result.put("alias", aliastran);
                            result.put("transkeys", itemstring);
                            String str2 = aliastran;
                            result.put("pubkey", Base64.encodeToString(localcerts[0].getPublicKey().getEncoded(), 0));
                        } catch (JSONException e2) {
                        }
                        transKeys.mtranskeys = result.toString();
                        return totalNum;
                    }
                } catch (JSONException e3) {
                    result = result2;
                    Log.e(TAG, "getNewCerts json failed JSONException!!");
                    transKeys.mtranskeys = result.toString();
                    return totalNum;
                }
            } else {
                TransKeys transKeys2 = transKeys;
                Log.e(TAG, "cloneOut transkey is null!!");
                return -1;
            }
        } catch (JSONException e4) {
            Log.e(TAG, "cloneOutStart JSONException failed!!");
        } catch (CertificateException e5) {
            Log.e(TAG, "cloneOutStart CertificateException failed!!");
        } catch (NoSuchProviderException e6) {
            Log.e(TAG, "cloneOutStart NoSuchProviderException failed!!");
        } catch (NoSuchAlgorithmException e7) {
            Log.e(TAG, "cloneOutStart NoSuchAlgorithmException failed!!");
        } catch (InvalidKeyException e8) {
            Log.e(TAG, "cloneOutStart InvalidKeyException failed!!");
        } catch (SignatureException e9) {
            Log.e(TAG, "cloneOutStart SignatureException failed!!");
        } catch (IOException e10) {
            Log.e(TAG, "cloneOutStart IOException failed!!");
        }
    }

    public String cloneOut(Context mContext) {
        HwKeymasterArguments assetArgs = new HwKeymasterArguments();
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 4);
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_PUBLICKEY, getPubKey(mContext).getBytes(StandardCharsets.UTF_8));
        HwKeymasterCertificateChain outChain = new HwKeymasterCertificateChain();
        if (HwKeystoreManager.getInstance().assetHandleReq(assetArgs, outChain) == -1) {
            Log.e(TAG, "cloneOut failed!!");
            return null;
        }
        Iterator it = outChain.getCertificates().iterator();
        if (it.hasNext()) {
            return new String(it.next(), StandardCharsets.UTF_8);
        }
        Log.e(TAG, "cloneOut data is null!!");
        return null;
    }

    public int cloneInStart(Context mContext, String row) {
        if (row == null) {
            return -1;
        }
        String alias = "";
        String stranskeys = "";
        String parternerpubkey = "";
        int assetType = -1;
        try {
            JSONObject jsonObject = new JSONObject(row);
            assetType = ((Integer) jsonObject.get("assettype")).intValue();
            alias = (String) jsonObject.get("alias");
            stranskeys = (String) jsonObject.get("transkeys");
            parternerpubkey = (String) jsonObject.get("pubkey");
            if (!(alias == null || stranskeys == null)) {
                if (parternerpubkey != null) {
                    byte[] transkeys = stranskeys.getBytes(StandardCharsets.UTF_8);
                    HwKeymasterArguments assetArgs = new HwKeymasterArguments();
                    assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_CLONE_INDATA, transkeys);
                    assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 8);
                    assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETTYPE, assetType);
                    assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_PUBLICKEY, getPubKey(mContext).getBytes(StandardCharsets.UTF_8));
                    assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_PUBLICKEY_FORECDH, Base64.decode(parternerpubkey, 0));
                    assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_ALIAS_FORECDH, alias.getBytes(StandardCharsets.UTF_8));
                    if (HwKeystoreManager.getInstance().assetHandleReq(assetArgs, new HwKeymasterCertificateChain()) != -1) {
                        return 0;
                    }
                    Log.e(TAG, "cloneInStart failed!!");
                    return -1;
                }
            }
            Log.e(TAG, "cloneInStart get data failed!!");
            return -1;
        } catch (JSONException e) {
            Log.e(TAG, "cloneInStart json failed JSONException!!");
        }
    }

    public int cloneIn(Context mContext, String row) {
        if (row == null) {
            return -1;
        }
        HwKeymasterArguments assetArgs = new HwKeymasterArguments();
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_CLONE_INDATA, row.getBytes(StandardCharsets.UTF_8));
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 5);
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_PUBLICKEY, getPubKey(mContext).getBytes(StandardCharsets.UTF_8));
        return HwKeystoreManager.getInstance().assetHandleReq(assetArgs, new HwKeymasterCertificateChain());
    }

    public int backupStart(Context mContext, int assetType, TransKeys transKeys) {
        HwKeymasterArguments assetArgs = new HwKeymasterArguments();
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETTYPE, assetType);
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 9);
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_PUBLICKEY, getPubKey(mContext).getBytes(StandardCharsets.UTF_8));
        int totalNum = HwKeystoreManager.getInstance().assetHandleReq(assetArgs, new HwKeymasterCertificateChain());
        if (totalNum == -1) {
            Log.e(TAG, "backupStart failed!!");
            return -1;
        }
        JSONObject result = new JSONObject();
        try {
            result.put("assettype", assetType);
            transKeys.mtranskeys = result.toString();
            return totalNum;
        } catch (JSONException e) {
            Log.e(TAG, "backupStart json failed JSONException!!");
            return -1;
        }
    }

    public String backup(Context mContext) {
        HwKeymasterArguments assetArgs = new HwKeymasterArguments();
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, 10);
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_PUBLICKEY, getPubKey(mContext).getBytes(StandardCharsets.UTF_8));
        HwKeymasterCertificateChain outChain = new HwKeymasterCertificateChain();
        if (HwKeystoreManager.getInstance().assetHandleReq(assetArgs, outChain) == -1) {
            Log.e(TAG, "backup failed!!");
            return null;
        }
        Iterator it = outChain.getCertificates().iterator();
        if (it.hasNext()) {
            return new String(it.next(), StandardCharsets.UTF_8);
        }
        Log.e(TAG, "backup data is null!!");
        return null;
    }

    public int restoreStart(Context mContext, String row) {
        if (row == null) {
            return -1;
        }
        int assetType = -1;
        try {
            assetType = ((Integer) new JSONObject(row).get("assettype")).intValue();
        } catch (JSONException e) {
            Log.e(TAG, "restoreStart json failed JSONException!!");
        }
        HwKeymasterArguments assetArgs = new HwKeymasterArguments();
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, ASSET_OPCODE_RESTORESTART);
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_ASSETTYPE, assetType);
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_PUBLICKEY, getPubKey(mContext).getBytes(StandardCharsets.UTF_8));
        if (HwKeystoreManager.getInstance().assetHandleReq(assetArgs, new HwKeymasterCertificateChain()) != -1) {
            return 0;
        }
        Log.e(TAG, "restoreStart failed!!");
        return -1;
    }

    public int restore(Context mContext, String row) {
        if (row == null) {
            return -1;
        }
        HwKeymasterArguments assetArgs = new HwKeymasterArguments();
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_CLONE_INDATA, row.getBytes(StandardCharsets.UTF_8));
        assetArgs.addEnum(HwKeymasterDefs.KM_TAG_ASSETSTORE_OPCODE, ASSET_OPCODE_RESTORECARRYON);
        assetArgs.addBytes(HwKeymasterDefs.KM_TAG_ASSETSTORE_PUBLICKEY, getPubKey(mContext).getBytes(StandardCharsets.UTF_8));
        return HwKeystoreManager.getInstance().assetHandleReq(assetArgs, new HwKeymasterCertificateChain());
    }

    public static X509Certificate getCertificate(byte[] bData) throws CertificateException, IOException {
        return getCertificate((InputStream) new ByteArrayInputStream(bData));
    }

    public static X509Certificate getCertificate(InputStream in) throws CertificateException, IOException {
        X509Certificate x509Cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
        in.close();
        return x509Cert;
    }

    private KeyPair generateKeyPair(String alias, String keyAlg, int purpose, String digest, String padding) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlg, new HwUniversalKeyStoreProvider());
            Calendar start = new GregorianCalendar();
            Calendar end = new GregorianCalendar();
            end.add(1, 10);
            keyPairGenerator.initialize(new KeyGenParameterSpec.Builder(alias, purpose).setDigests(new String[]{digest}).setEncryptionPaddings(new String[]{padding}).setCertificateSerialNumber(BigInteger.valueOf(1337)).setCertificateNotBefore(start.getTime()).setCertificateNotAfter(end.getTime()).setAttestationChallenge("hello world".getBytes(StandardCharsets.UTF_8)).setUserAuthenticationRequired(false).build());
            return keyPairGenerator.generateKeyPair();
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, "generateKeyPair failed InvalidAlgorithmParameterException!!");
            return null;
        } catch (NoSuchAlgorithmException e2) {
            Log.e(TAG, "generateKeyPair failed NoSuchAlgorithmException!!");
            return null;
        }
    }

    private KeyPair generateKeyPair(String alias) {
        return generateKeyPair(alias, HwKeyProperties.KEY_ALGORITHM_EC, 3, HwKeyProperties.DIGEST_SHA256, HwKeyProperties.ENCRYPTION_PADDING_NONE);
    }

    private Certificate[] getCertificateChain() {
        Certificate[] certificates = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("HwKeyStore");
            keyStore.load(null);
            KeyStore.Entry entry = keyStore.getEntry(this.mAlias, null);
            if (entry == null) {
                Log.e(TAG, "getCertificateChain generateKeyPair!!");
                if (generateKeyPair(this.mAlias) == null) {
                    Log.w(TAG, "can not get Entry");
                    return null;
                }
                entry = keyStore.getEntry(this.mAlias, null);
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
            Log.d(TAG, "getCertificateChain succ!!");
            return certificates;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "getCertificateChain failed NoSuchAlgorithmException!!");
        } catch (IOException e2) {
            Log.e(TAG, "getCertificateChain failed IOException!!");
        } catch (KeyStoreException e3) {
            Log.e(TAG, "getCertificateChain failed KeyStoreException!!");
        } catch (UnrecoverableEntryException e4) {
            Log.e(TAG, "getCertificateChain failed UnrecoverableEntryException!!");
        } catch (CertificateException e5) {
            Log.e(TAG, "getCertificateChain failed CertificateException!!");
        }
    }

    public String getNewCerts() {
        JSONObject result = new JSONObject();
        Certificate[] certs = getCertificateChain();
        if (certs != null && certs.length > 0) {
            try {
                result.put("alias", this.mAlias);
                result.put("certs", certs.length);
            } catch (JSONException e) {
                Log.e(TAG, "getNewCerts json failed JSONException!!");
            }
            for (int i = 0; i < certs.length; i++) {
                try {
                    result.put("" + i, Base64.encodeToString(certs[i].getEncoded(), 0));
                } catch (JSONException e2) {
                    Log.e(TAG, "cloneOutStart json failed!!");
                } catch (CertificateEncodingException e3) {
                    Log.e(TAG, "cloneOutStart json failed CertificateEncodingException!!");
                }
            }
        }
        return result.toString();
    }

    private static String getPublicKey(byte[] signature) {
        try {
            return ((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(signature))).getPublicKey().toString();
        } catch (CertificateException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getPubKey(Context mContext) {
        try {
            return getPublicKey(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 64).signatures[0].toByteArray());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }
}
