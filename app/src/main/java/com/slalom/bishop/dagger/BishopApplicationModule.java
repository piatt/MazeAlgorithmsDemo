package com.slalom.bishop.dagger;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.slalom.bishop.BishopApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by petrask on 4/30/16.
 */
@Module
public class BishopApplicationModule {

    BishopApplication bishopApplication;

    public BishopApplicationModule(BishopApplication bishopApplication) {
        this.bishopApplication = bishopApplication;
    }

    @Provides
    @Singleton
    SharedPreferences providesSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(bishopApplication);
    }
}
