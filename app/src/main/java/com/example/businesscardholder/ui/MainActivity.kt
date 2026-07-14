package com.example.businesscardholder.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.businesscardholder.data.AppDatabase
import com.example.businesscardholder.data.BusinessCard
import com.example.businesscardholder.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val dao by lazy { AppDatabase.getInstance(this).businessCardDao() }
    private lateinit var adapter: CardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = CardAdapter { card -> openDetail(card) }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddCardActivity::class.java))
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadCards(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadCards("")
    }

    private var loadJob: kotlinx.coroutines.Job? = null

    private fun loadCards(query: String) {
        loadJob?.cancel()
        val flow = if (query.isBlank()) dao.getAll() else dao.search(query)
        loadJob = lifecycleScope.launch {
            flow.collect { list ->
                adapter.submitList(list)
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun openDetail(card: BusinessCard) {
        val intent = Intent(this, CardDetailActivity::class.java)
        intent.putExtra(CardDetailActivity.EXTRA_CARD_ID, card.id)
        startActivity(intent)
    }
}
