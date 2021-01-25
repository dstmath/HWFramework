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

/* access modifiers changed from: package-private */
public final class Include extends TopLevelElement {
    private Stylesheet _included = null;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
    }

    Include() {
    }

    public Stylesheet getIncludedStylesheet() {
        return this._included;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        InputSource inputSource;
        SyntaxTreeNode syntaxTreeNode;
        XSLTC xsltc = parser.getXSLTC();
        Stylesheet currentStylesheet = parser.getCurrentStylesheet();
        String attribute = getAttribute(Constants.ATTRNAME_HREF);
        try {
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
            this._included = parser.makeStylesheet(syntaxTreeNode);
            if (this._included == null) {
                parser.setCurrentStylesheet(currentStylesheet);
                return;
            }
            this._included.setSourceLoader(sourceLoader);
            this._included.setSystemId(attribute);
            this._included.setParentStylesheet(currentStylesheet);
            this._included.setIncludingStylesheet(currentStylesheet);
            this._included.setTemplateInlining(currentStylesheet.getTemplateInlining());
            this._included.setImportPrecedence(currentStylesheet.getImportPrecedence());
            parser.setCurrentStylesheet(this._included);
            this._included.parseContents(parser);
            Iterator<SyntaxTreeNode> elements = this._included.elements();
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
