package com.arnanzz.framework.biz.context.holder;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.arnanzz.framework.common.constant.GlobalConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 登录用户上下文
 * ThreadLocal存在的问题
 *  如果 这个线程途中的代码 有异步的逻辑 那么线程上下文数据就会丢失
 * 使用 阿里巴巴 TransmittableThreadLocal 支持异步 线程池 数据共享
 **/
public class LoginUserContextHolder {

    private static final ThreadLocal<Map<String, Object>> LOGIN_USER_CONTEXT_THREAD_LOCAL =
            TransmittableThreadLocal.withInitial(HashMap::new);

    /**
     * 设置用户id
     */
    public static void setUserId(Object value) {
        LOGIN_USER_CONTEXT_THREAD_LOCAL.get().put(GlobalConstants.USER_ID, value);
    }

    /**
     * 获取用户id
     */
    public static Long getUserId() {
        Object value = LOGIN_USER_CONTEXT_THREAD_LOCAL.get().get(GlobalConstants.USER_ID);
        if (Objects.isNull(value)) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }

    /**
     * 删除该线程
     */
    public static void remove() {
        LOGIN_USER_CONTEXT_THREAD_LOCAL.remove();
    }
}
