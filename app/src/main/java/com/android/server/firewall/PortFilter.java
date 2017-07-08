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
    public static final FilterFactory FACTORY = null;
    private static final int NO_BOUND = -1;
    private final int mLowerBound;
    private final int mUpperBound;

    /* renamed from: com.android.server.firewall.PortFilter.1 */
    static class AnonymousClass1 extends FilterFactory {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public Filter newFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
            int lowerBound = PortFilter.NO_BOUND;
            int upperBound = PortFilter.NO_BOUND;
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
            return new PortFilter(upperBound, null);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.firewall.PortFilter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.firewall.PortFilter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.firewall.PortFilter.<clinit>():void");
    }

    private PortFilter(int lowerBound, int upperBound) {
        this.mLowerBound = lowerBound;
        this.mUpperBound = upperBound;
    }

    public boolean matches(IntentFirewall ifw, ComponentName resolvedComponent, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
        int port = NO_BOUND;
        Uri uri = intent.getData();
        if (uri != null) {
            port = uri.getPort();
        }
        if (port == NO_BOUND || (this.mLowerBound != NO_BOUND && this.mLowerBound > port)) {
            return false;
        }
        if (this.mUpperBound == NO_BOUND || this.mUpperBound >= port) {
            return true;
        }
        return false;
    }
}
