package com.android.server.firewall;

import android.content.ComponentName;
import android.content.Intent;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class NotFilter implements Filter {
    public static final FilterFactory FACTORY = null;
    private final Filter mChild;

    /* renamed from: com.android.server.firewall.NotFilter.1 */
    static class AnonymousClass1 extends FilterFactory {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public Filter newFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
            Filter child = null;
            int outerDepth = parser.getDepth();
            while (XmlUtils.nextElementWithin(parser, outerDepth)) {
                Filter filter = IntentFirewall.parseFilter(parser);
                if (child == null) {
                    child = filter;
                } else {
                    throw new XmlPullParserException("<not> tag can only contain a single child filter.", parser, null);
                }
            }
            return new NotFilter(null);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.firewall.NotFilter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.firewall.NotFilter.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.firewall.NotFilter.<clinit>():void");
    }

    private NotFilter(Filter child) {
        this.mChild = child;
    }

    public boolean matches(IntentFirewall ifw, ComponentName resolvedComponent, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
        return !this.mChild.matches(ifw, resolvedComponent, intent, callerUid, callerPid, resolvedType, receivingUid);
    }
}
