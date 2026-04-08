package org.dspace.app.rest.authn;

public enum OtpType {
    SMS,
    WHATSAPP,
    EMAIL;

    public static OtpType from(String value) {
        if (value == null) {
            return SMS;
        }
        try {
            return OtpType.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            return SMS; // safe default
        }
    }
}
