package jcifs.http;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import jcifs.Config;
import jcifs.UniAddress;
import jcifs.netbios.NbtAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbSession;
import jcifs.util.Base64;

public abstract class NtlmServlet extends HttpServlet {
    private String defaultDomain;
    private String domainController;
    private boolean enableBasic;
    private boolean insecureBasic;
    private boolean loadBalance;
    private String realm;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Config.setProperty("jcifs.smb.client.soTimeout", "300000");
        Config.setProperty("jcifs.netbios.cachePolicy", "600");
        Enumeration e = config.getInitParameterNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            if (name.startsWith("jcifs.")) {
                Config.setProperty(name, config.getInitParameter(name));
            }
        }
        this.defaultDomain = Config.getProperty("jcifs.smb.client.domain");
        this.domainController = Config.getProperty("jcifs.http.domainController");
        if (this.domainController == null) {
            this.domainController = this.defaultDomain;
            this.loadBalance = Config.getBoolean("jcifs.http.loadBalance", true);
        }
        this.enableBasic = Boolean.valueOf(Config.getProperty("jcifs.http.enableBasic")).booleanValue();
        this.insecureBasic = Boolean.valueOf(Config.getProperty("jcifs.http.insecureBasic")).booleanValue();
        this.realm = Config.getProperty("jcifs.http.basicRealm");
        if (this.realm == null) {
            this.realm = "jCIFS";
        }
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean offerBasic = this.enableBasic && (this.insecureBasic || request.isSecure());
        String msg = request.getHeader("Authorization");
        HttpSession ssn;
        if (msg == null || !(msg.startsWith("NTLM ") || (offerBasic && msg.startsWith("Basic ")))) {
            ssn = request.getSession(false);
            if (ssn == null || ssn.getAttribute("NtlmHttpAuth") == null) {
                response.setHeader("WWW-Authenticate", "NTLM");
                if (offerBasic) {
                    response.addHeader("WWW-Authenticate", "Basic realm=\"" + this.realm + "\"");
                }
                response.setStatus(401);
                response.flushBuffer();
                return;
            }
        }
        UniAddress dc;
        NtlmPasswordAuthentication ntlm;
        if (this.loadBalance) {
            dc = new UniAddress(NbtAddress.getByName(this.domainController, 28, null));
        } else {
            dc = UniAddress.getByName(this.domainController, true);
        }
        if (msg.startsWith("NTLM ")) {
            ntlm = NtlmSsp.authenticate(request, response, SmbSession.getChallenge(dc));
            if (ntlm == null) {
                return;
            }
        }
        String user;
        String auth = new String(Base64.decode(msg.substring(6)), "US-ASCII");
        int index = auth.indexOf(58);
        if (index != -1) {
            user = auth.substring(0, index);
        } else {
            user = auth;
        }
        String password = index != -1 ? auth.substring(index + 1) : "";
        index = user.indexOf(92);
        if (index == -1) {
            index = user.indexOf(47);
        }
        String domain = index != -1 ? user.substring(0, index) : this.defaultDomain;
        if (index != -1) {
            user = user.substring(index + 1);
        }
        ntlm = new NtlmPasswordAuthentication(domain, user, password);
        try {
            SmbSession.logon(dc, ntlm);
            ssn = request.getSession();
            ssn.setAttribute("NtlmHttpAuth", ntlm);
            ssn.setAttribute("ntlmdomain", ntlm.getDomain());
            ssn.setAttribute("ntlmuser", ntlm.getUsername());
        } catch (SmbAuthException e) {
            response.setHeader("WWW-Authenticate", "NTLM");
            if (offerBasic) {
                response.addHeader("WWW-Authenticate", "Basic realm=\"" + this.realm + "\"");
            }
            response.setHeader("Connection", "close");
            response.setStatus(401);
            response.flushBuffer();
            return;
        }
        super.service(request, response);
    }
}
