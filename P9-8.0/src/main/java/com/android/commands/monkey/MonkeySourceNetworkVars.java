package com.android.commands.monkey;

import android.hardware.display.DisplayManagerGlobal;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Display;
import com.android.commands.monkey.MonkeySourceNetwork.CommandQueue;
import com.android.commands.monkey.MonkeySourceNetwork.MonkeyCommand;
import com.android.commands.monkey.MonkeySourceNetwork.MonkeyCommandReturn;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MonkeySourceNetworkVars {
    private static final Map<String, VarGetter> VAR_MAP = new TreeMap();

    private interface VarGetter {
        String get();
    }

    public static class GetVarCommand implements MonkeyCommand {
        public MonkeyCommandReturn translateCommand(List<String> command, CommandQueue queue) {
            if (command.size() != 2) {
                return MonkeySourceNetwork.EARG;
            }
            VarGetter getter = (VarGetter) MonkeySourceNetworkVars.VAR_MAP.get(command.get(1));
            if (getter == null) {
                return new MonkeyCommandReturn(false, "unknown var");
            }
            return new MonkeyCommandReturn(true, getter.get());
        }
    }

    public static class ListVarCommand implements MonkeyCommand {
        public MonkeyCommandReturn translateCommand(List<String> list, CommandQueue queue) {
            Set<String> keys = MonkeySourceNetworkVars.VAR_MAP.keySet();
            StringBuffer sb = new StringBuffer();
            for (String key : keys) {
                sb.append(key).append(" ");
            }
            return new MonkeyCommandReturn(true, sb.toString());
        }
    }

    private static class StaticVarGetter implements VarGetter {
        private final String value;

        public StaticVarGetter(String value) {
            this.value = value;
        }

        public String get() {
            return this.value;
        }
    }

    static {
        VAR_MAP.put("build.board", new StaticVarGetter(Build.BOARD));
        VAR_MAP.put("build.brand", new StaticVarGetter(Build.BRAND));
        VAR_MAP.put("build.device", new StaticVarGetter(Build.DEVICE));
        VAR_MAP.put("build.display", new StaticVarGetter(Build.DISPLAY));
        VAR_MAP.put("build.fingerprint", new StaticVarGetter(Build.FINGERPRINT));
        VAR_MAP.put("build.host", new StaticVarGetter(Build.HOST));
        VAR_MAP.put("build.id", new StaticVarGetter(Build.ID));
        VAR_MAP.put("build.model", new StaticVarGetter(Build.MODEL));
        VAR_MAP.put("build.product", new StaticVarGetter(Build.PRODUCT));
        VAR_MAP.put("build.tags", new StaticVarGetter(Build.TAGS));
        VAR_MAP.put("build.brand", new StaticVarGetter(Long.toString(Build.TIME)));
        VAR_MAP.put("build.type", new StaticVarGetter(Build.TYPE));
        VAR_MAP.put("build.user", new StaticVarGetter(Build.USER));
        VAR_MAP.put("build.cpu_abi", new StaticVarGetter(Build.CPU_ABI));
        VAR_MAP.put("build.manufacturer", new StaticVarGetter(Build.MANUFACTURER));
        VAR_MAP.put("build.version.incremental", new StaticVarGetter(VERSION.INCREMENTAL));
        VAR_MAP.put("build.version.release", new StaticVarGetter(VERSION.RELEASE));
        VAR_MAP.put("build.version.sdk", new StaticVarGetter(Integer.toString(VERSION.SDK_INT)));
        VAR_MAP.put("build.version.codename", new StaticVarGetter(VERSION.CODENAME));
        Display display = DisplayManagerGlobal.getInstance().getRealDisplay(0);
        VAR_MAP.put("display.width", new StaticVarGetter(Integer.toString(display.getWidth())));
        VAR_MAP.put("display.height", new StaticVarGetter(Integer.toString(display.getHeight())));
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        VAR_MAP.put("display.density", new StaticVarGetter(Float.toString(dm.density)));
        VAR_MAP.put("am.current.package", new VarGetter() {
            public String get() {
                return Monkey.currentPackage;
            }
        });
        VAR_MAP.put("am.current.action", new VarGetter() {
            public String get() {
                if (Monkey.currentIntent == null) {
                    return null;
                }
                return Monkey.currentIntent.getAction();
            }
        });
        VAR_MAP.put("am.current.comp.class", new VarGetter() {
            public String get() {
                if (Monkey.currentIntent == null) {
                    return null;
                }
                return Monkey.currentIntent.getComponent().getClassName();
            }
        });
        VAR_MAP.put("am.current.comp.package", new VarGetter() {
            public String get() {
                if (Monkey.currentIntent == null) {
                    return null;
                }
                return Monkey.currentIntent.getComponent().getPackageName();
            }
        });
        VAR_MAP.put("am.current.data", new VarGetter() {
            public String get() {
                if (Monkey.currentIntent == null) {
                    return null;
                }
                return Monkey.currentIntent.getDataString();
            }
        });
        VAR_MAP.put("am.current.categories", new VarGetter() {
            public String get() {
                if (Monkey.currentIntent == null) {
                    return null;
                }
                StringBuffer sb = new StringBuffer();
                for (String cat : Monkey.currentIntent.getCategories()) {
                    sb.append(cat).append(" ");
                }
                return sb.toString();
            }
        });
        VAR_MAP.put("clock.realtime", new VarGetter() {
            public String get() {
                return Long.toString(SystemClock.elapsedRealtime());
            }
        });
        VAR_MAP.put("clock.uptime", new VarGetter() {
            public String get() {
                return Long.toString(SystemClock.uptimeMillis());
            }
        });
        VAR_MAP.put("clock.millis", new VarGetter() {
            public String get() {
                return Long.toString(System.currentTimeMillis());
            }
        });
        VAR_MAP.put("monkey.version", new VarGetter() {
            public String get() {
                return Integer.toString(2);
            }
        });
    }
}
