package android.nfc;

import android.net.NetworkPolicyManager;

public final class NfcEvent {
    public final NfcAdapter nfcAdapter;
    public final int peerLlcpMajorVersion;
    public final int peerLlcpMinorVersion;

    NfcEvent(NfcAdapter nfcAdapter, byte peerLlcpVersion) {
        this.nfcAdapter = nfcAdapter;
        this.peerLlcpMajorVersion = (peerLlcpVersion & NetworkPolicyManager.MASK_ALL_NETWORKS) >> 4;
        this.peerLlcpMinorVersion = peerLlcpVersion & 15;
    }
}
