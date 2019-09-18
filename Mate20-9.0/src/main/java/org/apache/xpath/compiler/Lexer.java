package org.apache.xpath.compiler;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.utils.ObjectVector;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.res.XPATHErrorResources;

class Lexer {
    static final int TARGETEXTRA = 10000;
    private Compiler m_compiler;
    PrefixResolver m_namespaceContext;
    private int[] m_patternMap = new int[100];
    private int m_patternMapSize;
    XPathParser m_processor;

    Lexer(Compiler compiler, PrefixResolver resolver, XPathParser xpathProcessor) {
        this.m_compiler = compiler;
        this.m_namespaceContext = resolver;
        this.m_processor = xpathProcessor;
    }

    /* access modifiers changed from: package-private */
    public void tokenize(String pat) throws TransformerException {
        tokenize(pat, null);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x008d, code lost:
        if ('-' != r3) goto L_0x0117;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x008f, code lost:
        if (r11 != false) goto L_0x0095;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0091, code lost:
        if (r7 == -1) goto L_0x0095;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0095, code lost:
        r11 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0117, code lost:
        if (r7 == -1) goto L_0x0130;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0119, code lost:
        r11 = false;
        r9 = mapPatternElemPos(r12, r9, r10);
        r10 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x011f, code lost:
        if (-1 == r13) goto L_0x0127;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0121, code lost:
        r13 = mapNSTokens(r1, r7, r13, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0127, code lost:
        addToTokenQueue(r1.substring(r7, r6));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x012e, code lost:
        r7 = -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0132, code lost:
        if ('/' != r3) goto L_0x013b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0134, code lost:
        if (r9 == false) goto L_0x013b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0136, code lost:
        r9 = mapPatternElemPos(r12, r9, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x013d, code lost:
        if ('*' != r3) goto L_0x0144;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x013f, code lost:
        r9 = mapPatternElemPos(r12, r9, r10);
        r10 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0144, code lost:
        if (r12 != 0) goto L_0x0150;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0148, code lost:
        if ('|' != r3) goto L_0x0150;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x014a, code lost:
        if (r2 == null) goto L_0x014f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x014c, code lost:
        recordTokenString(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x014f, code lost:
        r9 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0152, code lost:
        if (')' == r3) goto L_0x0164;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0156, code lost:
        if (']' != r3) goto L_0x0159;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x015b, code lost:
        if ('(' == r3) goto L_0x0161;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x015f, code lost:
        if ('[' != r3) goto L_0x0166;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x0161, code lost:
        r12 = r12 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x0164, code lost:
        r12 = r12 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0166, code lost:
        addToTokenQueue(r1.substring(r6, r6 + 1));
     */
    public void tokenize(String pat, Vector targetStrings) throws TransformerException {
        int i;
        char charAt;
        String str = pat;
        Vector vector = targetStrings;
        this.m_compiler.m_currentPattern = str;
        this.m_patternMapSize = 0;
        int i2 = 500;
        if (pat.length() < 500) {
            i2 = pat.length();
        }
        this.m_compiler.m_opMap = new OpMapVector(i2 * 5, 2500, 1);
        int nChars = pat.length();
        boolean isStartOfPat = true;
        boolean isAttrName = false;
        boolean isNum = false;
        int nesting = 0;
        int posOfNSSep = -1;
        int startSubstring = -1;
        int i3 = 0;
        while (i < nChars) {
            char c = str.charAt(i);
            switch (c) {
                case 9:
                case 10:
                    if (startSubstring == -1) {
                        break;
                    } else {
                        isStartOfPat = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
                        isAttrName = false;
                        if (-1 != posOfNSSep) {
                            posOfNSSep = mapNSTokens(str, startSubstring, posOfNSSep, i);
                        } else {
                            addToTokenQueue(str.substring(startSubstring, i));
                        }
                        startSubstring = -1;
                        isNum = false;
                        break;
                    }
                default:
                    switch (c) {
                        case ' ':
                            break;
                        case '!':
                            break;
                        case '\"':
                            if (startSubstring != -1) {
                                isNum = false;
                                isStartOfPat = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
                                isAttrName = false;
                                if (-1 != posOfNSSep) {
                                    posOfNSSep = mapNSTokens(str, startSubstring, posOfNSSep, i);
                                } else {
                                    addToTokenQueue(str.substring(startSubstring, i));
                                }
                            }
                            startSubstring = i;
                            do {
                                i++;
                                if (i < nChars) {
                                    charAt = str.charAt(i);
                                    c = charAt;
                                }
                                if (c != '\"' && i < nChars) {
                                    addToTokenQueue(str.substring(startSubstring, i + 1));
                                    startSubstring = -1;
                                    break;
                                } else {
                                    this.m_processor.error(XPATHErrorResources.ER_EXPECTED_DOUBLE_QUOTE, null);
                                    break;
                                }
                            } while (charAt != '\"');
                            if (c != '\"') {
                            }
                            this.m_processor.error(XPATHErrorResources.ER_EXPECTED_DOUBLE_QUOTE, null);
                        default:
                            switch (c) {
                                case '\'':
                                    if (startSubstring != -1) {
                                        isNum = false;
                                        isStartOfPat = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
                                        isAttrName = false;
                                        if (-1 != posOfNSSep) {
                                            posOfNSSep = mapNSTokens(str, startSubstring, posOfNSSep, i);
                                        } else {
                                            addToTokenQueue(str.substring(startSubstring, i));
                                        }
                                    }
                                    startSubstring = i;
                                    i++;
                                    while (i < nChars) {
                                        char charAt2 = str.charAt(i);
                                        c = charAt2;
                                        if (charAt2 == '\'') {
                                            if (c != '\'' && i < nChars) {
                                                addToTokenQueue(str.substring(startSubstring, i + 1));
                                                startSubstring = -1;
                                                break;
                                            } else {
                                                this.m_processor.error(XPATHErrorResources.ER_EXPECTED_SINGLE_QUOTE, null);
                                                break;
                                            }
                                        } else {
                                            i++;
                                        }
                                    }
                                    if (c != '\'') {
                                    }
                                    this.m_processor.error(XPATHErrorResources.ER_EXPECTED_SINGLE_QUOTE, null);
                                case '-':
                                    break;
                                case '(':
                                case ')':
                                case '*':
                                case '+':
                                case ',':
                                    break;
                                default:
                                    switch (c) {
                                        case '<':
                                        case '=':
                                        case '>':
                                            break;
                                        default:
                                            switch (c) {
                                                case '[':
                                                case '\\':
                                                case ']':
                                                case '^':
                                                    break;
                                                default:
                                                    switch (c) {
                                                        case 13:
                                                            break;
                                                        case '$':
                                                        case '/':
                                                        case '|':
                                                            break;
                                                        case ':':
                                                            if (i > 0) {
                                                                if (posOfNSSep == i - 1) {
                                                                    if (startSubstring != -1 && startSubstring < i - 1) {
                                                                        addToTokenQueue(str.substring(startSubstring, i - 1));
                                                                    }
                                                                    isNum = false;
                                                                    isAttrName = false;
                                                                    startSubstring = -1;
                                                                    posOfNSSep = -1;
                                                                    addToTokenQueue(str.substring(i - 1, i + 1));
                                                                    break;
                                                                } else {
                                                                    posOfNSSep = i;
                                                                }
                                                            }
                                                        case '@':
                                                            isAttrName = true;
                                                            break;
                                                        default:
                                                            if (-1 != startSubstring) {
                                                                if (!isNum) {
                                                                    break;
                                                                } else {
                                                                    isNum = Character.isDigit(c);
                                                                    break;
                                                                }
                                                            } else {
                                                                startSubstring = i;
                                                                isNum = Character.isDigit(c);
                                                                break;
                                                            }
                                                    }
                                                    break;
                                            }
                                    }
                            }
                            break;
                    }
                    break;
            }
            i3 = i + 1;
        }
        if (startSubstring != -1) {
            boolean isStartOfPat2 = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
            if (-1 != posOfNSSep || (this.m_namespaceContext != null && this.m_namespaceContext.handlesNullPrefixes())) {
                int posOfNSSep2 = mapNSTokens(str, startSubstring, posOfNSSep, nChars);
            } else {
                addToTokenQueue(str.substring(startSubstring, nChars));
            }
        }
        if (this.m_compiler.getTokenQueueSize() == 0) {
            this.m_processor.error(XPATHErrorResources.ER_EMPTY_EXPRESSION, null);
        } else if (vector != null) {
            recordTokenString(vector);
        }
        this.m_processor.m_queueMark = 0;
    }

    private boolean mapPatternElemPos(int nesting, boolean isStart, boolean isAttrName) {
        int[] iArr;
        int i;
        if (nesting != 0) {
            return isStart;
        }
        if (this.m_patternMapSize >= this.m_patternMap.length) {
            int[] patternMap = this.m_patternMap;
            int len = this.m_patternMap.length;
            this.m_patternMap = new int[(this.m_patternMapSize + 100)];
            System.arraycopy(patternMap, 0, this.m_patternMap, 0, len);
        }
        if (!isStart) {
            iArr[i] = this.m_patternMap[this.m_patternMapSize - 1] - 10000;
        }
        this.m_patternMap[this.m_patternMapSize] = (this.m_compiler.getTokenQueueSize() - (isAttrName)) + TARGETEXTRA;
        this.m_patternMapSize++;
        return false;
    }

    private int getTokenQueuePosFromMap(int i) {
        int pos = this.m_patternMap[i];
        return pos >= TARGETEXTRA ? pos - 10000 : pos;
    }

    private final void resetTokenMark(int mark) {
        int qsz = this.m_compiler.getTokenQueueSize();
        this.m_processor.m_queueMark = mark > 0 ? mark <= qsz ? mark - 1 : mark : 0;
        if (this.m_processor.m_queueMark < qsz) {
            XPathParser xPathParser = this.m_processor;
            ObjectVector tokenQueue = this.m_compiler.getTokenQueue();
            XPathParser xPathParser2 = this.m_processor;
            int i = xPathParser2.m_queueMark;
            xPathParser2.m_queueMark = i + 1;
            xPathParser.m_token = (String) tokenQueue.elementAt(i);
            this.m_processor.m_tokenChar = this.m_processor.m_token.charAt(0);
            return;
        }
        this.m_processor.m_token = null;
        this.m_processor.m_tokenChar = 0;
    }

    /* access modifiers changed from: package-private */
    public final int getKeywordToken(String key) {
        try {
            Integer itok = (Integer) Keywords.getKeyWord(key);
            if (itok != null) {
                return itok.intValue();
            }
            return 0;
        } catch (NullPointerException e) {
            return 0;
        } catch (ClassCastException e2) {
            return 0;
        }
    }

    private void recordTokenString(Vector targetStrings) {
        int tokPos = getTokenQueuePosFromMap(this.m_patternMapSize - 1);
        resetTokenMark(tokPos + 1);
        if (this.m_processor.lookahead('(', 1)) {
            int tok = getKeywordToken(this.m_processor.m_token);
            switch (tok) {
                case 35:
                    targetStrings.addElement(PsuedoNames.PSEUDONAME_ROOT);
                    return;
                case 36:
                    targetStrings.addElement("*");
                    return;
                default:
                    switch (tok) {
                        case OpCodes.NODETYPE_COMMENT:
                            targetStrings.addElement(PsuedoNames.PSEUDONAME_COMMENT);
                            return;
                        case OpCodes.NODETYPE_TEXT:
                            targetStrings.addElement(PsuedoNames.PSEUDONAME_TEXT);
                            return;
                        case OpCodes.NODETYPE_PI:
                            targetStrings.addElement("*");
                            return;
                        case OpCodes.NODETYPE_NODE:
                            targetStrings.addElement("*");
                            return;
                        default:
                            targetStrings.addElement("*");
                            return;
                    }
            }
        } else {
            if (this.m_processor.tokenIs('@')) {
                tokPos++;
                resetTokenMark(tokPos + 1);
            }
            if (this.m_processor.lookahead(':', 1)) {
                tokPos += 2;
            }
            targetStrings.addElement(this.m_compiler.getTokenQueue().elementAt(tokPos));
        }
    }

    private final void addToTokenQueue(String s) {
        this.m_compiler.getTokenQueue().addElement(s);
    }

    private int mapNSTokens(String pat, int startSubstring, int posOfNSSep, int posOfScan) throws TransformerException {
        String uName;
        String prefix = "";
        if (startSubstring >= 0 && posOfNSSep >= 0) {
            prefix = pat.substring(startSubstring, posOfNSSep);
        }
        if (this.m_namespaceContext == null || prefix.equals("*") || prefix.equals("xmlns")) {
            uName = prefix;
        } else {
            try {
                if (prefix.length() > 0) {
                    uName = this.m_namespaceContext.getNamespaceForPrefix(prefix);
                } else {
                    uName = this.m_namespaceContext.getNamespaceForPrefix(prefix);
                }
            } catch (ClassCastException e) {
                uName = this.m_namespaceContext.getNamespaceForPrefix(prefix);
            }
        }
        if (uName == null || uName.length() <= 0) {
            this.m_processor.errorForDOM3("ER_PREFIX_MUST_RESOLVE", new String[]{prefix});
        } else {
            addToTokenQueue(uName);
            addToTokenQueue(":");
            String s = pat.substring(posOfNSSep + 1, posOfScan);
            if (s.length() > 0) {
                addToTokenQueue(s);
            }
        }
        return -1;
    }
}
