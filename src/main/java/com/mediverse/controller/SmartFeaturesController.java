package com.mediverse.controller;

import com.mediverse.security.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/smart")
public class SmartFeaturesController {

    @PostMapping("/symptom-checker")
    public ResponseEntity<?> checkSymptoms(@RequestBody Map<String, List<String>> request) {
        List<String> symptoms = request.get("symptoms");

        // Basic placeholder logic for "AI" Symptom Checker
        String possibleDiagnosis = "Unknown. Please consult a doctor.";
        String recommendedSpecialist = "General Physician";

        if (symptoms != null && (symptoms.contains("chest pain") || symptoms.contains("shortness of breath"))) {
            possibleDiagnosis = "Heart-related issue (Possible Myocardial Infarction) or Severe Respiratory Condition";
            recommendedSpecialist = "Cardiologist or Pulmonologist";
        } else if (symptoms != null && (symptoms.contains("headache") && symptoms.contains("nausea") && symptoms.contains("stiff neck"))) {
             possibleDiagnosis = "Possible Meningitis (Urgent Consultation Required)";
             recommendedSpecialist = "Neurologist or Emergency Department";
        } else if (symptoms != null && (symptoms.contains("headache") && symptoms.contains("nausea"))) {
             possibleDiagnosis = "Migraine or Viral infection";
             recommendedSpecialist = "Neurologist or General Physician";
        } else if (symptoms != null && (symptoms.contains("fever") && (symptoms.contains("cough") || symptoms.contains("sore throat")))) {
             possibleDiagnosis = "Common Cold, Flu, or Upper Respiratory Infection";
             recommendedSpecialist = "General Physician";
        } else if (symptoms != null && (symptoms.contains("abdominal pain") && symptoms.contains("bloating"))) {
             possibleDiagnosis = "Gastrointestinal issue (Indigestion or Gastritis)";
             recommendedSpecialist = "Gastroenterologist";
        }

        return ResponseEntity.ok(Map.of(
                "possibleDiagnosis", possibleDiagnosis,
                "recommendedSpecialist", recommendedSpecialist,
                "urgencyLevel", (possibleDiagnosis.contains("Urgent") || possibleDiagnosis.contains("Heart-related")) ? "HIGH" : "NORMAL",
                "disclaimer", "DISCLAIMER: This is an automated preliminary screening based on common medical literature and does NOT replace professional medical advice, diagnosis, or treatment. If you are experiencing a medical emergency, please call your local emergency services immediately."
        ));
    }
}
