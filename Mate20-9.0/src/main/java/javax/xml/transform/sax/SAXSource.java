package javax.xml.transform.sax;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class SAXSource implements Source {
    public static final String FEATURE = "http://javax.xml.transform.sax.SAXSource/feature";
    private InputSource inputSource;
    private XMLReader reader;

    public SAXSource() {
    }

    public SAXSource(XMLReader reader2, InputSource inputSource2) {
        this.reader = reader2;
        this.inputSource = inputSource2;
    }

    public SAXSource(InputSource inputSource2) {
        this.inputSource = inputSource2;
    }

    public void setXMLReader(XMLReader reader2) {
        this.reader = reader2;
    }

    public XMLReader getXMLReader() {
        return this.reader;
    }

    public void setInputSource(InputSource inputSource2) {
        this.inputSource = inputSource2;
    }

    public InputSource getInputSource() {
        return this.inputSource;
    }

    public void setSystemId(String systemId) {
        if (this.inputSource == null) {
            this.inputSource = new InputSource(systemId);
        } else {
            this.inputSource.setSystemId(systemId);
        }
    }

    public String getSystemId() {
        if (this.inputSource == null) {
            return null;
        }
        return this.inputSource.getSystemId();
    }

    public static InputSource sourceToInputSource(Source source) {
        if (source instanceof SAXSource) {
            return ((SAXSource) source).getInputSource();
        }
        if (!(source instanceof StreamSource)) {
            return null;
        }
        StreamSource ss = (StreamSource) source;
        InputSource isource = new InputSource(ss.getSystemId());
        isource.setByteStream(ss.getInputStream());
        isource.setCharacterStream(ss.getReader());
        isource.setPublicId(ss.getPublicId());
        return isource;
    }
}
