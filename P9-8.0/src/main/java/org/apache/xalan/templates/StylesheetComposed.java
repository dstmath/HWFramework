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
            int j;
            Stylesheet included = getIncludeComposed(i);
            int s = included.getOutputCount();
            for (j = 0; j < s; j++) {
                recomposableElements.addElement(included.getOutput(j));
            }
            s = included.getAttributeSetCount();
            for (j = 0; j < s; j++) {
                recomposableElements.addElement(included.getAttributeSet(j));
            }
            s = included.getDecimalFormatCount();
            for (j = 0; j < s; j++) {
                recomposableElements.addElement(included.getDecimalFormat(j));
            }
            s = included.getKeyCount();
            for (j = 0; j < s; j++) {
                recomposableElements.addElement(included.getKey(j));
            }
            s = included.getNamespaceAliasCount();
            for (j = 0; j < s; j++) {
                recomposableElements.addElement(included.getNamespaceAlias(j));
            }
            s = included.getTemplateCount();
            for (j = 0; j < s; j++) {
                recomposableElements.addElement(included.getTemplate(j));
            }
            s = included.getVariableOrParamCount();
            for (j = 0; j < s; j++) {
                recomposableElements.addElement(included.getVariableOrParam(j));
            }
            s = included.getStripSpaceCount();
            for (j = 0; j < s; j++) {
                recomposableElements.addElement(included.getStripSpace(j));
            }
            s = included.getPreserveSpaceCount();
            for (j = 0; j < s; j++) {
                recomposableElements.addElement(included.getPreserveSpace(j));
            }
        }
    }

    void recomposeImports() {
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
        count = getIncludeCountComposed();
        while (count > 0) {
            count--;
            int imports = getIncludeComposed(count).getImportCount();
            this.m_endImportCountComposed += imports;
            while (imports > 0) {
                imports--;
                this.m_endImportCountComposed += getIncludeComposed(count).getImport(imports).getEndImportCountComposed();
            }
        }
    }

    public StylesheetComposed getImportComposed(int i) throws ArrayIndexOutOfBoundsException {
        return getStylesheetRoot().getGlobalImport((this.m_importNumber + 1) + i);
    }

    public int getImportCountComposed() {
        return this.m_importCountComposed;
    }

    public int getEndImportCountComposed() {
        return this.m_endImportCountComposed;
    }

    void recomposeIncludes(Stylesheet including) {
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
        return this.m_includesComposed != null ? this.m_includesComposed.size() : 0;
    }

    public void recomposeTemplates(boolean flushFirst) throws TransformerException {
    }
}
