//package com.secureauth.productservice.service;
//
//import com.secureauth.productservice.dto.ReallocateApprovalRequest;
//import com.secureauth.productservice.dto.ReallocateRequest;
//import com.secureauth.productservice.dto.ReallocateResponse;
//import com.secureauth.productservice.entity.Gage;
//import com.secureauth.productservice.entity.GageType;
//import com.secureauth.productservice.entity.Reallocate;
//import com.secureauth.productservice.repository.GageRepository;
//import com.secureauth.productservice.repository.GageTypeRepository;
//import com.secureauth.productservice.repository.ReallocateRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@ActiveProfiles("test")
//@Transactional
//public class ReallocateServiceIntegrationTest {
//
//    @Autowired
//    private ReallocateService reallocateService;
//
//    @Autowired
//    private ReallocateRepository reallocateRepository;
//
//    @Autowired
//    private GageRepository gageRepository;
//
//    @Autowired
//    private GageTypeRepository gageTypeRepository;
//
//    private Gage testGage;
//    private GageType testGageType;
//
//    @BeforeEach
//    void setUp() {
//        // Create test gage type
//        testGageType = GageType.builder()
//                .name("Test Gage Type")
//                .description("Test Description")
//                .build();
//        testGageType = gageTypeRepository.save(testGageType);
//
//        // Create test gage
//        testGage = Gage.builder()
//                .serialNumber("TEST-001")
//                .modelNumber("MODEL-001")
//                .gageType(testGageType)
//                .status(Gage.Status.ACTIVE)
//                .category(Gage.Category.DIMENSIONAL)
//                .usageFrequency(Gage.UsageFrequency.DAILY)
//                .criticality(Gage.Criticality.HIGH)
//                .location(Gage.Location.SHOP_FLOOR)
//                .measurementRange("0-100mm")
//                .accuracy("±0.01mm")
//                .purchaseDate(LocalDate.now().minusDays(30))
//                .calibrationInterval(90)
//                .nextCalibrationDate(LocalDate.now().plusDays(60))
//                .maxUsersNumber(5)
//                .build();
//        testGage = gageRepository.save(testGage);
//    }
//
//    @Test
//    void testCreateReallocateRequest() {
//        // Given
//        ReallocateRequest request = ReallocateRequest.builder()
//                .gageId(testGage.getId())
//                .requestedBy("operator1")
//                .requestedByRole("F")
//                .requestedByFunction("F1")
//                .requestedByOperation("OT1")
//                .timeLimit(Reallocate.TimeLimit.ONE_DAY)
//                .reason("Test reallocation")
//                .build();
//
//        // When
//        ReallocateResponse response = reallocateService.createReallocateRequest(request);
//
//        // Then
//        assertNotNull(response);
//        assertEquals(testGage.getId(), response.getGageId());
//        assertEquals("operator1", response.getRequestedBy());
//        assertEquals(Reallocate.Status.PENDING_APPROVAL, response.getStatus());
//        assertEquals(Reallocate.TimeLimit.ONE_DAY, response.getTimeLimit());
//
//        // Verify in database
//        Reallocate savedReallocate = reallocateRepository.findById(response.getId()).orElse(null);
//        assertNotNull(savedReallocate);
//        assertEquals(testGage.getId(), savedReallocate.getGage().getId());
//    }
//
//    @Test
//    void testApproveReallocateRequest() {
//        // Given - Create a pending request
//        ReallocateRequest request = ReallocateRequest.builder()
//                .gageId(testGage.getId())
//                .requestedBy("operator1")
//                .requestedByRole("F")
//                .requestedByFunction("F1")
//                .requestedByOperation("OT1")
//                .timeLimit(Reallocate.TimeLimit.ONE_DAY)
//                .reason("Test reallocation")
//                .build();
//
//        ReallocateResponse createdResponse = reallocateService.createReallocateRequest(request);
//
//        ReallocateApprovalRequest approvalRequest = ReallocateApprovalRequest.builder()
//                .reallocateId(createdResponse.getId())
//                .approvedBy("plant.hod")
//                .timeLimit(Reallocate.TimeLimit.ONE_DAY)
//                .newDepartment("Quality")
//                .newFunction("F2")
//                .newOperation("OT2")
//                .notes("Approved with modifications")
//                .build();
//
//        // When
//        ReallocateResponse response = reallocateService.approveReallocateRequest(approvalRequest);
//
//        // Then
//        assertNotNull(response);
//        assertEquals(Reallocate.Status.APPROVED, response.getStatus());
//        assertEquals("plant.hod", response.getApprovedBy());
//        assertEquals("Quality", response.getCurrentDepartment());
//        assertEquals("F2", response.getCurrentFunction());
//        assertEquals("OT2", response.getCurrentOperation());
//        assertNotNull(response.getExpiresAt());
//
//        // Verify gage status updated
//        Gage updatedGage = gageRepository.findById(testGage.getId()).orElse(null);
//        assertNotNull(updatedGage);
//        assertEquals(Gage.Status.ISSUED, updatedGage.getStatus());
//    }
//
//    @Test
//    void testRejectReallocateRequest() {
//        // Given - Create a pending request
//        ReallocateRequest request = ReallocateRequest.builder()
//                .gageId(testGage.getId())
//                .requestedBy("operator1")
//                .requestedByRole("F")
//                .requestedByFunction("F1")
//                .requestedByOperation("OT1")
//                .timeLimit(Reallocate.TimeLimit.ONE_DAY)
//                .reason("Test reallocation")
//                .build();
//
//        ReallocateResponse createdResponse = reallocateService.createReallocateRequest(request);
//
//        // When
//        ReallocateResponse response = reallocateService.rejectReallocateRequest(
//                createdResponse.getId(), "plant.hod", "Not approved");
//
//        // Then
//        assertNotNull(response);
//        assertEquals(Reallocate.Status.CANCELLED, response.getStatus());
//        assertEquals("plant.hod", response.getApprovedBy());
//    }
//
//    @Test
//    void testReturnGage() {
//        // Given - Create and approve a request
//        ReallocateRequest request = ReallocateRequest.builder()
//                .gageId(testGage.getId())
//                .requestedBy("operator1")
//                .requestedByRole("F")
//                .requestedByFunction("F1")
//                .requestedByOperation("OT1")
//                .timeLimit(Reallocate.TimeLimit.ONE_DAY)
//                .reason("Test reallocation")
//                .build();
//
//        ReallocateResponse createdResponse = reallocateService.createReallocateRequest(request);
//
//        ReallocateApprovalRequest approvalRequest = ReallocateApprovalRequest.builder()
//                .reallocateId(createdResponse.getId())
//                .approvedBy("plant.hod")
//                .timeLimit(Reallocate.TimeLimit.ONE_DAY)
//                .build();
//
//        reallocateService.approveReallocateRequest(approvalRequest);
//
//        // When
//        ReallocateResponse response = reallocateService.returnGage(
//                createdResponse.getId(), "operator1", "Measurement completed");
//
//        // Then
//        assertNotNull(response);
//        assertEquals(Reallocate.Status.RETURNED, response.getStatus());
//
//        // Verify gage status updated
//        Gage updatedGage = gageRepository.findById(testGage.getId()).orElse(null);
//        assertNotNull(updatedGage);
//        assertEquals(Gage.Status.ACTIVE, updatedGage.getStatus());
//    }
//
//    @Test
//    void testGetReallocatesByStatus() {
//        // Given - Create multiple requests with different statuses
//        ReallocateRequest request1 = ReallocateRequest.builder()
//                .gageId(testGage.getId())
//                .requestedBy("operator1")
//                .requestedByRole("F")
//                .requestedByFunction("F1")
//                .requestedByOperation("OT1")
//                .timeLimit(Reallocate.TimeLimit.ONE_DAY)
//                .reason("Test reallocation 1")
//                .build();
//
//        ReallocateRequest request2 = ReallocateRequest.builder()
//                .gageId(testGage.getId())
//                .requestedBy("operator2")
//                .requestedByRole("F")
//                .requestedByFunction("F2")
//                .requestedByOperation("OT2")
//                .timeLimit(Reallocate.TimeLimit.TWO_HOURS)
//                .reason("Test reallocation 2")
//                .build();
//
//        reallocateService.createReallocateRequest(request1);
//        reallocateService.createReallocateRequest(request2);
//
//        // When
//        var pendingRequests = reallocateService.getReallocatesByStatus(Reallocate.Status.PENDING_APPROVAL);
//
//        // Then
//        assertEquals(2, pendingRequests.size());
//    }
//
//    @Test
//    void testIsGageAvailableForReallocation() {
//        // Given
//        Long gageId = testGage.getId();
//
//        // When - No active reallocations
//        boolean isAvailable = reallocateService.isGageAvailableForReallocation(gageId);
//
//        // Then
//        assertTrue(isAvailable);
//
//        // When - Create an active reallocation
//        ReallocateRequest request = ReallocateRequest.builder()
//                .gageId(gageId)
//                .requestedBy("operator1")
//                .requestedByRole("F")
//                .requestedByFunction("F1")
//                .requestedByOperation("OT1")
//                .timeLimit(Reallocate.TimeLimit.ONE_DAY)
//                .reason("Test reallocation")
//                .build();
//
//        reallocateService.createReallocateRequest(request);
//
//        // Then
//        boolean isAvailableAfter = reallocateService.isGageAvailableForReallocation(gageId);
//        assertFalse(isAvailableAfter);
//    }
//
//    @Test
//    void testTimeLimitCalculation() {
//        // Given
//        ReallocateRequest request = ReallocateRequest.builder()
//                .gageId(testGage.getId())
//                .requestedBy("operator1")
//                .requestedByRole("F")
//                .requestedByFunction("F1")
//                .requestedByOperation("OT1")
//                .timeLimit(Reallocate.TimeLimit.TWO_HOURS)
//                .reason("Test reallocation")
//                .build();
//
//        ReallocateResponse createdResponse = reallocateService.createReallocateRequest(request);
//
//        ReallocateApprovalRequest approvalRequest = ReallocateApprovalRequest.builder()
//                .reallocateId(createdResponse.getId())
//                .approvedBy("plant.hod")
//                .timeLimit(Reallocate.TimeLimit.TWO_HOURS)
//                .build();
//
//        // When
//        ReallocateResponse response = reallocateService.approveReallocateRequest(approvalRequest);
//
//        // Then
//        assertNotNull(response.getExpiresAt());
//        assertTrue(response.getExpiresAt().isAfter(response.getAllocatedAt()));
//        assertTrue(response.getRemainingMinutes() > 0);
//    }
//}
