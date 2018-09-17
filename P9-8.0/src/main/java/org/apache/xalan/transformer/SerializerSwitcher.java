package org.apache.xalan.transformer;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;
import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.Constants;
import org.apache.xalan.templates.OutputProperties;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;

public class SerializerSwitcher {
    public static void switchSerializerIfHTML(TransformerImpl transformer, String ns, String localName) throws TransformerException {
        if (transformer != null) {
            if ((ns == null || ns.length() == 0) && localName.equalsIgnoreCase("html") && transformer.getOutputPropertyNoDefault(Constants.ATTRNAME_OUTPUT_METHOD) == null) {
                Properties prevProperties = transformer.getOutputFormat().getProperties();
                OutputProperties htmlOutputProperties = new OutputProperties("html");
                htmlOutputProperties.copyFrom(prevProperties, true);
                Properties htmlProperties = htmlOutputProperties.getProperties();
            }
        }
    }

    private static String getOutputPropertyNoDefault(String qnameString, Properties props) throws IllegalArgumentException {
        return (String) props.get(qnameString);
    }

    public static Serializer switchSerializerIfHTML(String ns, String localName, Properties props, Serializer oldSerializer) throws TransformerException {
        Serializer newSerializer = oldSerializer;
        if ((ns == null || ns.length() == 0) && localName.equalsIgnoreCase("html")) {
            if (getOutputPropertyNoDefault(Constants.ATTRNAME_OUTPUT_METHOD, props) != null) {
                return oldSerializer;
            }
            Properties prevProperties = props;
            OutputProperties htmlOutputProperties = new OutputProperties("html");
            htmlOutputProperties.copyFrom(props, true);
            Properties htmlProperties = htmlOutputProperties.getProperties();
            if (oldSerializer != null) {
                Serializer serializer = SerializerFactory.getSerializer(htmlProperties);
                Writer writer = oldSerializer.getWriter();
                if (writer != null) {
                    serializer.setWriter(writer);
                } else {
                    OutputStream os = serializer.getOutputStream();
                    if (os != null) {
                        serializer.setOutputStream(os);
                    }
                }
                newSerializer = serializer;
            }
        }
        return newSerializer;
    }
}
