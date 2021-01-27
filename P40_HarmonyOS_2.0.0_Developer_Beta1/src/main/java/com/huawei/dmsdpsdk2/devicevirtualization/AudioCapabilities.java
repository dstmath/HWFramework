package com.huawei.dmsdpsdk2.devicevirtualization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioCapabilities {
    private Map<String, String> extendInfo = new HashMap();
    private List<AudioProfile> profiles = new ArrayList();

    private String mapToString(Map<String, String> map) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            buffer.append('\"');
            buffer.append(entry.getKey());
            buffer.append("\":\"");
            buffer.append(entry.getValue());
            buffer.append("\",");
        }
        buffer.append('}');
        return buffer.toString();
    }

    public List<AudioProfile> getProfiles() {
        return this.profiles;
    }

    public void setProfiles(List<AudioProfile> profiles2) {
        this.profiles = profiles2;
    }

    public Map<String, String> getExtendInfo() {
        return this.extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo2) {
        this.extendInfo = extendInfo2;
    }

    public String toString() {
        return "{\"profiles\":" + this.profiles.toString() + ",\"extendInfo\":" + mapToString(this.extendInfo) + "}";
    }
}
