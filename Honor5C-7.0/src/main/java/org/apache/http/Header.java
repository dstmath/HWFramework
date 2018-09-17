package org.apache.http;

@Deprecated
public interface Header {
    HeaderElement[] getElements() throws ParseException;

    String getName();

    String getValue();
}
