package android.nfc;

public final class NfcEvent {
    public final NfcAdapter nfcAdapter;
    public final int peerLlcpMajorVersion;
    public final int peerLlcpMinorVersion;

    NfcEvent(NfcAdapter nfcAdapter2, byte peerLlcpVersion) {
        this.nfcAdapter = nfcAdapter2;
        this.peerLlcpMajorVersion = (peerLlcpVersion & 240) >> 4;
        this.peerLlcpMinorVersion = peerLlcpVersion & 15;
    }
}
