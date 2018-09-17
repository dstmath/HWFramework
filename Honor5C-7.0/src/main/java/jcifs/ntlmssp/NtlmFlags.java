package jcifs.ntlmssp;

public interface NtlmFlags {
    public static final int NTLMSSP_NEGOTIATE_128 = 536870912;
    public static final int NTLMSSP_NEGOTIATE_56 = Integer.MIN_VALUE;
    public static final int NTLMSSP_NEGOTIATE_ALWAYS_SIGN = 32768;
    public static final int NTLMSSP_NEGOTIATE_DATAGRAM_STYLE = 64;
    public static final int NTLMSSP_NEGOTIATE_KEY_EXCH = 1073741824;
    public static final int NTLMSSP_NEGOTIATE_LM_KEY = 128;
    public static final int NTLMSSP_NEGOTIATE_LOCAL_CALL = 16384;
    public static final int NTLMSSP_NEGOTIATE_NETWARE = 256;
    public static final int NTLMSSP_NEGOTIATE_NTLM = 512;
    public static final int NTLMSSP_NEGOTIATE_NTLM2 = 524288;
    public static final int NTLMSSP_NEGOTIATE_OEM = 2;
    public static final int NTLMSSP_NEGOTIATE_OEM_DOMAIN_SUPPLIED = 4096;
    public static final int NTLMSSP_NEGOTIATE_OEM_WORKSTATION_SUPPLIED = 8192;
    public static final int NTLMSSP_NEGOTIATE_SEAL = 32;
    public static final int NTLMSSP_NEGOTIATE_SIGN = 16;
    public static final int NTLMSSP_NEGOTIATE_TARGET_INFO = 8388608;
    public static final int NTLMSSP_NEGOTIATE_UNICODE = 1;
    public static final int NTLMSSP_REQUEST_ACCEPT_RESPONSE = 2097152;
    public static final int NTLMSSP_REQUEST_INIT_RESPONSE = 1048576;
    public static final int NTLMSSP_REQUEST_NON_NT_SESSION_KEY = 4194304;
    public static final int NTLMSSP_REQUEST_TARGET = 4;
    public static final int NTLMSSP_TARGET_TYPE_DOMAIN = 65536;
    public static final int NTLMSSP_TARGET_TYPE_SERVER = 131072;
    public static final int NTLMSSP_TARGET_TYPE_SHARE = 262144;
}
