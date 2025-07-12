package org.example;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class GameSessionRepository {
    // 使用线程安全的ConcurrentHashMap存储会话数据
    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();

    // 保存或更新游戏会话
    public GameSession save(GameSession session) {
        if (session.getSessionId() == null) {
            session.setSessionId(UUID.randomUUID().toString());
        }
        sessions.put(session.getSessionId(), session);
        return session;
    }

    // 根据sessionId查找游戏会话
    public Optional<GameSession> findBySessionId(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    // 查找所有已解决的会话
    public List<GameSession> findBySolvedTrue() {
        return sessions.values().stream()
                .filter(GameSession::isSolved)
                .collect(Collectors.toList());
    }

    // 查找所有未解决的会话
    public List<GameSession> findBySolvedFalse() {
        return sessions.values().stream()
                .filter(session -> !session.isSolved())
                .collect(Collectors.toList());
    }
/*
    // 删除过期的游戏会话
    public int deleteExpiredSessions(int expiryHours) {
        long expiryMillis = System.currentTimeMillis() - (expiryHours * 60 * 60 * 1000L);

        List<String> toRemove = sessions.values().stream()
                .filter(session -> session.getCreatedAt().getTime() < expiryMillis)
                .map(GameSession::getSessionId)
                .collect(Collectors.toList());

        toRemove.forEach(sessions::remove);
        return toRemove.size();
    }
*/
    // 更新游戏会话的提示列表
    public void updateHints(String sessionId, List<String> availableHints, List<String> usedHints) {
        findBySessionId(sessionId).ifPresent(session -> {
            session.setAvailableHints(availableHints);
            session.setUsedHints(usedHints);
        });
    }

    // 更新游戏会话的问题历史
    public void updateQuestionHistory(String sessionId, List<String> questionHistory) {
        findBySessionId(sessionId).ifPresent(session -> {
            session.setQuestionHistory(questionHistory);
        });
    }

    // 标记会话为已解决
    public void markAsSolved(String sessionId) {
        findBySessionId(sessionId).ifPresent(session -> {
            session.setSolved(true);
        });
    }

    // 根据关键词搜索谜题
    public List<GameSession> findByMysteryKeyword(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return sessions.values().stream()
                .filter(session -> session.getMystery().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    // 获取所有会话（测试用）
    public List<GameSession> findAll() {
        return new ArrayList<>(sessions.values());
    }

    // 删除所有会话（测试用）
    public void deleteAll() {
        sessions.clear();
    }
}