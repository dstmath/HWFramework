package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Iterator;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.XMLReader;

final class Import extends TopLevelElement {
    private Stylesheet _imported = null;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
    }

    Import() {
    }

    public Stylesheet getImportedStylesheet() {
        return this._imported;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        InputSource inputSource;
        SyntaxTreeNode syntaxTreeNode;
        XSLTC xsltc = parser.getXSLTC();
        Stylesheet currentStylesheet = parser.getCurrentStylesheet();
        try {
            String attribute = getAttribute(Constants.ATTRNAME_HREF);
            if (currentStylesheet.checkForLoop(attribute)) {
                parser.reportError(2, new ErrorMsg(ErrorMsg.CIRCULAR_INCLUDE_ERR, (Object) attribute, (SyntaxTreeNode) this));
                parser.setCurrentStylesheet(currentStylesheet);
                return;
            }
            String systemId = currentStylesheet.getSystemId();
            SourceLoader sourceLoader = currentStylesheet.getSourceLoader();
            XMLReader xMLReader = null;
            if (sourceLoader != null) {
                inputSource = sourceLoader.loadSource(attribute, systemId, xsltc);
                if (inputSource != null) {
                    attribute = inputSource.getSystemId();
                    xMLReader = xsltc.getXMLReader();
                } else if (parser.errorsFound()) {
                    parser.setCurrentStylesheet(currentStylesheet);
                    return;
                }
            } else {
                inputSource = null;
            }
            if (inputSource == null) {
                attribute = SystemIDResolver.getAbsoluteURI(attribute, systemId);
                String checkAccess = SecuritySupport.checkAccess(attribute, (String) xsltc.getProperty("http://ohos.javax.xml.XMLConstants/property/accessExternalStylesheet"), "all");
                if (checkAccess != null) {
                    parser.reportError(2, new ErrorMsg(ErrorMsg.ACCESSING_XSLT_TARGET_ERR, SecuritySupport.sanitizePath(attribute), checkAccess, this));
                    parser.setCurrentStylesheet(currentStylesheet);
                    return;
                }
                inputSource = new InputSource(attribute);
            }
            if (xMLReader != null) {
                syntaxTreeNode = parser.parse(xMLReader, inputSource);
            } else {
                syntaxTreeNode = parser.parse(inputSource);
            }
            if (syntaxTreeNode == null) {
                parser.setCurrentStylesheet(currentStylesheet);
                return;
            }
            this._imported = parser.makeStylesheet(syntaxTreeNode);
            if (this._imported == null) {
                parser.setCurrentStylesheet(currentStylesheet);
                return;
            }
            this._imported.setSourceLoader(sourceLoader);
            this._imported.setSystemId(attribute);
            this._imported.setParentStylesheet(currentStylesheet);
            this._imported.setImportingStylesheet(currentStylesheet);
            this._imported.setTemplateInlining(currentStylesheet.getTemplateInlining());
            int currentImportPrecedence = parser.getCurrentImportPrecedence();
            int nextImportPrecedence = parser.getNextImportPrecedence();
            this._imported.setImportPrecedence(currentImportPrecedence);
            currentStylesheet.setImportPrecedence(nextImportPrecedence);
            parser.setCurrentStylesheet(this._imported);
            this._imported.parseContents(parser);
            Iterator<SyntaxTreeNode> elements = this._imported.elements();
            Stylesheet topLevelStylesheet = parser.getTopLevelStylesheet();
            while (elements.hasNext()) {
                SyntaxTreeNode next = elements.next();
                if (next instanceof TopLevelElement) {
                    if (next instanceof Variable) {
                        topLevelStylesheet.addVariable((Variable) next);
                    } else if (next instanceof Param) {
                        topLevelStylesheet.addParam((Param) next);
                    } else {
                        topLevelStylesheet.addElement((TopLevelElement) next);
                    }
                }
            }
            parser.setCurrentStylesheet(currentStylesheet);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            parser.setCurrentStylesheet(currentStylesheet);
            throw th;
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        return Type.Void;
    }
}
