package com.example.teacherassistant.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacherassistant.R;
import com.example.teacherassistant.adapters.StudentAdapter;
import com.example.teacherassistant.database.DatabaseHelper;
import com.example.teacherassistant.models.SchoolClass;
import com.example.teacherassistant.models.Student;
import com.example.teacherassistant.models.Grade;

import java.util.ArrayList;
import java.util.List;

public class ClassActivity extends AppCompatActivity implements StudentAdapter.OnStudentClickListener {
    private RecyclerView studentsRecyclerView;
    private StudentAdapter studentAdapter;
    private DatabaseHelper databaseHelper;
    private SchoolClass currentClass;
    private List<Student> students = new ArrayList<>();

    public static void start(AppCompatActivity activity, int classId) { // Запуск активности
        Intent intent = new Intent(activity, ClassActivity.class);
        intent.putExtra("classId", classId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        int classId = getIntent().getIntExtra("classId", -1);
        databaseHelper = DatabaseHelper.getInstance(this);

        // Загружаем класс
        new Thread(() -> {
            currentClass = databaseHelper.getClassById(classId);
            runOnUiThread(() -> {
                if (currentClass == null) {
                    finish();
                    return;
                }

                setTitle(currentClass.getClassName() + " (" + currentClass.getSubject() + ")");

                studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
                studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

                studentAdapter = new StudentAdapter(students, this);
                studentsRecyclerView.setAdapter(studentAdapter);

                loadStudents(); // Загрузить учеников

                findViewById(R.id.fabAddStudent).setOnClickListener(v -> showAddStudentDialog());
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void loadStudents() { // Загрузить учеников
        new Thread(() -> {
            if (currentClass != null) {
                List<Student> newStudents = databaseHelper.getStudentsByClass(currentClass.getId());
                runOnUiThread(() -> {
                    students.clear();
                    students.addAll(newStudents);
                    studentAdapter.notifyDataSetChanged();
                });
            }
        }).start();
    }

    private void showAddStudentDialog() { // Диалог добавления ученика
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить ученика");

        EditText editText = new EditText(this);
        editText.setHint("ФИО ученика");
        builder.setView(editText);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String fullName = editText.getText().toString();
            if (!fullName.isEmpty()) {
                Student student = new Student(fullName, currentClass.getId());
                new Thread(() -> {
                    databaseHelper.addStudent(student);
                    runOnUiThread(this::loadStudents);
                }).start();
            }
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    @Override
    public void onStudentClick(Student student) { // Клик по ученику
        StudentDetailActivity.start(this, student.getId());
    }

    @Override
    public void onAddGradeClick(Student student) { // Добавить оценку
        showGradeDialog(student);
    }

    @Override
    public void onChangeColorClick(Student student) { // Сменить цвет
        showColorDialog(student);
    }

    @Override
    public void onAddNoteClick(Student student) { // Добавить заметку
        showNoteDialog(student);
    }

    @Override
    public void onDeleteGradesClick(Student student) { // Удалить оценки
        new AlertDialog.Builder(this)
                .setTitle("Удалить все оценки")
                .setMessage("Удалить все оценки " + student.getFullName() + "?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    new Thread(() -> {
                        databaseHelper.deleteAllGradesForStudent(student.getId());
                        runOnUiThread(this::loadStudents);
                    }).start();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showGradeDialog(Student student) { // Диалог выбора оценки
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Поставить оценку " + student.getFullName());

        String[] grades = {"1", "2", "3", "4", "5", "Н"};
        builder.setItems(grades, (dialog, which) -> {
            int gradeValue;
            String subject = currentClass.getSubject() != null ? currentClass.getSubject() : "Предмет";

            if (which == 5) {
                gradeValue = 0;
            } else {
                gradeValue = which + 1;
            }

            Grade grade = new Grade(student.getId(), gradeValue, subject);
            new Thread(() -> {
                databaseHelper.addGrade(grade);
                runOnUiThread(this::loadStudents);
            }).start();
        });
        builder.show();
    }

    private void showColorDialog(Student student) { // Диалог выбора цвета
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите цвет " + student.getFullName());

        final int[] colors = {
                getColor(R.color.color_red),
                getColor(R.color.color_green),
                getColor(R.color.color_blue),
                getColor(R.color.color_yellow),
                getColor(R.color.color_orange),
                getColor(R.color.color_purple)
        };

        final String[] colorLabels = {"Красный", "Зеленый", "Синий", "Желтый", "Оранжевый", "Фиолетовый"};

        builder.setItems(colorLabels, (dialog, which) -> {
            student.updateStudentData(student.getNotes(), colors[which], colorLabels[which]);
            new Thread(() -> {
                databaseHelper.updateStudent(student);
                runOnUiThread(this::loadStudents);
            }).start();
        });
        builder.show();
    }

    private void showNoteDialog(Student student) { // Диалог заметки
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Заметка " + student.getFullName());

        EditText editText = new EditText(this);
        editText.setHint("Введите заметку");
        if (student.getNotes() != null) {
            editText.setText(student.getNotes());
        }
        builder.setView(editText);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String note = editText.getText().toString();
            student.updateStudentData(note, student.getColorCode(), student.getColorLabel());
            new Thread(() -> {
                databaseHelper.updateStudent(student);
                runOnUiThread(this::loadStudents);
            }).start();
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // Создать меню
        getMenuInflater().inflate(R.menu.class_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // Выбор пункта меню
        if (item.getItemId() == R.id.menu_class_notes) {
            showClassNotesDialog();
            return true;
        } else if (item.getItemId() == R.id.menu_copy_class) {
            showCopyClassDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showClassNotesDialog() { // Заметки класса
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Заметки класса " + currentClass.getClassName());

        EditText editText = new EditText(this);
        editText.setHint("Введите заметки для класса");
        if (currentClass.getNotes() != null) {
            editText.setText(currentClass.getNotes());
        }
        builder.setView(editText);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String note = editText.getText().toString();
            currentClass.setNotes(note);
            new Thread(() -> {
                databaseHelper.updateClass(currentClass);
                runOnUiThread(() ->
                        setTitle(currentClass.getClassName() + " (" + currentClass.getSubject() + ")")
                );
            }).start();
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showCopyClassDialog() { // Копировать класс
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Копировать класс");

        EditText editText = new EditText(this);
        editText.setHint("Новый предмет");
        builder.setView(editText);

        builder.setPositiveButton("Создать", (dialog, which) -> {
            String newSubject = editText.getText().toString();
            if (!newSubject.isEmpty()) {
                new Thread(() -> copyClassWithNewSubject(newSubject)).start();
            }
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void copyClassWithNewSubject(String newSubject) { // Копирование с новым предметом
        SchoolClass newClass = new SchoolClass(currentClass.getClassName(), newSubject);

        long newClassId = databaseHelper.addClass(newClass);

        if (newClassId != -1) {
            for (Student student : students) {
                Student newStudent = new Student(student.getFullName(), (int) newClassId);
                databaseHelper.addStudent(newStudent);
            }

            runOnUiThread(() -> new AlertDialog.Builder(this)
                    .setTitle("Успешно")
                    .setPositiveButton("ОК", null)
                    .show());
        }
    }

    @Override
    public void onStudentLongClick(Student student) { // Долгий клик - удалить
        new AlertDialog.Builder(this)
                .setTitle("Удалить ученика")
                .setMessage("Удалить " + student.getFullName() + "?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    new Thread(() -> {
                        databaseHelper.deleteStudent(student.getId());
                        runOnUiThread(this::loadStudents);
                    }).start();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}