package android.content;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PersistableBundle;
import android.security.keymaster.KeymasterDefs;
import android.text.TextUtils;
import java.util.ArrayList;

public class ClipDescription implements Parcelable {
    public static final Creator<ClipDescription> CREATOR = null;
    public static final String EXTRA_TARGET_COMPONENT_NAME = "android.content.extra.TARGET_COMPONENT_NAME";
    public static final String EXTRA_USER_SERIAL_NUMBER = "android.content.extra.USER_SERIAL_NUMBER";
    public static final String MIMETYPE_TEXT_HTML = "text/html";
    public static final String MIMETYPE_TEXT_INTENT = "text/vnd.android.intent";
    public static final String MIMETYPE_TEXT_PLAIN = "text/plain";
    public static final String MIMETYPE_TEXT_URILIST = "text/uri-list";
    private PersistableBundle mExtras;
    final CharSequence mLabel;
    final String[] mMimeTypes;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.ClipDescription.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.ClipDescription.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.ClipDescription.<clinit>():void");
    }

    public ClipDescription(CharSequence label, String[] mimeTypes) {
        if (mimeTypes == null) {
            throw new NullPointerException("mimeTypes is null");
        }
        this.mLabel = label;
        this.mMimeTypes = mimeTypes;
    }

    public ClipDescription(ClipDescription o) {
        this.mLabel = o.mLabel;
        this.mMimeTypes = o.mMimeTypes;
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

    public CharSequence getLabel() {
        return this.mLabel;
    }

    public boolean hasMimeType(String mimeType) {
        for (String compareMimeTypes : this.mMimeTypes) {
            if (compareMimeTypes(compareMimeTypes, mimeType)) {
                return true;
            }
        }
        return false;
    }

    public String[] filterMimeTypes(String mimeType) {
        ArrayList array = null;
        for (int i = 0; i < this.mMimeTypes.length; i++) {
            if (compareMimeTypes(this.mMimeTypes[i], mimeType)) {
                if (array == null) {
                    array = new ArrayList();
                }
                array.add(this.mMimeTypes[i]);
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
        return this.mMimeTypes.length;
    }

    public String getMimeType(int index) {
        return this.mMimeTypes[index];
    }

    public PersistableBundle getExtras() {
        return this.mExtras;
    }

    public void setExtras(PersistableBundle extras) {
        this.mExtras = new PersistableBundle(extras);
    }

    public void validate() {
        if (this.mMimeTypes == null) {
            throw new NullPointerException("null mime types");
        } else if (this.mMimeTypes.length <= 0) {
            throw new IllegalArgumentException("must have at least 1 mime type");
        } else {
            for (int i = 0; i < this.mMimeTypes.length; i++) {
                if (this.mMimeTypes[i] == null) {
                    throw new NullPointerException("mime type at " + i + " is null");
                }
            }
        }
    }

    public String toString() {
        StringBuilder b = new StringBuilder(KeymasterDefs.KM_ALGORITHM_HMAC);
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
        if (first) {
            return false;
        }
        return true;
    }

    public boolean toShortStringTypesOnly(StringBuilder b) {
        boolean first = true;
        for (String append : this.mMimeTypes) {
            if (!first) {
                b.append(' ');
            }
            first = false;
            b.append(append);
        }
        return !first;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        TextUtils.writeToParcel(this.mLabel, dest, flags);
        dest.writeStringArray(this.mMimeTypes);
        dest.writePersistableBundle(this.mExtras);
    }

    ClipDescription(Parcel in) {
        this.mLabel = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mMimeTypes = in.createStringArray();
        this.mExtras = in.readPersistableBundle();
    }
}
