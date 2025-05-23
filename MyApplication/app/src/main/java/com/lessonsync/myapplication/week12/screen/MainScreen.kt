package com.example.week12.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.week09.week12.roomDB.ItemDatabase
import com.example.week09.week12.roomDB.ItemEntity
import com.example.week12.viewmodel.ItemRepository
import com.example.week12.viewmodel.ItemViewModel
import com.example.week12.viewmodel.ItemViewModelFactory


@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val itemdb = ItemDatabase.getDBInstance(context)
    val itemViewModel: ItemViewModel = viewModel(factory = ItemViewModelFactory(ItemRepository(itemdb)))
    val itemListState by itemViewModel.itemList.collectAsState(initial = emptyList())
    var selectedItem by remember {
        mutableStateOf<ItemEntity?>(null)
    }
    val selectedEvent = {ItemEntity : ItemEntity -> selectedItem = ItemEntity }
    Column {
        InputScreen(viewModel = itemViewModel, selectedItem)
        ItemList(list = itemListState, selectedEvent)
    }
}