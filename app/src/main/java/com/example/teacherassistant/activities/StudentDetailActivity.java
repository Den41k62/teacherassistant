package com.example.teacherassistant.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacherassistant.R;
import com.example.teacherassistant.adapters.GradeAdapter;
import com.example.teacherassistant.database.DatabaseHelper;
import com.example.teacherassistant.models.Student;
import com.example.teacherassistant.models.Grade;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentDetailActivity extends AppCompatActivity implements GradeAdapter.OnGradeClickListener {
    private DatabaseHelper databaseHelper;
    private Student currentStudent;
    private List<Grade> grades = new ArrayList<>();

    private TextView tvStudentName, tvAverageGrade, tvColorLabel, tvStudentNotes, tvNoGrades;
    private View colorIndicator;
    private Button btnAddGrade;
    private RecyclerView gradesRecyclerView;
    private GradeAdapter gradeAdapter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public static void start(AppCompatActivity activity, int studentId) { // Запуск активности
        android.content.Intent intent = new android.content.Intent(activity, StudentDetailActivity.class);
        intent.putExtra("studentId", studentId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        int studentId = getIntent().getIntExtra("studentId", -1);
        databaseHelper = DatabaseHelper.getInstance(this);

        initViews(); // Инициализация View

        new Thread(() -> loadStudentData(studentId)).start(); // Загрузить данные

        setupClickListeners(); // Настроить клики
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHelper.closeDatabase(); // Закрыть БД
    }

    private void initViews() { // Инициализация
        tvStudentName = findViewById(R.id.tvStudentName);
        tvAverageGrade = findViewById(R.id.tvAverageGrade);
        tvColorLabel = findViewById(R.id.tvColorLabel);
        tvStudentNotes = findViewById(R.id.tvStudentNotes);
        tvNoGrades = findViewById(R.id.tvNoGrades);
        colorIndicator = findViewById(R.id.colorIndicator);
        btnAddGrade = findViewById(R.id.btnAddGrade);
        gradesRecyclerView = findViewById(R.id.gradesRecyclerView);

        gradesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadStudentData(int studentId) { // Загрузить ученика
        currentStudent = databaseHelper.getStudentById(studentId);

        runOnUiThread(() -> {
            if (currentStudent == null) {
                finish();
                return;
            }

            setTitle(currentStudent.getFullName());
            updateUI(); // Обновить UI
        });
    }

    private void updateUI() { // Обновить интерфейс
        if (currentStudent == null) return;

        tvStudentName.setText(currentStudent.getFullName());
        tvAverageGrade.setText(String.format("%.1f", currentStudent.getAverageGrade()));

        if (currentStudent.getColorCode() != 0) {
            colorIndicator.setBackgroundColor(currentStudent.getColorCode());
            colorIndicator.setVisibility(View.VISIBLE);
            tvColorLabel.setText(currentStudent.getColorLabel() != null ? currentStudent.getColorLabel() : "");
        } else {
            colorIndicator.setVisibility(View.INVISIBLE);
            tvColorLabel.setText("");
        }

        if (currentStudent.getNotes() != null && !currentStudent.getNotes().isEmpty()) {
            tvStudentNotes.setText(currentStudent.getNotes());
        }

        grades = databaseHelper.getGradesByStudent(currentStudent.getId());
        if (grades.isEmpty()) {
            tvNoGrades.setVisibility(View.VISIBLE);
            gradesRecyclerView.setVisibility(View.GONE);
        } else {
            tvNoGrades.setVisibility(View.GONE);
            gradesRecyclerView.setVisibility(View.VISIBLE);
            gradeAdapter = new GradeAdapter(grades, this);
            gradesRecyclerView.setAdapter(gradeAdapter);
        }
    }

    private void setupClickListeners() { // Настроить кнопки
        btnAddGrade.setOnClickListener(v -> showAddGradeDialog());
    }

    private void showAddGradeDialog() { // Диалог добавления оценки
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Поставить оценку");

        String[] grades = {"1", "2", "3", "4", "5", "Н"};
        builder.setItems(grades, (dialog, which) -> {
            int gradeValue;
            if (which == 5) {
                gradeValue = 0;
            } else {
                gradeValue = which + 1;
            }

            String subject = "Предмет";
            currentStudent = databaseHelper.getStudentById(currentStudent.getId());
            if (currentStudent != null) {
                List<com.example.teacherassistant.models.SchoolClass> classes = databaseHelper.getAllClasses();
                for (com.example.teacherassistant.models.SchoolClass schoolClass : classes) {
                    List<Student> students = databaseHelper.getStudentsByClass(schoolClass.getId());
                    for (Student student : students) {
                        if (student.getId() == currentStudent.getId()) {
                            subject = schoolClass.getSubject() != null ? schoolClass.getSubject() : "Предмет";
                            break;
                        }
                    }
                }
            }

            Grade grade = new Grade(currentStudent.getId(), gradeValue, subject);
            databaseHelper.addGrade(grade);
            updateUI();
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    @Override
    public void onGradeLongClick(Grade grade) { // Долгий клик - удалить оценку
        new AlertDialog.Builder(this)
                .setTitle("Удалить оценку")
                .setMessage("Вы уверены, что хотите удалить оценку \"" +
                        (grade.getValue() == 0 ? "Н" : grade.getValue()) +
                        "\" по предмету \"" + grade.getSubject() + "\" от " +
                        dateFormat.format(grade.getDate()) + "?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    new Thread(() -> {
                        databaseHelper.deleteGrade(grade.getId());
                        runOnUiThread(this::updateUI);
                    }).start();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}