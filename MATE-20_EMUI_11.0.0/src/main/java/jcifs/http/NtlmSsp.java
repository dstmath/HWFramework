package jcifs.http;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jcifs.ntlmssp.NtlmFlags;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.util.Base64;

public class NtlmSsp implements NtlmFlags {
    public NtlmPasswordAuthentication doAuthentication(HttpServletRequest req, HttpServletResponse resp, byte[] challenge) throws IOException, ServletException {
        return authenticate(req, resp, challenge);
    }

    public static NtlmPasswordAuthentication authenticate(HttpServletRequest req, HttpServletResponse resp, byte[] challenge) throws IOException, ServletException {
        String msg = req.getHeader("Authorization");
        if (msg == null || !msg.startsWith("NTLM ")) {
            resp.setHeader("WWW-Authenticate", "NTLM");
        } else {
            byte[] src = Base64.decode(msg.substring(5));
            if (src[8] == 1) {
                resp.setHeader("WWW-Authenticate", "NTLM " + Base64.encode(new Type2Message(new Type1Message(src), challenge, (String) null).toByteArray()));
            } else if (src[8] == 3) {
                Type3Message type3 = new Type3Message(src);
                byte[] lmResponse = type3.getLMResponse();
                if (lmResponse == null) {
                    lmResponse = new byte[0];
                }
                byte[] ntResponse = type3.getNTResponse();
                if (ntResponse == null) {
                    ntResponse = new byte[0];
                }
                return new NtlmPasswordAuthentication(type3.getDomain(), type3.getUser(), challenge, lmResponse, ntResponse);
            }
        }
        resp.setStatus(401);
        resp.setContentLength(0);
        resp.flushBuffer();
        return null;
    }
}
