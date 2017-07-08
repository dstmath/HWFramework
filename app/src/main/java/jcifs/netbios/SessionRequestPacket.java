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

    public SessionRequestPacket(Name calledName, Name callingName) {
        this.type = NbtException.NOT_LISTENING_CALLING;
        this.calledName = calledName;
        this.callingName = callingName;
    }

    int writeTrailerWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        dstIndex += this.calledName.writeWireFormat(dst, dstIndex);
        return (dstIndex + this.callingName.writeWireFormat(dst, dstIndex)) - start;
    }

    int readTrailerWireFormat(InputStream in, byte[] buffer, int bufferIndex) throws IOException {
        int start = bufferIndex;
        if (in.read(buffer, bufferIndex, this.length) != this.length) {
            throw new IOException("invalid session request wire format");
        }
        bufferIndex += this.calledName.readWireFormat(buffer, bufferIndex);
        return (bufferIndex + this.callingName.readWireFormat(buffer, bufferIndex)) - start;
    }
}
