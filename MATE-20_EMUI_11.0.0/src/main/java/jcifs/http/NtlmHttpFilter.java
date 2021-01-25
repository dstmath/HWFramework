package jcifs.http;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import jcifs.Config;
import jcifs.UniAddress;
import jcifs.smb.NtlmChallenge;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbSession;
import jcifs.util.Base64;
import jcifs.util.Hexdump;
import jcifs.util.LogStream;

public class NtlmHttpFilter implements Filter {
    private static LogStream log = LogStream.getInstance();
    private String defaultDomain;
    private String domainController;
    private boolean enableBasic;
    private boolean insecureBasic;
    private boolean loadBalance;
    private String realm;

    public void init(FilterConfig filterConfig) throws ServletException {
        Config.setProperty("jcifs.smb.client.soTimeout", "1800000");
        Config.setProperty("jcifs.netbios.cachePolicy", "1200");
        Config.setProperty("jcifs.smb.lmCompatibility", "0");
        Config.setProperty("jcifs.smb.client.useExtendedSecurity", "false");
        Enumeration e = filterConfig.getInitParameterNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            if (name.startsWith("jcifs.")) {
                Config.setProperty(name, filterConfig.getInitParameter(name));
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
        int level = Config.getInt("jcifs.util.loglevel", -1);
        if (level != -1) {
            LogStream.setLevel(level);
        }
        LogStream logStream = log;
        if (LogStream.level > 2) {
            try {
                Config.store(log, "JCIFS PROPERTIES");
            } catch (IOException e2) {
            }
        }
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        NtlmPasswordAuthentication ntlm = negotiate(req, (HttpServletResponse) response, false);
        if (ntlm != null) {
            chain.doFilter(new NtlmHttpServletRequest(req, ntlm), response);
        }
    }

    /* access modifiers changed from: protected */
    public NtlmPasswordAuthentication negotiate(HttpServletRequest req, HttpServletResponse resp, boolean skipAuthentication) throws IOException, ServletException {
        HttpSession ssn;
        String user;
        String domain;
        UniAddress dc;
        HttpSession ssn2;
        byte[] challenge;
        NtlmPasswordAuthentication ntlm = null;
        String msg = req.getHeader("Authorization");
        boolean offerBasic = this.enableBasic && (this.insecureBasic || req.isSecure());
        if (msg != null && (msg.startsWith("NTLM ") || (offerBasic && msg.startsWith("Basic ")))) {
            if (msg.startsWith("NTLM ")) {
                HttpSession ssn3 = req.getSession();
                if (this.loadBalance) {
                    NtlmChallenge chal = (NtlmChallenge) ssn3.getAttribute("NtlmHttpChal");
                    if (chal == null) {
                        chal = SmbSession.getChallengeForDomain();
                        ssn3.setAttribute("NtlmHttpChal", chal);
                    }
                    dc = chal.dc;
                    challenge = chal.challenge;
                } else {
                    dc = UniAddress.getByName(this.domainController, true);
                    challenge = SmbSession.getChallenge(dc);
                }
                ntlm = NtlmSsp.authenticate(req, resp, challenge);
                if (ntlm == null) {
                    return null;
                }
                ssn3.removeAttribute("NtlmHttpChal");
            } else {
                String auth = new String(Base64.decode(msg.substring(6)), "US-ASCII");
                int index = auth.indexOf(58);
                if (index != -1) {
                    user = auth.substring(0, index);
                } else {
                    user = auth;
                }
                String password = index != -1 ? auth.substring(index + 1) : "";
                int index2 = user.indexOf(92);
                if (index2 == -1) {
                    index2 = user.indexOf(47);
                }
                if (index2 != -1) {
                    domain = user.substring(0, index2);
                } else {
                    domain = this.defaultDomain;
                }
                if (index2 != -1) {
                    user = user.substring(index2 + 1);
                }
                ntlm = new NtlmPasswordAuthentication(domain, user, password);
                dc = UniAddress.getByName(this.domainController, true);
            }
            try {
                SmbSession.logon(dc, ntlm);
                LogStream logStream = log;
                if (LogStream.level > 2) {
                    log.println("NtlmHttpFilter: " + ntlm + " successfully authenticated against " + dc);
                }
                req.getSession().setAttribute("NtlmHttpAuth", ntlm);
            } catch (SmbAuthException sae) {
                LogStream logStream2 = log;
                if (LogStream.level > 1) {
                    log.println("NtlmHttpFilter: " + ntlm.getName() + ": 0x" + Hexdump.toHexString(sae.getNtStatus(), 8) + ": " + sae);
                }
                if (sae.getNtStatus() == -1073741819 && (ssn2 = req.getSession(false)) != null) {
                    ssn2.removeAttribute("NtlmHttpAuth");
                }
                resp.setHeader("WWW-Authenticate", "NTLM");
                if (offerBasic) {
                    resp.addHeader("WWW-Authenticate", "Basic realm=\"" + this.realm + "\"");
                }
                resp.setStatus(401);
                resp.setContentLength(0);
                resp.flushBuffer();
                return null;
            }
        } else if (!skipAuthentication && ((ssn = req.getSession(false)) == null || (ntlm = (NtlmPasswordAuthentication) ssn.getAttribute("NtlmHttpAuth")) == null)) {
            resp.setHeader("WWW-Authenticate", "NTLM");
            if (offerBasic) {
                resp.addHeader("WWW-Authenticate", "Basic realm=\"" + this.realm + "\"");
            }
            resp.setStatus(401);
            resp.setContentLength(0);
            resp.flushBuffer();
            return null;
        }
        return ntlm;
    }

    public void setFilterConfig(FilterConfig f) {
        try {
            init(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FilterConfig getFilterConfig() {
        return null;
    }
}
