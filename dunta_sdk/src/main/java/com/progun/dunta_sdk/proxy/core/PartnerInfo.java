package com.progun.dunta_sdk.proxy.core;

public class PartnerInfo {
        int deviceId;
        int appId;
        int partnerId;

        public PartnerInfo(int deviceId, int appId, int partnerId) {
                this.deviceId = deviceId;
                this.appId = appId;
                this.partnerId = partnerId;
        }

        public int getDeviceId() {
                return deviceId;
        }

        public void setDeviceId(int deviceId) {
                this.deviceId = deviceId;
        }

        public int getAppId() {
                return appId;
        }

        public void setAppId(int appId) {
                this.appId = appId;
        }

        public int getPartnerId() {
                return partnerId;
        }

        public void setPartnerId(int partnerId) {
                this.partnerId = partnerId;
        }


}

