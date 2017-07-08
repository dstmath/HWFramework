package android.content;

import java.io.IOException;
import java.io.InputStream;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public interface ContentInsertHandler extends ContentHandler {
    void insert(ContentResolver contentResolver, InputStream inputStream) throws IOException, SAXException;

    void insert(ContentResolver contentResolver, String str) throws SAXException;
}
