package android.net.wifi.hotspot2;

import android.net.wifi.hotspot2.omadm.PpsMoParser;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class ConfigParser {
    private static final String BOUNDARY = "boundary=";
    private static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ENCODING_BASE64 = "base64";
    private static final String TAG = "ConfigParser";
    private static final String TYPE_CA_CERT = "application/x-x509-ca-cert";
    private static final String TYPE_MULTIPART_MIXED = "multipart/mixed";
    private static final String TYPE_PASSPOINT_PROFILE = "application/x-passpoint-profile";
    private static final String TYPE_PKCS12 = "application/x-pkcs12";
    private static final String TYPE_WIFI_CONFIG = "application/x-wifi-config";

    private static class MimeHeader {
        public String boundary;
        public String contentType;
        public String encodingType;

        /* synthetic */ MimeHeader(MimeHeader -this0) {
            this();
        }

        private MimeHeader() {
            this.contentType = null;
            this.boundary = null;
            this.encodingType = null;
        }
    }

    private static class MimePart {
        public byte[] data;
        public boolean isLast;
        public String type;

        /* synthetic */ MimePart(MimePart -this0) {
            this();
        }

        private MimePart() {
            this.type = null;
            this.data = null;
            this.isLast = false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x004b A:{ExcHandler: java.io.IOException (r1_0 'e' java.lang.Exception), Splitter: B:4:0x0025} */
    /* JADX WARNING: Missing block: B:7:0x004b, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:8:0x004c, code:
            android.util.Log.e(TAG, "Failed to parse installation file: " + r1.getMessage());
     */
    /* JADX WARNING: Missing block: B:9:0x006a, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static PasspointConfiguration parsePasspointConfig(String mimeType, byte[] data) {
        if (TextUtils.equals(mimeType, TYPE_WIFI_CONFIG)) {
            try {
                return createPasspointConfig(parseMimeMultipartMessage(new LineNumberReader(new InputStreamReader(new ByteArrayInputStream(Base64.decode(new String(data, StandardCharsets.ISO_8859_1), 0)), StandardCharsets.ISO_8859_1))));
            } catch (Exception e) {
            }
        } else {
            Log.e(TAG, "Unexpected MIME type: " + mimeType);
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x008d A:{ExcHandler: java.security.GeneralSecurityException (e java.security.GeneralSecurityException), Splitter: B:18:0x0058} */
    /* JADX WARNING: Missing block: B:26:0x0096, code:
            throw new java.io.IOException("Failed to parse PCKS12 string");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static PasspointConfiguration createPasspointConfig(Map<String, byte[]> mimeParts) throws IOException {
        byte[] profileData = (byte[]) mimeParts.get(TYPE_PASSPOINT_PROFILE);
        if (profileData == null) {
            throw new IOException("Missing Passpoint Profile");
        }
        PasspointConfiguration config = PpsMoParser.parseMoText(new String(profileData));
        if (config == null) {
            throw new IOException("Failed to parse Passpoint profile");
        } else if (config.getCredential() == null) {
            throw new IOException("Passpoint profile missing credential");
        } else {
            byte[] caCertData = (byte[]) mimeParts.get(TYPE_CA_CERT);
            if (caCertData != null) {
                try {
                    config.getCredential().setCaCertificate(parseCACert(caCertData));
                } catch (CertificateException e) {
                    throw new IOException("Failed to parse CA Certificate");
                }
            }
            byte[] pkcs12Data = (byte[]) mimeParts.get(TYPE_PKCS12);
            if (pkcs12Data != null) {
                try {
                    Pair<PrivateKey, List<X509Certificate>> clientKey = parsePkcs12(pkcs12Data);
                    config.getCredential().setClientPrivateKey((PrivateKey) clientKey.first);
                    config.getCredential().setClientCertificateChain((X509Certificate[]) ((List) clientKey.second).toArray(new X509Certificate[((List) clientKey.second).size()]));
                } catch (GeneralSecurityException e2) {
                }
            }
            return config;
        }
    }

    private static Map<String, byte[]> parseMimeMultipartMessage(LineNumberReader in) throws IOException {
        MimeHeader header = parseHeaders(in);
        if (!TextUtils.equals(header.contentType, TYPE_MULTIPART_MIXED)) {
            throw new IOException("Invalid content type: " + header.contentType);
        } else if (TextUtils.isEmpty(header.boundary)) {
            throw new IOException("Missing boundary string");
        } else if (TextUtils.equals(header.encodingType, ENCODING_BASE64)) {
            String line;
            do {
                line = in.readLine();
                if (line == null) {
                    throw new IOException("Unexpected EOF before first boundary @ " + in.getLineNumber());
                }
            } while (!line.equals("--" + header.boundary));
            Map<String, byte[]> mimeParts = new HashMap();
            MimePart mimePart;
            do {
                mimePart = parseMimePart(in, header.boundary);
                mimeParts.put(mimePart.type, mimePart.data);
            } while (!mimePart.isLast);
            return mimeParts;
        } else {
            throw new IOException("Unexpected encoding: " + header.encodingType);
        }
    }

    private static MimePart parseMimePart(LineNumberReader in, String boundary) throws IOException {
        MimeHeader header = parseHeaders(in);
        if (!TextUtils.equals(header.encodingType, ENCODING_BASE64)) {
            throw new IOException("Unexpected encoding type: " + header.encodingType);
        } else if (TextUtils.equals(header.contentType, TYPE_PASSPOINT_PROFILE) || (TextUtils.equals(header.contentType, TYPE_CA_CERT) ^ 1) == 0 || (TextUtils.equals(header.contentType, TYPE_PKCS12) ^ 1) == 0) {
            StringBuilder text = new StringBuilder();
            boolean isLast = false;
            String partBoundary = "--" + boundary;
            String endBoundary = partBoundary + "--";
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    throw new IOException("Unexpected EOF file in body @ " + in.getLineNumber());
                } else if (line.startsWith(partBoundary)) {
                    if (line.equals(endBoundary)) {
                        isLast = true;
                    }
                    MimePart part = new MimePart();
                    part.type = header.contentType;
                    part.data = Base64.decode(text.toString(), 0);
                    part.isLast = isLast;
                    return part;
                } else {
                    text.append(line);
                }
            }
        } else {
            throw new IOException("Unexpected content type: " + header.contentType);
        }
    }

    private static MimeHeader parseHeaders(LineNumberReader in) throws IOException {
        MimeHeader header = new MimeHeader();
        for (Entry<String, String> entry : readHeaders(in).entrySet()) {
            String str = (String) entry.getKey();
            if (str.equals(CONTENT_TYPE)) {
                Pair<String, String> value = parseContentType((String) entry.getValue());
                header.contentType = (String) value.first;
                header.boundary = (String) value.second;
            } else if (str.equals(CONTENT_TRANSFER_ENCODING)) {
                header.encodingType = (String) entry.getValue();
            } else {
                Log.d(TAG, "Ignore header: " + ((String) entry.getKey()));
            }
        }
        return header;
    }

    private static Pair<String, String> parseContentType(String contentType) throws IOException {
        String[] attributes = contentType.split(";");
        Object boundary = null;
        if (attributes.length < 1) {
            throw new IOException("Invalid Content-Type: " + contentType);
        }
        String type = attributes[0].trim();
        for (int i = 1; i < attributes.length; i++) {
            String attribute = attributes[i].trim();
            if (attribute.startsWith(BOUNDARY)) {
                boundary = attribute.substring(BOUNDARY.length());
                if (boundary.length() > 1 && boundary.startsWith("\"") && boundary.endsWith("\"")) {
                    boundary = boundary.substring(1, boundary.length() - 1);
                }
            } else {
                Log.d(TAG, "Ignore Content-Type attribute: " + attributes[i]);
            }
        }
        return new Pair(type, boundary);
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x003e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Map<String, String> readHeaders(LineNumberReader in) throws IOException {
        Map<String, String> headers = new HashMap();
        Object name = null;
        StringBuilder value = null;
        while (true) {
            String line = in.readLine();
            if (line == null) {
                throw new IOException("Missing line @ " + in.getLineNumber());
            } else if (line.length() != 0 && line.trim().length() != 0) {
                int nameEnd = line.indexOf(58);
                if (nameEnd < 0) {
                    if (value != null) {
                        value.append(' ').append(line.trim());
                    } else {
                        throw new IOException("Bad header line: '" + line + "' @ " + in.getLineNumber());
                    }
                } else if (Character.isWhitespace(line.charAt(0))) {
                    throw new IOException("Illegal blank prefix in header line '" + line + "' @ " + in.getLineNumber());
                } else {
                    if (name != null) {
                        headers.put(name, value.toString());
                    }
                    name = line.substring(0, nameEnd).trim();
                    value = new StringBuilder();
                    value.append(line.substring(nameEnd + 1).trim());
                }
            } else if (name != null) {
                headers.put(name, value.toString());
            }
        }
        if (name != null) {
        }
        return headers;
    }

    private static X509Certificate parseCACert(byte[] octets) throws CertificateException {
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(octets));
    }

    private static Pair<PrivateKey, List<X509Certificate>> parsePkcs12(byte[] octets) throws GeneralSecurityException, IOException {
        int i = 0;
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ByteArrayInputStream in = new ByteArrayInputStream(octets);
        ks.load(in, new char[0]);
        in.close();
        if (ks.size() != 1) {
            throw new IOException("Unexpected key size: " + ks.size());
        }
        String alias = (String) ks.aliases().nextElement();
        if (alias == null) {
            throw new IOException("No alias found");
        }
        PrivateKey clientKey = (PrivateKey) ks.getKey(alias, null);
        Object clientCertificateChain = null;
        Certificate[] chain = ks.getCertificateChain(alias);
        if (chain != null) {
            clientCertificateChain = new ArrayList();
            int length = chain.length;
            while (i < length) {
                Certificate certificate = chain[i];
                if (certificate instanceof X509Certificate) {
                    clientCertificateChain.add((X509Certificate) certificate);
                    i++;
                } else {
                    throw new IOException("Unexpceted certificate type: " + certificate.getClass());
                }
            }
        }
        return new Pair(clientKey, clientCertificateChain);
    }
}
