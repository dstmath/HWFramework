package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class EDIPartyName implements GeneralNameInterface {
    private static final byte TAG_ASSIGNER = (byte) 0;
    private static final byte TAG_PARTYNAME = (byte) 1;
    private String assigner;
    private int myhash;
    private String party;

    public EDIPartyName(String assignerName, String partyName) {
        this.assigner = null;
        this.party = null;
        this.myhash = -1;
        this.assigner = assignerName;
        this.party = partyName;
    }

    public EDIPartyName(String partyName) {
        this.assigner = null;
        this.party = null;
        this.myhash = -1;
        this.party = partyName;
    }

    public EDIPartyName(DerValue derValue) throws IOException {
        this.assigner = null;
        this.party = null;
        this.myhash = -1;
        if (len < 1 || len > 2) {
            throw new IOException("Invalid encoding of EDIPartyName");
        }
        for (DerValue opt : new DerInputStream(derValue.toByteArray()).getSequence(2)) {
            DerValue opt2;
            if (opt2.isContextSpecific(TAG_ASSIGNER) && !opt2.isConstructed()) {
                if (this.assigner != null) {
                    throw new IOException("Duplicate nameAssigner found in EDIPartyName");
                }
                opt2 = opt2.data.getDerValue();
                this.assigner = opt2.getAsString();
            }
            if (opt2.isContextSpecific(TAG_PARTYNAME) && !opt2.isConstructed()) {
                if (this.party != null) {
                    throw new IOException("Duplicate partyName found in EDIPartyName");
                }
                this.party = opt2.data.getDerValue().getAsString();
            }
        }
    }

    public int getType() {
        return 5;
    }

    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream tagged = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();
        if (this.assigner != null) {
            DerOutputStream tmp2 = new DerOutputStream();
            tmp2.putPrintableString(this.assigner);
            tagged.write(DerValue.createTag(DerValue.TAG_CONTEXT, false, TAG_ASSIGNER), tmp2);
        }
        if (this.party == null) {
            throw new IOException("Cannot have null partyName");
        }
        tmp.putPrintableString(this.party);
        tagged.write(DerValue.createTag(DerValue.TAG_CONTEXT, false, TAG_PARTYNAME), tmp);
        out.write((byte) DerValue.tag_SequenceOf, tagged);
    }

    public String getAssignerName() {
        return this.assigner;
    }

    public String getPartyName() {
        return this.party;
    }

    public boolean equals(Object other) {
        if (!(other instanceof EDIPartyName)) {
            return false;
        }
        String otherAssigner = ((EDIPartyName) other).assigner;
        if (this.assigner == null) {
            if (otherAssigner != null) {
                return false;
            }
        } else if (!this.assigner.equals(otherAssigner)) {
            return false;
        }
        String otherParty = ((EDIPartyName) other).party;
        if (this.party == null) {
            if (otherParty != null) {
                return false;
            }
        } else if (!this.party.equals(otherParty)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        if (this.myhash == -1) {
            this.myhash = this.party.hashCode() + 37;
            if (this.assigner != null) {
                this.myhash = (this.myhash * 37) + this.assigner.hashCode();
            }
        }
        return this.myhash;
    }

    public String toString() {
        String str;
        StringBuilder append = new StringBuilder().append("EDIPartyName: ");
        if (this.assigner == null) {
            str = "";
        } else {
            str = "  nameAssigner = " + this.assigner + ",";
        }
        return append.append(str).append("  partyName = ").append(this.party).toString();
    }

    public int constrains(GeneralNameInterface inputName) throws UnsupportedOperationException {
        if (inputName == null) {
            return -1;
        }
        if (inputName.getType() != 5) {
            return -1;
        }
        throw new UnsupportedOperationException("Narrowing, widening, and matching of names not supported for EDIPartyName");
    }

    public int subtreeDepth() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("subtreeDepth() not supported for EDIPartyName");
    }
}
