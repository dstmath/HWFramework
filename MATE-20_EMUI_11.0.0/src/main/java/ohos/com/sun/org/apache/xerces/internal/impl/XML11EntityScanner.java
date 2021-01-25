package ohos.com.sun.org.apache.xerces.internal.impl;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner;
import ohos.com.sun.org.apache.xerces.internal.util.XML11Char;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.xml.internal.stream.Entity;

public class XML11EntityScanner extends XMLEntityScanner {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityScanner
    public int peekChar() throws IOException {
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        }
        char c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        if (!this.fCurrentEntity.isExternal()) {
            return c;
        }
        if (c == '\r' || c == 133 || c == 8232) {
            return 10;
        }
        return c;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0037, code lost:
        if (r7 == false) goto L_0x007c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x002f, code lost:
        if (r1 != 8232) goto L_0x007c;
     */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityScanner
    public int scanChar(XMLScanner.NameType nameType) throws IOException {
        boolean z;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        }
        int i = this.fCurrentEntity.position;
        char[] cArr = this.fCurrentEntity.ch;
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        int i2 = scannedEntity.position;
        scannedEntity.position = i2 + 1;
        char c = cArr[i2];
        if (c != '\n') {
            if (c != '\r') {
                if (c != 133) {
                }
            }
            z = this.fCurrentEntity.isExternal();
        } else {
            z = false;
        }
        this.fCurrentEntity.lineNumber++;
        this.fCurrentEntity.columnNumber = 1;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            invokeListeners(1);
            this.fCurrentEntity.ch[0] = (char) c;
            load(1, false, false);
            i = 0;
        }
        if (c == '\r' && z) {
            char[] cArr2 = this.fCurrentEntity.ch;
            Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
            int i3 = scannedEntity2.position;
            scannedEntity2.position = i3 + 1;
            char c2 = cArr2[i3];
            if (!(c2 == '\n' || c2 == 133)) {
                this.fCurrentEntity.position--;
            }
        }
        c = '\n';
        this.fCurrentEntity.columnNumber++;
        if (!this.detectingVersion) {
            checkEntityLimit(nameType, this.fCurrentEntity, i, this.fCurrentEntity.position - i);
        }
        return c;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x011c, code lost:
        r7.fCurrentEntity.position--;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0123, code lost:
        r3 = r0;
     */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityScanner
    public String scanNmtoken() throws IOException {
        int i = 0;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        }
        int i2 = this.fCurrentEntity.position;
        while (true) {
            char c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
            if (!XML11Char.isXML11Name(c)) {
                if (!XML11Char.isXML11NameHighSurrogate(c)) {
                    break;
                }
                Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
                int i3 = scannedEntity.position + 1;
                scannedEntity.position = i3;
                if (i3 == this.fCurrentEntity.count) {
                    int i4 = this.fCurrentEntity.position - i2;
                    invokeListeners(i4);
                    if (i4 == this.fCurrentEntity.ch.length) {
                        char[] cArr = new char[(this.fCurrentEntity.ch.length << 1)];
                        System.arraycopy(this.fCurrentEntity.ch, i2, cArr, 0, i4);
                        this.fCurrentEntity.ch = cArr;
                    } else {
                        System.arraycopy(this.fCurrentEntity.ch, i2, this.fCurrentEntity.ch, 0, i4);
                    }
                    if (load(i4, false, false)) {
                        this.fCurrentEntity.startPosition--;
                        this.fCurrentEntity.position--;
                        break;
                    }
                    i2 = 0;
                }
                char c2 = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                if (!XMLChar.isLowSurrogate(c2) || !XML11Char.isXML11Name(XMLChar.supplemental(c, c2))) {
                    break;
                }
                Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
                int i5 = scannedEntity2.position + 1;
                scannedEntity2.position = i5;
                if (i5 == this.fCurrentEntity.count) {
                    int i6 = this.fCurrentEntity.position - i2;
                    invokeListeners(i6);
                    if (i6 == this.fCurrentEntity.ch.length) {
                        char[] cArr2 = new char[(this.fCurrentEntity.ch.length << 1)];
                        System.arraycopy(this.fCurrentEntity.ch, i2, cArr2, 0, i6);
                        this.fCurrentEntity.ch = cArr2;
                    } else {
                        System.arraycopy(this.fCurrentEntity.ch, i2, this.fCurrentEntity.ch, 0, i6);
                    }
                    if (load(i6, false, false)) {
                        break;
                    }
                } else {
                    continue;
                }
            } else {
                Entity.ScannedEntity scannedEntity3 = this.fCurrentEntity;
                int i7 = scannedEntity3.position + 1;
                scannedEntity3.position = i7;
                if (i7 == this.fCurrentEntity.count) {
                    int i8 = this.fCurrentEntity.position - i2;
                    invokeListeners(i8);
                    if (i8 == this.fCurrentEntity.ch.length) {
                        char[] cArr3 = new char[(this.fCurrentEntity.ch.length << 1)];
                        System.arraycopy(this.fCurrentEntity.ch, i2, cArr3, 0, i8);
                        this.fCurrentEntity.ch = cArr3;
                    } else {
                        System.arraycopy(this.fCurrentEntity.ch, i2, this.fCurrentEntity.ch, 0, i8);
                    }
                    if (load(i8, false, false)) {
                        break;
                    }
                } else {
                    continue;
                }
            }
            i2 = 0;
        }
        int i9 = this.fCurrentEntity.position - i;
        this.fCurrentEntity.columnNumber += i9;
        if (i9 > 0) {
            return this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, i, i9);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00ed, code lost:
        if (load(r1, false, false) == false) goto L_0x00f0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x013d, code lost:
        if (load(r1, false, false) == false) goto L_0x00f0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0140, code lost:
        r8.fCurrentEntity.position--;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0147, code lost:
        r3 = r0;
     */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00e1  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00f2  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0156  */
    /* JADX WARNING: Removed duplicated region for block: B:71:? A[RETURN, SYNTHETIC] */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityScanner
    public String scanName(XMLScanner.NameType nameType) throws IOException {
        char c;
        int i;
        int i2 = 0;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        }
        int i3 = this.fCurrentEntity.position;
        char c2 = this.fCurrentEntity.ch[i3];
        if (XML11Char.isXML11NameStart(c2)) {
            Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
            int i4 = scannedEntity.position + 1;
            scannedEntity.position = i4;
            if (i4 == this.fCurrentEntity.count) {
                invokeListeners(1);
                this.fCurrentEntity.ch[0] = c2;
                if (load(1, false, false)) {
                    this.fCurrentEntity.columnNumber++;
                    return this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, 0, 1);
                }
            }
            while (true) {
                c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                if (!XML11Char.isXML11Name(c)) {
                    if (XML11Char.isXML11NameHighSurrogate(c)) {
                        int checkBeforeLoad = checkBeforeLoad(this.fCurrentEntity, i3, i3);
                        if (checkBeforeLoad > 0) {
                            if (!load(checkBeforeLoad, false, false)) {
                                i3 = 0;
                            }
                            this.fCurrentEntity.position--;
                            this.fCurrentEntity.startPosition--;
                            break;
                        }
                        char c3 = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                        if (XMLChar.isLowSurrogate(c3) && XML11Char.isXML11Name(XMLChar.supplemental(c, c3))) {
                            int checkBeforeLoad2 = checkBeforeLoad(this.fCurrentEntity, i3, i3);
                            if (checkBeforeLoad2 > 0) {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    int checkBeforeLoad3 = checkBeforeLoad(this.fCurrentEntity, i3, i3);
                    if (checkBeforeLoad3 > 0) {
                        break;
                    }
                }
            }
            i = this.fCurrentEntity.position - i2;
            this.fCurrentEntity.columnNumber += i;
            if (i <= 0) {
                return null;
            }
            checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, i2, i);
            checkEntityLimit(nameType, this.fCurrentEntity, i2, i);
            return this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, i2, i);
        }
        if (XML11Char.isXML11NameHighSurrogate(c2)) {
            Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
            int i5 = scannedEntity2.position + 1;
            scannedEntity2.position = i5;
            if (i5 == this.fCurrentEntity.count) {
                invokeListeners(1);
                this.fCurrentEntity.ch[0] = c2;
                if (load(1, false, false)) {
                    this.fCurrentEntity.position--;
                    this.fCurrentEntity.startPosition--;
                    return null;
                }
                i3 = 0;
            }
            char c4 = this.fCurrentEntity.ch[this.fCurrentEntity.position];
            if (!XMLChar.isLowSurrogate(c4) || !XML11Char.isXML11NameStart(XMLChar.supplemental(c2, c4))) {
                this.fCurrentEntity.position--;
            } else {
                Entity.ScannedEntity scannedEntity3 = this.fCurrentEntity;
                int i6 = scannedEntity3.position + 1;
                scannedEntity3.position = i6;
                if (i6 == this.fCurrentEntity.count) {
                    invokeListeners(2);
                    this.fCurrentEntity.ch[0] = c2;
                    this.fCurrentEntity.ch[1] = c4;
                    if (load(2, false, false)) {
                        this.fCurrentEntity.columnNumber += 2;
                        return this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, 0, 2);
                    }
                }
                while (true) {
                    c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                    if (!XML11Char.isXML11Name(c)) {
                    }
                }
                i = this.fCurrentEntity.position - i2;
                this.fCurrentEntity.columnNumber += i;
                if (i <= 0) {
                }
            }
        }
        return null;
        i3 = 0;
        while (true) {
            c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
            if (!XML11Char.isXML11Name(c)) {
            }
        }
        i = this.fCurrentEntity.position - i2;
        this.fCurrentEntity.columnNumber += i;
        if (i <= 0) {
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00f1, code lost:
        r1 = r8.fCurrentEntity.position - r0;
        invokeListeners(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00fe, code lost:
        if (r1 != r8.fCurrentEntity.ch.length) goto L_0x0114;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0100, code lost:
        r4 = new char[(r8.fCurrentEntity.ch.length << 1)];
        java.lang.System.arraycopy(r8.fCurrentEntity.ch, r0, r4, 0, r1);
        r8.fCurrentEntity.ch = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0114, code lost:
        java.lang.System.arraycopy(r8.fCurrentEntity.ch, r0, r8.fCurrentEntity.ch, 0, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0123, code lost:
        if (load(r1, false, false) == false) goto L_0x004e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x01a6, code lost:
        r1 = r8.fCurrentEntity.position - r0;
        invokeListeners(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x01b3, code lost:
        if (r1 != r8.fCurrentEntity.ch.length) goto L_0x01c9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x01b5, code lost:
        r4 = new char[(r8.fCurrentEntity.ch.length << 1)];
        java.lang.System.arraycopy(r8.fCurrentEntity.ch, r0, r4, 0, r1);
        r8.fCurrentEntity.ch = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x01c9, code lost:
        java.lang.System.arraycopy(r8.fCurrentEntity.ch, r0, r8.fCurrentEntity.ch, 0, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x01d8, code lost:
        if (load(r1, false, false) == false) goto L_0x004e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00e4  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0127  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x01f1  */
    /* JADX WARNING: Removed duplicated region for block: B:83:? A[RETURN, SYNTHETIC] */
    public String scanNCName() throws IOException {
        char c;
        int i;
        int i2 = 0;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        }
        int i3 = this.fCurrentEntity.position;
        char c2 = this.fCurrentEntity.ch[i3];
        if (XML11Char.isXML11NCNameStart(c2)) {
            Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
            int i4 = scannedEntity.position + 1;
            scannedEntity.position = i4;
            if (i4 == this.fCurrentEntity.count) {
                invokeListeners(1);
                this.fCurrentEntity.ch[0] = c2;
                if (load(1, false, false)) {
                    this.fCurrentEntity.columnNumber++;
                    return this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, 0, 1);
                }
            }
            while (true) {
                c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                if (!XML11Char.isXML11NCName(c)) {
                    if (!XML11Char.isXML11NameHighSurrogate(c)) {
                        break;
                    }
                    Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
                    int i5 = scannedEntity2.position + 1;
                    scannedEntity2.position = i5;
                    if (i5 == this.fCurrentEntity.count) {
                        int i6 = this.fCurrentEntity.position - i3;
                        invokeListeners(i6);
                        if (i6 == this.fCurrentEntity.ch.length) {
                            char[] cArr = new char[(this.fCurrentEntity.ch.length << 1)];
                            System.arraycopy(this.fCurrentEntity.ch, i3, cArr, 0, i6);
                            this.fCurrentEntity.ch = cArr;
                        } else {
                            System.arraycopy(this.fCurrentEntity.ch, i3, this.fCurrentEntity.ch, 0, i6);
                        }
                        if (!load(i6, false, false)) {
                            i3 = 0;
                        }
                        this.fCurrentEntity.startPosition--;
                        this.fCurrentEntity.position--;
                        break;
                    }
                    char c3 = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                    if (XMLChar.isLowSurrogate(c3) && XML11Char.isXML11NCName(XMLChar.supplemental(c, c3))) {
                        Entity.ScannedEntity scannedEntity3 = this.fCurrentEntity;
                        int i7 = scannedEntity3.position + 1;
                        scannedEntity3.position = i7;
                        if (i7 == this.fCurrentEntity.count) {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    Entity.ScannedEntity scannedEntity4 = this.fCurrentEntity;
                    int i8 = scannedEntity4.position + 1;
                    scannedEntity4.position = i8;
                    if (i8 == this.fCurrentEntity.count) {
                        break;
                    }
                }
            }
            this.fCurrentEntity.position--;
            i2 = i3;
            i = this.fCurrentEntity.position - i2;
            this.fCurrentEntity.columnNumber += i;
            if (i > 0) {
                return this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, i2, i);
            }
            return null;
        }
        if (XML11Char.isXML11NameHighSurrogate(c2)) {
            Entity.ScannedEntity scannedEntity5 = this.fCurrentEntity;
            int i9 = scannedEntity5.position + 1;
            scannedEntity5.position = i9;
            if (i9 == this.fCurrentEntity.count) {
                invokeListeners(1);
                this.fCurrentEntity.ch[0] = c2;
                if (load(1, false, false)) {
                    this.fCurrentEntity.position--;
                    this.fCurrentEntity.startPosition--;
                    return null;
                }
                i3 = 0;
            }
            char c4 = this.fCurrentEntity.ch[this.fCurrentEntity.position];
            if (!XMLChar.isLowSurrogate(c4) || !XML11Char.isXML11NCNameStart(XMLChar.supplemental(c2, c4))) {
                this.fCurrentEntity.position--;
            } else {
                Entity.ScannedEntity scannedEntity6 = this.fCurrentEntity;
                int i10 = scannedEntity6.position + 1;
                scannedEntity6.position = i10;
                if (i10 == this.fCurrentEntity.count) {
                    invokeListeners(2);
                    this.fCurrentEntity.ch[0] = c2;
                    this.fCurrentEntity.ch[1] = c4;
                    if (load(2, false, false)) {
                        this.fCurrentEntity.columnNumber += 2;
                        return this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, 0, 2);
                    }
                }
                while (true) {
                    c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                    if (!XML11Char.isXML11NCName(c)) {
                    }
                }
                this.fCurrentEntity.position--;
                i2 = i3;
                i = this.fCurrentEntity.position - i2;
                this.fCurrentEntity.columnNumber += i;
                if (i > 0) {
                }
            }
        }
        return null;
        i3 = 0;
        while (true) {
            c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
            if (!XML11Char.isXML11NCName(c)) {
            }
        }
        this.fCurrentEntity.position--;
        i2 = i3;
        i = this.fCurrentEntity.position - i2;
        this.fCurrentEntity.columnNumber += i;
        if (i > 0) {
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x011c, code lost:
        r0 = 0;
        r7 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0178, code lost:
        r12.fCurrentEntity.position--;
        r7 = true;
     */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00f6  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0122  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0190  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x01fa A[RETURN] */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityScanner
    public boolean scanQName(QName qName, XMLScanner.NameType nameType) throws IOException {
        char c;
        boolean z;
        int i;
        String str;
        String str2;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        }
        int i2 = this.fCurrentEntity.position;
        char c2 = this.fCurrentEntity.ch[i2];
        if (XML11Char.isXML11NCNameStart(c2)) {
            Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
            int i3 = scannedEntity.position + 1;
            scannedEntity.position = i3;
            if (i3 == this.fCurrentEntity.count) {
                invokeListeners(1);
                this.fCurrentEntity.ch[0] = c2;
                if (load(1, false, false)) {
                    this.fCurrentEntity.columnNumber++;
                    String addSymbol = this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, 0, 1);
                    qName.setValues(null, addSymbol, addSymbol, null);
                    checkEntityLimit(nameType, this.fCurrentEntity, 0, 1);
                    return true;
                }
            }
            int i4 = -1;
            while (true) {
                c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                if (!XML11Char.isXML11Name(c)) {
                    if (!XML11Char.isXML11NameHighSurrogate(c)) {
                        break;
                    }
                    int checkBeforeLoad = checkBeforeLoad(this.fCurrentEntity, i2, i4);
                    if (checkBeforeLoad > 0) {
                        if (i4 != -1) {
                            i4 -= i2;
                        }
                        if (load(checkBeforeLoad, false, false)) {
                            this.fCurrentEntity.startPosition--;
                            this.fCurrentEntity.position--;
                            z = true;
                            i2 = 0;
                            break;
                        }
                        i2 = 0;
                    }
                    char c3 = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                    if (!XMLChar.isLowSurrogate(c3) || !XML11Char.isXML11Name(XMLChar.supplemental(c, c3))) {
                        break;
                    }
                    int checkBeforeLoad2 = checkBeforeLoad(this.fCurrentEntity, i2, i4);
                    if (checkBeforeLoad2 <= 0) {
                        continue;
                    } else {
                        if (i4 != -1) {
                            i4 -= i2;
                        }
                        if (load(checkBeforeLoad2, false, false)) {
                            break;
                        }
                        i2 = 0;
                    }
                } else {
                    if (c == ':') {
                        if (i4 != -1) {
                            break;
                        }
                        i4 = this.fCurrentEntity.position;
                        checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, i2, i4 - i2);
                    }
                    int checkBeforeLoad3 = checkBeforeLoad(this.fCurrentEntity, i2, i4);
                    if (checkBeforeLoad3 <= 0) {
                        continue;
                    } else {
                        if (i4 != -1) {
                            i4 -= i2;
                        }
                        if (load(checkBeforeLoad3, false, false)) {
                            break;
                        }
                        i2 = 0;
                    }
                }
            }
            z = false;
            i = this.fCurrentEntity.position - i2;
            this.fCurrentEntity.columnNumber += i;
            if (i <= 0) {
                return false;
            }
            String addSymbol2 = this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, i2, i);
            if (i4 != -1) {
                int i5 = i4 - i2;
                checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, i2, i5);
                str = this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, i2, i5);
                int i6 = (i - i5) - 1;
                int i7 = i4 + 1;
                if (!XML11Char.isXML11NCNameStart(this.fCurrentEntity.ch[i7]) && (!XML11Char.isXML11NameHighSurrogate(this.fCurrentEntity.ch[i7]) || z)) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "IllegalQName", null, 2);
                }
                checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, i7, i6);
                str2 = this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, i7, i6);
            } else {
                checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, i2, i);
                str2 = addSymbol2;
                str = null;
            }
            qName.setValues(str, str2, addSymbol2, null);
            checkEntityLimit(nameType, this.fCurrentEntity, i2, i);
            return true;
        }
        if (XML11Char.isXML11NameHighSurrogate(c2)) {
            Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
            int i8 = scannedEntity2.position + 1;
            scannedEntity2.position = i8;
            if (i8 == this.fCurrentEntity.count) {
                invokeListeners(1);
                this.fCurrentEntity.ch[0] = c2;
                if (load(1, false, false)) {
                    this.fCurrentEntity.startPosition--;
                    this.fCurrentEntity.position--;
                    return false;
                }
                i2 = 0;
            }
            char c4 = this.fCurrentEntity.ch[this.fCurrentEntity.position];
            if (!XMLChar.isLowSurrogate(c4) || !XML11Char.isXML11NCNameStart(XMLChar.supplemental(c2, c4))) {
                this.fCurrentEntity.position--;
            } else {
                Entity.ScannedEntity scannedEntity3 = this.fCurrentEntity;
                int i9 = scannedEntity3.position + 1;
                scannedEntity3.position = i9;
                if (i9 == this.fCurrentEntity.count) {
                    invokeListeners(2);
                    this.fCurrentEntity.ch[0] = c2;
                    this.fCurrentEntity.ch[1] = c4;
                    if (load(2, false, false)) {
                        this.fCurrentEntity.columnNumber += 2;
                        String addSymbol3 = this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, 0, 2);
                        qName.setValues(null, addSymbol3, addSymbol3, null);
                        checkEntityLimit(nameType, this.fCurrentEntity, 0, 2);
                        return true;
                    }
                }
                int i42 = -1;
                while (true) {
                    c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                    if (!XML11Char.isXML11Name(c)) {
                    }
                }
                z = false;
                i = this.fCurrentEntity.position - i2;
                this.fCurrentEntity.columnNumber += i;
                if (i <= 0) {
                }
            }
        }
        return false;
        i2 = 0;
        int i422 = -1;
        while (true) {
            c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
            if (!XML11Char.isXML11Name(c)) {
            }
        }
        z = false;
        i = this.fCurrentEntity.position - i2;
        this.fCurrentEntity.columnNumber += i;
        if (i <= 0) {
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00db, code lost:
        r18.fCurrentEntity.position--;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0130, code lost:
        r4 = r2;
        r2 = r3;
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x018b, code lost:
        r18.fCurrentEntity.position--;
     */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0130 A[EDGE_INSN: B:77:0x0130->B:41:0x0130 ?: BREAK  , SYNTHETIC] */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityScanner
    public int scanContent(XMLString xMLString) throws IOException {
        boolean z;
        int i = 0;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        } else if (this.fCurrentEntity.position == this.fCurrentEntity.count - 1) {
            invokeListeners(1);
            this.fCurrentEntity.ch[0] = this.fCurrentEntity.ch[this.fCurrentEntity.count - 1];
            load(1, false, false);
            this.fCurrentEntity.position = 0;
            this.fCurrentEntity.startPosition = 0;
        }
        int i2 = this.fCurrentEntity.position;
        char c = this.fCurrentEntity.ch[i2];
        boolean isExternal = this.fCurrentEntity.isExternal();
        if (c == '\n' || ((c == '\r' || c == 133 || c == 8232) && isExternal)) {
            int i3 = i2;
            int i4 = 0;
            while (true) {
                char[] cArr = this.fCurrentEntity.ch;
                Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
                int i5 = scannedEntity.position;
                scannedEntity.position = i5 + 1;
                char c2 = cArr[i5];
                if (c2 != '\r' || !isExternal) {
                    if (c2 == '\n' || ((c2 == 133 || c2 == 8232) && isExternal)) {
                        i4++;
                        this.fCurrentEntity.lineNumber++;
                        this.fCurrentEntity.columnNumber = 1;
                        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                            checkEntityLimit(null, this.fCurrentEntity, i3, i4);
                            this.fCurrentEntity.baseCharOffset += this.fCurrentEntity.position - this.fCurrentEntity.startPosition;
                            this.fCurrentEntity.position = i4;
                            this.fCurrentEntity.startPosition = i4;
                            if (load(i4, false, true)) {
                                break;
                            }
                            i3 = 0;
                        }
                        if (this.fCurrentEntity.position >= this.fCurrentEntity.count - 1) {
                            break;
                        }
                    }
                } else {
                    i4++;
                    this.fCurrentEntity.lineNumber++;
                    this.fCurrentEntity.columnNumber = 1;
                    if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                        checkEntityLimit(null, this.fCurrentEntity, i3, i4);
                        this.fCurrentEntity.baseCharOffset += this.fCurrentEntity.position - this.fCurrentEntity.startPosition;
                        this.fCurrentEntity.position = i4;
                        this.fCurrentEntity.startPosition = i4;
                        if (load(i4, false, true)) {
                            break;
                        }
                        i3 = 0;
                    }
                    char c3 = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                    if (c3 == '\n' || c3 == 133) {
                        this.fCurrentEntity.position++;
                        i3++;
                    } else {
                        i4++;
                    }
                    if (this.fCurrentEntity.position >= this.fCurrentEntity.count - 1) {
                    }
                }
            }
            z = true;
            i = i4;
            i2 = 0;
            for (int i6 = i2; i6 < this.fCurrentEntity.position; i6++) {
                this.fCurrentEntity.ch[i6] = '\n';
            }
            int i7 = this.fCurrentEntity.position - i2;
            if (this.fCurrentEntity.position == this.fCurrentEntity.count - 1) {
                checkEntityLimit(null, this.fCurrentEntity, i2, i7);
                xMLString.setValues(this.fCurrentEntity.ch, i2, i7);
                return -1;
            }
        } else {
            z = false;
        }
        if (isExternal) {
            while (true) {
                if (this.fCurrentEntity.position >= this.fCurrentEntity.count) {
                    break;
                }
                char[] cArr2 = this.fCurrentEntity.ch;
                Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
                int i8 = scannedEntity2.position;
                scannedEntity2.position = i8 + 1;
                char c4 = cArr2[i8];
                if (!XML11Char.isXML11Content(c4) || c4 == 133 || c4 == 8232) {
                    break;
                }
            }
        } else {
            while (true) {
                if (this.fCurrentEntity.position >= this.fCurrentEntity.count) {
                    break;
                }
                char[] cArr3 = this.fCurrentEntity.ch;
                Entity.ScannedEntity scannedEntity3 = this.fCurrentEntity;
                int i9 = scannedEntity3.position;
                scannedEntity3.position = i9 + 1;
                if (!XML11Char.isXML11InternalEntityContent(cArr3[i9])) {
                    this.fCurrentEntity.position--;
                    break;
                }
            }
        }
        int i10 = this.fCurrentEntity.position - i2;
        this.fCurrentEntity.columnNumber += i10 - i;
        if (!z) {
            checkEntityLimit(null, this.fCurrentEntity, i2, i10);
        }
        xMLString.setValues(this.fCurrentEntity.ch, i2, i10);
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            return -1;
        }
        char c5 = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        if ((c5 == '\r' || c5 == 133 || c5 == 8232) && isExternal) {
            return 10;
        }
        return c5;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0113, code lost:
        r5 = r3;
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x01aa, code lost:
        r17.fCurrentEntity.position--;
     */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x018c  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0125 A[EDGE_INSN: B:81:0x0125->B:40:0x0125 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x01b1 A[EDGE_INSN: B:96:0x01b1->B:71:0x01b1 ?: BREAK  , SYNTHETIC] */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityScanner
    public int scanLiteral(int i, XMLString xMLString, boolean z) throws IOException {
        int i2 = 0;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        } else if (this.fCurrentEntity.position == this.fCurrentEntity.count - 1) {
            invokeListeners(1);
            this.fCurrentEntity.ch[0] = this.fCurrentEntity.ch[this.fCurrentEntity.count - 1];
            load(1, false, false);
            this.fCurrentEntity.startPosition = 0;
            this.fCurrentEntity.position = 0;
        }
        int i3 = this.fCurrentEntity.position;
        char c = this.fCurrentEntity.ch[i3];
        boolean isExternal = this.fCurrentEntity.isExternal();
        if (c == '\n' || ((c == '\r' || c == 133 || c == 8232) && isExternal)) {
            int i4 = i3;
            int i5 = 0;
            while (true) {
                char[] cArr = this.fCurrentEntity.ch;
                Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
                int i6 = scannedEntity.position;
                scannedEntity.position = i6 + 1;
                char c2 = cArr[i6];
                if (c2 != '\r' || !isExternal) {
                    if (c2 == '\n' || ((c2 == 133 || c2 == 8232) && isExternal)) {
                        i5++;
                        this.fCurrentEntity.lineNumber++;
                        this.fCurrentEntity.columnNumber = 1;
                        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                            this.fCurrentEntity.baseCharOffset += this.fCurrentEntity.position - this.fCurrentEntity.startPosition;
                            this.fCurrentEntity.position = i5;
                            this.fCurrentEntity.startPosition = i5;
                            if (load(i5, false, true)) {
                                break;
                            }
                            i4 = 0;
                        }
                        if (this.fCurrentEntity.position >= this.fCurrentEntity.count - 1) {
                            break;
                        }
                    }
                } else {
                    i5++;
                    this.fCurrentEntity.lineNumber++;
                    this.fCurrentEntity.columnNumber = 1;
                    if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                        this.fCurrentEntity.baseCharOffset += this.fCurrentEntity.position - this.fCurrentEntity.startPosition;
                        this.fCurrentEntity.position = i5;
                        this.fCurrentEntity.startPosition = i5;
                        if (load(i5, false, true)) {
                            break;
                        }
                        i4 = 0;
                    }
                    char c3 = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                    if (c3 == '\n' || c3 == 133) {
                        this.fCurrentEntity.position++;
                        i4++;
                    } else {
                        i5++;
                    }
                    if (this.fCurrentEntity.position >= this.fCurrentEntity.count - 1) {
                    }
                }
            }
            this.fCurrentEntity.position--;
            i2 = i5;
            i3 = i4;
            for (int i7 = i3; i7 < this.fCurrentEntity.position; i7++) {
                this.fCurrentEntity.ch[i7] = '\n';
            }
            int i8 = this.fCurrentEntity.position - i3;
            if (this.fCurrentEntity.position == this.fCurrentEntity.count - 1) {
                xMLString.setValues(this.fCurrentEntity.ch, i3, i8);
                return -1;
            }
        }
        if (isExternal) {
            while (true) {
                if (this.fCurrentEntity.position >= this.fCurrentEntity.count) {
                    break;
                }
                char[] cArr2 = this.fCurrentEntity.ch;
                Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
                int i9 = scannedEntity2.position;
                scannedEntity2.position = i9 + 1;
                char c4 = cArr2[i9];
                if (c4 != i && c4 != '%' && XML11Char.isXML11Content(c4) && c4 != 133) {
                    if (c4 == 8232) {
                        break;
                    }
                } else {
                    break;
                }
            }
            this.fCurrentEntity.position--;
        } else {
            while (true) {
                if (this.fCurrentEntity.position >= this.fCurrentEntity.count) {
                    char[] cArr3 = this.fCurrentEntity.ch;
                    Entity.ScannedEntity scannedEntity3 = this.fCurrentEntity;
                    int i10 = scannedEntity3.position;
                    scannedEntity3.position = i10 + 1;
                    char c5 = cArr3[i10];
                    if ((c5 == i && !this.fCurrentEntity.literal) || c5 == '%' || !XML11Char.isXML11InternalEntityContent(c5)) {
                        break;
                    }
                    if (this.fCurrentEntity.position >= this.fCurrentEntity.count) {
                        break;
                    }
                }
            }
        }
        int i11 = this.fCurrentEntity.position - i3;
        this.fCurrentEntity.columnNumber += i11 - i2;
        checkEntityLimit(null, this.fCurrentEntity, i3, i11);
        if (z) {
            checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, i3, i11);
        }
        xMLString.setValues(this.fCurrentEntity.ch, i3, i11);
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            return -1;
        }
        char c6 = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        if (c6 != i || !this.fCurrentEntity.literal) {
            return c6;
        }
        return -1;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r17v0, resolved type: ohos.com.sun.org.apache.xerces.internal.impl.XML11EntityScanner */
    /* JADX DEBUG: Multi-variable search result rejected for r4v37, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r4v39, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r4v49, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v0 */
    /* JADX WARN: Type inference failed for: r4v1, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r4v6 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0154, code lost:
        r17.fCurrentEntity.position--;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0195, code lost:
        r9 = r8;
        r4 = r4 ? 1 : 0;
        r4 = r4 ? 1 : 0;
        r8 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x0280, code lost:
        r17.fCurrentEntity.position--;
     */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x0332  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x033f A[LOOP:0: B:1:0x0016->B:121:0x033f, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x0342 A[LOOP:2: B:21:0x00db->B:122:0x0342, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x033c A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:134:0x01a4 A[EDGE_INSN: B:134:0x01a4->B:51:0x01a4 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityScanner
    public boolean scanData(String str, XMLStringBuffer xMLStringBuffer) throws IOException {
        int i;
        int length = str.length();
        ?? r4 = 0;
        char charAt = str.charAt(0);
        boolean isExternal = this.fCurrentEntity.isExternal();
        boolean z = false;
        while (true) {
            if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                load(r4, true, r4);
            }
            boolean z2 = r4 == true ? 1 : 0;
            boolean z3 = r4 == true ? 1 : 0;
            boolean z4 = r4 == true ? 1 : 0;
            boolean z5 = z2;
            while (this.fCurrentEntity.position >= this.fCurrentEntity.count - length && !z5) {
                System.arraycopy(this.fCurrentEntity.ch, this.fCurrentEntity.position, this.fCurrentEntity.ch, r4, this.fCurrentEntity.count - this.fCurrentEntity.position);
                z5 = load(this.fCurrentEntity.count - this.fCurrentEntity.position, r4, r4);
                this.fCurrentEntity.position = r4;
                this.fCurrentEntity.startPosition = r4;
            }
            if (this.fCurrentEntity.position >= this.fCurrentEntity.count - length) {
                int i2 = this.fCurrentEntity.count - this.fCurrentEntity.position;
                checkEntityLimit(XMLScanner.NameType.COMMENT, this.fCurrentEntity, this.fCurrentEntity.position, i2);
                xMLStringBuffer.append(this.fCurrentEntity.ch, this.fCurrentEntity.position, i2);
                this.fCurrentEntity.columnNumber += this.fCurrentEntity.count;
                this.fCurrentEntity.baseCharOffset += this.fCurrentEntity.position - this.fCurrentEntity.startPosition;
                this.fCurrentEntity.position = this.fCurrentEntity.count;
                this.fCurrentEntity.startPosition = this.fCurrentEntity.count;
                int i3 = r4 == true ? 1 : 0;
                int i4 = r4 == true ? 1 : 0;
                int i5 = r4 == true ? 1 : 0;
                int i6 = r4 == true ? 1 : 0;
                load(i3, true, r4);
                return r4;
            }
            int i7 = this.fCurrentEntity.position;
            char c = this.fCurrentEntity.ch[i7];
            if (c == '\n' || ((c == '\r' || c == 133 || c == 8232) && isExternal)) {
                int i8 = i7;
                int i9 = r4;
                boolean z6 = r4;
                while (true) {
                    char[] cArr = this.fCurrentEntity.ch;
                    Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
                    int i10 = scannedEntity.position;
                    scannedEntity.position = i10 + 1;
                    char c2 = cArr[i10];
                    if (c2 == '\r' && isExternal) {
                        i9++;
                        this.fCurrentEntity.lineNumber++;
                        this.fCurrentEntity.columnNumber = 1;
                        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                            this.fCurrentEntity.baseCharOffset += this.fCurrentEntity.position - this.fCurrentEntity.startPosition;
                            this.fCurrentEntity.position = i9;
                            this.fCurrentEntity.startPosition = i9;
                            if (load(i9, z6, true)) {
                                break;
                            }
                            i8 = z6;
                        }
                        char c3 = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                        if (c3 == '\n' || c3 == 133) {
                            this.fCurrentEntity.position++;
                            i8++;
                        } else {
                            i9++;
                        }
                        if (this.fCurrentEntity.position < this.fCurrentEntity.count - 1) {
                        }
                    } else if (c2 == '\n' || ((c2 == 133 || c2 == 8232) && isExternal)) {
                        i9++;
                        this.fCurrentEntity.lineNumber++;
                        this.fCurrentEntity.columnNumber = 1;
                        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                            this.fCurrentEntity.baseCharOffset += this.fCurrentEntity.position - this.fCurrentEntity.startPosition;
                            this.fCurrentEntity.position = i9;
                            this.fCurrentEntity.startPosition = i9;
                            this.fCurrentEntity.count = i9;
                            if (load(i9, z6, true)) {
                                break;
                            }
                            i8 = z6;
                        }
                        if (this.fCurrentEntity.position < this.fCurrentEntity.count - 1) {
                            break;
                        }
                        z6 = 0;
                    }
                }
                i = i9;
                i7 = i8;
                for (int i11 = i7; i11 < this.fCurrentEntity.position; i11++) {
                    this.fCurrentEntity.ch[i11] = '\n';
                }
                int i12 = this.fCurrentEntity.position - i7;
                if (this.fCurrentEntity.position == this.fCurrentEntity.count - 1) {
                    checkEntityLimit(XMLScanner.NameType.COMMENT, this.fCurrentEntity, i7, i12);
                    xMLStringBuffer.append(this.fCurrentEntity.ch, i7, i12);
                    return true;
                }
            } else {
                i = r4;
            }
            if (isExternal) {
                while (true) {
                    if (this.fCurrentEntity.position >= this.fCurrentEntity.count) {
                        break;
                    }
                    char[] cArr2 = this.fCurrentEntity.ch;
                    Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
                    int i13 = scannedEntity2.position;
                    scannedEntity2.position = i13 + 1;
                    char c4 = cArr2[i13];
                    if (c4 == charAt) {
                        int i14 = this.fCurrentEntity.position - 1;
                        int i15 = 1;
                        while (true) {
                            if (i15 >= length) {
                                break;
                            } else if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                                this.fCurrentEntity.position -= i15;
                                break;
                            } else {
                                char[] cArr3 = this.fCurrentEntity.ch;
                                Entity.ScannedEntity scannedEntity3 = this.fCurrentEntity;
                                int i16 = scannedEntity3.position;
                                scannedEntity3.position = i16 + 1;
                                if (str.charAt(i15) != cArr3[i16]) {
                                    this.fCurrentEntity.position--;
                                    break;
                                }
                                i15++;
                            }
                        }
                        if (this.fCurrentEntity.position == i14 + length) {
                            break;
                        }
                    } else if (c4 == '\n' || c4 == '\r' || c4 == 133 || c4 == 8232) {
                        break;
                    } else if (!XML11Char.isXML11ValidLiteral(c4)) {
                        this.fCurrentEntity.position--;
                        int i17 = this.fCurrentEntity.position - i7;
                        this.fCurrentEntity.columnNumber += i17 - i;
                        checkEntityLimit(XMLScanner.NameType.COMMENT, this.fCurrentEntity, i7, i17);
                        xMLStringBuffer.append(this.fCurrentEntity.ch, i7, i17);
                        return true;
                    }
                }
            } else {
                while (true) {
                    if (this.fCurrentEntity.position >= this.fCurrentEntity.count) {
                        break;
                    }
                    char[] cArr4 = this.fCurrentEntity.ch;
                    Entity.ScannedEntity scannedEntity4 = this.fCurrentEntity;
                    int i18 = scannedEntity4.position;
                    scannedEntity4.position = i18 + 1;
                    char c5 = cArr4[i18];
                    if (c5 == charAt) {
                        int i19 = this.fCurrentEntity.position - 1;
                        int i20 = 1;
                        while (true) {
                            if (i20 >= length) {
                                break;
                            } else if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                                this.fCurrentEntity.position -= i20;
                                break;
                            } else {
                                char[] cArr5 = this.fCurrentEntity.ch;
                                Entity.ScannedEntity scannedEntity5 = this.fCurrentEntity;
                                int i21 = scannedEntity5.position;
                                scannedEntity5.position = i21 + 1;
                                if (str.charAt(i20) != cArr5[i21]) {
                                    this.fCurrentEntity.position--;
                                    break;
                                }
                                i20++;
                            }
                        }
                        if (this.fCurrentEntity.position == i19 + length) {
                            break;
                        }
                    } else if (c5 == '\n') {
                        this.fCurrentEntity.position--;
                        break;
                    } else if (!XML11Char.isXML11Valid(c5)) {
                        this.fCurrentEntity.position--;
                        int i22 = this.fCurrentEntity.position - i7;
                        this.fCurrentEntity.columnNumber += i22 - i;
                        checkEntityLimit(XMLScanner.NameType.COMMENT, this.fCurrentEntity, i7, i22);
                        xMLStringBuffer.append(this.fCurrentEntity.ch, i7, i22);
                        return true;
                    }
                }
                int i23 = this.fCurrentEntity.position - i7;
                this.fCurrentEntity.columnNumber += i23 - i;
                checkEntityLimit(XMLScanner.NameType.COMMENT, this.fCurrentEntity, i7, i23);
                if (z) {
                    i23 -= length;
                }
                xMLStringBuffer.append(this.fCurrentEntity.ch, i7, i23);
                if (!z) {
                    return !z;
                }
                r4 = 0;
            }
            z = true;
            int i232 = this.fCurrentEntity.position - i7;
            this.fCurrentEntity.columnNumber += i232 - i;
            checkEntityLimit(XMLScanner.NameType.COMMENT, this.fCurrentEntity, i7, i232);
            if (z) {
            }
            xMLStringBuffer.append(this.fCurrentEntity.ch, i7, i232);
            if (!z) {
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityScanner
    public boolean skipChar(int i, XMLScanner.NameType nameType) throws IOException {
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        }
        int i2 = this.fCurrentEntity.position;
        char c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        if (c == i) {
            this.fCurrentEntity.position++;
            if (i == 10) {
                this.fCurrentEntity.lineNumber++;
                this.fCurrentEntity.columnNumber = 1;
            } else {
                this.fCurrentEntity.columnNumber++;
            }
            checkEntityLimit(nameType, this.fCurrentEntity, i2, this.fCurrentEntity.position - i2);
            return true;
        } else if (i == 10 && ((c == 8232 || c == 133) && this.fCurrentEntity.isExternal())) {
            this.fCurrentEntity.position++;
            this.fCurrentEntity.lineNumber++;
            this.fCurrentEntity.columnNumber = 1;
            checkEntityLimit(nameType, this.fCurrentEntity, i2, this.fCurrentEntity.position - i2);
            return true;
        } else if (i != 10 || c != '\r' || !this.fCurrentEntity.isExternal()) {
            return false;
        } else {
            if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                invokeListeners(1);
                this.fCurrentEntity.ch[0] = (char) c;
                load(1, false, false);
            }
            char[] cArr = this.fCurrentEntity.ch;
            Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
            int i3 = scannedEntity.position + 1;
            scannedEntity.position = i3;
            char c2 = cArr[i3];
            if (c2 == '\n' || c2 == 133) {
                this.fCurrentEntity.position++;
            }
            this.fCurrentEntity.lineNumber++;
            this.fCurrentEntity.columnNumber = 1;
            checkEntityLimit(nameType, this.fCurrentEntity, i2, this.fCurrentEntity.position - i2);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0129  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x013a  */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityScanner
    public boolean skipSpaces() throws IOException {
        boolean z;
        boolean z2;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        }
        if (this.fCurrentEntity == null) {
            return false;
        }
        char c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        int i = this.fCurrentEntity.position - 1;
        if (this.fCurrentEntity.isExternal()) {
            if (XML11Char.isXML11Space(c)) {
                do {
                    if (c == '\n' || c == '\r' || c == 133 || c == 8232) {
                        this.fCurrentEntity.lineNumber++;
                        this.fCurrentEntity.columnNumber = 1;
                        if (this.fCurrentEntity.position == this.fCurrentEntity.count - 1) {
                            invokeListeners(1);
                            this.fCurrentEntity.ch[0] = (char) c;
                            z2 = load(1, true, false);
                            if (!z2) {
                                this.fCurrentEntity.startPosition = 0;
                                this.fCurrentEntity.position = 0;
                            } else if (this.fCurrentEntity == null) {
                                return true;
                            }
                        } else {
                            z2 = false;
                        }
                        if (c == '\r') {
                            char[] cArr = this.fCurrentEntity.ch;
                            Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
                            int i2 = scannedEntity.position + 1;
                            scannedEntity.position = i2;
                            char c2 = cArr[i2];
                            if (!(c2 == '\n' || c2 == 133)) {
                                this.fCurrentEntity.position--;
                            }
                        }
                    } else {
                        this.fCurrentEntity.columnNumber++;
                        z2 = false;
                    }
                    checkEntityLimit(null, this.fCurrentEntity, i, this.fCurrentEntity.position - i);
                    i = this.fCurrentEntity.position;
                    if (!z2) {
                        this.fCurrentEntity.position++;
                    }
                    if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                        load(0, true, true);
                        if (this.fCurrentEntity == null) {
                            return true;
                        }
                    }
                    c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                } while (XML11Char.isXML11Space(c));
                return true;
            }
        } else if (XMLChar.isSpace(c)) {
            do {
                if (c == '\n') {
                    this.fCurrentEntity.lineNumber++;
                    this.fCurrentEntity.columnNumber = 1;
                    if (this.fCurrentEntity.position == this.fCurrentEntity.count - 1) {
                        invokeListeners(1);
                        this.fCurrentEntity.ch[0] = (char) c;
                        z = load(1, true, false);
                        if (!z) {
                            this.fCurrentEntity.startPosition = 0;
                            this.fCurrentEntity.position = 0;
                        } else if (this.fCurrentEntity == null) {
                            return true;
                        }
                        checkEntityLimit(null, this.fCurrentEntity, i, this.fCurrentEntity.position - i);
                        i = this.fCurrentEntity.position;
                        if (!z) {
                            this.fCurrentEntity.position++;
                        }
                        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                            load(0, true, true);
                            if (this.fCurrentEntity == null) {
                                return true;
                            }
                        }
                        c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                    }
                } else {
                    this.fCurrentEntity.columnNumber++;
                }
                z = false;
                checkEntityLimit(null, this.fCurrentEntity, i, this.fCurrentEntity.position - i);
                i = this.fCurrentEntity.position;
                if (!z) {
                }
                if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                }
                c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
            } while (XMLChar.isSpace(c));
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityScanner
    public boolean skipString(String str) throws IOException {
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        }
        int length = str.length();
        int i = this.fCurrentEntity.position;
        for (int i2 = 0; i2 < length; i2++) {
            char[] cArr = this.fCurrentEntity.ch;
            Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
            int i3 = scannedEntity.position;
            scannedEntity.position = i3 + 1;
            if (cArr[i3] != str.charAt(i2)) {
                this.fCurrentEntity.position -= i2 + 1;
                return false;
            }
            if (i2 < length - 1 && this.fCurrentEntity.position == this.fCurrentEntity.count) {
                invokeListeners(0);
                int i4 = i2 + 1;
                System.arraycopy(this.fCurrentEntity.ch, (this.fCurrentEntity.count - i2) - 1, this.fCurrentEntity.ch, 0, i4);
                if (load(i4, false, false)) {
                    this.fCurrentEntity.startPosition -= i4;
                    this.fCurrentEntity.position -= i4;
                    return false;
                }
            }
        }
        this.fCurrentEntity.columnNumber += length;
        if (!this.detectingVersion) {
            checkEntityLimit(null, this.fCurrentEntity, i, length);
        }
        return true;
    }
}
