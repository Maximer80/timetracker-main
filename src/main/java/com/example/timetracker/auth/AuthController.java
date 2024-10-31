package com.example.timetracker.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.Base64;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user, @RequestHeader("Authorization") String authHeader) {
        // Извлекаем логин и пароль из заголовка Authorization
        String[] authParts = new String(Base64.getDecoder().decode(authHeader.substring(6))).split(":");
        String username = authParts[0];
        String password = authParts[1];

        // Проверяем аутентификацию текущего пользователя
        boolean isAuthenticated = userService.authenticateUser(username, password);
        
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Получаем роль текущего пользователя
        Optional<User> currentUserOptional = userService.findByUsername(username);
        String currentUserRole = currentUserOptional.map(User::getRole).orElse("user");

        User savedUser = userService.registerUser(user, currentUserRole);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginRequest loginRequest) {
        boolean isAuthenticated = userService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());
        
        if (isAuthenticated) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверные учётные данные пользователя");
        }
    }
}

// Класс для передачи данных при логине
class UserLoginRequest {
    private String username;
    private String password;

    // Геттеры и сеттеры
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
