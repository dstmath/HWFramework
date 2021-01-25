package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.StringListImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.xs.StringList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAnnotation;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSWildcard;

public class XSWildcardDecl implements XSWildcard {
    public static final String ABSENT = null;
    public XSObjectList fAnnotations = null;
    private String fDescription = null;
    public String[] fNamespaceList;
    public short fProcessContents = 1;
    public short fType = 1;

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getName() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getNamespace() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public XSNamespaceItem getNamespaceItem() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public short getType() {
        return 9;
    }

    public boolean allowNamespace(String str) {
        short s = this.fType;
        if (s == 1) {
            return true;
        }
        if (s == 2) {
            int length = this.fNamespaceList.length;
            boolean z = false;
            for (int i = 0; i < length && !z; i++) {
                if (str == this.fNamespaceList[i]) {
                    z = true;
                }
            }
            if (!z) {
                return true;
            }
        }
        if (this.fType == 3) {
            int length2 = this.fNamespaceList.length;
            for (int i2 = 0; i2 < length2; i2++) {
                if (str == this.fNamespaceList[i2]) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSubsetOf(XSWildcardDecl xSWildcardDecl) {
        if (xSWildcardDecl == null) {
            return false;
        }
        short s = xSWildcardDecl.fType;
        if (s == 1) {
            return true;
        }
        if (this.fType == 2 && s == 2 && this.fNamespaceList[0] == xSWildcardDecl.fNamespaceList[0]) {
            return true;
        }
        if (this.fType == 3) {
            if (xSWildcardDecl.fType == 3 && subset2sets(this.fNamespaceList, xSWildcardDecl.fNamespaceList)) {
                return true;
            }
            if (xSWildcardDecl.fType == 2 && !elementInSet(xSWildcardDecl.fNamespaceList[0], this.fNamespaceList) && !elementInSet(ABSENT, this.fNamespaceList)) {
                return true;
            }
        }
        return false;
    }

    public boolean weakerProcessContents(XSWildcardDecl xSWildcardDecl) {
        if (this.fProcessContents == 3 && xSWildcardDecl.fProcessContents == 1) {
            return true;
        }
        return this.fProcessContents == 2 && xSWildcardDecl.fProcessContents != 2;
    }

    public XSWildcardDecl performUnionWith(XSWildcardDecl xSWildcardDecl, short s) {
        short s2;
        String[] strArr;
        String[] strArr2;
        if (xSWildcardDecl == null) {
            return null;
        }
        XSWildcardDecl xSWildcardDecl2 = new XSWildcardDecl();
        xSWildcardDecl2.fProcessContents = s;
        if (areSame(xSWildcardDecl)) {
            xSWildcardDecl2.fType = this.fType;
            xSWildcardDecl2.fNamespaceList = this.fNamespaceList;
        } else {
            short s3 = this.fType;
            if (s3 == 1 || (s2 = xSWildcardDecl.fType) == 1) {
                xSWildcardDecl2.fType = 1;
            } else if (s3 == 3 && s2 == 3) {
                xSWildcardDecl2.fType = 3;
                xSWildcardDecl2.fNamespaceList = union2sets(this.fNamespaceList, xSWildcardDecl.fNamespaceList);
            } else if (this.fType == 2 && xSWildcardDecl.fType == 2) {
                xSWildcardDecl2.fType = 2;
                xSWildcardDecl2.fNamespaceList = new String[2];
                String[] strArr3 = xSWildcardDecl2.fNamespaceList;
                String str = ABSENT;
                strArr3[0] = str;
                strArr3[1] = str;
            } else if ((this.fType == 2 && xSWildcardDecl.fType == 3) || (this.fType == 3 && xSWildcardDecl.fType == 2)) {
                if (this.fType == 2) {
                    strArr = this.fNamespaceList;
                    strArr2 = xSWildcardDecl.fNamespaceList;
                } else {
                    strArr = xSWildcardDecl.fNamespaceList;
                    strArr2 = this.fNamespaceList;
                }
                boolean elementInSet = elementInSet(ABSENT, strArr2);
                if (strArr[0] != ABSENT) {
                    boolean elementInSet2 = elementInSet(strArr[0], strArr2);
                    if (elementInSet2 && elementInSet) {
                        xSWildcardDecl2.fType = 1;
                    } else if (elementInSet2 && !elementInSet) {
                        xSWildcardDecl2.fType = 2;
                        xSWildcardDecl2.fNamespaceList = new String[2];
                        String[] strArr4 = xSWildcardDecl2.fNamespaceList;
                        String str2 = ABSENT;
                        strArr4[0] = str2;
                        strArr4[1] = str2;
                    } else if (!elementInSet2 && elementInSet) {
                        return null;
                    } else {
                        xSWildcardDecl2.fType = 2;
                        xSWildcardDecl2.fNamespaceList = strArr;
                    }
                } else if (elementInSet) {
                    xSWildcardDecl2.fType = 1;
                } else {
                    xSWildcardDecl2.fType = 2;
                    xSWildcardDecl2.fNamespaceList = strArr;
                }
            }
        }
        return xSWildcardDecl2;
    }

    public XSWildcardDecl performIntersectionWith(XSWildcardDecl xSWildcardDecl, short s) {
        short s2;
        String[] strArr;
        String[] strArr2;
        if (xSWildcardDecl == null) {
            return null;
        }
        XSWildcardDecl xSWildcardDecl2 = new XSWildcardDecl();
        xSWildcardDecl2.fProcessContents = s;
        if (areSame(xSWildcardDecl)) {
            xSWildcardDecl2.fType = this.fType;
            xSWildcardDecl2.fNamespaceList = this.fNamespaceList;
        } else {
            short s3 = this.fType;
            if (s3 == 1 || (s2 = xSWildcardDecl.fType) == 1) {
                if (this.fType == 1) {
                    this = xSWildcardDecl;
                }
                xSWildcardDecl2.fType = this.fType;
                xSWildcardDecl2.fNamespaceList = this.fNamespaceList;
            } else if ((s3 == 2 && s2 == 3) || (this.fType == 3 && xSWildcardDecl.fType == 2)) {
                if (this.fType == 2) {
                    String[] strArr3 = this.fNamespaceList;
                    String[] strArr4 = xSWildcardDecl.fNamespaceList;
                    strArr = strArr3;
                    strArr2 = strArr4;
                } else {
                    strArr = xSWildcardDecl.fNamespaceList;
                    strArr2 = this.fNamespaceList;
                }
                int length = strArr2.length;
                String[] strArr5 = new String[length];
                int i = 0;
                for (int i2 = 0; i2 < length; i2++) {
                    if (!(strArr2[i2] == strArr[0] || strArr2[i2] == ABSENT)) {
                        strArr5[i] = strArr2[i2];
                        i++;
                    }
                }
                xSWildcardDecl2.fType = 3;
                xSWildcardDecl2.fNamespaceList = new String[i];
                System.arraycopy(strArr5, 0, xSWildcardDecl2.fNamespaceList, 0, i);
            } else if (this.fType == 3 && xSWildcardDecl.fType == 3) {
                xSWildcardDecl2.fType = 3;
                xSWildcardDecl2.fNamespaceList = intersect2sets(this.fNamespaceList, xSWildcardDecl.fNamespaceList);
            } else if (this.fType == 2 && xSWildcardDecl.fType == 2) {
                String str = this.fNamespaceList[0];
                String str2 = ABSENT;
                if (!(str == str2 || xSWildcardDecl.fNamespaceList[0] == str2)) {
                    return null;
                }
                if (this.fNamespaceList[0] == ABSENT) {
                    this = xSWildcardDecl;
                }
                xSWildcardDecl2.fType = this.fType;
                xSWildcardDecl2.fNamespaceList = this.fNamespaceList;
            }
        }
        return xSWildcardDecl2;
    }

    private boolean areSame(XSWildcardDecl xSWildcardDecl) {
        short s = this.fType;
        if (s == xSWildcardDecl.fType) {
            if (s == 1) {
                return true;
            }
            if (s == 2) {
                return this.fNamespaceList[0] == xSWildcardDecl.fNamespaceList[0];
            }
            if (this.fNamespaceList.length == xSWildcardDecl.fNamespaceList.length) {
                int i = 0;
                while (true) {
                    String[] strArr = this.fNamespaceList;
                    if (i >= strArr.length) {
                        return true;
                    }
                    if (!elementInSet(strArr[i], xSWildcardDecl.fNamespaceList)) {
                        return false;
                    }
                    i++;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public String[] intersect2sets(String[] strArr, String[] strArr2) {
        String[] strArr3 = new String[Math.min(strArr.length, strArr2.length)];
        int i = 0;
        for (int i2 = 0; i2 < strArr.length; i2++) {
            if (elementInSet(strArr[i2], strArr2)) {
                strArr3[i] = strArr[i2];
                i++;
            }
        }
        String[] strArr4 = new String[i];
        System.arraycopy(strArr3, 0, strArr4, 0, i);
        return strArr4;
    }

    /* access modifiers changed from: package-private */
    public String[] union2sets(String[] strArr, String[] strArr2) {
        String[] strArr3 = new String[strArr.length];
        int i = 0;
        for (int i2 = 0; i2 < strArr.length; i2++) {
            if (!elementInSet(strArr[i2], strArr2)) {
                strArr3[i] = strArr[i2];
                i++;
            }
        }
        String[] strArr4 = new String[(strArr2.length + i)];
        System.arraycopy(strArr3, 0, strArr4, 0, i);
        System.arraycopy(strArr2, 0, strArr4, i, strArr2.length);
        return strArr4;
    }

    /* access modifiers changed from: package-private */
    public boolean subset2sets(String[] strArr, String[] strArr2) {
        for (String str : strArr) {
            if (!elementInSet(str, strArr2)) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean elementInSet(String str, String[] strArr) {
        boolean z = false;
        for (int i = 0; i < strArr.length && !z; i++) {
            if (str == strArr[i]) {
                z = true;
            }
        }
        return z;
    }

    public String toString() {
        if (this.fDescription == null) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("WC[");
            short s = this.fType;
            if (s == 1) {
                stringBuffer.append(SchemaSymbols.ATTVAL_TWOPOUNDANY);
            } else if (s == 2) {
                stringBuffer.append(SchemaSymbols.ATTVAL_TWOPOUNDOTHER);
                stringBuffer.append(":\"");
                String[] strArr = this.fNamespaceList;
                if (strArr[0] != null) {
                    stringBuffer.append(strArr[0]);
                }
                stringBuffer.append("\"");
            } else if (s == 3 && this.fNamespaceList.length != 0) {
                stringBuffer.append("\"");
                String[] strArr2 = this.fNamespaceList;
                if (strArr2[0] != null) {
                    stringBuffer.append(strArr2[0]);
                }
                stringBuffer.append("\"");
                for (int i = 1; i < this.fNamespaceList.length; i++) {
                    stringBuffer.append(",\"");
                    String[] strArr3 = this.fNamespaceList;
                    if (strArr3[i] != null) {
                        stringBuffer.append(strArr3[i]);
                    }
                    stringBuffer.append("\"");
                }
            }
            stringBuffer.append(']');
            this.fDescription = stringBuffer.toString();
        }
        return this.fDescription;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSWildcard
    public short getConstraintType() {
        return this.fType;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSWildcard
    public StringList getNsConstraintList() {
        String[] strArr = this.fNamespaceList;
        return new StringListImpl(strArr, strArr == null ? 0 : strArr.length);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSWildcard
    public short getProcessContents() {
        return this.fProcessContents;
    }

    public String getProcessContentsAsString() {
        short s = this.fProcessContents;
        if (s == 1) {
            return SchemaSymbols.ATTVAL_STRICT;
        }
        if (s != 2) {
            return s != 3 ? "invalid value" : SchemaSymbols.ATTVAL_LAX;
        }
        return SchemaSymbols.ATTVAL_SKIP;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSWildcard
    public XSAnnotation getAnnotation() {
        XSObjectList xSObjectList = this.fAnnotations;
        if (xSObjectList != null) {
            return (XSAnnotation) xSObjectList.item(0);
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSWildcard
    public XSObjectList getAnnotations() {
        XSObjectList xSObjectList = this.fAnnotations;
        return xSObjectList != null ? xSObjectList : XSObjectListImpl.EMPTY_LIST;
    }
}
