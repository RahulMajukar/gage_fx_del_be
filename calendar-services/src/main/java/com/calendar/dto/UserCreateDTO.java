//// UserCreateDTO.java
//package com.calendar.dto;
//
//import lombok.Data;
//
//import java.time.LocalDate;
//
//@Data
//public class UserCreateDTO {
//    private String namePrefix;      // Mr., Mrs., Miss, etc.
//    private String firstName;
//    private String lastName;
//    private String userName;        // Optional - will be generated if not provided
//    private String email;
//    private String phone;
//    private LocalDate dateOfBirth;
//    private String gender;          // MALE, FEMALE, OTHER
//    private String role;
//    private String password;
//    private Boolean status = true;
//    private String signature;       // Base64 encoded signature
//    private String profilePhoto;    // Base64 encoded profile photo
//}