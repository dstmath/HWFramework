package org.bouncycastle.mime;

import java.io.IOException;

public interface MimeParser {
    void parse(MimeParserListener mimeParserListener) throws IOException;
}
