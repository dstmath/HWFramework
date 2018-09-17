package android.telephony;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.DisplayMetrics;

public class SubscriptionInfo implements Parcelable {
    public static final Creator<SubscriptionInfo> CREATOR = new Creator<SubscriptionInfo>() {
        public SubscriptionInfo createFromParcel(Parcel source) {
            int id = source.readInt();
            String iccId = source.readString();
            int simSlotIndex = source.readInt();
            CharSequence displayName = source.readCharSequence();
            CharSequence carrierName = source.readCharSequence();
            int nameSource = source.readInt();
            int iconTint = source.readInt();
            String number = source.readString();
            int dataRoaming = source.readInt();
            int mcc = source.readInt();
            int mnc = source.readInt();
            int status = source.readInt();
            int nwMode = source.readInt();
            return new SubscriptionInfo(id, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number, dataRoaming, (Bitmap) Bitmap.CREATOR.createFromParcel(source), mcc, mnc, source.readString(), status, nwMode);
        }

        public SubscriptionInfo[] newArray(int size) {
            return new SubscriptionInfo[size];
        }
    };
    private static final int TEXT_SIZE = 16;
    private CharSequence mCarrierName;
    private String mCountryIso;
    private int mDataRoaming;
    private CharSequence mDisplayName;
    private String mIccId;
    private Bitmap mIconBitmap;
    private int mIconTint;
    private int mId;
    private int mMcc;
    private int mMnc;
    private int mNameSource;
    private String mNumber;
    public int mNwMode;
    private int mSimSlotIndex;
    public int mStatus;

    public SubscriptionInfo(int id, String iccId, int simSlotIndex, CharSequence displayName, CharSequence carrierName, int nameSource, int iconTint, String number, int roaming, Bitmap icon, int mcc, int mnc, String countryIso) {
        this.mId = id;
        this.mIccId = iccId;
        this.mSimSlotIndex = simSlotIndex;
        this.mDisplayName = displayName;
        this.mCarrierName = carrierName;
        this.mNameSource = nameSource;
        this.mIconTint = iconTint;
        this.mNumber = number;
        this.mDataRoaming = roaming;
        this.mIconBitmap = icon;
        this.mMcc = mcc;
        this.mMnc = mnc;
        this.mCountryIso = countryIso;
        this.mStatus = 1;
        this.mNwMode = -1;
    }

    public SubscriptionInfo(int id, String iccId, int simSlotIndex, CharSequence displayName, CharSequence carrierName, int nameSource, int iconTint, String number, int roaming, Bitmap icon, int mcc, int mnc, String countryIso, int status, int nwMode) {
        this.mId = id;
        this.mIccId = iccId;
        this.mSimSlotIndex = simSlotIndex;
        this.mDisplayName = displayName;
        this.mCarrierName = carrierName;
        this.mNameSource = nameSource;
        this.mIconTint = iconTint;
        this.mNumber = number;
        this.mDataRoaming = roaming;
        this.mIconBitmap = icon;
        this.mMcc = mcc;
        this.mMnc = mnc;
        this.mStatus = status;
        this.mNwMode = nwMode;
        this.mCountryIso = countryIso;
    }

    public int getSubscriptionId() {
        return this.mId;
    }

    public String getIccId() {
        return this.mIccId;
    }

    public int getSimSlotIndex() {
        return this.mSimSlotIndex;
    }

    public CharSequence getDisplayName() {
        return this.mDisplayName;
    }

    public void setDisplayName(CharSequence name) {
        this.mDisplayName = name;
    }

    public CharSequence getCarrierName() {
        return this.mCarrierName;
    }

    public void setCarrierName(CharSequence name) {
        this.mCarrierName = name;
    }

    public int getNameSource() {
        return this.mNameSource;
    }

    public Bitmap createIconBitmap(Context context) {
        int width = this.mIconBitmap.getWidth();
        int height = this.mIconBitmap.getHeight();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        Bitmap workingBitmap = Bitmap.createBitmap(metrics, width, height, this.mIconBitmap.getConfig());
        Canvas canvas = new Canvas(workingBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(this.mIconTint, Mode.SRC_ATOP));
        canvas.drawBitmap(this.mIconBitmap, 0.0f, 0.0f, paint);
        paint.setColorFilter(null);
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.create("sans-serif", 0));
        paint.setColor(-1);
        paint.setTextSize(metrics.density * 16.0f);
        String index = String.format("%d", new Object[]{Integer.valueOf(this.mSimSlotIndex + 1)});
        Rect textBound = new Rect();
        paint.getTextBounds(index, 0, 1, textBound);
        canvas.drawText(index, (((float) width) / 2.0f) - ((float) textBound.centerX()), (((float) height) / 2.0f) - ((float) textBound.centerY()), paint);
        return workingBitmap;
    }

    public int getIconTint() {
        return this.mIconTint;
    }

    public void setIconTint(int iconTint) {
        this.mIconTint = iconTint;
    }

    public String getNumber() {
        return this.mNumber;
    }

    public int getDataRoaming() {
        return this.mDataRoaming;
    }

    public int getMcc() {
        return this.mMcc;
    }

    public int getMnc() {
        return this.mMnc;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public int getNwMode() {
        return this.mNwMode;
    }

    public String getCountryIso() {
        return this.mCountryIso;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeString(this.mIccId);
        dest.writeInt(this.mSimSlotIndex);
        dest.writeCharSequence(this.mDisplayName);
        dest.writeCharSequence(this.mCarrierName);
        dest.writeInt(this.mNameSource);
        dest.writeInt(this.mIconTint);
        dest.writeString(this.mNumber);
        dest.writeInt(this.mDataRoaming);
        dest.writeInt(this.mMcc);
        dest.writeInt(this.mMnc);
        dest.writeInt(this.mStatus);
        dest.writeInt(this.mNwMode);
        dest.writeString(this.mCountryIso);
        this.mIconBitmap.writeToParcel(dest, flags);
    }

    public int describeContents() {
        return 0;
    }

    public static String givePrintableIccid(String iccId) {
        if (iccId == null) {
            return null;
        }
        if (iccId.length() > 9) {
            return iccId.substring(0, 9) + "XXXXXXXXXXX";
        }
        return iccId;
    }

    public String toString() {
        return "{id=" + this.mId + ", iccId=" + givePrintableIccid(this.mIccId) + " simSlotIndex=" + this.mSimSlotIndex + " displayName=" + this.mDisplayName + " carrierName=" + this.mCarrierName + " nameSource=" + this.mNameSource + " iconTint=" + this.mIconTint + " dataRoaming=" + this.mDataRoaming + " iconBitmap=" + this.mIconBitmap + " mcc " + this.mMcc + " mnc " + this.mMnc + " mSubStatus=" + this.mStatus + " mNwMode=" + this.mNwMode + "}";
    }
}
