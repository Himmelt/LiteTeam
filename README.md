# LiteTeam
LiteTeam

### 指令
```
/teamguild|team                   主命令
/team info [player]               查看自己队伍或<player>队伍的基本信息
/team disband [leader]            会长解散队伍,或由OP解散会长<leader>的队伍,需要管理权限
/team create [display]            创建队伍,可选参数设置队伍显示名.
/team join <leader>               向<leader>的队伍发送加入申请
/team accept join <player>        接受<player>的加入申请（会长副会长可执行）
/team reject join <player>        拒绝<player>的加入申请（会长副会长可执行）
/team invite <player>             邀请 <player> 加入队伍
/team uninvite <@ALL|player>      取消邀请 <player> 如果参数为 @ALL 则取消所有邀请
/team accept invite <leader>      接受 <leader> 队伍的邀请
/team reject invite <leader>      拒绝 <leader> 队伍的邀请
/team home                        (未实现) 前往队伍领地传送点
/team sethome                     (未实现) 设置队伍领地传送点(仅会长)
/team list                        列出所在队伍的成员
/team leave                       退出当前队伍
/team kick <player>               将<player>移出队伍（会长副会长可执行, 会长可以踢除自己外的所有人,副会长只能踢普通成员）
/team display [name]              查看/设置队伍显示名（仅会长可执行）
/team tpr                         随机传送 延时
/team tpraccept                   接受队长的传送邀请
/team tprcancel                   取消随机传送
/team chat                        小队内部聊天
/teamchat|tchat|tmsg|tm           小队内部聊天
```

### 配置
```hocon
# 插件版本，用于自动任务，请勿修改 !!
version = 1.1.3
# 显示语言
lang = zh_cn
# 调试模式
debug = true
# 自动重新释放语言文件(版本变化时).
autoUpLang = true
# 需要对接的经济系统插件
# 目前支持的经济插件: 
# 1. Vault经济服务 (任何实现了Vault经济服务的插件都可以, 例如PlayerPoints)
# 2. Essentials
# 3. PlayerPoints
ecoType = Essentials
# 没有经济系统时是否允许创建队伍.
ignoreNoEco = true
# 是否允许队伍内PVP (重启生效).
teamPvP = false
# 转让队伍后是否直接脱离队伍.
attornLeave = false
# 队伍显示名最大长度, 每个中文占2个长度, 颜色代码也算在长度内.
maxDisplay = 17
# 队伍简介最大长度, 每个中文占2个长度.
maxDescription = 100
# 用于聊天文字点击的命令, 此处已默认插件主命令,
# 请勿随意修改, 只有在默认命令冲突无效时才需要修改.
textCommand = /team
# 队伍等级设置
# 等级列表, 插件会从低到高自动排序, 不必手动排序.
levels {
  0 {
    # 该等级队伍成员容量
    size = 5
    # 该等级队伍升级费用
    cost = 10
    # 该等级队伍管理数量
    mans = 1
  }
  1 {
    # 该等级队伍成员容量
    size = 10
    # 该等级队伍升级费用
    cost = 20
    # 该等级队伍管理数量
    mans = 3
  }
  2 {
    # 该等级队伍成员容量
    size = 15
    # 该等级队伍升级费用
    cost = 30
    # 该等级队伍管理数量
    mans = 5
  }
}
```

### 队伍配置
```hocon
# 会长名，是队伍的唯一识别标志
Himmelt {
  # 队伍等级，和配置中的 levels 对应
  level = 0
  # 队伍荣耀值
  frame = 30
  # 队伍资金(未实现)
  balance = 0
  # 队伍显示名 
  display = "&5紫罗兰&r"
  # 队伍简介
  description = Himmelt's Team.
  # 队伍成员
  members = [
    Kasei
    Miral
  ]
  # 队伍管理员
  managers = [
    Shiki
    Rikka
  ]
  # 待处理加入申请
  applications = [
    Bob
    Shina  
  ]
}
```
