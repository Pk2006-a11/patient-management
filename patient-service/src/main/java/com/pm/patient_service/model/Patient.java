package com.pm.patient_service.model;

import java.time.LocalDate;
//import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
//import jakarta.annotation.Generated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
//import lombok.Data;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Entity 
public class Patient {
    //UserId
    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    private UUID id;

    public UUID getId()
    {
        return id;
    }
    
    public void setId(UUID id)
    {
        this.id=id;
    }

    //UserName

    @NotNull 
    private String name;

    public @NotNull String getName()
    {
        return name;
    }

    public void setName( @NotNull String name)
    {
        this.name=name;
    }
    
    //EMAIL
    @NotNull 
    @Email 
    @Column(unique = true)
    private String email;

    public @NotNull @Email String getEmail()
    {
        return email;
    }

    public void setEmail(@NotNull @Email String email)
    {
        this.email=email;
    }

    //ADDRESS
    @NotNull 
    private String address;

    public @NotNull String getAddress()
    {
        return address;
    }

    public void setAddress(@NotNull String address)
    {
        this.address=address;
    }

    //DOB
    @NotNull 
    private LocalDate dateOfBirth;

    public @NotNull LocalDate getDateOfBirth()
    {
        return dateOfBirth;
    }

    public void setDateOfBirth(@NotNull LocalDate dateOfBirth)
    {
        this.dateOfBirth=dateOfBirth;
    }

    //REGISTRATION DATE
    @NotNull 
    private LocalDate registeredDate;

    public @NotNull LocalDate getRegisteredDate()
    {
        return registeredDate;
    }
    public void setRegisteredDate(@NotNull LocalDate registeredDate)
    {
        this.registeredDate=registeredDate;
    }

}
