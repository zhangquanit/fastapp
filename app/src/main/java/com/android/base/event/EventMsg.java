package com.android.base.event;

/**
 * EventBus推送事件
 * <p>
 * 不同事件通过action来区分，避免定义很多的XXEvent类
 * </p>
 *
 * @authos 张全
 */
public class EventMsg {
    private String action;
    private Object data;

    public EventMsg(String action) {
        this.action = action;
    }

    public EventMsg(String action, Object data) {
        this.action = action;
        this.data = data;
    }

    public String getAction() {
        return this.action;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return this.data;
    }
}