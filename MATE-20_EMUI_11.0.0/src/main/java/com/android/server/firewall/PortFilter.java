package com.android.server.firewall;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class PortFilter implements Filter {
    private static final String ATTR_EQUALS = "equals";
    private static final String ATTR_MAX = "max";
    private static final String ATTR_MIN = "min";
    public static final FilterFactory FACTORY = new FilterFactory("port") {
        /* class com.android.server.firewall.PortFilter.AnonymousClass1 */

        @Override // com.android.server.firewall.FilterFactory
        public Filter newFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
            int lowerBound = -1;
            int upperBound = -1;
            String equalsValue = parser.getAttributeValue(null, PortFilter.ATTR_EQUALS);
            if (equalsValue != null) {
                try {
                    int value = Integer.parseInt(equalsValue);
                    lowerBound = value;
                    upperBound = value;
                } catch (NumberFormatException e) {
                    throw new XmlPullParserException("Invalid port value: " + equalsValue, parser, null);
                }
            }
            String lowerBoundString = parser.getAttributeValue(null, PortFilter.ATTR_MIN);
            String upperBoundString = parser.getAttributeValue(null, PortFilter.ATTR_MAX);
            if (!(lowerBoundString == null && upperBoundString == null)) {
                if (equalsValue == null) {
                    if (lowerBoundString != null) {
                        try {
                            lowerBound = Integer.parseInt(lowerBoundString);
                        } catch (NumberFormatException e2) {
                            throw new XmlPullParserException("Invalid minimum port value: " + lowerBoundString, parser, null);
                        }
                    }
                    if (upperBoundString != null) {
                        try {
                            upperBound = Integer.parseInt(upperBoundString);
                        } catch (NumberFormatException e3) {
                            throw new XmlPullParserException("Invalid maximum port value: " + upperBoundString, parser, null);
                        }
                    }
                } else {
                    throw new XmlPullParserException("Port filter cannot use both equals and range filtering", parser, null);
                }
            }
            return new PortFilter(lowerBound, upperBound);
        }
    };
    private static final int NO_BOUND = -1;
    private final int mLowerBound;
    private final int mUpperBound;

    private PortFilter(int lowerBound, int upperBound) {
        this.mLowerBound = lowerBound;
        this.mUpperBound = upperBound;
    }

    @Override // com.android.server.firewall.Filter
    public boolean matches(IntentFirewall ifw, ComponentName resolvedComponent, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
        int i;
        int i2;
        int port = -1;
        Uri uri = intent.getData();
        if (uri != null) {
            port = uri.getPort();
        }
        return port != -1 && ((i = this.mLowerBound) == -1 || i <= port) && ((i2 = this.mUpperBound) == -1 || i2 >= port);
    }
}
