package com.android.internal.telephony.cat;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.android.internal.telephony.CallFailCause;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduHeaders;
import java.util.HashMap;

class IconLoader extends Handler {
    private static final int CLUT_ENTRY_SIZE = 3;
    private static final int CLUT_LOCATION_OFFSET = 4;
    private static final int EVENT_READ_CLUT_DONE = 3;
    private static final int EVENT_READ_EF_IMG_RECOED_DONE = 1;
    private static final int EVENT_READ_ICON_DONE = 2;
    private static final int STATE_MULTI_ICONS = 2;
    private static final int STATE_SINGLE_ICON = 1;
    private static IconLoader sLoader;
    private static HandlerThread sThread;
    private Bitmap mCurrentIcon;
    private int mCurrentRecordIndex;
    private Message mEndMsg;
    private byte[] mIconData;
    private Bitmap[] mIcons;
    private HashMap<Integer, Bitmap> mIconsCache;
    private ImageDescriptor mId;
    private int mRecordNumber;
    private int[] mRecordNumbers;
    private IccFileHandler mSimFH;
    private int mState;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cat.IconLoader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cat.IconLoader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.IconLoader.<clinit>():void");
    }

    private IconLoader(Looper looper, IccFileHandler fh) {
        super(looper);
        this.mState = STATE_SINGLE_ICON;
        this.mId = null;
        this.mCurrentIcon = null;
        this.mSimFH = null;
        this.mEndMsg = null;
        this.mIconData = null;
        this.mRecordNumbers = null;
        this.mCurrentRecordIndex = 0;
        this.mIcons = null;
        this.mIconsCache = null;
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
            this.mState = STATE_MULTI_ICONS;
            startLoadingIcon(recordNumbers[0]);
        }
    }

    void loadIcon(int recordNumber, Message msg) {
        if (msg != null) {
            this.mEndMsg = msg;
            this.mState = STATE_SINGLE_ICON;
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
                case STATE_SINGLE_ICON /*1*/:
                    if (handleImageDescriptor((byte[]) msg.obj.result)) {
                        readIconData();
                        return;
                    }
                    throw new Exception("Unable to parse image descriptor");
                case STATE_MULTI_ICONS /*2*/:
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
                case EVENT_READ_CLUT_DONE /*3*/:
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
        if (rawData[0] <= null) {
            CatLog.d((Object) this, "handleImageDescriptor, then length is wrong :" + rawData[0]);
            return false;
        }
        this.mId = ImageDescriptor.parse(rawData, STATE_SINGLE_ICON);
        return this.mId != null;
    }

    private void readClut() {
        this.mSimFH.loadEFImgTransparent(this.mId.mImageId, this.mIconData[CLUT_LOCATION_OFFSET], this.mIconData[5], this.mIconData[EVENT_READ_CLUT_DONE] * EVENT_READ_CLUT_DONE, obtainMessage(EVENT_READ_CLUT_DONE));
    }

    private void readId() {
        if (this.mRecordNumber < 0) {
            this.mCurrentIcon = null;
            postIcon();
            return;
        }
        this.mSimFH.loadEFImgLinearFixed(this.mRecordNumber, obtainMessage(STATE_SINGLE_ICON));
    }

    private void readIconData() {
        Message msg = obtainMessage(STATE_MULTI_ICONS);
        this.mSimFH.loadEFImgTransparent(this.mId.mImageId, 0, 0, this.mId.mLength, msg);
    }

    private void postIcon() {
        if (this.mState == STATE_SINGLE_ICON) {
            this.mEndMsg.obj = this.mCurrentIcon;
            this.mEndMsg.sendToTarget();
        } else if (this.mState == STATE_MULTI_ICONS) {
            Bitmap[] bitmapArr = this.mIcons;
            int i = this.mCurrentRecordIndex;
            this.mCurrentRecordIndex = i + STATE_SINGLE_ICON;
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
        int width = data[0] & PduHeaders.STORE_STATUS_ERROR_END;
        int valueIndex = STATE_SINGLE_ICON + STATE_SINGLE_ICON;
        int height = data[STATE_SINGLE_ICON] & PduHeaders.STORE_STATUS_ERROR_END;
        int numOfPixels = width * height;
        int[] pixels = new int[numOfPixels];
        int bitIndex = 7;
        byte currentByte = (byte) 0;
        int pixelIndex = 0;
        while (pixelIndex < numOfPixels) {
            int valueIndex2;
            if (pixelIndex % 8 == 0) {
                valueIndex2 = valueIndex + STATE_SINGLE_ICON;
                currentByte = data[valueIndex];
                bitIndex = 7;
            } else {
                valueIndex2 = valueIndex;
            }
            int pixelIndex2 = pixelIndex + STATE_SINGLE_ICON;
            int bitIndex2 = bitIndex - 1;
            pixels[pixelIndex] = bitToBnW((currentByte >> bitIndex) & STATE_SINGLE_ICON);
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
        if (bit == STATE_SINGLE_ICON) {
            return -1;
        }
        return -16777216;
    }

    public static Bitmap parseToRGB(byte[] data, int length, boolean transparency, byte[] clut) {
        boolean bitsOverlaps;
        int pixelIndex;
        int width = data[0] & PduHeaders.STORE_STATUS_ERROR_END;
        int valueIndex = STATE_SINGLE_ICON + STATE_SINGLE_ICON;
        int height = data[STATE_SINGLE_ICON] & PduHeaders.STORE_STATUS_ERROR_END;
        int valueIndex2 = valueIndex + STATE_SINGLE_ICON;
        int bitsPerImg = data[valueIndex] & PduHeaders.STORE_STATUS_ERROR_END;
        valueIndex = valueIndex2 + STATE_SINGLE_ICON;
        int numOfClutEntries = data[valueIndex2] & PduHeaders.STORE_STATUS_ERROR_END;
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
                valueIndex2 = valueIndex + STATE_SINGLE_ICON;
                currentByte = data[valueIndex];
                bitIndex = bitsOverlaps ? bitsStartOffset : bitIndex * -1;
            } else {
                valueIndex2 = valueIndex;
            }
            int clutIndex = ((currentByte >> bitIndex) & mask) * EVENT_READ_CLUT_DONE;
            int pixelIndex2 = pixelIndex + STATE_SINGLE_ICON;
            pixels[pixelIndex] = Color.rgb(clut[clutIndex], clut[clutIndex + STATE_SINGLE_ICON], clut[clutIndex + STATE_MULTI_ICONS]);
            bitIndex -= bitsPerImg;
            pixelIndex = pixelIndex2;
            valueIndex = valueIndex2;
        }
        return Bitmap.createBitmap(pixels, width, height, Config.ARGB_8888);
    }

    private static int getMask(int numOfBits) {
        switch (numOfBits) {
            case STATE_SINGLE_ICON /*1*/:
                return STATE_SINGLE_ICON;
            case STATE_MULTI_ICONS /*2*/:
                return EVENT_READ_CLUT_DONE;
            case EVENT_READ_CLUT_DONE /*3*/:
                return 7;
            case CLUT_LOCATION_OFFSET /*4*/:
                return 15;
            case CharacterSets.ISO_8859_2 /*5*/:
                return 31;
            case CharacterSets.ISO_8859_3 /*6*/:
                return 63;
            case CharacterSets.ISO_8859_4 /*7*/:
                return CallFailCause.INTERWORKING_UNSPECIFIED;
            case CharacterSets.ISO_8859_5 /*8*/:
                return PduHeaders.STORE_STATUS_ERROR_END;
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
