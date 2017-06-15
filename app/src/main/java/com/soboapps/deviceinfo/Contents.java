package com.soboapps.deviceinfo;


public final class Contents {
    private Contents() {
    }

    public static final class Type {
        // Plain text. Use Intent.putExtra(DATA, string). This can be used for URLs too, but string
        // must include "http://" or "https://".
        public static final String TEXT = "TEXT_TYPE";

        private Type() {
        }
    }

}
