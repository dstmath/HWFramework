package com.android.server.firewall;

import android.app.AppGlobals;
import android.os.Process;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.am.ProcessList;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class SenderFilter {
    private static final String ATTR_TYPE = "type";
    public static final FilterFactory FACTORY = null;
    private static final Filter SIGNATURE = null;
    private static final Filter SYSTEM = null;
    private static final Filter SYSTEM_OR_SIGNATURE = null;
    private static final Filter USER_ID = null;
    private static final String VAL_SIGNATURE = "signature";
    private static final String VAL_SYSTEM = "system";
    private static final String VAL_SYSTEM_OR_SIGNATURE = "system|signature";
    private static final String VAL_USER_ID = "userId";

    /* renamed from: com.android.server.firewall.SenderFilter.1 */
    static class AnonymousClass1 extends FilterFactory {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public Filter newFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
            String typeString = parser.getAttributeValue(null, SenderFilter.ATTR_TYPE);
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
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.firewall.SenderFilter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.firewall.SenderFilter.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.firewall.SenderFilter.<clinit>():void");
    }

    SenderFilter() {
    }

    static boolean isPrivilegedApp(int callerUid, int callerPid) {
        boolean z = true;
        if (callerUid == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || callerUid == 0 || callerPid == Process.myPid() || callerPid == 0) {
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
