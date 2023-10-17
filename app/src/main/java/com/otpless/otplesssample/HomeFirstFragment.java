package com.otpless.otplesssample;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.otpless.dto.OtplessResponse;
import com.otpless.main.OtplessManager;
import com.otpless.main.OtplessView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFirstFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFirstFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OtplessView otplessView;

    public HomeFirstFragment() {
        // Required empty public constructor
    }

    public static HomeFirstFragment newInstance(String param1, String param2) {
        HomeFirstFragment fragment = new HomeFirstFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_first, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.start_otpless_btn).setOnClickListener(v -> {
            startOtpless();
        });

        view.findViewById(R.id.open_second_fragment).setOnClickListener(v -> {
            openSecondFragment();
        });

        otplessView = OtplessManager.getInstance().getOtplessView(requireActivity());
        otplessView.showOtplessFab(false);

        final Intent activityIntent = requireActivity().getIntent();
        otplessView.verifyIntent(activityIntent);
    }

    private void startOtpless() {
        otplessView.startOtpless(null, this::onOtplessResponse);
    }

    private void openSecondFragment() {
        ((HomeActivity) requireActivity()).openSecondFragment();
    }

    private void onOtplessResponse(final OtplessResponse response) {
        String message = response.toString();
        message = "first: " + message;
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
}