package com.redbus.bootstrap;

import com.redbus.model.User;
import com.redbus.adminmodel.Admin;
import com.redbus.repository.UserRepository;
import com.redbus.adminrepository.AdminRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class CreateAdminRunner implements CommandLineRunner {

    @Value("${app.create-admin:false}")
    private boolean createAdmin;

    private final UserRepository userRepo;
    private final AdminRepository adminRepo;

    public CreateAdminRunner(UserRepository userRepo, AdminRepository adminRepo) {
        this.userRepo = userRepo;
        this.adminRepo = adminRepo;
    }

    @Override
    public void run(String... args) {
        if (!createAdmin) {
            return;  // disabled unless enabled in properties
        }

        String email = "inamdarsahil708@gmail.com";     // admin login email
        String password = "Sahil@2410";          // admin password

        // check if already exists
        if (userRepo.findByEmail(email).isPresent()) {
            System.out.println("Admin user already exists. Skipping...");
            return;
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 1. Create User for Admin
        User user = new User();
        user.setName("Super Admin");
        user.setEmail(email);
        user.setPassword(encoder.encode(password));  // bcrypt hash
        user.setPhoneNumber("9890656246");
        user.setGender("Other");
        user.setCity("N/A");
        user.setState("N/A");

        User savedUser = userRepo.save(user);

        // 2. Create Admin linked to this User
        Admin admin = Admin.builder()
                .user(savedUser)
                .displayName("Super Admin")
                .phone("9890656246")
                .build();

        adminRepo.save(admin);

        System.out.println("=======================================");
        System.out.println("  🎉   ADMIN CREATED SUCCESSFULLY");
        System.out.println("  Email: " + email);
        System.out.println("  Password: " + password);
        System.out.println("=======================================");
    }
}
