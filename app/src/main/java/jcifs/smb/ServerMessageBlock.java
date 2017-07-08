package jcifs.smb;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import jcifs.util.Encdec;
import jcifs.util.Hexdump;
import jcifs.util.LogStream;
import jcifs.util.transport.Request;
import jcifs.util.transport.Response;

abstract class ServerMessageBlock extends Response implements Request, SmbConstants {
    static final byte SMB_COM_CHECK_DIRECTORY = (byte) 16;
    static final byte SMB_COM_CLOSE = (byte) 4;
    static final byte SMB_COM_CREATE_DIRECTORY = (byte) 0;
    static final byte SMB_COM_DELETE = (byte) 6;
    static final byte SMB_COM_DELETE_DIRECTORY = (byte) 1;
    static final byte SMB_COM_ECHO = (byte) 43;
    static final byte SMB_COM_FIND_CLOSE2 = (byte) 52;
    static final byte SMB_COM_LOGOFF_ANDX = (byte) 116;
    static final byte SMB_COM_MOVE = (byte) 42;
    static final byte SMB_COM_NEGOTIATE = (byte) 114;
    static final byte SMB_COM_NT_CREATE_ANDX = (byte) -94;
    static final byte SMB_COM_NT_TRANSACT = (byte) -96;
    static final byte SMB_COM_NT_TRANSACT_SECONDARY = (byte) -95;
    static final byte SMB_COM_OPEN_ANDX = (byte) 45;
    static final byte SMB_COM_QUERY_INFORMATION = (byte) 8;
    static final byte SMB_COM_READ_ANDX = (byte) 46;
    static final byte SMB_COM_RENAME = (byte) 7;
    static final byte SMB_COM_SESSION_SETUP_ANDX = (byte) 115;
    static final byte SMB_COM_TRANSACTION = (byte) 37;
    static final byte SMB_COM_TRANSACTION2 = (byte) 50;
    static final byte SMB_COM_TRANSACTION_SECONDARY = (byte) 38;
    static final byte SMB_COM_TREE_CONNECT_ANDX = (byte) 117;
    static final byte SMB_COM_TREE_DISCONNECT = (byte) 113;
    static final byte SMB_COM_WRITE = (byte) 11;
    static final byte SMB_COM_WRITE_ANDX = (byte) 47;
    static final byte[] header = null;
    static LogStream log;
    NtlmPasswordAuthentication auth;
    int batchLevel;
    int byteCount;
    byte command;
    SigningDigest digest;
    int errorCode;
    boolean extendedSecurity;
    byte flags;
    int flags2;
    int headerStart;
    int length;
    int mid;
    String path;
    int pid;
    boolean received;
    ServerMessageBlock response;
    long responseTimeout;
    int signSeq;
    int tid;
    int uid;
    boolean useUnicode;
    boolean verifyFailed;
    int wordCount;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.smb.ServerMessageBlock.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.smb.ServerMessageBlock.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: jcifs.smb.ServerMessageBlock.<clinit>():void");
    }

    abstract int readBytesWireFormat(byte[] bArr, int i);

    abstract int readParameterWordsWireFormat(byte[] bArr, int i);

    abstract int writeBytesWireFormat(byte[] bArr, int i);

    abstract int writeParameterWordsWireFormat(byte[] bArr, int i);

    static void writeInt2(long val, byte[] dst, int dstIndex) {
        dst[dstIndex] = (byte) ((int) val);
        dst[dstIndex + 1] = (byte) ((int) (val >> 8));
    }

    static void writeInt4(long val, byte[] dst, int dstIndex) {
        dst[dstIndex] = (byte) ((int) val);
        dstIndex++;
        val >>= 8;
        dst[dstIndex] = (byte) ((int) val);
        dstIndex++;
        val >>= 8;
        dst[dstIndex] = (byte) ((int) val);
        dst[dstIndex + 1] = (byte) ((int) (val >> 8));
    }

    static int readInt2(byte[] src, int srcIndex) {
        return (src[srcIndex] & 255) + ((src[srcIndex + 1] & 255) << 8);
    }

    static int readInt4(byte[] src, int srcIndex) {
        return (((src[srcIndex] & 255) + ((src[srcIndex + 1] & 255) << 8)) + ((src[srcIndex + 2] & 255) << 16)) + ((src[srcIndex + 3] & 255) << 24);
    }

    static long readInt8(byte[] src, int srcIndex) {
        return (((long) readInt4(src, srcIndex)) & 4294967295L) + (((long) readInt4(src, srcIndex + 4)) << 32);
    }

    static void writeInt8(long val, byte[] dst, int dstIndex) {
        dst[dstIndex] = (byte) ((int) val);
        dstIndex++;
        val >>= 8;
        dst[dstIndex] = (byte) ((int) val);
        dstIndex++;
        val >>= 8;
        dst[dstIndex] = (byte) ((int) val);
        dstIndex++;
        val >>= 8;
        dst[dstIndex] = (byte) ((int) val);
        dstIndex++;
        val >>= 8;
        dst[dstIndex] = (byte) ((int) val);
        dstIndex++;
        val >>= 8;
        dst[dstIndex] = (byte) ((int) val);
        dstIndex++;
        val >>= 8;
        dst[dstIndex] = (byte) ((int) val);
        dst[dstIndex + 1] = (byte) ((int) (val >> 8));
    }

    static long readTime(byte[] src, int srcIndex) {
        return (((((long) readInt4(src, srcIndex + 4)) << 32) | (((long) readInt4(src, srcIndex)) & 4294967295L)) / 10000) - Encdec.MILLISECONDS_BETWEEN_1970_AND_1601;
    }

    static void writeTime(long t, byte[] dst, int dstIndex) {
        if (t != 0) {
            t = (Encdec.MILLISECONDS_BETWEEN_1970_AND_1601 + t) * 10000;
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
        this.responseTimeout = 1;
        this.auth = null;
        this.digest = null;
        this.flags = (byte) 24;
        this.pid = PID;
        this.batchLevel = 0;
    }

    void reset() {
        this.flags = (byte) 24;
        this.flags2 = 0;
        this.errorCode = 0;
        this.received = false;
        this.digest = null;
    }

    int writeString(String str, byte[] dst, int dstIndex) {
        return writeString(str, dst, dstIndex, this.useUnicode);
    }

    int writeString(String str, byte[] dst, int dstIndex, boolean useUnicode) {
        int dstIndex2;
        UnsupportedEncodingException uee;
        int start = dstIndex;
        if (useUnicode) {
            try {
                if ((dstIndex - this.headerStart) % 2 != 0) {
                    dstIndex2 = dstIndex + 1;
                    try {
                        dst[dstIndex] = SMB_COM_CREATE_DIRECTORY;
                        dstIndex = dstIndex2;
                    } catch (UnsupportedEncodingException e) {
                        uee = e;
                        dstIndex = dstIndex2;
                        LogStream logStream = log;
                        if (LogStream.level > 1) {
                            uee.printStackTrace(log);
                        }
                        return dstIndex - start;
                    }
                }
                System.arraycopy(str.getBytes(SmbConstants.UNI_ENCODING), 0, dst, dstIndex, str.length() * 2);
                dstIndex += str.length() * 2;
                dstIndex2 = dstIndex + 1;
                dst[dstIndex] = SMB_COM_CREATE_DIRECTORY;
                dstIndex = dstIndex2 + 1;
                dst[dstIndex2] = SMB_COM_CREATE_DIRECTORY;
            } catch (UnsupportedEncodingException e2) {
                uee = e2;
                LogStream logStream2 = log;
                if (LogStream.level > 1) {
                    uee.printStackTrace(log);
                }
                return dstIndex - start;
            }
        }
        byte[] b = str.getBytes(OEM_ENCODING);
        System.arraycopy(b, 0, dst, dstIndex, b.length);
        dstIndex += b.length;
        dstIndex2 = dstIndex + 1;
        dst[dstIndex] = SMB_COM_CREATE_DIRECTORY;
        dstIndex = dstIndex2;
        return dstIndex - start;
    }

    String readString(byte[] src, int srcIndex) {
        return readString(src, srcIndex, SmbNamedPipe.PIPE_TYPE_CALL, this.useUnicode);
    }

    String readString(byte[] src, int srcIndex, int maxLen, boolean useUnicode) {
        int i = SmbConstants.FLAGS_RESPONSE;
        int len = 0;
        if (useUnicode) {
            try {
                if ((srcIndex - this.headerStart) % 2 != 0) {
                    srcIndex++;
                }
                do {
                    if (src[srcIndex + len] == null && src[(srcIndex + len) + 1] == null) {
                        return new String(src, srcIndex, len, SmbConstants.UNI_ENCODING);
                    }
                    len += 2;
                } while (len <= maxLen);
                LogStream logStream = log;
                if (LogStream.level > 0) {
                    PrintStream printStream = System.err;
                    if (maxLen < SmbConstants.FLAGS_RESPONSE) {
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
        }
        while (src[srcIndex + len] != null) {
            len++;
            if (len > maxLen) {
                logStream = log;
                if (LogStream.level > 0) {
                    printStream = System.err;
                    if (maxLen < SmbConstants.FLAGS_RESPONSE) {
                        i = maxLen + 8;
                    }
                    Hexdump.hexdump(printStream, src, srcIndex, i);
                }
                throw new RuntimeException("zero termination not found");
            }
        }
        return new String(src, srcIndex, len, OEM_ENCODING);
    }

    String readString(byte[] src, int srcIndex, int srcEnd, int maxLen, boolean useUnicode) {
        int i = SmbConstants.FLAGS_RESPONSE;
        int len;
        if (useUnicode) {
            try {
                if ((srcIndex - this.headerStart) % 2 != 0) {
                    srcIndex++;
                }
                len = 0;
                while ((srcIndex + len) + 1 < srcEnd && (src[srcIndex + len] != null || src[(srcIndex + len) + 1] != null)) {
                    if (len > maxLen) {
                        LogStream logStream = log;
                        if (LogStream.level > 0) {
                            PrintStream printStream = System.err;
                            if (maxLen < SmbConstants.FLAGS_RESPONSE) {
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
        }
        len = 0;
        while (srcIndex < srcEnd) {
            if (src[srcIndex + len] == null) {
                break;
            } else if (len > maxLen) {
                logStream = log;
                if (LogStream.level > 0) {
                    printStream = System.err;
                    if (maxLen < SmbConstants.FLAGS_RESPONSE) {
                        i = maxLen + 8;
                    }
                    Hexdump.hexdump(printStream, src, srcIndex, i);
                }
                throw new RuntimeException("zero termination not found");
            } else {
                len++;
            }
        }
        return new String(src, srcIndex, len, OEM_ENCODING);
    }

    int stringWireLength(String str, int offset) {
        int len = str.length() + 1;
        if (!this.useUnicode) {
            return len;
        }
        len = (str.length() * 2) + 2;
        if (offset % 2 != 0) {
            return len + 1;
        }
        return len;
    }

    int readStringLength(byte[] src, int srcIndex, int max) {
        int len = 0;
        while (src[srcIndex + len] != null) {
            int len2 = len + 1;
            if (len > max) {
                throw new RuntimeException("zero termination not found: " + this);
            }
            len = len2;
        }
        return len;
    }

    int encode(byte[] dst, int dstIndex) {
        this.headerStart = dstIndex;
        int start = dstIndex;
        dstIndex += writeHeaderWireFormat(dst, dstIndex);
        this.wordCount = writeParameterWordsWireFormat(dst, dstIndex + 1);
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) ((this.wordCount / 2) & 255);
        dstIndex = dstIndex2 + this.wordCount;
        this.wordCount /= 2;
        this.byteCount = writeBytesWireFormat(dst, dstIndex + 2);
        dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) (this.byteCount & 255);
        dstIndex = dstIndex2 + 1;
        dst[dstIndex2] = (byte) ((this.byteCount >> 8) & 255);
        this.length = (dstIndex + this.byteCount) - start;
        if (this.digest != null) {
            this.digest.sign(dst, this.headerStart, this.length, this, this.response);
        }
        return this.length;
    }

    int decode(byte[] buffer, int bufferIndex) {
        int n;
        this.headerStart = bufferIndex;
        int start = bufferIndex;
        bufferIndex += readHeaderWireFormat(buffer, bufferIndex);
        int bufferIndex2 = bufferIndex + 1;
        this.wordCount = buffer[bufferIndex];
        if (this.wordCount != 0) {
            n = readParameterWordsWireFormat(buffer, bufferIndex2);
            if (n != this.wordCount * 2) {
                LogStream logStream = log;
                if (LogStream.level >= 5) {
                    log.println("wordCount * 2=" + (this.wordCount * 2) + " but readParameterWordsWireFormat returned " + n);
                }
            }
            bufferIndex = bufferIndex2 + (this.wordCount * 2);
        } else {
            bufferIndex = bufferIndex2;
        }
        this.byteCount = readInt2(buffer, bufferIndex);
        bufferIndex += 2;
        if (this.byteCount != 0) {
            n = readBytesWireFormat(buffer, bufferIndex);
            if (n != this.byteCount) {
                logStream = log;
                if (LogStream.level >= 5) {
                    log.println("byteCount=" + this.byteCount + " but readBytesWireFormat returned " + n);
                }
            }
            bufferIndex += this.byteCount;
        }
        this.length = bufferIndex - start;
        return this.length;
    }

    int writeHeaderWireFormat(byte[] dst, int dstIndex) {
        System.arraycopy(header, 0, dst, dstIndex, header.length);
        dst[dstIndex + 4] = this.command;
        dst[dstIndex + 9] = this.flags;
        writeInt2((long) this.flags2, dst, (dstIndex + 9) + 1);
        dstIndex += 24;
        writeInt2((long) this.tid, dst, dstIndex);
        writeInt2((long) this.pid, dst, dstIndex + 2);
        writeInt2((long) this.uid, dst, dstIndex + 4);
        writeInt2((long) this.mid, dst, dstIndex + 6);
        return 32;
    }

    int readHeaderWireFormat(byte[] buffer, int bufferIndex) {
        this.command = buffer[bufferIndex + 4];
        this.errorCode = readInt4(buffer, bufferIndex + 5);
        this.flags = buffer[bufferIndex + 9];
        this.flags2 = readInt2(buffer, (bufferIndex + 9) + 1);
        this.tid = readInt2(buffer, bufferIndex + 24);
        this.pid = readInt2(buffer, (bufferIndex + 24) + 2);
        this.uid = readInt2(buffer, (bufferIndex + 24) + 4);
        this.mid = readInt2(buffer, (bufferIndex + 24) + 6);
        return 32;
    }

    boolean isResponse() {
        return (this.flags & SmbConstants.FLAGS_RESPONSE) == SmbConstants.FLAGS_RESPONSE;
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
            case (byte) -96:
                c = "SMB_COM_NT_TRANSACT";
                break;
            case (byte) -95:
                c = "SMB_COM_NT_TRANSACT_SECONDARY";
                break;
            case (byte) -94:
                c = "SMB_COM_NT_CREATE_ANDX";
                break;
            case SmbFile.FILE_NO_SHARE /*0*/:
                c = "SMB_COM_CREATE_DIRECTORY";
                break;
            case Encdec.TIME_1970_SEC_32BE /*1*/:
                c = "SMB_COM_DELETE_DIRECTORY";
                break;
            case Encdec.TIME_1904_SEC_32LE /*4*/:
                c = "SMB_COM_CLOSE";
                break;
            case Encdec.TIME_1601_NANOS_64BE /*6*/:
                c = "SMB_COM_DELETE";
                break;
            case Encdec.TIME_1970_MILLIS_64BE /*7*/:
                c = "SMB_COM_RENAME";
                break;
            case Encdec.TIME_1970_MILLIS_64LE /*8*/:
                c = "SMB_COM_QUERY_INFORMATION";
                break;
            case SmbFile.TYPE_NAMED_PIPE /*16*/:
                c = "SMB_COM_CHECK_DIRECTORY";
                break;
            case (byte) 37:
                c = "SMB_COM_TRANSACTION";
                break;
            case (byte) 38:
                c = "SMB_COM_TRANSACTION_SECONDARY";
                break;
            case (byte) 42:
                c = "SMB_COM_MOVE";
                break;
            case (byte) 43:
                c = "SMB_COM_ECHO";
                break;
            case (byte) 45:
                c = "SMB_COM_OPEN_ANDX";
                break;
            case (byte) 46:
                c = "SMB_COM_READ_ANDX";
                break;
            case (byte) 47:
                c = "SMB_COM_WRITE_ANDX";
                break;
            case (byte) 50:
                c = "SMB_COM_TRANSACTION2";
                break;
            case (byte) 52:
                c = "SMB_COM_FIND_CLOSE2";
                break;
            case (byte) 113:
                c = "SMB_COM_TREE_DISCONNECT";
                break;
            case (byte) 114:
                c = "SMB_COM_NEGOTIATE";
                break;
            case (byte) 115:
                c = "SMB_COM_SESSION_SETUP_ANDX";
                break;
            case (byte) 116:
                c = "SMB_COM_LOGOFF_ANDX";
                break;
            case (byte) 117:
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
