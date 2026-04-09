package com.library.app.service;

import com.library.app.dao.FeedbackDAO;
import com.library.app.model.Feedback;
import com.library.app.model.Member;
import com.library.app.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.List;

public class FeedbackService {
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private final MemberService memberService = new MemberService();

    public void submitFeedback(String memberCode, String senderName, String message) {
        ValidationUtil.requireNotBlank(senderName, "Nama pengirim wajib diisi.");
        ValidationUtil.requireNotBlank(message, "Feedback wajib diisi.");

        Feedback feedback = new Feedback();
        if (memberCode != null && !memberCode.trim().isEmpty()) {
            Member member = memberService.findByCode(memberCode.trim());
            feedback.setMemberId(member.getId());
        }
        feedback.setSenderName(senderName.trim());
        feedback.setMessage(message.trim());
        feedback.setCreatedAt(LocalDateTime.now());
        feedbackDAO.save(feedback);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackDAO.findAll();
    }
}
