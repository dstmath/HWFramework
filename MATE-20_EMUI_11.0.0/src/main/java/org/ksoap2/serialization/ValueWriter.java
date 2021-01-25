package org.ksoap2.serialization;

import java.io.IOException;
import org.xmlpull.v1.XmlSerializer;

public interface ValueWriter {
    void write(XmlSerializer xmlSerializer) throws IOException;
}
