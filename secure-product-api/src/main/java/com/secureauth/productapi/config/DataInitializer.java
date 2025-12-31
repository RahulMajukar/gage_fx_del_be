package com.secureauth.productapi.config;

import com.secureauth.productapi.entity.*;
import com.secureauth.productapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

        private final PasswordEncoder passwordEncoder;

        @Bean
        CommandLineRunner initData(
                        UserRepository userRepository,
                        RoleRepository roleRepository,
                        DepartmentRepository departmentRepository,
                        FunctionRepository functionRepository,
                        OperationRepository operationRepository,
                        TransactionTemplate transactionTemplate) {
                return args -> {
                        // --- Load and convert profile images to Base64 ---
                        String[] profileImagesBase64 = loadProfileImagesAsBase64();

                        transactionTemplate.execute((TransactionCallback<Void>) status -> {
                                // --- Create roles ---
                                Role adminRole = createRoleIfNotExists(roleRepository, "ROLE_ADMIN");
                                Role userRole = createRoleIfNotExists(roleRepository, "ROLE_USER");
                                Role managerRole = createRoleIfNotExists(roleRepository, "ROLE_MANAGER");
                                Role operatorRole = createRoleIfNotExists(roleRepository, "ROLE_OPERATOR");
                                Role cribManagerRole = createRoleIfNotExists(roleRepository, "ROLE_CRIB_MANAGER");
                                Role storeCalibrationRole = createRoleIfNotExists(roleRepository,
                                                "ROLE_CALIBRATION_MANAGER");
                                Role labTechnicianRole = createRoleIfNotExists(roleRepository, "ROLE_LAB_TECHNICIAN");
                                Role itAdminRole = createRoleIfNotExists(roleRepository, "IT_ADMIN");
                                Role plantHod = createRoleIfNotExists(roleRepository, "PLANT_HOD");

                                // --- Create departments ---
                                Department engineering = createDepartmentIfNotExists(departmentRepository,
                                                "Engineering");
                                Department tooling = createDepartmentIfNotExists(departmentRepository, "Tooling");
                                Department machining = createDepartmentIfNotExists(departmentRepository, "Machining");
                                Department quality = createDepartmentIfNotExists(departmentRepository, "Quality");

                                // --- Create functions ---
                                Function funcF1 = createFunctionIfNotExists(functionRepository, "f1");
                                Function funcF2 = createFunctionIfNotExists(functionRepository, "f2");
                                Function funcF3 = createFunctionIfNotExists(functionRepository, "f3");

                                // --- Create operations ---
                                Operation opOT1 = createOperationIfNotExists(operationRepository, "ot1");
                                Operation opOT2 = createOperationIfNotExists(operationRepository, "ot2");
                                Operation opOT3 = createOperationIfNotExists(operationRepository, "ot3");

                                // --- Create users ---
                                createUserIfNotExists(userRepository, "itadmin.system", "user123", Set.of(itAdminRole),
                                                null, null, null,
                                                profileImagesBase64[0], "Soumik", "Mukherjee", "+91", "9876543210",
                                                "soumik@swajyot.co.in", User.Location.KOLKATA, User.Plant.PLANT_A,
                                                User.Area.SOUTH);

                                createUserIfNotExists(userRepository, "admin.system", "user123", Set.of(adminRole),
                                                null, null, null,
                                                profileImagesBase64[1], "System", "Admin", "+91", "9876543211",
                                                "admin@swajyot.co.in", User.Location.MUMBAI, User.Plant.PLANT_B,
                                                User.Area.WEST);

                                createUserIfNotExists(userRepository, "planthod.system", "user123", Set.of(plantHod),
                                                null, null, null,
                                                profileImagesBase64[0], "Jui", "Dey", "+91", "9876543212",
                                                "jui.dey@swajyot.co.in", User.Location.KOLKATA, User.Plant.PLANT_C,
                                                User.Area.EAST);

                                createUserIfNotExists(userRepository, "calibartion.system", "user123",
                                                Set.of(storeCalibrationRole), null, null, null,
                                                profileImagesBase64[2], "Nandini", "Manager", "+91", "9876543213",
                                                "nandini@swajyot.co.in", User.Location.CHENNAI, User.Plant.PLANT_A,
                                                User.Area.SOUTH);

                                createUserIfNotExists(userRepository, "lab.system", "user123",
                                                Set.of(labTechnicianRole), null, null, null,
                                                profileImagesBase64[2], "lab", "Technician", "+91", "9876543213",
                                                "lab@swajyot.co.in", User.Location.CHENNAI, User.Plant.PLANT_A,
                                                User.Area.SOUTH);

                                createUserIfNotExists(userRepository, "user.system", "user123", Set.of(userRole),
                                                Set.of(engineering), Set.of(funcF1, funcF2, funcF3),
                                                Set.of(opOT1, opOT2),
                                                profileImagesBase64[3], "Nootan", "Sharma", "+91", "9012345678",
                                                "nootan.sharma@swajyot.co.in", User.Location.BENGALURU,
                                                User.Plant.PLANT_A, User.Area.SOUTH);

                                createUserIfNotExists(userRepository, "manager.system", "user123", Set.of(managerRole),
                                                Set.of(tooling, engineering), Set.of(funcF1, funcF2, funcF3),
                                                Set.of(opOT1, opOT2, opOT3),
                                                profileImagesBase64[4], "Soumik", "Ghosh", "+91", "9012345679",
                                                "soumik.ghosh@swajyot.co.in", User.Location.KOLKATA, User.Plant.PLANT_C,
                                                User.Area.EAST);

                                createUserIfNotExists(userRepository, "crib.system", "user123", Set.of(cribManagerRole),
                                                Set.of(machining), Set.of(funcF1, funcF2), Set.of(opOT2, opOT3),
                                                profileImagesBase64[5], "Sanket", "Patil", "+91", "9012345680",
                                                "sanket.patil@swajyot.co.in", User.Location.MUMBAI, User.Plant.PLANT_B,
                                                User.Area.WEST);

                                createUserIfNotExists(userRepository, "operator.system", "user123",
                                                Set.of(operatorRole), Set.of(quality), Set.of(funcF1), Set.of(opOT1),
                                                profileImagesBase64[6], "Sanket", "D", "+91", "9012345681",
                                                "sanket.d@swajyot.co.in", User.Location.HYDERABAD, User.Plant.PLANT_A,
                                                User.Area.SOUTH);

                                createUserIfNotExists(userRepository, "operator2.system", "user123",
                                                Set.of(operatorRole), Set.of(tooling, engineering),
                                                Set.of(funcF1, funcF3), Set.of(opOT1, opOT3),
                                                profileImagesBase64[7], "Arjun", "Reddy", "+91", "9012345682",
                                                "arjun.reddy@swajyot.co.in", User.Location.CHENNAI, User.Plant.PLANT_C,
                                                User.Area.SOUTH);

                                createUserIfNotExists(userRepository, "user2.system", "user123", Set.of(userRole),
                                                Set.of(machining), Set.of(funcF2, funcF3), Set.of(opOT2, opOT3),
                                                profileImagesBase64[0], "Divya", "Iyer", "+91", "9012345683",
                                                "divya.iyer@swajyot.co.in", User.Location.BENGALURU, User.Plant.PLANT_B,
                                                User.Area.SOUTH);

                                createUserIfNotExists(userRepository, "manager2.system", "user123", Set.of(managerRole),
                                                Set.of(tooling, machining), Set.of(funcF1, funcF2),
                                                Set.of(opOT1, opOT2),
                                                profileImagesBase64[1], "Rahul", "Kumar", "+91", "9012345684",
                                                "rahul.kumar@swajyot.co.in", User.Location.MUMBAI, User.Plant.PLANT_A,
                                                User.Area.WEST);

                                createUserIfNotExists(userRepository, "cribmanager2.system", "user123",
                                                Set.of(cribManagerRole), Set.of(quality, machining),
                                                Set.of(funcF2, funcF3), Set.of(opOT2, opOT3, opOT1),
                                                profileImagesBase64[2], "Neha", "Singh", "+91", "9012345685",
                                                "neha.singh@swajyot.co.in", User.Location.KOLKATA, User.Plant.PLANT_C,
                                                User.Area.EAST);

                                createUserIfNotExists(userRepository, "operator3.system", "user123",
                                                Set.of(operatorRole), Set.of(engineering), Set.of(funcF2),
                                                Set.of(opOT2),
                                                profileImagesBase64[3], "Vikram", "Malhotra", "+91", "9012345686",
                                                "vikram.malhotra@swajyot.co.in", User.Location.HYDERABAD,
                                                User.Plant.PLANT_B, User.Area.SOUTH);

                                createUserIfNotExists(userRepository, "operator4.system", "user123",
                                                Set.of(operatorRole), Set.of(engineering, machining, tooling),
                                                Set.of(funcF1, funcF2, funcF3), Set.of(opOT1, opOT2, opOT3),
                                                profileImagesBase64[4], "Nootan", "Swajyot", "+91", "9012345687",
                                                "nootan.swajyot@swajyot.co.in", User.Location.BENGALURU,
                                                User.Plant.PLANT_A, User.Area.SOUTH);

                                createUserIfNotExists(userRepository, "user3.system", "user123", Set.of(userRole),
                                                Set.of(engineering, tooling), Set.of(funcF1, funcF3),
                                                Set.of(opOT1, opOT3),
                                                profileImagesBase64[5], "Aditya", "Joshi", "+91", "9012345688",
                                                "aditya.joshi@swajyot.co.in", User.Location.MUMBAI, User.Plant.PLANT_B,
                                                User.Area.WEST);

                                createUserIfNotExists(userRepository, "manager3.system", "user123", Set.of(managerRole),
                                                Set.of(machining), Set.of(funcF2, funcF3), Set.of(opOT2, opOT3),
                                                profileImagesBase64[6], "Ananya", "Rao", "+91", "9012345689",
                                                "ananya.rao@swajyot.co.in", User.Location.CHENNAI, User.Plant.PLANT_C,
                                                User.Area.SOUTH);

                                createUserIfNotExists(userRepository, "cribmanager3.system", "user123",
                                                Set.of(cribManagerRole), Set.of(quality, engineering),
                                                Set.of(funcF1, funcF3), Set.of(opOT1, opOT2),
                                                profileImagesBase64[7], "Karan", "Shah", "+91", "9012345690",
                                                "karan.shah@swajyot.co.in", User.Location.BENGALURU, User.Plant.PLANT_A,
                                                User.Area.SOUTH);

                                createUserIfNotExists(userRepository, "operator5.system", "user123",
                                                Set.of(operatorRole), Set.of(engineering), Set.of(funcF2),
                                                Set.of(opOT2),
                                                profileImagesBase64[0], "Riya", "Chopra", "+91", "9012345691",
                                                "riya.chopra@swajyot.co.in", User.Location.MUMBAI, User.Plant.PLANT_B,
                                                User.Area.WEST);

                                createUserIfNotExists(userRepository, "operatorF1OT1.system", "user123",
                                                Set.of(operatorRole), Set.of(engineering), null, Set.of(opOT1),
                                                profileImagesBase64[1], "Tanvi", "Agarwal", "+91", "9012345692",
                                                "tanvi.agarwal@swajyot.co.in", User.Location.KOLKATA,
                                                User.Plant.PLANT_C, User.Area.EAST);

                                System.out.println("✅ Data initialization completed successfully.");
                                return null;
                        });
                };
        }

        private String[] loadProfileImagesAsBase64() {
                String[] base64Images = new String[8];
                try {
                        for (int i = 0; i < 8; i++) {
                                String filename = "pf" + i + ".jpg";
                                Path imagePath = ResourceUtils.getFile("classpath:static/profile-photos/" + filename)
                                                .toPath();
                                byte[] imageBytes = Files.readAllBytes(imagePath);
                                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                                base64Images[i] = "data:image/jpeg;base64," + base64Image;
                                System.out.println("✅ Loaded profile image: " + filename);
                        }
                } catch (IOException e) {
                        System.err.println("❌ Failed to load profile images: " + e.getMessage());
                        for (int i = 0; i < 8; i++) {
                                base64Images[i] = createPlaceholderBase64Image();
                        }
                }
                return base64Images;
        }

        private String createPlaceholderBase64Image() {
                return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=";
        }

        private Role createRoleIfNotExists(RoleRepository repo, String name) {
                return repo.findByName(name).orElseGet(() -> repo.save(Role.builder().name(name).build()));
        }

        private Department createDepartmentIfNotExists(DepartmentRepository repo, String name) {
                return repo.findByName(name).orElseGet(() -> repo.save(Department.builder().name(name).build()));
        }

        private Function createFunctionIfNotExists(FunctionRepository repo, String name) {
                return repo.findByName(name).orElseGet(() -> repo.save(Function.builder().name(name).build()));
        }

        private Operation createOperationIfNotExists(OperationRepository repo, String name) {
                return repo.findByName(name).orElseGet(() -> repo.save(Operation.builder().name(name).build()));
        }

        private void createUserIfNotExists(
                        UserRepository repo,
                        String username,
                        String password,
                        Set<Role> roles,
                        Set<Department> departments,
                        Set<Function> functions,
                        Set<Operation> operations,
                        String profileImage,
                        String firstName,
                        String lastName,
                        String countryCode,
                        String phone,
                        String email,
                        User.Location location,
                        User.Plant plant,
                        User.Area area) {
                // Check both username and email to avoid duplicates
                Optional<User> existingByUsername = repo.findByUsername(username);
                Optional<User> existingByEmail = repo.findByEmail(email);

                if (existingByUsername.isPresent() || existingByEmail.isPresent()) {
                        System.out.println("⏭️ User already exists - Username: " + username + " or Email: " + email);
                        return;
                }

                try {
                        User user = User.builder()
                                        .username(username)
                                        .email(email)
                                        .password(passwordEncoder.encode(password))
                                        .firstName(firstName)
                                        .lastName(lastName)
                                        .countryCode(countryCode)
                                        .phone(phone)
                                        .location(location)
                                        .plant(plant)
                                        .area(area)
                                        .roles(roles)
                                        .departments(departments)
                                        .functions(functions)
                                        .operations(operations)
                                        .profileImage(profileImage)
                                        .isActive(true)
                                        .build();
                        repo.save(user);
                        System.out.println("✅ Created user: " + username + " | Email: " + email);
                } catch (Exception e) {
                        System.err.println("❌ Failed to create user: " + username + " | Error: " + e.getMessage());
                        // Don't throw the exception, just log it and continue
                }
        }
}