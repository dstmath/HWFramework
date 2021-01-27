package ohos.com.sun.org.apache.xerces.internal.impl;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner;
import ohos.com.sun.org.apache.xerces.internal.util.XML11Char;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializerConstants;

public class XML11DocumentScannerImpl extends XMLDocumentScannerImpl {
    private final XMLStringBuffer fStringBuffer = new XMLStringBuffer();
    private final XMLStringBuffer fStringBuffer2 = new XMLStringBuffer();
    private final XMLStringBuffer fStringBuffer3 = new XMLStringBuffer();

    /* access modifiers changed from: protected */
    public String getVersionNotSupportedKey() {
        return "VersionNotSupported11";
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl
    public int scanContent(XMLStringBuffer xMLStringBuffer) throws IOException, XNIException {
        this.fTempString.length = 0;
        int scanContent = this.fEntityScanner.scanContent(this.fTempString);
        xMLStringBuffer.append(this.fTempString);
        if (scanContent == 13 || scanContent == 133 || scanContent == 8232) {
            this.fEntityScanner.scanChar(null);
            xMLStringBuffer.append((char) scanContent);
            scanContent = -1;
        }
        if (scanContent != 93) {
            return scanContent;
        }
        xMLStringBuffer.append((char) this.fEntityScanner.scanChar(null));
        this.fInScanContent = true;
        if (this.fEntityScanner.skipChar(93, null)) {
            xMLStringBuffer.append(']');
            while (this.fEntityScanner.skipChar(93, null)) {
                xMLStringBuffer.append(']');
            }
            if (this.fEntityScanner.skipChar(62, null)) {
                reportFatalError("CDEndInContent", null);
            }
        }
        this.fInScanContent = false;
        return -1;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x01fc  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x020d A[ADDED_TO_REGION, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x01f2  */
    public boolean scanAttributeValue(XMLString xMLString, XMLString xMLString2, String str, boolean z, String str2, boolean z2) throws IOException, XNIException {
        int i;
        int i2;
        int peekChar = this.fEntityScanner.peekChar();
        if (!(peekChar == 39 || peekChar == 34)) {
            reportFatalError("OpenQuoteExpected", new Object[]{str2, str});
        }
        this.fEntityScanner.scanChar(XMLScanner.NameType.ATTRIBUTE);
        int i3 = this.fEntityDepth;
        int scanLiteral = this.fEntityScanner.scanLiteral(peekChar, xMLString, z2);
        int i4 = -1;
        if (scanLiteral == peekChar) {
            i = isUnchangedByNormalization(xMLString);
            if (i == -1) {
                xMLString2.setValues(xMLString);
                if (this.fEntityScanner.scanChar(XMLScanner.NameType.ATTRIBUTE) != peekChar) {
                    reportFatalError("CloseQuoteExpected", new Object[]{str2, str});
                }
                return true;
            }
        } else {
            i = 0;
        }
        this.fStringBuffer2.clear();
        this.fStringBuffer2.append(xMLString);
        normalizeWhitespace(xMLString, i);
        if (scanLiteral != peekChar) {
            this.fScanningAttribute = true;
            this.fStringBuffer.clear();
            while (true) {
                this.fStringBuffer.append(xMLString);
                if (scanLiteral == 38) {
                    this.fEntityScanner.skipChar(38, XMLScanner.NameType.REFERENCE);
                    if (i3 == this.fEntityDepth) {
                        this.fStringBuffer2.append('&');
                    }
                    if (this.fEntityScanner.skipChar(35, XMLScanner.NameType.REFERENCE)) {
                        if (i3 == this.fEntityDepth) {
                            this.fStringBuffer2.append('#');
                        }
                        scanCharReferenceValue(this.fStringBuffer, this.fStringBuffer2);
                        i2 = i4;
                        scanLiteral = this.fEntityScanner.scanLiteral(peekChar, xMLString, z2);
                        if (i3 == this.fEntityDepth) {
                            this.fStringBuffer2.append(xMLString);
                        }
                        normalizeWhitespace(xMLString);
                        if (scanLiteral == peekChar && i3 == this.fEntityDepth) {
                            break;
                        }
                        i4 = i2;
                    } else {
                        String scanName = this.fEntityScanner.scanName(XMLScanner.NameType.REFERENCE);
                        if (scanName == null) {
                            reportFatalError("NameRequiredInReference", null);
                        } else if (i3 == this.fEntityDepth) {
                            this.fStringBuffer2.append(scanName);
                        }
                        if (!this.fEntityScanner.skipChar(59, XMLScanner.NameType.REFERENCE)) {
                            reportFatalError("SemicolonRequiredInReference", new Object[]{scanName});
                        } else if (i3 == this.fEntityDepth) {
                            this.fStringBuffer2.append(';');
                        }
                        if (resolveCharacter(scanName, this.fStringBuffer)) {
                            checkEntityLimit(false, this.fEntityScanner.fCurrentEntity.name, 1);
                        } else if (this.fEntityManager.isExternalEntity(scanName)) {
                            reportFatalError("ReferenceToExternalEntity", new Object[]{scanName});
                        } else {
                            if (!this.fEntityManager.isDeclaredEntity(scanName)) {
                                if (!z) {
                                    reportFatalError("EntityNotDeclared", new Object[]{scanName});
                                } else if (this.fValidation) {
                                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EntityNotDeclared", new Object[]{scanName}, 1);
                                }
                            }
                            this.fEntityManager.startEntity(true, scanName, true);
                        }
                    }
                } else if (scanLiteral == 60) {
                    reportFatalError("LessthanInAttValue", new Object[]{str2, str});
                    this.fEntityScanner.scanChar(null);
                    if (i3 == this.fEntityDepth) {
                        this.fStringBuffer2.append((char) scanLiteral);
                    }
                } else if (scanLiteral == 37 || scanLiteral == 93) {
                    i2 = -1;
                    this.fEntityScanner.scanChar(null);
                    char c = (char) scanLiteral;
                    this.fStringBuffer.append(c);
                    if (i3 == this.fEntityDepth) {
                        this.fStringBuffer2.append(c);
                    }
                    scanLiteral = this.fEntityScanner.scanLiteral(peekChar, xMLString, z2);
                    if (i3 == this.fEntityDepth) {
                    }
                    normalizeWhitespace(xMLString);
                    if (scanLiteral == peekChar) {
                    }
                    i4 = i2;
                } else if (scanLiteral == 10 || scanLiteral == 13 || scanLiteral == 133 || scanLiteral == 8232) {
                    i2 = -1;
                    this.fEntityScanner.scanChar(null);
                    this.fStringBuffer.append(' ');
                    if (i3 == this.fEntityDepth) {
                        this.fStringBuffer2.append('\n');
                    }
                    scanLiteral = this.fEntityScanner.scanLiteral(peekChar, xMLString, z2);
                    if (i3 == this.fEntityDepth) {
                    }
                    normalizeWhitespace(xMLString);
                    if (scanLiteral == peekChar) {
                    }
                    i4 = i2;
                } else {
                    i2 = -1;
                    if (scanLiteral != -1) {
                        if (XMLChar.isHighSurrogate(scanLiteral)) {
                            this.fStringBuffer3.clear();
                            if (scanSurrogates(this.fStringBuffer3)) {
                                this.fStringBuffer.append(this.fStringBuffer3);
                                if (i3 == this.fEntityDepth) {
                                    this.fStringBuffer2.append(this.fStringBuffer3);
                                }
                            }
                        } else {
                            i2 = -1;
                        }
                    }
                    if (scanLiteral != i2 && isInvalidLiteral(scanLiteral)) {
                        reportFatalError("InvalidCharInAttValue", new Object[]{str2, str, Integer.toString(scanLiteral, 16)});
                        this.fEntityScanner.scanChar(null);
                        if (i3 == this.fEntityDepth) {
                            this.fStringBuffer2.append((char) scanLiteral);
                        }
                    }
                    scanLiteral = this.fEntityScanner.scanLiteral(peekChar, xMLString, z2);
                    if (i3 == this.fEntityDepth) {
                    }
                    normalizeWhitespace(xMLString);
                    if (scanLiteral == peekChar) {
                    }
                    i4 = i2;
                }
                i2 = -1;
                scanLiteral = this.fEntityScanner.scanLiteral(peekChar, xMLString, z2);
                if (i3 == this.fEntityDepth) {
                }
                normalizeWhitespace(xMLString);
                if (scanLiteral == peekChar) {
                }
                i4 = i2;
            }
            this.fStringBuffer.append(xMLString);
            xMLString.setValues(this.fStringBuffer);
            this.fScanningAttribute = false;
        }
        xMLString2.setValues(this.fStringBuffer2);
        if (this.fEntityScanner.scanChar(null) != peekChar) {
            reportFatalError("CloseQuoteExpected", new Object[]{str2, str});
        }
        return xMLString2.equals(xMLString.ch, xMLString.offset, xMLString.length);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner
    public boolean scanPubidLiteral(XMLString xMLString) throws IOException, XNIException {
        int scanChar = this.fEntityScanner.scanChar(null);
        if (scanChar == 39 || scanChar == 34) {
            this.fStringBuffer.clear();
            boolean z = true;
            boolean z2 = true;
            while (true) {
                int scanChar2 = this.fEntityScanner.scanChar(null);
                if (scanChar2 == 32 || scanChar2 == 10 || scanChar2 == 13 || scanChar2 == 133 || scanChar2 == 8232) {
                    if (!z) {
                        this.fStringBuffer.append(' ');
                        z = true;
                    }
                } else if (scanChar2 == scanChar) {
                    if (z) {
                        this.fStringBuffer.length--;
                    }
                    xMLString.setValues(this.fStringBuffer);
                    return z2;
                } else if (XMLChar.isPubid(scanChar2)) {
                    this.fStringBuffer.append((char) scanChar2);
                    z = false;
                } else if (scanChar2 == -1) {
                    reportFatalError("PublicIDUnterminated", null);
                    return false;
                } else {
                    reportFatalError("InvalidCharInPublicID", new Object[]{Integer.toHexString(scanChar2)});
                    z2 = false;
                }
            }
        } else {
            reportFatalError("QuoteRequiredInPublicID", null);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner
    public void normalizeWhitespace(XMLString xMLString) {
        int i = xMLString.offset + xMLString.length;
        for (int i2 = xMLString.offset; i2 < i; i2++) {
            if (XMLChar.isSpace(xMLString.ch[i2])) {
                xMLString.ch[i2] = ' ';
            }
        }
    }

    /* access modifiers changed from: protected */
    public void normalizeWhitespace(XMLString xMLString, int i) {
        int i2 = xMLString.offset + xMLString.length;
        for (int i3 = xMLString.offset + i; i3 < i2; i3++) {
            if (XMLChar.isSpace(xMLString.ch[i3])) {
                xMLString.ch[i3] = ' ';
            }
        }
    }

    /* access modifiers changed from: protected */
    public int isUnchangedByNormalization(XMLString xMLString) {
        int i = xMLString.offset + xMLString.length;
        for (int i2 = xMLString.offset; i2 < i; i2++) {
            if (XMLChar.isSpace(xMLString.ch[i2])) {
                return i2 - xMLString.offset;
            }
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner
    public boolean isInvalid(int i) {
        return XML11Char.isXML11Invalid(i);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner
    public boolean isInvalidLiteral(int i) {
        return !XML11Char.isXML11ValidLiteral(i);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner
    public boolean isValidNameChar(int i) {
        return XML11Char.isXML11Name(i);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner
    public boolean isValidNameStartChar(int i) {
        return XML11Char.isXML11NameStart(i);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner
    public boolean isValidNCName(int i) {
        return XML11Char.isXML11NCName(i);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner
    public boolean isValidNameStartHighSurrogate(int i) {
        return XML11Char.isXML11NameHighSurrogate(i);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner
    public boolean versionSupported(String str) {
        return str.equals(SerializerConstants.XMLVERSION11) || str.equals("1.0");
    }
}
