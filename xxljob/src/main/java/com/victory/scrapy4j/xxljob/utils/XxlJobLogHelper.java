package com.victory.scrapy4j.xxljob.utils;

import org.apache.ibatis.reflection.ExceptionUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class XxlJobLogHelper {
    private com.xxl.job.core.log.XxlJobLogger xxlJobLogger;

    public XxlJobLogHelper() {
    }

    public org.slf4j.Logger getProxy(com.xxl.job.core.log.XxlJobLogger xxlJobLogger) {
        this.xxlJobLogger = xxlJobLogger;
        return (org.slf4j.Logger) Proxy.newProxyInstance(XxlJobLogHelper.class.getClassLoader(), new Class[]{org.slf4j.Logger.class}, new XxlJobLogHelper.Interceptor());
    }

    private class Interceptor implements InvocationHandler {
        private Interceptor() {
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object unwrapped;
            try {
                Object[] newArgs = new Object[2];
                newArgs[0] = args[0];
                newArgs[1] = null;
                if (args.length > 1) {
                    Object[] objects = new Object[args.length - 1];
                    System.arraycopy(args, 1, objects, 0, args.length - 1);
                    newArgs[1] = objects;
                }
                Method newMethod = xxlJobLogger.getClass().getMethod("log", String.class,Object[].class);
                unwrapped = newMethod.invoke(xxlJobLogger,newArgs);
            } catch (Throwable var11) {
                unwrapped = ExceptionUtil.unwrapThrowable(var11);
            }
            return unwrapped;
        }
    }
}
