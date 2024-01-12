package cn.com.gpic.ini.common.util.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * 本地缓存管理
 */
public class LocalCache {

    /**
     * 通用配置缓存
     */
    public static final Map<String, String> config = new HashMap<>();

    private LocalCache() {// 私有化构造方法-单例
    }
}
