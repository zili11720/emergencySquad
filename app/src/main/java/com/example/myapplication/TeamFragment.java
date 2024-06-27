package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;

public class TeamFragment extends Fragment {
    private RecyclerView recyclerView;
    // private PersonAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<HashMap<String, String>> personList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team, container, false);
        return view;
    }
}
//        // טעינת רשימת האנשים הקיימת
//        personList = loadPersonList();
//
//        recyclerView = view.findViewById(R.id.recyclerViewPeople);
//        recyclerView.setHasFixedSize(true);
//        layoutManager = new LinearLayoutManager(getActivity());
//        adapter = new PersonAdapter(personList);
//
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setAdapter(adapter);
//
//        Button btnAddPerson = view.findViewById(R.id.btnAddPerson);
//        btnAddPerson.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // לוגיקה להוספת אדם חדש
//                HashMap<String, String> newPerson = new HashMap<>();
//                newPerson.put("שם", "New Person");
//                newPerson.put("טלפון", "123-456-7890");
//                newPerson.put("תפקיד", "תפקיד חדש");
//                personList.add(newPerson);
//                adapter.notifyItemInserted(personList.size() - 1);
//            }
//        });
//
//        adapter.setOnItemClickListener(new PersonAdapter.OnItemClickListener() {
//            @Override
//            public void onDeleteClick(int position) {
//                personList.remove(position);
//                adapter.notifyItemRemoved(position);
//            }
//        });
//
//        return view;
//    }
//
//    // מתודה לטעינת רשימת האנשים הקיימת
//    private ArrayList<HashMap<String, String>> loadPersonList() {
//        ArrayList<HashMap<String, String>> list = new ArrayList<>();
//
//        HashMap<String, String> person1 = new HashMap<>();
//        person1.put("שם", "User 1");
//        person1.put("טלפון", "050-1234567");
//        person1.put("תפקיד", "Manager");
//        list.add(person1);
//
//        HashMap<String, String> person2 = new HashMap<>();
//        person2.put("שם", "User 2");
//        person2.put("טלפון", "050-2345678");
//        person2.put("תפקיד", "Developer");
//        list.add(person2);
//
//        HashMap<String, String> person3 = new HashMap<>();
//        person3.put("שם", "User 3");
//        person3.put("טלפון", "050-3456789");
//        person3.put("תפקיד", "Designer");
//        list.add(person3);
//
//        // הוספת עוד אנשים לפי הצורך
//        return list;
//    }
//
//    // Adapter למחלקה TeamFragment
//    private class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.PersonViewHolder> {
//        private ArrayList<HashMap<String, String>> personList;
//        private OnItemClickListener listener;
//
////        public interface OnItemClickListener {
////            void onDeleteClick(int position);
////        }
//
//        public void setOnItemClickListener(OnItemClickListener listener) {
//            this.listener = listener;
//        }
//
//        public class PersonViewHolder extends RecyclerView.ViewHolder {
//            public TextView tvName;
//            public TextView tvPhone;
//            public TextView tvRole;
//            public Button btnDelete;
//
//            public PersonViewHolder(View itemView, final OnItemClickListener listener) {
//                super(itemView);
//                tvName = itemView.findViewById(R.id.tvName);
//                tvPhone = itemView.findViewById(R.id.tvPhone);
//                tvRole = itemView.findViewById(R.id.tvRole);
//                btnDelete = itemView.findViewById(R.id.btnDelete);
//
//                btnDelete.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (listener != null) {
//                            int position = getAdapterPosition();
//                            if (position != RecyclerView.NO_POSITION) {
//                                listener.onDeleteClick(position);
//                            }
//                        }
//                    }
//                });
//            }
//        }
//
//        public PersonAdapter(ArrayList<HashMap<String, String>> personList) {
//            this.personList = personList;
//        }
//
//        @Override
//        public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_person, parent, false);
//            PersonViewHolder pvh = new PersonViewHolder(v, listener);
//            return pvh;
//        }
//    }
//}
