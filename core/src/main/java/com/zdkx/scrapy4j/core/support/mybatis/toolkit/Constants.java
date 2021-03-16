package com.zdkx.scrapy4j.core.support.mybatis.toolkit;

/*
copied from mybatis-plus
*/
public interface Constants extends StringPool {
    String MYBATIS_PLUS = "mybatis-plus";
    String MD5 = "MD5";
    String AES = "AES";
    String AES_CBC_CIPHER = "AES/CBC/PKCS5Padding";
    String ENTITY = "et";
    String ENTITY_DOT = "et.";
    String WRAPPER = "ew";
    String WRAPPER_DOT = "ew.";
    String WRAPPER_ENTITY = "ew.entity";
    String WRAPPER_SQLSEGMENT = "ew.sqlSegment";
    String WRAPPER_EMPTYOFNORMAL = "ew.emptyOfNormal";
    String WRAPPER_NONEMPTYOFNORMAL = "ew.nonEmptyOfNormal";
    String WRAPPER_NONEMPTYOFENTITY = "ew.nonEmptyOfEntity";
    String WRAPPER_EMPTYOFWHERE = "ew.emptyOfWhere";
    String WRAPPER_NONEMPTYOFWHERE = "ew.nonEmptyOfWhere";
    String WRAPPER_ENTITY_DOT = "ew.entity.";
    String U_WRAPPER_SQL_SET = "ew.sqlSet";
    String Q_WRAPPER_SQL_SELECT = "ew.sqlSelect";
    String Q_WRAPPER_SQL_COMMENT = "ew.sqlComment";
    String Q_WRAPPER_SQL_FIRST = "ew.sqlFirst";
    String COLUMN_MAP = "cm";
    String COLUMN_MAP_IS_EMPTY = "cm.isEmpty";
    String COLLECTION = "coll";
    String WHERE = "WHERE";
    String MP_OPTLOCK_INTERCEPTOR = "oli";
    String MP_OPTLOCK_VERSION_ORIGINAL = "MP_OPTLOCK_VERSION_ORIGINAL";
    String MP_OPTLOCK_VERSION_COLUMN = "MP_OPTLOCK_VERSION_COLUMN";
    String MP_OPTLOCK_ET_ORIGINAL = "MP_OPTLOCK_ET_ORIGINAL";
    String WRAPPER_PARAM = "MPGENVAL";
    String WRAPPER_PARAM_FORMAT = "#{%s.paramNameValuePairs.%s}";
}