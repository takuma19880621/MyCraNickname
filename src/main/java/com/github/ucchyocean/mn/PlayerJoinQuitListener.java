/*
 * @author     ucchy
 * @license    GPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.mn;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * プレイヤーのログインを取得するリスナークラス
 * @author ucchy
 */
public class PlayerJoinQuitListener implements Listener {

    /**
     * プレイヤーがログインしたときに呼び出されるメソッド。
     * joinメッセージを置き換える他のプラグインより先にdisplaynameを置き換えるため、
     * 優先度を低くしている。
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        String nickname = UserConfiguration.getUserNickname(player.getName());

        // 保存していたニックネームを再設定
        if ( nickname != null ) {
            player.setDisplayName(ChatColor.AQUA + nickname +
                    ChatColor.WHITE + "(" + player.getName() + ")");
        }
    }
}
