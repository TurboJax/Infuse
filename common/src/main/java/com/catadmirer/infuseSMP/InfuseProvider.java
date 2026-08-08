package com.catadmirer.infuseSMP;

import org.jspecify.annotations.NonNull;

public class InfuseProvider {
    private static Infuse instance;

    @NonNull
    public static Infuse getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Infuse has not been loaded yet.  This is likely a plugin issue, so please make a ticket on github.");
        }

        return instance;
    }

    public static void setInstance(Infuse instance) {
        if (InfuseProvider.instance != null) {
            throw new IllegalStateException("Infuse has already been loaded.  Don't try loading it twice.");
        }

        InfuseProvider.instance = instance;
    }
}
