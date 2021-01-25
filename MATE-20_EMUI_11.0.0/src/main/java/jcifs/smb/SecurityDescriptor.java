package jcifs.smb;

import java.io.IOException;

public class SecurityDescriptor {
    public ACE[] aces;
    public int type;

    public SecurityDescriptor() {
    }

    public SecurityDescriptor(byte[] buffer, int bufferIndex, int len) throws IOException {
        decode(buffer, bufferIndex, len);
    }

    public int decode(byte[] buffer, int bufferIndex, int len) throws IOException {
        int bufferIndex2 = bufferIndex + 1 + 1;
        this.type = ServerMessageBlock.readInt2(buffer, bufferIndex2);
        int bufferIndex3 = bufferIndex2 + 2;
        ServerMessageBlock.readInt4(buffer, bufferIndex3);
        int bufferIndex4 = bufferIndex3 + 4;
        ServerMessageBlock.readInt4(buffer, bufferIndex4);
        int bufferIndex5 = bufferIndex4 + 4;
        ServerMessageBlock.readInt4(buffer, bufferIndex5);
        int daclOffset = ServerMessageBlock.readInt4(buffer, bufferIndex5 + 4);
        int bufferIndex6 = bufferIndex + daclOffset + 1 + 1;
        ServerMessageBlock.readInt2(buffer, bufferIndex6);
        int bufferIndex7 = bufferIndex6 + 2;
        int numAces = ServerMessageBlock.readInt4(buffer, bufferIndex7);
        int bufferIndex8 = bufferIndex7 + 4;
        if (numAces > 4096) {
            throw new IOException("Invalid SecurityDescriptor");
        }
        if (daclOffset != 0) {
            this.aces = new ACE[numAces];
            for (int i = 0; i < numAces; i++) {
                this.aces[i] = new ACE();
                bufferIndex8 += this.aces[i].decode(buffer, bufferIndex8);
            }
        } else {
            this.aces = null;
        }
        return bufferIndex8 - bufferIndex;
    }

    public String toString() {
        String ret = "SecurityDescriptor:\n";
        if (this.aces == null) {
            return ret + "NULL";
        }
        for (int ai = 0; ai < this.aces.length; ai++) {
            ret = ret + this.aces[ai].toString() + "\n";
        }
        return ret;
    }
}
