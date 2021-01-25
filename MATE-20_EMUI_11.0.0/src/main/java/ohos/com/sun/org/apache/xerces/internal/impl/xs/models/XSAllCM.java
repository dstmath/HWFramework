package ohos.com.sun.org.apache.xerces.internal.impl.xs.models;

import java.util.ArrayList;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SubstitutionGroupHandler;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaException;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSConstraints;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSElementDecl;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;

public class XSAllCM implements XSCMValidator {
    private static final short STATE_CHILD = 1;
    private static final short STATE_START = 0;
    private static final short STATE_VALID = 1;
    private XSElementDecl[] fAllElements;
    private boolean fHasOptionalContent = false;
    private boolean[] fIsOptionalElement;
    private int fNumElements = 0;

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public ArrayList checkMinMaxBounds() {
        return null;
    }

    public XSAllCM(boolean z, int i) {
        this.fHasOptionalContent = z;
        this.fAllElements = new XSElementDecl[i];
        this.fIsOptionalElement = new boolean[i];
    }

    public void addElement(XSElementDecl xSElementDecl, boolean z) {
        XSElementDecl[] xSElementDeclArr = this.fAllElements;
        int i = this.fNumElements;
        xSElementDeclArr[i] = xSElementDecl;
        this.fIsOptionalElement[i] = z;
        this.fNumElements = i + 1;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public int[] startContentModel() {
        int[] iArr = new int[(this.fNumElements + 1)];
        for (int i = 0; i <= this.fNumElements; i++) {
            iArr[i] = 0;
        }
        return iArr;
    }

    /* access modifiers changed from: package-private */
    public Object findMatchingDecl(QName qName, SubstitutionGroupHandler substitutionGroupHandler) {
        XSElementDecl xSElementDecl = null;
        int i = 0;
        while (i < this.fNumElements && (xSElementDecl = substitutionGroupHandler.getMatchingElemDecl(qName, this.fAllElements[i])) == null) {
            i++;
        }
        return xSElementDecl;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public Object oneTransition(QName qName, int[] iArr, SubstitutionGroupHandler substitutionGroupHandler) {
        XSElementDecl matchingElemDecl;
        if (iArr[0] < 0) {
            iArr[0] = -2;
            return findMatchingDecl(qName, substitutionGroupHandler);
        }
        iArr[0] = 1;
        int i = 0;
        while (i < this.fNumElements) {
            int i2 = i + 1;
            if (iArr[i2] == 0 && (matchingElemDecl = substitutionGroupHandler.getMatchingElemDecl(qName, this.fAllElements[i])) != null) {
                iArr[i2] = 1;
                return matchingElemDecl;
            }
            i = i2;
        }
        iArr[0] = -1;
        return findMatchingDecl(qName, substitutionGroupHandler);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public boolean endContentModel(int[] iArr) {
        int i = iArr[0];
        if (i == -1 || i == -2) {
            return false;
        }
        if (this.fHasOptionalContent && i == 0) {
            return true;
        }
        for (int i2 = 0; i2 < this.fNumElements; i2++) {
            if (!this.fIsOptionalElement[i2] && iArr[i2 + 1] == 0) {
                return false;
            }
        }
        return true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public boolean checkUniqueParticleAttribution(SubstitutionGroupHandler substitutionGroupHandler) throws XMLSchemaException {
        int i = 0;
        while (i < this.fNumElements) {
            int i2 = i + 1;
            for (int i3 = i2; i3 < this.fNumElements; i3++) {
                XSElementDecl[] xSElementDeclArr = this.fAllElements;
                if (XSConstraints.overlapUPA(xSElementDeclArr[i], xSElementDeclArr[i3], substitutionGroupHandler)) {
                    throw new XMLSchemaException("cos-nonambig", new Object[]{this.fAllElements[i].toString(), this.fAllElements[i3].toString()});
                }
            }
            i = i2;
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public Vector whatCanGoHere(int[] iArr) {
        Vector vector = new Vector();
        int i = 0;
        while (i < this.fNumElements) {
            int i2 = i + 1;
            if (iArr[i2] == 0) {
                vector.addElement(this.fAllElements[i]);
            }
            i = i2;
        }
        return vector;
    }
}
