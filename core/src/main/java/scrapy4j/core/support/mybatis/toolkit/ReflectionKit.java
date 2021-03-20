package scrapy4j.core.support.mybatis.toolkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.hutool.core.lang.Assert;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

public final class ReflectionKit {
    private static final Log logger = LogFactory.getLog(ReflectionKit.class);
    private static final Map<Class<?>, List<Field>> CLASS_FIELD_CACHE = new ConcurrentHashMap();
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_TYPE_MAP = new IdentityHashMap(8);

    public ReflectionKit() {
    }

    /** @deprecated */
    @Deprecated
    public static String getMethodCapitalize(Field field, final String str) {
        Class<?> fieldType = field.getType();
        return StringUtils.guessGetterName(str, fieldType);
    }

    /** @deprecated */
    @Deprecated
    public static String setMethodCapitalize(Field field, final String str) {
        return StringUtils.concatCapitalize("set", str);
    }

    /** @deprecated */
    @Deprecated
    public static Object getMethodValue(Class<?> cls, Object entity, String str) {
        Map fieldMaps = getFieldMap(cls);

        try {
            Assert.notEmpty(fieldMaps, "Error: NoSuchField in %s for %s.  Cause:", new Object[]{cls.getSimpleName(), str});
            Method method = cls.getMethod(guessGetterName((Field)fieldMaps.get(str), str));
            return method.invoke(entity);
        } catch (NoSuchMethodException var5) {
            throw ExceptionUtils.mpe("Error: NoSuchMethod in %s.  Cause:", var5, new Object[]{cls.getSimpleName()});
        } catch (IllegalAccessException var6) {
            throw ExceptionUtils.mpe("Error: Cannot execute a private method. in %s.  Cause:", var6, new Object[]{cls.getSimpleName()});
        } catch (InvocationTargetException var7) {
            throw ExceptionUtils.mpe("Error: InvocationTargetException on getMethodValue.  Cause:" + var7, new Object[0]);
        }
    }

    public static Object getFieldValue(Object entity, String fieldName) {
        Class cls = entity.getClass();
        Map fieldMaps = getFieldMap(cls);

        try {
            Field field = (Field)fieldMaps.get(fieldName);
            Assert.notNull(field, "Error: NoSuchField in %s for %s.  Cause:", new Object[]{cls.getSimpleName(), fieldName});
            field.setAccessible(true);
            return field.get(entity);
        } catch (ReflectiveOperationException var5) {
            throw ExceptionUtils.mpe("Error: Cannot read field in %s.  Cause:", var5, new Object[]{cls.getSimpleName()});
        }
    }

    /** @deprecated */
    @Deprecated
    private static String guessGetterName(Field field, final String str) {
        return StringUtils.guessGetterName(str, field.getType());
    }

    /** @deprecated */
    @Deprecated
    public static Object getMethodValue(Object entity, String str) {
        return null == entity ? null : getMethodValue(entity.getClass(), entity, str);
    }

    public static Class<?> getSuperClassGenericType(final Class<?> clazz, final int index) {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            logger.warn(String.format("Warn: %s's superclass not ParameterizedType", clazz.getSimpleName()));
            return Object.class;
        } else {
            Type[] params = ((ParameterizedType)genType).getActualTypeArguments();
            if (index < params.length && index >= 0) {
                if (!(params[index] instanceof Class)) {
                    logger.warn(String.format("Warn: %s not set the actual class on superclass generic parameter", clazz.getSimpleName()));
                    return Object.class;
                } else {
                    return (Class)params[index];
                }
            } else {
                logger.warn(String.format("Warn: Index: %s, Size of %s's Parameterized Type: %s .", index, clazz.getSimpleName(), params.length));
                return Object.class;
            }
        }
    }

    public static Map<String, Field> getFieldMap(Class<?> clazz) {
        List<Field> fieldList = getFieldList(clazz);
        return CollectionUtils.isNotEmpty(fieldList) ? (Map)fieldList.stream().collect(Collectors.toMap(Field::getName, (field) -> {
            return field;
        })) : Collections.emptyMap();
    }

    public static List<Field> getFieldList(Class<?> clazz) {
        if (Objects.isNull(clazz)) {
            return Collections.emptyList();
        } else {
            List<Field> fields = (List)CLASS_FIELD_CACHE.get(clazz);
            if (CollectionUtils.isEmpty(fields)) {
                synchronized(CLASS_FIELD_CACHE) {
                    fields = doGetFieldList(clazz);
                    CLASS_FIELD_CACHE.put(clazz, fields);
                }
            }

            return fields;
        }
    }

    public static List<Field> doGetFieldList(Class<?> clazz) {
        if (clazz.getSuperclass() != null) {
            Map<String, Field> fieldMap = excludeOverrideSuperField(clazz.getDeclaredFields(), getFieldList(clazz.getSuperclass()));
            return (List)fieldMap.values().stream().filter((f) -> {
                return !Modifier.isStatic(f.getModifiers());
            }).filter((f) -> {
                return !Modifier.isTransient(f.getModifiers());
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public static Map<String, Field> excludeOverrideSuperField(Field[] fields, List<Field> superFieldList) {
        Map<String, Field> fieldMap = (Map)Stream.of(fields).collect(Collectors.toMap(Field::getName, Function.identity(), (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", u));
        }, LinkedHashMap::new));
        superFieldList.stream().filter((field) -> {
            return !fieldMap.containsKey(field.getName());
        }).forEach((f) -> {
            Field var10000 = (Field)fieldMap.put(f.getName(), f);
        });
        return fieldMap;
    }

    /** @deprecated */
    @Deprecated
    public static Method getMethod(Class<?> cls, Field field) {
        try {
            return cls.getDeclaredMethod(guessGetterName(field, field.getName()));
        } catch (NoSuchMethodException var3) {
            throw ExceptionUtils.mpe("Error: NoSuchMethod in %s.  Cause:", var3, new Object[]{cls.getName()});
        }
    }

    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null", new Object[0]);
        return clazz.isPrimitive() || PRIMITIVE_WRAPPER_TYPE_MAP.containsKey(clazz);
    }

    static {
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Boolean.class, Boolean.TYPE);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Byte.class, Byte.TYPE);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Character.class, Character.TYPE);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Double.class, Double.TYPE);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Float.class, Float.TYPE);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Integer.class, Integer.TYPE);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Long.class, Long.TYPE);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Short.class, Short.TYPE);
    }
}