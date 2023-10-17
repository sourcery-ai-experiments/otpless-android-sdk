package com.otpless.otplesssample;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.otpless.dto.OtplessResponse;
import com.otpless.main.OtplessManager;
import com.otpless.main.OtplessView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeSecondFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeSecondFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeSecondFragment() {
        // Required empty public constructor
    }

    private OtplessView otplessView;

    public static HomeSecondFragment newInstance(String param1, String param2) {
        HomeSecondFragment fragment = new HomeSecondFragment();
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
        return inflater.inflate(R.layout.fragment_home_second, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        otplessView = OtplessManager.getInstance().getOtplessView(requireActivity());

        view.findViewById(R.id.open_otpless_btn).setOnClickListener(v -> {
            startOtpless();
        });
    }

    private void startOtpless() {
        otplessView.startOtpless(null, this::onOtplessResponse);
    }

    private void onOtplessResponse(final OtplessResponse response) {
        String message = response.toString();
        message = "second: " + message;
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
}