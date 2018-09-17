package com.android.internal.telephony.cat;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.android.internal.telephony.uicc.IccFileHandler;
import java.util.HashMap;

class IconLoader extends Handler {
    private static final int CLUT_ENTRY_SIZE = 3;
    private static final int CLUT_LOCATION_OFFSET = 4;
    private static final int EVENT_READ_CLUT_DONE = 3;
    private static final int EVENT_READ_EF_IMG_RECOED_DONE = 1;
    private static final int EVENT_READ_ICON_DONE = 2;
    private static final int STATE_MULTI_ICONS = 2;
    private static final int STATE_SINGLE_ICON = 1;
    private static IconLoader sLoader = null;
    private static HandlerThread sThread = null;
    private Bitmap mCurrentIcon = null;
    private int mCurrentRecordIndex = 0;
    private Message mEndMsg = null;
    private byte[] mIconData = null;
    private Bitmap[] mIcons = null;
    private HashMap<Integer, Bitmap> mIconsCache = null;
    private ImageDescriptor mId = null;
    private int mRecordNumber;
    private int[] mRecordNumbers = null;
    private IccFileHandler mSimFH = null;
    private int mState = 1;

    private IconLoader(Looper looper, IccFileHandler fh) {
        super(looper);
        this.mSimFH = fh;
        this.mIconsCache = new HashMap(50);
    }

    static IconLoader getInstance(Handler caller, IccFileHandler fh) {
        if (sLoader != null) {
            return sLoader;
        }
        if (fh == null) {
            return null;
        }
        sThread = new HandlerThread("Cat Icon Loader");
        sThread.start();
        return new IconLoader(sThread.getLooper(), fh);
    }

    void loadIcons(int[] recordNumbers, Message msg) {
        if (recordNumbers != null && recordNumbers.length != 0 && msg != null) {
            this.mEndMsg = msg;
            this.mIcons = new Bitmap[recordNumbers.length];
            this.mRecordNumbers = recordNumbers;
            this.mCurrentRecordIndex = 0;
            this.mState = 2;
            startLoadingIcon(recordNumbers[0]);
        }
    }

    void loadIcon(int recordNumber, Message msg) {
        if (msg != null) {
            this.mEndMsg = msg;
            this.mState = 1;
            startLoadingIcon(recordNumber);
        }
    }

    private void startLoadingIcon(int recordNumber) {
        this.mId = null;
        this.mIconData = null;
        this.mCurrentIcon = null;
        this.mRecordNumber = recordNumber;
        if (this.mIconsCache.containsKey(Integer.valueOf(recordNumber))) {
            this.mCurrentIcon = (Bitmap) this.mIconsCache.get(Integer.valueOf(recordNumber));
            postIcon();
            return;
        }
        readId();
    }

    public void handleMessage(Message msg) {
        try {
            switch (msg.what) {
                case 1:
                    if (handleImageDescriptor((byte[]) msg.obj.result)) {
                        readIconData();
                        return;
                    }
                    throw new Exception("Unable to parse image descriptor");
                case 2:
                    CatLog.d((Object) this, "load icon done");
                    byte[] rawData = ((AsyncResult) msg.obj).result;
                    if (this.mId.mCodingScheme == 17) {
                        this.mCurrentIcon = parseToBnW(rawData, rawData.length);
                        this.mIconsCache.put(Integer.valueOf(this.mRecordNumber), this.mCurrentIcon);
                        postIcon();
                        return;
                    } else if (this.mId.mCodingScheme == 33) {
                        this.mIconData = rawData;
                        readClut();
                        return;
                    } else {
                        CatLog.d((Object) this, "else  /postIcon ");
                        postIcon();
                        return;
                    }
                case 3:
                    this.mCurrentIcon = parseToRGB(this.mIconData, this.mIconData.length, false, ((AsyncResult) msg.obj).result);
                    this.mIconsCache.put(Integer.valueOf(this.mRecordNumber), this.mCurrentIcon);
                    postIcon();
                    return;
                default:
                    return;
            }
        } catch (Exception e) {
            CatLog.d((Object) this, "Icon load failed");
            postIcon();
        }
        CatLog.d((Object) this, "Icon load failed");
        postIcon();
    }

    private boolean handleImageDescriptor(byte[] rawData) {
        if (rawData[0] <= (byte) 0) {
            CatLog.d((Object) this, "handleImageDescriptor, then length is wrong :" + rawData[0]);
            return false;
        }
        this.mId = ImageDescriptor.parse(rawData, 1);
        return this.mId != null;
    }

    private void readClut() {
        this.mSimFH.loadEFImgTransparent(this.mId.mImageId, this.mIconData[4], this.mIconData[5], this.mIconData[3] * 3, obtainMessage(3));
    }

    private void readId() {
        if (this.mRecordNumber < 0) {
            this.mCurrentIcon = null;
            postIcon();
            return;
        }
        this.mSimFH.loadEFImgLinearFixed(this.mRecordNumber, obtainMessage(1));
    }

    private void readIconData() {
        Message msg = obtainMessage(2);
        this.mSimFH.loadEFImgTransparent(this.mId.mImageId, 0, 0, this.mId.mLength, msg);
    }

    private void postIcon() {
        if (this.mState == 1) {
            this.mEndMsg.obj = this.mCurrentIcon;
            this.mEndMsg.sendToTarget();
        } else if (this.mState == 2) {
            Bitmap[] bitmapArr = this.mIcons;
            int i = this.mCurrentRecordIndex;
            this.mCurrentRecordIndex = i + 1;
            bitmapArr[i] = this.mCurrentIcon;
            if (this.mCurrentRecordIndex < this.mRecordNumbers.length) {
                startLoadingIcon(this.mRecordNumbers[this.mCurrentRecordIndex]);
                return;
            }
            this.mEndMsg.obj = this.mIcons;
            this.mEndMsg.sendToTarget();
        }
    }

    public static Bitmap parseToBnW(byte[] data, int length) {
        int width = data[0] & 255;
        int valueIndex = 1 + 1;
        int height = data[1] & 255;
        int numOfPixels = width * height;
        int[] pixels = new int[numOfPixels];
        int bitIndex = 7;
        byte currentByte = (byte) 0;
        int pixelIndex = 0;
        while (pixelIndex < numOfPixels) {
            int valueIndex2;
            if (pixelIndex % 8 == 0) {
                valueIndex2 = valueIndex + 1;
                currentByte = data[valueIndex];
                bitIndex = 7;
            } else {
                valueIndex2 = valueIndex;
            }
            int pixelIndex2 = pixelIndex + 1;
            int bitIndex2 = bitIndex - 1;
            pixels[pixelIndex] = bitToBnW((currentByte >> bitIndex) & 1);
            bitIndex = bitIndex2;
            pixelIndex = pixelIndex2;
            valueIndex = valueIndex2;
        }
        if (pixelIndex != numOfPixels) {
            CatLog.d("IconLoader", "parseToBnW; size error");
        }
        return Bitmap.createBitmap(pixels, width, height, Config.ARGB_8888);
    }

    private static int bitToBnW(int bit) {
        if (bit == 1) {
            return -1;
        }
        return -16777216;
    }

    public static Bitmap parseToRGB(byte[] data, int length, boolean transparency, byte[] clut) {
        boolean bitsOverlaps;
        int pixelIndex;
        int width = data[0] & 255;
        int valueIndex = 1 + 1;
        int height = data[1] & 255;
        int valueIndex2 = valueIndex + 1;
        int bitsPerImg = data[valueIndex] & 255;
        valueIndex = valueIndex2 + 1;
        int numOfClutEntries = data[valueIndex2] & 255;
        if (transparency) {
            clut[numOfClutEntries - 1] = (byte) 0;
        }
        int numOfPixels = width * height;
        int[] pixels = new int[numOfPixels];
        int bitsStartOffset = 8 - bitsPerImg;
        int bitIndex = bitsStartOffset;
        byte currentByte = data[6];
        int mask = getMask(bitsPerImg);
        if (8 % bitsPerImg == 0) {
            bitsOverlaps = true;
            pixelIndex = 0;
            valueIndex = 7;
        } else {
            bitsOverlaps = false;
            pixelIndex = 0;
            valueIndex = 7;
        }
        while (pixelIndex < numOfPixels) {
            if (bitIndex < 0) {
                valueIndex2 = valueIndex + 1;
                currentByte = data[valueIndex];
                bitIndex = bitsOverlaps ? bitsStartOffset : bitIndex * -1;
            } else {
                valueIndex2 = valueIndex;
            }
            int clutIndex = ((currentByte >> bitIndex) & mask) * 3;
            int pixelIndex2 = pixelIndex + 1;
            pixels[pixelIndex] = Color.rgb(clut[clutIndex], clut[clutIndex + 1], clut[clutIndex + 2]);
            bitIndex -= bitsPerImg;
            pixelIndex = pixelIndex2;
            valueIndex = valueIndex2;
        }
        return Bitmap.createBitmap(pixels, width, height, Config.ARGB_8888);
    }

    private static int getMask(int numOfBits) {
        switch (numOfBits) {
            case 1:
                return 1;
            case 2:
                return 3;
            case 3:
                return 7;
            case 4:
                return 15;
            case 5:
                return 31;
            case 6:
                return 63;
            case 7:
                return 127;
            case 8:
                return 255;
            default:
                return 0;
        }
    }

    public void dispose() {
        this.mSimFH = null;
        if (sThread != null) {
            sThread.quit();
            sThread = null;
        }
        Looper looper = getLooper();
        if (looper != null) {
            looper.quit();
        }
        this.mIconsCache = null;
        sLoader = null;
    }
}
