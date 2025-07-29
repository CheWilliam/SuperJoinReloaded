# ⚡️ SuperJoinReloaded

一款轻量级 Minecraft 插件，专注于提升玩家登录体验，支持自定义标题、VIP 特权展示与登录特效。


## 核心功能

- **自定义登录标题**  
  支持颜色代码（使用 `&` 符号）和占位符（如 `{player}` 显示玩家名），为玩家展示个性化标题和副标题。

- **VIP 等级系统**  
  自动检测玩家最高优先级的 VIP 组（基于权限 `superjoin.组名`，如 `superjoin.vip3`），并加载对应组的专属标题和音效。

- **登录特效**  
  可配置的闪电特效（无伤害）和自定义音效，为 VIP 玩家提供独特的登录仪式感。

- **版本兼容与灵活配置**  
  适配 1.7.10 服务器（如 Crucible 核心），通过 `config.yml` 可轻松开关功能（如闪电效果、普通玩家欢迎消息）。


## 安装步骤

1. 下载最新版本的 `SuperJoinReloaded.jar`。
2. 将 JAR 文件放入服务器的 `plugins` 文件夹。
3. 重启服务器，插件会自动生成默认配置文件。
4. 编辑 `plugins/SuperJoinReloaded/config.yml` 自定义设置，修改后可通过 `/superjoin reload` 重载配置。


## 配置说明

- `lightning`: 是否启用登录闪电特效（默认 `true`）。
- `viptitle`: 配置各 VIP 组的标题、副标题和音效（按配置顺序优先级递减）。
- `normalJoinMessage`: 普通玩家的欢迎消息设置（可开关）。
- `version-specific.1_7_10`: 1.7.10 版本专属配置（如标题命令适配）。


## 依赖

- **服务器版本**: 1.7.10（Bukkit/Spigot/Crucible）。
- **可选依赖**:  
  - `PlaceholderAPI`: 支持动态占位符（如玩家等级、金币等）。  
  - `iu` 插件: 1.7.10 版本中用于正确渲染标题。  


## 命令与权限

- 命令: `/superjoin reload` - 重载配置文件（需权限 `superjoin.reload`）。
- VIP 权限: `superjoin.组名`（如 `superjoin.vip1`、`superjoin.premium`）。


## 未来计划

- 新增粒子特效（如爱心、烟花）。
- 支持多语言消息配置。
- 高版本支持
- 与 AuthMe、LuckPerms 等插件深度集成。


## 致谢

感谢测试者对插件兼容性的反馈，特别感谢 ASJCore作者 AlexSocol、GedeonGrays 协助修复 1.7.10 版本适配问题。
