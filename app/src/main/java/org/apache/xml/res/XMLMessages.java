package org.apache.xml.res;

import java.text.MessageFormat;
import java.util.ListResourceBundle;
import java.util.Locale;

public class XMLMessages {
    protected static final String BAD_CODE = "BAD_CODE";
    protected static final String FORMAT_FAILED = "FORMAT_FAILED";
    private static ListResourceBundle XMLBundle;
    protected Locale fLocale;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.res.XMLMessages.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.res.XMLMessages.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.res.XMLMessages.<clinit>():void");
    }

    public XMLMessages() {
        this.fLocale = Locale.getDefault();
    }

    public void setLocale(Locale locale) {
        this.fLocale = locale;
    }

    public Locale getLocale() {
        return this.fLocale;
    }

    public static final String createXMLMessage(String msgKey, Object[] args) {
        return createMsg(XMLBundle, msgKey, args);
    }

    public static final String createMsg(ListResourceBundle fResourceBundle, String msgKey, Object[] args) {
        String fmsg;
        boolean throwex = false;
        String msg = null;
        if (msgKey != null) {
            msg = fResourceBundle.getString(msgKey);
        }
        if (msg == null) {
            msg = fResourceBundle.getString(BAD_CODE);
            throwex = true;
        }
        if (args != null) {
            try {
                int n = args.length;
                for (int i = 0; i < n; i++) {
                    if (args[i] == null) {
                        args[i] = SerializerConstants.EMPTYSTRING;
                    }
                }
                fmsg = MessageFormat.format(msg, args);
            } catch (Exception e) {
                fmsg = fResourceBundle.getString(FORMAT_FAILED) + " " + msg;
            }
        } else {
            fmsg = msg;
        }
        if (!throwex) {
            return fmsg;
        }
        throw new RuntimeException(fmsg);
    }
}
