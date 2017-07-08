package jcifs.smb;

import jcifs.util.LogStream;

abstract class SmbComNtTransactionResponse extends SmbComTransactionResponse {
    SmbComNtTransactionResponse() {
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        int start = bufferIndex;
        int bufferIndex2 = bufferIndex + 1;
        buffer[bufferIndex] = (byte) 0;
        bufferIndex = bufferIndex2 + 1;
        buffer[bufferIndex2] = (byte) 0;
        bufferIndex2 = bufferIndex + 1;
        buffer[bufferIndex] = (byte) 0;
        this.totalParameterCount = ServerMessageBlock.readInt4(buffer, bufferIndex2);
        if (this.bufDataStart == 0) {
            this.bufDataStart = this.totalParameterCount;
        }
        bufferIndex = bufferIndex2 + 4;
        this.totalDataCount = ServerMessageBlock.readInt4(buffer, bufferIndex);
        bufferIndex += 4;
        this.parameterCount = ServerMessageBlock.readInt4(buffer, bufferIndex);
        bufferIndex += 4;
        this.parameterOffset = ServerMessageBlock.readInt4(buffer, bufferIndex);
        bufferIndex += 4;
        this.parameterDisplacement = ServerMessageBlock.readInt4(buffer, bufferIndex);
        bufferIndex += 4;
        this.dataCount = ServerMessageBlock.readInt4(buffer, bufferIndex);
        bufferIndex += 4;
        this.dataOffset = ServerMessageBlock.readInt4(buffer, bufferIndex);
        bufferIndex += 4;
        this.dataDisplacement = ServerMessageBlock.readInt4(buffer, bufferIndex);
        bufferIndex += 4;
        this.setupCount = buffer[bufferIndex] & 255;
        bufferIndex += 2;
        if (this.setupCount != 0) {
            LogStream logStream = log;
            if (LogStream.level >= 3) {
                log.println("setupCount is not zero: " + this.setupCount);
            }
        }
        return bufferIndex - start;
    }
}
