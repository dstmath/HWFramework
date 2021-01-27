package android.media.hwmnote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;

class HwMnoteOutputStream {
    private static final boolean DEBUG = false;
    protected static final byte[] HW_MNOTE_HEADER = {72, 85, 65, 87, 69, 73, 0, 0};
    private static final int MAX_HW_MNOTE_SIZE = 65535;
    private static final int OFFSET_2 = 2;
    private static final int OFFSET_4 = 4;
    private static final int OFFSET_8 = 8;
    private static final String TAG = "HwMnoteOutputStream";
    private static final short TAG_SIZE = 12;
    private static final short TIFF_BIG_ENDIAN = 19789;
    private static final short TIFF_HEADER = 42;
    private static final short TIFF_HEADER_SIZE = 8;
    private static final short TIFF_LITTLE_ENDIAN = 18761;
    private HwMnoteData mHwMnoteData;

    protected HwMnoteOutputStream() {
    }

    /* access modifiers changed from: protected */
    public void setData(HwMnoteData hwMnoteData) {
        this.mHwMnoteData = hwMnoteData;
    }

    /* access modifiers changed from: protected */
    public HwMnoteData getHwMnoteData() {
        return this.mHwMnoteData;
    }

    public byte[] getHwMnoteBuffer() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        writeHwMnoteData(outStream);
        byte[] mnoteBuffer = outStream.toByteArray();
        outStream.close();
        return mnoteBuffer;
    }

    private void writeHwMnoteData(OutputStream outStream) throws IOException {
        if (this.mHwMnoteData != null) {
            createRequiredIfdAndTag();
            if (calculateAllOffset() + 8 <= 65535) {
                OrderedDataOutputStream dataOutputStream = new OrderedDataOutputStream(outStream);
                dataOutputStream.setByteOrder(ByteOrder.BIG_ENDIAN);
                dataOutputStream.write(HW_MNOTE_HEADER);
                if (this.mHwMnoteData.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                    dataOutputStream.writeShort(TIFF_BIG_ENDIAN);
                } else {
                    dataOutputStream.writeShort(TIFF_LITTLE_ENDIAN);
                }
                dataOutputStream.setByteOrder(this.mHwMnoteData.getByteOrder());
                dataOutputStream.writeShort(TIFF_HEADER);
                dataOutputStream.writeInt(8);
                writeAllTags(dataOutputStream);
                ArrayList<HwMnoteTag> nullTags = stripNullValueTags(this.mHwMnoteData);
                int size = nullTags.size();
                for (int i = 0; i < size; i++) {
                    this.mHwMnoteData.addTag(nullTags.get(i));
                }
                return;
            }
            throw new IOException("Mnote HW header is too large (>64Kb)");
        }
    }

    private ArrayList<HwMnoteTag> stripNullValueTags(HwMnoteData data) {
        ArrayList<HwMnoteTag> nullTags = new ArrayList<>();
        if (data == null || data.getAllTags() == null) {
            return nullTags;
        }
        for (HwMnoteTag tag : data.getAllTags()) {
            if (tag.getValue() == null && !HwMnoteInterfaceImpl.isOffsetTag(tag.getTagId())) {
                data.removeTag(tag.getTagId(), tag.getIfd());
                nullTags.add(tag);
            }
        }
        return nullTags;
    }

    private void writeAllTags(OrderedDataOutputStream dataOutputStream) throws IOException {
        writeIfd(this.mHwMnoteData.getIfdData(0), dataOutputStream);
        HwMnoteIfdData sceneIfd = this.mHwMnoteData.getIfdData(1);
        if (sceneIfd != null) {
            writeIfd(sceneIfd, dataOutputStream);
        }
        HwMnoteIfdData faceIfd = this.mHwMnoteData.getIfdData(2);
        if (faceIfd != null) {
            writeIfd(faceIfd, dataOutputStream);
        }
    }

    private void writeIfd(HwMnoteIfdData ifd, OrderedDataOutputStream dataOutputStream) throws IOException {
        HwMnoteTag[] tags = ifd.getAllTags();
        dataOutputStream.writeShort((short) tags.length);
        for (HwMnoteTag tag : tags) {
            dataOutputStream.writeShort(tag.getTagId());
            dataOutputStream.writeShort(tag.getDataType());
            dataOutputStream.writeInt(tag.getComponentCount());
            if (tag.getDataSize() > 4) {
                dataOutputStream.writeInt(tag.getOffset());
            } else {
                writeTagValue(tag, dataOutputStream);
                int n = 4 - tag.getDataSize();
                for (int i = 0; i < n; i++) {
                    dataOutputStream.write(0);
                }
            }
        }
        dataOutputStream.writeInt(ifd.getOffsetToNextIfd());
        for (HwMnoteTag tag2 : tags) {
            if (tag2.getDataSize() > 4) {
                writeTagValue(tag2, dataOutputStream);
            }
        }
    }

    private int calculateOffsetOfIfd(HwMnoteIfdData ifd, int offset) {
        int calculateOff = offset + (ifd.getTagCount() * 12) + 2 + 4;
        HwMnoteTag[] tags = ifd.getAllTags();
        for (HwMnoteTag tag : tags) {
            if (tag.getDataSize() > 4) {
                tag.setOffset(calculateOff);
                calculateOff += tag.getDataSize();
            }
        }
        return calculateOff;
    }

    private void createRequiredIfdAndTag() {
        HwMnoteIfdData ifd0 = this.mHwMnoteData.getIfdData(0);
        if (ifd0 == null) {
            ifd0 = new HwMnoteIfdData(0);
            this.mHwMnoteData.addIfdData(ifd0);
        }
        if (this.mHwMnoteData.getIfdData(1) != null) {
            ifd0.setTag(new HwMnoteTag(HwMnoteInterfaceImpl.getTrueTagKey(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_IFD), 4, 1, 0, true));
        }
        if (this.mHwMnoteData.getIfdData(2) != null) {
            ifd0.setTag(new HwMnoteTag(HwMnoteInterfaceImpl.getTrueTagKey(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_IFD), 4, 1, 0, true));
        }
    }

    private int calculateAllOffset() {
        HwMnoteIfdData ifd0 = this.mHwMnoteData.getIfdData(0);
        int offset = calculateOffsetOfIfd(ifd0, 8);
        HwMnoteIfdData sceneIfd = this.mHwMnoteData.getIfdData(1);
        if (sceneIfd != null) {
            ifd0.getTag(HwMnoteInterfaceImpl.getTrueTagKey(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_IFD)).setValue(offset);
            offset = calculateOffsetOfIfd(sceneIfd, offset);
        }
        HwMnoteIfdData faceIfd = this.mHwMnoteData.getIfdData(2);
        if (faceIfd == null) {
            return offset;
        }
        ifd0.getTag(HwMnoteInterfaceImpl.getTrueTagKey(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_IFD)).setValue(offset);
        return calculateOffsetOfIfd(faceIfd, offset);
    }

    static void writeTagValue(HwMnoteTag tag, OrderedDataOutputStream dataOutputStream) throws IOException {
        short dataType = tag.getDataType();
        if (dataType == 4) {
            int n = tag.getComponentCount();
            for (int i = 0; i < n; i++) {
                dataOutputStream.writeInt((int) tag.getValueAt(i));
            }
        } else if (dataType == 7) {
            byte[] buf = new byte[tag.getComponentCount()];
            tag.getBytes(buf);
            dataOutputStream.write(buf);
        }
    }
}
