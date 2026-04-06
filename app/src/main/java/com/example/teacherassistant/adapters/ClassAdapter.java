package com.example.teacherassistant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacherassistant.R;
import com.example.teacherassistant.models.SchoolClass;

import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {
    private List<SchoolClass> classes;
    private OnClassClickListener listener;

    public interface OnClassClickListener { // Интерфейс кликов
        void onClassClick(SchoolClass schoolClass);
        void onClassLongClick(SchoolClass schoolClass);
    }

    public ClassAdapter(List<SchoolClass> classes, OnClassClickListener listener) {
        this.classes = classes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        SchoolClass schoolClass = classes.get(position);
        holder.className.setText(schoolClass.getClassName());
        holder.subject.setText(schoolClass.getSubject());

        int studentCount = schoolClass.getStudents() != null ? schoolClass.getStudents().size() : 0;
        holder.studentCount.setText("Учеников: " + studentCount);

        if (schoolClass.getNotes() != null && !schoolClass.getNotes().isEmpty()) { // Показать заметку
            String notes = schoolClass.getNotes();
            if (notes.length() > 50) {
                notes = notes.substring(0, 47) + "...";
            }
            holder.classNotesPreview.setText(notes);
            holder.classNotesPreview.setVisibility(View.VISIBLE);
        } else {
            holder.classNotesPreview.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> { // Клик
            if (listener != null) listener.onClassClick(schoolClass);
        });

        holder.itemView.setOnLongClickListener(v -> { // Долгий клик
            if (listener != null) {
                listener.onClassLongClick(schoolClass);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return classes == null ? 0 : classes.size();
    }

    public void updateClasses(List<SchoolClass> newClasses) { // Обновить список
        if (classes != null) {
            classes.clear();
            if (newClasses != null) {
                classes.addAll(newClasses);
            }
            notifyDataSetChanged();
        }
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView className;
        TextView subject;
        TextView studentCount;
        TextView classNotesPreview;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            className = itemView.findViewById(R.id.className);
            subject = itemView.findViewById(R.id.subject);
            studentCount = itemView.findViewById(R.id.studentCount);
            classNotesPreview = itemView.findViewById(R.id.classNotesPreview);
        }
    }
}