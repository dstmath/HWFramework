package ohos.com.sun.org.apache.xerces.internal.impl.xs.models;

import java.util.ArrayList;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SubstitutionGroupHandler;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaException;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;

public class XSEmptyCM implements XSCMValidator {
    private static final Vector EMPTY = new Vector(0);
    private static final short STATE_START = 0;

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public ArrayList checkMinMaxBounds() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public boolean checkUniqueParticleAttribution(SubstitutionGroupHandler substitutionGroupHandler) throws XMLSchemaException {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public int[] startContentModel() {
        return new int[]{0};
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public Object oneTransition(QName qName, int[] iArr, SubstitutionGroupHandler substitutionGroupHandler) {
        if (iArr[0] < 0) {
            iArr[0] = -2;
            return null;
        }
        iArr[0] = -1;
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public boolean endContentModel(int[] iArr) {
        return iArr[0] >= 0;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public Vector whatCanGoHere(int[] iArr) {
        return EMPTY;
    }
}
