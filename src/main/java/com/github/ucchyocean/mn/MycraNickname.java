/*
 * @author     ucchy
 * @license    GPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.mn;

import java.io.File;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * my-craサーバー ニックネームシステム
 * @author ucchy
 */
public class MycraNickname extends JavaPlugin {

    /** ユーザーのコテハン情報を保存するフォルダ */
    protected static String userFolder;
    /** コテハンを再設定できるようになるまでの時間 */
    protected static int cooltime;
    /** 数字のみのコテハンを許可するかどうか */
    protected static boolean allowOnlyNumbers;
    /** 設定可能な最大文字長 */
    protected static int maxLength;

    private static final String PREERR = ChatColor.RED.toString();
    private static final String PREINFO = ChatColor.AQUA.toString();

    /**
     * プラグインが有効化されたときに呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        userFolder = new File(getDataFolder(), "user").getAbsolutePath();

        // 設定ファイル読み込み
        try {
            reloadConfigFile();
        } catch (Exception e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // リスナー登録
        getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(), this);
    }

    /**
     * コマンドが実行されたときに呼び出されるメソッド
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    public boolean onCommand(
            CommandSender sender, Command command, String label, String[] args) {

        if ( args.length == 0 ) {
            return false;
        }

        if ( args[0].equalsIgnoreCase("off") ) {
            // 解除コマンド

            if ( args.length == 1 ) {
                // 自分の解除
                return offSelf(sender, command, label, args);

            } else {
                // 他者の解除
                return offOther(sender, command, label, args);
            }

        } else if ( args[0].equalsIgnoreCase("set") ) {
            // 設定コマンド
            return setSelf(sender, command, label, args);

        } else if ( args[0].equalsIgnoreCase("list") ) {
            // リストコマンド
            return list(sender, command, label, args);

        } else if ( args[0].equalsIgnoreCase("reload") ) {
            // リロードコマンド
            return reload(sender, command, label, args);

        }

        return false;
    }

    private boolean offSelf(
            CommandSender sender, Command command, String label, String[] args) {

        if ( !(sender instanceof Player) ) {
            sender.sendMessage(PREERR +
                    "このコマンドはコンソールからは実行できません。");
            return true;
        }

        Player player = (Player)sender;

        if ( !player.hasPermission("nickname.self") ) {
            player.sendMessage(PREERR +
                    "パーミッション \"nickname.self\" がないため、コマンドを実行できません。");
            return true;
        }

        // DisplayNameをリセットして、ユーザー設定を削除する
        player.setDisplayName(player.getName());
        YamlConfiguration config =
                UserConfiguration.getUserConfiguration(player.getName());
        config.set(UserConfiguration.KEY_USER_NICKNAME, null);
        UserConfiguration.saveUserConfiguration(player.getName(), config);

        player.sendMessage(PREINFO +
                "あなたのニックネームが解除されました。");

        return true;
    }

    private boolean offOther(
            CommandSender sender, Command command, String label, String[] args) {

        if ( !sender.hasPermission("nickname.other") ) {
            sender.sendMessage(PREERR +
                    "パーミッション \"nickname.other\" がないため、コマンドを実行できません。");
            return true;
        }

        String old = UserConfiguration.getUserNickname(args[1]);
        if ( old == null ) {
            sender.sendMessage(PREERR +
                    "プレイヤー " + args[1] + "のニックネームが見つかりません。");
            return true;
        }

        // DisplayNameをリセットして、ユーザー設定を削除する
        YamlConfiguration config =
                UserConfiguration.getUserConfiguration(args[1]);
        config.set(UserConfiguration.KEY_USER_NICKNAME, null);
        UserConfiguration.saveUserConfiguration(args[1], config);

        sender.sendMessage(PREINFO +
                args[1] + " のニックネームが解除されました。");
        Player other = getServer().getPlayerExact(args[1]);
        if ( other != null ) {
            other.setDisplayName(other.getName());
            other.sendMessage(PREINFO +
                    "あなたのニックネームが解除されました。");
        }

        return true;
    }

    private boolean setSelf(
            CommandSender sender, Command command, String label, String[] args) {

        if ( !(sender instanceof Player) ) {
            sender.sendMessage(PREERR +
                    "このコマンドはコンソールからは実行できません。");
            return true;
        }

        if ( args.length <= 1 ) {
            sender.sendMessage(PREERR +
                    "コマンド使用方法: /" + label + " set (nickname)");
            return true;
        }

        Player player = (Player)sender;
        String nickname = args[1];

        if ( !player.hasPermission("nickname.self") ) {
            player.sendMessage(PREERR +
                    "パーミッション \"nickname.self\" がないため、コマンドを実行できません。");
            return true;
        }

        long lastTime = UserConfiguration.getLastSetTime(player.getName());
        long nowTime = new Date().getTime();
        if ( lastTime != -1 ) {
            int gap = (int)( (nowTime - lastTime) / 1000 );
            if ( gap < MycraNickname.cooltime ) {
                player.sendMessage(PREERR +
                        "ニックネームを再設定するには、" +
                        "あと " + (MycraNickname.cooltime - gap) +
                        "秒待つ必要があります。");
                return true;
            }
        }

        // 数字のみのコテハンは設定させない
        if ( !MycraNickname.allowOnlyNumbers &&
                nickname.matches("[0-9０１２３４５６７８９]+") ) {
            player.sendMessage(PREERR +
                    "数字のみのニックネームは設定できません。");
            return true;
        }

        // 最大文字長を超えた場合はエラーにする
        if ( MycraNickname.maxLength < nickname.getBytes().length ) {
            player.sendMessage(PREERR +
                    "ニックネームに設定できる最大文字長を超えました。");
            return true;
        }

        player.setDisplayName(ChatColor.AQUA + nickname +
                ChatColor.WHITE + "(" + player.getName() + ")");

        YamlConfiguration config =
                UserConfiguration.getUserConfiguration(player.getName());
        config.set(UserConfiguration.KEY_USER_NICKNAME, nickname);
        config.set(UserConfiguration.KEY_USER_LASTSETTIME, nowTime);
        UserConfiguration.saveUserConfiguration(player.getName(), config);

        player.sendMessage(PREINFO +
                nickname + " がニックネームに設定されました。");

        return true;
    }

    private boolean list(
            CommandSender sender, Command command, String label, String[] args) {

        if ( !sender.hasPermission("nickname.list") ) {
            sender.sendMessage(PREERR +
                    "パーミッション \"nickname.list\" がないため、コマンドを実行できません。");
            return true;
        }

        Properties prop = UserConfiguration.getAllUserNickname();
        Enumeration<Object> keys = prop.keys();
        while ( keys.hasMoreElements() ) {
            String key = (String)keys.nextElement();
            String value = prop.getProperty(key);
            sender.sendMessage(String.format("%s|%s %s %s->%s %s",
                    ChatColor.DARK_GRAY.toString(), ChatColor.WHITE.toString(), key,
                    ChatColor.GRAY.toString(), ChatColor.WHITE.toString(), value));
        }

        return true;
    }

    private boolean reload(
            CommandSender sender, Command command, String label, String[] args) {

        if ( !sender.hasPermission("nickname.reload") ) {
            sender.sendMessage(PREERR +
                    "パーミッション \"nickname.reload\" がないため、コマンドを実行できません。");
            return true;
        }

        reloadConfigFile();
        sender.sendMessage(PREINFO + "config.yml を再読み込みしました。");

        return true;
    }

    /**
     * config.ymlの読み出し処理。
     */
    protected void reloadConfigFile() {

        // フォルダやファイルがない場合は、作成したりする
        File dir = new File(getDataFolder().getAbsolutePath());
        if ( !dir.exists() ) {
            dir.mkdirs();
        }

        File file = new File(getDataFolder(), "config.yml");
        if ( !file.exists() ) {
            Utility.copyFileFromJar(getFile(),
                    file, "config.yml", false);
        }

        // コンフィグのリロードと読み込み
        reloadConfig();
        FileConfiguration config = getConfig();

        cooltime = config.getInt("cooltime", 300);
        allowOnlyNumbers = config.getBoolean("allowOnlyNumbers", false);
        maxLength = config.getInt("maxLength", 16);
    }
}
