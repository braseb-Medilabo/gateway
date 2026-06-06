package com.medilab.gateway.entity;

import jakarta.validation.constraints.NotBlank;

public record CredentialDto(@NotBlank(message = "A username is required") String username, 
                            @NotBlank(message = "A password is required") String password) {

}
