package javax.xml.transform;

import java.util.Properties;

public interface Templates {
    Properties getOutputProperties();

    Transformer newTransformer() throws TransformerConfigurationException;
}
