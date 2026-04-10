package com.library.app.model;

import com.library.app.model.enums.MemberType;

public class Member {
    private Long id;
    private String memberCode;
    private String name;
    private MemberType memberType;
    private String major;
    private String phone;

    public Member() {
    }

    public Member(Long id, String memberCode, String name, MemberType memberType, String major, String phone) {
        this.id = id;
        this.memberCode = memberCode;
        this.name = name;
        this.memberType = memberType;
        this.major = major;
        this.phone = phone;
    }

    public boolean canBorrow(int activeLoanCount, int maxActiveLoans) {
        return activeLoanCount < maxActiveLoans;
    }

    public Long getId() {
        return id;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public String getName() {
        return name;
    }

    public MemberType getMemberType() {
        return memberType;
    }

    public String getMajor() {
        return major;
    }

    public String getPhone() {
        return phone;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMemberType(MemberType memberType) {
        this.memberType = memberType;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
