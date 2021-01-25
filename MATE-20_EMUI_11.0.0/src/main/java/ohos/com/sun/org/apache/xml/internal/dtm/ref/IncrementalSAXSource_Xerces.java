package ohos.com.sun.org.apache.xml.internal.dtm.ref;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import ohos.com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import ohos.com.sun.org.apache.xerces.internal.parsers.SAXParser;
import ohos.com.sun.org.apache.xml.internal.res.XMLMessages;
import ohos.com.sun.org.apache.xml.internal.serialize.OutputFormat;
import ohos.com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;
import ohos.org.xml.sax.ext.LexicalHandler;

public class IncrementalSAXSource_Xerces implements IncrementalSAXSource {
    private static final Object[] noparms = new Object[0];
    private static final Object[] parmsfalse = {Boolean.FALSE};
    Constructor fConfigInputSourceCtor = null;
    Method fConfigParse = null;
    Method fConfigSetByteStream = null;
    Method fConfigSetCharStream = null;
    Method fConfigSetEncoding = null;
    Method fConfigSetInput = null;
    SAXParser fIncrementalParser;
    private boolean fParseInProgress = false;
    Method fParseSome = null;
    Method fParseSomeSetup = null;
    Object fPullParserConfig = null;
    Method fReset = null;
    Method fSetInputSource = null;

    public IncrementalSAXSource_Xerces() throws NoSuchMethodException {
        try {
            Constructor constructor = SAXParser.class.getConstructor(ObjectFactory.findProviderClass("ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration", true));
            Class<?> findProviderClass = ObjectFactory.findProviderClass("ohos.com.sun.org.apache.xerces.internal.parsers.StandardParserConfiguration", true);
            this.fPullParserConfig = findProviderClass.newInstance();
            this.fIncrementalParser = (SAXParser) constructor.newInstance(this.fPullParserConfig);
            Class<?> findProviderClass2 = ObjectFactory.findProviderClass("ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource", true);
            this.fConfigSetInput = findProviderClass.getMethod("setInputSource", findProviderClass2);
            this.fConfigInputSourceCtor = findProviderClass2.getConstructor(String.class, String.class, String.class);
            this.fConfigSetByteStream = findProviderClass2.getMethod("setByteStream", InputStream.class);
            this.fConfigSetCharStream = findProviderClass2.getMethod("setCharacterStream", Reader.class);
            this.fConfigSetEncoding = findProviderClass2.getMethod("setEncoding", String.class);
            this.fConfigParse = findProviderClass.getMethod("parse", Boolean.TYPE);
            this.fReset = this.fIncrementalParser.getClass().getMethod(Constants.RESET, new Class[0]);
        } catch (Exception unused) {
            IncrementalSAXSource_Xerces incrementalSAXSource_Xerces = new IncrementalSAXSource_Xerces(new SAXParser());
            this.fParseSomeSetup = incrementalSAXSource_Xerces.fParseSomeSetup;
            this.fParseSome = incrementalSAXSource_Xerces.fParseSome;
            this.fIncrementalParser = incrementalSAXSource_Xerces.fIncrementalParser;
        }
    }

    public IncrementalSAXSource_Xerces(SAXParser sAXParser) throws NoSuchMethodException {
        this.fIncrementalParser = sAXParser;
        Class<?> cls = sAXParser.getClass();
        this.fParseSomeSetup = cls.getMethod("parseSomeSetup", InputSource.class);
        this.fParseSome = cls.getMethod("parseSome", new Class[0]);
    }

    public static IncrementalSAXSource createIncrementalSAXSource() {
        try {
            return new IncrementalSAXSource_Xerces();
        } catch (NoSuchMethodException unused) {
            IncrementalSAXSource_Filter incrementalSAXSource_Filter = new IncrementalSAXSource_Filter();
            incrementalSAXSource_Filter.setXMLReader(new SAXParser());
            return incrementalSAXSource_Filter;
        }
    }

    public static IncrementalSAXSource createIncrementalSAXSource(SAXParser sAXParser) {
        try {
            return new IncrementalSAXSource_Xerces(sAXParser);
        } catch (NoSuchMethodException unused) {
            IncrementalSAXSource_Filter incrementalSAXSource_Filter = new IncrementalSAXSource_Filter();
            incrementalSAXSource_Filter.setXMLReader(sAXParser);
            return incrementalSAXSource_Filter;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource
    public void setContentHandler(ContentHandler contentHandler) {
        this.fIncrementalParser.setContentHandler(contentHandler);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource
    public void setLexicalHandler(LexicalHandler lexicalHandler) {
        try {
            this.fIncrementalParser.setProperty("http://xml.org/sax/properties/lexical-handler", lexicalHandler);
        } catch (SAXNotRecognizedException | SAXNotSupportedException unused) {
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource
    public void setDTDHandler(DTDHandler dTDHandler) {
        this.fIncrementalParser.setDTDHandler(dTDHandler);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource
    public void startParse(InputSource inputSource) throws SAXException {
        if (this.fIncrementalParser == null) {
            throw new SAXException(XMLMessages.createXMLMessage("ER_STARTPARSE_NEEDS_SAXPARSER", null));
        } else if (!this.fParseInProgress) {
            try {
                if (!parseSomeSetup(inputSource)) {
                    throw new SAXException(XMLMessages.createXMLMessage("ER_COULD_NOT_INIT_PARSER", null));
                }
            } catch (Exception e) {
                throw new SAXException(e);
            }
        } else {
            throw new SAXException(XMLMessages.createXMLMessage("ER_STARTPARSE_WHILE_PARSING", null));
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource
    public Object deliverMoreNodes(boolean z) {
        if (!z) {
            this.fParseInProgress = false;
            return Boolean.FALSE;
        }
        try {
            return parseSome() ? Boolean.TRUE : Boolean.FALSE;
        } catch (IOException | SAXException e) {
            return e;
        } catch (Exception e2) {
            return new SAXException(e2);
        }
    }

    private boolean parseSomeSetup(InputSource inputSource) throws SAXException, IOException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (this.fConfigSetInput == null) {
            return ((Boolean) this.fParseSomeSetup.invoke(this.fIncrementalParser, inputSource)).booleanValue();
        }
        Object newInstance = this.fConfigInputSourceCtor.newInstance(inputSource.getPublicId(), inputSource.getSystemId(), null);
        Object[] objArr = {inputSource.getByteStream()};
        this.fConfigSetByteStream.invoke(newInstance, objArr);
        objArr[0] = inputSource.getCharacterStream();
        this.fConfigSetCharStream.invoke(newInstance, objArr);
        objArr[0] = inputSource.getEncoding();
        this.fConfigSetEncoding.invoke(newInstance, objArr);
        this.fReset.invoke(this.fIncrementalParser, new Object[0]);
        objArr[0] = newInstance;
        this.fConfigSetInput.invoke(this.fPullParserConfig, objArr);
        return parseSome();
    }

    private boolean parseSome() throws SAXException, IOException, IllegalAccessException, InvocationTargetException {
        if (this.fConfigSetInput != null) {
            return ((Boolean) this.fConfigParse.invoke(this.fPullParserConfig, parmsfalse)).booleanValue();
        }
        return ((Boolean) this.fParseSome.invoke(this.fIncrementalParser, noparms)).booleanValue();
    }

    public static void _main(String[] strArr) {
        System.out.println("Starting...");
        if (new CoroutineManager().co_joinCoroutineSet(-1) == -1) {
            System.out.println("ERROR: Couldn't allocate coroutine number.\n");
            return;
        }
        IncrementalSAXSource createIncrementalSAXSource = createIncrementalSAXSource();
        XMLSerializer xMLSerializer = new XMLSerializer(System.out, (OutputFormat) null);
        createIncrementalSAXSource.setContentHandler(xMLSerializer);
        createIncrementalSAXSource.setLexicalHandler(xMLSerializer);
        int i = 0;
        while (i < strArr.length) {
            try {
                createIncrementalSAXSource.startParse(new InputSource(strArr[i]));
                Object deliverMoreNodes = createIncrementalSAXSource.deliverMoreNodes(true);
                boolean z = true;
                while (deliverMoreNodes == Boolean.TRUE) {
                    System.out.println("\nSome parsing successful, trying more.\n");
                    int i2 = i + 1;
                    if (i2 < strArr.length && "!".equals(strArr[i2])) {
                        z = false;
                        i = i2;
                    }
                    deliverMoreNodes = createIncrementalSAXSource.deliverMoreNodes(z);
                }
                if (!(deliverMoreNodes instanceof Boolean) || ((Boolean) deliverMoreNodes) != Boolean.FALSE) {
                    if (deliverMoreNodes == null) {
                        System.out.println("\nUNEXPECTED: Parser says shut down prematurely.\n");
                    } else if (deliverMoreNodes instanceof Exception) {
                        throw new WrappedRuntimeException((Exception) deliverMoreNodes);
                    }
                    i++;
                } else {
                    System.out.println("\nParser ended (EOF or on request).\n");
                    i++;
                }
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }
    }
}
