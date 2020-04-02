package com.example.gamenite.helpers;

public interface Valid {
    default boolean isValid(String[] strings){
        for (int i = 0; i<strings.length;i++){
            if (strings[i].equals(""))
                return true;
        }
        return false;
    }
}
