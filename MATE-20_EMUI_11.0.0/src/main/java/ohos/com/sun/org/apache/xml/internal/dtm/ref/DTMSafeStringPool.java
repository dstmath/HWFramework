package ohos.com.sun.org.apache.xml.internal.dtm.ref;

public class DTMSafeStringPool extends DTMStringPool {
    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMStringPool
    public synchronized void removeAllElements() {
        super.removeAllElements();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMStringPool
    public synchronized String indexToString(int i) throws ArrayIndexOutOfBoundsException {
        return super.indexToString(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMStringPool
    public synchronized int stringToIndex(String str) {
        return super.stringToIndex(str);
    }

    public static void _main(String[] strArr) {
        String[] strArr2 = {"Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen", "Twenty", "Twenty-One", "Twenty-Two", "Twenty-Three", "Twenty-Four", "Twenty-Five", "Twenty-Six", "Twenty-Seven", "Twenty-Eight", "Twenty-Nine", "Thirty", "Thirty-One", "Thirty-Two", "Thirty-Three", "Thirty-Four", "Thirty-Five", "Thirty-Six", "Thirty-Seven", "Thirty-Eight", "Thirty-Nine"};
        DTMSafeStringPool dTMSafeStringPool = new DTMSafeStringPool();
        System.out.println("If no complaints are printed below, we passed initial test.");
        for (int i = 0; i <= 1; i++) {
            for (int i2 = 0; i2 < strArr2.length; i2++) {
                int stringToIndex = dTMSafeStringPool.stringToIndex(strArr2[i2]);
                if (stringToIndex != i2) {
                    System.out.println("\tMismatch populating pool: assigned " + stringToIndex + " for create " + i2);
                }
            }
            for (int i3 = 0; i3 < strArr2.length; i3++) {
                int stringToIndex2 = dTMSafeStringPool.stringToIndex(strArr2[i3]);
                if (stringToIndex2 != i3) {
                    System.out.println("\tMismatch in stringToIndex: returned " + stringToIndex2 + " for lookup " + i3);
                }
            }
            for (int i4 = 0; i4 < strArr2.length; i4++) {
                String indexToString = dTMSafeStringPool.indexToString(i4);
                if (!strArr2[i4].equals(indexToString)) {
                    System.out.println("\tMismatch in indexToString: returned" + indexToString + " for lookup " + i4);
                }
            }
            dTMSafeStringPool.removeAllElements();
            System.out.println("\nPass " + i + " complete\n");
        }
    }
}
