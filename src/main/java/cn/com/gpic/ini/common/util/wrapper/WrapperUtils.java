package cn.com.gpic.ini.common.util.wrapper;

import cn.com.gpic.ini.common.util.wrapper.condition.annotations.*;
import cn.com.gpic.ini.common.util.wrapper.condition.domain.SqlLogical;
import cn.com.gpic.ini.common.util.wrapper.condition.domain.SqlSymbol;
import cn.com.gpic.ini.common.util.wrapper.condition.util.ClassUtils;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.AbstractChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class WrapperUtils {
    private static final Field PARAM_NAME_SEQ_FIELD;
    private static FieldStrategy whereStrategy;
    private static boolean mapUnderscoreToCamelCase;
    private static boolean capitalMode;
    private static String columnFormat;
    private static String logicNotDeleteValue;

    private WrapperUtils() {
        throw new AssertionError();
    }

    public static <T> QueryWrapper<T> query() {
        return Wrappers.query();
    }

    public static <T> UpdateWrapper<T> update() {
        return Wrappers.update();
    }

    public static <T> QueryWrapper<T> query(@Nullable Object parameters) {
        return query(parameters, Wrappers.query());
    }

    public static <T> QueryWrapper<T> query(@Nullable Object parameters, Class<T> entityClass) {
        QueryWrapper<T> wrapper = query(parameters, Wrappers.query());
        wrapper.setEntityClass(entityClass);
        return wrapper;
    }

    public static <T> UpdateWrapper<T> update(@Nullable Object parameters) {
        return update(parameters, Wrappers.update());
    }

    public static <T> QueryWrapper<T> query(@Nullable Object parameters, @Nonnull QueryWrapper<T> wrapper) {
        return (QueryWrapper) buildWrapper(parameters, (AbstractWrapper) wrapper);
    }

    public static <T> UpdateWrapper<T> update(@Nullable Object parameters, @Nonnull UpdateWrapper<T> wrapper) {
        return (UpdateWrapper) buildWrapper(parameters, (AbstractWrapper) wrapper);
    }

    public static <T> QueryChainWrapper<T> query(@Nullable Object parameters, @Nonnull QueryChainWrapper<T> wrapper) {
        return (QueryChainWrapper) buildWrapper(parameters, (AbstractChainWrapper) wrapper);
    }

    public static <T> UpdateChainWrapper<T> update(@Nullable Object parameters, @Nonnull UpdateChainWrapper<T> wrapper) {
        return (UpdateChainWrapper) buildWrapper(parameters, (AbstractChainWrapper) wrapper);
    }

    public static <T, Children extends AbstractWrapper<T, String, Children>> Children buildWrapper(@Nullable Object parameters, @Nonnull AbstractWrapper<T, String, Children> wrapper) {
        if (!Objects.isNull(parameters) && !ClassUtils.isSimpleProperty(parameters.getClass())) {
            if (parameters instanceof Map) {
                return (Children) buildWrapperByMap((Map) parameters, wrapper);
            } else {
                return !ClassUtils.isCollectionType(parameters.getClass()) && ClassUtils.isNormalClass(parameters.getClass()) ? buildWrapperByJavaBean(parameters, wrapper) : (Children) wrapper;
            }
        } else {
            return (Children) wrapper;
        }
    }

    public static <T, Children extends AbstractChainWrapper<T, String, Children, Param>, Param extends AbstractWrapper<T, String, Param>> Children buildWrapper(@Nullable Object parameters, @Nonnull AbstractChainWrapper<T, String, Children, Param> wrapper) {
        buildWrapper(parameters, wrapper.getWrapper());
        return (Children) wrapper;
    }

    public static <T, Children extends AbstractWrapper<T, String, Children>> Children buildWrapperByMap(@Nullable Map<String, Object> parameters, @Nonnull AbstractWrapper<T, String, Children> wrapper) {
        return MapUtil.isEmpty(parameters) ? (Children) wrapper : wrapper.allEq(parameters);
    }

    public static <T, Children extends AbstractWrapper<T, String, Children>> Children buildWrapperByJavaBean(@Nullable Object parameters, @Nonnull AbstractWrapper<T, String, Children> wrapper) {
        if (!Objects.isNull(parameters)) {
            Class<?> javaBeanType = parameters.getClass();
            PropertyDescriptor[] descriptors = BeanUtil.getPropertyDescriptors(javaBeanType);
            Map<SqlOrderBy, String> orderByMap = new HashMap<>();
            for (PropertyDescriptor descriptor : descriptors) {
                Method readMethod = descriptor.getReadMethod();
                Method writeMethod = descriptor.getWriteMethod();
                if (!Objects.isNull(readMethod) && !readMethod.getDeclaringClass().isAssignableFrom(Object.class)) {
                    ReflectionUtils.makeAccessible(readMethod);
                    getFieldAnnotation(javaBeanType, readMethod, writeMethod, SqlOrderBy.class).ifPresent(orderBy -> {
                        String column = StrUtil.isNotEmpty(orderBy.column()) ? getColumn(orderBy.column(), true) :
                                getFieldAnnotation(javaBeanType, readMethod, writeMethod, TableField.class).map(WrapperUtils::getColumn)
                                        .orElse(Optional.ofNullable(getColumn(descriptor.getName(), true)).orElse(descriptor.getName()));
                        orderByMap.put(orderBy, column);
                    });
                    Object value = ReflectionUtils.invokeMethod(readMethod, parameters);
                    if (!Objects.isNull(value)) {
                        Optional<TableField> tableFieldOptional = getFieldAnnotation(javaBeanType, readMethod, writeMethod, TableField.class);
                        if (validFieldStrategy(tableFieldOptional.map(TableField::whereStrategy).orElseGet(WrapperUtils::getWhereStrategy), value)) {
                            I18n i18n = getFieldAnnotation(javaBeanType, readMethod, writeMethod, I18n.class).orElse(null);
                            Optional<SqlCondition> sqlConditionOptional = getFieldAnnotation(javaBeanType, readMethod, writeMethod, SqlCondition.class);
                            SqlSymbol sqlSymbol = sqlConditionOptional.map(SqlCondition::symbol).orElse(SqlSymbol.EQ);
                            SqlLogical sqlLogical = sqlConditionOptional.map(SqlCondition::columnLogical).orElse(SqlLogical.OR);
                            boolean keepColumn = Objects.equals(sqlSymbol, SqlSymbol.EXISTS) || Objects.equals(sqlSymbol, SqlSymbol.NOT_EXISTS);
                            String[] columns = sqlConditionOptional.map((sqlCondition) -> keepColumn ? sqlCondition.columns() : getColumn(sqlCondition.columns()))
                                    .orElseGet(() -> new String[]{tableFieldOptional.map(WrapperUtils::getColumn).orElseGet(() -> Optional.ofNullable(getColumn(descriptor.getName(), true)).orElse(descriptor.getName()))});
                            int columnLength = columns.length;
                            if (columnLength == 1) {
                                addCondition(wrapper, sqlSymbol, columns[0], value, i18n);
                            } else {
                                boolean logicalIsAnd = SqlLogical.AND.equals(sqlLogical);
                                wrapper.and((children) -> {
                                    IntStream.range(0, columnLength).forEach((i) -> {
                                        if (logicalIsAnd) {
                                            children.and((and) -> addCondition(and, sqlSymbol, columns[i], value, i18n));
                                        } else {
                                            children.or((or) -> addCondition(or, sqlSymbol, columns[i], value, i18n));
                                        }
                                    });
                                });
                            }
                        }
                    }
                }
            }
            if (CollUtil.isNotEmpty(orderByMap.keySet())) {
                orderByMap.keySet().stream().sorted(Comparator.comparing(SqlOrderBy::sortNum))
                        .forEachOrdered(order -> wrapper.orderBy(true, order.isAsc(), orderByMap.get(order)));
            }
        }
        return (Children) wrapper;
    }

    public static <T, Children extends AbstractWrapper<T, String, Children>> void addCondition(@Nonnull AbstractWrapper<T, String, Children> wrapper, @Nonnull SqlSymbol sqlSymbol, @Nonnull String column, @Nullable Object value, I18n i18n) {
        if (Objects.isNull(i18n)) {
            sqlSymbol.getValue().accept(wrapper, column, value);
        } else {
            addI18nCondition(wrapper, sqlSymbol, column, value, i18n.value());
        }

    }

    public static <T, Children extends AbstractWrapper<T, String, Children>> void addI18nCondition(@Nonnull AbstractWrapper<T, String, Children> wrapper, @Nonnull SqlSymbol sqlSymbol, @Nonnull String column, @Nullable Object value, @Nullable String basename) {
        QueryWrapper<Object> query = Wrappers.query();
        sqlSymbol.getValue().accept(query, column, value);
        String tableAlias = "t" + RandomUtils.nextInt(0, 10000);
        QueryWrapper<Object> i18nWrapper = Wrappers.query();
        sqlSymbol.getValue().accept(i18nWrapper, String.format("%s.translation", tableAlias), value);
        ((QueryWrapper) ((QueryWrapper) i18nWrapper.eq(StringUtils.isNotBlank(basename), String.format("%s.basename", tableAlias), basename)).eq(String.format("%s.locale", tableAlias), LocaleContextHolder.getLocale().toLanguageTag())).apply(String.format("%s.code = REPLACE(REPLACE(%s, '{', ''), '}', '')", tableAlias, column), new Object[0]);
        String logicDeleteField = String.format("%s.is_deleted", tableAlias);
        if ("null".equalsIgnoreCase(logicNotDeleteValue)) {
            i18nWrapper.isNull(logicDeleteField);
        } else if (NumberUtils.isCreatable(logicNotDeleteValue)) {
            i18nWrapper.eq(logicDeleteField, NumberUtils.createNumber(logicNotDeleteValue));
        } else {
            i18nWrapper.eq(logicDeleteField, logicNotDeleteValue);
        }

        wrapper.getSqlSegment();
        getParamNameSeq(query).set(getParamNameSeq(wrapper).get());
        String querySqlSegment = query.getSqlSegment();
        getParamNameSeq(i18nWrapper).set(getParamNameSeq(query).get());
        String i18nWrapperSqlSegment = i18nWrapper.getSqlSegment();
        getParamNameSeq(wrapper).set(getParamNameSeq(i18nWrapper).get());
        wrapper.getParamNameValuePairs().putAll(query.getParamNameValuePairs());
        wrapper.getParamNameValuePairs().putAll(i18nWrapper.getParamNameValuePairs());
        wrapper.and((and) -> {
            AbstractWrapper var10000 = (AbstractWrapper) ((AbstractWrapper) ((AbstractWrapper) and.apply(querySqlSegment, new Object[0])).or()).exists(String.format("SELECT id FROM system_i18n %s WHERE %s", tableAlias, i18nWrapperSqlSegment), new Object[0]);
        });
    }

    private static <A extends Annotation> Optional<A> getFieldAnnotation(Class<?> javaBeanType, Method readMethod, Method writeMethod, Class<A> annotationType) {
        A annotation = AnnotationUtils.findAnnotation(readMethod, annotationType);
        if (Objects.isNull(annotation)) {
            if (Objects.nonNull(writeMethod)) {
                annotation = AnnotationUtils.findAnnotation(writeMethod, annotationType);
            }

            if (Objects.isNull(annotation)) {
                String propertyName = PropertyNamer.methodToProperty(readMethod.getName());
                Field field = ReflectionUtils.findField(javaBeanType, propertyName);
                if (Objects.nonNull(field)) {
                    annotation = AnnotationUtils.findAnnotation(field, annotationType);
                }
            }
        }

        return Optional.ofNullable(annotation);
    }

    private static boolean validFieldStrategy(@Nonnull FieldStrategy fieldStrategy, @Nullable Object value) {
        switch (fieldStrategy) {
            case IGNORED:
                return true;
            case NOT_EMPTY:
                return StringUtils.checkValNotNull(value);
            case NEVER:
                return false;
            case NOT_NULL:
            default:
                return Objects.nonNull(value);
        }
    }

    private static String[] getColumn(String[] columns) {
        return ArrayUtils.isEmpty(columns) ? null : Stream.of(columns)
                .map((item) -> getColumn(item, true))
                .filter(StringUtils::isNotBlank).toArray(String[]::new);
    }

    private static String getColumn(TableField tableField) {
        return getColumn(tableField.value(), tableField.keepGlobalFormat());
    }

    private static String getColumn(String column, boolean keepGlobalFormat) {
        if (StringUtils.isBlank(column)) {
            return null;
        } else {
            String columnName = column;
            if (mapUnderscoreToCamelCase) {
                columnName = StrUtil.toUnderlineCase(column);
            }

            if (capitalMode) {
                columnName = columnName.toUpperCase();
            }

            if (StringUtils.isNotBlank(columnFormat) && keepGlobalFormat) {
                columnName = String.format(columnFormat, columnName);
            }

            return columnName;
        }
    }

    private static AtomicInteger getParamNameSeq(AbstractWrapper<?, ?, ?> wrapper) {
        if (Objects.isNull(PARAM_NAME_SEQ_FIELD)) {
            return new AtomicInteger(0);
        } else {
            AtomicInteger paramNameSeq = (AtomicInteger) ReflectionUtils.getField(PARAM_NAME_SEQ_FIELD, wrapper);
            return Objects.isNull(paramNameSeq) ? new AtomicInteger(0) : paramNameSeq;
        }
    }

    public static FieldStrategy getWhereStrategy() {
        return whereStrategy;
    }

    public static void setWhereStrategy(FieldStrategy whereStrategy) {
        if (Objects.nonNull(whereStrategy)) {
            WrapperUtils.whereStrategy = whereStrategy;
        }

    }

    public static boolean isMapUnderscoreToCamelCase() {
        return mapUnderscoreToCamelCase;
    }

    public static void setMapUnderscoreToCamelCase(boolean mapUnderscoreToCamelCase) {
        WrapperUtils.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
    }

    public static boolean isCapitalMode() {
        return capitalMode;
    }

    public static void setCapitalMode(boolean capitalMode) {
        WrapperUtils.capitalMode = capitalMode;
    }

    public static String getColumnFormat() {
        return columnFormat;
    }

    public static void setColumnFormat(String columnFormat) {
        WrapperUtils.columnFormat = columnFormat;
    }

    public static String getLogicNotDeleteValue() {
        return logicNotDeleteValue;
    }

    public static void setLogicNotDeleteValue(String logicNotDeleteValue) {
        if (StringUtils.isNotBlank(logicNotDeleteValue)) {
            WrapperUtils.logicNotDeleteValue = logicNotDeleteValue;
        }

    }

    public static <T, R, Children extends AbstractWrapper<T, R, Children>> AbstractWrapper<T, R, Children> toAbstractWrapper(@Nonnull Wrapper<T> wrapper) {
        return wrapper instanceof AbstractChainWrapper ? ((AbstractChainWrapper) wrapper).getWrapper() : (AbstractWrapper) wrapper;
    }

    static {
        whereStrategy = FieldStrategy.NOT_EMPTY;
        mapUnderscoreToCamelCase = true;
        capitalMode = false;
        logicNotDeleteValue = "0";
        PARAM_NAME_SEQ_FIELD = ReflectionUtils.findField(AbstractWrapper.class, "paramNameSeq", AtomicInteger.class);
        if (Objects.nonNull(PARAM_NAME_SEQ_FIELD)) {
            ReflectionUtils.makeAccessible(PARAM_NAME_SEQ_FIELD);
        }

    }
}
