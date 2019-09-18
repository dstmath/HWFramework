package android.security.keystore.recovery;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.io.ByteArrayInputStream;
import java.security.cert.CertPath;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public final class RecoveryCertPath implements Parcelable {
    private static final String CERT_PATH_ENCODING = "PkiPath";
    public static final Parcelable.Creator<RecoveryCertPath> CREATOR = new Parcelable.Creator<RecoveryCertPath>() {
        public RecoveryCertPath createFromParcel(Parcel in) {
            return new RecoveryCertPath(in);
        }

        public RecoveryCertPath[] newArray(int length) {
            return new RecoveryCertPath[length];
        }
    };
    private final byte[] mEncodedCertPath;

    public static RecoveryCertPath createRecoveryCertPath(CertPath certPath) throws CertificateException {
        try {
            return new RecoveryCertPath(encodeCertPath(certPath));
        } catch (CertificateEncodingException e) {
            throw new CertificateException("Failed to encode the given CertPath", e);
        }
    }

    public CertPath getCertPath() throws CertificateException {
        return decodeCertPath(this.mEncodedCertPath);
    }

    private RecoveryCertPath(byte[] encodedCertPath) {
        this.mEncodedCertPath = (byte[]) Preconditions.checkNotNull(encodedCertPath);
    }

    private RecoveryCertPath(Parcel in) {
        this.mEncodedCertPath = in.createByteArray();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeByteArray(this.mEncodedCertPath);
    }

    public int describeContents() {
        return 0;
    }

    private static byte[] encodeCertPath(CertPath certPath) throws CertificateEncodingException {
        Preconditions.checkNotNull(certPath);
        return certPath.getEncoded(CERT_PATH_ENCODING);
    }

    private static CertPath decodeCertPath(byte[] bytes) throws CertificateException {
        Preconditions.checkNotNull(bytes);
        try {
            return CertificateFactory.getInstance("X.509").generateCertPath(new ByteArrayInputStream(bytes), CERT_PATH_ENCODING);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }
}
