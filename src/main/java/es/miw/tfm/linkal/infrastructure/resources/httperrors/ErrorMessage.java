package es.miw.tfm.linkal.infrastructure.resources.httperrors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessage {
    private String error;
    private String message;
    private int code;

    public ErrorMessage(Exception ex, int code) {
        this.error = ex.getClass().getSimpleName();
        this.message = ex.getMessage();
        this.code = code;
    }
}