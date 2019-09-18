package android.content.pm;

import android.os.Parcel;
import java.util.ArrayList;
import java.util.HashMap;

public class PackageParserCacheHelper {
    private static final boolean DEBUG = false;
    private static final String TAG = "PackageParserCacheHelper";

    public static class ReadHelper extends Parcel.ReadWriteHelper {
        private final Parcel mParcel;
        private final ArrayList<String> mStrings = new ArrayList<>();

        public ReadHelper(Parcel p) {
            this.mParcel = p;
        }

        public void startAndInstall() {
            this.mStrings.clear();
            int poolPosition = this.mParcel.readInt();
            int startPosition = this.mParcel.dataPosition();
            this.mParcel.setDataPosition(poolPosition);
            this.mParcel.readStringList(this.mStrings);
            this.mParcel.setDataPosition(startPosition);
            this.mParcel.setReadWriteHelper(this);
        }

        public String readString(Parcel p) {
            return this.mStrings.get(p.readInt());
        }
    }

    public static class WriteHelper extends Parcel.ReadWriteHelper {
        private final HashMap<String, Integer> mIndexes = new HashMap<>();
        private final Parcel mParcel;
        private final int mStartPos;
        private final ArrayList<String> mStrings = new ArrayList<>();

        public WriteHelper(Parcel p) {
            this.mParcel = p;
            this.mStartPos = p.dataPosition();
            this.mParcel.writeInt(0);
            this.mParcel.setReadWriteHelper(this);
        }

        public void writeString(Parcel p, String s) {
            Integer cur = this.mIndexes.get(s);
            if (cur != null) {
                p.writeInt(cur.intValue());
                return;
            }
            int index = this.mStrings.size();
            this.mIndexes.put(s, Integer.valueOf(index));
            this.mStrings.add(s);
            p.writeInt(index);
        }

        public void finishAndUninstall() {
            this.mParcel.setReadWriteHelper(null);
            int poolPosition = this.mParcel.dataPosition();
            this.mParcel.writeStringList(this.mStrings);
            this.mParcel.setDataPosition(this.mStartPos);
            this.mParcel.writeInt(poolPosition);
            this.mParcel.setDataPosition(this.mParcel.dataSize());
        }
    }

    private PackageParserCacheHelper() {
    }
}
