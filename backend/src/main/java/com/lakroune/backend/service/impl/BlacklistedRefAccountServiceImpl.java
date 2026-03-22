package com.lakroune.backend.service.impl;

import com.lakroune.backend.entity.BlacklistedRefAccount;
import com.lakroune.backend.repository.BlacklistedRefAccountRepository;
import com.lakroune.backend.service.IBlacklistedRefAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlacklistedRefAccountServiceImpl  implements IBlacklistedRefAccountService {

    private final BlacklistedRefAccountRepository blacklistedRefAccountRepository ;

    @Override
    public String addReferenceAccount() {
        String ref ="";
        do {
            ref = generateRef();
        } while (blacklistedRefAccountRepository.existsByRefAccount(ref));

        BlacklistedRefAccount blacklistedRefAccount = BlacklistedRefAccount.builder()
                .refAccount(ref)
                .build();

        try {
            blacklistedRefAccountRepository.save(blacklistedRefAccount);
        } catch (Exception e) {
            e.printStackTrace();  // Affiche la vraie cause
            System.out.println("Cause réelle : " + e.getCause());
        }

        return ref;
    }

    private String generateRef(){
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 100);
        return timestamp + String.format("%02d", random);
    }
}
