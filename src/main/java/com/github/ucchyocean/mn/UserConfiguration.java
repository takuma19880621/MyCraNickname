/*
 * @author     ucchy
 * @license    GPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.mn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * ユーザーデータ保存クラス
 * @author ucchy
 */
public class UserConfiguration extends HashMap<String, Object> {

    private static final long serialVersionUID = 4254412226841328410L;

    private static final String KEY_USER_NICKNAME = "nickname";
    private static final String KEY_USER_LASTSETTIME = "lastSetTime";

    private UserConfiguration() {
        this.put(KEY_USER_NICKNAME, "");
        this.put(KEY_USER_LASTSETTIME, "-1");
    }

    private static UserConfiguration getUserConfiguration(String name) {

        File folder = new File(MycraNickname.userFolder);
        if ( !folder.exists() ) {
            folder.mkdirs();
        }

        File file = new File(MycraNickname.userFolder, name + ".yml");
        if ( !file.exists() ) {
            UserConfiguration conf = new UserConfiguration();
            conf.save(file);
        }

        return UserConfiguration.load(file);
    }

    private void save(String name) {

        File file = new File(MycraNickname.userFolder, name + ".yml");
        save(file);
    }

    private void save(File file) {

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(KEY_USER_NICKNAME + ": " + this.get(KEY_USER_NICKNAME));
            writer.newLine();
            writer.write(KEY_USER_LASTSETTIME + ": " + this.get(KEY_USER_LASTSETTIME));
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( writer != null ) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
        }
    }

    private static UserConfiguration load(File file) {

        UserConfiguration conf = new UserConfiguration();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] l = line.split(":");
                if (l[0].contains(KEY_USER_NICKNAME)) {
                    conf.put(KEY_USER_NICKNAME, l[1].trim());
                }
                if (l[0].contains(KEY_USER_LASTSETTIME)) {
                    conf.put(KEY_USER_LASTSETTIME, l[1].trim());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if ( reader != null ) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
        }

        return conf;
    }

    protected static String getUserNickname(String name) {

        UserConfiguration conf = getUserConfiguration(name);
        if ( conf == null ) {
            return null;
        }
        String nickname = (String)conf.get(KEY_USER_NICKNAME);
        if ( nickname == null || nickname.equals("") ) {
            return null;
        }
        return nickname;
    }

    protected static void setUserNickname(String name, String nickname) {

        UserConfiguration conf = getUserConfiguration(name);
        if ( conf == null ) {
            conf = new UserConfiguration();
        }
        conf.put(KEY_USER_NICKNAME, nickname);

        conf.save(name);
    }

    protected static long getLastSetTime(String name) {

        UserConfiguration conf = getUserConfiguration(name);
        if ( conf == null ) {
            return -1;
        }
        String time = (String)conf.get(KEY_USER_LASTSETTIME);
        if ( time == null ) {
            return -1;
        }
        return Long.parseLong(time);
    }

    protected static void setLastSetTime(String name, long time) {

        UserConfiguration conf = getUserConfiguration(name);
        if ( conf == null ) {
            conf = new UserConfiguration();
        }
        conf.put(KEY_USER_LASTSETTIME, time);

        conf.save(name);
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
