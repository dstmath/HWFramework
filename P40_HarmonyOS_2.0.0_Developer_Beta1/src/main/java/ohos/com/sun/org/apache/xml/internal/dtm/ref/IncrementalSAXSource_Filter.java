package ohos.com.sun.org.apache.xml.internal.dtm.ref;

import java.io.IOException;
import java.io.PrintStream;
import ohos.com.sun.org.apache.xml.internal.res.XMLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.ThreadControllerWrapper;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;
import ohos.org.xml.sax.SAXParseException;
import ohos.org.xml.sax.XMLReader;
import ohos.org.xml.sax.ext.LexicalHandler;

/* access modifiers changed from: package-private */
public final class IncrementalSAXSource_Filter implements IncrementalSAXSource, ContentHandler, DTDHandler, LexicalHandler, ErrorHandler, Runnable {
    boolean DEBUG = false;
    private ContentHandler clientContentHandler = null;
    private DTDHandler clientDTDHandler = null;
    private ErrorHandler clientErrorHandler = null;
    private LexicalHandler clientLexicalHandler = null;
    private int eventcounter;
    private int fControllerCoroutineID = -1;
    private CoroutineManager fCoroutineManager = null;
    private boolean fNoMoreEvents = false;
    private int fSourceCoroutineID = -1;
    private XMLReader fXMLReader = null;
    private InputSource fXMLReaderInputSource = null;
    private int frequency = 5;

    public IncrementalSAXSource_Filter() {
        init(new CoroutineManager(), -1, -1);
    }

    public IncrementalSAXSource_Filter(CoroutineManager coroutineManager, int i) {
        init(coroutineManager, i, -1);
    }

    public static IncrementalSAXSource createIncrementalSAXSource(CoroutineManager coroutineManager, int i) {
        return new IncrementalSAXSource_Filter(coroutineManager, i);
    }

    public void init(CoroutineManager coroutineManager, int i, int i2) {
        if (coroutineManager == null) {
            coroutineManager = new CoroutineManager();
        }
        this.fCoroutineManager = coroutineManager;
        this.fControllerCoroutineID = coroutineManager.co_joinCoroutineSet(i);
        this.fSourceCoroutineID = coroutineManager.co_joinCoroutineSet(i2);
        if (this.fControllerCoroutineID == -1 || this.fSourceCoroutineID == -1) {
            throw new RuntimeException(XMLMessages.createXMLMessage("ER_COJOINROUTINESET_FAILED", null));
        }
        this.fNoMoreEvents = false;
        this.eventcounter = this.frequency;
    }

    public void setXMLReader(XMLReader xMLReader) {
        this.fXMLReader = xMLReader;
        xMLReader.setContentHandler(this);
        xMLReader.setDTDHandler(this);
        xMLReader.setErrorHandler(this);
        try {
            xMLReader.setProperty("http://xml.org/sax/properties/lexical-handler", this);
        } catch (SAXNotRecognizedException | SAXNotSupportedException unused) {
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource
    public void setContentHandler(ContentHandler contentHandler) {
        this.clientContentHandler = contentHandler;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource
    public void setDTDHandler(DTDHandler dTDHandler) {
        this.clientDTDHandler = dTDHandler;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource
    public void setLexicalHandler(LexicalHandler lexicalHandler) {
        this.clientLexicalHandler = lexicalHandler;
    }

    public void setErrHandler(ErrorHandler errorHandler) {
        this.clientErrorHandler = errorHandler;
    }

    public void setReturnFrequency(int i) {
        if (i < 1) {
            i = 1;
        }
        this.eventcounter = i;
        this.frequency = i;
    }

    public void characters(char[] cArr, int i, int i2) throws SAXException {
        int i3 = this.eventcounter - 1;
        this.eventcounter = i3;
        if (i3 <= 0) {
            co_yield(true);
            this.eventcounter = this.frequency;
        }
        ContentHandler contentHandler = this.clientContentHandler;
        if (contentHandler != null) {
            contentHandler.characters(cArr, i, i2);
        }
    }

    public void endDocument() throws SAXException {
        ContentHandler contentHandler = this.clientContentHandler;
        if (contentHandler != null) {
            contentHandler.endDocument();
        }
        this.eventcounter = 0;
        co_yield(false);
    }

    public void endElement(String str, String str2, String str3) throws SAXException {
        int i = this.eventcounter - 1;
        this.eventcounter = i;
        if (i <= 0) {
            co_yield(true);
            this.eventcounter = this.frequency;
        }
        ContentHandler contentHandler = this.clientContentHandler;
        if (contentHandler != null) {
            contentHandler.endElement(str, str2, str3);
        }
    }

    public void endPrefixMapping(String str) throws SAXException {
        int i = this.eventcounter - 1;
        this.eventcounter = i;
        if (i <= 0) {
            co_yield(true);
            this.eventcounter = this.frequency;
        }
        ContentHandler contentHandler = this.clientContentHandler;
        if (contentHandler != null) {
            contentHandler.endPrefixMapping(str);
        }
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        int i3 = this.eventcounter - 1;
        this.eventcounter = i3;
        if (i3 <= 0) {
            co_yield(true);
            this.eventcounter = this.frequency;
        }
        ContentHandler contentHandler = this.clientContentHandler;
        if (contentHandler != null) {
            contentHandler.ignorableWhitespace(cArr, i, i2);
        }
    }

    public void processingInstruction(String str, String str2) throws SAXException {
        int i = this.eventcounter - 1;
        this.eventcounter = i;
        if (i <= 0) {
            co_yield(true);
            this.eventcounter = this.frequency;
        }
        ContentHandler contentHandler = this.clientContentHandler;
        if (contentHandler != null) {
            contentHandler.processingInstruction(str, str2);
        }
    }

    public void setDocumentLocator(Locator locator) {
        int i = this.eventcounter - 1;
        this.eventcounter = i;
        if (i <= 0) {
            this.eventcounter = this.frequency;
        }
        ContentHandler contentHandler = this.clientContentHandler;
        if (contentHandler != null) {
            contentHandler.setDocumentLocator(locator);
        }
    }

    public void skippedEntity(String str) throws SAXException {
        int i = this.eventcounter - 1;
        this.eventcounter = i;
        if (i <= 0) {
            co_yield(true);
            this.eventcounter = this.frequency;
        }
        ContentHandler contentHandler = this.clientContentHandler;
        if (contentHandler != null) {
            contentHandler.skippedEntity(str);
        }
    }

    public void startDocument() throws SAXException {
        co_entry_pause();
        int i = this.eventcounter - 1;
        this.eventcounter = i;
        if (i <= 0) {
            co_yield(true);
            this.eventcounter = this.frequency;
        }
        ContentHandler contentHandler = this.clientContentHandler;
        if (contentHandler != null) {
            contentHandler.startDocument();
        }
    }

    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        int i = this.eventcounter - 1;
        this.eventcounter = i;
        if (i <= 0) {
            co_yield(true);
            this.eventcounter = this.frequency;
        }
        ContentHandler contentHandler = this.clientContentHandler;
        if (contentHandler != null) {
            contentHandler.startElement(str, str2, str3, attributes);
        }
    }

    public void startPrefixMapping(String str, String str2) throws SAXException {
        int i = this.eventcounter - 1;
        this.eventcounter = i;
        if (i <= 0) {
            co_yield(true);
            this.eventcounter = this.frequency;
        }
        ContentHandler contentHandler = this.clientContentHandler;
        if (contentHandler != null) {
            contentHandler.startPrefixMapping(str, str2);
        }
    }

    public void comment(char[] cArr, int i, int i2) throws SAXException {
        LexicalHandler lexicalHandler = this.clientLexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.comment(cArr, i, i2);
        }
    }

    public void endCDATA() throws SAXException {
        LexicalHandler lexicalHandler = this.clientLexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.endCDATA();
        }
    }

    public void endDTD() throws SAXException {
        LexicalHandler lexicalHandler = this.clientLexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.endDTD();
        }
    }

    public void endEntity(String str) throws SAXException {
        LexicalHandler lexicalHandler = this.clientLexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.endEntity(str);
        }
    }

    public void startCDATA() throws SAXException {
        LexicalHandler lexicalHandler = this.clientLexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.startCDATA();
        }
    }

    public void startDTD(String str, String str2, String str3) throws SAXException {
        LexicalHandler lexicalHandler = this.clientLexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.startDTD(str, str2, str3);
        }
    }

    public void startEntity(String str) throws SAXException {
        LexicalHandler lexicalHandler = this.clientLexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.startEntity(str);
        }
    }

    public void notationDecl(String str, String str2, String str3) throws SAXException {
        DTDHandler dTDHandler = this.clientDTDHandler;
        if (dTDHandler != null) {
            dTDHandler.notationDecl(str, str2, str3);
        }
    }

    public void unparsedEntityDecl(String str, String str2, String str3, String str4) throws SAXException {
        DTDHandler dTDHandler = this.clientDTDHandler;
        if (dTDHandler != null) {
            dTDHandler.unparsedEntityDecl(str, str2, str3, str4);
        }
    }

    public void error(SAXParseException sAXParseException) throws SAXException {
        ErrorHandler errorHandler = this.clientErrorHandler;
        if (errorHandler != null) {
            errorHandler.error(sAXParseException);
        }
    }

    public void fatalError(SAXParseException sAXParseException) throws SAXException {
        ErrorHandler errorHandler = this.clientErrorHandler;
        if (errorHandler != null) {
            errorHandler.error(sAXParseException);
        }
        this.eventcounter = 0;
        co_yield(false);
    }

    public void warning(SAXParseException sAXParseException) throws SAXException {
        ErrorHandler errorHandler = this.clientErrorHandler;
        if (errorHandler != null) {
            errorHandler.error(sAXParseException);
        }
    }

    public int getSourceCoroutineID() {
        return this.fSourceCoroutineID;
    }

    public int getControllerCoroutineID() {
        return this.fControllerCoroutineID;
    }

    public CoroutineManager getCoroutineManager() {
        return this.fCoroutineManager;
    }

    /* access modifiers changed from: protected */
    public void count_and_yield(boolean z) throws SAXException {
        if (!z) {
            this.eventcounter = 0;
        }
        int i = this.eventcounter - 1;
        this.eventcounter = i;
        if (i <= 0) {
            co_yield(true);
            this.eventcounter = this.frequency;
        }
    }

    private void co_entry_pause() throws SAXException {
        if (this.fCoroutineManager == null) {
            init(null, -1, -1);
        }
        try {
            if (this.fCoroutineManager.co_entry_pause(this.fSourceCoroutineID) == Boolean.FALSE) {
                co_yield(false);
            }
        } catch (NoSuchMethodException e) {
            if (this.DEBUG) {
                e.printStackTrace();
            }
            throw new SAXException(e);
        }
    }

    private void co_yield(boolean z) throws SAXException {
        if (!this.fNoMoreEvents) {
            try {
                Object obj = Boolean.FALSE;
                if (z) {
                    obj = this.fCoroutineManager.co_resume(Boolean.TRUE, this.fSourceCoroutineID, this.fControllerCoroutineID);
                }
                if (obj == Boolean.FALSE) {
                    this.fNoMoreEvents = true;
                    if (this.fXMLReader == null) {
                        this.fCoroutineManager.co_exit_to(Boolean.FALSE, this.fSourceCoroutineID, this.fControllerCoroutineID);
                        return;
                    }
                    throw new StopException();
                }
            } catch (NoSuchMethodException e) {
                this.fNoMoreEvents = true;
                this.fCoroutineManager.co_exit(this.fSourceCoroutineID);
                throw new SAXException(e);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource
    public void startParse(InputSource inputSource) throws SAXException {
        if (this.fNoMoreEvents) {
            throw new SAXException(XMLMessages.createXMLMessage("ER_INCRSAXSRCFILTER_NOT_RESTARTABLE", null));
        } else if (this.fXMLReader != null) {
            this.fXMLReaderInputSource = inputSource;
            ThreadControllerWrapper.runThread(this, -1);
        } else {
            throw new SAXException(XMLMessages.createXMLMessage("ER_XMLRDR_NOT_BEFORE_STARTPARSE", null));
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.fXMLReader != null) {
            if (this.DEBUG) {
                System.out.println("IncrementalSAXSource_Filter parse thread launched");
            }
            SAXException sAXException = Boolean.FALSE;
            try {
                this.fXMLReader.parse(this.fXMLReaderInputSource);
            } catch (IOException e) {
                sAXException = e;
            } catch (StopException unused) {
                if (this.DEBUG) {
                    System.out.println("Active IncrementalSAXSource_Filter normal stop exception");
                }
            } catch (SAXException e2) {
                Exception exception = e2.getException();
                if (!(exception instanceof StopException)) {
                    if (this.DEBUG) {
                        PrintStream printStream = System.out;
                        printStream.println("Active IncrementalSAXSource_Filter UNEXPECTED SAX exception: " + exception);
                        exception.printStackTrace();
                    }
                    sAXException = e2;
                } else if (this.DEBUG) {
                    System.out.println("Active IncrementalSAXSource_Filter normal stop exception");
                }
            }
            this.fXMLReader = null;
            try {
                this.fNoMoreEvents = true;
                this.fCoroutineManager.co_exit_to(sAXException, this.fSourceCoroutineID, this.fControllerCoroutineID);
            } catch (NoSuchMethodException e3) {
                e3.printStackTrace(System.err);
                this.fCoroutineManager.co_exit(this.fSourceCoroutineID);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class StopException extends RuntimeException {
        static final long serialVersionUID = -1129245796185754956L;

        StopException() {
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource
    public Object deliverMoreNodes(boolean z) {
        if (this.fNoMoreEvents) {
            return Boolean.FALSE;
        }
        try {
            Object co_resume = this.fCoroutineManager.co_resume(z ? Boolean.TRUE : Boolean.FALSE, this.fControllerCoroutineID, this.fSourceCoroutineID);
            if (co_resume == Boolean.FALSE) {
                this.fCoroutineManager.co_exit(this.fControllerCoroutineID);
            }
            return co_resume;
        } catch (NoSuchMethodException e) {
            return e;
        }
    }
}
