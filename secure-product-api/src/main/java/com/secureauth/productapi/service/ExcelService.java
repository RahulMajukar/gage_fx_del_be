package com.secureauth.productapi.service;

import com.secureauth.productapi.dto.UserCreateRequest;
import com.secureauth.productapi.entity.*;
import com.secureauth.productapi.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExcelService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private OperationRepository operationRepository;

    public List<UserCreateRequest> processExcelFile(MultipartFile file) {
        List<UserCreateRequest> userRequests = new ArrayList<>();

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new RuntimeException("File name is null");
        }

        String fileExtension = getFileExtension(fileName).toLowerCase();
        System.out.println("🔄 Processing file: " + fileName + " | Type: " + fileExtension + " | Size: " + file.getSize() + " bytes");

        try (InputStream inputStream = file.getInputStream()) {
            if (fileExtension.equals("csv")) {
                userRequests = processCsvFile(inputStream);
            } else if (fileExtension.equals("xlsx") || fileExtension.equals("xls")) {
                userRequests = processExcelFile(inputStream, fileExtension);
            } else {
                throw new RuntimeException("Unsupported file type: " + fileExtension);
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to parse file: " + e.getMessage());
            throw new RuntimeException("Failed to parse file: " + e.getMessage(), e);
        }

        System.out.println("✅ File processing completed. Found " + userRequests.size() + " valid user records");
        return userRequests;
    }

    private List<UserCreateRequest> processExcelFile(InputStream inputStream, String fileType) {
        List<UserCreateRequest> userRequests = new ArrayList<>();

        try (Workbook workbook = fileType.equals("xlsx") ?
                new XSSFWorkbook(inputStream) : new HSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new RuntimeException("No sheets found in the Excel file");
            }

            int totalRows = sheet.getLastRowNum() + 1;
            System.out.println("📊 Excel Sheet: " + sheet.getSheetName() + " | Total rows: " + totalRows);

            Iterator<Row> rows = sheet.iterator();

            // Process header row
            if (rows.hasNext()) {
                Row headerRow = rows.next();
                List<String> headers = new ArrayList<>();
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    String header = getStringValue(headerRow.getCell(i));
                    headers.add(header != null ? header : "Empty");
                }
                System.out.println("📋 Headers found: " + headers);
                System.out.println("📋 Expected: [username, firstName, lastName, email, countryCode, phone, location, area, plant, role, departments, functions, operations, password]");
            }

            int rowNum = 1; // Start from row 1 (0-based, after header)
            int processedCount = 0;
            int errorCount = 0;
            int emptyCount = 0;

            while (rows.hasNext()) {
                Row row = rows.next();
                rowNum++;

                try {
                    // Check if row is empty
                    if (isRowEmpty(row)) {
                        emptyCount++;
                        System.out.println("⏭️ Skipping empty row " + rowNum);
                        continue;
                    }

                    System.out.println("🔍 Processing row " + rowNum + " with " + row.getLastCellNum() + " columns");

                    // Print all cell values for debugging
                    for (int i = 0; i < row.getLastCellNum(); i++) {
                        String cellValue = getStringValue(row.getCell(i));
                        System.out.println("  Cell " + i + ": '" + (cellValue != null ? cellValue : "NULL") + "'");
                    }

                    UserCreateRequest userRequest = createUserRequestFromRow(row, rowNum);
                    if (userRequest != null && isValidUserRequest(userRequest)) {
                        userRequests.add(userRequest);
                        processedCount++;
                        System.out.println("✅ Successfully processed row " + rowNum + ": " + userRequest.getUsername());
                    } else {
                        errorCount++;
                        System.err.println("❌ Invalid user request in row " + rowNum);
                    }
                } catch (Exception e) {
                    errorCount++;
                    System.err.println("❌ Error processing row " + rowNum + ": " + e.getMessage());
                }
            }

            System.out.println("📈 Processing summary:");
            System.out.println("   Total rows processed: " + (rowNum - 1));
            System.out.println("   Empty rows skipped: " + emptyCount);
            System.out.println("   Successfully processed: " + processedCount);
            System.out.println("   Errors: " + errorCount);
            System.out.println("   Valid records: " + userRequests.size());

        } catch (Exception e) {
            System.err.println("❌ Failed to process Excel file: " + e.getMessage());
            throw new RuntimeException("Failed to process Excel file: " + e.getMessage(), e);
        }

        return userRequests;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;

        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                String value = getStringValue(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private UserCreateRequest createUserRequestFromRow(Row row, int rowNum) {
        try {
            UserCreateRequest request = new UserCreateRequest();

            // Required fields - SIMPLIFIED: Only check first 3 columns
            String username = getStringValue(row.getCell(0)); // Column A
            String firstName = getStringValue(row.getCell(1)); // Column B
            String lastName = getStringValue(row.getCell(2)); // Column C

            System.out.println("  📝 Required fields - Username: '" + username + "', FirstName: '" + firstName + "', LastName: '" + lastName + "'");

            // Validate required fields
            if (username == null || username.trim().isEmpty()) {
                throw new RuntimeException("Username is required (Column A)");
            }
            if (firstName == null || firstName.trim().isEmpty()) {
                throw new RuntimeException("First name is required (Column B)");
            }
            if (lastName == null || lastName.trim().isEmpty()) {
                throw new RuntimeException("Last name is required (Column C)");
            }

            request.setUsername(username.trim());
            request.setFirstName(firstName.trim());
            request.setLastName(lastName.trim());

            // Optional fields - use safe method that doesn't throw exceptions
            request.setEmail(getSafeStringValue(row.getCell(3))); // Column D
            request.setCountryCode(getSafeStringValue(row.getCell(4), "+91")); // Column E
            request.setPhone(getSafeStringValue(row.getCell(5))); // Column F

            // Enum fields
            request.setLocation(getSafeStringValue(row.getCell(6))); // Column G
            request.setArea(getSafeStringValue(row.getCell(7))); // Column H
            request.setPlant(getSafeStringValue(row.getCell(8))); // Column I

            // Role - Column J
            String roleName = getSafeStringValue(row.getCell(9));
            if (roleName != null && !roleName.trim().isEmpty()) {
                try {
                    Optional<Role> roleOpt = roleRepository.findByName(roleName.trim());
                    if (roleOpt.isPresent()) {
                        Role role = roleOpt.get();
                        request.setRoleId(role.getId());
                        System.out.println("  ✅ Role mapped: " + roleName + " -> ID: " + role.getId());
                    } else {
                        System.out.println("  ⚠️ Role not found: " + roleName + ", user will be created without role");
                    }
                } catch (Exception e) {
                    System.err.println("  ⚠️ Error finding role '" + roleName + "': " + e.getMessage());
                }
            }

            // Departments - Column K
            String departments = getSafeStringValue(row.getCell(10));
            if (departments != null && !departments.trim().isEmpty()) {
                try {
                    Set<Long> departmentIds = parseEntityNames(departments, departmentRepository, "Department");
                    request.setDepartmentIds(departmentIds);
                    System.out.println("  ✅ Departments mapped: " + departments + " -> IDs: " + departmentIds);
                } catch (Exception e) {
                    System.err.println("  ⚠️ Error parsing departments '" + departments + "': " + e.getMessage());
                }
            }

            // Functions - Column L
            String functions = getSafeStringValue(row.getCell(11));
            if (functions != null && !functions.trim().isEmpty()) {
                try {
                    Set<Long> functionIds = parseEntityNames(functions, functionRepository, "Function");
                    request.setFunctionIds(functionIds);
                    System.out.println("  ✅ Functions mapped: " + functions + " -> IDs: " + functionIds);
                } catch (Exception e) {
                    System.err.println("  ⚠️ Error parsing functions '" + functions + "': " + e.getMessage());
                }
            }

            // Operations - Column M
            String operations = getSafeStringValue(row.getCell(12));
            if (operations != null && !operations.trim().isEmpty()) {
                try {
                    Set<Long> operationIds = parseEntityNames(operations, operationRepository, "Operation");
                    request.setOperationIds(operationIds);
                    System.out.println("  ✅ Operations mapped: " + operations + " -> IDs: " + operationIds);
                } catch (Exception e) {
                    System.err.println("  ⚠️ Error parsing operations '" + operations + "': " + e.getMessage());
                }
            }

            // Password - Column N
            String password = getSafeStringValue(row.getCell(13));
            if (password != null && !password.trim().isEmpty()) {
                request.setAdminSetsPassword(true);
                request.setPassword(password.trim());
                System.out.println("  ✅ Password provided by admin");
            } else {
                request.setAdminSetsPassword(false);
                System.out.println("  ✅ Auto-generate password");
            }

            System.out.println("  ✅ Successfully created UserCreateRequest for: " + username);
            return request;

        } catch (Exception e) {
            throw new RuntimeException("Row " + rowNum + ": " + e.getMessage());
        }
    }

    private List<UserCreateRequest> processCsvFile(InputStream inputStream) {
        List<UserCreateRequest> userRequests = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            int rowNum = 0;
            int processedCount = 0;
            int errorCount = 0;
            int emptyCount = 0;

            // Read header
            String header = reader.readLine();
            if (header == null) {
                throw new RuntimeException("CSV file is empty");
            }

            System.out.println("📋 CSV Header: " + header);
            System.out.println("📋 Expected: username,firstName,lastName,email,countryCode,phone,location,area,plant,role,departments,functions,operations,password");

            rowNum++;

            while ((line = reader.readLine()) != null) {
                rowNum++;
                try {
                    if (line.trim().isEmpty()) {
                        emptyCount++;
                        System.out.println("⏭️ Skipping empty line at row " + rowNum);
                        continue;
                    }

                    System.out.println("🔍 Processing CSV row " + rowNum + ": " + line);

                    UserCreateRequest userRequest = createUserRequestFromCsvLine(line, rowNum);
                    if (userRequest != null && isValidUserRequest(userRequest)) {
                        userRequests.add(userRequest);
                        processedCount++;
                        System.out.println("✅ Successfully processed CSV row " + rowNum + ": " + userRequest.getUsername());
                    } else {
                        errorCount++;
                        System.err.println("❌ Invalid user request in CSV row " + rowNum);
                    }
                } catch (Exception e) {
                    errorCount++;
                    System.err.println("❌ Error processing CSV row " + rowNum + ": " + e.getMessage());
                }
            }

            System.out.println("📈 CSV processing summary:");
            System.out.println("   Total rows processed: " + (rowNum - 1));
            System.out.println("   Empty rows skipped: " + emptyCount);
            System.out.println("   Successfully processed: " + processedCount);
            System.out.println("   Errors: " + errorCount);
            System.out.println("   Valid records: " + userRequests.size());

        } catch (Exception e) {
            System.err.println("❌ Failed to process CSV file: " + e.getMessage());
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage(), e);
        }

        return userRequests;
    }

    private UserCreateRequest createUserRequestFromCsvLine(String line, int rowNum) {
        try {
            String[] values = parseCsvLine(line);
            System.out.println("  📝 Parsed " + values.length + " columns: " + Arrays.toString(values));

            if (values.length < 3) {
                throw new RuntimeException("Invalid CSV format - not enough columns. Found: " + values.length + ", Expected at least: 3");
            }

            UserCreateRequest request = new UserCreateRequest();

            // Required fields
            String username = values[0] != null ? values[0].trim() : null;
            String firstName = values[1] != null ? values[1].trim() : null;
            String lastName = values[2] != null ? values[2].trim() : null;

            System.out.println("  📝 Required fields - Username: '" + username + "', FirstName: '" + firstName + "', LastName: '" + lastName + "'");

            if (username == null || username.isEmpty()) {
                throw new RuntimeException("Username is required (Column 1)");
            }
            if (firstName == null || firstName.isEmpty()) {
                throw new RuntimeException("First name is required (Column 2)");
            }
            if (lastName == null || lastName.isEmpty()) {
                throw new RuntimeException("Last name is required (Column 3)");
            }

            request.setUsername(username);
            request.setFirstName(firstName);
            request.setLastName(lastName);

            // Optional fields
            request.setEmail(getSafeCsvValue(values, 3));
            request.setCountryCode(getSafeCsvValue(values, 4, "+91"));
            request.setPhone(getSafeCsvValue(values, 5));

            // Enum fields
            request.setLocation(getSafeCsvValue(values, 6));
            request.setArea(getSafeCsvValue(values, 7));
            request.setPlant(getSafeCsvValue(values, 8));

            // Role
            String roleName = getSafeCsvValue(values, 9);
            if (roleName != null && !roleName.trim().isEmpty()) {
                try {
                    Optional<Role> roleOpt = roleRepository.findByName(roleName.trim());
                    if (roleOpt.isPresent()) {
                        Role role = roleOpt.get();
                        request.setRoleId(role.getId());
                        System.out.println("  ✅ Role mapped: " + roleName + " -> ID: " + role.getId());
                    }
                } catch (Exception e) {
                    System.err.println("  ⚠️ Error finding role '" + roleName + "': " + e.getMessage());
                }
            }

            // Departments
            String departments = getSafeCsvValue(values, 10);
            if (departments != null && !departments.trim().isEmpty()) {
                try {
                    Set<Long> departmentIds = parseEntityNames(departments, departmentRepository, "Department");
                    request.setDepartmentIds(departmentIds);
                } catch (Exception e) {
                    System.err.println("  ⚠️ Error parsing departments '" + departments + "': " + e.getMessage());
                }
            }

            // Functions
            String functions = getSafeCsvValue(values, 11);
            if (functions != null && !functions.trim().isEmpty()) {
                try {
                    Set<Long> functionIds = parseEntityNames(functions, functionRepository, "Function");
                    request.setFunctionIds(functionIds);
                } catch (Exception e) {
                    System.err.println("  ⚠️ Error parsing functions '" + functions + "': " + e.getMessage());
                }
            }

            // Operations
            String operations = getSafeCsvValue(values, 12);
            if (operations != null && !operations.trim().isEmpty()) {
                try {
                    Set<Long> operationIds = parseEntityNames(operations, operationRepository, "Operation");
                    request.setOperationIds(operationIds);
                } catch (Exception e) {
                    System.err.println("  ⚠️ Error parsing operations '" + operations + "': " + e.getMessage());
                }
            }

            // Password
            String password = getSafeCsvValue(values, 13);
            if (password != null && !password.trim().isEmpty()) {
                request.setAdminSetsPassword(true);
                request.setPassword(password.trim());
            } else {
                request.setAdminSetsPassword(false);
            }

            System.out.println("  ✅ Successfully created UserCreateRequest from CSV for: " + username);
            return request;

        } catch (Exception e) {
            throw new RuntimeException("Row " + rowNum + ": " + e.getMessage());
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentValue = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(currentValue.toString().trim());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }
        values.add(currentValue.toString().trim());

        return values.toArray(new String[0]);
    }

    private String getSafeCsvValue(String[] values, int index) {
        return getSafeCsvValue(values, index, null);
    }

    private String getSafeCsvValue(String[] values, int index, String defaultValue) {
        if (index < values.length && values[index] != null) {
            String value = values[index].trim();
            return value.isEmpty() ? defaultValue : value;
        }
        return defaultValue;
    }

    private <T> Set<Long> parseEntityNames(String names, org.springframework.data.jpa.repository.JpaRepository<T, Long> repository, String entityType) {
        System.out.println("  🔍 Parsing " + entityType + " names: '" + names + "'");

        return Arrays.stream(names.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(name -> {
                    try {
                        Object entity = null;
                        if (repository instanceof RoleRepository) {
                            Optional<Role> roleOpt = ((RoleRepository) repository).findByName(name);
                            if (roleOpt.isPresent()) {
                                entity = roleOpt.get();
                            }
                        } else if (repository instanceof DepartmentRepository) {
                            Optional<Department> deptOpt = ((DepartmentRepository) repository).findByName(name);
                            if (deptOpt.isPresent()) {
                                entity = deptOpt.get();
                            }
                        } else if (repository instanceof FunctionRepository) {
                            Optional<Function> funcOpt = ((FunctionRepository) repository).findByName(name);
                            if (funcOpt.isPresent()) {
                                entity = funcOpt.get();
                            }
                        } else if (repository instanceof OperationRepository) {
                            Optional<Operation> opOpt = ((OperationRepository) repository).findByName(name);
                            if (opOpt.isPresent()) {
                                entity = opOpt.get();
                            }
                        }

                        if (entity != null) {
                            System.out.println("    ✅ Found " + entityType + ": " + name);
                            return entity;
                        } else {
                            throw new RuntimeException(entityType + " not found: " + name);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to find " + entityType + ": " + name + " - " + e.getMessage());
                    }
                })
                .map(entity -> {
                    if (entity instanceof Role) return ((Role) entity).getId();
                    if (entity instanceof Department) return ((Department) entity).getId();
                    if (entity instanceof Function) return ((Function) entity).getId();
                    if (entity instanceof Operation) return ((Operation) entity).getId();
                    throw new RuntimeException("Unsupported entity type");
                })
                .collect(Collectors.toSet());
    }

    private String getStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    } else {
                        double numValue = cell.getNumericCellValue();
                        if (numValue == Math.floor(numValue)) {
                            return String.valueOf((long) numValue);
                        } else {
                            return String.valueOf(numValue);
                        }
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    try {
                        return cell.getStringCellValue();
                    } catch (Exception e) {
                        return String.valueOf(cell.getNumericCellValue());
                    }
                case BLANK:
                    return null;
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String getSafeStringValue(Cell cell) {
        return getSafeStringValue(cell, null);
    }

    private String getSafeStringValue(Cell cell, String defaultValue) {
        try {
            String value = getStringValue(cell);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    private boolean isValidUserRequest(UserCreateRequest request) {
        boolean isValid = request.getUsername() != null && !request.getUsername().trim().isEmpty() &&
                request.getFirstName() != null && !request.getFirstName().trim().isEmpty() &&
                request.getLastName() != null && !request.getLastName().trim().isEmpty();

        if (isValid) {
            System.out.println("✅ Valid user request: " + request.getUsername());
        } else {
            System.out.println("❌ Invalid user request - missing required fields");
            System.out.println("  Username: " + request.getUsername());
            System.out.println("  FirstName: " + request.getFirstName());
            System.out.println("  LastName: " + request.getLastName());
        }

        return isValid;
    }
}