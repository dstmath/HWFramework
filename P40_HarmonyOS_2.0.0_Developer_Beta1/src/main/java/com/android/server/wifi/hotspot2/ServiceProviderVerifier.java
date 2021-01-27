package com.android.server.wifi.hotspot2;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DERTaggedObject;
import com.android.org.bouncycastle.asn1.DERUTF8String;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class ServiceProviderVerifier {
    private static final int ENTRY_COUNT = 2;
    @VisibleForTesting
    public static final String ID_WFA_OID_HOTSPOT_FRIENDLYNAME = "1.3.6.1.4.1.40808.1.1.1";
    private static final int LANGUAGE_CODE_LENGTH = 3;
    private static final int OTHER_NAME = 0;
    private static final String TAG = "PasspointServiceProviderVerifier";

    public static List<Pair<Locale, String>> getProviderNames(X509Certificate providerCert) {
        List<Pair<Locale, String>> providerNames = new ArrayList<>();
        if (providerCert == null) {
            return providerNames;
        }
        try {
            Collection<List<?>> col = providerCert.getSubjectAlternativeNames();
            if (col == null) {
                return providerNames;
            }
            for (List<?> entry : col) {
                if (entry != null) {
                    if (entry.size() == 2) {
                        if (((Integer) entry.get(0)).intValue() == 0) {
                            if (entry.toArray()[1] instanceof byte[]) {
                                ASN1Encodable obj = new ASN1InputStream((byte[]) entry.toArray()[1]).readObject();
                                if (obj instanceof DERTaggedObject) {
                                    ASN1Encodable encodedObject = ((DERTaggedObject) obj).getObject();
                                    if (encodedObject instanceof ASN1Sequence) {
                                        ASN1Sequence innerSequence = (ASN1Sequence) encodedObject;
                                        ASN1Encodable innerObject = innerSequence.getObjectAt(0);
                                        if (innerObject instanceof ASN1ObjectIdentifier) {
                                            if (ASN1ObjectIdentifier.getInstance(innerObject).getId().equals(ID_WFA_OID_HOTSPOT_FRIENDLYNAME)) {
                                                for (int index = 1; index < innerSequence.size(); index++) {
                                                    ASN1Encodable innerObject2 = innerSequence.getObjectAt(index);
                                                    if (innerObject2 instanceof DERTaggedObject) {
                                                        ASN1Encodable innerSequenceEncodedObject = ((DERTaggedObject) innerObject2).getObject();
                                                        if (innerSequenceEncodedObject instanceof DERUTF8String) {
                                                            Pair<Locale, String> providerName = getFriendlyName(((DERUTF8String) innerSequenceEncodedObject).getString());
                                                            if (providerName != null) {
                                                                providerNames.add(providerName);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return providerNames;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean verifyCertFingerprint(X509Certificate x509Cert, byte[] certSHA256Fingerprint) {
        try {
            byte[] fingerPrintSha256 = computeHash(x509Cert.getEncoded());
            if (fingerPrintSha256 != null && Arrays.equals(fingerPrintSha256, certSHA256Fingerprint)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Exception happened in verifyCertFingerprint()");
        }
    }

    private static byte[] computeHash(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static Pair<Locale, String> getFriendlyName(String alternativeName) {
        if (TextUtils.isEmpty(alternativeName) || alternativeName.length() < 3) {
            return null;
        }
        try {
            return Pair.create(new Locale.Builder().setLanguage(alternativeName.substring(0, 3)).build(), alternativeName.substring(3));
        } catch (Exception e) {
            return null;
        }
    }
}
