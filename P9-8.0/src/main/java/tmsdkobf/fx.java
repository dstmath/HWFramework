package tmsdkobf;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;

public final class fx {
    private String[] mArgs;
    private int nA;
    private String nB;
    private String ny = null;
    private Intent nz = null;

    public fx(String str) {
        this.mArgs = str.split(" ");
        if (this.mArgs != null && this.mArgs.length >= 1) {
            this.nA = 1;
            this.nB = null;
            return;
        }
        throw new IllegalArgumentException("Illegal argument: " + str);
    }

    private String K() {
        if (this.nB != null) {
            throw new IllegalArgumentException("No argument expected after \"" + this.mArgs[this.nA - 1] + "\"");
        } else if (this.nA >= this.mArgs.length) {
            return null;
        } else {
            String str = this.mArgs[this.nA];
            if (!str.startsWith("-")) {
                return null;
            }
            this.nA++;
            if (str.equals("--")) {
                return null;
            }
            if (str.length() <= 1 || str.charAt(1) == '-') {
                this.nB = null;
                return str;
            } else if (str.length() <= 2) {
                this.nB = null;
                return str;
            } else {
                this.nB = str.substring(2);
                return str.substring(0, 2);
            }
        }
    }

    private String L() {
        if (this.nB != null) {
            String str = this.nB;
            this.nB = null;
            return str;
        } else if (this.nA >= this.mArgs.length) {
            return null;
        } else {
            String[] strArr = this.mArgs;
            int i = this.nA;
            this.nA = i + 1;
            return strArr[i];
        }
    }

    private String M() {
        String L = L();
        if (L != null) {
            return L;
        }
        throw new IllegalArgumentException("Argument expected after \"" + this.mArgs[this.nA - 1] + "\"");
    }

    private void N() {
        if (this.mArgs != null && this.mArgs.length > 0) {
            try {
                this.ny = M();
                this.nz = O();
            } catch (Exception e) {
                this.ny = null;
                this.nz = null;
            }
        }
        this.mArgs = null;
    }

    private Intent O() throws URISyntaxException {
        Intent intent = new Intent();
        Object obj = null;
        Uri uri = null;
        String str = null;
        while (true) {
            String K = K();
            String L;
            if (K == null) {
                intent.setDataAndType(uri, str);
                L = L();
                if (L != null) {
                    Intent parseUri;
                    if (L.indexOf(58) >= 0) {
                        parseUri = Intent.parseUri(L, 1);
                        intent.addCategory("android.intent.category.BROWSABLE");
                        intent.setComponent(null);
                        intent.setSelector(null);
                    } else if (L.indexOf(47) < 0) {
                        parseUri = new Intent("android.intent.action.MAIN");
                        parseUri.addCategory("android.intent.category.LAUNCHER");
                        parseUri.setPackage(L);
                    } else {
                        parseUri = new Intent("android.intent.action.MAIN");
                        parseUri.addCategory("android.intent.category.LAUNCHER");
                        parseUri.setComponent(ComponentName.unflattenFromString(L));
                    }
                    Bundle extras = intent.getExtras();
                    intent.replaceExtras((Bundle) null);
                    Bundle extras2 = parseUri.getExtras();
                    parseUri.replaceExtras((Bundle) null);
                    if (!(intent.getAction() == null || parseUri.getCategories() == null)) {
                        Iterator it = new HashSet(parseUri.getCategories()).iterator();
                        while (it.hasNext()) {
                            parseUri.removeCategory((String) it.next());
                        }
                    }
                    intent.fillIn(parseUri, 8);
                    if (extras != null) {
                        if (extras2 != null) {
                            extras2.putAll(extras);
                        }
                        intent.replaceExtras(extras);
                        obj = 1;
                    }
                    extras = extras2;
                    intent.replaceExtras(extras);
                    obj = 1;
                }
                if (obj != null) {
                    return intent;
                }
                throw new IllegalArgumentException("No intent supplied");
            }
            String[] split;
            int i;
            if (K.equals("-a")) {
                intent.setAction(M());
            } else if (K.equals("-d")) {
                uri = Uri.parse(M());
                obj = 1;
            } else if (K.equals("-t")) {
                str = M();
                obj = 1;
            } else if (K.equals("-c")) {
                intent.addCategory(M());
            } else if (K.equals("-e") || K.equals("--es")) {
                intent.putExtra(M(), M());
            } else if (K.equals("--esn")) {
                intent.putExtra(M(), (String) null);
            } else if (K.equals("--ei")) {
                intent.putExtra(M(), Integer.valueOf(M()));
            } else if (K.equals("--eu")) {
                intent.putExtra(M(), Uri.parse(M()));
            } else if (K.equals("--eia")) {
                L = M();
                split = M().split(",");
                int[] iArr = new int[split.length];
                for (i = 0; i < split.length; i++) {
                    iArr[i] = Integer.valueOf(split[i]).intValue();
                }
                intent.putExtra(L, iArr);
            } else if (K.equals("--el")) {
                intent.putExtra(M(), Long.valueOf(M()));
            } else if (K.equals("--ela")) {
                L = M();
                split = M().split(",");
                long[] jArr = new long[split.length];
                for (i = 0; i < split.length; i++) {
                    jArr[i] = Long.valueOf(split[i]).longValue();
                }
                intent.putExtra(L, jArr);
            } else if (K.equals("--ez")) {
                intent.putExtra(M(), Boolean.valueOf(M()));
            } else if (K.equals("-n")) {
                L = M();
                ComponentName unflattenFromString = ComponentName.unflattenFromString(L);
                if (unflattenFromString != null) {
                    intent.setComponent(unflattenFromString);
                } else {
                    throw new IllegalArgumentException("Bad component name: " + L);
                }
            } else if (K.equals("-f")) {
                intent.setFlags(Integer.decode(M()).intValue());
            } else if (K.equals("-p")) {
                intent.setPackage(M());
            } else if (!K.equals("--exclude-stopped-packages")) {
                return null;
            } else {
                intent.addFlags(16);
            }
            obj = 1;
        }
    }

    public String J() {
        N();
        return this.ny;
    }

    public boolean d(Context context) {
        try {
            String J = J();
            if (J == null) {
                return false;
            }
            if (J.equals("start")) {
                getIntent().addFlags(268435456);
                context.startActivity(getIntent());
                return true;
            } else if (J.equals("startservice")) {
                context.startService(getIntent());
                return true;
            } else {
                if (J.equals("broadcast")) {
                    context.sendBroadcast(getIntent());
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
        }
    }

    public Intent getIntent() {
        N();
        return this.nz;
    }
}
