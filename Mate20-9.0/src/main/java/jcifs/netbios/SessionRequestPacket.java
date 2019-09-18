package jcifs.netbios;

import java.io.IOException;
import java.io.InputStream;

public class SessionRequestPacket extends SessionServicePacket {
    private Name calledName;
    private Name callingName;

    SessionRequestPacket() {
        this.calledName = new Name();
        this.callingName = new Name();
    }

    public SessionRequestPacket(Name calledName2, Name callingName2) {
        this.type = NbtException.NOT_LISTENING_CALLING;
        this.calledName = calledName2;
        this.callingName = callingName2;
    }

    /* access modifiers changed from: package-private */
    public int writeTrailerWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        int dstIndex2 = dstIndex + this.calledName.writeWireFormat(dst, dstIndex);
        return (dstIndex2 + this.callingName.writeWireFormat(dst, dstIndex2)) - start;
    }

    /* access modifiers changed from: package-private */
    public int readTrailerWireFormat(InputStream in, byte[] buffer, int bufferIndex) throws IOException {
        int start = bufferIndex;
        if (in.read(buffer, bufferIndex, this.length) != this.length) {
            throw new IOException("invalid session request wire format");
        }
        int bufferIndex2 = bufferIndex + this.calledName.readWireFormat(buffer, bufferIndex);
        return (bufferIndex2 + this.callingName.readWireFormat(buffer, bufferIndex2)) - start;
    }
}
