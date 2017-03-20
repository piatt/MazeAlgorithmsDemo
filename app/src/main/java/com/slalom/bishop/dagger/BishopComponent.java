package com.slalom.bishop.dagger;

import com.slalom.bishop.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by petrask on 4/30/16.
 */
@Singleton
@Component(modules = {BishopApplicationModule.class})
public interface BishopComponent {
    void inject(MainActivity activity);
}