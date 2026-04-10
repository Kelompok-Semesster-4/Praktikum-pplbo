package com.library.app.session;

import com.library.app.model.User;
import com.library.app.model.enums.Role;

public class UserSession {
   private final Long userId;
   private final String username;
   private final Role role;

   public UserSession(User user) {
      this.userId = user.getId();
      this.username = user.getUsername();
      this.role = user.getRole();
   }

   public Long getUserId() {
      return userId;
   }

   public String getUsername() {
      return username;
   }

   public Role getRole() {
      return role;
   }
}
