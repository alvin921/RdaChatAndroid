package com.rda.util;

/**
 * Created by mingangwang on 2016/8/1.
 */

public class DebugInfo extends Exception {
    public int line() {
        StackTraceElement[] trace = getStackTrace();
        if (trace == null || trace.length == 0) {
            return -1;
        }
        return trace[0].getLineNumber();
    }

    public String func() {
        StackTraceElement[] trace = getStackTrace();
        if (trace == null || trace.length == 0) {
            return "";
        }
        return trace[0].getMethodName();
    }

    public DebugInfo() {
        super();
    }

    @Override
    public String toString() {
        return func() + "|" + line() + "|";
    }
}