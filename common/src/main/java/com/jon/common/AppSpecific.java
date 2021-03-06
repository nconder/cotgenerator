package com.jon.common;

import android.content.Context;

import androidx.annotation.ColorRes;
import androidx.annotation.XmlRes;

import com.jon.common.service.CotFactory;
import com.jon.common.service.CotService;
import com.jon.common.ui.ListPresetsActivity;
import com.jon.common.ui.MainFragment;

import java.util.Date;

public class AppSpecific {
    AppSpecific() { /* blank */ }

    private static Repo repo;

    public static void setReferenceRepo(Repo appSpecificRepo) {
        repo = appSpecificRepo;
    }

    public static MainFragment getMainFragment() { return repo.getMainFragment(); }
    public static Date getBuildDate() { return repo.getBuildDate(); }
    public static int getBuildVersionCode() { return repo.getBuildVersionCode(); }
    public static String getAppId() { return repo.getAppId(); }
    public static String getAppName() { return repo.getAppName(); }
    public static String getPermissionRationale() { return repo.getPermissionRationale(); }
    public static String getVersionName() { return repo.getVersionName(); }
    public static String getPlatform() { return repo.getPlatform(); }
    public static boolean isDebug() { return repo.isDebug(); }
    public static Class<? extends CotService> getCotServiceClass() { return repo.getCotServiceClass(); }
    public static Class<? extends CotFactory> getCotFactoryClass() { return repo.getCotFactoryClass(); }
    public static Class<? extends ListPresetsActivity> getListActivityClass() { return repo.getListActivityClass(); }
    public static @XmlRes int getSettingsXmlId() { return repo.getSettingsXmlId(); }
    public static @ColorRes int getIconColourId() { return repo.getIconColourId(); }

    public abstract static class Repo {
        protected Context context() { return CotApplication.getContext(); }

        abstract protected MainFragment getMainFragment();
        abstract protected Date getBuildDate();
        abstract protected int getBuildVersionCode();
        abstract protected String getAppId();
        abstract protected String getAppName();
        abstract protected String getPermissionRationale();
        abstract protected String getVersionName();
        abstract protected String getPlatform();
        abstract protected boolean isDebug();
        abstract protected Class<? extends CotService> getCotServiceClass();
        abstract protected Class<? extends CotFactory> getCotFactoryClass();
        abstract protected Class<? extends ListPresetsActivity> getListActivityClass();
        abstract protected @XmlRes int getSettingsXmlId();
        abstract protected @ColorRes int getIconColourId();
    }
}
