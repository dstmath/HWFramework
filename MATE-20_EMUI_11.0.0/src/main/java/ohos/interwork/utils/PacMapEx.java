package ohos.interwork.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.ParcelException;
import ohos.utils.Sequenceable;

public class PacMapEx implements ParcelableEx, Sequenceable {
    private static final int IN_VALID = -1;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218119424, LOG_TAG);
    private static final String LOG_TAG = "PacMapEx";
    private static final int PACMAP_EX_MAGIC = 1279544898;
    protected final HashMap<String, Object> dataMap;

    public PacMapEx(int i) {
        if (i > 0) {
            this.dataMap = new HashMap<>(i);
        } else {
            this.dataMap = new HashMap<>(0);
        }
    }

    public PacMapEx() {
        this(0);
    }

    public int size() {
        return this.dataMap.size();
    }

    public void putAll(Map<String, Object> map) {
        if (map != null) {
            this.dataMap.putAll(map);
        }
    }

    public void putObjectValue(String str, Object obj) {
        this.dataMap.put(str, obj);
    }

    public Optional<Object> getObjectValue(String str) {
        return Optional.ofNullable(this.dataMap.get(str));
    }

    @Override // ohos.interwork.utils.ParcelableEx
    public void marshallingEx(Parcel parcel) {
        if (this.dataMap.isEmpty()) {
            parcel.writeInt(0);
            return;
        }
        int writePosition = parcel.getWritePosition();
        parcel.writeInt(-1);
        parcel.writeInt(PACMAP_EX_MAGIC);
        int writePosition2 = parcel.getWritePosition();
        try {
            ParcelUtilsEx.writeMapEx(this.dataMap, parcel);
            int writePosition3 = parcel.getWritePosition();
            parcel.rewindWrite(writePosition);
            parcel.writeInt(writePosition3 - writePosition2);
            parcel.setSize(writePosition3);
            parcel.rewindWrite(writePosition3);
        } catch (ParcelException unused) {
            parcel.rewindWrite(writePosition2);
            parcel.writeInt(-1);
        }
    }

    @Override // ohos.interwork.utils.ParcelableEx
    public void unmarshallingEx(Parcel parcel) {
        if (parcel.readInt() <= 0) {
            this.dataMap.clear();
            return;
        }
        try {
            if (parcel.readInt() == PACMAP_EX_MAGIC) {
                ParcelUtilsEx.readMapEx(this.dataMap, parcel);
                return;
            }
            throw new ParcelException("not a valid parcel object.");
        } catch (ParcelException unused) {
            HiLog.error(LOG_LABEL, "some thing wrong when unmarshalling, clear this map.", new Object[0]);
            this.dataMap.clear();
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        try {
            marshallingEx(parcel);
            return true;
        } catch (ParcelException unused) {
            return false;
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        try {
            unmarshallingEx(parcel);
            return true;
        } catch (ParcelException unused) {
            return false;
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean hasFileDescriptor() {
        return super.hasFileDescriptor();
    }
}
