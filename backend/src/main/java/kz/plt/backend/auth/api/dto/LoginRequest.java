package kz.plt.backend.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный формат email")
        @Size(max = 100, message = "Email не должен превышать 100 символов")
        String email,

        @NotBlank(message = "Пароль обязателен")
        @Size(max = 60, message = "Пароль не должен превышать 60 символов")
        String password
) {
}
