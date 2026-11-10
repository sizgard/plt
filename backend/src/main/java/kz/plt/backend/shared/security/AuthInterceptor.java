package kz.plt.backend.shared.security;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.plt.backend.shared.error.ForbiddenException;
import kz.plt.backend.shared.error.UnauthorizedException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final TokenVerifier tokenVerifier;

    public AuthInterceptor(TokenVerifier tokenVerifier){
        this.tokenVerifier = tokenVerifier;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        if (!(handler instanceof HandlerMethod handlerMethod)){
            return true;
        }
        Optional<AuthenticatedUser> userOpt = tokenVerifier.verify(request);
        userOpt.ifPresent(user -> request.setAttribute(CurrentUserArgumentResolver.CURRENT_USER_ATTRIBUTE, user));

        RequireRole requireRole = getRequireRoleAnnotation(handlerMethod);
        if (requireRole != null) {
            if (userOpt.isEmpty()) {
                throw new UnauthorizedException("Требуется авторизация");
            }
            AuthenticatedUser user = userOpt.get();
            if (!user.hasRole(requireRole.value())) {
                throw new ForbiddenException("Недостаточно прав для выполнения действия");
            }
        }

        boolean requiresCurrentUSer = Arrays.stream(handlerMethod.getMethodParameters())
                .anyMatch(param -> param.hasParameterAnnotation(CurrentUser.class));
        if (requiresCurrentUSer && userOpt.isEmpty()) {
            throw new UnauthorizedException("Требуется авторизация");
        }
        return true;
    }

    public RequireRole getRequireRoleAnnotation(HandlerMethod handlerMethod){
        RequireRole annotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        if(annotation == null){
            annotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        return annotation;
    }
}