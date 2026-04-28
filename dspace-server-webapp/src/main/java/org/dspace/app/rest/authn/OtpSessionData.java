package org.dspace.app.rest.authn;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public class OtpSessionData {

    private String email;
    private String mobile;
    private String otpHash;
    private Date createdTime;
    private Date expiryTime;
    private boolean verified;
    // ✅ ADD THESE
    private long lastResendTime;
    private int resendCount;

    // Required for Redis / Jackson
    public OtpSessionData() {
    }

    public OtpSessionData(String email, String otpHash) {
        this.email = email;
        this.otpHash = otpHash;
        this.createdTime = new Date();
        this.expiryTime = new Date(System.currentTimeMillis() + 5 * 60 * 1000); // 5 min
        this.verified = false;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Date expiryTime) {
        this.expiryTime = expiryTime;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }


    // getters & setters
    public long getLastResendTime() {
        return lastResendTime;
    }

    public void setLastResendTime(long lastResendTime) {
        this.lastResendTime = lastResendTime;
    }

    public int getResendCount() {
        return resendCount;
    }

    public void setResendCount(int resendCount) {
        this.resendCount = resendCount;
    }


    @JsonIgnore
    public boolean isExpired() {
        return new Date().after(expiryTime);
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
