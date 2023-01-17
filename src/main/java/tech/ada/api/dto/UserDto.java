package tech.ada.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.ada.api.model.User;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private String username;
    private String password;
    private String name;

    public UserDto(User user) {
        this.username = user.getUsername();
        this.name = user.getName();
    }

}
