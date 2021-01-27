package ohos.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Sequenceable;

public class PacMap extends BasePacMap {
    public static final PacMap EMPTY_PAC_MAP = new PacMap();
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218119424, "PacMap");
    public static final Sequenceable.Producer<PacMap> PRODUCER = $$Lambda$PacMap$hsZ2Mcd5GFzcmB0sfnSGf8YE2uw.INSTANCE;

    static /* synthetic */ PacMap lambda$static$0(Parcel parcel) {
        PacMap pacMap = new PacMap();
        pacMap.unmarshalling(parcel);
        return pacMap;
    }

    public PacMap(int i) {
        super(i);
    }

    public PacMap() {
        this(0);
    }

    PacMap(boolean z) {
        super(z);
    }

    @Override // ohos.utils.BasePacMap
    public void putAll(Map<String, Object> map) {
        super.putAll(map);
    }

    public void putAll(PacMap pacMap) {
        super.putAll((BasePacMap) pacMap);
    }

    @Override // ohos.utils.BasePacMap
    public Map<String, Object> getAll() {
        return super.getAll();
    }

    @Override // ohos.utils.BasePacMap
    public void putObjectValue(String str, Object obj) {
        super.putObjectValue(str, obj);
    }

    @Override // ohos.utils.BasePacMap
    public void putByteValue(String str, byte b) {
        super.putByteValue(str, b);
    }

    @Override // ohos.utils.BasePacMap
    public void putShortValue(String str, short s) {
        super.putShortValue(str, s);
    }

    @Override // ohos.utils.BasePacMap
    public void putIntValue(String str, int i) {
        super.putIntValue(str, i);
    }

    @Override // ohos.utils.BasePacMap
    public void putLongValue(String str, long j) {
        super.putLongValue(str, j);
    }

    @Override // ohos.utils.BasePacMap
    public void putFloatValue(String str, float f) {
        super.putFloatValue(str, f);
    }

    @Override // ohos.utils.BasePacMap
    public void putDoubleValue(String str, double d) {
        super.putDoubleValue(str, d);
    }

    @Override // ohos.utils.BasePacMap
    public void putBooleanValue(String str, boolean z) {
        super.putBooleanValue(str, z);
    }

    @Override // ohos.utils.BasePacMap
    public void putChar(String str, char c) {
        super.putChar(str, c);
    }

    @Override // ohos.utils.BasePacMap
    public void putString(String str, String str2) {
        super.putString(str, str2);
    }

    @Override // ohos.utils.BasePacMap
    public void putByteValueArray(String str, byte[] bArr) {
        super.putByteValueArray(str, bArr);
    }

    @Override // ohos.utils.BasePacMap
    public void putShortValueArray(String str, short[] sArr) {
        super.putShortValueArray(str, sArr);
    }

    @Override // ohos.utils.BasePacMap
    public void putIntValueArray(String str, int[] iArr) {
        super.putIntValueArray(str, iArr);
    }

    public void putIntegerList(String str, ArrayList<Integer> arrayList) {
        putObjectValue(str, arrayList);
    }

    @Override // ohos.utils.BasePacMap
    public void putLongValueArray(String str, long[] jArr) {
        super.putLongValueArray(str, jArr);
    }

    @Override // ohos.utils.BasePacMap
    public void putFloatValueArray(String str, float[] fArr) {
        super.putFloatValueArray(str, fArr);
    }

    @Override // ohos.utils.BasePacMap
    public void putDoubleValueArray(String str, double[] dArr) {
        super.putDoubleValueArray(str, dArr);
    }

    @Override // ohos.utils.BasePacMap
    public void putBooleanValueArray(String str, boolean[] zArr) {
        super.putBooleanValueArray(str, zArr);
    }

    @Override // ohos.utils.BasePacMap
    public void putCharArray(String str, char[] cArr) {
        super.putCharArray(str, cArr);
    }

    @Override // ohos.utils.BasePacMap
    public void putStringArray(String str, String[] strArr) {
        super.putStringArray(str, strArr);
    }

    public void putStringList(String str, ArrayList<String> arrayList) {
        putObjectValue(str, arrayList);
    }

    public void putPacMap(String str, PacMap pacMap) {
        putObjectValue(str, pacMap);
    }

    public void putSerializableObject(String str, Serializable serializable) {
        putObjectValue(str, serializable);
    }

    public void putSequenceableObject(String str, Sequenceable sequenceable) {
        putObjectValue(str, sequenceable);
    }

    public void putSequenceableObjectArray(String str, Sequenceable[] sequenceableArr) {
        putObjectValue(str, sequenceableArr);
    }

    public void putSequenceableObjectList(String str, ArrayList<Sequenceable> arrayList) {
        putObjectValue(str, arrayList);
    }

    @Override // ohos.utils.BasePacMap
    public Optional<Object> getObjectValue(String str) {
        return super.getObjectValue(str);
    }

    @Override // ohos.utils.BasePacMap
    public byte getByteValue(String str, byte b) {
        return super.getByteValue(str, b);
    }

    @Override // ohos.utils.BasePacMap
    public byte getByteValue(String str) {
        return super.getByteValue(str);
    }

    @Override // ohos.utils.BasePacMap
    public short getShortValue(String str, short s) {
        return super.getShortValue(str, s);
    }

    @Override // ohos.utils.BasePacMap
    public short getShortValue(String str) {
        return super.getShortValue(str);
    }

    @Override // ohos.utils.BasePacMap
    public int getIntValue(String str, int i) {
        return super.getIntValue(str, i);
    }

    @Override // ohos.utils.BasePacMap
    public int getIntValue(String str) {
        return super.getIntValue(str);
    }

    @Override // ohos.utils.BasePacMap
    public long getLongValue(String str, long j) {
        return super.getLongValue(str, j);
    }

    @Override // ohos.utils.BasePacMap
    public long getLongValue(String str) {
        return super.getLongValue(str);
    }

    @Override // ohos.utils.BasePacMap
    public float getFloatValue(String str, float f) {
        return super.getFloatValue(str, f);
    }

    @Override // ohos.utils.BasePacMap
    public float getFloatValue(String str) {
        return super.getFloatValue(str);
    }

    @Override // ohos.utils.BasePacMap
    public double getDoubleValue(String str, double d) {
        return super.getDoubleValue(str, d);
    }

    @Override // ohos.utils.BasePacMap
    public double getDoubleValue(String str) {
        return super.getDoubleValue(str);
    }

    @Override // ohos.utils.BasePacMap
    public boolean getBooleanValue(String str, boolean z) {
        return super.getBooleanValue(str, z);
    }

    @Override // ohos.utils.BasePacMap
    public boolean getBooleanValue(String str) {
        return super.getBooleanValue(str);
    }

    @Override // ohos.utils.BasePacMap
    public char getChar(String str, char c) {
        return super.getChar(str, c);
    }

    @Override // ohos.utils.BasePacMap
    public char getChar(String str) {
        return super.getChar(str);
    }

    @Override // ohos.utils.BasePacMap
    public String getString(String str, String str2) {
        return super.getString(str, str2);
    }

    @Override // ohos.utils.BasePacMap
    public String getString(String str) {
        return super.getString(str);
    }

    @Override // ohos.utils.BasePacMap
    public byte[] getByteValueArray(String str) {
        return super.getByteValueArray(str);
    }

    @Override // ohos.utils.BasePacMap
    public short[] getShortValueArray(String str) {
        return super.getShortValueArray(str);
    }

    @Override // ohos.utils.BasePacMap
    public int[] getIntValueArray(String str) {
        return super.getIntValueArray(str);
    }

    public ArrayList<Integer> getIntegerList(String str) {
        Object orElse = getObjectValue(str).orElse(null);
        if (orElse == null) {
            return null;
        }
        try {
            return (ArrayList) orElse;
        } catch (ClassCastException e) {
            HiLog.error(LABEL, "fail to get string list, error: %{public}s", e.getMessage());
            return null;
        }
    }

    @Override // ohos.utils.BasePacMap
    public long[] getLongValueArray(String str) {
        return super.getLongValueArray(str);
    }

    @Override // ohos.utils.BasePacMap
    public float[] getFloatValueArray(String str) {
        return super.getFloatValueArray(str);
    }

    @Override // ohos.utils.BasePacMap
    public double[] getDoubleValueArray(String str) {
        return super.getDoubleValueArray(str);
    }

    @Override // ohos.utils.BasePacMap
    public boolean[] getBooleanValueArray(String str) {
        return super.getBooleanValueArray(str);
    }

    @Override // ohos.utils.BasePacMap
    public char[] getCharArray(String str) {
        return super.getCharArray(str);
    }

    @Override // ohos.utils.BasePacMap
    public String[] getStringArray(String str) {
        return super.getStringArray(str);
    }

    public ArrayList<String> getStringList(String str) {
        Object orElse = getObjectValue(str).orElse(null);
        if (orElse == null) {
            return null;
        }
        try {
            return (ArrayList) orElse;
        } catch (ClassCastException e) {
            HiLog.error(LABEL, "fail to get string list, error: %{public}s", e.getMessage());
            return null;
        }
    }

    public Optional<PacMap> getPacMap(String str) {
        return getObjectValue(str).filter(new Predicate(PacMap.class) {
            /* class ohos.utils.$$Lambda$PacMap$oYuvetTQXzVnKDAGs7_tOrgG58 */
            private final /* synthetic */ Class f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return this.f$0.isInstance(obj);
            }
        }).map(new Function(PacMap.class) {
            /* class ohos.utils.$$Lambda$PacMap$3ucTokVe72J30tGABY6GSP_Y54 */
            private final /* synthetic */ Class f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return (PacMap) this.f$0.cast(obj);
            }
        });
    }

    public Optional<Serializable> getSerializable(String str) {
        return getObjectValue(str).filter(new Predicate(Serializable.class) {
            /* class ohos.utils.$$Lambda$PacMap$oYuvetTQXzVnKDAGs7_tOrgG58 */
            private final /* synthetic */ Class f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return this.f$0.isInstance(obj);
            }
        }).map(new Function(Serializable.class) {
            /* class ohos.utils.$$Lambda$PacMap$d5j4VrtMiyHDllsG4RquyPRZmNk */
            private final /* synthetic */ Class f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return (Serializable) this.f$0.cast(obj);
            }
        });
    }

    public Optional<Sequenceable> getSequenceable(String str) {
        return getObjectValue(str).filter(new Predicate(Sequenceable.class) {
            /* class ohos.utils.$$Lambda$PacMap$oYuvetTQXzVnKDAGs7_tOrgG58 */
            private final /* synthetic */ Class f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return this.f$0.isInstance(obj);
            }
        }).map(new Function(Sequenceable.class) {
            /* class ohos.utils.$$Lambda$PacMap$akX0Dzp8T3T9Upqq3cJ_ghHWAM */
            private final /* synthetic */ Class f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return (Sequenceable) this.f$0.cast(obj);
            }
        });
    }

    public Sequenceable[] getSequenceableArray(String str) {
        return (Sequenceable[]) getObjectValue(str, Sequenceable[].class).orElse(null);
    }

    public ArrayList<Sequenceable> getSequenceableList(String str) {
        Object orElse = getObjectValue(str).orElse(null);
        if (orElse == null) {
            return null;
        }
        try {
            return (ArrayList) orElse;
        } catch (ClassCastException e) {
            HiLog.error(LABEL, "fail to get sequenceable list, error: %{public}s", e.getMessage());
            return null;
        }
    }

    @Override // ohos.utils.BasePacMap
    public int getSize() {
        return super.getSize();
    }

    @Override // ohos.utils.BasePacMap
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override // ohos.utils.BasePacMap
    public Set<String> getKeys() {
        return super.getKeys();
    }

    @Override // ohos.utils.BasePacMap
    public boolean hasKey(String str) {
        return super.hasKey(str);
    }

    @Override // ohos.utils.BasePacMap
    public void remove(String str) {
        super.remove(str);
    }

    @Override // ohos.utils.BasePacMap
    public void clear() {
        super.clear();
    }

    @Override // ohos.utils.BasePacMap, ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        return super.marshalling(parcel);
    }

    @Override // ohos.utils.BasePacMap, ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        return super.unmarshalling(parcel);
    }

    @Override // ohos.utils.BasePacMap, java.lang.Object
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // ohos.utils.BasePacMap, java.lang.Object
    public int hashCode() {
        return super.hashCode();
    }

    @Override // ohos.utils.BasePacMap, java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public PacMap deepCopy() {
        PacMap pacMap = new PacMap(false);
        pacMap.copyFrom(this, true);
        return pacMap;
    }
}
