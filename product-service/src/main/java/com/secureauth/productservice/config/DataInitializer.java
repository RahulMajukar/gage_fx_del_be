package com.secureauth.productservice.config;

import com.secureauth.productservice.entity.*;
import com.secureauth.productservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private GageSubTypeRepository gageSubTypeRepository;

    @Autowired
    private GageTypeRepository gageTypeRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private GageRepository gageRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize gage sub-types first (required for gage types)
        if (gageSubTypeRepository.count() == 0) {
            initializeGageSubTypes();
        }

        // Initialize gage types if they don't exist
        if (gageTypeRepository.count() == 0) {
            initializeGageTypes();
        }

        // Initialize manufacturers if they don't exist
        if (manufacturerRepository.count() == 0) {
            initializeManufacturers();
        }

        // Initialize suppliers if they don't exist
        if (supplierRepository.count() == 0) {
            initializeSuppliers();
        }

        // Initialize service providers if they don't exist
        if (serviceProviderRepository.count() == 0) {
            initializeServiceProviders();
        }

        // Initialize gages if they don't exist
        if (gageRepository.count() == 0) {
            initializeGages();
        }
    }

    private void initializeGageSubTypes() {
        // Create sample gage sub-types
        GageSubType dimensional = GageSubType.builder()
                .name("Dimensional")
                .description("Dimensional measurement tools")
                .build();
        gageSubTypeRepository.save(dimensional);

        GageSubType mechanical = GageSubType.builder()
                .name("Mechanical")
                .description("Mechanical measurement tools")
                .build();
        gageSubTypeRepository.save(mechanical);

        GageSubType electrical = GageSubType.builder()
                .name("Electrical")
                .description("Electrical measurement tools")
                .build();
        gageSubTypeRepository.save(electrical);

        System.out.println("✅ Initialized " + gageSubTypeRepository.count() + " gage sub-types");
    }

    private void initializeGageTypes() {
        // Fetch gage sub-type (dimensional)
        GageSubType dimensional = gageSubTypeRepository.findByName("Dimensional").orElseThrow();

        // Create sample gage types
        GageType calipers = GageType.builder()
                .name("Calipers")
                .gageSubType(dimensional)
                .description("Precision measuring tool for dimensional measurements")
                .build();
        gageTypeRepository.save(calipers);

        GageType micrometers = GageType.builder()
                .name("Micrometers")
                .gageSubType(dimensional)
                .description("High-precision measuring instrument for small measurements")
                .build();
        gageTypeRepository.save(micrometers);

        GageType dialIndicators = GageType.builder()
                .name("Dial Indicators")
                .gageSubType(dimensional)
                .description("Precision measurement tool for small movements")
                .build();
        gageTypeRepository.save(dialIndicators);

        GageType heightGages = GageType.builder()
                .name("Height Gages")
                .gageSubType(dimensional)
                .description("Precision tool for measuring heights and depths")
                .build();
        gageTypeRepository.save(heightGages);

        GageType surfacePlates = GageType.builder()
                .name("Surface Plates")
                .gageSubType(dimensional)
                .description("Precision reference surface for measurements")
                .build();
        gageTypeRepository.save(surfacePlates);

        System.out.println("✅ Initialized " + gageTypeRepository.count() + " gage types");
    }

    private void initializeManufacturers() {
        // Create sample manufacturers
        Manufacturer mitutoyo = Manufacturer.builder()
                .name("Mitutoyo Corporation")
                .address("20-1, Sakado 1-chome, Takatsu-ku, Kawasaki-shi, Kanagawa 213-8533, Japan")
                .country("Japan")
                .contactPerson("John Smith")
                .phoneNumber("+81-44-813-8234")
                .email("info@mitutoyo.co.jp")
                .website("https://www.mitutoyo.co.jp")
                .description("Leading manufacturer of precision measuring instruments")
                .manufacturerType(Manufacturer.ManufacturerType.ORIGINAL_EQUIPMENT_MANUFACTURER)
                .certificationNumber("ISO9001-2023")
                .build();
        manufacturerRepository.save(mitutoyo);

        Manufacturer starrett = Manufacturer.builder()
                .name("The L.S. Starrett Company")
                .address("121 Crescent Street, Athol, MA 01331, USA")
                .country("USA")
                .contactPerson("Mike Johnson")
                .phoneNumber("+1-978-249-3551")
                .email("info@starrett.com")
                .website("https://www.starrett.com")
                .description("American manufacturer of precision tools and measuring instruments")
                .manufacturerType(Manufacturer.ManufacturerType.ORIGINAL_EQUIPMENT_MANUFACTURER)
                .certificationNumber("ISO9001-2023")
                .build();
        manufacturerRepository.save(starrett);

        System.out.println("✅ Initialized " + manufacturerRepository.count() + " manufacturers");
    }

    private void initializeSuppliers() {
        // Create sample suppliers
        Supplier precisionTools = Supplier.builder()
                .name("Precision Tools Supply Co.")
                .address("123 Industrial Blvd, Manufacturing District, TX 75001")
                .country("USA")
                .contactPerson("Sarah Wilson")
                .phoneNumber("+1-555-123-4567")
                .email("sales@precisiontools.com")
                .website("https://www.precisiontools.com")
                .invoicePONumber("PO-2024-001")
                .description("Authorized distributor of precision measuring tools")
                .supplierType(Supplier.SupplierType.AUTHORIZED_DISTRIBUTOR)
                .taxIdentificationNumber("12-3456789")
                .businessLicenseNumber("BL-2024-001")
                .paymentTerms(Supplier.PaymentTerms.NET_30)
                .build();
        supplierRepository.save(precisionTools);

        Supplier qualityInstruments = Supplier.builder()
                .name("Quality Instruments & Tools")
                .address("456 Measurement Ave, Lab District, CA 90210")
                .country("USA")
                .contactPerson("David Chen")
                .phoneNumber("+1-555-987-6543")
                .email("info@qualityinstruments.com")
                .website("https://www.qualityinstruments.com")
                .invoicePONumber("PO-2024-002")
                .description("Specialized supplier of high-precision measurement tools")
                .supplierType(Supplier.SupplierType.WHOLESALE_SUPPLIER)
                .taxIdentificationNumber("98-7654321")
                .businessLicenseNumber("BL-2024-002")
                .paymentTerms(Supplier.PaymentTerms.NET_30)
                .build();
        supplierRepository.save(qualityInstruments);

        System.out.println("✅ Initialized " + supplierRepository.count() + " suppliers");
    }

    private void initializeServiceProviders() {
        // Create sample service providers
        ServiceProvider calibrationServices = ServiceProvider.builder()
                .name("Calibration Services International")
                .accreditation(ServiceProvider.Accreditation.ISO_IEC_17025)
                .address("789 Calibration Drive, Lab District, NY 10001")
                .country("USA")
                .contactPerson("Dr. Robert Chen")
                .phoneNumber("+1-555-456-7890")
                .email("calibration@csi.com")
                .website("https://www.csi.com")
                .description("ISO 17025 accredited calibration laboratory")
                .serviceType(ServiceProvider.ServiceType.CALIBRATION_SERVICE)
                .serviceProviderType(ServiceProvider.ServiceProviderType.AUTHORIZED)
                .accreditationNumber("AC-2024-001")
                .accreditationExpiryDate(java.time.LocalDate.of(2025, 12, 31))
                .serviceAreas("Dimensional, Temperature, Pressure")
                .responseTime(ServiceProvider.ResponseTime.WITHIN_48_HOURS)
                .build();
        serviceProviderRepository.save(calibrationServices);

        ServiceProvider maintenancePro = ServiceProvider.builder()
                .name("Maintenance Pro Services")
                .accreditation(ServiceProvider.Accreditation.OTHER)
                .address("321 Service Street, Industrial Zone, TX 75002")
                .country("USA")
                .contactPerson("Lisa Rodriguez")
                .phoneNumber("+1-555-789-0123")
                .email("service@maintenancepro.com")
                .website("https://www.maintenancepro.com")
                .description("Professional maintenance and repair services")
                .serviceType(ServiceProvider.ServiceType.MAINTENANCE_SERVICE)
                .serviceProviderType(ServiceProvider.ServiceProviderType.AUTHORIZED)
                .accreditationNumber("AC-2024-002")
                .accreditationExpiryDate(java.time.LocalDate.of(2025, 6, 30))
                .serviceAreas("Mechanical, Electrical, Calibration")
                .responseTime(ServiceProvider.ResponseTime.WITHIN_24_HOURS)
                .build();
        serviceProviderRepository.save(maintenancePro);

        System.out.println("✅ Initialized " + serviceProviderRepository.count() + " service providers");
    }

    private void initializeGages() {
        // Fetch initialized entities
        GageType calipers = gageTypeRepository.findByName("Calipers").orElseThrow();
        GageType micrometers = gageTypeRepository.findByName("Micrometers").orElseThrow();
        GageSubType dimensional = gageSubTypeRepository.findByName("Dimensional").orElseThrow();
        Manufacturer mitutoyo = manufacturerRepository.findByName("Mitutoyo Corporation").orElseThrow();
        Manufacturer starrett = manufacturerRepository.findByName("The L.S. Starrett Company").orElseThrow();

        // Create sample gages
        LocalDate today = LocalDate.now();
        LocalDate nextCalDate = today.plusMonths(6);

        Gage mitutoyoCaliper = Gage.builder()
                .serialNumber("MIT-CAL-001")
                .modelNumber("500-196-30")
                .gageType(calipers)
                .gageSubType(dimensional)
                .status(Gage.Status.ACTIVE)
                .usageFrequency(Gage.UsageFrequency.DAILY)
                .criticality(Gage.Criticality.HIGH)
                .location(Gage.Location.SHOP_FLOOR)
                .measurementRange("0-150mm")
                .accuracy("±0.02mm")
                .purchaseDate(today.minusYears(1))
                .manufacturer(mitutoyo)
                .calibrationInterval(6)
                .nextCalibrationDate(nextCalDate)
                .maxUsersNumber(5)
                .notes("Standard digital caliper for shop floor use")
                .codeType(Gage.CodeType.BARCODE_ONLY)
                .build();
        gageRepository.save(mitutoyoCaliper);

        Gage starrettMicrometer = Gage.builder()
                .serialNumber("STR-MIC-001")
                .modelNumber("21-101-8")
                .gageType(micrometers)
                .gageSubType(dimensional)
                .status(Gage.Status.ACTIVE)
                .usageFrequency(Gage.UsageFrequency.WEEKLY)
                .criticality(Gage.Criticality.MEDIUM)
                .location(Gage.Location.LAB)
                .measurementRange("0-25mm")
                .accuracy("±0.001mm")
                .purchaseDate(today.minusYears(2))
                .manufacturer(starrett)
                .calibrationInterval(12)
                .nextCalibrationDate(nextCalDate.plusMonths(6))
                .maxUsersNumber(3)
                .notes("Outside micrometer for lab precision measurements")
                .codeType(Gage.CodeType.BARCODE_ONLY)
                .build();
        gageRepository.save(starrettMicrometer);

        Gage mitutoyoDialIndicator = Gage.builder()
                .serialNumber("MIT-DIA-001")
                .modelNumber("2116S-10")
                .gageType(gageTypeRepository.findByName("Dial Indicators").orElseThrow())
                .gageSubType(dimensional)
                .status(Gage.Status.ACTIVE)
                .usageFrequency(Gage.UsageFrequency.MONTHLY)
                .criticality(Gage.Criticality.LOW)
                .location(Gage.Location.WAREHOUSE)
                .measurementRange("0-10mm")
                .accuracy("0.01mm")
                .purchaseDate(today.minusMonths(6))
                .manufacturer(mitutoyo)
                .calibrationInterval(12)
                .nextCalibrationDate(nextCalDate.plusMonths(6))
                .maxUsersNumber(2)
                .notes("Dial indicator for occasional use")
                .codeType(Gage.CodeType.BARCODE_ONLY)
                .build();
        gageRepository.save(mitutoyoDialIndicator);

        System.out.println("✅ Initialized " + gageRepository.count() + " gages");
    }
}