package com.goldenkat.bank.controller;

import com.goldenkat.bank.dto.TransferDto;
import com.goldenkat.bank.model.Account;
import com.goldenkat.bank.model.Transaction;
import com.goldenkat.bank.model.User;
import com.goldenkat.bank.service.AccountService;
import com.goldenkat.bank.service.TransactionService;
import com.goldenkat.bank.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final UserService userService;

    @GetMapping
    public String listTransactions(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow();
        List<Account> accounts = accountService.findByUserId(user.getId());

        if (!model.containsAttribute("transferDto")) {
            model.addAttribute("transferDto", new TransferDto());
        }

        if (!accounts.isEmpty()) {
            Long selectedAccountId = accounts.get(0).getId();
            List<Transaction> transactions = transactionService.getTransactionsForAccount(selectedAccountId);
            model.addAttribute("transactions", transactions);
        }

        model.addAttribute("accounts", accounts);
        model.addAttribute("user", user);
        return "transactions";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam Long accountId,
                          @RequestParam BigDecimal amount,
                          @RequestParam(required = false) String description,
                          RedirectAttributes redirectAttributes) {
        try {
            transactionService.deposit(accountId, amount, description);
            redirectAttributes.addFlashAttribute("success", "Deposit successful!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transactions";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam Long accountId,
                           @RequestParam BigDecimal amount,
                           @RequestParam(required = false) String description,
                           RedirectAttributes redirectAttributes) {
        try {
            transactionService.withdraw(accountId, amount, description);
            redirectAttributes.addFlashAttribute("success", "Withdrawal successful!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transactions";
    }

    @PostMapping("/transfer")
    public String performTransfer(@Valid @ModelAttribute("transferDto") TransferDto dto,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid transfer details");
            return "redirect:/transactions";
        }
        try {
            transactionService.transfer(dto);
            redirectAttributes.addFlashAttribute("success", "Transfer completed!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transactions";
    }
}
