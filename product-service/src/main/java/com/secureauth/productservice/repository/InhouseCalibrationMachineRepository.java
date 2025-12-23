package com.secureauth.productservice.repository;

import com.secureauth.productservice.entity.InhouseCalibrationMachine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InhouseCalibrationMachineRepository extends JpaRepository<InhouseCalibrationMachine, Long> {
    Optional<InhouseCalibrationMachine> findByMachineName(String machineName);
    boolean existsByMachineName(String machineName);
    boolean existsByMachineNameAndIdNot(String machineName, Long id);

    Optional<InhouseCalibrationMachine> findByInstrumentCode(String instrumentCode);
    boolean existsByInstrumentCode(String instrumentCode);
    boolean existsByInstrumentCodeAndIdNot(String instrumentCode, Long id);

    Optional<InhouseCalibrationMachine> findByMachineEquipmentNumber(String machineEquipmentNumber);
    boolean existsByMachineEquipmentNumber(String machineEquipmentNumber);
    boolean existsByMachineEquipmentNumberAndIdNot(String machineEquipmentNumber, Long id);
}