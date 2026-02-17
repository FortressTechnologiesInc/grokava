package com.goldenkat.bank.dto;

import com.goldenkat.bank.model.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountCreationDto {
    @NotNull
    private AccountType accountType;
}
