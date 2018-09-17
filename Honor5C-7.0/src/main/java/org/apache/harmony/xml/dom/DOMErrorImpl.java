package org.apache.harmony.xml.dom;

import org.w3c.dom.DOMError;
import org.w3c.dom.DOMLocator;

public final class DOMErrorImpl implements DOMError {
    private static final DOMLocator NULL_DOM_LOCATOR = null;
    private final short severity;
    private final String type;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.harmony.xml.dom.DOMErrorImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.harmony.xml.dom.DOMErrorImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMErrorImpl.<clinit>():void");
    }

    public DOMErrorImpl(short severity, String type) {
        this.severity = severity;
        this.type = type;
    }

    public short getSeverity() {
        return this.severity;
    }

    public String getMessage() {
        return this.type;
    }

    public String getType() {
        return this.type;
    }

    public Object getRelatedException() {
        return null;
    }

    public Object getRelatedData() {
        return null;
    }

    public DOMLocator getLocation() {
        return NULL_DOM_LOCATOR;
    }
}
