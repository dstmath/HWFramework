package ohos.com.sun.org.apache.xerces.internal.impl.dtd;

import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;

/* access modifiers changed from: package-private */
public final class BalancedDTDGrammar extends DTDGrammar {
    private int fDepth = 0;
    private int[][] fGroupIndexStack;
    private int[] fGroupIndexStackSizes;
    private boolean fMixed;
    private short[] fOpStack = null;

    public BalancedDTDGrammar(SymbolTable symbolTable, XMLDTDDescription xMLDTDDescription) {
        super(symbolTable, xMLDTDDescription);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public final void startContentModel(String str, Augmentations augmentations) throws XNIException {
        this.fDepth = 0;
        initializeContentModelStacks();
        super.startContentModel(str, augmentations);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public final void startGroup(Augmentations augmentations) throws XNIException {
        this.fDepth++;
        initializeContentModelStacks();
        this.fMixed = false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public final void pcdata(Augmentations augmentations) throws XNIException {
        this.fMixed = true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public final void element(String str, Augmentations augmentations) throws XNIException {
        addToCurrentGroup(addUniqueLeafNode(str));
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public final void separator(short s, Augmentations augmentations) throws XNIException {
        if (s == 0) {
            this.fOpStack[this.fDepth] = 4;
        } else if (s == 1) {
            this.fOpStack[this.fDepth] = 5;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public final void occurrence(short s, Augmentations augmentations) throws XNIException {
        if (!this.fMixed) {
            int[] iArr = this.fGroupIndexStackSizes;
            int i = this.fDepth;
            int i2 = iArr[i] - 1;
            if (s == 2) {
                int[][] iArr2 = this.fGroupIndexStack;
                iArr2[i][i2] = addContentSpecNode(1, iArr2[i][i2], -1);
            } else if (s == 3) {
                int[][] iArr3 = this.fGroupIndexStack;
                iArr3[i][i2] = addContentSpecNode(2, iArr3[i][i2], -1);
            } else if (s == 4) {
                int[][] iArr4 = this.fGroupIndexStack;
                iArr4[i][i2] = addContentSpecNode(3, iArr4[i][i2], -1);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public final void endGroup(Augmentations augmentations) throws XNIException {
        int i = this.fGroupIndexStackSizes[this.fDepth];
        this.fDepth--;
        addToCurrentGroup(i > 0 ? addContentSpecNodes(0, i - 1) : addUniqueLeafNode(null));
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public final void endDTD(Augmentations augmentations) throws XNIException {
        super.endDTD(augmentations);
        this.fOpStack = null;
        this.fGroupIndexStack = null;
        this.fGroupIndexStackSizes = null;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar
    public final void addContentSpecToElement(XMLElementDecl xMLElementDecl) {
        setContentSpecIndex(this.fCurrentElementIndex, this.fGroupIndexStackSizes[0] > 0 ? this.fGroupIndexStack[0][0] : -1);
    }

    private int addContentSpecNodes(int i, int i2) {
        if (i == i2) {
            return this.fGroupIndexStack[this.fDepth][i];
        }
        int i3 = (i + i2) >>> 1;
        return addContentSpecNode(this.fOpStack[this.fDepth], addContentSpecNodes(i, i3), addContentSpecNodes(i3 + 1, i2));
    }

    private void initializeContentModelStacks() {
        short[] sArr = this.fOpStack;
        if (sArr == null) {
            this.fOpStack = new short[8];
            this.fGroupIndexStack = new int[8][];
            this.fGroupIndexStackSizes = new int[8];
        } else {
            int i = this.fDepth;
            if (i == sArr.length) {
                short[] sArr2 = new short[(i * 2)];
                System.arraycopy(sArr, 0, sArr2, 0, i);
                this.fOpStack = sArr2;
                int i2 = this.fDepth;
                int[][] iArr = new int[(i2 * 2)][];
                System.arraycopy(this.fGroupIndexStack, 0, iArr, 0, i2);
                this.fGroupIndexStack = iArr;
                int i3 = this.fDepth;
                int[] iArr2 = new int[(i3 * 2)];
                System.arraycopy(this.fGroupIndexStackSizes, 0, iArr2, 0, i3);
                this.fGroupIndexStackSizes = iArr2;
            }
        }
        short[] sArr3 = this.fOpStack;
        int i4 = this.fDepth;
        sArr3[i4] = -1;
        this.fGroupIndexStackSizes[i4] = 0;
    }

    private void addToCurrentGroup(int i) {
        int[][] iArr = this.fGroupIndexStack;
        int i2 = this.fDepth;
        int[] iArr2 = iArr[i2];
        int[] iArr3 = this.fGroupIndexStackSizes;
        int i3 = iArr3[i2];
        iArr3[i2] = i3 + 1;
        if (iArr2 == null) {
            iArr2 = new int[8];
            iArr[i2] = iArr2;
        } else if (i3 == iArr2.length) {
            int[] iArr4 = new int[(iArr2.length * 2)];
            System.arraycopy(iArr2, 0, iArr4, 0, iArr2.length);
            this.fGroupIndexStack[this.fDepth] = iArr4;
            iArr2 = iArr4;
        }
        iArr2[i3] = i;
    }
}
