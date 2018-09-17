package android.security.keystore;

import android.content.Context;
import android.os.Build;
import android.security.KeyStore;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterCertificateChain;
import android.security.keymaster.KeymasterDefs;
import android.telephony.TelephonyManager;
import android.util.ArraySet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Set;

public abstract class AttestationUtils {
    public static final int ID_TYPE_IMEI = 2;
    public static final int ID_TYPE_MEID = 3;
    public static final int ID_TYPE_SERIAL = 1;

    private AttestationUtils() {
    }

    public static X509Certificate[] attestDeviceIds(Context context, int[] idTypes, byte[] attestationChallenge) throws DeviceIdAttestationException {
        if (idTypes == null) {
            throw new NullPointerException("Missing id types");
        } else if (attestationChallenge == null) {
            throw new NullPointerException("Missing attestation challenge");
        } else {
            KeymasterArguments attestArgs = new KeymasterArguments();
            attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_CHALLENGE, attestationChallenge);
            Set<Integer> idTypesSet = new ArraySet(idTypes.length);
            for (int idType : idTypes) {
                idTypesSet.add(Integer.valueOf(idType));
            }
            TelephonyManager telephonyService = null;
            if (idTypesSet.contains(Integer.valueOf(2)) || idTypesSet.contains(Integer.valueOf(3))) {
                telephonyService = (TelephonyManager) context.getSystemService("phone");
                if (telephonyService == null) {
                    throw new DeviceIdAttestationException("Unable to access telephony service");
                }
            }
            for (Integer idType2 : idTypesSet) {
                switch (idType2.intValue()) {
                    case 1:
                        attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_ID_SERIAL, Build.getSerial().getBytes(StandardCharsets.UTF_8));
                        break;
                    case 2:
                        String imei = telephonyService.getImei(0);
                        if (imei != null) {
                            attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_ID_IMEI, imei.getBytes(StandardCharsets.UTF_8));
                            break;
                        }
                        throw new DeviceIdAttestationException("Unable to retrieve IMEI");
                    case 3:
                        String meid = telephonyService.getDeviceId();
                        if (meid != null) {
                            attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_ID_MEID, meid.getBytes(StandardCharsets.UTF_8));
                            break;
                        }
                        throw new DeviceIdAttestationException("Unable to retrieve MEID");
                    default:
                        throw new IllegalArgumentException("Unknown device ID type " + idType2);
                }
            }
            attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_ID_BRAND, Build.BRAND.getBytes(StandardCharsets.UTF_8));
            attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_ID_DEVICE, Build.DEVICE.getBytes(StandardCharsets.UTF_8));
            attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_ID_PRODUCT, Build.PRODUCT.getBytes(StandardCharsets.UTF_8));
            attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_ID_MANUFACTURER, Build.MANUFACTURER.getBytes(StandardCharsets.UTF_8));
            attestArgs.addBytes(KeymasterDefs.KM_TAG_ATTESTATION_ID_MODEL, Build.MODEL.getBytes(StandardCharsets.UTF_8));
            KeymasterCertificateChain outChain = new KeymasterCertificateChain();
            int errorCode = KeyStore.getInstance().attestDeviceIds(attestArgs, outChain);
            if (errorCode != 1) {
                throw new DeviceIdAttestationException("Unable to perform attestation", KeyStore.getKeyStoreException(errorCode));
            }
            Collection<byte[]> rawChain = outChain.getCertificates();
            if (rawChain.size() < 2) {
                throw new DeviceIdAttestationException("Attestation certificate chain contained " + rawChain.size() + " entries. At least two are required.");
            }
            ByteArrayOutputStream concatenatedRawChain = new ByteArrayOutputStream();
            try {
                for (byte[] cert : rawChain) {
                    concatenatedRawChain.write(cert);
                }
                return (X509Certificate[]) CertificateFactory.getInstance("X.509").generateCertificates(new ByteArrayInputStream(concatenatedRawChain.toByteArray())).toArray(new X509Certificate[0]);
            } catch (Exception e) {
                throw new DeviceIdAttestationException("Unable to construct certificate chain", e);
            }
        }
    }
}
