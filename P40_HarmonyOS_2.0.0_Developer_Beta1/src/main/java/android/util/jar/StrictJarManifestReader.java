package android.util.jar;

import android.util.jar.StrictJarManifest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;

/* access modifiers changed from: package-private */
public class StrictJarManifestReader {
    private final HashMap<String, Attributes.Name> attributeNameCache = new HashMap<>();
    private final byte[] buf;
    private int consecutiveLineBreaks = 0;
    private final int endOfMainSection;
    private Attributes.Name name;
    private int pos;
    private String value;
    private final ByteArrayOutputStream valueBuffer = new ByteArrayOutputStream(80);

    public StrictJarManifestReader(byte[] buf2, Attributes main) throws IOException {
        this.buf = buf2;
        while (readHeader()) {
            main.put(this.name, this.value);
        }
        this.endOfMainSection = this.pos;
    }

    public void readEntries(Map<String, Attributes> entries, Map<String, StrictJarManifest.Chunk> chunks) throws IOException {
        int mark = this.pos;
        while (readHeader()) {
            if (StrictJarManifest.ATTRIBUTE_NAME_NAME.equals(this.name)) {
                String entryNameValue = this.value;
                Attributes entry = entries.get(entryNameValue);
                if (entry == null) {
                    entry = new Attributes(12);
                }
                while (readHeader()) {
                    entry.put(this.name, this.value);
                }
                if (chunks != null) {
                    if (chunks.get(entryNameValue) == null) {
                        chunks.put(entryNameValue, new StrictJarManifest.Chunk(mark, this.pos));
                        mark = this.pos;
                    } else {
                        throw new IOException("A jar verifier does not support more than one entry with the same name");
                    }
                }
                entries.put(entryNameValue, entry);
            } else {
                throw new IOException("Entry is not named");
            }
        }
    }

    public int getEndOfMainSection() {
        return this.endOfMainSection;
    }

    private boolean readHeader() throws IOException {
        if (this.consecutiveLineBreaks > 1) {
            this.consecutiveLineBreaks = 0;
            return false;
        }
        readName();
        this.consecutiveLineBreaks = 0;
        readValue();
        return this.consecutiveLineBreaks > 0;
    }

    private void readName() throws IOException {
        int i;
        byte[] bArr;
        int mark = this.pos;
        do {
            i = this.pos;
            bArr = this.buf;
            if (i < bArr.length) {
                this.pos = i + 1;
            } else {
                return;
            }
        } while (bArr[i] != 58);
        String nameString = new String(bArr, mark, (this.pos - mark) - 1, StandardCharsets.US_ASCII);
        byte[] bArr2 = this.buf;
        int i2 = this.pos;
        this.pos = i2 + 1;
        if (bArr2[i2] == 32) {
            try {
                this.name = this.attributeNameCache.get(nameString);
                if (this.name == null) {
                    this.name = new Attributes.Name(nameString);
                    this.attributeNameCache.put(nameString, this.name);
                }
            } catch (IllegalArgumentException e) {
                throw new IOException(e.getMessage());
            }
        } else {
            throw new IOException(String.format("Invalid value for attribute '%s'", nameString));
        }
    }

    private void readValue() throws IOException {
        boolean lastCr = false;
        int mark = this.pos;
        int last = this.pos;
        this.valueBuffer.reset();
        while (true) {
            int i = this.pos;
            byte[] bArr = this.buf;
            if (i >= bArr.length) {
                break;
            }
            this.pos = i + 1;
            byte next = bArr[i];
            if (next == 0) {
                throw new IOException("NUL character in a manifest");
            } else if (next != 10) {
                if (next == 13) {
                    lastCr = true;
                    this.consecutiveLineBreaks++;
                } else if (next == 32 && this.consecutiveLineBreaks == 1) {
                    this.valueBuffer.write(bArr, mark, last - mark);
                    mark = this.pos;
                    this.consecutiveLineBreaks = 0;
                } else if (this.consecutiveLineBreaks >= 1) {
                    this.pos--;
                    break;
                } else {
                    last = this.pos;
                }
            } else if (lastCr) {
                lastCr = false;
            } else {
                this.consecutiveLineBreaks++;
            }
        }
        this.valueBuffer.write(this.buf, mark, last - mark);
        this.value = this.valueBuffer.toString(StandardCharsets.UTF_8.name());
    }
}
