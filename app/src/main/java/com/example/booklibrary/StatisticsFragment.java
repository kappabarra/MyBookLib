package com.example.booklibrary;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class StatisticsFragment extends Fragment {

    private TextView textReadThisYear, textYearProgress, textTotalReadCount,
            textReadingNowCount, textPlannedCount, textAverageRating,
            textTotalBooks, textNoRecentBooks;
    private ProgressBar progressBarYear;
    private RecyclerView recyclerViewRecentBooks;
    private RecentBooksAdapter recentBooksAdapter;
    private int yearGoal;


    private View cardYearProgress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // Инициализация UI элементов
        textReadThisYear = view.findViewById(R.id.textReadThisYear);
        textYearProgress = view.findViewById(R.id.textYearProgress);
        textTotalReadCount = view.findViewById(R.id.textTotalReadCount);
        textReadingNowCount = view.findViewById(R.id.textReadingNowCount);
        textPlannedCount = view.findViewById(R.id.textPlannedCount);
        textAverageRating = view.findViewById(R.id.textAverageRating);
        textTotalBooks = view.findViewById(R.id.textTotalBooks);
        progressBarYear = view.findViewById(R.id.progressBarYear);
        textNoRecentBooks = view.findViewById(R.id.textNoRecentBooks);
        recyclerViewRecentBooks = view.findViewById(R.id.recyclerViewRecentBooks);
        cardYearProgress = view.findViewById(R.id.cardYearProgress); // id зададим чуть ниже
        int goal = GoalPrefs.getYearGoal(requireContext());
        textYearProgress.setText("0 / " + goal + " книг"); // временно, до прихода данных

        cardYearProgress.setOnClickListener(v -> showGoalDialog());


        setupRecyclerView();
        setupViewModel();

        return view;
    }

    private void setupRecyclerView() {
        recyclerViewRecentBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        recentBooksAdapter = new RecentBooksAdapter();
        recyclerViewRecentBooks.setAdapter(recentBooksAdapter);
    }

    @SuppressLint("NewApi")
    private void setupViewModel() {
        BookViewModel viewModel = new ViewModelProvider(requireActivity()).get(BookViewModel.class);

        yearGoal = GoalPrefs.getYearGoal(requireContext());

        viewModel.getReadBooksCountForYear(String.valueOf(java.time.Year.now().getValue()))
                .observe(getViewLifecycleOwner(), count -> {
                    textReadThisYear.setText(String.valueOf(count));
                    progressBarYear.setMax(yearGoal);
                    progressBarYear.setProgress(count);
                    textYearProgress.setText(count + " / " + yearGoal + " книг");
                });

        viewModel.getTotalReadBooks().observe(getViewLifecycleOwner(), count ->
                textTotalReadCount.setText(String.valueOf(count)));

        viewModel.getTotalReadingBooks().observe(getViewLifecycleOwner(), count ->
                textReadingNowCount.setText(String.valueOf(count)));

        viewModel.getTotalPlannedBooks().observe(getViewLifecycleOwner(), count ->
                textPlannedCount.setText(String.valueOf(count)));

        viewModel.getAverageRating().observe(getViewLifecycleOwner(), rating ->
                textAverageRating.setText(String.format("%.1f", rating)));

        viewModel.getTotalBooks().observe(getViewLifecycleOwner(), total ->
                textTotalBooks.setText(String.valueOf(total)));

        viewModel.getRecentReadBooks(5).observe(getViewLifecycleOwner(), books -> {
            if (books != null && !books.isEmpty()) {
                recentBooksAdapter.setBooks(books);
                textNoRecentBooks.setVisibility(View.GONE);
                recyclerViewRecentBooks.setVisibility(View.VISIBLE);
            } else {
                textNoRecentBooks.setVisibility(View.VISIBLE);
                recyclerViewRecentBooks.setVisibility(View.GONE);
            }
        });
    }

    private void showGoalDialog() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_year_goal, null);

        EditText editGoal = dialogView.findViewById(R.id.editGoal);

        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .setPositiveButton("Сохранить", null)
                        .setNegativeButton("Отмена", (d, w) -> d.dismiss())
                        .create();

        dialog.setOnShowListener(d -> {
            android.widget.Button btn = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            btn.setTextColor(getResources().getColor(R.color.ocean_blue));

            btn.setOnClickListener(v -> {
                String text = editGoal.getText().toString().trim();
                if (text.isEmpty()) {
                    editGoal.setError("Введите число");
                    return;
                }
                int newGoal;
                try {
                    newGoal = Integer.parseInt(text);
                } catch (Exception e) {
                    editGoal.setError("Некорректное число");
                    return;
                }
                if (newGoal <= 0) {
                    editGoal.setError("Цель должна быть больше нуля");
                    return;
                }

                GoalPrefs.setYearGoal(requireContext(), newGoal);
                yearGoal = newGoal; // важно
                progressBarYear.setMax(newGoal);


                // обновляем UI
                progressBarYear.setMax(newGoal);
                int count = 0;
                try { count = Integer.parseInt(textReadThisYear.getText().toString()); } catch (Exception ignored) {}
                textYearProgress.setText(count + " / " + newGoal + " книг");

                dialog.dismiss();
            });
        });

        dialog.show();
    }


    @Override
    public void onResume() {
        super.onResume();
        // всё обновляется через LiveData, отдельная загрузка не нужна
    }
}
