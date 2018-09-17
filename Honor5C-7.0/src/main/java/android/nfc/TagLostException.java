package android.nfc;

import java.io.IOException;

public class TagLostException extends IOException {
    public TagLostException(String message) {
        super(message);
    }
}
