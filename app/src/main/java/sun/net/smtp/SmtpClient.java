package sun.net.smtp;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.security.AccessController;
import sun.net.TransferProtocolClient;
import sun.security.action.GetPropertyAction;

public class SmtpClient extends TransferProtocolClient {
    String mailhost;
    SmtpPrintStream message;

    public void closeServer() throws IOException {
        if (serverIsOpen()) {
            closeMessage();
            issueCommand("QUIT\r\n", 221);
            super.closeServer();
        }
    }

    void issueCommand(String cmd, int expect) throws IOException {
        sendServer(cmd);
        int reply;
        do {
            reply = readServerResponse();
            if (reply == expect) {
                return;
            }
        } while (reply == 220);
        throw new SmtpProtocolException(getResponseString());
    }

    private void toCanonical(String s) throws IOException {
        if (s.startsWith("<")) {
            issueCommand("rcpt to: " + s + "\r\n", 250);
        } else {
            issueCommand("rcpt to: <" + s + ">\r\n", 250);
        }
    }

    public void to(String s) throws IOException {
        int st = 0;
        int limit = s.length();
        int lastnonsp = 0;
        int parendepth = 0;
        boolean ignore = false;
        for (int pos = 0; pos < limit; pos++) {
            int c = s.charAt(pos);
            if (parendepth > 0) {
                if (c == 40) {
                    parendepth++;
                } else if (c == 41) {
                    parendepth--;
                }
                if (parendepth == 0) {
                    if (lastnonsp > st) {
                        ignore = true;
                    } else {
                        st = pos + 1;
                    }
                }
            } else if (c == 40) {
                parendepth++;
            } else if (c == 60) {
                lastnonsp = pos + 1;
                st = lastnonsp;
            } else if (c == 62) {
                ignore = true;
            } else if (c == 44) {
                if (lastnonsp > st) {
                    toCanonical(s.substring(st, lastnonsp));
                }
                st = pos + 1;
                ignore = false;
            } else if (c > 32 && !ignore) {
                lastnonsp = pos + 1;
            } else if (st == pos) {
                st++;
            }
        }
        if (lastnonsp > st) {
            toCanonical(s.substring(st, lastnonsp));
        }
    }

    public void from(String s) throws IOException {
        if (s.startsWith("<")) {
            issueCommand("mail from: " + s + "\r\n", 250);
        } else {
            issueCommand("mail from: <" + s + ">\r\n", 250);
        }
    }

    private void openServer(String host) throws IOException {
        this.mailhost = host;
        openServer(this.mailhost, 25);
        issueCommand("helo " + InetAddress.getLocalHost().getHostName() + "\r\n", 250);
    }

    public PrintStream startMessage() throws IOException {
        issueCommand("data\r\n", 354);
        try {
            this.message = new SmtpPrintStream(this.serverOutput, this);
            return this.message;
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(encoding + " encoding not found");
        }
    }

    void closeMessage() throws IOException {
        if (this.message != null) {
            this.message.close();
        }
    }

    public SmtpClient(String host) throws IOException {
        if (host != null) {
            try {
                openServer(host);
                this.mailhost = host;
                return;
            } catch (Exception e) {
            }
        }
        try {
            this.mailhost = (String) AccessController.doPrivileged(new GetPropertyAction("mail.host"));
            if (this.mailhost != null) {
                openServer(this.mailhost);
                return;
            }
        } catch (Exception e2) {
        }
        try {
            this.mailhost = "localhost";
            openServer(this.mailhost);
        } catch (Exception e3) {
            this.mailhost = "mailhost";
            openServer(this.mailhost);
        }
    }

    public SmtpClient() throws IOException {
        this(null);
    }

    public SmtpClient(int to) throws IOException {
        setConnectTimeout(to);
        try {
            this.mailhost = (String) AccessController.doPrivileged(new GetPropertyAction("mail.host"));
            if (this.mailhost != null) {
                openServer(this.mailhost);
                return;
            }
        } catch (Exception e) {
        }
        try {
            this.mailhost = "localhost";
            openServer(this.mailhost);
        } catch (Exception e2) {
            this.mailhost = "mailhost";
            openServer(this.mailhost);
        }
    }

    public String getMailHost() {
        return this.mailhost;
    }

    String getEncoding() {
        return encoding;
    }
}
