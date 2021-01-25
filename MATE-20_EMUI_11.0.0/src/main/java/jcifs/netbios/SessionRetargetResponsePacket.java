package jcifs.netbios;

import java.io.IOException;
import java.io.InputStream;

class SessionRetargetResponsePacket extends SessionServicePacket {
    private NbtAddress retargetAddress;
    private int retargetPort;

    SessionRetargetResponsePacket() {
        this.type = 132;
        this.length = 6;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.netbios.SessionServicePacket
    public int writeTrailerWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.netbios.SessionServicePacket
    public int readTrailerWireFormat(InputStream in, byte[] buffer, int bufferIndex) throws IOException {
        if (in.read(buffer, bufferIndex, this.length) != this.length) {
            throw new IOException("unexpected EOF reading netbios retarget session response");
        }
        this.retargetAddress = new NbtAddress(null, readInt4(buffer, bufferIndex), false, 0);
        this.retargetPort = readInt2(buffer, bufferIndex + 4);
        return this.length;
    }
}
