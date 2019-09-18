package android.net.netlink;

import android.net.util.NetworkConstants;
import android.system.OsConstants;
import com.android.internal.util.HexDump;
import com.android.server.backup.internal.BackupHandler;
import com.android.server.wm.WindowManagerService;
import java.nio.ByteBuffer;

public class NetlinkConstants {
    public static final int NLA_ALIGNTO = 4;
    public static final short NLMSG_DONE = 3;
    public static final short NLMSG_ERROR = 2;
    public static final short NLMSG_MAX_RESERVED = 15;
    public static final short NLMSG_NOOP = 1;
    public static final short NLMSG_OVERRUN = 4;
    public static final short RTM_DELADDR = 21;
    public static final short RTM_DELLINK = 17;
    public static final short RTM_DELNEIGH = 29;
    public static final short RTM_DELROUTE = 25;
    public static final short RTM_DELRULE = 33;
    public static final short RTM_GETADDR = 22;
    public static final short RTM_GETLINK = 18;
    public static final short RTM_GETNEIGH = 30;
    public static final short RTM_GETROUTE = 26;
    public static final short RTM_GETRULE = 34;
    public static final short RTM_NEWADDR = 20;
    public static final short RTM_NEWLINK = 16;
    public static final short RTM_NEWNDUSEROPT = 68;
    public static final short RTM_NEWNEIGH = 28;
    public static final short RTM_NEWROUTE = 24;
    public static final short RTM_NEWRULE = 32;
    public static final short RTM_SETLINK = 19;

    private NetlinkConstants() {
    }

    public static final int alignedLengthOf(short length) {
        return alignedLengthOf(65535 & length);
    }

    public static final int alignedLengthOf(int length) {
        if (length <= 0) {
            return 0;
        }
        return (((length + 4) - 1) / 4) * 4;
    }

    public static String stringForAddressFamily(int family) {
        if (family == OsConstants.AF_INET) {
            return "AF_INET";
        }
        if (family == OsConstants.AF_INET6) {
            return "AF_INET6";
        }
        if (family == OsConstants.AF_NETLINK) {
            return "AF_NETLINK";
        }
        return String.valueOf(family);
    }

    public static String hexify(byte[] bytes) {
        if (bytes == null) {
            return "(null)";
        }
        return HexDump.toHexString(bytes);
    }

    public static String hexify(ByteBuffer buffer) {
        if (buffer == null) {
            return "(null)";
        }
        return HexDump.toHexString(buffer.array(), buffer.position(), buffer.remaining());
    }

    public static String stringForNlMsgType(short nlm_type) {
        if (nlm_type == 68) {
            return "RTM_NEWNDUSEROPT";
        }
        switch (nlm_type) {
            case 1:
                return "NLMSG_NOOP";
            case 2:
                return "NLMSG_ERROR";
            case 3:
                return "NLMSG_DONE";
            case 4:
                return "NLMSG_OVERRUN";
            default:
                switch (nlm_type) {
                    case 16:
                        return "RTM_NEWLINK";
                    case 17:
                        return "RTM_DELLINK";
                    case 18:
                        return "RTM_GETLINK";
                    case WindowManagerService.H.REPORT_WINDOWS_CHANGE /*19*/:
                        return "RTM_SETLINK";
                    case 20:
                        return "RTM_NEWADDR";
                    case BackupHandler.MSG_OP_COMPLETE /*21*/:
                        return "RTM_DELADDR";
                    case WindowManagerService.H.REPORT_HARD_KEYBOARD_STATUS_CHANGE /*22*/:
                        return "RTM_GETADDR";
                    default:
                        switch (nlm_type) {
                            case 24:
                                return "RTM_NEWROUTE";
                            case WindowManagerService.H.SHOW_STRICT_MODE_VIOLATION /*25*/:
                                return "RTM_DELROUTE";
                            case WindowManagerService.H.DO_ANIMATION_CALLBACK /*26*/:
                                return "RTM_GETROUTE";
                            default:
                                switch (nlm_type) {
                                    case NetworkConstants.ARP_PAYLOAD_LEN /*28*/:
                                        return "RTM_NEWNEIGH";
                                    case HdmiCecKeycode.CEC_KEYCODE_NUMBER_ENTRY_MODE /*29*/:
                                        return "RTM_DELNEIGH";
                                    case 30:
                                        return "RTM_GETNEIGH";
                                    default:
                                        switch (nlm_type) {
                                            case 32:
                                                return "RTM_NEWRULE";
                                            case 33:
                                                return "RTM_DELRULE";
                                            case 34:
                                                return "RTM_GETRULE";
                                            default:
                                                return "unknown RTM type: " + String.valueOf(nlm_type);
                                        }
                                }
                        }
                }
        }
    }
}
