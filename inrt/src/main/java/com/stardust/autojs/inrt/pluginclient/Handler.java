package com.stardust.autojs.inrt.pluginclient;

import com.google.gson.JsonObject;

/**
 * Created by Stardust on 2017/5/11.
 */

public interface Handler {

    boolean handle(JsonObject data);
}
