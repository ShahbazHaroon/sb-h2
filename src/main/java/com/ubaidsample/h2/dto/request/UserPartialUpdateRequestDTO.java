/*
 * @author Muhammad Ubaid Ur Raheem Ahmad AKA Shahbaz Haroon
 * Email: shahbazhrn@gmail.com
 * Cell: +923002585925
 * GitHub: https://github.com/ShahbazHaroon
 */

package com.ubaidsample.h2.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPartialUpdateRequestDTO {

    @Size(min = 4, max = 50)
    @JsonProperty("user_name")
    private String userName;

    @Email
    @JsonProperty("email")
    private String email;

    @Size(min = 4, max = 255)
    @JsonProperty("password")
    private String password;

    @Past
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    @Future
    @JsonProperty("date_of_leaving")
    private LocalDate dateOfLeaving;

    @Digits(integer = 5, fraction = 0)
    @Positive
    @JsonProperty("postal_code")
    private Integer postalCode;
}