package com.mediverse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "patients")
public class Patient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Links to the authentication user

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String dateOfBirth;

    private String gender;

    private String contactNumber;

    private String address;

    @Column(columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    private String bloodGroup;

    private String emergencyContactName;

    private String emergencyContactNumber;

    private String insuranceProvider;

    private String insurancePolicyNumber;
}
