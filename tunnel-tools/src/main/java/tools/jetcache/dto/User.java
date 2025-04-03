package tools.jetcache.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String password;
    public static User generic(String name) {
        User user = new User();
        user.setName(name);
        user.setPassword(UUID.randomUUID().toString());
        return user;
    }
}