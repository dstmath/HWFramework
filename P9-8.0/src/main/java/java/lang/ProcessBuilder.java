package java.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ProcessBuilder {
    static final /* synthetic */ boolean -assertionsDisabled = (ProcessBuilder.class.desiredAssertionStatus() ^ 1);
    private List<String> command;
    private File directory;
    private Map<String, String> environment;
    private boolean redirectErrorStream;
    private Redirect[] redirects;

    static class NullInputStream extends InputStream {
        static final NullInputStream INSTANCE = new NullInputStream();

        private NullInputStream() {
        }

        public int read() {
            return -1;
        }

        public int available() {
            return 0;
        }
    }

    static class NullOutputStream extends OutputStream {
        static final NullOutputStream INSTANCE = new NullOutputStream();

        private NullOutputStream() {
        }

        public void write(int b) throws IOException {
            throw new IOException("Stream closed");
        }
    }

    public static abstract class Redirect {
        static final /* synthetic */ boolean -assertionsDisabled = (Redirect.class.desiredAssertionStatus() ^ 1);
        public static final Redirect INHERIT = new Redirect() {
            public Type type() {
                return Type.INHERIT;
            }

            public String toString() {
                return type().toString();
            }
        };
        public static final Redirect PIPE = new Redirect() {
            public Type type() {
                return Type.PIPE;
            }

            public String toString() {
                return type().toString();
            }
        };

        public enum Type {
            PIPE,
            INHERIT,
            READ,
            WRITE,
            APPEND
        }

        /* synthetic */ Redirect(Redirect -this0) {
            this();
        }

        public abstract Type type();

        public File file() {
            return null;
        }

        boolean append() {
            throw new UnsupportedOperationException();
        }

        public static Redirect from(final File file) {
            if (file != null) {
                return new Redirect() {
                    public Type type() {
                        return Type.READ;
                    }

                    public File file() {
                        return file;
                    }

                    public String toString() {
                        return "redirect to read from file \"" + file + "\"";
                    }
                };
            }
            throw new NullPointerException();
        }

        public static Redirect to(final File file) {
            if (file != null) {
                return new Redirect() {
                    public Type type() {
                        return Type.WRITE;
                    }

                    public File file() {
                        return file;
                    }

                    public String toString() {
                        return "redirect to write to file \"" + file + "\"";
                    }

                    boolean append() {
                        return false;
                    }
                };
            }
            throw new NullPointerException();
        }

        public static Redirect appendTo(final File file) {
            if (file != null) {
                return new Redirect() {
                    public Type type() {
                        return Type.APPEND;
                    }

                    public File file() {
                        return file;
                    }

                    public String toString() {
                        return "redirect to append to file \"" + file + "\"";
                    }

                    boolean append() {
                        return true;
                    }
                };
            }
            throw new NullPointerException();
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Redirect)) {
                return false;
            }
            Redirect r = (Redirect) obj;
            if (r.type() != type()) {
                return false;
            }
            if (-assertionsDisabled || file() != null) {
                return file().equals(r.file());
            }
            throw new AssertionError();
        }

        public int hashCode() {
            File file = file();
            if (file == null) {
                return super.hashCode();
            }
            return file.hashCode();
        }

        private Redirect() {
        }
    }

    public ProcessBuilder(List<String> command) {
        if (command == null) {
            throw new NullPointerException();
        }
        this.command = command;
    }

    public ProcessBuilder(String... command) {
        this.command = new ArrayList(command.length);
        for (String arg : command) {
            this.command.add(arg);
        }
    }

    public ProcessBuilder command(List<String> command) {
        if (command == null) {
            throw new NullPointerException();
        }
        this.command = command;
        return this;
    }

    public ProcessBuilder command(String... command) {
        this.command = new ArrayList(command.length);
        for (String arg : command) {
            this.command.add(arg);
        }
        return this;
    }

    public List<String> command() {
        return this.command;
    }

    public Map<String, String> environment() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("getenv.*"));
        }
        if (this.environment == null) {
            this.environment = ProcessEnvironment.environment();
        }
        if (-assertionsDisabled || this.environment != null) {
            return this.environment;
        }
        throw new AssertionError();
    }

    ProcessBuilder environment(String[] envp) {
        if (-assertionsDisabled || this.environment == null) {
            if (envp != null) {
                this.environment = ProcessEnvironment.emptyEnvironment(envp.length);
                if (-assertionsDisabled || this.environment != null) {
                    for (String envstring : envp) {
                        String envstring2;
                        if (envstring2.indexOf(0) != -1) {
                            envstring2 = envstring2.replaceFirst("\u0000.*", "");
                        }
                        int eqlsign = envstring2.indexOf(61, 0);
                        if (eqlsign != -1) {
                            this.environment.put(envstring2.substring(0, eqlsign), envstring2.substring(eqlsign + 1));
                        }
                    }
                } else {
                    throw new AssertionError();
                }
            }
            return this;
        }
        throw new AssertionError();
    }

    public File directory() {
        return this.directory;
    }

    public ProcessBuilder directory(File directory) {
        this.directory = directory;
        return this;
    }

    private Redirect[] redirects() {
        if (this.redirects == null) {
            this.redirects = new Redirect[]{Redirect.PIPE, Redirect.PIPE, Redirect.PIPE};
        }
        return this.redirects;
    }

    public ProcessBuilder redirectInput(Redirect source) {
        if (source.type() == Type.WRITE || source.type() == Type.APPEND) {
            throw new IllegalArgumentException("Redirect invalid for reading: " + source);
        }
        redirects()[0] = source;
        return this;
    }

    public ProcessBuilder redirectOutput(Redirect destination) {
        if (destination.type() == Type.READ) {
            throw new IllegalArgumentException("Redirect invalid for writing: " + destination);
        }
        redirects()[1] = destination;
        return this;
    }

    public ProcessBuilder redirectError(Redirect destination) {
        if (destination.type() == Type.READ) {
            throw new IllegalArgumentException("Redirect invalid for writing: " + destination);
        }
        redirects()[2] = destination;
        return this;
    }

    public ProcessBuilder redirectInput(File file) {
        return redirectInput(Redirect.from(file));
    }

    public ProcessBuilder redirectOutput(File file) {
        return redirectOutput(Redirect.to(file));
    }

    public ProcessBuilder redirectError(File file) {
        return redirectError(Redirect.to(file));
    }

    public Redirect redirectInput() {
        return this.redirects == null ? Redirect.PIPE : this.redirects[0];
    }

    public Redirect redirectOutput() {
        return this.redirects == null ? Redirect.PIPE : this.redirects[1];
    }

    public Redirect redirectError() {
        return this.redirects == null ? Redirect.PIPE : this.redirects[2];
    }

    public ProcessBuilder inheritIO() {
        Arrays.fill(redirects(), Redirect.INHERIT);
        return this;
    }

    public boolean redirectErrorStream() {
        return this.redirectErrorStream;
    }

    public ProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0062 A:{Splitter: B:22:0x0057, ExcHandler: java.io.IOException (r4_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00b9  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00a0  */
    /* JADX WARNING: Missing block: B:25:0x0062, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:26:0x0063, code:
            r5 = ": " + r4.getMessage();
            r1 = r4;
     */
    /* JADX WARNING: Missing block: B:30:?, code:
            r9.checkRead(r7);
     */
    /* JADX WARNING: Missing block: B:33:0x00a0, code:
            r10 = "";
     */
    /* JADX WARNING: Missing block: B:36:0x00b3, code:
            r8 = move-exception;
     */
    /* JADX WARNING: Missing block: B:37:0x00b4, code:
            r5 = "";
            r1 = r8;
     */
    /* JADX WARNING: Missing block: B:38:0x00b9, code:
            r10 = " (in directory \"" + r3 + "\")";
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Process start() throws IOException {
        String[] cmdarray = (String[]) ((String[]) this.command.toArray(new String[this.command.size()])).clone();
        for (String arg : cmdarray) {
            if (arg == null) {
                throw new NullPointerException();
            }
        }
        String prog = cmdarray[0];
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkExec(prog);
        }
        String dir = this.directory == null ? null : this.directory.toString();
        for (int i = 1; i < cmdarray.length; i++) {
            if (cmdarray[i].indexOf(0) >= 0) {
                throw new IOException("invalid null character in command");
            }
        }
        try {
            return ProcessImpl.start(cmdarray, this.environment, dir, this.redirects, this.redirectErrorStream);
        } catch (Throwable e) {
        }
        StringBuilder append = new StringBuilder().append("Cannot run program \"").append(prog).append("\"");
        if (dir != null) {
        }
        throw new IOException(append.append(r10).append(exceptionInfo).toString(), cause);
    }
}
