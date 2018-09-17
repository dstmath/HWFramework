package jcifs.smb;

import jcifs.util.Hexdump;

abstract class AndXServerMessageBlock extends ServerMessageBlock {
    private static final int ANDX_COMMAND_OFFSET = 1;
    private static final int ANDX_OFFSET_OFFSET = 3;
    private static final int ANDX_RESERVED_OFFSET = 2;
    ServerMessageBlock andx = null;
    private byte andxCommand = (byte) -1;
    private int andxOffset = 0;

    AndXServerMessageBlock() {
    }

    AndXServerMessageBlock(ServerMessageBlock andx) {
        if (andx != null) {
            this.andx = andx;
            this.andxCommand = andx.command;
        }
    }

    int getBatchLimit(byte command) {
        return 0;
    }

    int encode(byte[] dst, int dstIndex) {
        this.headerStart = dstIndex;
        int start = dstIndex;
        dstIndex += writeHeaderWireFormat(dst, dstIndex);
        this.length = (dstIndex + writeAndXWireFormat(dst, dstIndex)) - start;
        if (this.digest != null) {
            this.digest.sign(dst, this.headerStart, this.length, this, this.response);
        }
        return this.length;
    }

    int decode(byte[] buffer, int bufferIndex) {
        this.headerStart = bufferIndex;
        int start = bufferIndex;
        bufferIndex += readHeaderWireFormat(buffer, bufferIndex);
        this.length = (bufferIndex + readAndXWireFormat(buffer, bufferIndex)) - start;
        return this.length;
    }

    int writeAndXWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        this.wordCount = writeParameterWordsWireFormat(dst, (start + 3) + 2);
        this.wordCount += 4;
        dstIndex += this.wordCount + 1;
        this.wordCount /= 2;
        dst[start] = (byte) (this.wordCount & 255);
        this.byteCount = writeBytesWireFormat(dst, dstIndex + 2);
        int i = dstIndex + 1;
        dst[dstIndex] = (byte) (this.byteCount & 255);
        dstIndex = i + 1;
        dst[i] = (byte) ((this.byteCount >> 8) & 255);
        dstIndex += this.byteCount;
        if (this.andx == null || !USE_BATCHING || this.batchLevel >= getBatchLimit(this.andx.command)) {
            this.andxCommand = (byte) -1;
            this.andx = null;
            dst[start + 1] = (byte) -1;
            dst[start + 2] = (byte) 0;
            dst[start + 3] = (byte) -34;
            dst[(start + 3) + 1] = (byte) -34;
            return dstIndex - start;
        }
        this.andx.batchLevel = this.batchLevel + 1;
        dst[start + 1] = this.andxCommand;
        dst[start + 2] = (byte) 0;
        this.andxOffset = dstIndex - this.headerStart;
        ServerMessageBlock.writeInt2((long) this.andxOffset, dst, start + 3);
        this.andx.useUnicode = this.useUnicode;
        if (this.andx instanceof AndXServerMessageBlock) {
            this.andx.uid = this.uid;
            dstIndex += ((AndXServerMessageBlock) this.andx).writeAndXWireFormat(dst, dstIndex);
        } else {
            int andxStart = dstIndex;
            this.andx.wordCount = this.andx.writeParameterWordsWireFormat(dst, dstIndex);
            dstIndex += this.andx.wordCount + 1;
            ServerMessageBlock serverMessageBlock = this.andx;
            serverMessageBlock.wordCount /= 2;
            dst[andxStart] = (byte) (this.andx.wordCount & 255);
            this.andx.byteCount = this.andx.writeBytesWireFormat(dst, dstIndex + 2);
            i = dstIndex + 1;
            dst[dstIndex] = (byte) (this.andx.byteCount & 255);
            dstIndex = i + 1;
            dst[i] = (byte) ((this.andx.byteCount >> 8) & 255);
            dstIndex += this.andx.byteCount;
        }
        return dstIndex - start;
    }

    int readAndXWireFormat(byte[] buffer, int bufferIndex) {
        int start = bufferIndex;
        int bufferIndex2 = bufferIndex + 1;
        this.wordCount = buffer[bufferIndex];
        if (this.wordCount != 0) {
            this.andxCommand = buffer[bufferIndex2];
            this.andxOffset = ServerMessageBlock.readInt2(buffer, bufferIndex2 + 2);
            if (this.andxOffset == 0) {
                this.andxCommand = (byte) -1;
            }
            if (this.wordCount > 2) {
                readParameterWordsWireFormat(buffer, bufferIndex2 + 4);
                if (this.command == (byte) -94 && ((SmbComNTCreateAndXResponse) this).isExtended) {
                    this.wordCount += 8;
                }
            }
            bufferIndex = (start + 1) + (this.wordCount * 2);
        } else {
            bufferIndex = bufferIndex2;
        }
        this.byteCount = ServerMessageBlock.readInt2(buffer, bufferIndex);
        bufferIndex += 2;
        if (this.byteCount != 0) {
            int n = readBytesWireFormat(buffer, bufferIndex);
            bufferIndex += this.byteCount;
        }
        if (this.errorCode != 0 || this.andxCommand == (byte) -1) {
            this.andxCommand = (byte) -1;
            this.andx = null;
        } else if (this.andx == null) {
            this.andxCommand = (byte) -1;
            throw new RuntimeException("no andx command supplied with response");
        } else {
            bufferIndex = this.headerStart + this.andxOffset;
            this.andx.headerStart = this.headerStart;
            this.andx.command = this.andxCommand;
            this.andx.errorCode = this.errorCode;
            this.andx.flags = this.flags;
            this.andx.flags2 = this.flags2;
            this.andx.tid = this.tid;
            this.andx.pid = this.pid;
            this.andx.uid = this.uid;
            this.andx.mid = this.mid;
            this.andx.useUnicode = this.useUnicode;
            if (this.andx instanceof AndXServerMessageBlock) {
                bufferIndex += ((AndXServerMessageBlock) this.andx).readAndXWireFormat(buffer, bufferIndex);
            } else {
                bufferIndex2 = bufferIndex + 1;
                buffer[bufferIndex] = (byte) (this.andx.wordCount & 255);
                if (this.andx.wordCount == 0 || this.andx.wordCount <= 2) {
                    bufferIndex = bufferIndex2;
                } else {
                    bufferIndex = bufferIndex2 + this.andx.readParameterWordsWireFormat(buffer, bufferIndex2);
                }
                this.andx.byteCount = ServerMessageBlock.readInt2(buffer, bufferIndex);
                bufferIndex += 2;
                if (this.andx.byteCount != 0) {
                    this.andx.readBytesWireFormat(buffer, bufferIndex);
                    bufferIndex += this.andx.byteCount;
                }
            }
            this.andx.received = true;
        }
        return bufferIndex - start;
    }

    public String toString() {
        return new String(super.toString() + ",andxCommand=0x" + Hexdump.toHexString(this.andxCommand, 2) + ",andxOffset=" + this.andxOffset);
    }
}
