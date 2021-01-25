package ohos.com.sun.org.apache.xml.internal.serializer;

import java.util.HashMap;
import java.util.Stack;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.SAXException;

public class NamespaceMappings {
    private static final String EMPTYSTRING = "";
    private static final String XML_PREFIX = "xml";
    private int count;
    private HashMap m_namespaces = new HashMap();
    private Stack m_nodeStack = new Stack();

    public NamespaceMappings() {
        initNamespaces();
    }

    private void initNamespaces() {
        HashMap hashMap = this.m_namespaces;
        Stack stack = new Stack();
        hashMap.put("", stack);
        stack.push(new MappingRecord("", "", 0));
        HashMap hashMap2 = this.m_namespaces;
        Stack stack2 = new Stack();
        hashMap2.put("xml", stack2);
        stack2.push(new MappingRecord("xml", "http://www.w3.org/XML/1998/namespace", 0));
        this.m_nodeStack.push(new MappingRecord(null, null, -1));
    }

    public String lookupNamespace(String str) {
        Stack stack = (Stack) this.m_namespaces.get(str);
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return ((MappingRecord) stack.peek()).m_uri;
    }

    /* access modifiers changed from: package-private */
    public MappingRecord getMappingFromPrefix(String str) {
        Stack stack = (Stack) this.m_namespaces.get(str);
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return (MappingRecord) stack.peek();
    }

    public String lookupPrefix(String str) {
        for (String str2 : this.m_namespaces.keySet()) {
            String lookupNamespace = lookupNamespace(str2);
            if (lookupNamespace != null && lookupNamespace.equals(str)) {
                return str2;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public MappingRecord getMappingFromURI(String str) {
        for (String str2 : this.m_namespaces.keySet()) {
            MappingRecord mappingFromPrefix = getMappingFromPrefix(str2);
            if (mappingFromPrefix != null && mappingFromPrefix.m_uri.equals(str)) {
                return mappingFromPrefix;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean popNamespace(String str) {
        Stack stack;
        if (str.startsWith("xml") || (stack = (Stack) this.m_namespaces.get(str)) == null) {
            return false;
        }
        stack.pop();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean pushNamespace(String str, String str2, int i) {
        if (str.startsWith("xml")) {
            return false;
        }
        Stack stack = (Stack) this.m_namespaces.get(str);
        if (stack == null) {
            HashMap hashMap = this.m_namespaces;
            Stack stack2 = new Stack();
            hashMap.put(str, stack2);
            stack = stack2;
        }
        if (!stack.empty() && str2.equals(((MappingRecord) stack.peek()).m_uri)) {
            return false;
        }
        MappingRecord mappingRecord = new MappingRecord(str, str2, i);
        stack.push(mappingRecord);
        this.m_nodeStack.push(mappingRecord);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void popNamespaces(int i, ContentHandler contentHandler) {
        while (!this.m_nodeStack.isEmpty() && ((MappingRecord) this.m_nodeStack.peek()).m_declarationDepth >= i) {
            String str = ((MappingRecord) this.m_nodeStack.pop()).m_prefix;
            popNamespace(str);
            if (contentHandler != null) {
                try {
                    contentHandler.endPrefixMapping(str);
                } catch (SAXException unused) {
                }
            }
        }
    }

    public String generateNextPrefix() {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.ATTRNAME_NS);
        int i = this.count;
        this.count = i + 1;
        sb.append(i);
        return sb.toString();
    }

    public Object clone() throws CloneNotSupportedException {
        NamespaceMappings namespaceMappings = new NamespaceMappings();
        namespaceMappings.m_nodeStack = (Stack) this.m_nodeStack.clone();
        namespaceMappings.m_namespaces = (HashMap) this.m_namespaces.clone();
        namespaceMappings.count = this.count;
        return namespaceMappings;
    }

    /* access modifiers changed from: package-private */
    public final void reset() {
        this.count = 0;
        this.m_namespaces.clear();
        this.m_nodeStack.clear();
        initNamespaces();
    }

    /* access modifiers changed from: package-private */
    public class MappingRecord {
        final int m_declarationDepth;
        final String m_prefix;
        final String m_uri;

        MappingRecord(String str, String str2, int i) {
            this.m_prefix = str;
            this.m_uri = str2;
            this.m_declarationDepth = i;
        }
    }
}
