package com.unionpay.tsmservice.request;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import com.unionpay.tsmservice.data.NinePatchInfo;
import java.util.ArrayList;

public class SafetyKeyboardRequestParams extends RequestParams {
    public static final Parcelable.Creator<SafetyKeyboardRequestParams> CREATOR = new Parcelable.Creator<SafetyKeyboardRequestParams>() {
        public final SafetyKeyboardRequestParams createFromParcel(Parcel parcel) {
            return new SafetyKeyboardRequestParams(parcel);
        }

        public final SafetyKeyboardRequestParams[] newArray(int i) {
            return new SafetyKeyboardRequestParams[i];
        }
    };
    private int mConfirmBtnHeight;
    private int mConfirmBtnOutPaddingRight;
    private int mConfirmBtnWidth;
    private Bitmap mDelBgBitmap;
    private int mDelBgColor;
    private Bitmap mDelForeBitmap;
    private Bitmap mDoneBgBitmap;
    private int mDoneBgColor;
    private Bitmap mDoneForeBitmap;
    private int mDoneRight;
    private boolean mEnableLightStatusBar;
    private int mEnableOKBtn;
    private int mInnerPaddingBottom;
    private int mInnerPaddingLeft;
    private int mInnerPaddingRight;
    private int mInnerPaddingTop;
    private int mIsAudio;
    private int mIsDefaultPosition;
    private int mIsVibrate;
    private Bitmap mKeyboardBgBitmap;
    private int mKeyboardBgColor;
    private int mKeyboardHeight;
    private int mKeyboardWidth;
    private int mMarginCol;
    private int mMarginRow;
    private NinePatchInfo mNinePatchBackground;
    private NinePatchInfo mNinePatchDelKeyBg;
    private NinePatchInfo mNinePatchDoneKeyBg;
    private NinePatchInfo mNinePatchNumKeyBg;
    private NinePatchInfo mNinePatchTitleBg;
    private Bitmap mNumBgBitmap;
    private int mNumBgColor;
    private ArrayList<Bitmap> mNumForeBitmaps;
    private int mNumSize;
    private int mNumberKeyColor;
    private int mOutPaddingBottom;
    private int mOutPaddingLeft;
    private int mOutPaddingRight;
    private int mOutPaddingTop;
    private int mSecureHeight;
    private int mSecureWidth;
    private int mStartX;
    private int mStartY;
    private String mTitle;
    private Bitmap mTitleBgBitmap;
    private int mTitleBgColor;
    private int mTitleColor;
    private int mTitleDrawablePadding;
    private Bitmap mTitleDropBitmap;
    private int mTitleFont;
    private int mTitleHeight;
    private Bitmap mTitleIconBitmap;
    private int mTitleSize;

    public SafetyKeyboardRequestParams() {
        this.mKeyboardWidth = -1;
        this.mKeyboardHeight = -1;
        this.mTitleHeight = -1;
        this.mMarginRow = -1;
        this.mMarginCol = -1;
        this.mOutPaddingLeft = -1;
        this.mOutPaddingTop = -1;
        this.mOutPaddingRight = -1;
        this.mOutPaddingBottom = -1;
        this.mInnerPaddingLeft = -1;
        this.mInnerPaddingTop = -1;
        this.mInnerPaddingRight = -1;
        this.mInnerPaddingBottom = -1;
        this.mConfirmBtnOutPaddingRight = -1;
        this.mConfirmBtnWidth = -1;
        this.mConfirmBtnHeight = -1;
        this.mStartX = 0;
        this.mStartY = 0;
        this.mIsDefaultPosition = 1;
        this.mNumSize = -1;
        this.mKeyboardBgColor = -1;
        this.mTitleBgColor = -1;
        this.mDoneBgColor = -1;
        this.mDelBgColor = -1;
        this.mNumBgColor = -1;
        this.mIsAudio = 0;
        this.mEnableOKBtn = 1;
        this.mDoneRight = 0;
        this.mIsVibrate = 0;
        this.mSecureWidth = -1;
        this.mSecureHeight = -1;
        this.mTitleDrawablePadding = -1;
        this.mTitleColor = -1;
        this.mTitleSize = -1;
        this.mNumberKeyColor = -16777216;
        this.mEnableLightStatusBar = false;
    }

    public SafetyKeyboardRequestParams(Parcel parcel) {
        super(parcel);
        this.mKeyboardWidth = -1;
        this.mKeyboardHeight = -1;
        this.mTitleHeight = -1;
        this.mMarginRow = -1;
        this.mMarginCol = -1;
        this.mOutPaddingLeft = -1;
        this.mOutPaddingTop = -1;
        this.mOutPaddingRight = -1;
        this.mOutPaddingBottom = -1;
        this.mInnerPaddingLeft = -1;
        this.mInnerPaddingTop = -1;
        this.mInnerPaddingRight = -1;
        this.mInnerPaddingBottom = -1;
        this.mConfirmBtnOutPaddingRight = -1;
        this.mConfirmBtnWidth = -1;
        this.mConfirmBtnHeight = -1;
        boolean z = false;
        this.mStartX = 0;
        this.mStartY = 0;
        this.mIsDefaultPosition = 1;
        this.mNumSize = -1;
        this.mKeyboardBgColor = -1;
        this.mTitleBgColor = -1;
        this.mDoneBgColor = -1;
        this.mDelBgColor = -1;
        this.mNumBgColor = -1;
        this.mIsAudio = 0;
        this.mEnableOKBtn = 1;
        this.mDoneRight = 0;
        this.mIsVibrate = 0;
        this.mSecureWidth = -1;
        this.mSecureHeight = -1;
        this.mTitleDrawablePadding = -1;
        this.mTitleColor = -1;
        this.mTitleSize = -1;
        this.mNumberKeyColor = -16777216;
        this.mEnableLightStatusBar = false;
        this.mTitle = parcel.readString();
        this.mKeyboardWidth = parcel.readInt();
        this.mKeyboardHeight = parcel.readInt();
        this.mTitleHeight = parcel.readInt();
        this.mMarginRow = parcel.readInt();
        this.mMarginCol = parcel.readInt();
        this.mOutPaddingLeft = parcel.readInt();
        this.mOutPaddingTop = parcel.readInt();
        this.mOutPaddingRight = parcel.readInt();
        this.mOutPaddingBottom = parcel.readInt();
        this.mInnerPaddingLeft = parcel.readInt();
        this.mInnerPaddingTop = parcel.readInt();
        this.mInnerPaddingRight = parcel.readInt();
        this.mInnerPaddingBottom = parcel.readInt();
        this.mConfirmBtnOutPaddingRight = parcel.readInt();
        this.mConfirmBtnWidth = parcel.readInt();
        this.mConfirmBtnHeight = parcel.readInt();
        this.mStartX = parcel.readInt();
        this.mStartY = parcel.readInt();
        this.mIsDefaultPosition = parcel.readInt();
        this.mNumSize = parcel.readInt();
        this.mKeyboardBgBitmap = (Bitmap) parcel.readParcelable(Bitmap.class.getClassLoader());
        this.mTitleBgBitmap = (Bitmap) parcel.readParcelable(Bitmap.class.getClassLoader());
        this.mTitleIconBitmap = (Bitmap) parcel.readParcelable(Bitmap.class.getClassLoader());
        this.mTitleDropBitmap = (Bitmap) parcel.readParcelable(Bitmap.class.getClassLoader());
        this.mDoneForeBitmap = (Bitmap) parcel.readParcelable(Bitmap.class.getClassLoader());
        this.mDoneBgBitmap = (Bitmap) parcel.readParcelable(Bitmap.class.getClassLoader());
        this.mDelForeBitmap = (Bitmap) parcel.readParcelable(Bitmap.class.getClassLoader());
        this.mDelBgBitmap = (Bitmap) parcel.readParcelable(Bitmap.class.getClassLoader());
        this.mNumBgBitmap = (Bitmap) parcel.readParcelable(Bitmap.class.getClassLoader());
        this.mNumForeBitmaps = parcel.readArrayList(ArrayList.class.getClassLoader());
        this.mKeyboardBgColor = parcel.readInt();
        this.mTitleBgColor = parcel.readInt();
        this.mDoneBgColor = parcel.readInt();
        this.mDelBgColor = parcel.readInt();
        this.mNumBgColor = parcel.readInt();
        this.mIsAudio = parcel.readInt();
        this.mEnableOKBtn = parcel.readInt();
        this.mDoneRight = parcel.readInt();
        this.mIsVibrate = parcel.readInt();
        this.mSecureWidth = parcel.readInt();
        this.mSecureHeight = parcel.readInt();
        this.mTitleDrawablePadding = parcel.readInt();
        this.mTitleColor = parcel.readInt();
        this.mTitleSize = parcel.readInt();
        this.mNumberKeyColor = parcel.readInt();
        this.mNinePatchBackground = (NinePatchInfo) parcel.readParcelable(NinePatchInfo.class.getClassLoader());
        this.mNinePatchDelKeyBg = (NinePatchInfo) parcel.readParcelable(NinePatchInfo.class.getClassLoader());
        this.mNinePatchDoneKeyBg = (NinePatchInfo) parcel.readParcelable(NinePatchInfo.class.getClassLoader());
        this.mNinePatchNumKeyBg = (NinePatchInfo) parcel.readParcelable(NinePatchInfo.class.getClassLoader());
        this.mNinePatchTitleBg = (NinePatchInfo) parcel.readParcelable(NinePatchInfo.class.getClassLoader());
        this.mEnableLightStatusBar = parcel.readInt() == 1 ? true : z;
    }

    public int getConfirmBtnHeight() {
        return this.mConfirmBtnHeight;
    }

    public int getConfirmBtnOutPaddingRight() {
        return this.mConfirmBtnOutPaddingRight;
    }

    public int getConfirmBtnWidth() {
        return this.mConfirmBtnWidth;
    }

    public int getDefaultPosition() {
        return this.mIsDefaultPosition;
    }

    public Bitmap getDelBgBitmap() {
        return this.mDelBgBitmap;
    }

    public int getDelBgColor() {
        return this.mDelBgColor;
    }

    public Bitmap getDelForeBitmap() {
        return this.mDelForeBitmap;
    }

    public NinePatchInfo getDelKeyBgNinePatch() {
        return this.mNinePatchDelKeyBg;
    }

    public Bitmap getDoneBgBitmap() {
        return this.mDoneBgBitmap;
    }

    public int getDoneBgColor() {
        return this.mDoneBgColor;
    }

    public Bitmap getDoneForeBitmap() {
        return this.mDoneForeBitmap;
    }

    public NinePatchInfo getDoneKeyBgNinePatch() {
        return this.mNinePatchDoneKeyBg;
    }

    public int getDoneRight() {
        return this.mDoneRight;
    }

    public int getEnableOKBtn() {
        return this.mEnableOKBtn;
    }

    public int getInnerPaddingBottom() {
        return this.mInnerPaddingBottom;
    }

    public int getInnerPaddingLeft() {
        return this.mInnerPaddingLeft;
    }

    public int getInnerPaddingRight() {
        return this.mInnerPaddingRight;
    }

    public int getInnerPaddingTop() {
        return this.mInnerPaddingTop;
    }

    public int getIsAudio() {
        return this.mIsAudio;
    }

    public int getIsVibrate() {
        return this.mIsVibrate;
    }

    public Bitmap getKeyboardBgBitmap() {
        return this.mKeyboardBgBitmap;
    }

    public int getKeyboardBgColor() {
        return this.mKeyboardBgColor;
    }

    public NinePatchInfo getKeyboardBgNinePatch() {
        return this.mNinePatchBackground;
    }

    public int getKeyboardHeight() {
        return this.mKeyboardHeight;
    }

    public int getKeyboardWidth() {
        return this.mKeyboardWidth;
    }

    public int getMarginCol() {
        return this.mMarginCol;
    }

    public int getMarginRow() {
        return this.mMarginRow;
    }

    public Bitmap getNumBgBitmap() {
        return this.mNumBgBitmap;
    }

    public int getNumBgColor() {
        return this.mNumBgColor;
    }

    public ArrayList<Bitmap> getNumForeBitmaps() {
        return this.mNumForeBitmaps;
    }

    public NinePatchInfo getNumKeyBgNinePatch() {
        return this.mNinePatchNumKeyBg;
    }

    public int getNumSize() {
        return this.mNumSize;
    }

    public int getNumberKeyColor() {
        return this.mNumberKeyColor;
    }

    public int getOutPaddingBottom() {
        return this.mOutPaddingBottom;
    }

    public int getOutPaddingLeft() {
        return this.mOutPaddingLeft;
    }

    public int getOutPaddingRight() {
        return this.mOutPaddingRight;
    }

    public int getOutPaddingTop() {
        return this.mOutPaddingTop;
    }

    public int getSecureHeight() {
        return this.mSecureHeight;
    }

    public int getSecureWidth() {
        return this.mSecureWidth;
    }

    public int getStartX() {
        return this.mStartX;
    }

    public int getStartY() {
        return this.mStartY;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public Bitmap getTitleBgBitmap() {
        return this.mTitleBgBitmap;
    }

    public int getTitleBgColor() {
        return this.mTitleBgColor;
    }

    public NinePatchInfo getTitleBgNinePatch() {
        return this.mNinePatchTitleBg;
    }

    public int getTitleColor() {
        return this.mTitleColor;
    }

    public int getTitleDrawablePadding() {
        return this.mTitleDrawablePadding;
    }

    public Bitmap getTitleDropBitmap() {
        return this.mTitleDropBitmap;
    }

    public int getTitleFont() {
        return this.mTitleFont;
    }

    public int getTitleHeight() {
        return this.mTitleHeight;
    }

    public Bitmap getTitleIconBitmap() {
        return this.mTitleIconBitmap;
    }

    public int getTitleSize() {
        return this.mTitleSize;
    }

    public boolean isEnableLightStatusBar() {
        return this.mEnableLightStatusBar;
    }

    public void setConfirmBtnHeight(int i) {
        this.mConfirmBtnHeight = i;
    }

    public void setConfirmBtnOutPaddingRight(int i) {
        this.mConfirmBtnOutPaddingRight = i;
    }

    public void setConfirmBtnWidth(int i) {
        this.mConfirmBtnWidth = i;
    }

    public void setDefaultPosition(int i) {
        this.mIsDefaultPosition = i;
    }

    public void setDelBgBitmap(Bitmap bitmap) {
        this.mDelBgBitmap = bitmap;
    }

    public void setDelBgColor(int i) {
        this.mDelBgColor = i;
    }

    public void setDelForeBitmap(Bitmap bitmap) {
        this.mDelForeBitmap = bitmap;
    }

    public void setDelKeyBgNinePatch(NinePatchInfo ninePatchInfo) {
        this.mNinePatchDelKeyBg = ninePatchInfo;
    }

    public void setDoneBgBitmap(Bitmap bitmap) {
        this.mDoneBgBitmap = bitmap;
    }

    public void setDoneBgColor(int i) {
        this.mDoneBgColor = i;
    }

    public void setDoneForeBitmap(Bitmap bitmap) {
        this.mDoneForeBitmap = bitmap;
    }

    public void setDoneKeyBgNinePatch(NinePatchInfo ninePatchInfo) {
        this.mNinePatchDoneKeyBg = ninePatchInfo;
    }

    public void setDoneRight(int i) {
        this.mDoneRight = i;
    }

    public void setEnableLightStatusBar(boolean z) {
        this.mEnableLightStatusBar = z;
    }

    public void setEnableOKBtn(int i) {
        this.mEnableOKBtn = i;
    }

    public void setInnerPaddingBottom(int i) {
        this.mInnerPaddingBottom = i;
    }

    public void setInnerPaddingLeft(int i) {
        this.mInnerPaddingLeft = i;
    }

    public void setInnerPaddingRight(int i) {
        this.mInnerPaddingRight = i;
    }

    public void setInnerPaddingTop(int i) {
        this.mInnerPaddingTop = i;
    }

    public void setIsAudio(int i) {
        this.mIsAudio = i;
    }

    public void setIsVibrate(int i) {
        this.mIsVibrate = i;
    }

    public void setKeyboardBgBitmap(Bitmap bitmap) {
        this.mKeyboardBgBitmap = bitmap;
    }

    public void setKeyboardBgColor(int i) {
        this.mKeyboardBgColor = i;
    }

    public void setKeyboardBgNinePatch(NinePatchInfo ninePatchInfo) {
        this.mNinePatchBackground = ninePatchInfo;
    }

    public void setKeyboardHeight(int i) {
        this.mKeyboardHeight = i;
    }

    public void setKeyboardWidth(int i) {
        this.mKeyboardWidth = i;
    }

    public void setMarginCol(int i) {
        this.mMarginCol = i;
    }

    public void setMarginRow(int i) {
        this.mMarginRow = i;
    }

    public void setNumBgBitmap(Bitmap bitmap) {
        this.mNumBgBitmap = bitmap;
    }

    public void setNumBgColor(int i) {
        this.mNumBgColor = i;
    }

    public void setNumForeBitmaps(ArrayList<Bitmap> arrayList) {
        this.mNumForeBitmaps = arrayList;
    }

    public void setNumKeyBgNinePatch(NinePatchInfo ninePatchInfo) {
        this.mNinePatchNumKeyBg = ninePatchInfo;
    }

    public void setNumSize(int i) {
        this.mNumSize = i;
    }

    public void setNumberKeyColor(int i) {
        this.mNumberKeyColor = i;
    }

    public void setOutPaddingBottom(int i) {
        this.mOutPaddingBottom = i;
    }

    public void setOutPaddingLeft(int i) {
        this.mOutPaddingLeft = i;
    }

    public void setOutPaddingRight(int i) {
        this.mOutPaddingRight = i;
    }

    public void setOutPaddingTop(int i) {
        this.mOutPaddingTop = i;
    }

    public void setSecureHeight(int i) {
        this.mSecureHeight = i;
    }

    public void setSecureWidth(int i) {
        this.mSecureWidth = i;
    }

    public void setStartX(int i) {
        this.mStartX = i;
    }

    public void setStartY(int i) {
        this.mStartY = i;
    }

    public void setTitle(String str) {
        this.mTitle = str;
    }

    public void setTitleBgBitmap(Bitmap bitmap) {
        this.mTitleBgBitmap = bitmap;
    }

    public void setTitleBgColor(int i) {
        this.mTitleBgColor = i;
    }

    public void setTitleBgNinePatch(NinePatchInfo ninePatchInfo) {
        this.mNinePatchTitleBg = ninePatchInfo;
    }

    public void setTitleColor(int i) {
        this.mTitleColor = i;
    }

    public void setTitleDrawablePadding(int i) {
        this.mTitleDrawablePadding = i;
    }

    public void setTitleDropBitmap(Bitmap bitmap) {
        this.mTitleDropBitmap = bitmap;
    }

    public void setTitleFont(int i) {
        this.mTitleFont = i;
    }

    public void setTitleHeight(int i) {
        this.mTitleHeight = i;
    }

    public void setTitleIconBitmap(Bitmap bitmap) {
        this.mTitleIconBitmap = bitmap;
    }

    public void setTitleSize(int i) {
        this.mTitleSize = i;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(this.mTitle);
        parcel.writeInt(this.mKeyboardWidth);
        parcel.writeInt(this.mKeyboardHeight);
        parcel.writeInt(this.mTitleHeight);
        parcel.writeInt(this.mMarginRow);
        parcel.writeInt(this.mMarginCol);
        parcel.writeInt(this.mOutPaddingLeft);
        parcel.writeInt(this.mOutPaddingTop);
        parcel.writeInt(this.mOutPaddingRight);
        parcel.writeInt(this.mOutPaddingBottom);
        parcel.writeInt(this.mInnerPaddingLeft);
        parcel.writeInt(this.mInnerPaddingTop);
        parcel.writeInt(this.mInnerPaddingRight);
        parcel.writeInt(this.mInnerPaddingBottom);
        parcel.writeInt(this.mConfirmBtnOutPaddingRight);
        parcel.writeInt(this.mConfirmBtnWidth);
        parcel.writeInt(this.mConfirmBtnHeight);
        parcel.writeInt(this.mStartX);
        parcel.writeInt(this.mStartY);
        parcel.writeInt(this.mIsDefaultPosition);
        parcel.writeInt(this.mNumSize);
        parcel.writeParcelable(this.mKeyboardBgBitmap, 0);
        parcel.writeParcelable(this.mTitleBgBitmap, 0);
        parcel.writeParcelable(this.mTitleIconBitmap, 0);
        parcel.writeParcelable(this.mTitleDropBitmap, 0);
        parcel.writeParcelable(this.mDoneForeBitmap, 0);
        parcel.writeParcelable(this.mDoneBgBitmap, 0);
        parcel.writeParcelable(this.mDelForeBitmap, 0);
        parcel.writeParcelable(this.mDelBgBitmap, 0);
        parcel.writeParcelable(this.mNumBgBitmap, 0);
        parcel.writeList(this.mNumForeBitmaps);
        parcel.writeInt(this.mKeyboardBgColor);
        parcel.writeInt(this.mTitleBgColor);
        parcel.writeInt(this.mDoneBgColor);
        parcel.writeInt(this.mDelBgColor);
        parcel.writeInt(this.mNumBgColor);
        parcel.writeInt(this.mIsAudio);
        parcel.writeInt(this.mEnableOKBtn);
        parcel.writeInt(this.mDoneRight);
        parcel.writeInt(this.mIsVibrate);
        parcel.writeInt(this.mSecureWidth);
        parcel.writeInt(this.mSecureHeight);
        parcel.writeInt(this.mTitleDrawablePadding);
        parcel.writeInt(this.mTitleColor);
        parcel.writeInt(this.mTitleSize);
        parcel.writeInt(this.mNumberKeyColor);
        parcel.writeParcelable(this.mNinePatchBackground, i);
        parcel.writeParcelable(this.mNinePatchDelKeyBg, i);
        parcel.writeParcelable(this.mNinePatchDoneKeyBg, i);
        parcel.writeParcelable(this.mNinePatchNumKeyBg, i);
        parcel.writeParcelable(this.mNinePatchTitleBg, i);
        int i2 = 1;
        if (!this.mEnableLightStatusBar) {
            i2 = 0;
        }
        parcel.writeInt(i2);
    }
}
