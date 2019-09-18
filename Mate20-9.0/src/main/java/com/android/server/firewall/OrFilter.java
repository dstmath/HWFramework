package com.android.server.firewall;

import android.content.ComponentName;
import android.content.Intent;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class OrFilter extends FilterList {
    public static final FilterFactory FACTORY = new FilterFactory("or") {
        public Filter newFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
            return new OrFilter().readFromXml(parser);
        }
    };

    OrFilter() {
    }

    public boolean matches(IntentFirewall ifw, ComponentName resolvedComponent, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
        for (int i = 0; i < this.children.size(); i++) {
            if (((Filter) this.children.get(i)).matches(ifw, resolvedComponent, intent, callerUid, callerPid, resolvedType, receivingUid)) {
                return true;
            }
        }
        return false;
    }
}
