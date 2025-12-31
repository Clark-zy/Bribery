package org.clark;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RedPacket {
    private final UUID id;          // 红包唯一ID
    private final String sender;    // 发送者名称
    private final int totalCount;   // 总红包个数
    private final int totalPoints;  // 总点券数
    private final long createTime;  // 创建时间（毫秒）

    private int remainingCount;     // 剩余红包个数
    private int remainingPoints;    // 剩余点券数
    private final Set<String> grabbedPlayers; // 已领取的玩家名

    public RedPacket(UUID id, String sender, int totalCount, int totalPoints, long createTime) {
        this.id = id;
        this.sender = sender;
        this.totalCount = totalCount;
        this.totalPoints = totalPoints;
        this.createTime = createTime;
        this.remainingCount = totalCount;
        this.remainingPoints = totalPoints;
        this.grabbedPlayers = new HashSet<>();
    }

    public int grabPacket(String playerName) {
        // 检查是否已抢过
        if (grabbedPlayers.contains(playerName)) {
            return -1;
        }
        // 检查是否还有剩余红包
        if (remainingCount <= 0) {
            return -2;
        }

        // 添加到已领取列表
        grabbedPlayers.add(playerName);
        remainingCount--;

        // 计算随机点券数
        int points;
        if (remainingCount == 0) {
            // 最后一个红包，拿走所有剩余点券
            points = remainingPoints;
        } else {
            // 随机分配，保证每个红包至少1点券
            // 随机范围：1 ~ (剩余点券 - 剩余个数 + 1)
            int max = remainingPoints - remainingCount;
            points = 1 + (int) (Math.random() * max);
        }

        // 更新剩余点券
        remainingPoints -= points;
        return points;
    }

    // Getter方法
    public UUID getId() { return id; }
    public String getSender() { return sender; }
    public int getTotalCount() { return totalCount; }
    public int getTotalPoints() { return totalPoints; }
    public long getCreateTime() { return createTime; }
    public int getRemainingCount() { return remainingCount; }
    public int getRemainingPoints() { return remainingPoints; }
    public Set<String> getGrabbedPlayers() { return grabbedPlayers; }


}
