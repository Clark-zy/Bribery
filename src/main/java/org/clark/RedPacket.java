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

        // 计算随机点券数
        int points;

        if (remainingCount == 1) {
            // 最后一个红包，拿走所有剩余点券
            points = remainingPoints;
        } else {
            // 二倍均值法：
            // 1. 计算当前剩余金额的平均值
            // 2. 随机范围：1 ~ 平均值*2
            // 3. 但要保证剩余金额足够给后面的人每人至少1点券

            // 当前剩余人数的平均值（包括当前玩家）
            double avg = (double) remainingPoints / remainingCount;

            // 最大可抢金额：平均值*2 和 (剩余金额-剩余人数+1) 的较小值
            // (剩余金额-剩余人数+1) 确保后面的人每人至少能抢到1点券
            int max = (int) Math.min(avg * 2, remainingPoints - (remainingCount - 1));

            // 确保至少有1点券
            if (max < 1) {
                max = 1;
            }

            // 随机分配：1 ~ max
            points = 1 + (int) (Math.random() * (max - 1));
        }

        // 更新剩余点券和剩余人数
        remainingPoints -= points;
        remainingCount--;

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
