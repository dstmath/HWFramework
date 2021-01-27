package ohos.ai.engine.bigreport;

import java.util.LinkedHashMap;
import java.util.Map;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class OriginInfo implements Sequenceable {
    private LinkedHashMap<String, String> originInfos;

    public OriginInfo(LinkedHashMap<String, String> linkedHashMap) {
        this.originInfos = linkedHashMap;
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.originInfos.size());
        for (Map.Entry<String, String> entry : this.originInfos.entrySet()) {
            parcel.writeString(entry.getKey());
            parcel.writeString(entry.getValue());
        }
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        int readInt = parcel.readInt();
        for (int i = 0; i < readInt; i++) {
            this.originInfos.put(parcel.readString(), parcel.readString());
        }
        return true;
    }

    public LinkedHashMap<String, String> getOriginInfo() {
        return this.originInfos;
    }

    public void setOriginInfo(LinkedHashMap<String, String> linkedHashMap) {
        this.originInfos = linkedHashMap;
    }
}
