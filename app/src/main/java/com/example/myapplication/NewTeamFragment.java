package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class NewTeamFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_team, container, false);

        Button enterManagerDetailsButton = view.findViewById(R.id.enterManagerDetailsButton);
        Button createButton = view.findViewById(R.id.createButton);

        // כרגע הכפתור לא מבצע שום פעולה
        enterManagerDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // פעולה שתתבצע בעת לחיצה על הכפתור
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // פעולה שתתבצע בעת לחיצה על הכפתור
            }
        });

        return view;
    }
}