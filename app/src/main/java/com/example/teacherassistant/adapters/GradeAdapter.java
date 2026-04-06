package com.example.teacherassistant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacherassistant.R;
import com.example.teacherassistant.models.Grade;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.GradeViewHolder> {
    private List<Grade> grades;
    private OnGradeClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public interface OnGradeClickListener { // Интерфейс кликов
        void onGradeLongClick(Grade grade);
    }

    public GradeAdapter(List<Grade> grades) {
        this.grades = grades;
    }

    public GradeAdapter(List<Grade> grades, OnGradeClickListener listener) {
        this.grades = grades;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grade, parent, false);
        return new GradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
        Grade grade = grades.get(position);

        if (grade.getValue() == 0) { // Отобразить "Н"
            holder.tvGradeValue.setText("Н");
        } else {
            holder.tvGradeValue.setText(String.valueOf(grade.getValue()));
        }

        holder.tvGradeSubject.setText(grade.getSubject());
        holder.tvGradeDate.setText(dateFormat.format(grade.getDate()));

        if (listener != null) { // Долгий клик
            holder.itemView.setOnLongClickListener(v -> {
                listener.onGradeLongClick(grade);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return grades.size();
    }

    public void updateGrades(List<Grade> newGrades) { // Обновить список
        grades = newGrades;
        notifyDataSetChanged();
    }

    static class GradeViewHolder extends RecyclerView.ViewHolder {
        TextView tvGradeValue;
        TextView tvGradeSubject;
        TextView tvGradeDate;

        public GradeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGradeValue = itemView.findViewById(R.id.tvGradeValue);
            tvGradeSubject = itemView.findViewById(R.id.tvGradeSubject);
            tvGradeDate = itemView.findViewById(R.id.tvGradeDate);
        }
    }
}