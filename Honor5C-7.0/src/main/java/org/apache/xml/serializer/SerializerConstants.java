package org.apache.xml.serializer;

interface SerializerConstants {
    public static final String CDATA_CONTINUE = "]]]]><![CDATA[>";
    public static final String CDATA_DELIMITER_CLOSE = "]]>";
    public static final String CDATA_DELIMITER_OPEN = "<![CDATA[";
    public static final String DEFAULT_SAX_SERIALIZER = null;
    public static final String EMPTYSTRING = "";
    public static final String ENTITY_AMP = "&amp;";
    public static final String ENTITY_CRLF = "&#xA;";
    public static final String ENTITY_GT = "&gt;";
    public static final String ENTITY_LT = "&lt;";
    public static final String ENTITY_QUOT = "&quot;";
    public static final String XMLNS_PREFIX = "xmlns";
    public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
    public static final String XMLVERSION10 = "1.0";
    public static final String XMLVERSION11 = "1.1";
    public static final String XML_PREFIX = "xml";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.serializer.SerializerConstants.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.serializer.SerializerConstants.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.serializer.SerializerConstants.<clinit>():void");
    }
}
