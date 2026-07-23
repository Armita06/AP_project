package controller;

import model.User;
import service.UserService;
import security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            if (!credentials.containsKey("username") || !credentials.containsKey("password")) {
                throw new RuntimeException("نام کاربری و رمز عبور الزامی است!");
            }

            String username = credentials.get("username");
            String password = credentials.get("password");
            User user = userService.loginUser(username, password);

            String token = JwtUtil.generateToken(username);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("role", user.getRole());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);

            String username = JwtUtil.extractUsername(jwt);

            User user = userService.getUserByUsername(username);

            user.setPassword(null);

            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String token, @RequestBody User updatedUser) {
        try {
            String jwt = token.substring(7);
            String username = JwtUtil.extractUsername(jwt);

            User user = userService.updateUserProfile(username, updatedUser);
            user.setPassword(null);

            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}