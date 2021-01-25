package com.android.internal.http.multipart;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FilePartSource implements PartSource {
    private File file;
    private String fileName;

    public FilePartSource(File file2) throws FileNotFoundException {
        this.file = null;
        this.fileName = null;
        this.file = file2;
        if (file2 == null) {
            return;
        }
        if (!file2.isFile()) {
            throw new FileNotFoundException("File is not a normal file.");
        } else if (file2.canRead()) {
            this.fileName = file2.getName();
        } else {
            throw new FileNotFoundException("File is not readable.");
        }
    }

    public FilePartSource(String fileName2, File file2) throws FileNotFoundException {
        this(file2);
        if (fileName2 != null) {
            this.fileName = fileName2;
        }
    }

    @Override // com.android.internal.http.multipart.PartSource
    public long getLength() {
        File file2 = this.file;
        if (file2 != null) {
            return file2.length();
        }
        return 0;
    }

    @Override // com.android.internal.http.multipart.PartSource
    public String getFileName() {
        String str = this.fileName;
        return str == null ? "noname" : str;
    }

    @Override // com.android.internal.http.multipart.PartSource
    public InputStream createInputStream() throws IOException {
        File file2 = this.file;
        if (file2 != null) {
            return new FileInputStream(file2);
        }
        return new ByteArrayInputStream(new byte[0]);
    }
}
