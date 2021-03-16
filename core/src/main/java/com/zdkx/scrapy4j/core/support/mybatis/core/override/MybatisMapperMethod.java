package com.zdkx.scrapy4j.core.support.mybatis.core.override;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.zdkx.scrapy4j.core.support.mybatis.core.metadata.IPage;
import com.zdkx.scrapy4j.core.support.mybatis.core.metadata.PageList;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperMethod.MethodSignature;
import org.apache.ibatis.binding.MapperMethod.SqlCommand;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

public class MybatisMapperMethod {
    private final SqlCommand command;
    private final MethodSignature method;

    public MybatisMapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
        this.command = new SqlCommand(config, mapperInterface, method);
        this.method = new MethodSignature(config, mapperInterface, method);
    }

    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result = null;
        Object param;
        switch(this.command.getType()) {
            case INSERT:
                param = this.method.convertArgsToSqlCommandParam(args);
                result = this.rowCountResult(sqlSession.insert(this.command.getName(), param));
                break;
            case UPDATE:
                param = this.method.convertArgsToSqlCommandParam(args);
                result = this.rowCountResult(sqlSession.update(this.command.getName(), param));
                break;
            case DELETE:
                param = this.method.convertArgsToSqlCommandParam(args);
                result = this.rowCountResult(sqlSession.delete(this.command.getName(), param));
                break;
            case SELECT:
                if (this.method.returnsVoid() && this.method.hasResultHandler()) {
                    this.executeWithResultHandler(sqlSession, args);
                    result = null;
                } else if (this.method.returnsMany()) {
                    result = this.executeForMany(sqlSession, args);
                } else if (this.method.returnsMap()) {
                    result = this.executeForMap(sqlSession, args);
                } else if (this.method.returnsCursor()) {
                    result = this.executeForCursor(sqlSession, args);
                } else {
                    param = this.method.convertArgsToSqlCommandParam(args);
                    if (IPage.class.isAssignableFrom(this.method.getReturnType())) {
                        assert args != null;

                        IPage<?> page = null;
                        Object[] var6 = args;
                        int var7 = args.length;

                        for(int var8 = 0; var8 < var7; ++var8) {
                            Object arg = var6[var8];
                            if (arg instanceof IPage) {
                                page = (IPage)arg;
                                break;
                            }
                        }

                        assert page != null;

                        result = this.executeForIPage(sqlSession, args);
                        if (result instanceof PageList) {
                            PageList pageList = (PageList)result;
                            page.setRecords(pageList.getRecords());
                            page.setTotal(pageList.getTotal());
                            result = page;
                        } else {
                            List list = (List)result;
                            result = page.setRecords(list);
                        }
                    } else {
                        result = sqlSession.selectOne(this.command.getName(), param);
                        if (this.method.returnsOptional() && (result == null || !this.method.getReturnType().equals(result.getClass()))) {
                            result = Optional.ofNullable(result);
                        }
                    }
                }
                break;
            case FLUSH:
                result = sqlSession.flushStatements();
                break;
            default:
                throw new BindingException("Unknown execution method for: " + this.command.getName());
        }

        if (result == null && this.method.getReturnType().isPrimitive() && !this.method.returnsVoid()) {
            throw new BindingException("Mapper method '" + this.command.getName() + " attempted to return null from a method with a primitive return type (" + this.method.getReturnType() + ").");
        } else {
            return result;
        }
    }

    private <E> List<E> executeForIPage(SqlSession sqlSession, Object[] args) {
        Object param = this.method.convertArgsToSqlCommandParam(args);
        return sqlSession.selectList(this.command.getName(), param);
    }

    private Object rowCountResult(int rowCount) {
        Object result;
        if (this.method.returnsVoid()) {
            result = null;
        } else if (!Integer.class.equals(this.method.getReturnType()) && !Integer.TYPE.equals(this.method.getReturnType())) {
            if (!Long.class.equals(this.method.getReturnType()) && !Long.TYPE.equals(this.method.getReturnType())) {
                if (!Boolean.class.equals(this.method.getReturnType()) && !Boolean.TYPE.equals(this.method.getReturnType())) {
                    throw new BindingException("Mapper method '" + this.command.getName() + "' has an unsupported return type: " + this.method.getReturnType());
                }

                result = rowCount > 0;
            } else {
                result = (long)rowCount;
            }
        } else {
            result = rowCount;
        }

        return result;
    }

    private void executeWithResultHandler(SqlSession sqlSession, Object[] args) {
        MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(this.command.getName());
        if (!StatementType.CALLABLE.equals(ms.getStatementType()) && Void.TYPE.equals(((ResultMap)ms.getResultMaps().get(0)).getType())) {
            throw new BindingException("method " + this.command.getName() + " needs either a @ResultMap annotation, a @ResultType annotation, or a resultType attribute in XML so a ResultHandler can be used as a parameter.");
        } else {
            Object param = this.method.convertArgsToSqlCommandParam(args);
            if (this.method.hasRowBounds()) {
                RowBounds rowBounds = this.method.extractRowBounds(args);
                sqlSession.select(this.command.getName(), param, rowBounds, this.method.extractResultHandler(args));
            } else {
                sqlSession.select(this.command.getName(), param, this.method.extractResultHandler(args));
            }

        }
    }

    private <E> Object executeForMany(SqlSession sqlSession, Object[] args) {
        Object param = this.method.convertArgsToSqlCommandParam(args);
        List result;
        if (this.method.hasRowBounds()) {
            RowBounds rowBounds = this.method.extractRowBounds(args);
            result = sqlSession.selectList(this.command.getName(), param, rowBounds);
        } else {
            result = sqlSession.selectList(this.command.getName(), param);
        }

        if (!this.method.getReturnType().isAssignableFrom(result.getClass())) {
            return this.method.getReturnType().isArray() ? this.convertToArray(result) : this.convertToDeclaredCollection(sqlSession.getConfiguration(), result);
        } else {
            return result;
        }
    }

    private <T> Cursor<T> executeForCursor(SqlSession sqlSession, Object[] args) {
        Object param = this.method.convertArgsToSqlCommandParam(args);
        Cursor result;
        if (this.method.hasRowBounds()) {
            RowBounds rowBounds = this.method.extractRowBounds(args);
            result = sqlSession.selectCursor(this.command.getName(), param, rowBounds);
        } else {
            result = sqlSession.selectCursor(this.command.getName(), param);
        }

        return result;
    }

    private <E> Object convertToDeclaredCollection(Configuration config, List<E> list) {
        Object collection = config.getObjectFactory().create(this.method.getReturnType());
        MetaObject metaObject = config.newMetaObject(collection);
        metaObject.addAll(list);
        return collection;
    }

    private <E> Object convertToArray(List<E> list) {
        Class<?> arrayComponentType = this.method.getReturnType().getComponentType();
        Object array = Array.newInstance(arrayComponentType, list.size());
        if (!arrayComponentType.isPrimitive()) {
            return list.toArray((Object[])((Object[])array));
        } else {
            for(int i = 0; i < list.size(); ++i) {
                Array.set(array, i, list.get(i));
            }

            return array;
        }
    }

    private <K, V> Map<K, V> executeForMap(SqlSession sqlSession, Object[] args) {
        Object param = this.method.convertArgsToSqlCommandParam(args);
        Map result;
        if (this.method.hasRowBounds()) {
            RowBounds rowBounds = this.method.extractRowBounds(args);
            result = sqlSession.selectMap(this.command.getName(), param, this.method.getMapKey(), rowBounds);
        } else {
            result = sqlSession.selectMap(this.command.getName(), param, this.method.getMapKey());
        }

        return result;
    }
}
