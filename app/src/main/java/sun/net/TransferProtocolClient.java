package sun.net;

import java.io.IOException;
import java.util.Vector;

public class TransferProtocolClient extends NetworkClient {
    static final boolean debug = false;
    protected int lastReplyCode;
    protected Vector serverResponse;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int readServerResponse() throws IOException {
        int code;
        StringBuffer replyBuf = new StringBuffer(32);
        int continuingCode = -1;
        this.serverResponse.setSize(0);
        while (true) {
            int c = this.serverInput.read();
            if (c != -1) {
                if (c == 13) {
                    c = this.serverInput.read();
                    if (c != 10) {
                        replyBuf.append('\r');
                    }
                }
                replyBuf.append((char) c);
                if (c != 10) {
                    continue;
                }
            }
            String response = replyBuf.toString();
            replyBuf.setLength(0);
            if (response.length() == 0) {
                code = -1;
            } else {
                try {
                    code = Integer.parseInt(response.substring(0, 3));
                } catch (NumberFormatException e) {
                    code = -1;
                } catch (StringIndexOutOfBoundsException e2) {
                }
            }
            this.serverResponse.addElement(response);
            if (continuingCode != -1) {
                if (code == continuingCode && (response.length() < 4 || response.charAt(3) != '-')) {
                }
            } else if (response.length() >= 4 && response.charAt(3) == '-') {
                continuingCode = code;
            }
        }
        this.lastReplyCode = code;
        return code;
    }

    public void sendServer(String cmd) {
        this.serverOutput.print(cmd);
    }

    public String getResponseString() {
        return (String) this.serverResponse.elementAt(0);
    }

    public Vector getResponseStrings() {
        return this.serverResponse;
    }

    public TransferProtocolClient(String host, int port) throws IOException {
        super(host, port);
        this.serverResponse = new Vector(1);
    }

    public TransferProtocolClient() {
        this.serverResponse = new Vector(1);
    }
}
