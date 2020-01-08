# Core-check
基于Spring切面的注解校验

# 简介
自定义注解校验，简称“系统注解校验”，是一款通过AOP切面实现的Controller层参数校验。设计的来由是因为我们系统中校验逻辑混乱，校验代码比较繁重，为解决项目代码在各个版本中的灵活使用应运而生的。

# 注解说明
“系统注解校验”工具一共涉及到三类注解，如下：
第一类：参数注解@EnableCheck
第二类：属性说明注解@CheckField
第三类：属性校验注解@CheckAssertFalse、@CheckAssertTrue、@CheckDigits、@CheckEnum、@CheckLength、@CheckMax、@CheckMin、@CheckNotBlank、@CheckNotEmpty、@CheckNotNull、@CheckNull、@CheckPattern、@CheckSqlUniqe
#### 注解详细说明
| 注解  | 位置  | 类别  | 参数  | 范围  | 说明  |
| ------------ | ------------ | ------------ | ------------ | ------------ | ------------ |
| @EnableCheck  | 参数  | 一  | // 有效分组<br>String[] effectGroup() default {};  | All  | 系统注解开关，只有该注解修饰的参数才进行校验  |
| @CheckField  | 属性  | 二  | // 属性名称<br>String name();<br>// 属性顺序<br>int sort();<br>// 属性描述<br>String describe() default "";  | All  | 说明的注解，该注解配置了属性的名称、校验执行的顺序、以及属性说明  |
| @CheckAssertFalse  | 属性  | 三  | // 自定义校验信息<br>String message() default "";<br>// 分组<br>String[] groups() default {};  | java.lang.Boolean<br>boolean  | 验证注解的元素值是false  |
| @CheckAssertTrue  | 属性  | 三  | // 自定义校验信息<br>String message() default "";<br>// 分组<br>String[] groups() default {};  | java.lang.Boolean<br>boolean  | 验证注解的元素值是true  |
| @CheckDigits  | 属性  | 三  | // 自定义校验信息<br>String message() default "";<br>// 分组<br>String[] groups() default {};<br>// 整数上限<br>int integer();<br>// 小数上限<br>int fraction();  | byte<br>short<br>int<br>long<br>java.lang.Byte<br>java.lang.Short<br>java.lang.Integer<br>java.lang.Long<br>java.lang.String<br>java.lang.StringBuffer<br>java.lang.StringBuilder<br>java.math.BigInteger<br>java.math.BigDecimal  | 验证注解的元素值的整数位数和小数位数上限  |
| @CheckEnum  | 属性  | 三  | // 自定义校验信息<br>String message() default "";<br>// 分组<br>String[] groups() default {};<br>// 枚举存在的类别<br>String[] values();  | byte<br>short<br>int<br>long<br>java.lang.Byte<br>java.lang.Short<br>java.lang.Integer<br>java.lang.Long<br>java.lang.String<br>java.lang.StringBuffer<br>java.lang.StringBuilder  | 验证枚举匹配  |
| @CheckLength  | 属性  | 三  | // 自定义校验信息<br>String message() default "";<br>// 分组<br>String[] groups() default {};<br>// 最小<br>int min() default 0;<br>// 最大<br>int max() default Integer.MAX_VALUE;  | java.lang.String<br>java.lang.StringBuffer<br>java.lang.StringBuilder  | 验证注解的元素值长度在min和max（包含）指定区间之内  |
| @CheckMax  | 属性  | 三  | // 自定义校验信息<br>String message() default "";<br>// 分组<br>String[] groups() default {};<br>// 指定数值<br>String max();  | byte<br>short<br>int<br>long<br>java.lang.Byte<br>java.lang.Short<br>java.lang.Integer<br>java.lang.Long<br>java.lang.String<br>java.lang.StringBuffer<br>java.lang.StringBuilder<br>java.math.BigInteger<br>java.math.BigDecimal  | 验证注解的元素值小于等于@Max指定的value值  |
| @CheckMin  | 属性  | 三  | // 自定义校验信息<br>String message() default "";<br>// 分组<br>String[] groups() default {};<br>// 指定数值<br>String min();  | byte<br>short<br>int<br>long<br>java.lang.Byte<br>java.lang.Short<br>java.lang.Integer<br>java.lang.Long<br>java.lang.String<br>java.lang.StringBuffer<br>java.lang.StringBuilder<br>java.math.BigInteger<br>java.math.BigDecimal  | 验证注解的元素值大于等于@Min指定的value值  |
| @CheckNotBlank  | 属性  | 三  | // 自定义校验信息<br>String message() default "";<br>// 分组<br>String[] groups() default {};  | java.lang.String<br>java.lang.StringBuffer<br>java.lang.StringBuilder  | 验证注解的元素值不为空（不为null、去除首尾空格后长度不为0），不同于@NotEmpty，@NotBlank只应用于字符串且在比较时会去除字符串的首尾空格  |
| @CheckNotEmpty  | 属性  | 三  | // 自定义校验信息<br>String message() default "";<br>// 分组<br>String[] groups() default {};  | java.lang.String<br>java.lang.StringBuffer<br>java.lang.StringBuilder<br>java.util.Collection<br>java.util.List<br>java.util.Set<br>java.util.Map<br>java.util.ArrayList<br>java.util.LinkedList<br>java.util.HashSet<br>java.util.LinkedHashSet<br>java.util.HashMap<br>java.util.LinkedHashMap  | 验证注解的元素值不为null且不为空（字符串长度不为0、集合大小不为0）  |
| @CheckNotNull  | 属性  | 三  | // 自定义校验信息<br>String message() default "";<br>// 分组<br>String[] groups() default {};  | byte<br>short<br>int<br>long<br>java.lang.Byte<br>java.lang.Short<br>java.lang.Integer<br>java.lang.Long<br>java.lang.String<br>java.lang.StringBuffer<br>java.lang.StringBuilder<br>java.math.BigInteger<br>java.math.BigDecimal<br>java.util.Collection<br>java.util.List<br>java.util.Set<br>java.util.Map<br>java.util.ArrayList<br>java.util.LinkedList<br>java.util.HashSet<br>java.util.LinkedHashSet<br>java.util.HashMap<br>java.util.LinkedHashMap<br>java.lang.Boolean<br>boolean<br>java.util.Date  | 验证注解的元素不是null  |
| @CheckNull  | 属性  | 三  | // 自定义校验信息<br>String message() default "";<br>// 分组<br>String[] groups() default {};  | byte<br>short<br>int<br>long<br>java.lang.Byte<br>java.lang.Short<br>java.lang.Integer<br>java.lang.Long<br>java.lang.String<br>java.lang.StringBuffer<br>java.lang.StringBuilder<br>java.math.BigInteger<br>java.math.BigDecimal<br>java.util.Collection<br>java.util.List<br>java.util.Set<br>java.util.Map<br>java.util.ArrayList<br>java.util.LinkedList<br>java.util.HashSet<br>java.util.LinkedHashSet<br>java.util.HashMap<br>java.util.LinkedHashMap<br>java.lang.Boolean<br>boolean<br>java.util.Date  | 验证注解的元素是null  |
| @CheckPattern  | 属性  | 三  | // 自定义校验信息<br>String message() default "";<br>// 分组<br>String[] groups() default {};<br>// 正则表达式<br>String regexp();  | java.lang.String<br>java.lang.StringBuffer<br>java.lang.StringBuilder  | 验证注解的元素值与指定的正则表达式匹配  |
| @CheckSqlUniqe  | 属性  | 三  | // 自定义校验信息<br>String message() default "";<br>// 分组<br>String[] groups() default {};<br>// 表名<br>String tableName();<br>// 字段名<br>String columName();  | java.lang.String<br>java.lang.StringBuffer<br>java.lang.StringBuilder  | 验证数据库是否唯一  |

# 扩展
系统注解校验，不光可以校验JDK里已经有的数据类型，对于我们自定义的数据类型也可以校验。
但对于自定义类型的校验，只可以使用@CheckNotNull、@CheckNull两个注解。
1.属性为自定义类
![](http://192.168.1.151:4999/server/../Public/Uploads/2020-01-07/5e144218e5027.png)
2.属性为集合类型（泛型为自定义类）注：集合类型只可以为java.util.Collection、java.util.List、java.util.Set、java.util.ArrayList、java.util.LinkedList、java.util.HashSet、java.util.LinkedHashSet
![](http://192.168.1.151:4999/server/../Public/Uploads/2020-01-07/5e1442cd6929b.png)
3.属性为Map类型（泛型为自定义类）注：Map类型只可以为java.util.Map、java.util.HashMap、java.util.LinkedHashMap
![](http://192.168.1.151:4999/server/../Public/Uploads/2020-01-07/5e14430b53047.png)

# 校验顺序
校验顺序配置在 第二类 属性说明注解@CheckField的sort属性中。
![](http://192.168.1.151:4999/server/../Public/Uploads/2020-01-07/5e1443791089d.png)

# 校验分组
校验分组配置在 第三类 属性校验注解@CheckAssertFalse、@CheckAssertTrue、@CheckDigits、@CheckEnum、@CheckLength、@CheckMax、@CheckMin、@CheckNotBlank、@CheckNotEmpty、@CheckNotNull、@CheckNull、@CheckPattern、@CheckSqlUniqe的groups属性中。并与第一类 @EnableCheck的effectGroup属性相对应。
![](http://192.168.1.151:4999/server/../Public/Uploads/2020-01-07/5e1443f3be1ca.png)
![](http://192.168.1.151:4999/server/../Public/Uploads/2020-01-07/5e144449046cb.png)

# 自定义校验消息
自定义校验消息配置在 第三类 属性校验注解@CheckAssertFalse、@CheckAssertTrue、@CheckDigits、@CheckEnum、@CheckLength、@CheckMax、@CheckMin、@CheckNotBlank、@CheckNotEmpty、@CheckNotNull、@CheckNull、@CheckPattern、@CheckSqlUniqe的message属性中。
![](http://192.168.1.151:4999/server/../Public/Uploads/2020-01-07/5e1448ebe7f02.png)

# 约束条件
1.Controller接口参数只能有一个，也就是只能接收一个vo参数。
2.vo中的属性字段如果需要校验，则必须加@CheckFiled注解才能生效。
3.校验Map的时候，不会校验Map的key，只校验Map的value，无论value是Jdk自带的数据类型，还是自定义类型。
