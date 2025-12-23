package com.secureauth.productapi.controller;

import com.secureauth.productapi.dto.BulkUserResponse;
import com.secureauth.productapi.dto.UserCreateRequest;
import com.secureauth.productapi.entity.*;
import com.secureauth.productapi.repository.*;
import com.secureauth.productapi.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserBulkController {

    @Autowired
    private ExcelService excelService;

    @Autowired
    private UserController userController;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private OperationRepository operationRepository;

    @PostMapping("/bulk-upload")
    public ResponseEntity<BulkUserResponse> bulkUploadUsers(
            @RequestParam("file") MultipartFile file) {

        BulkUserResponse response = new BulkUserResponse();
        List<String> errors = new ArrayList<>();

        try {
            System.out.println("🚀 ========== BULK UPLOAD STARTED ==========");
            System.out.println("📁 File: " + file.getOriginalFilename());
            System.out.println("📊 Size: " + file.getSize() + " bytes");
            System.out.println("🔍 Type: " + file.getContentType());

            // Basic file validation
            if (file.isEmpty()) {
                String error = "File is empty";
                System.err.println("❌ " + error);
                response.setErrors(List.of(error));
                return ResponseEntity.badRequest().body(response);
            }

            if (file.getSize() == 0) {
                String error = "File is completely empty";
                System.err.println("❌ " + error);
                response.setErrors(List.of(error));
                return ResponseEntity.badRequest().body(response);
            }

            // Process file
            System.out.println("🔄 Starting file processing...");
            List<UserCreateRequest> userRequests = excelService.processExcelFile(file);

            System.out.println("📊 File processing completed. Found " + userRequests.size() + " user requests");

            if (userRequests.isEmpty()) {
                // Provide detailed error information
                StringBuilder errorBuilder = new StringBuilder();
                errorBuilder.append("No valid user records found in the file.\n\n");
                errorBuilder.append("COMMON ISSUES:\n");
                errorBuilder.append("1. File must have data rows AFTER the header row\n");
                errorBuilder.append("2. First 3 columns (username, firstName, lastName) are REQUIRED\n");
                errorBuilder.append("3. Check that your file has the correct column order\n");
                errorBuilder.append("4. Make sure there are no empty rows between data\n");
                errorBuilder.append("5. Supported formats: .xlsx, .xls, .csv\n\n");
                errorBuilder.append("COLUMN FORMAT (14 columns total):\n");
                errorBuilder.append("1. username (required)\n");
                errorBuilder.append("2. firstName (required)\n");
                errorBuilder.append("3. lastName (required)\n");
                errorBuilder.append("4. email (optional)\n");
                errorBuilder.append("5. countryCode (optional, default: +91)\n");
                errorBuilder.append("6. phone (optional)\n");
                errorBuilder.append("7. location (optional: BENGALURU, MUMBAI, KOLKATA, CHENNAI, HYDERABAD)\n");
                errorBuilder.append("8. area (optional: EAST, WEST, NORTH, SOUTH)\n");
                errorBuilder.append("9. plant (optional: PLANT_A, PLANT_B, PLANT_C)\n");
                errorBuilder.append("10. role (optional)\n");
                errorBuilder.append("11. departments (optional, comma-separated)\n");
                errorBuilder.append("12. functions (optional, comma-separated)\n");
                errorBuilder.append("13. operations (optional, comma-separated)\n");
                errorBuilder.append("14. password (optional, leave empty for auto-generation)\n\n");
                errorBuilder.append("TIP: Download the template first to see the exact format!");

                String error = errorBuilder.toString();
                System.err.println("❌ " + error);
                response.setErrors(List.of(error));
                return ResponseEntity.badRequest().body(response);
            }

            response.setTotalRecords(userRequests.size());
            System.out.println("✅ Total records to process: " + userRequests.size());

            // Create users
            int successCount = 0;
            System.out.println("🔄 Starting user creation process...");

            for (int i = 0; i < userRequests.size(); i++) {
                UserCreateRequest userRequest = userRequests.get(i);
                String username = userRequest != null && userRequest.getUsername() != null ?
                        userRequest.getUsername() : "Unknown";

                try {
                    if (userRequest != null) {
                        System.out.println("👤 Processing user " + (i + 1) + "/" + userRequests.size() + ": " + username);

                        ResponseEntity<?> result = userController.create(userRequest);
                        if (result.getStatusCode().is2xxSuccessful()) {
                            successCount++;
                            System.out.println("✅ Successfully created user: " + username);
                        } else {
                            Object body = result.getBody();
                            String errorMsg = "Unknown error (Status: " + result.getStatusCode() + ")";
                            if (body != null) {
                                if (body instanceof String) {
                                    errorMsg = (String) body;
                                } else {
                                    errorMsg = body.toString();
                                }
                            }
                            String error = "Row " + (i + 2) + " (" + username + "): " + errorMsg;
                            errors.add(error);
                            System.err.println("❌ Failed to create user: " + error);
                        }
                    }
                } catch (Exception e) {
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown exception";
                    String error = "Row " + (i + 2) + " (" + username + "): " + errorMsg;
                    errors.add(error);
                    System.err.println("❌ Exception creating user: " + error);
                }
            }

            response.setSuccessfulImports(successCount);
            response.setFailedImports(errors.size());
            response.setErrors(errors);

            System.out.println("🎉 ========== BULK UPLOAD COMPLETED ==========");
            System.out.println("📈 Summary:");
            System.out.println("   Total Records: " + response.getTotalRecords());
            System.out.println("   Successful: " + response.getSuccessfulImports());
            System.out.println("   Failed: " + response.getFailedImports());

            if (errors.isEmpty()) {
                System.out.println("✅ All records processed successfully!");
            } else {
                System.out.println("⚠️  Some records failed, but " + successCount + " were successful.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            String errorMsg = "Failed to process file: " + e.getMessage();
            System.err.println("💥 CRITICAL ERROR: " + errorMsg);
            e.printStackTrace();

            response.setErrors(List.of(errorMsg));
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/bulk-template")
    public ResponseEntity<String> getTemplate() {
        try {
            // Simple template with minimal required fields
            String template = "username,firstName,lastName,email,countryCode,phone,location,area,plant,role,departments,functions,operations,password\n" +
                    "john.doe,John,Doe,john.doe@example.com,+91,9876543210,BENGALURU,EAST,PLANT_A,ROLE_USER,Engineering,Quality,F1,OP1,\n" +
                    "jane.smith,Jane,Smith,jane.smith@example.com,+91,9876543211,MUMBAI,WEST,PLANT_B,ROLE_USER,Production,,F2,,\n" +
                    "bob.wilson,Bob,Wilson,bob.wilson@example.com,+91,9876543212,KOLKATA,NORTH,PLANT_C,ROLE_USER,Engineering,Quality,F1,OP1,user123";

            System.out.println("📋 Template requested");
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            System.err.println("❌ Error generating template: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/bulk-template-download")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            // Create a very simple template that's guaranteed to work
            String template = "username,firstName,lastName,email,countryCode,phone,location,area,plant,role,departments,functions,operations,password\n" +
                    "test.user1,Test,User1,test1@example.com,+91,9876543210,BENGALURU,EAST,PLANT_A,ROLE_USER,Engineering,Quality,F1,OP1,\n" +
                    "test.user2,Test,User2,test2@example.com,+91,9876543211,MUMBAI,WEST,PLANT_B,ROLE_USER,Production,,F2,,\n" +
                    "test.user3,Test,User3,test3@example.com,+91,9876543212,KOLKATA,NORTH,PLANT_C,ROLE_USER,Engineering,Quality,F1,OP1,test123";

            byte[] csvBytes = template.getBytes("UTF-8");

            System.out.println("📥 Template download requested - " + csvBytes.length + " bytes");

            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv; charset=utf-8")
                    .header("Content-Disposition", "attachment; filename=user_bulk_template.csv")
                    .body(csvBytes);

        } catch (Exception e) {
            System.err.println("❌ Error downloading template: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/test-upload")
    public ResponseEntity<String> testUpload() {
        try {
            // Test if repositories are working
            StringBuilder result = new StringBuilder();
            result.append("=== TESTING REPOSITORIES ===\n\n");

            result.append("Roles: ").append(roleRepository.count()).append(" found\n");
            result.append("Departments: ").append(departmentRepository.count()).append(" found\n");
            result.append("Functions: ").append(functionRepository.count()).append(" found\n");
            result.append("Operations: ").append(operationRepository.count()).append(" found\n\n");

            result.append("=== SAMPLE DATA FOR TESTING ===\n");
            result.append("Try this CSV content:\n\n");
            result.append("username,firstName,lastName,email,countryCode,phone,location,area,plant,role,departments,functions,operations,password\n");
            result.append("test.user,John,Doe,john@test.com,+91,9876543210,BENGALURU,EAST,PLANT_A,ROLE_USER,Engineering,Quality,F1,OP1,\n");

            return ResponseEntity.ok(result.toString());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Test failed: " + e.getMessage());
        }
    }
}