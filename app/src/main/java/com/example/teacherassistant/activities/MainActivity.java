package com.example.teacherassistant.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacherassistant.R;
import com.example.teacherassistant.adapters.ClassAdapter;
import com.example.teacherassistant.database.DatabaseHelper;
import com.example.teacherassistant.models.SchoolClass;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ClassAdapter.OnClassClickListener {
    private RecyclerView classesRecyclerView;
    private ClassAdapter classAdapter;
    private DatabaseHelper databaseHelper;
    private List<SchoolClass> classes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Помощник учителя");
        }

        databaseHelper = DatabaseHelper.getInstance(this);

        classesRecyclerView = findViewById(R.id.classesRecyclerView);
        classesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        classAdapter = new ClassAdapter(classes, this);
        classesRecyclerView.setAdapter(classAdapter);

        loadClasses(); // Загрузить классы

        findViewById(R.id.fabAddClass).setOnClickListener(v -> showAddClassDialog());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHelper.closeDatabase(); // Закрыть БД
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClasses(); // Обновить при возврате
    }

    private void loadClasses() { // Загрузить классы из БД
        new Thread(() -> {
            List<SchoolClass> newClasses = databaseHelper.getAllClasses();
            for (SchoolClass schoolClass : newClasses) {
                schoolClass.setStudents(databaseHelper.getStudentsByClass(schoolClass.getId()));
            }

            runOnUiThread(() -> {
                classes.clear();
                classes.addAll(newClasses);
                classAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void showAddClassDialog() { // Диалог добавления класса
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить класс");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_class, null);
        EditText classNameEditText = dialogView.findViewById(R.id.classNameEditText);
        EditText subjectEditText = dialogView.findViewById(R.id.subjectEditText);

        builder.setView(dialogView);
        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String className = classNameEditText.getText().toString();
            String subject = subjectEditText.getText().toString();

            if (!className.isEmpty()) {
                SchoolClass newClass = new SchoolClass(className, subject);
                new Thread(() -> {
                    databaseHelper.addClass(newClass);
                    runOnUiThread(this::loadClasses);
                }).start();
            }
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    @Override
    public void onClassClick(SchoolClass schoolClass) { // Клик по классу
        ClassActivity.start(this, schoolClass.getId());
    }

    @Override
    public void onClassLongClick(SchoolClass schoolClass) { // Долгий клик - удалить
        new AlertDialog.Builder(this)
                .setTitle("Удалить класс")
                .setMessage("Вы уверены, что хотите удалить класс " + schoolClass.getClassName() + "?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    new Thread(() -> {
                        databaseHelper.deleteClass(schoolClass.getId());
                        runOnUiThread(this::loadClasses);
                    }).start();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}