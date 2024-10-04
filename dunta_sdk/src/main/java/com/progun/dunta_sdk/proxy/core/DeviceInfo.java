package com.progun.dunta_sdk.proxy.core;

import androidx.annotation.NonNull;

import java.util.Objects;

public class DeviceInfo {
    private int sdkVersion;
    private int totalRam;
    private String cpuArch;
    private String deviceModel;
    private String deviceId;

    public DeviceInfo(int sdk, int ram, @NonNull String arch, @NonNull String model) {
        sdkVersion = sdk;
        totalRam = ram;
        cpuArch = arch;
        deviceModel = model;
    }



    public DeviceInfo(int sdk, int ram, @NonNull String arch, @NonNull String model, @NonNull String deviceId) {
        sdkVersion = sdk;
        totalRam = ram;
        cpuArch = arch;
        deviceModel = model;
        this.deviceId = deviceId;
    }
    public String getDeviceId() { return deviceId; }

    public int getSdkVersion() { return sdkVersion; }

    public int getTotalRam() { return totalRam; }

    public String getCpuArch() { return cpuArch; }

    public String getDeviceModel() { return deviceModel; }

    public void setSdkVersion(int sdkVersion) { this.sdkVersion = sdkVersion; }

    public void setTotalRam(int totalRam) { this.totalRam = totalRam; }

    public void setCpuArch(String cpuArch) { this.cpuArch = cpuArch; }

    public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }

    @NonNull
    @Override
    public String toString() {
        return "Device info: sdk=" + sdkVersion +
                "/ ram=" + totalRam +
                "/ cpu=" + cpuArch +
                "/ model=" + deviceModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceInfo that = (DeviceInfo) o;
        return sdkVersion == that.sdkVersion && totalRam == that.totalRam && cpuArch.equals(that.cpuArch) && deviceModel.equals(
                that.deviceModel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sdkVersion, totalRam, cpuArch, deviceModel);
    }
}
