package service;

import jakarta.annotation.PostConstruct;
import model.User;
import repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void initAdminUser() {
        Optional<User> adminExists = userRepository.findByUsername("admin");

        if (adminExists.isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setFullName("مدیر سیستم");
            admin.setRole("ADMIN");
            admin.setStatus("ACTIVE");

            userRepository.save(admin);
            System.out.println("✅ اکانت ادمین پیش‌فرض با موفقیت ساخته شد. (نام کاربری: admin | رمز عبور: admin123)");
        }
    }

    public User registerUser(User user) {
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }

        user.setRole("USER");
        user.setStatus("ACTIVE");

        return userRepository.save(user);
    }

    public User loginUser(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && user.get().getPassword().equals(password)) {

            if ("BLOCKED".equals(user.get().getStatus())) {
                throw new RuntimeException("حساب کاربری شما توسط مدیر مسدود شده است!");
            }

            return user.get();
        }
        throw new RuntimeException("Invalid username or password!");
    }
}