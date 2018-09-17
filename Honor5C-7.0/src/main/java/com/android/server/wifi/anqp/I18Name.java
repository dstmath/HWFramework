package com.android.server.wifi.anqp;

import java.io.IOException;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class I18Name {
    private final String mLanguage;
    private final Locale mLocale;
    private final String mText;

    public I18Name(ByteBuffer payload) throws ProtocolException {
        if (payload.remaining() < 4) {
            throw new ProtocolException("Truncated I18Name: " + payload.remaining());
        }
        int nameLength = payload.get() & Constants.BYTE_MASK;
        if (nameLength < 3) {
            throw new ProtocolException("Runt I18Name: " + nameLength);
        }
        this.mLanguage = Constants.getTrimmedString(payload, 3, StandardCharsets.US_ASCII);
        this.mLocale = Locale.forLanguageTag(this.mLanguage);
        this.mText = Constants.getString(payload, nameLength - 3, StandardCharsets.UTF_8);
    }

    public I18Name(String compoundString) throws IOException {
        if (compoundString.length() < 3) {
            throw new IOException("I18String too short: '" + compoundString + "'");
        }
        this.mLanguage = compoundString.substring(0, 3);
        this.mText = compoundString.substring(3);
        this.mLocale = Locale.forLanguageTag(this.mLanguage);
    }

    public String getLanguage() {
        return this.mLanguage;
    }

    public Locale getLocale() {
        return this.mLocale;
    }

    public String getText() {
        return this.mText;
    }

    public boolean equals(Object thatObject) {
        boolean z = false;
        if (this == thatObject) {
            return true;
        }
        if (thatObject == null || getClass() != thatObject.getClass()) {
            return false;
        }
        I18Name that = (I18Name) thatObject;
        if (this.mLanguage.equals(that.mLanguage)) {
            z = this.mText.equals(that.mText);
        }
        return z;
    }

    public int hashCode() {
        return (this.mLanguage.hashCode() * 31) + this.mText.hashCode();
    }

    public String toString() {
        return this.mText + ':' + this.mLocale.getLanguage();
    }
}
