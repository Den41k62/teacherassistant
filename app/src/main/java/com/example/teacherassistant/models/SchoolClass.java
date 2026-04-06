package com.example.teacherassistant.models;

import java.util.ArrayList;
import java.util.List;

public class SchoolClass {
    private int id; // ID
    private String className; // Название
    private String subject; // Предмет
    private String notes; // Заметки
    private List<Student> students; // Ученики

    public SchoolClass() {
        students = new ArrayList<>();
    }

    public SchoolClass(String className, String subject) {
        this.className = className;
        this.subject = subject;
        this.students = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<Student> getStudents() { return students; }
    public void setStudents(List<Student> students) { this.students = students; }
}