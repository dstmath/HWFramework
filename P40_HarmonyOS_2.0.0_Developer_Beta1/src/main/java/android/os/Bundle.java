package android.os;

import android.annotation.UnsupportedAppUsage;
import android.net.TrafficStats;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.Size;
import android.util.SizeF;
import android.util.SparseArray;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Bundle extends BaseBundle implements Cloneable, Parcelable {
    public static final Parcelable.Creator<Bundle> CREATOR = new Parcelable.Creator<Bundle>() {
        /* class android.os.Bundle.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Bundle createFromParcel(Parcel in) {
            return in.readBundle();
        }

        @Override // android.os.Parcelable.Creator
        public Bundle[] newArray(int size) {
            return new Bundle[size];
        }
    };
    public static final Bundle EMPTY = new Bundle();
    @VisibleForTesting
    static final int FLAG_ALLOW_FDS = 1024;
    @VisibleForTesting
    static final int FLAG_HAS_FDS = 256;
    @VisibleForTesting
    static final int FLAG_HAS_FDS_KNOWN = 512;
    public static final Bundle STRIPPED = new Bundle();

    static {
        EMPTY.mMap = ArrayMap.EMPTY;
        STRIPPED.putInt("STRIPPED", 1);
    }

    public Bundle() {
        this.mFlags = 1536;
    }

    @VisibleForTesting
    public Bundle(Parcel parcelledData) {
        super(parcelledData);
        this.mFlags = 1024;
        maybePrefillHasFds();
    }

    @VisibleForTesting
    public Bundle(Parcel parcelledData, int length) {
        super(parcelledData, length);
        this.mFlags = 1024;
        maybePrefillHasFds();
    }

    private void maybePrefillHasFds() {
        if (this.mParcelledData == null) {
            return;
        }
        if (this.mParcelledData.hasFileDescriptors()) {
            this.mFlags |= 768;
        } else {
            this.mFlags |= 512;
        }
    }

    public Bundle(ClassLoader loader) {
        super(loader);
        this.mFlags = 1536;
    }

    public Bundle(int capacity) {
        super(capacity);
        this.mFlags = 1536;
    }

    public Bundle(Bundle b) {
        super(b);
        this.mFlags = b.mFlags;
    }

    public Bundle(PersistableBundle b) {
        super(b);
        this.mFlags = 1536;
    }

    Bundle(boolean doInit) {
        super(doInit);
    }

    @UnsupportedAppUsage
    public static Bundle forPair(String key, String value) {
        Bundle b = new Bundle(1);
        b.putString(key, value);
        return b;
    }

    @Override // android.os.BaseBundle
    public void setClassLoader(ClassLoader loader) {
        super.setClassLoader(loader);
    }

    @Override // android.os.BaseBundle
    public ClassLoader getClassLoader() {
        return super.getClassLoader();
    }

    public boolean setAllowFds(boolean allowFds) {
        boolean orig = (this.mFlags & 1024) != 0;
        if (allowFds) {
            this.mFlags |= 1024;
        } else {
            this.mFlags &= -1025;
        }
        return orig;
    }

    public void setDefusable(boolean defusable) {
        if (defusable) {
            this.mFlags |= 1;
        } else {
            this.mFlags &= -2;
        }
    }

    @UnsupportedAppUsage
    public static Bundle setDefusable(Bundle bundle, boolean defusable) {
        if (bundle != null) {
            bundle.setDefusable(defusable);
        }
        return bundle;
    }

    @Override // java.lang.Object
    public Object clone() {
        return new Bundle(this);
    }

    public Bundle deepCopy() {
        Bundle b = new Bundle(false);
        b.copyInternal(this, true);
        return b;
    }

    @Override // android.os.BaseBundle
    public void clear() {
        super.clear();
        this.mFlags = 1536;
    }

    @Override // android.os.BaseBundle
    public void remove(String key) {
        super.remove(key);
        if ((this.mFlags & 256) != 0) {
            this.mFlags &= -513;
        }
    }

    public void putAll(Bundle bundle) {
        unparcel();
        bundle.unparcel();
        this.mMap.putAll(bundle.mMap);
        if ((bundle.mFlags & 256) != 0) {
            this.mFlags |= 256;
        }
        if ((bundle.mFlags & 512) == 0) {
            this.mFlags &= -513;
        }
    }

    @UnsupportedAppUsage
    public int getSize() {
        if (this.mParcelledData != null) {
            return this.mParcelledData.dataSize();
        }
        return 0;
    }

    public boolean hasFileDescriptors() {
        if ((this.mFlags & 512) == 0) {
            boolean fdFound = false;
            if (this.mParcelledData == null) {
                int i = this.mMap.size() - 1;
                while (true) {
                    if (i < 0) {
                        break;
                    }
                    Object obj = this.mMap.valueAt(i);
                    if (obj instanceof Parcelable) {
                        if ((((Parcelable) obj).describeContents() & 1) != 0) {
                            fdFound = true;
                            break;
                        }
                    } else if (obj instanceof Parcelable[]) {
                        Parcelable[] array = (Parcelable[]) obj;
                        int n = array.length - 1;
                        while (true) {
                            if (n >= 0) {
                                Parcelable p = array[n];
                                if (p != null && (p.describeContents() & 1) != 0) {
                                    fdFound = true;
                                    break;
                                }
                                n--;
                            } else {
                                break;
                            }
                        }
                    } else if (obj instanceof SparseArray) {
                        SparseArray<? extends Parcelable> array2 = (SparseArray) obj;
                        int n2 = array2.size() - 1;
                        while (true) {
                            if (n2 >= 0) {
                                Parcelable p2 = (Parcelable) array2.valueAt(n2);
                                if (p2 != null && (p2.describeContents() & 1) != 0) {
                                    fdFound = true;
                                    break;
                                }
                                n2--;
                            } else {
                                break;
                            }
                        }
                    } else if (obj instanceof ArrayList) {
                        ArrayList array3 = (ArrayList) obj;
                        if (!array3.isEmpty() && (array3.get(0) instanceof Parcelable)) {
                            int n3 = array3.size() - 1;
                            while (true) {
                                if (n3 >= 0) {
                                    Parcelable p3 = (Parcelable) array3.get(n3);
                                    if (p3 != null && (p3.describeContents() & 1) != 0) {
                                        fdFound = true;
                                        break;
                                    }
                                    n3--;
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                    i--;
                }
            } else if (this.mParcelledData.hasFileDescriptors()) {
                fdFound = true;
            }
            if (fdFound) {
                this.mFlags |= 256;
            } else {
                this.mFlags &= TrafficStats.TAG_NETWORK_STACK_RANGE_END;
            }
            this.mFlags |= 512;
        }
        return (this.mFlags & 256) != 0;
    }

    @UnsupportedAppUsage
    public Bundle filterValues() {
        unparcel();
        Bundle bundle = this;
        if (this.mMap != null) {
            ArrayMap<String, Object> map = this.mMap;
            for (int i = map.size() - 1; i >= 0; i--) {
                Object value = map.valueAt(i);
                if (!PersistableBundle.isValidType(value)) {
                    if (value instanceof Bundle) {
                        Bundle newBundle = ((Bundle) value).filterValues();
                        if (newBundle != value) {
                            if (map == this.mMap) {
                                bundle = new Bundle(this);
                                map = bundle.mMap;
                            }
                            map.setValueAt(i, newBundle);
                        }
                    } else if (!value.getClass().getName().startsWith("android.")) {
                        if (map == this.mMap) {
                            bundle = new Bundle(this);
                            map = bundle.mMap;
                        }
                        map.removeAt(i);
                    }
                }
            }
        }
        this.mFlags |= 512;
        this.mFlags &= TrafficStats.TAG_NETWORK_STACK_RANGE_END;
        return bundle;
    }

    @Override // android.os.BaseBundle
    public void putByte(String key, byte value) {
        super.putByte(key, value);
    }

    @Override // android.os.BaseBundle
    public void putChar(String key, char value) {
        super.putChar(key, value);
    }

    @Override // android.os.BaseBundle
    public void putShort(String key, short value) {
        super.putShort(key, value);
    }

    @Override // android.os.BaseBundle
    public void putFloat(String key, float value) {
        super.putFloat(key, value);
    }

    @Override // android.os.BaseBundle
    public void putCharSequence(String key, CharSequence value) {
        super.putCharSequence(key, value);
    }

    public void putParcelable(String key, Parcelable value) {
        unparcel();
        this.mMap.put(key, value);
        this.mFlags &= -513;
    }

    public void putSize(String key, Size value) {
        unparcel();
        this.mMap.put(key, value);
    }

    public void putSizeF(String key, SizeF value) {
        unparcel();
        this.mMap.put(key, value);
    }

    public void putParcelableArray(String key, Parcelable[] value) {
        unparcel();
        this.mMap.put(key, value);
        this.mFlags &= -513;
    }

    public void putParcelableArrayList(String key, ArrayList<? extends Parcelable> value) {
        unparcel();
        this.mMap.put(key, value);
        this.mFlags &= -513;
    }

    @UnsupportedAppUsage
    public void putParcelableList(String key, List<? extends Parcelable> value) {
        unparcel();
        this.mMap.put(key, value);
        this.mFlags &= -513;
    }

    public void putSparseParcelableArray(String key, SparseArray<? extends Parcelable> value) {
        unparcel();
        this.mMap.put(key, value);
        this.mFlags &= -513;
    }

    @Override // android.os.BaseBundle
    public void putIntegerArrayList(String key, ArrayList<Integer> value) {
        super.putIntegerArrayList(key, value);
    }

    @Override // android.os.BaseBundle
    public void putStringArrayList(String key, ArrayList<String> value) {
        super.putStringArrayList(key, value);
    }

    @Override // android.os.BaseBundle
    public void putCharSequenceArrayList(String key, ArrayList<CharSequence> value) {
        super.putCharSequenceArrayList(key, value);
    }

    @Override // android.os.BaseBundle
    public void putSerializable(String key, Serializable value) {
        super.putSerializable(key, value);
    }

    @Override // android.os.BaseBundle
    public void putByteArray(String key, byte[] value) {
        super.putByteArray(key, value);
    }

    @Override // android.os.BaseBundle
    public void putShortArray(String key, short[] value) {
        super.putShortArray(key, value);
    }

    @Override // android.os.BaseBundle
    public void putCharArray(String key, char[] value) {
        super.putCharArray(key, value);
    }

    @Override // android.os.BaseBundle
    public void putFloatArray(String key, float[] value) {
        super.putFloatArray(key, value);
    }

    @Override // android.os.BaseBundle
    public void putCharSequenceArray(String key, CharSequence[] value) {
        super.putCharSequenceArray(key, value);
    }

    public void putBundle(String key, Bundle value) {
        unparcel();
        this.mMap.put(key, value);
    }

    public void putBinder(String key, IBinder value) {
        unparcel();
        this.mMap.put(key, value);
    }

    @UnsupportedAppUsage
    @Deprecated
    public void putIBinder(String key, IBinder value) {
        unparcel();
        this.mMap.put(key, value);
    }

    @Override // android.os.BaseBundle
    public byte getByte(String key) {
        return super.getByte(key);
    }

    @Override // android.os.BaseBundle
    public Byte getByte(String key, byte defaultValue) {
        return super.getByte(key, defaultValue);
    }

    @Override // android.os.BaseBundle
    public char getChar(String key) {
        return super.getChar(key);
    }

    @Override // android.os.BaseBundle
    public char getChar(String key, char defaultValue) {
        return super.getChar(key, defaultValue);
    }

    @Override // android.os.BaseBundle
    public short getShort(String key) {
        return super.getShort(key);
    }

    @Override // android.os.BaseBundle
    public short getShort(String key, short defaultValue) {
        return super.getShort(key, defaultValue);
    }

    @Override // android.os.BaseBundle
    public float getFloat(String key) {
        return super.getFloat(key);
    }

    @Override // android.os.BaseBundle
    public float getFloat(String key, float defaultValue) {
        return super.getFloat(key, defaultValue);
    }

    @Override // android.os.BaseBundle
    public CharSequence getCharSequence(String key) {
        return super.getCharSequence(key);
    }

    @Override // android.os.BaseBundle
    public CharSequence getCharSequence(String key, CharSequence defaultValue) {
        return super.getCharSequence(key, defaultValue);
    }

    public Size getSize(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        try {
            return (Size) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Size", e);
            return null;
        }
    }

    public SizeF getSizeF(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        try {
            return (SizeF) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "SizeF", e);
            return null;
        }
    }

    public Bundle getBundle(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (Bundle) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Bundle", e);
            return null;
        }
    }

    public <T extends Parcelable> T getParcelable(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (T) ((Parcelable) o);
        } catch (ClassCastException e) {
            typeWarning(key, o, "Parcelable", e);
            return null;
        }
    }

    public Parcelable[] getParcelableArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (Parcelable[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Parcelable[]", e);
            return null;
        }
    }

    public <T extends Parcelable> ArrayList<T> getParcelableArrayList(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (ArrayList) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList", e);
            return null;
        }
    }

    public <T extends Parcelable> SparseArray<T> getSparseParcelableArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (SparseArray) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "SparseArray", e);
            return null;
        }
    }

    @Override // android.os.BaseBundle
    public Serializable getSerializable(String key) {
        return super.getSerializable(key);
    }

    @Override // android.os.BaseBundle
    public ArrayList<Integer> getIntegerArrayList(String key) {
        return super.getIntegerArrayList(key);
    }

    @Override // android.os.BaseBundle
    public ArrayList<String> getStringArrayList(String key) {
        return super.getStringArrayList(key);
    }

    @Override // android.os.BaseBundle
    public ArrayList<CharSequence> getCharSequenceArrayList(String key) {
        return super.getCharSequenceArrayList(key);
    }

    @Override // android.os.BaseBundle
    public byte[] getByteArray(String key) {
        return super.getByteArray(key);
    }

    @Override // android.os.BaseBundle
    public short[] getShortArray(String key) {
        return super.getShortArray(key);
    }

    @Override // android.os.BaseBundle
    public char[] getCharArray(String key) {
        return super.getCharArray(key);
    }

    @Override // android.os.BaseBundle
    public float[] getFloatArray(String key) {
        return super.getFloatArray(key);
    }

    @Override // android.os.BaseBundle
    public CharSequence[] getCharSequenceArray(String key) {
        return super.getCharSequenceArray(key);
    }

    public IBinder getBinder(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (IBinder) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "IBinder", e);
            return null;
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public IBinder getIBinder(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (IBinder) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "IBinder", e);
            return null;
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        if (hasFileDescriptors()) {
            return 0 | 1;
        }
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        boolean oldAllowFds = parcel.pushAllowFds((this.mFlags & 1024) != 0);
        try {
            super.writeToParcelInner(parcel, flags);
        } finally {
            parcel.restoreAllowFds(oldAllowFds);
        }
    }

    public void readFromParcel(Parcel parcel) {
        super.readFromParcelInner(parcel);
        this.mFlags = 1024;
        maybePrefillHasFds();
    }

    @Override // java.lang.Object
    public synchronized String toString() {
        if (this.mParcelledData == null) {
            return "Bundle[" + this.mMap.toString() + "]";
        } else if (isEmptyParcel()) {
            return "Bundle[EMPTY_PARCEL]";
        } else {
            return "Bundle[mParcelledData.dataSize=" + this.mParcelledData.dataSize() + "]";
        }
    }

    public synchronized String toShortString() {
        if (this.mParcelledData == null) {
            return this.mMap.toString();
        } else if (isEmptyParcel()) {
            return "EMPTY_PARCEL";
        } else {
            return "mParcelledData.dataSize=" + this.mParcelledData.dataSize();
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        if (this.mParcelledData == null) {
            proto.write(1138166333442L, this.mMap.toString());
        } else if (isEmptyParcel()) {
            proto.write(1120986464257L, 0);
        } else {
            proto.write(1120986464257L, this.mParcelledData.dataSize());
        }
        proto.end(token);
    }
}
