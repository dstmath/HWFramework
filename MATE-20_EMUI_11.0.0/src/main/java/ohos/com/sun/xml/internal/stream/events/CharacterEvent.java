package ohos.com.sun.xml.internal.stream.events;

import java.io.IOException;
import java.io.Writer;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.javax.xml.stream.events.Characters;

public class CharacterEvent extends DummyEvent implements Characters {
    private boolean fCheckIfSpaceNeeded;
    private String fData;
    private boolean fIsCData;
    private boolean fIsIgnorableWhitespace;
    private boolean fIsSpace;

    public CharacterEvent() {
        this.fIsSpace = false;
        this.fCheckIfSpaceNeeded = true;
        this.fIsCData = false;
        init();
    }

    public CharacterEvent(String str) {
        this.fIsSpace = false;
        this.fCheckIfSpaceNeeded = true;
        this.fIsCData = false;
        init();
        this.fData = str;
    }

    public CharacterEvent(String str, boolean z) {
        this.fIsSpace = false;
        this.fCheckIfSpaceNeeded = true;
        init();
        this.fData = str;
        this.fIsCData = z;
    }

    public CharacterEvent(String str, boolean z, boolean z2) {
        this.fIsSpace = false;
        this.fCheckIfSpaceNeeded = true;
        init();
        this.fData = str;
        this.fIsCData = z;
        this.fIsIgnorableWhitespace = z2;
    }

    /* access modifiers changed from: protected */
    public void init() {
        setEventType(4);
    }

    public String getData() {
        return this.fData;
    }

    public void setData(String str) {
        this.fData = str;
        this.fCheckIfSpaceNeeded = true;
    }

    public boolean isCData() {
        return this.fIsCData;
    }

    public String toString() {
        if (!this.fIsCData) {
            return this.fData;
        }
        return "<![CDATA[" + getData() + "]]>";
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.xml.internal.stream.events.DummyEvent
    public void writeAsEncodedUnicodeEx(Writer writer) throws IOException {
        if (this.fIsCData) {
            writer.write("<![CDATA[" + getData() + "]]>");
            return;
        }
        charEncode(writer, this.fData);
    }

    public boolean isIgnorableWhiteSpace() {
        return this.fIsIgnorableWhitespace;
    }

    public boolean isWhiteSpace() {
        if (this.fCheckIfSpaceNeeded) {
            checkWhiteSpace();
            this.fCheckIfSpaceNeeded = false;
        }
        return this.fIsSpace;
    }

    private void checkWhiteSpace() {
        String str = this.fData;
        if (str != null && str.length() > 0) {
            this.fIsSpace = true;
            for (int i = 0; i < this.fData.length(); i++) {
                if (!XMLChar.isSpace(this.fData.charAt(i))) {
                    this.fIsSpace = false;
                    return;
                }
            }
        }
    }
}
