package io.github.abhishekabhi789.mdnshelper.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.abhishekabhi789.mdnshelper.ui.theme.MDNSHelperTheme
import io.github.abhishekabhi789.mdnshelper.utils.UrlUtils
import io.github.abhishekabhi789.mdnshelper.viewmodel.ShortcutHandleViewmodel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShortcutHandleActivity @Inject constructor() : ComponentActivity() {
    private val viewModel: ShortcutHandleViewmodel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.processShortcutAction(intent)
        lifecycleScope.launch {
            viewModel.currentAddress.collectLatest { value ->
                if (value != null) {
                    val url = UrlUtils.addressAsUrl(value)
                    UrlUtils.openWithBrowser(context = this@ShortcutHandleActivity, url)
                }
            }
        }
        lifecycleScope.launch {
            viewModel.errorMessage.collectLatest { errorMsg ->
                Log.e(TAG, "onCreate: error $errorMsg")
                val toast = Toast(this@ShortcutHandleActivity)
                toast.setText(errorMsg)
                toast.show()
                delay(toast.duration.toLong())
                finish()
            }
        }
        setContent {
            MDNSHelperTheme {
                Surface {
                    CardDefaults.let { card ->
                        BasicAlertDialog(
                            onDismissRequest = { finish() },
                            modifier = Modifier
                                .background(card.cardColors().containerColor, card.shape)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Resolving device address")
                                Button(onClick = { finish() }) {
                                    Text(text = "Cancel")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onResume: closing activity")
        viewModel.terminateBackgroundProcess()
        finish()
    }

    companion object {
        private const val TAG = "ShortcutHandleActivity"
    }
}
