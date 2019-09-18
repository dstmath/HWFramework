package jcifs.smb;

import java.io.Serializable;
import jcifs.UniAddress;
import jcifs.util.Hexdump;

public final class NtlmChallenge implements Serializable {
    public byte[] challenge;
    public UniAddress dc;

    NtlmChallenge(byte[] challenge2, UniAddress dc2) {
        this.challenge = challenge2;
        this.dc = dc2;
    }

    public String toString() {
        return "NtlmChallenge[challenge=0x" + Hexdump.toHexString(this.challenge, 0, this.challenge.length * 2) + ",dc=" + this.dc.toString() + "]";
    }
}
