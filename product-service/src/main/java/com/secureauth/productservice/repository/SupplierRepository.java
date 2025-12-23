package com.secureauth.productservice.repository;

import com.secureauth.productservice.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByName(String name);
    
    List<Supplier> findByCountry(String country);
    
    List<Supplier> findBySupplierType(Supplier.SupplierType supplierType);
    
    List<Supplier> findByPaymentTerms(Supplier.PaymentTerms paymentTerms);
    
    @Query("SELECT s FROM Supplier s WHERE s.name LIKE %:searchTerm% OR s.country LIKE %:searchTerm% OR s.contactPerson LIKE %:searchTerm%")
    List<Supplier> searchSuppliers(@Param("searchTerm") String searchTerm);
    
    boolean existsByName(String name);
    

} 