package com.directharvest.backend.users.service;

import com.directharvest.backend.users.entity.User;
import com.directharvest.backend.users.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserSecurityLookupServiceImpl implements UserSecurityLookupService {

    private final UserRepository userRepository;

    public UserSecurityLookupServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<SecurityUserProjection> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).map(this::toProjection);
    }

    private SecurityUserProjection toProjection(User user) {
        return new SecurityUserProjection(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.isEnabled()
        );
    }
}

