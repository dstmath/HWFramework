package huawei.android.aod;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Locale;

public class Layer implements Parcelable {
    public static final Parcelable.Creator<Layer> CREATOR = new Parcelable.Creator<Layer>() {
        /* class huawei.android.aod.Layer.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Layer createFromParcel(Parcel in) {
            return new Layer(in);
        }

        @Override // android.os.Parcelable.Creator
        public Layer[] newArray(int size) {
            return new Layer[size];
        }
    };
    private int mActionCount;
    private ArrayList<Action> mActionList;
    private int mBrightness;
    private int mGlbAlpha;
    private Rect mLayerRect;
    private int mLayerType;
    private int mPictureRgbValue;
    private int mSaturation;

    public Layer(int actionType, int pictureRgbValue, int glbAlpha, int saturation, int brightness, Rect rect, ArrayList<Action> actionList) {
        this.mLayerType = actionType;
        this.mPictureRgbValue = pictureRgbValue;
        this.mGlbAlpha = glbAlpha;
        this.mSaturation = saturation;
        this.mBrightness = brightness;
        this.mLayerRect = rect;
        this.mActionList = actionList;
        this.mActionCount = actionList.size();
    }

    public Layer(Parcel in) {
        readFromParcel(in);
    }

    public int getLayerType() {
        return this.mLayerType;
    }

    public void setRgbValue(int rgbValue) {
        this.mPictureRgbValue = rgbValue;
    }

    public int getRgbValue() {
        return this.mPictureRgbValue;
    }

    public int getGlbAlpha() {
        return this.mGlbAlpha;
    }

    public int getSaturation() {
        return this.mSaturation;
    }

    public int getBrightness() {
        return this.mBrightness;
    }

    public int getActionCount() {
        return this.mActionCount;
    }

    public ArrayList<Action> getActionList() {
        return this.mActionList;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mLayerType);
        out.writeParcelable(this.mLayerRect, flags);
        out.writeInt(this.mPictureRgbValue);
        out.writeInt(this.mGlbAlpha);
        out.writeInt(this.mSaturation);
        out.writeInt(this.mBrightness);
        out.writeInt(this.mActionCount);
        out.writeTypedList(this.mActionList);
    }

    private void readFromParcel(Parcel in) {
        this.mLayerType = in.readInt();
        this.mLayerRect = (Rect) in.readParcelable(Rect.class.getClassLoader());
        this.mPictureRgbValue = in.readInt();
        this.mGlbAlpha = in.readInt();
        this.mSaturation = in.readInt();
        this.mBrightness = in.readInt();
        this.mActionCount = in.readInt();
        this.mActionList = new ArrayList<>();
        in.readTypedList(this.mActionList, Action.CREATOR);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x003e: APUT  
      (r1v1 java.lang.Object[])
      (6 ??[int, float, short, byte, char])
      (r2v12 java.util.ArrayList<huawei.android.aod.Action>)
     */
    @Override // java.lang.Object
    public String toString() {
        Locale locale = Locale.ENGLISH;
        Object[] objArr = new Object[7];
        objArr[0] = Integer.valueOf(this.mLayerType);
        objArr[1] = Integer.valueOf(this.mPictureRgbValue);
        objArr[2] = Integer.valueOf(this.mGlbAlpha);
        objArr[3] = Integer.valueOf(this.mSaturation);
        objArr[4] = Integer.valueOf(this.mBrightness);
        objArr[5] = this.mLayerRect;
        ArrayList<Action> arrayList = this.mActionList;
        if (arrayList == null) {
            arrayList = null;
        }
        objArr[6] = arrayList;
        return String.format(locale, "{layer: %d, pigtureRgb : %d, glbAlpha : %d, saturation : %d, brightness : %d, rect : %s, actions : %s}", objArr);
    }
}
