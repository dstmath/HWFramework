package com.android.server.firewall;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Process;
import android.os.RemoteException;
import android.util.Slog;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class SenderFilter {
    private static final String ATTR_TYPE = "type";
    public static final FilterFactory FACTORY = new FilterFactory("sender") {
        public Filter newFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
            String typeString = parser.getAttributeValue(null, "type");
            if (typeString == null) {
                throw new XmlPullParserException("type attribute must be specified for <sender>", parser, null);
            } else if (typeString.equals(SenderFilter.VAL_SYSTEM)) {
                return SenderFilter.SYSTEM;
            } else {
                if (typeString.equals(SenderFilter.VAL_SIGNATURE)) {
                    return SenderFilter.SIGNATURE;
                }
                if (typeString.equals(SenderFilter.VAL_SYSTEM_OR_SIGNATURE)) {
                    return SenderFilter.SYSTEM_OR_SIGNATURE;
                }
                if (typeString.equals(SenderFilter.VAL_USER_ID)) {
                    return SenderFilter.USER_ID;
                }
                throw new XmlPullParserException("Invalid type attribute for <sender>: " + typeString, parser, null);
            }
        }
    };
    private static final Filter SIGNATURE = new Filter() {
        public boolean matches(IntentFirewall ifw, ComponentName resolvedComponent, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
            return ifw.signaturesMatch(callerUid, receivingUid);
        }
    };
    private static final Filter SYSTEM = new Filter() {
        public boolean matches(IntentFirewall ifw, ComponentName resolvedComponent, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
            return SenderFilter.isPrivilegedApp(callerUid, callerPid);
        }
    };
    private static final Filter SYSTEM_OR_SIGNATURE = new Filter() {
        public boolean matches(IntentFirewall ifw, ComponentName resolvedComponent, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
            if (SenderFilter.isPrivilegedApp(callerUid, callerPid)) {
                return true;
            }
            return ifw.signaturesMatch(callerUid, receivingUid);
        }
    };
    private static final Filter USER_ID = new Filter() {
        public boolean matches(IntentFirewall ifw, ComponentName resolvedComponent, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
            return ifw.checkComponentPermission(null, callerPid, callerUid, receivingUid, false);
        }
    };
    private static final String VAL_SIGNATURE = "signature";
    private static final String VAL_SYSTEM = "system";
    private static final String VAL_SYSTEM_OR_SIGNATURE = "system|signature";
    private static final String VAL_USER_ID = "userId";

    SenderFilter() {
    }

    static boolean isPrivilegedApp(int callerUid, int callerPid) {
        boolean z = true;
        if (callerUid == 1000 || callerUid == 0 || callerPid == Process.myPid() || callerPid == 0) {
            return true;
        }
        try {
            if ((AppGlobals.getPackageManager().getPrivateFlagsForUid(callerUid) & 8) == 0) {
                z = false;
            }
            return z;
        } catch (RemoteException ex) {
            Slog.e("IntentFirewall", "Remote exception while retrieving uid flags", ex);
            return false;
        }
    }
}
