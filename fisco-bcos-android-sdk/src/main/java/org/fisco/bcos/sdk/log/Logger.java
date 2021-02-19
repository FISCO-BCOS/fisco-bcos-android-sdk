package org.fisco.bcos.sdk.log;

import android.util.Log;

public class Logger {
    protected String tag = "BcosSDKLogger";
    protected int effectiveLevel = Log.VERBOSE;

    protected Logger() {}

    public Logger(String className) {
        this.tag = className;
    }

    public void setLevel(int level) {
        this.effectiveLevel = level;
    }

    public void error(String format, Object... arg) {
        if (isErrorEnabled()) {
            Log.e(this.tag, String.format(format, arg));
        }
    }

    public void warn(String format, Object... arg) {
        if (isWarnEnabled()) {
            Log.w(this.tag, String.format(format, arg));
        }
    }

    public void info(String format, Object... arg) {
        if (isInfoEnabled()) {
            Log.i(this.tag, String.format(format, arg));
        }
    }

    public void debug(String format, Object... arg) {
        if (isDebugEnabled()) {
            Log.d(this.tag, String.format(format, arg));
        }
    }

    public void trace(String format, Object... arg) {
        if (isTraceEnabled()) {
            Log.v(this.tag, String.format(format, arg));
        }
    }

    public boolean isErrorEnabled() {
        return Log.ERROR >= this.effectiveLevel;
    }

    public boolean isWarnEnabled() {
        return Log.WARN >= this.effectiveLevel;
    }

    public boolean isInfoEnabled() {
        return Log.INFO >= this.effectiveLevel;
    }

    public boolean isDebugEnabled() {
        return Log.DEBUG >= this.effectiveLevel;
    }

    public boolean isTraceEnabled() {
        return Log.VERBOSE >= this.effectiveLevel;
    }
}
