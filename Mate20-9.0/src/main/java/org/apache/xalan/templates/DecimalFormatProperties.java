package org.apache.xalan.templates;

import java.text.DecimalFormatSymbols;
import org.apache.xml.utils.QName;

public class DecimalFormatProperties extends ElemTemplateElement {
    static final long serialVersionUID = -6559409339256269446L;
    DecimalFormatSymbols m_dfs = new DecimalFormatSymbols();
    private QName m_qname = null;

    public DecimalFormatProperties(int docOrderNumber) {
        this.m_dfs.setInfinity(Constants.ATTRVAL_INFINITY);
        this.m_dfs.setNaN("NaN");
        this.m_docOrderNumber = docOrderNumber;
    }

    public DecimalFormatSymbols getDecimalFormatSymbols() {
        return this.m_dfs;
    }

    public void setName(QName qname) {
        this.m_qname = qname;
    }

    public QName getName() {
        if (this.m_qname == null) {
            return new QName("");
        }
        return this.m_qname;
    }

    public void setDecimalSeparator(char ds) {
        this.m_dfs.setDecimalSeparator(ds);
    }

    public char getDecimalSeparator() {
        return this.m_dfs.getDecimalSeparator();
    }

    public void setGroupingSeparator(char gs) {
        this.m_dfs.setGroupingSeparator(gs);
    }

    public char getGroupingSeparator() {
        return this.m_dfs.getGroupingSeparator();
    }

    public void setInfinity(String inf) {
        this.m_dfs.setInfinity(inf);
    }

    public String getInfinity() {
        return this.m_dfs.getInfinity();
    }

    public void setMinusSign(char v) {
        this.m_dfs.setMinusSign(v);
    }

    public char getMinusSign() {
        return this.m_dfs.getMinusSign();
    }

    public void setNaN(String v) {
        this.m_dfs.setNaN(v);
    }

    public String getNaN() {
        return this.m_dfs.getNaN();
    }

    public String getNodeName() {
        return Constants.ELEMNAME_DECIMALFORMAT_STRING;
    }

    public void setPercent(char v) {
        this.m_dfs.setPercent(v);
    }

    public char getPercent() {
        return this.m_dfs.getPercent();
    }

    public void setPerMille(char v) {
        this.m_dfs.setPerMill(v);
    }

    public char getPerMille() {
        return this.m_dfs.getPerMill();
    }

    public int getXSLToken() {
        return 83;
    }

    public void setZeroDigit(char v) {
        this.m_dfs.setZeroDigit(v);
    }

    public char getZeroDigit() {
        return this.m_dfs.getZeroDigit();
    }

    public void setDigit(char v) {
        this.m_dfs.setDigit(v);
    }

    public char getDigit() {
        return this.m_dfs.getDigit();
    }

    public void setPatternSeparator(char v) {
        this.m_dfs.setPatternSeparator(v);
    }

    public char getPatternSeparator() {
        return this.m_dfs.getPatternSeparator();
    }

    public void recompose(StylesheetRoot root) {
        root.recomposeDecimalFormats(this);
    }
}
