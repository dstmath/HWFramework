package com.android.server.hidata.hicure;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DnsProbe {
    private static final String TAG = "DnsProbe";
    private int MAX_DOMAIN_CNT = 4;
    /* access modifiers changed from: private */
    public int mCheckMsg;
    private DNSLookupThread[] mDnsLookup = new DNSLookupThread[this.MAX_DOMAIN_CNT];
    private String[] mDomains;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private boolean mMessageSent;
    private int mMsg;
    private int mThreadCnt;
    /* access modifiers changed from: private */
    public int mdelaytime;
    /* access modifiers changed from: private */
    public int mnetid;

    private class DNSLookupThread extends Thread {
        private String hostname;

        public DNSLookupThread(String hostname2) {
            this.hostname = hostname2;
        }

        public void run() {
            try {
                Log.d(DnsProbe.TAG, "hostname");
                long before = SystemClock.elapsedRealtime();
                InetAddress add = InetAddress.getByNameOnNet(this.hostname, DnsProbe.this.mnetid);
                long delay = SystemClock.elapsedRealtime() - before;
                if (add != null) {
                    Log.d(DnsProbe.TAG, "hostname/addr not null, delay=" + delay);
                } else {
                    Log.d(DnsProbe.TAG, "hostname/addr is null, delay=" + delay);
                }
                if (add != null && delay < ((long) DnsProbe.this.mdelaytime) && DnsProbe.this.mHandler.hasMessages(DnsProbe.this.mCheckMsg)) {
                    DnsProbe.this.sendProbeMessage();
                }
            } catch (UnknownHostException e) {
                Log.d(DnsProbe.TAG, "UnknownHostException");
            }
        }
    }

    public DnsProbe(String[] Domains, int time, int netId, Handler handler, int msg, int checkMsg) {
        this.mThreadCnt = 0;
        this.mDomains = new String[this.MAX_DOMAIN_CNT];
        this.mMessageSent = false;
        this.mHandler = handler;
        this.mMsg = msg;
        this.mCheckMsg = checkMsg;
        this.mnetid = netId;
        this.mdelaytime = time;
        this.mThreadCnt = Domains.length > this.MAX_DOMAIN_CNT ? this.MAX_DOMAIN_CNT : Domains.length;
        for (int i = 0; i < this.mThreadCnt; i++) {
            this.mDomains[i] = Domains[i];
        }
    }

    public void startProbe() {
        int i = 0;
        while (i < this.mThreadCnt) {
            try {
                this.mDnsLookup[i] = new DNSLookupThread(this.mDomains[i]);
                this.mDnsLookup[i].start();
                i++;
            } catch (IllegalThreadStateException e) {
                Log.e(TAG, "IllegalThreadStateException");
                return;
            }
        }
    }

    public synchronized void sendProbeMessage() {
        if (!this.mMessageSent) {
            this.mMessageSent = true;
            this.mHandler.sendMessage(Message.obtain(this.mHandler, this.mMsg));
        } else {
            Log.d(TAG, "message have sent");
        }
    }
}
