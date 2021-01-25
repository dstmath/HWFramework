package ohos.com.sun.org.apache.xerces.internal.impl.xs.identity;

import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.xpath.XPath;
import ohos.com.sun.org.apache.xerces.internal.util.IntStack;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xs.AttributePSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.ShortList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

public class XPathMatcher {
    protected static final boolean DEBUG_ALL = false;
    protected static final boolean DEBUG_ANY = false;
    protected static final boolean DEBUG_MATCH = false;
    protected static final boolean DEBUG_METHODS = false;
    protected static final boolean DEBUG_METHODS2 = false;
    protected static final boolean DEBUG_METHODS3 = false;
    protected static final boolean DEBUG_STACK = false;
    protected static final int MATCHED = 1;
    protected static final int MATCHED_ATTRIBUTE = 3;
    protected static final int MATCHED_DESCENDANT = 5;
    protected static final int MATCHED_DESCENDANT_PREVIOUS = 13;
    private int[] fCurrentStep;
    private XPath.LocationPath[] fLocationPaths;
    private int[] fMatched;
    protected Object fMatchedString;
    private int[] fNoMatchDepth;
    final QName fQName = new QName();
    private IntStack[] fStepIndexes;

    /* access modifiers changed from: protected */
    public void handleContent(XSTypeDefinition xSTypeDefinition, boolean z, Object obj, short s, ShortList shortList) {
    }

    /* access modifiers changed from: protected */
    public void matched(Object obj, short s, ShortList shortList, boolean z) {
    }

    public XPathMatcher(XPath xPath) {
        this.fLocationPaths = xPath.getLocationPaths();
        this.fStepIndexes = new IntStack[this.fLocationPaths.length];
        int i = 0;
        while (true) {
            IntStack[] intStackArr = this.fStepIndexes;
            if (i < intStackArr.length) {
                intStackArr[i] = new IntStack();
                i++;
            } else {
                XPath.LocationPath[] locationPathArr = this.fLocationPaths;
                this.fCurrentStep = new int[locationPathArr.length];
                this.fNoMatchDepth = new int[locationPathArr.length];
                this.fMatched = new int[locationPathArr.length];
                return;
            }
        }
    }

    public boolean isMatched() {
        for (int i = 0; i < this.fLocationPaths.length; i++) {
            int[] iArr = this.fMatched;
            if ((iArr[i] & 1) == 1 && (iArr[i] & 13) != 13 && (this.fNoMatchDepth[i] == 0 || (iArr[i] & 5) == 5)) {
                return true;
            }
        }
        return false;
    }

    public void startDocumentFragment() {
        this.fMatchedString = null;
        for (int i = 0; i < this.fLocationPaths.length; i++) {
            this.fStepIndexes[i].clear();
            this.fCurrentStep[i] = 0;
            this.fNoMatchDepth[i] = 0;
            this.fMatched[i] = 0;
        }
    }

    public void startElement(QName qName, XMLAttributes xMLAttributes) {
        for (int i = 0; i < this.fLocationPaths.length; i++) {
            int i2 = this.fCurrentStep[i];
            this.fStepIndexes[i].push(i2);
            int[] iArr = this.fMatched;
            if ((iArr[i] & 5) == 1 || this.fNoMatchDepth[i] > 0) {
                int[] iArr2 = this.fNoMatchDepth;
                iArr2[i] = iArr2[i] + 1;
            } else {
                if ((iArr[i] & 5) == 5) {
                    iArr[i] = 13;
                }
                XPath.Step[] stepArr = this.fLocationPaths[i].steps;
                while (true) {
                    int[] iArr3 = this.fCurrentStep;
                    if (iArr3[i] >= stepArr.length || stepArr[iArr3[i]].axis.type != 3) {
                        break;
                    }
                    int[] iArr4 = this.fCurrentStep;
                    iArr4[i] = iArr4[i] + 1;
                }
                int[] iArr5 = this.fCurrentStep;
                if (iArr5[i] == stepArr.length) {
                    this.fMatched[i] = 1;
                } else {
                    int i3 = iArr5[i];
                    while (true) {
                        int[] iArr6 = this.fCurrentStep;
                        if (iArr6[i] >= stepArr.length || stepArr[iArr6[i]].axis.type != 4) {
                            break;
                        }
                        int[] iArr7 = this.fCurrentStep;
                        iArr7[i] = iArr7[i] + 1;
                    }
                    boolean z = this.fCurrentStep[i] > i3;
                    int[] iArr8 = this.fCurrentStep;
                    if (iArr8[i] == stepArr.length) {
                        int[] iArr9 = this.fNoMatchDepth;
                        iArr9[i] = iArr9[i] + 1;
                    } else {
                        if ((iArr8[i] == i2 || iArr8[i] > i3) && stepArr[this.fCurrentStep[i]].axis.type == 1) {
                            XPath.NodeTest nodeTest = stepArr[this.fCurrentStep[i]].nodeTest;
                            if (nodeTest.type != 1 || nodeTest.name.equals(qName)) {
                                int[] iArr10 = this.fCurrentStep;
                                iArr10[i] = iArr10[i] + 1;
                            } else {
                                int[] iArr11 = this.fCurrentStep;
                                if (iArr11[i] > i3) {
                                    iArr11[i] = i3;
                                } else {
                                    int[] iArr12 = this.fNoMatchDepth;
                                    iArr12[i] = iArr12[i] + 1;
                                }
                            }
                        }
                        int[] iArr13 = this.fCurrentStep;
                        if (iArr13[i] == stepArr.length) {
                            if (z) {
                                iArr13[i] = i3;
                                this.fMatched[i] = 5;
                            } else {
                                this.fMatched[i] = 1;
                            }
                        } else if (iArr13[i] < stepArr.length && stepArr[iArr13[i]].axis.type == 2) {
                            int length = xMLAttributes.getLength();
                            if (length > 0) {
                                XPath.NodeTest nodeTest2 = stepArr[this.fCurrentStep[i]].nodeTest;
                                int i4 = 0;
                                while (true) {
                                    if (i4 >= length) {
                                        break;
                                    }
                                    xMLAttributes.getName(i4, this.fQName);
                                    if (nodeTest2.type != 1 || nodeTest2.name.equals(this.fQName)) {
                                        break;
                                    }
                                    i4++;
                                }
                                int[] iArr14 = this.fCurrentStep;
                                iArr14[i] = iArr14[i] + 1;
                                if (iArr14[i] == stepArr.length) {
                                    this.fMatched[i] = 3;
                                    int i5 = 0;
                                    while (i5 < i && (this.fMatched[i5] & 1) != 1) {
                                        i5++;
                                    }
                                    if (i5 == i) {
                                        AttributePSVI attributePSVI = (AttributePSVI) xMLAttributes.getAugmentations(i4).getItem(Constants.ATTRIBUTE_PSVI);
                                        this.fMatchedString = attributePSVI.getActualNormalizedValue();
                                        matched(this.fMatchedString, attributePSVI.getActualNormalizedValueType(), attributePSVI.getItemValueTypes(), false);
                                    }
                                }
                            }
                            if ((this.fMatched[i] & 1) != 1) {
                                int[] iArr15 = this.fCurrentStep;
                                if (iArr15[i] > i3) {
                                    iArr15[i] = i3;
                                } else {
                                    int[] iArr16 = this.fNoMatchDepth;
                                    iArr16[i] = iArr16[i] + 1;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void endElement(QName qName, XSTypeDefinition xSTypeDefinition, boolean z, Object obj, short s, ShortList shortList) {
        for (int i = 0; i < this.fLocationPaths.length; i++) {
            this.fCurrentStep[i] = this.fStepIndexes[i].pop();
            int[] iArr = this.fNoMatchDepth;
            if (iArr[i] > 0) {
                iArr[i] = iArr[i] - 1;
            } else {
                int i2 = 0;
                while (i2 < i && (this.fMatched[i2] & 1) != 1) {
                    i2++;
                }
                if (i2 >= i) {
                    int[] iArr2 = this.fMatched;
                    if (!(iArr2[i2] == 0 || (iArr2[i2] & 3) == 3)) {
                        handleContent(xSTypeDefinition, z, obj, s, shortList);
                        this.fMatched[i] = 0;
                    }
                }
            }
        }
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        String obj = super.toString();
        int lastIndexOf = obj.lastIndexOf(46);
        if (lastIndexOf != -1) {
            obj = obj.substring(lastIndexOf + 1);
        }
        stringBuffer.append(obj);
        for (int i = 0; i < this.fLocationPaths.length; i++) {
            stringBuffer.append('[');
            XPath.Step[] stepArr = this.fLocationPaths[i].steps;
            for (int i2 = 0; i2 < stepArr.length; i2++) {
                if (i2 == this.fCurrentStep[i]) {
                    stringBuffer.append('^');
                }
                stringBuffer.append(stepArr[i2].toString());
                if (i2 < stepArr.length - 1) {
                    stringBuffer.append('/');
                }
            }
            if (this.fCurrentStep[i] == stepArr.length) {
                stringBuffer.append('^');
            }
            stringBuffer.append(']');
            stringBuffer.append(',');
        }
        return stringBuffer.toString();
    }

    private String normalize(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char charAt = str.charAt(i);
            if (charAt != '\n') {
                stringBuffer.append(charAt);
            } else {
                stringBuffer.append("\\n");
            }
        }
        return stringBuffer.toString();
    }
}
