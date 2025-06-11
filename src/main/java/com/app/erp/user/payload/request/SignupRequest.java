package com.app.erp.user.payload.request;

import jakarta.validation.constraints.*;

public class SignupRequest {


  @Size(max = 50)
  @Email
  private String email;

  @NotBlank
  @Size(min = 6, max = 40)
  private String password;

  @NotBlank
  @Size(max = 30)
  private String firstName;

  @NotBlank
  @Size(max = 30)
  private String lastName;

  @NotBlank
  @Size(max = 15)
  private String phoneNumber;

  @NotBlank
  @Size(max = 64)
  private String city;

  @NotBlank
  @Size(max = 64)
  private String address;

  @NotBlank
  @Size(max = 64)
  private String postalCode;


  // Getteri i setteri
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }


  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public  String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getCity() {
    return city;
  }

  public void setCity( String city) {
    this.city = city;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }
}
