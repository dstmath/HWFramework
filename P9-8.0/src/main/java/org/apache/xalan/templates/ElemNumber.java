package org.apache.xalan.templates;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.CountersTable;
import org.apache.xalan.transformer.DecimalToRoman;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.NodeVector;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.StringBufferPool;
import org.apache.xml.utils.res.CharArrayWrapper;
import org.apache.xml.utils.res.IntArrayWrapper;
import org.apache.xml.utils.res.LongArrayWrapper;
import org.apache.xml.utils.res.StringArrayWrapper;
import org.apache.xml.utils.res.XResourceBundle;
import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.compiler.PsuedoNames;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ElemNumber extends ElemTemplateElement {
    private static final DecimalToRoman[] m_romanConvertTable;
    static final long serialVersionUID = 8118472298274407610L;
    private CharArrayWrapper m_alphaCountTable = null;
    private XPath m_countMatchPattern = null;
    private AVT m_format_avt = null;
    private XPath m_fromMatchPattern = null;
    private AVT m_groupingSeparator_avt = null;
    private AVT m_groupingSize_avt = null;
    private AVT m_lang_avt = null;
    private AVT m_lettervalue_avt = null;
    private int m_level = 1;
    private XPath m_valueExpr = null;

    private class MyPrefixResolver implements PrefixResolver {
        DTM dtm;
        int handle;
        boolean handleNullPrefix;

        public MyPrefixResolver(Node xpathExpressionContext, DTM dtm, int handle, boolean handleNullPrefix) {
            this.dtm = dtm;
            this.handle = handle;
            this.handleNullPrefix = handleNullPrefix;
        }

        public String getNamespaceForPrefix(String prefix) {
            return this.dtm.getNamespaceURI(this.handle);
        }

        public String getNamespaceForPrefix(String prefix, Node context) {
            return getNamespaceForPrefix(prefix);
        }

        public String getBaseIdentifier() {
            return ElemNumber.this.getBaseIdentifier();
        }

        public boolean handlesNullPrefixes() {
            return this.handleNullPrefix;
        }
    }

    class NumberFormatStringTokenizer {
        private int currentPosition;
        private int maxPosition;
        private String str;

        public NumberFormatStringTokenizer(String str) {
            this.str = str;
            this.maxPosition = str.length();
        }

        public void reset() {
            this.currentPosition = 0;
        }

        public String nextToken() {
            if (this.currentPosition >= this.maxPosition) {
                throw new NoSuchElementException();
            }
            int start = this.currentPosition;
            while (this.currentPosition < this.maxPosition && Character.isLetterOrDigit(this.str.charAt(this.currentPosition))) {
                this.currentPosition++;
            }
            if (start == this.currentPosition && (Character.isLetterOrDigit(this.str.charAt(this.currentPosition)) ^ 1) != 0) {
                this.currentPosition++;
            }
            return this.str.substring(start, this.currentPosition);
        }

        public boolean isLetterOrDigitAhead() {
            for (int pos = this.currentPosition; pos < this.maxPosition; pos++) {
                if (Character.isLetterOrDigit(this.str.charAt(pos))) {
                    return true;
                }
            }
            return false;
        }

        public boolean nextIsSep() {
            if (Character.isLetterOrDigit(this.str.charAt(this.currentPosition))) {
                return false;
            }
            return true;
        }

        public boolean hasMoreTokens() {
            return this.currentPosition < this.maxPosition;
        }

        public int countTokens() {
            int count = 0;
            int currpos = this.currentPosition;
            while (currpos < this.maxPosition) {
                int start = currpos;
                while (currpos < this.maxPosition && Character.isLetterOrDigit(this.str.charAt(currpos))) {
                    currpos++;
                }
                if (start == currpos && !Character.isLetterOrDigit(this.str.charAt(currpos))) {
                    currpos++;
                }
                count++;
            }
            return count;
        }
    }

    public void setCount(XPath v) {
        this.m_countMatchPattern = v;
    }

    public XPath getCount() {
        return this.m_countMatchPattern;
    }

    public void setFrom(XPath v) {
        this.m_fromMatchPattern = v;
    }

    public XPath getFrom() {
        return this.m_fromMatchPattern;
    }

    public void setLevel(int v) {
        this.m_level = v;
    }

    public int getLevel() {
        return this.m_level;
    }

    public void setValue(XPath v) {
        this.m_valueExpr = v;
    }

    public XPath getValue() {
        return this.m_valueExpr;
    }

    public void setFormat(AVT v) {
        this.m_format_avt = v;
    }

    public AVT getFormat() {
        return this.m_format_avt;
    }

    public void setLang(AVT v) {
        this.m_lang_avt = v;
    }

    public AVT getLang() {
        return this.m_lang_avt;
    }

    public void setLetterValue(AVT v) {
        this.m_lettervalue_avt = v;
    }

    public AVT getLetterValue() {
        return this.m_lettervalue_avt;
    }

    public void setGroupingSeparator(AVT v) {
        this.m_groupingSeparator_avt = v;
    }

    public AVT getGroupingSeparator() {
        return this.m_groupingSeparator_avt;
    }

    public void setGroupingSize(AVT v) {
        this.m_groupingSize_avt = v;
    }

    public AVT getGroupingSize() {
        return this.m_groupingSize_avt;
    }

    static {
        DecimalToRoman[] decimalToRomanArr = new DecimalToRoman[7];
        decimalToRomanArr[0] = new DecimalToRoman(1000, "M", 900, "CM");
        decimalToRomanArr[1] = new DecimalToRoman(500, "D", 400, "CD");
        decimalToRomanArr[2] = new DecimalToRoman(100, "C", 90, "XC");
        decimalToRomanArr[3] = new DecimalToRoman(50, "L", 40, "XL");
        decimalToRomanArr[4] = new DecimalToRoman(10, "X", 9, "IX");
        decimalToRomanArr[5] = new DecimalToRoman(5, "V", 4, "IV");
        decimalToRomanArr[6] = new DecimalToRoman(1, "I", 1, "I");
        m_romanConvertTable = decimalToRomanArr;
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        super.compose(sroot);
        ComposeState cstate = sroot.getComposeState();
        Vector vnames = cstate.getVariableNames();
        if (this.m_countMatchPattern != null) {
            this.m_countMatchPattern.fixupVariables(vnames, cstate.getGlobalsSize());
        }
        if (this.m_format_avt != null) {
            this.m_format_avt.fixupVariables(vnames, cstate.getGlobalsSize());
        }
        if (this.m_fromMatchPattern != null) {
            this.m_fromMatchPattern.fixupVariables(vnames, cstate.getGlobalsSize());
        }
        if (this.m_groupingSeparator_avt != null) {
            this.m_groupingSeparator_avt.fixupVariables(vnames, cstate.getGlobalsSize());
        }
        if (this.m_groupingSize_avt != null) {
            this.m_groupingSize_avt.fixupVariables(vnames, cstate.getGlobalsSize());
        }
        if (this.m_lang_avt != null) {
            this.m_lang_avt.fixupVariables(vnames, cstate.getGlobalsSize());
        }
        if (this.m_lettervalue_avt != null) {
            this.m_lettervalue_avt.fixupVariables(vnames, cstate.getGlobalsSize());
        }
        if (this.m_valueExpr != null) {
            this.m_valueExpr.fixupVariables(vnames, cstate.getGlobalsSize());
        }
    }

    public int getXSLToken() {
        return 35;
    }

    public String getNodeName() {
        return "number";
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        String countString = getCountString(transformer, transformer.getXPathContext().getCurrentNode());
        try {
            transformer.getResultTreeHandler().characters(countString.toCharArray(), 0, countString.length());
        } catch (SAXException se) {
            throw new TransformerException(se);
        }
    }

    public ElemTemplateElement appendChild(ElemTemplateElement newChild) {
        error(XSLTErrorResources.ER_CANNOT_ADD, new Object[]{newChild.getNodeName(), getNodeName()});
        return null;
    }

    int findAncestor(XPathContext xctxt, XPath fromMatchPattern, XPath countMatchPattern, int context, ElemNumber namespaceContext) throws TransformerException {
        DTM dtm = xctxt.getDTM(context);
        while (-1 != context && ((fromMatchPattern == null || fromMatchPattern.getMatchScore(xctxt, context) == Double.NEGATIVE_INFINITY) && (countMatchPattern == null || countMatchPattern.getMatchScore(xctxt, context) == Double.NEGATIVE_INFINITY))) {
            context = dtm.getParent(context);
        }
        return context;
    }

    private int findPrecedingOrAncestorOrSelf(XPathContext xctxt, XPath fromMatchPattern, XPath countMatchPattern, int context, ElemNumber namespaceContext) throws TransformerException {
        DTM dtm = xctxt.getDTM(context);
        while (-1 != context) {
            if (fromMatchPattern != null && fromMatchPattern.getMatchScore(xctxt, context) != Double.NEGATIVE_INFINITY) {
                return -1;
            }
            if (countMatchPattern != null && countMatchPattern.getMatchScore(xctxt, context) != Double.NEGATIVE_INFINITY) {
                return context;
            }
            int prevSibling = dtm.getPreviousSibling(context);
            if (-1 == prevSibling) {
                context = dtm.getParent(context);
            } else {
                context = dtm.getLastChild(prevSibling);
                if (context == -1) {
                    context = prevSibling;
                }
            }
        }
        return context;
    }

    XPath getCountMatchPattern(XPathContext support, int contextNode) throws TransformerException {
        XPath countMatchPattern = this.m_countMatchPattern;
        DTM dtm = support.getDTM(contextNode);
        if (countMatchPattern != null) {
            return countMatchPattern;
        }
        switch (dtm.getNodeType(contextNode)) {
            case (short) 1:
                MyPrefixResolver resolver;
                if (dtm.getNamespaceURI(contextNode) == null) {
                    resolver = new MyPrefixResolver(dtm.getNode(contextNode), dtm, contextNode, false);
                } else {
                    resolver = new MyPrefixResolver(dtm.getNode(contextNode), dtm, contextNode, true);
                }
                return new XPath(dtm.getNodeName(contextNode), this, resolver, 1, support.getErrorListener());
            case (short) 2:
                return new XPath("@" + dtm.getNodeName(contextNode), this, this, 1, support.getErrorListener());
            case (short) 3:
            case (short) 4:
                return new XPath("text()", this, this, 1, support.getErrorListener());
            case (short) 7:
                return new XPath("pi(" + dtm.getNodeName(contextNode) + ")", this, this, 1, support.getErrorListener());
            case (short) 8:
                return new XPath("comment()", this, this, 1, support.getErrorListener());
            case (short) 9:
                return new XPath(PsuedoNames.PSEUDONAME_ROOT, this, this, 1, support.getErrorListener());
            default:
                return null;
        }
    }

    String getCountString(TransformerImpl transformer, int sourceNode) throws TransformerException {
        long[] list = null;
        XPathContext xctxt = transformer.getXPathContext();
        CountersTable ctable = transformer.getCountersTable();
        if (this.m_valueExpr != null) {
            double d_count = Math.floor(this.m_valueExpr.execute(xctxt, sourceNode, (PrefixResolver) this).num() + 0.5d);
            if (Double.isNaN(d_count)) {
                return "NaN";
            }
            if (d_count < XPath.MATCH_SCORE_QNAME && Double.isInfinite(d_count)) {
                return "-Infinity";
            }
            if (Double.isInfinite(d_count)) {
                return Constants.ATTRVAL_INFINITY;
            }
            if (d_count == XPath.MATCH_SCORE_QNAME) {
                return "0";
            }
            list = new long[]{(long) d_count};
        } else {
            if (3 == this.m_level) {
                list = new long[]{(long) ctable.countNode(xctxt, this, sourceNode)};
            } else {
                NodeVector ancestors = getMatchingAncestors(xctxt, sourceNode, 1 == this.m_level);
                int lastIndex = ancestors.size() - 1;
                if (lastIndex >= 0) {
                    list = new long[(lastIndex + 1)];
                    for (int i = lastIndex; i >= 0; i--) {
                        list[lastIndex - i] = (long) ctable.countNode(xctxt, this, ancestors.elementAt(i));
                    }
                }
            }
        }
        return list != null ? formatNumberList(transformer, list, sourceNode) : "";
    }

    public int getPreviousNode(XPathContext xctxt, int pos) throws TransformerException {
        XPath countMatchPattern = getCountMatchPattern(xctxt, pos);
        DTM dtm = xctxt.getDTM(pos);
        if (3 == this.m_level) {
            XPath fromMatchPattern = this.m_fromMatchPattern;
            while (-1 != pos) {
                int next = dtm.getPreviousSibling(pos);
                if (-1 == next) {
                    next = dtm.getParent(pos);
                    if (-1 != next && (!(fromMatchPattern == null || fromMatchPattern.getMatchScore(xctxt, next) == Double.NEGATIVE_INFINITY) || dtm.getNodeType(next) == (short) 9)) {
                        return -1;
                    }
                }
                int child = next;
                while (-1 != child) {
                    child = dtm.getLastChild(next);
                    if (-1 != child) {
                        next = child;
                    }
                }
                pos = next;
                if (-1 != next && (countMatchPattern == null || countMatchPattern.getMatchScore(xctxt, pos) != Double.NEGATIVE_INFINITY)) {
                    return pos;
                }
            }
            return pos;
        }
        while (-1 != pos) {
            pos = dtm.getPreviousSibling(pos);
            if (-1 != pos && (countMatchPattern == null || countMatchPattern.getMatchScore(xctxt, pos) != Double.NEGATIVE_INFINITY)) {
                return pos;
            }
        }
        return pos;
    }

    public int getTargetNode(XPathContext xctxt, int sourceNode) throws TransformerException {
        XPath countMatchPattern = getCountMatchPattern(xctxt, sourceNode);
        if (3 == this.m_level) {
            return findPrecedingOrAncestorOrSelf(xctxt, this.m_fromMatchPattern, countMatchPattern, sourceNode, this);
        }
        return findAncestor(xctxt, this.m_fromMatchPattern, countMatchPattern, sourceNode, this);
    }

    NodeVector getMatchingAncestors(XPathContext xctxt, int node, boolean stopAtFirstFound) throws TransformerException {
        NodeSetDTM ancestors = new NodeSetDTM(xctxt.getDTMManager());
        XPath countMatchPattern = getCountMatchPattern(xctxt, node);
        DTM dtm = xctxt.getDTM(node);
        while (-1 != node && (this.m_fromMatchPattern == null || this.m_fromMatchPattern.getMatchScore(xctxt, node) == Double.NEGATIVE_INFINITY || stopAtFirstFound)) {
            if (countMatchPattern == null) {
                System.out.println("Programmers error! countMatchPattern should never be null!");
            }
            if (countMatchPattern.getMatchScore(xctxt, node) != Double.NEGATIVE_INFINITY) {
                ancestors.addElement(node);
                if (stopAtFirstFound) {
                    break;
                }
            }
            node = dtm.getParent(node);
        }
        return ancestors;
    }

    Locale getLocale(TransformerImpl transformer, int contextNode) throws TransformerException {
        if (this.m_lang_avt == null) {
            return Locale.getDefault();
        }
        XPathContext xctxt = transformer.getXPathContext();
        String langValue = this.m_lang_avt.evaluate(xctxt, contextNode, this);
        if (langValue == null) {
            return null;
        }
        Locale locale = new Locale(langValue.toUpperCase(), "");
        if (locale != null) {
            return locale;
        }
        transformer.getMsgMgr().warn(this, null, xctxt.getDTM(contextNode).getNode(contextNode), XSLTErrorResources.WG_LOCALE_NOT_FOUND, new Object[]{langValue});
        return Locale.getDefault();
    }

    private DecimalFormat getNumberFormatter(TransformerImpl transformer, int contextNode) throws TransformerException {
        String digitGroupSepValue;
        String nDigitsPerGroupValue;
        Locale locale = (Locale) getLocale(transformer, contextNode).clone();
        DecimalFormat formatter = null;
        if (this.m_groupingSeparator_avt != null) {
            digitGroupSepValue = this.m_groupingSeparator_avt.evaluate(transformer.getXPathContext(), contextNode, this);
        } else {
            digitGroupSepValue = null;
        }
        if (!(digitGroupSepValue == null || (this.m_groupingSeparator_avt.isSimple() ^ 1) == 0 || digitGroupSepValue.length() == 1)) {
            transformer.getMsgMgr().warn(this, XSLTErrorResources.WG_ILLEGAL_ATTRIBUTE_VALUE, new Object[]{"name", this.m_groupingSeparator_avt.getName()});
        }
        if (this.m_groupingSize_avt != null) {
            nDigitsPerGroupValue = this.m_groupingSize_avt.evaluate(transformer.getXPathContext(), contextNode, this);
        } else {
            nDigitsPerGroupValue = null;
        }
        if (digitGroupSepValue == null || nDigitsPerGroupValue == null || digitGroupSepValue.length() <= 0) {
            return null;
        }
        try {
            formatter = (DecimalFormat) NumberFormat.getNumberInstance(locale);
            formatter.setGroupingSize(Integer.valueOf(nDigitsPerGroupValue).intValue());
            DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
            symbols.setGroupingSeparator(digitGroupSepValue.charAt(0));
            formatter.setDecimalFormatSymbols(symbols);
            formatter.setGroupingUsed(true);
            return formatter;
        } catch (NumberFormatException e) {
            formatter.setGroupingUsed(false);
            return formatter;
        }
    }

    String formatNumberList(TransformerImpl transformer, long[] list, int contextNode) throws TransformerException {
        FastStringBuffer formattedNumber = StringBufferPool.get();
        try {
            String formatValue;
            int numberWidth = 1;
            char numberType = '1';
            String lastSepString = null;
            String formatTokenString = null;
            String lastSep = Constants.ATTRVAL_THIS;
            boolean isFirstToken = true;
            if (this.m_format_avt != null) {
                formatValue = this.m_format_avt.evaluate(transformer.getXPathContext(), contextNode, this);
            } else {
                formatValue = null;
            }
            if (formatValue == null) {
                formatValue = "1";
            }
            NumberFormatStringTokenizer formatTokenizer = new NumberFormatStringTokenizer(formatValue);
            for (long formattedNumber2 : list) {
                if (formatTokenizer.hasMoreTokens()) {
                    String formatToken = formatTokenizer.nextToken();
                    if (Character.isLetterOrDigit(formatToken.charAt(formatToken.length() - 1))) {
                        numberWidth = formatToken.length();
                        numberType = formatToken.charAt(numberWidth - 1);
                    } else if (formatTokenizer.isLetterOrDigitAhead()) {
                        formatTokenString = formatToken;
                        while (formatTokenizer.nextIsSep()) {
                            formatTokenString = formatTokenString + formatTokenizer.nextToken();
                        }
                        if (!isFirstToken) {
                            lastSep = formatTokenString;
                        }
                        formatToken = formatTokenizer.nextToken();
                        numberWidth = formatToken.length();
                        numberType = formatToken.charAt(numberWidth - 1);
                    } else {
                        lastSepString = formatToken;
                        while (formatTokenizer.hasMoreTokens()) {
                            lastSepString = lastSepString + formatTokenizer.nextToken();
                        }
                    }
                }
                if (formatTokenString != null && isFirstToken) {
                    formattedNumber.append(formatTokenString);
                } else if (!(lastSep == null || (isFirstToken ^ 1) == 0)) {
                    formattedNumber.append(lastSep);
                }
                getFormattedNumber(transformer, contextNode, numberType, numberWidth, formattedNumber2, formattedNumber);
                isFirstToken = false;
            }
            while (formatTokenizer.isLetterOrDigitAhead()) {
                formatTokenizer.nextToken();
            }
            if (lastSepString != null) {
                formattedNumber.append(lastSepString);
            }
            while (formatTokenizer.hasMoreTokens()) {
                formattedNumber.append(formatTokenizer.nextToken());
            }
            String numStr = formattedNumber.toString();
            return numStr;
        } finally {
            StringBufferPool.free(formattedNumber);
        }
    }

    private void getFormattedNumber(TransformerImpl transformer, int contextNode, char numberType, int numberWidth, long listElement, FastStringBuffer formattedNumber) throws TransformerException {
        String letterVal;
        if (this.m_lettervalue_avt != null) {
            letterVal = this.m_lettervalue_avt.evaluate(transformer.getXPathContext(), contextNode, this);
        } else {
            letterVal = null;
        }
        XResourceBundle thisBundle;
        switch (numberType) {
            case 'A':
                if (this.m_alphaCountTable == null) {
                    this.m_alphaCountTable = (CharArrayWrapper) XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, getLocale(transformer, contextNode)).getObject(XResourceBundle.LANG_ALPHABET);
                }
                int2alphaCount(listElement, this.m_alphaCountTable, formattedNumber);
                return;
            case Constants.ELEMNAME_VARIABLE /*73*/:
                formattedNumber.append(long2roman(listElement, true));
                return;
            case 'a':
                if (this.m_alphaCountTable == null) {
                    this.m_alphaCountTable = (CharArrayWrapper) XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, getLocale(transformer, contextNode)).getObject(XResourceBundle.LANG_ALPHABET);
                }
                FastStringBuffer stringBuf = StringBufferPool.get();
                try {
                    int2alphaCount(listElement, this.m_alphaCountTable, stringBuf);
                    formattedNumber.append(stringBuf.toString().toLowerCase(getLocale(transformer, contextNode)));
                    return;
                } finally {
                    StringBufferPool.free(stringBuf);
                }
            case 'i':
                formattedNumber.append(long2roman(listElement, true).toLowerCase(getLocale(transformer, contextNode)));
                return;
            case 945:
                thisBundle = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("el", ""));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    int2alphaCount(listElement, (CharArrayWrapper) thisBundle.getObject(XResourceBundle.LANG_ALPHABET), formattedNumber);
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle));
                    return;
                }
            case 1072:
                thisBundle = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("cy", ""));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    int2alphaCount(listElement, (CharArrayWrapper) thisBundle.getObject(XResourceBundle.LANG_ALPHABET), formattedNumber);
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle));
                    return;
                }
            case 1488:
                thisBundle = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("he", ""));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    int2alphaCount(listElement, (CharArrayWrapper) thisBundle.getObject(XResourceBundle.LANG_ALPHABET), formattedNumber);
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle));
                    return;
                }
            case 3665:
                thisBundle = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("th", ""));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    int2alphaCount(listElement, (CharArrayWrapper) thisBundle.getObject(XResourceBundle.LANG_ALPHABET), formattedNumber);
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle));
                    return;
                }
            case 4304:
                thisBundle = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("ka", ""));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    int2alphaCount(listElement, (CharArrayWrapper) thisBundle.getObject(XResourceBundle.LANG_ALPHABET), formattedNumber);
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle));
                    return;
                }
            case 12354:
                thisBundle = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("ja", "JP", "HA"));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    formattedNumber.append(int2singlealphaCount(listElement, (CharArrayWrapper) thisBundle.getObject(XResourceBundle.LANG_ALPHABET)));
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle));
                    return;
                }
            case 12356:
                thisBundle = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("ja", "JP", "HI"));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    formattedNumber.append(int2singlealphaCount(listElement, (CharArrayWrapper) thisBundle.getObject(XResourceBundle.LANG_ALPHABET)));
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle));
                    return;
                }
            case 12450:
                thisBundle = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("ja", "JP", "A"));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    formattedNumber.append(int2singlealphaCount(listElement, (CharArrayWrapper) thisBundle.getObject(XResourceBundle.LANG_ALPHABET)));
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle));
                    return;
                }
            case 12452:
                thisBundle = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("ja", "JP", "I"));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    formattedNumber.append(int2singlealphaCount(listElement, (CharArrayWrapper) thisBundle.getObject(XResourceBundle.LANG_ALPHABET)));
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle));
                    return;
                }
            case 19968:
                thisBundle = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("zh", "CN"));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    int2alphaCount(listElement, (CharArrayWrapper) thisBundle.getObject(XResourceBundle.LANG_ALPHABET), formattedNumber);
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle));
                    return;
                }
            case 22777:
                thisBundle = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("zh", "TW"));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    int2alphaCount(listElement, (CharArrayWrapper) thisBundle.getObject(XResourceBundle.LANG_ALPHABET), formattedNumber);
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle));
                    return;
                }
            default:
                DecimalFormat formatter = getNumberFormatter(transformer, contextNode);
                String padString = formatter == null ? String.valueOf(0) : formatter.format(0);
                String numString = formatter == null ? String.valueOf(listElement) : formatter.format(listElement);
                int nPadding = numberWidth - numString.length();
                for (int k = 0; k < nPadding; k++) {
                    formattedNumber.append(padString);
                }
                formattedNumber.append(numString);
                return;
        }
    }

    String getZeroString() {
        return "0";
    }

    protected String int2singlealphaCount(long val, CharArrayWrapper table) {
        if (val > ((long) table.getLength())) {
            return getZeroString();
        }
        return new Character(table.getChar(((int) val) - 1)).toString();
    }

    protected void int2alphaCount(long val, CharArrayWrapper aTable, FastStringBuffer stringBuf) {
        int charPos;
        int radix = aTable.getLength();
        char[] table = new char[radix];
        int i = 0;
        while (i < radix - 1) {
            table[i + 1] = aTable.getChar(i);
            i++;
        }
        table[0] = aTable.getChar(i);
        char[] buf = new char[100];
        int lookupIndex = 1;
        long correction = 0;
        int charPos2 = buf.length - 1;
        while (true) {
            int i2 = (lookupIndex == 0 || (correction != 0 && lookupIndex == radix - 1)) ? radix - 1 : 0;
            correction = (long) i2;
            lookupIndex = ((int) (val + correction)) % radix;
            val /= (long) radix;
            if (lookupIndex != 0 || val != 0) {
                charPos = charPos2 - 1;
                buf[charPos2] = table[lookupIndex];
                if (val <= 0) {
                    break;
                }
                charPos2 = charPos;
            } else {
                charPos = charPos2;
                break;
            }
        }
        stringBuf.append(buf, charPos + 1, (buf.length - charPos) - 1);
    }

    protected String tradAlphaCount(long val, XResourceBundle thisBundle) {
        if (val > Long.MAX_VALUE) {
            error(XSLTErrorResources.ER_NUMBER_TOO_BIG);
            return "#error";
        }
        int charPos;
        char[] table;
        int j;
        int lookupIndex;
        char[] buf = new char[100];
        int charPos2 = 0;
        IntArrayWrapper groups = (IntArrayWrapper) thisBundle.getObject(XResourceBundle.LANG_NUMBERGROUPS);
        StringArrayWrapper tables = (StringArrayWrapper) thisBundle.getObject(XResourceBundle.LANG_NUM_TABLES);
        if (thisBundle.getString(XResourceBundle.LANG_NUMBERING).equals(XResourceBundle.LANG_MULT_ADD)) {
            String mult_order = thisBundle.getString(XResourceBundle.MULT_ORDER);
            LongArrayWrapper multiplier = (LongArrayWrapper) thisBundle.getObject(XResourceBundle.LANG_MULTIPLIER);
            CharArrayWrapper zeroChar = (CharArrayWrapper) thisBundle.getObject("zero");
            int i = 0;
            while (i < multiplier.getLength() && val < multiplier.getLong(i)) {
                i++;
            }
            while (i < multiplier.getLength()) {
                if (val < multiplier.getLong(i)) {
                    if (zeroChar.getLength() == 0) {
                        i++;
                    } else {
                        if (buf[charPos2 - 1] != zeroChar.getChar(0)) {
                            charPos = charPos2 + 1;
                            buf[charPos2] = zeroChar.getChar(0);
                            charPos2 = charPos;
                        }
                        i++;
                    }
                } else if (val >= multiplier.getLong(i)) {
                    long mult = val / multiplier.getLong(i);
                    val %= multiplier.getLong(i);
                    int k = 0;
                    while (k < groups.getLength()) {
                        if (mult / ((long) groups.getInt(k)) <= 0) {
                            k++;
                        } else {
                            CharArrayWrapper THEletters = (CharArrayWrapper) thisBundle.getObject(tables.getString(k));
                            table = new char[(THEletters.getLength() + 1)];
                            j = 0;
                            while (j < THEletters.getLength()) {
                                table[j + 1] = THEletters.getChar(j);
                                j++;
                            }
                            table[0] = THEletters.getChar(j - 1);
                            lookupIndex = ((int) mult) / groups.getInt(k);
                            if (!(lookupIndex == 0 && mult == 0)) {
                                char multiplierChar = ((CharArrayWrapper) thisBundle.getObject(XResourceBundle.LANG_MULTIPLIER_CHAR)).getChar(i);
                                if (lookupIndex >= table.length) {
                                    return "#error";
                                }
                                if (mult_order.equals(XResourceBundle.MULT_PRECEDES)) {
                                    charPos = charPos2 + 1;
                                    buf[charPos2] = multiplierChar;
                                    charPos2 = charPos + 1;
                                    buf[charPos] = table[lookupIndex];
                                } else {
                                    if (!(lookupIndex == 1 && i == multiplier.getLength() - 1)) {
                                        charPos = charPos2 + 1;
                                        buf[charPos2] = table[lookupIndex];
                                        charPos2 = charPos;
                                    }
                                    charPos = charPos2 + 1;
                                    buf[charPos2] = multiplierChar;
                                    charPos2 = charPos;
                                }
                            }
                            i++;
                        }
                    }
                    i++;
                }
                if (i >= multiplier.getLength()) {
                    break;
                }
            }
        }
        int count = 0;
        while (count < groups.getLength()) {
            if (val / ((long) groups.getInt(count)) > 0) {
                CharArrayWrapper theletters = (CharArrayWrapper) thisBundle.getObject(tables.getString(count));
                table = new char[(theletters.getLength() + 1)];
                j = 0;
                while (j < theletters.getLength()) {
                    table[j + 1] = theletters.getChar(j);
                    j++;
                }
                table[0] = theletters.getChar(j - 1);
                lookupIndex = ((int) val) / groups.getInt(count);
                val %= (long) groups.getInt(count);
                if (lookupIndex == 0 && val == 0) {
                    break;
                } else if (lookupIndex >= table.length) {
                    return "#error";
                } else {
                    charPos = charPos2 + 1;
                    buf[charPos2] = table[lookupIndex];
                    count++;
                    charPos2 = charPos;
                }
            } else {
                count++;
            }
        }
        return new String(buf, 0, charPos2);
    }

    protected String long2roman(long val, boolean prefixesAreOK) {
        if (val <= 0) {
            return getZeroString();
        }
        String roman = "";
        int place = 0;
        if (val <= 3999) {
            while (true) {
                if (val >= m_romanConvertTable[place].m_postValue) {
                    roman = roman + m_romanConvertTable[place].m_postLetter;
                    val -= m_romanConvertTable[place].m_postValue;
                } else {
                    if (prefixesAreOK && val >= m_romanConvertTable[place].m_preValue) {
                        roman = roman + m_romanConvertTable[place].m_preLetter;
                        val -= m_romanConvertTable[place].m_preValue;
                    }
                    place++;
                    if (val <= 0) {
                        break;
                    }
                }
            }
        } else {
            roman = "#error";
        }
        return roman;
    }

    public void callChildVisitors(XSLTVisitor visitor, boolean callAttrs) {
        if (callAttrs) {
            if (this.m_countMatchPattern != null) {
                this.m_countMatchPattern.getExpression().callVisitors(this.m_countMatchPattern, visitor);
            }
            if (this.m_fromMatchPattern != null) {
                this.m_fromMatchPattern.getExpression().callVisitors(this.m_fromMatchPattern, visitor);
            }
            if (this.m_valueExpr != null) {
                this.m_valueExpr.getExpression().callVisitors(this.m_valueExpr, visitor);
            }
            if (this.m_format_avt != null) {
                this.m_format_avt.callVisitors(visitor);
            }
            if (this.m_groupingSeparator_avt != null) {
                this.m_groupingSeparator_avt.callVisitors(visitor);
            }
            if (this.m_groupingSize_avt != null) {
                this.m_groupingSize_avt.callVisitors(visitor);
            }
            if (this.m_lang_avt != null) {
                this.m_lang_avt.callVisitors(visitor);
            }
            if (this.m_lettervalue_avt != null) {
                this.m_lettervalue_avt.callVisitors(visitor);
            }
        }
        super.callChildVisitors(visitor, callAttrs);
    }
}
