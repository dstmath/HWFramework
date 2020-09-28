package org.ccil.cowan.tagsoup;

import java.io.InputStream;
import java.io.Reader;

public interface AutoDetector {
    Reader autoDetectingReader(InputStream inputStream);
}
