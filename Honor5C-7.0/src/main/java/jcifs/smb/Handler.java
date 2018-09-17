package jcifs.smb;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {
    static final URLStreamHandler SMB_HANDLER = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.smb.Handler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.smb.Handler.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: jcifs.smb.Handler.<clinit>():void");
    }

    protected int getDefaultPort() {
        return SmbConstants.DEFAULT_PORT;
    }

    public URLConnection openConnection(URL u) throws IOException {
        return new SmbFile(u);
    }

    protected void parseURL(URL u, String spec, int start, int limit) {
        String host = u.getHost();
        if (spec.equals("smb://")) {
            spec = "smb:////";
            limit += 2;
        } else if (!(spec.startsWith("smb://") || host == null || host.length() != 0)) {
            spec = "//" + spec;
            limit += 2;
        }
        super.parseURL(u, spec, start, limit);
        String path = u.getPath();
        String ref = u.getRef();
        if (ref != null) {
            path = path + '#' + ref;
        }
        int port = u.getPort();
        if (port == -1) {
            port = getDefaultPort();
        }
        setURL(u, "smb", u.getHost(), port, u.getAuthority(), u.getUserInfo(), path, u.getQuery(), null);
    }
}
