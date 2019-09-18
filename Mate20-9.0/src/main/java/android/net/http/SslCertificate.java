package android.net.http;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.util.HexDump;
import com.android.org.bouncycastle.asn1.x509.X509Name;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class SslCertificate {
    private static String ISO_8601_DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";
    private static final String ISSUED_BY = "issued-by";
    private static final String ISSUED_TO = "issued-to";
    private static final String VALID_NOT_AFTER = "valid-not-after";
    private static final String VALID_NOT_BEFORE = "valid-not-before";
    private static final String X509_CERTIFICATE = "x509-certificate";
    private final DName mIssuedBy;
    private final DName mIssuedTo;
    private final Date mValidNotAfter;
    private final Date mValidNotBefore;
    private final X509Certificate mX509Certificate;

    public class DName {
        private String mCName;
        private String mDName;
        private String mOName;
        private String mUName;

        public DName(String dName) {
            if (dName != null) {
                this.mDName = dName;
                try {
                    X509Name x509Name = new X509Name(dName);
                    Vector val = x509Name.getValues();
                    Vector oid = x509Name.getOIDs();
                    for (int i = 0; i < oid.size(); i++) {
                        if (oid.elementAt(i).equals(X509Name.CN)) {
                            if (this.mCName == null) {
                                this.mCName = (String) val.elementAt(i);
                            }
                        } else if (oid.elementAt(i).equals(X509Name.O) && this.mOName == null) {
                            this.mOName = (String) val.elementAt(i);
                        } else if (oid.elementAt(i).equals(X509Name.OU) && this.mUName == null) {
                            this.mUName = (String) val.elementAt(i);
                        }
                    }
                } catch (IllegalArgumentException e) {
                }
            }
        }

        public String getDName() {
            return this.mDName != null ? this.mDName : "";
        }

        public String getCName() {
            return this.mCName != null ? this.mCName : "";
        }

        public String getOName() {
            return this.mOName != null ? this.mOName : "";
        }

        public String getUName() {
            return this.mUName != null ? this.mUName : "";
        }
    }

    public static Bundle saveState(SslCertificate certificate) {
        if (certificate == null) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putString(ISSUED_TO, certificate.getIssuedTo().getDName());
        bundle.putString(ISSUED_BY, certificate.getIssuedBy().getDName());
        bundle.putString(VALID_NOT_BEFORE, certificate.getValidNotBefore());
        bundle.putString(VALID_NOT_AFTER, certificate.getValidNotAfter());
        X509Certificate x509Certificate = certificate.mX509Certificate;
        if (x509Certificate != null) {
            try {
                bundle.putByteArray(X509_CERTIFICATE, x509Certificate.getEncoded());
            } catch (CertificateEncodingException e) {
            }
        }
        return bundle;
    }

    public static SslCertificate restoreState(Bundle bundle) {
        X509Certificate x509Certificate = null;
        if (bundle == null) {
            return null;
        }
        byte[] bytes = bundle.getByteArray(X509_CERTIFICATE);
        if (bytes == null) {
            x509Certificate = null;
        } else {
            try {
                x509Certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(bytes));
            } catch (CertificateException e) {
            }
        }
        SslCertificate sslCertificate = new SslCertificate(bundle.getString(ISSUED_TO), bundle.getString(ISSUED_BY), parseDate(bundle.getString(VALID_NOT_BEFORE)), parseDate(bundle.getString(VALID_NOT_AFTER)), x509Certificate);
        return sslCertificate;
    }

    @Deprecated
    public SslCertificate(String issuedTo, String issuedBy, String validNotBefore, String validNotAfter) {
        this(issuedTo, issuedBy, parseDate(validNotBefore), parseDate(validNotAfter), null);
    }

    @Deprecated
    public SslCertificate(String issuedTo, String issuedBy, Date validNotBefore, Date validNotAfter) {
        this(issuedTo, issuedBy, validNotBefore, validNotAfter, null);
    }

    public SslCertificate(X509Certificate certificate) {
        this(certificate.getSubjectDN().getName(), certificate.getIssuerDN().getName(), certificate.getNotBefore(), certificate.getNotAfter(), certificate);
    }

    private SslCertificate(String issuedTo, String issuedBy, Date validNotBefore, Date validNotAfter, X509Certificate x509Certificate) {
        this.mIssuedTo = new DName(issuedTo);
        this.mIssuedBy = new DName(issuedBy);
        this.mValidNotBefore = cloneDate(validNotBefore);
        this.mValidNotAfter = cloneDate(validNotAfter);
        this.mX509Certificate = x509Certificate;
    }

    public Date getValidNotBeforeDate() {
        return cloneDate(this.mValidNotBefore);
    }

    @Deprecated
    public String getValidNotBefore() {
        return formatDate(this.mValidNotBefore);
    }

    public Date getValidNotAfterDate() {
        return cloneDate(this.mValidNotAfter);
    }

    @Deprecated
    public String getValidNotAfter() {
        return formatDate(this.mValidNotAfter);
    }

    public DName getIssuedTo() {
        return this.mIssuedTo;
    }

    public DName getIssuedBy() {
        return this.mIssuedBy;
    }

    private static String getSerialNumber(X509Certificate x509Certificate) {
        if (x509Certificate == null) {
            return "";
        }
        BigInteger serialNumber = x509Certificate.getSerialNumber();
        if (serialNumber == null) {
            return "";
        }
        return fingerprint(serialNumber.toByteArray());
    }

    private static String getDigest(X509Certificate x509Certificate, String algorithm) {
        if (x509Certificate == null) {
            return "";
        }
        try {
            return fingerprint(MessageDigest.getInstance(algorithm).digest(x509Certificate.getEncoded()));
        } catch (CertificateEncodingException e) {
            return "";
        } catch (NoSuchAlgorithmException e2) {
            return "";
        }
    }

    private static final String fingerprint(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            HexDump.appendByteAsHex(sb, bytes[i], true);
            if (i + 1 != bytes.length) {
                sb.append(':');
            }
        }
        return sb.toString();
    }

    public String toString() {
        return "Issued to: " + this.mIssuedTo.getDName() + ";\nIssued by: " + this.mIssuedBy.getDName() + ";\n";
    }

    private static Date parseDate(String string) {
        try {
            return new SimpleDateFormat(ISO_8601_DATE_FORMAT).parse(string);
        } catch (ParseException e) {
            return null;
        }
    }

    private static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(ISO_8601_DATE_FORMAT).format(date);
    }

    private static Date cloneDate(Date date) {
        if (date == null) {
            return null;
        }
        return (Date) date.clone();
    }

    public View inflateCertificateView(Context context) {
        View certificateView = LayoutInflater.from(context).inflate(R.layout.ssl_certificate, null);
        DName issuedTo = getIssuedTo();
        if (issuedTo != null) {
            ((TextView) certificateView.findViewById(R.id.to_common)).setText(issuedTo.getCName());
            ((TextView) certificateView.findViewById(R.id.to_org)).setText(issuedTo.getOName());
            ((TextView) certificateView.findViewById(R.id.to_org_unit)).setText(issuedTo.getUName());
        }
        ((TextView) certificateView.findViewById(R.id.serial_number)).setText(getSerialNumber(this.mX509Certificate));
        DName issuedBy = getIssuedBy();
        if (issuedBy != null) {
            ((TextView) certificateView.findViewById(R.id.by_common)).setText(issuedBy.getCName());
            ((TextView) certificateView.findViewById(R.id.by_org)).setText(issuedBy.getOName());
            ((TextView) certificateView.findViewById(R.id.by_org_unit)).setText(issuedBy.getUName());
        }
        ((TextView) certificateView.findViewById(R.id.issued_on)).setText(formatCertificateDate(context, getValidNotBeforeDate()));
        ((TextView) certificateView.findViewById(R.id.expires_on)).setText(formatCertificateDate(context, getValidNotAfterDate()));
        ((TextView) certificateView.findViewById(R.id.sha256_fingerprint)).setText(getDigest(this.mX509Certificate, "SHA256"));
        ((TextView) certificateView.findViewById(R.id.sha1_fingerprint)).setText(getDigest(this.mX509Certificate, "SHA1"));
        return certificateView;
    }

    private String formatCertificateDate(Context context, Date certificateDate) {
        if (certificateDate == null) {
            return "";
        }
        return DateFormat.getMediumDateFormat(context).format(certificateDate);
    }
}
