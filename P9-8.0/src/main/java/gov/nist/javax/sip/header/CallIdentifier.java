package gov.nist.javax.sip.header;

import gov.nist.core.Separators;

public final class CallIdentifier extends SIPObject {
    private static final long serialVersionUID = 7314773655675451377L;
    protected String host;
    protected String localId;

    public CallIdentifier(String localId, String host) {
        this.localId = localId;
        this.host = host;
    }

    public CallIdentifier(String cid) throws IllegalArgumentException {
        setCallID(cid);
    }

    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    public StringBuffer encode(StringBuffer buffer) {
        buffer.append(this.localId);
        if (this.host != null) {
            buffer.append(Separators.AT).append(this.host);
        }
        return buffer;
    }

    /* JADX WARNING: Missing block: B:16:0x0031, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(getClass())) {
            return false;
        }
        CallIdentifier that = (CallIdentifier) other;
        if (this.localId.compareTo(that.localId) != 0) {
            return false;
        }
        if (this.host == that.host) {
            return true;
        }
        return (this.host != null || that.host == null) && ((this.host == null || that.host != null) && this.host.compareToIgnoreCase(that.host) == 0);
    }

    public int hashCode() {
        if (this.localId != null) {
            return this.localId.hashCode();
        }
        throw new UnsupportedOperationException("Hash code called before id is set");
    }

    public String getLocalId() {
        return this.localId;
    }

    public String getHost() {
        return this.host;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public void setCallID(String cid) throws IllegalArgumentException {
        if (cid == null) {
            throw new IllegalArgumentException("NULL!");
        }
        int index = cid.indexOf(64);
        if (index == -1) {
            this.localId = cid;
            this.host = null;
            return;
        }
        this.localId = cid.substring(0, index);
        this.host = cid.substring(index + 1, cid.length());
        if (this.localId == null || this.host == null) {
            throw new IllegalArgumentException("CallID  must be token@token or token");
        }
    }

    public void setHost(String host) {
        this.host = host;
    }
}
