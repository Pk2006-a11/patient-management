package com.pm.patient_service.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.pm.patient_service.grpc.BillingServiceGrpcClient;
import com.pm.patient_service.kafka.KafkaProducer;
//import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.stereotype.Service;

import com.pm.patient_service.dto.PatientRequestDTO;
import com.pm.patient_service.dto.PatientResponseDTO;
import com.pm.patient_service.exception.EmailAlreadyExistsException;
import com.pm.patient_service.exception.PatientNotFoundException;
import com.pm.patient_service.mapper.PatientMapper;
import com.pm.patient_service.model.Patient;
import com.pm.patient_service.repository.PatientRespository;

@Service 
public class PatientService {
    private final PatientRespository patientRespository;

    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public PatientService(PatientRespository patientRespository, BillingServiceGrpcClient billingServiceGrpcClient, KafkaProducer kafkaProducer)
    {
        this.patientRespository=patientRespository;
        this.billingServiceGrpcClient=billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getPatients()
    {
        List<Patient> patients=patientRespository.findAll();
        
        List<PatientResponseDTO> patientResponseDTOs = patients.stream()
                                                                .map(patient -> PatientMapper.toDTO(patient))
                                                                .toList();
        return patientResponseDTOs;
    }

    
    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO)
    {
        if(patientRespository.existsByEmail(patientRequestDTO.getEmail()))
        {
            throw new EmailAlreadyExistsException("A patient with this email " + "alraedy exists " +patientRequestDTO.getEmail());
        }


        Patient newpatient = patientRespository.save(PatientMapper.toModel(patientRequestDTO));

        billingServiceGrpcClient.createBillingAccount(newpatient.getId().toString(),newpatient.getName(),newpatient.getEmail());


        kafkaProducer.sendEvent(newpatient);
        return PatientMapper.toDTO(newpatient);

    }

    public PatientResponseDTO updatePatient(UUID Id, PatientRequestDTO patientRequestDTO)
    {
        Patient patient = patientRespository.findById(Id)
            .orElseThrow(() -> new PatientNotFoundException("Patient not found with this Id : " + Id));

        if(patientRespository.existsByEmailAndIdNot(patientRequestDTO.getEmail(),Id))
        {
            throw new EmailAlreadyExistsException("A patient with this email " + "alraedy exists " +patientRequestDTO.getEmail());
        }


        patient.setName(patientRequestDTO.getName());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatedPatient = patientRespository.save(patient);
        
        return PatientMapper.toDTO(updatedPatient);


    }
    public void deletePatient(UUID id)
    {
        patientRespository.deleteById(id);
    }

    
}
