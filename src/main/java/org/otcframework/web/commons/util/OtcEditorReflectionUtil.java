/**
 * Copyright (c) otcframework.org
 *
 * @author  Franklin J Abel
 * @version 1.0
 * @since   2020-06-08
 *
 * This file is part of the OTC framework.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */package org.otcframework.web.commons.util;

import org.otcframework.common.util.CommonUtils;
import org.otcframework.common.util.OtcReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class OtcEditorReflectionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtcEditorReflectionUtil.class);

    private static final Class[] ZERO_LENGTH_CLASS_ARRAY = new Class[0];

    public static String findGetterName(Field field, String tokenPath) throws NoSuchMethodException {
        String fieldName = field.getName();
        String getter;
        if (Boolean.class.isAssignableFrom(field.getType())) {
            getter = "is" + CommonUtils.initCap(fieldName);
        } else {
            getter = "get" + CommonUtils.initCap(fieldName);
        }
        Method method = findMethod(OtcReflectionUtil.GETTER_SETTER.GETTER, getter, field, tokenPath);
        String methodName = method.getName();
        return methodName;
    }

    public static String findSetterName(Field field, String tokenPath) throws NoSuchMethodException {
        String fieldName = field.getName();
        String setter = "set" + CommonUtils.initCap(fieldName);
        Method method = findMethod(OtcReflectionUtil.GETTER_SETTER.SETTER, setter, field, tokenPath);
        String methodName = method.getName();
        return methodName;
    }

    private static Method findMethod(OtcReflectionUtil.GETTER_SETTER enumGetterSetter, String methodName, Field field,
            String tokenPath) throws NoSuchMethodException {
        Method method = null;
        Class<?> declaringClz = field.getDeclaringClass();
        Class<?>[] paramTypes = null;
        try {
            if (OtcReflectionUtil.GETTER_SETTER.GETTER == enumGetterSetter) {
                paramTypes = ZERO_LENGTH_CLASS_ARRAY;
            } else {
                paramTypes = new Class[] { field.getType() };
            }
            method = declaringClz.getMethod(methodName, paramTypes);
            return method;
        } catch (NoSuchMethodException | SecurityException e) {
            String msg = createMethodNotFoundMessage(declaringClz, methodName, paramTypes, tokenPath);
            LOGGER.warn(msg, e.getMessage());
            throw e;
        }
    }

    public static String findNonJavaBeanSetterName(Field field) {
        String fieldName = field.getName();
        String setter = "set" + CommonUtils.initCap(fieldName);
        Method method = findNonJavaBeanMethod(OtcReflectionUtil.GETTER_SETTER.SETTER, field, setter);
        String methodName = null;
        if (method != null) {
            methodName = method.getName();
        }
        return methodName;
    }

    public static String findNonJavaBeanGetterName(Field field) {
        String fieldName = field.getName();
        String getter;
        if (Boolean.class.isAssignableFrom(field.getType())) {
            getter = "is" + CommonUtils.initCap(fieldName);
        } else {
            getter = "get" + CommonUtils.initCap(fieldName);
        }
        Method method = findNonJavaBeanMethod(OtcReflectionUtil.GETTER_SETTER.GETTER, field, getter);
        String methodName = null;
        if (method != null) {
            methodName = method.getName();
        }
        return methodName;
    }

    private static Method findNonJavaBeanMethod(OtcReflectionUtil.GETTER_SETTER enumGetterSetter, Field field,
                                                String setterGetterName) {
        Class<?> declaringClz = field.getDeclaringClass();
        Class<?> fieldType = field.getType();
        Class<?>[] paramTypes = null;
        if (OtcReflectionUtil.GETTER_SETTER.GETTER == enumGetterSetter) {
            paramTypes = ZERO_LENGTH_CLASS_ARRAY;
        } else {
            paramTypes = new Class[] { fieldType };
        }
        Method[] methods = declaringClz.getMethods();
        if (methods == null) {
            return null;
        }
        for (Method method : methods) {
            String methodName = method.getName();
            if (!setterGetterName.equalsIgnoreCase(methodName)) {
                continue;
            }
            if (Arrays.equals(paramTypes, method.getParameterTypes())) {
                if (OtcReflectionUtil.GETTER_SETTER.GETTER == enumGetterSetter) {
                    if (method.getReturnType().equals(fieldType)) {
                        return method;
                    }
                    return null;
                }
                return method;
            }
        }
        return null;
    }

    private static String createMethodNotFoundMessage(Class<?> clz, String methodName, Class<?>[] paramTypes,
                                                      String tokenPath) {
        StringBuilder paramsBuilder = null;
        if (paramTypes != null && paramTypes.length > 0) {
            for (Class<?> paramType : paramTypes) {
                if (paramsBuilder == null) {
                    paramsBuilder = new StringBuilder("(").append(paramType.getName());
                } else {
                    paramsBuilder.append(",").append(paramType.getName());
                }
            }
            paramsBuilder.append(")");
        } else {
            paramsBuilder = new StringBuilder("()");
        }
        return String.format("Java Bean spec violation - Method '%s.%s%s not found for object-path : %s", clz.getName(), methodName,
                paramsBuilder, tokenPath);
    }

}
