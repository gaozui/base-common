package cn.com.gpic.ini.common.util.wrapper.condition.domain;

import cn.com.gpic.ini.common.util.wrapper.condition.interfaces.BaseLabelEnum;

public enum SqlLogical implements BaseLabelEnum<String> {
    AND("且"),
    OR("或");

    private final String label;

    private SqlLogical(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }
}
