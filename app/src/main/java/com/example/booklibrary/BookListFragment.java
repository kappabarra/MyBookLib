package com.example.booklibrary;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class BookListFragment extends Fragment implements BookAdapter.OnBookClickListener {

    private BookViewModel viewModel;
    private BookAdapter adapter;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private TabLayout tabLayout;
    private static final int MAX_SEARCH_LEN = 50;


    // Чтобы избежать повторной подписки слушателя при пересоздании view
    private TabLayout.OnTabSelectedListener tabSelectedListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_book_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewBooks);
        searchView = view.findViewById(R.id.searchView);

        // TabLayout лежит в Activity (activity_main.xml)
        tabLayout = requireActivity().findViewById(R.id.tabLayout);

        setupRecyclerView();
        setupViewModel();
        setupSearch();
        setupTabLayout();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(BookViewModel.class);

        viewModel.getAllBooks().observe(getViewLifecycleOwner(), books -> {
            adapter.setBooks(books);
            updateEmptyState(books);
        });
    }

    private void setupSearch() {
        android.widget.EditText searchEdit =
                searchView.findViewById(androidx.appcompat.R.id.search_src_text);

        if (searchEdit != null) {
            searchEdit.setFilters(new android.text.InputFilter[] {
                    new android.text.InputFilter.LengthFilter(MAX_SEARCH_LEN)
            });
        }



        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                String q = (query == null) ? "" : query.trim();
                if (q.length() > MAX_SEARCH_LEN) q = q.substring(0, MAX_SEARCH_LEN);

                if (q.isEmpty()) {
                    viewModel.clearFilter();
                    return true;
                }
                viewModel.search(q);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String q = (newText == null) ? "" : newText.trim();
                if (q.length() > MAX_SEARCH_LEN) q = q.substring(0, MAX_SEARCH_LEN);

                if (q.isEmpty()) {
                    viewModel.clearFilter();
                } else {
                    viewModel.search(q);
                }
                return true;
            }

        });

        searchView.setOnCloseListener(() -> {
            viewModel.clearFilter();
            return false;
        });
    }

    private void setupTabLayout() {
        if (tabLayout == null) return;

        if (tabSelectedListener != null) {
            tabLayout.removeOnTabSelectedListener(tabSelectedListener);
        }

        tabSelectedListener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (searchView != null) {
                    searchView.setQuery("", false);
                    searchView.clearFocus();
                    searchView.setIconified(true);
                }

                switch (tab.getPosition()) {
                    case 0:
                        viewModel.clearFilter();
                        break;
                    case 1:
                        viewModel.filterByStatus(Book.STATUS_PLANNED);
                        break;
                    case 2:
                        viewModel.filterByStatus(Book.STATUS_READING);
                        break;
                    case 3:
                        viewModel.filterByStatus(Book.STATUS_READ);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        };

        tabLayout.addOnTabSelectedListener(tabSelectedListener);
    }

    private void updateEmptyState(List<Book> books) {
        View emptyView = requireView().findViewById(R.id.textNoBooks);

        if (books == null || books.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBookClick(Book book) {
        BookDetailActivity.start(requireContext(), book.getId());
    }

    @Override
    public void onBookLongClick(Book book) {
        Toast.makeText(getContext(), book.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
