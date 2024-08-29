package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class NewTeamFragment extends Fragment {

    private String groupNumber;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_team, container, false);

        Button enterManagerDetailsButton = view.findViewById(R.id.enterManagerDetailsButton);
        Button createButton = view.findViewById(R.id.createButton);
        EditText groupNumberInput = view.findViewById(R.id.groupNumberInput);

        enterManagerDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(NewTeamFragment.this)
                        .navigate(R.id.action_NewTeamFragment_to_NewMemberFragment);
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupNumber = groupNumberInput.getText().toString();
                // Handle the button click, groupNumber variable contains the entered group number
            }
        });

        return view;
    }
}
