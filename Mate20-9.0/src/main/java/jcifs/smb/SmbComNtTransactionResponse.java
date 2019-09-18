package jcifs.smb;

import jcifs.util.LogStream;

abstract class SmbComNtTransactionResponse extends SmbComTransactionResponse {
    SmbComNtTransactionResponse() {
    }

    /* access modifiers changed from: package-private */
    public int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        int start = bufferIndex;
        int bufferIndex2 = bufferIndex + 1;
        buffer[bufferIndex] = 0;
        int bufferIndex3 = bufferIndex2 + 1;
        buffer[bufferIndex2] = 0;
        int bufferIndex4 = bufferIndex3 + 1;
        buffer[bufferIndex3] = 0;
        this.totalParameterCount = readInt4(buffer, bufferIndex4);
        if (this.bufDataStart == 0) {
            this.bufDataStart = this.totalParameterCount;
        }
        int bufferIndex5 = bufferIndex4 + 4;
        this.totalDataCount = readInt4(buffer, bufferIndex5);
        int bufferIndex6 = bufferIndex5 + 4;
        this.parameterCount = readInt4(buffer, bufferIndex6);
        int bufferIndex7 = bufferIndex6 + 4;
        this.parameterOffset = readInt4(buffer, bufferIndex7);
        int bufferIndex8 = bufferIndex7 + 4;
        this.parameterDisplacement = readInt4(buffer, bufferIndex8);
        int bufferIndex9 = bufferIndex8 + 4;
        this.dataCount = readInt4(buffer, bufferIndex9);
        int bufferIndex10 = bufferIndex9 + 4;
        this.dataOffset = readInt4(buffer, bufferIndex10);
        int bufferIndex11 = bufferIndex10 + 4;
        this.dataDisplacement = readInt4(buffer, bufferIndex11);
        int bufferIndex12 = bufferIndex11 + 4;
        this.setupCount = buffer[bufferIndex12] & 255;
        int bufferIndex13 = bufferIndex12 + 2;
        if (this.setupCount != 0) {
            LogStream logStream = log;
            if (LogStream.level >= 3) {
                log.println("setupCount is not zero: " + this.setupCount);
            }
        }
        return bufferIndex13 - start;
    }
}
