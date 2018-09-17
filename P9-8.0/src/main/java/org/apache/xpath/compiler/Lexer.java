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

    void tokenize(String pat) throws TransformerException {
        tokenize(pat, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:80:0x0150  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x010f  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0127  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void tokenize(String pat, Vector targetStrings) throws TransformerException {
        this.m_compiler.m_currentPattern = pat;
        this.m_patternMapSize = 0;
        this.m_compiler.m_opMap = new OpMapVector((pat.length() < 500 ? pat.length() : 500) * 5, 2500, 1);
        int nChars = pat.length();
        int startSubstring = -1;
        int posOfNSSep = -1;
        boolean isStartOfPat = true;
        boolean isAttrName = false;
        boolean isNum = false;
        int nesting = 0;
        int i = 0;
        while (i < nChars) {
            char c = pat.charAt(i);
            switch (c) {
                case 9:
                case 10:
                case 13:
                case ' ':
                    if (startSubstring != -1) {
                        isNum = false;
                        isStartOfPat = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
                        isAttrName = false;
                        if (-1 != posOfNSSep) {
                            posOfNSSep = mapNSTokens(pat, startSubstring, posOfNSSep, i);
                        } else {
                            addToTokenQueue(pat.substring(startSubstring, i));
                        }
                        startSubstring = -1;
                        break;
                    }
                    continue;
                case '!':
                case '$':
                case '(':
                case ')':
                case '*':
                case '+':
                case ',':
                case '/':
                case '<':
                case '=':
                case '>':
                case '[':
                case '\\':
                case ']':
                case '^':
                case '|':
                    if (startSubstring == -1) {
                        isNum = false;
                        isStartOfPat = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
                        isAttrName = false;
                        if (-1 != posOfNSSep) {
                            posOfNSSep = mapNSTokens(pat, startSubstring, posOfNSSep, i);
                        } else {
                            addToTokenQueue(pat.substring(startSubstring, i));
                        }
                        startSubstring = -1;
                    } else if ('/' == c && isStartOfPat) {
                        isStartOfPat = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
                    } else if ('*' == c) {
                        isStartOfPat = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
                        isAttrName = false;
                    }
                    if (nesting == 0 && '|' == c) {
                        if (targetStrings != null) {
                            recordTokenString(targetStrings);
                        }
                        isStartOfPat = true;
                    }
                    if (')' != c || ']' == c) {
                        nesting--;
                    } else if ('(' == c || '[' == c) {
                        nesting++;
                    }
                    addToTokenQueue(pat.substring(i, i + 1));
                    continue;
                case '\"':
                    if (startSubstring != -1) {
                        isNum = false;
                        isStartOfPat = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
                        isAttrName = false;
                        if (-1 != posOfNSSep) {
                            posOfNSSep = mapNSTokens(pat, startSubstring, posOfNSSep, i);
                        } else {
                            addToTokenQueue(pat.substring(startSubstring, i));
                        }
                    }
                    startSubstring = i;
                    i++;
                    while (i < nChars) {
                        c = pat.charAt(i);
                        if (c != '\"') {
                            i++;
                        } else if (c == '\"' || i >= nChars) {
                            this.m_processor.error(XPATHErrorResources.ER_EXPECTED_DOUBLE_QUOTE, null);
                            break;
                        } else {
                            addToTokenQueue(pat.substring(startSubstring, i + 1));
                            startSubstring = -1;
                            continue;
                        }
                    }
                    if (c == '\"') {
                    }
                    this.m_processor.error(XPATHErrorResources.ER_EXPECTED_DOUBLE_QUOTE, null);
                    break;
                case '\'':
                    if (startSubstring != -1) {
                        isNum = false;
                        isStartOfPat = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
                        isAttrName = false;
                        if (-1 != posOfNSSep) {
                            posOfNSSep = mapNSTokens(pat, startSubstring, posOfNSSep, i);
                        } else {
                            addToTokenQueue(pat.substring(startSubstring, i));
                        }
                    }
                    startSubstring = i;
                    i++;
                    while (i < nChars) {
                        c = pat.charAt(i);
                        if (c != '\'') {
                            i++;
                        } else if (c == '\'' || i >= nChars) {
                            this.m_processor.error(XPATHErrorResources.ER_EXPECTED_SINGLE_QUOTE, null);
                            break;
                        } else {
                            addToTokenQueue(pat.substring(startSubstring, i + 1));
                            startSubstring = -1;
                            continue;
                        }
                    }
                    if (c == '\'') {
                    }
                    this.m_processor.error(XPATHErrorResources.ER_EXPECTED_SINGLE_QUOTE, null);
                    break;
                case '-':
                    break;
                case ':':
                    if (i > 0) {
                        if (posOfNSSep == i - 1) {
                            if (startSubstring != -1 && startSubstring < i - 1) {
                                addToTokenQueue(pat.substring(startSubstring, i - 1));
                            }
                            isNum = false;
                            isAttrName = false;
                            startSubstring = -1;
                            posOfNSSep = -1;
                            addToTokenQueue(pat.substring(i - 1, i + 1));
                            continue;
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
                        }
                        isNum = Character.isDigit(c);
                        break;
                    }
                    startSubstring = i;
                    isNum = Character.isDigit(c);
                    continue;
            }
            if ('-' == c) {
                Object obj = (isNum || startSubstring == -1) ? 1 : null;
                if (obj != null) {
                    isNum = false;
                } else {
                    i++;
                }
            }
            if (startSubstring == -1) {
            }
            if (targetStrings != null) {
            }
            isStartOfPat = true;
            if (')' != c) {
            }
            nesting--;
            addToTokenQueue(pat.substring(i, i + 1));
            continue;
            i++;
        }
        if (startSubstring != -1) {
            isStartOfPat = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
            if (-1 != posOfNSSep || (this.m_namespaceContext != null && this.m_namespaceContext.handlesNullPrefixes())) {
                posOfNSSep = mapNSTokens(pat, startSubstring, posOfNSSep, nChars);
            } else {
                addToTokenQueue(pat.substring(startSubstring, nChars));
            }
        }
        if (this.m_compiler.getTokenQueueSize() == 0) {
            this.m_processor.error(XPATHErrorResources.ER_EMPTY_EXPRESSION, null);
        } else if (targetStrings != null) {
            recordTokenString(targetStrings);
        }
        this.m_processor.m_queueMark = 0;
    }

    private boolean mapPatternElemPos(int nesting, boolean isStart, boolean isAttrName) {
        int i = 0;
        if (nesting != 0) {
            return isStart;
        }
        int[] iArr;
        int i2;
        if (this.m_patternMapSize >= this.m_patternMap.length) {
            int[] patternMap = this.m_patternMap;
            int len = this.m_patternMap.length;
            this.m_patternMap = new int[(this.m_patternMapSize + 100)];
            System.arraycopy(patternMap, 0, this.m_patternMap, 0, len);
        }
        if (!isStart) {
            iArr = this.m_patternMap;
            i2 = this.m_patternMapSize - 1;
            iArr[i2] = iArr[i2] - 10000;
        }
        iArr = this.m_patternMap;
        i2 = this.m_patternMapSize;
        int tokenQueueSize = this.m_compiler.getTokenQueueSize();
        if (isAttrName) {
            i = 1;
        }
        iArr[i2] = (tokenQueueSize - i) + TARGETEXTRA;
        this.m_patternMapSize++;
        return false;
    }

    private int getTokenQueuePosFromMap(int i) {
        int pos = this.m_patternMap[i];
        return pos >= TARGETEXTRA ? pos - 10000 : pos;
    }

    private final void resetTokenMark(int mark) {
        int qsz = this.m_compiler.getTokenQueueSize();
        XPathParser xPathParser = this.m_processor;
        if (mark <= 0) {
            mark = 0;
        } else if (mark <= qsz) {
            mark--;
        }
        xPathParser.m_queueMark = mark;
        if (this.m_processor.m_queueMark < qsz) {
            XPathParser xPathParser2 = this.m_processor;
            ObjectVector tokenQueue = this.m_compiler.getTokenQueue();
            XPathParser xPathParser3 = this.m_processor;
            int i = xPathParser3.m_queueMark;
            xPathParser3.m_queueMark = i + 1;
            xPathParser2.m_token = (String) tokenQueue.elementAt(i);
            this.m_processor.m_tokenChar = this.m_processor.m_token.charAt(0);
            return;
        }
        this.m_processor.m_token = null;
        this.m_processor.m_tokenChar = 0;
    }

    final int getKeywordToken(String key) {
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
            switch (getKeywordToken(this.m_processor.m_token)) {
                case 35:
                    targetStrings.addElement(PsuedoNames.PSEUDONAME_ROOT);
                    return;
                case 36:
                    targetStrings.addElement("*");
                    return;
                case OpCodes.NODETYPE_COMMENT /*1030*/:
                    targetStrings.addElement(PsuedoNames.PSEUDONAME_COMMENT);
                    return;
                case OpCodes.NODETYPE_TEXT /*1031*/:
                    targetStrings.addElement(PsuedoNames.PSEUDONAME_TEXT);
                    return;
                case OpCodes.NODETYPE_PI /*1032*/:
                    targetStrings.addElement("*");
                    return;
                case OpCodes.NODETYPE_NODE /*1033*/:
                    targetStrings.addElement("*");
                    return;
                default:
                    targetStrings.addElement("*");
                    return;
            }
        }
        if (this.m_processor.tokenIs('@')) {
            tokPos++;
            resetTokenMark(tokPos + 1);
        }
        if (this.m_processor.lookahead(':', 1)) {
            tokPos += 2;
        }
        targetStrings.addElement(this.m_compiler.getTokenQueue().elementAt(tokPos));
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
        if (this.m_namespaceContext == null || (prefix.equals("*") ^ 1) == 0 || (prefix.equals("xmlns") ^ 1) == 0) {
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
