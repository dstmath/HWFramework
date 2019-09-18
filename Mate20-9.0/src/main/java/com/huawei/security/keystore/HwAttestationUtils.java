package com.huawei.security.keystore;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.ArraySet;
import android.util.Log;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterCertificateChain;
import com.huawei.security.keymaster.HwKeymasterDefs;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public abstract class HwAttestationUtils {
    private static final int ATTEST_CHALLENGE_LEN_MAX = 128;
    public static final int ID_TYPE_IMEI = 2;
    public static final int ID_TYPE_MEID = 3;
    public static final int ID_TYPE_SERIAL = 1;
    public static final int ID_TYPE_SKIP_DEFAULT = -65536;
    private static final String TAG = "HwAttestationUtils";

    private HwAttestationUtils() {
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v7, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v9, resolved type: android.telephony.TelephonyManager} */
    /* JADX WARNING: Multi-variable type inference failed */
    public static X509Certificate[] attestDeviceIds(Context context, int[] idTypes, byte[] attestationChallenge) throws HwDeviceIdAttestationException {
        if (idTypes == null) {
            throw new NullPointerException("Missing id types");
        } else if (idTypes.length > 4) {
            throw new HwDeviceIdAttestationException("idTypes length is too long");
        } else if (attestationChallenge == null) {
            throw new NullPointerException("Missing attestationChallenge");
        } else if (attestationChallenge.length > 128) {
            throw new HwDeviceIdAttestationException("Attestation Challenge is too long");
        } else if ("true".equals(SystemProperties.get("ro.config.support_hwpki"))) {
            Log.d(TAG, "attestationChallenge length is: " + attestationChallenge.length + "\n");
            HwKeymasterArguments attestArgs = new HwKeymasterArguments();
            Log.i(TAG, "idTypes are" + Arrays.toString(idTypes));
            attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_CHALLENGE, attestationChallenge);
            Set<Integer> idTypesSet = new ArraySet<>(idTypes.length);
            for (int idType : idTypes) {
                idTypesSet.add(Integer.valueOf(idType));
            }
            TelephonyManager telephonyService = null;
            if (idTypesSet.contains(2) || idTypesSet.contains(3)) {
                telephonyService = context.getSystemService("phone");
                if (telephonyService == null) {
                    throw new HwDeviceIdAttestationException("Unable to access telephony service");
                }
            }
            boolean needBuildInfo = true;
            for (Integer intValue : idTypesSet) {
                int idType2 = intValue.intValue();
                if (idType2 != -65536) {
                    switch (idType2) {
                        case 1:
                            attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_SERIAL, Build.getSerial().getBytes(StandardCharsets.UTF_8));
                            break;
                        case 2:
                            int i = 0;
                            while (true) {
                                String imei = telephonyService.getImei(i);
                                if (imei != null) {
                                    attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_IMEI, imei.getBytes(StandardCharsets.UTF_8));
                                    i++;
                                } else if (i != 0) {
                                    break;
                                } else {
                                    throw new HwDeviceIdAttestationException("Unable to retrieve IMEI");
                                }
                            }
                        case 3:
                            String meid = telephonyService.getMeid();
                            if (meid != null) {
                                attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_MEID, meid.getBytes(StandardCharsets.UTF_8));
                                break;
                            } else {
                                throw new HwDeviceIdAttestationException("Unable to retrieve MEID");
                            }
                        default:
                            throw new IllegalArgumentException("Unknown device ID type " + idType2);
                    }
                } else {
                    needBuildInfo = false;
                }
            }
            if (needBuildInfo) {
                attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_BRAND, Build.BRAND.getBytes(StandardCharsets.UTF_8));
                attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_DEVICE, Build.DEVICE.getBytes(StandardCharsets.UTF_8));
                attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_PRODUCT, Build.PRODUCT.getBytes(StandardCharsets.UTF_8));
                attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_MANUFACTURER, Build.MANUFACTURER.getBytes(StandardCharsets.UTF_8));
                attestArgs.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_ID_MODEL, Build.MODEL.getBytes(StandardCharsets.UTF_8));
            }
            Log.i(TAG, "perform ID attestation");
            HwKeymasterCertificateChain outChain = new HwKeymasterCertificateChain();
            int errorCode = HwKeystoreManager.getInstance().attestDeviceIds(attestArgs, outChain);
            Log.i(TAG, "errorCode is " + Integer.toString(errorCode));
            if (errorCode == 1) {
                Log.i(TAG, "Extract certificate chain.");
                Collection<byte[]> rawChain = outChain.getCertificates();
                if (rawChain.size() >= 2) {
                    ByteArrayOutputStream concatenatedRawChain = new ByteArrayOutputStream();
                    try {
                        for (byte[] cert : rawChain) {
                            concatenatedRawChain.write(cert);
                        }
                        return (X509Certificate[]) CertificateFactory.getInstance("X.509").generateCertificates(new ByteArrayInputStream(concatenatedRawChain.toByteArray())).toArray(new X509Certificate[0]);
                    } catch (IOException e) {
                        Log.i(TAG, "Unable to construct certificate chain");
                        throw new HwDeviceIdAttestationException("Unable to construct certificate chain", e);
                    } catch (Exception e2) {
                        Log.i(TAG, "Unable to construct certificate chain");
                        throw new HwDeviceIdAttestationException("Unable to construct certificate chain", e2);
                    }
                } else {
                    throw new HwDeviceIdAttestationException("Attestation certificate chain contained" + rawChain.size() + " entries. At least 2 are required.");
                }
            } else {
                throw new HwDeviceIdAttestationException("Unable to perform attestation", HwKeystoreManager.getKeyStoreException(errorCode));
            }
        } else {
            throw new HwDeviceIdAttestationException("Attestation not support for this version");
        }
    }
}
