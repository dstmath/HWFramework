package org.apache.xalan.templates;

import java.util.Vector;
import javax.xml.transform.TransformerException;

public class StylesheetComposed extends Stylesheet {
    static final long serialVersionUID = -3444072247410233923L;
    private int m_endImportCountComposed;
    private int m_importCountComposed;
    private int m_importNumber = -1;
    private transient Vector m_includesComposed;

    public StylesheetComposed(Stylesheet parent) {
        super(parent);
    }

    public boolean isAggregatedType() {
        return true;
    }

    public void recompose(Vector recomposableElements) throws TransformerException {
        int n = getIncludeCountComposed();
        for (int i = -1; i < n; i++) {
            Stylesheet included = getIncludeComposed(i);
            int s = included.getOutputCount();
            for (int j = 0; j < s; j++) {
                recomposableElements.addElement(included.getOutput(j));
            }
            int s2 = included.getAttributeSetCount();
            for (int j2 = 0; j2 < s2; j2++) {
                recomposableElements.addElement(included.getAttributeSet(j2));
            }
            int s3 = included.getDecimalFormatCount();
            for (int j3 = 0; j3 < s3; j3++) {
                recomposableElements.addElement(included.getDecimalFormat(j3));
            }
            int s4 = included.getKeyCount();
            for (int j4 = 0; j4 < s4; j4++) {
                recomposableElements.addElement(included.getKey(j4));
            }
            int s5 = included.getNamespaceAliasCount();
            for (int j5 = 0; j5 < s5; j5++) {
                recomposableElements.addElement(included.getNamespaceAlias(j5));
            }
            int s6 = included.getTemplateCount();
            for (int j6 = 0; j6 < s6; j6++) {
                recomposableElements.addElement(included.getTemplate(j6));
            }
            int s7 = included.getVariableOrParamCount();
            for (int j7 = 0; j7 < s7; j7++) {
                recomposableElements.addElement(included.getVariableOrParam(j7));
            }
            int s8 = included.getStripSpaceCount();
            for (int j8 = 0; j8 < s8; j8++) {
                recomposableElements.addElement(included.getStripSpace(j8));
            }
            int s9 = included.getPreserveSpaceCount();
            for (int j9 = 0; j9 < s9; j9++) {
                recomposableElements.addElement(included.getPreserveSpace(j9));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void recomposeImports() {
        this.m_importNumber = getStylesheetRoot().getImportNumber(this);
        this.m_importCountComposed = (getStylesheetRoot().getGlobalImportCount() - this.m_importNumber) - 1;
        int count = getImportCount();
        if (count > 0) {
            this.m_endImportCountComposed += count;
            while (count > 0) {
                count--;
                this.m_endImportCountComposed += getImport(count).getEndImportCountComposed();
            }
        }
        int count2 = getIncludeCountComposed();
        while (count2 > 0) {
            count2--;
            int imports = getIncludeComposed(count2).getImportCount();
            this.m_endImportCountComposed += imports;
            while (imports > 0) {
                imports--;
                this.m_endImportCountComposed += getIncludeComposed(count2).getImport(imports).getEndImportCountComposed();
            }
        }
    }

    public StylesheetComposed getImportComposed(int i) throws ArrayIndexOutOfBoundsException {
        return getStylesheetRoot().getGlobalImport(1 + this.m_importNumber + i);
    }

    public int getImportCountComposed() {
        return this.m_importCountComposed;
    }

    public int getEndImportCountComposed() {
        return this.m_endImportCountComposed;
    }

    /* access modifiers changed from: package-private */
    public void recomposeIncludes(Stylesheet including) {
        int n = including.getIncludeCount();
        if (n > 0) {
            if (this.m_includesComposed == null) {
                this.m_includesComposed = new Vector();
            }
            for (int i = 0; i < n; i++) {
                Stylesheet included = including.getInclude(i);
                this.m_includesComposed.addElement(included);
                recomposeIncludes(included);
            }
        }
    }

    public Stylesheet getIncludeComposed(int i) throws ArrayIndexOutOfBoundsException {
        if (-1 == i) {
            return this;
        }
        if (this.m_includesComposed != null) {
            return (Stylesheet) this.m_includesComposed.elementAt(i);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getIncludeCountComposed() {
        if (this.m_includesComposed != null) {
            return this.m_includesComposed.size();
        }
        return 0;
    }

    public void recomposeTemplates(boolean flushFirst) throws TransformerException {
    }
}
