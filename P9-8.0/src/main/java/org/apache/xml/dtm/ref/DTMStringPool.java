package org.apache.xml.dtm.ref;

import java.util.Vector;
import org.apache.xml.utils.IntVector;

public class DTMStringPool {
    static final int HASHPRIME = 101;
    public static final int NULL = -1;
    IntVector m_hashChain;
    int[] m_hashStart;
    Vector m_intToString;

    public DTMStringPool(int chainSize) {
        this.m_hashStart = new int[HASHPRIME];
        this.m_intToString = new Vector();
        this.m_hashChain = new IntVector(chainSize);
        removeAllElements();
        stringToIndex("");
    }

    public DTMStringPool() {
        this(512);
    }

    public void removeAllElements() {
        this.m_intToString.removeAllElements();
        for (int i = 0; i < HASHPRIME; i++) {
            this.m_hashStart[i] = -1;
        }
        this.m_hashChain.removeAllElements();
    }

    public String indexToString(int i) throws ArrayIndexOutOfBoundsException {
        if (i == -1) {
            return null;
        }
        return (String) this.m_intToString.elementAt(i);
    }

    public int stringToIndex(String s) {
        if (s == null) {
            return -1;
        }
        int hashslot = s.hashCode() % HASHPRIME;
        if (hashslot < 0) {
            hashslot = -hashslot;
        }
        int hashlast = this.m_hashStart[hashslot];
        int hashcandidate = hashlast;
        while (hashcandidate != -1) {
            if (this.m_intToString.elementAt(hashcandidate).equals(s)) {
                return hashcandidate;
            }
            hashlast = hashcandidate;
            hashcandidate = this.m_hashChain.elementAt(hashcandidate);
        }
        int newIndex = this.m_intToString.size();
        this.m_intToString.addElement(s);
        this.m_hashChain.addElement(-1);
        if (hashlast == -1) {
            this.m_hashStart[hashslot] = newIndex;
        } else {
            this.m_hashChain.setElementAt(newIndex, hashlast);
        }
        return newIndex;
    }

    public static void main(String[] args) {
        String[] word = new String[]{"Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen", "Twenty", "Twenty-One", "Twenty-Two", "Twenty-Three", "Twenty-Four", "Twenty-Five", "Twenty-Six", "Twenty-Seven", "Twenty-Eight", "Twenty-Nine", "Thirty", "Thirty-One", "Thirty-Two", "Thirty-Three", "Thirty-Four", "Thirty-Five", "Thirty-Six", "Thirty-Seven", "Thirty-Eight", "Thirty-Nine"};
        DTMStringPool pool = new DTMStringPool();
        System.out.println("If no complaints are printed below, we passed initial test.");
        for (int pass = 0; pass <= 1; pass++) {
            int i;
            int j;
            for (i = 0; i < word.length; i++) {
                j = pool.stringToIndex(word[i]);
                if (j != i) {
                    System.out.println("\tMismatch populating pool: assigned " + j + " for create " + i);
                }
            }
            for (i = 0; i < word.length; i++) {
                j = pool.stringToIndex(word[i]);
                if (j != i) {
                    System.out.println("\tMismatch in stringToIndex: returned " + j + " for lookup " + i);
                }
            }
            for (i = 0; i < word.length; i++) {
                String w = pool.indexToString(i);
                if (!word[i].equals(w)) {
                    System.out.println("\tMismatch in indexToString: returned" + w + " for lookup " + i);
                }
            }
            pool.removeAllElements();
            System.out.println("\nPass " + pass + " complete\n");
        }
    }
}
