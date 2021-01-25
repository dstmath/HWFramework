package ohos.com.sun.org.apache.xerces.internal.impl.dtd.models;

import ohos.com.sun.org.apache.xerces.internal.xni.QName;

public class SimpleContentModel implements ContentModelValidator {
    public static final short CHOICE = -1;
    public static final short SEQUENCE = -1;
    private QName fFirstChild = new QName();
    private int fOperator;
    private QName fSecondChild = new QName();

    public SimpleContentModel(short s, QName qName, QName qName2) {
        this.fFirstChild.setValues(qName);
        if (qName2 != null) {
            this.fSecondChild.setValues(qName2);
        } else {
            this.fSecondChild.clear();
        }
        this.fOperator = s;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.ContentModelValidator
    public int validate(QName[] qNameArr, int i, int i2) {
        int i3 = this.fOperator;
        int i4 = 0;
        if (i3 != 0) {
            if (i3 != 1) {
                if (i3 != 2) {
                    if (i3 != 3) {
                        if (i3 != 4) {
                            if (i3 != 5) {
                                throw new RuntimeException("ImplementationMessages.VAL_CST");
                            } else if (i2 == 2) {
                                if (qNameArr[i].rawname != this.fFirstChild.rawname) {
                                    return 0;
                                }
                                if (qNameArr[i + 1].rawname != this.fSecondChild.rawname) {
                                    return 1;
                                }
                                return -1;
                            } else if (i2 > 2) {
                                return 2;
                            } else {
                                return i2;
                            }
                        } else if (i2 == 0) {
                            return 0;
                        } else {
                            if (qNameArr[i].rawname != this.fFirstChild.rawname && qNameArr[i].rawname != this.fSecondChild.rawname) {
                                return 0;
                            }
                            if (i2 > 1) {
                                return 1;
                            }
                            return -1;
                        }
                    } else if (i2 == 0) {
                        return 0;
                    } else {
                        while (i4 < i2) {
                            if (qNameArr[i + i4].rawname != this.fFirstChild.rawname) {
                                return i4;
                            }
                            i4++;
                        }
                        return -1;
                    }
                } else if (i2 <= 0) {
                    return -1;
                } else {
                    while (i4 < i2) {
                        if (qNameArr[i + i4].rawname != this.fFirstChild.rawname) {
                            return i4;
                        }
                        i4++;
                    }
                    return -1;
                }
            } else if (i2 == 1 && qNameArr[i].rawname != this.fFirstChild.rawname) {
                return 0;
            } else {
                if (i2 > 1) {
                    return 1;
                }
                return -1;
            }
        } else if (i2 == 0 || qNameArr[i].rawname != this.fFirstChild.rawname) {
            return 0;
        } else {
            if (i2 > 1) {
                return 1;
            }
            return -1;
        }
    }
}
