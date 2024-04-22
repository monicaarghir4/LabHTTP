package ro.pub.cs.systems.eim.calculatorwebservice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ro.pub.cs.systems.eim.calculatorwebservice.ui.theme.CalculatorWebServiceTheme

class MainActivity : ComponentActivity() {
    private lateinit var calculatorService: CalculatorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a Retrofit instance to handle the communication with the web service.
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        calculatorService = retrofit.create(CalculatorService::class.java)

        // create the viewModel using the factory
        val factory = CalculatorViewModelFactory(calculatorService)
        val calculatorViewModel: CalculatorViewModel by viewModels { factory }

        setContent {
            CalculatorWebServiceTheme {
                CalculatorScreen(
                    viewModel = calculatorViewModel,
                )
            }
        }
    }

    companion object {
        private const val BASE_URL = "http://jepi.cs.pub.ro/"
    }
}
