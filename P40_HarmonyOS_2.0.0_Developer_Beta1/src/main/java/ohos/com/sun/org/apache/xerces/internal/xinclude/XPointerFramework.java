package ohos.com.sun.org.apache.xerces.internal.xinclude;

import java.util.Stack;

public class XPointerFramework {
    int fCountSchemaName;
    String fCurrentSchemaPointer;
    XPointerSchema fDefaultXPointerSchema;
    Stack fSchemaNotAvailable;
    String fSchemaPointer;
    String[] fSchemaPointerName;
    String[] fSchemaPointerURI;
    XPointerSchema[] fXPointerSchema;
    int schemaLength;

    public String getEscapedURI(String str) {
        return str;
    }

    public XPointerFramework() {
        this(null);
    }

    public XPointerFramework(XPointerSchema[] xPointerSchemaArr) {
        this.fCountSchemaName = 0;
        this.schemaLength = 0;
        this.fXPointerSchema = xPointerSchemaArr;
        this.fSchemaNotAvailable = new Stack();
    }

    public void reset() {
        this.fXPointerSchema = null;
        this.fXPointerSchema = null;
        this.fCountSchemaName = 0;
        this.schemaLength = 0;
        this.fSchemaPointerName = null;
        this.fSchemaPointerURI = null;
        this.fDefaultXPointerSchema = null;
        this.fCurrentSchemaPointer = null;
    }

    public void setXPointerSchema(XPointerSchema[] xPointerSchemaArr) {
        this.fXPointerSchema = xPointerSchemaArr;
    }

    public void setSchemaPointer(String str) {
        this.fSchemaPointer = str;
    }

    public XPointerSchema getNextXPointerSchema() {
        int i = this.fCountSchemaName;
        if (this.fSchemaPointerName == null) {
            getSchemaNames();
        }
        if (this.fDefaultXPointerSchema == null) {
            getDefaultSchema();
        }
        if (this.fDefaultXPointerSchema.getXpointerSchemaName().equalsIgnoreCase(this.fSchemaPointerName[i])) {
            this.fDefaultXPointerSchema.reset();
            this.fDefaultXPointerSchema.setXPointerSchemaPointer(this.fSchemaPointerURI[i]);
            this.fCountSchemaName = i + 1;
            return getDefaultSchema();
        }
        XPointerSchema[] xPointerSchemaArr = this.fXPointerSchema;
        if (xPointerSchemaArr == null) {
            this.fCountSchemaName = i + 1;
            return null;
        }
        int length = xPointerSchemaArr.length;
        while (this.fSchemaPointerName[i] != null) {
            for (int i2 = 0; i2 < length; i2++) {
                if (this.fSchemaPointerName[i].equalsIgnoreCase(this.fXPointerSchema[i2].getXpointerSchemaName())) {
                    this.fXPointerSchema[i2].setXPointerSchemaPointer(this.fSchemaPointerURI[i]);
                    this.fCountSchemaName = i + 1;
                    return this.fXPointerSchema[i2];
                }
            }
            if (this.fSchemaNotAvailable == null) {
                this.fSchemaNotAvailable = new Stack();
            }
            this.fSchemaNotAvailable.push(this.fSchemaPointerName[i]);
            i++;
        }
        return null;
    }

    public XPointerSchema getDefaultSchema() {
        if (this.fDefaultXPointerSchema == null) {
            this.fDefaultXPointerSchema = new XPointerElementHandler();
        }
        return this.fDefaultXPointerSchema;
    }

    public void getSchemaNames() {
        int length = this.fSchemaPointer.length();
        this.fSchemaPointerName = new String[5];
        this.fSchemaPointerURI = new String[5];
        int indexOf = this.fSchemaPointer.indexOf(40);
        if (indexOf > 0) {
            int i = indexOf + 1;
            int i2 = 0;
            this.fSchemaPointerName[0] = this.fSchemaPointer.substring(0, indexOf).trim();
            int i3 = 1;
            int i4 = 1;
            int i5 = i;
            while (i < length) {
                char charAt = this.fSchemaPointer.charAt(i);
                if (charAt == '(') {
                    i3++;
                }
                if (charAt == ')') {
                    i3--;
                }
                if (i3 == 0) {
                    int i6 = i2 + 1;
                    this.fSchemaPointerURI[i2] = getEscapedURI(this.fSchemaPointer.substring(i5, i).trim());
                    int indexOf2 = this.fSchemaPointer.indexOf(40, i);
                    if (indexOf2 != -1) {
                        this.fSchemaPointerName[i4] = this.fSchemaPointer.substring(i + 1, indexOf2).trim();
                        i3++;
                        i4++;
                        i2 = i6;
                        i = indexOf2;
                        i5 = indexOf2 + 1;
                    } else {
                        i5 = i;
                        i2 = i6;
                    }
                }
                i++;
            }
            this.schemaLength = i2 - 1;
        }
    }

    public int getSchemaCount() {
        return this.schemaLength;
    }

    public int getCurrentPointer() {
        return this.fCountSchemaName;
    }
}
