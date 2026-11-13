package kz.plt.backend.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kz.plt.backend.auth.api.dto.AuthResponse;
import kz.plt.backend.auth.api.dto.LoginRequest;
import kz.plt.backend.auth.api.dto.RegisterRequest;
import kz.plt.backend.auth.domain.service.AuthService;
import kz.plt.backend.shared.security.AuthenticatedUser;
import kz.plt.backend.shared.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация", description = "Регистрация, вход, выход и проверка текущей сессии")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Регистрация нового пользователя", description = "Создает пользователя и автоматически открывает сессию")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "409", description = "Email уже занят")
    })
    public AuthResponse register(@Valid @RequestBody RegisterRequest req, @Parameter(hidden = true) HttpServletRequest request) {
        AuthenticatedUser user = authService.register(req.name(), req.email(), req.password());
        bindSession(request, user);
        return new AuthResponse(user.id(), user.email(), user.roles(), user.permissions());
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Аутентифицирует пользователя и устанавливает cookie сессии")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешная авторизация"),
            @ApiResponse(responseCode = "401", description = "Неверный логин/пароль или аккаунт заблокирован")
    })
    public AuthResponse login(@Valid @RequestBody LoginRequest req, @Parameter(hidden = true) HttpServletRequest request) {
        AuthenticatedUser user = authService.login(req.email(), req.password());
        bindSession(request, user);
        return new AuthResponse(user.id(), user.email(), user.roles(), user.permissions());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Выход из системы", description = "Инвалидирует текущую сессию пользователя")
    @ApiResponse(responseCode = "204", description = "Сессия успешно завершена")
    public void logout(@Parameter(hidden = true) HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session != null){
            session.invalidate();
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Текущий пользователь", description = "Возвращает информацию о текущем авторизованном пользователе из сессии")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные текущего пользователя"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public AuthResponse me(@Parameter(hidden = true) @CurrentUser AuthenticatedUser currentUser) {
        return new AuthResponse(currentUser.id(), currentUser.email(), currentUser.roles(), currentUser.permissions());
    }

    private void bindSession(HttpServletRequest request, AuthenticatedUser user){
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null){
            existingSession.invalidate();
        }
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(AuthenticatedUser.SESSION_KEY, user);
    }
}