/*
 * @author Muhammad Ubaid Ur Raheem Ahmad AKA Shahbaz Haroon
 * Email: shahbazhrn@gmail.com
 * Cell: +923002585925
 * GitHub: https://github.com/ShahbazHaroon
 */

package com.ubaidsample.inventory.dto.request;

import lombok.Getter;

import java.util.Optional;

@Getter
public class UserPartialUpdateRequestDTO {

    private Optional<String> userName = Optional.empty();
    private Optional<String> email = Optional.empty();
    private Optional<String> password = Optional.empty();
    private Optional<LocalDate> dateOfBirth = Optional.empty();
    private Optional<LocalDate> dateOfLeaving = Optional.empty();
    private Optional<Integer> postalCode = Optional.empty();
}
