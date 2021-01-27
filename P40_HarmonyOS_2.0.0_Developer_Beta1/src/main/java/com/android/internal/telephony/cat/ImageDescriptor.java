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
                int valueIndex4 = valueIndex3 + 1;
                d.mCodingScheme = rawData[valueIndex3] & 255;
                int valueIndex5 = valueIndex4 + 1;
                d.mImageId = (rawData[valueIndex4] & 255) << 8;
                int valueIndex6 = valueIndex5 + 1;
                try {
                    d.mImageId = (rawData[valueIndex5] & 255) | d.mImageId;
                    int valueIndex7 = valueIndex6 + 1;
                    d.mHighOffset = rawData[valueIndex6] & 255;
                    int valueIndex8 = valueIndex7 + 1;
                    d.mLowOffset = rawData[valueIndex7] & 255;
                    int valueIndex9 = valueIndex8 + 1;
                    int i = valueIndex9 + 1;
                    d.mLength = ((rawData[valueIndex8] & 255) << 8) | (rawData[valueIndex9] & 255);
                    CatLog.d("ImageDescriptor", "parse; Descriptor : " + d.mWidth + ", " + d.mHeight + ", " + d.mCodingScheme + ", 0x" + Integer.toHexString(d.mImageId) + ", " + d.mHighOffset + ", " + d.mLowOffset + ", " + d.mLength);
                    return d;
                } catch (IndexOutOfBoundsException e) {
                    CatLog.d("ImageDescriptor", "parse; failed parsing image descriptor");
                    return null;
                }
            } catch (IndexOutOfBoundsException e2) {
                CatLog.d("ImageDescriptor", "parse; failed parsing image descriptor");
                return null;
            }
        } catch (IndexOutOfBoundsException e3) {
            CatLog.d("ImageDescriptor", "parse; failed parsing image descriptor");
            return null;
        }
    }
}
