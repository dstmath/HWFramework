package com.android.server.firewall;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Intent;
import android.os.RemoteException;
import android.os.UserHandle;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SenderPackageFilter implements Filter {
    private static final String ATTR_NAME = "name";
    public static final FilterFactory FACTORY = new FilterFactory("sender-package") {
        public Filter newFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
            String packageName = parser.getAttributeValue(null, SenderPackageFilter.ATTR_NAME);
            if (packageName != null) {
                return new SenderPackageFilter(packageName);
            }
            throw new XmlPullParserException("A package name must be specified.", parser, null);
        }
    };
    public final String mPackageName;

    public SenderPackageFilter(String packageName) {
        this.mPackageName = packageName;
    }

    public boolean matches(IntentFirewall ifw, ComponentName resolvedComponent, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
        int packageUid = -1;
        try {
            packageUid = AppGlobals.getPackageManager().getPackageUid(this.mPackageName, DumpState.DUMP_CHANGES, 0);
        } catch (RemoteException e) {
        }
        if (packageUid == -1) {
            return false;
        }
        return UserHandle.isSameApp(packageUid, callerUid);
    }
}
