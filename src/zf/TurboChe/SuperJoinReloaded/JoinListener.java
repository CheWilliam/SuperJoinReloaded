package zf.TurboChe.SuperJoinReloaded;

import fr.xephi.authme.events.LoginEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class JoinListener implements Listener {
    // 插件主类实例
    private final Main plugin;
    // 是否使用命令发送标题的开关
    private boolean useCommandTitle;
    // 标题命令格式（从配置读取）
    private String titleCommandFormat;
    // 音效名称映射表（适配1.7.10音效名）
    private Map<String, String> soundMappings = new HashMap<>();

    // 构造方法：初始化监听器并加载配置
    public JoinListener(Main plugin) {
        this.plugin = plugin;
        loadConfig();
        plugin.getLogger().info(plugin.getPrefix() + ChatColor.GREEN + "事件监听器已初始化");
    }

    // 加载配置文件中的版本适配参数
    private void loadConfig() {
        // 读取1.7.10版本的配置节点
        ConfigurationSection versionConfig = plugin.getConfig().getConfigurationSection("version-specific.1_7_10");
        if (versionConfig == null) {
            // 配置不存在时使用默认值
            useCommandTitle = true;
            titleCommandFormat = "iu title {player} {title} {subtitle}";
            soundMappings.put("ENDERDRAGON_GROWL", "ENDERDRAGON_WINGS");
            return;
        }
        // 从配置读取标题命令相关设置
        useCommandTitle = versionConfig.getBoolean("use-command-title", true);
        titleCommandFormat = versionConfig.getString("title-command", "iu title {player} {title} {subtitle}");

        // 加载音效映射配置
        ConfigurationSection sounds = versionConfig.getConfigurationSection("sound-mappings");
        if (sounds != null) {
            sounds.getKeys(false).forEach(key -> soundMappings.put(key, sounds.getString(key)));
        }
    }

    // 监听玩家登录事件（AuthMe登录成功后触发）
    @EventHandler
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().info(plugin.getPrefix() + "玩家 " + player.getName() + " 登录处理中");

        // 处理闪电特效（如果配置启用）
        if (plugin.getConfig().getBoolean("lightning", true)) {
            // 主线程执行闪电效果（避免线程安全问题）
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    // 在玩家位置生成无伤害闪电特效
                    player.getWorld().strikeLightningEffect(player.getLocation());
                    plugin.getLogger().info(plugin.getPrefix() + "闪电特效生成成功（主线程执行）");
                } catch (Exception e) {
                    plugin.getLogger().severe(plugin.getPrefix() + "闪电特效失败: " + e.getMessage());
                }
            });
        }

        // 处理VIP玩家特效
        processVipEffects(player);
        // 发送普通玩家欢迎消息
        sendWelcomeMessage(player);
    }

    // 处理VIP玩家的标题和音效特效
    private void processVipEffects(Player player) {
        // 读取VIP配置节点
        ConfigurationSection vipConfig = plugin.getConfig().getConfigurationSection("viptitle");
        if (vipConfig == null) return;

        // 获取玩家拥有的最高优先级VIP组
        String highestGroup = getHighestVipGroup(player, vipConfig);
        if (highestGroup == null) return;

        // 读取该VIP组的具体配置
        ConfigurationSection groupConfig = vipConfig.getConfigurationSection(highestGroup);
        if (groupConfig == null) return;

        // 格式化标题和副标题（替换变量）
        String title = formatMessageForCommand(player, groupConfig.getString("title", ""));
        String subtitle = formatMessageForCommand(player, groupConfig.getString("subtitle", ""));

        // 发送标题
        sendTitle(player, title, subtitle);

        // 播放音效（如果配置了音效）
        String sound = groupConfig.getString("sound");
        if (sound != null && !sound.isEmpty()) {
            playSound(player, sound);
        }
    }

    // 获取玩家拥有的最高优先级VIP组
    private String getHighestVipGroup(Player player, ConfigurationSection vipConfig) {
        String highestGroup = null;
        int maxPriority = -1;
        // 遍历所有VIP组配置
        for (String group : vipConfig.getKeys(false)) {
            // 检查玩家是否有该VIP组的权限
            if (player.hasPermission("superjoin.vip." + group)) {
                // 提取组名中的优先级数字（如"vip2"提取2）
                int priority = getGroupPriority(group);
                // 更新最高优先级组
                if (priority > maxPriority) {
                    maxPriority = priority;
                    highestGroup = group;
                }
            }
        }
        return highestGroup;
    }

    // 从VIP组名中提取优先级（如"vip3"返回3）
    private int getGroupPriority(String group) {
        try {
            // 移除非数字字符后转换为整数
            return Integer.parseInt(group.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            // 无法提取数字时默认优先级为0
            return 0;
        }
    }

    // 发送标题给玩家
    private void sendTitle(Player player, String title, String subtitle) {
        if (useCommandTitle && titleCommandFormat != null) {
            // 构建标题命令并执行
            String command = buildTitleCommand(player, title, subtitle);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            plugin.getLogger().info(plugin.getPrefix() + "执行标题命令: " + command);
        } else {
            // 未启用命令标题时，使用API直接发送（适配高版本）
            String formattedTitle = ChatColor.translateAlternateColorCodes('&', title);
            String formattedSubtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
            player.sendTitle(formattedTitle, formattedSubtitle);
        }
    }

    // 构建标题命令字符串（替换变量并处理空格）
    private String buildTitleCommand(Player player, String title, String subtitle) {
        // 将空格替换为下划线（匹配iu插件中的gsub("_", " ")）
        String escapedTitle = title.replace(" ", "_");
        String escapedSubtitle = subtitle.replace(" ", "_");

        return titleCommandFormat
                .replace("{player}", player.getName())
                .replace("{title}", escapedTitle)
                .replace("{subtitle}", escapedSubtitle);
    }

    // 格式化命令中的消息（仅替换玩家名变量）
    private String formatMessageForCommand(Player player, String message) {
        if (message == null) return "";
        return message.replace("{player}", player.getName());
    }

    // 格式化游戏内消息（替换变量、处理颜色代码）
    private String formatMessage(Player player, String message) {
        if (message == null) return "";
        // 替换玩家名变量
        message = message.replace("{player}", player.getName());
        // 处理PlaceholderAPI变量（如果启用）
        if (plugin.isPapiEnabled()) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        // 转换颜色代码（&符号转§）
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    // 播放音效
    private void playSound(Player player, String soundName) {
        try {
            // 使用映射后的音效名（适配1.7.10）
            String sound = soundMappings.getOrDefault(soundName, soundName);
            // 在玩家位置播放音效
            player.playSound(player.getLocation(), Sound.valueOf(sound), 1.0F, 1.0F);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning(plugin.getPrefix() + "无效音效: " + soundName);
        }
    }

    // 发送普通玩家的欢迎消息
    private void sendWelcomeMessage(Player player) {
        // 如果未启用欢迎消息则直接返回
        if (!plugin.getConfig().getBoolean("normalJoinMessage.enabled", false)) return;
        // 遍历消息列表并发送（逐条格式化）
        plugin.getConfig().getStringList("normalJoinMessage.messages").forEach(msg -> {
            player.sendMessage(formatMessage(player, msg));
        });
    }
}