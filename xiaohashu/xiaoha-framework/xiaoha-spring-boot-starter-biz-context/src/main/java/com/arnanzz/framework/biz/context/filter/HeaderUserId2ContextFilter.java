package com.arnanzz.framework.biz.context.filter;

import com.arnanzz.framework.biz.context.holder.LoginUserContextHolder;
import com.smallfish.framework.common.constant.GlobalConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 提取请求头中的用户 ID 保存到上下文中，以方便后续使用
 * 继承自 OncePerRequestFilter，确保每个请求只会执行一次过滤操作;
 **/
@Slf4j
@Component
public class HeaderUserId2ContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader(GlobalConstants.USER_ID);
        log.info("## HeaderUserId2ContextFilter, 用户 ID: {}", userId);

        // 判断请求中是否有用户id
        if (StringUtils.isBlank(userId)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 如果不为空 设置用户id
        log.info("===== 设置 userId 到 ThreadLocal 中， 用户 ID: {}", userId);
        LoginUserContextHolder.setUserId(userId);

        // 将请求和响应传递给过滤链中的下一个过滤器。
        try {
            filterChain.doFilter(request, response);
        } finally {
            // 一定要删除 ThreadLocal ，防止内存泄露
            LoginUserContextHolder.remove();
            log.info("===== 删除 ThreadLocal， userId: {}", userId);
        }
    }
}
