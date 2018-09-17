package android.icu.text;

import android.icu.impl.Utility;
import dalvik.bytecode.Opcodes;
import org.xmlpull.v1.XmlPullParser;

class StringReplacer implements UnicodeReplacer {
    private int cursorPos;
    private final Data data;
    private boolean hasCursor;
    private boolean isComplex;
    private String output;

    public StringReplacer(String theOutput, int theCursorPos, Data theData) {
        this.output = theOutput;
        this.cursorPos = theCursorPos;
        this.hasCursor = true;
        this.data = theData;
        this.isComplex = true;
    }

    public StringReplacer(String theOutput, Data theData) {
        this.output = theOutput;
        this.cursorPos = 0;
        this.hasCursor = false;
        this.data = theData;
        this.isComplex = true;
    }

    public int replace(Replaceable text, int start, int limit, int[] cursor) {
        int outLen;
        int newStart = 0;
        if (this.isComplex) {
            int i;
            StringBuffer buf = new StringBuffer();
            this.isComplex = false;
            int tempStart = text.length();
            int destStart = tempStart;
            if (start > 0) {
                int len = UTF16.getCharCount(text.char32At(start - 1));
                text.copy(start - len, start, tempStart);
                destStart = tempStart + len;
            } else {
                text.replace(tempStart, tempStart, "\uffff");
                destStart = tempStart + 1;
            }
            int destLimit = destStart;
            int tempExtra = 0;
            int oOutput = 0;
            while (true) {
                if (oOutput >= this.output.length()) {
                    break;
                }
                i = this.cursorPos;
                if (oOutput == r0) {
                    newStart = (buf.length() + destLimit) - destStart;
                }
                int c = UTF16.charAt(this.output, oOutput);
                int nextIndex = oOutput + UTF16.getCharCount(c);
                if (nextIndex == this.output.length()) {
                    tempExtra = UTF16.getCharCount(text.char32At(limit));
                    text.copy(limit, limit + tempExtra, destLimit);
                }
                UnicodeReplacer r = this.data.lookupReplacer(c);
                if (r == null) {
                    UTF16.append(buf, c);
                } else {
                    this.isComplex = true;
                    if (buf.length() > 0) {
                        text.replace(destLimit, destLimit, buf.toString());
                        destLimit += buf.length();
                        buf.setLength(0);
                    }
                    destLimit += r.replace(text, destLimit, destLimit, cursor);
                }
                oOutput = nextIndex;
            }
            if (buf.length() > 0) {
                text.replace(destLimit, destLimit, buf.toString());
                destLimit += buf.length();
            }
            i = this.cursorPos;
            if (oOutput == r0) {
                newStart = destLimit - destStart;
            }
            outLen = destLimit - destStart;
            text.copy(destStart, destLimit, start);
            text.replace(tempStart + outLen, (destLimit + tempExtra) + outLen, XmlPullParser.NO_NAMESPACE);
            text.replace(start + outLen, limit + outLen, XmlPullParser.NO_NAMESPACE);
        } else {
            text.replace(start, limit, this.output);
            outLen = this.output.length();
            newStart = this.cursorPos;
        }
        if (this.hasCursor) {
            int n;
            if (this.cursorPos < 0) {
                newStart = start;
                n = this.cursorPos;
                while (n < 0 && newStart > 0) {
                    newStart -= UTF16.getCharCount(text.char32At(newStart - 1));
                    n++;
                }
                newStart += n;
            } else {
                if (this.cursorPos > this.output.length()) {
                    newStart = start + outLen;
                    n = this.cursorPos - this.output.length();
                    while (n > 0 && newStart < text.length()) {
                        newStart += UTF16.getCharCount(text.char32At(newStart));
                        n--;
                    }
                    newStart += n;
                } else {
                    newStart += start;
                }
            }
            cursor[0] = newStart;
        }
        return outLen;
    }

    public String toReplacerPattern(boolean escapeUnprintable) {
        int cursor;
        StringBuffer rule = new StringBuffer();
        StringBuffer quoteBuf = new StringBuffer();
        int cursor2 = this.cursorPos;
        if (this.hasCursor && cursor2 < 0) {
            cursor = cursor2;
            while (true) {
                cursor2 = cursor + 1;
                if (cursor >= 0) {
                    break;
                }
                Utility.appendToRule(rule, 64, true, escapeUnprintable, quoteBuf);
                cursor = cursor2;
            }
        }
        int i = 0;
        while (i < this.output.length()) {
            if (this.hasCursor && i == cursor2) {
                Utility.appendToRule(rule, (int) Opcodes.OP_NOT_INT, true, escapeUnprintable, quoteBuf);
            }
            int c = this.output.charAt(i);
            UnicodeReplacer r = this.data.lookupReplacer(c);
            if (r == null) {
                Utility.appendToRule(rule, c, false, escapeUnprintable, quoteBuf);
            } else {
                StringBuffer buf = new StringBuffer(" ");
                buf.append(r.toReplacerPattern(escapeUnprintable));
                buf.append(' ');
                Utility.appendToRule(rule, buf.toString(), true, escapeUnprintable, quoteBuf);
            }
            i++;
        }
        if (this.hasCursor && cursor2 > this.output.length()) {
            cursor = cursor2 - this.output.length();
            while (true) {
                cursor2 = cursor - 1;
                if (cursor <= 0) {
                    break;
                }
                Utility.appendToRule(rule, 64, true, escapeUnprintable, quoteBuf);
                cursor = cursor2;
            }
            Utility.appendToRule(rule, (int) Opcodes.OP_NOT_INT, true, escapeUnprintable, quoteBuf);
        }
        Utility.appendToRule(rule, -1, true, escapeUnprintable, quoteBuf);
        return rule.toString();
    }

    public void addReplacementSetTo(UnicodeSet toUnionTo) {
        int i = 0;
        while (i < this.output.length()) {
            int ch = UTF16.charAt(this.output, i);
            UnicodeReplacer r = this.data.lookupReplacer(ch);
            if (r == null) {
                toUnionTo.add(ch);
            } else {
                r.addReplacementSetTo(toUnionTo);
            }
            i += UTF16.getCharCount(ch);
        }
    }
}
