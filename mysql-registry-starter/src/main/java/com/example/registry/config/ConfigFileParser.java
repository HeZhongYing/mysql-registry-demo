package com.example.registry.config;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.env.PropertySource;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 配置文件解析：把 yaml / properties 文件内容解析为扁平的键值对。
 */
public final class ConfigFileParser {

    private ConfigFileParser() {
    }

    /**
     * 按文件格式解析内容为配置键值对，非法格式返回空 Map。
     */
    public static Map<String, Object> parse(String fileName, String format, String content) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (content == null || content.isBlank()) {
            return result;
        }
        try {
            Resource resource = new ByteArrayResource(content.getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };
            if ("properties".equalsIgnoreCase(format)) {
                Properties properties = new Properties();
                properties.load(new StringReader(content));
                properties.forEach((key, value) -> result.put(String.valueOf(key), value));
            } else {
                List<PropertySource<?>> sources = new YamlPropertySourceLoader().load("mysql-config", resource);
                for (PropertySource<?> source : sources) {
                    if (source.getSource() instanceof Map<?, ?> map) {
                        map.forEach((key, value) -> result.put(String.valueOf(key), value));
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("配置文件解析失败: " + fileName, e);
        }
        return result;
    }

}
