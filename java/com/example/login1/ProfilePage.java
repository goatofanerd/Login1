package com.example.login1;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class ProfilePage extends Fragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GoogleSignInAccount account = (GoogleSignInAccount) getActivity().getIntent().getExtras().get("account");
        TextView name = getView().findViewById(R.id.showName);
        TextView email = getView().findViewById(R.id.showEmail);
        name.setText(name.getText() + account.getDisplayName());
        email.setText(email.getText() + account.getEmail());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_page, container, false);
    }
}