package common.viewmodel

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import common.model.SplitTunnelingApp

class SplitTunnelingVIewModel : ViewModel() {

    fun getAppListWithInternetPermission(packageManager: PackageManager): List<SplitTunnelingApp> {
        val appList = mutableListOf<SplitTunnelingApp>()
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in installedApps) {
            if(app.packageName == "com.vpnduck" || app.packageName == "com.vpndonkey") continue
            if (app.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                val permissions = packageManager.getPackageInfo(
                    app.packageName,
                    PackageManager.GET_PERMISSIONS
                ).requestedPermissions
                permissions?.let {
                    if (it.contains("android.permission.INTERNET")) {
                        appList.add(
                            SplitTunnelingApp(
                                image = app.loadIcon(packageManager),
                                appName = app.loadLabel(packageManager).toString(),
                                packageName = app.packageName
                            )
                        )
                    }
                }
            }
        }
        return appList
    }
}