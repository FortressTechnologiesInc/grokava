package com.goldenkat.bank.controller;

import com.goldenkat.bank.dto.AccountCreationDto;
import com.goldenkat.bank.model.Account;
import com.goldenkat.bank.model.User;
import com.goldenkat.bank.service.AccountService;
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

import java.util.List;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;

    @GetMapping
    public String listAccounts(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow();

        List<Account> accounts = accountService.findByUserId(user.getId());

        if (!model.containsAttribute("accountDto")) {
            model.addAttribute("accountDto", new AccountCreationDto());
        }

        model.addAttribute("accounts", accounts);
        model.addAttribute("user", user);
        return "accounts";
    }

    @PostMapping
    public String createAccount(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @ModelAttribute("accountDto") AccountCreationDto dto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow();

        if (bindingResult.hasErrors()) {
            model.addAttribute("accounts", accountService.findByUserId(user.getId()));
            model.addAttribute("user", user);
            return "accounts";
        }

        accountService.createAccount(user.getId(), dto.getAccountType());
        redirectAttributes.addFlashAttribute("message", "New account created successfully!");
        return "redirect:/accounts";
    }
}
