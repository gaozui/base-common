package cn.com.gpic.ini.common.util.wrapper.condition.interfaces;

import java.io.Serializable;

public interface BaseLabelEnum<T extends Serializable> {
    T getLabel();
}
