package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.CafeGameScreen
import com.example.ui.theme.CozyCafeTheme
import com.example.viewmodel.CafeGameViewModel

class MainActivity : ComponentActivity() {
  private val gameViewModel: CafeGameViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      CozyCafeTheme {
        CafeGameScreen(viewModel = gameViewModel)
      }
    }
  }
}
