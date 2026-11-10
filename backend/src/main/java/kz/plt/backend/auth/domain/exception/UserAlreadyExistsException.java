package kz.plt.backend.auth.domain.exception;

import kz.plt.backend.shared.error.ConflictException;

public class UserAlreadyExistsException extends ConflictException {
    public UserAlreadyExistsException(String email){
        super("Пользователь с email '" + email + "' уже существует");
    }
}
