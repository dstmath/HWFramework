package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodType;

/* access modifiers changed from: package-private */
public final class SymbolTable {
    private Map<String, String> _aliases = null;
    private Map<String, AttributeSet> _attributeSets = null;
    private SyntaxTreeNode _current = null;
    private Map<String, DecimalFormatting> _decimalFormats = null;
    private Map<String, Integer> _excludedURI = null;
    private Stack<Map<String, Integer>> _excludedURIStack = null;
    private Map<String, Key> _keys = null;
    private int _nsCounter = 0;
    private final Map<String, Vector> _primops = new HashMap();
    private final Map<String, Stylesheet> _stylesheets = new HashMap();
    private Map<String, Template> _templates = null;
    private Map<String, VariableBase> _variables = null;

    SymbolTable() {
    }

    public DecimalFormatting getDecimalFormatting(QName qName) {
        Map<String, DecimalFormatting> map = this._decimalFormats;
        if (map == null) {
            return null;
        }
        return map.get(qName.getStringRep());
    }

    public void addDecimalFormatting(QName qName, DecimalFormatting decimalFormatting) {
        if (this._decimalFormats == null) {
            this._decimalFormats = new HashMap();
        }
        this._decimalFormats.put(qName.getStringRep(), decimalFormatting);
    }

    public Key getKey(QName qName) {
        Map<String, Key> map = this._keys;
        if (map == null) {
            return null;
        }
        return map.get(qName.getStringRep());
    }

    public void addKey(QName qName, Key key) {
        if (this._keys == null) {
            this._keys = new HashMap();
        }
        this._keys.put(qName.getStringRep(), key);
    }

    public Stylesheet addStylesheet(QName qName, Stylesheet stylesheet) {
        return this._stylesheets.put(qName.getStringRep(), stylesheet);
    }

    public Stylesheet lookupStylesheet(QName qName) {
        return this._stylesheets.get(qName.getStringRep());
    }

    public Template addTemplate(Template template) {
        QName name = template.getName();
        if (this._templates == null) {
            this._templates = new HashMap();
        }
        return this._templates.put(name.getStringRep(), template);
    }

    public Template lookupTemplate(QName qName) {
        Map<String, Template> map = this._templates;
        if (map == null) {
            return null;
        }
        return map.get(qName.getStringRep());
    }

    public Variable addVariable(Variable variable) {
        if (this._variables == null) {
            this._variables = new HashMap();
        }
        return (Variable) this._variables.put(variable.getName().getStringRep(), variable);
    }

    public Param addParam(Param param) {
        if (this._variables == null) {
            this._variables = new HashMap();
        }
        return (Param) this._variables.put(param.getName().getStringRep(), param);
    }

    public Variable lookupVariable(QName qName) {
        if (this._variables == null) {
            return null;
        }
        VariableBase variableBase = this._variables.get(qName.getStringRep());
        if (variableBase instanceof Variable) {
            return (Variable) variableBase;
        }
        return null;
    }

    public Param lookupParam(QName qName) {
        if (this._variables == null) {
            return null;
        }
        VariableBase variableBase = this._variables.get(qName.getStringRep());
        if (variableBase instanceof Param) {
            return (Param) variableBase;
        }
        return null;
    }

    public SyntaxTreeNode lookupName(QName qName) {
        if (this._variables == null) {
            return null;
        }
        return this._variables.get(qName.getStringRep());
    }

    public AttributeSet addAttributeSet(AttributeSet attributeSet) {
        if (this._attributeSets == null) {
            this._attributeSets = new HashMap();
        }
        return this._attributeSets.put(attributeSet.getName().getStringRep(), attributeSet);
    }

    public AttributeSet lookupAttributeSet(QName qName) {
        Map<String, AttributeSet> map = this._attributeSets;
        if (map == null) {
            return null;
        }
        return map.get(qName.getStringRep());
    }

    public void addPrimop(String str, MethodType methodType) {
        Vector vector = this._primops.get(str);
        if (vector == null) {
            Map<String, Vector> map = this._primops;
            vector = new Vector();
            map.put(str, vector);
        }
        vector.addElement(methodType);
    }

    public Vector lookupPrimop(String str) {
        return this._primops.get(str);
    }

    public String generateNamespacePrefix() {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.ATTRNAME_NS);
        int i = this._nsCounter;
        this._nsCounter = i + 1;
        sb.append(i);
        return sb.toString();
    }

    public void setCurrentNode(SyntaxTreeNode syntaxTreeNode) {
        this._current = syntaxTreeNode;
    }

    public String lookupNamespace(String str) {
        SyntaxTreeNode syntaxTreeNode = this._current;
        if (syntaxTreeNode == null) {
            return "";
        }
        return syntaxTreeNode.lookupNamespace(str);
    }

    public void addPrefixAlias(String str, String str2) {
        if (this._aliases == null) {
            this._aliases = new HashMap();
        }
        this._aliases.put(str, str2);
    }

    public String lookupPrefixAlias(String str) {
        Map<String, String> map = this._aliases;
        if (map == null) {
            return null;
        }
        return map.get(str);
    }

    public void excludeURI(String str) {
        int i;
        if (str != null) {
            if (this._excludedURI == null) {
                this._excludedURI = new HashMap();
            }
            Integer num = this._excludedURI.get(str);
            if (num == null) {
                i = 1;
            } else {
                i = Integer.valueOf(num.intValue() + 1);
            }
            this._excludedURI.put(str, i);
        }
    }

    public void excludeNamespaces(String str) {
        String str2;
        if (str != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(str);
            while (stringTokenizer.hasMoreTokens()) {
                String nextToken = stringTokenizer.nextToken();
                if (nextToken.equals("#default")) {
                    str2 = lookupNamespace("");
                } else {
                    str2 = lookupNamespace(nextToken);
                }
                if (str2 != null) {
                    excludeURI(str2);
                }
            }
        }
    }

    public boolean isExcludedNamespace(String str) {
        Map<String, Integer> map;
        Integer num;
        if (str == null || (map = this._excludedURI) == null || (num = map.get(str)) == null || num.intValue() <= 0) {
            return false;
        }
        return true;
    }

    public void unExcludeNamespaces(String str) {
        String str2;
        if (this._excludedURI != null && str != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(str);
            while (stringTokenizer.hasMoreTokens()) {
                String nextToken = stringTokenizer.nextToken();
                if (nextToken.equals("#default")) {
                    str2 = lookupNamespace("");
                } else {
                    str2 = lookupNamespace(nextToken);
                }
                Integer num = this._excludedURI.get(str2);
                if (num != null) {
                    this._excludedURI.put(str2, Integer.valueOf(num.intValue() - 1));
                }
            }
        }
    }

    public void pushExcludedNamespacesContext() {
        if (this._excludedURIStack == null) {
            this._excludedURIStack = new Stack<>();
        }
        this._excludedURIStack.push(this._excludedURI);
        this._excludedURI = null;
    }

    public void popExcludedNamespacesContext() {
        this._excludedURI = this._excludedURIStack.pop();
        if (this._excludedURIStack.isEmpty()) {
            this._excludedURIStack = null;
        }
    }
}
