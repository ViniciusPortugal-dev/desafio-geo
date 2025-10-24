package dev.challenge.common.replication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class ReplicationFlagFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Replicated";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        try {
            boolean replicated = "true".equalsIgnoreCase(req.getHeader(HEADER));
            System.out.println(">>> HEADER X-Replicated = " + replicated);
            ReplicationContext.mark(replicated);            chain.doFilter(req, res);
        } finally {
            ReplicationContext.clear();
        }
    }

}