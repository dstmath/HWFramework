package jcifs.smb;

public interface NtStatus {
    public static final int NT_STATUS_ACCESS_DENIED = -1073741790;
    public static final int NT_STATUS_ACCESS_VIOLATION = -1073741819;
    public static final int NT_STATUS_ACCOUNT_DISABLED = -1073741710;
    public static final int NT_STATUS_ACCOUNT_LOCKED_OUT = -1073741260;
    public static final int NT_STATUS_ACCOUNT_RESTRICTION = -1073741714;
    public static final int NT_STATUS_BAD_NETWORK_NAME = -1073741620;
    public static final int NT_STATUS_BUFFER_TOO_SMALL = -1073741789;
    public static final int NT_STATUS_CANNOT_DELETE = -1073741535;
    public static final int NT_STATUS_CANT_ACCESS_DOMAIN_INFO = -1073741606;
    public static final int[] NT_STATUS_CODES = null;
    public static final int NT_STATUS_DELETE_PENDING = -1073741738;
    public static final int NT_STATUS_DUPLICATE_NAME = -1073741635;
    public static final int NT_STATUS_FILE_IS_A_DIRECTORY = -1073741638;
    public static final int NT_STATUS_INSTANCE_NOT_AVAILABLE = -1073741653;
    public static final int NT_STATUS_INVALID_COMPUTER_NAME = -1073741534;
    public static final int NT_STATUS_INVALID_HANDLE = -1073741816;
    public static final int NT_STATUS_INVALID_INFO_CLASS = -1073741821;
    public static final int NT_STATUS_INVALID_LOGON_HOURS = -1073741713;
    public static final int NT_STATUS_INVALID_PARAMETER = -1073741811;
    public static final int NT_STATUS_INVALID_PIPE_STATE = -1073741651;
    public static final int NT_STATUS_INVALID_SID = -1073741704;
    public static final int NT_STATUS_INVALID_WORKSTATION = -1073741712;
    public static final int NT_STATUS_IO_REPARSE_TAG_NOT_HANDLED = -1073741191;
    public static final int NT_STATUS_LOGON_FAILURE = -1073741715;
    public static final int NT_STATUS_LOGON_TYPE_NOT_GRANTED = -1073741477;
    public static final String[] NT_STATUS_MESSAGES = null;
    public static final int NT_STATUS_MORE_PROCESSING_REQUIRED = -1073741802;
    public static final int NT_STATUS_NETWORK_ACCESS_DENIED = -1073741622;
    public static final int NT_STATUS_NETWORK_NAME_DELETED = -1073741623;
    public static final int NT_STATUS_NOLOGON_WORKSTATION_TRUST_ACCOUNT = -1073741415;
    public static final int NT_STATUS_NONE_MAPPED = -1073741709;
    public static final int NT_STATUS_NOT_A_DIRECTORY = -1073741565;
    public static final int NT_STATUS_NOT_FOUND = -1073741275;
    public static final int NT_STATUS_NOT_IMPLEMENTED = -1073741822;
    public static final int NT_STATUS_NO_LOGON_SERVERS = -1073741730;
    public static final int NT_STATUS_NO_SUCH_ALIAS = -1073741487;
    public static final int NT_STATUS_NO_SUCH_DEVICE = -1073741810;
    public static final int NT_STATUS_NO_SUCH_DOMAIN = -1073741601;
    public static final int NT_STATUS_NO_SUCH_FILE = -1073741809;
    public static final int NT_STATUS_NO_SUCH_USER = -1073741724;
    public static final int NT_STATUS_NO_TRUST_SAM_ACCOUNT = -1073741429;
    public static final int NT_STATUS_OBJECT_NAME_COLLISION = -1073741771;
    public static final int NT_STATUS_OBJECT_NAME_INVALID = -1073741773;
    public static final int NT_STATUS_OBJECT_NAME_NOT_FOUND = -1073741772;
    public static final int NT_STATUS_OBJECT_PATH_INVALID = -1073741767;
    public static final int NT_STATUS_OBJECT_PATH_NOT_FOUND = -1073741766;
    public static final int NT_STATUS_OBJECT_PATH_SYNTAX_BAD = -1073741765;
    public static final int NT_STATUS_OK = 0;
    public static final int NT_STATUS_PASSWORD_EXPIRED = -1073741711;
    public static final int NT_STATUS_PASSWORD_MUST_CHANGE = -1073741276;
    public static final int NT_STATUS_PATH_NOT_COVERED = -1073741225;
    public static final int NT_STATUS_PIPE_BROKEN = -1073741493;
    public static final int NT_STATUS_PIPE_BUSY = -1073741650;
    public static final int NT_STATUS_PIPE_CLOSING = -1073741647;
    public static final int NT_STATUS_PIPE_DISCONNECTED = -1073741648;
    public static final int NT_STATUS_PIPE_LISTENING = -1073741645;
    public static final int NT_STATUS_PIPE_NOT_AVAILABLE = -1073741652;
    public static final int NT_STATUS_PORT_DISCONNECTED = -1073741769;
    public static final int NT_STATUS_REQUEST_NOT_ACCEPTED = -1073741616;
    public static final int NT_STATUS_SHARING_VIOLATION = -1073741757;
    public static final int NT_STATUS_TRUSTED_DOMAIN_FAILURE = -1073741428;
    public static final int NT_STATUS_UNSUCCESSFUL = -1073741823;
    public static final int NT_STATUS_USER_EXISTS = -1073741725;
    public static final int NT_STATUS_WRONG_PASSWORD = -1073741718;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.smb.NtStatus.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.smb.NtStatus.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: jcifs.smb.NtStatus.<clinit>():void");
    }
}
