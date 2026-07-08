package org.dspace.app.rest.model;

import java.util.UUID;

public class captcha {
    private UUID uuid;
    private  String base64captch;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getBase64captch() {
        return base64captch;
    }

    public void setBase64captch(String base64captch) {
        this.base64captch = base64captch;
    }
}
