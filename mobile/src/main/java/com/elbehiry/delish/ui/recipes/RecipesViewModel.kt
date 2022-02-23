/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elbehiry.delish.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elbehiry.delish.ui.util.WhileViewSubscribed
import com.elbehiry.model.CuisineItem
import com.elbehiry.model.IngredientItem
import com.elbehiry.model.RecipesItem
import com.elbehiry.shared.domain.recipes.bookmark.DeleteRecipeSuspendUseCase
import com.elbehiry.shared.domain.recipes.bookmark.IsRecipeSavedSuspendUseCase
import com.elbehiry.shared.domain.recipes.bookmark.SaveRecipeSuspendUseCase
import com.elbehiry.shared.domain.recipes.cuisines.GetAvailableCuisinesUseCase
import com.elbehiry.shared.domain.recipes.ingredients.GetIngredientsUseCase
import com.elbehiry.shared.domain.recipes.random.GetRandomRecipesUseCase
import com.elbehiry.shared.result.data
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val randomRecipesCount = 20

@HiltViewModel
class RecipesViewModel @Inject constructor(
    getRandomRecipesUseCase: GetRandomRecipesUseCase,
    getAvailableCuisinesUseCase: GetAvailableCuisinesUseCase,
    getIngredientsUseCase: GetIngredientsUseCase,
    private val saveRecipeUseCase: SaveRecipeSuspendUseCase,
    private val deleteRecipeUseCase: DeleteRecipeSuspendUseCase,
    private val isRecipeSavedUseCase: IsRecipeSavedSuspendUseCase
) : ViewModel() {

    private val hasError = MutableStateFlow(false)
    private val loading = MutableStateFlow(false)

    val viewState: StateFlow<RecipesViewState> = combine(
        getIngredientsUseCase(Unit).map { it.data ?: emptyList() },
        getAvailableCuisinesUseCase(Unit).map { it.data ?: emptyList() },
        getRandomRecipesUseCase(
            GetRandomRecipesUseCase.Params.create(null, randomRecipesCount)
        ).map { it.data ?: emptyList() },
        loading,
        hasError,
        ::RecipesViewState
    ).catch {
        hasError.value = true
    }.onCompletion {
        loading.value = false
    }.stateIn(
        scope = viewModelScope,
        started = WhileViewSubscribed,
        initialValue = RecipesViewState.Empty,
    )

    fun onBookMark(recipesItem: RecipesItem) {
        viewModelScope.launch {
            if (isRecipeSavedUseCase(recipesItem.id).data == true) {
                deleteRecipeUseCase(recipesItem.id)
            } else {
                saveRecipeUseCase(recipesItem)
            }
        }
    }
}

data class RecipesViewState(
    val ingredientList: List<IngredientItem> = emptyList(),
    val cuisinesList: List<CuisineItem> = emptyList(),
    val randomRecipes: List<RecipesItem> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false
) {
    companion object {
        val Empty = RecipesViewState()
    }
}

