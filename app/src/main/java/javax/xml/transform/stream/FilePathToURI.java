package javax.xml.transform.stream;

import java.io.File;
import java.io.UnsupportedEncodingException;
import org.w3c.dom.traversal.NodeFilter;

class FilePathToURI {
    private static char[] gAfterEscaping1;
    private static char[] gAfterEscaping2;
    private static char[] gHexChs;
    private static boolean[] gNeedEscaping;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.xml.transform.stream.FilePathToURI.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.xml.transform.stream.FilePathToURI.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.transform.stream.FilePathToURI.<clinit>():void");
    }

    FilePathToURI() {
    }

    public static String filepath2URI(String path) {
        if (path == null) {
            return null;
        }
        int ch;
        path = path.replace(File.separatorChar, '/');
        int len = path.length();
        StringBuilder buffer = new StringBuilder(len * 3);
        buffer.append("file://");
        if (len >= 2 && path.charAt(1) == ':') {
            ch = Character.toUpperCase(path.charAt(0));
            if (ch >= 65 && ch <= 90) {
                buffer.append('/');
            }
        }
        int i = 0;
        while (i < len) {
            ch = path.charAt(i);
            if (ch >= NodeFilter.SHOW_COMMENT) {
                break;
            }
            if (gNeedEscaping[ch]) {
                buffer.append('%');
                buffer.append(gAfterEscaping1[ch]);
                buffer.append(gAfterEscaping2[ch]);
            } else {
                buffer.append((char) ch);
            }
            i++;
        }
        if (i < len) {
            try {
                for (byte b : path.substring(i).getBytes("UTF-8")) {
                    if (b < null) {
                        ch = b + NodeFilter.SHOW_DOCUMENT;
                        buffer.append('%');
                        buffer.append(gHexChs[ch >> 4]);
                        buffer.append(gHexChs[ch & 15]);
                    } else if (gNeedEscaping[b]) {
                        buffer.append('%');
                        buffer.append(gAfterEscaping1[b]);
                        buffer.append(gAfterEscaping2[b]);
                    } else {
                        buffer.append((char) b);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                return path;
            }
        }
        return buffer.toString();
    }
}
