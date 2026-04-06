package com.example.teacherassistant.models;

import java.util.Date;

public class Grade {
    private int id; // ID
    private int studentId; // ID ученика
    private int value; // Оценка
    private Date date; // Дата
    private String subject; // Предмет

    public Grade() {}

    public Grade(int studentId, int value, String subject) {
        this.studentId = studentId;
        this.value = value;
        this.subject = subject;
        this.date = new Date();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
}