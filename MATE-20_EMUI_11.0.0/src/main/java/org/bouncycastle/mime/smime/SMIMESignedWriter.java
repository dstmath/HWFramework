package org.bouncycastle.mime.smime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedDataStreamGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.mime.Headers;
import org.bouncycastle.mime.MimeWriter;
import org.bouncycastle.mime.encoding.Base64OutputStream;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.Strings;

public class SMIMESignedWriter extends MimeWriter {
    public static final Map RFC3851_MICALGS;
    public static final Map RFC5751_MICALGS;
    public static final Map STANDARD_MICALGS = RFC5751_MICALGS;
    private final String boundary;
    private final String contentTransferEncoding;
    private final OutputStream mimeOut;
    private final CMSSignedDataStreamGenerator sigGen;

    public static class Builder {
        private static final String[] detHeaders = {"Content-Type"};
        private static final String[] detValues = {"multipart/signed; protocol=\"application/pkcs7-signature\""};
        private static final String[] encHeaders = {"Content-Type", "Content-Disposition", "Content-Transfer-Encoding", "Content-Description"};
        private static final String[] encValues = {"application/pkcs7-mime; name=\"smime.p7m\"; smime-type=enveloped-data", "attachment; filename=\"smime.p7m\"", "base64", "S/MIME Signed Message"};
        String contentTransferEncoding;
        private final boolean encapsulated;
        private final Map<String, String> extraHeaders;
        private final Map micAlgs;
        private final CMSSignedDataStreamGenerator sigGen;

        public Builder() {
            this(false);
        }

        public Builder(boolean z) {
            this.sigGen = new CMSSignedDataStreamGenerator();
            this.extraHeaders = new LinkedHashMap();
            this.micAlgs = SMIMESignedWriter.STANDARD_MICALGS;
            this.contentTransferEncoding = "base64";
            this.encapsulated = z;
        }

        private void addBoundary(StringBuffer stringBuffer, String str) {
            stringBuffer.append(";\r\n\tboundary=\"");
            stringBuffer.append(str);
            stringBuffer.append("\"");
        }

        private void addHashHeader(StringBuffer stringBuffer, List list) {
            Iterator it = list.iterator();
            TreeSet<String> treeSet = new TreeSet();
            while (it.hasNext()) {
                String str = (String) this.micAlgs.get(((AlgorithmIdentifier) it.next()).getAlgorithm());
                if (str == null) {
                    str = "unknown";
                }
                treeSet.add(str);
            }
            int i = 0;
            for (String str2 : treeSet) {
                if (i == 0) {
                    stringBuffer.append(treeSet.size() != 1 ? "; micalg=\"" : "; micalg=");
                } else {
                    stringBuffer.append(',');
                }
                stringBuffer.append(str2);
                i++;
            }
            if (!(i == 0 || treeSet.size() == 1)) {
                stringBuffer.append('\"');
            }
        }

        private String generateBoundary() {
            SecureRandom secureRandom = new SecureRandom();
            return "==" + new BigInteger(180, secureRandom).setBit(179).toString(16) + "=";
        }

        public Builder addCertificate(X509CertificateHolder x509CertificateHolder) throws CMSException {
            this.sigGen.addCertificate(x509CertificateHolder);
            return this;
        }

        public Builder addCertificates(Store store) throws CMSException {
            this.sigGen.addCertificates(store);
            return this;
        }

        public Builder addSignerInfoGenerator(SignerInfoGenerator signerInfoGenerator) {
            this.sigGen.addSignerInfoGenerator(signerInfoGenerator);
            return this;
        }

        public SMIMESignedWriter build(OutputStream outputStream) {
            String str;
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            int i = 0;
            if (!this.encapsulated) {
                str = generateBoundary();
                StringBuffer stringBuffer = new StringBuffer(detValues[0]);
                addHashHeader(stringBuffer, this.sigGen.getDigestAlgorithms());
                addBoundary(stringBuffer, str);
                linkedHashMap.put(detHeaders[0], stringBuffer.toString());
                int i2 = 1;
                while (true) {
                    String[] strArr = detHeaders;
                    if (i2 >= strArr.length) {
                        break;
                    }
                    linkedHashMap.put(strArr[i2], detValues[i2]);
                    i2++;
                }
            } else {
                str = null;
                while (true) {
                    String[] strArr2 = encHeaders;
                    if (i == strArr2.length) {
                        break;
                    }
                    linkedHashMap.put(strArr2[i], encValues[i]);
                    i++;
                }
            }
            for (Map.Entry<String, String> entry : this.extraHeaders.entrySet()) {
                linkedHashMap.put(entry.getKey(), entry.getValue());
            }
            return new SMIMESignedWriter(this, linkedHashMap, str, outputStream);
        }

        public Builder withHeader(String str, String str2) {
            this.extraHeaders.put(str, str2);
            return this;
        }
    }

    private class ContentOutputStream extends OutputStream {
        private final OutputStream backing;
        private final OutputStream main;
        private final OutputStream sigBase;
        private final ByteArrayOutputStream sigStream;

        ContentOutputStream(OutputStream outputStream, OutputStream outputStream2, ByteArrayOutputStream byteArrayOutputStream, OutputStream outputStream3) {
            this.main = outputStream;
            this.backing = outputStream2;
            this.sigStream = byteArrayOutputStream;
            this.sigBase = outputStream3;
        }

        @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            if (SMIMESignedWriter.this.boundary != null) {
                this.main.close();
                this.backing.write(Strings.toByteArray("\r\n--"));
                this.backing.write(Strings.toByteArray(SMIMESignedWriter.this.boundary));
                this.backing.write(Strings.toByteArray("\r\n"));
                this.backing.write(Strings.toByteArray("Content-Type: application/pkcs7-signature; name=\"smime.p7s\"\r\n"));
                this.backing.write(Strings.toByteArray("Content-Transfer-Encoding: base64\r\n"));
                this.backing.write(Strings.toByteArray("Content-Disposition: attachment; filename=\"smime.p7s\"\r\n"));
                this.backing.write(Strings.toByteArray("\r\n"));
                OutputStream outputStream = this.sigBase;
                if (outputStream != null) {
                    outputStream.close();
                }
                this.backing.write(this.sigStream.toByteArray());
                this.backing.write(Strings.toByteArray("\r\n--"));
                this.backing.write(Strings.toByteArray(SMIMESignedWriter.this.boundary));
                this.backing.write(Strings.toByteArray("--\r\n"));
            }
            OutputStream outputStream2 = this.backing;
            if (outputStream2 != null) {
                outputStream2.close();
            }
        }

        @Override // java.io.OutputStream
        public void write(int i) throws IOException {
            this.main.write(i);
        }
    }

    static {
        HashMap hashMap = new HashMap();
        hashMap.put(CMSAlgorithm.MD5, "md5");
        hashMap.put(CMSAlgorithm.SHA1, "sha-1");
        hashMap.put(CMSAlgorithm.SHA224, "sha-224");
        hashMap.put(CMSAlgorithm.SHA256, "sha-256");
        hashMap.put(CMSAlgorithm.SHA384, "sha-384");
        hashMap.put(CMSAlgorithm.SHA512, "sha-512");
        hashMap.put(CMSAlgorithm.GOST3411, "gostr3411-94");
        hashMap.put(CMSAlgorithm.GOST3411_2012_256, "gostr3411-2012-256");
        hashMap.put(CMSAlgorithm.GOST3411_2012_512, "gostr3411-2012-512");
        RFC5751_MICALGS = Collections.unmodifiableMap(hashMap);
        HashMap hashMap2 = new HashMap();
        hashMap2.put(CMSAlgorithm.MD5, "md5");
        hashMap2.put(CMSAlgorithm.SHA1, "sha1");
        hashMap2.put(CMSAlgorithm.SHA224, "sha224");
        hashMap2.put(CMSAlgorithm.SHA256, "sha256");
        hashMap2.put(CMSAlgorithm.SHA384, "sha384");
        hashMap2.put(CMSAlgorithm.SHA512, "sha512");
        hashMap2.put(CMSAlgorithm.GOST3411, "gostr3411-94");
        hashMap2.put(CMSAlgorithm.GOST3411_2012_256, "gostr3411-2012-256");
        hashMap2.put(CMSAlgorithm.GOST3411_2012_512, "gostr3411-2012-512");
        RFC3851_MICALGS = Collections.unmodifiableMap(hashMap2);
    }

    private SMIMESignedWriter(Builder builder, Map<String, String> map, String str, OutputStream outputStream) {
        super(new Headers(mapToLines(map), builder.contentTransferEncoding));
        this.sigGen = builder.sigGen;
        this.contentTransferEncoding = builder.contentTransferEncoding;
        this.boundary = str;
        this.mimeOut = outputStream;
    }

    @Override // org.bouncycastle.mime.MimeWriter
    public OutputStream getContentStream() throws IOException {
        this.headers.dumpHeaders(this.mimeOut);
        this.mimeOut.write(Strings.toByteArray("\r\n"));
        if (this.boundary == null) {
            return null;
        }
        this.mimeOut.write(Strings.toByteArray("This is an S/MIME signed message\r\n"));
        this.mimeOut.write(Strings.toByteArray("\r\n--"));
        this.mimeOut.write(Strings.toByteArray(this.boundary));
        this.mimeOut.write(Strings.toByteArray("\r\n"));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Base64OutputStream base64OutputStream = new Base64OutputStream(byteArrayOutputStream);
        return new ContentOutputStream(this.sigGen.open((OutputStream) base64OutputStream, false, SMimeUtils.createUnclosable(this.mimeOut)), this.mimeOut, byteArrayOutputStream, base64OutputStream);
    }
}
