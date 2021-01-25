package android.media;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class AudioRoutesInfo implements Parcelable {
    public static final Parcelable.Creator<AudioRoutesInfo> CREATOR = new Parcelable.Creator<AudioRoutesInfo>() {
        /* class android.media.AudioRoutesInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AudioRoutesInfo createFromParcel(Parcel in) {
            return new AudioRoutesInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public AudioRoutesInfo[] newArray(int size) {
            return new AudioRoutesInfo[size];
        }
    };
    public static final int MAIN_DOCK_SPEAKERS = 4;
    public static final int MAIN_HDMI = 8;
    public static final int MAIN_HEADPHONES = 2;
    public static final int MAIN_HEADSET = 1;
    public static final int MAIN_SPEAKER = 0;
    public static final int MAIN_USB = 16;
    public CharSequence bluetoothName;
    public int mainType = 0;

    public AudioRoutesInfo() {
    }

    public AudioRoutesInfo(AudioRoutesInfo o) {
        this.bluetoothName = o.bluetoothName;
        this.mainType = o.mainType;
    }

    AudioRoutesInfo(Parcel src) {
        this.bluetoothName = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(src);
        this.mainType = src.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("{ type=");
        sb.append(typeToString(this.mainType));
        if (TextUtils.isEmpty(this.bluetoothName)) {
            str = "";
        } else {
            str = ", bluetoothName=" + ((Object) this.bluetoothName);
        }
        sb.append(str);
        sb.append(" }");
        return sb.toString();
    }

    private static String typeToString(int type) {
        if (type == 0) {
            return "SPEAKER";
        }
        if ((type & 1) != 0) {
            return "HEADSET";
        }
        if ((type & 2) != 0) {
            return "HEADPHONES";
        }
        if ((type & 4) != 0) {
            return "DOCK_SPEAKERS";
        }
        if ((type & 8) != 0) {
            return "HDMI";
        }
        if ((type & 16) != 0) {
            return "USB";
        }
        return Integer.toHexString(type);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        TextUtils.writeToParcel(this.bluetoothName, dest, flags);
        dest.writeInt(this.mainType);
    }
}
