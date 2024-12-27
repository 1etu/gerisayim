package com.etu.countdown;

public class Timer {
    private int seconds;
    private TimerType type;
    private String content;

    public Timer(int seconds, TimerType type, String content) {
        this.seconds = seconds;
        this.type = type;
        this.content = content;
    }

    public int getSeconds() {
        return seconds;
    }

    public TimerType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }
}