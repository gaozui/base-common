package cn.com.gpic.ini.common.util.wrapper.condition.domain;

import cn.com.gpic.ini.common.util.wrapper.condition.interfaces.BaseLabelEnum;
import cn.com.gpic.ini.common.util.wrapper.condition.interfaces.BaseValueEnum;
import cn.com.gpic.ini.common.util.wrapper.condition.interfaces.SqlSymbolAbstractWrapperConsumer;
import cn.com.gpic.ini.common.util.wrapper.condition.util.CollectionUtils;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.interfaces.Compare;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum SqlSymbol implements BaseValueEnum<SqlSymbolAbstractWrapperConsumer>, BaseLabelEnum<String> {
    EQ(Compare::eq, "等于"),
    NE(Compare::ne, "不等于"),
    GT(Compare::gt, "大于"),
    GE(Compare::ge, "大于等于"),
    LT(Compare::lt, "小于"),
    LE(Compare::le, "小于等于"),
    LIKE(Compare::like, "模糊匹配"),
    NOT_LIKE(Compare::notLike, "模糊不匹配"),
    LIKE_LEFT(Compare::likeLeft, "模糊匹配左侧"),
    LIKE_RIGHT(Compare::likeRight, "模糊匹配右侧"),
    BETWEEN((wrapper, column, value) -> {
        if (!Objects.isNull(value)) {
            int size = CollectionUtils.size(value);
            if (size != 0) {
                Object val1 = size >= 1 ? CollectionUtils.get(value, 0) : null;
                Object val2 = size >= 2 ? CollectionUtils.get(value, 1) : null;
                wrapper.between(column, val1, val2);
            }
        }
    }, "介于"),
    NOT_BETWEEN((wrapper, column, value) -> {
        if (!Objects.isNull(value)) {
            int size = CollectionUtils.size(value);
            if (size != 0) {
                Object val1 = size >= 1 ? CollectionUtils.get(value, 0) : null;
                Object val2 = size >= 2 ? CollectionUtils.get(value, 1) : null;
                wrapper.notBetween(column, val1, val2);
            }
        }
    }, "不介于"),
    IS_NULL((wrapper, column, value) -> {
        if (value instanceof Boolean) {
            if (BooleanUtil.isTrue((Boolean)value)) {
                wrapper.isNull(column);
            }
        } else if (value instanceof String) {
            if (BooleanUtil.toBoolean((String)value)) {
                wrapper.isNull(column);
            }
        } else {
            wrapper.isNull(column);
        }

    }, "为空"),
    IS_NOT_NULL((wrapper, column, value) -> {
        if (value instanceof Boolean) {
            if (BooleanUtil.isTrue((Boolean)value)) {
                wrapper.isNotNull(column);
            }
        } else if (value instanceof String) {
            if (BooleanUtil.toBoolean((String)value)) {
                wrapper.isNotNull(column);
            }
        } else {
            wrapper.isNotNull(column);
        }

    }, "不为空"),
    IN((wrapper, column, value) -> {
        if (!Objects.isNull(value)) {
            int size = CollectionUtils.size(value);
            if (size != 0) {
                wrapper.in(column, (Collection)IntStream.range(0, size).mapToObj((i) -> {
                    return CollectionUtils.get(value, i);
                }).collect(Collectors.toList()));
            }
        }
    }, "包含"),
    MUST_IN((wrapper, column, value) -> {
        if (!Objects.isNull(value)) {
            int size = CollectionUtils.size(value);
            if (size == 0) {
                wrapper.apply("1=0", new Object[0]);
            } else {
                wrapper.in(column, (Collection)IntStream.range(0, size).mapToObj((i) -> {
                    return CollectionUtils.get(value, i);
                }).collect(Collectors.toList()));
            }

        }
    }, "必须包含"),
    NOT_IN((wrapper, column, value) -> {
        if (!Objects.isNull(value)) {
            int size = CollectionUtils.size(value);
            if (size != 0) {
                wrapper.notIn(column, (Collection)IntStream.range(0, size).mapToObj((i) -> {
                    return CollectionUtils.get(value, i);
                }).collect(Collectors.toList()));
            }
        }
    }, "不包含"),
    EXISTS((wrapper, column, value) -> {
        if (!Objects.isNull(value)) {
            wrapper.exists(column, new Object[]{value});
        }
    }, "存在"),
    NOT_EXISTS((wrapper, column, value) -> {
        if (!Objects.isNull(value)) {
            wrapper.notExists(column, new Object[]{value});
        }
    }, "不存在");

    private final SqlSymbolAbstractWrapperConsumer value;
    private final String label;

    private SqlSymbol(SqlSymbolAbstractWrapperConsumer value, String label) {
        this.value = value;
        this.label = label;
    }

    public SqlSymbolAbstractWrapperConsumer getValue() {
        return this.value;
    }

    public String getLabel() {
        return this.label;
    }
}
