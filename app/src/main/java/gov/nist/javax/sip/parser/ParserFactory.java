package gov.nist.javax.sip.parser;

import gov.nist.core.InternalErrorHandler;
import gov.nist.javax.sip.header.SIPHeaderNamesCache;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.Hashtable;

public class ParserFactory {
    private static Class[] constructorArgs;
    private static Hashtable parserConstructorCache;
    private static Hashtable<String, Class<? extends HeaderParser>> parserTable;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: gov.nist.javax.sip.parser.ParserFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: gov.nist.javax.sip.parser.ParserFactory.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.parser.ParserFactory.<clinit>():void");
    }

    public static HeaderParser createParser(String line) throws ParseException {
        String headerName = Lexer.getHeaderName(line);
        String headerValue = Lexer.getHeaderValue(line);
        if (headerName == null || headerValue == null) {
            throw new ParseException("The header name or value is null", 0);
        }
        Class parserClass = (Class) parserTable.get(SIPHeaderNamesCache.toLowerCase(headerName));
        if (parserClass == null) {
            return new HeaderParser(line);
        }
        try {
            Constructor cons = (Constructor) parserConstructorCache.get(parserClass);
            if (cons == null) {
                cons = parserClass.getConstructor(constructorArgs);
                parserConstructorCache.put(parserClass, cons);
            }
            return (HeaderParser) cons.newInstance(new Object[]{line});
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
            return null;
        }
    }
}
