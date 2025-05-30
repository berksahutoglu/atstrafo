package com.example.talepyonetimbackend.talepyonetim.config;

import com.example.talepyonetimbackend.talepyonetim.model.Role;
import com.example.talepyonetimbackend.talepyonetim.model.User;
import com.example.talepyonetimbackend.talepyonetim.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Örnek kullanıcıları sadece yeni kurulumda ekle
            if (userRepository.count() == 0) {
                // Talep eden kullanıcı
                User requester = new User();
                requester.setUsername("requester");
                requester.setPassword(passwordEncoder.encode("123456"));
                requester.setFirstName("Talep");
                requester.setLastName("Eden");
                requester.setEmail("talep@sirket.com");
                requester.setRole(Role.ROLE_REQUESTER);
                requester.setEnabled(true);
                userRepository.save(requester);

                // Onay veren kullanıcı
                User approver = new User();
                approver.setUsername("approver");
                approver.setPassword(passwordEncoder.encode("123456"));
                approver.setFirstName("Onay");
                approver.setLastName("Veren");
                approver.setEmail("onay@sirket.com");
                approver.setRole(Role.ROLE_APPROVER);
                approver.setEnabled(true);
                userRepository.save(approver);

                // Teslim alan kullanıcı
                User receiver = new User();
                receiver.setUsername("receiver");
                receiver.setPassword(passwordEncoder.encode("123456"));
                receiver.setFirstName("Teslim");
                receiver.setLastName("Alan");
                receiver.setEmail("teslim@sirket.com");
                receiver.setRole(Role.ROLE_RECEIVER);
                receiver.setEnabled(true);
                userRepository.save(receiver);
                
                // Satış ve Pazarlama kullanıcısı
                User salesAndMarketing = new User();
                salesAndMarketing.setUsername("sales");
                salesAndMarketing.setPassword(passwordEncoder.encode("123456"));
                salesAndMarketing.setFirstName("Satış");
                salesAndMarketing.setLastName("Pazarlama");
                salesAndMarketing.setEmail("satis@sirket.com");
                salesAndMarketing.setRole(Role.ROLE_SALESANDMARKETING);
                salesAndMarketing.setEnabled(true);
                userRepository.save(salesAndMarketing);
                
                // Üretim kullanıcısı
                User production = new User();
                production.setUsername("production");
                production.setPassword(passwordEncoder.encode("123456"));
                production.setFirstName("Üretim");
                production.setLastName("Sorumlusu");
                production.setEmail("uretim@sirket.com");
                production.setRole(Role.ROLE_PRODUCTION);
                production.setEnabled(true);
                userRepository.save(production);

                System.out.println("Örnek kullanıcılar oluşturuldu!");
            }
        };
    }
}
