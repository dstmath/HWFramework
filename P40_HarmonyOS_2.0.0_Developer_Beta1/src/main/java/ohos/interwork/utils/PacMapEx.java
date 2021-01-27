package ohos.interwork.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private ClassLoader classLoader;
    protected final HashMap<String, Object> dataMap;
    private Parcel parcelData;

    public PacMapEx(int i) {
        this.parcelData = null;
        this.classLoader = null;
        if (i > 0) {
            this.dataMap = new HashMap<>(i);
        } else {
            this.dataMap = new HashMap<>(0);
        }
    }

    public PacMapEx() {
        this(0);
    }

    public PacMapEx(PacMapEx pacMapEx, boolean z) {
        this.parcelData = null;
        this.classLoader = null;
        this.classLoader = pacMapEx.classLoader;
        if (pacMapEx.parcelData != null) {
            this.dataMap = new HashMap<>();
            if (z) {
                Parcel create = Parcel.create();
                create.appendFrom(pacMapEx.parcelData);
                create.rewindRead(0);
                pacMapEx.parcelData.rewindRead(0);
                this.parcelData = create;
                return;
            }
            this.parcelData = pacMapEx.parcelData;
            return;
        }
        this.parcelData = null;
        HashMap<String, Object> hashMap = pacMapEx.dataMap;
        if (hashMap == null) {
            this.dataMap = null;
        } else if (z) {
            this.dataMap = new HashMap<>(hashMap.size());
            for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
                this.dataMap.put(entry.getKey(), deepCopyValue(entry.getValue()));
            }
        } else {
            this.dataMap = new HashMap<>(hashMap);
        }
    }

    public void setCustomClassLoader(ClassLoader classLoader2) {
        this.classLoader = classLoader2;
    }

    public ClassLoader getCustomClassLoader() {
        return this.classLoader;
    }

    public int size() {
        unParcel();
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
        unParcel();
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
        } catch (ParcelException e) {
            parcel.rewindWrite(writePosition2);
            parcel.writeInt(-1);
            throw e;
        }
    }

    @Override // ohos.interwork.utils.ParcelableEx
    public void unmarshallingEx(Parcel parcel) {
        if (parcel == null) {
            HiLog.error(LOG_LABEL, "Parcel data is null. can not unmarshalling!", new Object[0]);
            return;
        }
        int readInt = parcel.readInt();
        if (readInt <= 0) {
            this.dataMap.clear();
            return;
        }
        try {
            if (parcel.readInt() == PACMAP_EX_MAGIC) {
                int readPosition = parcel.getReadPosition();
                Parcel create = Parcel.create();
                create.appendFromPartial(parcel, readPosition, readInt);
                create.rewindRead(0);
                this.parcelData = create;
                parcel.rewindRead(addOrThrow(readPosition, readInt));
                return;
            }
            throw new ParcelException("not a valid parcel object.");
        } catch (ParcelException e) {
            HiLog.error(LOG_LABEL, "some thing wrong when unmarshalling, clear this map.", new Object[0]);
            this.dataMap.clear();
            throw e;
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        marshallingEx(parcel);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        unmarshallingEx(parcel);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean hasFileDescriptor() {
        return super.hasFileDescriptor();
    }

    private void unParcel() {
        if (this.parcelData != null) {
            HiLog.debug(LOG_LABEL, "begin unparcel.", new Object[0]);
            ParcelUtilsEx.readMapEx(this.dataMap, this.parcelData, this.classLoader);
            this.parcelData.reclaim();
            this.parcelData = null;
            return;
        }
        HiLog.debug(LOG_LABEL, "no data needed to be parceled.", new Object[0]);
    }

    private static int addOrThrow(int i, int i2) throws ParcelException {
        if (i2 == 0) {
            return i;
        }
        if ((i2 > 0 && i <= Integer.MAX_VALUE - i2) || (i2 < 0 && i >= Integer.MIN_VALUE - i2)) {
            return i + i2;
        }
        throw new ParcelException("Add overflow: " + i + " + " + i2);
    }

    private Object deepCopyValue(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof PacMapEx) {
            return new PacMapEx((PacMapEx) obj, true);
        }
        if (obj instanceof List) {
            return deepCopyArrayList((List) obj);
        }
        if (obj.getClass().isArray()) {
            if (obj instanceof int[]) {
                return ((int[]) obj).clone();
            }
            if (obj instanceof long[]) {
                return ((long[]) obj).clone();
            }
            if (obj instanceof float[]) {
                return ((float[]) obj).clone();
            }
            if (obj instanceof double[]) {
                return ((double[]) obj).clone();
            }
            if (obj instanceof Object[]) {
                return ((Object[]) obj).clone();
            }
            if (obj instanceof byte[]) {
                return ((byte[]) obj).clone();
            }
            if (obj instanceof short[]) {
                return ((short[]) obj).clone();
            }
            if (obj instanceof char[]) {
                return ((char[]) obj).clone();
            }
        }
        return obj;
    }

    private List<?> deepCopyArrayList(List<?> list) {
        int size = list.size();
        ArrayList arrayList = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            arrayList.add(deepCopyValue(list.get(i)));
        }
        return arrayList;
    }
}
