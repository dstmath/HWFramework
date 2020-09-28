package com.huawei.security.keystore;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterCertificateChain;
import com.huawei.security.keymaster.HwKeymasterDefs;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;

public abstract class HwAttestationUtils {
    private static final int ATTEST_CHALLENGE_LEN_MAX = 128;
    public static final int ID_TYPE_IMEI = 2;
    public static final int ID_TYPE_MEID = 3;
    public static final int ID_TYPE_SERIAL = 1;
    public static final int ID_TYPE_SKIP_DEFAULT = -65536;
    public static final int ID_TYPE_UDID = -65535;
    private static final String TAG = "HwAttestationUtils";

    private HwAttestationUtils() {
    }

    @NonNull
    @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    public static X509Certificate[] attestDeviceIds(Context context, @Nullable int[] idTypes, @Nullable byte[] attestationChallenge) throws HwDeviceIdAttestationException {
        boolean z = true;
        checkNotNull(idTypes == null, "Missing id types");
        checkArgument(idTypes.length < 5, "idTypes length is too long");
        checkNotNull(attestationChallenge == null, "Missing attestationChallenge");
        if (attestationChallenge.length >= 128) {
            z = false;
        }
        checkArgument(z, "Attestation Challenge is too long");
        if (supportHwPki()) {
            try {
                HwKeymasterArguments attestArgs = new HwKeymasterArguments();
                Log.i(TAG, "attestationChallenge length is: " + attestationChallenge.length);
                attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_CHALLENGE, attestationChallenge);
                addDeviceId(context, attestArgs, idTypes);
                return convertToX509CertChain(generateRawCertChain(attestArgs));
            } catch (IOException | CertificateException e) {
                Log.e(TAG, "Unable to construct certificate chain: " + e.getMessage());
                throw new HwDeviceIdAttestationException("Unable to construct certificate chain", e);
            }
        } else {
            throw new HwDeviceIdAttestationException("Attestation not support for this version");
        }
    }

    private static void addDeviceId(Context context, HwKeymasterArguments attestArgs, int[] idTypes) throws HwDeviceIdAttestationException {
        boolean needBuildInfo = true;
        Log.i(TAG, "idTypes are" + Arrays.toString(idTypes));
        for (int idType : idTypes) {
            if (idType == -65536) {
                needBuildInfo = false;
            } else if (idType == -65535) {
                Log.i(TAG, "ID_TYPE_UDID is selected, KM_TAG_ATTESTATION_ID_UDID is inserted");
                attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_UDID, "udid".getBytes(StandardCharsets.UTF_8));
            } else if (idType == 1) {
                attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_SERIAL, Build.getSerial().getBytes(StandardCharsets.UTF_8));
            } else if (idType == 2) {
                addAllImei(context, attestArgs);
            } else if (idType == 3) {
                addMeid(context, attestArgs);
            } else {
                throw new IllegalArgumentException("Unknown device ID type: " + idType);
            }
        }
        if (needBuildInfo) {
            attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_BRAND, Build.BRAND.getBytes(StandardCharsets.UTF_8));
            attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_DEVICE, Build.DEVICE.getBytes(StandardCharsets.UTF_8));
            attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_PRODUCT, Build.PRODUCT.getBytes(StandardCharsets.UTF_8));
            attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_MANUFACTURER, Build.MANUFACTURER.getBytes(StandardCharsets.UTF_8));
            attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_MODEL, Build.MODEL.getBytes(StandardCharsets.UTF_8));
        }
    }

    @NonNull
    private static X509Certificate[] convertToX509CertChain(Collection<byte[]> rawChain) throws IOException, CertificateException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            for (byte[] cert : rawChain) {
                baos.write(cert);
            }
            return (X509Certificate[]) CertificateFactory.getInstance("X.509").generateCertificates(new ByteArrayInputStream(baos.toByteArray())).toArray(new X509Certificate[0]);
        } finally {
            closeQuietly(baos);
        }
    }

    private static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.w(TAG, "Failed to close a closeable: " + e.getMessage());
            }
        }
    }

    public static void checkNotNull(boolean isNull, String errorMessage) {
        if (isNull) {
            throw new NullPointerException(errorMessage);
        }
    }

    private static void checkArgument(boolean isValid, String errorMessage) throws HwDeviceIdAttestationException {
        if (!isValid) {
            throw new HwDeviceIdAttestationException(errorMessage);
        }
    }

    private static Collection<byte[]> generateRawCertChain(HwKeymasterArguments attestArgs) throws HwDeviceIdAttestationException {
        Log.i(TAG, "perform ID attestation");
        HwKeymasterCertificateChain outChain = new HwKeymasterCertificateChain();
        int errorCode = HwKeystoreManager.getInstance().attestDeviceIds(attestArgs, outChain);
        Log.i(TAG, "errorCode is " + errorCode);
        if (errorCode == 1) {
            Log.i(TAG, "Extract certificate chain.");
            Collection<byte[]> rawChain = outChain.getCertificates();
            if (rawChain.size() >= 2) {
                return rawChain;
            }
            throw new HwDeviceIdAttestationException("Attestation certificate chain contained" + rawChain.size() + " entries. At least 2 are required.");
        }
        throw new HwDeviceIdAttestationException("Unable to perform attestation", HwKeystoreManager.getKeyStoreException(errorCode));
    }

    private static boolean supportHwPki() {
        return "true".equals(SystemPropertiesEx.get("ro.config.support_hwpki"));
    }

    private static void addBuildInfoIfNeed(HwKeymasterArguments attestArgs, boolean needBuildInfo) {
        if (needBuildInfo) {
            attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_BRAND, Build.BRAND.getBytes(StandardCharsets.UTF_8));
            attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_DEVICE, Build.DEVICE.getBytes(StandardCharsets.UTF_8));
            attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_PRODUCT, Build.PRODUCT.getBytes(StandardCharsets.UTF_8));
            attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_MANUFACTURER, Build.MANUFACTURER.getBytes(StandardCharsets.UTF_8));
            attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_MODEL, Build.MODEL.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void addMeid(Context context, HwKeymasterArguments attestArgs) throws HwDeviceIdAttestationException {
        TelephonyManager telephonyService = (TelephonyManager) context.getSystemService("phone");
        if (telephonyService != null) {
            String meid = telephonyService.getMeid();
            if (meid != null) {
                attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_MEID, meid.getBytes(StandardCharsets.UTF_8));
                return;
            }
            throw new HwDeviceIdAttestationException("Unable to retrieve MEID");
        }
        throw new HwDeviceIdAttestationException("Unable to access telephony service");
    }

    private static void addAllImei(Context context, HwKeymasterArguments attestArgs) throws HwDeviceIdAttestationException {
        TelephonyManager telephonyService = (TelephonyManager) context.getSystemService("phone");
        if (telephonyService != null) {
            int counter = 0;
            while (true) {
                String imei = telephonyService.getImei(counter);
                if (imei == null) {
                    break;
                }
                attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_IMEI, imei.getBytes(StandardCharsets.UTF_8));
                counter++;
            }
            if (counter == 0) {
                throw new HwDeviceIdAttestationException("Unable to retrieve IMEI");
            }
            return;
        }
        throw new HwDeviceIdAttestationException("Unable to access telephony service");
    }
}
