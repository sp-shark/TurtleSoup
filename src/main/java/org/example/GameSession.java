package org.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GameSession {
    private String sessionId;
    private String mystery;
    private String truth;
    private List<String> availableHints = new ArrayList<>();
    private List<String> usedHints = new ArrayList<>();
    private List<String> questionHistory = new ArrayList<>();
    private boolean solved = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    // 构造器
    public GameSession() {}

    public GameSession(String mystery, String truth, List<String> availableHints) {
        this.mystery = mystery;
        this.truth = truth;
        this.availableHints = availableHints;
    }

    // 添加问题到历史记录
    public void addQuestion(String question) {
        if (questionHistory == null) {
            questionHistory = new ArrayList<>();
        }
        questionHistory.add(question);
    }

    // 使用提示
    public String useHint() {
        if (availableHints == null || availableHints.isEmpty()) {
            return null;
        }
        String hint = availableHints.remove(0);
        if (usedHints == null) {
            usedHints = new ArrayList<>();
        }
        usedHints.add(hint);
        return hint;
    }

    // Getter 和 Setter
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMystery() {
        return mystery;
    }

    public void setMystery(String mystery) {
        this.mystery = mystery;
    }

    public String getTruth() {
        return truth;
    }

    public void setTruth(String truth) {
        this.truth = truth;
    }

    public List<String> getAvailableHints() {
        return availableHints;
    }

    public void setAvailableHints(List<String> availableHints) {
        this.availableHints = availableHints;
    }

    public List<String> getUsedHints() {
        return usedHints;
    }

    public void setUsedHints(List<String> usedHints) {
        this.usedHints = usedHints;
    }

    public List<String> getQuestionHistory() {
        return questionHistory;
    }

    public void setQuestionHistory(List<String> questionHistory) {
        this.questionHistory = questionHistory;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}