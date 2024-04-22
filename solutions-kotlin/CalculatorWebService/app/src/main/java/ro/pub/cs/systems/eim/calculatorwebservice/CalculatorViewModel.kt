package ro.pub.cs.systems.eim.calculatorwebservice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response


class CalculatorViewModel(
    private val calculatorService: CalculatorService
) : ViewModel() {
    private val _result = MutableLiveData<String>()
    val result: LiveData<String> = _result

    /**
     * Calculate the result of the operation.
     *
     * @param operator1 the first operator
     * @param operator2 the second operator
     * @param operation the operation to be performed
     * @param method the method to be used (0 for GET, 1 for POST)
     */
    fun calculate(
        operator1: String,
        operator2: String,
        operation: String,
        method: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response: Response<String> = if (method == 0) {
                    calculatorService.getOperation(operation, operator1, operator2).execute()
                } else {
                    calculatorService.postOperation(operation, operator1, operator2).execute()
                }

                // Update the result LiveData with the response body or an error message.
                if (response.isSuccessful) {
                    _result.postValue(response.body() ?: "No result")
                } else {
                    _result.postValue("Error: ${response.message()}")
                }
            } catch (e: Exception) {
                _result.postValue("Failed to fetch data: ${e.message}")
            }
        }
    }
}

/**
 * Factory for creating a [CalculatorViewModel] with a constructor that takes a [CalculatorService].
 */
class CalculatorViewModelFactory(
    private val calculatorService: CalculatorService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            return CalculatorViewModel(calculatorService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


