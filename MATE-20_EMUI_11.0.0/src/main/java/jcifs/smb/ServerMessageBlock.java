package jcifs.smb;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import jcifs.util.Hexdump;
import jcifs.util.LogStream;
import jcifs.util.transport.Request;
import jcifs.util.transport.Response;

/* access modifiers changed from: package-private */
public abstract class ServerMessageBlock extends Response implements Request, SmbConstants {
    static final byte SMB_COM_CHECK_DIRECTORY = 16;
    static final byte SMB_COM_CLOSE = 4;
    static final byte SMB_COM_CREATE_DIRECTORY = 0;
    static final byte SMB_COM_DELETE = 6;
    static final byte SMB_COM_DELETE_DIRECTORY = 1;
    static final byte SMB_COM_ECHO = 43;
    static final byte SMB_COM_FIND_CLOSE2 = 52;
    static final byte SMB_COM_LOGOFF_ANDX = 116;
    static final byte SMB_COM_MOVE = 42;
    static final byte SMB_COM_NEGOTIATE = 114;
    static final byte SMB_COM_NT_CREATE_ANDX = -94;
    static final byte SMB_COM_NT_TRANSACT = -96;
    static final byte SMB_COM_NT_TRANSACT_SECONDARY = -95;
    static final byte SMB_COM_OPEN_ANDX = 45;
    static final byte SMB_COM_QUERY_INFORMATION = 8;
    static final byte SMB_COM_READ_ANDX = 46;
    static final byte SMB_COM_RENAME = 7;
    static final byte SMB_COM_SESSION_SETUP_ANDX = 115;
    static final byte SMB_COM_TRANSACTION = 37;
    static final byte SMB_COM_TRANSACTION2 = 50;
    static final byte SMB_COM_TRANSACTION_SECONDARY = 38;
    static final byte SMB_COM_TREE_CONNECT_ANDX = 117;
    static final byte SMB_COM_TREE_DISCONNECT = 113;
    static final byte SMB_COM_WRITE = 11;
    static final byte SMB_COM_WRITE_ANDX = 47;
    static final byte[] header = {-1, 83, 77, 66, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY, SMB_COM_CREATE_DIRECTORY};
    static LogStream log = LogStream.getInstance();
    NtlmPasswordAuthentication auth = null;
    int batchLevel = 0;
    int byteCount;
    byte command;
    SigningDigest digest = null;
    int errorCode;
    boolean extendedSecurity;
    byte flags = 24;
    int flags2;
    int headerStart;
    int length;
    int mid;
    String path;
    int pid = PID;
    boolean received;
    ServerMessageBlock response;
    long responseTimeout = 1;
    int signSeq;
    int tid;
    int uid;
    boolean useUnicode;
    boolean verifyFailed;
    int wordCount;

    /* access modifiers changed from: package-private */
    public abstract int readBytesWireFormat(byte[] bArr, int i);

    /* access modifiers changed from: package-private */
    public abstract int readParameterWordsWireFormat(byte[] bArr, int i);

    /* access modifiers changed from: package-private */
    public abstract int writeBytesWireFormat(byte[] bArr, int i);

    /* access modifiers changed from: package-private */
    public abstract int writeParameterWordsWireFormat(byte[] bArr, int i);

    static void writeInt2(long val, byte[] dst, int dstIndex) {
        dst[dstIndex] = (byte) ((int) val);
        dst[dstIndex + 1] = (byte) ((int) (val >> 8));
    }

    static void writeInt4(long val, byte[] dst, int dstIndex) {
        dst[dstIndex] = (byte) ((int) val);
        int dstIndex2 = dstIndex + 1;
        long val2 = val >> 8;
        dst[dstIndex2] = (byte) ((int) val2);
        int dstIndex3 = dstIndex2 + 1;
        long val3 = val2 >> 8;
        dst[dstIndex3] = (byte) ((int) val3);
        dst[dstIndex3 + 1] = (byte) ((int) (val3 >> 8));
    }

    static int readInt2(byte[] src, int srcIndex) {
        return (src[srcIndex] & 255) + ((src[srcIndex + 1] & 255) << 8);
    }

    static int readInt4(byte[] src, int srcIndex) {
        return (src[srcIndex] & 255) + ((src[srcIndex + 1] & 255) << 8) + ((src[srcIndex + 2] & 255) << 16) + ((src[srcIndex + 3] & 255) << 24);
    }

    static long readInt8(byte[] src, int srcIndex) {
        return (((long) readInt4(src, srcIndex)) & 4294967295L) + (((long) readInt4(src, srcIndex + 4)) << 32);
    }

    static void writeInt8(long val, byte[] dst, int dstIndex) {
        dst[dstIndex] = (byte) ((int) val);
        int dstIndex2 = dstIndex + 1;
        long val2 = val >> 8;
        dst[dstIndex2] = (byte) ((int) val2);
        int dstIndex3 = dstIndex2 + 1;
        long val3 = val2 >> 8;
        dst[dstIndex3] = (byte) ((int) val3);
        int dstIndex4 = dstIndex3 + 1;
        long val4 = val3 >> 8;
        dst[dstIndex4] = (byte) ((int) val4);
        int dstIndex5 = dstIndex4 + 1;
        long val5 = val4 >> 8;
        dst[dstIndex5] = (byte) ((int) val5);
        int dstIndex6 = dstIndex5 + 1;
        long val6 = val5 >> 8;
        dst[dstIndex6] = (byte) ((int) val6);
        int dstIndex7 = dstIndex6 + 1;
        long val7 = val6 >> 8;
        dst[dstIndex7] = (byte) ((int) val7);
        dst[dstIndex7 + 1] = (byte) ((int) (val7 >> 8));
    }

    static long readTime(byte[] src, int srcIndex) {
        return (((((long) readInt4(src, srcIndex + 4)) << 32) | (((long) readInt4(src, srcIndex)) & 4294967295L)) / 10000) - 11644473600000L;
    }

    static void writeTime(long t, byte[] dst, int dstIndex) {
        if (t != 0) {
            t = (11644473600000L + t) * 10000;
        }
        writeInt8(t, dst, dstIndex);
    }

    static long readUTime(byte[] buffer, int bufferIndex) {
        return ((long) readInt4(buffer, bufferIndex)) * 1000;
    }

    static void writeUTime(long t, byte[] dst, int dstIndex) {
        if (t == 0 || t == -1) {
            writeInt4(-1, dst, dstIndex);
            return;
        }
        synchronized (TZ) {
            if (TZ.inDaylightTime(new Date())) {
                if (!TZ.inDaylightTime(new Date(t))) {
                    t -= 3600000;
                }
            } else if (TZ.inDaylightTime(new Date(t))) {
                t += 3600000;
            }
        }
        writeInt4((long) ((int) (t / 1000)), dst, dstIndex);
    }

    ServerMessageBlock() {
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        this.flags = 24;
        this.flags2 = 0;
        this.errorCode = 0;
        this.received = false;
        this.digest = null;
    }

    /* access modifiers changed from: package-private */
    public int writeString(String str, byte[] dst, int dstIndex) {
        return writeString(str, dst, dstIndex, this.useUnicode);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0051  */
    public int writeString(String str, byte[] dst, int dstIndex, boolean useUnicode2) {
        UnsupportedEncodingException uee;
        if (useUnicode2) {
            try {
                if ((dstIndex - this.headerStart) % 2 != 0) {
                    int dstIndex2 = dstIndex + 1;
                    try {
                        dst[dstIndex] = SMB_COM_CREATE_DIRECTORY;
                        dstIndex = dstIndex2;
                    } catch (UnsupportedEncodingException e) {
                        uee = e;
                        dstIndex = dstIndex2;
                        LogStream logStream = log;
                        if (LogStream.level > 1) {
                        }
                        return dstIndex - dstIndex;
                    }
                }
                System.arraycopy(str.getBytes(SmbConstants.UNI_ENCODING), 0, dst, dstIndex, str.length() * 2);
                int dstIndex3 = dstIndex + (str.length() * 2);
                int dstIndex4 = dstIndex3 + 1;
                dst[dstIndex3] = SMB_COM_CREATE_DIRECTORY;
                dstIndex = dstIndex4 + 1;
                dst[dstIndex4] = SMB_COM_CREATE_DIRECTORY;
            } catch (UnsupportedEncodingException e2) {
                uee = e2;
                LogStream logStream2 = log;
                if (LogStream.level > 1) {
                    uee.printStackTrace(log);
                }
                return dstIndex - dstIndex;
            }
        } else {
            byte[] b = str.getBytes(OEM_ENCODING);
            System.arraycopy(b, 0, dst, dstIndex, b.length);
            int dstIndex5 = dstIndex + b.length;
            dst[dstIndex5] = SMB_COM_CREATE_DIRECTORY;
            dstIndex = dstIndex5 + 1;
        }
        return dstIndex - dstIndex;
    }

    /* access modifiers changed from: package-private */
    public String readString(byte[] src, int srcIndex) {
        return readString(src, srcIndex, 256, this.useUnicode);
    }

    /* access modifiers changed from: package-private */
    public String readString(byte[] src, int srcIndex, int maxLen, boolean useUnicode2) {
        int i = 128;
        int len = 0;
        if (useUnicode2) {
            try {
                if ((srcIndex - this.headerStart) % 2 != 0) {
                    srcIndex++;
                }
                do {
                    if (src[srcIndex + len] == 0 && src[srcIndex + len + 1] == 0) {
                        return new String(src, srcIndex, len, SmbConstants.UNI_ENCODING);
                    }
                    len += 2;
                } while (len <= maxLen);
                LogStream logStream = log;
                if (LogStream.level > 0) {
                    PrintStream printStream = System.err;
                    if (maxLen < 128) {
                        i = maxLen + 8;
                    }
                    Hexdump.hexdump(printStream, src, srcIndex, i);
                }
                throw new RuntimeException("zero termination not found");
            } catch (UnsupportedEncodingException uee) {
                LogStream logStream2 = log;
                if (LogStream.level <= 1) {
                    return null;
                }
                uee.printStackTrace(log);
                return null;
            }
        } else {
            while (src[srcIndex + len] != 0) {
                len++;
                if (len > maxLen) {
                    LogStream logStream3 = log;
                    if (LogStream.level > 0) {
                        PrintStream printStream2 = System.err;
                        if (maxLen < 128) {
                            i = maxLen + 8;
                        }
                        Hexdump.hexdump(printStream2, src, srcIndex, i);
                    }
                    throw new RuntimeException("zero termination not found");
                }
            }
            return new String(src, srcIndex, len, OEM_ENCODING);
        }
    }

    /* access modifiers changed from: package-private */
    public String readString(byte[] src, int srcIndex, int srcEnd, int maxLen, boolean useUnicode2) {
        int i = 128;
        if (useUnicode2) {
            try {
                if ((srcIndex - this.headerStart) % 2 != 0) {
                    srcIndex++;
                }
                int len = 0;
                while (srcIndex + len + 1 < srcEnd && (src[srcIndex + len] != 0 || src[srcIndex + len + 1] != 0)) {
                    if (len > maxLen) {
                        LogStream logStream = log;
                        if (LogStream.level > 0) {
                            PrintStream printStream = System.err;
                            if (maxLen < 128) {
                                i = maxLen + 8;
                            }
                            Hexdump.hexdump(printStream, src, srcIndex, i);
                        }
                        throw new RuntimeException("zero termination not found");
                    }
                    len += 2;
                }
                return new String(src, srcIndex, len, SmbConstants.UNI_ENCODING);
            } catch (UnsupportedEncodingException uee) {
                LogStream logStream2 = log;
                if (LogStream.level <= 1) {
                    return null;
                }
                uee.printStackTrace(log);
                return null;
            }
        } else {
            int len2 = 0;
            while (srcIndex < srcEnd && src[srcIndex + len2] != 0) {
                if (len2 > maxLen) {
                    LogStream logStream3 = log;
                    if (LogStream.level > 0) {
                        PrintStream printStream2 = System.err;
                        if (maxLen < 128) {
                            i = maxLen + 8;
                        }
                        Hexdump.hexdump(printStream2, src, srcIndex, i);
                    }
                    throw new RuntimeException("zero termination not found");
                }
                len2++;
            }
            return new String(src, srcIndex, len2, OEM_ENCODING);
        }
    }

    /* access modifiers changed from: package-private */
    public int stringWireLength(String str, int offset) {
        int len = str.length() + 1;
        if (!this.useUnicode) {
            return len;
        }
        int len2 = (str.length() * 2) + 2;
        if (offset % 2 != 0) {
            return len2 + 1;
        }
        return len2;
    }

    /* access modifiers changed from: package-private */
    public int readStringLength(byte[] src, int srcIndex, int max) {
        int len = 0;
        while (src[srcIndex + len] != 0) {
            int len2 = len + 1;
            if (len > max) {
                throw new RuntimeException("zero termination not found: " + this);
            }
            len = len2;
        }
        return len;
    }

    /* access modifiers changed from: package-private */
    public int encode(byte[] dst, int dstIndex) {
        this.headerStart = dstIndex;
        int dstIndex2 = dstIndex + writeHeaderWireFormat(dst, dstIndex);
        this.wordCount = writeParameterWordsWireFormat(dst, dstIndex2 + 1);
        dst[dstIndex2] = (byte) ((this.wordCount / 2) & 255);
        int dstIndex3 = dstIndex2 + 1 + this.wordCount;
        this.wordCount /= 2;
        this.byteCount = writeBytesWireFormat(dst, dstIndex3 + 2);
        int dstIndex4 = dstIndex3 + 1;
        dst[dstIndex3] = (byte) (this.byteCount & 255);
        dst[dstIndex4] = (byte) ((this.byteCount >> 8) & 255);
        this.length = ((dstIndex4 + 1) + this.byteCount) - dstIndex;
        if (this.digest != null) {
            this.digest.sign(dst, this.headerStart, this.length, this, this.response);
        }
        return this.length;
    }

    /* access modifiers changed from: package-private */
    public int decode(byte[] buffer, int bufferIndex) {
        int bufferIndex2;
        this.headerStart = bufferIndex;
        int bufferIndex3 = bufferIndex + readHeaderWireFormat(buffer, bufferIndex);
        int bufferIndex4 = bufferIndex3 + 1;
        this.wordCount = buffer[bufferIndex3];
        if (this.wordCount != 0) {
            int n = readParameterWordsWireFormat(buffer, bufferIndex4);
            if (n != this.wordCount * 2) {
                LogStream logStream = log;
                if (LogStream.level >= 5) {
                    log.println("wordCount * 2=" + (this.wordCount * 2) + " but readParameterWordsWireFormat returned " + n);
                }
            }
            bufferIndex2 = bufferIndex4 + (this.wordCount * 2);
        } else {
            bufferIndex2 = bufferIndex4;
        }
        this.byteCount = readInt2(buffer, bufferIndex2);
        int bufferIndex5 = bufferIndex2 + 2;
        if (this.byteCount != 0) {
            int n2 = readBytesWireFormat(buffer, bufferIndex5);
            if (n2 != this.byteCount) {
                LogStream logStream2 = log;
                if (LogStream.level >= 5) {
                    log.println("byteCount=" + this.byteCount + " but readBytesWireFormat returned " + n2);
                }
            }
            bufferIndex5 += this.byteCount;
        }
        this.length = bufferIndex5 - bufferIndex;
        return this.length;
    }

    /* access modifiers changed from: package-private */
    public int writeHeaderWireFormat(byte[] dst, int dstIndex) {
        System.arraycopy(header, 0, dst, dstIndex, header.length);
        dst[dstIndex + 4] = this.command;
        dst[dstIndex + 9] = this.flags;
        writeInt2((long) this.flags2, dst, dstIndex + 9 + 1);
        int dstIndex2 = dstIndex + 24;
        writeInt2((long) this.tid, dst, dstIndex2);
        writeInt2((long) this.pid, dst, dstIndex2 + 2);
        writeInt2((long) this.uid, dst, dstIndex2 + 4);
        writeInt2((long) this.mid, dst, dstIndex2 + 6);
        return 32;
    }

    /* access modifiers changed from: package-private */
    public int readHeaderWireFormat(byte[] buffer, int bufferIndex) {
        this.command = buffer[bufferIndex + 4];
        this.errorCode = readInt4(buffer, bufferIndex + 5);
        this.flags = buffer[bufferIndex + 9];
        this.flags2 = readInt2(buffer, bufferIndex + 9 + 1);
        this.tid = readInt2(buffer, bufferIndex + 24);
        this.pid = readInt2(buffer, bufferIndex + 24 + 2);
        this.uid = readInt2(buffer, bufferIndex + 24 + 4);
        this.mid = readInt2(buffer, bufferIndex + 24 + 6);
        return 32;
    }

    /* access modifiers changed from: package-private */
    public boolean isResponse() {
        return (this.flags & 128) == 128;
    }

    public int hashCode() {
        return this.mid;
    }

    public boolean equals(Object obj) {
        return (obj instanceof ServerMessageBlock) && ((ServerMessageBlock) obj).mid == this.mid;
    }

    public String toString() {
        String c;
        String str;
        switch (this.command) {
            case -96:
                c = "SMB_COM_NT_TRANSACT";
                break;
            case -95:
                c = "SMB_COM_NT_TRANSACT_SECONDARY";
                break;
            case -94:
                c = "SMB_COM_NT_CREATE_ANDX";
                break;
            case 0:
                c = "SMB_COM_CREATE_DIRECTORY";
                break;
            case 1:
                c = "SMB_COM_DELETE_DIRECTORY";
                break;
            case 4:
                c = "SMB_COM_CLOSE";
                break;
            case 6:
                c = "SMB_COM_DELETE";
                break;
            case 7:
                c = "SMB_COM_RENAME";
                break;
            case 8:
                c = "SMB_COM_QUERY_INFORMATION";
                break;
            case 16:
                c = "SMB_COM_CHECK_DIRECTORY";
                break;
            case 37:
                c = "SMB_COM_TRANSACTION";
                break;
            case 38:
                c = "SMB_COM_TRANSACTION_SECONDARY";
                break;
            case 42:
                c = "SMB_COM_MOVE";
                break;
            case 43:
                c = "SMB_COM_ECHO";
                break;
            case 45:
                c = "SMB_COM_OPEN_ANDX";
                break;
            case 46:
                c = "SMB_COM_READ_ANDX";
                break;
            case 47:
                c = "SMB_COM_WRITE_ANDX";
                break;
            case 50:
                c = "SMB_COM_TRANSACTION2";
                break;
            case 52:
                c = "SMB_COM_FIND_CLOSE2";
                break;
            case 113:
                c = "SMB_COM_TREE_DISCONNECT";
                break;
            case 114:
                c = "SMB_COM_NEGOTIATE";
                break;
            case 115:
                c = "SMB_COM_SESSION_SETUP_ANDX";
                break;
            case 116:
                c = "SMB_COM_LOGOFF_ANDX";
                break;
            case 117:
                c = "SMB_COM_TREE_CONNECT_ANDX";
                break;
            default:
                c = "UNKNOWN";
                break;
        }
        if (this.errorCode == 0) {
            str = "0";
        } else {
            str = SmbException.getMessageByCode(this.errorCode);
        }
        return new String("command=" + c + ",received=" + this.received + ",errorCode=" + str + ",flags=0x" + Hexdump.toHexString(this.flags & 255, 4) + ",flags2=0x" + Hexdump.toHexString(this.flags2, 4) + ",signSeq=" + this.signSeq + ",tid=" + this.tid + ",pid=" + this.pid + ",uid=" + this.uid + ",mid=" + this.mid + ",wordCount=" + this.wordCount + ",byteCount=" + this.byteCount);
    }
}
