package com.directharvest.backend.auth.service;

public interface GoogleIdTokenVerifier {

    GoogleUserInfo verify(String idToken);

    record GoogleUserInfo(String subject, String email, String name) {
    }
}

