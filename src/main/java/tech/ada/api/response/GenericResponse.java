package tech.ada.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericResponse {
    private Integer status;
    private String message;
    private Object data;

    public GenericResponse(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

}
