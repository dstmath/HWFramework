package android.media.hwmnote;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

class HwMnoteParser {
    private static final short BIG_ENDIAN_TAG = 19789;
    private static final boolean DEBUG = false;
    private static final int DEFAULT_IFD0_OFFSET = 8;
    public static final int EVENT_END = 3;
    public static final int EVENT_NEW_TAG = 1;
    public static final int EVENT_START_OF_IFD = 0;
    public static final int EVENT_VALUE_OF_REGISTERED_TAG = 2;
    private static final byte[] HW_MNOTE_HEADER = {72, 85, 65, 87, 69, 73, 0, 0};
    private static final short HW_MNOTE_TAG_FACE_IFD = HwMnoteInterfaceImpl.getTrueTagKey(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_IFD);
    private static final short HW_MNOTE_TAG_SCENE_IFD = HwMnoteInterfaceImpl.getTrueTagKey(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_IFD);
    private static final short LITTLE_ENDIAN_TAG = 18761;
    private static final int OFFSET_4 = 4;
    private static final int OFFSET_8 = 8;
    private static final int OFFSET_SIZE = 2;
    private static final int OPTION_HW_MNOTE_IFD_0 = 1;
    private static final int OPTION_HW_MNOTE_IFD_FACE = 8;
    private static final int OPTION_HW_MNOTE_IFD_SCENE = 2;
    private static final int SHIFT_0 = 0;
    private static final int SHIFT_1 = 1;
    private static final int SHIFT_2 = 3;
    private static final String TAG = "HwMnoteParser";
    private static final int TAG_SIZE = 12;
    private static final short TIFF_HEADER_TAIL = 42;
    private static final long UNSIGNED_DWORD = 4294967295L;
    private static final int UNSIGNED_WORD = 65535;
    private static final Charset US_ASCII = Charset.forName("US-ASCII");
    private boolean mContainHwMnoteData = false;
    private final TreeMap<Integer, Object> mCorrespondingEvent = new TreeMap<>();
    private byte[] mDataAboveIfd0;
    private int mIfdStartOffset = 0;
    private int mIfdType;
    private final HwMnoteInterfaceImpl mInterface;
    private boolean mNeedToParseOffsetsInCurrentIfd;
    private int mNumOfTagInIfd = 0;
    private final int mOptions;
    private HwMnoteTag mTag;
    private final CountedDataInputStream mTiffStream;

    private static class HwMnoteTagEvent {
        /* access modifiers changed from: private */
        public boolean isRequested;
        /* access modifiers changed from: private */
        public HwMnoteTag tag;

        HwMnoteTagEvent(HwMnoteTag tag2, boolean isRequireByUser) {
            this.tag = tag2;
            this.isRequested = isRequireByUser;
        }

        public boolean isRequested() {
            return this.isRequested;
        }
    }

    private static class IfdEvent {
        /* access modifiers changed from: private */
        public int ifd;
        /* access modifiers changed from: private */
        public boolean isRequested;

        IfdEvent(int ifd2, boolean isInterestedIfd) {
            this.ifd = ifd2;
            this.isRequested = isInterestedIfd;
        }

        public int getIfd() {
            return this.ifd;
        }

        public void setIfd(int ifd2) {
            this.ifd = ifd2;
        }

        public boolean isRequested() {
            return this.isRequested;
        }
    }

    private HwMnoteParser(InputStream inputStream, int options, HwMnoteInterfaceImpl iRef) throws IOException, IllegalArgumentException {
        if (inputStream != null) {
            this.mInterface = iRef;
            this.mContainHwMnoteData = seekTiffData(inputStream);
            this.mTiffStream = new CountedDataInputStream(inputStream);
            this.mOptions = options;
            if (this.mContainHwMnoteData) {
                parseTiffHeader();
                long offset = this.mTiffStream.readUnsignedInt();
                if (offset <= 2147483647L) {
                    this.mIfdType = 0;
                    if (isIfdRequested(0) || needToParseOffsetsInCurrentIfd()) {
                        registerIfd(0, offset);
                        if (offset != 8) {
                            this.mDataAboveIfd0 = new byte[(((int) offset) - 8)];
                            readInternal(this.mDataAboveIfd0);
                        }
                    }
                    return;
                }
                throw new IllegalArgumentException("Invalid offset " + offset);
            }
            return;
        }
        throw new IOException("Null argument inputStream to HwMnoteParser");
    }

    private boolean isIfdRequested(int ifdType) {
        boolean z = true;
        switch (ifdType) {
            case 0:
                if ((this.mOptions & 1) == 0) {
                    z = false;
                }
                return z;
            case 1:
                if ((this.mOptions & 2) == 0) {
                    z = false;
                }
                return z;
            case 2:
                if ((this.mOptions & 8) == 0) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }

    protected static HwMnoteParser parse(InputStream inputStream, HwMnoteInterfaceImpl iRef) throws IOException, IllegalArgumentException {
        return new HwMnoteParser(inputStream, 11, iRef);
    }

    /* access modifiers changed from: protected */
    public int next() throws IOException, IllegalArgumentException {
        if (!this.mContainHwMnoteData) {
            return 3;
        }
        int offset = this.mTiffStream.getReadByteCount();
        int endOfTags = this.mIfdStartOffset + 2 + (12 * this.mNumOfTagInIfd);
        if (offset < endOfTags) {
            this.mTag = readTag();
            if (this.mTag == null) {
                return next();
            }
            if (this.mNeedToParseOffsetsInCurrentIfd) {
                checkOffsetTag(this.mTag);
            }
            return 1;
        }
        if (offset == endOfTags) {
            doEndOfTags();
        } else {
            Log.w(TAG, "offset bigger than endOfTags");
        }
        while (this.mCorrespondingEvent.size() != 0) {
            Map.Entry<Integer, Object> entry = this.mCorrespondingEvent.pollFirstEntry();
            Object event = entry.getValue();
            try {
                skipTo(entry.getKey().intValue());
                if (event instanceof IfdEvent) {
                    Integer x = doIfdEvent(entry, (IfdEvent) event);
                    if (x != null) {
                        return x.intValue();
                    }
                } else {
                    HwMnoteTagEvent tagEvent = (HwMnoteTagEvent) event;
                    this.mTag = tagEvent.tag;
                    if (this.mTag.getDataType() != 7) {
                        readFullTagValue(this.mTag);
                        checkOffsetTag(this.mTag);
                    }
                    if (tagEvent.isRequested) {
                        return 2;
                    }
                }
            } catch (IOException e) {
                Log.w(TAG, "Failed to skip to data at: " + entry.getKey() + " for " + event.getClass().getName() + ", the file may be broken.");
            }
        }
        return 3;
    }

    private void doEndOfTags() throws IOException {
        if (this.mIfdType != 0) {
            int offsetSize = 4;
            if (this.mCorrespondingEvent.size() > 0) {
                offsetSize = this.mCorrespondingEvent.firstEntry().getKey().intValue() - this.mTiffStream.getReadByteCount();
            }
            if (offsetSize < 4) {
                Log.w(TAG, "Invalid size of link to next IFD: " + offsetSize);
                return;
            }
            long ifdOffset = readUnsignedLong();
            if (ifdOffset != 0) {
                Log.w(TAG, "Invalid link to next IFD: " + ifdOffset);
            }
        }
    }

    private Integer doIfdEvent(Map.Entry<Integer, Object> entry, IfdEvent event) throws IOException, IllegalArgumentException {
        this.mIfdType = event.ifd;
        this.mNumOfTagInIfd = this.mTiffStream.readUnsignedShort();
        this.mIfdStartOffset = entry.getKey().intValue();
        this.mNeedToParseOffsetsInCurrentIfd = needToParseOffsetsInCurrentIfd();
        if (event.isRequested) {
            return 0;
        }
        skipRemainingTagsInCurrentIfd();
        return null;
    }

    /* access modifiers changed from: protected */
    public void skipRemainingTagsInCurrentIfd() throws IOException, IllegalArgumentException {
        int endOfTags = this.mIfdStartOffset + 2 + (12 * this.mNumOfTagInIfd);
        int offset = this.mTiffStream.getReadByteCount();
        if (offset <= endOfTags) {
            if (this.mNeedToParseOffsetsInCurrentIfd) {
                while (offset < endOfTags) {
                    this.mTag = readTag();
                    offset += 12;
                    if (this.mTag != null) {
                        checkOffsetTag(this.mTag);
                    }
                }
            } else {
                skipTo(endOfTags);
            }
            readUnsignedLong();
        }
    }

    private boolean needToParseOffsetsInCurrentIfd() {
        switch (this.mIfdType) {
            case 0:
                boolean z = true;
                if (!isIfdRequested(1) && !isIfdRequested(2)) {
                    z = false;
                }
                return z;
            case 1:
                return false;
            case 2:
                return false;
            default:
                return false;
        }
    }

    /* access modifiers changed from: protected */
    public HwMnoteTag getTag() {
        return this.mTag;
    }

    /* access modifiers changed from: protected */
    public int getTagCountInCurrentIfd() {
        return this.mNumOfTagInIfd;
    }

    /* access modifiers changed from: protected */
    public int getCurrentIfd() {
        return this.mIfdType;
    }

    private void skipTo(int offset) throws IOException {
        this.mTiffStream.skipTo((long) offset);
        while (!this.mCorrespondingEvent.isEmpty() && this.mCorrespondingEvent.firstKey().intValue() < offset) {
            this.mCorrespondingEvent.pollFirstEntry();
        }
    }

    /* access modifiers changed from: protected */
    public void registerForTagValue(HwMnoteTag tag) {
        if (tag.getOffset() >= this.mTiffStream.getReadByteCount()) {
            this.mCorrespondingEvent.put(Integer.valueOf(tag.getOffset()), new HwMnoteTagEvent(tag, true));
        }
    }

    private void registerIfd(int ifdType, long offset) {
        this.mCorrespondingEvent.put(Integer.valueOf((int) offset), new IfdEvent(ifdType, isIfdRequested(ifdType)));
    }

    private HwMnoteTag readTag() throws IOException, IllegalArgumentException {
        short tagId = this.mTiffStream.readShort();
        short dataFormat = this.mTiffStream.readShort();
        long numOfComp = this.mTiffStream.readUnsignedInt();
        if (numOfComp > 2147483647L) {
            throw new IllegalArgumentException("Number of component is larger then Integer.MAX_VALUE");
        } else if (!HwMnoteTag.isValidType(dataFormat)) {
            Log.w(TAG, String.format("Tag %04x: Invalid data type %d", new Object[]{Short.valueOf(tagId), Short.valueOf(dataFormat)}));
            if (this.mTiffStream.skip(4) != 4) {
                Log.w(TAG, "Can't skip 4 bytes");
            }
            return null;
        } else {
            HwMnoteTag tag = new HwMnoteTag(tagId, dataFormat, (int) numOfComp, this.mIfdType, ((int) numOfComp) != 0);
            int dataSize = tag.getDataSize();
            if (dataSize > 4) {
                long offset = this.mTiffStream.readUnsignedInt();
                if (offset <= 2147483647L) {
                    tag.setOffset((int) offset);
                } else {
                    throw new IllegalArgumentException("offset is larger then Integer.MAX_VALUE");
                }
            } else {
                boolean defCount = tag.hasDefinedCount();
                tag.setHasDefinedCount(false);
                readFullTagValue(tag);
                tag.setHasDefinedCount(defCount);
                if (this.mTiffStream.skip((long) (4 - dataSize)) != ((long) (4 - dataSize))) {
                    Log.w(TAG, String.format("Can't skip %d byte(s)", new Object[]{Integer.valueOf(4 - dataSize)}));
                }
                tag.setOffset(this.mTiffStream.getReadByteCount() - 4);
            }
            return tag;
        }
    }

    private void checkOffsetTag(HwMnoteTag tag) {
        if (tag.getComponentCount() != 0) {
            short tid = tag.getTagId();
            int ifd = tag.getIfd();
            if (tid != HW_MNOTE_TAG_SCENE_IFD || !checkAllowed(ifd, HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_IFD)) {
                if (tid == HW_MNOTE_TAG_FACE_IFD && checkAllowed(ifd, HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_IFD) && isIfdRequested(2)) {
                    registerIfd(2, tag.getValueAt(0));
                }
            } else if (isIfdRequested(1)) {
                registerIfd(1, tag.getValueAt(0));
            }
        }
    }

    private boolean checkAllowed(int ifd, int tagId) {
        int info = this.mInterface.getTagInfo().get(tagId);
        if (info == 0) {
            return false;
        }
        return HwMnoteInterfaceImpl.isIfdAllowed(info, ifd);
    }

    /* access modifiers changed from: protected */
    public void readFullTagValue(HwMnoteTag tag) throws IOException {
        if (isValidHwMnote(tag.getDataType()) && this.mCorrespondingEvent.size() > 0) {
            if (this.mCorrespondingEvent.firstEntry().getKey().intValue() < this.mTiffStream.getReadByteCount() + tag.getComponentCount()) {
                doTagValueOverlaps(tag, this.mCorrespondingEvent.firstEntry().getValue());
            }
        }
        int size = tag.getDataType();
        if (size == 4) {
            long[] value = new long[tag.getComponentCount()];
            int i = value.length;
            for (int i2 = 0; i2 < i; i2++) {
                value[i2] = readUnsignedLong();
            }
            tag.setValue(value);
        } else if (size == 7) {
            byte[] buf = new byte[tag.getComponentCount()];
            readInternal(buf);
            tag.setValue(buf);
        }
    }

    private boolean isValidHwMnote(short type) {
        return type == 7;
    }

    private void doTagValueOverlaps(HwMnoteTag tag, Object event) {
        if (event instanceof IfdEvent) {
            Log.w(TAG, "Ifd " + ((IfdEvent) event).ifd + " overlaps value for tag: " + tag.getTagId());
        } else if (event instanceof HwMnoteTagEvent) {
            Log.w(TAG, "Tag value for tag: " + ((HwMnoteTagEvent) event).tag.getTagId() + " overlaps value for tag: " + tag.getTagId());
        } else {
            Log.w(TAG, "Ifd overlaps value for tag: " + tag.getTagId());
        }
        int size = this.mCorrespondingEvent.firstEntry().getKey().intValue() - this.mTiffStream.getReadByteCount();
        Log.w(TAG, "Invalid size of tag: " + tag.getTagId() + " setting count to: " + size);
        tag.forceSetComponentCount(size);
    }

    private void parseTiffHeader() throws IOException, IllegalArgumentException {
        short byteOrder = this.mTiffStream.readShort();
        if (18761 == byteOrder) {
            this.mTiffStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        } else if (19789 == byteOrder) {
            this.mTiffStream.setByteOrder(ByteOrder.BIG_ENDIAN);
        } else {
            throw new IllegalArgumentException("Invalid TIFF header");
        }
        if (this.mTiffStream.readShort() != 42) {
            throw new IllegalArgumentException("Invalid TIFF header");
        }
    }

    private boolean seekTiffData(InputStream inputStream) throws IOException {
        byte[] header = new byte[8];
        if (new CountedDataInputStream(inputStream).read(header, 0, 8) < 8) {
            return false;
        }
        if (Arrays.equals(header, HW_MNOTE_HEADER)) {
            return true;
        }
        Log.w(TAG, "Invalid Huawei Maker Note.");
        return false;
    }

    private int readInternal(byte[] buffer) throws IOException {
        return this.mTiffStream.read(buffer);
    }

    /* access modifiers changed from: protected */
    public int read(byte[] buffer) throws IOException {
        return this.mTiffStream.read(buffer);
    }

    /* access modifiers changed from: protected */
    public int readUnsignedShort() throws IOException {
        return this.mTiffStream.readShort() & 65535;
    }

    /* access modifiers changed from: protected */
    public long readUnsignedLong() throws IOException {
        return ((long) readLong()) & UNSIGNED_DWORD;
    }

    /* access modifiers changed from: protected */
    public int readLong() throws IOException {
        return this.mTiffStream.readInt();
    }

    /* access modifiers changed from: protected */
    public ByteOrder getByteOrder() {
        return this.mTiffStream.getByteOrder();
    }
}
