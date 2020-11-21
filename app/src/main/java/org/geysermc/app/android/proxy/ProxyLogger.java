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

package org.geysermc.app.android.proxy;

import org.geysermc.app.android.utils.EventListeners;

import lombok.Getter;
import lombok.Setter;

public class ProxyLogger {

    @Getter
    private static String log = "";

    @Setter
    private static EventListeners.LogEventListener listener;

    public void warning(String message) {
        log += "警告 - " + message + "\n";
        if (listener != null) listener.onLogLine("WARN - " + message);
        // System.out.println("WARN - " + message);
    }

    public void info(String message) {
        log += "信息 - " + message + "\n";
        if (listener != null) listener.onLogLine("INFO - " + message);
        // System.out.println("INFO - " + message);
    }

    public void error(String message, Throwable error) {
        log += "错误 - " + message + "\n";
        if (listener != null) listener.onLogLine("ERROR - " + message);
        // System.out.println("ERROD - " + message + " - " + error.getMessage());
        // error.printStackTrace();
    }

    public void debug(String message) {
        log += "DEBUG - " + message + "\n";
        if (listener != null) listener.onLogLine ("DEBUG - " + message);
        // System.out.println("DEBUG - " + message);
    }
}
