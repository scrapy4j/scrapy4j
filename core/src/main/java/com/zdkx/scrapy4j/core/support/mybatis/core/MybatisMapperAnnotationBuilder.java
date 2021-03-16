package com.zdkx.scrapy4j.core.support.mybatis.core;

import com.zdkx.scrapy4j.core.support.mybatis.core.metadata.IPage;
import com.zdkx.scrapy4j.core.support.mybatis.core.parser.SqlParserHelper;
import com.zdkx.scrapy4j.core.support.mybatis.toolkit.GlobalConfigUtils;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Options.FlushCachePolicy;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.CacheRefResolver;
import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.builder.annotation.MethodResolver;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class MybatisMapperAnnotationBuilder extends MapperAnnotationBuilder {
    private static final Set<Class<? extends Annotation>> SQL_ANNOTATION_TYPES = new HashSet();
    private static final Set<Class<? extends Annotation>> SQL_PROVIDER_ANNOTATION_TYPES = new HashSet();
    private final MybatisConfiguration configuration;
    private final MapperBuilderAssistant assistant;
    private final Class<?> type;

    public MybatisMapperAnnotationBuilder(MybatisConfiguration configuration, Class<?> type) {
        super(configuration, type);
        String resource = type.getName().replace('.', '/') + ".java (best guess)";
        this.assistant = new MapperBuilderAssistant(configuration, resource);
        this.configuration = configuration;
        this.type = type;
    }

    public void parse() {
        String resource = this.type.toString();
        if (!this.configuration.isResourceLoaded(resource)) {
            this.loadXmlResource();
            this.configuration.addLoadedResource(resource);
            String typeName = this.type.getName();
            this.assistant.setCurrentNamespace(typeName);
            this.parseCache();
            this.parseCacheRef();
            SqlParserHelper.initSqlParserInfoCache(this.type);
            Method[] methods = this.type.getMethods();
            Method[] var4 = methods;
            int var5 = methods.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                Method method = var4[var6];

                try {
                    if (!method.isBridge()) {
                        this.parseStatement(method);
                        SqlParserHelper.initSqlParserInfoCache(typeName, method);
                    }
                } catch (IncompleteElementException var9) {
                    this.configuration.addIncompleteMethod(new MybatisMethodResolver(this, method));
                }
            }

            if (GlobalConfigUtils.isSupperMapperChildren(this.configuration, this.type)) {
                GlobalConfigUtils.getSqlInjector(this.configuration).inspectInject(this.assistant, this.type);
            }
        }

        this.parsePendingMethods();
    }

    private void parsePendingMethods() {
        Collection<MethodResolver> incompleteMethods = this.configuration.getIncompleteMethods();
        synchronized (incompleteMethods) {
            Iterator iter = incompleteMethods.iterator();

            while (iter.hasNext()) {
                try {
                    ((MethodResolver) iter.next()).resolve();
                    iter.remove();
                } catch (IncompleteElementException var6) {
                }
            }

        }
    }

    private void loadXmlResource() {
        if (!this.configuration.isResourceLoaded("namespace:" + this.type.getName())) {
            String xmlResource = this.type.getName().replace('.', '/') + ".xml";
            InputStream inputStream = this.type.getResourceAsStream("/" + xmlResource);
            if (inputStream == null) {
                try {
                    inputStream = Resources.getResourceAsStream(this.type.getClassLoader(), xmlResource);
                } catch (IOException var4) {
                }
            }

            if (inputStream != null) {
                XMLMapperBuilder xmlParser = new XMLMapperBuilder(inputStream, this.assistant.getConfiguration(), xmlResource, this.configuration.getSqlFragments(), this.type.getName());
                xmlParser.parse();
            }
        }

    }

    private void parseCache() {
        CacheNamespace cacheDomain = (CacheNamespace) this.type.getAnnotation(CacheNamespace.class);
        if (cacheDomain != null) {
            Integer size = cacheDomain.size() == 0 ? null : cacheDomain.size();
            Long flushInterval = cacheDomain.flushInterval() == 0L ? null : cacheDomain.flushInterval();
            Properties props = this.convertToProperties(cacheDomain.properties());
            this.assistant.useNewCache(cacheDomain.implementation(), cacheDomain.eviction(), flushInterval, size, cacheDomain.readWrite(), cacheDomain.blocking(), props);
        }

    }

    private Properties convertToProperties(Property[] properties) {
        if (properties.length == 0) {
            return null;
        } else {
            Properties props = new Properties();
            Property[] var3 = properties;
            int var4 = properties.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                Property property = var3[var5];
                props.setProperty(property.name(), PropertyParser.parse(property.value(), this.configuration.getVariables()));
            }

            return props;
        }
    }

    private void parseCacheRef() {
        CacheNamespaceRef cacheDomainRef = (CacheNamespaceRef) this.type.getAnnotation(CacheNamespaceRef.class);
        if (cacheDomainRef != null) {
            Class<?> refType = cacheDomainRef.value();
            String refName = cacheDomainRef.name();
            if (refType == Void.TYPE && refName.isEmpty()) {
                throw new BuilderException("Should be specified either value() or name() attribute in the @CacheNamespaceRef");
            }

            if (refType != Void.TYPE && !refName.isEmpty()) {
                throw new BuilderException("Cannot use both value() and name() attribute in the @CacheNamespaceRef");
            }

            String namespace = refType != Void.TYPE ? refType.getName() : refName;

            try {
                this.assistant.useCacheRef(namespace);
            } catch (IncompleteElementException var6) {
                this.configuration.addIncompleteCacheRef(new CacheRefResolver(this.assistant, namespace));
            }
        }

    }

    private String parseResultMap(Method method) {
        Class<?> returnType = this.getReturnType(method);
        Arg[] args = (Arg[]) method.getAnnotationsByType(Arg.class);
        Result[] results = (Result[]) method.getAnnotationsByType(Result.class);
        TypeDiscriminator typeDiscriminator = (TypeDiscriminator) method.getAnnotation(TypeDiscriminator.class);
        String resultMapId = this.generateResultMapName(method);
        this.applyResultMap(resultMapId, returnType, args, results, typeDiscriminator);
        return resultMapId;
    }

    private String generateResultMapName(Method method) {
        Results results = (Results) method.getAnnotation(Results.class);
        if (results != null && !results.id().isEmpty()) {
            return this.type.getName() + "." + results.id();
        } else {
            StringBuilder suffix = new StringBuilder();
            Class[] var4 = method.getParameterTypes();
            int var5 = var4.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                Class<?> c = var4[var6];
                suffix.append("-");
                suffix.append(c.getSimpleName());
            }

            if (suffix.length() < 1) {
                suffix.append("-void");
            }

            return this.type.getName() + "." + method.getName() + suffix;
        }
    }

    private void applyResultMap(String resultMapId, Class<?> returnType, Arg[] args, Result[] results, TypeDiscriminator discriminator) {
        List<ResultMapping> resultMappings = new ArrayList();
        this.applyConstructorArgs(args, returnType, resultMappings);
        this.applyResults(results, returnType, resultMappings);
        Discriminator disc = this.applyDiscriminator(resultMapId, returnType, discriminator);
        this.assistant.addResultMap(resultMapId, returnType, (String) null, disc, resultMappings, (Boolean) null);
        this.createDiscriminatorResultMaps(resultMapId, returnType, discriminator);
    }

    private void createDiscriminatorResultMaps(String resultMapId, Class<?> resultType, TypeDiscriminator discriminator) {
        if (discriminator != null) {
            Case[] var4 = discriminator.cases();
            int var5 = var4.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                Case c = var4[var6];
                String caseResultMapId = resultMapId + "-" + c.value();
                List<ResultMapping> resultMappings = new ArrayList();
                this.applyConstructorArgs(c.constructArgs(), resultType, resultMappings);
                this.applyResults(c.results(), resultType, resultMappings);
                this.assistant.addResultMap(caseResultMapId, c.type(), resultMapId, (Discriminator) null, resultMappings, (Boolean) null);
            }
        }

    }

    private Discriminator applyDiscriminator(String resultMapId, Class<?> resultType, TypeDiscriminator discriminator) {
        if (discriminator == null) {
            return null;
        } else {
            String column = discriminator.column();
            Class<?> javaType = discriminator.javaType() == Void.TYPE ? String.class : discriminator.javaType();
            JdbcType jdbcType = discriminator.jdbcType() == JdbcType.UNDEFINED ? null : discriminator.jdbcType();
            Class<? extends TypeHandler<?>> typeHandler = discriminator.typeHandler() == UnknownTypeHandler.class ? null : (Class<? extends TypeHandler<?>>) discriminator.typeHandler();
            Case[] cases = discriminator.cases();
            Map<String, String> discriminatorMap = new HashMap();
            Case[] var10 = cases;
            int var11 = cases.length;

            for (int var12 = 0; var12 < var11; ++var12) {
                Case c = var10[var12];
                String value = c.value();
                String caseResultMapId = resultMapId + "-" + value;
                discriminatorMap.put(value, caseResultMapId);
            }

            return this.assistant.buildDiscriminator(resultType, column, javaType, jdbcType, typeHandler, discriminatorMap);
        }
    }

    void parseStatement(Method method) {
        Class<?> parameterTypeClass = this.getParameterType(method);
        LanguageDriver languageDriver = this.getLanguageDriver(method);
        SqlSource sqlSource = this.getSqlSourceFromAnnotations(method, parameterTypeClass, languageDriver);
        if (sqlSource != null) {
            Options options = (Options) method.getAnnotation(Options.class);
            String mappedStatementId = this.type.getName() + "." + method.getName();
            Integer fetchSize = null;
            Integer timeout = null;
            StatementType statementType = StatementType.PREPARED;
            ResultSetType resultSetType = this.configuration.getDefaultResultSetType();
            SqlCommandType sqlCommandType = this.getSqlCommandType(method);
            boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
            boolean flushCache = !isSelect;
            boolean useCache = isSelect;
            String keyProperty = null;
            String keyColumn = null;
            Object keyGenerator;
            if (!SqlCommandType.INSERT.equals(sqlCommandType) && !SqlCommandType.UPDATE.equals(sqlCommandType)) {
                keyGenerator = NoKeyGenerator.INSTANCE;
            } else {
                SelectKey selectKey = (SelectKey) method.getAnnotation(SelectKey.class);
                if (selectKey != null) {
                    keyGenerator = this.handleSelectKeyAnnotation(selectKey, mappedStatementId, this.getParameterType(method), languageDriver);
                    keyProperty = selectKey.keyProperty();
                } else if (options == null) {
                    keyGenerator = this.configuration.isUseGeneratedKeys() ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
                } else {
                    keyGenerator = options.useGeneratedKeys() ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
                    keyProperty = options.keyProperty();
                    keyColumn = options.keyColumn();
                }
            }

            if (options != null) {
                if (FlushCachePolicy.TRUE.equals(options.flushCache())) {
                    flushCache = true;
                } else if (FlushCachePolicy.FALSE.equals(options.flushCache())) {
                    flushCache = false;
                }

                useCache = options.useCache();
                fetchSize = options.fetchSize() <= -1 && options.fetchSize() != -2147483648 ? null : options.fetchSize();
                timeout = options.timeout() > -1 ? options.timeout() : null;
                statementType = options.statementType();
                if (options.resultSetType() != ResultSetType.DEFAULT) {
                    resultSetType = options.resultSetType();
                }
            }

            String resultMapId = null;
            ResultMap resultMapAnnotation = (ResultMap) method.getAnnotation(ResultMap.class);
            if (resultMapAnnotation != null) {
                resultMapId = String.join(",", resultMapAnnotation.value());
            } else if (isSelect) {
                resultMapId = this.parseResultMap(method);
            }

            this.assistant.addMappedStatement(mappedStatementId, sqlSource, statementType, sqlCommandType, fetchSize, timeout, (String) null, parameterTypeClass, resultMapId, this.getReturnType(method), resultSetType, flushCache, useCache, false, (KeyGenerator) keyGenerator, keyProperty, keyColumn, (String) null, languageDriver, options != null ? this.nullOrEmpty(options.resultSets()) : null);
        }

    }

    private LanguageDriver getLanguageDriver(Method method) {
        Lang lang = (Lang) method.getAnnotation(Lang.class);
        Class<? extends LanguageDriver> langClass = null;
        if (lang != null) {
            langClass = lang.value();
        }

        return this.configuration.getLanguageDriver(langClass);
    }

    private Class<?> getParameterType(Method method) {
        Class<?> parameterType = null;
        Class<?>[] parameterTypes = method.getParameterTypes();
        Class[] var4 = parameterTypes;
        int var5 = parameterTypes.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            Class<?> currentParameterType = var4[var6];
            if (!RowBounds.class.isAssignableFrom(currentParameterType) && !ResultHandler.class.isAssignableFrom(currentParameterType)) {
                if (parameterType == null) {
                    parameterType = currentParameterType;
                } else {
                    parameterType = ParamMap.class;
                }
            }
        }

        return parameterType;
    }

    private Class<?> getReturnType(Method method) {
        Class<?> returnType = method.getReturnType();
        Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, this.type);
        if (resolvedReturnType instanceof Class) {
            returnType = (Class) resolvedReturnType;
            if (returnType.isArray()) {
                returnType = returnType.getComponentType();
            }

            if (Void.TYPE.equals(returnType)) {
                ResultType rt = (ResultType) method.getAnnotation(ResultType.class);
                if (rt != null) {
                    returnType = rt.value();
                }
            }
        } else if (resolvedReturnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) resolvedReturnType;
            Class<?> rawType = (Class) parameterizedType.getRawType();
            Type[] actualTypeArguments;
            Type returnTypeParameter;
            if (!Collection.class.isAssignableFrom(rawType) && !Cursor.class.isAssignableFrom(rawType)) {
                if (method.isAnnotationPresent(MapKey.class) && Map.class.isAssignableFrom(rawType)) {
                    actualTypeArguments = parameterizedType.getActualTypeArguments();
                    if (actualTypeArguments != null && actualTypeArguments.length == 2) {
                        returnTypeParameter = actualTypeArguments[1];
                        if (returnTypeParameter instanceof Class) {
                            returnType = (Class) returnTypeParameter;
                        } else if (returnTypeParameter instanceof ParameterizedType) {
                            returnType = (Class) ((ParameterizedType) returnTypeParameter).getRawType();
                        }
                    }
                } else if (Optional.class.equals(rawType)) {
                    actualTypeArguments = parameterizedType.getActualTypeArguments();
                    returnTypeParameter = actualTypeArguments[0];
                    if (returnTypeParameter instanceof Class) {
                        returnType = (Class) returnTypeParameter;
                    }
                } else if (IPage.class.isAssignableFrom(rawType)) {
                    actualTypeArguments = parameterizedType.getActualTypeArguments();
                    returnTypeParameter = actualTypeArguments[0];
                    if (returnTypeParameter instanceof Class) {
                        returnType = (Class) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        returnType = (Class) ((ParameterizedType) returnTypeParameter).getRawType();
                    }
                }
            } else {
                actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    returnTypeParameter = actualTypeArguments[0];
                    if (returnTypeParameter instanceof Class) {
                        returnType = (Class) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        returnType = (Class) ((ParameterizedType) returnTypeParameter).getRawType();
                    } else if (returnTypeParameter instanceof GenericArrayType) {
                        Class<?> componentType = (Class) ((GenericArrayType) returnTypeParameter).getGenericComponentType();
                        returnType = Array.newInstance(componentType, 0).getClass();
                    }
                }
            }
        }

        return returnType;
    }

    private SqlSource getSqlSourceFromAnnotations(Method method, Class<?> parameterType, LanguageDriver languageDriver) {
        try {
            Class<? extends Annotation> sqlAnnotationType = this.getSqlAnnotationType(method);
            Class<? extends Annotation> sqlProviderAnnotationType = this.getSqlProviderAnnotationType(method);
            Annotation sqlProviderAnnotation;
            if (sqlAnnotationType != null) {
                if (sqlProviderAnnotationType != null) {
                    throw new BindingException("You cannot supply both a static SQL and SqlProvider to method named " + method.getName());
                } else {
                    sqlProviderAnnotation = method.getAnnotation(sqlAnnotationType);
                    String[] strings = (String[]) ((String[]) sqlProviderAnnotation.getClass().getMethod("value").invoke(sqlProviderAnnotation));
                    return this.buildSqlSourceFromStrings(strings, parameterType, languageDriver);
                }
            } else if (sqlProviderAnnotationType != null) {
                sqlProviderAnnotation = method.getAnnotation(sqlProviderAnnotationType);
                return new ProviderSqlSource(this.assistant.getConfiguration(), sqlProviderAnnotation, this.type, method);
            } else {
                return null;
            }
        } catch (Exception var8) {
            throw new BuilderException("Could not find value method on SQL annotation.  Cause: " + var8, var8);
        }
    }

    private SqlSource buildSqlSourceFromStrings(String[] strings, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        StringBuilder sql = new StringBuilder();
        String[] var5 = strings;
        int var6 = strings.length;

        for (int var7 = 0; var7 < var6; ++var7) {
            String fragment = var5[var7];
            sql.append(fragment);
            sql.append(" ");
        }

        return languageDriver.createSqlSource(this.configuration, sql.toString().trim(), parameterTypeClass);
    }

    private SqlCommandType getSqlCommandType(Method method) {
        Class<? extends Annotation> type = this.getSqlAnnotationType(method);
        if (type == null) {
            type = this.getSqlProviderAnnotationType(method);
            if (type == null) {
                return SqlCommandType.UNKNOWN;
            }

            if (type == SelectProvider.class) {
                type = Select.class;
            } else if (type == InsertProvider.class) {
                type = Insert.class;
            } else if (type == UpdateProvider.class) {
                type = Update.class;
            } else if (type == DeleteProvider.class) {
                type = Delete.class;
            }
        }

        return SqlCommandType.valueOf(type.getSimpleName().toUpperCase(Locale.ENGLISH));
    }

    private Class<? extends Annotation> getSqlAnnotationType(Method method) {
        return this.chooseAnnotationType(method, SQL_ANNOTATION_TYPES);
    }

    private Class<? extends Annotation> getSqlProviderAnnotationType(Method method) {
        return this.chooseAnnotationType(method, SQL_PROVIDER_ANNOTATION_TYPES);
    }

    private Class<? extends Annotation> chooseAnnotationType(Method method, Set<Class<? extends Annotation>> types) {
        Iterator var3 = types.iterator();

        Class type;
        Annotation annotation;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            type = (Class) var3.next();
            annotation = method.getAnnotation(type);
        } while (annotation == null);

        return type;
    }

    private void applyResults(Result[] results, Class<?> resultType, List<ResultMapping> resultMappings) {
        Result[] var4 = results;
        int var5 = results.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            Result result = var4[var6];
            List<ResultFlag> flags = new ArrayList();
            if (result.id()) {
                flags.add(ResultFlag.ID);
            }

            Class<? extends TypeHandler<?>> typeHandler = result.typeHandler() == UnknownTypeHandler.class ? null : (Class<? extends TypeHandler<?>>) result.typeHandler();
            ResultMapping resultMapping = this.assistant.buildResultMapping(resultType, this.nullOrEmpty(result.property()), this.nullOrEmpty(result.column()), result.javaType() == Void.TYPE ? null : result.javaType(), result.jdbcType() == JdbcType.UNDEFINED ? null : result.jdbcType(), this.hasNestedSelect(result) ? this.nestedSelectId(result) : null, (String) null, (String) null, (String) null, typeHandler, flags, (String) null, (String) null, this.isLazy(result));
            resultMappings.add(resultMapping);
        }

    }

    private String nestedSelectId(Result result) {
        String nestedSelect = result.one().select();
        if (nestedSelect.length() < 1) {
            nestedSelect = result.many().select();
        }

        if (!nestedSelect.contains(".")) {
            nestedSelect = this.type.getName() + "." + nestedSelect;
        }

        return nestedSelect;
    }

    private boolean isLazy(Result result) {
        boolean isLazy = this.configuration.isLazyLoadingEnabled();
        if (result.one().select().length() > 0 && FetchType.DEFAULT != result.one().fetchType()) {
            isLazy = result.one().fetchType() == FetchType.LAZY;
        } else if (result.many().select().length() > 0 && FetchType.DEFAULT != result.many().fetchType()) {
            isLazy = result.many().fetchType() == FetchType.LAZY;
        }

        return isLazy;
    }

    private boolean hasNestedSelect(Result result) {
        if (result.one().select().length() > 0 && result.many().select().length() > 0) {
            throw new BuilderException("Cannot use both @One and @Many annotations in the same @Result");
        } else {
            return result.one().select().length() > 0 || result.many().select().length() > 0;
        }
    }

    private void applyConstructorArgs(Arg[] args, Class<?> resultType, List<ResultMapping> resultMappings) {
        Arg[] var4 = args;
        int var5 = args.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            Arg arg = var4[var6];
            List<ResultFlag> flags = new ArrayList();
            flags.add(ResultFlag.CONSTRUCTOR);
            if (arg.id()) {
                flags.add(ResultFlag.ID);
            }

            Class<? extends TypeHandler<?>> typeHandler = arg.typeHandler() == UnknownTypeHandler.class ? null : (Class<? extends TypeHandler<?>>) arg.typeHandler();
            ResultMapping resultMapping = this.assistant.buildResultMapping(resultType, this.nullOrEmpty(arg.name()), this.nullOrEmpty(arg.column()), arg.javaType() == Void.TYPE ? null : arg.javaType(), arg.jdbcType() == JdbcType.UNDEFINED ? null : arg.jdbcType(), this.nullOrEmpty(arg.select()), this.nullOrEmpty(arg.resultMap()), (String) null, this.nullOrEmpty(arg.columnPrefix()), typeHandler, flags, (String) null, (String) null, false);
            resultMappings.add(resultMapping);
        }

    }

    private String nullOrEmpty(String value) {
        return value != null && value.trim().length() != 0 ? value : null;
    }

    private KeyGenerator handleSelectKeyAnnotation(SelectKey selectKeyAnnotation, String baseStatementId, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        String id = baseStatementId + "!selectKey";
        Class<?> resultTypeClass = selectKeyAnnotation.resultType();
        StatementType statementType = selectKeyAnnotation.statementType();
        String keyProperty = selectKeyAnnotation.keyProperty();
        String keyColumn = selectKeyAnnotation.keyColumn();
        boolean executeBefore = selectKeyAnnotation.before();
        boolean useCache = false;
        KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;
        Integer fetchSize = null;
        Integer timeout = null;
        boolean flushCache = false;
        String parameterMap = null;
        String resultMap = null;
        ResultSetType resultSetTypeEnum = null;
        SqlSource sqlSource = this.buildSqlSourceFromStrings(selectKeyAnnotation.statement(), parameterTypeClass, languageDriver);
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;
        this.assistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType, (Integer) fetchSize, (Integer) timeout, (String) parameterMap, parameterTypeClass, (String) resultMap, resultTypeClass, (ResultSetType) resultSetTypeEnum, flushCache, useCache, false, keyGenerator, keyProperty, keyColumn, (String) null, languageDriver, (String) null);
        id = this.assistant.applyCurrentNamespace(id, false);
        MappedStatement keyStatement = this.configuration.getMappedStatement(id, false);
        SelectKeyGenerator answer = new SelectKeyGenerator(keyStatement, executeBefore);
        this.configuration.addKeyGenerator(id, answer);
        return answer;
    }

    static {
        SQL_ANNOTATION_TYPES.add(Select.class);
        SQL_ANNOTATION_TYPES.add(Insert.class);
        SQL_ANNOTATION_TYPES.add(Update.class);
        SQL_ANNOTATION_TYPES.add(Delete.class);
        SQL_PROVIDER_ANNOTATION_TYPES.add(SelectProvider.class);
        SQL_PROVIDER_ANNOTATION_TYPES.add(InsertProvider.class);
        SQL_PROVIDER_ANNOTATION_TYPES.add(UpdateProvider.class);
        SQL_PROVIDER_ANNOTATION_TYPES.add(DeleteProvider.class);
    }
}
