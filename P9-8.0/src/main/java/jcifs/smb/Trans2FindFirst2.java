package jcifs.smb;

import jcifs.Config;
import jcifs.util.Hexdump;

class Trans2FindFirst2 extends SmbComTransaction {
    private static final int DEFAULT_LIST_COUNT = 200;
    private static final int DEFAULT_LIST_SIZE = 65535;
    private static final int FLAGS_CLOSE_AFTER_THIS_REQUEST = 1;
    private static final int FLAGS_CLOSE_IF_END_REACHED = 2;
    private static final int FLAGS_FIND_WITH_BACKUP_INTENT = 16;
    private static final int FLAGS_RESUME_FROM_PREVIOUS_END = 8;
    private static final int FLAGS_RETURN_RESUME_KEYS = 4;
    static final int LIST_COUNT = Config.getInt("jcifs.smb.client.listCount", DEFAULT_LIST_COUNT);
    static final int LIST_SIZE = Config.getInt("jcifs.smb.client.listSize", DEFAULT_LIST_SIZE);
    static final int SMB_FILE_BOTH_DIRECTORY_INFO = 260;
    static final int SMB_FILE_NAMES_INFO = 259;
    static final int SMB_FIND_FILE_DIRECTORY_INFO = 257;
    static final int SMB_FIND_FILE_FULL_DIRECTORY_INFO = 258;
    static final int SMB_INFO_QUERY_EAS_FROM_LIST = 3;
    static final int SMB_INFO_QUERY_EA_SIZE = 2;
    static final int SMB_INFO_STANDARD = 1;
    private int flags;
    private int informationLevel;
    private int searchAttributes;
    private int searchStorageType = 0;
    private String wildcard;

    Trans2FindFirst2(String filename, String wildcard, int searchAttributes) {
        if (filename.equals("\\")) {
            this.path = filename;
        } else {
            this.path = filename + "\\";
        }
        this.wildcard = wildcard;
        this.searchAttributes = searchAttributes & 55;
        this.command = (byte) 50;
        this.subCommand = (byte) 1;
        this.flags = 0;
        this.informationLevel = SMB_FILE_BOTH_DIRECTORY_INFO;
        this.totalDataCount = 0;
        this.maxParameterCount = 10;
        this.maxDataCount = LIST_SIZE;
        this.maxSetupCount = (byte) 0;
    }

    int writeSetupWireFormat(byte[] dst, int dstIndex) {
        int i = dstIndex + 1;
        dst[dstIndex] = this.subCommand;
        dstIndex = i + 1;
        dst[i] = (byte) 0;
        return 2;
    }

    int writeParametersWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        ServerMessageBlock.writeInt2((long) this.searchAttributes, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt2((long) LIST_COUNT, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt2((long) this.flags, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt2((long) this.informationLevel, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt4((long) this.searchStorageType, dst, dstIndex);
        dstIndex += 4;
        return (dstIndex + writeString(this.path + this.wildcard, dst, dstIndex)) - start;
    }

    int writeDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int readSetupWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    int readParametersWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    public String toString() {
        return new String("Trans2FindFirst2[" + super.toString() + ",searchAttributes=0x" + Hexdump.toHexString(this.searchAttributes, 2) + ",searchCount=" + LIST_COUNT + ",flags=0x" + Hexdump.toHexString(this.flags, 2) + ",informationLevel=0x" + Hexdump.toHexString(this.informationLevel, 3) + ",searchStorageType=" + this.searchStorageType + ",filename=" + this.path + "]");
    }
}
