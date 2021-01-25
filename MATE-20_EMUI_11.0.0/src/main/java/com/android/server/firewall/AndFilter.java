package com.android.server.firewall;

import android.content.ComponentName;
import android.content.Intent;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* access modifiers changed from: package-private */
public class AndFilter extends FilterList {
    public static final FilterFactory FACTORY = new FilterFactory("and") {
        /* class com.android.server.firewall.AndFilter.AnonymousClass1 */

        @Override // com.android.server.firewall.FilterFactory
        public Filter newFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
            return new AndFilter().readFromXml(parser);
        }
    };

    AndFilter() {
    }

    @Override // com.android.server.firewall.Filter
    public boolean matches(IntentFirewall ifw, ComponentName resolvedComponent, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
        for (int i = 0; i < this.children.size(); i++) {
            if (!((Filter) this.children.get(i)).matches(ifw, resolvedComponent, intent, callerUid, callerPid, resolvedType, receivingUid)) {
                return false;
            }
        }
        return true;
    }
}
