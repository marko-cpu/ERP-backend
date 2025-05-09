package com.app.erp.dto.auth;


public class LoginDto {

    private String email;
    private String password;

    public LoginDto() {}

    public LoginDto(String email, String password) {
        this.email = email;
        this.password = password;
    }


}
