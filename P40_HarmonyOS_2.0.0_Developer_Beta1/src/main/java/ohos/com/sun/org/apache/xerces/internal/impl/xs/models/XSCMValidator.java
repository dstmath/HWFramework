package ohos.com.sun.org.apache.xerces.internal.impl.xs.models;

import java.util.ArrayList;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SubstitutionGroupHandler;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaException;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;

public interface XSCMValidator {
    public static final short FIRST_ERROR = -1;
    public static final short SUBSEQUENT_ERROR = -2;

    ArrayList checkMinMaxBounds();

    boolean checkUniqueParticleAttribution(SubstitutionGroupHandler substitutionGroupHandler) throws XMLSchemaException;

    boolean endContentModel(int[] iArr);

    Object oneTransition(QName qName, int[] iArr, SubstitutionGroupHandler substitutionGroupHandler);

    int[] startContentModel();

    Vector whatCanGoHere(int[] iArr);
}
