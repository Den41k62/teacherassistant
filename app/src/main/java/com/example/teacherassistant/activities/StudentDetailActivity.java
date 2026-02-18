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

    public static void start(AppCompatActivity activity, int studentId) {
        android.content.Intent intent = new android.content.Intent(activity, StudentDetailActivity.class);
        intent.putExtra("studentId", studentId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        int studentId = getIntent().getIntExtra("studentId", -1);
        // Используем синглтон вместо прямого вызова конструктора
        databaseHelper = DatabaseHelper.getInstance(this);

        initViews();

        // Загружаем данные в фоновом потоке
        new Thread(() -> loadStudentData(studentId)).start();

        setupClickListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Закрываем базу данных при уничтожении активности
        databaseHelper.closeDatabase();
    }

    private void initViews() {
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

    private void loadStudentData(int studentId) {
        // Используем метод getStudentById из DatabaseHelper
        currentStudent = databaseHelper.getStudentById(studentId);

        // Обновляем UI в основном потоке
        runOnUiThread(() -> {
            if (currentStudent == null) {
                finish();
                return;
            }

            setTitle(currentStudent.getFullName());
            updateUI();
        });
    }

    private void updateUI() {
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

        // Загружаем оценки
        grades = databaseHelper.getGradesByStudent(currentStudent.getId());
        if (grades.isEmpty()) {
            tvNoGrades.setVisibility(View.VISIBLE);
            gradesRecyclerView.setVisibility(View.GONE);
        } else {
            tvNoGrades.setVisibility(View.GONE);
            gradesRecyclerView.setVisibility(View.VISIBLE);
            // Передаем this как слушатель
            gradeAdapter = new GradeAdapter(grades, this);
            gradesRecyclerView.setAdapter(gradeAdapter);
        }
    }

    private void setupClickListeners() {
        btnAddGrade.setOnClickListener(v -> showAddGradeDialog());
    }

    private void showAddGradeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Поставить оценку");

        // Добавляем "Н" в список оценок
        String[] grades = {"1", "2", "3", "4", "5", "Н"};
        builder.setItems(grades, (dialog, which) -> {
            int gradeValue;
            if (which == 5) { // "Н" - это 6-й элемент
                gradeValue = 0;
            } else {
                gradeValue = which + 1;
            }

            // Определяем предмет из информации об ученике
            String subject = "Предмет";
            // Получаем класс ученика
            currentStudent = databaseHelper.getStudentById(currentStudent.getId());
            if (currentStudent != null) {
                // Получаем информацию о классе
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
            // Обновляем UI
            updateUI();
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    // Реализация интерфейса для удаления оценок
    @Override
    public void onGradeLongClick(Grade grade) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить оценку")
                .setMessage("Вы уверены, что хотите удалить оценку \"" +
                        (grade.getValue() == 0 ? "Н" : grade.getValue()) +
                        "\" по предмету \"" + grade.getSubject() + "\" от " +
                        dateFormat.format(grade.getDate()) + "?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    // Удаление в фоновом потоке
                    new Thread(() -> {
                        databaseHelper.deleteGrade(grade.getId());
                        // Обновляем UI в основном потоке
                        runOnUiThread(this::updateUI);
                    }).start();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}