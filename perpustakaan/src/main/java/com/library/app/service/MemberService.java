package com.library.app.service;

import com.library.app.dao.MemberDAO;
import com.library.app.model.Member;
import com.library.app.model.enums.MemberType;
import com.library.app.util.ValidationUtil;

import java.util.List;

public class MemberService {
    private final MemberDAO memberDAO = new MemberDAO();

    public Member registerMember(String memberCode, String name, MemberType memberType, String major, String phone) {
        ValidationUtil.requireStudentOrLecturerCode(memberCode, "NIM/NIS/NIDN harus numerik dan panjangnya sesuai.");
        ValidationUtil.requireNotBlank(name, "Nama anggota wajib diisi.");
        if (memberDAO.findByCode(memberCode).isPresent()) {
            throw new IllegalArgumentException("Kode anggota sudah terdaftar.");
        }

        Member member = new Member();
        member.setMemberCode(memberCode.trim());
        member.setName(name.trim());
        member.setMemberType(memberType);
        member.setMajor(major == null ? "" : major.trim());
        member.setPhone(phone == null ? "" : phone.trim());
        memberDAO.save(member);
        return member;
    }

    public Member findByCode(String memberCode) {
        ValidationUtil.requireStudentOrLecturerCode(memberCode, "Kode anggota wajib numerik.");
        return memberDAO.findByCode(memberCode.trim())
                .orElseThrow(() -> new IllegalArgumentException("Anggota tidak ditemukan."));
    }

    public List<Member> search(String keyword) {
        return memberDAO.search(keyword == null ? "" : keyword.trim());
    }
}
