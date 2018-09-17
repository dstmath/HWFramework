package com.android.internal.telephony.dataconnection;

import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.google.android.mms.pdu.PduPart;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DataCallResponse {
    private final boolean DBG;
    private final String LOG_TAG;
    public int active;
    public String[] addresses;
    public int cid;
    public String[] dnses;
    public String[] gateways;
    public String ifname;
    public int mtu;
    public String[] pcscf;
    public int status;
    public int suggestedRetryTime;
    public String type;
    public int version;

    public enum SetupResult {
        ;
        
        public DcFailCause mFailCause;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.dataconnection.DataCallResponse.SetupResult.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.dataconnection.DataCallResponse.SetupResult.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DataCallResponse.SetupResult.<clinit>():void");
        }

        public String toString() {
            return name() + "  SetupResult.mFailCause=" + this.mFailCause;
        }
    }

    public DataCallResponse() {
        this.DBG = true;
        this.LOG_TAG = "DataCallResponse";
        this.version = 0;
        this.status = 0;
        this.cid = 0;
        this.active = 0;
        this.type = "";
        this.ifname = "";
        this.addresses = new String[0];
        this.dnses = new String[0];
        this.gateways = new String[0];
        this.suggestedRetryTime = -1;
        this.pcscf = new String[0];
        this.mtu = 0;
    }

    public String toString() {
        int i = 0;
        StringBuffer sb = new StringBuffer();
        sb.append("DataCallResponse: {").append("version=").append(this.version).append(" status=").append(this.status).append(" retry=").append(this.suggestedRetryTime).append(" cid=").append(this.cid).append(" active=").append(this.active).append(" type=").append(this.type).append(" ifname=").append(this.ifname).append(" mtu=").append(this.mtu).append(" addresses=[*");
        sb.append("] dnses=[");
        for (String addr : this.dnses) {
            sb.append(addr);
            sb.append(",");
        }
        if (this.dnses.length > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("] gateways=[*");
        sb.append("] pcscf=[");
        String[] strArr = this.pcscf;
        int length = strArr.length;
        while (i < length) {
            sb.append(strArr[i]);
            sb.append(",");
            i++;
        }
        if (this.pcscf.length > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("]}");
        return sb.toString();
    }

    public SetupResult setLinkProperties(LinkProperties linkProperties, boolean okToUseSystemPropertyDns, int subId) {
        String addr;
        SetupResult result;
        if (linkProperties == null) {
            linkProperties = new LinkProperties();
        } else {
            linkProperties.clear();
        }
        if (this.status == DcFailCause.NONE.getErrorCode()) {
            String propertyPrefix = "net." + this.ifname + ".";
            linkProperties.setInterfaceName(this.ifname);
            if (this.addresses == null || this.addresses.length <= 0) {
                throw new UnknownHostException("no address for ifname=" + this.ifname);
            }
            InetAddress ia;
            String dnsAddr;
            for (String addr2 : this.addresses) {
                addr2 = addr2.trim();
                if (!addr2.isEmpty()) {
                    int addrPrefixLen;
                    String[] ap = addr2.split("/");
                    int length = ap.length;
                    if (r0 == 2) {
                        addr2 = ap[0];
                        addrPrefixLen = Integer.parseInt(ap[1]);
                    } else {
                        addrPrefixLen = 0;
                    }
                    try {
                        ia = NetworkUtils.numericToInetAddress(addr2);
                        if (ia.isAnyLocalAddress()) {
                            continue;
                        } else {
                            if (addrPrefixLen == 0) {
                                addrPrefixLen = ia instanceof Inet4Address ? 32 : PduPart.P_Q;
                            }
                            Rlog.d("DataCallResponse", "addr/pl=* ");
                            linkProperties.addLinkAddress(new LinkAddress(ia, addrPrefixLen));
                        }
                    } catch (IllegalArgumentException e) {
                        throw new UnknownHostException("Non-numeric gateway addr=" + addr2);
                    } catch (IllegalArgumentException e2) {
                        throw new UnknownHostException("Non-numeric dns addr=" + dnsAddr);
                    } catch (IllegalArgumentException e3) {
                        throw new UnknownHostException("Non-numeric dns addr=" + addr2);
                    } catch (IllegalArgumentException e4) {
                        throw new UnknownHostException("Bad parameter for LinkAddress, ia=" + ia.getHostAddress() + "/" + addrPrefixLen);
                    } catch (IllegalArgumentException e5) {
                        throw new UnknownHostException("Non-numeric ip addr=" + addr2);
                    } catch (UnknownHostException e6) {
                        Rlog.d("DataCallResponse", "setLinkProperties: UnknownHostException " + e6);
                        e6.printStackTrace();
                        result = SetupResult.ERR_UnacceptableParameter;
                    }
                }
            }
            if (this.dnses != null && this.dnses.length > 0) {
                for (String addr22 : this.dnses) {
                    addr22 = addr22.trim();
                    if (!addr22.isEmpty()) {
                        ia = NetworkUtils.numericToInetAddress(addr22);
                        if (!ia.isAnyLocalAddress()) {
                            linkProperties.addDnsServer(ia);
                            if (okToUseSystemPropertyDns && (ia instanceof Inet4Address)) {
                                Rlog.d("DataCallResponse", "setLinkProperties: set system property in " + subId + " dns: " + addr22);
                                SystemProperties.set("persist.radio.dns.sub" + subId, addr22);
                                okToUseSystemPropertyDns = false;
                            }
                        }
                    }
                }
            } else if (okToUseSystemPropertyDns) {
                String[] dnsServers = new String[]{SystemProperties.get("persist.radio.dns.sub" + subId, ""), "8.8.8.8"};
                Rlog.d("DataCallResponse", "setLinkProperties: use system property in " + subId + " dns: " + dnsServers[0]);
                for (String dnsAddr2 : dnsServers) {
                    dnsAddr2 = dnsAddr2.trim();
                    if (!dnsAddr2.isEmpty()) {
                        ia = NetworkUtils.numericToInetAddress(dnsAddr2);
                        if (!ia.isAnyLocalAddress()) {
                            linkProperties.addDnsServer(ia);
                        }
                    }
                }
            } else {
                Rlog.e("DataCallResponse", "Empty dns response and no system default dns");
            }
            if (this.gateways == null || this.gateways.length == 0) {
                String sysGateways = SystemProperties.get(propertyPrefix + "gw");
                if (sysGateways != null) {
                    this.gateways = sysGateways.split(" ");
                } else {
                    this.gateways = new String[0];
                }
            }
            for (String addr222 : this.gateways) {
                addr222 = addr222.trim();
                if (!addr222.isEmpty()) {
                    linkProperties.addRoute(new RouteInfo(NetworkUtils.numericToInetAddress(addr222)));
                }
            }
            linkProperties.setMtu(this.mtu);
            result = SetupResult.SUCCESS;
        } else if (this.version < 4) {
            result = SetupResult.ERR_GetLastErrorFromRil;
        } else {
            result = SetupResult.ERR_RilError;
        }
        if (result != SetupResult.SUCCESS) {
            Rlog.d("DataCallResponse", "setLinkProperties: error clearing LinkProperties status=" + this.status + " result=" + result);
            linkProperties.clear();
        }
        return result;
    }
}
