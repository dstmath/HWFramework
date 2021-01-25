package gov.nist.javax.sip.clientauthutils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import javax.sip.header.AuthorizationHeader;

/* access modifiers changed from: package-private */
public class CredentialsCache {
    private ConcurrentHashMap<String, List<AuthorizationHeader>> authorizationHeaders = new ConcurrentHashMap<>();
    private Timer timer;

    /* access modifiers changed from: package-private */
    public class TimeoutTask extends TimerTask {
        String callId;
        String userName;

        public TimeoutTask(String userName2, String proxyDomain) {
            this.callId = proxyDomain;
            this.userName = userName2;
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            CredentialsCache.this.authorizationHeaders.remove(this.callId);
        }
    }

    CredentialsCache(Timer timer2) {
        this.timer = timer2;
    }

    /* access modifiers changed from: package-private */
    public void cacheAuthorizationHeader(String callId, AuthorizationHeader authorization, int cacheTime) {
        String user = authorization.getUsername();
        if (callId != null) {
            List<AuthorizationHeader> authHeaders = this.authorizationHeaders.get(callId);
            if (authHeaders == null) {
                authHeaders = new LinkedList();
                this.authorizationHeaders.put(callId, authHeaders);
            } else {
                String realm = authorization.getRealm();
                ListIterator<AuthorizationHeader> li = authHeaders.listIterator();
                while (li.hasNext()) {
                    if (realm.equals(li.next().getRealm())) {
                        li.remove();
                    }
                }
            }
            authHeaders.add(authorization);
            TimeoutTask timeoutTask = new TimeoutTask(callId, user);
            if (cacheTime != -1) {
                this.timer.schedule(timeoutTask, (long) (cacheTime * 1000));
                return;
            }
            return;
        }
        throw new NullPointerException("Call ID is null!");
    }

    /* access modifiers changed from: package-private */
    public Collection<AuthorizationHeader> getCachedAuthorizationHeaders(String callid) {
        if (callid != null) {
            return this.authorizationHeaders.get(callid);
        }
        throw new NullPointerException("Null arg!");
    }

    public void removeAuthenticationHeader(String callId) {
        this.authorizationHeaders.remove(callId);
    }
}
