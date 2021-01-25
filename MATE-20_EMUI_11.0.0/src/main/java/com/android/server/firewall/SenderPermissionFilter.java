package com.android.server.firewall;

import android.content.ComponentName;
import android.content.Intent;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class SenderPermissionFilter implements Filter {
    private static final String ATTR_NAME = "name";
    public static final FilterFactory FACTORY = new FilterFactory("sender-permission") {
        /* class com.android.server.firewall.SenderPermissionFilter.AnonymousClass1 */

        @Override // com.android.server.firewall.FilterFactory
        public Filter newFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
            String permission = parser.getAttributeValue(null, "name");
            if (permission != null) {
                return new SenderPermissionFilter(permission);
            }
            throw new XmlPullParserException("Permission name must be specified.", parser, null);
        }
    };
    private final String mPermission;

    private SenderPermissionFilter(String permission) {
        this.mPermission = permission;
    }

    @Override // com.android.server.firewall.Filter
    public boolean matches(IntentFirewall ifw, ComponentName resolvedComponent, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
        return ifw.checkComponentPermission(this.mPermission, callerPid, callerUid, receivingUid, true);
    }
}
