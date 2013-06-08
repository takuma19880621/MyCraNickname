/*
 * @author     ucchy
 * @license    GPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.mn;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * ユーザーデータ保存クラス
 * @author ucchy
 */
public class UserConfiguration {

    protected static final String KEY_USER_NICKNAME = "nickname";
    protected static final String KEY_USER_LASTSETTIME = "lastSetTime";

    protected static YamlConfiguration getUserConfiguration(String name) {

        File folder = new File(MycraNickname.userFolder);
        if ( !folder.exists() ) {
            folder.mkdirs();
        }

        File file = new File(MycraNickname.userFolder, name + ".yml");
        if ( !file.exists() ) {
            YamlConfiguration conf = new YamlConfiguration();
            try {
                conf.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    protected static void saveUserConfiguration(String name, YamlConfiguration config) {

        File file = new File(MycraNickname.userFolder, name + ".yml");
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static String getUserNickname(String name) {

        YamlConfiguration conf = getUserConfiguration(name);
        if ( conf == null ) {
            return null;
        }
        String nickname = conf.getString(KEY_USER_NICKNAME);
        if ( nickname == null || nickname.equals("") ) {
            return null;
        }
        return nickname;
    }

    protected static long getLastSetTime(String name) {

        YamlConfiguration conf = getUserConfiguration(name);
        if ( conf == null ) {
            return -1;
        }
        String time = conf.getString(KEY_USER_LASTSETTIME);
        if ( time == null ) {
            return -1;
        }
        return Long.parseLong(time);
    }

    protected static Properties getAllUserNickname() {

        Properties prop = new Properties();

        File folder = new File(MycraNickname.userFolder);
        if ( !folder.exists() ) {
            folder.mkdirs();
        }

        String[] filelist = folder.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if ( name.endsWith(".yml") )
                    return true;
                return false;
            }
        });

        for ( String f : filelist ) {
            String name = f.substring(0, f.indexOf(".") );
            String nickname = getUserNickname(name);
            if ( nickname != null ) {
                prop.setProperty(name, nickname);
            }
        }

        return prop;
    }

    protected static boolean removeUserFile(String name) {

        File file = new File(MycraNickname.userFolder, name + ".yml");

        if ( file.exists() ) {
            return file.delete();
        }
        return false;
    }
}
