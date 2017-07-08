package android.icu.impl;

import android.icu.text.UCharacterIterator;
import org.xmlpull.v1.XmlPullParser;

public final class StringUCharacterIterator extends UCharacterIterator {
    private int m_currentIndex_;
    private String m_text_;

    public StringUCharacterIterator(String str) {
        if (str == null) {
            throw new IllegalArgumentException();
        }
        this.m_text_ = str;
        this.m_currentIndex_ = 0;
    }

    public StringUCharacterIterator() {
        this.m_text_ = XmlPullParser.NO_NAMESPACE;
        this.m_currentIndex_ = 0;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public int current() {
        if (this.m_currentIndex_ < this.m_text_.length()) {
            return this.m_text_.charAt(this.m_currentIndex_);
        }
        return -1;
    }

    public int getLength() {
        return this.m_text_.length();
    }

    public int getIndex() {
        return this.m_currentIndex_;
    }

    public int next() {
        if (this.m_currentIndex_ >= this.m_text_.length()) {
            return -1;
        }
        String str = this.m_text_;
        int i = this.m_currentIndex_;
        this.m_currentIndex_ = i + 1;
        return str.charAt(i);
    }

    public int previous() {
        if (this.m_currentIndex_ <= 0) {
            return -1;
        }
        String str = this.m_text_;
        int i = this.m_currentIndex_ - 1;
        this.m_currentIndex_ = i;
        return str.charAt(i);
    }

    public void setIndex(int currentIndex) throws IndexOutOfBoundsException {
        if (currentIndex < 0 || currentIndex > this.m_text_.length()) {
            throw new IndexOutOfBoundsException();
        }
        this.m_currentIndex_ = currentIndex;
    }

    public int getText(char[] fillIn, int offset) {
        int length = this.m_text_.length();
        if (offset < 0 || offset + length > fillIn.length) {
            throw new IndexOutOfBoundsException(Integer.toString(length));
        }
        this.m_text_.getChars(0, length, fillIn, offset);
        return length;
    }

    public String getText() {
        return this.m_text_;
    }

    public void setText(String text) {
        if (text == null) {
            throw new NullPointerException();
        }
        this.m_text_ = text;
        this.m_currentIndex_ = 0;
    }
}
