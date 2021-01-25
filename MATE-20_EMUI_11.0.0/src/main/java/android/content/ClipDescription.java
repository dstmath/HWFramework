package android.content;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class ClipDescription implements Parcelable {
    public static final Parcelable.Creator<ClipDescription> CREATOR = new Parcelable.Creator<ClipDescription>() {
        /* class android.content.ClipDescription.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ClipDescription createFromParcel(Parcel source) {
            return new ClipDescription(source);
        }

        @Override // android.os.Parcelable.Creator
        public ClipDescription[] newArray(int size) {
            return new ClipDescription[size];
        }
    };
    public static final String EXTRA_TARGET_COMPONENT_NAME = "android.content.extra.TARGET_COMPONENT_NAME";
    public static final String EXTRA_USER_SERIAL_NUMBER = "android.content.extra.USER_SERIAL_NUMBER";
    public static final String MIMETYPE_TEXT_HTML = "text/html";
    public static final String MIMETYPE_TEXT_INTENT = "text/vnd.android.intent";
    public static final String MIMETYPE_TEXT_PLAIN = "text/plain";
    public static final String MIMETYPE_TEXT_URILIST = "text/uri-list";
    private PersistableBundle mExtras;
    final CharSequence mLabel;
    private SparseIntArray mMimeItemCounter = new SparseIntArray();
    private final ArrayList<String> mMimeTypes;
    private long mTimeStamp;

    public ClipDescription(CharSequence label, String[] mimeTypes) {
        if (mimeTypes != null) {
            this.mLabel = label;
            this.mMimeTypes = new ArrayList<>(Arrays.asList(mimeTypes));
            for (int key = 0; key != mimeTypes.length; key++) {
                this.mMimeItemCounter.put(key, 1);
            }
            return;
        }
        throw new NullPointerException("mimeTypes is null");
    }

    public ClipDescription(ClipDescription o) {
        this.mLabel = o.mLabel;
        this.mMimeTypes = new ArrayList<>(o.mMimeTypes);
        this.mTimeStamp = o.mTimeStamp;
    }

    public static boolean compareMimeTypes(String concreteType, String desiredType) {
        int typeLength = desiredType.length();
        if (typeLength == 3 && desiredType.equals("*/*")) {
            return true;
        }
        int slashpos = desiredType.indexOf(47);
        if (slashpos > 0) {
            if (typeLength == slashpos + 2 && desiredType.charAt(slashpos + 1) == '*') {
                if (desiredType.regionMatches(0, concreteType, 0, slashpos + 1)) {
                    return true;
                }
            } else if (desiredType.equals(concreteType)) {
                return true;
            }
        }
        return false;
    }

    public void setTimestamp(long timeStamp) {
        this.mTimeStamp = timeStamp;
    }

    public long getTimestamp() {
        return this.mTimeStamp;
    }

    public CharSequence getLabel() {
        return this.mLabel;
    }

    public boolean hasMimeType(String mimeType) {
        int size = this.mMimeTypes.size();
        for (int i = 0; i < size; i++) {
            if (compareMimeTypes(this.mMimeTypes.get(i), mimeType)) {
                return true;
            }
        }
        return false;
    }

    public String[] filterMimeTypes(String mimeType) {
        ArrayList<String> array = null;
        int size = this.mMimeTypes.size();
        for (int i = 0; i < size; i++) {
            if (compareMimeTypes(this.mMimeTypes.get(i), mimeType)) {
                if (array == null) {
                    array = new ArrayList<>();
                }
                array.add(this.mMimeTypes.get(i));
            }
        }
        if (array == null) {
            return null;
        }
        String[] rawArray = new String[array.size()];
        array.toArray(rawArray);
        return rawArray;
    }

    public int getMimeTypeCount() {
        return this.mMimeTypes.size();
    }

    public int getMimeTypeItemCount(int index) {
        return this.mMimeItemCounter.get(index, 0);
    }

    public String getMimeType(int index) {
        return this.mMimeTypes.get(index);
    }

    /* access modifiers changed from: package-private */
    public void addMimeTypes(String[] mimeTypes) {
        for (int i = 0; i != mimeTypes.length; i++) {
            String mimeType = mimeTypes[i];
            if (!this.mMimeTypes.contains(mimeType)) {
                this.mMimeTypes.add(mimeType);
            }
            int key = this.mMimeTypes.indexOf(mimeType);
            if (key != -1) {
                this.mMimeItemCounter.put(key, this.mMimeItemCounter.get(key, 0) + 1);
            }
        }
    }

    public PersistableBundle getExtras() {
        return this.mExtras;
    }

    public void setExtras(PersistableBundle extras) {
        this.mExtras = new PersistableBundle(extras);
    }

    public void validate() {
        ArrayList<String> arrayList = this.mMimeTypes;
        if (arrayList != null) {
            int size = arrayList.size();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    if (this.mMimeTypes.get(i) == null) {
                        throw new NullPointerException("mime type at " + i + " is null");
                    }
                }
                return;
            }
            throw new IllegalArgumentException("must have at least 1 mime type");
        }
        throw new NullPointerException("null mime types");
    }

    public String toString() {
        StringBuilder b = new StringBuilder(128);
        b.append("ClipDescription { ");
        toShortString(b);
        b.append(" }");
        return b.toString();
    }

    public boolean toShortString(StringBuilder b) {
        boolean first = !toShortStringTypesOnly(b);
        if (this.mLabel != null) {
            if (!first) {
                b.append(' ');
            }
            first = false;
            b.append('\"');
            b.append(this.mLabel);
            b.append('\"');
        }
        if (this.mExtras != null) {
            if (!first) {
                b.append(' ');
            }
            first = false;
            b.append(this.mExtras.toString());
        }
        if (this.mTimeStamp > 0) {
            if (!first) {
                b.append(' ');
            }
            first = false;
            b.append('<');
            b.append(TimeUtils.logTimeOfDay(this.mTimeStamp));
            b.append('>');
        }
        if (!first) {
            return true;
        }
        return false;
    }

    public boolean toShortStringTypesOnly(StringBuilder b) {
        boolean first = true;
        int size = this.mMimeTypes.size();
        for (int i = 0; i < size; i++) {
            if (!first) {
                b.append(' ');
            }
            first = false;
            b.append(this.mMimeTypes.get(i));
            b.append('=');
            b.append(this.mMimeItemCounter.get(i, 0));
        }
        if (!first) {
            return true;
        }
        return false;
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        int size = this.mMimeTypes.size();
        for (int i = 0; i < size; i++) {
            proto.write(2237677961217L, this.mMimeTypes.get(i));
        }
        CharSequence charSequence = this.mLabel;
        if (charSequence != null) {
            proto.write(1138166333442L, charSequence.toString());
        }
        PersistableBundle persistableBundle = this.mExtras;
        if (persistableBundle != null) {
            persistableBundle.writeToProto(proto, 1146756268035L);
        }
        long j = this.mTimeStamp;
        if (j > 0) {
            proto.write(1112396529668L, j);
        }
        proto.end(token);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        TextUtils.writeToParcel(this.mLabel, dest, flags);
        dest.writeStringList(this.mMimeTypes);
        dest.writePersistableBundle(this.mExtras);
        dest.writeLong(this.mTimeStamp);
        dest.writeSparseIntArray(this.mMimeItemCounter);
    }

    ClipDescription(Parcel in) {
        this.mLabel = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mMimeTypes = in.createStringArrayList();
        this.mExtras = in.readPersistableBundle();
        this.mTimeStamp = in.readLong();
        this.mMimeItemCounter = in.readSparseIntArray();
    }
}
