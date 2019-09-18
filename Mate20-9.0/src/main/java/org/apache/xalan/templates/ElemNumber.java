package org.apache.xalan.templates;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.StylesheetRoot;
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

        public MyPrefixResolver(Node xpathExpressionContext, DTM dtm2, int handle2, boolean handleNullPrefix2) {
            this.dtm = dtm2;
            this.handle = handle2;
            this.handleNullPrefix = handleNullPrefix2;
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

        public NumberFormatStringTokenizer(String str2) {
            this.str = str2;
            this.maxPosition = str2.length();
        }

        public void reset() {
            this.currentPosition = 0;
        }

        public String nextToken() {
            if (this.currentPosition < this.maxPosition) {
                int start = this.currentPosition;
                while (this.currentPosition < this.maxPosition && Character.isLetterOrDigit(this.str.charAt(this.currentPosition))) {
                    this.currentPosition++;
                }
                if (start == this.currentPosition && !Character.isLetterOrDigit(this.str.charAt(this.currentPosition))) {
                    this.currentPosition++;
                }
                return this.str.substring(start, this.currentPosition);
            }
            throw new NoSuchElementException();
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
            int currpos;
            int count = 0;
            for (int currpos2 = this.currentPosition; currpos2 < this.maxPosition; currpos2 = currpos) {
                currpos = currpos2;
                while (currpos < this.maxPosition && Character.isLetterOrDigit(this.str.charAt(currpos))) {
                    currpos++;
                }
                if (currpos2 == currpos && !Character.isLetterOrDigit(this.str.charAt(currpos))) {
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
        DecimalToRoman decimalToRoman = new DecimalToRoman(1000, "M", 900, "CM");
        DecimalToRoman decimalToRoman2 = new DecimalToRoman(500, "D", 400, "CD");
        DecimalToRoman decimalToRoman3 = new DecimalToRoman(100, "C", 90, "XC");
        DecimalToRoman decimalToRoman4 = new DecimalToRoman(50, "L", 40, "XL");
        DecimalToRoman decimalToRoman5 = new DecimalToRoman(10, "X", 9, "IX");
        DecimalToRoman decimalToRoman6 = new DecimalToRoman(5, "V", 4, "IV");
        DecimalToRoman decimalToRoman7 = new DecimalToRoman(1, "I", 1, "I");
        m_romanConvertTable = new DecimalToRoman[]{decimalToRoman, decimalToRoman2, decimalToRoman3, decimalToRoman4, decimalToRoman5, decimalToRoman6, decimalToRoman7};
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        super.compose(sroot);
        StylesheetRoot.ComposeState cstate = sroot.getComposeState();
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

    /* access modifiers changed from: package-private */
    public int findAncestor(XPathContext xctxt, XPath fromMatchPattern, XPath countMatchPattern, int context, ElemNumber namespaceContext) throws TransformerException {
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

    /* access modifiers changed from: package-private */
    public XPath getCountMatchPattern(XPathContext support, int contextNode) throws TransformerException {
        MyPrefixResolver resolver;
        XPath countMatchPattern = this.m_countMatchPattern;
        DTM dtm = support.getDTM(contextNode);
        if (countMatchPattern != null) {
            return countMatchPattern;
        }
        switch (dtm.getNodeType(contextNode)) {
            case 1:
                if (dtm.getNamespaceURI(contextNode) == null) {
                    MyPrefixResolver resolver2 = new MyPrefixResolver(dtm.getNode(contextNode), dtm, contextNode, false);
                    resolver = resolver2;
                } else {
                    MyPrefixResolver myPrefixResolver = new MyPrefixResolver(dtm.getNode(contextNode), dtm, contextNode, true);
                    resolver = myPrefixResolver;
                }
                XPath countMatchPattern2 = new XPath(dtm.getNodeName(contextNode), this, resolver, 1, support.getErrorListener());
                return countMatchPattern2;
            case 2:
                XPath countMatchPattern3 = new XPath("@" + dtm.getNodeName(contextNode), this, this, 1, support.getErrorListener());
                return countMatchPattern3;
            case 3:
            case 4:
                XPath countMatchPattern4 = new XPath("text()", this, this, 1, support.getErrorListener());
                return countMatchPattern4;
            case 7:
                XPath countMatchPattern5 = new XPath("pi(" + dtm.getNodeName(contextNode) + ")", this, this, 1, support.getErrorListener());
                return countMatchPattern5;
            case 8:
                XPath countMatchPattern6 = new XPath("comment()", this, this, 1, support.getErrorListener());
                return countMatchPattern6;
            case 9:
                XPath countMatchPattern7 = new XPath(PsuedoNames.PSEUDONAME_ROOT, this, this, 1, support.getErrorListener());
                return countMatchPattern7;
            default:
                return null;
        }
    }

    /* access modifiers changed from: package-private */
    public String getCountString(TransformerImpl transformer, int sourceNode) throws TransformerException {
        long[] list = null;
        XPathContext xctxt = transformer.getXPathContext();
        CountersTable ctable = transformer.getCountersTable();
        boolean z = false;
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
        } else if (3 == this.m_level) {
            list = new long[]{(long) ctable.countNode(xctxt, this, sourceNode)};
        } else {
            if (1 == this.m_level) {
                z = true;
            }
            NodeVector ancestors = getMatchingAncestors(xctxt, sourceNode, z);
            int lastIndex = ancestors.size() - 1;
            if (lastIndex >= 0) {
                list = new long[(lastIndex + 1)];
                for (int i = lastIndex; i >= 0; i--) {
                    list[lastIndex - i] = (long) ctable.countNode(xctxt, this, ancestors.elementAt(i));
                }
            }
        }
        return list != null ? formatNumberList(transformer, list, sourceNode) : "";
    }

    public int getPreviousNode(XPathContext xctxt, int pos) throws TransformerException {
        int next;
        XPath countMatchPattern = getCountMatchPattern(xctxt, pos);
        DTM dtm = xctxt.getDTM(pos);
        if (3 == this.m_level) {
            XPath fromMatchPattern = this.m_fromMatchPattern;
            while (-1 != pos) {
                int child = dtm.getPreviousSibling(pos);
                if (-1 == child) {
                    next = dtm.getParent(pos);
                    if (-1 != next && (!(fromMatchPattern == null || fromMatchPattern.getMatchScore(xctxt, next) == Double.NEGATIVE_INFINITY) || dtm.getNodeType(next) == 9)) {
                        return -1;
                    }
                } else {
                    int next2 = child;
                    while (-1 != child) {
                        child = dtm.getLastChild(next2);
                        if (-1 != child) {
                            next2 = child;
                        }
                    }
                    next = next2;
                }
                pos = next;
                if (-1 != pos && (countMatchPattern == null || countMatchPattern.getMatchScore(xctxt, pos) != Double.NEGATIVE_INFINITY)) {
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

    /* access modifiers changed from: package-private */
    public NodeVector getMatchingAncestors(XPathContext xctxt, int node, boolean stopAtFirstFound) throws TransformerException {
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

    /* access modifiers changed from: package-private */
    public Locale getLocale(TransformerImpl transformer, int contextNode) throws TransformerException {
        if (this.m_lang_avt == null) {
            return Locale.getDefault();
        }
        String langValue = this.m_lang_avt.evaluate(transformer.getXPathContext(), contextNode, this);
        if (langValue != null) {
            return new Locale(langValue.toUpperCase(), "");
        }
        return null;
    }

    private DecimalFormat getNumberFormatter(TransformerImpl transformer, int contextNode) throws TransformerException {
        String digitGroupSepValue;
        Locale locale = (Locale) getLocale(transformer, contextNode).clone();
        DecimalFormat formatter = null;
        String nDigitsPerGroupValue = null;
        if (this.m_groupingSeparator_avt != null) {
            digitGroupSepValue = this.m_groupingSeparator_avt.evaluate(transformer.getXPathContext(), contextNode, this);
        } else {
            digitGroupSepValue = null;
        }
        if (!(digitGroupSepValue == null || this.m_groupingSeparator_avt.isSimple() || digitGroupSepValue.length() == 1)) {
            transformer.getMsgMgr().warn(this, XSLTErrorResources.WG_ILLEGAL_ATTRIBUTE_VALUE, new Object[]{"name", this.m_groupingSeparator_avt.getName()});
        }
        if (this.m_groupingSize_avt != null) {
            nDigitsPerGroupValue = this.m_groupingSize_avt.evaluate(transformer.getXPathContext(), contextNode, this);
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

    /* access modifiers changed from: package-private */
    public String formatNumberList(TransformerImpl transformer, long[] list, int contextNode) throws TransformerException {
        int i;
        String formatValue;
        String formatTokenString;
        String lastSepString;
        long[] jArr = list;
        FastStringBuffer formattedNumber = StringBufferPool.get();
        try {
            int nNumbers = jArr.length;
            int numberWidth = 1;
            char numberType = '1';
            String lastSepString2 = null;
            String formatTokenString2 = null;
            String lastSep = Constants.ATTRVAL_THIS;
            if (this.m_format_avt != null) {
                i = contextNode;
                try {
                    formatValue = this.m_format_avt.evaluate(transformer.getXPathContext(), i, this);
                } catch (Throwable th) {
                    th = th;
                    StringBufferPool.free(formattedNumber);
                    throw th;
                }
            } else {
                i = contextNode;
                formatValue = null;
            }
            if (formatValue == null) {
                formatValue = "1";
            }
            NumberFormatStringTokenizer formatTokenizer = new NumberFormatStringTokenizer(formatValue);
            int i2 = 0;
            boolean isFirstToken = true;
            while (true) {
                int i3 = i2;
                if (i3 >= nNumbers) {
                    break;
                }
                if (formatTokenizer.hasMoreTokens()) {
                    String formatToken = formatTokenizer.nextToken();
                    if (Character.isLetterOrDigit(formatToken.charAt(formatToken.length() - 1))) {
                        numberWidth = formatToken.length();
                        numberType = formatToken.charAt(numberWidth - 1);
                    } else if (formatTokenizer.isLetterOrDigitAhead()) {
                        formatTokenString = formatToken;
                        while (formatTokenizer.nextIsSep()) {
                            String formatToken2 = formatTokenizer.nextToken();
                            formatTokenString = formatTokenString + formatToken2;
                        }
                        if (!isFirstToken) {
                            lastSep = formatTokenString;
                        }
                        String formatToken3 = formatTokenizer.nextToken();
                        numberWidth = formatToken3.length();
                        numberType = formatToken3.charAt(numberWidth - 1);
                    } else {
                        lastSepString = formatToken;
                        while (formatTokenizer.hasMoreTokens()) {
                            String formatToken4 = formatTokenizer.nextToken();
                            lastSepString = lastSepString + formatToken4;
                        }
                    }
                }
                int numberWidth2 = numberWidth;
                char numberType2 = numberType;
                String lastSepString3 = lastSepString;
                String formatTokenString3 = formatTokenString;
                String lastSep2 = lastSep;
                if (formatTokenString3 != null && isFirstToken) {
                    formattedNumber.append(formatTokenString3);
                } else if (lastSep2 != null && !isFirstToken) {
                    formattedNumber.append(lastSep2);
                }
                String lastSep3 = lastSep2;
                getFormattedNumber(transformer, i, numberType2, numberWidth2, jArr[i3], formattedNumber);
                isFirstToken = false;
                i2 = i3 + 1;
                lastSepString2 = lastSepString3;
                numberWidth = numberWidth2;
                numberType = numberType2;
                formatTokenString2 = formatTokenString3;
                lastSep = lastSep3;
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
            StringBufferPool.free(formattedNumber);
            return numStr;
        } catch (Throwable th2) {
            th = th2;
            int i4 = contextNode;
            StringBufferPool.free(formattedNumber);
            throw th;
        }
    }

    private void getFormattedNumber(TransformerImpl transformer, int contextNode, char numberType, int numberWidth, long listElement, FastStringBuffer formattedNumber) throws TransformerException {
        String letterVal;
        String padString;
        if (this.m_lettervalue_avt != null) {
            letterVal = this.m_lettervalue_avt.evaluate(transformer.getXPathContext(), contextNode, this);
        } else {
            letterVal = null;
        }
        switch (numberType) {
            case 'A':
                if (this.m_alphaCountTable == null) {
                    this.m_alphaCountTable = (CharArrayWrapper) XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, getLocale(transformer, contextNode)).getObject(XResourceBundle.LANG_ALPHABET);
                }
                int2alphaCount(listElement, this.m_alphaCountTable, formattedNumber);
                return;
            case Constants.ELEMNAME_VARIABLE:
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
                XResourceBundle thisBundle = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("el", ""));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    int2alphaCount(listElement, (CharArrayWrapper) thisBundle.getObject(XResourceBundle.LANG_ALPHABET), formattedNumber);
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle));
                    return;
                }
            case 1072:
                XResourceBundle thisBundle2 = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("cy", ""));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    int2alphaCount(listElement, (CharArrayWrapper) thisBundle2.getObject(XResourceBundle.LANG_ALPHABET), formattedNumber);
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle2));
                    return;
                }
            case 1488:
                XResourceBundle thisBundle3 = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("he", ""));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    int2alphaCount(listElement, (CharArrayWrapper) thisBundle3.getObject(XResourceBundle.LANG_ALPHABET), formattedNumber);
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle3));
                    return;
                }
            case 3665:
                XResourceBundle thisBundle4 = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("th", ""));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    int2alphaCount(listElement, (CharArrayWrapper) thisBundle4.getObject(XResourceBundle.LANG_ALPHABET), formattedNumber);
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle4));
                    return;
                }
            case 4304:
                XResourceBundle thisBundle5 = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("ka", ""));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    int2alphaCount(listElement, (CharArrayWrapper) thisBundle5.getObject(XResourceBundle.LANG_ALPHABET), formattedNumber);
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle5));
                    return;
                }
            case 12354:
                XResourceBundle thisBundle6 = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("ja", "JP", "HA"));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    formattedNumber.append(int2singlealphaCount(listElement, (CharArrayWrapper) thisBundle6.getObject(XResourceBundle.LANG_ALPHABET)));
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle6));
                    return;
                }
            case 12356:
                XResourceBundle thisBundle7 = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("ja", "JP", "HI"));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    formattedNumber.append(int2singlealphaCount(listElement, (CharArrayWrapper) thisBundle7.getObject(XResourceBundle.LANG_ALPHABET)));
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle7));
                    return;
                }
            case 12450:
                XResourceBundle thisBundle8 = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("ja", "JP", "A"));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    formattedNumber.append(int2singlealphaCount(listElement, (CharArrayWrapper) thisBundle8.getObject(XResourceBundle.LANG_ALPHABET)));
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle8));
                    return;
                }
            case 12452:
                XResourceBundle thisBundle9 = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("ja", "JP", "I"));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    formattedNumber.append(int2singlealphaCount(listElement, (CharArrayWrapper) thisBundle9.getObject(XResourceBundle.LANG_ALPHABET)));
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle9));
                    return;
                }
            case 19968:
                XResourceBundle thisBundle10 = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("zh", "CN"));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    int2alphaCount(listElement, (CharArrayWrapper) thisBundle10.getObject(XResourceBundle.LANG_ALPHABET), formattedNumber);
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle10));
                    return;
                }
            case 22777:
                XResourceBundle thisBundle11 = XResourceBundle.loadResourceBundle(XResourceBundle.LANG_BUNDLE_NAME, new Locale("zh", "TW"));
                if (letterVal == null || !letterVal.equals(Constants.ATTRVAL_TRADITIONAL)) {
                    int2alphaCount(listElement, (CharArrayWrapper) thisBundle11.getObject(XResourceBundle.LANG_ALPHABET), formattedNumber);
                    return;
                } else {
                    formattedNumber.append(tradAlphaCount(listElement, thisBundle11));
                    return;
                }
            default:
                DecimalFormat formatter = getNumberFormatter(transformer, contextNode);
                if (formatter == null) {
                    padString = String.valueOf(0);
                } else {
                    padString = formatter.format(0);
                }
                String numString = formatter == null ? String.valueOf(listElement) : formatter.format(listElement);
                int nPadding = numberWidth - numString.length();
                for (int k = 0; k < nPadding; k++) {
                    formattedNumber.append(padString);
                }
                formattedNumber.append(numString);
                return;
        }
    }

    /* access modifiers changed from: package-private */
    public String getZeroString() {
        return "0";
    }

    /* access modifiers changed from: protected */
    public String int2singlealphaCount(long val, CharArrayWrapper table) {
        if (val > ((long) table.getLength())) {
            return getZeroString();
        }
        return new Character(table.getChar(((int) val) - 1)).toString();
    }

    /* access modifiers changed from: protected */
    public void int2alphaCount(long val, CharArrayWrapper aTable, FastStringBuffer stringBuf) {
        CharArrayWrapper charArrayWrapper = aTable;
        int radix = aTable.getLength();
        char[] table = new char[radix];
        int i = 0;
        while (i < radix - 1) {
            table[i + 1] = charArrayWrapper.getChar(i);
            i++;
        }
        table[0] = charArrayWrapper.getChar(i);
        char[] buf = new char[100];
        long val2 = val;
        int charPos = buf.length - 1;
        int lookupIndex = 1;
        long correction = 0;
        while (true) {
            correction = (lookupIndex == 0 || (correction != 0 && lookupIndex == radix + -1)) ? (long) (radix - 1) : 0;
            lookupIndex = ((int) (val2 + correction)) % radix;
            val2 /= (long) radix;
            if (lookupIndex == 0 && val2 == 0) {
                break;
            }
            int charPos2 = charPos - 1;
            buf[charPos] = table[lookupIndex];
            if (val2 <= 0) {
                charPos = charPos2;
                break;
            } else {
                FastStringBuffer fastStringBuffer = stringBuf;
                charPos = charPos2;
            }
        }
        stringBuf.append(buf, charPos + 1, (buf.length - charPos) - 1);
    }

    /* access modifiers changed from: protected */
    public String tradAlphaCount(long val, XResourceBundle thisBundle) {
        long val2;
        CharArrayWrapper zeroChar;
        String numbering;
        long val3;
        char[] table;
        int j;
        int charPos;
        int charPos2;
        XResourceBundle xResourceBundle = thisBundle;
        if (val > Long.MAX_VALUE) {
            error(XSLTErrorResources.ER_NUMBER_TOO_BIG);
            return "#error";
        }
        char[] table2 = null;
        int lookupIndex = 1;
        char[] buf = new char[100];
        int charPos3 = 0;
        IntArrayWrapper groups = (IntArrayWrapper) xResourceBundle.getObject(XResourceBundle.LANG_NUMBERGROUPS);
        StringArrayWrapper tables = (StringArrayWrapper) xResourceBundle.getObject(XResourceBundle.LANG_NUM_TABLES);
        String numbering2 = xResourceBundle.getString(XResourceBundle.LANG_NUMBERING);
        if (numbering2.equals(XResourceBundle.LANG_MULT_ADD)) {
            String mult_order = xResourceBundle.getString(XResourceBundle.MULT_ORDER);
            LongArrayWrapper multiplier = (LongArrayWrapper) xResourceBundle.getObject(XResourceBundle.LANG_MULTIPLIER);
            CharArrayWrapper zeroChar2 = (CharArrayWrapper) xResourceBundle.getObject("zero");
            int i = 0;
            while (i < multiplier.getLength() && val < multiplier.getLong(i)) {
                i++;
            }
            val2 = val;
            while (true) {
                if (i >= multiplier.getLength()) {
                    String str = numbering2;
                    break;
                }
                if (val2 >= multiplier.getLong(i)) {
                    char[] table3 = table2;
                    if (val2 >= multiplier.getLong(i)) {
                        long mult = val2 / multiplier.getLong(i);
                        long val4 = val2 % multiplier.getLong(i);
                        int lookupIndex2 = lookupIndex;
                        int k = 0;
                        while (true) {
                            val3 = val4;
                            if (k >= groups.getLength()) {
                                numbering = numbering2;
                                zeroChar = zeroChar2;
                                table = table3;
                                break;
                            }
                            lookupIndex2 = 1;
                            if (mult / ((long) groups.getInt(k)) <= 0) {
                                k++;
                                val4 = val3;
                            } else {
                                CharArrayWrapper THEletters = (CharArrayWrapper) xResourceBundle.getObject(tables.getString(k));
                                table = new char[(THEletters.getLength() + 1)];
                                int j2 = 0;
                                while (true) {
                                    numbering = numbering2;
                                    zeroChar = zeroChar2;
                                    j = j2;
                                    if (j >= THEletters.getLength()) {
                                        break;
                                    }
                                    table[j + 1] = THEletters.getChar(j);
                                    j2 = j + 1;
                                    numbering2 = numbering;
                                    zeroChar2 = zeroChar;
                                }
                                table[0] = THEletters.getChar(j - 1);
                                lookupIndex2 = ((int) mult) / groups.getInt(k);
                                if (!(lookupIndex2 == 0 && mult == 0)) {
                                    char multiplierChar = ((CharArrayWrapper) xResourceBundle.getObject(XResourceBundle.LANG_MULTIPLIER_CHAR)).getChar(i);
                                    CharArrayWrapper charArrayWrapper = THEletters;
                                    if (lookupIndex2 >= table.length) {
                                        return "#error";
                                    } else if (mult_order.equals(XResourceBundle.MULT_PRECEDES)) {
                                        int charPos4 = charPos3 + 1;
                                        buf[charPos3] = multiplierChar;
                                        charPos3 = charPos4 + 1;
                                        buf[charPos4] = table[lookupIndex2];
                                    } else {
                                        if (lookupIndex2 == 1) {
                                            long j3 = mult;
                                            if (i == multiplier.getLength() - 1) {
                                                charPos = charPos3;
                                                charPos3 = charPos + 1;
                                                buf[charPos] = multiplierChar;
                                            }
                                        }
                                        charPos = charPos3 + 1;
                                        buf[charPos3] = table[lookupIndex2];
                                        charPos3 = charPos + 1;
                                        buf[charPos] = multiplierChar;
                                    }
                                }
                            }
                        }
                        i++;
                        table2 = table;
                        lookupIndex = lookupIndex2;
                        val2 = val3;
                    } else {
                        numbering = numbering2;
                        zeroChar = zeroChar2;
                        table2 = table3;
                    }
                } else if (zeroChar2.getLength() == 0) {
                    i++;
                    numbering = numbering2;
                    zeroChar = zeroChar2;
                } else {
                    char[] table4 = table2;
                    if (buf[charPos3 - 1] != zeroChar2.getChar(0)) {
                        charPos2 = charPos3 + 1;
                        buf[charPos3] = zeroChar2.getChar(0);
                    } else {
                        charPos2 = charPos3;
                    }
                    i++;
                    charPos3 = charPos2;
                    numbering = numbering2;
                    zeroChar = zeroChar2;
                    table2 = table4;
                }
                if (i >= multiplier.getLength()) {
                    break;
                }
                numbering2 = numbering;
                zeroChar2 = zeroChar;
            }
        } else {
            val2 = val;
        }
        char[] cArr = table2;
        long val5 = val2;
        int count = 0;
        while (count < groups.getLength()) {
            if (val5 / ((long) groups.getInt(count)) <= 0) {
                count++;
            } else {
                CharArrayWrapper theletters = (CharArrayWrapper) xResourceBundle.getObject(tables.getString(count));
                char[] table5 = new char[(theletters.getLength() + 1)];
                for (int j4 = 0; j4 < theletters.getLength(); j4++) {
                    table5[j4 + 1] = theletters.getChar(j4);
                }
                table5[0] = theletters.getChar(j4 - 1);
                int lookupIndex3 = ((int) val5) / groups.getInt(count);
                val5 %= (long) groups.getInt(count);
                if (lookupIndex3 == 0) {
                    if (val5 == 0) {
                        break;
                    }
                }
                if (lookupIndex3 >= table5.length) {
                    return "#error";
                }
                buf[charPos3] = table5[lookupIndex3];
                count++;
                charPos3++;
            }
        }
        return new String(buf, 0, charPos3);
    }

    /* access modifiers changed from: protected */
    public String long2roman(long val, boolean prefixesAreOK) {
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
