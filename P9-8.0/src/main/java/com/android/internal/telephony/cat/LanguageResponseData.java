package com.android.internal.telephony.cat;

import com.android.internal.telephony.GsmAlphabet;
import java.io.ByteArrayOutputStream;

/* compiled from: ResponseData */
class LanguageResponseData extends ResponseData {
    private String mLang;

    public LanguageResponseData(String lang) {
        this.mLang = lang;
    }

    public void format(ByteArrayOutputStream buf) {
        int i = 0;
        if (buf != null) {
            byte[] data;
            buf.write(ComprehensionTlvTag.LANGUAGE.value() | 128);
            if (this.mLang == null || this.mLang.length() <= 0) {
                data = new byte[0];
            } else {
                data = GsmAlphabet.stringToGsm8BitPacked(this.mLang);
            }
            buf.write(data.length);
            int length = data.length;
            while (i < length) {
                buf.write(data[i]);
                i++;
            }
        }
    }
}
