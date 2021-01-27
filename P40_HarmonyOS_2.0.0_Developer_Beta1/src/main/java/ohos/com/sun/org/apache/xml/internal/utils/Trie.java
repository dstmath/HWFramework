package ohos.com.sun.org.apache.xml.internal.utils;

public class Trie {
    public static final int ALPHA_SIZE = 128;
    Node m_Root = new Node();
    private char[] m_charBuffer = new char[0];

    public Object put(String str, Object obj) {
        int length = str.length();
        if (length > this.m_charBuffer.length) {
            this.m_charBuffer = new char[length];
        }
        Node node = this.m_Root;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            Node node2 = node.m_nextChar[Character.toUpperCase(str.charAt(i))];
            if (node2 != null) {
                i++;
                node = node2;
            } else {
                while (i < length) {
                    Node node3 = new Node();
                    node.m_nextChar[Character.toUpperCase(str.charAt(i))] = node3;
                    node.m_nextChar[Character.toLowerCase(str.charAt(i))] = node3;
                    i++;
                    node = node3;
                }
            }
        }
        Object obj2 = node.m_Value;
        node.m_Value = obj;
        return obj2;
    }

    public Object get(String str) {
        Node node;
        int length = str.length();
        char[] cArr = this.m_charBuffer;
        if (cArr.length < length) {
            return null;
        }
        Node node2 = this.m_Root;
        if (length != 0) {
            if (length != 1) {
                str.getChars(0, length, cArr, 0);
                for (int i = 0; i < length; i++) {
                    char c = this.m_charBuffer[i];
                    if (128 <= c || (node2 = node2.m_nextChar[c]) == null) {
                        return null;
                    }
                }
                return node2.m_Value;
            }
            char charAt = str.charAt(0);
            if (charAt < 128 && (node = node2.m_nextChar[charAt]) != null) {
                return node.m_Value;
            }
        }
        return null;
    }

    class Node {
        Object m_Value = null;
        Node[] m_nextChar = new Node[128];

        Node() {
        }
    }
}
