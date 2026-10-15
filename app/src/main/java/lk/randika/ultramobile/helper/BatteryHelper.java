package lk.randika.ultramobile.helper;

import android.content.Context;
import android.os.PowerManager;

public class BatteryHelper {
    public static boolean isBatterySaverEnabled(Context context) {
        if (context == null) return false;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            return powerManager.isPowerSaveMode();
        }
        return false;
    }
}
