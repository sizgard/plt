package kz.plt.backend.shared.error;

import org.springframework.http.HttpStatus;

public class NotFoundException extends DomainException{
    public NotFoundException(String message){
        super(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
    }
}