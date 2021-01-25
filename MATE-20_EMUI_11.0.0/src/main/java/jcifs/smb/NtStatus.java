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
    public static final int[] NT_STATUS_CODES = {0, NT_STATUS_UNSUCCESSFUL, NT_STATUS_NOT_IMPLEMENTED, NT_STATUS_INVALID_INFO_CLASS, NT_STATUS_ACCESS_VIOLATION, NT_STATUS_INVALID_HANDLE, NT_STATUS_INVALID_PARAMETER, NT_STATUS_NO_SUCH_DEVICE, NT_STATUS_NO_SUCH_FILE, NT_STATUS_MORE_PROCESSING_REQUIRED, NT_STATUS_ACCESS_DENIED, NT_STATUS_BUFFER_TOO_SMALL, NT_STATUS_OBJECT_NAME_INVALID, NT_STATUS_OBJECT_NAME_NOT_FOUND, NT_STATUS_OBJECT_NAME_COLLISION, NT_STATUS_PORT_DISCONNECTED, NT_STATUS_OBJECT_PATH_INVALID, NT_STATUS_OBJECT_PATH_NOT_FOUND, NT_STATUS_OBJECT_PATH_SYNTAX_BAD, NT_STATUS_SHARING_VIOLATION, NT_STATUS_DELETE_PENDING, NT_STATUS_NO_LOGON_SERVERS, NT_STATUS_USER_EXISTS, NT_STATUS_NO_SUCH_USER, NT_STATUS_WRONG_PASSWORD, NT_STATUS_LOGON_FAILURE, NT_STATUS_ACCOUNT_RESTRICTION, NT_STATUS_INVALID_LOGON_HOURS, NT_STATUS_INVALID_WORKSTATION, NT_STATUS_PASSWORD_EXPIRED, NT_STATUS_ACCOUNT_DISABLED, NT_STATUS_NONE_MAPPED, NT_STATUS_INVALID_SID, NT_STATUS_INSTANCE_NOT_AVAILABLE, NT_STATUS_PIPE_NOT_AVAILABLE, NT_STATUS_INVALID_PIPE_STATE, NT_STATUS_PIPE_BUSY, NT_STATUS_PIPE_DISCONNECTED, NT_STATUS_PIPE_CLOSING, NT_STATUS_PIPE_LISTENING, NT_STATUS_FILE_IS_A_DIRECTORY, NT_STATUS_DUPLICATE_NAME, NT_STATUS_NETWORK_NAME_DELETED, NT_STATUS_NETWORK_ACCESS_DENIED, NT_STATUS_BAD_NETWORK_NAME, NT_STATUS_REQUEST_NOT_ACCEPTED, NT_STATUS_CANT_ACCESS_DOMAIN_INFO, NT_STATUS_NO_SUCH_DOMAIN, NT_STATUS_NOT_A_DIRECTORY, NT_STATUS_CANNOT_DELETE, NT_STATUS_INVALID_COMPUTER_NAME, NT_STATUS_PIPE_BROKEN, NT_STATUS_NO_SUCH_ALIAS, NT_STATUS_LOGON_TYPE_NOT_GRANTED, NT_STATUS_NO_TRUST_SAM_ACCOUNT, NT_STATUS_TRUSTED_DOMAIN_FAILURE, NT_STATUS_NOLOGON_WORKSTATION_TRUST_ACCOUNT, NT_STATUS_PASSWORD_MUST_CHANGE, NT_STATUS_NOT_FOUND, NT_STATUS_ACCOUNT_LOCKED_OUT, NT_STATUS_PATH_NOT_COVERED, NT_STATUS_IO_REPARSE_TAG_NOT_HANDLED};
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
    public static final String[] NT_STATUS_MESSAGES = {"The operation completed successfully.", "A device attached to the system is not functioning.", "Incorrect function.", "The parameter is incorrect.", "Invalid access to memory location.", "The handle is invalid.", "The parameter is incorrect.", "The system cannot find the file specified.", "The system cannot find the file specified.", "More data is available.", "Access is denied.", "The data area passed to a system call is too small.", "The filename, directory name, or volume label syntax is incorrect.", "The system cannot find the file specified.", "Cannot create a file when that file already exists.", "The handle is invalid.", "The specified path is invalid.", "The system cannot find the path specified.", "The specified path is invalid.", "The process cannot access the file because it is being used by another process.", "Access is denied.", "There are currently no logon servers available to service the logon request.", "The specified user already exists.", "The specified user does not exist.", "The specified network password is not correct.", "Logon failure: unknown user name or bad password.", "Logon failure: user account restriction.", "Logon failure: account logon time restriction violation.", "Logon failure: user not allowed to log on to this computer.", "Logon failure: the specified account password has expired.", "Logon failure: account currently disabled.", "No mapping between account names and security IDs was done.", "The security ID structure is invalid.", "All pipe instances are busy.", "All pipe instances are busy.", "The pipe state is invalid.", "All pipe instances are busy.", "No process is on the other end of the pipe.", "The pipe is being closed.", "Waiting for a process to open the other end of the pipe.", "Access is denied.", "A duplicate name exists on the network.", "The specified network name is no longer available.", "Network access is denied.", "The network name cannot be found.", "No more connections can be made to this remote computer at this time because there are already as many connections as the computer can accept.", "Indicates a Windows NT Server could not be contacted or that objects within the domain are protected such that necessary information could not be retrieved.", "The specified domain did not exist.", "The directory name is invalid.", "Access is denied.", "The format of the specified computer name is invalid.", "The pipe has been ended.", "The specified local group does not exist.", "Logon failure: the user has not been granted the requested logon type at this computer.", "The SAM database on the Windows NT Server does not have a computer account for this workstation trust relationship.", "The trust relationship between the primary domain and the trusted domain failed.", "The account used is a Computer Account. Use your global user account or local user account to access this server.", "The user must change his password before he logs on the first time.", "NT_STATUS_NOT_FOUND", "The referenced account is currently locked out and may not be logged on to.", "The remote system is not reachable by the transport.", "NT_STATUS_IO_REPARSE_TAG_NOT_HANDLED"};
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
}
