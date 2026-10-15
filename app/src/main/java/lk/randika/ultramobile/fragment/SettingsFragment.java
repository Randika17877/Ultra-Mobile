package lk.randika.ultramobile.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import lk.randika.ultramobile.R;

public class SettingsFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UltraMobilePrefs";
    private static final String KEY_BATTERY_SAVER = "battery_saver_mode";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Call Support Functionality
        view.findViewById(R.id.btn_call_support).setOnClickListener(v -> makePhoneCall("+94112345678"));

        // Map Functionality - Open Location in Maps Activity inside app
        view.findViewById(R.id.btn_our_location).setOnClickListener(v -> {
            if (lk.randika.ultramobile.helper.BatteryHelper.isBatterySaverEnabled(getContext())) {
                Toast.makeText(getContext(), R.string.msg_maps_disabled, Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getActivity(), lk.randika.ultramobile.activity.MapsActivity.class);
            startActivity(intent);
        });


        // Placeholder for Terms and Privacy
        view.findViewById(R.id.btn_terms).setOnClickListener(v -> Toast.makeText(getContext(), getString(R.string.settings_terms) + " " + getString(R.string.msg_coming_soon), Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_privacy).setOnClickListener(v -> Toast.makeText(getContext(), getString(R.string.settings_privacy) + " " + getString(R.string.msg_coming_soon), Toast.LENGTH_SHORT).show());
    }

    private void makePhoneCall(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to open dialer", Toast.LENGTH_SHORT).show();
        }
    }
}