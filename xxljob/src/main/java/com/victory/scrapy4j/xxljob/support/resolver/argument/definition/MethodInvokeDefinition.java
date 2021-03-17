package com.victory.scrapy4j.xxljob.support.resolver.argument.definition;

public class MethodInvokeDefinition {
    private String[] params;
    private String methodName;
    private Class<?>[] methodParamTypes;

    public MethodInvokeDefinition(String methodName, String[] params, Class<?>[] methodParamTypes) {
        this.params = params;
        this.methodName = methodName;
        this.methodParamTypes = methodParamTypes;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setMethodParamTypes(Class<?>[] methodParamTypes) {
        this.methodParamTypes = methodParamTypes;
    }

    public Class<?>[] getMethodParamTypes() {
        return methodParamTypes;
    }
}
