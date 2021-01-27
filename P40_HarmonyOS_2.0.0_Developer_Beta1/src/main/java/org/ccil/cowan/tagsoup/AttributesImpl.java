package org.ccil.cowan.tagsoup;

import org.xml.sax.Attributes;

public class AttributesImpl implements Attributes {
    String[] data;
    int length;

    public AttributesImpl() {
        this.length = 0;
        this.data = null;
    }

    public AttributesImpl(Attributes atts) {
        setAttributes(atts);
    }

    @Override // org.xml.sax.Attributes
    public int getLength() {
        return this.length;
    }

    @Override // org.xml.sax.Attributes
    public String getURI(int index) {
        if (index < 0 || index >= this.length) {
            return null;
        }
        return this.data[index * 5];
    }

    @Override // org.xml.sax.Attributes
    public String getLocalName(int index) {
        if (index < 0 || index >= this.length) {
            return null;
        }
        return this.data[(index * 5) + 1];
    }

    @Override // org.xml.sax.Attributes
    public String getQName(int index) {
        if (index < 0 || index >= this.length) {
            return null;
        }
        return this.data[(index * 5) + 2];
    }

    @Override // org.xml.sax.Attributes
    public String getType(int index) {
        if (index < 0 || index >= this.length) {
            return null;
        }
        return this.data[(index * 5) + 3];
    }

    @Override // org.xml.sax.Attributes
    public String getValue(int index) {
        if (index < 0 || index >= this.length) {
            return null;
        }
        return this.data[(index * 5) + 4];
    }

    @Override // org.xml.sax.Attributes
    public int getIndex(String uri, String localName) {
        int max = this.length * 5;
        for (int i = 0; i < max; i += 5) {
            if (this.data[i].equals(uri) && this.data[i + 1].equals(localName)) {
                return i / 5;
            }
        }
        return -1;
    }

    @Override // org.xml.sax.Attributes
    public int getIndex(String qName) {
        int max = this.length * 5;
        for (int i = 0; i < max; i += 5) {
            if (this.data[i + 2].equals(qName)) {
                return i / 5;
            }
        }
        return -1;
    }

    @Override // org.xml.sax.Attributes
    public String getType(String uri, String localName) {
        int max = this.length * 5;
        for (int i = 0; i < max; i += 5) {
            if (this.data[i].equals(uri) && this.data[i + 1].equals(localName)) {
                return this.data[i + 3];
            }
        }
        return null;
    }

    @Override // org.xml.sax.Attributes
    public String getType(String qName) {
        int max = this.length * 5;
        for (int i = 0; i < max; i += 5) {
            if (this.data[i + 2].equals(qName)) {
                return this.data[i + 3];
            }
        }
        return null;
    }

    @Override // org.xml.sax.Attributes
    public String getValue(String uri, String localName) {
        int max = this.length * 5;
        for (int i = 0; i < max; i += 5) {
            if (this.data[i].equals(uri) && this.data[i + 1].equals(localName)) {
                return this.data[i + 4];
            }
        }
        return null;
    }

    @Override // org.xml.sax.Attributes
    public String getValue(String qName) {
        int max = this.length * 5;
        for (int i = 0; i < max; i += 5) {
            if (this.data[i + 2].equals(qName)) {
                return this.data[i + 4];
            }
        }
        return null;
    }

    public void clear() {
        if (this.data != null) {
            for (int i = 0; i < this.length * 5; i++) {
                this.data[i] = null;
            }
        }
        this.length = 0;
    }

    public void setAttributes(Attributes atts) {
        clear();
        this.length = atts.getLength();
        int i = this.length;
        if (i > 0) {
            this.data = new String[(i * 5)];
            for (int i2 = 0; i2 < this.length; i2++) {
                this.data[i2 * 5] = atts.getURI(i2);
                this.data[(i2 * 5) + 1] = atts.getLocalName(i2);
                this.data[(i2 * 5) + 2] = atts.getQName(i2);
                this.data[(i2 * 5) + 3] = atts.getType(i2);
                this.data[(i2 * 5) + 4] = atts.getValue(i2);
            }
        }
    }

    public void addAttribute(String uri, String localName, String qName, String type, String value) {
        ensureCapacity(this.length + 1);
        String[] strArr = this.data;
        int i = this.length;
        strArr[i * 5] = uri;
        strArr[(i * 5) + 1] = localName;
        strArr[(i * 5) + 2] = qName;
        strArr[(i * 5) + 3] = type;
        strArr[(i * 5) + 4] = value;
        this.length = i + 1;
    }

    public void setAttribute(int index, String uri, String localName, String qName, String type, String value) {
        if (index < 0 || index >= this.length) {
            badIndex(index);
            return;
        }
        String[] strArr = this.data;
        strArr[index * 5] = uri;
        strArr[(index * 5) + 1] = localName;
        strArr[(index * 5) + 2] = qName;
        strArr[(index * 5) + 3] = type;
        strArr[(index * 5) + 4] = value;
    }

    public void removeAttribute(int index) {
        int i;
        if (index < 0 || index >= (i = this.length)) {
            badIndex(index);
            return;
        }
        if (index < i - 1) {
            String[] strArr = this.data;
            System.arraycopy(strArr, (index + 1) * 5, strArr, index * 5, ((i - index) - 1) * 5);
        }
        int i2 = this.length;
        int index2 = (i2 - 1) * 5;
        String[] strArr2 = this.data;
        int index3 = index2 + 1;
        strArr2[index2] = null;
        int index4 = index3 + 1;
        strArr2[index3] = null;
        int index5 = index4 + 1;
        strArr2[index4] = null;
        strArr2[index5] = null;
        strArr2[index5 + 1] = null;
        this.length = i2 - 1;
    }

    public void setURI(int index, String uri) {
        if (index < 0 || index >= this.length) {
            badIndex(index);
        } else {
            this.data[index * 5] = uri;
        }
    }

    public void setLocalName(int index, String localName) {
        if (index < 0 || index >= this.length) {
            badIndex(index);
        } else {
            this.data[(index * 5) + 1] = localName;
        }
    }

    public void setQName(int index, String qName) {
        if (index < 0 || index >= this.length) {
            badIndex(index);
        } else {
            this.data[(index * 5) + 2] = qName;
        }
    }

    public void setType(int index, String type) {
        if (index < 0 || index >= this.length) {
            badIndex(index);
        } else {
            this.data[(index * 5) + 3] = type;
        }
    }

    public void setValue(int index, String value) {
        if (index < 0 || index >= this.length) {
            badIndex(index);
        } else {
            this.data[(index * 5) + 4] = value;
        }
    }

    private void ensureCapacity(int n) {
        int max;
        if (n > 0) {
            String[] strArr = this.data;
            if (strArr == null || strArr.length == 0) {
                max = 25;
            } else if (strArr.length < n * 5) {
                max = strArr.length;
            } else {
                return;
            }
            while (max < n * 5) {
                max *= 2;
            }
            String[] newData = new String[max];
            int i = this.length;
            if (i > 0) {
                System.arraycopy(this.data, 0, newData, 0, i * 5);
            }
            this.data = newData;
        }
    }

    private void badIndex(int index) throws ArrayIndexOutOfBoundsException {
        throw new ArrayIndexOutOfBoundsException("Attempt to modify attribute at illegal index: " + index);
    }
}
