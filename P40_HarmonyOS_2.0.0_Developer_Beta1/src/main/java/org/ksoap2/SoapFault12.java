package org.ksoap2;

import java.io.IOException;
import org.ksoap2.kdom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class SoapFault12 extends SoapFault {
    private static final long serialVersionUID = 1012001;
    public Node Code;
    public Node Detail;
    public Node Node;
    public Node Reason;
    public Node Role;

    public SoapFault12() {
        this.version = SoapEnvelope.VER12;
    }

    public SoapFault12(int version) {
        this.version = version;
    }

    @Override // org.ksoap2.SoapFault
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        parseSelf(parser);
        this.faultcode = this.Code.getElement(SoapEnvelope.ENV2003, "Value").getText(0);
        this.faultstring = this.Reason.getElement(SoapEnvelope.ENV2003, "Text").getText(0);
        this.detail = this.Detail;
        this.faultactor = null;
    }

    private void parseSelf(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, SoapEnvelope.ENV2003, "Fault");
        while (parser.nextTag() == 2) {
            String name = parser.getName();
            String namespace = parser.getNamespace();
            parser.nextTag();
            if (name.toLowerCase().equals("Code".toLowerCase())) {
                this.Code = new Node();
                this.Code.parse(parser);
            } else if (name.toLowerCase().equals("Reason".toLowerCase())) {
                this.Reason = new Node();
                this.Reason.parse(parser);
            } else if (name.toLowerCase().equals("Node".toLowerCase())) {
                this.Node = new Node();
                this.Node.parse(parser);
            } else if (name.toLowerCase().equals("Role".toLowerCase())) {
                this.Role = new Node();
                this.Role.parse(parser);
            } else if (name.toLowerCase().equals("Detail".toLowerCase())) {
                this.Detail = new Node();
                this.Detail.parse(parser);
            } else {
                throw new RuntimeException("unexpected tag:" + name);
            }
            parser.require(3, namespace, name);
        }
        parser.require(3, SoapEnvelope.ENV2003, "Fault");
        parser.nextTag();
    }

    @Override // org.ksoap2.SoapFault
    public void write(XmlSerializer xw) throws IOException {
        xw.startTag(SoapEnvelope.ENV2003, "Fault");
        xw.startTag(SoapEnvelope.ENV2003, "Code");
        this.Code.write(xw);
        xw.endTag(SoapEnvelope.ENV2003, "Code");
        xw.startTag(SoapEnvelope.ENV2003, "Reason");
        this.Reason.write(xw);
        xw.endTag(SoapEnvelope.ENV2003, "Reason");
        if (this.Node != null) {
            xw.startTag(SoapEnvelope.ENV2003, "Node");
            this.Node.write(xw);
            xw.endTag(SoapEnvelope.ENV2003, "Node");
        }
        if (this.Role != null) {
            xw.startTag(SoapEnvelope.ENV2003, "Role");
            this.Role.write(xw);
            xw.endTag(SoapEnvelope.ENV2003, "Role");
        }
        if (this.Detail != null) {
            xw.startTag(SoapEnvelope.ENV2003, "Detail");
            this.Detail.write(xw);
            xw.endTag(SoapEnvelope.ENV2003, "Detail");
        }
        xw.endTag(SoapEnvelope.ENV2003, "Fault");
    }

    @Override // org.ksoap2.SoapFault, java.lang.Throwable
    public String getMessage() {
        return this.Reason.getElement(SoapEnvelope.ENV2003, "Text").getText(0);
    }

    @Override // org.ksoap2.SoapFault, java.lang.Throwable, java.lang.Object
    public String toString() {
        String reason = this.Reason.getElement(SoapEnvelope.ENV2003, "Text").getText(0);
        String code = this.Code.getElement(SoapEnvelope.ENV2003, "Value").getText(0);
        return "Code: " + code + ", Reason: " + reason;
    }
}
