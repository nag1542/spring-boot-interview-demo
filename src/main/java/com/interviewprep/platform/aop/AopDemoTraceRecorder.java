package com.interviewprep.platform.aop;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AopDemoTraceRecorder {
    private final ThreadLocal<List<String>> events = ThreadLocal.withInitial(ArrayList::new);

    public void record(String event) {
        events.get().add(event);
    }

    public List<String> drain() {
        List<String> snapshot = List.copyOf(events.get());
        events.remove();
        return snapshot;
    }

    public void clear() {
        events.remove();
    }
}
