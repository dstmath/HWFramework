package android.media.hwmnote;

import java.io.IOException;
import java.io.InputStream;

class HwMnoteReader {
    private final HwMnoteInterfaceImpl mInterface;

    HwMnoteReader(HwMnoteInterfaceImpl iRef) {
        this.mInterface = iRef;
    }

    /* access modifiers changed from: protected */
    public HwMnoteData read(InputStream inputStream) throws IllegalArgumentException, IOException {
        HwMnoteParser parser = HwMnoteParser.parse(inputStream, this.mInterface);
        HwMnoteData hwMnoteData = new HwMnoteData(parser.getByteOrder());
        for (int event = parser.next(); event != 3; event = parser.next()) {
            if (event == 0) {
                hwMnoteData.addIfdData(new HwMnoteIfdData(parser.getCurrentIfd()));
            } else if (event == 1) {
                HwMnoteTag tag = parser.getTag();
                if (!tag.hasValue()) {
                    parser.registerForTagValue(tag);
                } else {
                    hwMnoteData.getIfdData(tag.getIfd()).setTag(tag);
                }
            } else if (event == 2) {
                HwMnoteTag tag2 = parser.getTag();
                if (tag2.getDataType() == 7) {
                    parser.readFullTagValue(tag2);
                }
                hwMnoteData.getIfdData(tag2.getIfd()).setTag(tag2);
            }
        }
        return hwMnoteData;
    }
}
