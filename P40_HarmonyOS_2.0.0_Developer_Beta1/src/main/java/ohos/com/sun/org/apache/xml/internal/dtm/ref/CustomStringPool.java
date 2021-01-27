package ohos.com.sun.org.apache.xml.internal.dtm.ref;

import java.util.HashMap;
import java.util.Map;

public class CustomStringPool extends DTMStringPool {
    public static final int NULL = -1;
    final Map<String, Integer> m_stringToInt = new HashMap();

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMStringPool
    public void removeAllElements() {
        this.m_intToString.removeAllElements();
        Map<String, Integer> map = this.m_stringToInt;
        if (map != null) {
            map.clear();
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMStringPool
    public String indexToString(int i) throws ArrayIndexOutOfBoundsException {
        return (String) this.m_intToString.elementAt(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMStringPool
    public int stringToIndex(String str) {
        if (str == null) {
            return -1;
        }
        Integer num = this.m_stringToInt.get(str);
        if (num == null) {
            this.m_intToString.addElement(str);
            num = Integer.valueOf(this.m_intToString.size());
            this.m_stringToInt.put(str, num);
        }
        return num.intValue();
    }
}
