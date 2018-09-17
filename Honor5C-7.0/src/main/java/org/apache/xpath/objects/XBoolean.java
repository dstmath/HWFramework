package org.apache.xpath.objects;

import javax.xml.transform.TransformerException;
import org.apache.xml.utils.Constants;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xpath.compiler.Keywords;

public class XBoolean extends XObject {
    public static final XBoolean S_FALSE = null;
    public static final XBoolean S_TRUE = null;
    static final long serialVersionUID = -2964933058866100881L;
    private final boolean m_val;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xpath.objects.XBoolean.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xpath.objects.XBoolean.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xpath.objects.XBoolean.<clinit>():void");
    }

    public XBoolean(boolean b) {
        this.m_val = b;
    }

    public XBoolean(Boolean b) {
        this.m_val = b.booleanValue();
        setObject(b);
    }

    public int getType() {
        return 1;
    }

    public String getTypeString() {
        return "#BOOLEAN";
    }

    public double num() {
        return this.m_val ? Constants.XSLTVERSUPPORTED : 0.0d;
    }

    public boolean bool() {
        return this.m_val;
    }

    public String str() {
        return this.m_val ? Keywords.FUNC_TRUE_STRING : Keywords.FUNC_FALSE_STRING;
    }

    public Object object() {
        if (this.m_obj == null) {
            setObject(new Boolean(this.m_val));
        }
        return this.m_obj;
    }

    public boolean equals(XObject obj2) {
        if (obj2.getType() == 4) {
            return obj2.equals(this);
        }
        try {
            return this.m_val == obj2.bool();
        } catch (TransformerException te) {
            throw new WrappedRuntimeException(te);
        }
    }
}
