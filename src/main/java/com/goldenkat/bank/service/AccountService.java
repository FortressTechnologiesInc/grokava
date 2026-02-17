package com.goldenkat.bank.service;

import com.goldenkat.bank.dto.AccountCreationDto;
import com.goldenkat.bank.model.Account;
import com.goldenkat.bank.model.AccountType;
import com.goldenkat.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Account createAccount(Long userId, AccountType type) {
        Account account = new Account();
        account.setUserId(userId);
        account.setAccountType(type);
        account.setAccountNumber(generateAccountNumber());
        return accountRepository.save(account);
    }

    public List<Account> findByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public Account findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    private String generateAccountNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder("GK");
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
