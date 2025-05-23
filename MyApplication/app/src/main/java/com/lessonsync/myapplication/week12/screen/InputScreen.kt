package com.example.week12.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.week09.week12.roomDB.ItemEntity
import com.example.week12.viewmodel.ItemViewModel

@Composable
fun InputScreen(viewModel: ItemViewModel, selectedItem: ItemEntity?) {

    var itemId by remember {
        mutableStateOf("")
    }

    var itemName by remember {
        mutableStateOf("")
    }
    var itemQuantity by remember {
        mutableStateOf("")
    }

    fun buildItem(): ItemEntity {
        val id = itemId.toIntOrNull() ?: 0
        val quantity = itemQuantity.toIntOrNull() ?: 0
        return ItemEntity(itemName, quantity, id)
    }

    fun clearText() {
        itemName = ""
        itemQuantity = ""
        itemId = ""
    }

    LaunchedEffect(selectedItem) {
        if(selectedItem != null) {
            itemId = selectedItem.itemID.toString()
            itemName = selectedItem.itemName
            itemQuantity = selectedItem.itemQuantity.toString()

        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = itemId,
            onValueChange = { itemId = it },
            label = { Text("Item ID") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = itemName,
            onValueChange = { itemName = it },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = itemQuantity,
            onValueChange = { itemQuantity = it },
            label = { Text("Item Quantity") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val item = buildItem()
                viewModel.InsertItem(item)
                clearText()
            }) {
                Text("Insert")
            }
            Button(onClick = {
                val item = buildItem()
                viewModel.UpdateItem(item)
                clearText()
            }) {
                Text("Update")
            }
            Button(onClick = {
                val item = buildItem()
                viewModel.DeleteItem(item)
                clearText()
            }) {
                Text("delete")
            }
            Button(onClick = {
                viewModel.getItems("$itemName%")
                clearText()
            }) {
                Text("find")
            }
        }
    }
}