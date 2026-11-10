package kz.plt.backend.auth.domain.exception;

import kz.plt.backend.shared.error.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException {
    public InvalidCredentialsException(){
        super("Неверный email или пароль");
    }
}
