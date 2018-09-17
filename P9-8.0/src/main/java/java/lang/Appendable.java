package java.lang;

import java.io.IOException;

public interface Appendable {
    Appendable append(char c) throws IOException;

    Appendable append(CharSequence charSequence) throws IOException;

    Appendable append(CharSequence charSequence, int i, int i2) throws IOException;
}
