package it.eng.knowage.boot.filter;
import static java.util.Objects.isNull;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class LoggerSetupFilter implements Filter {

	private static final Logger LOGGER = LogManager.getLogger(LoggerSetupFilter.class);

	private static final String HTTP_HEADER_NGINX_CORRELATION_ID = "X-Request-ID";
	private static final String HTTP_HEADER_X_KN_CORRELATION_ID = "X-Kn-Correlation-Id";
	private static final String HTTP_HEADER_X_FORWARDED_HOST = "X-Forwarded-Host";

	private static final String THREAD_CONTEXT_KEY_CORRELATION_ID = "correlationId";
	private static final String THREAD_CONTEXT_KEY_ENVIRONMENT = "environment";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			preDoFilterForTenant(httpRequest);
			preDoFilterForCorrelationId(httpRequest, httpResponse);

			try {
				chain.doFilter(request, response);
			} finally {

				postDoFilterForTenant();
				postDoFilterForCorrelationId();

			}
		}
	}

	private void preDoFilterForTenant(HttpServletRequest httpRequest) {
		String header = httpRequest.getHeader(HTTP_HEADER_X_FORWARDED_HOST);
		if (header != null) {
			if (header.contains(",")) {
				int iend = header.indexOf(".");
				header = header.substring(0, iend);
			}

			if (header.contains(":")) {
				header = header.substring(0, header.indexOf(":"));
			}

			if (header.contains(".")) {
				header = header.substring(0, header.indexOf("."));
			}

			ThreadContext.put(THREAD_CONTEXT_KEY_ENVIRONMENT, header);

		}
	}

	private void preDoFilterForCorrelationId(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

		String header1 = httpRequest.getHeader(HTTP_HEADER_NGINX_CORRELATION_ID);
		String header2 = httpRequest.getHeader(HTTP_HEADER_X_KN_CORRELATION_ID);
		String correlationId = null;

		correlationId = header1;
		if (isNull(correlationId)) {
			correlationId = header2;
		}
		if (isNull(correlationId)) {
			correlationId = UUID.randomUUID().toString();
		}

		LOGGER.debug("Correlation id is {}", correlationId);

		ThreadContext.put(THREAD_CONTEXT_KEY_CORRELATION_ID, correlationId);

		httpResponse.setHeader(HTTP_HEADER_X_KN_CORRELATION_ID, correlationId);
	}

	private void postDoFilterForTenant() {
		ThreadContext.remove(THREAD_CONTEXT_KEY_ENVIRONMENT);
	}

	private void postDoFilterForCorrelationId() {
		ThreadContext.remove(THREAD_CONTEXT_KEY_CORRELATION_ID);
	}

}
