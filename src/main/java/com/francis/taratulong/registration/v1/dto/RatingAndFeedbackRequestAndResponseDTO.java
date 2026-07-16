package com.francis.taratulong.registration.v1.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RatingAndFeedbackRequestAndResponseDTO(
        @NotBlank(message = "Rating required")
        @Min(value = 0, message = "Rating must be at least 0")
        @Max(value = 5, message = "Rating cannot exceed 5")
        Integer rating,
        String feedback
){
}
