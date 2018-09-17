package com.android.internal.telephony.cat;

import com.google.android.mms.pdu.PduHeaders;

public class ImageDescriptor {
    static final int CODING_SCHEME_BASIC = 17;
    static final int CODING_SCHEME_COLOUR = 33;
    int mCodingScheme;
    int mHeight;
    int mHighOffset;
    int mImageId;
    int mLength;
    int mLowOffset;
    int mWidth;

    ImageDescriptor() {
        this.mWidth = 0;
        this.mHeight = 0;
        this.mCodingScheme = 0;
        this.mImageId = 0;
        this.mHighOffset = 0;
        this.mLowOffset = 0;
        this.mLength = 0;
    }

    static ImageDescriptor parse(byte[] rawData, int valueIndex) {
        ImageDescriptor d = new ImageDescriptor();
        int valueIndex2 = valueIndex + 1;
        try {
            d.mWidth = rawData[valueIndex] & PduHeaders.STORE_STATUS_ERROR_END;
            valueIndex = valueIndex2 + 1;
            try {
                d.mHeight = rawData[valueIndex2] & PduHeaders.STORE_STATUS_ERROR_END;
                valueIndex2 = valueIndex + 1;
                d.mCodingScheme = rawData[valueIndex] & PduHeaders.STORE_STATUS_ERROR_END;
                valueIndex = valueIndex2 + 1;
                d.mImageId = (rawData[valueIndex2] & PduHeaders.STORE_STATUS_ERROR_END) << 8;
                valueIndex2 = valueIndex + 1;
                d.mImageId |= rawData[valueIndex] & PduHeaders.STORE_STATUS_ERROR_END;
                valueIndex = valueIndex2 + 1;
                d.mHighOffset = rawData[valueIndex2] & PduHeaders.STORE_STATUS_ERROR_END;
                valueIndex2 = valueIndex + 1;
                d.mLowOffset = rawData[valueIndex] & PduHeaders.STORE_STATUS_ERROR_END;
                valueIndex = valueIndex2 + 1;
                int i = (rawData[valueIndex2] & PduHeaders.STORE_STATUS_ERROR_END) << 8;
                valueIndex2 = valueIndex + 1;
                d.mLength = i | (rawData[valueIndex] & PduHeaders.STORE_STATUS_ERROR_END);
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
