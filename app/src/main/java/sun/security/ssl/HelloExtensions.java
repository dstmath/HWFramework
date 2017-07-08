package sun.security.ssl;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.SSLProtocolException;

final class HelloExtensions {
    private int encodedLength;
    private List<HelloExtension> extensions;

    HelloExtensions() {
        this.extensions = Collections.emptyList();
    }

    HelloExtensions(HandshakeInStream s) throws IOException {
        int len = s.getInt16();
        this.extensions = new ArrayList();
        this.encodedLength = len + 2;
        while (len > 0) {
            HelloExtension extension;
            int type = s.getInt16();
            int extlen = s.getInt16();
            ExtensionType extType = ExtensionType.get(type);
            if (extType == ExtensionType.EXT_SERVER_NAME) {
                extension = new ServerNameExtension(s, extlen);
            } else if (extType == ExtensionType.EXT_SIGNATURE_ALGORITHMS) {
                extension = new SignatureAlgorithmsExtension(s, extlen);
            } else if (extType == ExtensionType.EXT_ELLIPTIC_CURVES) {
                extension = new SupportedEllipticCurvesExtension(s, extlen);
            } else if (extType == ExtensionType.EXT_EC_POINT_FORMATS) {
                extension = new SupportedEllipticPointFormatsExtension(s, extlen);
            } else if (extType == ExtensionType.EXT_RENEGOTIATION_INFO) {
                extension = new RenegotiationInfoExtension(s, extlen);
            } else {
                extension = new UnknownExtension(s, extlen, extType);
            }
            this.extensions.add(extension);
            len -= extlen + 4;
        }
        if (len != 0) {
            throw new SSLProtocolException("Error parsing extensions: extra data");
        }
    }

    List<HelloExtension> list() {
        return this.extensions;
    }

    void add(HelloExtension ext) {
        if (this.extensions.isEmpty()) {
            this.extensions = new ArrayList();
        }
        this.extensions.add(ext);
        this.encodedLength = -1;
    }

    HelloExtension get(ExtensionType type) {
        for (HelloExtension ext : this.extensions) {
            if (ext.type == type) {
                return ext;
            }
        }
        return null;
    }

    int length() {
        if (this.encodedLength >= 0) {
            return this.encodedLength;
        }
        if (this.extensions.isEmpty()) {
            this.encodedLength = 0;
        } else {
            this.encodedLength = 2;
            for (HelloExtension ext : this.extensions) {
                this.encodedLength += ext.length();
            }
        }
        return this.encodedLength;
    }

    void send(HandshakeOutStream s) throws IOException {
        int length = length();
        if (length != 0) {
            s.putInt16(length - 2);
            for (HelloExtension ext : this.extensions) {
                ext.send(s);
            }
        }
    }

    void print(PrintStream s) throws IOException {
        for (HelloExtension ext : this.extensions) {
            s.println(ext.toString());
        }
    }
}
