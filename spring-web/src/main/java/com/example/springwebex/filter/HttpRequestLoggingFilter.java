package com.example.springwebex.filter;

import java.io.IOException;
import org.springframework.stereotype.Component;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HttpRequestLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        /*-
         * this context run before controller processing
         */
        if (request instanceof HttpServletRequest) {
            log.info("### Incoming request");
            log.info("Url=" + ((HttpServletRequest) request).getRequestURL().toString());
            log.info("QueryString=" + ((HttpServletRequest) request).getQueryString());
            log.info("Attributes=");
            request.getAttributeNames().asIterator().forEachRemaining(e -> log.info("- "+e+": "+request.getAttribute(e)));
            log.info("Headers=");
            ((HttpServletRequest) request).getHeaderNames().asIterator().forEachRemaining(e -> log.info("- "+e+": "+((HttpServletRequest) request).getHeader(e)));
        }

        // // "X-TEST-HEADER" will be added by TestServletInterceptor.postHandle()
        // if (response instanceof HttpServletResponse) {
        //     System.out.println("filter::response::modified-header="
        //             + ((HttpServletResponse) response).getHeader("X-TEST-HEADER"));
        // }

        /*-
         * processing next filter chain and spring controller
         */
        chain.doFilter(request, response);

        /*-
        * this context run after controller processing
        */
        // if (response instanceof HttpServletResponse) {
        // int code = ((HttpServletResponse) response).getStatus();
        // System.out.println("filter::response::code=" + code);

        // // "X-TEST-HEADER" should be added by TestServletInterceptor.postHandle()
        // System.out.println("filter::response::modified-header="
        //         + ((HttpServletResponse) response).getHeader("X-TEST-HEADER"));
        // }
    }

    @Override
    public void destroy() {
        // do nothing
    }

}


