/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import openvpn.R;


public class VPNLaunchHelper {
    private static final String MIN_IN_ON_PIE_VPN = "nopie_openvpn";
    private static final String MINIPIEVPN = "pie_openvpn";
    private static final String OVPNCONFIGFILE = "android.conf";


    private static String writeMiniVPN(Context context) {

        String nativeAPI = NativeUtils.getNativeAPI();
        /* Q does not allow executing binaries written in temp directory anymore */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            return new File(
                    context.getApplicationInfo().nativeLibraryDir,
                    "libovpnexec.so"
            ).getPath();

        String[] abis;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            abis = getSupportedABIsLollipop();
        else
            //noinspection deprecation
            abis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};

//        String nativeAPI = NativeUtils.getNativeAPI();

        if (!nativeAPI.equals(abis[0])) {
            VpnStatus.logWarning(R.string.abi_mismatch, Arrays.toString(abis), nativeAPI);
            abis = new String[]{nativeAPI};
        }

        for (String abi : abis) {

            File vpnExecutable =
                    new File(context.getCacheDir(), getMiniVPNExecutableName() + "." + abi);
            if ((vpnExecutable.exists() && vpnExecutable.canExecute()) || writeMiniVPNBinary(
                    context,
                    abi,
                    vpnExecutable
            )) {
                return vpnExecutable.getPath();
            }
        }

        return null;
    }

    private static String[] getSupportedABIsLollipop() {
        return Build.SUPPORTED_ABIS;
    }

    private static String getMiniVPNExecutableName() {
        return MINIPIEVPN;
    }


    public static String[] replacePieWithNoPie(String[] mArgv) {
        mArgv[0] = mArgv[0].replace(MINIPIEVPN, MIN_IN_ON_PIE_VPN);
        return mArgv;
    }


    static String[] buildOpenvpnArgv(Context c) {
        Vector<String> args = new Vector<>();

        String binaryName = writeMiniVPN(c);
        if (binaryName == null) {
            VpnStatus.logError("Error writing minivpn binary");
            return null;
        }

        args.add(binaryName);

        args.add("--config");
        args.add(getConfigFilePath(c));

        return args.toArray(new String[0]);
    }

    private static boolean writeMiniVPNBinary(Context context, String abi, File mvpnout) {
        try {
            InputStream mvpn;
            try {
                mvpn = context.getAssets().open(getMiniVPNExecutableName() + "." + abi);
            } catch (IOException errabi) {
                VpnStatus.logInfo("Failed getting assets for archicture " + abi);
                return false;
            }

            FileOutputStream fout = new FileOutputStream(mvpnout);

            byte[] buf = new byte[4096];

            int lenread = mvpn.read(buf);
            while (lenread > 0) {
                fout.write(buf, 0, lenread);
                lenread = mvpn.read(buf);
            }
            fout.close();

            if (!mvpnout.setExecutable(true)) {
                VpnStatus.logError("Failed to make OpenVPN executable");
                return false;
            }
            return true;
        } catch (IOException e) {
            VpnStatus.logException(e);
            return false;
        }
    }


    public static void startOpenVpn(final VpnProfile startprofile, final Context context) {
        // Создаем ExecutorService для выполнения задачи в фоновом потоке
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Отправляем задачу на выполнение в фоновом потоке
        executor.submit(new Runnable() {
            @Override
            public void run() {
                // Этот код выполняется в фоновом потоке
                Intent startVPN = startprofile.prepareStartService(context);
                if (startVPN != null) {
                    context.startService(startVPN);
                }
            }
        });

        // Закрываем executor, когда он больше не нужен
        executor.shutdown();
    }

    public static String getConfigFilePath(Context context) {
        return context.getCacheDir().getAbsolutePath() + "/" + OVPNCONFIGFILE;
    }

}
