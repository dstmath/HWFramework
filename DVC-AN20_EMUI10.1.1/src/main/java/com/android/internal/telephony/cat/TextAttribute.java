package com.android.internal.telephony.cat;

public class TextAttribute {
    public TextAlignment align;
    public boolean bold;
    public TextColor color;
    public boolean italic;
    public int length;
    public FontSize size;
    public int start;
    public boolean strikeThrough;
    public boolean underlined;

    public TextAttribute(int start2, int length2, TextAlignment align2, FontSize size2, boolean bold2, boolean italic2, boolean underlined2, boolean strikeThrough2, TextColor color2) {
        this.start = start2;
        this.length = length2;
        this.align = align2;
        this.size = size2;
        this.bold = bold2;
        this.italic = italic2;
        this.underlined = underlined2;
        this.strikeThrough = strikeThrough2;
        this.color = color2;
    }
}
