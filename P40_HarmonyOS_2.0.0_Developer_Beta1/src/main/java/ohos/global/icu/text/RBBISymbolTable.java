package ohos.global.icu.text;

import java.text.ParsePosition;
import java.util.HashMap;
import ohos.global.icu.lang.UCharacter;

/* access modifiers changed from: package-private */
public class RBBISymbolTable implements SymbolTable {
    UnicodeSet fCachedSetLookup;
    HashMap<String, RBBISymbolTableEntry> fHashTable = new HashMap<>();
    RBBIRuleScanner fRuleScanner;
    String ffffString = "￿";

    static class RBBISymbolTableEntry {
        String key;
        RBBINode val;

        RBBISymbolTableEntry() {
        }
    }

    RBBISymbolTable(RBBIRuleScanner rBBIRuleScanner) {
        this.fRuleScanner = rBBIRuleScanner;
    }

    @Override // ohos.global.icu.text.SymbolTable
    public char[] lookup(String str) {
        String str2;
        RBBISymbolTableEntry rBBISymbolTableEntry = this.fHashTable.get(str);
        if (rBBISymbolTableEntry == null) {
            return null;
        }
        RBBINode rBBINode = rBBISymbolTableEntry.val;
        while (rBBINode.fLeftChild.fType == 2) {
            rBBINode = rBBINode.fLeftChild;
        }
        RBBINode rBBINode2 = rBBINode.fLeftChild;
        if (rBBINode2.fType == 0) {
            this.fCachedSetLookup = rBBINode2.fLeftChild.fInputSet;
            str2 = this.ffffString;
        } else {
            this.fRuleScanner.error(66063);
            String str3 = rBBINode2.fText;
            this.fCachedSetLookup = null;
            str2 = str3;
        }
        return str2.toCharArray();
    }

    @Override // ohos.global.icu.text.SymbolTable
    public UnicodeMatcher lookupMatcher(int i) {
        if (i != 65535) {
            return null;
        }
        UnicodeSet unicodeSet = this.fCachedSetLookup;
        this.fCachedSetLookup = null;
        return unicodeSet;
    }

    @Override // ohos.global.icu.text.SymbolTable
    public String parseReference(String str, ParsePosition parsePosition, int i) {
        int index = parsePosition.getIndex();
        int i2 = index;
        while (i2 < i) {
            int charAt = UTF16.charAt(str, i2);
            if ((i2 == index && !UCharacter.isUnicodeIdentifierStart(charAt)) || !UCharacter.isUnicodeIdentifierPart(charAt)) {
                break;
            }
            i2 += UTF16.getCharCount(charAt);
        }
        if (i2 == index) {
            return "";
        }
        parsePosition.setIndex(i2);
        return str.substring(index, i2);
    }

    /* access modifiers changed from: package-private */
    public RBBINode lookupNode(String str) {
        RBBISymbolTableEntry rBBISymbolTableEntry = this.fHashTable.get(str);
        if (rBBISymbolTableEntry != null) {
            return rBBISymbolTableEntry.val;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void addEntry(String str, RBBINode rBBINode) {
        if (this.fHashTable.get(str) != null) {
            this.fRuleScanner.error(66055);
            return;
        }
        RBBISymbolTableEntry rBBISymbolTableEntry = new RBBISymbolTableEntry();
        rBBISymbolTableEntry.key = str;
        rBBISymbolTableEntry.val = rBBINode;
        this.fHashTable.put(rBBISymbolTableEntry.key, rBBISymbolTableEntry);
    }

    /* access modifiers changed from: package-private */
    public void rbbiSymtablePrint() {
        System.out.print("Variable Definitions\nName               Node Val     String Val\n----------------------------------------------------------------------\n");
        RBBISymbolTableEntry[] rBBISymbolTableEntryArr = (RBBISymbolTableEntry[]) this.fHashTable.values().toArray(new RBBISymbolTableEntry[0]);
        for (RBBISymbolTableEntry rBBISymbolTableEntry : rBBISymbolTableEntryArr) {
            System.out.print("  " + rBBISymbolTableEntry.key + "  ");
            System.out.print("  " + rBBISymbolTableEntry.val + "  ");
            System.out.print(rBBISymbolTableEntry.val.fLeftChild.fText);
            System.out.print("\n");
        }
        System.out.println("\nParsed Variable Definitions\n");
        for (RBBISymbolTableEntry rBBISymbolTableEntry2 : rBBISymbolTableEntryArr) {
            System.out.print(rBBISymbolTableEntry2.key);
            rBBISymbolTableEntry2.val.fLeftChild.printTree(true);
            System.out.print("\n");
        }
    }
}
