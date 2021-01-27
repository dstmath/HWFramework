package ohos.com.sun.org.apache.xml.internal.dtm.ref;

import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.utils.IntVector;

public class DTMStringPool {
    static final int HASHPRIME = 101;
    public static final int NULL = -1;
    IntVector m_hashChain;
    int[] m_hashStart;
    Vector m_intToString;

    public DTMStringPool(int i) {
        this.m_hashStart = new int[101];
        this.m_intToString = new Vector();
        this.m_hashChain = new IntVector(i);
        removeAllElements();
        stringToIndex("");
    }

    public DTMStringPool() {
        this(512);
    }

    public void removeAllElements() {
        this.m_intToString.removeAllElements();
        for (int i = 0; i < 101; i++) {
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

    public int stringToIndex(String str) {
        if (str == null) {
            return -1;
        }
        int hashCode = str.hashCode() % 101;
        if (hashCode < 0) {
            hashCode = -hashCode;
        }
        int i = this.m_hashStart[hashCode];
        int i2 = i;
        while (i != -1) {
            if (this.m_intToString.elementAt(i).equals(str)) {
                return i;
            }
            i2 = i;
            i = this.m_hashChain.elementAt(i);
        }
        int size = this.m_intToString.size();
        this.m_intToString.addElement(str);
        this.m_hashChain.addElement(-1);
        if (i2 == -1) {
            this.m_hashStart[hashCode] = size;
        } else {
            this.m_hashChain.setElementAt(size, i2);
        }
        return size;
    }

    public static void _main(String[] strArr) {
        String[] strArr2 = {"Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen", "Twenty", "Twenty-One", "Twenty-Two", "Twenty-Three", "Twenty-Four", "Twenty-Five", "Twenty-Six", "Twenty-Seven", "Twenty-Eight", "Twenty-Nine", "Thirty", "Thirty-One", "Thirty-Two", "Thirty-Three", "Thirty-Four", "Thirty-Five", "Thirty-Six", "Thirty-Seven", "Thirty-Eight", "Thirty-Nine"};
        DTMStringPool dTMStringPool = new DTMStringPool();
        System.out.println("If no complaints are printed below, we passed initial test.");
        for (int i = 0; i <= 1; i++) {
            for (int i2 = 0; i2 < strArr2.length; i2++) {
                int stringToIndex = dTMStringPool.stringToIndex(strArr2[i2]);
                if (stringToIndex != i2) {
                    System.out.println("\tMismatch populating pool: assigned " + stringToIndex + " for create " + i2);
                }
            }
            for (int i3 = 0; i3 < strArr2.length; i3++) {
                int stringToIndex2 = dTMStringPool.stringToIndex(strArr2[i3]);
                if (stringToIndex2 != i3) {
                    System.out.println("\tMismatch in stringToIndex: returned " + stringToIndex2 + " for lookup " + i3);
                }
            }
            for (int i4 = 0; i4 < strArr2.length; i4++) {
                String indexToString = dTMStringPool.indexToString(i4);
                if (!strArr2[i4].equals(indexToString)) {
                    System.out.println("\tMismatch in indexToString: returned" + indexToString + " for lookup " + i4);
                }
            }
            dTMStringPool.removeAllElements();
            System.out.println("\nPass " + i + " complete\n");
        }
    }
}
