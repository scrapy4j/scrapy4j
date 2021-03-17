package com.victory.scrapy4j.xxljob.support.resolver.argument.definition;

public class PropertySetDefinition {
    private String argName;
    private String propertyName;

    public PropertySetDefinition(String argName, String propertyName) {
        this.argName = argName;
        this.propertyName = propertyName;
    }

    public String getArgName() {
        return argName;
    }

    public void setArgName(String argName) {
        this.argName = argName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
