package android.net.netlink;

import android.system.OsConstants;
import com.android.internal.util.HexDump;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import java.nio.ByteBuffer;

public class NetlinkConstants {
    public static final int NLA_ALIGNTO = 4;
    public static final short NLMSG_DONE = (short) 3;
    public static final short NLMSG_ERROR = (short) 2;
    public static final short NLMSG_MAX_RESERVED = (short) 15;
    public static final short NLMSG_NOOP = (short) 1;
    public static final short NLMSG_OVERRUN = (short) 4;
    public static final short RTM_DELADDR = (short) 21;
    public static final short RTM_DELLINK = (short) 17;
    public static final short RTM_DELNEIGH = (short) 29;
    public static final short RTM_DELROUTE = (short) 25;
    public static final short RTM_DELRULE = (short) 33;
    public static final short RTM_GETADDR = (short) 22;
    public static final short RTM_GETLINK = (short) 18;
    public static final short RTM_GETNEIGH = (short) 30;
    public static final short RTM_GETROUTE = (short) 26;
    public static final short RTM_GETRULE = (short) 34;
    public static final short RTM_NEWADDR = (short) 20;
    public static final short RTM_NEWLINK = (short) 16;
    public static final short RTM_NEWNDUSEROPT = (short) 68;
    public static final short RTM_NEWNEIGH = (short) 28;
    public static final short RTM_NEWROUTE = (short) 24;
    public static final short RTM_NEWRULE = (short) 32;
    public static final short RTM_SETLINK = (short) 19;

    private NetlinkConstants() {
    }

    public static final int alignedLengthOf(short length) {
        return alignedLengthOf(length & 65535);
    }

    public static final int alignedLengthOf(int length) {
        if (length <= 0) {
            return 0;
        }
        return (((length + NLA_ALIGNTO) - 1) / NLA_ALIGNTO) * NLA_ALIGNTO;
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
        switch (nlm_type) {
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                return "NLMSG_NOOP";
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                return "NLMSG_ERROR";
            case H.REPORT_LOSING_FOCUS /*3*/:
                return "NLMSG_DONE";
            case NLA_ALIGNTO /*4*/:
                return "NLMSG_OVERRUN";
            case H.ENABLE_SCREEN /*16*/:
                return "RTM_NEWLINK";
            case H.APP_FREEZE_TIMEOUT /*17*/:
                return "RTM_DELLINK";
            case H.SEND_NEW_CONFIGURATION /*18*/:
                return "RTM_GETLINK";
            case H.REPORT_WINDOWS_CHANGE /*19*/:
                return "RTM_SETLINK";
            case H.DRAG_START_TIMEOUT /*20*/:
                return "RTM_NEWADDR";
            case H.DRAG_END_TIMEOUT /*21*/:
                return "RTM_DELADDR";
            case H.REPORT_HARD_KEYBOARD_STATUS_CHANGE /*22*/:
                return "RTM_GETADDR";
            case H.WAITING_FOR_DRAWN_TIMEOUT /*24*/:
                return "RTM_NEWROUTE";
            case H.SHOW_STRICT_MODE_VIOLATION /*25*/:
                return "RTM_DELROUTE";
            case H.DO_ANIMATION_CALLBACK /*26*/:
                return "RTM_GETROUTE";
            case H.DO_DISPLAY_REMOVED /*28*/:
                return "RTM_NEWNEIGH";
            case H.DO_DISPLAY_CHANGED /*29*/:
                return "RTM_DELNEIGH";
            case H.CLIENT_FREEZE_TIMEOUT /*30*/:
                return "RTM_GETNEIGH";
            case H.NOTIFY_ACTIVITY_DRAWN /*32*/:
                return "RTM_NEWRULE";
            case H.ALL_WINDOWS_DRAWN /*33*/:
                return "RTM_DELRULE";
            case H.NEW_ANIMATOR_SCALE /*34*/:
                return "RTM_GETRULE";
            case HdmiCecKeycode.CEC_KEYCODE_PLAY /*68*/:
                return "RTM_NEWNDUSEROPT";
            default:
                return "unknown RTM type: " + String.valueOf(nlm_type);
        }
    }
}
