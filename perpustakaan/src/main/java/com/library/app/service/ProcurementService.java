package com.library.app.service;

import com.library.app.dao.ProcurementRequestDAO;
import com.library.app.model.Member;
import com.library.app.model.ProcurementRequest;
import com.library.app.model.enums.RequestStatus;
import com.library.app.util.GlobalEventPublisher;
import com.library.app.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.List;

public class ProcurementService {
    private final ProcurementRequestDAO requestDAO = new ProcurementRequestDAO();
    private final NotificationService notificationService = new NotificationService();

    public ProcurementRequest registerRequest(String requesterName, String title, String author, String note) {
        return registerRequest(null, requesterName, title, author, null, null, null, note);
    }

    public ProcurementRequest registerRequest(Long memberId,
                                              String requesterName,
                                              String title,
                                              String author,
                                              String publisher,
                                              Integer publicationYear,
                                              String isbn,
                                              String note) {
        ValidationUtil.requireNotBlank(requesterName, "Nama pemohon wajib diisi.");
        ValidationUtil.requireNotBlank(title, "Judul buku wajib diisi.");
        ValidationUtil.requireNotBlank(author, "Nama pengarang wajib diisi.");
        ValidationUtil.requireNotBlank(note, "Alasan permintaan wajib diisi.");
        if (publicationYear != null) {
            ValidationUtil.requirePublicationYear(publicationYear);
        }
        validateIsbn(isbn);

        ProcurementRequest request = new ProcurementRequest();
        request.setMemberId(memberId);
        request.setRequesterName(requesterName.trim());
        request.setTitle(title.trim());
        request.setAuthor(author.trim());
        request.setPublisher(normalizeOptional(publisher));
        request.setPublicationYear(publicationYear);
        request.setIsbn(normalizeOptional(isbn));
        request.setNote(note.trim());
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        requestDAO.save(request);

        notificationService.createProcurementNotification(request);
        GlobalEventPublisher.publishProcurementUpdated();
        return request;
    }

    public List<ProcurementRequest> findAll() {
        return requestDAO.findAll();
    }

    public List<ProcurementRequest> getAllRequests() {
        return findAll();
    }

    public ProcurementRequest submitRequest(String memberCode,
                                            String requesterName,
                                            String title,
                                            String author,
                                            String note) {
        Long memberId = null;
        String resolvedRequesterName = requesterName;

        if (memberCode != null && !memberCode.trim().isEmpty()) {
            Member member = new MemberService().findByCode(memberCode.trim());
            memberId = member.getId();
            if (resolvedRequesterName == null || resolvedRequesterName.trim().isEmpty()) {
                resolvedRequesterName = member.getName();
            }
        }

        return registerRequest(memberId, resolvedRequesterName, title, author, null, null, null, note);
    }

    public void reviewRequest(Long requestId, RequestStatus status, String responseNote) {
        requestDAO.reviewRequest(requestId, status, normalizeOptional(responseNote));
    }

    private String normalizeOptional(String value) {
        return ValidationUtil.isBlank(value) ? null : value.trim();
    }

    private void validateIsbn(String isbn) {
        if (ValidationUtil.isBlank(isbn)) {
            return;
        }
        String normalized = isbn.replaceAll("[-\\s]", "");
        if (!normalized.matches("\\d{10}|\\d{13}")) {
            throw new IllegalArgumentException("ISBN harus 10 atau 13 digit.");
        }
    }
}