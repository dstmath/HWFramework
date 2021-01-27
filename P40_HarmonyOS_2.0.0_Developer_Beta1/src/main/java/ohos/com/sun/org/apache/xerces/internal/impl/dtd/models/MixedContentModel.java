package ohos.com.sun.org.apache.xerces.internal.impl.dtd.models;

import ohos.com.sun.org.apache.xerces.internal.xni.QName;

public class MixedContentModel implements ContentModelValidator {
    private QName[] fChildren;
    private int[] fChildrenType;
    private int fCount;
    private boolean fOrdered;

    public MixedContentModel(QName[] qNameArr, int[] iArr, int i, int i2, boolean z) {
        this.fCount = i2;
        int i3 = this.fCount;
        this.fChildren = new QName[i3];
        this.fChildrenType = new int[i3];
        for (int i4 = 0; i4 < this.fCount; i4++) {
            int i5 = i + i4;
            this.fChildren[i4] = new QName(qNameArr[i5]);
            this.fChildrenType[i4] = iArr[i5];
        }
        this.fOrdered = z;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.ContentModelValidator
    public int validate(QName[] qNameArr, int i, int i2) {
        if (this.fOrdered) {
            int i3 = 0;
            for (int i4 = 0; i4 < i2; i4++) {
                int i5 = i + i4;
                if (qNameArr[i5].localpart != null) {
                    int i6 = this.fChildrenType[i3];
                    if (i6 == 0) {
                        if (this.fChildren[i3].rawname != qNameArr[i5].rawname) {
                            return i4;
                        }
                    } else if (i6 == 6) {
                        String str = this.fChildren[i3].uri;
                        if (!(str == null || str == qNameArr[i4].uri)) {
                            return i4;
                        }
                    } else if (i6 == 8) {
                        if (qNameArr[i4].uri != null) {
                            return i4;
                        }
                    } else if (i6 == 7 && this.fChildren[i3].uri == qNameArr[i4].uri) {
                        return i4;
                    }
                    i3++;
                }
            }
            return -1;
        }
        for (int i7 = 0; i7 < i2; i7++) {
            QName qName = qNameArr[i + i7];
            if (qName.localpart != null) {
                int i8 = 0;
                while (i8 < this.fCount) {
                    int i9 = this.fChildrenType[i8];
                    if (i9 != 0) {
                        if (i9 == 6) {
                            String str2 = this.fChildren[i8].uri;
                            if (str2 == null || str2 == qNameArr[i7].uri) {
                                break;
                            }
                        } else if (i9 != 8) {
                            if (i9 == 7 && this.fChildren[i8].uri != qNameArr[i7].uri) {
                                break;
                            }
                        } else if (qNameArr[i7].uri == null) {
                            break;
                        }
                    } else if (qName.rawname == this.fChildren[i8].rawname) {
                        break;
                    }
                    i8++;
                }
                if (i8 == this.fCount) {
                    return i7;
                }
            }
        }
        return -1;
    }
}
