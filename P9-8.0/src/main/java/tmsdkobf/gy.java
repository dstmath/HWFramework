package tmsdkobf;

import java.util.HashMap;
import tmsdk.common.TMSDKContext;

public final class gy {
    private static final HashMap<String, gz> pe = new HashMap(5);

    static {
        pe.put("ConfigProvider", new gz(0, new he()));
        pe.put("MeriExtProvider", new gz(0, new hf()));
        pe.put("QQSecureProvider", new gz(1, new ha()));
        pe.put("SpProvider", new gz(0, new jf(TMSDKContext.getApplicaionContext())));
    }

    public static gz ak(String str) {
        return str == null ? null : (gz) pe.get(str);
    }
}
