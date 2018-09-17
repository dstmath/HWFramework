package org.apache.xml.serializer.dom3;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.xalan.templates.Constants;
import org.apache.xml.serializer.DOM3Serializer;
import org.apache.xml.serializer.Encodings;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xml.serializer.utils.Messages;
import org.apache.xml.serializer.utils.MsgKey;
import org.apache.xml.serializer.utils.SystemIDResolver;
import org.apache.xml.serializer.utils.Utils;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.LSSerializerFilter;

public final class LSSerializerImpl implements DOMConfiguration, LSSerializer {
    private static final int CANONICAL = 1;
    private static final int CDATA = 2;
    private static final int CHARNORMALIZE = 4;
    private static final int COMMENTS = 8;
    private static final String DEFAULT_END_OF_LINE = null;
    private static final int DISCARDDEFAULT = 32768;
    private static final int DTNORMALIZE = 16;
    private static final int ELEM_CONTENT_WHITESPACE = 32;
    private static final int ENTITIES = 64;
    private static final int IGNORE_CHAR_DENORMALIZE = 131072;
    private static final int INFOSET = 128;
    private static final int NAMESPACEDECLS = 512;
    private static final int NAMESPACES = 256;
    private static final int NORMALIZECHARS = 1024;
    private static final int PRETTY_PRINT = 65536;
    private static final int SCHEMAVALIDATE = 8192;
    private static final int SPLITCDATA = 2048;
    private static final int VALIDATE = 4096;
    private static final int WELLFORMED = 16384;
    private static final int XMLDECL = 262144;
    private Properties fDOMConfigProperties;
    private DOMErrorHandler fDOMErrorHandler;
    private DOM3Serializer fDOMSerializer;
    private String fEncoding;
    private String fEndOfLine;
    protected int fFeatures;
    private String[] fRecognizedParameters;
    private LSSerializerFilter fSerializerFilter;
    private Node fVisitedNode;
    private Serializer fXMLSerializer;

    static class ThrowableMethods {
        private static Method fgThrowableInitCauseMethod;
        private static boolean fgThrowableMethodsAvailable;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.serializer.dom3.LSSerializerImpl.ThrowableMethods.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.serializer.dom3.LSSerializerImpl.ThrowableMethods.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.serializer.dom3.LSSerializerImpl.ThrowableMethods.<clinit>():void");
        }

        private ThrowableMethods() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.serializer.dom3.LSSerializerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.serializer.dom3.LSSerializerImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.serializer.dom3.LSSerializerImpl.<clinit>():void");
    }

    public LSSerializerImpl() {
        this.fXMLSerializer = null;
        this.fFeatures = 0;
        this.fDOMSerializer = null;
        this.fSerializerFilter = null;
        this.fVisitedNode = null;
        this.fEndOfLine = DEFAULT_END_OF_LINE;
        this.fDOMErrorHandler = null;
        this.fDOMConfigProperties = null;
        this.fRecognizedParameters = new String[]{DOMConstants.DOM_CANONICAL_FORM, DOMConstants.DOM_CDATA_SECTIONS, DOMConstants.DOM_CHECK_CHAR_NORMALIZATION, DOMConstants.DOM_COMMENTS, DOMConstants.DOM_DATATYPE_NORMALIZATION, DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE, DOMConstants.DOM_ENTITIES, DOMConstants.DOM_INFOSET, DOMConstants.DOM_NAMESPACES, DOMConstants.DOM_NAMESPACE_DECLARATIONS, DOMConstants.DOM_SPLIT_CDATA, DOMConstants.DOM_VALIDATE, DOMConstants.DOM_VALIDATE_IF_SCHEMA, DOMConstants.DOM_WELLFORMED, DOMConstants.DOM_DISCARD_DEFAULT_CONTENT, DOMConstants.DOM_FORMAT_PRETTY_PRINT, DOMConstants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS, DOMConstants.DOM_XMLDECL, DOMConstants.DOM_ERROR_HANDLER};
        this.fFeatures |= CDATA;
        this.fFeatures |= COMMENTS;
        this.fFeatures |= ELEM_CONTENT_WHITESPACE;
        this.fFeatures |= ENTITIES;
        this.fFeatures |= NAMESPACES;
        this.fFeatures |= NAMESPACEDECLS;
        this.fFeatures |= SPLITCDATA;
        this.fFeatures |= WELLFORMED;
        this.fFeatures |= DISCARDDEFAULT;
        this.fFeatures |= XMLDECL;
        this.fDOMConfigProperties = new Properties();
        initializeSerializerProps();
        this.fXMLSerializer = SerializerFactory.getSerializer(OutputPropertiesFactory.getDefaultMethodProperties(SerializerConstants.XML_PREFIX));
        this.fXMLSerializer.setOutputFormat(this.fDOMConfigProperties);
    }

    public void initializeSerializerProps() {
        this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}canonical-form", DOMConstants.DOM3_DEFAULT_FALSE);
        this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}cdata-sections", DOMConstants.DOM3_DEFAULT_TRUE);
        this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}check-character-normalization", DOMConstants.DOM3_DEFAULT_FALSE);
        this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}comments", DOMConstants.DOM3_DEFAULT_TRUE);
        this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}datatype-normalization", DOMConstants.DOM3_DEFAULT_FALSE);
        this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}element-content-whitespace", DOMConstants.DOM3_DEFAULT_TRUE);
        this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}entities", DOMConstants.DOM3_DEFAULT_TRUE);
        this.fDOMConfigProperties.setProperty("{http://xml.apache.org/xerces-2j}entities", DOMConstants.DOM3_DEFAULT_TRUE);
        if ((this.fFeatures & INFOSET) != 0) {
            this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespaces", DOMConstants.DOM3_DEFAULT_TRUE);
            this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespace-declarations", DOMConstants.DOM3_DEFAULT_TRUE);
            this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}comments", DOMConstants.DOM3_DEFAULT_TRUE);
            this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}element-content-whitespace", DOMConstants.DOM3_DEFAULT_TRUE);
            this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}well-formed", DOMConstants.DOM3_DEFAULT_TRUE);
            this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}entities", DOMConstants.DOM3_DEFAULT_FALSE);
            this.fDOMConfigProperties.setProperty("{http://xml.apache.org/xerces-2j}entities", DOMConstants.DOM3_DEFAULT_FALSE);
            this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}cdata-sections", DOMConstants.DOM3_DEFAULT_FALSE);
            this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}validate-if-schema", DOMConstants.DOM3_DEFAULT_FALSE);
            this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}datatype-normalization", DOMConstants.DOM3_DEFAULT_FALSE);
        }
        this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespaces", DOMConstants.DOM3_DEFAULT_TRUE);
        this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespace-declarations", DOMConstants.DOM3_DEFAULT_TRUE);
        this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}split-cdata-sections", DOMConstants.DOM3_DEFAULT_TRUE);
        this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}validate", DOMConstants.DOM3_DEFAULT_FALSE);
        this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}validate-if-schema", DOMConstants.DOM3_DEFAULT_FALSE);
        this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}well-formed", DOMConstants.DOM3_DEFAULT_TRUE);
        this.fDOMConfigProperties.setProperty(DOMConstants.S_XSL_OUTPUT_INDENT, DOMConstants.DOM3_DEFAULT_TRUE);
        this.fDOMConfigProperties.setProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, Integer.toString(3));
        this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}discard-default-content", DOMConstants.DOM3_DEFAULT_TRUE);
        this.fDOMConfigProperties.setProperty(DOMConstants.S_XSL_OUTPUT_OMIT_XML_DECL, "no");
    }

    public boolean canSetParameter(String name, Object value) {
        boolean z = false;
        if (!(value instanceof Boolean)) {
            return (name.equalsIgnoreCase(DOMConstants.DOM_ERROR_HANDLER) && value == null) || (value instanceof DOMErrorHandler);
        } else {
            if (name.equalsIgnoreCase(DOMConstants.DOM_CDATA_SECTIONS) || name.equalsIgnoreCase(DOMConstants.DOM_COMMENTS) || name.equalsIgnoreCase(DOMConstants.DOM_ENTITIES) || name.equalsIgnoreCase(DOMConstants.DOM_INFOSET) || name.equalsIgnoreCase(DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE) || name.equalsIgnoreCase(DOMConstants.DOM_NAMESPACES) || name.equalsIgnoreCase(DOMConstants.DOM_NAMESPACE_DECLARATIONS) || name.equalsIgnoreCase(DOMConstants.DOM_SPLIT_CDATA) || name.equalsIgnoreCase(DOMConstants.DOM_WELLFORMED) || name.equalsIgnoreCase(DOMConstants.DOM_DISCARD_DEFAULT_CONTENT) || name.equalsIgnoreCase(DOMConstants.DOM_FORMAT_PRETTY_PRINT) || name.equalsIgnoreCase(DOMConstants.DOM_XMLDECL)) {
                return true;
            }
            if (name.equalsIgnoreCase(DOMConstants.DOM_CANONICAL_FORM) || name.equalsIgnoreCase(DOMConstants.DOM_CHECK_CHAR_NORMALIZATION) || name.equalsIgnoreCase(DOMConstants.DOM_DATATYPE_NORMALIZATION) || name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE_IF_SCHEMA) || name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE)) {
                if (!((Boolean) value).booleanValue()) {
                    z = true;
                }
                return z;
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS)) {
                return ((Boolean) value).booleanValue();
            }
        }
    }

    public Object getParameter(String name) throws DOMException {
        if (name.equalsIgnoreCase(DOMConstants.DOM_COMMENTS)) {
            return (this.fFeatures & COMMENTS) != 0 ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_CDATA_SECTIONS)) {
            return (this.fFeatures & CDATA) != 0 ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_ENTITIES)) {
            return (this.fFeatures & ENTITIES) != 0 ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_NAMESPACES)) {
            return (this.fFeatures & NAMESPACES) != 0 ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_NAMESPACE_DECLARATIONS)) {
            return (this.fFeatures & NAMESPACEDECLS) != 0 ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_SPLIT_CDATA)) {
            return (this.fFeatures & SPLITCDATA) != 0 ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_WELLFORMED)) {
            return (this.fFeatures & WELLFORMED) != 0 ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_DISCARD_DEFAULT_CONTENT)) {
            return (this.fFeatures & DISCARDDEFAULT) != 0 ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_FORMAT_PRETTY_PRINT)) {
            return (this.fFeatures & PRETTY_PRINT) != 0 ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_XMLDECL)) {
            return (this.fFeatures & XMLDECL) != 0 ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE)) {
            return (this.fFeatures & ELEM_CONTENT_WHITESPACE) != 0 ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_FORMAT_PRETTY_PRINT)) {
            return (this.fFeatures & PRETTY_PRINT) != 0 ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS)) {
            return Boolean.TRUE;
        } else {
            if (name.equalsIgnoreCase(DOMConstants.DOM_CANONICAL_FORM) || name.equalsIgnoreCase(DOMConstants.DOM_CHECK_CHAR_NORMALIZATION) || name.equalsIgnoreCase(DOMConstants.DOM_DATATYPE_NORMALIZATION) || name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE) || name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE_IF_SCHEMA)) {
                return Boolean.FALSE;
            }
            if (name.equalsIgnoreCase(DOMConstants.DOM_INFOSET)) {
                if ((this.fFeatures & ENTITIES) != 0 || (this.fFeatures & CDATA) != 0 || (this.fFeatures & ELEM_CONTENT_WHITESPACE) == 0 || (this.fFeatures & NAMESPACES) == 0 || (this.fFeatures & NAMESPACEDECLS) == 0 || (this.fFeatures & WELLFORMED) == 0 || (this.fFeatures & COMMENTS) == 0) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_ERROR_HANDLER)) {
                return this.fDOMErrorHandler;
            } else {
                if (name.equalsIgnoreCase(DOMConstants.DOM_SCHEMA_LOCATION) || name.equalsIgnoreCase(DOMConstants.DOM_SCHEMA_TYPE)) {
                    return null;
                }
                Messages messages = Utils.messages;
                String str = MsgKey.ER_FEATURE_NOT_FOUND;
                Object[] objArr = new Object[CANONICAL];
                objArr[0] = name;
                throw new DOMException((short) 8, messages.createMessage(str, objArr));
            }
        }
    }

    public DOMStringList getParameterNames() {
        return new DOMStringListImpl(this.fRecognizedParameters);
    }

    public void setParameter(String name, Object value) throws DOMException {
        Messages messages;
        String str;
        Object[] objArr;
        if (value instanceof Boolean) {
            boolean state = ((Boolean) value).booleanValue();
            if (name.equalsIgnoreCase(DOMConstants.DOM_COMMENTS)) {
                this.fFeatures = state ? this.fFeatures | COMMENTS : this.fFeatures & -9;
                if (state) {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}comments", DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}comments", DOMConstants.DOM3_EXPLICIT_FALSE);
                }
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_CDATA_SECTIONS)) {
                this.fFeatures = state ? this.fFeatures | CDATA : this.fFeatures & -3;
                if (state) {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}cdata-sections", DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}cdata-sections", DOMConstants.DOM3_EXPLICIT_FALSE);
                }
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_ENTITIES)) {
                this.fFeatures = state ? this.fFeatures | ENTITIES : this.fFeatures & -65;
                if (state) {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}entities", DOMConstants.DOM3_EXPLICIT_TRUE);
                    this.fDOMConfigProperties.setProperty("{http://xml.apache.org/xerces-2j}entities", DOMConstants.DOM3_EXPLICIT_TRUE);
                    return;
                }
                this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}entities", DOMConstants.DOM3_EXPLICIT_FALSE);
                this.fDOMConfigProperties.setProperty("{http://xml.apache.org/xerces-2j}entities", DOMConstants.DOM3_EXPLICIT_FALSE);
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_NAMESPACES)) {
                this.fFeatures = state ? this.fFeatures | NAMESPACES : this.fFeatures & -257;
                if (state) {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespaces", DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespaces", DOMConstants.DOM3_EXPLICIT_FALSE);
                }
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_NAMESPACE_DECLARATIONS)) {
                if (state) {
                    r2 = this.fFeatures | NAMESPACEDECLS;
                } else {
                    r2 = this.fFeatures & -513;
                }
                this.fFeatures = r2;
                if (state) {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespace-declarations", DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespace-declarations", DOMConstants.DOM3_EXPLICIT_FALSE);
                }
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_SPLIT_CDATA)) {
                this.fFeatures = state ? this.fFeatures | SPLITCDATA : this.fFeatures & -2049;
                if (state) {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}split-cdata-sections", DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}split-cdata-sections", DOMConstants.DOM3_EXPLICIT_FALSE);
                }
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_WELLFORMED)) {
                this.fFeatures = state ? this.fFeatures | WELLFORMED : this.fFeatures & -16385;
                if (state) {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}well-formed", DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}well-formed", DOMConstants.DOM3_EXPLICIT_FALSE);
                }
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_DISCARD_DEFAULT_CONTENT)) {
                if (state) {
                    r2 = this.fFeatures | DISCARDDEFAULT;
                } else {
                    r2 = this.fFeatures & -32769;
                }
                this.fFeatures = r2;
                if (state) {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}discard-default-content", DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}discard-default-content", DOMConstants.DOM3_EXPLICIT_FALSE);
                }
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_FORMAT_PRETTY_PRINT)) {
                this.fFeatures = state ? this.fFeatures | PRETTY_PRINT : this.fFeatures & -65537;
                if (state) {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}format-pretty-print", DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}format-pretty-print", DOMConstants.DOM3_EXPLICIT_FALSE);
                }
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_XMLDECL)) {
                this.fFeatures = state ? this.fFeatures | XMLDECL : this.fFeatures & -262145;
                if (state) {
                    this.fDOMConfigProperties.setProperty(DOMConstants.S_XSL_OUTPUT_OMIT_XML_DECL, "no");
                } else {
                    this.fDOMConfigProperties.setProperty(DOMConstants.S_XSL_OUTPUT_OMIT_XML_DECL, "yes");
                }
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE)) {
                this.fFeatures = state ? this.fFeatures | ELEM_CONTENT_WHITESPACE : this.fFeatures & -33;
                if (state) {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}element-content-whitespace", DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}element-content-whitespace", DOMConstants.DOM3_EXPLICIT_FALSE);
                }
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS)) {
                if (state) {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}ignore-unknown-character-denormalizations", DOMConstants.DOM3_EXPLICIT_TRUE);
                    return;
                }
                messages = Utils.messages;
                str = MsgKey.ER_FEATURE_NOT_SUPPORTED;
                objArr = new Object[CANONICAL];
                objArr[0] = name;
                throw new DOMException((short) 9, messages.createMessage(str, objArr));
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_CANONICAL_FORM) || name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE_IF_SCHEMA) || name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE) || name.equalsIgnoreCase(DOMConstants.DOM_CHECK_CHAR_NORMALIZATION) || name.equalsIgnoreCase(DOMConstants.DOM_DATATYPE_NORMALIZATION)) {
                if (state) {
                    messages = Utils.messages;
                    str = MsgKey.ER_FEATURE_NOT_SUPPORTED;
                    objArr = new Object[CANONICAL];
                    objArr[0] = name;
                    throw new DOMException((short) 9, messages.createMessage(str, objArr));
                } else if (name.equalsIgnoreCase(DOMConstants.DOM_CANONICAL_FORM)) {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}canonical-form", DOMConstants.DOM3_EXPLICIT_FALSE);
                } else if (name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE_IF_SCHEMA)) {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}validate-if-schema", DOMConstants.DOM3_EXPLICIT_FALSE);
                } else if (name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE)) {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}validate", DOMConstants.DOM3_EXPLICIT_FALSE);
                } else if (name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE_IF_SCHEMA)) {
                    this.fDOMConfigProperties.setProperty("check-character-normalizationcheck-character-normalization", DOMConstants.DOM3_EXPLICIT_FALSE);
                } else if (name.equalsIgnoreCase(DOMConstants.DOM_DATATYPE_NORMALIZATION)) {
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}datatype-normalization", DOMConstants.DOM3_EXPLICIT_FALSE);
                }
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_INFOSET)) {
                if (state) {
                    this.fFeatures &= -65;
                    this.fFeatures &= -3;
                    this.fFeatures &= -8193;
                    this.fFeatures &= -17;
                    this.fFeatures |= NAMESPACES;
                    this.fFeatures |= NAMESPACEDECLS;
                    this.fFeatures |= WELLFORMED;
                    this.fFeatures |= ELEM_CONTENT_WHITESPACE;
                    this.fFeatures |= COMMENTS;
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespaces", DOMConstants.DOM3_EXPLICIT_TRUE);
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespace-declarations", DOMConstants.DOM3_EXPLICIT_TRUE);
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}comments", DOMConstants.DOM3_EXPLICIT_TRUE);
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}element-content-whitespace", DOMConstants.DOM3_EXPLICIT_TRUE);
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}well-formed", DOMConstants.DOM3_EXPLICIT_TRUE);
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}entities", DOMConstants.DOM3_EXPLICIT_FALSE);
                    this.fDOMConfigProperties.setProperty("{http://xml.apache.org/xerces-2j}entities", DOMConstants.DOM3_EXPLICIT_FALSE);
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}cdata-sections", DOMConstants.DOM3_EXPLICIT_FALSE);
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}validate-if-schema", DOMConstants.DOM3_EXPLICIT_FALSE);
                    this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}datatype-normalization", DOMConstants.DOM3_EXPLICIT_FALSE);
                }
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_ERROR_HANDLER) || name.equalsIgnoreCase(DOMConstants.DOM_SCHEMA_LOCATION) || name.equalsIgnoreCase(DOMConstants.DOM_SCHEMA_TYPE)) {
                messages = Utils.messages;
                str = MsgKey.ER_TYPE_MISMATCH_ERR;
                objArr = new Object[CANONICAL];
                objArr[0] = name;
                throw new DOMException((short) 17, messages.createMessage(str, objArr));
            } else {
                messages = Utils.messages;
                str = MsgKey.ER_FEATURE_NOT_FOUND;
                objArr = new Object[CANONICAL];
                objArr[0] = name;
                throw new DOMException((short) 8, messages.createMessage(str, objArr));
            }
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_ERROR_HANDLER)) {
            if (value == null || (value instanceof DOMErrorHandler)) {
                this.fDOMErrorHandler = (DOMErrorHandler) value;
                return;
            }
            messages = Utils.messages;
            str = MsgKey.ER_TYPE_MISMATCH_ERR;
            objArr = new Object[CANONICAL];
            objArr[0] = name;
            throw new DOMException((short) 17, messages.createMessage(str, objArr));
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_SCHEMA_LOCATION) || name.equalsIgnoreCase(DOMConstants.DOM_SCHEMA_TYPE)) {
            if (value == null) {
                return;
            }
            if (value instanceof String) {
                messages = Utils.messages;
                str = MsgKey.ER_FEATURE_NOT_SUPPORTED;
                objArr = new Object[CANONICAL];
                objArr[0] = name;
                throw new DOMException((short) 9, messages.createMessage(str, objArr));
            }
            messages = Utils.messages;
            str = MsgKey.ER_TYPE_MISMATCH_ERR;
            objArr = new Object[CANONICAL];
            objArr[0] = name;
            throw new DOMException((short) 17, messages.createMessage(str, objArr));
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_COMMENTS) || name.equalsIgnoreCase(DOMConstants.DOM_CDATA_SECTIONS) || name.equalsIgnoreCase(DOMConstants.DOM_ENTITIES) || name.equalsIgnoreCase(DOMConstants.DOM_NAMESPACES) || name.equalsIgnoreCase(DOMConstants.DOM_NAMESPACE_DECLARATIONS) || name.equalsIgnoreCase(DOMConstants.DOM_SPLIT_CDATA) || name.equalsIgnoreCase(DOMConstants.DOM_WELLFORMED) || name.equalsIgnoreCase(DOMConstants.DOM_DISCARD_DEFAULT_CONTENT) || name.equalsIgnoreCase(DOMConstants.DOM_FORMAT_PRETTY_PRINT) || name.equalsIgnoreCase(DOMConstants.DOM_XMLDECL) || name.equalsIgnoreCase(DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE) || name.equalsIgnoreCase(DOMConstants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS) || name.equalsIgnoreCase(DOMConstants.DOM_CANONICAL_FORM) || name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE_IF_SCHEMA) || name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE) || name.equalsIgnoreCase(DOMConstants.DOM_CHECK_CHAR_NORMALIZATION) || name.equalsIgnoreCase(DOMConstants.DOM_DATATYPE_NORMALIZATION) || name.equalsIgnoreCase(DOMConstants.DOM_INFOSET)) {
            messages = Utils.messages;
            str = MsgKey.ER_TYPE_MISMATCH_ERR;
            objArr = new Object[CANONICAL];
            objArr[0] = name;
            throw new DOMException((short) 17, messages.createMessage(str, objArr));
        } else {
            messages = Utils.messages;
            str = MsgKey.ER_FEATURE_NOT_FOUND;
            objArr = new Object[CANONICAL];
            objArr[0] = name;
            throw new DOMException((short) 8, messages.createMessage(str, objArr));
        }
    }

    public DOMConfiguration getDomConfig() {
        return this;
    }

    public LSSerializerFilter getFilter() {
        return this.fSerializerFilter;
    }

    public String getNewLine() {
        return this.fEndOfLine;
    }

    public void setFilter(LSSerializerFilter filter) {
        this.fSerializerFilter = filter;
    }

    public void setNewLine(String newLine) {
        if (newLine == null) {
            newLine = DEFAULT_END_OF_LINE;
        }
        this.fEndOfLine = newLine;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean write(Node nodeArg, LSOutput destination) throws LSException {
        String msg;
        if (destination == null) {
            msg = Utils.messages.createMessage(MsgKey.ER_NO_OUTPUT_SPECIFIED, null);
            if (this.fDOMErrorHandler != null) {
                this.fDOMErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_NO_OUTPUT_SPECIFIED));
            }
            throw new LSException((short) 82, msg);
        } else if (nodeArg == null) {
            return false;
        } else {
            Serializer serializer = this.fXMLSerializer;
            serializer.reset();
            if (nodeArg != this.fVisitedNode) {
                String xmlVersion = getXMLVersion(nodeArg);
                this.fEncoding = destination.getEncoding();
                if (this.fEncoding == null) {
                    this.fEncoding = getInputEncoding(nodeArg);
                    String xMLEncoding = this.fEncoding != null ? this.fEncoding : getXMLEncoding(nodeArg) == null ? "UTF-8" : getXMLEncoding(nodeArg);
                    this.fEncoding = xMLEncoding;
                }
                if (Encodings.isRecognizedEncoding(this.fEncoding)) {
                    serializer.getOutputFormat().setProperty(Constants.ATTRNAME_VERSION, xmlVersion);
                    this.fDOMConfigProperties.setProperty("{http://xml.apache.org/xerces-2j}xml-version", xmlVersion);
                    this.fDOMConfigProperties.setProperty(DOMConstants.S_XSL_OUTPUT_ENCODING, this.fEncoding);
                    if (nodeArg.getNodeType() == (short) 9 && nodeArg.getNodeType() == CANONICAL) {
                        if (nodeArg.getNodeType() != (short) 6) {
                        }
                        this.fVisitedNode = nodeArg;
                    }
                    if ((this.fFeatures & XMLDECL) != 0) {
                        this.fDOMConfigProperties.setProperty(DOMConstants.S_XSL_OUTPUT_OMIT_XML_DECL, DOMConstants.DOM3_DEFAULT_FALSE);
                    }
                    this.fVisitedNode = nodeArg;
                } else {
                    msg = Utils.messages.createMessage(MsgKey.ER_UNSUPPORTED_ENCODING, null);
                    if (this.fDOMErrorHandler != null) {
                        this.fDOMErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_UNSUPPORTED_ENCODING));
                    }
                    throw new LSException((short) 82, msg);
                }
            }
            this.fXMLSerializer.setOutputFormat(this.fDOMConfigProperties);
            try {
                Writer writer = destination.getCharacterStream();
                if (writer == null) {
                    OutputStream outputStream = destination.getByteStream();
                    if (outputStream == null) {
                        String uri = destination.getSystemId();
                        if (uri == null) {
                            msg = Utils.messages.createMessage(MsgKey.ER_NO_OUTPUT_SPECIFIED, null);
                            if (this.fDOMErrorHandler != null) {
                                this.fDOMErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_NO_OUTPUT_SPECIFIED));
                            }
                            throw new LSException((short) 82, msg);
                        }
                        URL url = new URL(SystemIDResolver.getAbsoluteURI(uri));
                        String protocol = url.getProtocol();
                        String host = url.getHost();
                        if (protocol.equalsIgnoreCase("file")) {
                            if (!(host == null || host.length() == 0)) {
                            }
                            OutputStream fileOutputStream = new FileOutputStream(getPathWithoutEscapes(url.getPath()));
                            serializer.setOutputStream(urlOutStream);
                        }
                        URLConnection urlCon = url.openConnection();
                        urlCon.setDoInput(false);
                        urlCon.setDoOutput(true);
                        urlCon.setUseCaches(false);
                        urlCon.setAllowUserInteraction(false);
                        if (urlCon instanceof HttpURLConnection) {
                            ((HttpURLConnection) urlCon).setRequestMethod("PUT");
                        }
                        OutputStream urlOutStream = urlCon.getOutputStream();
                        serializer.setOutputStream(urlOutStream);
                    } else {
                        serializer.setOutputStream(outputStream);
                    }
                } else {
                    serializer.setWriter(writer);
                }
                if (this.fDOMSerializer == null) {
                    this.fDOMSerializer = (DOM3Serializer) serializer.asDOM3Serializer();
                }
                if (this.fDOMErrorHandler != null) {
                    this.fDOMSerializer.setErrorHandler(this.fDOMErrorHandler);
                }
                if (this.fSerializerFilter != null) {
                    this.fDOMSerializer.setNodeFilter(this.fSerializerFilter);
                }
                this.fDOMSerializer.setNewLine(this.fEndOfLine.toCharArray());
                this.fDOMSerializer.serializeDOM3(nodeArg);
                return true;
            } catch (UnsupportedEncodingException ue) {
                msg = Utils.messages.createMessage(MsgKey.ER_UNSUPPORTED_ENCODING, null);
                if (this.fDOMErrorHandler != null) {
                    this.fDOMErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_UNSUPPORTED_ENCODING, ue));
                }
                throw ((LSException) createLSException((short) 82, ue).fillInStackTrace());
            } catch (LSException lse) {
                throw lse;
            } catch (RuntimeException e) {
                throw ((LSException) createLSException((short) 82, e).fillInStackTrace());
            } catch (Exception e2) {
                if (this.fDOMErrorHandler != null) {
                    this.fDOMErrorHandler.handleError(new DOMErrorImpl((short) 3, e2.getMessage(), null, e2));
                }
                throw ((LSException) createLSException((short) 82, e2).fillInStackTrace());
            }
        }
    }

    public String writeToString(Node nodeArg) throws DOMException, LSException {
        if (nodeArg == null) {
            return null;
        }
        Serializer serializer = this.fXMLSerializer;
        serializer.reset();
        if (nodeArg != this.fVisitedNode) {
            String xmlVersion = getXMLVersion(nodeArg);
            serializer.getOutputFormat().setProperty(Constants.ATTRNAME_VERSION, xmlVersion);
            this.fDOMConfigProperties.setProperty("{http://xml.apache.org/xerces-2j}xml-version", xmlVersion);
            this.fDOMConfigProperties.setProperty(DOMConstants.S_XSL_OUTPUT_ENCODING, "UTF-16");
            if (nodeArg.getNodeType() == (short) 9 && nodeArg.getNodeType() == (short) 1) {
                if (nodeArg.getNodeType() != (short) 6) {
                }
                this.fVisitedNode = nodeArg;
            }
            if ((this.fFeatures & XMLDECL) != 0) {
                this.fDOMConfigProperties.setProperty(DOMConstants.S_XSL_OUTPUT_OMIT_XML_DECL, DOMConstants.DOM3_DEFAULT_FALSE);
            }
            this.fVisitedNode = nodeArg;
        }
        this.fXMLSerializer.setOutputFormat(this.fDOMConfigProperties);
        StringWriter output = new StringWriter();
        try {
            serializer.setWriter(output);
            if (this.fDOMSerializer == null) {
                this.fDOMSerializer = (DOM3Serializer) serializer.asDOM3Serializer();
            }
            if (this.fDOMErrorHandler != null) {
                this.fDOMSerializer.setErrorHandler(this.fDOMErrorHandler);
            }
            if (this.fSerializerFilter != null) {
                this.fDOMSerializer.setNodeFilter(this.fSerializerFilter);
            }
            this.fDOMSerializer.setNewLine(this.fEndOfLine.toCharArray());
            this.fDOMSerializer.serializeDOM3(nodeArg);
            return output.toString();
        } catch (LSException lse) {
            throw lse;
        } catch (RuntimeException e) {
            throw ((LSException) createLSException((short) 82, e).fillInStackTrace());
        } catch (Exception e2) {
            if (this.fDOMErrorHandler != null) {
                this.fDOMErrorHandler.handleError(new DOMErrorImpl((short) 3, e2.getMessage(), null, e2));
            }
            throw ((LSException) createLSException((short) 82, e2).fillInStackTrace());
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean writeToURI(Node nodeArg, String uri) throws LSException {
        if (nodeArg == null) {
            return false;
        }
        Serializer serializer = this.fXMLSerializer;
        serializer.reset();
        if (nodeArg != this.fVisitedNode) {
            String xmlVersion = getXMLVersion(nodeArg);
            this.fEncoding = getInputEncoding(nodeArg);
            if (this.fEncoding == null) {
                String xMLEncoding = this.fEncoding != null ? this.fEncoding : getXMLEncoding(nodeArg) == null ? "UTF-8" : getXMLEncoding(nodeArg);
                this.fEncoding = xMLEncoding;
            }
            serializer.getOutputFormat().setProperty(Constants.ATTRNAME_VERSION, xmlVersion);
            this.fDOMConfigProperties.setProperty("{http://xml.apache.org/xerces-2j}xml-version", xmlVersion);
            this.fDOMConfigProperties.setProperty(DOMConstants.S_XSL_OUTPUT_ENCODING, this.fEncoding);
            if (nodeArg.getNodeType() == (short) 9 && nodeArg.getNodeType() == CANONICAL) {
                if (nodeArg.getNodeType() != (short) 6) {
                }
                this.fVisitedNode = nodeArg;
            }
            if ((this.fFeatures & XMLDECL) != 0) {
                this.fDOMConfigProperties.setProperty(DOMConstants.S_XSL_OUTPUT_OMIT_XML_DECL, DOMConstants.DOM3_DEFAULT_FALSE);
            }
            this.fVisitedNode = nodeArg;
        }
        this.fXMLSerializer.setOutputFormat(this.fDOMConfigProperties);
        if (uri == null) {
            try {
                String msg = Utils.messages.createMessage(MsgKey.ER_NO_OUTPUT_SPECIFIED, null);
                if (this.fDOMErrorHandler != null) {
                    this.fDOMErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_NO_OUTPUT_SPECIFIED));
                }
                throw new LSException((short) 82, msg);
            } catch (LSException lse) {
                throw lse;
            } catch (RuntimeException e) {
                throw ((LSException) createLSException((short) 82, e).fillInStackTrace());
            } catch (Exception e2) {
                if (this.fDOMErrorHandler != null) {
                    this.fDOMErrorHandler.handleError(new DOMErrorImpl((short) 3, e2.getMessage(), null, e2));
                }
                throw ((LSException) createLSException((short) 82, e2).fillInStackTrace());
            }
        }
        OutputStream urlOutStream;
        URL url = new URL(SystemIDResolver.getAbsoluteURI(uri));
        String protocol = url.getProtocol();
        String host = url.getHost();
        if (protocol.equalsIgnoreCase("file")) {
            if (!(host == null || host.length() == 0)) {
            }
            urlOutStream = new FileOutputStream(getPathWithoutEscapes(url.getPath()));
            serializer.setOutputStream(urlOutStream);
            if (this.fDOMSerializer == null) {
                this.fDOMSerializer = (DOM3Serializer) serializer.asDOM3Serializer();
            }
            if (this.fDOMErrorHandler != null) {
                this.fDOMSerializer.setErrorHandler(this.fDOMErrorHandler);
            }
            if (this.fSerializerFilter != null) {
                this.fDOMSerializer.setNodeFilter(this.fSerializerFilter);
            }
            this.fDOMSerializer.setNewLine(this.fEndOfLine.toCharArray());
            this.fDOMSerializer.serializeDOM3(nodeArg);
            return true;
        }
        URLConnection urlCon = url.openConnection();
        urlCon.setDoInput(false);
        urlCon.setDoOutput(true);
        urlCon.setUseCaches(false);
        urlCon.setAllowUserInteraction(false);
        if (urlCon instanceof HttpURLConnection) {
            ((HttpURLConnection) urlCon).setRequestMethod("PUT");
        }
        urlOutStream = urlCon.getOutputStream();
        serializer.setOutputStream(urlOutStream);
        if (this.fDOMSerializer == null) {
            this.fDOMSerializer = (DOM3Serializer) serializer.asDOM3Serializer();
        }
        if (this.fDOMErrorHandler != null) {
            this.fDOMSerializer.setErrorHandler(this.fDOMErrorHandler);
        }
        if (this.fSerializerFilter != null) {
            this.fDOMSerializer.setNodeFilter(this.fSerializerFilter);
        }
        this.fDOMSerializer.setNewLine(this.fEndOfLine.toCharArray());
        this.fDOMSerializer.serializeDOM3(nodeArg);
        return true;
    }

    protected String getXMLVersion(Node nodeArg) {
        if (nodeArg != null) {
            Document doc;
            if (nodeArg.getNodeType() == (short) 9) {
                doc = (Document) nodeArg;
            } else {
                doc = nodeArg.getOwnerDocument();
            }
            if (doc != null && doc.getImplementation().hasFeature("Core", "3.0")) {
                return doc.getXmlVersion();
            }
        }
        return SerializerConstants.XMLVERSION10;
    }

    protected String getXMLEncoding(Node nodeArg) {
        if (nodeArg != null) {
            Document doc;
            if (nodeArg.getNodeType() == (short) 9) {
                doc = (Document) nodeArg;
            } else {
                doc = nodeArg.getOwnerDocument();
            }
            if (doc != null && doc.getImplementation().hasFeature("Core", "3.0")) {
                return doc.getXmlEncoding();
            }
        }
        return "UTF-8";
    }

    protected String getInputEncoding(Node nodeArg) {
        if (nodeArg != null) {
            Document doc;
            if (nodeArg.getNodeType() == (short) 9) {
                doc = (Document) nodeArg;
            } else {
                doc = nodeArg.getOwnerDocument();
            }
            if (doc != null && doc.getImplementation().hasFeature("Core", "3.0")) {
                return doc.getInputEncoding();
            }
        }
        return null;
    }

    public DOMErrorHandler getErrorHandler() {
        return this.fDOMErrorHandler;
    }

    private static String getPathWithoutEscapes(String origPath) {
        if (origPath == null || origPath.length() == 0 || origPath.indexOf(37) == -1) {
            return origPath;
        }
        StringTokenizer tokenizer = new StringTokenizer(origPath, "%");
        StringBuffer result = new StringBuffer(origPath.length());
        int size = tokenizer.countTokens();
        result.append(tokenizer.nextToken());
        for (int i = CANONICAL; i < size; i += CANONICAL) {
            String token = tokenizer.nextToken();
            if (token.length() >= CDATA && isHexDigit(token.charAt(0)) && isHexDigit(token.charAt(CANONICAL))) {
                result.append((char) Integer.valueOf(token.substring(0, CDATA), DTNORMALIZE).intValue());
                token = token.substring(CDATA);
            }
            result.append(token);
        }
        return result.toString();
    }

    private static boolean isHexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return true;
        }
        if (c >= 'a' && c <= 'f') {
            return true;
        }
        if (c < 'A' || c > 'F') {
            return false;
        }
        return true;
    }

    private static LSException createLSException(short code, Throwable cause) {
        String str = null;
        if (cause != null) {
            str = cause.getMessage();
        }
        LSException lse = new LSException(code, str);
        if (cause != null && ThrowableMethods.fgThrowableMethodsAvailable) {
            try {
                Method -get0 = ThrowableMethods.fgThrowableInitCauseMethod;
                Object[] objArr = new Object[CANONICAL];
                objArr[0] = cause;
                -get0.invoke(lse, objArr);
            } catch (Exception e) {
            }
        }
        return lse;
    }
}
