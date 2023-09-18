package com.ivoriechat.android.utils;

public enum ChatTypeEnum {
    CHAT_TO_EARN(1),
    PAY_TO_CHAT(2);

    private final Integer chatType;

    ChatTypeEnum(Integer chatType) {
        this.chatType = chatType;
    }

    public Integer getChatType() {
        return this.chatType;
    }
}
