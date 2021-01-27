package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Bitmap;
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
        this.mIconsCache = new HashMap<>(50);
    }

    static IconLoader getInstance(Handler caller, IccFileHandler fh) {
        IconLoader iconLoader = sLoader;
        if (iconLoader != null) {
            return iconLoader;
        }
        if (fh == null) {
            return null;
        }
        sThread = new HandlerThread("Cat Icon Loader");
        sThread.start();
        return new IconLoader(sThread.getLooper(), fh);
    }

    /* access modifiers changed from: package-private */
    public void loadIcons(int[] recordNumbers, Message msg) {
        if (recordNumbers != null && recordNumbers.length != 0 && msg != null) {
            this.mEndMsg = msg;
            this.mIcons = new Bitmap[recordNumbers.length];
            this.mRecordNumbers = recordNumbers;
            this.mCurrentRecordIndex = 0;
            this.mState = 2;
            startLoadingIcon(recordNumbers[0]);
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void loadIcon(int recordNumber, Message msg) {
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
            this.mCurrentIcon = this.mIconsCache.get(Integer.valueOf(recordNumber));
            postIcon();
            return;
        }
        readId();
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        try {
            int i = msg.what;
            if (i != 1) {
                if (i == 2) {
                    CatLog.d(this, "load icon done");
                    byte[] rawData = (byte[]) ((AsyncResult) msg.obj).result;
                    if (this.mId.mCodingScheme == 17) {
                        this.mCurrentIcon = parseToBnW(rawData, rawData.length);
                        this.mIconsCache.put(Integer.valueOf(this.mRecordNumber), this.mCurrentIcon);
                        postIcon();
                    } else if (this.mId.mCodingScheme == 33) {
                        this.mIconData = rawData;
                        readClut();
                    } else {
                        CatLog.d(this, "else  /postIcon ");
                        postIcon();
                    }
                } else if (i == 3) {
                    this.mCurrentIcon = parseToRGB(this.mIconData, this.mIconData.length, false, (byte[]) ((AsyncResult) msg.obj).result);
                    this.mIconsCache.put(Integer.valueOf(this.mRecordNumber), this.mCurrentIcon);
                    postIcon();
                }
            } else if (handleImageDescriptor((byte[]) ((AsyncResult) msg.obj).result)) {
                readIconData();
            } else {
                throw new Exception("Unable to parse image descriptor");
            }
        } catch (Exception e) {
            CatLog.d(this, "Icon load failed");
            postIcon();
        }
    }

    private boolean handleImageDescriptor(byte[] rawData) {
        if (rawData[0] <= 0) {
            CatLog.d(this, "handleImageDescriptor, then length is wrong :" + ((int) rawData[0]));
            return false;
        }
        this.mId = ImageDescriptor.parse(rawData, 1);
        if (this.mId == null) {
            return false;
        }
        return true;
    }

    private void readClut() {
        Message msg = obtainMessage(3);
        IccFileHandler iccFileHandler = this.mSimFH;
        int i = this.mId.mImageId;
        byte[] bArr = this.mIconData;
        iccFileHandler.loadEFImgTransparent(i, bArr[4], bArr[5], this.mIconData[3] * 3, msg);
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
        this.mSimFH.loadEFImgTransparent(this.mId.mImageId, 0, 0, this.mId.mLength, obtainMessage(2));
    }

    private void postIcon() {
        int i = this.mState;
        if (i == 1) {
            Message message = this.mEndMsg;
            message.obj = this.mCurrentIcon;
            message.sendToTarget();
        } else if (i == 2) {
            Bitmap[] bitmapArr = this.mIcons;
            int i2 = this.mCurrentRecordIndex;
            this.mCurrentRecordIndex = i2 + 1;
            bitmapArr[i2] = this.mCurrentIcon;
            int i3 = this.mCurrentRecordIndex;
            int[] iArr = this.mRecordNumbers;
            if (i3 < iArr.length) {
                startLoadingIcon(iArr[i3]);
                return;
            }
            Message message2 = this.mEndMsg;
            message2.obj = bitmapArr;
            message2.sendToTarget();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r8v4 int: [D('valueIndex' int), D('pixelIndex' int)] */
    /* JADX INFO: Multiple debug info for r2v3 byte: [D('currentByte' byte), D('valueIndex' int)] */
    public static Bitmap parseToBnW(byte[] data, int length) {
        int valueIndex = 0 + 1;
        int width = data[0] & 255;
        int valueIndex2 = valueIndex + 1;
        int height = data[valueIndex] & 255;
        int numOfPixels = width * height;
        int[] pixels = new int[numOfPixels];
        int pixelIndex = 0;
        byte bitIndex = 7;
        byte currentByte = 0;
        while (pixelIndex < numOfPixels) {
            if (pixelIndex % 8 == 0) {
                bitIndex = 7;
                currentByte = data[valueIndex2];
                valueIndex2++;
            }
            pixels[pixelIndex] = bitToBnW((currentByte >> bitIndex) & 1);
            pixelIndex++;
            bitIndex--;
        }
        if (pixelIndex != numOfPixels) {
            CatLog.d("IconLoader", "parseToBnW; size error");
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }

    private static int bitToBnW(int bit) {
        if (bit == 1) {
            return -1;
        }
        return -16777216;
    }

    public static Bitmap parseToRGB(byte[] data, int length, boolean transparency, byte[] clut) {
        int valueIndex = 0 + 1;
        int width = data[0] & 255;
        int valueIndex2 = valueIndex + 1;
        int height = data[valueIndex] & 255;
        int valueIndex3 = valueIndex2 + 1;
        int bitsPerImg = data[valueIndex2] & 255;
        int i = valueIndex3 + 1;
        int numOfClutEntries = data[valueIndex3] & 255;
        boolean bitsOverlaps = false;
        if (true == transparency) {
            clut[numOfClutEntries - 1] = 0;
        }
        int numOfPixels = width * height;
        int[] pixels = new int[numOfPixels];
        int pixelIndex = 0;
        int bitsStartOffset = 8 - bitsPerImg;
        int bitIndex = bitsStartOffset;
        int valueIndex4 = 6 + 1;
        byte currentByte = data[6];
        int mask = getMask(bitsPerImg);
        if (8 % bitsPerImg == 0) {
            bitsOverlaps = true;
        }
        while (pixelIndex < numOfPixels) {
            if (bitIndex < 0) {
                int valueIndex5 = valueIndex4 + 1;
                currentByte = data[valueIndex4];
                bitIndex = bitsOverlaps ? bitsStartOffset : bitIndex * -1;
                valueIndex4 = valueIndex5;
            }
            int clutIndex = ((currentByte >> bitIndex) & mask) * 3;
            pixels[pixelIndex] = Color.rgb((int) clut[clutIndex], (int) clut[clutIndex + 1], (int) clut[clutIndex + 2]);
            bitIndex -= bitsPerImg;
            pixelIndex++;
            numOfClutEntries = numOfClutEntries;
            currentByte = currentByte;
            bitsOverlaps = bitsOverlaps;
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
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
        HandlerThread handlerThread = sThread;
        if (handlerThread != null) {
            handlerThread.quit();
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
