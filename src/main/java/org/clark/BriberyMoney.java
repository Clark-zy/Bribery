package org.clark;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class BriberyMoney extends JavaPlugin {

    public static final Map<UUID, RedPacket> RED_PACKETS = new ConcurrentHashMap<>();
    private PlayerPointsAPI playerPointsAPI;

    @Override
    public void onEnable() {

        if (!setupPlayerPoints()) {
            getLogger().severe("PlayerPoints 插件未找到！红包插件无法启用！");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // Plugin startup logic
        this.getCommand("bm").setExecutor(this);
        getServer().getPluginManager().registerEvents(new RedPacketListener(this), this);

        // 启动定时任务，清理过期红包（每10秒检查一次）
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                // 遍历并移除过期红包
                RED_PACKETS.entrySet().removeIf(entry -> {
                    RedPacket packet = entry.getValue();
                    boolean isExpired = currentTime - packet.getCreateTime() > 3 * 60 * 1000; // 3分钟过期
                    boolean isEmpty = packet.getRemainingCount() <= 0;

                    if (isExpired || isEmpty) {
                        // 发送红包过期提示
                        if (isExpired) {
                            Bukkit.broadcastMessage("§c[红包] §eID为" + entry.getKey() + "的红包已过期失效！");
                        }
                        return true;
                    }
                    return false;
                });
            }
        }.runTaskTimer(this, 200, 200); // 200tick = 10秒
        getLogger().info("红包插件已成功启用！");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        RED_PACKETS.clear();
        getLogger().info("红包插件已禁用！");
    }

    private boolean setupPlayerPoints() {
        Plugin plugin = getServer().getPluginManager().getPlugin("PlayerPoints");
        if (plugin == null || !(plugin instanceof PlayerPoints)) {
            return false;
        }
        this.playerPointsAPI = ((PlayerPoints) plugin).getAPI();
        return this.playerPointsAPI != null;
    }

    public boolean addPlayerPoints(Player player, int points) {
        if (playerPointsAPI == null) {
            player.sendMessage("§c点券系统异常！无法添加点券！");
            return false;
        }

        try {
            // 使用 PlayerPoints API 添加点券
            playerPointsAPI.give(player.getUniqueId(), points);

            // 发送提示消息
            player.sendMessage("§a恭喜你抢到 §6" + points + " §a点券！当前点券余额: §6" + playerPointsAPI.look(player.getUniqueId()));
            return true;
        } catch (Exception e) {
            getLogger().severe("添加点券失败：" + e.getMessage());
            player.sendMessage("§c抢红包失败！点券添加出错，请联系管理员！");
            return false;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 检查指令是否为bm
        if (cmd.getName().equalsIgnoreCase("bm")) {
            // 检查是否是OP
            if (!sender.isOp()) {
                sender.sendMessage("§c你没有权限使用此指令！");
                return true;
            }

            // 检查参数数量
            if (args.length != 2) {
                sender.sendMessage("§c用法错误！正确用法: /bm <红包个数> <总点券数>");
                sender.sendMessage("§e示例: /bm 10 1000 (发送10个红包，总点券1000)");
                return true;
            }

            // 解析参数
            int packetCount;
            int totalPoints;
            try {
                packetCount = Integer.parseInt(args[0]);
                totalPoints = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c参数必须是整数！");
                return true;
            }

            // 验证参数合法性
            if (packetCount <= 0 || packetCount > 100) {
                sender.sendMessage("§c红包个数必须在1-100之间！");
                return true;
            }
            if (totalPoints <= 0 || totalPoints < packetCount) {
                sender.sendMessage("§c总点券数必须大于0且不少于红包个数（每个红包至少1点券）！");
                return true;
            }

            if (sender instanceof Player player) {
                int playerBalance = playerPointsAPI.look(player.getUniqueId());
                if (playerBalance < totalPoints) {
                    player.sendMessage("§c你的点券不足！当前余额: §6" + playerBalance + " §c需要: §6" + totalPoints);
                    return true;
                }

                // 扣除发送者的点券（可选功能，根据需求决定是否保留）
                playerPointsAPI.take(player.getUniqueId(), totalPoints);
                player.sendMessage("§e已扣除你 §6" + totalPoints + " §e点券用于发送红包！");
            }

            // 创建红包
            UUID packetId = UUID.randomUUID();
            RedPacket redPacket = new RedPacket(
                    packetId,
                    sender.getName(),
                    packetCount,
                    totalPoints,
                    System.currentTimeMillis()
            );

            // 存储红包
            RED_PACKETS.put(packetId, redPacket);

            // 发送红包提示（带可点击的"抢"按钮）
            String clickableMessage = String.format(
                    "§6[红包] §a%s §e发送了 §6%d §e个红包，总点券 §6%d §e！§n§b[抢]§r",
                    sender.getName(), packetCount, totalPoints
            );

            // 构建带点击事件的聊天组件
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.spigot().sendMessage(
                        RedPacketListener.createClickableMessage(
                                clickableMessage,
                                "抢红包",
                                "/briberymoney grab " + packetId
                        )
                );
            }

            sender.sendMessage("§a红包发送成功！红包ID: " + packetId);
            return true;
        }
        return false;
    }





}
