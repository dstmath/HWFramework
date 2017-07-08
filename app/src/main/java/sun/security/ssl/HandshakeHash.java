package sun.security.ssl;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Set;
import sun.util.calendar.BaseCalendar;

final class HandshakeHash {
    private final int clonesNeeded;
    private String cvAlg;
    private boolean cvAlgDetermined;
    private ByteArrayOutputStream data;
    private MessageDigest finMD;
    private final boolean isServer;
    private MessageDigest md5;
    private MessageDigest sha;
    private int version;

    HandshakeHash(boolean isServer, boolean needCertificateVerify, Set<String> set) {
        this.version = -1;
        this.data = new ByteArrayOutputStream();
        this.cvAlgDetermined = false;
        this.isServer = isServer;
        this.clonesNeeded = needCertificateVerify ? 3 : 2;
    }

    void update(byte[] b, int offset, int len) {
        switch (this.version) {
            case BaseCalendar.SUNDAY /*1*/:
                this.md5.update(b, offset, len);
                this.sha.update(b, offset, len);
            default:
                if (this.finMD != null) {
                    this.finMD.update(b, offset, len);
                }
                this.data.write(b, offset, len);
        }
    }

    void reset() {
        if (this.version != -1) {
            throw new RuntimeException("reset() can be only be called before protocolDetermined");
        }
        this.data.reset();
    }

    void protocolDetermined(ProtocolVersion pv) {
        if (this.version == -1) {
            this.version = pv.compareTo(ProtocolVersion.TLS12) >= 0 ? 2 : 1;
            switch (this.version) {
                case BaseCalendar.SUNDAY /*1*/:
                    try {
                        this.md5 = CloneableDigest.getDigest("MD5", this.clonesNeeded);
                        this.sha = CloneableDigest.getDigest("SHA", this.clonesNeeded);
                        byte[] bytes = this.data.toByteArray();
                        update(bytes, 0, bytes.length);
                        break;
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException("Algorithm MD5 or SHA not available", e);
                    }
            }
        }
    }

    MessageDigest getMD5Clone() {
        if (this.version == 1) {
            return cloneDigest(this.md5);
        }
        throw new RuntimeException("getMD5Clone() can be only be called for TLS 1.1");
    }

    MessageDigest getSHAClone() {
        if (this.version == 1) {
            return cloneDigest(this.sha);
        }
        throw new RuntimeException("getSHAClone() can be only be called for TLS 1.1");
    }

    private static MessageDigest cloneDigest(MessageDigest digest) {
        try {
            return (MessageDigest) digest.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Could not clone digest", e);
        }
    }

    private static String normalizeAlgName(String alg) {
        alg = alg.toUpperCase(Locale.US);
        if (alg.startsWith("SHA")) {
            if (alg.length() == 3) {
                return "SHA-1";
            }
            if (alg.charAt(3) != '-') {
                return "SHA-" + alg.substring(3);
            }
        }
        return alg;
    }

    void setFinishedAlg(String s) {
        if (s == null) {
            throw new RuntimeException("setFinishedAlg's argument cannot be null");
        } else if (this.finMD == null) {
            try {
                this.finMD = CloneableDigest.getDigest(normalizeAlgName(s), 2);
                this.finMD.update(this.data.toByteArray());
            } catch (Throwable e) {
                throw new Error(e);
            }
        }
    }

    void restrictCertificateVerifyAlgs(Set<String> set) {
        if (this.version == 1) {
            throw new RuntimeException("setCertificateVerifyAlg() cannot be called for TLS 1.1");
        }
    }

    void setCertificateVerifyAlg(String s) {
        String str = null;
        if (!this.cvAlgDetermined) {
            if (s != null) {
                str = normalizeAlgName(s);
            }
            this.cvAlg = str;
            this.cvAlgDetermined = true;
        }
    }

    byte[] getAllHandshakeMessages() {
        return this.data.toByteArray();
    }

    byte[] getFinishedHash() {
        try {
            return cloneDigest(this.finMD).digest();
        } catch (Exception e) {
            throw new Error("BAD");
        }
    }
}
