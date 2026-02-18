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

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Устанавливаем заголовок приложения
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Помощник учителя");
        }

        // Используем синглтон
        databaseHelper = DatabaseHelper.getInstance(this);

        classesRecyclerView = findViewById(R.id.classesRecyclerView);
        classesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Инициализируем адаптер с пустым списком
        classAdapter = new ClassAdapter(classes, this);
        classesRecyclerView.setAdapter(classAdapter);

        loadClasses();

        findViewById(R.id.fabAddClass).setOnClickListener(v -> showAddClassDialog());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Закрываем базу данных только при уничтожении активности
        databaseHelper.closeDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем данные при возвращении на экран
        loadClasses();
    }

    private void loadClasses() {
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

    private void showAddClassDialog() {
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
    public void onClassClick(SchoolClass schoolClass) {
        ClassActivity.start(this, schoolClass.getId());
    }

    @Override
    public void onClassLongClick(SchoolClass schoolClass) {
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