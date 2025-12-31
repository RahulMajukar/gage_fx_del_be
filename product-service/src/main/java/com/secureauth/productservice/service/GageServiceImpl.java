package com.secureauth.productservice.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.secureauth.productservice.dto.*;
import com.secureauth.productservice.entity.*;
import com.secureauth.productservice.exception.ResourceNotFoundException;
import com.secureauth.productservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GageServiceImpl implements GageService {

    @Autowired
    private GageRepository gageRepository;

    @Autowired
    private GageTypeRepository gageTypeRepository;

    @Autowired
    private GageSubTypeRepository gageSubTypeRepository;

    @Autowired
    private InhouseCalibrationMachineRepository inhouseCalibrationMachineRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private GageIssueRepository gageIssueRepository;

    @Autowired
    private JobRepository jobRepository;

    @Value("${app.frontend.baseUrl:http://10.2.0.95:5173}")
    private String frontendBaseUrl;

    // =============== BARCODE GENERATION ===============
    private String generateBarcodeBase64(String serialNumber) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            return null;
        }
        try {
            BitMatrix bitMatrix = new Code128Writer().encode(
                    serialNumber.trim(),
                    BarcodeFormat.CODE_128,
                    300,
                    80);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());
        } catch (Exception e) {
            System.err.println("⚠️ Failed to generate barcode: " + e.getMessage());
            return null;
        }
    }

    // =============== QR CODE GENERATION ===============
    private String generateQrCodeBase64(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    content.trim(),
                    BarcodeFormat.QR_CODE,
                    300,
                    300);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());
        } catch (Exception e) {
            System.err.println("⚠️ Failed to generate QR code: " + e.getMessage());
            return null;
        }
    }

    private String buildGageDeepLink(String serialNumber) {
        if (serialNumber == null || serialNumber.isBlank())
            return null;
        String base = frontendBaseUrl != null ? frontendBaseUrl.trim() : "http://10.2.0.95:5173";
        if (base.endsWith("/"))
            base = base.substring(0, base.length() - 1);
        return base + "/scan?serial=" + serialNumber.trim();
    }

    // =============== BARCODE DECODING HELPER ===============
    private String decodeBarcodeImage(byte[] imageBytes) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bais);
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            MultiFormatReader reader = new MultiFormatReader();
            Result result = reader.decode(bitmap);
            return result.getText();
        } catch (Exception e) {
            System.err.println("⚠️ Failed to decode barcode image: " + e.getMessage());
            return null;
        }
    }

    // =============== SCAN VIA BARCODE IMAGE UPLOAD ===============
    @Override
    public GageScanResponse getGageDetailsByBarcodeImage(MultipartFile barcodeImage) {
        try {
            if (barcodeImage == null || barcodeImage.isEmpty()) {
                return GageScanResponse.builder()
                        .success(false)
                        .message("No barcode image provided")
                        .scanTime(LocalDateTime.now())
                        .build();
            }
            String serialNumber = decodeBarcodeImage(barcodeImage.getBytes());
            if (serialNumber == null || serialNumber.trim().isEmpty()) {
                return GageScanResponse.builder()
                        .success(false)
                        .message("Could not decode serial number from barcode image")
                        .scanTime(LocalDateTime.now())
                        .build();
            }
            return getGageDetailsByBarcodeScan(serialNumber.trim());
        } catch (Exception e) {
            return GageScanResponse.builder()
                    .success(false)
                    .message("Error processing barcode image: " + e.getMessage())
                    .scanTime(LocalDateTime.now())
                    .build();
        }
    }

    // =============== CREATE GAGE ===============
    @Override
    public GageResponse createGage(GageRequest request) {
        Optional<Gage> existing = gageRepository.findBySerialNumber(request.getSerialNumber());
        if (existing.isPresent()) {
            Gage gage = existing.get();
            if (gage.getStatus() == Gage.Status.ISSUED) {
                updateGageFromRequest(gage, request);
                gage.setStatus(Gage.Status.ISSUED);
                Gage updatedGage = gageRepository.save(gage);
                return mapToGageResponse(updatedGage);
            } else if (gage.getStatus() == Gage.Status.ACTIVE) {
                System.out.println("🔄 Issuing ACTIVE gage with serial number: " + request.getSerialNumber());
                updateGageFromRequest(gage, request);
                gage.setStatus(Gage.Status.ISSUED);
                Gage updatedGage = gageRepository.save(gage);
                System.out.println("✅ Gage issued successfully. New status: " + updatedGage.getStatus());
                return mapToGageResponse(updatedGage);
            } else {
                throw new IllegalArgumentException("Cannot issue gage with status: " + gage.getStatus()
                        + ". Serial number: " + request.getSerialNumber());
            }
        }
        Gage gage = mapToGage(request);
        gage.setStatus(Gage.Status.ACTIVE);
        Gage savedGage = gageRepository.save(gage);
        return mapToGageResponse(savedGage);
    }

    // =============== MAPPING: REQUEST → ENTITY ===============
    private Gage mapToGage(GageRequest request) {
        GageType gageType = gageTypeRepository.findByName(request.getGageTypeName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "GageType not found with name: " + request.getGageTypeName()));
        GageSubType gageSubType = gageSubTypeRepository.findById(request.getGageSubTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "GageSubType not found with id: " + request.getGageSubTypeId()));
        Manufacturer manufacturer = manufacturerRepository.findById(Long.valueOf(request.getManufacturerId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Manufacturer not found with id: " + request.getManufacturerId()));

        InhouseCalibrationMachine inhouseCalibrationMachine = null;
        if (request.getInhouseCalibrationMachineId() != null) {
            inhouseCalibrationMachine = inhouseCalibrationMachineRepository.findById(request.getInhouseCalibrationMachineId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "InhouseCalibrationMachine not found with id: " + request.getInhouseCalibrationMachineId()));
        }
        LocalDate pending = calculatePendingCalibrationDate(request.getNextCalibrationDate(), request.getCriticality());
        Integer remaining = calculateRemainingDays(request.getNextCalibrationDate());

        Gage.CodeType codeType = request.getCodeType() != null ? request.getCodeType() : Gage.CodeType.BARCODE_ONLY;
        String barcodeImg = null;
        String qrCodeImg = null;
        switch (codeType) {
            case BARCODE_ONLY:
                barcodeImg = generateBarcodeBase64(request.getSerialNumber());
                break;
            case QR_ONLY:
                qrCodeImg = generateQrCodeBase64(buildGageDeepLink(request.getSerialNumber()));
                break;
            case BOTH:
                barcodeImg = generateBarcodeBase64(request.getSerialNumber());
                qrCodeImg = generateQrCodeBase64(buildGageDeepLink(request.getSerialNumber()));
                break;
        }

        return Gage.builder()
                .serialNumber(request.getSerialNumber())
                .modelNumber(request.getModelNumber())
                .gageType(gageType)
                .gageSubType(gageSubType)
                .inhouseCalibrationMachine(inhouseCalibrationMachine)
                .usageFrequency(request.getUsageFrequency())
                .criticality(request.getCriticality())
                .location(request.getLocation())
                .measurementRange(request.getMeasurementRange())
                .accuracy(request.getAccuracy())
                .purchaseDate(request.getPurchaseDate())
                .manufacturer(manufacturer)
                .calibrationInterval(request.getCalibrationInterval())
                .nextCalibrationDate(request.getNextCalibrationDate())
                .maxUsersNumber(request.getMaxUsersNumber())
                .pendingCalibrationDate(pending)
                .remainingDays(remaining)
                .notes(request.getNotes())
                // ✅ Multiple images
                .gageImages(
                        request.getGageImages() != null ? new ArrayList<>(request.getGageImages()) : new ArrayList<>())
                // ✅ New: optional videos
                .gageVideos(
                        request.getGageVideos() != null ? new ArrayList<>(request.getGageVideos()) : new ArrayList<>())
                .gageManual(request.getGageManual())
                .barcodeImage(barcodeImg)
                .qrCodeImage(qrCodeImg)
                .codeType(codeType)
                .status(request.getStatus() != null ? request.getStatus() : Gage.Status.ACTIVE)
                .build();
    }

    // =============== UPDATE GAGE FROM REQUEST ===============
    private void updateGageFromRequest(Gage gage, GageRequest request) {
        GageType gageType = gageTypeRepository.findByName(request.getGageTypeName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "GageType not found with name: " + request.getGageTypeName()));
        GageSubType gageSubType = gageSubTypeRepository.findById(request.getGageSubTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "GageSubType not found with id: " + request.getGageSubTypeId()));
        Manufacturer manufacturer = manufacturerRepository.findById(Long.valueOf(request.getManufacturerId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Manufacturer not found with id: " + request.getManufacturerId()));

        InhouseCalibrationMachine inhouseCalibrationMachine = null;
        if (request.getInhouseCalibrationMachineId() != null) {
            inhouseCalibrationMachine = inhouseCalibrationMachineRepository.findById(request.getInhouseCalibrationMachineId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "InhouseCalibrationMachine not found with id: " + request.getInhouseCalibrationMachineId()));
        }

        boolean serialChanged = !gage.getSerialNumber().equals(request.getSerialNumber());
        Gage.CodeType newCodeType = request.getCodeType() != null ? request.getCodeType() : gage.getCodeType();

        String barcodeImg = gage.getBarcodeImage();
        String qrCodeImg = gage.getQrCodeImage();
        if (serialChanged || newCodeType != gage.getCodeType()) {
            barcodeImg = null;
            qrCodeImg = null;
            switch (newCodeType) {
                case BARCODE_ONLY:
                    barcodeImg = generateBarcodeBase64(request.getSerialNumber());
                    break;
                case QR_ONLY:
                    qrCodeImg = generateQrCodeBase64(buildGageDeepLink(request.getSerialNumber()));
                    break;
                case BOTH:
                    barcodeImg = generateBarcodeBase64(request.getSerialNumber());
                    qrCodeImg = generateQrCodeBase64(buildGageDeepLink(request.getSerialNumber()));
                    break;
            }
        }

        gage.setSerialNumber(request.getSerialNumber());
        gage.setModelNumber(request.getModelNumber());
        gage.setGageType(gageType);
        gage.setGageSubType(gageSubType);
        gage.setInhouseCalibrationMachine(inhouseCalibrationMachine);
        gage.setUsageFrequency(request.getUsageFrequency());
        gage.setCriticality(request.getCriticality());
        gage.setLocation(request.getLocation());
        gage.setMeasurementRange(request.getMeasurementRange());
        gage.setAccuracy(request.getAccuracy());
        gage.setPurchaseDate(request.getPurchaseDate());
        gage.setManufacturer(manufacturer);
        gage.setCalibrationInterval(request.getCalibrationInterval());
        gage.setNextCalibrationDate(request.getNextCalibrationDate());
        gage.setMaxUsersNumber(request.getMaxUsersNumber());
        gage.setPendingCalibrationDate(
                calculatePendingCalibrationDate(request.getNextCalibrationDate(), request.getCriticality()));
        gage.setRemainingDays(calculateRemainingDays(request.getNextCalibrationDate()));
        if (request.getNotes() != null)
            gage.setNotes(request.getNotes());

        // ✅ Update multiple images
        if (request.getGageImages() != null) {
            gage.setGageImages(new ArrayList<>(request.getGageImages()));
        }

        // ✅ Update optional videos
        if (request.getGageVideos() != null) {
            gage.setGageVideos(new ArrayList<>(request.getGageVideos()));
        }

        if (request.getGageManual() != null)
            gage.setGageManual(request.getGageManual());

        if (request.getStatus() != null)
            gage.setStatus(request.getStatus());

        gage.setBarcodeImage(barcodeImg);
        gage.setQrCodeImage(qrCodeImg);
        gage.setCodeType(newCodeType);
    }

    // =============== MAPPING HELPERS ===============
    private GageResponse mapToGageResponse(Gage gage) {
        return GageResponse.builder()
                .id(gage.getId())
                .serialNumber(gage.getSerialNumber())
                .modelNumber(gage.getModelNumber())
                .gageType(mapToGageTypeResponse(gage.getGageType()))
                .gageSubType(gage.getGageSubType() != null ? mapToGageSubTypeResponse(gage.getGageSubType()) : null)
                .inhouseCalibrationMachine(gage.getInhouseCalibrationMachine() != null ?
                        mapToInhouseCalibrationMachineResponse(gage.getInhouseCalibrationMachine()) : null)
                .usageFrequency(gage.getUsageFrequency())
                .criticality(gage.getCriticality())
                .location(gage.getLocation())
                .status(gage.getStatus())
                .measurementRange(gage.getMeasurementRange())
                .accuracy(gage.getAccuracy())
                .purchaseDate(gage.getPurchaseDate())
                .manufacturerId(gage.getManufacturer() != null ? gage.getManufacturer().getId().toString() : null)
                .manufacturerName(gage.getManufacturer() != null ? gage.getManufacturer().getName() : null)
                .calibrationInterval(gage.getCalibrationInterval())
                .nextCalibrationDate(gage.getNextCalibrationDate())
                .maxUsersNumber(gage.getMaxUsersNumber())
                .pendingCalibrationDate(gage.getPendingCalibrationDate())
                .remainingDays(gage.getRemainingDays())
                .notes(gage.getNotes())
                // ✅ Multiple images
                .gageImages(gage.getGageImages() != null ? new ArrayList<>(gage.getGageImages()) : new ArrayList<>())
                // ✅ New: optional videos
                .gageVideos(gage.getGageVideos() != null ? new ArrayList<>(gage.getGageVideos()) : new ArrayList<>())
                .gageManual(gage.getGageManual())
                .barcodeImage(gage.getBarcodeImage())
                .qrCodeImage(gage.getQrCodeImage())
                .build();
    }

    private GageScanResponse mapToGageScanResponse(Gage gage) {
        return GageScanResponse.builder()
                .id(gage.getId())
                .serialNumber(gage.getSerialNumber())
                .modelNumber(gage.getModelNumber())
                .gageType(mapToGageTypeResponse(gage.getGageType()))
                .gageSubType(gage.getGageSubType() != null ? gage.getGageSubType().getName() : null)
                .usageFrequency(gage.getUsageFrequency())
                .criticality(gage.getCriticality())
                .location(gage.getLocation())
                .status(gage.getStatus())
                .measurementRange(gage.getMeasurementRange())
                .accuracy(gage.getAccuracy())
                .purchaseDate(gage.getPurchaseDate())
                .manufacturerName(gage.getManufacturer() != null ? gage.getManufacturer().getName() : null)
                .calibrationInterval(gage.getCalibrationInterval())
                .nextCalibrationDate(gage.getNextCalibrationDate())
                .maxUsersNumber(gage.getMaxUsersNumber())
                .pendingCalibrationDate(gage.getPendingCalibrationDate())
                .remainingDays(gage.getRemainingDays())
                .notes(gage.getNotes())
                // ✅ Multiple images
                .gageImages(gage.getGageImages() != null ? new ArrayList<>(gage.getGageImages()) : new ArrayList<>())
                // ✅ New: optional videos
                .gageVideos(gage.getGageVideos() != null ? new ArrayList<>(gage.getGageVideos()) : new ArrayList<>())
                .barcodeImage(gage.getBarcodeImage())
                .qrCodeImage(gage.getQrCodeImage())
                .scanTime(LocalDateTime.now())
                .success(true)
                .message("Gage details retrieved successfully")
                .build();
    }

    // =============== SCAN BY TEXT ===============
    @Override
    public GageScanResponse getGageDetailsByBarcodeScan(String serialNumber) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            return GageScanResponse.builder()
                    .success(false)
                    .message("Serial number is null or empty")
                    .scanTime(LocalDateTime.now())
                    .build();
        }
        try {
            Gage gage = gageRepository.findBySerialNumber(serialNumber.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Gage not found"));
            return mapToGageScanResponse(gage);
        } catch (Exception e) {
            return GageScanResponse.builder()
                    .success(false)
                    .message("Error scanning gage: " + e.getMessage())
                    .scanTime(LocalDateTime.now())
                    .build();
        }
    }

    // =============== BASIC CRUD OPERATIONS ===============
    @Override
    public GageResponse getGageById(Long id) {
        Gage gage = gageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gage not found with id: " + id));
        return mapToGageResponse(gage);
    }

    @Override
    public GageResponse getGageBySerialNumber(String serialNumber) {
        Gage gage = gageRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Gage not found with serial number: " + serialNumber));
        return mapToGageResponse(gage);
    }

    @Override
    public List<GageResponse> getAllGages() {
        return gageRepository.findAll().stream()
                .map(this::mapToGageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GageResponse updateGage(Long id, GageRequest gageRequest) {
        Gage existingGage = gageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gage not found with id: " + id));
        if (!existingGage.getSerialNumber().equals(gageRequest.getSerialNumber()) &&
                !isSerialNumberUnique(gageRequest.getSerialNumber())) {
            throw new IllegalArgumentException("Serial number already exists: " + gageRequest.getSerialNumber());
        }
        updateGageFromRequest(existingGage, gageRequest);
        Gage updatedGage = gageRepository.save(existingGage);
        return mapToGageResponse(updatedGage);
    }

    @Override
    public void deleteGage(Long id) {
        if (!gageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Gage not found with id: " + id);
        }
        gageRepository.deleteById(id);
    }

    @Override
    public GageResponse updateGageStatus(Long id, Gage.Status status) {
        Gage gage = gageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gage not found with id: " + id));
        gage.setStatus(status);
        Gage updatedGage = gageRepository.save(gage);
        return mapToGageResponse(updatedGage);
    }

    @Override
    public GageResponse inwardupdateGageStatus(Long id, Gage.Status status) {
        Gage gage = gageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gage not found with id: " + id));
        gage.setStatus(status);
        gage.setNextCalibrationDate(LocalDate.now().plusDays(30));
        Gage updatedGage = gageRepository.save(gage);
        return mapToGageResponse(updatedGage);
    }

    @Override
    public GageResponse issueGageBySerialNumber(String serialNumber) {
        Gage gage = gageRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Gage not found with serial number: " + serialNumber));
        if (gage.getStatus() == Gage.Status.ACTIVE) {
            System.out.println("🔄 Issuing gage by serial number: " + serialNumber + " (current status: ACTIVE)");
            gage.setStatus(Gage.Status.ISSUED);
            Gage updatedGage = gageRepository.save(gage);
            System.out.println("✅ Gage issued by serial number. New status: " + updatedGage.getStatus());
            return mapToGageResponse(updatedGage);
        } else if (gage.getStatus() == Gage.Status.ISSUED) {
            return mapToGageResponse(gage);
        } else {
            throw new IllegalArgumentException(
                    "Cannot issue gage with status: " + gage.getStatus() + ". Only ACTIVE gages can be issued.");
        }
    }

    @Override
    public boolean isSerialNumberUnique(String serialNumber) {
        return gageRepository.findBySerialNumber(serialNumber).isEmpty();
    }

    @Override
    public List<GageResponse> searchGages(String searchTerm) {
        return gageRepository.findAll().stream()
                .filter(gage -> gage.getSerialNumber().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        gage.getModelNumber().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        (gage.getGageType() != null
                                && gage.getGageType().getName().toLowerCase().contains(searchTerm.toLowerCase())))
                .map(this::mapToGageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GageResponse> getGagesByType(Long gageTypeId) {
        return gageRepository.findByGageTypeId(gageTypeId).stream()
                .map(this::mapToGageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GageResponse> getGagesBySubType(Long gageSubTypeId) {
        return gageRepository.findByGageSubTypeId(gageSubTypeId).stream()
                .map(this::mapToGageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GageResponse> getGagesByLocation(Gage.Location location) {
        return gageRepository.findByLocation(location).stream()
                .map(this::mapToGageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GageResponse> getGagesByCriticality(Gage.Criticality criticality) {
        return gageRepository.findByCriticality(criticality).stream()
                .map(this::mapToGageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GageResponse> getGagesByStatus(Gage.Status status) {
        return gageRepository.findByStatus(status).stream()
                .map(this::mapToGageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GageResponse> getGagesByTypeName(String gageTypeName) {
        return gageRepository.findByGageTypeName(gageTypeName).stream()
                .map(this::mapToGageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GageResponse> getGagesByInhouseCalibrationMachine(Long inhouseCalibrationMachineId) {
        return gageRepository.findByInhouseCalibrationMachineId(inhouseCalibrationMachineId).stream()
                .map(this::mapToGageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void updateAllGagesRemainingDays() {
        System.out.println("🔄 Starting daily update of remaining days for all gages...");
        List<Gage> allGages = gageRepository.findAll();
        int updatedCount = 0;
        for (Gage gage : allGages) {
            if (gage.getNextCalibrationDate() != null) {
                Integer newRemainingDays = calculateRemainingDays(gage.getNextCalibrationDate());
                gage.setRemainingDays(newRemainingDays);
                gageRepository.save(gage);
                updatedCount++;
                System.out.println("📅 Updated gage " + gage.getSerialNumber() +
                        " - Remaining days: " + newRemainingDays);
            }
        }
        System.out.println("✅ Daily update completed. Updated " + updatedCount + " gages.");
    }

    // =============== BUSINESS LOGIC HELPERS ===============
    private LocalDate calculatePendingCalibrationDate(LocalDate nextCalibrationDate, Gage.Criticality criticality) {
        if (nextCalibrationDate == null)
            return null;
        int extendDays = switch (criticality) {
            case HIGH -> 0;
            case MEDIUM -> 10;
            case LOW -> 15;
            default -> 0;
        };
        return nextCalibrationDate.plusDays(extendDays);
    }

    private Integer calculateRemainingDays(LocalDate nextCalibrationDate) {
        if (nextCalibrationDate == null)
            return null;
        LocalDate today = LocalDate.now();
        return (int) ChronoUnit.DAYS.between(today, nextCalibrationDate);
    }

    private GageSubTypeResponse mapToGageSubTypeResponse(GageSubType gageSubType) {
        return GageSubTypeResponse.builder()
                .id(gageSubType.getId())
                .name(gageSubType.getName())
                .description(gageSubType.getDescription())
                .build();
    }

    private InhouseCalibrationMachineResponse mapToInhouseCalibrationMachineResponse(InhouseCalibrationMachine machine) {
        return InhouseCalibrationMachineResponse.builder()
                .id(machine.getId())
                .machineName(machine.getMachineName())
                .gageTypeId(machine.getGageType().getId())
                .gageTypeName(machine.getGageType().getName())
                .gageSubTypeId(machine.getGageSubType().getId())
                .gageSubTypeName(machine.getGageSubType().getName())
                .build();
    }

    private GageTypeResponse mapToGageTypeResponse(GageType gageType) {
        if (gageType == null)
            return null;
        return GageTypeResponse.builder()
                .id(gageType.getId())
                .name(gageType.getName())
                .description(gageType.getDescription())
                .build();
    }

    // =============== GAGE USAGE OPERATIONS ===============
    @Override
    public GageUsageResponse validateGageForUsage(String gageType, String serialNumber) {
        System.out.println("🔍 Validating gage for usage - Type: " + gageType + ", Serial: " + serialNumber);
        Optional<Gage> gageOpt = gageRepository.findBySerialNumber(serialNumber);
        if (gageOpt.isEmpty()) {
            return GageUsageResponse.builder()
                    .serialNumber(serialNumber)
                    .gageType(gageType)
                    .isValidSerial(false)
                    .validationMessage("Gage with serial number '" + serialNumber + "' not found")
                    .build();
        }
        Gage gage = gageOpt.get();
        if (!gage.getGageType().getName().equalsIgnoreCase(gageType)) {
            return GageUsageResponse.builder()
                    .serialNumber(serialNumber)
                    .gageType(gageType)
                    .isValidSerial(false)
                    .validationMessage("Serial number '" + serialNumber + "' belongs to gage type '" +
                            gage.getGageType().getName() + "', not '" + gageType + "'")
                    .build();
        }
        if (gage.getStatus() == Gage.Status.INACTIVE || gage.getStatus() == Gage.Status.OUT_OF_STORE) {
            return GageUsageResponse.builder()
                    .serialNumber(serialNumber)
                    .gageType(gageType)
                    .isValidSerial(false)
                    .validationMessage("Gage is currently " + gage.getStatus() + " and not available for use")
                    .build();
        }
        Integer totalUsesUsed = getTotalUsesForGage(serialNumber);
        Integer currentUsesCount = gage.getMaxUsersNumber() - totalUsesUsed;
        return GageUsageResponse.builder()
                .serialNumber(serialNumber)
                .gageType(gage.getGageType().getName())
                .modelNumber(gage.getModelNumber())
                .nextCalibrationDate(gage.getNextCalibrationDate())
                .pendingCalibrationDate(gage.getPendingCalibrationDate())
                .remainingDays(gage.getRemainingDays())
                .maxUsersNumber(gage.getMaxUsersNumber())
                .currentUsesCount(Math.max(0, currentUsesCount))
                .isValidSerial(true)
                .validationMessage("Gage validated successfully and available for use")
                .build();
    }

    @Override
    public GageUsageResponse recordGageUsage(GageUsageRequest usageRequest) {
        System.out.println("📝 Recording gage usage for serial: " + usageRequest.getSerialNumber());
        GageUsageResponse validation = validateGageForUsage(usageRequest.getGageType(), usageRequest.getSerialNumber());
        if (!validation.getIsValidSerial()) {
            return validation;
        }
        Gage gage = gageRepository.findBySerialNumber(usageRequest.getSerialNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Gage not found"));
        if (gage.getRemainingDays() != null && gage.getRemainingDays() < usageRequest.getDaysUsed()) {
            return GageUsageResponse.builder()
                    .serialNumber(usageRequest.getSerialNumber())
                    .isValidSerial(false)
                    .validationMessage("Insufficient remaining calibration days. Remaining: " +
                            gage.getRemainingDays() + ", Requested: " + usageRequest.getDaysUsed())
                    .build();
        }
        Integer totalUsesUsed = getTotalUsesForGage(usageRequest.getSerialNumber());
        Integer currentUsesCount = gage.getMaxUsersNumber() - totalUsesUsed;
        if (currentUsesCount < usageRequest.getUsesCount()) {
            return GageUsageResponse.builder()
                    .serialNumber(usageRequest.getSerialNumber())
                    .isValidSerial(false)
                    .validationMessage("Insufficient remaining uses. Remaining: " +
                            currentUsesCount + ", Requested: " + usageRequest.getUsesCount())
                    .build();
        }
        String description = "Gage usage recorded";
        if (usageRequest.getJobDescription() != null && !usageRequest.getJobDescription().trim().isEmpty()) {
            description += " for " + usageRequest.getJobDescription();
        }
        Job gageUsage = Job.builder()
                .jobNumber(usageRequest.getJobNumber())
                .jobDescription(usageRequest.getJobDescription())
                .title("Gage Usage - " + usageRequest.getGageType())
                .description(description)
                .status(Job.Status.COMPLETED)
                .priority(Job.Priority.MEDIUM)
                .createdBy(usageRequest.getOperatorUsername())
                .assignedTo(usageRequest.getOperatorUsername())
                .usageDate(usageRequest.getUsageDate())
                .department(usageRequest.getDepartment())
                .functionName(usageRequest.getFunctionName())
                .operationName(usageRequest.getOperationName())
                .gageType(usageRequest.getGageType())
                .gageSerialNumber(usageRequest.getSerialNumber())
                .daysUsed(usageRequest.getDaysUsed())
                .usesCount(usageRequest.getUsesCount())
                .operatorUsername(usageRequest.getOperatorUsername())
                .operatorRole(usageRequest.getOperatorRole())
                .operatorFunction(usageRequest.getOperatorFunction())
                .operatorOperation(usageRequest.getOperatorOperation())
                .usageCount(usageRequest.getUsageCount())
                .usageNotes(usageRequest.getUsageNotes())
                .build();
        Job savedUsage = jobRepository.save(gageUsage);
        if (gage.getRemainingDays() != null) {
            Integer newRemainingDays = gage.getRemainingDays() - usageRequest.getDaysUsed();
            gage.setRemainingDays(Math.max(0, newRemainingDays));
            gageRepository.save(gage);
        }
        Integer newTotalUsesUsed = getTotalUsesForGage(usageRequest.getSerialNumber());
        Integer newCurrentUsesCount = gage.getMaxUsersNumber() - newTotalUsesUsed;
        return GageUsageResponse.builder()
                .id(savedUsage.getId())
                .serialNumber(usageRequest.getSerialNumber())
                .gageType(gage.getGageType().getName())
                .modelNumber(gage.getModelNumber())
                .nextCalibrationDate(gage.getNextCalibrationDate())
                .pendingCalibrationDate(gage.getPendingCalibrationDate())
                .remainingDays(gage.getRemainingDays())
                .maxUsersNumber(gage.getMaxUsersNumber())
                .currentUsesCount(Math.max(0, newCurrentUsesCount))
                .daysUsed(usageRequest.getDaysUsed())
                .usesCount(usageRequest.getUsesCount())
                .operatorUsername(usageRequest.getOperatorUsername())
                .operatorRole(usageRequest.getOperatorRole())
                .operatorFunction(usageRequest.getOperatorFunction())
                .operatorOperation(usageRequest.getOperatorOperation())
                .usageDate(usageRequest.getUsageDate())
                .jobDescription(usageRequest.getJobDescription())
                .jobNumber(usageRequest.getJobNumber())
                .usageCount(usageRequest.getUsageCount())
                .usageNotes(usageRequest.getUsageNotes())
                .isValidSerial(true)
                .validationMessage("Gage usage recorded successfully")
                .build();
    }

    @Override
    public List<GageResponse> getGagesByType(String gageTypeName) {
        return gageRepository.findByGageTypeName(gageTypeName).stream()
                .map(this::mapToGageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GageResponse> getFilteredGages(String department, String function, String operation) {
        return gageRepository.findAll().stream()
                .map(this::mapToGageResponse)
                .collect(Collectors.toList());
    }

    private Integer getTotalUsesForGage(String serialNumber) {
        List<Job> usageRecords = jobRepository.findByGageSerialNumberAndUsesCountIsNotNull(serialNumber);
        return usageRecords.stream()
                .mapToInt(record -> record.getUsesCount() != null ? record.getUsesCount() : 0)
                .sum();
    }

}