package sun.security.ssl;

import java.security.SecureRandom;

final class SessionId {
    private byte[] sessionId;

    SessionId(boolean isRejoinable, SecureRandom generator) {
        if (isRejoinable) {
            this.sessionId = new RandomCookie(generator).random_bytes;
        } else {
            this.sessionId = new byte[0];
        }
    }

    SessionId(byte[] sessionId) {
        this.sessionId = sessionId;
    }

    int length() {
        return this.sessionId.length;
    }

    byte[] getId() {
        return (byte[]) this.sessionId.clone();
    }

    public String toString() {
        int len = this.sessionId.length;
        StringBuffer s = new StringBuffer((len * 2) + 10);
        s.append("{");
        for (int i = 0; i < len; i++) {
            s.append(this.sessionId[i] & 255);
            if (i != len - 1) {
                s.append(", ");
            }
        }
        s.append("}");
        return s.toString();
    }

    public int hashCode() {
        int retval = 0;
        for (byte b : this.sessionId) {
            retval += b;
        }
        return retval;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SessionId)) {
            return false;
        }
        byte[] b = ((SessionId) obj).getId();
        if (b.length != this.sessionId.length) {
            return false;
        }
        for (int i = 0; i < this.sessionId.length; i++) {
            if (b[i] != this.sessionId[i]) {
                return false;
            }
        }
        return true;
    }
}
