package com.medilab.gateway.entity;

import java.util.List;

public record UserDto(String username, List<String> roles) {

}
