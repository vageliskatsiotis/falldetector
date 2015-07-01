package com.example.vag.falldetector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
// Ρουτίνα εκκίνησης υπηρεσίας στην εκκίνηση
public class StartFallDetectorAtBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, MainActivity.class);
            context.startService(serviceIntent);
			context.startActivity(new Intent(context, MainActivity.class));
        }
    }
}