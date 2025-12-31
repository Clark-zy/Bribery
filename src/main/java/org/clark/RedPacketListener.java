package org.clark;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class RedPacketListener implements Listener, CommandExecutor {
    private final BriberyMoney plugin;
    public RedPacketListener(BriberyMoney plugin) {
        this.plugin = plugin;
        // 注册抢红包指令
        plugin.getCommand("briberymoney").setExecutor(this);
    }
    // 创建带点击事件的聊天组件
    public static TextComponent createClickableMessage(String text, String hoverText, String command) {
        TextComponent component = new TextComponent(text);
        // 设置点击事件（执行指令）
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        // 设置悬浮提示
        component.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(hoverText).create()
        ));
        return component;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 处理抢红包指令
        if (cmd.getName().equalsIgnoreCase("briberymoney") && args.length == 2 && args[0].equalsIgnoreCase("grab")) {
            // 检查是否是玩家
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§c只有玩家可以抢红包！");
                return true;
            }

            // 解析红包ID
            UUID packetId;
            try {
                packetId = UUID.fromString(args[1]);
            } catch (IllegalArgumentException e) {
                player.sendMessage("§c无效的红包ID！");
                return true;
            }

            // 获取红包
            RedPacket redPacket = BriberyMoney.RED_PACKETS.get(packetId);
            if (redPacket == null) {
                player.sendMessage("§c该红包不存在或已失效！");
                return true;
            }

            // 抢红包
            int points = redPacket.grabPacket(player.getName());
            switch (points) {
                case -1:
                    player.sendMessage("§c你已经抢过这个红包了！");
                    break;
                case -2:
                    player.sendMessage("§c手慢了！这个红包已经被抢完了！");
                    break;
                default:
                    // 添加点券
                    boolean success = plugin.addPlayerPoints(player, points);
                    if (success) {
                        // 广播抢红包成功消息
                        Bukkit.broadcastMessage(
                                String.format("§6[红包] §a%s §e抢到了 §6%d §e点券！剩余红包: §6%d §e个",
                                        player.getName(), points, redPacket.getRemainingCount())
                        );
                    } else {
                        player.sendMessage("§c抢红包失败！点券系统异常，请联系管理员！");
                    }
                    break;
            }
            return true;
        }
        return false;
    }

}
