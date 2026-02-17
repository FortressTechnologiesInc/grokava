package com.goldenkat.bank.service;

import com.goldenkat.bank.dto.TransferDto;
import com.goldenkat.bank.model.Account;
import com.goldenkat.bank.model.Transaction;
import com.goldenkat.bank.model.TransactionType;
import com.goldenkat.bank.repository.AccountRepository;
import com.goldenkat.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public void deposit(Long accountId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setAccountId(accountId);
        tx.setType(TransactionType.DEPOSIT);
        tx.setAmount(amount);
        tx.setDescription(description != null ? description : "Deposit");
        transactionRepository.save(tx);
    }

    @Transactional
    public void withdraw(Long accountId, BigDecimal amount, String description) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setAccountId(accountId);
        tx.setType(TransactionType.WITHDRAWAL);
        tx.setAmount(amount.negate());
        tx.setDescription(description != null ? description : "Withdrawal");
        transactionRepository.save(tx);
    }

    @Transactional
    public void transfer(TransferDto dto) {
        Account from = accountRepository.findByAccountNumber(dto.getFromAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("From account not found"));
        Account to = accountRepository.findByAccountNumber(dto.getToAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("To account not found"));

        if (from.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(dto.getAmount()));
        accountRepository.save(from);

        to.setBalance(to.getBalance().add(dto.getAmount()));
        accountRepository.save(to);

        Transaction debitTx = new Transaction();
        debitTx.setAccountId(from.getId());
        debitTx.setType(TransactionType.TRANSFER);
        debitTx.setAmount(dto.getAmount().negate());
        debitTx.setDescription(dto.getDescription() != null ? dto.getDescription() : "Transfer to " + dto.getToAccountNumber());
        debitTx.setReference(dto.getToAccountNumber());
        transactionRepository.save(debitTx);

        Transaction creditTx = new Transaction();
        creditTx.setAccountId(to.getId());
        creditTx.setType(TransactionType.TRANSFER);
        creditTx.setAmount(dto.getAmount());
        creditTx.setDescription(dto.getDescription() != null ? dto.getDescription() : "Transfer from " + dto.getFromAccountNumber());
        creditTx.setReference(dto.getFromAccountNumber());
        transactionRepository.save(creditTx);
    }

    public List<Transaction> getTransactionsForAccount(Long accountId) {
        return transactionRepository.findByAccountIdOrderByTransactionDateDesc(accountId);
    }
}
