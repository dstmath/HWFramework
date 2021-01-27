package ohos.com.sun.org.apache.xml.internal.serialize;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;
import java.util.Vector;
import ohos.ai.asr.util.AsrConstants;
import ohos.com.sun.org.apache.xerces.internal.dom.AbortException;
import ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMErrorImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMLocatorImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMNormalizer;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMStringListImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import ohos.com.sun.org.apache.xerces.internal.util.DOMUtil;
import ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XML11Char;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializerConstants;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.Comment;
import ohos.org.w3c.dom.DOMConfiguration;
import ohos.org.w3c.dom.DOMErrorHandler;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.DOMStringList;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentFragment;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.ProcessingInstruction;
import ohos.org.w3c.dom.ls.LSException;
import ohos.org.w3c.dom.ls.LSOutput;
import ohos.org.w3c.dom.ls.LSSerializer;
import ohos.org.w3c.dom.ls.LSSerializerFilter;

public class DOMSerializerImpl implements LSSerializer, DOMConfiguration {
    protected static final short CDATA = 8;
    protected static final short COMMENTS = 32;
    protected static final short DISCARDDEFAULT = 64;
    protected static final short DOM_ELEMENT_CONTENT_WHITESPACE = 1024;
    protected static final short ENTITIES = 4;
    protected static final short FORMAT_PRETTY_PRINT = 2048;
    protected static final short INFOSET = 128;
    protected static final short NAMESPACES = 1;
    protected static final short NSDECL = 512;
    protected static final short SPLITCDATA = 16;
    protected static final short WELLFORMED = 2;
    protected static final short XMLDECL = 256;
    private final DOMErrorImpl fError;
    private DOMErrorHandler fErrorHandler;
    private final DOMLocatorImpl fLocator;
    private DOMStringList fRecognizedParameters;
    protected short features;
    private XMLSerializer serializer;
    private XML11Serializer xml11Serializer;

    public DOMConfiguration getDomConfig() {
        return this;
    }

    public DOMSerializerImpl() {
        this.features = 0;
        this.fErrorHandler = null;
        this.fError = new DOMErrorImpl();
        this.fLocator = new DOMLocatorImpl();
        this.features = (short) (this.features | 1);
        this.features = (short) (this.features | 4);
        this.features = (short) (this.features | 32);
        this.features = (short) (this.features | 8);
        this.features = (short) (this.features | 16);
        this.features = (short) (this.features | 2);
        this.features = (short) (this.features | 512);
        this.features = (short) (this.features | 1024);
        this.features = (short) (this.features | 64);
        this.features = (short) (this.features | 256);
        this.serializer = new XMLSerializer();
        initSerializer(this.serializer);
    }

    public void setParameter(String str, Object obj) throws DOMException {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        if (obj instanceof Boolean) {
            boolean booleanValue = ((Boolean) obj).booleanValue();
            if (str.equalsIgnoreCase(Constants.DOM_INFOSET)) {
                if (booleanValue) {
                    this.features = (short) (this.features & -5);
                    this.features = (short) (this.features & -9);
                    this.features = (short) (this.features | 1);
                    this.features = (short) (this.features | 512);
                    this.features = (short) (this.features | 2);
                    this.features = (short) (this.features | 32);
                }
            } else if (str.equalsIgnoreCase(Constants.DOM_XMLDECL)) {
                short s = this.features;
                this.features = (short) (booleanValue ? s | 256 : s & -257);
            } else if (str.equalsIgnoreCase("namespaces")) {
                if (booleanValue) {
                    i9 = this.features | 1;
                } else {
                    i9 = this.features & -2;
                }
                this.features = (short) i9;
                this.serializer.fNamespaces = booleanValue;
            } else if (str.equalsIgnoreCase(Constants.DOM_SPLIT_CDATA)) {
                if (booleanValue) {
                    i8 = this.features | 16;
                } else {
                    i8 = this.features & -17;
                }
                this.features = (short) i8;
            } else if (str.equalsIgnoreCase(Constants.DOM_DISCARD_DEFAULT_CONTENT)) {
                if (booleanValue) {
                    i7 = this.features | 64;
                } else {
                    i7 = this.features & -65;
                }
                this.features = (short) i7;
            } else if (str.equalsIgnoreCase(Constants.DOM_WELLFORMED)) {
                if (booleanValue) {
                    i6 = this.features | 2;
                } else {
                    i6 = this.features & -3;
                }
                this.features = (short) i6;
            } else if (str.equalsIgnoreCase(Constants.DOM_ENTITIES)) {
                if (booleanValue) {
                    i5 = this.features | 4;
                } else {
                    i5 = this.features & -5;
                }
                this.features = (short) i5;
            } else if (str.equalsIgnoreCase(Constants.DOM_CDATA_SECTIONS)) {
                if (booleanValue) {
                    i4 = this.features | 8;
                } else {
                    i4 = this.features & -9;
                }
                this.features = (short) i4;
            } else if (str.equalsIgnoreCase(Constants.DOM_COMMENTS)) {
                if (booleanValue) {
                    i3 = this.features | 32;
                } else {
                    i3 = this.features & -33;
                }
                this.features = (short) i3;
            } else if (str.equalsIgnoreCase(Constants.DOM_FORMAT_PRETTY_PRINT)) {
                if (booleanValue) {
                    i2 = this.features | 2048;
                } else {
                    i2 = this.features & -2049;
                }
                this.features = (short) i2;
            } else if (str.equalsIgnoreCase(Constants.DOM_CANONICAL_FORM) || str.equalsIgnoreCase(Constants.DOM_VALIDATE_IF_SCHEMA) || str.equalsIgnoreCase(Constants.DOM_VALIDATE) || str.equalsIgnoreCase(Constants.DOM_CHECK_CHAR_NORMALIZATION) || str.equalsIgnoreCase(Constants.DOM_DATATYPE_NORMALIZATION)) {
                if (booleanValue) {
                    throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_SUPPORTED", new Object[]{str}));
                }
            } else if (str.equalsIgnoreCase(Constants.DOM_NAMESPACE_DECLARATIONS)) {
                if (booleanValue) {
                    i = this.features | 512;
                } else {
                    i = this.features & -513;
                }
                this.features = (short) i;
                this.serializer.fNamespacePrefixes = booleanValue;
            } else if (!str.equalsIgnoreCase(Constants.DOM_ELEMENT_CONTENT_WHITESPACE) && !str.equalsIgnoreCase(Constants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS)) {
                throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_FOUND", new Object[]{str}));
            } else if (!booleanValue) {
                throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_SUPPORTED", new Object[]{str}));
            }
        } else if (str.equalsIgnoreCase(Constants.DOM_ERROR_HANDLER)) {
            if (obj == null || (obj instanceof DOMErrorHandler)) {
                this.fErrorHandler = (DOMErrorHandler) obj;
                return;
            }
            throw new DOMException(17, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "TYPE_MISMATCH_ERR", new Object[]{str}));
        } else if (str.equalsIgnoreCase(Constants.DOM_RESOURCE_RESOLVER) || str.equalsIgnoreCase(Constants.DOM_SCHEMA_LOCATION) || str.equalsIgnoreCase(Constants.DOM_SCHEMA_TYPE) || (str.equalsIgnoreCase(Constants.DOM_NORMALIZE_CHARACTERS) && obj != null)) {
            throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_SUPPORTED", new Object[]{str}));
        } else {
            throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_FOUND", new Object[]{str}));
        }
    }

    public boolean canSetParameter(String str, Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof Boolean) {
            boolean booleanValue = ((Boolean) obj).booleanValue();
            if (str.equalsIgnoreCase("namespaces") || str.equalsIgnoreCase(Constants.DOM_SPLIT_CDATA) || str.equalsIgnoreCase(Constants.DOM_DISCARD_DEFAULT_CONTENT) || str.equalsIgnoreCase(Constants.DOM_XMLDECL) || str.equalsIgnoreCase(Constants.DOM_WELLFORMED) || str.equalsIgnoreCase(Constants.DOM_INFOSET) || str.equalsIgnoreCase(Constants.DOM_ENTITIES) || str.equalsIgnoreCase(Constants.DOM_CDATA_SECTIONS) || str.equalsIgnoreCase(Constants.DOM_COMMENTS) || str.equalsIgnoreCase(Constants.DOM_NAMESPACE_DECLARATIONS) || str.equalsIgnoreCase(Constants.DOM_FORMAT_PRETTY_PRINT)) {
                return true;
            }
            if (str.equalsIgnoreCase(Constants.DOM_CANONICAL_FORM) || str.equalsIgnoreCase(Constants.DOM_VALIDATE_IF_SCHEMA) || str.equalsIgnoreCase(Constants.DOM_VALIDATE) || str.equalsIgnoreCase(Constants.DOM_CHECK_CHAR_NORMALIZATION) || str.equalsIgnoreCase(Constants.DOM_DATATYPE_NORMALIZATION)) {
                return true ^ booleanValue;
            }
            if (str.equalsIgnoreCase(Constants.DOM_ELEMENT_CONTENT_WHITESPACE) || str.equalsIgnoreCase(Constants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS)) {
                return booleanValue;
            }
            return false;
        }
        str.equalsIgnoreCase(Constants.DOM_ERROR_HANDLER);
        if (obj instanceof DOMErrorHandler) {
            return true;
        }
        return false;
    }

    public DOMStringList getParameterNames() {
        if (this.fRecognizedParameters == null) {
            Vector vector = new Vector();
            vector.add("namespaces");
            vector.add(Constants.DOM_SPLIT_CDATA);
            vector.add(Constants.DOM_DISCARD_DEFAULT_CONTENT);
            vector.add(Constants.DOM_XMLDECL);
            vector.add(Constants.DOM_CANONICAL_FORM);
            vector.add(Constants.DOM_VALIDATE_IF_SCHEMA);
            vector.add(Constants.DOM_VALIDATE);
            vector.add(Constants.DOM_CHECK_CHAR_NORMALIZATION);
            vector.add(Constants.DOM_DATATYPE_NORMALIZATION);
            vector.add(Constants.DOM_FORMAT_PRETTY_PRINT);
            vector.add(Constants.DOM_WELLFORMED);
            vector.add(Constants.DOM_INFOSET);
            vector.add(Constants.DOM_NAMESPACE_DECLARATIONS);
            vector.add(Constants.DOM_ELEMENT_CONTENT_WHITESPACE);
            vector.add(Constants.DOM_ENTITIES);
            vector.add(Constants.DOM_CDATA_SECTIONS);
            vector.add(Constants.DOM_COMMENTS);
            vector.add(Constants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS);
            vector.add(Constants.DOM_ERROR_HANDLER);
            this.fRecognizedParameters = new DOMStringListImpl(vector);
        }
        return this.fRecognizedParameters;
    }

    public Object getParameter(String str) throws DOMException {
        if (str.equalsIgnoreCase(Constants.DOM_NORMALIZE_CHARACTERS)) {
            return null;
        }
        if (str.equalsIgnoreCase(Constants.DOM_COMMENTS)) {
            return (this.features & 32) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase("namespaces")) {
            return (this.features & 1) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_XMLDECL)) {
            return (this.features & 256) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_CDATA_SECTIONS)) {
            return (this.features & 8) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_ENTITIES)) {
            return (this.features & 4) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_SPLIT_CDATA)) {
            return (this.features & 16) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_WELLFORMED)) {
            return (this.features & 2) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_NAMESPACE_DECLARATIONS)) {
            return (this.features & 512) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_FORMAT_PRETTY_PRINT)) {
            return (this.features & 2048) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_ELEMENT_CONTENT_WHITESPACE) || str.equalsIgnoreCase(Constants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS)) {
            return Boolean.TRUE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_DISCARD_DEFAULT_CONTENT)) {
            return (this.features & 64) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_INFOSET)) {
            short s = this.features;
            if ((s & 4) != 0 || (s & 8) != 0 || (s & 1) == 0 || (s & 512) == 0 || (s & 2) == 0 || (s & 32) == 0) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        } else if (str.equalsIgnoreCase(Constants.DOM_CANONICAL_FORM) || str.equalsIgnoreCase(Constants.DOM_VALIDATE_IF_SCHEMA) || str.equalsIgnoreCase(Constants.DOM_CHECK_CHAR_NORMALIZATION) || str.equalsIgnoreCase(Constants.DOM_VALIDATE) || str.equalsIgnoreCase(Constants.DOM_VALIDATE_IF_SCHEMA) || str.equalsIgnoreCase(Constants.DOM_DATATYPE_NORMALIZATION)) {
            return Boolean.FALSE;
        } else {
            if (str.equalsIgnoreCase(Constants.DOM_ERROR_HANDLER)) {
                return this.fErrorHandler;
            }
            if (str.equalsIgnoreCase(Constants.DOM_RESOURCE_RESOLVER) || str.equalsIgnoreCase(Constants.DOM_SCHEMA_LOCATION) || str.equalsIgnoreCase(Constants.DOM_SCHEMA_TYPE)) {
                throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_SUPPORTED", new Object[]{str}));
            }
            throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_FOUND", new Object[]{str}));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x006f A[Catch:{ LSException -> 0x010c, AbortException -> 0x010b, RuntimeException -> 0x00fa, IOException -> 0x00dc }] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0075 A[Catch:{ LSException -> 0x010c, AbortException -> 0x010b, RuntimeException -> 0x00fa, IOException -> 0x00dc }] */
    public String writeToString(Node node) throws DOMException, LSException {
        String str;
        XMLSerializer xMLSerializer;
        Document ownerDocument = node.getNodeType() == 9 ? (Document) node : node.getOwnerDocument();
        try {
            Method method = ownerDocument.getClass().getMethod("getXmlVersion", new Class[0]);
            if (method != null) {
                str = (String) method.invoke(ownerDocument, null);
                if (str != null || !str.equals(SerializerConstants.XMLVERSION11)) {
                    xMLSerializer = this.serializer;
                } else {
                    if (this.xml11Serializer == null) {
                        this.xml11Serializer = new XML11Serializer();
                        initSerializer(this.xml11Serializer);
                    }
                    copySettings(this.serializer, this.xml11Serializer);
                    xMLSerializer = this.xml11Serializer;
                }
                StringWriter stringWriter = new StringWriter();
                prepareForSerialization(xMLSerializer, node);
                xMLSerializer._format.setEncoding("UTF-16");
                xMLSerializer.setOutputCharStream(stringWriter);
                if (node.getNodeType() != 9) {
                    xMLSerializer.serialize((Document) node);
                } else if (node.getNodeType() == 11) {
                    xMLSerializer.serialize((DocumentFragment) node);
                } else if (node.getNodeType() == 1) {
                    xMLSerializer.serialize((Element) node);
                } else {
                    if (!(node.getNodeType() == 3 || node.getNodeType() == 8 || node.getNodeType() == 5 || node.getNodeType() == 4)) {
                        if (node.getNodeType() != 7) {
                            String formatMessage = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "unable-to-serialize-node", null);
                            if (xMLSerializer.fDOMErrorHandler != null) {
                                DOMErrorImpl dOMErrorImpl = new DOMErrorImpl();
                                dOMErrorImpl.fType = "unable-to-serialize-node";
                                dOMErrorImpl.fMessage = formatMessage;
                                dOMErrorImpl.fSeverity = 3;
                                xMLSerializer.fDOMErrorHandler.handleError(dOMErrorImpl);
                            }
                            throw new LSException(82, formatMessage);
                        }
                    }
                    xMLSerializer.serialize(node);
                }
                return stringWriter.toString();
            }
        } catch (Exception unused) {
        }
        str = null;
        if (str != null) {
        }
        xMLSerializer = this.serializer;
        StringWriter stringWriter2 = new StringWriter();
        try {
            prepareForSerialization(xMLSerializer, node);
            xMLSerializer._format.setEncoding("UTF-16");
            xMLSerializer.setOutputCharStream(stringWriter2);
            if (node.getNodeType() != 9) {
            }
            return stringWriter2.toString();
        } catch (LSException e) {
            throw e;
        } catch (AbortException unused2) {
            return null;
        } catch (RuntimeException e2) {
            throw new LSException(82, e2.toString()).initCause(e2);
        } catch (IOException e3) {
            throw new DOMException(2, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "STRING_TOO_LONG", new Object[]{e3.getMessage()})).initCause(e3);
        }
    }

    public void setNewLine(String str) {
        this.serializer._format.setLineSeparator(str);
    }

    public String getNewLine() {
        return this.serializer._format.getLineSeparator();
    }

    public LSSerializerFilter getFilter() {
        return this.serializer.fDOMFilter;
    }

    public void setFilter(LSSerializerFilter lSSerializerFilter) {
        this.serializer.fDOMFilter = lSSerializerFilter;
    }

    private void initSerializer(XMLSerializer xMLSerializer) {
        xMLSerializer.fNSBinder = new NamespaceSupport();
        xMLSerializer.fLocalNSBinder = new NamespaceSupport();
        xMLSerializer.fSymbolTable = new SymbolTable();
    }

    private void copySettings(XMLSerializer xMLSerializer, XMLSerializer xMLSerializer2) {
        xMLSerializer2.fDOMErrorHandler = this.fErrorHandler;
        xMLSerializer2._format.setEncoding(xMLSerializer._format.getEncoding());
        xMLSerializer2._format.setLineSeparator(xMLSerializer._format.getLineSeparator());
        xMLSerializer2.fDOMFilter = xMLSerializer.fDOMFilter;
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x005f A[SYNTHETIC, Splitter:B:24:0x005f] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00b1 A[Catch:{ UnsupportedEncodingException -> 0x01b8, LSException -> 0x01b6, AbortException -> 0x01b5, RuntimeException -> 0x01a9, Exception -> 0x0184 }] */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0132 A[Catch:{ UnsupportedEncodingException -> 0x01b8, LSException -> 0x01b6, AbortException -> 0x01b5, RuntimeException -> 0x01a9, Exception -> 0x0184 }] */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x013b A[Catch:{ UnsupportedEncodingException -> 0x01b8, LSException -> 0x01b6, AbortException -> 0x01b5, RuntimeException -> 0x01a9, Exception -> 0x0184 }] */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0141 A[Catch:{ UnsupportedEncodingException -> 0x01b8, LSException -> 0x01b6, AbortException -> 0x01b5, RuntimeException -> 0x01a9, Exception -> 0x0184 }] */
    public boolean write(Node node, LSOutput lSOutput) throws LSException {
        Document document;
        String str;
        XMLSerializer xMLSerializer;
        String encoding;
        Writer characterStream;
        OutputStream outputStream;
        if (node == null) {
            return false;
        }
        if (node.getNodeType() == 9) {
            document = (Document) node;
        } else {
            document = node.getOwnerDocument();
        }
        try {
            Method method = document.getClass().getMethod("getXmlVersion", new Class[0]);
            if (method != null) {
                str = (String) method.invoke(document, null);
                if (str != null || !str.equals(SerializerConstants.XMLVERSION11)) {
                    xMLSerializer = this.serializer;
                } else {
                    if (this.xml11Serializer == null) {
                        this.xml11Serializer = new XML11Serializer();
                        initSerializer(this.xml11Serializer);
                    }
                    copySettings(this.serializer, this.xml11Serializer);
                    xMLSerializer = this.xml11Serializer;
                }
                encoding = lSOutput.getEncoding();
                if (encoding == null) {
                    try {
                        Method method2 = document.getClass().getMethod("getInputEncoding", new Class[0]);
                        if (method2 != null) {
                            encoding = (String) method2.invoke(document, null);
                        }
                    } catch (Exception unused) {
                    }
                    if (encoding == null) {
                        try {
                            Method method3 = document.getClass().getMethod("getXmlEncoding", new Class[0]);
                            encoding = method3 != null ? (String) method3.invoke(document, null) : encoding;
                        } catch (Exception unused2) {
                        }
                        if (encoding == null) {
                            encoding = "UTF-8";
                        }
                    }
                }
                prepareForSerialization(xMLSerializer, node);
                xMLSerializer._format.setEncoding(encoding);
                OutputStream byteStream = lSOutput.getByteStream();
                characterStream = lSOutput.getCharacterStream();
                String systemId = lSOutput.getSystemId();
                if (characterStream == null) {
                    xMLSerializer.setOutputCharStream(characterStream);
                } else if (byteStream != null) {
                    xMLSerializer.setOutputByteStream(byteStream);
                } else if (systemId == null) {
                    String formatMessage = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "no-output-specified", null);
                    if (xMLSerializer.fDOMErrorHandler != null) {
                        DOMErrorImpl dOMErrorImpl = new DOMErrorImpl();
                        dOMErrorImpl.fType = "no-output-specified";
                        dOMErrorImpl.fMessage = formatMessage;
                        dOMErrorImpl.fSeverity = 3;
                        xMLSerializer.fDOMErrorHandler.handleError(dOMErrorImpl);
                    }
                    throw new LSException(82, formatMessage);
                } else {
                    String expandSystemId = XMLEntityManager.expandSystemId(systemId, null, true);
                    if (expandSystemId != null) {
                        systemId = expandSystemId;
                    }
                    URL url = new URL(systemId);
                    String protocol = url.getProtocol();
                    String host = url.getHost();
                    if (!protocol.equals(AsrConstants.ASR_SRC_FILE) || !(host == null || host.length() == 0 || host.equals("localhost"))) {
                        URLConnection openConnection = url.openConnection();
                        openConnection.setDoInput(false);
                        openConnection.setDoOutput(true);
                        openConnection.setUseCaches(false);
                        if (openConnection instanceof HttpURLConnection) {
                            ((HttpURLConnection) openConnection).setRequestMethod("PUT");
                        }
                        outputStream = openConnection.getOutputStream();
                    } else {
                        outputStream = new FileOutputStream(getPathWithoutEscapes(url.getFile()));
                    }
                    xMLSerializer.setOutputByteStream(outputStream);
                }
                if (node.getNodeType() != 9) {
                    xMLSerializer.serialize((Document) node);
                } else if (node.getNodeType() == 11) {
                    xMLSerializer.serialize((DocumentFragment) node);
                } else if (node.getNodeType() == 1) {
                    xMLSerializer.serialize((Element) node);
                } else {
                    if (!(node.getNodeType() == 3 || node.getNodeType() == 8 || node.getNodeType() == 5 || node.getNodeType() == 4)) {
                        if (node.getNodeType() != 7) {
                            return false;
                        }
                    }
                    xMLSerializer.serialize(node);
                }
                return true;
            }
        } catch (Exception unused3) {
        }
        str = null;
        if (str != null) {
        }
        xMLSerializer = this.serializer;
        encoding = lSOutput.getEncoding();
        if (encoding == null) {
        }
        try {
            prepareForSerialization(xMLSerializer, node);
            xMLSerializer._format.setEncoding(encoding);
            OutputStream byteStream2 = lSOutput.getByteStream();
            characterStream = lSOutput.getCharacterStream();
            String systemId2 = lSOutput.getSystemId();
            if (characterStream == null) {
            }
            if (node.getNodeType() != 9) {
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            if (xMLSerializer.fDOMErrorHandler != null) {
                DOMErrorImpl dOMErrorImpl2 = new DOMErrorImpl();
                dOMErrorImpl2.fException = e;
                dOMErrorImpl2.fType = "unsupported-encoding";
                dOMErrorImpl2.fMessage = e.getMessage();
                dOMErrorImpl2.fSeverity = 3;
                xMLSerializer.fDOMErrorHandler.handleError(dOMErrorImpl2);
            }
            throw new LSException(82, DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "unsupported-encoding", null));
        } catch (LSException e2) {
            throw e2;
        } catch (AbortException unused4) {
            return false;
        } catch (RuntimeException e3) {
            throw DOMUtil.createLSException(82, e3).fillInStackTrace();
        } catch (Exception e4) {
            if (xMLSerializer.fDOMErrorHandler != null) {
                DOMErrorImpl dOMErrorImpl3 = new DOMErrorImpl();
                dOMErrorImpl3.fException = e4;
                dOMErrorImpl3.fMessage = e4.getMessage();
                dOMErrorImpl3.fSeverity = 2;
                xMLSerializer.fDOMErrorHandler.handleError(dOMErrorImpl3);
            }
            throw DOMUtil.createLSException(82, e4).fillInStackTrace();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0062 A[Catch:{ Exception -> 0x006c }] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x006f A[SYNTHETIC, Splitter:B:29:0x006f] */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x009e A[Catch:{ LSException -> 0x0172, AbortException -> 0x0171, RuntimeException -> 0x0165, Exception -> 0x0140 }] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00b2 A[ADDED_TO_REGION, Catch:{ LSException -> 0x0172, AbortException -> 0x0171, RuntimeException -> 0x0165, Exception -> 0x0140 }] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00e1 A[Catch:{ LSException -> 0x0172, AbortException -> 0x0171, RuntimeException -> 0x0165, Exception -> 0x0140 }] */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00f6 A[Catch:{ LSException -> 0x0172, AbortException -> 0x0171, RuntimeException -> 0x0165, Exception -> 0x0140 }] */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00fc A[Catch:{ LSException -> 0x0172, AbortException -> 0x0171, RuntimeException -> 0x0165, Exception -> 0x0140 }] */
    public boolean writeToURI(Node node, String str) throws LSException {
        Document document;
        String str2;
        XMLSerializer xMLSerializer;
        String str3;
        String expandSystemId;
        URL url;
        String protocol;
        OutputStream outputStream;
        URLConnection openConnection;
        Method method;
        if (node == null) {
            return false;
        }
        if (node.getNodeType() == 9) {
            document = (Document) node;
        } else {
            document = node.getOwnerDocument();
        }
        try {
            Method method2 = document.getClass().getMethod("getXmlVersion", new Class[0]);
            if (method2 != null) {
                str2 = (String) method2.invoke(document, null);
                if (str2 != null || !str2.equals(SerializerConstants.XMLVERSION11)) {
                    xMLSerializer = this.serializer;
                } else {
                    if (this.xml11Serializer == null) {
                        this.xml11Serializer = new XML11Serializer();
                        initSerializer(this.xml11Serializer);
                    }
                    copySettings(this.serializer, this.xml11Serializer);
                    xMLSerializer = this.xml11Serializer;
                }
                method = document.getClass().getMethod("getInputEncoding", new Class[0]);
                if (method != null) {
                    str3 = (String) method.invoke(document, null);
                    if (str3 == null) {
                        try {
                            Method method3 = document.getClass().getMethod("getXmlEncoding", new Class[0]);
                            if (method3 != null) {
                                str3 = (String) method3.invoke(document, null);
                            }
                        } catch (Exception unused) {
                        }
                        if (str3 == null) {
                            str3 = "UTF-8";
                        }
                    }
                    prepareForSerialization(xMLSerializer, node);
                    xMLSerializer._format.setEncoding(str3);
                    expandSystemId = XMLEntityManager.expandSystemId(str, null, true);
                    if (expandSystemId != null) {
                        str = expandSystemId;
                    }
                    url = new URL(str);
                    protocol = url.getProtocol();
                    String host = url.getHost();
                    if (protocol.equals(AsrConstants.ASR_SRC_FILE) || !(host == null || host.length() == 0 || host.equals("localhost"))) {
                        openConnection = url.openConnection();
                        openConnection.setDoInput(false);
                        openConnection.setDoOutput(true);
                        openConnection.setUseCaches(false);
                        if (openConnection instanceof HttpURLConnection) {
                            ((HttpURLConnection) openConnection).setRequestMethod("PUT");
                        }
                        outputStream = openConnection.getOutputStream();
                    } else {
                        outputStream = new FileOutputStream(getPathWithoutEscapes(url.getFile()));
                    }
                    xMLSerializer.setOutputByteStream(outputStream);
                    if (node.getNodeType() == 9) {
                        xMLSerializer.serialize((Document) node);
                    } else if (node.getNodeType() == 11) {
                        xMLSerializer.serialize((DocumentFragment) node);
                    } else if (node.getNodeType() == 1) {
                        xMLSerializer.serialize((Element) node);
                    } else {
                        if (!(node.getNodeType() == 3 || node.getNodeType() == 8 || node.getNodeType() == 5 || node.getNodeType() == 4)) {
                            if (node.getNodeType() != 7) {
                                return false;
                            }
                        }
                        xMLSerializer.serialize(node);
                    }
                    return true;
                }
                str3 = null;
                if (str3 == null) {
                }
                prepareForSerialization(xMLSerializer, node);
                xMLSerializer._format.setEncoding(str3);
                expandSystemId = XMLEntityManager.expandSystemId(str, null, true);
                if (expandSystemId != null) {
                }
                url = new URL(str);
                protocol = url.getProtocol();
                String host2 = url.getHost();
                if (protocol.equals(AsrConstants.ASR_SRC_FILE)) {
                }
                openConnection = url.openConnection();
                openConnection.setDoInput(false);
                openConnection.setDoOutput(true);
                openConnection.setUseCaches(false);
                if (openConnection instanceof HttpURLConnection) {
                }
                outputStream = openConnection.getOutputStream();
                xMLSerializer.setOutputByteStream(outputStream);
                if (node.getNodeType() == 9) {
                }
                return true;
            }
        } catch (Exception unused2) {
        }
        str2 = null;
        if (str2 != null) {
        }
        xMLSerializer = this.serializer;
        try {
            method = document.getClass().getMethod("getInputEncoding", new Class[0]);
            if (method != null) {
            }
        } catch (Exception unused3) {
        }
        str3 = null;
        if (str3 == null) {
        }
        try {
            prepareForSerialization(xMLSerializer, node);
            xMLSerializer._format.setEncoding(str3);
            expandSystemId = XMLEntityManager.expandSystemId(str, null, true);
            if (expandSystemId != null) {
            }
            url = new URL(str);
            protocol = url.getProtocol();
            String host22 = url.getHost();
            if (protocol.equals(AsrConstants.ASR_SRC_FILE)) {
            }
            openConnection = url.openConnection();
            openConnection.setDoInput(false);
            openConnection.setDoOutput(true);
            openConnection.setUseCaches(false);
            if (openConnection instanceof HttpURLConnection) {
            }
            outputStream = openConnection.getOutputStream();
            xMLSerializer.setOutputByteStream(outputStream);
            if (node.getNodeType() == 9) {
            }
            return true;
        } catch (LSException e) {
            throw e;
        } catch (AbortException unused4) {
            return false;
        } catch (RuntimeException e2) {
            throw DOMUtil.createLSException(82, e2).fillInStackTrace();
        } catch (Exception e3) {
            if (xMLSerializer.fDOMErrorHandler != null) {
                DOMErrorImpl dOMErrorImpl = new DOMErrorImpl();
                dOMErrorImpl.fException = e3;
                dOMErrorImpl.fMessage = e3.getMessage();
                dOMErrorImpl.fSeverity = 2;
                xMLSerializer.fDOMErrorHandler.handleError(dOMErrorImpl);
            }
            throw DOMUtil.createLSException(82, e3).fillInStackTrace();
        }
    }

    private void prepareForSerialization(XMLSerializer xMLSerializer, Node node) {
        Document document;
        xMLSerializer.reset();
        short s = this.features;
        xMLSerializer.features = s;
        xMLSerializer.fDOMErrorHandler = this.fErrorHandler;
        boolean z = true;
        xMLSerializer.fNamespaces = (s & 1) != 0;
        xMLSerializer.fNamespacePrefixes = (this.features & 512) != 0;
        xMLSerializer._format.setOmitComments((this.features & 32) == 0);
        xMLSerializer._format.setOmitXMLDeclaration((this.features & 256) == 0);
        xMLSerializer._format.setIndenting((this.features & 2048) != 0);
        if ((this.features & 2) != 0) {
            if (node.getNodeType() == 9) {
                document = (Document) node;
            } else {
                document = node.getOwnerDocument();
            }
            try {
                Method method = document.getClass().getMethod("isXMLVersionChanged()", new Class[0]);
                if (method != null) {
                    z = ((Boolean) method.invoke(document, null)).booleanValue();
                }
            } catch (Exception unused) {
            }
            if (node.getFirstChild() != null) {
                Node node2 = node;
                while (node2 != null) {
                    verify(node2, z, false);
                    Node firstChild = node2.getFirstChild();
                    while (true) {
                        if (firstChild != null) {
                            node2 = firstChild;
                            break;
                        }
                        firstChild = node2.getNextSibling();
                        if (firstChild == null) {
                            node2 = node2.getParentNode();
                            if (node == node2) {
                                node2 = null;
                                break;
                            }
                            firstChild = node2.getNextSibling();
                        }
                    }
                }
                return;
            }
            verify(node, z, false);
        }
    }

    private void verify(Node node, boolean z, boolean z2) {
        boolean z3;
        boolean z4;
        short nodeType = node.getNodeType();
        DOMLocatorImpl dOMLocatorImpl = this.fLocator;
        dOMLocatorImpl.fRelatedNode = node;
        switch (nodeType) {
            case 1:
                if (z) {
                    if ((this.features & 1) != 0) {
                        z3 = CoreDocumentImpl.isValidQName(node.getPrefix(), node.getLocalName(), z2);
                    } else {
                        z3 = CoreDocumentImpl.isXMLName(node.getNodeName(), z2);
                    }
                    if (!z3 && !z3 && this.fErrorHandler != null) {
                        DOMNormalizer.reportDOMError(this.fErrorHandler, this.fError, this.fLocator, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "wf-invalid-character-in-node-name", new Object[]{"Element", node.getNodeName()}), 3, "wf-invalid-character-in-node-name");
                    }
                }
                NamedNodeMap attributes = node.hasAttributes() ? node.getAttributes() : null;
                if (attributes != null) {
                    for (int i = 0; i < attributes.getLength(); i++) {
                        Attr item = attributes.item(i);
                        DOMLocatorImpl dOMLocatorImpl2 = this.fLocator;
                        dOMLocatorImpl2.fRelatedNode = item;
                        DOMNormalizer.isAttrValueWF(this.fErrorHandler, this.fError, dOMLocatorImpl2, attributes, item, item.getValue(), z2);
                        if (z && !CoreDocumentImpl.isXMLName(item.getNodeName(), z2)) {
                            DOMNormalizer.reportDOMError(this.fErrorHandler, this.fError, this.fLocator, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "wf-invalid-character-in-node-name", new Object[]{"Attr", node.getNodeName()}), 3, "wf-invalid-character-in-node-name");
                        }
                    }
                    return;
                }
                return;
            case 2:
            case 6:
            case 9:
            case 10:
            default:
                return;
            case 3:
                DOMNormalizer.isXMLCharWF(this.fErrorHandler, this.fError, dOMLocatorImpl, node.getNodeValue(), z2);
                return;
            case 4:
                DOMNormalizer.isXMLCharWF(this.fErrorHandler, this.fError, dOMLocatorImpl, node.getNodeValue(), z2);
                return;
            case 5:
                if (z && (this.features & 4) != 0) {
                    CoreDocumentImpl.isXMLName(node.getNodeName(), z2);
                    return;
                }
                return;
            case 7:
                ProcessingInstruction processingInstruction = (ProcessingInstruction) node;
                String target = processingInstruction.getTarget();
                if (z) {
                    if (z2) {
                        z4 = XML11Char.isXML11ValidName(target);
                    } else {
                        z4 = XMLChar.isValidName(target);
                    }
                    if (!z4) {
                        DOMNormalizer.reportDOMError(this.fErrorHandler, this.fError, this.fLocator, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "wf-invalid-character-in-node-name", new Object[]{"Element", node.getNodeName()}), 3, "wf-invalid-character-in-node-name");
                    }
                }
                DOMNormalizer.isXMLCharWF(this.fErrorHandler, this.fError, this.fLocator, processingInstruction.getData(), z2);
                return;
            case 8:
                if ((this.features & 32) != 0) {
                    DOMNormalizer.isCommentWF(this.fErrorHandler, this.fError, dOMLocatorImpl, ((Comment) node).getData(), z2);
                    return;
                }
                return;
        }
    }

    private String getPathWithoutEscapes(String str) {
        if (str == null || str.length() == 0 || str.indexOf(37) == -1) {
            return str;
        }
        StringTokenizer stringTokenizer = new StringTokenizer(str, "%");
        StringBuffer stringBuffer = new StringBuffer(str.length());
        int countTokens = stringTokenizer.countTokens();
        stringBuffer.append(stringTokenizer.nextToken());
        for (int i = 1; i < countTokens; i++) {
            String nextToken = stringTokenizer.nextToken();
            stringBuffer.append((char) Integer.valueOf(nextToken.substring(0, 2), 16).intValue());
            stringBuffer.append(nextToken.substring(2));
        }
        return stringBuffer.toString();
    }
}
