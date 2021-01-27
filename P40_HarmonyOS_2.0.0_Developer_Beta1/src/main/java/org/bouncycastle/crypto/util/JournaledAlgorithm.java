package org.bouncycastle.crypto.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.SecureRandom;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.util.Encodable;
import org.bouncycastle.util.io.Streams;

public class JournaledAlgorithm implements Encodable, Serializable {
    private transient AlgorithmIdentifier algID;
    private transient JournalingSecureRandom journaling;

    public JournaledAlgorithm(AlgorithmIdentifier algorithmIdentifier, JournalingSecureRandom journalingSecureRandom) {
        if (algorithmIdentifier == null) {
            throw new NullPointerException("AlgorithmIdentifier passed to JournaledAlgorithm is null");
        } else if (journalingSecureRandom != null) {
            this.journaling = journalingSecureRandom;
            this.algID = algorithmIdentifier;
        } else {
            throw new NullPointerException("JournalingSecureRandom passed to JournaledAlgorithm is null");
        }
    }

    public JournaledAlgorithm(byte[] bArr) {
        this(bArr, CryptoServicesRegistrar.getSecureRandom());
    }

    public JournaledAlgorithm(byte[] bArr, SecureRandom secureRandom) {
        if (bArr == null) {
            throw new NullPointerException("encoding passed to JournaledAlgorithm is null");
        } else if (secureRandom != null) {
            initFromEncoding(bArr, secureRandom);
        } else {
            throw new NullPointerException("random passed to JournaledAlgorithm is null");
        }
    }

    public static JournaledAlgorithm getState(File file, SecureRandom secureRandom) throws IOException, ClassNotFoundException {
        if (file != null) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            try {
                return new JournaledAlgorithm(Streams.readAll(bufferedInputStream), secureRandom);
            } finally {
                bufferedInputStream.close();
            }
        } else {
            throw new NullPointerException("File for loading is null in JournaledAlgorithm");
        }
    }

    public static JournaledAlgorithm getState(InputStream inputStream, SecureRandom secureRandom) throws IOException, ClassNotFoundException {
        if (inputStream != null) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            try {
                return new JournaledAlgorithm(Streams.readAll(bufferedInputStream), secureRandom);
            } finally {
                bufferedInputStream.close();
            }
        } else {
            throw new NullPointerException("stream for loading is null in JournaledAlgorithm");
        }
    }

    private void initFromEncoding(byte[] bArr, SecureRandom secureRandom) {
        ASN1Sequence instance = ASN1Sequence.getInstance(bArr);
        this.algID = AlgorithmIdentifier.getInstance(instance.getObjectAt(0));
        this.journaling = new JournalingSecureRandom(ASN1OctetString.getInstance(instance.getObjectAt(1)).getOctets(), secureRandom);
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        initFromEncoding((byte[]) objectInputStream.readObject(), CryptoServicesRegistrar.getSecureRandom());
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeObject(getEncoded());
    }

    public AlgorithmIdentifier getAlgorithmIdentifier() {
        return this.algID;
    }

    @Override // org.bouncycastle.util.Encodable
    public byte[] getEncoded() throws IOException {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.algID);
        aSN1EncodableVector.add(new DEROctetString(this.journaling.getFullTranscript()));
        return new DERSequence(aSN1EncodableVector).getEncoded();
    }

    public JournalingSecureRandom getJournalingSecureRandom() {
        return this.journaling;
    }

    public void storeState(File file) throws IOException {
        if (file != null) {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            try {
                storeState(fileOutputStream);
            } finally {
                fileOutputStream.close();
            }
        } else {
            throw new NullPointerException("file for storage is null in JournaledAlgorithm");
        }
    }

    public void storeState(OutputStream outputStream) throws IOException {
        if (outputStream != null) {
            outputStream.write(getEncoded());
            return;
        }
        throw new NullPointerException("output stream for storage is null in JournaledAlgorithm");
    }
}
