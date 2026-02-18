package com.example.teacherassistant.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.teacherassistant.models.SchoolClass;
import com.example.teacherassistant.models.Student;
import com.example.teacherassistant.models.Grade;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TeacherAssistant.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_CLASSES = "classes";
    private static final String COLUMN_CLASS_ID = "class_id";
    private static final String COLUMN_CLASS_NAME = "class_name";
    private static final String COLUMN_SUBJECT = "subject";
    private static final String COLUMN_CLASS_NOTES = "class_notes";

    private static final String TABLE_STUDENTS = "students";
    private static final String COLUMN_STUDENT_ID = "student_id";
    private static final String COLUMN_FULL_NAME = "full_name";
    private static final String COLUMN_STUDENT_NOTES = "student_notes";
    private static final String COLUMN_COLOR_CODE = "color_code";
    private static final String COLUMN_COLOR_LABEL = "color_label";

    private static final String TABLE_GRADES = "grades";
    private static final String COLUMN_GRADE_ID = "grade_id";
    private static final String COLUMN_GRADE_VALUE = "grade_value";
    private static final String COLUMN_GRADE_DATE = "grade_date";
    private static final String COLUMN_GRADE_SUBJECT = "grade_subject";

    private static DatabaseHelper instance;
    private SQLiteDatabase database;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createClassTable = "CREATE TABLE " + TABLE_CLASSES + "(" +
                COLUMN_CLASS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_CLASS_NAME + " TEXT," +
                COLUMN_SUBJECT + " TEXT," +
                COLUMN_CLASS_NOTES + " TEXT)";

        String createStudentTable = "CREATE TABLE " + TABLE_STUDENTS + "(" +
                COLUMN_STUDENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_CLASS_ID + " INTEGER," +
                COLUMN_FULL_NAME + " TEXT," +
                COLUMN_STUDENT_NOTES + " TEXT," +
                COLUMN_COLOR_CODE + " INTEGER," +
                COLUMN_COLOR_LABEL + " TEXT)";

        String createGradeTable = "CREATE TABLE " + TABLE_GRADES + "(" +
                COLUMN_GRADE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_STUDENT_ID + " INTEGER," +
                COLUMN_GRADE_VALUE + " INTEGER," +
                COLUMN_GRADE_DATE + " INTEGER," +
                COLUMN_GRADE_SUBJECT + " TEXT)";

        db.execSQL(createClassTable);
        db.execSQL(createStudentTable);
        db.execSQL(createGradeTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
        }
    }

    public synchronized SQLiteDatabase openDatabase() {
        if (database == null || !database.isOpen()) {
            database = this.getWritableDatabase();
        }
        return database;
    }

    public synchronized void closeDatabase() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    public long addClass(SchoolClass schoolClass) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASS_NAME, schoolClass.getClassName());
        values.put(COLUMN_SUBJECT, schoolClass.getSubject());
        values.put(COLUMN_CLASS_NOTES, schoolClass.getNotes());

        long result = db.insert(TABLE_CLASSES, null, values);
        return result;
    }

    public List<SchoolClass> getAllClasses() {
        List<SchoolClass> classes = new ArrayList<>();
        SQLiteDatabase db = openDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_CLASSES, null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    SchoolClass schoolClass = new SchoolClass();
                    schoolClass.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_CLASS_ID)));
                    schoolClass.setClassName(cursor.getString(cursor.getColumnIndex(COLUMN_CLASS_NAME)));
                    schoolClass.setSubject(cursor.getString(cursor.getColumnIndex(COLUMN_SUBJECT)));
                    schoolClass.setNotes(cursor.getString(cursor.getColumnIndex(COLUMN_CLASS_NOTES)));
                    classes.add(schoolClass);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return classes;
    }

    public void updateClass(SchoolClass schoolClass) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASS_NAME, schoolClass.getClassName());
        values.put(COLUMN_SUBJECT, schoolClass.getSubject());
        values.put(COLUMN_CLASS_NOTES, schoolClass.getNotes());

        db.update(TABLE_CLASSES, values, COLUMN_CLASS_ID + " = ?",
                new String[]{String.valueOf(schoolClass.getId())});
    }

    public void deleteClass(int classId) {
        SQLiteDatabase db = openDatabase();

        List<Student> students = getStudentsByClass(classId);
        for (Student student : students) {
            deleteStudent(student.getId());
        }

        db.delete(TABLE_CLASSES, COLUMN_CLASS_ID + " = ?",
                new String[]{String.valueOf(classId)});
    }

    public long addStudent(Student student) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASS_ID, student.getClassId());
        values.put(COLUMN_FULL_NAME, student.getFullName());
        values.put(COLUMN_STUDENT_NOTES, student.getNotes());
        values.put(COLUMN_COLOR_CODE, student.getColorCode());
        values.put(COLUMN_COLOR_LABEL, student.getColorLabel());

        long result = db.insert(TABLE_STUDENTS, null, values);
        return result;
    }

    public List<Student> getStudentsByClass(int classId) {
        List<Student> students = new ArrayList<>();
        SQLiteDatabase db = openDatabase();
        Cursor cursor = null;

        try {
            String selection = COLUMN_CLASS_ID + " = ?";
            String[] selectionArgs = {String.valueOf(classId)};

            cursor = db.query(TABLE_STUDENTS, null, selection, selectionArgs,
                    null, null, COLUMN_FULL_NAME + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Student student = new Student();
                    student.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_STUDENT_ID)));
                    student.setClassId(cursor.getInt(cursor.getColumnIndex(COLUMN_CLASS_ID)));
                    student.setFullName(cursor.getString(cursor.getColumnIndex(COLUMN_FULL_NAME)));
                    student.setNotes(cursor.getString(cursor.getColumnIndex(COLUMN_STUDENT_NOTES)));
                    student.setColorCode(cursor.getInt(cursor.getColumnIndex(COLUMN_COLOR_CODE)));
                    student.setColorLabel(cursor.getString(cursor.getColumnIndex(COLUMN_COLOR_LABEL)));

                    student.setGrades(getGradesByStudent(student.getId()));
                    students.add(student);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return students;
    }

    public void updateStudent(Student student) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FULL_NAME, student.getFullName());
        values.put(COLUMN_STUDENT_NOTES, student.getNotes());
        values.put(COLUMN_COLOR_CODE, student.getColorCode());
        values.put(COLUMN_COLOR_LABEL, student.getColorLabel());

        db.update(TABLE_STUDENTS, values, COLUMN_STUDENT_ID + " = ?",
                new String[]{String.valueOf(student.getId())});
    }

    public void deleteStudent(int studentId) {
        SQLiteDatabase db = openDatabase();

        db.delete(TABLE_GRADES, COLUMN_STUDENT_ID + " = ?",
                new String[]{String.valueOf(studentId)});

        db.delete(TABLE_STUDENTS, COLUMN_STUDENT_ID + " = ?",
                new String[]{String.valueOf(studentId)});
    }

    public long addGrade(Grade grade) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STUDENT_ID, grade.getStudentId());
        values.put(COLUMN_GRADE_VALUE, grade.getValue());
        values.put(COLUMN_GRADE_DATE, grade.getDate().getTime());
        values.put(COLUMN_GRADE_SUBJECT, grade.getSubject());

        long result = db.insert(TABLE_GRADES, null, values);
        return result;
    }

    public List<Grade> getGradesByStudent(int studentId) {
        List<Grade> grades = new ArrayList<>();
        SQLiteDatabase db = openDatabase();
        Cursor cursor = null;

        try {
            String selection = COLUMN_STUDENT_ID + " = ?";
            String[] selectionArgs = {String.valueOf(studentId)};

            cursor = db.query(TABLE_GRADES, null, selection, selectionArgs,
                    null, null, COLUMN_GRADE_DATE + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Grade grade = new Grade();
                    grade.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_GRADE_ID)));
                    grade.setStudentId(cursor.getInt(cursor.getColumnIndex(COLUMN_STUDENT_ID)));
                    grade.setValue(cursor.getInt(cursor.getColumnIndex(COLUMN_GRADE_VALUE)));
                    grade.setDate(new java.util.Date(cursor.getLong(cursor.getColumnIndex(COLUMN_GRADE_DATE))));
                    grade.setSubject(cursor.getString(cursor.getColumnIndex(COLUMN_GRADE_SUBJECT)));
                    grades.add(grade);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return grades;
    }

    public void deleteGrade(int gradeId) {
        SQLiteDatabase db = openDatabase();
        db.delete(TABLE_GRADES, COLUMN_GRADE_ID + " = ?",
                new String[]{String.valueOf(gradeId)});
    }

    public void deleteAllGradesForStudent(int studentId) {
        SQLiteDatabase db = openDatabase();
        db.delete(TABLE_GRADES, COLUMN_STUDENT_ID + " = ?",
                new String[]{String.valueOf(studentId)});
    }

    public Grade getGradeById(int gradeId) {
        SQLiteDatabase db = openDatabase();
        Cursor cursor = null;
        Grade grade = null;

        try {
            String selection = COLUMN_GRADE_ID + " = ?";
            String[] selectionArgs = {String.valueOf(gradeId)};

            cursor = db.query(TABLE_GRADES, null, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                grade = new Grade();
                grade.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_GRADE_ID)));
                grade.setStudentId(cursor.getInt(cursor.getColumnIndex(COLUMN_STUDENT_ID)));
                grade.setValue(cursor.getInt(cursor.getColumnIndex(COLUMN_GRADE_VALUE)));
                grade.setDate(new java.util.Date(cursor.getLong(cursor.getColumnIndex(COLUMN_GRADE_DATE))));
                grade.setSubject(cursor.getString(cursor.getColumnIndex(COLUMN_GRADE_SUBJECT)));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return grade;
    }

    public SchoolClass getClassById(int classId) {
        SQLiteDatabase db = openDatabase();
        Cursor cursor = null;
        SchoolClass schoolClass = null;

        try {
            String selection = COLUMN_CLASS_ID + " = ?";
            String[] selectionArgs = {String.valueOf(classId)};

            cursor = db.query(TABLE_CLASSES, null, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                schoolClass = new SchoolClass();
                schoolClass.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_CLASS_ID)));
                schoolClass.setClassName(cursor.getString(cursor.getColumnIndex(COLUMN_CLASS_NAME)));
                schoolClass.setSubject(cursor.getString(cursor.getColumnIndex(COLUMN_SUBJECT)));
                schoolClass.setNotes(cursor.getString(cursor.getColumnIndex(COLUMN_CLASS_NOTES)));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return schoolClass;
    }

    public Student getStudentById(int studentId) {
        SQLiteDatabase db = openDatabase();
        Cursor cursor = null;
        Student student = null;

        try {
            String selection = COLUMN_STUDENT_ID + " = ?";
            String[] selectionArgs = {String.valueOf(studentId)};

            cursor = db.query(TABLE_STUDENTS, null, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                student = new Student();
                student.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_STUDENT_ID)));
                student.setClassId(cursor.getInt(cursor.getColumnIndex(COLUMN_CLASS_ID)));
                student.setFullName(cursor.getString(cursor.getColumnIndex(COLUMN_FULL_NAME)));
                student.setNotes(cursor.getString(cursor.getColumnIndex(COLUMN_STUDENT_NOTES)));
                student.setColorCode(cursor.getInt(cursor.getColumnIndex(COLUMN_COLOR_CODE)));
                student.setColorLabel(cursor.getString(cursor.getColumnIndex(COLUMN_COLOR_LABEL)));
                student.setGrades(getGradesByStudent(studentId));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return student;
    }
}