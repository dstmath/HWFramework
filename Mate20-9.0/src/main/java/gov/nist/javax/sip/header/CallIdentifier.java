package gov.nist.javax.sip.header;

import gov.nist.core.Separators;

public final class CallIdentifier extends SIPObject {
    private static final long serialVersionUID = 7314773655675451377L;
    protected String host;
    protected String localId;

    public CallIdentifier() {
    }

    public CallIdentifier(String localId2, String host2) {
        this.localId = localId2;
        this.host = host2;
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
            buffer.append(Separators.AT);
            buffer.append(this.host);
        }
        return buffer;
    }

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
        if ((this.host != null || that.host == null) && ((this.host == null || that.host != null) && this.host.compareToIgnoreCase(that.host) == 0)) {
            return true;
        }
        return false;
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

    public void setLocalId(String localId2) {
        this.localId = localId2;
    }

    public void setCallID(String cid) throws IllegalArgumentException {
        if (cid != null) {
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
            return;
        }
        throw new IllegalArgumentException("NULL!");
    }

    public void setHost(String host2) {
        this.host = host2;
    }
}
