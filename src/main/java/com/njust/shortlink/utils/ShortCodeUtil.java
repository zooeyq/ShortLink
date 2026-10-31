package com.njust.shortlink.utils;



import java.security.SecureRandom;

/**
 * 生成随机短码；唯一性由数据库唯一索引保证，冲突重试由 Service 处理。
 */
public final class ShortCodeUtil {

    // 字符集
    private static final String CHARACTERS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    // 长度
    private static final int LENGTH = 7;
    // 随机数生成器
    private static final SecureRandom RANDOM = new SecureRandom();

    private ShortCodeUtil() {
    }

    public static String generate() {
        StringBuilder code = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }
}
