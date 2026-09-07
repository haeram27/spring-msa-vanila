package com.example.springwebex.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import com.example.springwebex.exception.ecode.ErrorCode;
import com.example.springwebex.model.restresp.ResponseJsonDto;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ExceptionResolverFilter implements Filter {
    private static final String INTERNAL_ERROR_RESPONSE;

    static {
        ResponseJsonDto<?> responseObject = ResponseJsonDto.from(
                String.valueOf(ErrorCode.UNKNOWN.getCode()),
                ErrorCode.UNKNOWN.getDefaultMessage());

        try {
            INTERNAL_ERROR_RESPONSE = responseObject.toString();
        } catch (Exception e) {
            log.error("failed to initalize class member.", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error(e.getMessage());
            response.getOutputStream()
                    .write(INTERNAL_ERROR_RESPONSE.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public void destroy() {
        // do nothing
    }

}
