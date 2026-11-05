package br.com.fiap.postech.msorcamentos.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MdcRequestFilterTest {

    private final MdcRequestFilter filter = new MdcRequestFilter();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void propagatesCorrelationIdAndServiceOrderId() throws Exception {
        var request = new MockHttpServletRequest("POST", "/service-orders/42/budget/APPROVE");
        request.addHeader("X-Correlation-Id", "corr-123");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);
        doAnswer(invocation -> {
            assertThat(MDC.get("correlation_id")).isEqualTo("corr-123");
            assertThat(MDC.get("so_id")).isEqualTo("42");
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader("X-Correlation-Id")).isEqualTo("corr-123");
    }

    @Test
    void generatesCorrelationIdWhenHeaderIsAbsent() throws Exception {
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);
        doAnswer(invocation -> {
            assertThat(MDC.get("correlation_id")).isNotBlank();
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(new MockHttpServletRequest(), response, chain);

        assertThat(response.getHeader("X-Correlation-Id")).isNotBlank();
    }

    @Test
    void propagatesBranchIdFromRequestAttribute() throws Exception {
        var request = new MockHttpServletRequest();
        request.setAttribute("branchId", 7L);
        var chain = mock(FilterChain.class);
        doAnswer(invocation -> {
            assertThat(MDC.get("branch_id")).isEqualTo("7");
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(MDC.get("branch_id")).isNull();
    }

    @Test
    void clearsMdcWhenFilterChainThrows() {
        var chain = mock(FilterChain.class);
        try {
            doThrow(new ServletException("boom")).when(chain).doFilter(any(), any());
        } catch (Exception ignored) {
            throw new IllegalStateException(ignored);
        }

        assertThatThrownBy(() -> filter.doFilter(
                new MockHttpServletRequest(), new MockHttpServletResponse(), chain))
                .isInstanceOf(ServletException.class);

        assertThat(MDC.get("correlation_id")).isNull();
        assertThat(MDC.get("so_id")).isNull();
        assertThat(MDC.get("branch_id")).isNull();
    }
}
