package android.security.keystore;

import android.annotation.SystemApi;
import android.content.Context;
import android.os.Build;
import android.security.KeyStore;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterCertificateChain;
import android.security.keymaster.KeymasterDefs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import com.android.internal.R;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Set;

@SystemApi
public abstract class AttestationUtils {
    public static final int ID_TYPE_IMEI = 2;
    public static final int ID_TYPE_MEID = 3;
    public static final int ID_TYPE_SERIAL = 1;

    private AttestationUtils() {
    }

    public static X509Certificate[] parseCertificateChain(KeymasterCertificateChain kmChain) throws KeyAttestationException {
        Collection<byte[]> rawChain = kmChain.getCertificates();
        if (rawChain.size() >= 2) {
            ByteArrayOutputStream concatenatedRawChain = new ByteArrayOutputStream();
            try {
                for (byte[] cert : rawChain) {
                    concatenatedRawChain.write(cert);
                }
                return (X509Certificate[]) CertificateFactory.getInstance("X.509").generateCertificates(new ByteArrayInputStream(concatenatedRawChain.toByteArray())).toArray(new X509Certificate[0]);
            } catch (Exception e) {
                throw new KeyAttestationException("Unable to construct certificate chain", e);
            }
        } else {
            throw new KeyAttestationException("Attestation certificate chain contained " + rawChain.size() + " entries. At least two are required.");
        }
    }

    private static KeymasterArguments prepareAttestationArgumentsForDeviceId(Context context, int[] idTypes, byte[] attestationChallenge) throws DeviceIdAttestationException {
        if (idTypes != null) {
            return prepareAttestationArguments(context, idTypes, attestationChallenge);
        }
        throw new NullPointerException("Missing id types");
    }

    public static KeymasterArguments prepareAttestationArguments(Context context, int[] idTypes, byte[] attestationChallenge) throws DeviceIdAttestationException {
        return prepareAttestationArguments(context, idTypes, attestationChallenge, Build.BRAND);
    }

    public static KeymasterArguments prepareAttestationArgumentsIfMisprovisioned(Context context, int[] idTypes, byte[] attestationChallenge) throws DeviceIdAttestationException {
        String misprovisionedBrand = context.getResources().getString(R.string.config_misprovisionedBrandValue);
        if (TextUtils.isEmpty(misprovisionedBrand) == 1 || isPotentiallyMisprovisionedDevice(context) == 1) {
            return prepareAttestationArguments(context, idTypes, attestationChallenge, misprovisionedBrand);
        }
        return null;
    }

    private static boolean isPotentiallyMisprovisionedDevice(Context context) {
        return Build.MODEL.equals(context.getResources().getString(R.string.config_misprovisionedDeviceModel));
    }

    private static KeymasterArguments prepareAttestationArguments(Context context, int[] idTypes, byte[] attestationChallenge, String brand) throws DeviceIdAttestationException {
        if (attestationChallenge != null) {
            KeymasterArguments attestArgs = new KeymasterArguments();
            attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_CHALLENGE, attestationChallenge);
            if (idTypes == null) {
                return attestArgs;
            }
            Set<Integer> idTypesSet = new ArraySet<>(idTypes.length);
            for (int idType : idTypes) {
                idTypesSet.add(Integer.valueOf(idType));
            }
            TelephonyManager telephonyService = null;
            if ((idTypesSet.contains(2) == 1 || idTypesSet.contains(3) == 1) && (telephonyService = (TelephonyManager) context.getSystemService("phone")) == null) {
                throw new DeviceIdAttestationException("Unable to access telephony service");
            }
            for (Integer idType2 : idTypesSet) {
                int intValue = idType2.intValue();
                if (intValue != 1) {
                    if (intValue == 2) {
                        String imei = telephonyService.getImei(0);
                        if (imei != null) {
                            attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_ID_IMEI, imei.getBytes(StandardCharsets.UTF_8));
                        } else {
                            throw new DeviceIdAttestationException("Unable to retrieve IMEI");
                        }
                    } else if (intValue == 3) {
                        String meid = telephonyService.getMeid(0);
                        if (meid != null) {
                            attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_ID_MEID, meid.getBytes(StandardCharsets.UTF_8));
                        } else {
                            throw new DeviceIdAttestationException("Unable to retrieve MEID");
                        }
                    } else {
                        throw new IllegalArgumentException("Unknown device ID type " + idType2);
                    }
                } else if (Build.getSerial() != null) {
                    attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_ID_SERIAL, Build.getSerial().getBytes(StandardCharsets.UTF_8));
                }
            }
            attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_ID_BRAND, brand.getBytes(StandardCharsets.UTF_8));
            attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_ID_DEVICE, Build.DEVICE.getBytes(StandardCharsets.UTF_8));
            attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_ID_PRODUCT, Build.PRODUCT.getBytes(StandardCharsets.UTF_8));
            attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_ID_MANUFACTURER, Build.MANUFACTURER.getBytes(StandardCharsets.UTF_8));
            attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_ID_MODEL, Build.MODEL.getBytes(StandardCharsets.UTF_8));
            return attestArgs;
        }
        throw new NullPointerException("Missing attestation challenge");
    }

    public static X509Certificate[] attestDeviceIds(Context context, int[] idTypes, byte[] attestationChallenge) throws DeviceIdAttestationException {
        KeymasterArguments attestArgs = prepareAttestationArgumentsForDeviceId(context, idTypes, attestationChallenge);
        KeymasterCertificateChain outChain = new KeymasterCertificateChain();
        int errorCode = KeyStore.getInstance().attestDeviceIds(attestArgs, outChain);
        if (errorCode == 1) {
            try {
                return parseCertificateChain(outChain);
            } catch (KeyAttestationException e) {
                throw new DeviceIdAttestationException(e.getMessage(), e);
            }
        } else {
            throw new DeviceIdAttestationException("Unable to perform attestation", KeyStore.getKeyStoreException(errorCode));
        }
    }

    public static boolean isChainValid(KeymasterCertificateChain chain) {
        return (chain != null && chain.getCertificates().size() >= 2) ? true : false;
    }
}
