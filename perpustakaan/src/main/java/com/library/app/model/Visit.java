package com.library.app.model;

import com.library.app.model.enums.VisitType;

import java.time.LocalDate;

public class Visit {
    private Long id;
    private Long memberId;
    private String visitorName;
    private String visitorIdentifier;
    private VisitType visitType;
    private String institution;
    private String purpose;
    private LocalDate visitDate;

    public Visit() {
    }

    public Visit(Long id, Long memberId, String visitorName, String visitorIdentifier, VisitType visitType,
                 String institution, String purpose, LocalDate visitDate) {
        this.id = id;
        this.memberId = memberId;
        this.visitorName = visitorName;
        this.visitorIdentifier = visitorIdentifier;
        this.visitType = visitType;
        this.institution = institution;
        this.purpose = purpose;
        this.visitDate = visitDate;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public String getVisitorIdentifier() {
        return visitorIdentifier;
    }

    public VisitType getVisitType() {
        return visitType;
    }

    public String getInstitution() {
        return institution;
    }

    public String getPurpose() {
        return purpose;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public void setVisitorIdentifier(String visitorIdentifier) {
        this.visitorIdentifier = visitorIdentifier;
    }

    public void setVisitType(VisitType visitType) {
        this.visitType = visitType;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }
}