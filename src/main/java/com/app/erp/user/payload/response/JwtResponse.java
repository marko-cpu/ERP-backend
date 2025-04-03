package com.app.erp.user.payload.response;

import java.util.List;

public class JwtResponse {
  private String token;
  private String type = "Bearer";
  private String email;
  private String fullName;

  private List<String> roles;

  public JwtResponse(String accessToken, String email, String fullName, List<String> roles) {
    this.token = accessToken;
    this.email = email;
    this.roles = roles;
    this.fullName = fullName;

  }

  public String getAccessToken() {
    return token;
  }

  public void setAccessToken(String accessToken) {
    this.token = accessToken;
  }

  public String getTokenType() {
    return type;
  }

  public void setTokenType(String tokenType) {
    this.type = tokenType;
  }


  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public List<String> getRoles() {
    return roles;
  }
}
