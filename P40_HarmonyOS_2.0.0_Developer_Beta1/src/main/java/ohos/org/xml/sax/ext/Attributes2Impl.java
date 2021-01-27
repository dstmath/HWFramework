package ohos.org.xml.sax.ext;

import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.helpers.AttributesImpl;

public class Attributes2Impl extends AttributesImpl implements Attributes2 {
    private boolean[] declared;
    private boolean[] specified;

    public Attributes2Impl() {
        this.specified = null;
        this.declared = null;
    }

    public Attributes2Impl(Attributes attributes) {
        super(attributes);
    }

    @Override // ohos.org.xml.sax.ext.Attributes2
    public boolean isDeclared(int i) {
        if (i >= 0 && i < getLength()) {
            return this.declared[i];
        }
        throw new ArrayIndexOutOfBoundsException("No attribute at index: " + i);
    }

    @Override // ohos.org.xml.sax.ext.Attributes2
    public boolean isDeclared(String str, String str2) {
        int index = getIndex(str, str2);
        if (index >= 0) {
            return this.declared[index];
        }
        throw new IllegalArgumentException("No such attribute: local=" + str2 + ", namespace=" + str);
    }

    @Override // ohos.org.xml.sax.ext.Attributes2
    public boolean isDeclared(String str) {
        int index = getIndex(str);
        if (index >= 0) {
            return this.declared[index];
        }
        throw new IllegalArgumentException("No such attribute: " + str);
    }

    @Override // ohos.org.xml.sax.ext.Attributes2
    public boolean isSpecified(int i) {
        if (i >= 0 && i < getLength()) {
            return this.specified[i];
        }
        throw new ArrayIndexOutOfBoundsException("No attribute at index: " + i);
    }

    @Override // ohos.org.xml.sax.ext.Attributes2
    public boolean isSpecified(String str, String str2) {
        int index = getIndex(str, str2);
        if (index >= 0) {
            return this.specified[index];
        }
        throw new IllegalArgumentException("No such attribute: local=" + str2 + ", namespace=" + str);
    }

    @Override // ohos.org.xml.sax.ext.Attributes2
    public boolean isSpecified(String str) {
        int index = getIndex(str);
        if (index >= 0) {
            return this.specified[index];
        }
        throw new IllegalArgumentException("No such attribute: " + str);
    }

    @Override // ohos.org.xml.sax.helpers.AttributesImpl
    public void setAttributes(Attributes attributes) {
        int length = attributes.getLength();
        super.setAttributes(attributes);
        this.declared = new boolean[length];
        this.specified = new boolean[length];
        int i = 0;
        if (attributes instanceof Attributes2) {
            Attributes2 attributes2 = (Attributes2) attributes;
            while (i < length) {
                this.declared[i] = attributes2.isDeclared(i);
                this.specified[i] = attributes2.isSpecified(i);
                i++;
            }
            return;
        }
        while (i < length) {
            this.declared[i] = !"CDATA".equals(attributes.getType(i));
            this.specified[i] = true;
            i++;
        }
    }

    @Override // ohos.org.xml.sax.helpers.AttributesImpl
    public void addAttribute(String str, String str2, String str3, String str4, String str5) {
        super.addAttribute(str, str2, str3, str4, str5);
        int length = getLength();
        boolean[] zArr = this.specified;
        if (zArr == null) {
            this.specified = new boolean[length];
            this.declared = new boolean[length];
        } else if (length > zArr.length) {
            boolean[] zArr2 = new boolean[length];
            boolean[] zArr3 = this.declared;
            System.arraycopy(zArr3, 0, zArr2, 0, zArr3.length);
            this.declared = zArr2;
            boolean[] zArr4 = new boolean[length];
            boolean[] zArr5 = this.specified;
            System.arraycopy(zArr5, 0, zArr4, 0, zArr5.length);
            this.specified = zArr4;
        }
        int i = length - 1;
        this.specified[i] = true;
        this.declared[i] = !"CDATA".equals(str4);
    }

    @Override // ohos.org.xml.sax.helpers.AttributesImpl
    public void removeAttribute(int i) {
        int length = getLength() - 1;
        super.removeAttribute(i);
        if (i != length) {
            boolean[] zArr = this.declared;
            int i2 = i + 1;
            int i3 = length - i;
            System.arraycopy(zArr, i2, zArr, i, i3);
            boolean[] zArr2 = this.specified;
            System.arraycopy(zArr2, i2, zArr2, i, i3);
        }
    }

    public void setDeclared(int i, boolean z) {
        if (i < 0 || i >= getLength()) {
            throw new ArrayIndexOutOfBoundsException("No attribute at index: " + i);
        }
        this.declared[i] = z;
    }

    public void setSpecified(int i, boolean z) {
        if (i < 0 || i >= getLength()) {
            throw new ArrayIndexOutOfBoundsException("No attribute at index: " + i);
        }
        this.specified[i] = z;
    }
}
