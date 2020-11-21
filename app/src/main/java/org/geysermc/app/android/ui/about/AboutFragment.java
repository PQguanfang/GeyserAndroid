/*
 * Copyright (c) 2020-2020 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/GeyserAndroid
 */

package org.geysermc.app.android.ui.about;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.geysermc.app.android.BuildConfig;
import org.geysermc.app.android.R;
import org.geysermc.app.android.utils.AndroidUtils;

public class AboutFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.about_preferences, rootKey);

        findPreference("version").setSummary(BuildConfig.VERSION_NAME);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            // About
            case "version":
                AndroidUtils.setClipboard(getContext(), BuildConfig.VERSION_NAME);
                AndroidUtils.showToast(getContext(), getResources().getString(R.string.about_version_copied));
                return true;

            // Links
            case "github":
                AndroidUtils.showURL("https://mc.wdsj.com");
                return true;
            case "trello":
                AndroidUtils.showURL("https://jq.qq.com/?_wv=1027&k=xi9HZa23");
                return true;
            case "ci":
                AndroidUtils.showURL("https://jq.qq.com/?_wv=1027&k=wzml94Z5");
                return true;

            // Credits
            case "rtm516":
                AndroidUtils.showURL("https://github.com/rtm516/");
                return true;
            case "arcratist":
                AndroidUtils.showURL("https://space.bilibili.com/355026079?share_medium=android&share_source=copy_link&bbid=XZBE306B8D6F1B230EA0823B24335F047F91C&ts=1605959098926");
                return true;
            case "hellohi3654":
                AndroidUtils.showURL("https://github.com/GeyserMC/Geyser/");
                return true;
            case "geyser":
                AndroidUtils.showURL(getResources().getString(R.string.app_site));
                return true;

            // Licences
            case "fontawesome":
                AndroidUtils.showURL("https://fontawesome.com/license");
                return true;
            default:
                break;
        }

        return super.onPreferenceTreeClick(preference);
    }
}
