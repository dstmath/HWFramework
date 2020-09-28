package com.android.internal.telephony.cat;

import com.android.internal.telephony.GsmAlphabet;
import java.io.ByteArrayOutputStream;

/* access modifiers changed from: package-private */
/* compiled from: ResponseData */
public class LanguageResponseData extends ResponseData {
    private String mLang;

    public LanguageResponseData(String lang) {
        this.mLang = lang;
    }

    @Override // com.android.internal.telephony.cat.ResponseData
    public void format(ByteArrayOutputStream buf) {
        byte[] data;
        if (buf != null) {
            buf.write(ComprehensionTlvTag.LANGUAGE.value() | 128);
            String str = this.mLang;
            if (str == null || str.length() <= 0) {
                data = new byte[0];
            } else {
                data = GsmAlphabet.stringToGsm8BitPacked(this.mLang);
            }
            buf.write(data.length);
            for (byte b : data) {
                buf.write(b);
            }
        }
    }
}
