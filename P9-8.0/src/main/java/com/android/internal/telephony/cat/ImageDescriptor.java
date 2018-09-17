package com.android.internal.telephony.cat;

public class ImageDescriptor {
    static final int CODING_SCHEME_BASIC = 17;
    static final int CODING_SCHEME_COLOUR = 33;
    int mCodingScheme = 0;
    int mHeight = 0;
    int mHighOffset = 0;
    int mImageId = 0;
    int mLength = 0;
    int mLowOffset = 0;
    int mWidth = 0;

    ImageDescriptor() {
    }

    static ImageDescriptor parse(byte[] rawData, int valueIndex) {
        ImageDescriptor d = new ImageDescriptor();
        int valueIndex2 = valueIndex + 1;
        try {
            d.mWidth = rawData[valueIndex] & 255;
            valueIndex = valueIndex2 + 1;
            try {
                d.mHeight = rawData[valueIndex2] & 255;
                valueIndex2 = valueIndex + 1;
                d.mCodingScheme = rawData[valueIndex] & 255;
                valueIndex = valueIndex2 + 1;
                d.mImageId = (rawData[valueIndex2] & 255) << 8;
                valueIndex2 = valueIndex + 1;
                d.mImageId |= rawData[valueIndex] & 255;
                valueIndex = valueIndex2 + 1;
                d.mHighOffset = rawData[valueIndex2] & 255;
                valueIndex2 = valueIndex + 1;
                d.mLowOffset = rawData[valueIndex] & 255;
                valueIndex = valueIndex2 + 1;
                int i = (rawData[valueIndex2] & 255) << 8;
                valueIndex2 = valueIndex + 1;
                d.mLength = i | (rawData[valueIndex] & 255);
                CatLog.d("ImageDescriptor", "parse; Descriptor : " + d.mWidth + ", " + d.mHeight + ", " + d.mCodingScheme + ", 0x" + Integer.toHexString(d.mImageId) + ", " + d.mHighOffset + ", " + d.mLowOffset + ", " + d.mLength);
                valueIndex = valueIndex2;
                return d;
            } catch (IndexOutOfBoundsException e) {
                CatLog.d("ImageDescriptor", "parse; failed parsing image descriptor");
                return null;
            }
        } catch (IndexOutOfBoundsException e2) {
            valueIndex = valueIndex2;
            CatLog.d("ImageDescriptor", "parse; failed parsing image descriptor");
            return null;
        }
    }
}
