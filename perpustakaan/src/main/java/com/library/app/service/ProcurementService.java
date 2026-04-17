package com.library.app.service;

import com.library.app.dao.ProcurementRequestDAO;
import com.library.app.model.Member;
import com.library.app.model.ProcurementRequest;
import com.library.app.model.enums.RequestStatus;
import com.library.app.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.List;

public class ProcurementService {
    private final ProcurementRequestDAO requestDAO = new ProcurementRequestDAO();
    private final MemberService memberService = new MemberService();
    private final NotificationService notificationService = new NotificationService();

    public void submitRequest(String memberCode, String requesterName, String title, String author, String note) {
        ValidationUtil.requireNotBlank(requesterName, "Nama pengaju wajib diisi.");
        ValidationUtil.requireNotBlank(title, "Judul buku usulan wajib diisi.");

        ProcurementRequest request = new ProcurementRequest();
        if (memberCode != null && !memberCode.trim().isEmpty()) {
            Member member = memberService.findByCode(memberCode.trim());
            request.setMemberId(member.getId());
        }
        request.setRequesterName(requesterName.trim());
        request.setTitle(title.trim());
        request.setAuthor(author == null ? "" : author.trim());
        request.setNote(note == null ? "" : note.trim());
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        long requestId = requestDAO.save(request);
        notificationService.createProcurementNotification(requestId, request.getRequesterName(), request.getTitle());
    }

    public List<ProcurementRequest> getAllRequests() {
        return requestDAO.findAll();
    }

    public void reviewRequest(long requestId, RequestStatus status, String responseNote) {
        requestDAO.review(requestId, status, responseNote == null ? "" : responseNote.trim());
    }
}
