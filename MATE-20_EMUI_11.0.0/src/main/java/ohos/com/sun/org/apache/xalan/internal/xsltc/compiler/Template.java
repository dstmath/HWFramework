package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.List;
import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NamedMethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xml.internal.utils.XML11Char;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;

public final class Template extends TopLevelElement {
    private boolean _compiled = false;
    private boolean _disabled = false;
    private boolean _isSimpleNamedTemplate = false;
    private QName _mode;
    private QName _name;
    private Vector<Param> _parameters = new Vector<>();
    private Pattern _pattern;
    private int _position;
    private double _priority;
    private boolean _simplified = false;
    private Stylesheet _stylesheet = null;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement
    public /* bridge */ /* synthetic */ void addDependency(TopLevelElement topLevelElement) {
        super.addDependency(topLevelElement);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement
    public /* bridge */ /* synthetic */ InstructionList compile(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        return super.compile(classGenerator, methodGenerator);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement
    public /* bridge */ /* synthetic */ Vector getDependencies() {
        return super.getDependencies();
    }

    public boolean hasParams() {
        return this._parameters.size() > 0;
    }

    public boolean isSimplified() {
        return this._simplified;
    }

    public void setSimplified() {
        this._simplified = true;
    }

    public boolean isSimpleNamedTemplate() {
        return this._isSimpleNamedTemplate;
    }

    public void addParameter(Param param) {
        this._parameters.addElement(param);
    }

    public Vector<Param> getParameters() {
        return this._parameters;
    }

    public void disable() {
        this._disabled = true;
    }

    public boolean disabled() {
        return this._disabled;
    }

    public double getPriority() {
        return this._priority;
    }

    public int getPosition() {
        return this._position;
    }

    public boolean isNamed() {
        return this._name != null;
    }

    public Pattern getPattern() {
        return this._pattern;
    }

    public QName getName() {
        return this._name;
    }

    public void setName(QName qName) {
        if (this._name == null) {
            this._name = qName;
        }
    }

    public QName getModeName() {
        return this._mode;
    }

    public int compareTo(Object obj) {
        Template template = (Template) obj;
        double d = this._priority;
        double d2 = template._priority;
        if (d > d2) {
            return 1;
        }
        if (d < d2) {
            return -1;
        }
        int i = this._position;
        int i2 = template._position;
        if (i > i2) {
            return 1;
        }
        if (i < i2) {
            return -1;
        }
        return 0;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        Util.println('\n');
        indent(i);
        if (this._name != null) {
            indent(i);
            Util.println("name = " + this._name);
        } else if (this._pattern != null) {
            indent(i);
            Util.println("match = " + this._pattern.toString());
        }
        if (this._mode != null) {
            indent(i);
            Util.println("mode = " + this._mode);
        }
        displayContents(i + 4);
    }

    private boolean resolveNamedTemplates(Template template, Parser parser) {
        if (template == null) {
            return true;
        }
        SymbolTable symbolTable = parser.getSymbolTable();
        int importPrecedence = getImportPrecedence();
        int importPrecedence2 = template.getImportPrecedence();
        if (importPrecedence > importPrecedence2) {
            template.disable();
            return true;
        } else if (importPrecedence >= importPrecedence2) {
            return false;
        } else {
            symbolTable.addTemplate(template);
            disable();
            return true;
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Stylesheet getStylesheet() {
        return this._stylesheet;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        String attribute = getAttribute("name");
        String attribute2 = getAttribute(Constants.ATTRNAME_MODE);
        String attribute3 = getAttribute(Constants.ATTRNAME_MATCH);
        String attribute4 = getAttribute(Constants.ATTRNAME_PRIORITY);
        this._stylesheet = super.getStylesheet();
        if (attribute.length() > 0) {
            if (!XML11Char.isXML11ValidQName(attribute)) {
                parser.reportError(3, new ErrorMsg("INVALID_QNAME_ERR", (Object) attribute, (SyntaxTreeNode) this));
            }
            this._name = parser.getQNameIgnoreDefaultNs(attribute);
        }
        if (attribute2.length() > 0) {
            if (!XML11Char.isXML11ValidQName(attribute2)) {
                parser.reportError(3, new ErrorMsg("INVALID_QNAME_ERR", (Object) attribute2, (SyntaxTreeNode) this));
            }
            this._mode = parser.getQNameIgnoreDefaultNs(attribute2);
        }
        if (attribute3.length() > 0) {
            this._pattern = parser.parsePattern(this, Constants.ATTRNAME_MATCH, null);
        }
        if (attribute4.length() > 0) {
            this._priority = Double.parseDouble(attribute4);
        } else {
            Pattern pattern = this._pattern;
            if (pattern != null) {
                this._priority = pattern.getPriority();
            } else {
                this._priority = Double.NaN;
            }
        }
        this._position = parser.getTemplateIndex();
        if (this._name != null) {
            if (!resolveNamedTemplates(parser.getSymbolTable().addTemplate(this), parser)) {
                parser.reportError(3, new ErrorMsg(ErrorMsg.TEMPLATE_REDEF_ERR, (Object) this._name, (SyntaxTreeNode) this));
            }
            if (this._pattern == null && this._mode == null) {
                this._isSimpleNamedTemplate = true;
            }
        }
        if (this._parent instanceof Stylesheet) {
            ((Stylesheet) this._parent).addTemplate(this);
        }
        parser.setTemplate(this);
        parseChildren(parser);
        parser.setTemplate(null);
    }

    public void parseSimplified(Stylesheet stylesheet, Parser parser) {
        this._stylesheet = stylesheet;
        setParent(stylesheet);
        this._name = null;
        this._mode = null;
        this._priority = Double.NaN;
        this._pattern = parser.parsePattern(this, PsuedoNames.PSEUDONAME_ROOT);
        List<SyntaxTreeNode> contents = this._stylesheet.getContents();
        SyntaxTreeNode syntaxTreeNode = contents.get(0);
        if (syntaxTreeNode instanceof LiteralElement) {
            addElement(syntaxTreeNode);
            syntaxTreeNode.setParent(this);
            contents.set(0, this);
            parser.setTemplate(this);
            syntaxTreeNode.parseContents(parser);
            parser.setTemplate(null);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Pattern pattern = this._pattern;
        if (pattern != null) {
            pattern.typeCheck(symbolTable);
        }
        return typeCheckContents(symbolTable);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (!this._disabled) {
            String className = classGenerator.getClassName();
            if (this._compiled && isNamed()) {
                String escape = Util.escape(this._name.toString());
                instructionList.append(classGenerator.loadTranslet());
                instructionList.append(methodGenerator.loadDOM());
                instructionList.append(methodGenerator.loadIterator());
                instructionList.append(methodGenerator.loadHandler());
                instructionList.append(methodGenerator.loadCurrentNode());
                instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(className, escape, Constants.ATTR_SET_SIG)));
            } else if (!this._compiled) {
                this._compiled = true;
                if (this._isSimpleNamedTemplate && (methodGenerator instanceof NamedMethodGenerator)) {
                    int size = this._parameters.size();
                    NamedMethodGenerator namedMethodGenerator = (NamedMethodGenerator) methodGenerator;
                    for (int i = 0; i < size; i++) {
                        Param elementAt = this._parameters.elementAt(i);
                        elementAt.setLoadInstruction(namedMethodGenerator.loadParameter(i));
                        elementAt.setStoreInstruction(namedMethodGenerator.storeParameter(i));
                    }
                }
                translateContents(classGenerator, methodGenerator);
                instructionList.setPositions(true);
            }
        }
    }
}
