/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.installer.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

public class PictocraftUtil {
    public static boolean isSelected = true;
    public static boolean installModMenu = true;
    
    static final String latestJsonUrl = "https://api.github.com/repos/edeetee/pictocraft/releases/latest";
    public static final Release latestRelease = getLatestRelease();

    public class Release {
        public String name;
        public URL html_url;
        public String tag_name;
        public Asset[] assets;

        public URL getJarUrl(){
            return assets[0].browser_download_url;
        }

        @Override
        public String toString() {
            return name + ": " + getJarUrl();
        }
    }

    class Asset {
        public URL browser_download_url;
    }

    static Release getLatestRelease(){
        try{
            URL url = new URL(latestJsonUrl);
            InputStream input = url.openStream();
            Reader reader = new InputStreamReader(input, "UTF-8");
            Release resp = Utils.GSON.fromJson(reader, Release.class);
            return resp;
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}