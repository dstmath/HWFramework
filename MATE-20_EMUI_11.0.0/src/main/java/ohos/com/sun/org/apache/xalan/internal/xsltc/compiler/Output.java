package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.StringTokenizer;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.bcel.internal.generic.PUTFIELD;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xml.internal.serializer.Encodings;
import ohos.com.sun.org.apache.xml.internal.utils.XML11Char;

/* access modifiers changed from: package-private */
public final class Output extends TopLevelElement {
    private static final String HTML_VERSION = "4.0";
    private static final String STRING_SIG = "Ljava/lang/String;";
    private static final String XML_VERSION = "1.0";
    private String _cdata;
    private boolean _disabled = false;
    private String _doctypePublic;
    private String _doctypeSystem;
    private String _encoding;
    private boolean _indent = false;
    private String _indentamount;
    private String _mediaType;
    private String _method;
    private boolean _omitHeader = false;
    private String _standalone;
    private String _version;

    Output() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("Output " + this._method);
    }

    public void disable() {
        this._disabled = true;
    }

    public boolean enabled() {
        return !this._disabled;
    }

    public String getCdata() {
        return this._cdata;
    }

    public String getOutputMethod() {
        return this._method;
    }

    private void transferAttribute(Output output, String str) {
        if (!hasAttribute(str) && output.hasAttribute(str)) {
            addAttribute(str, output.getAttribute(str));
        }
    }

    public void mergeOutput(Output output) {
        transferAttribute(output, "version");
        transferAttribute(output, Constants.ATTRNAME_OUTPUT_METHOD);
        transferAttribute(output, Constants.ATTRNAME_OUTPUT_ENCODING);
        transferAttribute(output, Constants.ATTRNAME_OUTPUT_DOCTYPE_SYSTEM);
        transferAttribute(output, Constants.ATTRNAME_OUTPUT_DOCTYPE_PUBLIC);
        transferAttribute(output, Constants.ATTRNAME_OUTPUT_MEDIATYPE);
        transferAttribute(output, Constants.ATTRNAME_OUTPUT_INDENT);
        transferAttribute(output, Constants.ATTRNAME_OUTPUT_OMITXMLDECL);
        transferAttribute(output, Constants.ATTRNAME_OUTPUT_STANDALONE);
        if (output.hasAttribute(Constants.ATTRNAME_OUTPUT_CDATA_SECTION_ELEMENTS)) {
            addAttribute(Constants.ATTRNAME_OUTPUT_CDATA_SECTION_ELEMENTS, output.getAttribute(Constants.ATTRNAME_OUTPUT_CDATA_SECTION_ELEMENTS) + ' ' + getAttribute(Constants.ATTRNAME_OUTPUT_CDATA_SECTION_ELEMENTS));
        }
        String lookupPrefix = lookupPrefix("http://xml.apache.org/xalan");
        if (lookupPrefix != null) {
            transferAttribute(output, lookupPrefix + ":indent-amount");
        }
        String lookupPrefix2 = lookupPrefix(ohos.com.sun.org.apache.xml.internal.utils.Constants.S_BUILTIN_OLD_EXTENSIONS_URL);
        if (lookupPrefix2 != null) {
            transferAttribute(output, lookupPrefix2 + ":indent-amount");
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        Properties properties = new Properties();
        parser.setOutput(this);
        if (!this._disabled) {
            this._version = getAttribute("version");
            if (this._version.equals("")) {
                this._version = null;
            } else {
                properties.setProperty("version", this._version);
            }
            this._method = getAttribute(Constants.ATTRNAME_OUTPUT_METHOD);
            if (this._method.equals("")) {
                this._method = null;
            }
            String str = this._method;
            if (str != null) {
                this._method = str.toLowerCase();
                if (this._method.equals("xml") || this._method.equals("html") || this._method.equals("text") || (XML11Char.isXML11ValidQName(this._method) && this._method.indexOf(":") > 0)) {
                    properties.setProperty(Constants.ATTRNAME_OUTPUT_METHOD, this._method);
                } else {
                    reportError(this, parser, ErrorMsg.INVALID_METHOD_IN_OUTPUT, this._method);
                }
            }
            this._encoding = getAttribute(Constants.ATTRNAME_OUTPUT_ENCODING);
            if (this._encoding.equals("")) {
                this._encoding = null;
            } else {
                try {
                    new OutputStreamWriter(System.out, Encodings.convertMime2JavaEncoding(this._encoding));
                } catch (UnsupportedEncodingException unused) {
                    parser.reportError(4, new ErrorMsg(ErrorMsg.UNSUPPORTED_ENCODING, (Object) this._encoding, (SyntaxTreeNode) this));
                }
                properties.setProperty(Constants.ATTRNAME_OUTPUT_ENCODING, this._encoding);
            }
            String attribute = getAttribute(Constants.ATTRNAME_OUTPUT_OMITXMLDECL);
            if (!attribute.equals("")) {
                if (attribute.equals("yes")) {
                    this._omitHeader = true;
                }
                properties.setProperty(Constants.ATTRNAME_OUTPUT_OMITXMLDECL, attribute);
            }
            this._standalone = getAttribute(Constants.ATTRNAME_OUTPUT_STANDALONE);
            if (this._standalone.equals("")) {
                this._standalone = null;
            } else {
                properties.setProperty(Constants.ATTRNAME_OUTPUT_STANDALONE, this._standalone);
            }
            this._doctypeSystem = getAttribute(Constants.ATTRNAME_OUTPUT_DOCTYPE_SYSTEM);
            if (this._doctypeSystem.equals("")) {
                this._doctypeSystem = null;
            } else {
                properties.setProperty(Constants.ATTRNAME_OUTPUT_DOCTYPE_SYSTEM, this._doctypeSystem);
            }
            this._doctypePublic = getAttribute(Constants.ATTRNAME_OUTPUT_DOCTYPE_PUBLIC);
            if (this._doctypePublic.equals("")) {
                this._doctypePublic = null;
            } else {
                properties.setProperty(Constants.ATTRNAME_OUTPUT_DOCTYPE_PUBLIC, this._doctypePublic);
            }
            this._cdata = getAttribute(Constants.ATTRNAME_OUTPUT_CDATA_SECTION_ELEMENTS);
            if (this._cdata.equals("")) {
                this._cdata = null;
            } else {
                StringBuffer stringBuffer = new StringBuffer();
                StringTokenizer stringTokenizer = new StringTokenizer(this._cdata);
                while (stringTokenizer.hasMoreTokens()) {
                    String nextToken = stringTokenizer.nextToken();
                    if (!XML11Char.isXML11ValidQName(nextToken)) {
                        parser.reportError(3, new ErrorMsg("INVALID_QNAME_ERR", (Object) nextToken, (SyntaxTreeNode) this));
                    }
                    stringBuffer.append(parser.getQName(nextToken).toString());
                    stringBuffer.append(' ');
                }
                this._cdata = stringBuffer.toString();
                properties.setProperty(Constants.ATTRNAME_OUTPUT_CDATA_SECTION_ELEMENTS, this._cdata);
            }
            String attribute2 = getAttribute(Constants.ATTRNAME_OUTPUT_INDENT);
            if (!attribute2.equals("")) {
                if (attribute2.equals("yes")) {
                    this._indent = true;
                }
                properties.setProperty(Constants.ATTRNAME_OUTPUT_INDENT, attribute2);
            } else {
                String str2 = this._method;
                if (str2 != null && str2.equals("html")) {
                    this._indent = true;
                }
            }
            this._indentamount = getAttribute(lookupPrefix("http://xml.apache.org/xalan"), "indent-amount");
            if (this._indentamount.equals("")) {
                this._indentamount = getAttribute(lookupPrefix(ohos.com.sun.org.apache.xml.internal.utils.Constants.S_BUILTIN_OLD_EXTENSIONS_URL), "indent-amount");
            }
            if (!this._indentamount.equals("")) {
                properties.setProperty("indent_amount", this._indentamount);
            }
            this._mediaType = getAttribute(Constants.ATTRNAME_OUTPUT_MEDIATYPE);
            if (this._mediaType.equals("")) {
                this._mediaType = null;
            } else {
                properties.setProperty(Constants.ATTRNAME_OUTPUT_MEDIATYPE, this._mediaType);
            }
            String str3 = this._method;
            if (str3 != null) {
                if (str3.equals("html")) {
                    if (this._version == null) {
                        this._version = HTML_VERSION;
                    }
                    if (this._mediaType == null) {
                        this._mediaType = "text/html";
                    }
                } else if (this._method.equals("text") && this._mediaType == null) {
                    this._mediaType = "text/plain";
                }
            }
            parser.getCurrentStylesheet().setOutputProperties(properties);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        if (!this._disabled) {
            ConstantPoolGen constantPool = classGenerator.getConstantPool();
            InstructionList instructionList = methodGenerator.getInstructionList();
            instructionList.append(classGenerator.loadTranslet());
            String str = this._version;
            if (str != null && !str.equals("1.0")) {
                int addFieldref = constantPool.addFieldref(Constants.TRANSLET_CLASS, "_version", "Ljava/lang/String;");
                instructionList.append(DUP);
                instructionList.append(new PUSH(constantPool, this._version));
                instructionList.append(new PUTFIELD(addFieldref));
            }
            if (this._method != null) {
                int addFieldref2 = constantPool.addFieldref(Constants.TRANSLET_CLASS, "_method", "Ljava/lang/String;");
                instructionList.append(DUP);
                instructionList.append(new PUSH(constantPool, this._method));
                instructionList.append(new PUTFIELD(addFieldref2));
            }
            if (this._encoding != null) {
                int addFieldref3 = constantPool.addFieldref(Constants.TRANSLET_CLASS, "_encoding", "Ljava/lang/String;");
                instructionList.append(DUP);
                instructionList.append(new PUSH(constantPool, this._encoding));
                instructionList.append(new PUTFIELD(addFieldref3));
            }
            if (this._omitHeader) {
                int addFieldref4 = constantPool.addFieldref(Constants.TRANSLET_CLASS, "_omitHeader", Constants.HASIDCALL_INDEX_SIG);
                instructionList.append(DUP);
                instructionList.append(new PUSH(constantPool, this._omitHeader));
                instructionList.append(new PUTFIELD(addFieldref4));
            }
            if (this._standalone != null) {
                int addFieldref5 = constantPool.addFieldref(Constants.TRANSLET_CLASS, "_standalone", "Ljava/lang/String;");
                instructionList.append(DUP);
                instructionList.append(new PUSH(constantPool, this._standalone));
                instructionList.append(new PUTFIELD(addFieldref5));
            }
            int addFieldref6 = constantPool.addFieldref(Constants.TRANSLET_CLASS, "_doctypeSystem", "Ljava/lang/String;");
            instructionList.append(DUP);
            instructionList.append(new PUSH(constantPool, this._doctypeSystem));
            instructionList.append(new PUTFIELD(addFieldref6));
            int addFieldref7 = constantPool.addFieldref(Constants.TRANSLET_CLASS, "_doctypePublic", "Ljava/lang/String;");
            instructionList.append(DUP);
            instructionList.append(new PUSH(constantPool, this._doctypePublic));
            instructionList.append(new PUTFIELD(addFieldref7));
            if (this._mediaType != null) {
                int addFieldref8 = constantPool.addFieldref(Constants.TRANSLET_CLASS, "_mediaType", "Ljava/lang/String;");
                instructionList.append(DUP);
                instructionList.append(new PUSH(constantPool, this._mediaType));
                instructionList.append(new PUTFIELD(addFieldref8));
            }
            if (this._indent) {
                int addFieldref9 = constantPool.addFieldref(Constants.TRANSLET_CLASS, "_indent", Constants.HASIDCALL_INDEX_SIG);
                instructionList.append(DUP);
                instructionList.append(new PUSH(constantPool, this._indent));
                instructionList.append(new PUTFIELD(addFieldref9));
            }
            String str2 = this._indentamount;
            if (str2 != null && !str2.equals("")) {
                int addFieldref10 = constantPool.addFieldref(Constants.TRANSLET_CLASS, "_indentamount", "I");
                instructionList.append(DUP);
                instructionList.append(new PUSH(constantPool, Integer.parseInt(this._indentamount)));
                instructionList.append(new PUTFIELD(addFieldref10));
            }
            if (this._cdata != null) {
                int addMethodref = constantPool.addMethodref(Constants.TRANSLET_CLASS, "addCdataElement", "(Ljava/lang/String;)V");
                StringTokenizer stringTokenizer = new StringTokenizer(this._cdata);
                while (stringTokenizer.hasMoreTokens()) {
                    instructionList.append(DUP);
                    instructionList.append(new PUSH(constantPool, stringTokenizer.nextToken()));
                    instructionList.append(new INVOKEVIRTUAL(addMethodref));
                }
            }
            instructionList.append(POP);
        }
    }
}
