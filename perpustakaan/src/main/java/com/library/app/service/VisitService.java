package com.library.app.service;

import com.library.app.dao.VisitDAO;
import com.library.app.model.Member;
import com.library.app.model.Visit;
import com.library.app.model.enums.VisitPresenceStatus;
import com.library.app.model.enums.VisitType;
import com.library.app.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

public class VisitService {
    private final VisitDAO visitDAO = new VisitDAO();
    private final MemberService memberService = new MemberService();

    public String recordMemberVisit(String memberCode) {
        Member member = memberService.findByCode(memberCode);

        Visit latestVisit = visitDAO.findLatestMemberVisitToday(member.getId()).orElse(null);
        if (latestVisit == null) {
            Visit visit = new Visit();
            visit.setMemberId(member.getId());
            visit.setVisitorName(member.getName());
            visit.setVisitorIdentifier(member.getMemberCode());
            visit.setVisitType(VisitType.MEMBER);
            visit.setVisitStatus(VisitPresenceStatus.DI_DALAM);
            visit.setInstitution(member.getMemberType().name());
            visit.setPurpose("Kunjungan perpustakaan");
            visit.setVisitDate(LocalDate.now());
            visitDAO.save(visit);
            return "Absen masuk berhasil. Status kunjungan: Di dalam.";
        }

        if (latestVisit.getVisitStatus() == VisitPresenceStatus.DI_DALAM) {
            visitDAO.updateStatus(latestVisit.getId(), VisitPresenceStatus.SELESAI);
            return "Absen keluar berhasil. Status kunjungan: Selesai.";
        }

        throw new IllegalArgumentException("Kunjungan anggota hari ini sudah selesai.");
    }

    public void recordGuestVisit(String guestName, String institution, String purpose) {
        ValidationUtil.requireNotBlank(guestName, "Nama tamu wajib diisi.");
        ValidationUtil.requireNotBlank(institution, "Instansi/asal tamu wajib diisi.");
        ValidationUtil.requireNotBlank(purpose, "Keperluan tamu wajib diisi.");

        Visit visit = new Visit();
        visit.setVisitorName(guestName.trim());
        visit.setVisitorIdentifier("-");
        visit.setVisitType(VisitType.GUEST);
        visit.setVisitStatus(VisitPresenceStatus.SELESAI);
        visit.setInstitution(institution.trim());
        visit.setPurpose(purpose.trim());
        visit.setVisitDate(LocalDate.now());
        visitDAO.save(visit);
    }

    public List<Visit> getRecentVisits() {
        return visitDAO.findRecent(50);
    }
}
