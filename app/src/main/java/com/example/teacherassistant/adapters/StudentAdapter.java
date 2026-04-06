package com.example.teacherassistant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacherassistant.R;
import com.example.teacherassistant.models.Student;
import com.example.teacherassistant.models.Grade;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
    private List<Student> students;
    private OnStudentClickListener listener;

    public interface OnStudentClickListener { // Интерфейс кликов
        void onStudentClick(Student student);
        void onAddGradeClick(Student student);
        void onChangeColorClick(Student student);
        void onAddNoteClick(Student student);
        void onDeleteGradesClick(Student student);
        void onStudentLongClick(Student student);
    }

    public StudentAdapter(List<Student> students, OnStudentClickListener listener) {
        this.students = students;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = students.get(position);
        holder.studentName.setText(student.getFullName());

        double average = student.getAverageGrade();
        holder.averageGrade.setText("Ср. балл: " + String.format("%.1f", average));

        holder.studentNumber.setText(String.valueOf(position + 1));

        if (student.getNotes() != null && !student.getNotes().isEmpty()) { // Показать заметку
            holder.studentNotesPreview.setText(student.getNotes());
            holder.studentNotesPreview.setVisibility(View.VISIBLE);
        } else {
            holder.studentNotesPreview.setVisibility(View.GONE);
        }

        List<Grade> grades = student.getGrades();
        if (grades != null && !grades.isEmpty()) { // Показать оценки
            StringBuilder gradesText = new StringBuilder("Оценки: ");
            int count = Math.min(grades.size(), 3);
            for (int i = 0; i < count; i++) {
                int gradeValue = grades.get(i).getValue();
                if (gradeValue == 0) {
                    gradesText.append("Н");
                } else {
                    gradesText.append(gradeValue);
                }
                if (i < count - 1) gradesText.append(", ");
            }
            if (grades.size() > 3) {
                gradesText.append("...");
            }
            holder.gradesPreview.setText(gradesText.toString());
            holder.gradesPreview.setVisibility(View.VISIBLE);
        } else {
            holder.gradesPreview.setVisibility(View.GONE);
        }

        if (student.getColorCode() != 0) { // Показать цвет
            holder.colorIndicator.setBackgroundColor(student.getColorCode());
            holder.colorIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.colorIndicator.setVisibility(View.INVISIBLE);
        }

        holder.menuButton.setOnClickListener(v -> { // Кнопка меню
            PopupMenu popup = new PopupMenu(v.getContext(), holder.menuButton);
            popup.inflate(R.menu.student_menu);
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_add_grade) {
                    if (listener != null) listener.onAddGradeClick(student);
                    return true;
                } else if (id == R.id.menu_change_color) {
                    if (listener != null) listener.onChangeColorClick(student);
                    return true;
                } else if (id == R.id.menu_add_note) {
                    if (listener != null) listener.onAddNoteClick(student);
                    return true;
                } else if (id == R.id.menu_delete_grades) {
                    if (listener != null) listener.onDeleteGradesClick(student);
                    return true;
                }
                return false;
            });
            popup.show();
        });

        holder.itemView.setOnClickListener(v -> { // Клик
            if (listener != null) listener.onStudentClick(student);
        });

        holder.itemView.setOnLongClickListener(v -> { // Долгий клик
            if (listener != null) {
                listener.onStudentLongClick(student);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return students == null ? 0 : students.size();
    }

    public void updateStudents(List<Student> newStudents) { // Обновить список
        if (students != null) {
            students.clear();
            if (newStudents != null) {
                students.addAll(newStudents);
            }
            notifyDataSetChanged();
        }
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView studentName;
        TextView averageGrade;
        TextView studentNotesPreview;
        TextView gradesPreview;
        TextView studentNumber;
        View colorIndicator;
        ImageView menuButton;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.studentName);
            averageGrade = itemView.findViewById(R.id.averageGrade);
            studentNotesPreview = itemView.findViewById(R.id.studentNotesPreview);
            gradesPreview = itemView.findViewById(R.id.gradesPreview);
            studentNumber = itemView.findViewById(R.id.studentNumber);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
            menuButton = itemView.findViewById(R.id.menuButton);
        }
    }
}