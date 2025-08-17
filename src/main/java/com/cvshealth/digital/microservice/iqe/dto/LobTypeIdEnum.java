package com.cvshealth.digital.microservice.iqe.dto;

import com.cvshealth.digital.microservice.iqe.enums.LobEnum;

public enum LobTypeIdEnum {
    CLINIC(1),
    MHC(5),
    OPTICAL(3),
    CET(0),
    RxERP(6),
    POCT(8);

    public final Integer id;

    private LobTypeIdEnum(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return this.id;
    }

    public static Integer getLobId(LobEnum lob) {
        for(LobTypeIdEnum mode : values()) {
            if (mode.name().equalsIgnoreCase(lob.name())) {
                return mode.getId();
            }
        }

        return 0;
    }
}