package com.victory.scrapy4j.core.support.mybatis.core.override;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.SqlSession;

public class MybatisMapperProxy<T> implements InvocationHandler, Serializable {
    private static final long serialVersionUID = -6424540398559729838L;
    private static final int ALLOWED_MODES = 15;
    private static final Constructor<Lookup> lookupConstructor;
    private static final Method privateLookupInMethod;
    private final SqlSession sqlSession;
    private final Class<T> mapperInterface;
    private final Map<Method, MybatisMapperMethod> methodCache;

    public MybatisMapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MybatisMapperMethod> methodCache) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            }

            if (method.isDefault()) {
                if (privateLookupInMethod == null) {
                    return this.invokeDefaultMethodJava8(proxy, method, args);
                }

                return this.invokeDefaultMethodJava9(proxy, method, args);
            }
        } catch (Throwable var5) {
            throw ExceptionUtil.unwrapThrowable(var5);
        }

        MybatisMapperMethod mapperMethod = this.cachedMapperMethod(method);
        return mapperMethod.execute(this.sqlSession, args);
    }

    private MybatisMapperMethod cachedMapperMethod(Method method) {
        return (MybatisMapperMethod)this.methodCache.computeIfAbsent(method, (k) -> {
            return new MybatisMapperMethod(this.mapperInterface, method, this.sqlSession.getConfiguration());
        });
    }

    private Object invokeDefaultMethodJava9(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> declaringClass = method.getDeclaringClass();
        return ((Lookup)privateLookupInMethod.invoke((Object)null, declaringClass, MethodHandles.lookup())).findSpecial(declaringClass, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()), declaringClass).bindTo(proxy).invokeWithArguments(args);
    }

    private Object invokeDefaultMethodJava8(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> declaringClass = method.getDeclaringClass();
        return ((Lookup)lookupConstructor.newInstance(declaringClass, 15)).unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
    }

    static {
        Method privateLookupIn;
        try {
            privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, Lookup.class);
        } catch (NoSuchMethodException var5) {
            privateLookupIn = null;
        }

        privateLookupInMethod = privateLookupIn;
        Constructor<Lookup> lookup = null;
        if (privateLookupInMethod == null) {
            try {
                lookup = Lookup.class.getDeclaredConstructor(Class.class, Integer.TYPE);
                lookup.setAccessible(true);
            } catch (NoSuchMethodException var3) {
                throw new IllegalStateException("There is neither 'privateLookupIn(Class, Lookup)' nor 'Lookup(Class, int)' method in java.lang.invoke.MethodHandles.", var3);
            } catch (Throwable var4) {
                lookup = null;
            }
        }

        lookupConstructor = lookup;
    }
}
