package org.xml.sax.helpers;

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

    public int getLength() {
        return this.length;
    }

    public String getURI(int index) {
        if (index < 0 || index >= this.length) {
            return null;
        }
        return this.data[index * 5];
    }

    public String getLocalName(int index) {
        if (index < 0 || index >= this.length) {
            return null;
        }
        return this.data[(index * 5) + 1];
    }

    public String getQName(int index) {
        if (index < 0 || index >= this.length) {
            return null;
        }
        return this.data[(index * 5) + 2];
    }

    public String getType(int index) {
        if (index < 0 || index >= this.length) {
            return null;
        }
        return this.data[(index * 5) + 3];
    }

    public String getValue(int index) {
        if (index < 0 || index >= this.length) {
            return null;
        }
        return this.data[(index * 5) + 4];
    }

    public int getIndex(String uri, String localName) {
        int max = this.length * 5;
        int i = 0;
        while (i < max) {
            if (this.data[i].equals(uri) && this.data[i + 1].equals(localName)) {
                return i / 5;
            }
            i += 5;
        }
        return -1;
    }

    public int getIndex(String qName) {
        int max = this.length * 5;
        for (int i = 0; i < max; i += 5) {
            if (this.data[i + 2].equals(qName)) {
                return i / 5;
            }
        }
        return -1;
    }

    public String getType(String uri, String localName) {
        int max = this.length * 5;
        int i = 0;
        while (i < max) {
            if (this.data[i].equals(uri) && this.data[i + 1].equals(localName)) {
                return this.data[i + 3];
            }
            i += 5;
        }
        return null;
    }

    public String getType(String qName) {
        int max = this.length * 5;
        for (int i = 0; i < max; i += 5) {
            if (this.data[i + 2].equals(qName)) {
                return this.data[i + 3];
            }
        }
        return null;
    }

    public String getValue(String uri, String localName) {
        int max = this.length * 5;
        int i = 0;
        while (i < max) {
            if (this.data[i].equals(uri) && this.data[i + 1].equals(localName)) {
                return this.data[i + 4];
            }
            i += 5;
        }
        return null;
    }

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
        if (this.length > 0) {
            this.data = new String[(this.length * 5)];
            for (int i = 0; i < this.length; i++) {
                this.data[i * 5] = atts.getURI(i);
                this.data[(i * 5) + 1] = atts.getLocalName(i);
                this.data[(i * 5) + 2] = atts.getQName(i);
                this.data[(i * 5) + 3] = atts.getType(i);
                this.data[(i * 5) + 4] = atts.getValue(i);
            }
        }
    }

    public void addAttribute(String uri, String localName, String qName, String type, String value) {
        ensureCapacity(this.length + 1);
        this.data[this.length * 5] = uri;
        this.data[(this.length * 5) + 1] = localName;
        this.data[(this.length * 5) + 2] = qName;
        this.data[(this.length * 5) + 3] = type;
        this.data[(this.length * 5) + 4] = value;
        this.length++;
    }

    public void setAttribute(int index, String uri, String localName, String qName, String type, String value) {
        if (index < 0 || index >= this.length) {
            badIndex(index);
            return;
        }
        this.data[index * 5] = uri;
        this.data[(index * 5) + 1] = localName;
        this.data[(index * 5) + 2] = qName;
        this.data[(index * 5) + 3] = type;
        this.data[(index * 5) + 4] = value;
    }

    public void removeAttribute(int index) {
        if (index < 0 || index >= this.length) {
            badIndex(index);
            return;
        }
        if (index < this.length - 1) {
            System.arraycopy(this.data, (index + 1) * 5, this.data, index * 5, ((this.length - index) - 1) * 5);
        }
        index = (this.length - 1) * 5;
        int index2 = index + 1;
        this.data[index] = null;
        index = index2 + 1;
        this.data[index2] = null;
        index2 = index + 1;
        this.data[index] = null;
        index = index2 + 1;
        this.data[index2] = null;
        this.data[index] = null;
        this.length--;
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
        if (n > 0) {
            int max;
            if (this.data == null || this.data.length == 0) {
                max = 25;
            } else if (this.data.length < n * 5) {
                max = this.data.length;
            } else {
                return;
            }
            while (max < n * 5) {
                max *= 2;
            }
            String[] newData = new String[max];
            if (this.length > 0) {
                System.arraycopy(this.data, 0, newData, 0, this.length * 5);
            }
            this.data = newData;
        }
    }

    private void badIndex(int index) throws ArrayIndexOutOfBoundsException {
        throw new ArrayIndexOutOfBoundsException("Attempt to modify attribute at illegal index: " + index);
    }
}
