# TeamGuild
TeamGuild

v1_7_R4

1. 可由游戏币创建初始人数为5的小队。
2. 小队可升级6次，自定义升级价格，每升级一次，人数提高5，当升级到最顶级的时候，自动更名为公会。
3. 创建小队后，小队称号在玩家名字后戳，初始称号为{小队} 当将小队升级为最高级后，将转为{公会}称号
4. 当玩家同处于一个小队后，无视队友所有伤害(FlanMOD 枪械)
5. 拥有小队公会排行榜，小队容量越大，排名靠前。
6. 没有经济系统时是否仍然可以创建？
7. build时将 net包和flans包排除

```
if(player instanceof CraftPlayer){
    CraftPlayer mp = (CraftPlayer)player;
    mp.getHandle().b(new ChatComponentText("xxx").setChatModifier(new ChatModifier().setChatClickable(new ChatClickable(EnumClickAction.RUN_COMMAND,"help"))));
}
```
