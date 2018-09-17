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

    int writeTrailerWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int readTrailerWireFormat(InputStream in, byte[] buffer, int bufferIndex) throws IOException {
        if (in.read(buffer, bufferIndex, this.length) != this.length) {
            throw new IOException("unexpected EOF reading netbios retarget session response");
        }
        int addr = SessionServicePacket.readInt4(buffer, bufferIndex);
        bufferIndex += 4;
        this.retargetAddress = new NbtAddress(null, addr, false, 0);
        this.retargetPort = SessionServicePacket.readInt2(buffer, bufferIndex);
        return this.length;
    }
}
