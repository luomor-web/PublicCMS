package com.publiccms.common.constants;

import java.security.SecureRandom;
import java.util.Random;
import java.util.function.BinaryOperator;

import org.apache.http.client.config.RequestConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 *
 * Constants
 * 
 */
public abstract class Constants {
    private Constants() {
    }

    /**
     * Json Mapper
     */
    public static final ObjectMapper objectMapper = JsonMapper.builder().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false).build();

    /**
     * Default Request Config
     */
    public static final RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000)
            .setConnectionRequestTimeout(5000).build();
    /**
     * 随机数
     * 
     * Random
     */
    public static final Random random = new SecureRandom();
    /**
     * 间隔符
     * 
     * separator
     */
    public static final String SEPARATOR = "/";
    /**
     * 空白字符串
     * 
     * blank
     */
    public static final String BLANK = "";
    /**
     * 点
     * 
     * dot
     */
    public static final String DOT = ".";
    /**
     * 冒号
     * 
     * COLON
     */
    public static final String COLON = ":";
    /**
     * 下划线
     * 
     * underline
     */
    public static final String UNDERLINE = "_";
    /**
     * 空格
     * 
     * blank space
     */
    public static final String BLANK_SPACE = " ";
    /**
     * 逗号分隔符
     * 
     * comma delimited
     */
    public static final String COMMA_DELIMITED = ",";
    /**
     * 逗号分隔符
     * 
     * comma delimited
     */
    public static final char COMMA = ',';
    /**
     * 空字节数组
     * 
     * comma delimited
     */
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * @return deafult meger function
     */
    public static <T> BinaryOperator<T> defaultMegerFunction() {
        return (first, second) -> first;
    }
}