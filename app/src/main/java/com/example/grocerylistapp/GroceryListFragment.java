package com.example.grocerylistapp;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Calendar;

public class GroceryListFragment extends Fragment {
    private FirebaseAuth mAuth;
    private static String TAG = "GroceriesListApp";
    public GroceryListFragment() {
        // Required empty public constructor
    }

    ArrayList<Item> groceries = new ArrayList<>();
    GroceriesAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    EditText editTextNewItem;
    RecyclerView recyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_grocery_list, container, false);
        getActivity().setTitle("Grocery List");
        getData();
        editTextNewItem = view.findViewById(R.id.editTextNewItem);
        view.findViewById(R.id.buttonSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c1 = Calendar.getInstance();
                Date date = c1.getTime();
                Long time = date.getTime();
                Item newItem = new Item(editTextNewItem.getText().toString(),false, time, "");
                addItem(newItem);
                editTextNewItem.setText("");
            }
        });
        view.findViewById(R.id.buttonDeleteChecked).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Delete Checked Items")
                        .setMessage("Are you sure you want to delete all of the checked items?")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ArrayList<Item> toDelete = new ArrayList<>();
                                for (Item item: groceries) {
                                    if (item.checked) {
                                        toDelete.add(item);
                                    }
                                }
                                for (Item item: toDelete) {
                                    deleteItem(item);
                                }
                            }
                        });
                builder.create().show();
            }
        });

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new GroceriesAdapter();
        recyclerView.setAdapter(adapter);

        return view;
    }



    private void getData(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("groceries")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        groceries.clear();
                        for(QueryDocumentSnapshot document: value){
                            Log.d(TAG, "onEvent: "+document.getId());
                            groceries.add(new Item(document.getString("name"), document.getBoolean("checked"), document.getLong("creationTime"), document.getId()));
                        }
                        groceries.sort(new Comparator<Item>() {
                            @Override
                            public int compare(Item item1, Item item2) {
                                int sign = 0;
                                if (item1.creationTime-item2.creationTime == 0) {
                                    sign = 0;
                                } else if (item1.creationTime-item2.creationTime < 0) {
                                    sign = -1;
                                } else {
                                    sign = 1;
                                }
                                return sign;
                            }
                        });
                        Log.d("demo", "onEvent: "+groceries);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void addItem(Item item) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String, Object> itemMap = new HashMap<>();
        itemMap.put("name", item.name);
        itemMap.put("checked", item.checked);
        itemMap.put("creationTime", item.creationTime);

        db.collection("groceries")
                .add(itemMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "onSuccess: Item Added: "+item.name);
                    }
                });
    }

    private void updateItem(Item item) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String, Object> itemMap = new HashMap<>();
        itemMap.put("name", item.name);
        itemMap.put("checked", item.checked);
        itemMap.put("creationTime", item.creationTime);

        db.collection("groceries")
                .document(item.itemId)
                .update(itemMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                });
    }

    private void deleteItem(Item item) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("groceries")
                .document(item.itemId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                });
    }


    public class GroceriesAdapter extends RecyclerView.Adapter<GroceriesAdapter.GroceriesViewHolder> {

        public GroceriesAdapter() {

        }

        @NonNull
        @Override
        public GroceriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grocery_row_item, parent, false);
            GroceriesViewHolder groceriesViewHolder = new GroceriesViewHolder(view);
            return groceriesViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull GroceriesViewHolder holder, int position) {
            Item item = groceries.get(position);
            holder.editTextItem.setText(item.name);
            holder.item = item;
            holder.checkBox.setChecked(item.checked);
        }

        @Override
        public int getItemCount() {
            return groceries.size();
        }

        class GroceriesViewHolder extends RecyclerView.ViewHolder {
            Item item;
            EditText editTextItem;
            ImageView imageViewDelete;
            CheckBox checkBox;
            View rootView;


            public GroceriesViewHolder(@NonNull View itemView) {
                super(itemView);
                rootView = itemView;
                editTextItem = itemView.findViewById(R.id.editTextItem);
                imageViewDelete = itemView.findViewById(R.id.imageViewDelete);
                checkBox = itemView.findViewById(R.id.checkBox);
                imageViewDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteItem(item);
                        Log.d(TAG, "onClick: DELETE ITEM"+item);
                    }
                });
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        item.checked = checkBox.isChecked();
                        updateItem(item);
                    }
                });
                editTextItem.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if (!b) {
                            item.name = editTextItem.getText().toString();
                            updateItem(item);
                        }
                    }
                });
            }
        }
    }


}