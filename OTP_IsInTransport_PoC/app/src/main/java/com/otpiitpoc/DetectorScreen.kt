package com.otpiitpoc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DetectorScreen(viewModel: DetectorViewModel) {
    val currentState by viewModel.state.collectAsState()

    val (color, text, icon) = when (currentState) {
        TransportState.EXTERIOR -> Triple(
            Color.Gray,
            "EXTERIOR",
            Icons.Default.Public
        )
        TransportState.AT_STATION -> Triple(
            Color(0xFFFFA500),
            "AT_STATION",
            Icons.Default.LocationOn
        )
        TransportState.IN_TRANSIT -> Triple(
            Color.Green,
            "IN_TRANSIT",
            Icons.Default.DirectionsBus
        )
        TransportState.DESTINATION_REACHED -> Triple(
            Color.Blue,
            "POSSIBLE_DESTINATION_REACHED",
            Icons.Default.DirectionsWalk
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = color
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))

    }
}
