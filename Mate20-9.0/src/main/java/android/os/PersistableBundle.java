package android.os;

import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class PersistableBundle extends BaseBundle implements Cloneable, Parcelable, XmlUtils.WriteMapCallback {
    public static final Parcelable.Creator<PersistableBundle> CREATOR = new Parcelable.Creator<PersistableBundle>() {
        public PersistableBundle createFromParcel(Parcel in) {
            return in.readPersistableBundle();
        }

        public PersistableBundle[] newArray(int size) {
            return new PersistableBundle[size];
        }
    };
    public static final PersistableBundle EMPTY = new PersistableBundle();
    private static final String TAG_PERSISTABLEMAP = "pbundle_as_map";

    static class MyReadMapCallback implements XmlUtils.ReadMapCallback {
        MyReadMapCallback() {
        }

        public Object readThisUnknownObjectXml(XmlPullParser in, String tag) throws XmlPullParserException, IOException {
            if (PersistableBundle.TAG_PERSISTABLEMAP.equals(tag)) {
                return PersistableBundle.restoreFromXml(in);
            }
            throw new XmlPullParserException("Unknown tag=" + tag);
        }
    }

    static {
        EMPTY.mMap = ArrayMap.EMPTY;
    }

    public static boolean isValidType(Object value) {
        return (value instanceof Integer) || (value instanceof Long) || (value instanceof Double) || (value instanceof String) || (value instanceof int[]) || (value instanceof long[]) || (value instanceof double[]) || (value instanceof String[]) || (value instanceof PersistableBundle) || value == null || (value instanceof Boolean) || (value instanceof boolean[]);
    }

    public PersistableBundle() {
        this.mFlags = 1;
    }

    public PersistableBundle(int capacity) {
        super(capacity);
        this.mFlags = 1;
    }

    public PersistableBundle(PersistableBundle b) {
        super((BaseBundle) b);
        this.mFlags = b.mFlags;
    }

    public PersistableBundle(Bundle b) {
        this(b.getMap());
    }

    private PersistableBundle(ArrayMap<String, Object> map) {
        this.mFlags = 1;
        putAll((ArrayMap) map);
        int N = this.mMap.size();
        for (int i = 0; i < N; i++) {
            Object value = this.mMap.valueAt(i);
            if (value instanceof ArrayMap) {
                this.mMap.setValueAt(i, new PersistableBundle((ArrayMap<String, Object>) (ArrayMap) value));
            } else if (value instanceof Bundle) {
                this.mMap.setValueAt(i, new PersistableBundle((Bundle) value));
            } else if (!isValidType(value)) {
                throw new IllegalArgumentException("Bad value in PersistableBundle key=" + ((String) this.mMap.keyAt(i)) + " value=" + value);
            }
        }
    }

    PersistableBundle(Parcel parcelledData, int length) {
        super(parcelledData, length);
        this.mFlags = 1;
    }

    PersistableBundle(boolean doInit) {
        super(doInit);
    }

    public static PersistableBundle forPair(String key, String value) {
        PersistableBundle b = new PersistableBundle(1);
        b.putString(key, value);
        return b;
    }

    public Object clone() {
        return new PersistableBundle(this);
    }

    public PersistableBundle deepCopy() {
        PersistableBundle b = new PersistableBundle(false);
        b.copyInternal(this, true);
        return b;
    }

    public void putPersistableBundle(String key, PersistableBundle value) {
        unparcel();
        this.mMap.put(key, value);
    }

    public PersistableBundle getPersistableBundle(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (PersistableBundle) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Bundle", e);
            return null;
        }
    }

    public void writeUnknownObject(Object v, String name, XmlSerializer out) throws XmlPullParserException, IOException {
        if (v instanceof PersistableBundle) {
            out.startTag(null, TAG_PERSISTABLEMAP);
            out.attribute(null, "name", name);
            ((PersistableBundle) v).saveToXml(out);
            out.endTag(null, TAG_PERSISTABLEMAP);
            return;
        }
        throw new XmlPullParserException("Unknown Object o=" + v);
    }

    public void saveToXml(XmlSerializer out) throws IOException, XmlPullParserException {
        unparcel();
        XmlUtils.writeMapXml(this.mMap, out, this);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        boolean oldAllowFds = parcel.pushAllowFds(false);
        try {
            writeToParcelInner(parcel, flags);
        } finally {
            parcel.restoreAllowFds(oldAllowFds);
        }
    }

    public static PersistableBundle restoreFromXml(XmlPullParser in) throws IOException, XmlPullParserException {
        int event;
        int outerDepth = in.getDepth();
        String startTag = in.getName();
        String[] tagName = new String[1];
        do {
            int next = in.next();
            event = next;
            if (next == 1 || (event == 3 && in.getDepth() >= outerDepth)) {
                return EMPTY;
            }
        } while (event != 2);
        return new PersistableBundle((ArrayMap<String, Object>) XmlUtils.readThisArrayMapXml(in, startTag, tagName, new MyReadMapCallback()));
    }

    public synchronized String toString() {
        if (this.mParcelledData == null) {
            return "PersistableBundle[" + this.mMap.toString() + "]";
        } else if (isEmptyParcel()) {
            return "PersistableBundle[EMPTY_PARCEL]";
        } else {
            return "PersistableBundle[mParcelledData.dataSize=" + this.mParcelledData.dataSize() + "]";
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
