package defpackage;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import java.security.cert.X509Certificate;
import java.util.List;

/* renamed from: ac  reason: default package */
public final class ac {
    private final aj I;
    public String J;
    public final Context context;

    public ac(Context context2) {
        this.context = context2;
        this.I = new aj(context2);
        String b = b();
        if (b == null) {
            Log.e("PushLogSys", "Failed to find HMS apk");
            return;
        }
        this.J = b;
        Log.i("PushLogSys", "Succeed to find HMS apk: " + this.J);
    }

    private String b() {
        boolean z;
        try {
            List<ResolveInfo> queryIntentServices = this.context.getPackageManager().queryIntentServices(new Intent("com.huawei.hms.core.aidlservice"), 786560);
            if (queryIntentServices == null || queryIntentServices.size() == 0) {
                return null;
            }
            for (ResolveInfo resolveInfo : queryIntentServices) {
                String str = resolveInfo.serviceInfo.applicationInfo.packageName;
                Bundle bundle = resolveInfo.serviceInfo.metaData;
                if (bundle == null) {
                    Log.w("PushLogSys", "skip package " + str + " for metadata is null");
                } else if (!bundle.containsKey("hms_app_signer")) {
                    Log.w("PushLogSys", "skip package " + str + " for no signer");
                } else if (!bundle.containsKey("hms_app_cert_chain")) {
                    Log.w("PushLogSys", "skip package " + str + " for no cert chain");
                } else {
                    byte[] a = this.I.a(str);
                    String str2 = str + "&" + ((a == null || a.length == 0) ? null : ab.b(ao.digest(a)));
                    String string = bundle.getString("hms_app_signer");
                    String string2 = bundle.getString("hms_app_cert_chain");
                    if (TextUtils.isEmpty(string) || TextUtils.isEmpty(string2)) {
                        Log.w("PushLogSys", "args is invalid");
                        z = false;
                    } else {
                        List<X509Certificate> certChain = ap.getCertChain(ap.getCertChainByJson(string2));
                        if (certChain.size() == 0) {
                            Log.w("PushLogSys", "certChain is empty");
                            z = false;
                        } else if (!ap.verifyCertChain(ap.getCert(Base64.decode("LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tDQpNSUlGWkRDQ0EweWdBd0lCQWdJSVlzTExUZWhBWHBZd0RRWUpLb1pJaHZjTkFRRUxCUUF3VURFTE1Ba0dBMVVFDQpCaE1DUTA0eER6QU5CZ05WQkFvTUJraDFZWGRsYVRFVE1CRUdBMVVFQ3d3S1NIVmhkMlZwSUVOQ1J6RWJNQmtHDQpBMVVFQXd3U1NIVmhkMlZwSUVOQ1J5QlNiMjkwSUVOQk1CNFhEVEUzTURneU1URXdOVFl5TjFvWERUUXlNRGd4DQpOVEV3TlRZeU4xb3dVREVMTUFrR0ExVUVCaE1DUTA0eER6QU5CZ05WQkFvTUJraDFZWGRsYVRFVE1CRUdBMVVFDQpDd3dLU0hWaGQyVnBJRU5DUnpFYk1Ca0dBMVVFQXd3U1NIVmhkMlZwSUVOQ1J5QlNiMjkwSUVOQk1JSUNJakFODQpCZ2txaGtpRzl3MEJBUUVGQUFPQ0FnOEFNSUlDQ2dLQ0FnRUExT3lLbTNJZy82ZWliQjdVejJvOTNVcUdrMk03DQo4NFdkZkY4bXZmZnZ1MjE4ZDYxRzVNM1B4NTRFM2tlZlVUazVLeTF5d0h2dzdScDlLRHVZdjdrdGFIa2sreXI1DQo5SWhzZXUzYTdpTS9DNlNuTVNHdCtMZkIvQmNvYjlBYnc5NUVpZ1hRNHlRZGRYOWhiTnJpbjNBd1p3OHdNakVJDQpTWVlEbzVHdVlETDBOYkFpWWcyWTVHcGZZSXFSem9pNkdxRHorZXZMcnNsMjBrSmVDRVBnSlpONEpnMDBJcTlrDQorK0VLT1o1SmMvWngyMlpVZ0twZHdLQUJrdnpzaEVnRzZXV1VQQitnb3NPaUx2KytpbnUvOWJsRHBFelFaaGpaDQo5V1ZIcFVSSERLMVlsQ3Z1YlZBTWhEcG5icU5IWjBBeGxQbGV0ZG95dWdySC9PTEtsNWluaE1YTmozUmU3SGw4DQpXc0JXTFVLcDZzWEZmMGR2U0Z6cW5yMmpraGljUytLMklZWm5qZ2hDOWNPQlJPOGZua29uaDBFQnQwZXZqVUlLDQpyNUNsYkNLaW9CWDhKVStkNGxkdFdPcHAyRmx4ZUZUTHJlREo1WkJVNC8vYlFwVHdZTXQ3Z3dNSytNTzVXdG9rDQpVeDNVRjk4WjZHZFVnYmw2bkJqQmU4MmM3b0lRWGhIR0hQblVSUU83RERQZ3lWbk5PblRQSWttaUhKaC9lM3ZrDQpWaGlaTkhGQ0NMVGlwNkdvSlZyTHh3YjlpNHErZDB0aHc0ZG94Vko1TkI5T2ZETVY2NC95YkpncGY3bTNMZDJ5DQpFMGdzZjFwcnJSbERGRFhqbFl5cXFwZjFsOVkwdTNjdFhvN1VwWE1nYnlERXBVUWhxM2E3dHhaUU8vMTdsdVREDQpvQTZUejFBRGF2dkJ3SGtDQXdFQUFhTkNNRUF3RGdZRFZSMFBBUUgvQkFRREFnRUdNQThHQTFVZEV3RUIvd1FGDQpNQU1CQWY4d0hRWURWUjBPQkJZRUZLckUwM2xINkc0amErL3dxV3dpY3oxNkdXbWhNQTBHQ1NxR1NJYjNEUUVCDQpDd1VBQTRJQ0FRQzFkM1RNQitWSFpkR3JXSmJmYUJTaEZOaUNUTi9NY2VTSE9wekJuNkp1bVFQNE43bXhDT3dkDQpSU3NHS1F4VjJOUEg3TFRYV05oVXZVdzVTZWs5NkZXeC8rT2E3anNqM1dOQVZ0bVMzektwQ1E1aUdiMDhXSVJPDQpjRm54M29VUTVyY084ci9sVWs3UTJjTjBFK3JGNHhzZFFySDlrMmNkM2tBWFpYQmpmeGZLUEpUZFB5MVhuWlIvDQpoOEg1RXdFSzVEV2pTeksxd0tkM0cvRnhkbTNFMjNwY3I0RlpnZFlkT2xGU2lxVzJUSjNRZTZsRjRHT0tPT3lkDQpXSGtwdTU0aWVUc3FvWWN1TUtuS01qVDJTTE5OZ3Y5R3U1aXBhRzhPbHo2ZzlDN0h0cDk0M2xtSy8xVnRuaGdnDQpwTDNyRFRzRlgvK2VoazdPdHh1TnpSTUQ5bFhVdEVmb2s3ZjhYQjBkY0w0WmpuRWhEbXA1UVpxQzFrTXViSFF0DQpRblRhdUVpdjBZa1NHT3dKQVVacEsxUElmZjVHZ3hYWWZhSGZCQzZPcDRxMDJwcGw1UTNVUmw3WElqWUxqdnM5DQp0NFM5eFBlOHRiNjQxNlYyZmUxZFo2MnZPWE1NS0hrWmpWaWhoK0ljZVlwSllIdXlmS29ZSnlhaExPUVhaeWtHDQpLNWlQQUVFdHEzSFBmTVZGNDNSS0hPd2ZockFINUt3ZWxVQS8wRWtjUjRHenRoMU1LRXFvamRuWU5lbWtrU3k3DQphTlBQVDRMRW01UjdzVjZ2RzFDandiZ3ZRcldDZ2M0bk1iOG5nZGZuVkY3WWRxanFpOVNBcVV6SWs0K1VmMFpZDQorNlJZNUljSGRDYWlQYVdJRTF4VVJROEIwRFJVVVJzUXdYZGpaaGdMTi9ES0pwQ2w1YUNDeGc9PQ0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQ0K", 2)), certChain)) {
                            Log.w("PushLogSys", "failed to verify cert chain");
                            z = false;
                        } else {
                            X509Certificate x509Certificate = certChain.get(certChain.size() - 1);
                            if (!ap.checkSubject(x509Certificate, "CN", "Huawei CBG HMS")) {
                                Log.w("PushLogSys", "CN is invalid");
                                z = false;
                            } else if (!ap.checkSubject(x509Certificate, "OU", "Huawei CBG Cloud Security Signer")) {
                                Log.w("PushLogSys", "OU is invalid");
                                z = false;
                            } else if (!ap.a(x509Certificate, str2, string)) {
                                Log.w("PushLogSys", "signature is invalid: ".concat(String.valueOf(str2)));
                                z = false;
                            } else {
                                z = true;
                            }
                        }
                    }
                    if (z) {
                        return str;
                    }
                    Log.w("PushLogSys", "checkSinger failed");
                }
            }
            return null;
        } catch (Exception e) {
            Log.e("PushLogSys", "getHmsPackageName failed");
        }
    }
}
