package com.eventcollab.user.dto;

import com.eventcollab.user.domain.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "L email est obligatoire")
    @Email(message = "Format d email invalide")
    private String email;

    @NotBlank(message = "Le prenom est obligatoire")
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50)
    private String lastName;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Minimum 8 caracteres")
    private String password;

    private Role role = Role.USER;
}