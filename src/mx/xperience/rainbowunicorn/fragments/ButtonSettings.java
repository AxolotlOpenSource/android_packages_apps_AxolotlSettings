/*
 * Copyright (C) 2017 The XPerience Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mx.xperience.rainbowunicorn.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import mx.xperience.rainbowunicorn.preferences.ActionFragment;
import mx.xperience.rainbowunicorn.preferences.CustomSeekBarPreference;
import com.android.internal.util.hwkeys.ActionConstants;
import com.android.internal.util.hwkeys.ActionUtils;


public class ButtonSettings extends ActionFragment implements
        OnPreferenceChangeListener {

    private static final String HWKEY_DISABLE = "hardware_keys_disable";
    private static final String KEY_BUTTON_BRIGHTNESS = "button_brightness";
    private static final String KEY_BUTTON_BRIGHTNESS_SW = "button_brightness_sw";
    private static final String KEY_BACKLIGHT_TIMEOUT = "backlight_timeout";

    //CAT
    private static final String CATEGORY_HWKEY = "hardware_keys";
    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_MENU = "menu_key";
    private static final String CATEGORY_BACK = "back_key";
    private static final String CATEGORY_ASSIST = "assist_key";
    private static final String CATEGORY_APPSWITCH = "app_switch_key";

    private ListPreference mBacklightTimeout;
    private CustomSeekBarPreference mButtonBrightness;
    private SwitchPreference mButtonBrightness_sw;
    private SwitchPreference mHwKeyDisable;

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    public static final int KEY_MASK_HOME = 0x01;
    public static final int KEY_MASK_BACK = 0x02;
    public static final int KEY_MASK_MENU = 0x04;
    public static final int KEY_MASK_ASSIST = 0x08;
    public static final int KEY_MASK_APP_SWITCH = 0x10;
    public static final int KEY_MASK_CAMERA = 0x20;
    public static final int KEY_MASK_VOLUME = 0x40;

    private static final String VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control";

    private ListPreference mVolumeKeyCursorControl;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.xperience_buttons);

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final boolean needsNavbar = ActionUtils.hasNavbarByDefault(getActivity());
        final PreferenceCategory hwkeyCat = (PreferenceCategory) prefScreen.findPreference(CATEGORY_HWKEY);

        int keysDisabled = 0;
        if (!needsNavbar) {
          mHwKeyDisable = (SwitchPreference) findPreference(HWKEY_DISABLE);
          keysDisabled = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.HARDWARE_KEYS_DISABLE, 0,
                UserHandle.USER_CURRENT);
                mHwKeyDisable.setChecked(keysDisabled != 0);
                mHwKeyDisable.setOnPreferenceChangeListener(this);
          final boolean variableBrightness = getResources().getBoolean(
                com.android.internal.R.bool.config_deviceHasVariableButtonBrightness);
       mBacklightTimeout =
              (ListPreference) findPreference(KEY_BACKLIGHT_TIMEOUT);
       mButtonBrightness =
              (CustomSeekBarPreference) findPreference(KEY_BUTTON_BRIGHTNESS);
       mButtonBrightness_sw =
              (SwitchPreference) findPreference(KEY_BUTTON_BRIGHTNESS_SW);
           if (mBacklightTimeout != null) {
              mBacklightTimeout.setOnPreferenceChangeListener(this);
              int BacklightTimeout = Settings.System.getInt(getContentResolver(),
                      Settings.System.BUTTON_BACKLIGHT_TIMEOUT, 5000);
              mBacklightTimeout.setValue(Integer.toString(BacklightTimeout));
              mBacklightTimeout.setSummary(mBacklightTimeout.getEntry());
          }
           if (variableBrightness) {
              hwkeyCat.removePreference(mButtonBrightness_sw);
              if (mButtonBrightness != null) {
                  int ButtonBrightness = Settings.System.getInt(getContentResolver(),
                          Settings.System.BUTTON_BRIGHTNESS, 255);
                  mButtonBrightness.setValue(ButtonBrightness / 1);
                  mButtonBrightness.setOnPreferenceChangeListener(this);
              }
          } else {
              hwkeyCat.removePreference(mButtonBrightness);
              if (mButtonBrightness_sw != null) {
                  mButtonBrightness_sw.setChecked((Settings.System.getInt(getContentResolver(),
                          Settings.System.BUTTON_BRIGHTNESS, 1) == 1));
                  mButtonBrightness_sw.setOnPreferenceChangeListener(this);
              }
          }
  } else {
      prefScreen.removePreference(hwkeyCat);
    }
      // bits for hardware keys present on device
      final int deviceKeys = getResources().getInteger(
              com.android.internal.R.integer.config_deviceHardwareKeys);
       // read bits for present hardware keys
      final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
      final boolean hasBackKey = (deviceKeys & KEY_MASK_BACK) != 0;
      final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
      final boolean hasAssistKey = (deviceKeys & KEY_MASK_ASSIST) != 0;
      final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;
       // load categories and init/remove preferences based on device
      // configuration
      final PreferenceCategory backCategory =
              (PreferenceCategory) prefScreen.findPreference(CATEGORY_BACK);
      final PreferenceCategory homeCategory =
              (PreferenceCategory) prefScreen.findPreference(CATEGORY_HOME);
      final PreferenceCategory menuCategory =
              (PreferenceCategory) prefScreen.findPreference(CATEGORY_MENU);
      final PreferenceCategory assistCategory =
              (PreferenceCategory) prefScreen.findPreference(CATEGORY_ASSIST);
      final PreferenceCategory appSwitchCategory =
              (PreferenceCategory) prefScreen.findPreference(CATEGORY_APPSWITCH);
       // back key
      if (!hasBackKey) {
          prefScreen.removePreference(backCategory);
      }
       // home key
      if (!hasHomeKey) {
          prefScreen.removePreference(homeCategory);
      }
       // App switch key (recents)
      if (!hasAppSwitchKey) {
          prefScreen.removePreference(appSwitchCategory);
      }
       // menu key
      if (!hasMenuKey) {
          prefScreen.removePreference(menuCategory);
      }
       // search/assist key
      if (!hasAssistKey) {
          prefScreen.removePreference(assistCategory);
      }
       // let super know we can load ActionPreferences
      onPreferenceScreenLoaded(ActionConstants.getDefaults(ActionConstants.HWKEYS));
       // load preferences first
      setActionPreferencesEnabled(keysDisabled == 0);

        // volume key cursor control
        /*mVolumeKeyCursorControl = (ListPreference) findPreference(VOLUME_KEY_CURSOR_CONTROL);
        if (mVolumeKeyCursorControl != null) {
            mVolumeKeyCursorControl.setOnPreferenceChangeListener(this);
            int volumeRockerCursorControl = Settings.System.getInt(getContentResolver(),
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0);
            mVolumeKeyCursorControl.setValue(Integer.toString(volumeRockerCursorControl));
            mVolumeKeyCursorControl.setSummary(mVolumeKeyCursorControl.getEntry());
        }*/

  }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
      if (preference == mBacklightTimeout) {
          String BacklightTimeout = (String) objValue;
          int BacklightTimeoutValue = Integer.parseInt(BacklightTimeout);
          Settings.System.putInt(getActivity().getContentResolver(),
                  Settings.System.BUTTON_BACKLIGHT_TIMEOUT, BacklightTimeoutValue);
          int BacklightTimeoutIndex = mBacklightTimeout
                  .findIndexOfValue(BacklightTimeout);
          mBacklightTimeout
                  .setSummary(mBacklightTimeout.getEntries()[BacklightTimeoutIndex]);
          return true;
      } else if (preference == mButtonBrightness) {
          int value = (Integer) objValue;
          Settings.System.putInt(getActivity().getContentResolver(),
                  Settings.System.BUTTON_BRIGHTNESS, value * 1);
          return true;
        } else if (preference == mButtonBrightness_sw) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.BUTTON_BRIGHTNESS, value ? 1 : 0);
            return true;
          } else if (preference == mHwKeyDisable) {
              boolean value = (Boolean) objValue;
              Settings.Secure.putInt(getContentResolver(), Settings.Secure.HARDWARE_KEYS_DISABLE,
                      value ? 1 : 0);
              setActionPreferencesEnabled(!value);
              return true;
		  }/* else if (preference == mVolumeKeyCursorControl) {
            String volumeKeyCursorControl = (String) objValue;
            int volumeKeyCursorControlValue = Integer.parseInt(volumeKeyCursorControl);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL, volumeKeyCursorControlValue);
            int volumeKeyCursorControlIndex = mVolumeKeyCursorControl
                    .findIndexOfValue(volumeKeyCursorControl);
            mVolumeKeyCursorControl
                    .setSummary(mVolumeKeyCursorControl.getEntries()[volumeKeyCursorControlIndex]);
            return true;
		  }*/
        return false;
    }

    @Override
    protected boolean usesExtendedActionsList() {
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.RAINBOW_UNICORN;
    }

}
