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
            int valueIndex3 = valueIndex2 + 1;
            try {
                d.mHeight = rawData[valueIndex2] & 255;
                valueIndex2 = valueIndex3 + 1;
                d.mCodingScheme = rawData[valueIndex3] & 255;
                valueIndex3 = valueIndex2 + 1;
                d.mImageId = (rawData[valueIndex2] & 255) << 8;
                int valueIndex4 = valueIndex3 + 1;
                try {
                    d.mImageId = (rawData[valueIndex3] & 255) | d.mImageId;
                    valueIndex2 = valueIndex4 + 1;
                    d.mHighOffset = rawData[valueIndex4] & 255;
                    valueIndex3 = valueIndex2 + 1;
                    d.mLowOffset = rawData[valueIndex2] & 255;
                    valueIndex2 = valueIndex3 + 1;
                    int i = valueIndex2 + 1;
                    d.mLength = ((rawData[valueIndex3] & 255) << 8) | (rawData[valueIndex2] & 255);
                    CatLog.d("ImageDescriptor", "parse; Descriptor : " + d.mWidth + ", " + d.mHeight + ", " + d.mCodingScheme + ", 0x" + Integer.toHexString(d.mImageId) + ", " + d.mHighOffset + ", " + d.mLowOffset + ", " + d.mLength);
                    return d;
                } catch (IndexOutOfBoundsException e) {
                }
            } catch (IndexOutOfBoundsException e2) {
                int i2 = valueIndex3;
                IndexOutOfBoundsException indexOutOfBoundsException = e2;
                CatLog.d("ImageDescriptor", "parse; failed parsing image descriptor");
                return null;
            }
        } catch (IndexOutOfBoundsException e3) {
            int i3 = valueIndex2;
            CatLog.d("ImageDescriptor", "parse; failed parsing image descriptor");
            return null;
        }
    }
}
