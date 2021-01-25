package ohos.com.sun.org.apache.xerces.internal.impl.xpath;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;

public class XPath {
    private static final boolean DEBUG_ALL = false;
    private static final boolean DEBUG_ANY = false;
    private static final boolean DEBUG_XPATH_PARSE = false;
    protected String fExpression;
    protected LocationPath[] fLocationPaths;
    protected SymbolTable fSymbolTable;

    public XPath(String str, SymbolTable symbolTable, NamespaceContext namespaceContext) throws XPathException {
        this.fExpression = str;
        this.fSymbolTable = symbolTable;
        parseExpression(namespaceContext);
    }

    public LocationPath[] getLocationPaths() {
        LocationPath[] locationPathArr = new LocationPath[this.fLocationPaths.length];
        int i = 0;
        while (true) {
            LocationPath[] locationPathArr2 = this.fLocationPaths;
            if (i >= locationPathArr2.length) {
                return locationPathArr;
            }
            locationPathArr[i] = (LocationPath) locationPathArr2[i].clone();
            i++;
        }
    }

    public LocationPath getLocationPath() {
        return (LocationPath) this.fLocationPaths[0].clone();
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < this.fLocationPaths.length; i++) {
            if (i > 0) {
                stringBuffer.append("|");
            }
            stringBuffer.append(this.fLocationPaths[i].toString());
        }
        return stringBuffer.toString();
    }

    private static void check(boolean z) throws XPathException {
        if (!z) {
            throw new XPathException("c-general-xpath");
        }
    }

    private LocationPath buildLocationPath(Vector vector) throws XPathException {
        int size = vector.size();
        check(size != 0);
        Step[] stepArr = new Step[size];
        vector.copyInto(stepArr);
        vector.removeAllElements();
        return new LocationPath(stepArr);
    }

    private void parseExpression(NamespaceContext namespaceContext) throws XPathException {
        Tokens tokens = new Tokens(this.fSymbolTable);
        if (new Scanner(this.fSymbolTable) {
            /* class ohos.com.sun.org.apache.xerces.internal.impl.xpath.XPath.AnonymousClass1 */

            /* access modifiers changed from: protected */
            @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.XPath.Scanner
            public void addToken(Tokens tokens, int i) throws XPathException {
                if (i == 6 || i == 35 || i == 11 || i == 21 || i == 4 || i == 9 || i == 10 || i == 22 || i == 23 || i == 36 || i == 8) {
                    super.addToken(tokens, i);
                    return;
                }
                throw new XPathException("c-general-xpath");
            }
        }.scanExpr(this.fSymbolTable, tokens, this.fExpression, 0, this.fExpression.length())) {
            Vector vector = new Vector();
            Vector vector2 = new Vector();
            boolean z = false;
            while (true) {
                boolean z2 = true;
                while (tokens.hasMore()) {
                    int nextToken = tokens.nextToken();
                    if (nextToken == 4) {
                        check(z2);
                        if (vector.size() == 0) {
                            vector.addElement(new Step(new Axis((short) 3), new NodeTest((short) 3)));
                            if (tokens.hasMore() && tokens.peekToken() == 22) {
                                tokens.nextToken();
                                vector.addElement(new Step(new Axis((short) 4), new NodeTest((short) 3)));
                            }
                        }
                    } else if (nextToken != 6) {
                        if (nextToken == 35) {
                            check(z2);
                            if (tokens.nextToken() == 8) {
                                vector.addElement(new Step(new Axis((short) 2), parseNodeTest(tokens.nextToken(), tokens, namespaceContext)));
                                z2 = false;
                                z = false;
                            }
                        } else if (nextToken != 36) {
                            switch (nextToken) {
                                case 8:
                                    check(z2);
                                    check(z);
                                    z = false;
                                case 9:
                                case 10:
                                case 11:
                                    check(z2);
                                    vector.addElement(new Step(new Axis((short) 1), parseNodeTest(nextToken, tokens, namespaceContext)));
                                    break;
                                default:
                                    switch (nextToken) {
                                        case 21:
                                            check(!z2);
                                            continue;
                                        case 22:
                                            throw new XPathException("c-general-xpath");
                                        case 23:
                                            check(!z2);
                                            vector2.addElement(buildLocationPath(vector));
                                            continue;
                                        default:
                                            throw new XPathException("c-general-xpath");
                                    }
                            }
                        } else {
                            check(z2);
                        }
                        z = true;
                    } else {
                        check(z2);
                        vector.addElement(new Step(new Axis((short) 2), parseNodeTest(tokens.nextToken(), tokens, namespaceContext)));
                    }
                    z2 = false;
                }
                check(!z2);
                vector2.addElement(buildLocationPath(vector));
                this.fLocationPaths = new LocationPath[vector2.size()];
                vector2.copyInto(this.fLocationPaths);
                return;
            }
        }
        throw new XPathException("c-general-xpath");
    }

    private NodeTest parseNodeTest(int i, Tokens tokens, NamespaceContext namespaceContext) throws XPathException {
        String str;
        switch (i) {
            case 9:
                return new NodeTest((short) 2);
            case 10:
            case 11:
                String nextTokenAsString = tokens.nextTokenAsString();
                String str2 = null;
                if (!(namespaceContext == null || nextTokenAsString == XMLSymbols.EMPTY_STRING)) {
                    str2 = namespaceContext.getURI(nextTokenAsString);
                }
                if (nextTokenAsString != XMLSymbols.EMPTY_STRING && namespaceContext != null && str2 == null) {
                    throw new XPathException("c-general-xpath-ns");
                } else if (i == 10) {
                    return new NodeTest(nextTokenAsString, str2);
                } else {
                    String nextTokenAsString2 = tokens.nextTokenAsString();
                    if (nextTokenAsString != XMLSymbols.EMPTY_STRING) {
                        SymbolTable symbolTable = this.fSymbolTable;
                        str = symbolTable.addSymbol(nextTokenAsString + ':' + nextTokenAsString2);
                    } else {
                        str = nextTokenAsString2;
                    }
                    return new NodeTest(new QName(nextTokenAsString, nextTokenAsString2, str, str2));
                }
            default:
                throw new XPathException("c-general-xpath");
        }
    }

    public static class LocationPath implements Cloneable {
        public Step[] steps;

        public LocationPath(Step[] stepArr) {
            this.steps = stepArr;
        }

        protected LocationPath(LocationPath locationPath) {
            this.steps = new Step[locationPath.steps.length];
            int i = 0;
            while (true) {
                Step[] stepArr = this.steps;
                if (i < stepArr.length) {
                    stepArr[i] = (Step) locationPath.steps[i].clone();
                    i++;
                } else {
                    return;
                }
            }
        }

        @Override // java.lang.Object
        public String toString() {
            StringBuffer stringBuffer = new StringBuffer();
            int i = 0;
            while (true) {
                Step[] stepArr = this.steps;
                if (i >= stepArr.length) {
                    return stringBuffer.toString();
                }
                if (!(i <= 0 || stepArr[i - 1].axis.type == 4 || this.steps[i].axis.type == 4)) {
                    stringBuffer.append('/');
                }
                stringBuffer.append(this.steps[i].toString());
                i++;
            }
        }

        @Override // java.lang.Object
        public Object clone() {
            return new LocationPath(this);
        }
    }

    public static class Step implements Cloneable {
        public Axis axis;
        public NodeTest nodeTest;

        public Step(Axis axis2, NodeTest nodeTest2) {
            this.axis = axis2;
            this.nodeTest = nodeTest2;
        }

        protected Step(Step step) {
            this.axis = (Axis) step.axis.clone();
            this.nodeTest = (NodeTest) step.nodeTest.clone();
        }

        @Override // java.lang.Object
        public String toString() {
            if (this.axis.type == 3) {
                return ".";
            }
            if (this.axis.type == 2) {
                return "@" + this.nodeTest.toString();
            } else if (this.axis.type == 1) {
                return this.nodeTest.toString();
            } else {
                if (this.axis.type == 4) {
                    return "//";
                }
                return "??? (" + ((int) this.axis.type) + ')';
            }
        }

        @Override // java.lang.Object
        public Object clone() {
            return new Step(this);
        }
    }

    public static class Axis implements Cloneable {
        public static final short ATTRIBUTE = 2;
        public static final short CHILD = 1;
        public static final short DESCENDANT = 4;
        public static final short SELF = 3;
        public short type;

        public Axis(short s) {
            this.type = s;
        }

        protected Axis(Axis axis) {
            this.type = axis.type;
        }

        @Override // java.lang.Object
        public String toString() {
            short s = this.type;
            if (s == 1) {
                return "child";
            }
            if (s == 2) {
                return "attribute";
            }
            if (s != 3) {
                return s != 4 ? "???" : "descendant";
            }
            return "self";
        }

        @Override // java.lang.Object
        public Object clone() {
            return new Axis(this);
        }
    }

    public static class NodeTest implements Cloneable {
        public static final short NAMESPACE = 4;
        public static final short NODE = 3;
        public static final short QNAME = 1;
        public static final short WILDCARD = 2;
        public final QName name;
        public short type;

        public NodeTest(short s) {
            this.name = new QName();
            this.type = s;
        }

        public NodeTest(QName qName) {
            this.name = new QName();
            this.type = 1;
            this.name.setValues(qName);
        }

        public NodeTest(String str, String str2) {
            this.name = new QName();
            this.type = 4;
            this.name.setValues(str, null, null, str2);
        }

        public NodeTest(NodeTest nodeTest) {
            this.name = new QName();
            this.type = nodeTest.type;
            this.name.setValues(nodeTest.name);
        }

        @Override // java.lang.Object
        public String toString() {
            short s = this.type;
            if (s != 1) {
                if (s == 2) {
                    return "*";
                }
                if (s == 3) {
                    return "node()";
                }
                if (s != 4) {
                    return "???";
                }
                if (this.name.prefix.length() == 0) {
                    return "???:*";
                }
                if (this.name.uri != null) {
                    return this.name.prefix + ":*";
                }
                return "{" + this.name.uri + '}' + this.name.prefix + ":*";
            } else if (this.name.prefix.length() == 0) {
                return this.name.localpart;
            } else {
                if (this.name.uri != null) {
                    return this.name.prefix + ':' + this.name.localpart;
                }
                return "{" + this.name.uri + '}' + this.name.prefix + ':' + this.name.localpart;
            }
        }

        @Override // java.lang.Object
        public Object clone() {
            return new NodeTest(this);
        }
    }

    /* access modifiers changed from: private */
    public static final class Tokens {
        static final boolean DUMP_TOKENS = false;
        public static final int EXPRTOKEN_ATSIGN = 6;
        public static final int EXPRTOKEN_AXISNAME_ANCESTOR = 33;
        public static final int EXPRTOKEN_AXISNAME_ANCESTOR_OR_SELF = 34;
        public static final int EXPRTOKEN_AXISNAME_ATTRIBUTE = 35;
        public static final int EXPRTOKEN_AXISNAME_CHILD = 36;
        public static final int EXPRTOKEN_AXISNAME_DESCENDANT = 37;
        public static final int EXPRTOKEN_AXISNAME_DESCENDANT_OR_SELF = 38;
        public static final int EXPRTOKEN_AXISNAME_FOLLOWING = 39;
        public static final int EXPRTOKEN_AXISNAME_FOLLOWING_SIBLING = 40;
        public static final int EXPRTOKEN_AXISNAME_NAMESPACE = 41;
        public static final int EXPRTOKEN_AXISNAME_PARENT = 42;
        public static final int EXPRTOKEN_AXISNAME_PRECEDING = 43;
        public static final int EXPRTOKEN_AXISNAME_PRECEDING_SIBLING = 44;
        public static final int EXPRTOKEN_AXISNAME_SELF = 45;
        public static final int EXPRTOKEN_CLOSE_BRACKET = 3;
        public static final int EXPRTOKEN_CLOSE_PAREN = 1;
        public static final int EXPRTOKEN_COMMA = 7;
        public static final int EXPRTOKEN_DOUBLE_COLON = 8;
        public static final int EXPRTOKEN_DOUBLE_PERIOD = 5;
        public static final int EXPRTOKEN_FUNCTION_NAME = 32;
        public static final int EXPRTOKEN_LITERAL = 46;
        public static final int EXPRTOKEN_NAMETEST_ANY = 9;
        public static final int EXPRTOKEN_NAMETEST_NAMESPACE = 10;
        public static final int EXPRTOKEN_NAMETEST_QNAME = 11;
        public static final int EXPRTOKEN_NODETYPE_COMMENT = 12;
        public static final int EXPRTOKEN_NODETYPE_NODE = 15;
        public static final int EXPRTOKEN_NODETYPE_PI = 14;
        public static final int EXPRTOKEN_NODETYPE_TEXT = 13;
        public static final int EXPRTOKEN_NUMBER = 47;
        public static final int EXPRTOKEN_OPEN_BRACKET = 2;
        public static final int EXPRTOKEN_OPEN_PAREN = 0;
        public static final int EXPRTOKEN_OPERATOR_AND = 16;
        public static final int EXPRTOKEN_OPERATOR_DIV = 19;
        public static final int EXPRTOKEN_OPERATOR_DOUBLE_SLASH = 22;
        public static final int EXPRTOKEN_OPERATOR_EQUAL = 26;
        public static final int EXPRTOKEN_OPERATOR_GREATER = 30;
        public static final int EXPRTOKEN_OPERATOR_GREATER_EQUAL = 31;
        public static final int EXPRTOKEN_OPERATOR_LESS = 28;
        public static final int EXPRTOKEN_OPERATOR_LESS_EQUAL = 29;
        public static final int EXPRTOKEN_OPERATOR_MINUS = 25;
        public static final int EXPRTOKEN_OPERATOR_MOD = 18;
        public static final int EXPRTOKEN_OPERATOR_MULT = 20;
        public static final int EXPRTOKEN_OPERATOR_NOT_EQUAL = 27;
        public static final int EXPRTOKEN_OPERATOR_OR = 17;
        public static final int EXPRTOKEN_OPERATOR_PLUS = 24;
        public static final int EXPRTOKEN_OPERATOR_SLASH = 21;
        public static final int EXPRTOKEN_OPERATOR_UNION = 23;
        public static final int EXPRTOKEN_PERIOD = 4;
        public static final int EXPRTOKEN_VARIABLE_REFERENCE = 48;
        private static final int INITIAL_TOKEN_COUNT = 256;
        private static final String[] fgTokenNames = {"EXPRTOKEN_OPEN_PAREN", "EXPRTOKEN_CLOSE_PAREN", "EXPRTOKEN_OPEN_BRACKET", "EXPRTOKEN_CLOSE_BRACKET", "EXPRTOKEN_PERIOD", "EXPRTOKEN_DOUBLE_PERIOD", "EXPRTOKEN_ATSIGN", "EXPRTOKEN_COMMA", "EXPRTOKEN_DOUBLE_COLON", "EXPRTOKEN_NAMETEST_ANY", "EXPRTOKEN_NAMETEST_NAMESPACE", "EXPRTOKEN_NAMETEST_QNAME", "EXPRTOKEN_NODETYPE_COMMENT", "EXPRTOKEN_NODETYPE_TEXT", "EXPRTOKEN_NODETYPE_PI", "EXPRTOKEN_NODETYPE_NODE", "EXPRTOKEN_OPERATOR_AND", "EXPRTOKEN_OPERATOR_OR", "EXPRTOKEN_OPERATOR_MOD", "EXPRTOKEN_OPERATOR_DIV", "EXPRTOKEN_OPERATOR_MULT", "EXPRTOKEN_OPERATOR_SLASH", "EXPRTOKEN_OPERATOR_DOUBLE_SLASH", "EXPRTOKEN_OPERATOR_UNION", "EXPRTOKEN_OPERATOR_PLUS", "EXPRTOKEN_OPERATOR_MINUS", "EXPRTOKEN_OPERATOR_EQUAL", "EXPRTOKEN_OPERATOR_NOT_EQUAL", "EXPRTOKEN_OPERATOR_LESS", "EXPRTOKEN_OPERATOR_LESS_EQUAL", "EXPRTOKEN_OPERATOR_GREATER", "EXPRTOKEN_OPERATOR_GREATER_EQUAL", "EXPRTOKEN_FUNCTION_NAME", "EXPRTOKEN_AXISNAME_ANCESTOR", "EXPRTOKEN_AXISNAME_ANCESTOR_OR_SELF", "EXPRTOKEN_AXISNAME_ATTRIBUTE", "EXPRTOKEN_AXISNAME_CHILD", "EXPRTOKEN_AXISNAME_DESCENDANT", "EXPRTOKEN_AXISNAME_DESCENDANT_OR_SELF", "EXPRTOKEN_AXISNAME_FOLLOWING", "EXPRTOKEN_AXISNAME_FOLLOWING_SIBLING", "EXPRTOKEN_AXISNAME_NAMESPACE", "EXPRTOKEN_AXISNAME_PARENT", "EXPRTOKEN_AXISNAME_PRECEDING", "EXPRTOKEN_AXISNAME_PRECEDING_SIBLING", "EXPRTOKEN_AXISNAME_SELF", "EXPRTOKEN_LITERAL", "EXPRTOKEN_NUMBER", "EXPRTOKEN_VARIABLE_REFERENCE"};
        private int fCurrentTokenIndex;
        private Map<String, Integer> fSymbolMapping = new HashMap();
        private SymbolTable fSymbolTable;
        private int fTokenCount = 0;
        private Map<Integer, String> fTokenNames = new HashMap();
        private int[] fTokens = new int[256];

        public Tokens(SymbolTable symbolTable) {
            this.fSymbolTable = symbolTable;
            String[] strArr = {"ancestor", "ancestor-or-self", "attribute", "child", "descendant", "descendant-or-self", "following", "following-sibling", Constants.ATTRNAME_NAMESPACE, "parent", "preceding", "preceding-sibling", "self"};
            for (int i = 0; i < strArr.length; i++) {
                this.fSymbolMapping.put(this.fSymbolTable.addSymbol(strArr[i]), Integer.valueOf(i));
            }
            this.fTokenNames.put(0, "EXPRTOKEN_OPEN_PAREN");
            this.fTokenNames.put(1, "EXPRTOKEN_CLOSE_PAREN");
            this.fTokenNames.put(2, "EXPRTOKEN_OPEN_BRACKET");
            this.fTokenNames.put(3, "EXPRTOKEN_CLOSE_BRACKET");
            this.fTokenNames.put(4, "EXPRTOKEN_PERIOD");
            this.fTokenNames.put(5, "EXPRTOKEN_DOUBLE_PERIOD");
            this.fTokenNames.put(6, "EXPRTOKEN_ATSIGN");
            this.fTokenNames.put(7, "EXPRTOKEN_COMMA");
            this.fTokenNames.put(8, "EXPRTOKEN_DOUBLE_COLON");
            this.fTokenNames.put(9, "EXPRTOKEN_NAMETEST_ANY");
            this.fTokenNames.put(10, "EXPRTOKEN_NAMETEST_NAMESPACE");
            this.fTokenNames.put(11, "EXPRTOKEN_NAMETEST_QNAME");
            this.fTokenNames.put(12, "EXPRTOKEN_NODETYPE_COMMENT");
            this.fTokenNames.put(13, "EXPRTOKEN_NODETYPE_TEXT");
            this.fTokenNames.put(14, "EXPRTOKEN_NODETYPE_PI");
            this.fTokenNames.put(15, "EXPRTOKEN_NODETYPE_NODE");
            this.fTokenNames.put(16, "EXPRTOKEN_OPERATOR_AND");
            this.fTokenNames.put(17, "EXPRTOKEN_OPERATOR_OR");
            this.fTokenNames.put(18, "EXPRTOKEN_OPERATOR_MOD");
            this.fTokenNames.put(19, "EXPRTOKEN_OPERATOR_DIV");
            this.fTokenNames.put(20, "EXPRTOKEN_OPERATOR_MULT");
            this.fTokenNames.put(21, "EXPRTOKEN_OPERATOR_SLASH");
            this.fTokenNames.put(22, "EXPRTOKEN_OPERATOR_DOUBLE_SLASH");
            this.fTokenNames.put(23, "EXPRTOKEN_OPERATOR_UNION");
            this.fTokenNames.put(24, "EXPRTOKEN_OPERATOR_PLUS");
            this.fTokenNames.put(25, "EXPRTOKEN_OPERATOR_MINUS");
            this.fTokenNames.put(26, "EXPRTOKEN_OPERATOR_EQUAL");
            this.fTokenNames.put(27, "EXPRTOKEN_OPERATOR_NOT_EQUAL");
            this.fTokenNames.put(28, "EXPRTOKEN_OPERATOR_LESS");
            this.fTokenNames.put(29, "EXPRTOKEN_OPERATOR_LESS_EQUAL");
            this.fTokenNames.put(30, "EXPRTOKEN_OPERATOR_GREATER");
            this.fTokenNames.put(31, "EXPRTOKEN_OPERATOR_GREATER_EQUAL");
            this.fTokenNames.put(32, "EXPRTOKEN_FUNCTION_NAME");
            this.fTokenNames.put(33, "EXPRTOKEN_AXISNAME_ANCESTOR");
            this.fTokenNames.put(34, "EXPRTOKEN_AXISNAME_ANCESTOR_OR_SELF");
            this.fTokenNames.put(35, "EXPRTOKEN_AXISNAME_ATTRIBUTE");
            this.fTokenNames.put(36, "EXPRTOKEN_AXISNAME_CHILD");
            this.fTokenNames.put(37, "EXPRTOKEN_AXISNAME_DESCENDANT");
            this.fTokenNames.put(38, "EXPRTOKEN_AXISNAME_DESCENDANT_OR_SELF");
            this.fTokenNames.put(39, "EXPRTOKEN_AXISNAME_FOLLOWING");
            this.fTokenNames.put(40, "EXPRTOKEN_AXISNAME_FOLLOWING_SIBLING");
            this.fTokenNames.put(41, "EXPRTOKEN_AXISNAME_NAMESPACE");
            this.fTokenNames.put(42, "EXPRTOKEN_AXISNAME_PARENT");
            this.fTokenNames.put(43, "EXPRTOKEN_AXISNAME_PRECEDING");
            this.fTokenNames.put(44, "EXPRTOKEN_AXISNAME_PRECEDING_SIBLING");
            this.fTokenNames.put(45, "EXPRTOKEN_AXISNAME_SELF");
            this.fTokenNames.put(46, "EXPRTOKEN_LITERAL");
            this.fTokenNames.put(47, "EXPRTOKEN_NUMBER");
            this.fTokenNames.put(48, "EXPRTOKEN_VARIABLE_REFERENCE");
        }

        public String getTokenString(int i) {
            return this.fTokenNames.get(Integer.valueOf(i));
        }

        public void addToken(String str) {
            Integer num = null;
            for (Map.Entry<Integer, String> entry : this.fTokenNames.entrySet()) {
                if (entry.getValue().equals(str)) {
                    num = entry.getKey();
                }
            }
            if (num == null) {
                num = Integer.valueOf(this.fTokenNames.size());
                this.fTokenNames.put(num, str);
            }
            addToken(num.intValue());
        }

        public void addToken(int i) {
            try {
                this.fTokens[this.fTokenCount] = i;
            } catch (ArrayIndexOutOfBoundsException unused) {
                int[] iArr = this.fTokens;
                int i2 = this.fTokenCount;
                this.fTokens = new int[(i2 << 1)];
                System.arraycopy(iArr, 0, this.fTokens, 0, i2);
                this.fTokens[this.fTokenCount] = i;
            }
            this.fTokenCount++;
        }

        public void rewind() {
            this.fCurrentTokenIndex = 0;
        }

        public boolean hasMore() {
            return this.fCurrentTokenIndex < this.fTokenCount;
        }

        public int nextToken() throws XPathException {
            int i = this.fCurrentTokenIndex;
            if (i != this.fTokenCount) {
                int[] iArr = this.fTokens;
                this.fCurrentTokenIndex = i + 1;
                return iArr[i];
            }
            throw new XPathException("c-general-xpath");
        }

        public int peekToken() throws XPathException {
            int i = this.fCurrentTokenIndex;
            if (i != this.fTokenCount) {
                return this.fTokens[i];
            }
            throw new XPathException("c-general-xpath");
        }

        public String nextTokenAsString() throws XPathException {
            String tokenString = getTokenString(nextToken());
            if (tokenString != null) {
                return tokenString;
            }
            throw new XPathException("c-general-xpath");
        }

        public void dumpTokens() {
            int i = 0;
            while (i < this.fTokenCount) {
                switch (this.fTokens[i]) {
                    case 0:
                        System.out.print("<OPEN_PAREN/>");
                        break;
                    case 1:
                        System.out.print("<CLOSE_PAREN/>");
                        break;
                    case 2:
                        System.out.print("<OPEN_BRACKET/>");
                        break;
                    case 3:
                        System.out.print("<CLOSE_BRACKET/>");
                        break;
                    case 4:
                        System.out.print("<PERIOD/>");
                        break;
                    case 5:
                        System.out.print("<DOUBLE_PERIOD/>");
                        break;
                    case 6:
                        System.out.print("<ATSIGN/>");
                        break;
                    case 7:
                        System.out.print("<COMMA/>");
                        break;
                    case 8:
                        System.out.print("<DOUBLE_COLON/>");
                        break;
                    case 9:
                        System.out.print("<NAMETEST_ANY/>");
                        break;
                    case 10:
                        System.out.print("<NAMETEST_NAMESPACE");
                        PrintStream printStream = System.out;
                        StringBuilder sb = new StringBuilder();
                        sb.append(" prefix=\"");
                        i++;
                        sb.append(getTokenString(this.fTokens[i]));
                        sb.append("\"");
                        printStream.print(sb.toString());
                        System.out.print("/>");
                        break;
                    case 11:
                        System.out.print("<NAMETEST_QNAME");
                        int i2 = i + 1;
                        if (this.fTokens[i2] != -1) {
                            PrintStream printStream2 = System.out;
                            printStream2.print(" prefix=\"" + getTokenString(this.fTokens[i2]) + "\"");
                        }
                        PrintStream printStream3 = System.out;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(" localpart=\"");
                        i = i2 + 1;
                        sb2.append(getTokenString(this.fTokens[i]));
                        sb2.append("\"");
                        printStream3.print(sb2.toString());
                        System.out.print("/>");
                        break;
                    case 12:
                        System.out.print("<NODETYPE_COMMENT/>");
                        break;
                    case 13:
                        System.out.print("<NODETYPE_TEXT/>");
                        break;
                    case 14:
                        System.out.print("<NODETYPE_PI/>");
                        break;
                    case 15:
                        System.out.print("<NODETYPE_NODE/>");
                        break;
                    case 16:
                        System.out.print("<OPERATOR_AND/>");
                        break;
                    case 17:
                        System.out.print("<OPERATOR_OR/>");
                        break;
                    case 18:
                        System.out.print("<OPERATOR_MOD/>");
                        break;
                    case 19:
                        System.out.print("<OPERATOR_DIV/>");
                        break;
                    case 20:
                        System.out.print("<OPERATOR_MULT/>");
                        break;
                    case 21:
                        System.out.print("<OPERATOR_SLASH/>");
                        if (i + 1 >= this.fTokenCount) {
                            break;
                        } else {
                            System.out.println();
                            System.out.print("  ");
                            break;
                        }
                    case 22:
                        System.out.print("<OPERATOR_DOUBLE_SLASH/>");
                        break;
                    case 23:
                        System.out.print("<OPERATOR_UNION/>");
                        break;
                    case 24:
                        System.out.print("<OPERATOR_PLUS/>");
                        break;
                    case 25:
                        System.out.print("<OPERATOR_MINUS/>");
                        break;
                    case 26:
                        System.out.print("<OPERATOR_EQUAL/>");
                        break;
                    case 27:
                        System.out.print("<OPERATOR_NOT_EQUAL/>");
                        break;
                    case 28:
                        System.out.print("<OPERATOR_LESS/>");
                        break;
                    case 29:
                        System.out.print("<OPERATOR_LESS_EQUAL/>");
                        break;
                    case 30:
                        System.out.print("<OPERATOR_GREATER/>");
                        break;
                    case 31:
                        System.out.print("<OPERATOR_GREATER_EQUAL/>");
                        break;
                    case 32:
                        System.out.print("<FUNCTION_NAME");
                        int i3 = i + 1;
                        if (this.fTokens[i3] != -1) {
                            PrintStream printStream4 = System.out;
                            printStream4.print(" prefix=\"" + getTokenString(this.fTokens[i3]) + "\"");
                        }
                        PrintStream printStream5 = System.out;
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append(" localpart=\"");
                        i = i3 + 1;
                        sb3.append(getTokenString(this.fTokens[i]));
                        sb3.append("\"");
                        printStream5.print(sb3.toString());
                        System.out.print("/>");
                        break;
                    case 33:
                        System.out.print("<AXISNAME_ANCESTOR/>");
                        break;
                    case 34:
                        System.out.print("<AXISNAME_ANCESTOR_OR_SELF/>");
                        break;
                    case 35:
                        System.out.print("<AXISNAME_ATTRIBUTE/>");
                        break;
                    case 36:
                        System.out.print("<AXISNAME_CHILD/>");
                        break;
                    case 37:
                        System.out.print("<AXISNAME_DESCENDANT/>");
                        break;
                    case 38:
                        System.out.print("<AXISNAME_DESCENDANT_OR_SELF/>");
                        break;
                    case 39:
                        System.out.print("<AXISNAME_FOLLOWING/>");
                        break;
                    case 40:
                        System.out.print("<AXISNAME_FOLLOWING_SIBLING/>");
                        break;
                    case 41:
                        System.out.print("<AXISNAME_NAMESPACE/>");
                        break;
                    case 42:
                        System.out.print("<AXISNAME_PARENT/>");
                        break;
                    case 43:
                        System.out.print("<AXISNAME_PRECEDING/>");
                        break;
                    case 44:
                        System.out.print("<AXISNAME_PRECEDING_SIBLING/>");
                        break;
                    case 45:
                        System.out.print("<AXISNAME_SELF/>");
                        break;
                    case 46:
                        System.out.print("<LITERAL");
                        PrintStream printStream6 = System.out;
                        StringBuilder sb4 = new StringBuilder();
                        sb4.append(" value=\"");
                        i++;
                        sb4.append(getTokenString(this.fTokens[i]));
                        sb4.append("\"");
                        printStream6.print(sb4.toString());
                        System.out.print("/>");
                        break;
                    case 47:
                        System.out.print("<NUMBER");
                        PrintStream printStream7 = System.out;
                        StringBuilder sb5 = new StringBuilder();
                        sb5.append(" whole=\"");
                        int i4 = i + 1;
                        sb5.append(getTokenString(this.fTokens[i4]));
                        sb5.append("\"");
                        printStream7.print(sb5.toString());
                        PrintStream printStream8 = System.out;
                        StringBuilder sb6 = new StringBuilder();
                        sb6.append(" part=\"");
                        i = i4 + 1;
                        sb6.append(getTokenString(this.fTokens[i]));
                        sb6.append("\"");
                        printStream8.print(sb6.toString());
                        System.out.print("/>");
                        break;
                    case 48:
                        System.out.print("<VARIABLE_REFERENCE");
                        int i5 = i + 1;
                        if (this.fTokens[i5] != -1) {
                            PrintStream printStream9 = System.out;
                            printStream9.print(" prefix=\"" + getTokenString(this.fTokens[i5]) + "\"");
                        }
                        PrintStream printStream10 = System.out;
                        StringBuilder sb7 = new StringBuilder();
                        sb7.append(" localpart=\"");
                        i = i5 + 1;
                        sb7.append(getTokenString(this.fTokens[i]));
                        sb7.append("\"");
                        printStream10.print(sb7.toString());
                        System.out.print("/>");
                        break;
                    default:
                        System.out.println("<???/>");
                        break;
                }
                i++;
            }
            System.out.println();
        }
    }

    /* access modifiers changed from: private */
    public static class Scanner {
        private static final byte CHARTYPE_ATSIGN = 19;
        private static final byte CHARTYPE_CLOSE_BRACKET = 22;
        private static final byte CHARTYPE_CLOSE_PAREN = 7;
        private static final byte CHARTYPE_COLON = 15;
        private static final byte CHARTYPE_COMMA = 10;
        private static final byte CHARTYPE_DIGIT = 14;
        private static final byte CHARTYPE_DOLLAR = 5;
        private static final byte CHARTYPE_EQUAL = 17;
        private static final byte CHARTYPE_EXCLAMATION = 3;
        private static final byte CHARTYPE_GREATER = 18;
        private static final byte CHARTYPE_INVALID = 0;
        private static final byte CHARTYPE_LESS = 16;
        private static final byte CHARTYPE_LETTER = 20;
        private static final byte CHARTYPE_MINUS = 11;
        private static final byte CHARTYPE_NONASCII = 25;
        private static final byte CHARTYPE_OPEN_BRACKET = 21;
        private static final byte CHARTYPE_OPEN_PAREN = 6;
        private static final byte CHARTYPE_OTHER = 1;
        private static final byte CHARTYPE_PERIOD = 12;
        private static final byte CHARTYPE_PLUS = 9;
        private static final byte CHARTYPE_QUOTE = 4;
        private static final byte CHARTYPE_SLASH = 13;
        private static final byte CHARTYPE_STAR = 8;
        private static final byte CHARTYPE_UNDERSCORE = 23;
        private static final byte CHARTYPE_UNION = 24;
        private static final byte CHARTYPE_WHITESPACE = 2;
        private static final byte[] fASCIICharMap = {0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 4, 1, 5, 1, 1, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 15, 1, 16, 17, 18, 1, 19, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 21, 1, 22, 1, 23, 1, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 1, 24, 1, 1, 1};
        private static final String fAncestorOrSelfSymbol = "ancestor-or-self".intern();
        private static final String fAncestorSymbol = "ancestor".intern();
        private static final String fAndSymbol = "and".intern();
        private static final String fAttributeSymbol = "attribute".intern();
        private static final String fChildSymbol = "child".intern();
        private static final String fCommentSymbol = Constants.ELEMNAME_COMMENT_STRING.intern();
        private static final String fDescendantOrSelfSymbol = "descendant-or-self".intern();
        private static final String fDescendantSymbol = "descendant".intern();
        private static final String fDivSymbol = "div".intern();
        private static final String fFollowingSiblingSymbol = "following-sibling".intern();
        private static final String fFollowingSymbol = "following".intern();
        private static final String fModSymbol = "mod".intern();
        private static final String fNamespaceSymbol = Constants.ATTRNAME_NAMESPACE.intern();
        private static final String fNodeSymbol = "node".intern();
        private static final String fOrSymbol = "or".intern();
        private static final String fPISymbol = Constants.ELEMNAME_PI_STRING.intern();
        private static final String fParentSymbol = "parent".intern();
        private static final String fPrecedingSiblingSymbol = "preceding-sibling".intern();
        private static final String fPrecedingSymbol = "preceding".intern();
        private static final String fSelfSymbol = "self".intern();
        private static final String fTextSymbol = "text".intern();
        private SymbolTable fSymbolTable;

        public Scanner(SymbolTable symbolTable) {
            this.fSymbolTable = symbolTable;
        }

        public boolean scanExpr(SymbolTable symbolTable, Tokens tokens, String str, int i, int i2) throws XPathException {
            byte b;
            int i3;
            int scanNCName;
            String str2;
            boolean z;
            String str3;
            boolean z2;
            int i4;
            int i5 = i;
            while (true) {
                boolean z3 = false;
                while (i5 != i2) {
                    char charAt = str.charAt(i5);
                    while (true) {
                        if ((charAt == ' ' || charAt == '\n' || charAt == '\t' || charAt == '\r') && (i5 = i5 + 1) != i2) {
                            charAt = str.charAt(i5);
                        }
                    }
                    if (i5 != i2) {
                        if (charAt >= 128) {
                            b = 25;
                        } else {
                            b = fASCIICharMap[charAt];
                        }
                        char c = 65535;
                        switch (b) {
                            case 3:
                                i5++;
                                if (i5 != i2 && str.charAt(i5) == '=') {
                                    addToken(tokens, 27);
                                    break;
                                } else {
                                    return false;
                                }
                            case 4:
                                int i6 = i5 + 1;
                                if (i6 == i2) {
                                    return false;
                                }
                                char charAt2 = str.charAt(i6);
                                int i7 = i6;
                                while (charAt2 != charAt) {
                                    i7++;
                                    if (i7 == i2) {
                                        return false;
                                    }
                                    charAt2 = str.charAt(i7);
                                }
                                addToken(tokens, 46);
                                tokens.addToken(symbolTable.addSymbol(str.substring(i6, (i7 - i6) + i6)));
                                z3 = true;
                                i5 = i7 + 1;
                            case 5:
                                int i8 = i5 + 1;
                                if (i8 == i2 || (scanNCName = scanNCName(str, i2, i8)) == i8) {
                                    return false;
                                }
                                if (scanNCName < i2) {
                                    c = str.charAt(scanNCName);
                                }
                                String addSymbol = symbolTable.addSymbol(str.substring(i8, scanNCName));
                                if (c != ':') {
                                    str2 = addSymbol;
                                    addSymbol = XMLSymbols.EMPTY_STRING;
                                    i3 = scanNCName;
                                } else {
                                    int i9 = scanNCName + 1;
                                    if (i9 == i2 || (i3 = scanNCName(str, i2, i9)) == i9) {
                                        return false;
                                    }
                                    if (i3 < i2) {
                                        str.charAt(i3);
                                    }
                                    str2 = symbolTable.addSymbol(str.substring(i9, i3));
                                }
                                addToken(tokens, 48);
                                tokens.addToken(addSymbol);
                                tokens.addToken(str2);
                                z3 = true;
                                i5 = i3;
                                break;
                            case 6:
                                addToken(tokens, 0);
                                break;
                            case 7:
                                addToken(tokens, 1);
                                i5++;
                                z3 = true;
                            case 8:
                                if (z3) {
                                    addToken(tokens, 20);
                                    z3 = false;
                                } else {
                                    addToken(tokens, 9);
                                    z3 = true;
                                }
                                i5++;
                            case 9:
                                addToken(tokens, 24);
                                break;
                            case 10:
                                addToken(tokens, 7);
                                break;
                            case 11:
                                addToken(tokens, 25);
                                break;
                            case 12:
                                int i10 = i5 + 1;
                                if (i10 == i2) {
                                    addToken(tokens, 4);
                                } else {
                                    char charAt3 = str.charAt(i10);
                                    if (charAt3 == '.') {
                                        addToken(tokens, 5);
                                        i10 = i5 + 2;
                                    } else if (charAt3 >= '0' && charAt3 <= '9') {
                                        addToken(tokens, 47);
                                        i10 = scanNumber(tokens, str, i2, i5);
                                    } else if (charAt3 == '/') {
                                        addToken(tokens, 4);
                                    } else if (charAt3 == '|') {
                                        addToken(tokens, 4);
                                    } else if (charAt3 == ' ' || charAt3 == '\n' || charAt3 == '\t' || charAt3 == '\r') {
                                        while (true) {
                                            i5++;
                                            if (i5 != i2 && ((charAt3 = str.charAt(i5)) == ' ' || charAt3 == '\n' || charAt3 == '\t' || charAt3 == '\r')) {
                                            }
                                        }
                                        if (i5 == i2 || charAt3 == '|' || charAt3 == '/') {
                                            addToken(tokens, 4);
                                            z3 = true;
                                        } else {
                                            throw new XPathException("c-general-xpath");
                                        }
                                    } else {
                                        throw new XPathException("c-general-xpath");
                                    }
                                }
                                i5 = i10;
                                z3 = true;
                                break;
                            case 13:
                                i5++;
                                if (i5 == i2) {
                                    addToken(tokens, 21);
                                    continue;
                                } else if (str.charAt(i5) == '/') {
                                    addToken(tokens, 22);
                                    break;
                                } else {
                                    addToken(tokens, 21);
                                }
                            case 14:
                                addToken(tokens, 47);
                                i5 = scanNumber(tokens, str, i2, i5);
                                z3 = true;
                            case 15:
                                i5++;
                                if (i5 != i2 && str.charAt(i5) == ':') {
                                    addToken(tokens, 8);
                                    break;
                                } else {
                                    return false;
                                }
                                break;
                            case 16:
                                i5++;
                                if (i5 == i2) {
                                    addToken(tokens, 28);
                                    continue;
                                } else if (str.charAt(i5) == '=') {
                                    addToken(tokens, 29);
                                    break;
                                } else {
                                    addToken(tokens, 28);
                                }
                            case 17:
                                addToken(tokens, 26);
                                break;
                            case 18:
                                i5++;
                                if (i5 == i2) {
                                    addToken(tokens, 30);
                                    continue;
                                } else if (str.charAt(i5) == '=') {
                                    addToken(tokens, 31);
                                    break;
                                } else {
                                    addToken(tokens, 30);
                                }
                            case 19:
                                addToken(tokens, 6);
                                break;
                            case 20:
                            case 23:
                            case 25:
                                i3 = scanNCName(str, i2, i5);
                                if (i3 == i5) {
                                    return false;
                                }
                                char charAt4 = i3 < i2 ? str.charAt(i3) : 65535;
                                String addSymbol2 = symbolTable.addSymbol(str.substring(i5, i3));
                                String str4 = XMLSymbols.EMPTY_STRING;
                                if (charAt4 == ':') {
                                    int i11 = i3 + 1;
                                    if (i11 == i2) {
                                        return false;
                                    }
                                    charAt4 = str.charAt(i11);
                                    if (charAt4 == '*') {
                                        i3 = i11 + 1;
                                        if (i3 < i2) {
                                            charAt4 = str.charAt(i3);
                                        }
                                        z = false;
                                        str3 = str4;
                                        z2 = true;
                                    } else if (charAt4 == ':') {
                                        i3 = i11 + 1;
                                        if (i3 < i2) {
                                            charAt4 = str.charAt(i3);
                                        }
                                        z2 = false;
                                        str3 = str4;
                                        z = true;
                                    } else {
                                        int scanNCName2 = scanNCName(str, i2, i11);
                                        if (scanNCName2 == i11) {
                                            return false;
                                        }
                                        if (scanNCName2 < i2) {
                                            c = str.charAt(scanNCName2);
                                        }
                                        charAt4 = c;
                                        z = false;
                                        str3 = addSymbol2;
                                        addSymbol2 = symbolTable.addSymbol(str.substring(i11, scanNCName2));
                                        i3 = scanNCName2;
                                        z2 = false;
                                    }
                                } else {
                                    z2 = false;
                                    z = false;
                                    str3 = str4;
                                }
                                while (true) {
                                    if (charAt4 == ' ' || charAt4 == '\n' || charAt4 == '\t' || charAt4 == '\r') {
                                        i3++;
                                        if (i3 != i2) {
                                            charAt4 = str.charAt(i3);
                                        }
                                    }
                                }
                                if (z3) {
                                    if (addSymbol2 == fAndSymbol) {
                                        addToken(tokens, 16);
                                    } else if (addSymbol2 == fOrSymbol) {
                                        addToken(tokens, 17);
                                    } else if (addSymbol2 == fModSymbol) {
                                        addToken(tokens, 18);
                                    } else if (addSymbol2 != fDivSymbol) {
                                        return false;
                                    } else {
                                        addToken(tokens, 19);
                                    }
                                    if (z2 || z) {
                                        return false;
                                    }
                                } else if (charAt4 == '(' && !z2 && !z) {
                                    if (addSymbol2 == fCommentSymbol) {
                                        addToken(tokens, 12);
                                    } else if (addSymbol2 == fTextSymbol) {
                                        addToken(tokens, 13);
                                    } else if (addSymbol2 == fPISymbol) {
                                        addToken(tokens, 14);
                                    } else if (addSymbol2 == fNodeSymbol) {
                                        addToken(tokens, 15);
                                    } else {
                                        addToken(tokens, 32);
                                        tokens.addToken(str3);
                                        tokens.addToken(addSymbol2);
                                    }
                                    addToken(tokens, 0);
                                    i3++;
                                } else if (z || (charAt4 == ':' && (i4 = i3 + 1) < i2 && str.charAt(i4) == ':')) {
                                    if (addSymbol2 == fAncestorSymbol) {
                                        addToken(tokens, 33);
                                    } else if (addSymbol2 == fAncestorOrSelfSymbol) {
                                        addToken(tokens, 34);
                                    } else if (addSymbol2 == fAttributeSymbol) {
                                        addToken(tokens, 35);
                                    } else if (addSymbol2 == fChildSymbol) {
                                        addToken(tokens, 36);
                                    } else if (addSymbol2 == fDescendantSymbol) {
                                        addToken(tokens, 37);
                                    } else if (addSymbol2 == fDescendantOrSelfSymbol) {
                                        addToken(tokens, 38);
                                    } else if (addSymbol2 == fFollowingSymbol) {
                                        addToken(tokens, 39);
                                    } else if (addSymbol2 == fFollowingSiblingSymbol) {
                                        addToken(tokens, 40);
                                    } else if (addSymbol2 == fNamespaceSymbol) {
                                        addToken(tokens, 41);
                                    } else if (addSymbol2 == fParentSymbol) {
                                        addToken(tokens, 42);
                                    } else if (addSymbol2 == fPrecedingSymbol) {
                                        addToken(tokens, 43);
                                    } else if (addSymbol2 == fPrecedingSiblingSymbol) {
                                        addToken(tokens, 44);
                                    } else if (addSymbol2 != fSelfSymbol) {
                                        return false;
                                    } else {
                                        addToken(tokens, 45);
                                    }
                                    if (z2) {
                                        return false;
                                    }
                                    addToken(tokens, 8);
                                    if (!z) {
                                        i3 = i3 + 1 + 1;
                                    }
                                } else {
                                    if (z2) {
                                        addToken(tokens, 10);
                                        tokens.addToken(addSymbol2);
                                    } else {
                                        addToken(tokens, 11);
                                        tokens.addToken(str3);
                                        tokens.addToken(addSymbol2);
                                    }
                                    i5 = i3;
                                    z3 = true;
                                }
                                z3 = false;
                                i5 = i3;
                                break;
                            case 21:
                                addToken(tokens, 2);
                                break;
                            case 22:
                                addToken(tokens, 3);
                                i5++;
                                z3 = true;
                            case 24:
                                addToken(tokens, 23);
                                break;
                        }
                        i5++;
                    }
                }
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public int scanNCName(String str, int i, int i2) {
            char charAt = str.charAt(i2);
            if (charAt < 128) {
                byte b = fASCIICharMap[charAt];
                if (!(b == 20 || b == 23)) {
                    return i2;
                }
            } else if (!XMLChar.isNameStart(charAt)) {
                return i2;
            }
            while (true) {
                i2++;
                if (i2 < i) {
                    char charAt2 = str.charAt(i2);
                    if (charAt2 < 128) {
                        byte b2 = fASCIICharMap[charAt2];
                        if (!(b2 == 20 || b2 == 14 || b2 == 12 || b2 == 11 || b2 == 23)) {
                            break;
                        }
                    } else if (!XMLChar.isName(charAt2)) {
                        break;
                    }
                } else {
                    break;
                }
            }
            return i2;
        }

        private int scanNumber(Tokens tokens, String str, int i, int i2) {
            char charAt = str.charAt(i2);
            int i3 = 0;
            int i4 = i2;
            int i5 = 0;
            while (charAt >= '0' && charAt <= '9') {
                i5 = (i5 * 10) + (charAt - '0');
                i4++;
                if (i4 == i) {
                    break;
                }
                charAt = str.charAt(i4);
            }
            if (charAt == '.' && (i4 = i4 + 1) < i) {
                char charAt2 = str.charAt(i4);
                while (charAt2 >= '0' && charAt2 <= '9') {
                    i3 = (i3 * 10) + (charAt2 - '0');
                    i4++;
                    if (i4 == i) {
                        break;
                    }
                    charAt2 = str.charAt(i4);
                }
                if (i3 != 0) {
                    throw new RuntimeException("find a solution!");
                }
            }
            tokens.addToken(i5);
            tokens.addToken(i3);
            return i4;
        }

        /* access modifiers changed from: protected */
        public void addToken(Tokens tokens, int i) throws XPathException {
            tokens.addToken(i);
        }
    }

    public static void main(String[] strArr) throws Exception {
        for (String str : strArr) {
            System.out.println("# XPath expression: \"" + str + '\"');
            try {
                XPath xPath = new XPath(str, new SymbolTable(), null);
                System.out.println("expanded xpath: \"" + xPath.toString() + '\"');
            } catch (XPathException e) {
                System.out.println("error: " + e.getMessage());
            }
        }
    }
}
