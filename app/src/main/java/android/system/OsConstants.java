package android.system;

import android.icu.impl.Normalizer2Impl;
import dalvik.bytecode.Opcodes;
import org.w3c.dom.traversal.NodeFilter;

public final class OsConstants {
    public static final int AF_INET = 0;
    public static final int AF_INET6 = 0;
    public static final int AF_NETLINK = 0;
    public static final int AF_PACKET = 0;
    public static final int AF_UNIX = 0;
    public static final int AF_UNSPEC = 0;
    public static final int AI_ADDRCONFIG = 0;
    public static final int AI_ALL = 0;
    public static final int AI_CANONNAME = 0;
    public static final int AI_NUMERICHOST = 0;
    public static final int AI_NUMERICSERV = 0;
    public static final int AI_PASSIVE = 0;
    public static final int AI_V4MAPPED = 0;
    public static final int ARPHRD_ETHER = 0;
    public static final int ARPHRD_LOOPBACK = 0;
    public static final int CAP_AUDIT_CONTROL = 0;
    public static final int CAP_AUDIT_WRITE = 0;
    public static final int CAP_BLOCK_SUSPEND = 0;
    public static final int CAP_CHOWN = 0;
    public static final int CAP_DAC_OVERRIDE = 0;
    public static final int CAP_DAC_READ_SEARCH = 0;
    public static final int CAP_FOWNER = 0;
    public static final int CAP_FSETID = 0;
    public static final int CAP_IPC_LOCK = 0;
    public static final int CAP_IPC_OWNER = 0;
    public static final int CAP_KILL = 0;
    public static final int CAP_LAST_CAP = 0;
    public static final int CAP_LEASE = 0;
    public static final int CAP_LINUX_IMMUTABLE = 0;
    public static final int CAP_MAC_ADMIN = 0;
    public static final int CAP_MAC_OVERRIDE = 0;
    public static final int CAP_MKNOD = 0;
    public static final int CAP_NET_ADMIN = 0;
    public static final int CAP_NET_BIND_SERVICE = 0;
    public static final int CAP_NET_BROADCAST = 0;
    public static final int CAP_NET_RAW = 0;
    public static final int CAP_SETFCAP = 0;
    public static final int CAP_SETGID = 0;
    public static final int CAP_SETPCAP = 0;
    public static final int CAP_SETUID = 0;
    public static final int CAP_SYSLOG = 0;
    public static final int CAP_SYS_ADMIN = 0;
    public static final int CAP_SYS_BOOT = 0;
    public static final int CAP_SYS_CHROOT = 0;
    public static final int CAP_SYS_MODULE = 0;
    public static final int CAP_SYS_NICE = 0;
    public static final int CAP_SYS_PACCT = 0;
    public static final int CAP_SYS_PTRACE = 0;
    public static final int CAP_SYS_RAWIO = 0;
    public static final int CAP_SYS_RESOURCE = 0;
    public static final int CAP_SYS_TIME = 0;
    public static final int CAP_SYS_TTY_CONFIG = 0;
    public static final int CAP_WAKE_ALARM = 0;
    public static final int E2BIG = 0;
    public static final int EACCES = 0;
    public static final int EADDRINUSE = 0;
    public static final int EADDRNOTAVAIL = 0;
    public static final int EAFNOSUPPORT = 0;
    public static final int EAGAIN = 0;
    public static final int EAI_AGAIN = 0;
    public static final int EAI_BADFLAGS = 0;
    public static final int EAI_FAIL = 0;
    public static final int EAI_FAMILY = 0;
    public static final int EAI_MEMORY = 0;
    public static final int EAI_NODATA = 0;
    public static final int EAI_NONAME = 0;
    public static final int EAI_OVERFLOW = 0;
    public static final int EAI_SERVICE = 0;
    public static final int EAI_SOCKTYPE = 0;
    public static final int EAI_SYSTEM = 0;
    public static final int EALREADY = 0;
    public static final int EBADF = 0;
    public static final int EBADMSG = 0;
    public static final int EBUSY = 0;
    public static final int ECANCELED = 0;
    public static final int ECHILD = 0;
    public static final int ECONNABORTED = 0;
    public static final int ECONNREFUSED = 0;
    public static final int ECONNRESET = 0;
    public static final int EDEADLK = 0;
    public static final int EDESTADDRREQ = 0;
    public static final int EDOM = 0;
    public static final int EDQUOT = 0;
    public static final int EEXIST = 0;
    public static final int EFAULT = 0;
    public static final int EFBIG = 0;
    public static final int EHOSTUNREACH = 0;
    public static final int EIDRM = 0;
    public static final int EILSEQ = 0;
    public static final int EINPROGRESS = 0;
    public static final int EINTR = 0;
    public static final int EINVAL = 0;
    public static final int EIO = 0;
    public static final int EISCONN = 0;
    public static final int EISDIR = 0;
    public static final int ELOOP = 0;
    public static final int EMFILE = 0;
    public static final int EMLINK = 0;
    public static final int EMSGSIZE = 0;
    public static final int EMULTIHOP = 0;
    public static final int ENAMETOOLONG = 0;
    public static final int ENETDOWN = 0;
    public static final int ENETRESET = 0;
    public static final int ENETUNREACH = 0;
    public static final int ENFILE = 0;
    public static final int ENOBUFS = 0;
    public static final int ENODATA = 0;
    public static final int ENODEV = 0;
    public static final int ENOENT = 0;
    public static final int ENOEXEC = 0;
    public static final int ENOLCK = 0;
    public static final int ENOLINK = 0;
    public static final int ENOMEM = 0;
    public static final int ENOMSG = 0;
    public static final int ENONET = 0;
    public static final int ENOPROTOOPT = 0;
    public static final int ENOSPC = 0;
    public static final int ENOSR = 0;
    public static final int ENOSTR = 0;
    public static final int ENOSYS = 0;
    public static final int ENOTCONN = 0;
    public static final int ENOTDIR = 0;
    public static final int ENOTEMPTY = 0;
    public static final int ENOTSOCK = 0;
    public static final int ENOTSUP = 0;
    public static final int ENOTTY = 0;
    public static final int ENXIO = 0;
    public static final int EOPNOTSUPP = 0;
    public static final int EOVERFLOW = 0;
    public static final int EPERM = 0;
    public static final int EPIPE = 0;
    public static final int EPROTO = 0;
    public static final int EPROTONOSUPPORT = 0;
    public static final int EPROTOTYPE = 0;
    public static final int ERANGE = 0;
    public static final int EROFS = 0;
    public static final int ESPIPE = 0;
    public static final int ESRCH = 0;
    public static final int ESTALE = 0;
    public static final int ETH_P_ALL = 0;
    public static final int ETH_P_ARP = 0;
    public static final int ETH_P_IP = 0;
    public static final int ETH_P_IPV6 = 0;
    public static final int ETIME = 0;
    public static final int ETIMEDOUT = 0;
    public static final int ETXTBSY = 0;
    public static final int EUSERS = 0;
    public static final int EXDEV = 0;
    public static final int EXIT_FAILURE = 0;
    public static final int EXIT_SUCCESS = 0;
    public static final int FD_CLOEXEC = 0;
    public static final int FIONREAD = 0;
    public static final int F_DUPFD = 0;
    public static final int F_DUPFD_CLOEXEC = 0;
    public static final int F_GETFD = 0;
    public static final int F_GETFL = 0;
    public static final int F_GETLK = 0;
    public static final int F_GETLK64 = 0;
    public static final int F_GETOWN = 0;
    public static final int F_OK = 0;
    public static final int F_RDLCK = 0;
    public static final int F_SETFD = 0;
    public static final int F_SETFL = 0;
    public static final int F_SETLK = 0;
    public static final int F_SETLK64 = 0;
    public static final int F_SETLKW = 0;
    public static final int F_SETLKW64 = 0;
    public static final int F_SETOWN = 0;
    public static final int F_UNLCK = 0;
    public static final int F_WRLCK = 0;
    public static final int IFA_F_DADFAILED = 0;
    public static final int IFA_F_DEPRECATED = 0;
    public static final int IFA_F_HOMEADDRESS = 0;
    public static final int IFA_F_NODAD = 0;
    public static final int IFA_F_OPTIMISTIC = 0;
    public static final int IFA_F_PERMANENT = 0;
    public static final int IFA_F_SECONDARY = 0;
    public static final int IFA_F_TEMPORARY = 0;
    public static final int IFA_F_TENTATIVE = 0;
    public static final int IFF_ALLMULTI = 0;
    public static final int IFF_AUTOMEDIA = 0;
    public static final int IFF_BROADCAST = 0;
    public static final int IFF_DEBUG = 0;
    public static final int IFF_DYNAMIC = 0;
    public static final int IFF_LOOPBACK = 0;
    public static final int IFF_MASTER = 0;
    public static final int IFF_MULTICAST = 0;
    public static final int IFF_NOARP = 0;
    public static final int IFF_NOTRAILERS = 0;
    public static final int IFF_POINTOPOINT = 0;
    public static final int IFF_PORTSEL = 0;
    public static final int IFF_PROMISC = 0;
    public static final int IFF_RUNNING = 0;
    public static final int IFF_SLAVE = 0;
    public static final int IFF_UP = 0;
    public static final int IPPROTO_ICMP = 0;
    public static final int IPPROTO_ICMPV6 = 0;
    public static final int IPPROTO_IP = 0;
    public static final int IPPROTO_IPV6 = 0;
    public static final int IPPROTO_RAW = 0;
    public static final int IPPROTO_TCP = 0;
    public static final int IPPROTO_UDP = 0;
    public static final int IPV6_CHECKSUM = 0;
    public static final int IPV6_MULTICAST_HOPS = 0;
    public static final int IPV6_MULTICAST_IF = 0;
    public static final int IPV6_MULTICAST_LOOP = 0;
    public static final int IPV6_RECVDSTOPTS = 0;
    public static final int IPV6_RECVHOPLIMIT = 0;
    public static final int IPV6_RECVHOPOPTS = 0;
    public static final int IPV6_RECVPKTINFO = 0;
    public static final int IPV6_RECVRTHDR = 0;
    public static final int IPV6_RECVTCLASS = 0;
    public static final int IPV6_TCLASS = 0;
    public static final int IPV6_UNICAST_HOPS = 0;
    public static final int IPV6_V6ONLY = 0;
    public static final int IP_MULTICAST_IF = 0;
    public static final int IP_MULTICAST_LOOP = 0;
    public static final int IP_MULTICAST_TTL = 0;
    public static final int IP_RECVTOS = 0;
    public static final int IP_TOS = 0;
    public static final int IP_TTL = 0;
    public static final int MAP_FIXED = 0;
    public static final int MAP_POPULATE = 0;
    public static final int MAP_PRIVATE = 0;
    public static final int MAP_SHARED = 0;
    public static final int MCAST_BLOCK_SOURCE = 0;
    public static final int MCAST_JOIN_GROUP = 0;
    public static final int MCAST_JOIN_SOURCE_GROUP = 0;
    public static final int MCAST_LEAVE_GROUP = 0;
    public static final int MCAST_LEAVE_SOURCE_GROUP = 0;
    public static final int MCAST_UNBLOCK_SOURCE = 0;
    public static final int MCL_CURRENT = 0;
    public static final int MCL_FUTURE = 0;
    public static final int MSG_CTRUNC = 0;
    public static final int MSG_DONTROUTE = 0;
    public static final int MSG_EOR = 0;
    public static final int MSG_HRT = 0;
    public static final int MSG_LPW = 0;
    public static final int MSG_OOB = 0;
    public static final int MSG_PEEK = 0;
    public static final int MSG_TRUNC = 0;
    public static final int MSG_WAITALL = 0;
    public static final int MS_ASYNC = 0;
    public static final int MS_INVALIDATE = 0;
    public static final int MS_SYNC = 0;
    public static final int NETLINK_ROUTE = 0;
    public static final int NI_DGRAM = 0;
    public static final int NI_NAMEREQD = 0;
    public static final int NI_NOFQDN = 0;
    public static final int NI_NUMERICHOST = 0;
    public static final int NI_NUMERICSERV = 0;
    public static final int O_ACCMODE = 0;
    public static final int O_APPEND = 0;
    public static final int O_CLOEXEC = 0;
    public static final int O_CREAT = 0;
    public static final int O_DSYNC = 0;
    public static final int O_EXCL = 0;
    public static final int O_NOCTTY = 0;
    public static final int O_NOFOLLOW = 0;
    public static final int O_NONBLOCK = 0;
    public static final int O_RDONLY = 0;
    public static final int O_RDWR = 0;
    public static final int O_SYNC = 0;
    public static final int O_TRUNC = 0;
    public static final int O_WRONLY = 0;
    public static final int POLLERR = 0;
    public static final int POLLHUP = 0;
    public static final int POLLIN = 0;
    public static final int POLLNVAL = 0;
    public static final int POLLOUT = 0;
    public static final int POLLPRI = 0;
    public static final int POLLRDBAND = 0;
    public static final int POLLRDNORM = 0;
    public static final int POLLWRBAND = 0;
    public static final int POLLWRNORM = 0;
    public static final int PROT_EXEC = 0;
    public static final int PROT_NONE = 0;
    public static final int PROT_READ = 0;
    public static final int PROT_WRITE = 0;
    public static final int PR_GET_DUMPABLE = 0;
    public static final int PR_SET_DUMPABLE = 0;
    public static final int PR_SET_NO_NEW_PRIVS = 0;
    public static final int RTMGRP_IPV4_IFADDR = 0;
    public static final int RTMGRP_IPV4_MROUTE = 0;
    public static final int RTMGRP_IPV4_ROUTE = 0;
    public static final int RTMGRP_IPV4_RULE = 0;
    public static final int RTMGRP_IPV6_IFADDR = 0;
    public static final int RTMGRP_IPV6_IFINFO = 0;
    public static final int RTMGRP_IPV6_MROUTE = 0;
    public static final int RTMGRP_IPV6_PREFIX = 0;
    public static final int RTMGRP_IPV6_ROUTE = 0;
    public static final int RTMGRP_LINK = 0;
    public static final int RTMGRP_NEIGH = 0;
    public static final int RTMGRP_NOTIFY = 0;
    public static final int RTMGRP_TC = 0;
    public static final int RT_SCOPE_HOST = 0;
    public static final int RT_SCOPE_LINK = 0;
    public static final int RT_SCOPE_NOWHERE = 0;
    public static final int RT_SCOPE_SITE = 0;
    public static final int RT_SCOPE_UNIVERSE = 0;
    public static final int R_OK = 0;
    public static final int SEEK_CUR = 0;
    public static final int SEEK_END = 0;
    public static final int SEEK_SET = 0;
    public static final int SHUT_RD = 0;
    public static final int SHUT_RDWR = 0;
    public static final int SHUT_WR = 0;
    public static final int SIGABRT = 0;
    public static final int SIGALRM = 0;
    public static final int SIGBUS = 0;
    public static final int SIGCHLD = 0;
    public static final int SIGCONT = 0;
    public static final int SIGFPE = 0;
    public static final int SIGHUP = 0;
    public static final int SIGILL = 0;
    public static final int SIGINT = 0;
    public static final int SIGIO = 0;
    public static final int SIGKILL = 0;
    public static final int SIGPIPE = 0;
    public static final int SIGPROF = 0;
    public static final int SIGPWR = 0;
    public static final int SIGQUIT = 0;
    public static final int SIGRTMAX = 0;
    public static final int SIGRTMIN = 0;
    public static final int SIGSEGV = 0;
    public static final int SIGSTKFLT = 0;
    public static final int SIGSTOP = 0;
    public static final int SIGSYS = 0;
    public static final int SIGTERM = 0;
    public static final int SIGTRAP = 0;
    public static final int SIGTSTP = 0;
    public static final int SIGTTIN = 0;
    public static final int SIGTTOU = 0;
    public static final int SIGURG = 0;
    public static final int SIGUSR1 = 0;
    public static final int SIGUSR2 = 0;
    public static final int SIGVTALRM = 0;
    public static final int SIGWINCH = 0;
    public static final int SIGXCPU = 0;
    public static final int SIGXFSZ = 0;
    public static final int SIOCGIFADDR = 0;
    public static final int SIOCGIFBRDADDR = 0;
    public static final int SIOCGIFDSTADDR = 0;
    public static final int SIOCGIFNETMASK = 0;
    public static final int SOCK_DGRAM = 0;
    public static final int SOCK_RAW = 0;
    public static final int SOCK_SEQPACKET = 0;
    public static final int SOCK_STREAM = 0;
    public static final int SOL_SOCKET = 0;
    public static final int SO_BINDTODEVICE = 0;
    public static final int SO_BROADCAST = 0;
    public static final int SO_DEBUG = 0;
    public static final int SO_DONTROUTE = 0;
    public static final int SO_ERROR = 0;
    public static final int SO_KEEPALIVE = 0;
    public static final int SO_LINGER = 0;
    public static final int SO_OOBINLINE = 0;
    public static final int SO_PASSCRED = 0;
    public static final int SO_PEERCRED = 0;
    public static final int SO_RCVBUF = 0;
    public static final int SO_RCVLOWAT = 0;
    public static final int SO_RCVTIMEO = 0;
    public static final int SO_REUSEADDR = 0;
    public static final int SO_SNDBUF = 0;
    public static final int SO_SNDLOWAT = 0;
    public static final int SO_SNDTIMEO = 0;
    public static final int SO_TYPE = 0;
    public static final int STDERR_FILENO = 0;
    public static final int STDIN_FILENO = 0;
    public static final int STDOUT_FILENO = 0;
    public static final int ST_MANDLOCK = 0;
    public static final int ST_NOATIME = 0;
    public static final int ST_NODEV = 0;
    public static final int ST_NODIRATIME = 0;
    public static final int ST_NOEXEC = 0;
    public static final int ST_NOSUID = 0;
    public static final int ST_RDONLY = 0;
    public static final int ST_RELATIME = 0;
    public static final int ST_SYNCHRONOUS = 0;
    public static final int S_IFBLK = 0;
    public static final int S_IFCHR = 0;
    public static final int S_IFDIR = 0;
    public static final int S_IFIFO = 0;
    public static final int S_IFLNK = 0;
    public static final int S_IFMT = 0;
    public static final int S_IFREG = 0;
    public static final int S_IFSOCK = 0;
    public static final int S_IRGRP = 0;
    public static final int S_IROTH = 0;
    public static final int S_IRUSR = 0;
    public static final int S_IRWXG = 0;
    public static final int S_IRWXO = 0;
    public static final int S_IRWXU = 0;
    public static final int S_ISGID = 0;
    public static final int S_ISUID = 0;
    public static final int S_ISVTX = 0;
    public static final int S_IWGRP = 0;
    public static final int S_IWOTH = 0;
    public static final int S_IWUSR = 0;
    public static final int S_IXGRP = 0;
    public static final int S_IXOTH = 0;
    public static final int S_IXUSR = 0;
    public static final int TCP_NODELAY = 0;
    public static final int TCP_RECONN = 0;
    public static final int TIOCOUTQ = 0;
    public static final int UNIX_PATH_MAX = 0;
    public static final int WCONTINUED = 0;
    public static final int WEXITED = 0;
    public static final int WNOHANG = 0;
    public static final int WNOWAIT = 0;
    public static final int WSTOPPED = 0;
    public static final int WUNTRACED = 0;
    public static final int W_OK = 0;
    public static final int XATTR_CREATE = 0;
    public static final int XATTR_REPLACE = 0;
    public static final int X_OK = 0;
    public static final int _SC_2_CHAR_TERM = 0;
    public static final int _SC_2_C_BIND = 0;
    public static final int _SC_2_C_DEV = 0;
    public static final int _SC_2_C_VERSION = 0;
    public static final int _SC_2_FORT_DEV = 0;
    public static final int _SC_2_FORT_RUN = 0;
    public static final int _SC_2_LOCALEDEF = 0;
    public static final int _SC_2_SW_DEV = 0;
    public static final int _SC_2_UPE = 0;
    public static final int _SC_2_VERSION = 0;
    public static final int _SC_AIO_LISTIO_MAX = 0;
    public static final int _SC_AIO_MAX = 0;
    public static final int _SC_AIO_PRIO_DELTA_MAX = 0;
    public static final int _SC_ARG_MAX = 0;
    public static final int _SC_ASYNCHRONOUS_IO = 0;
    public static final int _SC_ATEXIT_MAX = 0;
    public static final int _SC_AVPHYS_PAGES = 0;
    public static final int _SC_BC_BASE_MAX = 0;
    public static final int _SC_BC_DIM_MAX = 0;
    public static final int _SC_BC_SCALE_MAX = 0;
    public static final int _SC_BC_STRING_MAX = 0;
    public static final int _SC_CHILD_MAX = 0;
    public static final int _SC_CLK_TCK = 0;
    public static final int _SC_COLL_WEIGHTS_MAX = 0;
    public static final int _SC_DELAYTIMER_MAX = 0;
    public static final int _SC_EXPR_NEST_MAX = 0;
    public static final int _SC_FSYNC = 0;
    public static final int _SC_GETGR_R_SIZE_MAX = 0;
    public static final int _SC_GETPW_R_SIZE_MAX = 0;
    public static final int _SC_IOV_MAX = 0;
    public static final int _SC_JOB_CONTROL = 0;
    public static final int _SC_LINE_MAX = 0;
    public static final int _SC_LOGIN_NAME_MAX = 0;
    public static final int _SC_MAPPED_FILES = 0;
    public static final int _SC_MEMLOCK = 0;
    public static final int _SC_MEMLOCK_RANGE = 0;
    public static final int _SC_MEMORY_PROTECTION = 0;
    public static final int _SC_MESSAGE_PASSING = 0;
    public static final int _SC_MQ_OPEN_MAX = 0;
    public static final int _SC_MQ_PRIO_MAX = 0;
    public static final int _SC_NGROUPS_MAX = 0;
    public static final int _SC_NPROCESSORS_CONF = 0;
    public static final int _SC_NPROCESSORS_ONLN = 0;
    public static final int _SC_OPEN_MAX = 0;
    public static final int _SC_PAGESIZE = 0;
    public static final int _SC_PAGE_SIZE = 0;
    public static final int _SC_PASS_MAX = 0;
    public static final int _SC_PHYS_PAGES = 0;
    public static final int _SC_PRIORITIZED_IO = 0;
    public static final int _SC_PRIORITY_SCHEDULING = 0;
    public static final int _SC_REALTIME_SIGNALS = 0;
    public static final int _SC_RE_DUP_MAX = 0;
    public static final int _SC_RTSIG_MAX = 0;
    public static final int _SC_SAVED_IDS = 0;
    public static final int _SC_SEMAPHORES = 0;
    public static final int _SC_SEM_NSEMS_MAX = 0;
    public static final int _SC_SEM_VALUE_MAX = 0;
    public static final int _SC_SHARED_MEMORY_OBJECTS = 0;
    public static final int _SC_SIGQUEUE_MAX = 0;
    public static final int _SC_STREAM_MAX = 0;
    public static final int _SC_SYNCHRONIZED_IO = 0;
    public static final int _SC_THREADS = 0;
    public static final int _SC_THREAD_ATTR_STACKADDR = 0;
    public static final int _SC_THREAD_ATTR_STACKSIZE = 0;
    public static final int _SC_THREAD_DESTRUCTOR_ITERATIONS = 0;
    public static final int _SC_THREAD_KEYS_MAX = 0;
    public static final int _SC_THREAD_PRIORITY_SCHEDULING = 0;
    public static final int _SC_THREAD_PRIO_INHERIT = 0;
    public static final int _SC_THREAD_PRIO_PROTECT = 0;
    public static final int _SC_THREAD_SAFE_FUNCTIONS = 0;
    public static final int _SC_THREAD_STACK_MIN = 0;
    public static final int _SC_THREAD_THREADS_MAX = 0;
    public static final int _SC_TIMERS = 0;
    public static final int _SC_TIMER_MAX = 0;
    public static final int _SC_TTY_NAME_MAX = 0;
    public static final int _SC_TZNAME_MAX = 0;
    public static final int _SC_VERSION = 0;
    public static final int _SC_XBS5_ILP32_OFF32 = 0;
    public static final int _SC_XBS5_ILP32_OFFBIG = 0;
    public static final int _SC_XBS5_LP64_OFF64 = 0;
    public static final int _SC_XBS5_LPBIG_OFFBIG = 0;
    public static final int _SC_XOPEN_CRYPT = 0;
    public static final int _SC_XOPEN_ENH_I18N = 0;
    public static final int _SC_XOPEN_LEGACY = 0;
    public static final int _SC_XOPEN_REALTIME = 0;
    public static final int _SC_XOPEN_REALTIME_THREADS = 0;
    public static final int _SC_XOPEN_SHM = 0;
    public static final int _SC_XOPEN_UNIX = 0;
    public static final int _SC_XOPEN_VERSION = 0;
    public static final int _SC_XOPEN_XCU_VERSION = 0;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.system.OsConstants.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.system.OsConstants.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.system.OsConstants.<clinit>():void");
    }

    private static native void initConstants();

    private OsConstants() {
    }

    public static boolean S_ISBLK(int mode) {
        return (S_IFMT & mode) == S_IFBLK;
    }

    public static boolean S_ISCHR(int mode) {
        return (S_IFMT & mode) == S_IFCHR;
    }

    public static boolean S_ISDIR(int mode) {
        return (S_IFMT & mode) == S_IFDIR;
    }

    public static boolean S_ISFIFO(int mode) {
        return (S_IFMT & mode) == S_IFIFO;
    }

    public static boolean S_ISREG(int mode) {
        return (S_IFMT & mode) == S_IFREG;
    }

    public static boolean S_ISLNK(int mode) {
        return (S_IFMT & mode) == S_IFLNK;
    }

    public static boolean S_ISSOCK(int mode) {
        return (S_IFMT & mode) == S_IFSOCK;
    }

    public static int WEXITSTATUS(int status) {
        return (Normalizer2Impl.JAMO_VT & status) >> 8;
    }

    public static boolean WCOREDUMP(int status) {
        return (status & NodeFilter.SHOW_COMMENT) != 0;
    }

    public static int WTERMSIG(int status) {
        return status & Opcodes.OP_NEG_FLOAT;
    }

    public static int WSTOPSIG(int status) {
        return WEXITSTATUS(status);
    }

    public static boolean WIFEXITED(int status) {
        return WTERMSIG(status) == 0;
    }

    public static boolean WIFSTOPPED(int status) {
        return WTERMSIG(status) == Opcodes.OP_NEG_FLOAT;
    }

    public static boolean WIFSIGNALED(int status) {
        return WTERMSIG(status + 1) >= 2;
    }

    public static String gaiName(int error) {
        if (error == EAI_AGAIN) {
            return "EAI_AGAIN";
        }
        if (error == EAI_BADFLAGS) {
            return "EAI_BADFLAGS";
        }
        if (error == EAI_FAIL) {
            return "EAI_FAIL";
        }
        if (error == EAI_FAMILY) {
            return "EAI_FAMILY";
        }
        if (error == EAI_MEMORY) {
            return "EAI_MEMORY";
        }
        if (error == EAI_NODATA) {
            return "EAI_NODATA";
        }
        if (error == EAI_NONAME) {
            return "EAI_NONAME";
        }
        if (error == EAI_OVERFLOW) {
            return "EAI_OVERFLOW";
        }
        if (error == EAI_SERVICE) {
            return "EAI_SERVICE";
        }
        if (error == EAI_SOCKTYPE) {
            return "EAI_SOCKTYPE";
        }
        if (error == EAI_SYSTEM) {
            return "EAI_SYSTEM";
        }
        return null;
    }

    public static String errnoName(int errno) {
        if (errno == E2BIG) {
            return "E2BIG";
        }
        if (errno == EACCES) {
            return "EACCES";
        }
        if (errno == EADDRINUSE) {
            return "EADDRINUSE";
        }
        if (errno == EADDRNOTAVAIL) {
            return "EADDRNOTAVAIL";
        }
        if (errno == EAFNOSUPPORT) {
            return "EAFNOSUPPORT";
        }
        if (errno == EAGAIN) {
            return "EAGAIN";
        }
        if (errno == EALREADY) {
            return "EALREADY";
        }
        if (errno == EBADF) {
            return "EBADF";
        }
        if (errno == EBADMSG) {
            return "EBADMSG";
        }
        if (errno == EBUSY) {
            return "EBUSY";
        }
        if (errno == ECANCELED) {
            return "ECANCELED";
        }
        if (errno == ECHILD) {
            return "ECHILD";
        }
        if (errno == ECONNABORTED) {
            return "ECONNABORTED";
        }
        if (errno == ECONNREFUSED) {
            return "ECONNREFUSED";
        }
        if (errno == ECONNRESET) {
            return "ECONNRESET";
        }
        if (errno == EDEADLK) {
            return "EDEADLK";
        }
        if (errno == EDESTADDRREQ) {
            return "EDESTADDRREQ";
        }
        if (errno == EDOM) {
            return "EDOM";
        }
        if (errno == EDQUOT) {
            return "EDQUOT";
        }
        if (errno == EEXIST) {
            return "EEXIST";
        }
        if (errno == EFAULT) {
            return "EFAULT";
        }
        if (errno == EFBIG) {
            return "EFBIG";
        }
        if (errno == EHOSTUNREACH) {
            return "EHOSTUNREACH";
        }
        if (errno == EIDRM) {
            return "EIDRM";
        }
        if (errno == EILSEQ) {
            return "EILSEQ";
        }
        if (errno == EINPROGRESS) {
            return "EINPROGRESS";
        }
        if (errno == EINTR) {
            return "EINTR";
        }
        if (errno == EINVAL) {
            return "EINVAL";
        }
        if (errno == EIO) {
            return "EIO";
        }
        if (errno == EISCONN) {
            return "EISCONN";
        }
        if (errno == EISDIR) {
            return "EISDIR";
        }
        if (errno == ELOOP) {
            return "ELOOP";
        }
        if (errno == EMFILE) {
            return "EMFILE";
        }
        if (errno == EMLINK) {
            return "EMLINK";
        }
        if (errno == EMSGSIZE) {
            return "EMSGSIZE";
        }
        if (errno == EMULTIHOP) {
            return "EMULTIHOP";
        }
        if (errno == ENAMETOOLONG) {
            return "ENAMETOOLONG";
        }
        if (errno == ENETDOWN) {
            return "ENETDOWN";
        }
        if (errno == ENETRESET) {
            return "ENETRESET";
        }
        if (errno == ENETUNREACH) {
            return "ENETUNREACH";
        }
        if (errno == ENFILE) {
            return "ENFILE";
        }
        if (errno == ENOBUFS) {
            return "ENOBUFS";
        }
        if (errno == ENODATA) {
            return "ENODATA";
        }
        if (errno == ENODEV) {
            return "ENODEV";
        }
        if (errno == ENOENT) {
            return "ENOENT";
        }
        if (errno == ENOEXEC) {
            return "ENOEXEC";
        }
        if (errno == ENOLCK) {
            return "ENOLCK";
        }
        if (errno == ENOLINK) {
            return "ENOLINK";
        }
        if (errno == ENOMEM) {
            return "ENOMEM";
        }
        if (errno == ENOMSG) {
            return "ENOMSG";
        }
        if (errno == ENONET) {
            return "ENONET";
        }
        if (errno == ENOPROTOOPT) {
            return "ENOPROTOOPT";
        }
        if (errno == ENOSPC) {
            return "ENOSPC";
        }
        if (errno == ENOSR) {
            return "ENOSR";
        }
        if (errno == ENOSTR) {
            return "ENOSTR";
        }
        if (errno == ENOSYS) {
            return "ENOSYS";
        }
        if (errno == ENOTCONN) {
            return "ENOTCONN";
        }
        if (errno == ENOTDIR) {
            return "ENOTDIR";
        }
        if (errno == ENOTEMPTY) {
            return "ENOTEMPTY";
        }
        if (errno == ENOTSOCK) {
            return "ENOTSOCK";
        }
        if (errno == ENOTSUP) {
            return "ENOTSUP";
        }
        if (errno == ENOTTY) {
            return "ENOTTY";
        }
        if (errno == ENXIO) {
            return "ENXIO";
        }
        if (errno == EOPNOTSUPP) {
            return "EOPNOTSUPP";
        }
        if (errno == EOVERFLOW) {
            return "EOVERFLOW";
        }
        if (errno == EPERM) {
            return "EPERM";
        }
        if (errno == EPIPE) {
            return "EPIPE";
        }
        if (errno == EPROTO) {
            return "EPROTO";
        }
        if (errno == EPROTONOSUPPORT) {
            return "EPROTONOSUPPORT";
        }
        if (errno == EPROTOTYPE) {
            return "EPROTOTYPE";
        }
        if (errno == ERANGE) {
            return "ERANGE";
        }
        if (errno == EROFS) {
            return "EROFS";
        }
        if (errno == ESPIPE) {
            return "ESPIPE";
        }
        if (errno == ESRCH) {
            return "ESRCH";
        }
        if (errno == ESTALE) {
            return "ESTALE";
        }
        if (errno == ETIME) {
            return "ETIME";
        }
        if (errno == ETIMEDOUT) {
            return "ETIMEDOUT";
        }
        if (errno == ETXTBSY) {
            return "ETXTBSY";
        }
        if (errno == EXDEV) {
            return "EXDEV";
        }
        return null;
    }

    private static int placeholder() {
        return 0;
    }
}
