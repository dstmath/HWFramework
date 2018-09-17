package jcifs.smb;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.TimeZone;

interface SmbConstants {
    public static final int ATTR_ARCHIVE = 32;
    public static final int ATTR_COMPRESSED = 2048;
    public static final int ATTR_DIRECTORY = 16;
    public static final int ATTR_HIDDEN = 2;
    public static final int ATTR_NORMAL = 128;
    public static final int ATTR_READONLY = 1;
    public static final int ATTR_SYSTEM = 4;
    public static final int ATTR_TEMPORARY = 256;
    public static final int ATTR_VOLUME = 8;
    public static final int CAPABILITIES = 0;
    public static final int CAP_DFS = 4096;
    public static final int CAP_EXTENDED_SECURITY = Integer.MIN_VALUE;
    public static final int CAP_LARGE_FILES = 8;
    public static final int CAP_LEVEL_II_OPLOCKS = 128;
    public static final int CAP_LOCK_AND_READ = 256;
    public static final int CAP_MPX_MODE = 2;
    public static final int CAP_NONE = 0;
    public static final int CAP_NT_FIND = 512;
    public static final int CAP_NT_SMBS = 16;
    public static final int CAP_RAW_MODE = 1;
    public static final int CAP_RPC_REMOTE_APIS = 32;
    public static final int CAP_STATUS32 = 64;
    public static final int CAP_UNICODE = 4;
    public static final int CMD_OFFSET = 4;
    public static final LinkedList CONNECTIONS = null;
    public static final int CONN_TIMEOUT = 0;
    public static final int DEFAULT_CAPABILITIES = 0;
    public static final int DEFAULT_CONN_TIMEOUT = 35000;
    public static final int DEFAULT_FLAGS2 = 0;
    public static final int DEFAULT_MAX_MPX_COUNT = 10;
    public static final int DEFAULT_PORT = 445;
    public static final int DEFAULT_RCV_BUF_SIZE = 60416;
    public static final int DEFAULT_RESPONSE_TIMEOUT = 30000;
    public static final int DEFAULT_SND_BUF_SIZE = 16644;
    public static final int DEFAULT_SO_TIMEOUT = 35000;
    public static final int DEFAULT_SSN_LIMIT = 250;
    public static final int DELETE = 65536;
    public static final int ERROR_CODE_OFFSET = 5;
    public static final int FILE_APPEND_DATA = 4;
    public static final int FILE_DELETE = 64;
    public static final int FILE_EXECUTE = 32;
    public static final int FILE_READ_ATTRIBUTES = 128;
    public static final int FILE_READ_DATA = 1;
    public static final int FILE_READ_EA = 8;
    public static final int FILE_WRITE_ATTRIBUTES = 256;
    public static final int FILE_WRITE_DATA = 2;
    public static final int FILE_WRITE_EA = 16;
    public static final int FLAGS2 = 0;
    public static final int FLAGS2_EXTENDED_ATTRIBUTES = 2;
    public static final int FLAGS2_EXTENDED_SECURITY_NEGOTIATION = 2048;
    public static final int FLAGS2_LONG_FILENAMES = 1;
    public static final int FLAGS2_NONE = 0;
    public static final int FLAGS2_PERMIT_READ_IF_EXECUTE_PERM = 8192;
    public static final int FLAGS2_RESOLVE_PATHS_IN_DFS = 4096;
    public static final int FLAGS2_SECURITY_SIGNATURES = 4;
    public static final int FLAGS2_STATUS32 = 16384;
    public static final int FLAGS2_UNICODE = 32768;
    public static final int FLAGS_COPY_SOURCE_MODE_ASCII = 8;
    public static final int FLAGS_COPY_TARGET_MODE_ASCII = 4;
    public static final int FLAGS_LOCK_AND_READ_WRITE_AND_UNLOCK = 1;
    public static final int FLAGS_NONE = 0;
    public static final int FLAGS_NOTIFY_OF_MODIFY_ACTION = 64;
    public static final int FLAGS_OFFSET = 9;
    public static final int FLAGS_OPLOCK_REQUESTED_OR_GRANTED = 32;
    public static final int FLAGS_PATH_NAMES_CANONICALIZED = 16;
    public static final int FLAGS_PATH_NAMES_CASELESS = 8;
    public static final int FLAGS_RECEIVE_BUFFER_POSTED = 2;
    public static final int FLAGS_RESPONSE = 128;
    public static final int FLAGS_TARGET_MUST_BE_DIRECTORY = 2;
    public static final int FLAGS_TARGET_MUST_BE_FILE = 1;
    public static final int FLAGS_TREE_COPY = 32;
    public static final int FLAGS_VERIFY_ALL_WRITES = 16;
    public static final boolean FORCE_UNICODE = false;
    public static final int GENERIC_ALL = 268435456;
    public static final int GENERIC_EXECUTE = 536870912;
    public static final int GENERIC_READ = Integer.MIN_VALUE;
    public static final int GENERIC_WRITE = 1073741824;
    public static final int HEADER_LENGTH = 32;
    public static final InetAddress LADDR = null;
    public static final int LM_COMPATIBILITY = 0;
    public static final int LPORT = 0;
    public static final int MAX_MPX_COUNT = 0;
    public static final long MILLISECONDS_BETWEEN_1970_AND_1601 = 11644473600000L;
    public static final String NATIVE_LANMAN = null;
    public static final String NATIVE_OS = null;
    public static final String NETBIOS_HOSTNAME = null;
    public static final SmbTransport NULL_TRANSPORT = null;
    public static final String OEM_ENCODING = null;
    public static final int OPEN_FUNCTION_FAIL_IF_EXISTS = 0;
    public static final int OPEN_FUNCTION_OVERWRITE_IF_EXISTS = 32;
    public static final int PID = 0;
    public static final int RCV_BUF_SIZE = 0;
    public static final int READ_CONTROL = 131072;
    public static final int RESPONSE_TIMEOUT = 0;
    public static final int SECURITY_SHARE = 0;
    public static final int SECURITY_USER = 1;
    public static final int SIGNATURE_OFFSET = 14;
    public static final boolean SIGNPREF = false;
    public static final int SND_BUF_SIZE = 0;
    public static final int SO_TIMEOUT = 0;
    public static final int SSN_LIMIT = 0;
    public static final int SYNCHRONIZE = 1048576;
    public static final boolean TCP_NODELAY = false;
    public static final int TID_OFFSET = 24;
    public static final TimeZone TZ = null;
    public static final String UNI_ENCODING = "UTF-16LE";
    public static final boolean USE_BATCHING = false;
    public static final boolean USE_EXTSEC = false;
    public static final boolean USE_NTSMBS = false;
    public static final boolean USE_NTSTATUS = false;
    public static final boolean USE_UNICODE = false;
    public static final int VC_NUMBER = 1;
    public static final int WRITE_DAC = 262144;
    public static final int WRITE_OWNER = 524288;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.smb.SmbConstants.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.smb.SmbConstants.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: jcifs.smb.SmbConstants.<clinit>():void");
    }
}
