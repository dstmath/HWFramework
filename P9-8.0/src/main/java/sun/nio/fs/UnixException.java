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

    UnixException(int errno) {
        this.errno = errno;
        this.msg = null;
    }

    UnixException(String msg) {
        this.errno = 0;
        this.msg = msg;
    }

    int errno() {
        return this.errno;
    }

    void setError(int errno) {
        this.errno = errno;
        this.msg = null;
    }

    String errorString() {
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

    void rethrowAsIOException(String file) throws IOException {
        throw translateToIOException(file, null);
    }

    void rethrowAsIOException(UnixPath file, UnixPath other) throws IOException {
        throw translateToIOException(file == null ? null : file.getPathForExceptionMessage(), other == null ? null : other.getPathForExceptionMessage());
    }

    void rethrowAsIOException(UnixPath file) throws IOException {
        rethrowAsIOException(file, null);
    }

    IOException asIOException(UnixPath file) {
        return translateToIOException(file.getPathForExceptionMessage(), null);
    }
}
