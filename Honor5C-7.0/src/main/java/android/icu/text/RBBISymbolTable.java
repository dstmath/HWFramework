package android.icu.text;

import android.icu.lang.UCharacter;
import com.android.dex.DexFormat;
import java.text.ParsePosition;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;

class RBBISymbolTable implements SymbolTable {
    UnicodeSet fCachedSetLookup;
    HashMap<String, RBBISymbolTableEntry> fHashTable;
    RBBIRuleScanner fRuleScanner;
    String fRules;
    String ffffString;

    static class RBBISymbolTableEntry {
        String key;
        RBBINode val;

        RBBISymbolTableEntry() {
        }
    }

    RBBISymbolTable(RBBIRuleScanner rs, String rules) {
        this.fRules = rules;
        this.fRuleScanner = rs;
        this.fHashTable = new HashMap();
        this.ffffString = "\uffff";
    }

    public char[] lookup(String s) {
        RBBISymbolTableEntry el = (RBBISymbolTableEntry) this.fHashTable.get(s);
        if (el == null) {
            return null;
        }
        String retString;
        RBBINode varRefNode = el.val;
        while (varRefNode.fLeftChild.fType == 2) {
            varRefNode = varRefNode.fLeftChild;
        }
        RBBINode exprNode = varRefNode.fLeftChild;
        if (exprNode.fType == 0) {
            this.fCachedSetLookup = exprNode.fLeftChild.fInputSet;
            retString = this.ffffString;
        } else {
            this.fRuleScanner.error(66063);
            retString = exprNode.fText;
            this.fCachedSetLookup = null;
        }
        return retString.toCharArray();
    }

    public UnicodeMatcher lookupMatcher(int ch) {
        if (ch != DexFormat.MAX_TYPE_IDX) {
            return null;
        }
        UnicodeSet retVal = this.fCachedSetLookup;
        this.fCachedSetLookup = null;
        return retVal;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String parseReference(String text, ParsePosition pos, int limit) {
        int start = pos.getIndex();
        int i = start;
        String result = XmlPullParser.NO_NAMESPACE;
        while (i < limit) {
            int c = UTF16.charAt(text, i);
            if ((i != start || UCharacter.isUnicodeIdentifierStart(c)) && UCharacter.isUnicodeIdentifierPart(c)) {
                i += UTF16.getCharCount(c);
            }
        }
        if (i == start) {
            return result;
        }
        pos.setIndex(i);
        return text.substring(start, i);
    }

    RBBINode lookupNode(String key) {
        RBBISymbolTableEntry el = (RBBISymbolTableEntry) this.fHashTable.get(key);
        if (el != null) {
            return el.val;
        }
        return null;
    }

    void addEntry(String key, RBBINode val) {
        if (((RBBISymbolTableEntry) this.fHashTable.get(key)) != null) {
            this.fRuleScanner.error(66055);
            return;
        }
        RBBISymbolTableEntry e = new RBBISymbolTableEntry();
        e.key = key;
        e.val = val;
        this.fHashTable.put(e.key, e);
    }

    void rbbiSymtablePrint() {
        System.out.print("Variable Definitions\nName               Node Val     String Val\n----------------------------------------------------------------------\n");
        RBBISymbolTableEntry[] syms = (RBBISymbolTableEntry[]) this.fHashTable.values().toArray(new RBBISymbolTableEntry[0]);
        for (RBBISymbolTableEntry s : syms) {
            System.out.print("  " + s.key + "  ");
            System.out.print("  " + s.val + "  ");
            System.out.print(s.val.fLeftChild.fText);
            System.out.print("\n");
        }
        System.out.println("\nParsed Variable Definitions\n");
        for (RBBISymbolTableEntry s2 : syms) {
            System.out.print(s2.key);
            s2.val.fLeftChild.printTree(true);
            System.out.print("\n");
        }
    }
}
