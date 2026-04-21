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

        if (memberType == null) {
            throw new IllegalArgumentException("Tipe anggota wajib dipilih.");
        }

        String normalizedCode = memberCode.trim();
        if (memberDAO.findByCode(normalizedCode).isPresent()) {
            throw new IllegalArgumentException("Kode anggota sudah terdaftar.");
        }

        Member member = new Member();
        member.setMemberCode(normalizedCode);
        member.setName(name.trim());
        member.setMemberType(memberType);
        member.setMajor(major == null ? "" : major.trim());
        member.setPhone(phone == null ? "" : phone.trim());

        memberDAO.save(member);
        return member;
    }

    public Member updateMember(long id, String memberCode, String name, MemberType memberType, String major, String phone) {
        ValidationUtil.requireStudentOrLecturerCode(memberCode, "NIM/NIS/NIDN harus numerik dan panjangnya sesuai.");
        ValidationUtil.requireNotBlank(name, "Nama anggota wajib diisi.");

        if (memberType == null) {
            throw new IllegalArgumentException("Tipe anggota wajib dipilih.");
        }

        Member existing = memberDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Data anggota tidak ditemukan."));

        String normalizedCode = memberCode.trim();

        memberDAO.findByCode(normalizedCode).ifPresent(found -> {
            if (!found.getId().equals(existing.getId())) {
                throw new IllegalArgumentException("Kode anggota sudah terdaftar.");
            }
        });

        existing.setMemberCode(normalizedCode);
        existing.setName(name.trim());
        existing.setMemberType(memberType);
        existing.setMajor(major == null ? "" : major.trim());
        existing.setPhone(phone == null ? "" : phone.trim());

        memberDAO.update(existing);
        return existing;
    }

    public void deleteMember(long id) {
        Member existing = memberDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Data anggota tidak ditemukan."));

        memberDAO.deleteById(existing.getId());
    }

    public Member findById(long id) {
        return memberDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Anggota tidak ditemukan."));
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