package com.android.server.locksettings.recoverablekeystore.serialization;

import android.security.keystore.recovery.KeyChainProtectionParams;
import android.security.keystore.recovery.KeyChainSnapshot;
import android.security.keystore.recovery.KeyDerivationParams;
import android.security.keystore.recovery.WrappedApplicationKey;
import android.util.Base64;
import android.util.Xml;
import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertPath;
import java.security.cert.CertificateEncodingException;
import java.util.List;
import org.xmlpull.v1.XmlSerializer;

public class KeyChainSnapshotSerializer {
    public static void serialize(KeyChainSnapshot keyChainSnapshot, OutputStream outputStream) throws IOException, CertificateEncodingException {
        XmlSerializer xmlSerializer = Xml.newSerializer();
        xmlSerializer.setOutput(outputStream, "UTF-8");
        xmlSerializer.startDocument(null, null);
        xmlSerializer.startTag(KeyChainSnapshotSchema.NAMESPACE, "keyChainSnapshot");
        writeKeyChainSnapshotProperties(xmlSerializer, keyChainSnapshot);
        writeKeyChainProtectionParams(xmlSerializer, keyChainSnapshot.getKeyChainProtectionParams());
        writeApplicationKeys(xmlSerializer, keyChainSnapshot.getWrappedApplicationKeys());
        xmlSerializer.endTag(KeyChainSnapshotSchema.NAMESPACE, "keyChainSnapshot");
        xmlSerializer.endDocument();
    }

    private static void writeApplicationKeys(XmlSerializer xmlSerializer, List<WrappedApplicationKey> wrappedApplicationKeys) throws IOException {
        xmlSerializer.startTag(KeyChainSnapshotSchema.NAMESPACE, "applicationKeysList");
        for (WrappedApplicationKey key : wrappedApplicationKeys) {
            xmlSerializer.startTag(KeyChainSnapshotSchema.NAMESPACE, "applicationKey");
            writeApplicationKeyProperties(xmlSerializer, key);
            xmlSerializer.endTag(KeyChainSnapshotSchema.NAMESPACE, "applicationKey");
        }
        xmlSerializer.endTag(KeyChainSnapshotSchema.NAMESPACE, "applicationKeysList");
    }

    private static void writeApplicationKeyProperties(XmlSerializer xmlSerializer, WrappedApplicationKey applicationKey) throws IOException {
        writePropertyTag(xmlSerializer, "alias", applicationKey.getAlias());
        writePropertyTag(xmlSerializer, "keyMaterial", applicationKey.getEncryptedKeyMaterial());
    }

    private static void writeKeyChainProtectionParams(XmlSerializer xmlSerializer, List<KeyChainProtectionParams> keyChainProtectionParamsList) throws IOException {
        xmlSerializer.startTag(KeyChainSnapshotSchema.NAMESPACE, "keyChainProtectionParamsList");
        for (KeyChainProtectionParams keyChainProtectionParams : keyChainProtectionParamsList) {
            xmlSerializer.startTag(KeyChainSnapshotSchema.NAMESPACE, "keyChainProtectionParams");
            writeKeyChainProtectionParamsProperties(xmlSerializer, keyChainProtectionParams);
            xmlSerializer.endTag(KeyChainSnapshotSchema.NAMESPACE, "keyChainProtectionParams");
        }
        xmlSerializer.endTag(KeyChainSnapshotSchema.NAMESPACE, "keyChainProtectionParamsList");
    }

    private static void writeKeyChainProtectionParamsProperties(XmlSerializer xmlSerializer, KeyChainProtectionParams keyChainProtectionParams) throws IOException {
        writePropertyTag(xmlSerializer, "userSecretType", (long) keyChainProtectionParams.getUserSecretType());
        writePropertyTag(xmlSerializer, "lockScreenUiType", (long) keyChainProtectionParams.getLockScreenUiFormat());
        writeKeyDerivationParams(xmlSerializer, keyChainProtectionParams.getKeyDerivationParams());
    }

    private static void writeKeyDerivationParams(XmlSerializer xmlSerializer, KeyDerivationParams keyDerivationParams) throws IOException {
        xmlSerializer.startTag(KeyChainSnapshotSchema.NAMESPACE, "keyDerivationParams");
        writeKeyDerivationParamsProperties(xmlSerializer, keyDerivationParams);
        xmlSerializer.endTag(KeyChainSnapshotSchema.NAMESPACE, "keyDerivationParams");
    }

    private static void writeKeyDerivationParamsProperties(XmlSerializer xmlSerializer, KeyDerivationParams keyDerivationParams) throws IOException {
        writePropertyTag(xmlSerializer, "algorithm", (long) keyDerivationParams.getAlgorithm());
        writePropertyTag(xmlSerializer, "salt", keyDerivationParams.getSalt());
        writePropertyTag(xmlSerializer, "memoryDifficulty", (long) keyDerivationParams.getMemoryDifficulty());
    }

    private static void writeKeyChainSnapshotProperties(XmlSerializer xmlSerializer, KeyChainSnapshot keyChainSnapshot) throws IOException, CertificateEncodingException {
        writePropertyTag(xmlSerializer, "snapshotVersion", (long) keyChainSnapshot.getSnapshotVersion());
        writePropertyTag(xmlSerializer, "maxAttempts", (long) keyChainSnapshot.getMaxAttempts());
        writePropertyTag(xmlSerializer, "counterId", keyChainSnapshot.getCounterId());
        writePropertyTag(xmlSerializer, "recoveryKeyMaterial", keyChainSnapshot.getEncryptedRecoveryKeyBlob());
        writePropertyTag(xmlSerializer, "serverParams", keyChainSnapshot.getServerParams());
        writePropertyTag(xmlSerializer, "thmCertPath", keyChainSnapshot.getTrustedHardwareCertPath());
    }

    private static void writePropertyTag(XmlSerializer xmlSerializer, String propertyName, long propertyValue) throws IOException {
        xmlSerializer.startTag(KeyChainSnapshotSchema.NAMESPACE, propertyName);
        xmlSerializer.text(Long.toString(propertyValue));
        xmlSerializer.endTag(KeyChainSnapshotSchema.NAMESPACE, propertyName);
    }

    private static void writePropertyTag(XmlSerializer xmlSerializer, String propertyName, String propertyValue) throws IOException {
        xmlSerializer.startTag(KeyChainSnapshotSchema.NAMESPACE, propertyName);
        xmlSerializer.text(propertyValue);
        xmlSerializer.endTag(KeyChainSnapshotSchema.NAMESPACE, propertyName);
    }

    private static void writePropertyTag(XmlSerializer xmlSerializer, String propertyName, byte[] propertyValue) throws IOException {
        xmlSerializer.startTag(KeyChainSnapshotSchema.NAMESPACE, propertyName);
        xmlSerializer.text(Base64.encodeToString(propertyValue, 0));
        xmlSerializer.endTag(KeyChainSnapshotSchema.NAMESPACE, propertyName);
    }

    private static void writePropertyTag(XmlSerializer xmlSerializer, String propertyName, CertPath certPath) throws IOException, CertificateEncodingException {
        writePropertyTag(xmlSerializer, propertyName, certPath.getEncoded("PkiPath"));
    }

    private KeyChainSnapshotSerializer() {
    }
}
