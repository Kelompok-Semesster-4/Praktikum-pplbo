package com.library.app.service;

import com.library.app.dao.VisitDAO;
import com.library.app.model.Member;
import com.library.app.model.Visit;
import com.library.app.model.enums.VisitPresenceStatus;
import com.library.app.model.enums.VisitType;
import com.library.app.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class VisitService {
    private final VisitDAO visitDAO = new VisitDAO();
    private final MemberService memberService = new MemberService();

    public String recordMemberVisit(String memberCode) {
        Member member = memberService.findByCode(memberCode);
        LocalDate today = LocalDate.now();

        // Automatically close unfinished member visits from previous days.
        visitDAO.closeOpenMemberVisitsBefore(today);

        Visit latestVisit = visitDAO.findLatestMemberVisit(member.getId()).orElse(null);
        if (latestVisit == null || latestVisit.getVisitDate() == null || latestVisit.getVisitDate().isBefore(today)) {
            return createMemberCheckIn(member, today);
        }

        if (latestVisit.getVisitDate().isAfter(today)) {
            throw new IllegalStateException("Tanggal kunjungan tidak valid.");
        }

        if (latestVisit.getVisitStatus() == VisitPresenceStatus.DI_DALAM) {
            visitDAO.checkoutMemberVisit(latestVisit.getId(), LocalTime.now().withSecond(0).withNano(0));
            return "Absen keluar berhasil. Status kunjungan: Selesai.";
        }

        return createMemberCheckIn(member, today);
    }

    public String recordGuestVisit(String guestName, String institution, String purpose) {
        ValidationUtil.requireNotBlank(guestName, "Nama tamu wajib diisi.");
        ValidationUtil.requireNotBlank(institution, "Instansi/asal tamu wajib diisi.");

        LocalDate today = LocalDate.now();

        // Automatically close unfinished guest visits from previous days.
        visitDAO.closeOpenGuestVisitsBefore(today);

        String normalizedGuestName = guestName.trim();
        String normalizedInstitution = institution.trim();
        Visit latestVisit = visitDAO.findLatestGuestVisitToday(normalizedGuestName, normalizedInstitution).orElse(null);

        if (latestVisit == null) {
            return createGuestCheckIn(normalizedGuestName, normalizedInstitution, purpose, today);
        }

        if (latestVisit.getVisitStatus() == VisitPresenceStatus.DI_DALAM) {
            visitDAO.checkoutGuestVisit(latestVisit.getId(), LocalTime.now().withSecond(0).withNano(0));
            return "Absen tamu keluar berhasil. Status kunjungan: Selesai.";
        }

        return createGuestCheckIn(normalizedGuestName, normalizedInstitution, purpose, today);
    }

    public String completeGuestVisit(long visitId) {
        if (visitId <= 0) {
            throw new IllegalArgumentException("Data kunjungan tamu tidak valid.");
        }

        Visit visit = visitDAO.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Data kunjungan tidak ditemukan."));

        if (visit.getVisitType() != VisitType.GUEST) {
            throw new IllegalArgumentException("Status hanya dapat diubah untuk absen tamu.");
        }

        if (visit.getVisitStatus() != VisitPresenceStatus.DI_DALAM) {
            throw new IllegalArgumentException("Status kunjungan tamu sudah selesai.");
        }

        visitDAO.checkoutGuestVisit(visitId, LocalTime.now().withSecond(0).withNano(0));
        return "Absen tamu keluar berhasil. Status kunjungan: Selesai.";
    }

    public List<Visit> getRecentVisits() {
        visitDAO.closeOpenMemberVisitsBefore(LocalDate.now());
        visitDAO.closeOpenGuestVisitsBefore(LocalDate.now());
        return visitDAO.findRecent(50);
    }

    private String createMemberCheckIn(Member member, LocalDate visitDate) {
        Visit visit = new Visit();
        visit.setMemberId(member.getId());
        visit.setVisitorName(member.getName());
        visit.setVisitorIdentifier(member.getMemberCode());
        visit.setVisitType(VisitType.MEMBER);
        visit.setVisitStatus(VisitPresenceStatus.DI_DALAM);
        visit.setInstitution(member.getMemberType().name());
        visit.setPurpose("Kunjungan perpustakaan");
        visit.setVisitDate(visitDate);
        visit.setCheckInTime(LocalTime.now().withSecond(0).withNano(0));
        visit.setCheckOutTime(null);
        visitDAO.save(visit);
        return "Absen masuk berhasil. Status kunjungan: Di dalam.";
    }

    private String createGuestCheckIn(String guestName, String institution, String purpose, LocalDate visitDate) {
        ValidationUtil.requireNotBlank(purpose, "Keperluan tamu wajib diisi.");

        Visit visit = new Visit();
        visit.setVisitorName(guestName);
        visit.setVisitorIdentifier("-");
        visit.setVisitType(VisitType.GUEST);
        visit.setVisitStatus(VisitPresenceStatus.DI_DALAM);
        visit.setInstitution(institution);
        visit.setPurpose(purpose.trim());
        visit.setVisitDate(visitDate);
        visit.setCheckInTime(LocalTime.now().withSecond(0).withNano(0));
        visit.setCheckOutTime(null);
        visitDAO.save(visit);
        return "Absen tamu masuk berhasil. Status kunjungan: Di dalam.";
    }
}
