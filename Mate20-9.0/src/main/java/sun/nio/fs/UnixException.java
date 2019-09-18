package sun.nio.fs;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;

class UnixException extends Exception {
    static final long serialVersionUID = 7227016794320723218L;
    private int errno;
    private String msg;

    UnixException(int errno2) {
        this.errno = errno2;
        this.msg = null;
    }

    UnixException(String msg2) {
        this.errno = 0;
        this.msg = msg2;
    }

    /* access modifiers changed from: package-private */
    public int errno() {
        return this.errno;
    }

    /* access modifiers changed from: package-private */
    public void setError(int errno2) {
        this.errno = errno2;
        this.msg = null;
    }

    /* access modifiers changed from: package-private */
    public String errorString() {
        if (this.msg != null) {
            return this.msg;
        }
        return Util.toString(UnixNativeDispatcher.strerror(errno()));
    }

    public String getMessage() {
        return errorString();
    }

    private IOException translateToIOException(String file, String other) {
        if (this.msg != null) {
            return new IOException(this.msg);
        }
        if (errno() == UnixConstants.EACCES) {
            return new AccessDeniedException(file, other, null);
        }
        if (errno() == UnixConstants.ENOENT) {
            return new NoSuchFileException(file, other, null);
        }
        if (errno() == UnixConstants.EEXIST) {
            return new FileAlreadyExistsException(file, other, null);
        }
        return new FileSystemException(file, other, errorString());
    }

    /* access modifiers changed from: package-private */
    public void rethrowAsIOException(String file) throws IOException {
        throw translateToIOException(file, null);
    }

    /* access modifiers changed from: package-private */
    public void rethrowAsIOException(UnixPath file, UnixPath other) throws IOException {
        String b = null;
        String a = file == null ? null : file.getPathForExceptionMessage();
        if (other != null) {
            b = other.getPathForExceptionMessage();
        }
        throw translateToIOException(a, b);
    }

    /* access modifiers changed from: package-private */
    public void rethrowAsIOException(UnixPath file) throws IOException {
        rethrowAsIOException(file, null);
    }

    /* access modifiers changed from: package-private */
    public IOException asIOException(UnixPath file) {
        return translateToIOException(file.getPathForExceptionMessage(), null);
    }
}
