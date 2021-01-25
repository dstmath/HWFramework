package jcifs.dcerpc;

import java.util.HashMap;
import jcifs.dcerpc.msrpc.lsarpc;
import jcifs.dcerpc.msrpc.netdfs;
import jcifs.dcerpc.msrpc.samr;
import jcifs.dcerpc.msrpc.srvsvc;

public class DcerpcBinding {
    private static HashMap INTERFACES = new HashMap();
    String endpoint = null;
    int major;
    int minor;
    HashMap options = null;
    String proto;
    String server;
    UUID uuid = null;

    static {
        INTERFACES.put("srvsvc", srvsvc.getSyntax());
        INTERFACES.put("lsarpc", lsarpc.getSyntax());
        INTERFACES.put("samr", samr.getSyntax());
        INTERFACES.put("netdfs", netdfs.getSyntax());
    }

    public static void addInterface(String name, String syntax) {
        INTERFACES.put(name, syntax);
    }

    DcerpcBinding(String proto2, String server2) {
        this.proto = proto2;
        this.server = server2;
    }

    /* access modifiers changed from: package-private */
    public void setOption(String key, Object val) throws DcerpcException {
        String iface;
        if (key.equals("endpoint")) {
            this.endpoint = val.toString().toLowerCase();
            if (!this.endpoint.startsWith("\\pipe\\") || (iface = (String) INTERFACES.get(this.endpoint.substring(6))) == null) {
                throw new DcerpcException("Bad endpoint: " + this.endpoint);
            }
            int c = iface.indexOf(58);
            int p = iface.indexOf(46, c + 1);
            this.uuid = new UUID(iface.substring(0, c));
            this.major = Integer.parseInt(iface.substring(c + 1, p));
            this.minor = Integer.parseInt(iface.substring(p + 1));
            return;
        }
        if (this.options == null) {
            this.options = new HashMap();
        }
        this.options.put(key, val);
    }

    /* access modifiers changed from: package-private */
    public Object getOption(String key) {
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
