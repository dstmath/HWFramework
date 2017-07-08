package org.apache.xpath.objects;

import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.XMLString;
import org.apache.xml.utils.XMLStringFactory;

public class XMLStringFactoryImpl extends XMLStringFactory {
    private static XMLStringFactory m_xstringfactory;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xpath.objects.XMLStringFactoryImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xpath.objects.XMLStringFactoryImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xpath.objects.XMLStringFactoryImpl.<clinit>():void");
    }

    public static XMLStringFactory getFactory() {
        return m_xstringfactory;
    }

    public XMLString newstr(String string) {
        return new XString(string);
    }

    public XMLString newstr(FastStringBuffer fsb, int start, int length) {
        return new XStringForFSB(fsb, start, length);
    }

    public XMLString newstr(char[] string, int start, int length) {
        return new XStringForChars(string, start, length);
    }

    public XMLString emptystr() {
        return XString.EMPTYSTRING;
    }
}
