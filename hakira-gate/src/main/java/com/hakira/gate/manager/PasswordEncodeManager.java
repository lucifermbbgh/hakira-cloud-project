package com.hakira.gate.manager;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: hakira-gate
 * @BelongsPackage: com.hakira.gate.manager
 * @Author: hakiraKafka
 * @CreateTime: 2024-02-01  16:10:41
 * @Description: TODO
 * @Version: 1.0
 */
@Component
public class PasswordEncodeManager {
    public String encodeByBCryptPasswordEncoder(String enablePswd) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(enablePswd);
    }

    public String encodeByDelegatingPasswordEncoder(String enablePswd){
        DelegatingPasswordEncoder delegatingPasswordEncoder = (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
        return delegatingPasswordEncoder.encode(enablePswd);
    }
}
