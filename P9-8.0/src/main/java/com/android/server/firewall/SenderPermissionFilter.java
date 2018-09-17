package com.android.server.firewall;

import android.content.ComponentName;
import android.content.Intent;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class SenderPermissionFilter implements Filter {
    private static final String ATTR_NAME = "name";
    public static final FilterFactory FACTORY = new FilterFactory("sender-permission") {
        public Filter newFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
            String permission = parser.getAttributeValue(null, SenderPermissionFilter.ATTR_NAME);
            if (permission != null) {
                return new SenderPermissionFilter(permission, null);
            }
            throw new XmlPullParserException("Permission name must be specified.", parser, null);
        }
    };
    private final String mPermission;

    /* synthetic */ SenderPermissionFilter(String permission, SenderPermissionFilter -this1) {
        this(permission);
    }

    private SenderPermissionFilter(String permission) {
        this.mPermission = permission;
    }

    public boolean matches(IntentFirewall ifw, ComponentName resolvedComponent, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
        return ifw.checkComponentPermission(this.mPermission, callerPid, callerUid, receivingUid, true);
    }
}
