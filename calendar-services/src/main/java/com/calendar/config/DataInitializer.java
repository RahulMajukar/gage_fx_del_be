//package com.calendar.config;
//
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class DataInitializer {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @PostConstruct
//    public void init() {
//        try {
//            if (!userRepository.existsByUsername("admin")) {
//                User admin = new User();
//                admin.setUsername("admin");
//                admin.setPassword("admin123"); // encode if using passwordEncoder
//                admin.setFirstName("Admin");
//                admin.setLastName("User");
//                admin.setEmail("admin@example.com");
//                admin.setRole(User.Role.ADMIN);
//                admin.setActive(true);
//
//                userRepository.save(admin);
//                System.out.println("Admin user created.");
//            }
//        } catch (Exception e) {
//            // Skip and log
//            System.out.println("Skipping user creation: " + e.getMessage());
//        }
//    }
//}
