package com.deckerth.thomas.foobarremotecontroller2.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.deckerth.thomas.foobarremotecontroller2.model.VolumeControl;

public class VolumeControlViewModel extends ViewModel {

    private VolumeControl mVolumeControl = new VolumeControl();

    public VolumeControl getVolumeControl() {
        return mVolumeControl;
    }

    private final MutableLiveData<Integer> mVolumePercent;

    private final MutableLiveData<Boolean> mIsMuted;

    public VolumeControlViewModel() {
        mVolumePercent = new MutableLiveData<>();
        mVolumePercent.setValue(mVolumeControl.getCurrentValuePercent());

        mIsMuted = new MutableLiveData<>();
        mIsMuted.setValue(mVolumeControl.getMuted());
    }

    public LiveData<Integer> getVolume() {return mVolumePercent;}
    public LiveData<Boolean> getIsMuted() {return mIsMuted;}

    public void setVolume(Integer decibel) {
        mVolumeControl.setValue(decibel); //-100..0
        mVolumePercent.setValue(mVolumeControl.getCurrentValuePercent());
    }

    public void setIsMuted(Boolean value) {
        mIsMuted.setValue(value);
        mVolumeControl.setMuted(value);
    }
}
