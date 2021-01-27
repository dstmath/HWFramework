package com.android.server.locksettings.recoverablekeystore.serialization;

import android.security.keystore.recovery.KeyChainProtectionParams;
import android.security.keystore.recovery.KeyChainSnapshot;
import android.security.keystore.recovery.KeyDerivationParams;
import android.security.keystore.recovery.WrappedApplicationKey;
import android.util.Base64;
import android.util.Xml;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class KeyChainSnapshotDeserializer {
    public static KeyChainSnapshot deserialize(InputStream inputStream) throws KeyChainSnapshotParserException, IOException {
        try {
            return deserializeInternal(inputStream);
        } catch (XmlPullParserException e) {
            throw new KeyChainSnapshotParserException("Malformed KeyChainSnapshot XML", e);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x004e, code lost:
        if (r7.equals("serverParams") != false) goto L_0x009a;
     */
    private static KeyChainSnapshot deserializeInternal(InputStream inputStream) throws IOException, XmlPullParserException, KeyChainSnapshotParserException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputStream, "UTF-8");
        parser.nextTag();
        parser.require(2, KeyChainSnapshotSchema.NAMESPACE, "keyChainSnapshot");
        KeyChainSnapshot.Builder builder = new KeyChainSnapshot.Builder();
        while (true) {
            char c = 3;
            if (parser.next() == 3) {
                parser.require(3, KeyChainSnapshotSchema.NAMESPACE, "keyChainSnapshot");
                try {
                    return builder.build();
                } catch (NullPointerException e) {
                    throw new KeyChainSnapshotParserException("Failed to build KeyChainSnapshot", e);
                }
            } else if (parser.getEventType() == 2) {
                String name = parser.getName();
                switch (name.hashCode()) {
                    case -1719931702:
                        if (name.equals("maxAttempts")) {
                            c = 4;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1388433662:
                        if (name.equals("backendPublicKey")) {
                            c = 6;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1370381871:
                        if (name.equals("recoveryKeyMaterial")) {
                            c = 1;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1368437758:
                        if (name.equals("thmCertPath")) {
                            c = 5;
                            break;
                        }
                        c = 65535;
                        break;
                    case 481270388:
                        if (name.equals("snapshotVersion")) {
                            c = 0;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1190285858:
                        if (name.equals("applicationKeysList")) {
                            c = '\b';
                            break;
                        }
                        c = 65535;
                        break;
                    case 1352257591:
                        if (name.equals("counterId")) {
                            c = 2;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1596875199:
                        if (name.equals("keyChainProtectionParamsList")) {
                            c = 7;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1806980777:
                        break;
                    default:
                        c = 65535;
                        break;
                }
                switch (c) {
                    case 0:
                        builder.setSnapshotVersion(readIntTag(parser, "snapshotVersion"));
                        continue;
                    case 1:
                        builder.setEncryptedRecoveryKeyBlob(readBlobTag(parser, "recoveryKeyMaterial"));
                        continue;
                    case 2:
                        builder.setCounterId(readLongTag(parser, "counterId"));
                        continue;
                    case 3:
                        builder.setServerParams(readBlobTag(parser, "serverParams"));
                        continue;
                    case 4:
                        builder.setMaxAttempts(readIntTag(parser, "maxAttempts"));
                        continue;
                    case 5:
                        try {
                            builder.setTrustedHardwareCertPath(readCertPathTag(parser, "thmCertPath"));
                            continue;
                        } catch (CertificateException e2) {
                            throw new KeyChainSnapshotParserException("Could not set trustedHardwareCertPath", e2);
                        }
                    case 6:
                        break;
                    case 7:
                        builder.setKeyChainProtectionParams(readKeyChainProtectionParamsList(parser));
                        continue;
                    case '\b':
                        builder.setWrappedApplicationKeys(readWrappedApplicationKeys(parser));
                        continue;
                    default:
                        throw new KeyChainSnapshotParserException(String.format(Locale.US, "Unexpected tag %s in keyChainSnapshot", name));
                }
            }
        }
    }

    private static List<WrappedApplicationKey> readWrappedApplicationKeys(XmlPullParser parser) throws IOException, XmlPullParserException, KeyChainSnapshotParserException {
        parser.require(2, KeyChainSnapshotSchema.NAMESPACE, "applicationKeysList");
        ArrayList<WrappedApplicationKey> keys = new ArrayList<>();
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                keys.add(readWrappedApplicationKey(parser));
            }
        }
        parser.require(3, KeyChainSnapshotSchema.NAMESPACE, "applicationKeysList");
        return keys;
    }

    private static WrappedApplicationKey readWrappedApplicationKey(XmlPullParser parser) throws IOException, XmlPullParserException, KeyChainSnapshotParserException {
        parser.require(2, KeyChainSnapshotSchema.NAMESPACE, "applicationKey");
        WrappedApplicationKey.Builder builder = new WrappedApplicationKey.Builder();
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != -1712279890) {
                    if (hashCode != -963209050) {
                        if (hashCode == 92902992 && name.equals("alias")) {
                            c = 0;
                        }
                    } else if (name.equals("keyMaterial")) {
                        c = 1;
                    }
                } else if (name.equals("keyMetadata")) {
                    c = 2;
                }
                if (c == 0) {
                    builder.setAlias(readStringTag(parser, "alias"));
                } else if (c == 1) {
                    builder.setEncryptedKeyMaterial(readBlobTag(parser, "keyMaterial"));
                } else if (c == 2) {
                    builder.setMetadata(readBlobTag(parser, "keyMetadata"));
                } else {
                    throw new KeyChainSnapshotParserException(String.format(Locale.US, "Unexpected tag %s in wrappedApplicationKey", name));
                }
            }
        }
        parser.require(3, KeyChainSnapshotSchema.NAMESPACE, "applicationKey");
        try {
            return builder.build();
        } catch (NullPointerException e) {
            throw new KeyChainSnapshotParserException("Failed to build WrappedApplicationKey", e);
        }
    }

    private static List<KeyChainProtectionParams> readKeyChainProtectionParamsList(XmlPullParser parser) throws IOException, XmlPullParserException, KeyChainSnapshotParserException {
        parser.require(2, KeyChainSnapshotSchema.NAMESPACE, "keyChainProtectionParamsList");
        ArrayList<KeyChainProtectionParams> keyChainProtectionParamsList = new ArrayList<>();
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                keyChainProtectionParamsList.add(readKeyChainProtectionParams(parser));
            }
        }
        parser.require(3, KeyChainSnapshotSchema.NAMESPACE, "keyChainProtectionParamsList");
        return keyChainProtectionParamsList;
    }

    private static KeyChainProtectionParams readKeyChainProtectionParams(XmlPullParser parser) throws IOException, XmlPullParserException, KeyChainSnapshotParserException {
        parser.require(2, KeyChainSnapshotSchema.NAMESPACE, "keyChainProtectionParams");
        KeyChainProtectionParams.Builder builder = new KeyChainProtectionParams.Builder();
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != -776797115) {
                    if (hashCode != -696958923) {
                        if (hashCode == 912448924 && name.equals("keyDerivationParams")) {
                            c = 2;
                        }
                    } else if (name.equals("userSecretType")) {
                        c = 1;
                    }
                } else if (name.equals("lockScreenUiType")) {
                    c = 0;
                }
                if (c == 0) {
                    builder.setLockScreenUiFormat(readIntTag(parser, "lockScreenUiType"));
                } else if (c == 1) {
                    builder.setUserSecretType(readIntTag(parser, "userSecretType"));
                } else if (c == 2) {
                    builder.setKeyDerivationParams(readKeyDerivationParams(parser));
                } else {
                    throw new KeyChainSnapshotParserException(String.format(Locale.US, "Unexpected tag %s in keyChainProtectionParams", name));
                }
            }
        }
        parser.require(3, KeyChainSnapshotSchema.NAMESPACE, "keyChainProtectionParams");
        try {
            return builder.build();
        } catch (NullPointerException e) {
            throw new KeyChainSnapshotParserException("Failed to build KeyChainProtectionParams", e);
        }
    }

    private static KeyDerivationParams readKeyDerivationParams(XmlPullParser parser) throws XmlPullParserException, IOException, KeyChainSnapshotParserException {
        KeyDerivationParams keyDerivationParams;
        parser.require(2, KeyChainSnapshotSchema.NAMESPACE, "keyDerivationParams");
        int memoryDifficulty = -1;
        int algorithm = -1;
        byte[] salt = null;
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != -973274212) {
                    if (hashCode != 3522646) {
                        if (hashCode == 225490031 && name.equals("algorithm")) {
                            c = 1;
                        }
                    } else if (name.equals("salt")) {
                        c = 2;
                    }
                } else if (name.equals("memoryDifficulty")) {
                    c = 0;
                }
                if (c == 0) {
                    memoryDifficulty = readIntTag(parser, "memoryDifficulty");
                } else if (c == 1) {
                    algorithm = readIntTag(parser, "algorithm");
                } else if (c == 2) {
                    salt = readBlobTag(parser, "salt");
                } else {
                    throw new KeyChainSnapshotParserException(String.format(Locale.US, "Unexpected tag %s in keyDerivationParams", name));
                }
            }
        }
        if (salt != null) {
            if (algorithm == 1) {
                keyDerivationParams = KeyDerivationParams.createSha256Params(salt);
            } else if (algorithm == 2) {
                keyDerivationParams = KeyDerivationParams.createScryptParams(salt, memoryDifficulty);
            } else {
                throw new KeyChainSnapshotParserException("Unknown algorithm in keyDerivationParams");
            }
            parser.require(3, KeyChainSnapshotSchema.NAMESPACE, "keyDerivationParams");
            return keyDerivationParams;
        }
        throw new KeyChainSnapshotParserException("salt was not set in keyDerivationParams");
    }

    private static int readIntTag(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException, KeyChainSnapshotParserException {
        parser.require(2, KeyChainSnapshotSchema.NAMESPACE, tagName);
        String text = readText(parser);
        parser.require(3, KeyChainSnapshotSchema.NAMESPACE, tagName);
        try {
            return Integer.valueOf(text).intValue();
        } catch (NumberFormatException e) {
            throw new KeyChainSnapshotParserException(String.format(Locale.US, "%s expected int but got '%s'", tagName, text), e);
        }
    }

    private static long readLongTag(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException, KeyChainSnapshotParserException {
        parser.require(2, KeyChainSnapshotSchema.NAMESPACE, tagName);
        String text = readText(parser);
        parser.require(3, KeyChainSnapshotSchema.NAMESPACE, tagName);
        try {
            return Long.valueOf(text).longValue();
        } catch (NumberFormatException e) {
            throw new KeyChainSnapshotParserException(String.format(Locale.US, "%s expected long but got '%s'", tagName, text), e);
        }
    }

    private static String readStringTag(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException {
        parser.require(2, KeyChainSnapshotSchema.NAMESPACE, tagName);
        String text = readText(parser);
        parser.require(3, KeyChainSnapshotSchema.NAMESPACE, tagName);
        return text;
    }

    private static byte[] readBlobTag(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException, KeyChainSnapshotParserException {
        parser.require(2, KeyChainSnapshotSchema.NAMESPACE, tagName);
        String text = readText(parser);
        parser.require(3, KeyChainSnapshotSchema.NAMESPACE, tagName);
        try {
            return Base64.decode(text, 0);
        } catch (IllegalArgumentException e) {
            throw new KeyChainSnapshotParserException(String.format(Locale.US, "%s expected base64 encoded bytes but got '%s'", tagName, text), e);
        }
    }

    private static CertPath readCertPathTag(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException, KeyChainSnapshotParserException {
        try {
            return CertificateFactory.getInstance("X.509").generateCertPath(new ByteArrayInputStream(readBlobTag(parser, tagName)));
        } catch (CertificateException e) {
            throw new KeyChainSnapshotParserException("Could not parse CertPath in tag " + tagName, e);
        }
    }

    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.next() != 4) {
            return "";
        }
        String result = parser.getText();
        parser.nextTag();
        return result;
    }

    private KeyChainSnapshotDeserializer() {
    }
}
