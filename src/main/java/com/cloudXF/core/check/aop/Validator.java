package com.cloudXF.core.check.aop;

import com.cloudXF.core.check.annotation.*;
import com.cloudXF.core.check.bean.Param;
import com.cloudXF.core.check.bean.Result;
import com.cloudXF.core.check.enumd.SystemTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName: Validator
 * @Description: 自定义校验处理类
 * @Author: MaoWei
 * @Date: 2020/1/3 16:35
 **/
@Aspect
@Component
public class Validator {

    @Resource
    private DataSource dataSource;

    @Around("execution(public * com.cloudXF..controller..*(..))")
    public Object validate(ProceedingJoinPoint pjp) throws NoSuchMethodException {
        // 获取被拦截的方法的参数
        Object[] args = pjp.getArgs();
        Object target = pjp.getTarget();
        MethodSignature msig = (MethodSignature) pjp.getSignature();
        Method method = target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        try {
            // 遍历该方法的所有参数
            if (args != null && args.length == 1) {
                for (Object arg : args) {
                    // 获取参数类型
                    Class<?> argClazz = arg.getClass();
                    Annotation[] parameterAnnotation = parameterAnnotations[0];
                    List<Annotation> annotations = Arrays.asList(parameterAnnotation);
                    EnableCheck enableCheckAnno = null;
                    for (Annotation annotation : annotations) {
                        if (annotation.annotationType().equals(EnableCheck.class)) {
                            enableCheckAnno = (EnableCheck) annotation;
                        }
                    }
                    if (enableCheckAnno != null) {
                        String[] effectGroup = enableCheckAnno.effectGroup();
                        // 获取参数所有属性
                        List<Param> paramList = new ArrayList<Param>();
                        try {
                            getAllFileds(paramList, argClazz, arg, 0);
                        } catch (Exception e) {
                            throw new RuntimeException("注解校验获取属性失败！！");
                        }

                        if (paramList != null && paramList.size() > 0) {
                            // 校验顺序排序
                            Collections.sort(paramList, new Comparator<Param>() {
                                @Override
                                public int compare(Param o1, Param o2) {
                                    if (o1.getSort() > o2.getSort()) {
                                        return 1;
                                    } else if (o1.getSort() == o2.getSort()) {
                                        return 0;
                                    } else {
                                        return -1;
                                    }
                                }
                            });
                            // 遍历所有属性,并找出有注解的
                            for (Param param : paramList) {
                                // 获取类名
                                String className = param.getClassName();
                                // 获取属性
                                Field field = param.getField();
                                // 获取属性值
                                Object fieldObj = param.getValue();
                                // 获取属性名称
                                String fieldName = field.getName();
                                // 获取属性类型
                                Class<?> fieldType = field.getType();
                                // 检查每个属性的注解,有注解的才处理
                                Annotation[] fieldAnns = field.getAnnotations();
                                // 校验属性使用注解的正确性
                                checkAnnotationForType(className, fieldName, fieldType, fieldAnns);
                                doCheck(param, effectGroup);
                            }
                        }
                    }
                }
            }
            Object result = pjp.proceed();
            return result;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return new Result().setStatus("fail").setErrcode(1).setMessage(throwable.getMessage());
        }
    }

    /**
     * 获取所有属性
     *
     * @param paramList Param集合
     * @param argClazz  参数类型
     * @param obj       参数值
     * @param index     循环次数
     */
    private void getAllFileds(List<Param> paramList, Class<?> argClazz, Object obj, int index) throws IllegalArgumentException, IllegalAccessException {
        if (argClazz == null) {
            return;
        }
        List<Class<?>> systemTypes = SystemTypeEnum.getAll();
        List<Class<?>> systemCollectionTypes = SystemTypeEnum.getAllCollection();
        if (systemTypes.contains(argClazz)) {
            return;
        }
        // 获取自定义类型的属性以及父类型属性
        List<Field> declaredFieldList = new ArrayList<Field>();
        Class<?> tempSuperClazz = argClazz;
        while (!tempSuperClazz.getName().toLowerCase().equals("java.lang.object")) {
            declaredFieldList.addAll(Arrays.asList(tempSuperClazz.getDeclaredFields()));
            tempSuperClazz = tempSuperClazz.getSuperclass();
        }
        if (declaredFieldList != null && declaredFieldList.size() > 0) {
            for (Field declaredField : declaredFieldList) {
                declaredField.setAccessible(true);
                CheckField checkFieldAnno = declaredField.getAnnotation(CheckField.class);
                if (checkFieldAnno == null) {
                    continue;
                }
                int sort = (index == -1) ? Integer.MAX_VALUE : checkFieldAnno.sort();
                Class<?> fieldClazz = declaredField.getType();
                Type genericType = declaredField.getGenericType();
                if (systemCollectionTypes.contains(fieldClazz)) {
                    // 集合类型处理
                    if (genericType == null) {
                        continue;
                    }
                    // 如果是泛型参数的类型
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) genericType;
                        //得到泛型里的class类型对象
                        Class<?> genericClazz = (Class<?>) pt.getActualTypeArguments()[0];
                        if (obj == null || declaredField.get(obj) == null) {
//                            getAllFileds(paramList, genericClazz, null);
                            paramList.add(new Param(argClazz.getSimpleName(), declaredField.getName(), sort, declaredField, null));
                        } else {
                            Class<?> tempClazz = declaredField.get(obj).getClass();
                            if (tempClazz.equals(java.util.Collection.class)
                                    || tempClazz.equals(java.util.List.class)
                                    || tempClazz.equals(java.util.Set.class)
                                    || tempClazz.equals(java.util.ArrayList.class)
                                    || tempClazz.equals(java.util.LinkedList.class)
                                    || tempClazz.equals(java.util.HashSet.class)
                                    || tempClazz.equals(java.util.LinkedHashSet.class)) {
                                Object tempObj = declaredField.get(obj);
                                Collection tempCollection = (Collection) tempObj;
                                Iterator iterator = tempCollection.iterator();
                                List resultList = new ArrayList();
                                while (iterator.hasNext()) {
                                    Object next = iterator.next();
                                    if (!systemTypes.contains(genericClazz)) {
                                        getAllFileds(paramList, genericClazz, next, -1);
                                    }
                                    resultList.add(next);
                                }
                                paramList.add(new Param(argClazz.getSimpleName(), declaredField.getName(), sort, declaredField, resultList));
                            } else if (tempClazz.equals(java.util.Map.class)
                                    || tempClazz.equals(java.util.HashMap.class)
                                    || tempClazz.equals(java.util.LinkedHashMap.class)) {
                                Class<?> genericClazz1 = (Class<?>) pt.getActualTypeArguments()[1];
                                Object tempObj = declaredField.get(obj);
                                Map tempMap = (Map) tempObj;
                                Iterator<Map.Entry> iterator = tempMap.entrySet().iterator();
                                Map resultMap = new HashMap();
                                while (iterator.hasNext()) {
                                    Map.Entry next = iterator.next();
                                    if (!systemTypes.contains(genericClazz1)) {
                                        getAllFileds(paramList, genericClazz1, next.getValue(), -1);
                                    }
                                    resultMap.put(next.getKey(), next.getValue());
                                }
                                paramList.add(new Param(argClazz.getSimpleName(), declaredField.getName(), sort, declaredField, resultMap));
                            }
                        }
                    }
                } else if (systemTypes.contains(fieldClazz)) {
                    // 系统类型处理（除集合类型外）
                    paramList.add(new Param(argClazz.getSimpleName(), declaredField.getName(), sort, declaredField, obj == null ? null : declaredField.get(obj)));
                } else {
                    // 自定义类型处理
                    if (obj != null && declaredField.get(obj) != null) {
                        getAllFileds(paramList, fieldClazz, declaredField.get(obj), -1);
                    }
                    paramList.add(new Param(argClazz.getSimpleName(), declaredField.getName(), sort, declaredField, obj == null ? null : declaredField.get(obj)));
                }
            }
        }
    }

    /**
     * 校验属性属性使用注解的正确性
     *
     * @param className 类名称
     * @param fieldName 属性名称
     * @param fieldType 属性类型
     * @param fieldAnns 属性注解
     */
    private void checkAnnotationForType(String className, String fieldName, Class<?> fieldType, Annotation[] fieldAnns) {
        int group = SystemTypeEnum.getGroup(fieldType);

        if (fieldAnns != null && fieldAnns.length > 0) {
            switch (group) {
                case 1:
                    for (Annotation fieldAnn : fieldAnns) {
                        Class<? extends Annotation> annClazz = fieldAnn.annotationType();
                        if (!annClazz.equals(CheckField.class)) {
                            if (!annClazz.equals(CheckDigits.class)
                                    && !annClazz.equals(CheckEnum.class)
                                    && !annClazz.equals(CheckMax.class)
                                    && !annClazz.equals(CheckMin.class)
                                    && !annClazz.equals(CheckNotNull.class)
                                    && !annClazz.equals(CheckNull.class)) {
                                throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]不能使用[" + fieldAnn.annotationType().getSimpleName() + "]注解!只可使用" +
                                        "[" + CheckDigits.class.getSimpleName() + "]、" +
                                        "[" + CheckEnum.class.getSimpleName() + "]、" +
                                        "[" + CheckMax.class.getSimpleName() + "]、" +
                                        "[" + CheckMin.class.getSimpleName() + "]、" +
                                        "[" + CheckNotNull.class.getSimpleName() + "]、" +
                                        "[" + CheckNull.class.getSimpleName() + "]等注解。");
                            }
                        }
                    }
                    break;
                case 2:
                    for (Annotation fieldAnn : fieldAnns) {
                        Class<? extends Annotation> annClazz = fieldAnn.annotationType();
                        if (!annClazz.equals(CheckField.class)) {
                            if (!annClazz.equals(CheckDigits.class)
                                    && !annClazz.equals(CheckEnum.class)
                                    && !annClazz.equals(CheckLength.class)
                                    && !annClazz.equals(CheckMax.class)
                                    && !annClazz.equals(CheckMin.class)
                                    && !annClazz.equals(CheckNotBlank.class)
                                    && !annClazz.equals(CheckNotEmpty.class)
                                    && !annClazz.equals(CheckNotNull.class)
                                    && !annClazz.equals(CheckNull.class)
                                    && !annClazz.equals(CheckPattern.class)
                                    && !annClazz.equals(CheckSqlUniqe.class)) {
                                throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]不能使用[" + fieldAnn.annotationType().getSimpleName() + "]注解!只可使用" +
                                        "[" + CheckDigits.class.getSimpleName() + "]、" +
                                        "[" + CheckEnum.class.getSimpleName() + "]、" +
                                        "[" + CheckLength.class.getSimpleName() + "]、" +
                                        "[" + CheckMax.class.getSimpleName() + "]、" +
                                        "[" + CheckMin.class.getSimpleName() + "]、" +
                                        "[" + CheckNotBlank.class.getSimpleName() + "]、" +
                                        "[" + CheckNotEmpty.class.getSimpleName() + "]、" +
                                        "[" + CheckNotNull.class.getSimpleName() + "]、" +
                                        "[" + CheckNull.class.getSimpleName() + "]、" +
                                        "[" + CheckPattern.class.getSimpleName() + "]、" +
                                        "[" + CheckSqlUniqe.class.getSimpleName() + "]等注解。");
                            }
                        }
                    }
                    break;
                case 3:
                    for (Annotation fieldAnn : fieldAnns) {
                        Class<? extends Annotation> annClazz = fieldAnn.annotationType();
                        if (!annClazz.equals(CheckField.class)) {
                            if (!annClazz.equals(CheckNotNull.class)
                                    && !annClazz.equals(CheckNull.class)) {
                                throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]不能使用[" + fieldAnn.annotationType().getSimpleName() + "]注解!只可使用" +
                                        "[" + CheckNotNull.class.getSimpleName() + "]、" +
                                        "[" + CheckNull.class.getSimpleName() + "]等注解。");
                            }
                        }
                    }
                    break;
                case 4:
                    for (Annotation fieldAnn : fieldAnns) {
                        Class<? extends Annotation> annClazz = fieldAnn.annotationType();
                        if (!annClazz.equals(CheckField.class)) {
                            if (!annClazz.equals(CheckDigits.class)
                                    && !annClazz.equals(CheckMax.class)
                                    && !annClazz.equals(CheckMin.class)
                                    && !annClazz.equals(CheckNotNull.class)
                                    && !annClazz.equals(CheckNull.class)) {
                                throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]不能使用[" + fieldAnn.annotationType().getSimpleName() + "]注解!只可使用" +
                                        "[" + CheckDigits.class.getSimpleName() + "]、" +
                                        "[" + CheckMax.class.getSimpleName() + "]、" +
                                        "[" + CheckMin.class.getSimpleName() + "]、" +
                                        "[" + CheckNotNull.class.getSimpleName() + "]、" +
                                        "[" + CheckNull.class.getSimpleName() + "]等注解。");
                            }
                        }
                    }
                    break;
                case 5:
                    for (Annotation fieldAnn : fieldAnns) {
                        Class<? extends Annotation> annClazz = fieldAnn.annotationType();
                        if (!annClazz.equals(CheckField.class)) {
                            if (!annClazz.equals(CheckNotEmpty.class)
                                    && !annClazz.equals(CheckNotNull.class)
                                    && !annClazz.equals(CheckNull.class)) {
                                throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]不能使用[" + fieldAnn.annotationType().getSimpleName() + "]注解!只可使用" +
                                        "[" + CheckNotEmpty.class.getSimpleName() + "]、" +
                                        "[" + CheckNotNull.class.getSimpleName() + "]、" +
                                        "[" + CheckNull.class.getSimpleName() + "]等注解。");
                            }
                        }
                    }
                    break;
                case 6:
                    for (Annotation fieldAnn : fieldAnns) {
                        Class<? extends Annotation> annClazz = fieldAnn.annotationType();
                        if (!annClazz.equals(CheckField.class)) {
                            if (!annClazz.equals(CheckAssertFalse.class)
                                    && !annClazz.equals(CheckAssertTrue.class)
                                    && !annClazz.equals(CheckNotNull.class)
                                    && !annClazz.equals(CheckNull.class)) {
                                throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]不能使用[" + fieldAnn.annotationType().getSimpleName() + "]注解!只可使用" +
                                        "[" + CheckAssertFalse.class.getSimpleName() + "]、" +
                                        "[" + CheckAssertTrue.class.getSimpleName() + "]、" +
                                        "[" + CheckNotNull.class.getSimpleName() + "]、" +
                                        "[" + CheckNull.class.getSimpleName() + "]等注解。");
                            }
                        }
                    }
                    break;
                default:
                    for (Annotation fieldAnn : fieldAnns) {
                        Class<? extends Annotation> annClazz = fieldAnn.annotationType();
                        if (!annClazz.equals(CheckField.class)) {
                            if (!annClazz.equals(CheckNotNull.class)
                                    && !annClazz.equals(CheckNull.class)) {
                                throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]不能使用[" + fieldAnn.annotationType().getSimpleName() + "]注解!只可使用" +
                                        "[" + CheckNotNull.class.getSimpleName() + "]、" +
                                        "[" + CheckNull.class.getSimpleName() + "]等注解。");
                            }
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 执行校验
     *
     * @param param       属性参数
     * @param effectGroup 有效分组
     */
    private void doCheck(Param param, String[] effectGroup) {
        Field field = param.getField();
        // 获取类名
        String className = param.getClassName();
        // 获取属性值
        Object fieldObj = param.getValue();
        // 获取属性名称
        String fieldName = field.getName();
        // 获取属性类型
        Class<?> fieldType = field.getType();
        // 检查每个属性的注解,有注解的才处理
        Annotation[] fieldAnns = field.getAnnotations();
        List<Class<? extends Annotation>> annClazzList = new ArrayList<Class<? extends Annotation>>();
        if (fieldAnns != null && fieldAnns.length > 0) {
            for (Annotation fieldAnn : fieldAnns) {
                annClazzList.add(fieldAnn.annotationType());
            }
        }
        // 【CheckNotNull】验证注解的元素不是null
        if (annClazzList.contains(CheckNotNull.class)) {
            checkField(annClazzList, className, fieldName);
            CheckNotNull checkNotNullAnno = field.getAnnotation(CheckNotNull.class);
            String message = checkNotNullAnno.message();
            String[] groups = checkNotNullAnno.groups();
            if (effectGroup(effectGroup, groups)) {
                try {
                    if (fieldObj == null) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的值为NULL!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getClass().equals(IllegalArgumentException.class)) {
                        if (StringUtils.isNotEmpty(message)) {
                            throw new IllegalArgumentException(message);
                        }
                        throw new IllegalArgumentException(e.getMessage());
                    }
                    throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]值的格式错误，无法进行相应的校验!!");
                }
            }
        }
        // 【CheckNull】验证注解的元素是null
        if (annClazzList.contains(CheckNull.class)) {
            checkField(annClazzList, className, fieldName);
            CheckNull checkNullAnno = field.getAnnotation(CheckNull.class);
            String message = checkNullAnno.message();
            String[] groups = checkNullAnno.groups();
            if (effectGroup(effectGroup, groups)) {
                try {
                    if (fieldObj != null) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的值不为NULL!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getClass().equals(IllegalArgumentException.class)) {
                        if (StringUtils.isNotEmpty(message)) {
                            throw new IllegalArgumentException(message);
                        }
                        throw new IllegalArgumentException(e.getMessage());
                    }
                    throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]值的格式错误，无法进行相应的校验!!");
                }
            }
        }
        // 【CheckNotBlank】验证注解的元素值不为空（不为null、去除首尾空格后长度不为0），不同于@NotEmpty，@NotBlank只应用于字符串且在比较时会去除字符串的首尾空格
        if (annClazzList.contains(CheckNotBlank.class)) {
            checkField(annClazzList, className, fieldName);
            CheckNotBlank checkNotBlankAnno = field.getAnnotation(CheckNotBlank.class);
            String message = checkNotBlankAnno.message();
            String[] groups = checkNotBlankAnno.groups();
            String source = "";
            if (effectGroup(effectGroup, groups)) {
                try {
                    if (fieldType.equals(SystemTypeEnum.STRING.getType())) {
                        source = String.valueOf(fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.STRINGBUFFER.getType())) {
                        source = String.valueOf(fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.STRINGBUILDER.getType())) {
                        source = String.valueOf(fieldObj);
                    }
                    if (StringUtils.isBlank(source)) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的值是空字符串!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getClass().equals(IllegalArgumentException.class)) {
                        if (StringUtils.isNotEmpty(message)) {
                            throw new IllegalArgumentException(message);
                        }
                        throw new IllegalArgumentException(e.getMessage());
                    }
                    throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]值的格式错误，无法进行相应的校验!!");
                }
            }
        }
        // 【CheckNotEmpty】验证注解的元素值不为null且不为空（字符串长度不为0、集合大小不为0）
        if (annClazzList.contains(CheckNotEmpty.class)) {
            checkField(annClazzList, className, fieldName);
            CheckNotEmpty checkNotEmptyAnno = field.getAnnotation(CheckNotEmpty.class);
            String message = checkNotEmptyAnno.message();
            String[] groups = checkNotEmptyAnno.groups();
            if (effectGroup(effectGroup, groups)) {
                if (fieldObj == null || fieldObj.toString().length() == 0) {
                    throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的值是空!");
                }
                try {
                    // 字符串判断
                    if (fieldType.equals(SystemTypeEnum.STRING.getType()) && StringUtils.isEmpty(String.valueOf(fieldObj))) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的值是空!");
                    } else if (fieldType.equals(SystemTypeEnum.STRINGBUFFER.getType()) && StringUtils.isEmpty(String.valueOf(fieldObj))) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的值是空!");
                    } else if (fieldType.equals(SystemTypeEnum.STRINGBUILDER.getType()) && StringUtils.isEmpty(String.valueOf(fieldObj))) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的值是空!");
                    }
                    // Collection判断
                    if (fieldType.equals(SystemTypeEnum.COLLECTION.getType()) && ((Collection) fieldObj).size() == 0) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的长度是空!");
                    } else if (fieldType.equals(SystemTypeEnum.LIST.getType()) && ((Collection) fieldObj).size() == 0) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的长度是空!");
                    } else if (fieldType.equals(SystemTypeEnum.SET.getType()) && ((Collection) fieldObj).size() == 0) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的长度是空!");
                    } else if (fieldType.equals(SystemTypeEnum.ARRAYLIST.getType()) && ((Collection) fieldObj).size() == 0) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的长度是空!");
                    } else if (fieldType.equals(SystemTypeEnum.LINKEDLIST.getType()) && ((Collection) fieldObj).size() == 0) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的长度是空!");
                    } else if (fieldType.equals(SystemTypeEnum.HASHSET.getType()) && ((Collection) fieldObj).size() == 0) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的长度是空!");
                    } else if (fieldType.equals(SystemTypeEnum.LINKEDHASHSET.getType()) && ((Collection) fieldObj).size() == 0) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的长度是空!");
                    }
                    // map判断
                    if (fieldType.equals(SystemTypeEnum.MAP.getType()) && ((Map) fieldObj).size() == 0) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的长度是空!");
                    } else if (fieldType.equals(SystemTypeEnum.HASHMAP.getType())) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的长度是空!");
                    } else if (fieldType.equals(SystemTypeEnum.LINKEDHASHMAP.getType())) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的长度是空!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getClass().equals(IllegalArgumentException.class)) {
                        if (StringUtils.isNotEmpty(message)) {
                            throw new IllegalArgumentException(message);
                        }
                        throw new IllegalArgumentException(e.getMessage());
                    }
                    throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]值的格式错误，无法进行相应的校验!!");
                }
            }
        }
        // 【CheckAssertFalse】验证注解的元素值是false
        if (annClazzList.contains(CheckAssertFalse.class)) {
            checkField(annClazzList, className, fieldName);
            CheckAssertFalse checkAssertFalseAnno = field.getAnnotation(CheckAssertFalse.class);
            String message = checkAssertFalseAnno.message();
            String[] groups = checkAssertFalseAnno.groups();
            if (effectGroup(effectGroup, groups)) {
                try {
                    if ((Boolean) fieldObj) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的值不为false!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getClass().equals(IllegalArgumentException.class)) {
                        if (StringUtils.isNotEmpty(message)) {
                            throw new IllegalArgumentException(message);
                        }
                        throw new IllegalArgumentException(e.getMessage());
                    }
                    throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]值的格式错误，无法进行相应的校验!!");
                }
            }
        }
        // 【CheckAssertTrue】验证注解的元素值是true
        if (annClazzList.contains(CheckAssertTrue.class)) {
            checkField(annClazzList, className, fieldName);
            CheckAssertTrue checkAssertTrueAnno = field.getAnnotation(CheckAssertTrue.class);
            String message = checkAssertTrueAnno.message();
            String[] groups = checkAssertTrueAnno.groups();
            if (effectGroup(effectGroup, groups)) {
                try {
                    if (!(Boolean) fieldObj) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的值不为true!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getClass().equals(IllegalArgumentException.class)) {
                        if (StringUtils.isNotEmpty(message)) {
                            throw new IllegalArgumentException(message);
                        }
                        throw new IllegalArgumentException(e.getMessage());
                    }
                    throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]值的格式错误，无法进行相应的校验!!");
                }
            }
        }
        // 【CheckLength】验证注解的元素值长度在min和max（包含）指定区间之内
        if (annClazzList.contains(CheckLength.class)) {
            checkField(annClazzList, className, fieldName);
            CheckLength checkLengthAnno = field.getAnnotation(CheckLength.class);
            String message = checkLengthAnno.message();
            String[] groups = checkLengthAnno.groups();
            if (effectGroup(effectGroup, groups)) {
                // 最小
                int min = checkLengthAnno.min();
                // 最大
                int max = checkLengthAnno.max();
                String source = "";
                try {
                    if (fieldType.equals(SystemTypeEnum.STRING.getType())) {
                        source = String.valueOf(fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.STRINGBUFFER.getType())) {
                        source = String.valueOf(fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.STRINGBUILDER.getType())) {
                        source = String.valueOf(fieldObj);
                    }
                    if (source.length() > max) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的长度大于最大值[" + max + "]!");
                    }
                    if (source.length() < min) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的长度小于最小值[" + min + "]!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getClass().equals(IllegalArgumentException.class)) {
                        if (StringUtils.isNotEmpty(message)) {
                            throw new IllegalArgumentException(message);
                        }
                        throw new IllegalArgumentException(e.getMessage());
                    }
                    throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]值的格式错误，无法进行相应的校验!!");
                }
            }
        }
        // 【CheckPattern】验证注解的元素值与指定的正则表达式匹配
        if (annClazzList.contains(CheckPattern.class)) {
            checkField(annClazzList, className, fieldName);
            CheckPattern checkPatternAnno = field.getAnnotation(CheckPattern.class);
            String message = checkPatternAnno.message();
            String[] groups = checkPatternAnno.groups();
            String mode = checkPatternAnno.mode();
            if (effectGroup(effectGroup, groups)) {
                if (!"matches".equals(mode) && !"find".equals(mode)) {
                    throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的正则校验注解CheckPattern的mode属性只可设置为find或matches!");
                }
                // 正则
                String regexp = checkPatternAnno.regexp();
                String source = "";
                Pattern p = Pattern.compile(regexp);
                try {
                    if (fieldType.equals(SystemTypeEnum.STRING.getType())) {
                        source = String.valueOf(fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.STRINGBUFFER.getType())) {
                        source = String.valueOf(fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.STRINGBUILDER.getType())) {
                        source = String.valueOf(fieldObj);
                    }
                    Matcher m = p.matcher(source);
                    if (!m.matches() && "matches".equals(mode)) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的正则校验不匹配!");
                    } else if (!m.find() && "find".equals(mode)) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的正则校验未找到!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getClass().equals(IllegalArgumentException.class)) {
                        if (StringUtils.isNotEmpty(message)) {
                            throw new IllegalArgumentException(message);
                        }
                        throw new IllegalArgumentException(e.getMessage());
                    }
                    throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]值的格式错误，无法进行相应的校验!!");
                }
            }
        }
        // 【CheckSqlUniqe】验证数据库是否唯一
        if (annClazzList.contains(CheckSqlUniqe.class)) {
            checkField(annClazzList, className, fieldName);
            CheckSqlUniqe checkSqlUniqeAnno = field.getAnnotation(CheckSqlUniqe.class);
            String message = checkSqlUniqeAnno.message();
            String[] groups = checkSqlUniqeAnno.groups();
            if (effectGroup(effectGroup, groups)) {
                // 获取表名
                String tableName = checkSqlUniqeAnno.tableName();
                // 获取属性名
                String columName = checkSqlUniqeAnno.columName();
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                int count = 0;
                try {
                    String sql = "SELECT count(*) from " + tableName + " where " + columName + " = '" + String.valueOf(fieldObj) + "'";
                    connection = dataSource.getConnection();
                    preparedStatement = connection.prepareStatement(sql);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                    if (count > 0) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的不唯一!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getClass().equals(IllegalArgumentException.class)) {
                        if (StringUtils.isNotEmpty(message)) {
                            throw new IllegalArgumentException(message);
                        }
                        throw new IllegalArgumentException(e.getMessage());
                    }
                    throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]值的格式错误，无法进行相应的校验!!");
                } finally {
                    try {
                        if (preparedStatement != null) {
                            preparedStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (SQLException sqlException) {
                        throw new IllegalArgumentException("操作数据库失败！");
                    }

                }
            }
        }
        // 【CheckEnum】验证枚举匹配
        if (annClazzList.contains(CheckEnum.class)) {
            checkField(annClazzList, className, fieldName);
            CheckEnum checkEnumAnno = field.getAnnotation(CheckEnum.class);
            String message = checkEnumAnno.message();
            String[] groups = checkEnumAnno.groups();
            if (effectGroup(effectGroup, groups)) {
                // 获取枚举类别
                String[] values = checkEnumAnno.values();
                List<String> sourceList = Arrays.asList(values);
                String source = "";
                try {
                    if (fieldType.equals(SystemTypeEnum.BYTE_BASIC.getType()) || fieldType.equals(SystemTypeEnum.BYTE.getType())) {
                        source = String.valueOf(fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.SHORT_BASIC.getType()) || fieldType.equals(SystemTypeEnum.SHORT.getType())) {
                        source = String.valueOf(fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.INT_BASIC.getType()) || fieldType.equals(SystemTypeEnum.INT.getType())) {
                        source = String.valueOf(fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.LONG_BASIC.getType()) || fieldType.equals(SystemTypeEnum.LONG.getType())) {
                        source = String.valueOf(fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.STRING.getType())) {
                        source = String.valueOf(fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.STRINGBUFFER.getType())) {
                        source = String.valueOf(fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.STRINGBUILDER.getType())) {
                        source = String.valueOf(fieldObj);
                    }
                    if (!sourceList.contains(source)) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的值不在" + sourceList.toString() + "之中!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getClass().equals(IllegalArgumentException.class)) {
                        if (StringUtils.isNotEmpty(message)) {
                            throw new IllegalArgumentException(message);
                        }
                        throw new IllegalArgumentException(e.getMessage());
                    }
                    throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]值的格式错误，无法进行相应的校验!!");
                }
            }
        }
        // 【CheckDigits】验证注解的元素值的整数位数和小数位数上限
        if (annClazzList.contains(CheckDigits.class)) {
            checkField(annClazzList, className, fieldName);
            CheckDigits checkDigitsAnno = field.getAnnotation(CheckDigits.class);
            String message = checkDigitsAnno.message();
            String[] groups = checkDigitsAnno.groups();
            if (effectGroup(effectGroup, groups)) {
                // 获取整数上限
                int integer = checkDigitsAnno.integer();
                // 获取小数上限
                int fraction = checkDigitsAnno.fraction();
                BigDecimal targetBigdecimal = new BigDecimal(0);
                try {
                    if (fieldType.equals(SystemTypeEnum.BYTE_BASIC.getType()) || fieldType.equals(SystemTypeEnum.BYTE.getType())) {
                        targetBigdecimal = new BigDecimal((Byte) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.SHORT_BASIC.getType()) || fieldType.equals(SystemTypeEnum.SHORT.getType())) {
                        targetBigdecimal = new BigDecimal((Short) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.INT_BASIC.getType()) || fieldType.equals(SystemTypeEnum.INT.getType())) {
                        targetBigdecimal = new BigDecimal((Integer) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.LONG_BASIC.getType()) || fieldType.equals(SystemTypeEnum.LONG.getType())) {
                        targetBigdecimal = new BigDecimal((Long) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.STRING.getType())) {
                        targetBigdecimal = new BigDecimal((String) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.STRINGBUFFER.getType())) {
                        targetBigdecimal = new BigDecimal(String.valueOf(fieldObj));
                    } else if (fieldType.equals(SystemTypeEnum.STRINGBUILDER.getType())) {
                        targetBigdecimal = new BigDecimal(String.valueOf(fieldObj));
                    } else if (fieldType.equals(SystemTypeEnum.BIGINTEGER.getType())) {
                        targetBigdecimal = new BigDecimal((BigInteger) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.BIGDECIMAL.getType())) {
                        targetBigdecimal = (BigDecimal) fieldObj;
                    }
                    if (targetBigdecimal.scale() > fraction) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的小数位数超过上限[" + fraction + "]位!");
                    }
                    if (targetBigdecimal.precision() - targetBigdecimal.scale() > integer) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的整数位数超过上限[" + integer + "]位!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getClass().equals(IllegalArgumentException.class)) {
                        if (StringUtils.isNotEmpty(message)) {
                            throw new IllegalArgumentException(message);
                        }
                        throw new IllegalArgumentException(e.getMessage());
                    }
                    throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]值的格式错误，无法进行相应的校验!!");
                }
            }
        }
        // 【CheckMax】验证注解的元素值小于等于@Max指定的value值
        if (annClazzList.contains(CheckMax.class)) {
            checkField(annClazzList, className, fieldName);
            CheckMax checkMaxAnno = field.getAnnotation(CheckMax.class);
            String message = checkMaxAnno.message();
            String[] groups = checkMaxAnno.groups();
            if (effectGroup(effectGroup, groups)) {
                // 获取指定数值
                String max = checkMaxAnno.max();
                BigDecimal targetBigdecimal = new BigDecimal(0);
                try {
                    if (fieldType.equals(SystemTypeEnum.BYTE_BASIC.getType()) || fieldType.equals(SystemTypeEnum.BYTE.getType())) {
                        targetBigdecimal = new BigDecimal((Byte) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.SHORT_BASIC.getType()) || fieldType.equals(SystemTypeEnum.SHORT.getType())) {
                        targetBigdecimal = new BigDecimal((Short) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.INT_BASIC.getType()) || fieldType.equals(SystemTypeEnum.INT.getType())) {
                        targetBigdecimal = new BigDecimal((Integer) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.LONG_BASIC.getType()) || fieldType.equals(SystemTypeEnum.LONG.getType())) {
                        targetBigdecimal = new BigDecimal((Long) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.STRING.getType())) {
                        targetBigdecimal = new BigDecimal((String) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.STRINGBUFFER.getType())) {
                        targetBigdecimal = new BigDecimal(String.valueOf(fieldObj));
                    } else if (fieldType.equals(SystemTypeEnum.STRINGBUILDER.getType())) {
                        targetBigdecimal = new BigDecimal(String.valueOf(fieldObj));
                    } else if (fieldType.equals(SystemTypeEnum.BIGINTEGER.getType())) {
                        targetBigdecimal = new BigDecimal((BigInteger) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.BIGDECIMAL.getType())) {
                        targetBigdecimal = (BigDecimal) fieldObj;
                    }
                    if (targetBigdecimal.compareTo(new BigDecimal(max)) > 0) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的数值大于指定值[" + new BigDecimal(max) + "]!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getClass().equals(IllegalArgumentException.class)) {
                        if (StringUtils.isNotEmpty(message)) {
                            throw new IllegalArgumentException(message);
                        }
                        throw new IllegalArgumentException(e.getMessage());
                    }
                    throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]值的格式错误，无法进行相应的校验!!");
                }
            }
        }
        // 【CheckMin】验证注解的元素值大于等于@Min指定的value值
        if (annClazzList.contains(CheckMin.class)) {
            checkField(annClazzList, className, fieldName);
            CheckMin checkMinAnno = field.getAnnotation(CheckMin.class);
            String message = checkMinAnno.message();
            String[] groups = checkMinAnno.groups();
            if (effectGroup(effectGroup, groups)) {
                // 获取指定数值
                String min = checkMinAnno.min();
                BigDecimal targetBigdecimal = new BigDecimal(0);
                try {
                    if (fieldType.equals(SystemTypeEnum.BYTE_BASIC.getType()) || fieldType.equals(SystemTypeEnum.BYTE.getType())) {
                        targetBigdecimal = new BigDecimal((Byte) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.SHORT_BASIC.getType()) || fieldType.equals(SystemTypeEnum.SHORT.getType())) {
                        targetBigdecimal = new BigDecimal((Short) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.INT_BASIC.getType()) || fieldType.equals(SystemTypeEnum.INT.getType())) {
                        targetBigdecimal = new BigDecimal((Integer) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.LONG_BASIC.getType()) || fieldType.equals(SystemTypeEnum.LONG.getType())) {
                        targetBigdecimal = new BigDecimal((Long) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.STRING.getType())) {
                        targetBigdecimal = new BigDecimal((String) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.STRINGBUFFER.getType())) {
                        targetBigdecimal = new BigDecimal(String.valueOf(fieldObj));
                    } else if (fieldType.equals(SystemTypeEnum.STRINGBUILDER.getType())) {
                        targetBigdecimal = new BigDecimal(String.valueOf(fieldObj));
                    } else if (fieldType.equals(SystemTypeEnum.BIGINTEGER.getType())) {
                        targetBigdecimal = new BigDecimal((BigInteger) fieldObj);
                    } else if (fieldType.equals(SystemTypeEnum.BIGDECIMAL.getType())) {
                        targetBigdecimal = (BigDecimal) fieldObj;
                    }
                    if (targetBigdecimal.compareTo(new BigDecimal(min)) < 0) {
                        throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]的数值小于指定值指定值[" + new BigDecimal(min) + "]!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getClass().equals(IllegalArgumentException.class)) {
                        if (StringUtils.isNotEmpty(message)) {
                            throw new IllegalArgumentException(message);
                        }
                        throw new IllegalArgumentException(e.getMessage());
                    }
                    throw new IllegalArgumentException("类名：[" + className + "], 属性：[" + fieldName + "]值的格式错误，无法进行相应的校验!!");
                }
            }
        }
    }

    /**
     * 校验分组
     *
     * @param effectGroup 有效分组
     * @param groups      属性组别
     * @return
     */
    private boolean effectGroup(String[] effectGroup, String[] groups) {
        boolean flag = false;
        if (effectGroup == null || effectGroup.length == 0 || groups == null || groups.length == 0) {
            flag = true;
        } else {
            for (String effect : effectGroup) {
                if (Arrays.asList(groups).contains(effect)) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    /**
     * 校验声明注解
     *
     * @param annClazzList 属性所包含的所有注解
     * @param fieldName    属性名称
     */
    private void checkField(List<Class<? extends Annotation>> annClazzList, String className, String fieldName) {
        if (!annClazzList.contains(CheckField.class)) {
            throw new IllegalArgumentException("如果需要系统校验，类名：[" + className + "], 属性：[" + fieldName + "]必须包含注解CheckField!");
        }
    }
}
