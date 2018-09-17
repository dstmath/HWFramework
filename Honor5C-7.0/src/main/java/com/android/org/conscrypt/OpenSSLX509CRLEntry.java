package com.android.org.conscrypt;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.cert.CRLException;
import java.security.cert.X509CRLEntry;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class OpenSSLX509CRLEntry extends X509CRLEntry {
    private final long mContext;

    OpenSSLX509CRLEntry(long ctx) {
        this.mContext = ctx;
    }

    public Set<String> getCriticalExtensionOIDs() {
        String[] critOids = NativeCrypto.get_X509_REVOKED_ext_oids(this.mContext, 1);
        if (critOids.length == 0 && NativeCrypto.get_X509_REVOKED_ext_oids(this.mContext, 0).length == 0) {
            return null;
        }
        return new HashSet(Arrays.asList(critOids));
    }

    public byte[] getExtensionValue(String oid) {
        return NativeCrypto.X509_REVOKED_get_ext_oid(this.mContext, oid);
    }

    public Set<String> getNonCriticalExtensionOIDs() {
        String[] critOids = NativeCrypto.get_X509_REVOKED_ext_oids(this.mContext, 0);
        if (critOids.length == 0 && NativeCrypto.get_X509_REVOKED_ext_oids(this.mContext, 1).length == 0) {
            return null;
        }
        return new HashSet(Arrays.asList(critOids));
    }

    public boolean hasUnsupportedCriticalExtension() {
        for (String oid : NativeCrypto.get_X509_REVOKED_ext_oids(this.mContext, 1)) {
            if (NativeCrypto.X509_supported_extension(NativeCrypto.X509_REVOKED_get_ext(this.mContext, oid)) != 1) {
                return true;
            }
        }
        return false;
    }

    public byte[] getEncoded() throws CRLException {
        return NativeCrypto.i2d_X509_REVOKED(this.mContext);
    }

    public BigInteger getSerialNumber() {
        return new BigInteger(NativeCrypto.X509_REVOKED_get_serialNumber(this.mContext));
    }

    public Date getRevocationDate() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(14, 0);
        NativeCrypto.ASN1_TIME_to_Calendar(NativeCrypto.get_X509_REVOKED_revocationDate(this.mContext), calendar);
        return calendar.getTime();
    }

    public boolean hasExtensions() {
        return (NativeCrypto.get_X509_REVOKED_ext_oids(this.mContext, 0).length == 0 && NativeCrypto.get_X509_REVOKED_ext_oids(this.mContext, 1).length == 0) ? false : true;
    }

    public String toString() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        long bioCtx = NativeCrypto.create_BIO_OutputStream(os);
        try {
            NativeCrypto.X509_REVOKED_print(bioCtx, this.mContext);
            String byteArrayOutputStream = os.toString();
            return byteArrayOutputStream;
        } finally {
            NativeCrypto.BIO_free_all(bioCtx);
        }
    }
}
