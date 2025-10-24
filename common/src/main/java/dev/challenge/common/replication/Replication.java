// dev.challenge.common.replication.Replication
package dev.challenge.common.replication;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class Replication {
    public static final String HEADER = "X-Replicated";
    private Replication() {}

    public static boolean incoming() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        if (!(ra instanceof ServletRequestAttributes attrs)) return false;
        HttpServletRequest req = attrs.getRequest();
        String h = req.getHeader(HEADER);
        return h != null && !"false".equalsIgnoreCase(h);
    }
}
