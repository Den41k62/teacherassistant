package com.example.teacherassistant.models;

import java.util.ArrayList;
import java.util.List;

public class Student {
    private int id; // ID
    private int classId; // ID класса
    private String fullName; // ФИО
    private String notes; // Заметки
    private int colorCode; // Цвет
    private String colorLabel; // Название цвета
    private List<Grade> grades; // Оценки

    public void updateStudentData(String notes, int colorCode, String colorLabel) { // Обновить данные
        this.notes = notes;
        this.colorCode = colorCode;
        this.colorLabel = colorLabel;
    }

    public Student() {
        grades = new ArrayList<>();
    }

    public void updateFrom(Student other) { // Копировать
        this.fullName = other.fullName;
        this.notes = other.notes;
        this.colorCode = other.colorCode;
        this.colorLabel = other.colorLabel;
        this.grades = other.grades;
    }

    public Student(String fullName, int classId) {
        this.fullName = fullName;
        this.classId = classId;
        this.grades = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getColorCode() { return colorCode; }
    public void setColorCode(int colorCode) { this.colorCode = colorCode; }

    public String getColorLabel() { return colorLabel; }
    public void setColorLabel(String colorLabel) { this.colorLabel = colorLabel; }

    public List<Grade> getGrades() { return grades; }
    public void setGrades(List<Grade> grades) { this.grades = grades; }

    public double getAverageGrade() { // Средний балл
        if (grades.isEmpty()) return 0;
        double sum = 0;
        int count = 0;

        for (Grade grade : grades) {
            if (grade.getValue() > 0) {
                sum += grade.getValue();
                count++;
            }
        }

        if (count == 0) return 0;
        return sum / count;
    }
}