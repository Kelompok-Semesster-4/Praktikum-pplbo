package com.library.app.service;

import com.library.app.dao.FeedbackDAO;
import com.library.app.model.Feedback;
import com.library.app.model.Member;
import com.library.app.model.enums.FeedbackStatus;
import com.library.app.util.GlobalEventPublisher;
import com.library.app.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.List;

public class FeedbackService {
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private final NotificationService notificationService = new NotificationService();

    public Feedback registerFeedback(String senderName, String message) {
        return registerFeedback(null, senderName, null, 0, message);
    }

    public Feedback registerFeedback(String senderName, String subject, int rating, String message) {
        return registerFeedback(null, senderName, subject, rating, message);
    }

    public Feedback registerFeedback(Long memberId, String senderName, String subject, int rating, String message) {
        ValidationUtil.requireNotBlank(senderName, "Nama pengirim wajib diisi.");
        ValidationUtil.requireNotBlank(message, "Pesan feedback wajib diisi.");
        String normalizedSubject = normalizeSubject(subject, message);
        int normalizedRating = normalizeRating(rating);

        Feedback feedback = new Feedback();
        feedback.setMemberId(memberId);
        feedback.setSenderName(senderName.trim());
        feedback.setSubject(normalizedSubject);
        feedback.setRating(normalizedRating);
        feedback.setMessage(message.trim());
        feedback.setStatus(FeedbackStatus.NEW);
        feedback.setCreatedAt(LocalDateTime.now());
        feedbackDAO.save(feedback);

        notificationService.createFeedbackNotification(feedback);
        GlobalEventPublisher.publishFeedbackUpdated();
        return feedback;
    }

    public List<Feedback> findAll() {
        return feedbackDAO.findAll();
    }

    public List<Feedback> getAllFeedback() {
        return findAll();
    }

    public Feedback submitFeedback(String memberCode, String senderName, String message) {
        Long memberId = null;
        if (memberCode != null && !memberCode.trim().isEmpty()) {
            Member member = new MemberService().findByCode(memberCode.trim());
            memberId = member.getId();
        }
        return registerFeedback(memberId, senderName, null, 0, message);
    }

    public void markAsRead(Long feedbackId) {
        feedbackDAO.markAsRead(feedbackId);
    }

    public void respond(Long feedbackId, String responseNote) {
        ValidationUtil.requireNotBlank(responseNote, "Respons admin tidak boleh kosong.");
        feedbackDAO.respond(feedbackId, responseNote.trim());
    }

    private String normalizeSubject(String subject, String message) {
        if (!ValidationUtil.isBlank(subject)) {
            return subject.trim();
        }
        String plainText = message == null ? "Feedback Pengguna" : message.trim();
        if (plainText.length() <= 48) {
            return plainText;
        }
        return plainText.substring(0, 48).trim() + "...";
    }

    private int normalizeRating(int rating) {
        if (rating < 0) {
            return 0;
        }
        return Math.min(rating, 5);
    }
}