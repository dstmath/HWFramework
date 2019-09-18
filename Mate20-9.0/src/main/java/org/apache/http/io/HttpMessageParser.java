package org.apache.http.io;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;

@Deprecated
public interface HttpMessageParser {
    HttpMessage parse() throws IOException, HttpException;
}
