package bsa.java.concurrency.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
@Order(1)
public class TransactionFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(TransactionFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(req);

        chain.doFilter(request, response);

        logger.info(getRequestStr(requestWrapper));
    }

    private String getRequestStr(ContentCachingRequestWrapper wrappedRequest) {
        StringBuilder requestStr = new StringBuilder(
                "\n\n" + wrappedRequest.getMethod() + " "
                        + wrappedRequest.getRequestURI()
                        + (wrappedRequest.getQueryString() == null
                        ? ""
                        : "?" + wrappedRequest.getQueryString())
                        + "\n"
        );

        var headerNames = wrappedRequest.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            String nextHeaderName = headerNames.nextElement();
            requestStr
                    .append(nextHeaderName)
                    .append(": ")
                    .append(wrappedRequest.getHeader(nextHeaderName))
                    .append("\n");
        }
        requestStr.append("\n");
        return requestStr.toString();
    }

}
