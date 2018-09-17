package com.huawei.android.pushagent.datatype.http.server;

import android.text.TextUtils;
import com.huawei.android.pushagent.utils.a.e;
import com.huawei.android.pushagent.utils.b.b;
import java.util.Map;

public class TrsRsp {
    private Map<String, Object> cfgs;

    public TrsRsp(String str) {
        this.cfgs = b.ox(str);
    }

    public boolean isValid() {
        if ("".equals(getServerIP()) || -1 == getServerPort() || getResult() != 0) {
            return false;
        }
        return true;
    }

    public boolean isNotAllowedPush() {
        int result = getResult();
        if (25 == result || 26 == result || 27 == result) {
            return true;
        }
        return false;
    }

    public Map<String, Object> getAll() {
        return this.cfgs;
    }

    public String getAnalyticUrl() {
        return getString("analyticUrl", null);
    }

    public long getWifiMinHeartbeat() {
        return getLong("wifiMinHeartbeat", 1800);
    }

    public long getWifiMaxHeartbeat() {
        return getLong("wifiMaxHeartbeat", 1800);
    }

    public long get3GMinHeartbeat() {
        return getLong("g3MinHeartbeat", 900);
    }

    public long get3GMaxHeartbeat() {
        return getLong("g3MaxHeartbeat", 1800);
    }

    public long getNextConnectTrsInterval() {
        return getLong("nextConnectInterval", 86400) * 1000;
    }

    public String getServerIP() {
        return getString("serverIp", "");
    }

    public int getServerPort() {
        return getInt("serverPort", -1);
    }

    public int getResult() {
        return getInt("result", -1);
    }

    public String getRsaPubKey() {
        return getString("publicKey", "");
    }

    public void encryptRsaPubKey() {
        CharSequence nv = e.nv(getRsaPubKey());
        if (!TextUtils.isEmpty(nv)) {
            setValue("publicKey", nv);
        }
    }

    public String getConnectionId() {
        return getString("connId", "");
    }

    public void encryptConnectionId() {
        CharSequence nv = e.nv(getConnectionId());
        if (!TextUtils.isEmpty(nv)) {
            setValue("connId", nv);
        }
    }

    public String getDeviceId() {
        return getString("pushDeviceId", "");
    }

    public boolean removeDeviceId() {
        return remove("pushDeviceId");
    }

    public void encryptDeviceId() {
        CharSequence nv = e.nv(getDeviceId());
        if (!TextUtils.isEmpty(nv)) {
            setValue("pushDeviceId", nv);
        }
    }

    public String getString(String str, String str2) {
        return String.valueOf(getValue(str, str2));
    }

    public int getInt(String str, int i) {
        Object value = getValue(str, Integer.valueOf(i));
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        }
        if (value instanceof Long) {
            return (int) ((Long) value).longValue();
        }
        return i;
    }

    public long getLong(String str, long j) {
        Object value = getValue(str, Long.valueOf(j));
        if (value instanceof Integer) {
            return (long) ((Integer) value).intValue();
        }
        if (value instanceof Long) {
            return ((Long) value).longValue();
        }
        return j;
    }

    private Object getValue(String str, Object obj) {
        Object obj2 = this.cfgs.get(str);
        if (obj2 == null) {
            return obj;
        }
        return obj2;
    }

    public boolean setValue(String str, Object obj) {
        this.cfgs.put(str, obj);
        return true;
    }

    public boolean remove(String str) {
        for (String str2 : this.cfgs.keySet()) {
            if (str.equals(str2)) {
                this.cfgs.remove(str2);
                return true;
            }
        }
        return false;
    }
}
