package com.example.week12.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.week09.week12.roomDB.ItemEntity

@Composable
fun ItemList(list: List<ItemEntity>, selectedEvent: (ItemEntity) -> Unit) {
    LazyColumn {
        items(list) { item ->
            ItemUI(item, selectedEvent)
            HorizontalDivider(color = Color.Black, thickness = 2.dp)
        }
    }
}

@Composable
fun ItemUI(itemEntity: ItemEntity, selectedEvent: (ItemEntity) -> Unit) {
    Column(modifier = Modifier
        .padding(10.dp)
        .fillMaxWidth()
        .clickable{ selectedEvent(itemEntity) }
    ) {
        Text(itemEntity.itemID.toString(), fontSize = 15.sp)
        Text(itemEntity.itemName, fontSize = 15.sp)
        Text(itemEntity.itemQuantity.toString(), fontSize = 15.sp)
    }
}