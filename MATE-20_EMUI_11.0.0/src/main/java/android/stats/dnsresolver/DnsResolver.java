package android.stats.dnsresolver;

public final class DnsResolver {
    public static final int CS_FOUND = 2;
    public static final int CS_NOTFOUND = 1;
    public static final int CS_SKIP = 3;
    public static final int CS_UNSUPPORTED = 0;
    public static final int EVENT_GETADDRINFO = 1;
    public static final int EVENT_GETHOSTBYADDR = 3;
    public static final int EVENT_GETHOSTBYNAME = 2;
    public static final int EVENT_RES_NSEND = 4;
    public static final int EVENT_UNKNOWN = 0;
    public static final int IV_IPV4 = 1;
    public static final int IV_IPV6 = 2;
    public static final int IV_UNKNOWN = 0;
    public static final int NS_R_BADKEY = 17;
    public static final int NS_R_BADTIME = 18;
    public static final int NS_R_BADVERS = 16;
    public static final int NS_R_FORMERR = 1;
    public static final int NS_R_MAX = 11;
    public static final int NS_R_NOTAUTH = 9;
    public static final int NS_R_NOTIMPL = 4;
    public static final int NS_R_NOTZONE = 10;
    public static final int NS_R_NO_ERROR = 0;
    public static final int NS_R_NXDOMAIN = 3;
    public static final int NS_R_NXRRSET = 8;
    public static final int NS_R_REFUSED = 5;
    public static final int NS_R_SERVFAIL = 2;
    public static final int NS_R_YXDOMAIN = 6;
    public static final int NS_R_YXRRSET = 7;
    public static final int NS_T_A = 1;
    public static final int NS_T_A6 = 38;
    public static final int NS_T_AAAA = 28;
    public static final int NS_T_AFSDB = 18;
    public static final int NS_T_ANY = 255;
    public static final int NS_T_APL = 42;
    public static final int NS_T_ATMA = 34;
    public static final int NS_T_AXFR = 252;
    public static final int NS_T_CERT = 37;
    public static final int NS_T_CNAME = 5;
    public static final int NS_T_DHCID = 49;
    public static final int NS_T_DLV = 32769;
    public static final int NS_T_DNAME = 39;
    public static final int NS_T_DNSKEY = 48;
    public static final int NS_T_DS = 43;
    public static final int NS_T_EID = 31;
    public static final int NS_T_GPOS = 27;
    public static final int NS_T_HINFO = 13;
    public static final int NS_T_HIP = 55;
    public static final int NS_T_INVALID = 0;
    public static final int NS_T_IPSECKEY = 45;
    public static final int NS_T_ISDN = 20;
    public static final int NS_T_IXFR = 251;
    public static final int NS_T_KEY = 25;
    public static final int NS_T_KX = 36;
    public static final int NS_T_LOC = 29;
    public static final int NS_T_MAILA = 254;
    public static final int NS_T_MAILB = 253;
    public static final int NS_T_MAX = 65536;
    public static final int NS_T_MB = 7;
    public static final int NS_T_MD = 3;
    public static final int NS_T_MF = 4;
    public static final int NS_T_MG = 8;
    public static final int NS_T_MINFO = 14;
    public static final int NS_T_MR = 9;
    public static final int NS_T_MX = 15;
    public static final int NS_T_NAPTR = 35;
    public static final int NS_T_NIMLOC = 32;
    public static final int NS_T_NS = 2;
    public static final int NS_T_NSAP = 22;
    public static final int NS_T_NSAP_PTR = 23;
    public static final int NS_T_NSEC = 47;
    public static final int NS_T_NSEC3 = 50;
    public static final int NS_T_NSEC3PARAM = 51;
    public static final int NS_T_NULL = 10;
    public static final int NS_T_NXT = 30;
    public static final int NS_T_OPT = 41;
    public static final int NS_T_PTR = 12;
    public static final int NS_T_PX = 26;
    public static final int NS_T_RP = 17;
    public static final int NS_T_RRSIG = 46;
    public static final int NS_T_RT = 21;
    public static final int NS_T_SIG = 24;
    public static final int NS_T_SINK = 40;
    public static final int NS_T_SOA = 6;
    public static final int NS_T_SPF = 99;
    public static final int NS_T_SRV = 33;
    public static final int NS_T_SSHFP = 44;
    public static final int NS_T_TKEY = 249;
    public static final int NS_T_TSIG = 250;
    public static final int NS_T_TXT = 16;
    public static final int NS_T_WKS = 11;
    public static final int NS_T_X25 = 19;
    public static final int NS_T_ZXFR = 256;
    public static final int PDM_OFF = 1;
    public static final int PDM_OPPORTUNISTIC = 2;
    public static final int PDM_STRICT = 3;
    public static final int PDM_UNKNOWN = 0;
    public static final int RC_EAI_ADDRFAMILY = 1;
    public static final int RC_EAI_AGAIN = 2;
    public static final int RC_EAI_BADFLAGS = 3;
    public static final int RC_EAI_BADHINTS = 12;
    public static final int RC_EAI_FAIL = 4;
    public static final int RC_EAI_FAMILY = 5;
    public static final int RC_EAI_MAX = 256;
    public static final int RC_EAI_MEMORY = 6;
    public static final int RC_EAI_NODATA = 7;
    public static final int RC_EAI_NONAME = 8;
    public static final int RC_EAI_NO_ERROR = 0;
    public static final int RC_EAI_OVERFLOW = 14;
    public static final int RC_EAI_PROTOCOL = 13;
    public static final int RC_EAI_SERVICE = 9;
    public static final int RC_EAI_SOCKTYPE = 10;
    public static final int RC_EAI_SYSTEM = 11;
    public static final int RC_RESOLV_TIMEOUT = 255;
    public static final int TRANSPORT_BLUETOOTH = 2;
    public static final int TRANSPORT_DEFAULT = 0;
    public static final int TRANSPORT_ETHERNET = 3;
    public static final int TRANSPORT_LOWPAN = 6;
    public static final int TRANSPORT_VPN = 4;
    public static final int TRANSPORT_WIFI = 1;
    public static final int TRANSPORT_WIFI_AWARE = 5;
    public static final int TT_DOT = 3;
    public static final int TT_TCP = 2;
    public static final int TT_UDP = 1;
    public static final int TT_UNKNOWN = 0;

    public final class DnsQueryEvent {
        public static final long CACHE_HIT = 1159641169923L;
        public static final long CONNECTED = 1133871366152L;
        public static final long DNS_SERVER_COUNT = 1120986464263L;
        public static final long IP_VERSION = 1159641169924L;
        public static final long LATENCY_MICROS = 1120986464265L;
        public static final long RCODE = 1159641169921L;
        public static final long RETRY_TIMES = 1120986464262L;
        public static final long TRANSPORT = 1159641169925L;
        public static final long TYPE = 1159641169922L;

        public DnsQueryEvent() {
        }
    }

    public final class DnsQueryEvents {
        public static final long DNS_QUERY_EVENT = 2246267895809L;

        public DnsQueryEvents() {
        }
    }
}
