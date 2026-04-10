package com.library.app.service;

import com.library.app.dao.UserDAO;
import com.library.app.model.User;
import com.library.app.util.PasswordUtil;
import com.library.app.util.ValidationUtil;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) {
        ValidationUtil.requireNotBlank(username, "Username wajib diisi.");
        ValidationUtil.requireNotBlank(password, "Password wajib diisi.");

        User user = userDAO.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("Username atau password salah."));

        if (!PasswordUtil.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Username atau password salah.");
        }
        return user;
    }
}
