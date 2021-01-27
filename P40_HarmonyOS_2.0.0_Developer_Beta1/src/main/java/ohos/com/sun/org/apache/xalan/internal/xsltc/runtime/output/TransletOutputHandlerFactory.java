package ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.output;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SAX2DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXEventWriter;
import ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXStreamWriter;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import ohos.com.sun.org.apache.xml.internal.serializer.ToHTMLSAXHandler;
import ohos.com.sun.org.apache.xml.internal.serializer.ToHTMLStream;
import ohos.com.sun.org.apache.xml.internal.serializer.ToTextSAXHandler;
import ohos.com.sun.org.apache.xml.internal.serializer.ToTextStream;
import ohos.com.sun.org.apache.xml.internal.serializer.ToUnknownStream;
import ohos.com.sun.org.apache.xml.internal.serializer.ToXMLSAXHandler;
import ohos.com.sun.org.apache.xml.internal.serializer.ToXMLStream;
import ohos.com.sun.xml.internal.stream.writers.WriterUtility;
import ohos.javax.xml.parsers.ParserConfigurationException;
import ohos.javax.xml.stream.XMLEventWriter;
import ohos.javax.xml.stream.XMLStreamWriter;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.ext.LexicalHandler;

public class TransletOutputHandlerFactory {
    public static final int DOM = 2;
    public static final int SAX = 1;
    public static final int STAX = 3;
    public static final int STREAM = 0;
    private String _encoding = WriterUtility.UTF_8;
    private ContentHandler _handler = null;
    private int _indentNumber = -1;
    private LexicalHandler _lexHandler = null;
    private String _method = null;
    private Node _nextSibling = null;
    private Node _node = null;
    private OutputStream _ostream = System.out;
    private int _outputType = 0;
    private boolean _overrideDefaultParser;
    private Writer _writer = null;
    private XMLEventWriter _xmlStAXEventWriter = null;
    private XMLStreamWriter _xmlStAXStreamWriter = null;

    public static TransletOutputHandlerFactory newInstance() {
        return new TransletOutputHandlerFactory(true);
    }

    public static TransletOutputHandlerFactory newInstance(boolean z) {
        return new TransletOutputHandlerFactory(z);
    }

    public TransletOutputHandlerFactory(boolean z) {
        this._overrideDefaultParser = z;
    }

    public void setOutputType(int i) {
        this._outputType = i;
    }

    public void setEncoding(String str) {
        if (str != null) {
            this._encoding = str;
        }
    }

    public void setOutputMethod(String str) {
        this._method = str;
    }

    public void setOutputStream(OutputStream outputStream) {
        this._ostream = outputStream;
    }

    public void setWriter(Writer writer) {
        this._writer = writer;
    }

    public void setHandler(ContentHandler contentHandler) {
        this._handler = contentHandler;
    }

    public void setLexicalHandler(LexicalHandler lexicalHandler) {
        this._lexHandler = lexicalHandler;
    }

    public void setNode(Node node) {
        this._node = node;
    }

    public Node getNode() {
        SAX2DOM sax2dom = this._handler;
        if (sax2dom instanceof SAX2DOM) {
            return sax2dom.getDOM();
        }
        return null;
    }

    public void setNextSibling(Node node) {
        this._nextSibling = node;
    }

    public XMLEventWriter getXMLEventWriter() {
        SAX2StAXEventWriter sAX2StAXEventWriter = this._handler;
        if (sAX2StAXEventWriter instanceof SAX2StAXEventWriter) {
            return sAX2StAXEventWriter.getEventWriter();
        }
        return null;
    }

    public void setXMLEventWriter(XMLEventWriter xMLEventWriter) {
        this._xmlStAXEventWriter = xMLEventWriter;
    }

    public XMLStreamWriter getXMLStreamWriter() {
        SAX2StAXStreamWriter sAX2StAXStreamWriter = this._handler;
        if (sAX2StAXStreamWriter instanceof SAX2StAXStreamWriter) {
            return sAX2StAXStreamWriter.getStreamWriter();
        }
        return null;
    }

    public void setXMLStreamWriter(XMLStreamWriter xMLStreamWriter) {
        this._xmlStAXStreamWriter = xMLStreamWriter;
    }

    public void setIndentNumber(int i) {
        this._indentNumber = i;
    }

    public SerializationHandler getSerializationHandler() throws IOException, ParserConfigurationException {
        int i;
        SAX2DOM sax2dom;
        int i2 = this._outputType;
        SerializationHandler serializationHandler = null;
        if (i2 != 0) {
            if (i2 != 1) {
                if (i2 == 2) {
                    Node node = this._node;
                    if (node != null) {
                        sax2dom = new SAX2DOM(node, this._nextSibling, this._overrideDefaultParser);
                    } else {
                        sax2dom = new SAX2DOM(this._overrideDefaultParser);
                    }
                    this._handler = sax2dom;
                    this._lexHandler = this._handler;
                } else if (i2 != 3) {
                    return null;
                }
                XMLEventWriter xMLEventWriter = this._xmlStAXEventWriter;
                if (xMLEventWriter != null) {
                    this._handler = new SAX2StAXEventWriter(xMLEventWriter);
                } else {
                    XMLStreamWriter xMLStreamWriter = this._xmlStAXStreamWriter;
                    if (xMLStreamWriter != null) {
                        this._handler = new SAX2StAXStreamWriter(xMLStreamWriter);
                    }
                }
                this._lexHandler = this._handler;
            }
            if (this._method == null) {
                this._method = "xml";
            }
            if (this._method.equalsIgnoreCase("xml")) {
                LexicalHandler lexicalHandler = this._lexHandler;
                if (lexicalHandler == null) {
                    return new ToXMLSAXHandler(this._handler, this._encoding);
                }
                return new ToXMLSAXHandler(this._handler, lexicalHandler, this._encoding);
            } else if (this._method.equalsIgnoreCase("html")) {
                LexicalHandler lexicalHandler2 = this._lexHandler;
                if (lexicalHandler2 == null) {
                    return new ToHTMLSAXHandler(this._handler, this._encoding);
                }
                return new ToHTMLSAXHandler(this._handler, lexicalHandler2, this._encoding);
            } else if (!this._method.equalsIgnoreCase("text")) {
                return null;
            } else {
                LexicalHandler lexicalHandler3 = this._lexHandler;
                if (lexicalHandler3 == null) {
                    return new ToTextSAXHandler(this._handler, this._encoding);
                }
                return new ToTextSAXHandler(this._handler, lexicalHandler3, this._encoding);
            }
        } else {
            String str = this._method;
            if (str == null) {
                serializationHandler = new ToUnknownStream();
            } else if (str.equalsIgnoreCase("xml")) {
                serializationHandler = new ToXMLStream();
            } else if (this._method.equalsIgnoreCase("html")) {
                serializationHandler = new ToHTMLStream();
            } else if (this._method.equalsIgnoreCase("text")) {
                serializationHandler = new ToTextStream();
            }
            if (serializationHandler != null && (i = this._indentNumber) >= 0) {
                serializationHandler.setIndentAmount(i);
            }
            serializationHandler.setEncoding(this._encoding);
            Writer writer = this._writer;
            if (writer != null) {
                serializationHandler.setWriter(writer);
            } else {
                serializationHandler.setOutputStream(this._ostream);
            }
            return serializationHandler;
        }
    }
}
