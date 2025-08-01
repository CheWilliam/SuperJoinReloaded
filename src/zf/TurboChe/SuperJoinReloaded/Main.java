package zf.TurboChe.SuperJoinReloaded;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private boolean papiEnabled;
    private boolean is1_7_10;
    private String serverVersion;

    private static final String PREFIX = ChatColor.GOLD + "[" + ChatColor.RED + "SuperJoin" + ChatColor.GOLD + "] ";

    @Override
    public void onEnable() {
        // 版本检测（适配 Crucible 核心）
        detectServerVersion();
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.GREEN + "===== 插件加载中 =====");
        Bukkit.getConsoleSender().sendMessage(PREFIX + "检测到服务器核心: " + ChatColor.YELLOW + Bukkit.getServer().getClass().getPackage().getName());
        Bukkit.getConsoleSender().sendMessage(PREFIX + "检测到版本标识: " + ChatColor.YELLOW + serverVersion);
        Bukkit.getConsoleSender().sendMessage(PREFIX + "兼容模式: " + (is1_7_10 ? ChatColor.GREEN + "1.7.10 (Crucible)" : ChatColor.RED + "未知版本"));

        // 配置加载
        saveDefaultConfig();
        reloadConfig();
        validateConfig();

        // 依赖检查
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().severe(PREFIX + ChatColor.RED + "错误: 缺少 PlaceholderAPI 插件！");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.papiEnabled = true;

        // 注册事件
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);

        // 加载提示
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "-=[ SUPER JOIN ]=--=[ SUPER JOIN ]=--=[ SUPER JOIN ]=-");
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "|                                                     |");
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "|  +-+-+-+-+-+ +-+-+-+-+ +-+-+-+-+-+ +-+-+-+-+ +-+-+  |");
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "|  |S|U|P|E|R| |J|O|I|N| |S|U|P|E|R| |J|O|I|N| |!|!|  |");
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "|  +-+ ✅ 功能: 闪电效果 / VIP标题 / 欢迎消息 / 音效 +-+  |");
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "|                     已成功加载                        |");
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "-=[ SUPER JOIN ]=--=[ SUPER JOIN ]=--=[ SUPER JOIN ]=-");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "-=[ SUPER JOIN ]=--=[ SUPER JOIN ]=--=[ SUPER JOIN ]=-");
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "|                                                     |");
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "|  +-+-+-+-+-+        已成功卸载       +-+-+-+-+ +-+-+  |");
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "|  |S|U|P|E|R| |J|O|I|N| |S|U|P|E|R| |J|O|I|N| |!|!|  |");
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "|  +-+-+-+-+-+        已成功卸载       +-+-+-+-+ +-+-+  |");
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "|                                                     |");
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "-=[ SUPER JOIN ]=--=[ SUPER JOIN ]=--=[ SUPER JOIN ]=-");
    }

    // 版本检测（适配 Crucible 包名）
    private void detectServerVersion() {
        try {
            String packageName = Bukkit.getServer().getClass().getPackage().getName();
            this.serverVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
            // Crucible 1.7.10 仍基于 v1_7_R4 内核
            this.is1_7_10 = serverVersion.contains("v1_7_R4");
        } catch (Exception e) {
            this.serverVersion = "Crucible-1.7.10 (检测失败)";
            this.is1_7_10 = true; // 强制认为是 1.7.10（Crucible 环境）
            getLogger().warning(PREFIX + ChatColor.RED + "版本检测异常，强制使用 1.7.10 兼容模式");
        }
    }

    // 配置验证
    private void validateConfig() {
        if (!getConfig().contains("debug")) getConfig().set("debug", false);
        if (!getConfig().contains("lightning")) getConfig().set("lightning", true);

        if (is1_7_10() && !getConfig().contains("version-specific.1_7_10")) {
            ConfigurationSection section = getConfig().createSection("version-specific.1_7_10");
            section.set("use-command-title", true);
            section.set("title-command", "iu title {player} {title} {subtitle}");

            ConfigurationSection sounds = section.createSection("sound-mappings");
            sounds.set("ENDERDRAGON_GROWL", "ENDERDRAGON_WINGS");
            sounds.set("LEVEL_UP", "LEVEL_UP");

            saveConfig();
            Bukkit.getConsoleSender().sendMessage(PREFIX + "已生成 Crucible 专用配置");
        }
    }

    // 命令处理
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("superjoin")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("superjoin.reload")) {
                    sender.sendMessage(PREFIX + ChatColor.RED + getConfig().getString("nopermission", "无权限！"));
                    return true;
                }
                reloadConfig();
                validateConfig();
                sender.sendMessage(PREFIX + ChatColor.GREEN + "配置已重载！");
                return true;
            }
        }
        return false;
    }

    // Getter 方法
    public boolean is1_7_10() { return is1_7_10; }
    public boolean isPapiEnabled() { return papiEnabled; }
    public String getPrefix() { return PREFIX; }
}