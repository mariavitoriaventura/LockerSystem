package org.project.demo.config;

import jakarta.annotation.PostConstruct;
import org.project.demo.enums.LockerStatus;
import org.project.demo.model.Locker;
import org.project.demo.model.User;
import org.project.demo.repository.LockerRepository;
import org.project.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
@Component
public class DataInitializer  implements CommandLineRunner {

    @Autowired
    private LockerRepository lockerRepository;

    @Override
    public void run(String... args) throws Exception {
        if(lockerRepository.count() == 0) {
            for(int i = 1; i <= 30; i++) {
                Locker locker = Locker.builder()
                        .number(i)
                        .status(LockerStatus.FREE)
                        .build();
                lockerRepository.save(locker);
            }
        }
    }
}
