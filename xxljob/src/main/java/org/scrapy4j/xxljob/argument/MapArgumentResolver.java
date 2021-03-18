package org.scrapy4j.xxljob.argument;

import org.scrapy4j.core.utils.Utils;
import org.scrapy4j.xxljob.Configuration;
import org.scrapy4j.xxljob.Registry;
import org.scrapy4j.xxljob.argument.definition.MethodInvokeDefinition;
import org.scrapy4j.xxljob.argument.definition.PropertySetDefinition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 指定别名、有复杂子元素类型的argument解析器，处理的结果为 class 或 map对象
 */
public class MapArgumentResolver extends AbstractArgumentResolver<Object, Object, MapArgumentResolver> {

    protected List<MethodInvokeDefinition> methodInvokeList = new LinkedList<>();
    protected List<PropertySetDefinition> propertySetList = new LinkedList<>();

    protected Class<?> clazz;

    public MapArgumentResolver(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object resolve(Registry registry, Object object) {
        Object res = null;
        Map<String, Object> argsMap = (Map<String, Object>) object;
        if (argsMap != null && !argsMap.isEmpty()) {
            Map<String, Object> resolvedArgs = new HashMap<>();
            for (String key : argsMap.keySet()) {
                if (this.delegates.get(key) == null) {
                    //map key未指定resolver 则取string值 或 通过spEL取spring bean,而{xx}变量则是交由后面request运行时处理
                    if (Utils.isSPEL(argsMap.get(key).toString())) {
                        resolvedArgs.put(key, registry.getSharedObject(argsMap.get(key).toString()));
                    } else {
                        resolvedArgs.put(key, argsMap.get(key));
                    }
                    continue;
                }
                //map key指定了resolver的，通过对应的resolver去取值
                resolvedArgs.put(key, this.delegates.get(key).resolve(registry, argsMap.get(key)));
            }

            if (Map.class.isAssignableFrom(this.clazz)
                    && this.propertySetList.size() == 0
                    && this.methodInvokeList.size() == 0) {
                //pure map class
                res = new HashMap<>(resolvedArgs);
            } else {
                res = resolveToClass(resolvedArgs);
            }
        }
        return res;
    }

    private Object[] getParamsByNames(String[] params, Class<?>[] paramTypes, Map<String, Object> args) {
        List<Object> paramList = new LinkedList<>();
        Object[] objects = new Object[params == null ? 0 : params.length];
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                Object objParam = args.get(params[i]);
                if (objParam != null) {
                    if (paramTypes[i].isEnum()) {
                        for (Object constant : paramTypes[i].getEnumConstants()) {
                            if (constant.toString().equalsIgnoreCase(objParam.toString())) {
                                objParam = constant;
                                break;
                            }
                        }
                    }
                }
                paramList.add(objParam);
            }
        }
        return paramList.toArray(objects);
    }

    private Object invokeMethod(MethodInvokeDefinition m, Object obj, Map<String, Object> args) {
        Method method = BeanUtils.findMethod(clazz, m.getMethodName(), m.getMethodParamTypes());
        Object res = null;
        try {
            res = method.invoke(obj, getParamsByNames(m.getParams(), m.getMethodParamTypes(), args));
        } catch (Exception ex) {
            throw new RuntimeException(String.format("method invoke exception: class name %s method name %s", clazz.getSimpleName(), m.getMethodName()), ex);
        }
        return res;
    }

    public MapArgumentResolver methodInvoke(String methodName, String[] params, Class<?>[] methodParamTypes) {
        this.methodInvokeList.add(new MethodInvokeDefinition(methodName, params, methodParamTypes));
        return this;
    }

    public MapArgumentResolver methodInvokeList(List<MethodInvokeDefinition> list){
        this.methodInvokeList.addAll(list);
        return this;
    }

    public MapArgumentResolver propertySet(String argName, String propertyName) {
        this.propertySetList.add(new PropertySetDefinition(argName, propertyName));
        return this;
    }

    public MapArgumentResolver propertySetList(List<PropertySetDefinition> list){
        this.propertySetList.addAll(list);
        return this;
    }

    public Map<String, ArgumentResolver> getDelegates() {
        return this.delegates;
    }
    public List<MethodInvokeDefinition> getMethodInvokeList(){
        return this.methodInvokeList;
    }
    public List<PropertySetDefinition> getPropertySetList(){
        return this.propertySetList;
    }

    public MapArgumentResolver setDelegates(Map<String, ArgumentResolver> delegates) {
        this.delegates = delegates;
        return this;
    }

    private Object resolveToClass(Map<String, Object> resolvedArgs) {
        Object res = null;
        //处理构造函数
        Optional<MethodInvokeDefinition> mmd = this.methodInvokeList.stream().filter(p -> p.getMethodName().equals(Configuration.CONSTRUCTOR)).findFirst();
        try {
            if (!mmd.isPresent()) {
                res = clazz.newInstance();
            } else {
                res = BeanUtils.instantiateClass(clazz.getConstructor(mmd.get().getMethodParamTypes()), getParamsByNames(mmd.get().getParams(), mmd.get().getMethodParamTypes(), resolvedArgs));
            }
        } catch (Exception ex) {
            throw new RuntimeException(String.format("constructor invoke exception: class name %s", clazz.getSimpleName()), ex);
        }

        //set未被构造函数及定义方法所使用的属性值
        List<String> boundProperties = new ArrayList<>();
        for (MethodInvokeDefinition methodInvokeDefinition : this.methodInvokeList) {
            String[] params = methodInvokeDefinition.getParams();
            if (params != null && params.length > 0) {
                boundProperties.addAll(Arrays.asList(params));
            }
        }
        BeanWrapper beanWrapper = new BeanWrapperImpl(res);
        this.propertySetList.forEach(p -> {
            if (!boundProperties.contains(p.getArgName())) {
                beanWrapper.setPropertyValue(p.getPropertyName(), resolvedArgs.get(p.getArgName()));
                boundProperties.add(p.getArgName());
            }
        });
        for (String key : resolvedArgs.keySet()) {
            if (!boundProperties.contains(key)) {
                try {
                    //未包含在propertySet definition声明范围内的属性也进行赋值，异常不处理
                    beanWrapper.setPropertyValue(key, resolvedArgs.get(key));
                } catch (Exception ex) {

                }
            }
        }

        //处理method definition
        List<MethodInvokeDefinition> methodsList = this.methodInvokeList.stream().filter(p -> !p.getMethodName().equals(Configuration.CONSTRUCTOR)).collect(Collectors.toList());
        for (int i = 0; i < methodsList.size(); i++) {
            //如果最后触发的一个方法有返回值，例如build方法，则返回该对象
            if (i == methodsList.size() - 1) {
                if (!BeanUtils.findMethod(clazz, methodsList.get(i).getMethodName(), methodsList.get(i).getMethodParamTypes())
                        .getReturnType().getName().equalsIgnoreCase("void")) {
                    return invokeMethod(methodsList.get(i), res, resolvedArgs);
                } else {
                    invokeMethod(methodsList.get(i), res, resolvedArgs);
                }
            } else {
                invokeMethod(methodsList.get(i), res, resolvedArgs);
            }
        }
        return res;
    }

    public MapArgumentResolver clone(){
        return new MapArgumentResolver(this.clazz)
                .propertySetList(this.getPropertySetList())
                .methodInvokeList(this.methodInvokeList)
                .setDelegates(this.delegates);
    }
}
