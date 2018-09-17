package org.apache.http;

import java.util.Locale;

@Deprecated
public interface ReasonPhraseCatalog {
    String getReason(int i, Locale locale);
}
