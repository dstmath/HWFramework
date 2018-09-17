package jcifs.smb;

import jcifs.util.Hexdump;
import jcifs.util.LogStream;

class NetServerEnum2Response extends SmbComTransactionResponse {
    private int converter;
    String lastName;
    private int totalAvailableEntries;

    class ServerInfo1 implements FileEntry {
        String commentOrMasterBrowser;
        String name;
        int type;
        int versionMajor;
        int versionMinor;

        ServerInfo1() {
        }

        public String getName() {
            return this.name;
        }

        public int getType() {
            return (this.type & Integer.MIN_VALUE) != 0 ? 2 : 4;
        }

        public int getAttributes() {
            return 17;
        }

        public long createTime() {
            return 0;
        }

        public long lastModified() {
            return 0;
        }

        public long length() {
            return 0;
        }

        public String toString() {
            return new String("ServerInfo1[name=" + this.name + ",versionMajor=" + this.versionMajor + ",versionMinor=" + this.versionMinor + ",type=0x" + Hexdump.toHexString(this.type, 8) + ",commentOrMasterBrowser=" + this.commentOrMasterBrowser + "]");
        }
    }

    NetServerEnum2Response() {
    }

    int writeSetupWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int writeParametersWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int writeDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int readSetupWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    int readParametersWireFormat(byte[] buffer, int bufferIndex, int len) {
        int start = bufferIndex;
        this.status = ServerMessageBlock.readInt2(buffer, bufferIndex);
        bufferIndex += 2;
        this.converter = ServerMessageBlock.readInt2(buffer, bufferIndex);
        bufferIndex += 2;
        this.numEntries = ServerMessageBlock.readInt2(buffer, bufferIndex);
        bufferIndex += 2;
        this.totalAvailableEntries = ServerMessageBlock.readInt2(buffer, bufferIndex);
        return (bufferIndex + 2) - start;
    }

    int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        int start = bufferIndex;
        ServerInfo1 e = null;
        this.results = new ServerInfo1[this.numEntries];
        for (int i = 0; i < this.numEntries; i++) {
            FileEntry[] fileEntryArr = this.results;
            e = new ServerInfo1();
            fileEntryArr[i] = e;
            e.name = readString(buffer, bufferIndex, 16, false);
            bufferIndex += 16;
            int bufferIndex2 = bufferIndex + 1;
            e.versionMajor = buffer[bufferIndex] & 255;
            bufferIndex = bufferIndex2 + 1;
            e.versionMinor = buffer[bufferIndex2] & 255;
            e.type = ServerMessageBlock.readInt4(buffer, bufferIndex);
            bufferIndex += 4;
            int off = ServerMessageBlock.readInt4(buffer, bufferIndex);
            bufferIndex += 4;
            e.commentOrMasterBrowser = readString(buffer, ((65535 & off) - this.converter) + start, 48, false);
            LogStream logStream = log;
            if (LogStream.level >= 4) {
                log.println(e);
            }
        }
        this.lastName = this.numEntries == 0 ? null : e.name;
        return bufferIndex - start;
    }

    public String toString() {
        return new String("NetServerEnum2Response[" + super.toString() + ",status=" + this.status + ",converter=" + this.converter + ",entriesReturned=" + this.numEntries + ",totalAvailableEntries=" + this.totalAvailableEntries + ",lastName=" + this.lastName + "]");
    }
}
