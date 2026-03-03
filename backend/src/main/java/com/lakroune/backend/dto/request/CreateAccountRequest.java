package com.lakroune.backend.dto.request;

import java.util.UUID;

public record CreateAccountRequest (
        UUID userId
){
}
