package com.easycoding.pagination.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.paging.map
import androidx.recyclerview.widget.GridLayoutManager
import com.easycoding.pagination.R
import com.easycoding.pagination.databinding.ActivityMainBinding
import com.easycoding.pagination.presentation.adapters.v3.RecipeLoaderStateAdapter
import com.easycoding.pagination.presentation.adapters.v1.RecipeAdapter as RecipeV1Adapter
import com.easycoding.pagination.presentation.adapters.v2.RecipeAdapter as RecipeV2Adapter
import com.easycoding.pagination.presentation.adapters.v3.RecipeAdapter as RecipeV3Adapter
import com.easycoding.pagination.presentation.adapters.v1.lib.PaginationScrollListener as PaginationScrollListenerV1
import com.easycoding.pagination.presentation.adapters.v2.lib.PaginationScrollListener as PaginationScrollListenerV2
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    private val adapterV1 = RecipeV1Adapter()
    private val adapterV2 = RecipeV2Adapter()
    private val adapterV3 = RecipeV3Adapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel
        binding.rvRecipes.layoutManager = GridLayoutManager(this, 2)

        // implementV1Adapter()
        // implementV2Adapter()
        implementV3Adapter()
    }

    private fun implementV1Adapter() {
        binding.rvRecipes.adapter = adapterV1
        binding.rvRecipes.addOnScrollListener(PaginationScrollListenerV1(adapterV1) {
            viewModel.fetchRecipes()
        })

        viewModel.fetchRecipes()
        viewModel.recipes.observe(this) {
            adapterV1.pushList(it)
        }
    }

    private fun implementV2Adapter() {
        binding.rvRecipes.adapter = adapterV2
        binding.rvRecipes.addOnScrollListener(PaginationScrollListenerV2(adapterV2) {
            viewModel.fetchRecipes()
        })

        viewModel.fetchRecipes()
        viewModel.recipes.observe(this) {
            // somehow cannot cast
            adapterV2.pushList(it)
        }
    }

    private fun implementV3Adapter() {
        val loaderStateAdapter = RecipeLoaderStateAdapter { adapterV3.retry() }
        binding.rvRecipes.adapter = adapterV3.withLoadStateFooter(loaderStateAdapter)
        lifecycleScope.launch {
            viewModel.fetchRecipesForPagingAdapter().distinctUntilChanged().collectLatest {
                if (viewModel.dataLoading.value == true)
                    viewModel.setDataLoading(false)
                adapterV3.submitData(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}