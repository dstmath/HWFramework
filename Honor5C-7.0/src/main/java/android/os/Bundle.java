package android.os;

import android.bluetooth.BluetoothClass.Device.Major;
import android.os.Parcelable.Creator;
import android.util.Size;
import android.util.SizeF;
import android.util.SparseArray;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Bundle extends BaseBundle implements Cloneable, Parcelable {
    public static final Creator<Bundle> CREATOR = null;
    public static final Bundle EMPTY = null;
    private static final int FLAG_ALLOW_FDS = 1024;
    private static final int FLAG_HAS_FDS = 256;
    private static final int FLAG_HAS_FDS_KNOWN = 512;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.Bundle.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.Bundle.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.Bundle.<clinit>():void");
    }

    public Bundle() {
        this.mFlags = Major.IMAGING;
    }

    Bundle(Parcel parcelledData) {
        super(parcelledData);
        this.mFlags = Major.IMAGING;
        if (this.mParcelledData.hasFileDescriptors()) {
            this.mFlags |= FLAG_HAS_FDS;
        }
    }

    Bundle(Parcel parcelledData, int length) {
        super(parcelledData, length);
        this.mFlags = Major.IMAGING;
        if (this.mParcelledData.hasFileDescriptors()) {
            this.mFlags |= FLAG_HAS_FDS;
        }
    }

    public Bundle(ClassLoader loader) {
        super(loader);
        this.mFlags = Major.IMAGING;
    }

    public Bundle(int capacity) {
        super(capacity);
        this.mFlags = Major.IMAGING;
    }

    public Bundle(Bundle b) {
        super((BaseBundle) b);
        this.mFlags = b.mFlags;
    }

    public Bundle(PersistableBundle b) {
        super((BaseBundle) b);
        this.mFlags = Major.IMAGING;
    }

    public static Bundle forPair(String key, String value) {
        Bundle b = new Bundle(1);
        b.putString(key, value);
        return b;
    }

    public void setClassLoader(ClassLoader loader) {
        super.setClassLoader(loader);
    }

    public ClassLoader getClassLoader() {
        return super.getClassLoader();
    }

    public boolean setAllowFds(boolean allowFds) {
        boolean orig = (this.mFlags & FLAG_ALLOW_FDS) != 0;
        if (allowFds) {
            this.mFlags |= FLAG_ALLOW_FDS;
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

    public static Bundle setDefusable(Bundle bundle, boolean defusable) {
        if (bundle != null) {
            bundle.setDefusable(defusable);
        }
        return bundle;
    }

    public Object clone() {
        return new Bundle(this);
    }

    public void clear() {
        super.clear();
        this.mFlags = Major.IMAGING;
    }

    public void remove(String key) {
        super.remove(key);
        if ((this.mFlags & FLAG_HAS_FDS) != 0) {
            this.mFlags &= -513;
        }
    }

    public void putAll(Bundle bundle) {
        unparcel();
        bundle.unparcel();
        this.mMap.putAll(bundle.mMap);
        if ((bundle.mFlags & FLAG_HAS_FDS) != 0) {
            this.mFlags |= FLAG_HAS_FDS;
        }
        if ((bundle.mFlags & FLAG_HAS_FDS_KNOWN) == 0) {
            this.mFlags &= -513;
        }
    }

    public boolean hasFileDescriptors() {
        if ((this.mFlags & FLAG_HAS_FDS_KNOWN) == 0) {
            boolean fdFound = false;
            if (this.mParcelledData == null) {
                for (int i = this.mMap.size() - 1; i >= 0; i--) {
                    SparseArray<? extends Parcelable> obj = this.mMap.valueAt(i);
                    if (obj instanceof Parcelable) {
                        if ((((Parcelable) obj).describeContents() & 1) != 0) {
                            fdFound = true;
                            break;
                        }
                    } else if (obj instanceof Parcelable[]) {
                        Parcelable[] array = (Parcelable[]) obj;
                        for (n = array.length - 1; n >= 0; n--) {
                            p = array[n];
                            if (p != null && (p.describeContents() & 1) != 0) {
                                fdFound = true;
                                break;
                            }
                        }
                    } else if (obj instanceof SparseArray) {
                        SparseArray<? extends Parcelable> array2 = obj;
                        for (n = array2.size() - 1; n >= 0; n--) {
                            p = (Parcelable) array2.valueAt(n);
                            if (p != null && (p.describeContents() & 1) != 0) {
                                fdFound = true;
                                break;
                            }
                        }
                    } else if (obj instanceof ArrayList) {
                        ArrayList array3 = (ArrayList) obj;
                        if (!array3.isEmpty() && (array3.get(0) instanceof Parcelable)) {
                            for (n = array3.size() - 1; n >= 0; n--) {
                                p = (Parcelable) array3.get(n);
                                if (p != null && (p.describeContents() & 1) != 0) {
                                    fdFound = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            } else if (this.mParcelledData.hasFileDescriptors()) {
                fdFound = true;
            }
            if (fdFound) {
                this.mFlags |= FLAG_HAS_FDS;
            } else {
                this.mFlags &= -257;
            }
            this.mFlags |= FLAG_HAS_FDS_KNOWN;
        }
        if ((this.mFlags & FLAG_HAS_FDS) != 0) {
            return true;
        }
        return false;
    }

    public void filterValues() {
        unparcel();
        if (this.mMap != null) {
            for (int i = this.mMap.size() - 1; i >= 0; i--) {
                Object value = this.mMap.valueAt(i);
                if (!PersistableBundle.isValidType(value)) {
                    if (value instanceof Bundle) {
                        ((Bundle) value).filterValues();
                    }
                    if (!value.getClass().getName().startsWith("android.")) {
                        this.mMap.removeAt(i);
                    }
                }
            }
        }
        this.mFlags |= FLAG_HAS_FDS_KNOWN;
        this.mFlags &= -257;
    }

    public void putByte(String key, byte value) {
        super.putByte(key, value);
    }

    public void putChar(String key, char value) {
        super.putChar(key, value);
    }

    public void putShort(String key, short value) {
        super.putShort(key, value);
    }

    public void putFloat(String key, float value) {
        super.putFloat(key, value);
    }

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

    public void putIntegerArrayList(String key, ArrayList<Integer> value) {
        super.putIntegerArrayList(key, value);
    }

    public void putStringArrayList(String key, ArrayList<String> value) {
        super.putStringArrayList(key, value);
    }

    public void putCharSequenceArrayList(String key, ArrayList<CharSequence> value) {
        super.putCharSequenceArrayList(key, value);
    }

    public void putSerializable(String key, Serializable value) {
        super.putSerializable(key, value);
    }

    public void putByteArray(String key, byte[] value) {
        super.putByteArray(key, value);
    }

    public void putShortArray(String key, short[] value) {
        super.putShortArray(key, value);
    }

    public void putCharArray(String key, char[] value) {
        super.putCharArray(key, value);
    }

    public void putFloatArray(String key, float[] value) {
        super.putFloatArray(key, value);
    }

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

    @Deprecated
    public void putIBinder(String key, IBinder value) {
        unparcel();
        this.mMap.put(key, value);
    }

    public byte getByte(String key) {
        return super.getByte(key);
    }

    public Byte getByte(String key, byte defaultValue) {
        return super.getByte(key, defaultValue);
    }

    public char getChar(String key) {
        return super.getChar(key);
    }

    public char getChar(String key, char defaultValue) {
        return super.getChar(key, defaultValue);
    }

    public short getShort(String key) {
        return super.getShort(key);
    }

    public short getShort(String key, short defaultValue) {
        return super.getShort(key, defaultValue);
    }

    public float getFloat(String key) {
        return super.getFloat(key);
    }

    public float getFloat(String key, float defaultValue) {
        return super.getFloat(key, defaultValue);
    }

    public CharSequence getCharSequence(String key) {
        return super.getCharSequence(key);
    }

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
            return (Parcelable) o;
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

    public Serializable getSerializable(String key) {
        return super.getSerializable(key);
    }

    public ArrayList<Integer> getIntegerArrayList(String key) {
        return super.getIntegerArrayList(key);
    }

    public ArrayList<String> getStringArrayList(String key) {
        return super.getStringArrayList(key);
    }

    public ArrayList<CharSequence> getCharSequenceArrayList(String key) {
        return super.getCharSequenceArrayList(key);
    }

    public byte[] getByteArray(String key) {
        return super.getByteArray(key);
    }

    public short[] getShortArray(String key) {
        return super.getShortArray(key);
    }

    public char[] getCharArray(String key) {
        return super.getCharArray(key);
    }

    public float[] getFloatArray(String key) {
        return super.getFloatArray(key);
    }

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

    public int describeContents() {
        if (hasFileDescriptors()) {
            return 1;
        }
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        boolean z = false;
        if ((this.mFlags & FLAG_ALLOW_FDS) != 0) {
            z = true;
        }
        boolean oldAllowFds = parcel.pushAllowFds(z);
        try {
            super.writeToParcelInner(parcel, flags);
        } finally {
            parcel.restoreAllowFds(oldAllowFds);
        }
    }

    public void readFromParcel(Parcel parcel) {
        super.readFromParcelInner(parcel);
        this.mFlags = Major.IMAGING;
        if (this.mParcelledData.hasFileDescriptors()) {
            this.mFlags |= FLAG_HAS_FDS;
        }
    }

    public synchronized String toString() {
        if (this.mParcelledData == null) {
            return "Bundle[" + this.mMap.toString() + "]";
        } else if (isEmptyParcel()) {
            return "Bundle[EMPTY_PARCEL]";
        } else {
            return "Bundle[mParcelledData.dataSize=" + this.mParcelledData.dataSize() + "]";
        }
    }
}
