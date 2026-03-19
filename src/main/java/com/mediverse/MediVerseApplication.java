package com.mediverse;

import com.mediverse.model.Role;
import com.mediverse.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MediVerseApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediVerseApplication.class, args);
	}

	@Bean
	CommandLineRunner seedRoles(RoleRepository roleRepository) {
		return args -> {
			for (Role.RoleType roleType : Role.RoleType.values()) {
				roleRepository.findByName(roleType).orElseGet(() -> {
					Role role = new Role();
					role.setName(roleType);
					return roleRepository.save(role);
				});
			}
		};
	}

}
