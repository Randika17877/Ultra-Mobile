package lk.randika.ultramobile.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import lk.randika.ultramobile.R;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Call Support Functionality
        view.findViewById(R.id.btn_call_support).setOnClickListener(v -> {
            makePhoneCall("+94112345678");
        });

        // Map Functionality - Open Location in Maps
        view.findViewById(R.id.btn_our_location).setOnClickListener(v -> {
            openLocationInMap("6.9271", "79.8612", "UltraMobile Main Branch");
        });

        // Placeholder for Terms and Privacy
        view.findViewById(R.id.btn_terms).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Terms and Conditions coming soon", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btn_privacy).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Privacy Policy coming soon", Toast.LENGTH_SHORT).show();
        });
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

    private void openLocationInMap(String latitude, String longitude, String label) {
        Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + Uri.encode(label));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        
        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback to any app that can handle geo intents if Google Maps isn't installed
            Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            try {
                startActivity(fallbackIntent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "No map application found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}