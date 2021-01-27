package ohos.javax.xml.transform.sax;

import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.stream.StreamSource;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.XMLReader;

public class SAXSource implements Source {
    public static final String FEATURE = "http://ohos.javax.xml.transform.sax.SAXSource/feature";
    private InputSource inputSource;
    private XMLReader reader;

    public SAXSource() {
    }

    public SAXSource(XMLReader xMLReader, InputSource inputSource2) {
        this.reader = xMLReader;
        this.inputSource = inputSource2;
    }

    public SAXSource(InputSource inputSource2) {
        this.inputSource = inputSource2;
    }

    public void setXMLReader(XMLReader xMLReader) {
        this.reader = xMLReader;
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

    @Override // ohos.javax.xml.transform.Source
    public void setSystemId(String str) {
        InputSource inputSource2 = this.inputSource;
        if (inputSource2 == null) {
            this.inputSource = new InputSource(str);
        } else {
            inputSource2.setSystemId(str);
        }
    }

    @Override // ohos.javax.xml.transform.Source
    public String getSystemId() {
        InputSource inputSource2 = this.inputSource;
        if (inputSource2 == null) {
            return null;
        }
        return inputSource2.getSystemId();
    }

    public static InputSource sourceToInputSource(Source source) {
        if (source instanceof SAXSource) {
            return ((SAXSource) source).getInputSource();
        }
        if (!(source instanceof StreamSource)) {
            return null;
        }
        StreamSource streamSource = (StreamSource) source;
        InputSource inputSource2 = new InputSource(streamSource.getSystemId());
        inputSource2.setByteStream(streamSource.getInputStream());
        inputSource2.setCharacterStream(streamSource.getReader());
        inputSource2.setPublicId(streamSource.getPublicId());
        return inputSource2;
    }
}
