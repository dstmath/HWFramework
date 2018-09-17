package org.xml.sax.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.xml.XMLConstants;

public class NamespaceSupport {
    private static final Enumeration EMPTY_ENUMERATION = Collections.enumeration(Collections.emptyList());
    public static final String NSDECL = "http://www.w3.org/xmlns/2000/";
    public static final String XMLNS = "http://www.w3.org/XML/1998/namespace";
    private int contextPos;
    private Context[] contexts;
    private Context currentContext;
    private boolean namespaceDeclUris;

    final class Context {
        Hashtable attributeNameTable;
        private boolean declSeen = false;
        private ArrayList<String> declarations = null;
        boolean declsOK = true;
        String defaultNS = null;
        Hashtable elementNameTable;
        private Context parent = null;
        Hashtable prefixTable;
        Hashtable uriTable;

        Context() {
            copyTables();
        }

        void setParent(Context parent) {
            this.parent = parent;
            this.declarations = null;
            this.prefixTable = parent.prefixTable;
            this.uriTable = parent.uriTable;
            this.elementNameTable = parent.elementNameTable;
            this.attributeNameTable = parent.attributeNameTable;
            this.defaultNS = parent.defaultNS;
            this.declSeen = false;
            this.declsOK = true;
        }

        void clear() {
            this.parent = null;
            this.prefixTable = null;
            this.uriTable = null;
            this.elementNameTable = null;
            this.attributeNameTable = null;
            this.defaultNS = null;
        }

        void declarePrefix(String prefix, String uri) {
            if (this.declsOK) {
                if (!this.declSeen) {
                    copyTables();
                }
                if (this.declarations == null) {
                    this.declarations = new ArrayList();
                }
                prefix = prefix.intern();
                uri = uri.intern();
                if (!"".equals(prefix)) {
                    this.prefixTable.put(prefix, uri);
                    this.uriTable.put(uri, prefix);
                } else if ("".equals(uri)) {
                    this.defaultNS = null;
                } else {
                    this.defaultNS = uri;
                }
                this.declarations.add(prefix);
                return;
            }
            throw new IllegalStateException("can't declare any more prefixes in this context");
        }

        String[] processName(String qName, boolean isAttribute) {
            Hashtable table;
            this.declsOK = false;
            if (isAttribute) {
                table = this.attributeNameTable;
            } else {
                table = this.elementNameTable;
            }
            String[] name = (String[]) table.get(qName);
            if (name != null) {
                return name;
            }
            name = new String[3];
            name[2] = qName.intern();
            int index = qName.indexOf(58);
            if (index == -1) {
                if (isAttribute) {
                    if (qName == XMLConstants.XMLNS_ATTRIBUTE && NamespaceSupport.this.namespaceDeclUris) {
                        name[0] = NamespaceSupport.NSDECL;
                    } else {
                        name[0] = "";
                    }
                } else if (this.defaultNS == null) {
                    name[0] = "";
                } else {
                    name[0] = this.defaultNS;
                }
                name[1] = name[2];
            } else {
                String uri;
                String prefix = qName.substring(0, index);
                String local = qName.substring(index + 1);
                if ("".equals(prefix)) {
                    uri = this.defaultNS;
                } else {
                    uri = (String) this.prefixTable.get(prefix);
                }
                if (uri == null || (!isAttribute && XMLConstants.XMLNS_ATTRIBUTE.equals(prefix))) {
                    return null;
                }
                name[0] = uri;
                name[1] = local.intern();
            }
            table.put(name[2], name);
            return name;
        }

        String getURI(String prefix) {
            if ("".equals(prefix)) {
                return this.defaultNS;
            }
            if (this.prefixTable == null) {
                return null;
            }
            return (String) this.prefixTable.get(prefix);
        }

        String getPrefix(String uri) {
            if (this.uriTable == null) {
                return null;
            }
            return (String) this.uriTable.get(uri);
        }

        Enumeration getDeclaredPrefixes() {
            return this.declarations == null ? NamespaceSupport.EMPTY_ENUMERATION : Collections.enumeration(this.declarations);
        }

        Enumeration getPrefixes() {
            if (this.prefixTable == null) {
                return NamespaceSupport.EMPTY_ENUMERATION;
            }
            return this.prefixTable.keys();
        }

        private void copyTables() {
            if (this.prefixTable != null) {
                this.prefixTable = (Hashtable) this.prefixTable.clone();
            } else {
                this.prefixTable = new Hashtable();
            }
            if (this.uriTable != null) {
                this.uriTable = (Hashtable) this.uriTable.clone();
            } else {
                this.uriTable = new Hashtable();
            }
            this.elementNameTable = new Hashtable();
            this.attributeNameTable = new Hashtable();
            this.declSeen = true;
        }
    }

    public NamespaceSupport() {
        reset();
    }

    public void reset() {
        this.contexts = new Context[32];
        this.namespaceDeclUris = false;
        this.contextPos = 0;
        Context[] contextArr = this.contexts;
        int i = this.contextPos;
        Context context = new Context();
        this.currentContext = context;
        contextArr[i] = context;
        this.currentContext.declarePrefix(XMLConstants.XML_NS_PREFIX, "http://www.w3.org/XML/1998/namespace");
    }

    public void pushContext() {
        int max = this.contexts.length;
        this.contexts[this.contextPos].declsOK = false;
        this.contextPos++;
        if (this.contextPos >= max) {
            Context[] newContexts = new Context[(max * 2)];
            System.arraycopy(this.contexts, 0, newContexts, 0, max);
            max *= 2;
            this.contexts = newContexts;
        }
        this.currentContext = this.contexts[this.contextPos];
        if (this.currentContext == null) {
            Context[] contextArr = this.contexts;
            int i = this.contextPos;
            Context context = new Context();
            this.currentContext = context;
            contextArr[i] = context;
        }
        if (this.contextPos > 0) {
            this.currentContext.setParent(this.contexts[this.contextPos - 1]);
        }
    }

    public void popContext() {
        this.contexts[this.contextPos].clear();
        this.contextPos--;
        if (this.contextPos < 0) {
            throw new EmptyStackException();
        }
        this.currentContext = this.contexts[this.contextPos];
    }

    public boolean declarePrefix(String prefix, String uri) {
        if (prefix.equals(XMLConstants.XML_NS_PREFIX) || prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            return false;
        }
        this.currentContext.declarePrefix(prefix, uri);
        return true;
    }

    public String[] processName(String qName, String[] parts, boolean isAttribute) {
        String[] myParts = this.currentContext.processName(qName, isAttribute);
        if (myParts == null) {
            return null;
        }
        parts[0] = myParts[0];
        parts[1] = myParts[1];
        parts[2] = myParts[2];
        return parts;
    }

    public String getURI(String prefix) {
        return this.currentContext.getURI(prefix);
    }

    public Enumeration getPrefixes() {
        return this.currentContext.getPrefixes();
    }

    public String getPrefix(String uri) {
        return this.currentContext.getPrefix(uri);
    }

    public Enumeration getPrefixes(String uri) {
        ArrayList<String> prefixes = new ArrayList();
        Enumeration allPrefixes = getPrefixes();
        while (allPrefixes.hasMoreElements()) {
            String prefix = (String) allPrefixes.nextElement();
            if (uri.equals(getURI(prefix))) {
                prefixes.add(prefix);
            }
        }
        return Collections.enumeration(prefixes);
    }

    public Enumeration getDeclaredPrefixes() {
        return this.currentContext.getDeclaredPrefixes();
    }

    public void setNamespaceDeclUris(boolean value) {
        if (this.contextPos != 0) {
            throw new IllegalStateException();
        } else if (value != this.namespaceDeclUris) {
            this.namespaceDeclUris = value;
            if (value) {
                this.currentContext.declarePrefix(XMLConstants.XMLNS_ATTRIBUTE, NSDECL);
            } else {
                Context[] contextArr = this.contexts;
                int i = this.contextPos;
                Context context = new Context();
                this.currentContext = context;
                contextArr[i] = context;
                this.currentContext.declarePrefix(XMLConstants.XML_NS_PREFIX, "http://www.w3.org/XML/1998/namespace");
            }
        }
    }

    public boolean isNamespaceDeclUris() {
        return this.namespaceDeclUris;
    }
}
