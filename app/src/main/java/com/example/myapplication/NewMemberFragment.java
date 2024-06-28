package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewMemberFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewMemberFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public NewMemberFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewMemberFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewMemberFragment newInstance(String param1, String param2) {
        NewMemberFragment fragment = new NewMemberFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the title in the ActionBar
        ActionBar actionBar=((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Add a New Team Friend");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_member, container, false);

        EditText editTextName=view.findViewById(R.id.editTextName);
        EditText editTextId=view.findViewById(R.id.editTextId);
        EditText editTextPhoneNumber=view.findViewById(R.id.editTextPhoneNumber);
        EditText editTextPassword=view.findViewById(R.id.editTextPassword);
        EditText editTextAddress=view.findViewById(R.id.editTextAddress);
        EditText editTextRole=view.findViewById(R.id.editTextRole);
        Button buttonAdd=view.findViewById(R.id.buttonAdd);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString();
                String phoneNumber = editTextPhoneNumber.getText().toString();
                String address = editTextAddress.getText().toString();
                String id = editTextAddress.getText().toString();
                String password = editTextAddress.getText().toString();
                String role = editTextAddress.getText().toString();

                if (!name.isEmpty() && !phoneNumber.isEmpty() && !address.isEmpty() && !id.isEmpty() && !password.isEmpty() && !role.isEmpty()) {
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MemberPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("name", name);
                    editor.putString("phoneNumber", phoneNumber);
                    editor.putString("address", address);
                    editor.putString("id", address);
                    editor.putString("password", address);
                    editor.putString("role", address);
                    editor.apply();
//
//                    Toast.makeText(getActivity(), "Details Saved", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}