package org.scrapy4j.xxljob.definition;

import java.util.LinkedHashMap;
import java.util.Map;

public class NameArgsDefinition {
    private String name;

    private Map<String, Object> args = new LinkedHashMap<>();

    public NameArgsDefinition(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }
}
