package com.library.app.model;

import java.time.LocalDateTime;

public class AppNotification {
   private long id;
   private String notificationKey;
   private String type;
   private String title;
   private String message;
   private String targetKey;
   private String priority;
   private boolean read;
   private LocalDateTime createdAt;
   private LocalDateTime readAt;

   public long getId() {
      return id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public String getNotificationKey() {
      return notificationKey;
   }

   public void setNotificationKey(String notificationKey) {
      this.notificationKey = notificationKey;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getMessage() {
      return message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public String getTargetKey() {
      return targetKey;
   }

   public void setTargetKey(String targetKey) {
      this.targetKey = targetKey;
   }

   public String getPriority() {
      return priority;
   }

   public void setPriority(String priority) {
      this.priority = priority;
   }

   public boolean isRead() {
      return read;
   }

   public void setRead(boolean read) {
      this.read = read;
   }

   public LocalDateTime getCreatedAt() {
      return createdAt;
   }

   public void setCreatedAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
   }

   public LocalDateTime getReadAt() {
      return readAt;
   }

   public void setReadAt(LocalDateTime readAt) {
      this.readAt = readAt;
   }
}