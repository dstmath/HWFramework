package ohos.com.sun.org.apache.xml.internal.utils;

import java.util.HashMap;
import java.util.Map;

class ElemDesc {
    static final int ASPECIAL = 65536;
    static final int ATTREMPTY = 4;
    static final int ATTRURL = 2;
    static final int BLOCK = 8;
    static final int BLOCKFORM = 16;
    static final int BLOCKFORMFIELDSET = 32;
    static final int CDATA = 64;
    static final int EMPTY = 2;
    static final int FLOW = 4;
    static final int FONTSTYLE = 4096;
    static final int FORMCTRL = 16384;
    static final int HEAD = 262144;
    static final int HEADMISC = 131072;
    static final int INLINE = 512;
    static final int INLINEA = 1024;
    static final int INLINELABEL = 2048;
    static final int LIST = 524288;
    static final int PCDATA = 128;
    static final int PHRASE = 8192;
    static final int PREFORMATTED = 1048576;
    static final int RAW = 256;
    static final int SPECIAL = 32768;
    static final int WHITESPACESENSITIVE = 2097152;
    Map<String, Integer> m_attrs = null;
    int m_flags;

    ElemDesc(int i) {
        this.m_flags = i;
    }

    /* access modifiers changed from: package-private */
    public boolean is(int i) {
        return (this.m_flags & i) != 0;
    }

    /* access modifiers changed from: package-private */
    public void setAttr(String str, int i) {
        if (this.m_attrs == null) {
            this.m_attrs = new HashMap();
        }
        this.m_attrs.put(str, Integer.valueOf(i));
    }

    /* access modifiers changed from: package-private */
    public boolean isAttrFlagSet(String str, int i) {
        Integer num;
        Map<String, Integer> map = this.m_attrs;
        if (map == null || (num = map.get(str)) == null || (num.intValue() & i) == 0) {
            return false;
        }
        return true;
    }
}
