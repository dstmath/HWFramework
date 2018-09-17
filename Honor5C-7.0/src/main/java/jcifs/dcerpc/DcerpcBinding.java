package jcifs.dcerpc;

import java.util.HashMap;

public class DcerpcBinding {
    private static HashMap INTERFACES;
    String endpoint;
    int major;
    int minor;
    HashMap options;
    String proto;
    String server;
    UUID uuid;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.dcerpc.DcerpcBinding.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.dcerpc.DcerpcBinding.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: jcifs.dcerpc.DcerpcBinding.<clinit>():void");
    }

    public static void addInterface(String name, String syntax) {
        INTERFACES.put(name, syntax);
    }

    DcerpcBinding(String proto, String server) {
        this.endpoint = null;
        this.options = null;
        this.uuid = null;
        this.proto = proto;
        this.server = server;
    }

    void setOption(String key, Object val) throws DcerpcException {
        if (key.equals("endpoint")) {
            this.endpoint = val.toString().toLowerCase();
            if (this.endpoint.startsWith("\\pipe\\")) {
                String iface = (String) INTERFACES.get(this.endpoint.substring(6));
                if (iface != null) {
                    int c = iface.indexOf(58);
                    int p = iface.indexOf(46, c + 1);
                    this.uuid = new UUID(iface.substring(0, c));
                    this.major = Integer.parseInt(iface.substring(c + 1, p));
                    this.minor = Integer.parseInt(iface.substring(p + 1));
                    return;
                }
            }
            throw new DcerpcException("Bad endpoint: " + this.endpoint);
        }
        if (this.options == null) {
            this.options = new HashMap();
        }
        this.options.put(key, val);
    }

    Object getOption(String key) {
        if (key.equals("endpoint")) {
            return this.endpoint;
        }
        if (this.options != null) {
            return this.options.get(key);
        }
        return null;
    }

    public String toString() {
        String ret = this.proto + ":" + this.server + "[" + this.endpoint;
        if (this.options != null) {
            for (Object key : this.options.keySet()) {
                ret = ret + "," + key + "=" + this.options.get(key);
            }
        }
        return ret + "]";
    }
}
