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
                if (equalsValue != null) {
                    throw new XmlPullParserException("Port filter cannot use both equals and range filtering", parser, null);
                }
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
            }
            return new PortFilter(lowerBound, upperBound, null);
        }
    };
    private static final int NO_BOUND = -1;
    private final int mLowerBound;
    private final int mUpperBound;

    /* synthetic */ PortFilter(int lowerBound, int upperBound, PortFilter -this2) {
        this(lowerBound, upperBound);
    }

    private PortFilter(int lowerBound, int upperBound) {
        this.mLowerBound = lowerBound;
        this.mUpperBound = upperBound;
    }

    public boolean matches(IntentFirewall ifw, ComponentName resolvedComponent, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
        int port = -1;
        Uri uri = intent.getData();
        if (uri != null) {
            port = uri.getPort();
        }
        if (port == -1 || (this.mLowerBound != -1 && this.mLowerBound > port)) {
            return false;
        }
        if (this.mUpperBound == -1 || this.mUpperBound >= port) {
            return true;
        }
        return false;
    }
}
